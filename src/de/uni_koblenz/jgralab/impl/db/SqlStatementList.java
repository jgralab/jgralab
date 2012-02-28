/*
* JGraLab - The Java Graph Laboratory
*
* Copyright (C) 2006-2012 Institute for Software Technology
*                         University of Koblenz-Landau, Germany
*                         ist@uni-koblenz.de
*
* For bug reports, documentation and further information, visit
*
*                         https://github.com/jgralab/jgralab
*
* This program is free software; you can redistribute it and/or modify it
* under the terms of the GNU General Public License as published by the
* Free Software Foundation; either version 3 of the License, or (at your
* option) any later version.
*
* This program is distributed in the hope that it will be useful, but
* WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
* Public License for more details.
*
* You should have received a copy of the GNU General Public License along
* with this program; if not, see <http://www.gnu.org/licenses>.
*
* Additional permission under GNU GPL version 3 section 7
*
* If you modify this Program, or any covered work, by linking or combining
* it with Eclipse (or a modified version of that program or an Eclipse
* plugin), containing parts covered by the terms of the Eclipse Public
* License (EPL), the licensors of this Program grant you additional
* permission to convey the resulting work.  Corresponding Source for a
* non-source form of such a combination shall include the source code for
* the parts of JGraLab used as well as that of the covered work.
*/
package de.uni_koblenz.jgralab.impl.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.schema.Schema;

public abstract class SqlStatementList {

	public static final String QUOTE = "$$\"$$";
	public static final String EOQ = "$$;$$";
	public static final String DIRECTION_TYPE = "$$?DIR$$";

	public static final String TABLE_SCHEMA = "GRAPH_SCHEMA";
	public static final String COLUMN_SCHEMA_ID = "SID";
	public static final String COLUMN_SCHEMA_PACKAGE_PREFIX = "PP";
	public static final String COLUMN_SCHEMA_NAME = "NAME";
	public static final String COLUMN_SCHEMA_TG = "TG";
	public static final String PRIMARY_KEY_SCHEMA = "PK_s";

	public static final String TABLE_TYPE = "TYPE";
	public static final String COLUMN_TYPE_ID = "TID";
	public static final String COLUMN_TYPE_QNAME = "QNAME";
	public static final String PRIMARY_KEY_TYPE = "PK_T";

	public static final String TABLE_GRAPH = "GRAPH";
	public static final String COLUMN_GRAPH_ID = "GID";
	public static final String COLUMN_GRAPH_UID = "UID";
	public static final String COLUMN_GRAPH_VERSION = "GV";
	public static final String COLUMN_GRAPH_VSEQ_VERSION = "VSV";
	public static final String COLUMN_GRAPH_ESEQ_VERSION = "ESV";
	public static final String PRIMARY_KEY_GRAPH = "PK_G";

	public static final String TABLE_VERTEX = "VERTEX";
	public static final String COLUMN_VERTEX_ID = "VID";
	public static final String COLUMN_VERTEX_LAMBDA_SEQ_VERSION = "LSV";
	public static final String COLUMN_SEQUENCE_NUMBER = "SEQ";
	public static final String PRIMARY_KEY_VERTEX = "PK_V";
	public static final String FOREIGN_KEY_VERTEX_TO_GRAPH = "FK_V_GID";
	public static final String FOREIGN_KEY_VERTEX_TO_TYPE = "FK_V_TID";

	public static final String TABLE_EDGE = "EDGE";
	public static final String COLUMN_EDGE_ID = "EID";
	public static final String PRIMARY_KEY_EDGE = "PK_E";
	public static final String FOREIGN_KEY_EDGE_TO_GRAPH = "FK_E_GID";
	public static final String FOREIGN_KEY_EDGE_TO_TYPE = "FK_E_TID";

	public static final String TABLE_INCIDENCE = "INCIDENCE";
	public static final String COLUMN_INCIDENCE_DIRECTION = "DIR";
	public static final String PRIMARY_KEY_INCIDENCE = "PK_I";
	public static final String FOREIGN_KEY_INCIDENCE_TO_GRAPH = "FK_I_GID";
	public static final String FOREIGN_KEY_INCIDENCE_TO_EDGE = "FK_I_EID";
	public static final String FOREIGN_KEY_INCIDENCE_TO_VERTEX = "FK_I_VID";
	public static final String INDEX_INCIDENCE_LAMBDA_SEQ = "LSEQ";

	public static final String TABLE_ATTRIBUTE = "ATTRIBUTE";
	public static final String COLUMN_ATTRIBUTE_ID = "AID";
	public static final String COLUMN_ATTRIBUTE_NAME = "NAME";
	public static final String PRIMARY_KEY_ATTRIBUTE = "PK_A";

	public static final String TABLE_GRAPH_ATTRIBUTE = "GRAPH_ATTRIBUTE";
	public static final String COLUMN_ATTRIBUTE_VALUE = "VAL";
	public static final String PRIMARY_KEY_GRAPH_ATTRIBUTE = "PK_GA";

	public static final String TABLE_VERTEX_ATTRIBUTE = "VERTEX_ATTRIBUTE";
	public static final String PRIMARY_KEY_VERTEX_ATTRIBUTE = "PK_VA";
	public static final String FOREIGN_KEY_VERTEX_ATTRIBUTE_TO_ATTRIBUTE = "FK_VA_AID";
	public static final String FOREIGN_KEY_VERTEX_ATTRIBUTE_TO_GRAPH = "FK_VA_GID";
	public static final String FOREIGN_KEY_VERTEX_ATTRIBUTE_TO_VERTEX = "FK_VA_VID";

	public static final String TABLE_EDGE_ATTRIBUTE = "EDGE_ATTRIBUTE";
	public static final String PRIMARY_KEY_EDGE_ATTRIBUTE = "PK_EA";
	public static final String FOREIGN_KEY_EDGE_ATTRIBUTE_TO_ATTRIBUTE = "FK_EA_AID";
	public static final String FOREIGN_KEY_EDGE_ATTRIBUTE_TO_GRAPH = "FK_EA_GID";
	public static final String FOREIGN_KEY_EDGE_ATTRIBUTE_TO_EDGE = "FK_EA_EID";

	/**
	 * Connection to database.
	 */
	protected Connection connection;

	/**
	 * Graph database using this factory.
	 */
	protected GraphDatabase graphDatabase;

	/**
	 * Collects all prepared statements which have been created in a sub class.
	 */
	private HashMap<String, PreparedStatement> preparedStatements = new HashMap<String, PreparedStatement>();

	/**
	 * Creates and initializes a new <code>SqlStatementList</code>. Provides
	 * default initialization for sub classes.
	 * 
	 * @param connection
	 *            Connection to graph database.
	 * @throws GraphDatabaseException
	 *             No connection given.
	 */
	protected SqlStatementList(GraphDatabase graphDatabase)
			throws GraphDatabaseException {
		if (graphDatabase != null) {
			this.graphDatabase = graphDatabase;
			initializeWith(graphDatabase.getConnection());
		} else {
			throw new GraphDatabaseException("No graph database given.");
		}
	}

	private void initializeWith(Connection connection)
			throws GraphDatabaseException {
		if (connection != null) {
			this.connection = connection;
		} else {
			throw new GraphDatabaseException("No connection to database.");
		}

	}

	protected PreparedStatement getPreparedStatement(String sqlStatement)
			throws SQLException {
		if (preparedStatements.containsKey(sqlStatement)) {
			return preparedStatements.get(sqlStatement);
		} else {
			return prepareAndCacheStatement(sqlStatement);
		}
	}

	private PreparedStatement prepareAndCacheStatement(String sqlStatement)
			throws SQLException {
		PreparedStatement preparedStatement = connection
				.prepareStatement(makeQueryVendorSpecific(sqlStatement));
		preparedStatements.put(sqlStatement, preparedStatement);
		return preparedStatement;
	}

	/**
	 * Sets quotation marks and semicolons in common queries, according to
	 * vendor specific rules.
	 * 
	 * @param query
	 *            the common query String
	 * @return a vendor specific query query String
	 */
	public abstract String makeQueryVendorSpecific(String query);

	// GraphSchema
	public abstract PreparedStatement createGraphSchemaTableWithConstraints()
			throws SQLException;

	// Type
	public abstract PreparedStatement createTypeTableWithConstraints()
			throws SQLException;

	// Attribute
	public abstract PreparedStatement createAttributeTableWithConstraints()
			throws SQLException;

	// Graph
	public abstract PreparedStatement createGraphTableWithConstraints()
			throws SQLException;

	// Vertex
	public abstract PreparedStatement createVertexTable() throws SQLException;

	public abstract PreparedStatement addPrimaryKeyConstraintOnVertexTable()
			throws SQLException;

	public abstract PreparedStatement dropPrimaryKeyConstraintFromVertexTable()
			throws SQLException;

	// Edge
	public abstract PreparedStatement createEdgeTable() throws SQLException;

	public abstract PreparedStatement addPrimaryKeyConstraintOnEdgeTable()
			throws SQLException;

	public abstract PreparedStatement dropPrimaryKeyConstraintFromEdgeTable()
			throws SQLException;

	// Incidence
	public abstract PreparedStatement createIncidenceTable()
			throws SQLException;

	public abstract PreparedStatement addPrimaryKeyConstraintOnIncidenceTable()
			throws SQLException;

	public abstract PreparedStatement dropPrimaryKeyConstraintFromIncidenceTable()
			throws SQLException;

	// GraphAttributeValue
	public abstract PreparedStatement createGraphAttributeValueTableWithConstraints()
			throws SQLException;

	// VertexAttributeValue
	public abstract PreparedStatement createVertexAttributeValueTable()
			throws SQLException;

	public abstract PreparedStatement addPrimaryKeyConstraintOnVertexAttributeValueTable()
			throws SQLException;

	public abstract PreparedStatement dropPrimaryKeyConstraintFromVertexAttributeValueTable()
			throws SQLException;

	// public abstract PreparedStatement
	// addClusteredIndexOnVertexAttributeValues()throws SQLException;

	// EdgeAttributeValue
	public abstract PreparedStatement createEdgeAttributeValueTable()
			throws SQLException;

	public abstract PreparedStatement addPrimaryKeyConstraintOnEdgeAttributeValueTable()
			throws SQLException;

	public abstract PreparedStatement dropPrimaryKeyConstraintFromEdgeAttributeValueTable()
			throws SQLException;

	// public abstract PreparedStatement
	// addClusteredIndexOnEdgeAttributeValues() throws SQLException;

	// to insert schema information
	private static final String INSERT_SCHEMA = "INSERT INTO " + QUOTE
			+ TABLE_SCHEMA + QUOTE + " ( " + QUOTE
			+ COLUMN_SCHEMA_PACKAGE_PREFIX + QUOTE + ", " + QUOTE
			+ COLUMN_SCHEMA_NAME + QUOTE + ", " + QUOTE + COLUMN_SCHEMA_TG
			+ QUOTE + " ) VALUES ( ?, ?, ? )" + EOQ;

	public PreparedStatement insertSchema(Schema schema,
			String serializedDefinition) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(
				makeQueryVendorSpecific(INSERT_SCHEMA),
				Statement.RETURN_GENERATED_KEYS);
		statement.setString(1, schema.getPackagePrefix());
		statement.setString(2, schema.getName());
		statement.setString(3, serializedDefinition);
		return statement;
	}

	private static final String DELETE_SCHEMA = "DELETE FROM " + QUOTE
			+ TABLE_SCHEMA + QUOTE + " WHERE " + QUOTE
			+ COLUMN_SCHEMA_PACKAGE_PREFIX + QUOTE + " = ? AND " + QUOTE
			+ COLUMN_SCHEMA_NAME + QUOTE + " = ?" + EOQ;

	public PreparedStatement deleteSchema(String prefix, String name)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(DELETE_SCHEMA);
		statement.setString(1, prefix);
		statement.setString(2, name);
		return statement;
	}

	private static final String INSERT_TYPE = "INSERT INTO " + QUOTE
			+ TABLE_TYPE + QUOTE + "( " + QUOTE + COLUMN_TYPE_QNAME + QUOTE
			+ ", " + QUOTE + COLUMN_SCHEMA_ID + QUOTE + " ) VALUES ( ?, ? )"
			+ EOQ;

	public PreparedStatement insertType(String qualifiedName, int schemaId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(INSERT_TYPE);
		statement.setString(1, qualifiedName);
		statement.setInt(2, schemaId);
		return statement;
	}

	private static final String INSERT_ATTRIBUTE = "INSERT INTO " + QUOTE
			+ TABLE_ATTRIBUTE + QUOTE + " ( " + QUOTE + COLUMN_ATTRIBUTE_NAME
			+ QUOTE + ", " + QUOTE + COLUMN_SCHEMA_ID + QUOTE
			+ " ) VALUES ( ?, ? )" + EOQ;

	public PreparedStatement insertAttribute(String name, int schemaId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(INSERT_ATTRIBUTE);
		statement.setString(1, name);
		statement.setInt(2, schemaId);
		return statement;
	}

	// to insert a graph
	private static final String INSERT_GRAPH = "INSERT INTO " + QUOTE
			+ TABLE_GRAPH + QUOTE + " ( " + QUOTE + COLUMN_GRAPH_UID + QUOTE
			+ ", " + QUOTE + COLUMN_GRAPH_VERSION + QUOTE + ", " + QUOTE
			+ COLUMN_GRAPH_VSEQ_VERSION + QUOTE + ", " + QUOTE
			+ COLUMN_GRAPH_ESEQ_VERSION + QUOTE + ", " + QUOTE + COLUMN_TYPE_ID
			+ QUOTE + " ) VALUES ( ?, ?, ?, ?, ? )" + EOQ;

	public PreparedStatement insertGraph(String id, long graphVersion,
			long vertexListVersion, long edgeListVersion, int typeId)
			throws SQLException {
		PreparedStatement statement = connection.prepareStatement(
				makeQueryVendorSpecific(INSERT_GRAPH),
				Statement.RETURN_GENERATED_KEYS);
		statement.setString(1, id);
		statement.setLong(2, graphVersion);
		statement.setLong(3, vertexListVersion);
		statement.setLong(4, edgeListVersion);
		statement.setInt(5, typeId);
		return statement;
	}

	private static final String INSERT_GRAPH_ATTRIBUTE_VALUE = "INSERT INTO "
			+ QUOTE + TABLE_GRAPH_ATTRIBUTE + QUOTE + " ( " + QUOTE
			+ COLUMN_GRAPH_ID + QUOTE + ", " + QUOTE + COLUMN_ATTRIBUTE_ID
			+ QUOTE + ", " + QUOTE + COLUMN_ATTRIBUTE_VALUE + QUOTE
			+ " ) VALUES ( ?, ?, ? )" + EOQ;

	public PreparedStatement insertGraphAttributeValue(int gId,
			int attributeId, String value) throws SQLException {
		PreparedStatement statement = getPreparedStatement(INSERT_GRAPH_ATTRIBUTE_VALUE);
		statement.setInt(1, gId);
		statement.setInt(2, attributeId);
		statement.setString(3, value);
		return statement;
	}

	// to insert a vertex
	protected static final String INSERT_VERTEX = "INSERT INTO " + QUOTE
			+ TABLE_VERTEX + QUOTE + " ( " + QUOTE + COLUMN_VERTEX_ID + QUOTE
			+ ", " + QUOTE + COLUMN_GRAPH_ID + QUOTE + ", " + QUOTE
			+ COLUMN_TYPE_ID + QUOTE + ", " + QUOTE
			+ COLUMN_VERTEX_LAMBDA_SEQ_VERSION + QUOTE + ", " + QUOTE
			+ COLUMN_SEQUENCE_NUMBER + QUOTE + " ) VALUES (?, ?, ?, ?, ?)"
			+ EOQ;

	public PreparedStatement insertVertex(int vId, int typeId, int gId,
			long incidenceListVersion, long sequenceNumberInVSeq)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(INSERT_VERTEX);
		statement.setInt(1, vId);
		statement.setInt(2, gId);
		statement.setInt(3, typeId);
		statement.setLong(4, incidenceListVersion);
		statement.setLong(5, sequenceNumberInVSeq);
		return statement;
	}

	protected static final String INSERT_VERTEX_ATTRIBUTE_VALUE = "INSERT INTO "
			+ QUOTE
			+ TABLE_VERTEX_ATTRIBUTE
			+ QUOTE
			+ " ( "
			+ QUOTE
			+ COLUMN_VERTEX_ID
			+ QUOTE
			+ ", "
			+ QUOTE
			+ COLUMN_GRAPH_ID
			+ QUOTE
			+ ", "
			+ QUOTE
			+ COLUMN_ATTRIBUTE_ID
			+ QUOTE
			+ ", "
			+ QUOTE
			+ COLUMN_ATTRIBUTE_VALUE + QUOTE + " ) VALUES ( ?, ?, ?, ? )" + EOQ;

	public PreparedStatement insertVertexAttributeValue(int vId, int gId,
			int attributeId, String value) throws SQLException {
		PreparedStatement statement = getPreparedStatement(INSERT_VERTEX_ATTRIBUTE_VALUE);
		statement.setInt(1, vId);
		statement.setInt(2, gId);
		statement.setInt(3, attributeId);
		statement.setString(4, value);
		return statement;
	}

	// to insert an edge

	protected static final String INSERT_EDGE = "INSERT INTO " + QUOTE
			+ TABLE_EDGE + QUOTE + " ( " + QUOTE + COLUMN_EDGE_ID + QUOTE
			+ ", " + QUOTE + COLUMN_GRAPH_ID + QUOTE + ", " + QUOTE
			+ COLUMN_TYPE_ID + QUOTE + ", " + QUOTE + COLUMN_SEQUENCE_NUMBER
			+ QUOTE + " ) VALUES ( ?, ?, ?, ? )" + EOQ;

	public PreparedStatement insertEdge(int eId, int gId, int typeId,
			long sequenceNumberInLambdaSeq) throws SQLException {
		PreparedStatement statement = getPreparedStatement(INSERT_EDGE);
		// TODO why is the edge id stored as abs?
		statement.setInt(1, Math.abs(eId));
		statement.setInt(2, gId);
		statement.setInt(3, typeId);
		statement.setLong(4, sequenceNumberInLambdaSeq);
		return statement;
	}

	protected static final String INSERT_EDGE_ATTRIBUTE_VALUE = "INSERT INTO "
			+ QUOTE + TABLE_EDGE_ATTRIBUTE + QUOTE + " ( " + QUOTE
			+ COLUMN_EDGE_ID + QUOTE + ", " + QUOTE + COLUMN_GRAPH_ID + QUOTE
			+ ", " + QUOTE + COLUMN_ATTRIBUTE_ID + QUOTE + ", " + QUOTE
			+ COLUMN_ATTRIBUTE_VALUE + QUOTE + " ) VALUES ( ?, ?, ?, ? )" + EOQ;

	public PreparedStatement insertEdgeAttributeValue(int eId, int gId,
			int attributeId, String value) throws SQLException {
		PreparedStatement statement = getPreparedStatement(INSERT_EDGE_ATTRIBUTE_VALUE);
		statement.setInt(1, eId);
		statement.setInt(2, gId);
		statement.setInt(3, attributeId);
		statement.setString(4, value);
		return statement;
	}

	protected static final String INSERT_INCIDENCE = "INSERT INTO " + QUOTE
			+ TABLE_INCIDENCE + QUOTE + " ( " + QUOTE + COLUMN_EDGE_ID + QUOTE
			+ ", " + QUOTE + COLUMN_GRAPH_ID + QUOTE + ", " + QUOTE
			+ COLUMN_VERTEX_ID + QUOTE + ", " + QUOTE
			+ COLUMN_INCIDENCE_DIRECTION + QUOTE + ", " + QUOTE
			+ COLUMN_SEQUENCE_NUMBER + QUOTE + " ) VALUES ( ?, ?, ?, "
			+ DIRECTION_TYPE + ", ? )" + EOQ;

	public PreparedStatement insertIncidence(int eId, int vId, int gId,
			long sequenceNumberInLambdaSeq) throws SQLException {
		PreparedStatement statement = getPreparedStatement(INSERT_INCIDENCE);
		statement.setInt(1, Math.abs(eId));
		statement.setInt(2, gId);
		statement.setInt(3, vId);
		if (eId > 0) {
			statement.setString(4, EdgeDirection.OUT.name());
		} else if (eId < 0) {
			statement.setString(4, EdgeDirection.IN.name());
		} else {
			throw new GraphException(
					"Cannot insert an incidence into database with incident edge id = 0.");
		}
		statement.setLong(5, sequenceNumberInLambdaSeq);
		return statement;
	}

	// to preload schema information when opening a graph
	private static final String SELECT_SCHEMA_ID = "SELECT " + QUOTE
			+ COLUMN_SCHEMA_ID + QUOTE + " FROM " + QUOTE + TABLE_SCHEMA
			+ QUOTE + " WHERE " + QUOTE + COLUMN_SCHEMA_PACKAGE_PREFIX + QUOTE
			+ " = ? AND " + QUOTE + COLUMN_SCHEMA_NAME + QUOTE + " = ?" + EOQ;

	public PreparedStatement selectSchemaId(String packagePrefix, String name)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_SCHEMA_ID);
		statement.setString(1, packagePrefix);
		statement.setString(2, name);
		return statement;
	}

	private static final String SELECT_SCHEMA_NAME = "SELECT " + QUOTE
			+ COLUMN_SCHEMA_PACKAGE_PREFIX + QUOTE + ", " + QUOTE
			+ COLUMN_SCHEMA_NAME + QUOTE + " FROM " + QUOTE + TABLE_SCHEMA
			+ QUOTE + " WHERE " + QUOTE + COLUMN_SCHEMA_ID + QUOTE + " = ("
			+ "SELECT " + QUOTE + COLUMN_SCHEMA_ID + QUOTE + " FROM " + QUOTE
			+ TABLE_TYPE + QUOTE + " WHERE " + QUOTE + COLUMN_TYPE_ID + QUOTE
			+ " = (" + "SELECT " + QUOTE + COLUMN_TYPE_ID + QUOTE + " FROM "
			+ QUOTE + TABLE_GRAPH + QUOTE + " WHERE " + QUOTE
			+ COLUMN_GRAPH_UID + QUOTE + " = ?" + ")" + ")" + EOQ;

	public PreparedStatement selectSchemaNameForGraph(String uid)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_SCHEMA_NAME);
		statement.setString(1, uid);
		return statement;
	}

	private static final String SELECT_SCHEMA_DEFINITION_BY_NAME = "SELECT "
			+ QUOTE + COLUMN_SCHEMA_TG + QUOTE + " FROM " + QUOTE
			+ TABLE_SCHEMA + QUOTE + " WHERE " + QUOTE
			+ COLUMN_SCHEMA_PACKAGE_PREFIX + QUOTE + " = ? AND "
			+ COLUMN_SCHEMA_NAME + " = ?;" + EOQ;

	public PreparedStatement selectSchemaDefinition(String packagePrefix,
			String schemaName) throws SQLException {
		PreparedStatement statement = connection
				.prepareStatement(SELECT_SCHEMA_DEFINITION_BY_NAME);
		statement.setString(1, packagePrefix);
		statement.setString(2, schemaName);
		return statement;
	}

	private static final String SELECT_SCHEMA_DEFINITION_FOR_GRAPH = "SELECT "
			+ QUOTE + COLUMN_SCHEMA_TG + QUOTE + " FROM " + QUOTE
			+ TABLE_SCHEMA + QUOTE + " WHERE " + QUOTE + COLUMN_SCHEMA_ID
			+ QUOTE + " = (" + "SELECT " + QUOTE + COLUMN_SCHEMA_ID + QUOTE
			+ " FROM " + QUOTE + TABLE_TYPE + QUOTE + " WHERE " + QUOTE
			+ COLUMN_TYPE_ID + QUOTE + " = (" + "SELECT " + QUOTE
			+ COLUMN_TYPE_ID + QUOTE + " FROM " + QUOTE + TABLE_GRAPH + QUOTE
			+ " WHERE " + QUOTE + COLUMN_GRAPH_UID + QUOTE + " = ?" + ")" + ")"
			+ EOQ;

	public PreparedStatement selectSchemaDefinitionForGraph(String uid)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_SCHEMA_DEFINITION_FOR_GRAPH);
		statement.setString(1, uid);
		return statement;
	}

	private static final String SELECT_TYPES = "SELECT " + QUOTE
			+ COLUMN_TYPE_QNAME + QUOTE + ", " + QUOTE + COLUMN_TYPE_ID + QUOTE
			+ " FROM " + QUOTE + TABLE_TYPE + QUOTE + " WHERE " + QUOTE
			+ COLUMN_SCHEMA_ID + QUOTE + " = " + "(SELECT " + QUOTE
			+ COLUMN_SCHEMA_ID + QUOTE + " FROM " + QUOTE + TABLE_SCHEMA
			+ QUOTE + " WHERE " + QUOTE + COLUMN_SCHEMA_PACKAGE_PREFIX + QUOTE
			+ " = ? AND " + QUOTE + COLUMN_SCHEMA_NAME + QUOTE + " = ?)" + EOQ;

	public PreparedStatement selectTypesOfSchema(String packagePrefix,
			String name) throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_TYPES);
		statement.setString(1, packagePrefix);
		statement.setString(2, name);
		return statement;
	}

	private static final String SELECT_ATTRIBUTES = "SELECT " + QUOTE
			+ COLUMN_ATTRIBUTE_NAME + QUOTE + ", " + QUOTE
			+ COLUMN_ATTRIBUTE_ID + QUOTE + " FROM " + QUOTE + TABLE_ATTRIBUTE
			+ QUOTE + " WHERE " + QUOTE + COLUMN_SCHEMA_ID + QUOTE + " = "
			+ "(SELECT " + QUOTE + COLUMN_SCHEMA_ID + QUOTE + " FROM " + QUOTE
			+ TABLE_SCHEMA + QUOTE + " WHERE " + QUOTE
			+ COLUMN_SCHEMA_PACKAGE_PREFIX + QUOTE + " = ? AND " + QUOTE
			+ COLUMN_SCHEMA_NAME + QUOTE + " = ?)" + EOQ;

	public PreparedStatement selectAttributesOfSchema(String packagePrefix,
			String name) throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_ATTRIBUTES);
		statement.setString(1, packagePrefix);
		statement.setString(2, name);
		return statement;
	}

	// to open a graph
	private static final String SELECT_GRAPH = "SELECT " + QUOTE
			+ COLUMN_GRAPH_ID + QUOTE + ", " + QUOTE + COLUMN_GRAPH_VERSION
			+ QUOTE + ", " + QUOTE + COLUMN_GRAPH_VSEQ_VERSION + QUOTE + ", "
			+ QUOTE + COLUMN_GRAPH_ESEQ_VERSION + QUOTE + " FROM " + QUOTE
			+ TABLE_GRAPH + QUOTE + " WHERE " + QUOTE + COLUMN_GRAPH_UID
			+ QUOTE + " = ?" + EOQ;

	public PreparedStatement selectGraph(String id) throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_GRAPH);
		statement.setString(1, id);
		return statement;
	}

	private static final String SELECT_VERTICES = "SELECT " + QUOTE
			+ COLUMN_VERTEX_ID + QUOTE + ", " + QUOTE + COLUMN_SEQUENCE_NUMBER
			+ QUOTE + " FROM " + QUOTE + TABLE_VERTEX + QUOTE + " WHERE "
			+ QUOTE + COLUMN_GRAPH_ID + QUOTE + " = ? ORDER BY " + QUOTE
			+ COLUMN_SEQUENCE_NUMBER + QUOTE + " ASC" + EOQ;

	public PreparedStatement selectVerticesOfGraph(int gId) throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_VERTICES);
		statement.setInt(1, gId);
		return statement;
	}

	private static final String SELECT_EDGES = "SELECT " + QUOTE
			+ COLUMN_EDGE_ID + QUOTE + ", " + QUOTE + COLUMN_SEQUENCE_NUMBER
			+ QUOTE + "  FROM " + QUOTE + TABLE_EDGE + QUOTE + " WHERE "
			+ QUOTE + COLUMN_GRAPH_ID + QUOTE + " = ? ORDER BY " + QUOTE
			+ COLUMN_SEQUENCE_NUMBER + QUOTE + " ASC" + EOQ;

	public PreparedStatement selectEdgesOfGraph(int gId) throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_EDGES);
		statement.setInt(1, gId);
		return statement;
	}

	private static final String SELECT_ATTRIBUTE_VALUES_OF_GRAPH = "SELECT "
			+ QUOTE + COLUMN_ATTRIBUTE_NAME + QUOTE + ", " + QUOTE
			+ COLUMN_ATTRIBUTE_VALUE + QUOTE + " FROM " + QUOTE
			+ TABLE_GRAPH_ATTRIBUTE + QUOTE + " JOIN " + QUOTE
			+ TABLE_ATTRIBUTE + QUOTE + " ON " + QUOTE + TABLE_GRAPH_ATTRIBUTE
			+ QUOTE + "." + QUOTE + COLUMN_ATTRIBUTE_ID + QUOTE + " = " + QUOTE
			+ TABLE_ATTRIBUTE + QUOTE + "." + QUOTE + COLUMN_ATTRIBUTE_ID
			+ QUOTE + " WHERE " + QUOTE + COLUMN_GRAPH_ID + QUOTE + " = ?"
			+ EOQ;

	public PreparedStatement selectAttributeValuesOfGraph(int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_ATTRIBUTE_VALUES_OF_GRAPH);
		statement.setInt(1, gId);
		return statement;
	}

	private static final String COUNT_VERTICES = "SELECT COUNT (*) FROM "
			+ QUOTE + TABLE_VERTEX + QUOTE + " WHERE " + QUOTE
			+ COLUMN_GRAPH_ID + QUOTE + " = ?" + EOQ;

	public PreparedStatement countVerticesOfGraph(int gId) throws SQLException {
		PreparedStatement statement = getPreparedStatement(COUNT_VERTICES);
		statement.setInt(1, gId);
		return statement;
	}

	private static final String COUNT_EDGES = "SELECT COUNT (*) FROM " + QUOTE
			+ TABLE_EDGE + QUOTE + " WHERE " + QUOTE + COLUMN_GRAPH_ID + QUOTE
			+ " = ?" + EOQ;

	public PreparedStatement countEdgesOfGraph(int gId) throws SQLException {
		PreparedStatement statement = getPreparedStatement(COUNT_EDGES);
		statement.setInt(1, gId);
		return statement;
	}

	// to open a vertex
	private static final String SELECT_VERTEX_WITH_INCIDENCES = "SELECT "
			+ QUOTE + COLUMN_TYPE_ID + QUOTE + ", " + QUOTE
			+ COLUMN_VERTEX_LAMBDA_SEQ_VERSION + QUOTE + ", " + QUOTE
			+ TABLE_VERTEX + QUOTE + "." + QUOTE + COLUMN_SEQUENCE_NUMBER
			+ QUOTE + ", " + QUOTE + TABLE_INCIDENCE + QUOTE + "." + QUOTE
			+ COLUMN_SEQUENCE_NUMBER + QUOTE + ", " + QUOTE
			+ COLUMN_INCIDENCE_DIRECTION + QUOTE + ", " + QUOTE
			+ COLUMN_EDGE_ID + QUOTE + " FROM " + QUOTE + TABLE_VERTEX + QUOTE
			+ " LEFT OUTER JOIN " + QUOTE + TABLE_INCIDENCE + QUOTE + " ON ( "
			+ QUOTE + TABLE_VERTEX + QUOTE + "." + QUOTE + COLUMN_VERTEX_ID
			+ QUOTE + " = " + QUOTE + TABLE_INCIDENCE + QUOTE + "." + QUOTE
			+ COLUMN_VERTEX_ID + QUOTE + " AND " + QUOTE + TABLE_VERTEX + QUOTE
			+ "." + QUOTE + COLUMN_GRAPH_ID + QUOTE + " = " + QUOTE
			+ TABLE_INCIDENCE + QUOTE + "." + QUOTE + COLUMN_GRAPH_ID + QUOTE
			+ " )" + "WHERE " + QUOTE + TABLE_VERTEX + QUOTE + "." + QUOTE
			+ COLUMN_VERTEX_ID + QUOTE + " = ? AND " + QUOTE + TABLE_VERTEX
			+ QUOTE + "." + QUOTE + COLUMN_GRAPH_ID + QUOTE + " = ?"
			+ "ORDER BY " + QUOTE + TABLE_INCIDENCE + QUOTE + "." + QUOTE
			+ COLUMN_SEQUENCE_NUMBER + QUOTE + " ASC" + EOQ;

	public PreparedStatement selectVertexWithIncidences(int vId, int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_VERTEX_WITH_INCIDENCES);
		statement.setInt(1, vId);
		statement.setInt(2, gId);
		return statement;
	}

	private static final String SELECT_ATTRIBUTE_VALUES_OF_VERTEX = "SELECT "
			+ QUOTE + COLUMN_ATTRIBUTE_ID + QUOTE + ", " + QUOTE
			+ COLUMN_ATTRIBUTE_VALUE + QUOTE + " FROM " + QUOTE
			+ TABLE_VERTEX_ATTRIBUTE + QUOTE + " WHERE " + QUOTE
			+ COLUMN_VERTEX_ID + QUOTE + " = ? AND " + QUOTE + COLUMN_GRAPH_ID
			+ QUOTE + " = ?" + EOQ;

	public PreparedStatement selectAttributeValuesOfVertex(int vId, int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_ATTRIBUTE_VALUES_OF_VERTEX);
		statement.setInt(1, vId);
		statement.setInt(2, gId);
		return statement;
	}

	// to open an edge
	private static final String SELECT_EDGE_WITH_INCIDENCES = "SELECT " + QUOTE
			+ COLUMN_TYPE_ID + QUOTE + ", " + QUOTE + TABLE_EDGE + QUOTE + "."
			+ QUOTE + COLUMN_SEQUENCE_NUMBER + QUOTE + ", " + QUOTE
			+ COLUMN_INCIDENCE_DIRECTION + QUOTE + ", " + QUOTE
			+ COLUMN_VERTEX_ID + QUOTE + ", " + QUOTE + TABLE_INCIDENCE + QUOTE
			+ "." + QUOTE + COLUMN_SEQUENCE_NUMBER + QUOTE + " FROM " + QUOTE
			+ TABLE_EDGE + QUOTE + " INNER JOIN " + QUOTE + TABLE_INCIDENCE
			+ QUOTE + " ON ( " + QUOTE + TABLE_EDGE + QUOTE + "." + QUOTE
			+ COLUMN_EDGE_ID + QUOTE + " = " + QUOTE + TABLE_INCIDENCE + QUOTE
			+ "." + QUOTE + COLUMN_EDGE_ID + QUOTE + " AND " + QUOTE
			+ TABLE_EDGE + QUOTE + "." + QUOTE + COLUMN_GRAPH_ID + QUOTE
			+ " = " + QUOTE + TABLE_INCIDENCE + QUOTE + "." + QUOTE
			+ COLUMN_GRAPH_ID + QUOTE + " )" + "WHERE " + QUOTE + TABLE_EDGE
			+ QUOTE + "." + QUOTE + COLUMN_EDGE_ID + QUOTE + " = ? AND "
			+ QUOTE + TABLE_EDGE + QUOTE + "." + QUOTE + COLUMN_GRAPH_ID
			+ QUOTE + " = ?" + EOQ;

	public PreparedStatement selectEdgeWithIncidences(int eId, int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_EDGE_WITH_INCIDENCES);
		statement.setInt(1, eId);
		statement.setInt(2, gId);
		return statement;
	}

	private static final String SELECT_ATTRIBUTE_VALUES_OF_EDGE = "SELECT "
			+ QUOTE + COLUMN_ATTRIBUTE_ID + QUOTE + ", " + QUOTE
			+ COLUMN_ATTRIBUTE_VALUE + QUOTE + " FROM " + QUOTE
			+ TABLE_EDGE_ATTRIBUTE + QUOTE + " WHERE " + QUOTE + COLUMN_EDGE_ID
			+ QUOTE + " = ? AND " + QUOTE + COLUMN_GRAPH_ID + QUOTE + " = ?"
			+ EOQ;

	public PreparedStatement selectAttributeValuesOfEdge(int eId, int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_ATTRIBUTE_VALUES_OF_EDGE);
		statement.setInt(1, eId);
		statement.setInt(2, gId);
		return statement;
	}

	// to delete a graph
	private static final String DELETE_GRAPH = "DELETE FROM " + QUOTE
			+ TABLE_GRAPH + QUOTE + " WHERE " + QUOTE + COLUMN_GRAPH_ID + QUOTE
			+ " = ?" + EOQ;

	public PreparedStatement deleteGraph(int gId) throws SQLException {
		PreparedStatement statement = getPreparedStatement(DELETE_GRAPH);
		statement.setInt(1, gId);
		return statement;
	}

	private static final String DELETE_INCIDENCES_OF_GRAPH = "DELETE FROM "
			+ QUOTE + TABLE_INCIDENCE + QUOTE + " WHERE " + QUOTE
			+ COLUMN_GRAPH_ID + QUOTE + " = ?" + EOQ;

	public PreparedStatement deleteIncidencesOfGraph(int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(DELETE_INCIDENCES_OF_GRAPH);
		statement.setInt(1, gId);
		return statement;
	}

	private static final String DELETE_GRAPH_ATTRIBUTES = "DELETE FROM "
			+ QUOTE + TABLE_GRAPH_ATTRIBUTE + QUOTE + " WHERE " + QUOTE
			+ COLUMN_GRAPH_ID + QUOTE + " = ?" + EOQ;

	public PreparedStatement deleteAttributeValuesOfGraph(int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(DELETE_GRAPH_ATTRIBUTES);
		statement.setInt(1, gId);
		return statement;
	}

	private static final String DELETE_VERTEX_ATTRIBUTE_VALUES_OF_GRAPH = "DELETE FROM "
			+ QUOTE
			+ TABLE_VERTEX_ATTRIBUTE
			+ QUOTE
			+ " WHERE "
			+ QUOTE
			+ COLUMN_GRAPH_ID + QUOTE + " = ?" + EOQ;

	public PreparedStatement deleteVertexAttributeValuesOfGraph(int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(DELETE_VERTEX_ATTRIBUTE_VALUES_OF_GRAPH);
		statement.setInt(1, gId);
		return statement;
	}

	private static final String DELETE_EDGE_ATTRIBUTE_VALUES_OF_GRAPH = "DELETE FROM "
			+ QUOTE
			+ TABLE_EDGE_ATTRIBUTE
			+ QUOTE
			+ " WHERE "
			+ QUOTE
			+ COLUMN_GRAPH_ID + QUOTE + " = ?" + EOQ;

	public PreparedStatement deleteEdgeAttributeValuesOfGraph(int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(DELETE_EDGE_ATTRIBUTE_VALUES_OF_GRAPH);
		statement.setInt(1, gId);
		return statement;
	}

	private static final String DELETE_VERTICES_OF_GRAPH = "DELETE FROM "
			+ QUOTE + TABLE_VERTEX + QUOTE + " WHERE " + QUOTE
			+ COLUMN_GRAPH_ID + QUOTE + " = ?" + EOQ;

	public PreparedStatement deleteVerticesOfGraph(int gId) throws SQLException {
		PreparedStatement statement = getPreparedStatement(DELETE_VERTICES_OF_GRAPH);
		statement.setInt(1, gId);
		return statement;
	}

	private static final String DELETE_EDGES_OF_GRAPH = "" + "DELETE FROM "
			+ QUOTE + TABLE_EDGE + QUOTE + " WHERE " + QUOTE + COLUMN_GRAPH_ID
			+ QUOTE + " = ?" + EOQ;

	public PreparedStatement deleteEdgesOfGraph(int gId) throws SQLException {
		PreparedStatement statement = getPreparedStatement(DELETE_EDGES_OF_GRAPH);
		statement.setInt(1, gId);
		return statement;
	}

	// to delete a vertex
	private static final String DELETE_VERTEX_ATTRIBUTES = "DELETE FROM "
			+ QUOTE + TABLE_VERTEX_ATTRIBUTE + QUOTE + " WHERE " + QUOTE
			+ COLUMN_VERTEX_ID + QUOTE + " = ? AND " + QUOTE + COLUMN_GRAPH_ID
			+ QUOTE + " = ?" + EOQ;

	public PreparedStatement deleteAttributeValuesOfVertex(int vId, int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(DELETE_VERTEX_ATTRIBUTES);
		statement.setInt(1, vId);
		statement.setInt(2, gId);
		return statement;
	}

	private static final String DELETE_VERTEX = "DELETE FROM " + QUOTE
			+ TABLE_VERTEX + QUOTE + " WHERE " + QUOTE + COLUMN_VERTEX_ID
			+ QUOTE + " = ? AND " + QUOTE + COLUMN_GRAPH_ID + QUOTE + " = ?"
			+ EOQ;

	public PreparedStatement deleteVertex(int vId, int gId) throws SQLException {
		PreparedStatement statement = getPreparedStatement(DELETE_VERTEX);
		statement.setInt(1, vId);
		statement.setInt(2, gId);
		return statement;
	}

	private static final String SELECT_ID_OF_INCIDENT_EDGES_OF_VERTEX = "SELECT "
			+ QUOTE
			+ COLUMN_EDGE_ID
			+ QUOTE
			+ " FROM "
			+ QUOTE
			+ TABLE_INCIDENCE
			+ QUOTE
			+ " WHERE "
			+ QUOTE
			+ COLUMN_VERTEX_ID
			+ QUOTE
			+ " = ? AND "
			+ QUOTE
			+ COLUMN_GRAPH_ID
			+ QUOTE
			+ " = ?"
			+ EOQ;

	public PreparedStatement selectIncidentEIdsOfVertex(int vId, int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_ID_OF_INCIDENT_EDGES_OF_VERTEX);
		statement.setInt(1, vId);
		statement.setInt(2, gId);
		return statement;
	}

	private static final String DELETE_EDGE_ATTRIBUTES = "DELETE FROM " + QUOTE
			+ TABLE_EDGE_ATTRIBUTE + QUOTE + " WHERE " + QUOTE + COLUMN_EDGE_ID
			+ QUOTE + " = ? AND " + QUOTE + COLUMN_GRAPH_ID + QUOTE + " = ?"
			+ EOQ;

	public PreparedStatement deleteAttributeValuesOfEdge(int eId, int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(DELETE_EDGE_ATTRIBUTES);
		statement.setInt(1, eId);
		statement.setInt(2, gId);
		return statement;
	}

	private static final String DELETE_INCIDENCES_OF_EDGE = "DELETE FROM "
			+ QUOTE + TABLE_INCIDENCE + QUOTE + " WHERE " + QUOTE
			+ COLUMN_EDGE_ID + QUOTE + " = ? AND " + QUOTE + COLUMN_GRAPH_ID
			+ QUOTE + " = ?" + EOQ;

	public PreparedStatement deleteIncidencesOfEdge(int eId, int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(DELETE_INCIDENCES_OF_EDGE);
		statement.setInt(1, eId);
		statement.setInt(2, gId);
		return statement;
	}

	private static final String DELETE_EDGE = "DELETE FROM " + QUOTE
			+ TABLE_EDGE + QUOTE + " WHERE " + QUOTE + COLUMN_EDGE_ID + QUOTE
			+ " = ? AND " + QUOTE + COLUMN_GRAPH_ID + QUOTE + " = ?" + EOQ;

	public PreparedStatement deleteEdge(int eId, int gId) throws SQLException {
		PreparedStatement statement = getPreparedStatement(DELETE_EDGE);
		statement.setInt(1, eId);
		statement.setInt(2, gId);
		return statement;
	}

	// to update a graph
	private static final String UPDATE_GRAPH_UID = "UPDATE " + QUOTE
			+ TABLE_GRAPH + QUOTE + " SET " + QUOTE + COLUMN_GRAPH_UID + QUOTE
			+ " = ? WHERE " + QUOTE + COLUMN_GRAPH_ID + QUOTE + " = ?" + EOQ;

	public PreparedStatement updateGraphId(int gId, String uid)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_GRAPH_UID);
		statement.setString(1, uid);
		statement.setInt(2, gId);
		return statement;
	}

	private static final String UPDATE_GRAPH_VERSION = "UPDATE " + QUOTE
			+ TABLE_GRAPH + QUOTE + " SET " + QUOTE + COLUMN_GRAPH_VERSION
			+ QUOTE + " = ? WHERE " + QUOTE + COLUMN_GRAPH_ID + QUOTE + " = ?"
			+ EOQ;

	public PreparedStatement updateGraphVersion(int gId, long version)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_GRAPH_VERSION);
		statement.setLong(1, version);
		statement.setInt(2, gId);
		return statement;
	}

	private static final String UPDATE_VERTEX_LIST_VERSION = "UPDATE " + QUOTE
			+ TABLE_GRAPH + QUOTE + " SET " + QUOTE + COLUMN_GRAPH_VSEQ_VERSION
			+ QUOTE + " = ? WHERE " + QUOTE + COLUMN_GRAPH_ID + QUOTE + " = ?"
			+ EOQ;

	public PreparedStatement updateVertexListVersionOfGraph(int gId,
			long version) throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_VERTEX_LIST_VERSION);
		statement.setLong(1, version);
		statement.setInt(2, gId);
		return statement;
	}

	private static final String UPDATE_EDGE_LIST_VERSION = "UPDATE " + QUOTE
			+ TABLE_GRAPH + QUOTE + " SET " + QUOTE + COLUMN_GRAPH_ESEQ_VERSION
			+ QUOTE + " = ? WHERE " + QUOTE + COLUMN_GRAPH_ID + QUOTE + " = ?"
			+ EOQ;

	public PreparedStatement updateEdgeListVersionOfGraph(int gId, long version)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_EDGE_LIST_VERSION);
		statement.setLong(1, version);
		statement.setInt(2, gId);
		return statement;
	}

	private static final String UPDATE_ATTRIBUTE_VALUE_OF_GRAPH = "UPDATE "
			+ QUOTE + TABLE_GRAPH_ATTRIBUTE + QUOTE + " SET " + QUOTE
			+ COLUMN_ATTRIBUTE_VALUE + QUOTE + " = ? WHERE " + QUOTE
			+ COLUMN_GRAPH_ID + QUOTE + " = ? AND " + QUOTE
			+ COLUMN_ATTRIBUTE_ID + QUOTE + " = ?" + EOQ;

	public PreparedStatement updateAttributeValueOfGraph(int gId,
			int attributeId, String serializedValue) throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_ATTRIBUTE_VALUE_OF_GRAPH);
		statement.setString(1, serializedValue);
		statement.setInt(2, gId);
		statement.setInt(3, attributeId);
		return statement;
	}

	private static final String UPDATE_ATTRIBUTE_VALUE_OF_GRAPH_AND_GRAPH_VERSION = "UPDATE "
			+ QUOTE
			+ TABLE_GRAPH_ATTRIBUTE
			+ QUOTE
			+ " SET "
			+ QUOTE
			+ COLUMN_ATTRIBUTE_VALUE
			+ QUOTE
			+ " = ? WHERE "
			+ QUOTE
			+ COLUMN_GRAPH_ID
			+ QUOTE
			+ " = ? AND "
			+ QUOTE
			+ COLUMN_ATTRIBUTE_ID
			+ QUOTE
			+ " = ?;"
			+ "UPDATE "
			+ QUOTE
			+ TABLE_GRAPH
			+ QUOTE
			+ " SET "
			+ QUOTE
			+ COLUMN_GRAPH_VERSION
			+ QUOTE
			+ " = ? WHERE "
			+ QUOTE
			+ COLUMN_GRAPH_ID
			+ QUOTE
			+ " = ?"
			+ EOQ;

	public PreparedStatement updateAttributeValueOfGraphAndGraphVersion(
			int gId, int attributeId, String serializedValue, long graphVersion)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_ATTRIBUTE_VALUE_OF_GRAPH_AND_GRAPH_VERSION);
		statement.setString(1, serializedValue);
		statement.setInt(2, gId);
		statement.setInt(3, attributeId);
		statement.setLong(4, graphVersion);
		statement.setInt(5, gId);
		return statement;
	}

	// to update a vertex
	private static final String UPDATE_VERTEX_ID = "UPDATE " + QUOTE
			+ TABLE_VERTEX + QUOTE + " SET " + QUOTE + COLUMN_VERTEX_ID + QUOTE
			+ " = ? WHERE " + QUOTE + COLUMN_VERTEX_ID + QUOTE + " = ? AND "
			+ QUOTE + COLUMN_GRAPH_ID + QUOTE + " = ?" + EOQ;

	public PreparedStatement updateIdOfVertex(int oldVId, int gId, int newVId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_VERTEX_ID);
		statement.setInt(1, newVId);
		statement.setInt(2, oldVId);
		statement.setInt(3, gId);
		return statement;
	}

	private static final String UPDATE_SEQUENCE_NUMBER_OF_VERTEX = "UPDATE "
			+ QUOTE + TABLE_VERTEX + QUOTE + " SET " + QUOTE
			+ COLUMN_SEQUENCE_NUMBER + QUOTE + " = ? WHERE " + QUOTE
			+ COLUMN_VERTEX_ID + QUOTE + " = ? AND " + QUOTE + COLUMN_GRAPH_ID
			+ QUOTE + " = ?" + EOQ;

	public PreparedStatement updateSequenceNumberInVSeqOfVertex(int vId,
			int gId, long sequenceNumberInVSeq) throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_SEQUENCE_NUMBER_OF_VERTEX);
		statement.setLong(1, sequenceNumberInVSeq);
		statement.setInt(2, vId);
		statement.setInt(3, gId);
		return statement;
	}

	protected static final String UPDATE_INCIDENCE_LIST_VERSION = "UPDATE "
			+ QUOTE + TABLE_VERTEX + QUOTE + " SET " + QUOTE
			+ COLUMN_VERTEX_LAMBDA_SEQ_VERSION + QUOTE + " = ? WHERE " + QUOTE
			+ COLUMN_VERTEX_ID + QUOTE + " = ? AND " + QUOTE + COLUMN_GRAPH_ID
			+ QUOTE + " = ?" + EOQ;

	public PreparedStatement updateLambdaSeqVersionOfVertex(int vId, int gId,
			long lambdaSeqVersion) throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_INCIDENCE_LIST_VERSION);
		statement.setLong(1, lambdaSeqVersion);
		statement.setInt(2, vId);
		statement.setInt(3, gId);
		return statement;
	}

	private static final String UPDATE_ATTRIBUTE_VALUE_OF_VERTEX = "UPDATE "
			+ QUOTE + TABLE_VERTEX_ATTRIBUTE + QUOTE + " SET " + QUOTE
			+ COLUMN_ATTRIBUTE_VALUE + QUOTE + " = ? WHERE " + QUOTE
			+ COLUMN_VERTEX_ID + QUOTE + " = ? AND " + QUOTE + COLUMN_GRAPH_ID
			+ QUOTE + " = ? AND " + QUOTE + COLUMN_ATTRIBUTE_ID + QUOTE
			+ " = ?" + EOQ;

	public PreparedStatement updateAttributeValueOfVertex(int vId, int gId,
			int attributeId, String serializedValue) throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_ATTRIBUTE_VALUE_OF_VERTEX);
		statement.setString(1, serializedValue);
		statement.setInt(2, vId);
		statement.setInt(3, gId);
		statement.setInt(4, attributeId);
		return statement;
	}

	private static final String UPDATE_ATTRIBUTE_VALUE_OF_VERTEX_AND_GRAPH_VERSION = "UPDATE "
			+ QUOTE
			+ TABLE_VERTEX_ATTRIBUTE
			+ QUOTE
			+ " SET "
			+ QUOTE
			+ COLUMN_ATTRIBUTE_VALUE
			+ QUOTE
			+ " = ? WHERE "
			+ QUOTE
			+ COLUMN_VERTEX_ID
			+ QUOTE
			+ " = ? AND "
			+ QUOTE
			+ COLUMN_GRAPH_ID
			+ QUOTE
			+ " = ? AND "
			+ QUOTE
			+ COLUMN_ATTRIBUTE_ID
			+ QUOTE
			+ " = ?"
			+ " UPDATE "
			+ QUOTE
			+ TABLE_GRAPH
			+ QUOTE
			+ " SET "
			+ QUOTE
			+ COLUMN_GRAPH_VERSION
			+ QUOTE
			+ " = ? WHERE "
			+ QUOTE
			+ COLUMN_GRAPH_ID + QUOTE + " = ?" + EOQ;

	public PreparedStatement updateAttributeValueOfVertexAndGraphVersion(
			int vId, int gId, int attributeId, String serializedValue,
			long graphVersion) throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_ATTRIBUTE_VALUE_OF_VERTEX_AND_GRAPH_VERSION);
		statement.setString(1, serializedValue);
		statement.setInt(2, vId);
		statement.setInt(3, gId);
		statement.setInt(4, attributeId);
		statement.setLong(5, graphVersion);
		statement.setInt(6, gId);
		return statement;
	}

	// to update an edge
	private static final String UPDATE_EDGE_ID = "UPDATE " + QUOTE + TABLE_EDGE
			+ QUOTE + " SET " + QUOTE + COLUMN_EDGE_ID + QUOTE + " = ? WHERE "
			+ QUOTE + COLUMN_EDGE_ID + QUOTE + " = ? AND " + QUOTE
			+ COLUMN_GRAPH_ID + QUOTE + " = ?" + EOQ;

	public PreparedStatement updateIdOfEdge(int oldEId, int gId, int newEId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_EDGE_ID);
		statement.setInt(1, newEId);
		statement.setInt(2, oldEId);
		statement.setInt(3, gId);
		return statement;
	}

	private static final String UPDATE_INCIDENT_VERTEX = "UPDATE " + QUOTE
			+ TABLE_INCIDENCE + QUOTE + " SET " + QUOTE + COLUMN_VERTEX_ID
			+ QUOTE + " = ? WHERE " + QUOTE + COLUMN_EDGE_ID + QUOTE
			+ " = ? AND " + QUOTE + COLUMN_GRAPH_ID + QUOTE + " = ? AND "
			+ QUOTE + COLUMN_INCIDENCE_DIRECTION + QUOTE + " = "
			+ DIRECTION_TYPE + EOQ;

	public PreparedStatement updateIncidentVIdOfIncidence(int eId, int vId,
			int gId) throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_INCIDENT_VERTEX);
		statement.setInt(1, vId);
		statement.setInt(2, Math.abs(eId));
		statement.setInt(3, gId);
		if (eId > 0) {
			statement.setString(4, EdgeDirection.OUT.name());
		} else if (eId < 0) {
			statement.setString(4, EdgeDirection.IN.name());
		}
		return statement;
	}

	private static final String UPDATE_SEQUENCE_NUMBER_IN_INCIDENCE_LIST = "UPDATE "
			+ QUOTE
			+ TABLE_INCIDENCE
			+ QUOTE
			+ " SET "
			+ QUOTE
			+ COLUMN_SEQUENCE_NUMBER
			+ QUOTE
			+ " = ? WHERE "
			+ QUOTE
			+ COLUMN_EDGE_ID
			+ QUOTE
			+ " = ? AND "
			+ QUOTE
			+ COLUMN_GRAPH_ID
			+ QUOTE
			+ " = ? AND "
			+ QUOTE
			+ COLUMN_VERTEX_ID
			+ QUOTE
			+ " = ?"
			+ EOQ;

	public PreparedStatement updateSequenceNumberInLambdaSeqOfIncidence(
			int eId, int vId, int gId, long sequenceNumberInLambdaSeq)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_SEQUENCE_NUMBER_IN_INCIDENCE_LIST);
		statement.setLong(1, sequenceNumberInLambdaSeq);
		statement.setInt(2, Math.abs(eId));
		statement.setInt(3, gId);
		statement.setInt(4, vId);
		return statement;
	}

	private static final String UPDATE_SEQUENCE_NUMBER_IN_EDGE_LIST = "UPDATE "
			+ QUOTE + TABLE_EDGE + QUOTE + " SET " + QUOTE
			+ COLUMN_SEQUENCE_NUMBER + QUOTE + " = ? WHERE " + QUOTE
			+ COLUMN_EDGE_ID + QUOTE + " = ? AND " + QUOTE + COLUMN_GRAPH_ID
			+ QUOTE + " = ?" + EOQ;

	public PreparedStatement updateSequenceNumberInESeqOfEdge(int eId, int gId,
			long SequenceNumberInESeq) throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_SEQUENCE_NUMBER_IN_EDGE_LIST);
		statement.setLong(1, SequenceNumberInESeq);
		statement.setInt(2, eId);
		statement.setInt(3, gId);
		return statement;
	}

	private static final String UPDATE_ATTRIBUTE_VALUE_OF_EDGE = "UPDATE "
			+ QUOTE + TABLE_EDGE_ATTRIBUTE + QUOTE + " SET " + QUOTE
			+ COLUMN_ATTRIBUTE_VALUE + QUOTE + " = ? WHERE " + QUOTE
			+ COLUMN_EDGE_ID + QUOTE + " = ? AND " + QUOTE + COLUMN_GRAPH_ID
			+ QUOTE + " = ? AND " + QUOTE + COLUMN_ATTRIBUTE_ID + QUOTE
			+ " = ?" + EOQ;

	public PreparedStatement updateAttributeValueOfEdge(int eId, int gId,
			int attributeId, String serializedValue) throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_ATTRIBUTE_VALUE_OF_EDGE);
		statement.setString(1, serializedValue);
		statement.setInt(2, eId);
		statement.setInt(3, gId);
		statement.setInt(4, attributeId);
		return statement;
	}

	private static final String UPDATE_ATTRIBUTE_VALUE_OF_EDGE_AND_GRAPH_VERSION = "UPDATE "
			+ QUOTE
			+ TABLE_EDGE_ATTRIBUTE
			+ QUOTE
			+ " SET "
			+ QUOTE
			+ COLUMN_ATTRIBUTE_VALUE
			+ QUOTE
			+ " = ? WHERE "
			+ QUOTE
			+ COLUMN_EDGE_ID
			+ QUOTE
			+ " = ? AND "
			+ QUOTE
			+ COLUMN_GRAPH_ID
			+ QUOTE
			+ " = ? AND "
			+ QUOTE
			+ COLUMN_ATTRIBUTE_ID
			+ QUOTE
			+ " = ?"
			+ " UPDATE "
			+ QUOTE
			+ TABLE_GRAPH
			+ QUOTE
			+ " SET "
			+ QUOTE
			+ COLUMN_GRAPH_VERSION
			+ QUOTE
			+ " = ? WHERE "
			+ QUOTE
			+ COLUMN_GRAPH_ID + QUOTE + " = ?" + EOQ;

	public PreparedStatement updateAttributeValueOfEdgeAndGraphVersion(int eId,
			int gId, int attributeId, String serializedValue, long graphVersion)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_ATTRIBUTE_VALUE_OF_EDGE_AND_GRAPH_VERSION);
		statement.setString(1, serializedValue);
		statement.setInt(2, eId);
		statement.setInt(3, gId);
		statement.setInt(4, attributeId);
		statement.setLong(5, graphVersion);
		statement.setInt(6, gId);
		return statement;
	}

	// stored procedures to reorganize sequence numbers in sequences of graph
	public abstract PreparedStatement createStoredProcedureToReorganizeVertexList()
			throws SQLException;

	public abstract PreparedStatement createStoredProcedureToReorganizeEdgeList()
			throws SQLException;

	public abstract PreparedStatement createStoredProcedureToReorganizeIncidenceList()
			throws SQLException;

	public abstract CallableStatement createReorganizeVertexListCall(int gId,
			long start) throws SQLException;

	public abstract CallableStatement createReorganizeEdgeListCall(int gId,
			long start) throws SQLException;

	public abstract CallableStatement createReorganizeIncidenceListCall(
			int vId, int gId, long start) throws SQLException;

	// to increase performance
	public abstract PreparedStatement addIndexOnLambdaSeq() throws SQLException;

	public abstract PreparedStatement dropIndexOnLambdaSeq()
			throws SQLException; // TODO find better name as it's an index on

	// table Incidence, modeling LambdaSeq

	// public abstract PreparedStatement clusterIncidenceTable() throws
	// SQLException; // TODO Must be PostgreSql specific.
	// public abstract PreparedStatement dropClusteredIndicesOnAttributeValues()
	// throws SQLException;
	// public abstract PreparedStatement clusterAttributeValues() throws
	// SQLException;
	// public abstract PreparedStatement createStoredProcedureToInsertVertex()
	// throws SQLException;

	private static final String SELECT_ID_OF_GRAPHS = "SELECT " + QUOTE
			+ COLUMN_GRAPH_UID + QUOTE + " FROM " + QUOTE + TABLE_GRAPH + QUOTE
			+ EOQ;

	public PreparedStatement selectIdOfGraphs() throws SQLException {
		return getPreparedStatement(SELECT_ID_OF_GRAPHS);
	}

	public abstract PreparedStatement clearAllTables() throws SQLException;
}
