/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.SortedSet;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.Schema;

/**
 * Factory that creates PostgreSql specific prepared statements.
 * 
 * @author ultbreit@uni-koblenz.de
 */
public class PostgreSqlStatementList extends SqlStatementList {

	public PostgreSqlStatementList(GraphDatabase graphDatabase)
			throws GraphDatabaseException {
		super(graphDatabase);
	}

	private static final String CREATE_GRAPH_SCHEMA_TABLE = "CREATE SEQUENCE \"schemaIdSequence\";"
			+ "CREATE TABLE \""
			+ GraphDatabase.TABLE_SCHEMA
			+ "\"("
			+ "\"schemaId\" INT4 PRIMARY KEY DEFAULT NEXTVAL('\"schemaIdSequence\"'),"
			+ "\"packagePrefix\" TEXT,"
			+ "name TEXT,"
			+ "\"serializedDefinition\" TEXT" + ");";

	@Override
	public PreparedStatement createGraphSchemaTableWithConstraints()
			throws SQLException {
		return connection.prepareStatement(CREATE_GRAPH_SCHEMA_TABLE);
	}

	private static final String CREATE_TYPE_TABLE = "CREATE SEQUENCE \"typeIdSequence\";"
			+ "CREATE TABLE \""
			+ GraphDatabase.TABLE_TYPE
			+ "\"("
			+ "\"typeId\" INT4 PRIMARY KEY DEFAULT NEXTVAL('\"typeIdSequence\"'),"
			+ "\"qualifiedName\" TEXT,"
			+ "\"schemaId\" INT4 REFERENCES \""
			+ GraphDatabase.TABLE_SCHEMA
			+ "\" ON DELETE CASCADE"
			+ ");";

	@Override
	public PreparedStatement createTypeTableWithConstraints()
			throws SQLException {
		return connection.prepareStatement(CREATE_TYPE_TABLE);
	}

	private static final String CREATE_GRAPH_TABLE = "CREATE SEQUENCE \"graphIdSequence\";"
			+ "CREATE TABLE \""
			+ GraphDatabase.TABLE_GRAPH
			+ "\"("
			+ "\"gId\" INT4 PRIMARY KEY DEFAULT NEXTVAL('\"graphIdSequence\"'),"
			+ "uid TEXT,"
			+ "version INT8,"
			+ "\"vSeqVersion\" INT8,"
			+ "\"eSeqVersion\" INT8,"
			+ "\"typeId\" INT4 REFERENCES \""
			+ GraphDatabase.TABLE_TYPE + "\"(\"typeId\")" + ");";

	@Override
	public PreparedStatement createGraphTableWithConstraints()
			throws SQLException {
		return connection.prepareStatement(CREATE_GRAPH_TABLE);
	}

	private static final String CREATE_VERTEX_TABLE = "CREATE TABLE \""
			+ GraphDatabase.TABLE_VERTEX + "\"(" + "\"vId\" INT4,"
			+ "\"gId\" INT4," + "\"typeId\" INT4,"
			+ "\"lambdaSeqVersion\" INT8," + // TODO Remove as this is really
			// only needed while an Iterator
			// is in memory
			"\"sequenceNumber\" INT8" + ");";

	@Override
	public PreparedStatement createVertexTable() throws SQLException {
		return connection.prepareStatement(CREATE_VERTEX_TABLE);
	}

	private static final String ADD_PRIMARY_KEY_CONSTRAINT_ON_VERTEX_TABLE = "ALTER TABLE \""
			+ GraphDatabase.TABLE_VERTEX
			+ "\" ADD CONSTRAINT \"vertexPrimaryKey\" PRIMARY KEY ( \"vId\", \"gId\" );";

	@Override
	public PreparedStatement addPrimaryKeyConstraintOnVertexTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_PRIMARY_KEY_CONSTRAINT_ON_VERTEX_TABLE);
	}

	private static final String DROP_PRIMARY_KEY_CONSTRAINT_FROM_VERTEX_TABLE = "ALTER TABLE \""
			+ GraphDatabase.TABLE_VERTEX
			+ "\" DROP CONSTRAINT \"vertexPrimaryKey\";";

	@Override
	public PreparedStatement dropPrimaryKeyConstraintFromVertexTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_PRIMARY_KEY_CONSTRAINT_FROM_VERTEX_TABLE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_VERTEX = "ALTER TABLE \""
			+ GraphDatabase.TABLE_VERTEX
			+ "\" ADD CONSTRAINT \"gIdIsForeignKey\" FOREIGN KEY (\"gId\") REFERENCES \""
			+ GraphDatabase.TABLE_GRAPH + "\" (\"gId\");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnGraphColumnOfVertexTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_VERTEX);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_VERTEX_TYPE = "ALTER TABLE \""
			+ GraphDatabase.TABLE_VERTEX
			+ "\" ADD CONSTRAINT \"typeIdIsForeignKey\" FOREIGN KEY (\"typeId\") REFERENCES \""
			+ GraphDatabase.TABLE_TYPE + "\" (\"typeId\");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnTypeColumnOfVertexTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_VERTEX_TYPE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_VERTEX = "ALTER TABLE \""
			+ GraphDatabase.TABLE_VERTEX
			+ "\" DROP CONSTRAINT \"gIdIsForeignKey\";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromGraphColumnOfVertexTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_VERTEX);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_VERTEX_TYPE = "ALTER TABLE \""
			+ GraphDatabase.TABLE_VERTEX
			+ "\" DROP CONSTRAINT \"typeIdIsForeignKey\";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromTypeColumnOfVertexTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_VERTEX_TYPE);
	}

	private static final String CREATE_EDGE_TABLE = "CREATE TABLE \""
			+ GraphDatabase.TABLE_EDGE + "\"(" + "\"eId\" INT4,"
			+ "\"gId\" INT4," + "\"typeId\" INT4," + "\"sequenceNumber\" INT8"
			+ ");";

	@Override
	public PreparedStatement createEdgeTable() throws SQLException {
		return connection.prepareStatement(CREATE_EDGE_TABLE);
	}

	private static final String ADD_PRIMARY_KEY_CONSTRAINT_ON_EDGE_TABLE = "ALTER TABLE \""
			+ GraphDatabase.TABLE_EDGE
			+ "\" ADD CONSTRAINT \"edgePrimaryKey\" PRIMARY KEY ( \"eId\", \"gId\" );";

	@Override
	public PreparedStatement addPrimaryKeyConstraintOnEdgeTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_PRIMARY_KEY_CONSTRAINT_ON_EDGE_TABLE);
	}

	private static final String DROP_PRIMARY_KEY_CONSTRAINT_FROM_EDGE_TABLE = "ALTER TABLE \""
			+ GraphDatabase.TABLE_EDGE
			+ "\" DROP CONSTRAINT \"edgePrimaryKey\";";

	@Override
	public PreparedStatement dropPrimaryKeyConstraintFromEdgeTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_PRIMARY_KEY_CONSTRAINT_FROM_EDGE_TABLE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_EDGE = "ALTER TABLE \""
			+ GraphDatabase.TABLE_EDGE
			+ "\" ADD CONSTRAINT \"gIdIsForeignKey\" FOREIGN KEY (\"gId\") REFERENCES \""
			+ GraphDatabase.TABLE_GRAPH + "\" (\"gId\");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnGraphColumnOfEdgeTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_EDGE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_EDGE_TYPE = "ALTER TABLE \""
			+ GraphDatabase.TABLE_EDGE
			+ "\" ADD CONSTRAINT \"typeIdIsForeignKey\" FOREIGN KEY (\"typeId\") REFERENCES \""
			+ GraphDatabase.TABLE_TYPE + "\" (\"typeId\");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnTypeColumnOfEdgeTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_EDGE_TYPE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_EDGE = "ALTER TABLE \""
			+ GraphDatabase.TABLE_EDGE
			+ "\" DROP CONSTRAINT \"gIdIsForeignKey\";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromGraphColumnOfEdgeTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_EDGE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_EDGE_TYPE = "ALTER TABLE \""
			+ GraphDatabase.TABLE_EDGE
			+ "\" DROP CONSTRAINT \"typeIdIsForeignKey\";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromTypeColumnOfEdgeTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_EDGE_TYPE);
	}

	private static final String CREATE_INCIDENCE_TABLE = "CREATE TYPE \"DIRECTION\" AS ENUM( 'OUT', 'IN' );"
			+ "CREATE TABLE \""
			+ GraphDatabase.TABLE_INCIDENCE
			+ "\"("
			+ "\"eId\" INT4,"
			+ "\"vId\" INT4,"
			+ "\"gId\" INT4,"
			+ "direction \"DIRECTION\"," + "\"sequenceNumber\" INT8" + ");";

	@Override
	public PreparedStatement createIncidenceTable() throws SQLException {
		return connection.prepareStatement(CREATE_INCIDENCE_TABLE);
	}

	private static final String ADD_PRIMARY_KEY_CONSTRAINT_ON_INCIDENCE_TABLE = "ALTER TABLE \""
			+ GraphDatabase.TABLE_INCIDENCE
			+ "\" ADD CONSTRAINT \"incidencePrimaryKey\" PRIMARY KEY ( \"eId\", \"gId\", direction );";

	@Override
	public PreparedStatement addPrimaryKeyConstraintOnIncidenceTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_PRIMARY_KEY_CONSTRAINT_ON_INCIDENCE_TABLE);
	}

	private static final String DROP_PRIMARY_KEY_CONSTRAINT_FROM_INCIDENCE_TABLE = "ALTER TABLE \""
			+ GraphDatabase.TABLE_INCIDENCE
			+ "\" DROP CONSTRAINT \"incidencePrimaryKey\";";

	@Override
	public PreparedStatement dropPrimaryKeyConstraintFromIncidenceTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_PRIMARY_KEY_CONSTRAINT_FROM_INCIDENCE_TABLE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_INCIDENCE = "ALTER TABLE \""
			+ GraphDatabase.TABLE_INCIDENCE
			+ "\" ADD CONSTRAINT \"gIdIsForeignKey\" FOREIGN KEY (\"gId\") REFERENCES \""
			+ GraphDatabase.TABLE_GRAPH + "\" (\"gId\");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnGraphColumnOfIncidenceTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_INCIDENCE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_EDGE_OF_INCIDENCE = "ALTER TABLE \""
			+ GraphDatabase.TABLE_INCIDENCE
			+ "\" ADD CONSTRAINT \"eIdIsForeignKey\" FOREIGN KEY (\"eId\", \"gId\") REFERENCES \""
			+ GraphDatabase.TABLE_EDGE + "\" (\"eId\", \"gId\");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnEdgeColumnOfIncidenceTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_EDGE_OF_INCIDENCE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_VERTEX_OF_INCIDENCE = "ALTER TABLE \""
			+ GraphDatabase.TABLE_INCIDENCE
			+ "\" ADD CONSTRAINT \"vIdIsForeignKey\" FOREIGN KEY (\"vId\", \"gId\") REFERENCES \""
			+ GraphDatabase.TABLE_VERTEX + "\" (\"vId\", \"gId\");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnVertexColumnOfIncidenceTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_VERTEX_OF_INCIDENCE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_EDGE_OF_INCIDENCE = "ALTER TABLE \""
			+ GraphDatabase.TABLE_INCIDENCE
			+ "\" DROP CONSTRAINT \"eIdIsForeignKey\";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromEdgeColumnOfIncidenceTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_EDGE_OF_INCIDENCE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_INCIDENCE = "ALTER TABLE \""
			+ GraphDatabase.TABLE_INCIDENCE
			+ "\" DROP CONSTRAINT \"gIdIsForeignKey\";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromGraphColumnOfIncidenceTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_INCIDENCE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_VERTEX_OF_INCIDENCE = "ALTER TABLE \""
			+ GraphDatabase.TABLE_INCIDENCE
			+ "\" DROP CONSTRAINT \"vIdIsForeignKey\";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromVertexColumnOfIncidenceTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_VERTEX_OF_INCIDENCE);
	}

	private static final String CREATE_INDEX_ON_LAMBDA_SEQ = "CREATE INDEX \"lambdaSeqIndex\" ON \""
			+ GraphDatabase.TABLE_INCIDENCE
			+ "\"( \"vId\", \"gId\", \"sequenceNumber\" ASC ) WITH (FILLFACTOR=80);"
			+ "ALTER TABLE \""
			+ GraphDatabase.TABLE_INCIDENCE
			+ "\" CLUSTER ON \"lambdaSeqIndex\";"
			+ "ANALYZE \""
			+ GraphDatabase.TABLE_INCIDENCE + "\";";

	@Override
	public PreparedStatement addIndexOnLambdaSeq() throws SQLException {
		return getPreparedStatement(CREATE_INDEX_ON_LAMBDA_SEQ);
	}

	private static final String DROP_INDEX_ON_LAMBDA_SEQ = "DROP INDEX IF EXISTS \"lambdaSeqIndex\";";

	@Override
	public PreparedStatement dropIndexOnLambdaSeq() throws SQLException {
		return getPreparedStatement(DROP_INDEX_ON_LAMBDA_SEQ);
	}

	private static final String CLUSTER_INCIDENCES = "CLUSTER \""
			+ GraphDatabase.TABLE_INCIDENCE + "\";";

	public PreparedStatement clusterIncidenceTable() throws SQLException {
		return getPreparedStatement(CLUSTER_INCIDENCES);
	}

	private static final String CREATE_ATTRIBUTE_TABLE = "CREATE SEQUENCE \"attributeIdSequence\";"
			+ "CREATE TABLE \""
			+ GraphDatabase.TABLE_ATTRIBUTE
			+ "\"("
			+ "\"attributeId\" INT4 PRIMARY KEY DEFAULT NEXTVAL('\"attributeIdSequence\"'),"
			+ "name TEXT,"
			+ "\"schemaId\" INT4 REFERENCES \""
			+ GraphDatabase.TABLE_SCHEMA
			+ "\" ON DELETE CASCADE"
			+ ");";

	@Override
	public PreparedStatement createAttributeTableWithConstraints()
			throws SQLException {
		return connection.prepareStatement(CREATE_ATTRIBUTE_TABLE);
	}

	private static final String CREATE_GRAPH_ATTRIBUTE_VALUE_TABLE = "CREATE TABLE \""
			+ GraphDatabase.TABLE_GRAPH_ATTRIBUTE_VALUE
			+ "\"("
			+ "\"gId\" INT4,"
			+ "\"attributeId\" INT4 REFERENCES \""
			+ GraphDatabase.TABLE_ATTRIBUTE
			+ "\" (\"attributeId\"),"
			+ "\"value\" TEXT,"
			+ "CONSTRAINT \"gaPrimaryKey\" PRIMARY KEY ( \"gId\", \"attributeId\" )"
			+ ");";

	@Override
	public PreparedStatement createGraphAttributeValueTableWithConstraints()
			throws SQLException {
		return connection.prepareStatement(CREATE_GRAPH_ATTRIBUTE_VALUE_TABLE);
	}

	private static final String CREATE_VERTEX_ATTRIBUTE_VALUE_TABLE = "CREATE TABLE \""
			+ GraphDatabase.TABLE_VERTEX_ATTRIBUTE_VALUE
			+ "\"("
			+ "\"vId\" INT4,"
			+ "\"gId\" INT4,"
			+ "\"attributeId\" INT4,"
			+ "\"value\" TEXT" + // TODO Replace by NVARCHAR(k).
			");";

	@Override
	public PreparedStatement createVertexAttributeValueTable()
			throws SQLException {
		return connection.prepareStatement(CREATE_VERTEX_ATTRIBUTE_VALUE_TABLE);
	}

	private static final String ADD_PRIMARY_KEY_CONSTRAINT_ON_VERTEX_ATTRIBUTE_VALUE_TABLE = "ALTER TABLE \""
			+ GraphDatabase.TABLE_VERTEX_ATTRIBUTE_VALUE
			+ "\" ADD CONSTRAINT \"vertexAttributeValuePrimaryKey\" PRIMARY KEY ( \"vId\", \"gId\", \"attributeId\" );";

	@Override
	public PreparedStatement addPrimaryKeyConstraintOnVertexAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_PRIMARY_KEY_CONSTRAINT_ON_VERTEX_ATTRIBUTE_VALUE_TABLE);
	}

	private static final String DROP_PRIMARY_KEY_CONSTRAINT_FROM_VERTEX_ATTRIBUTE_VALUE_TABLE = "ALTER TABLE \""
			+ GraphDatabase.TABLE_VERTEX_ATTRIBUTE_VALUE
			+ "\" DROP CONSTRAINT \"vertexAttributeValuePrimaryKey\";";

	@Override
	public PreparedStatement dropPrimaryKeyConstraintFromVertexAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_PRIMARY_KEY_CONSTRAINT_FROM_VERTEX_ATTRIBUTE_VALUE_TABLE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_VERTEX_ATTRIBUTE_VALUE = "ALTER TABLE \""
			+ GraphDatabase.TABLE_VERTEX_ATTRIBUTE_VALUE
			+ "\" ADD CONSTRAINT \"gIdIsForeignKey\" FOREIGN KEY (\"gId\") REFERENCES \""
			+ GraphDatabase.TABLE_GRAPH + "\" (\"gId\");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnGraphColumnOfVertexAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_VERTEX_ATTRIBUTE_VALUE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_VERTEX_OF_ATTRIBUTE_VALUE = "ALTER TABLE \""

			+ GraphDatabase.TABLE_VERTEX_ATTRIBUTE_VALUE
			+ "\" ADD CONSTRAINT \"vIdIsForeignKey\" FOREIGN KEY (\"vId\", \"gId\") REFERENCES \""
			+ GraphDatabase.TABLE_VERTEX + "\" (\"vId\", \"gId\");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnVertexColumnOfVertexAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_VERTEX_OF_ATTRIBUTE_VALUE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_ATTRIBUTE_OF_VERTEX_ATTRIBUTE_VALUE = "ALTER TABLE \""

			+ GraphDatabase.TABLE_VERTEX_ATTRIBUTE_VALUE
			+ "\" ADD CONSTRAINT \"attributeIdIsForeignKey\" FOREIGN KEY (\"attributeId\" ) REFERENCES \""

			+ GraphDatabase.TABLE_ATTRIBUTE + "\" (\"attributeId\");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnAttributeColumnOfVertexAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_ATTRIBUTE_OF_VERTEX_ATTRIBUTE_VALUE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_VERTEX_ATTRIBUTE_VALUE = "ALTER TABLE \""

			+ GraphDatabase.TABLE_VERTEX_ATTRIBUTE_VALUE
			+ "\" DROP CONSTRAINT \"gIdIsForeignKey\";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromGraphColumnOfVertexAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_VERTEX_ATTRIBUTE_VALUE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_VERTEX_OF_ATTRIBUTE_VALUE = "ALTER TABLE \""

			+ GraphDatabase.TABLE_VERTEX_ATTRIBUTE_VALUE
			+ "\" DROP CONSTRAINT \"vIdIsForeignKey\";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromVertexColumnOfVertexAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_VERTEX_OF_ATTRIBUTE_VALUE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_ATTRIBUTE_OF_VERTEX_ATTRIBUTE_VALUE = "ALTER TABLE \""

			+ GraphDatabase.TABLE_VERTEX_ATTRIBUTE_VALUE
			+ "\" DROP CONSTRAINT \"attributeIdIsForeignKey\";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromAttributeColumnOfVertexAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_ATTRIBUTE_OF_VERTEX_ATTRIBUTE_VALUE);
	}

	private static final String CREATE_EDGE_ATTRIBUTE_VALUE_TABLE = "CREATE TABLE \""

	+ GraphDatabase.TABLE_EDGE_ATTRIBUTE_VALUE + "\"(" + "\"eId\" INT4,"
			+ "\"gId\" INT4," + "\"attributeId\" INT4," + "\"value\" TEXT" + // TODO
			// Replace
			// by
			// NVARCHAR(k)
			");";

	@Override
	public PreparedStatement createEdgeAttributeValueTable()
			throws SQLException {
		return connection.prepareStatement(CREATE_EDGE_ATTRIBUTE_VALUE_TABLE);
	}

	private static final String ADD_PRIMARY_KEY_CONSTRAINT_ON_EDGE_ATTRIBUTE_VALUE_TABLE = "ALTER TABLE \""

			+ GraphDatabase.TABLE_EDGE_ATTRIBUTE_VALUE
			+ "\" ADD CONSTRAINT \"edgeAttributeValuePrimaryKey\" PRIMARY KEY ( \"eId\", \"gId\", \"attributeId\" );";

	@Override
	public PreparedStatement addPrimaryKeyConstraintOnEdgeAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_PRIMARY_KEY_CONSTRAINT_ON_EDGE_ATTRIBUTE_VALUE_TABLE);
	}

	private static final String DROP_PRIMARY_KEY_CONSTRAINT_FROM_EDGE_ATTRIBUTE_VALUE_TABLE = "ALTER TABLE \""

			+ GraphDatabase.TABLE_EDGE_ATTRIBUTE_VALUE
			+ "\" DROP CONSTRAINT \"edgeAttributeValuePrimaryKey\";";

	@Override
	public PreparedStatement dropPrimaryKeyConstraintFromEdgeAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_PRIMARY_KEY_CONSTRAINT_FROM_EDGE_ATTRIBUTE_VALUE_TABLE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_EDGE_ATTRIBUTE_VALUE = "ALTER TABLE \""

			+ GraphDatabase.TABLE_EDGE_ATTRIBUTE_VALUE
			+ "\" ADD CONSTRAINT \"gIdIsForeignKey\" FOREIGN KEY (\"gId\") REFERENCES \""
			+ GraphDatabase.TABLE_GRAPH + "\" (\"gId\");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnGraphColumnOfEdgeAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_EDGE_ATTRIBUTE_VALUE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_EDGE_OF_ATTRIBUTE_VALUE = "ALTER TABLE \""

			+ GraphDatabase.TABLE_EDGE_ATTRIBUTE_VALUE
			+ "\" ADD CONSTRAINT \"eIdIsForeignKey\" FOREIGN KEY (\"eId\", \"gId\") REFERENCES \""
			+ GraphDatabase.TABLE_EDGE + "\" (\"eId\", \"gId\");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnEdgeColumnOfEdgeAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_EDGE_OF_ATTRIBUTE_VALUE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_EDGE_ATTRIBUTE = "ALTER TABLE \""

			+ GraphDatabase.TABLE_EDGE_ATTRIBUTE_VALUE
			+ "\" ADD CONSTRAINT \"attributeIdIsForeignKey\" FOREIGN KEY (\"attributeId\" ) REFERENCES \""

			+ GraphDatabase.TABLE_ATTRIBUTE + "\" (\"attributeId\");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnAttributeColumnOfEdgeAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_EDGE_ATTRIBUTE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_EDGE_ATTRIBUTE = "ALTER TABLE \""

			+ GraphDatabase.TABLE_EDGE_ATTRIBUTE_VALUE
			+ "\" DROP CONSTRAINT \"gIdIsForeignKey\";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromGraphColumnOfEdgeAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_EDGE_ATTRIBUTE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_EDGE_OF_ATTRIBUTE_VALUE = "ALTER TABLE \""

			+ GraphDatabase.TABLE_EDGE_ATTRIBUTE_VALUE
			+ "\" DROP CONSTRAINT \"eIdIsForeignKey\";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromEdgeColumnOfEdgeAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_EDGE_OF_ATTRIBUTE_VALUE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_EDGE_ATTRIBUTE = "ALTER TABLE \""

			+ GraphDatabase.TABLE_EDGE_ATTRIBUTE_VALUE
			+ "\" DROP CONSTRAINT \"attributeIdIsForeignKey\";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromAttributeColumnOfEdgeAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_EDGE_ATTRIBUTE);
	}

	/*
	 * private static final String ADD_CLUSTERED_INDEX_ON_EDGE_ATTRIBUTE_VALUES =
	 * "CREATE INDEX \"edgeAttributeValueIndex\" ON \""
	 * +JGraLab.getDatabaseTablePrefix() +
	 * GraphDatabase.EDGE_ATTRIBUTE_VALUE_TABLE_NAME
	 * +"\"( \"eId\" ASC, \"gId\" ASC, \"attributeId\" ) WITH (FILLFACTOR=80);"
	 * + "ALTER TABLE \""+JGraLab.getDatabaseTablePrefix() +
	 * GraphDatabase.EDGE_ATTRIBUTE_VALUE_TABLE_NAME
	 * +"\" CLUSTER ON \"edgeAttributeValueIndex\";" +
	 * "ANALYZE \""+JGraLab.getDatabaseTablePrefix() +
	 * GraphDatabase.EDGE_ATTRIBUTE_VALUE_TABLE_NAME +"\";";
	 * 
	 * @Override public PreparedStatement
	 * addClusteredIndexOnEdgeAttributeValues()throws SQLException { return
	 * this.getPreparedStatement(ADD_CLUSTERED_INDEX_ON_EDGE_ATTRIBUTE_VALUES);
	 * }
	 * 
	 * private static final String ADD_CLUSTERED_INDEX_ON_VERTEX_ATTRIBUTE_VALUES =
	 * "CREATE INDEX \"vertexAttributeValueIndex\" ON \""
	 * +JGraLab.getDatabaseTablePrefix() +
	 * GraphDatabase.VERTEX_ATTRIBUTE_VALUE_TABLE_NAME
	 * +"\"( \"vId\" ASC, \"gId\" ASC, \"attributeId\" ) WITH (FILLFACTOR=80);"
	 * + "ALTER TABLE \""+JGraLab.getDatabaseTablePrefix() +
	 * GraphDatabase.VERTEX_ATTRIBUTE_VALUE_TABLE_NAME
	 * +"\" CLUSTER ON \"vertexAttributeValueIndex\";" +
	 * "ANALYZE \""+JGraLab.getDatabaseTablePrefix() +
	 * GraphDatabase.VERTEX_ATTRIBUTE_VALUE_TABLE_NAME +"\";";
	 * 
	 * @Override public PreparedStatement
	 * addClusteredIndexOnVertexAttributeValues()throws SQLException { return
	 * this
	 * .getPreparedStatement(ADD_CLUSTERED_INDEX_ON_VERTEX_ATTRIBUTE_VALUES); }
	 * 
	 * private static final String ADD_CLUSTERED_INDEX_ON_GRAPH_ATTRIBUTE_VALUES =
	 * "CREATE INDEX \"graphAttributeValueIndex\" ON \""
	 * +JGraLab.getDatabaseTablePrefix() +
	 * GraphDatabase.GRAPH_ATTRIBUTE_VALUE_TABLE_NAME
	 * +"\"( \"gId\" ASC, \"attributeId\" ) WITH (FILLFACTOR=80);" +
	 * "ALTER TABLE \""+JGraLab.getDatabaseTablePrefix() +
	 * GraphDatabase.GRAPH_ATTRIBUTE_VALUE_TABLE_NAME
	 * +"\" CLUSTER ON \"graphAttributeValueIndex\";" +
	 * "ANALYZE \""+JGraLab.getDatabaseTablePrefix() +
	 * GraphDatabase.GRAPH_ATTRIBUTE_VALUE_TABLE_NAME +"\";";
	 * 
	 * @Override public PreparedStatement
	 * addClusteredIndexOnGraphAttributeValues()throws SQLException { return
	 * this.getPreparedStatement(ADD_CLUSTERED_INDEX_ON_GRAPH_ATTRIBUTE_VALUES);
	 * }
	 * 
	 * private static final String DROP_CLUSTERED_INDICES_FROM_ATTRIBUTE_VALUES =
	 * "DROP INDEX IF EXISTS \"edgeAttributeValueIndex\";" +
	 * "DROP INDEX IF EXISTS \"vertexAttributeValueIndex\";" +
	 * "DROP INDEX IF EXISTS \"graphAttributeValueIndex\";";
	 * 
	 * @Override public PreparedStatement
	 * dropClusteredIndicesOnAttributeValues() throws SQLException { return
	 * this.getPreparedStatement(DROP_CLUSTERED_INDICES_FROM_ATTRIBUTE_VALUES);
	 * }
	 * 
	 * private static final String CLUSTER_ATTRIBUTE_VALUES =
	 * "CLUSTER \""+JGraLab.getDatabaseTablePrefix() +
	 * GraphDatabase.GRAPH_ATTRIBUTE_VALUE_TABLE_NAME +"\";" +
	 * "CLUSTER \""+JGraLab.getDatabaseTablePrefix() +
	 * GraphDatabase.VERTEX_ATTRIBUTE_VALUE_TABLE_NAME +"\";" +
	 * "CLUSTER \""+JGraLab.getDatabaseTablePrefix() +
	 * GraphDatabase.EDGE_ATTRIBUTE_VALUE_TABLE_NAME +"\";";
	 * 
	 * @Override public PreparedStatement clusterAttributeValues() throws
	 * SQLException { return
	 * this.getPreparedStatement(CLUSTER_ATTRIBUTE_VALUES); }
	 */

	private static final String INSERT_SCHEMA = "INSERT INTO \""

			+ GraphDatabase.TABLE_SCHEMA
			+ "\" ( \"packagePrefix\", name, \"serializedDefinition\" ) VALUES ( ?, ?, ? )";

	@Override
	public PreparedStatement insertSchema(Schema schema,
			String serializedDefinition) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(
				INSERT_SCHEMA, Statement.RETURN_GENERATED_KEYS);
		statement.setString(1, schema.getPackagePrefix());
		statement.setString(2, schema.getName());
		statement.setString(3, serializedDefinition);
		return statement;
	}

	private static final String INSERT_TYPE = "INSERT INTO \""
			+ GraphDatabase.TABLE_TYPE
			+ "\"( \"qualifiedName\", \"schemaId\" ) VALUES ( ?, ? )";

	@Override
	public PreparedStatement insertType(String qualifiedName, int schemaId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(INSERT_TYPE);
		statement.setString(1, qualifiedName);
		statement.setInt(2, schemaId);
		return statement;
	}

	private static final String INSERT_ATTRIBUTE = "INSERT INTO \""

	+ GraphDatabase.TABLE_ATTRIBUTE
			+ "\" ( name, \"schemaId\" ) VALUES ( ?, ? )";

	@Override
	public PreparedStatement insertAttribute(String name, int schemaId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(INSERT_ATTRIBUTE);
		statement.setString(1, name);
		statement.setInt(2, schemaId);
		return statement;
	}

	// --- to insert a graph ------------------------------------------

	private static final String INSERT_GRAPH = "INSERT INTO \""
			+ GraphDatabase.TABLE_GRAPH
			+ "\" ( uid, version, \"vSeqVersion\", \"eSeqVersion\", \"typeId\" ) VALUES ( ?, ?, ?, ?, ? )";

	@Override
	public PreparedStatement insertGraph(String id, long graphVersion,
			long vertexListVersion, long edgeListVersion, int typeId)
			throws SQLException {
		PreparedStatement statement = connection.prepareStatement(INSERT_GRAPH,
				Statement.RETURN_GENERATED_KEYS);
		statement.setString(1, id);
		statement.setLong(2, graphVersion);
		statement.setLong(3, vertexListVersion);
		statement.setLong(4, edgeListVersion);
		statement.setInt(5, typeId);
		return statement;
	}

	private static final String INSERT_GRAPH_ATTRIBUTE_VALUE = "INSERT INTO \""

	+ GraphDatabase.TABLE_GRAPH_ATTRIBUTE_VALUE
			+ "\" ( \"gId\", \"attributeId\", value ) VALUES ( ?, ?, ? )";

	@Override
	public PreparedStatement insertGraphAttributeValue(int gId,
			int attributeId, String value) throws SQLException {
		PreparedStatement statement = getPreparedStatement(INSERT_GRAPH_ATTRIBUTE_VALUE);
		statement.setInt(1, gId);
		statement.setInt(2, attributeId);
		statement.setString(3, value);
		return statement;
	}

	// --- to insert a vertex ------------------------------------------

	private static final String INSERT_VERTEX = "INSERT INTO \""
			+ GraphDatabase.TABLE_VERTEX
			+ "\" ( \"vId\", \"gId\", \"typeId\", \"lambdaSeqVersion\", \"sequenceNumber\" ) VALUES (?, ?, ?, ?, ?);";

	@Override
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

	@Override
	public PreparedStatement insertVertex(DatabasePersistableVertex vertex)
			throws SQLException, GraphIOException {
		String sqlStatement = createSqlInsertStatementFor(vertex);
		PreparedStatement statement = getPreparedStatement(sqlStatement);
		setParametersForVertex(statement, vertex);
		setAttributeValuesForVertex(statement, vertex);
		return statement;
	}

	private void setAttributeValuesForVertex(PreparedStatement statement,
			DatabasePersistableVertex vertex) throws SQLException,
			GraphIOException {
		int i = 6;
		SortedSet<Attribute> attributes = vertex.getAttributedElementClass()
				.getAttributeList();
		for (Attribute attribute : attributes) {
			statement.setInt(i, vertex.getId());
			i++;
			statement.setInt(i, vertex.getGId());
			i++;
			int attributeId = graphDatabase.getAttributeId(vertex.getGraph(),
					attribute.getName());
			statement.setInt(i, attributeId);
			i++;
			String value = graphDatabase.convertToString(vertex, attribute
					.getName());
			statement.setString(i, value);
			i++;
		}
	}

	private void setParametersForVertex(PreparedStatement statement,
			DatabasePersistableVertex vertex) throws SQLException {
		statement.setInt(1, vertex.getId());
		statement.setInt(2, vertex.getGId());
		int typeId = graphDatabase.getTypeIdOf(vertex);
		statement.setInt(3, typeId);
		statement.setLong(4, vertex.getIncidenceListVersion());
		statement.setLong(5, vertex.getSequenceNumberInVSeq());
	}

	private static final String createSqlInsertStatementFor(DatabasePersistableVertex vertex) {
		String sqlStatement = INSERT_VERTEX;
		int attributeCount = vertex.getAttributedElementClass()
				.getAttributeList().size();
		for (int i = 0; i < attributeCount; i++) {
			sqlStatement += INSERT_VERTEX_ATTRIBUTE_VALUE;
		}
		return sqlStatement;
	}

	private static final String INSERT_VERTEX_ATTRIBUTE_VALUE = "INSERT INTO \""

			+ GraphDatabase.TABLE_VERTEX_ATTRIBUTE_VALUE
			+ "\" ( \"vId\", \"gId\", \"attributeId\", value ) VALUES ( ?, ?, ?, ? );";

	@Override
	public PreparedStatement insertVertexAttributeValue(int vId, int gId,
			int attributeId, String value) throws SQLException {
		PreparedStatement statement = getPreparedStatement(INSERT_VERTEX_ATTRIBUTE_VALUE);
		statement.setInt(1, vId);
		statement.setInt(2, gId);
		statement.setInt(3, attributeId);
		statement.setString(4, value);
		return statement;
	}

	// --- to insert an edge -------------------------------------------

	private static final String INSERT_EDGE = "INSERT INTO \""
			+ GraphDatabase.TABLE_EDGE
			+ "\" ( \"eId\", \"gId\", \"typeId\", \"sequenceNumber\" ) VALUES ( ?, ?, ?, ? );";

	@Override
	public PreparedStatement insertEdge(int eId, int gId, int typeId,
			long sequenceNumberInLambdaSeq) throws SQLException {
		PreparedStatement statement = getPreparedStatement(INSERT_EDGE);
		statement.setInt(1, Math.abs(eId));
		statement.setInt(2, gId);
		statement.setInt(3, typeId);
		statement.setLong(4, sequenceNumberInLambdaSeq);
		return statement;
	}

	@Override
	public PreparedStatement insertEdge(DatabasePersistableEdge edge,
			DatabasePersistableVertex alpha, DatabasePersistableVertex omega)
			throws SQLException, GraphIOException {
		String sqlStatement = createSqlInsertStatementFor(edge);
		PreparedStatement statement = getPreparedStatement(sqlStatement);
		setParametersForEdge(statement, edge);

		// insert incidence: normal edge
		statement.setInt(5, edge.getId());
		statement.setInt(6, edge.getGId());
		statement.setInt(7, edge.getIncidentVId());
		statement.setString(8, EdgeDirection.OUT.name());
		statement.setLong(9, edge.getSequenceNumberInLambdaSeq());

		// insert incidence: reversed edge
		DatabasePersistableEdge reversedEdge = (DatabasePersistableEdge) edge
				.getReversedEdge();
		statement.setInt(10, Math.abs(reversedEdge.getId()));
		statement.setInt(11, reversedEdge.getGId());
		statement.setInt(12, reversedEdge.getIncidentVId());
		statement.setString(13, EdgeDirection.IN.name());
		statement.setLong(14, reversedEdge.getSequenceNumberInLambdaSeq());

		// insert attribute values
		int i = 15;
		SortedSet<Attribute> attributes = edge.getAttributedElementClass()
				.getAttributeList();
		for (Attribute attribute : attributes) {
			statement.setInt(i, edge.getId());
			i++;
			statement.setInt(i, edge.getGId());
			i++;
			int attributeId = graphDatabase.getAttributeId(edge.getGraph(),
					attribute.getName());
			statement.setInt(i, attributeId);
			i++;
			String value = graphDatabase.convertToString(edge, attribute
					.getName());
			statement.setString(i, value);
			i++;
		}

		// update incidence list version of alpha
		statement.setLong(i, alpha.getIncidenceListVersion());
		i++;
		statement.setInt(i, alpha.getId());
		i++;
		statement.setInt(i, alpha.getGId());
		i++;

		// update incidence list version of omega
		statement.setLong(i, omega.getIncidenceListVersion());
		i++;
		statement.setInt(i, omega.getId());
		i++;
		statement.setInt(i, omega.getGId());

		return statement;
	}

	private void setParametersForEdge(PreparedStatement statement,
			DatabasePersistableEdge edge) throws SQLException {
		statement.setInt(1, Math.abs(edge.getId()));
		statement.setInt(2, edge.getGId());
		int typeId = graphDatabase.getTypeIdOf(edge);
		statement.setInt(3, typeId);
		statement.setLong(4, edge.getSequenceNumberInESeq());
	}

	private static final String createSqlInsertStatementFor(DatabasePersistableEdge edge) {
		String sqlStatement = INSERT_EDGE;
		sqlStatement += INSERT_INCIDENCE;
		sqlStatement += INSERT_INCIDENCE;
		int attributeCount = edge.getAttributedElementClass()
				.getAttributeList().size();
		for (int i = 0; i < attributeCount; i++) {
			sqlStatement += INSERT_EDGE_ATTRIBUTE_VALUE;
		}
		sqlStatement += UPDATE_INCIDENCE_LIST_VERSION;
		sqlStatement += UPDATE_INCIDENCE_LIST_VERSION;
		return sqlStatement;
	}

	private static final String INSERT_INCIDENCE = "INSERT INTO \""
			+ GraphDatabase.TABLE_INCIDENCE
			+ "\" ( \"eId\", \"gId\", \"vId\", direction, \"sequenceNumber\" ) VALUES ( ?, ?, ?, ?::\"DIRECTION\", ? );";

	@Override
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

	private static final String INSERT_EDGE_ATTRIBUTE_VALUE = "INSERT INTO \""

			+ GraphDatabase.TABLE_EDGE_ATTRIBUTE_VALUE
			+ "\" ( \"eId\", \"gId\", \"attributeId\", value ) VALUES ( ?, ?, ?, ? );";

	@Override
	public PreparedStatement insertEdgeAttributeValue(int eId, int gId,
			int attributeId, String value) throws SQLException {
		PreparedStatement statement = getPreparedStatement(INSERT_EDGE_ATTRIBUTE_VALUE);
		statement.setInt(1, eId);
		statement.setInt(2, gId);
		statement.setInt(3, attributeId);
		statement.setString(4, value);
		return statement;
	}

	// --- to open a graph schema -------------------------------------------

	private static final String SELECT_SCHEMA_ID = "SELECT \"schemaId\" FROM \""

	+ GraphDatabase.TABLE_SCHEMA
			+ "\" WHERE \"packagePrefix\" = ? AND name = ?";

	@Override
	public PreparedStatement selectSchemaId(String packagePrefix, String name)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_SCHEMA_ID);
		statement.setString(1, packagePrefix);
		statement.setString(2, name);
		return statement;
	}

	private static final String SELECT_SCHEMA_DEFINITION_FOR_GRAPH = "SELECT \"serializedDefinition\" FROM \""

			+ GraphDatabase.TABLE_SCHEMA
			+ "\" WHERE \"schemaId\" = ("
			+ "SELECT \"schemaId\" FROM \""
			+ GraphDatabase.TABLE_TYPE
			+ "\" WHERE \"typeId\" = ("
			+ "SELECT \"typeId\" FROM \""
			+ GraphDatabase.TABLE_GRAPH
			+ "\" WHERE uid = ?" + ")" + ")";

	@Override
	public PreparedStatement selectSchemaDefinitionForGraph(String uid)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_SCHEMA_DEFINITION_FOR_GRAPH);
		statement.setString(1, uid);
		return statement;
	}

	private static final String SELECT_SCHEMA_NAME = "SELECT \"packagePrefix\", name FROM \""

			+ GraphDatabase.TABLE_SCHEMA
			+ "\" WHERE \"schemaId\" = ("
			+ "SELECT \"schemaId\" FROM \""
			+ GraphDatabase.TABLE_TYPE
			+ "\" WHERE \"typeId\" = ("
			+ "SELECT \"typeId\" FROM \""
			+ GraphDatabase.TABLE_GRAPH
			+ "\" WHERE uid = ?" + ")" + ")";

	@Override
	public PreparedStatement selectSchemaNameForGraph(String uid)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_SCHEMA_NAME);
		statement.setString(1, uid);
		return statement;
	}

	private static final String SELECT_TYPES = "SELECT \"qualifiedName\", \"typeId\" FROM \""
			+ GraphDatabase.TABLE_TYPE
			+ "\" WHERE \"schemaId\" = "
			+ "(SELECT \"schemaId\" FROM \""

			+ GraphDatabase.TABLE_SCHEMA
			+ "\" WHERE \"packagePrefix\" = ? AND name = ?)";

	@Override
	public PreparedStatement selectTypesOfSchema(String packagePrefix,
			String name) throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_TYPES);
		statement.setString(1, packagePrefix);
		statement.setString(2, name);
		return statement;
	}

	private static final String SELECT_ATTRIBUTES = "SELECT name, \"attributeId\" FROM \""

	+ GraphDatabase.TABLE_ATTRIBUTE + "\" WHERE \"schemaId\" = "
			+ "(SELECT \"schemaId\" FROM \""
			+ GraphDatabase.TABLE_SCHEMA
			+ "\" WHERE \"packagePrefix\" = ? AND name = ?)";

	@Override
	public PreparedStatement selectAttributesOfSchema(String packagePrefix,
			String name) throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_ATTRIBUTES);
		statement.setString(1, packagePrefix);
		statement.setString(2, name);
		return statement;
	}

	// --- to open a graph --------------------------------------------

	private static final String SELECT_GRAPH = "SELECT \"gId\", version, \"vSeqVersion\", \"eSeqVersion\" FROM \""
			+ GraphDatabase.TABLE_GRAPH + "\" WHERE uid = ?";

	@Override
	public PreparedStatement selectGraph(String id) throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_GRAPH);
		statement.setString(1, id);
		return statement;
	}

	private static final String COUNT_VERTICES_IN_GRAPH = "SELECT COUNT (*) FROM \""
			+ GraphDatabase.TABLE_VERTEX + "\" WHERE \"gId\" = ?";

	@Override
	public PreparedStatement countVerticesOfGraph(int gId) throws SQLException {
		PreparedStatement statement = getPreparedStatement(COUNT_VERTICES_IN_GRAPH);
		statement.setInt(1, gId);
		return statement;
	}

	private static final String COUNT_EDGES_IN_GRAPH = "SELECT COUNT (*) FROM \""
			+ GraphDatabase.TABLE_EDGE + "\" WHERE \"gId\" = ?";

	@Override
	public PreparedStatement countEdgesOfGraph(int gId) throws SQLException {
		PreparedStatement statement = getPreparedStatement(COUNT_EDGES_IN_GRAPH);
		statement.setInt(1, gId);
		return statement;
	}

	private static final String SELECT_VERTICES = "SELECT \"vId\", \"sequenceNumber\"  FROM \""
			+ GraphDatabase.TABLE_VERTEX
			+ "\" WHERE \"gId\" = ? ORDER BY \"sequenceNumber\" ASC";

	@Override
	public PreparedStatement selectVerticesOfGraph(int gId) throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_VERTICES);
		statement.setInt(1, gId);
		return statement;
	}

	private static final String SELECT_EDGES = "SELECT \"eId\", \"sequenceNumber\"  FROM \""
			+ GraphDatabase.TABLE_EDGE
			+ "\" WHERE \"gId\" = ? ORDER BY \"sequenceNumber\" ASC";

	@Override
	public PreparedStatement selectEdgesOfGraph(int gId) throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_EDGES);
		statement.setInt(1, gId);
		return statement;
	}

	private static final String SELECT_ATTRIBUTE_VALUES_OF_GRAPH = "SELECT name, \"value\" FROM \""

			+ GraphDatabase.TABLE_GRAPH_ATTRIBUTE_VALUE
			+ "\" JOIN \""
			+ GraphDatabase.TABLE_ATTRIBUTE
			+ "\" ON \""
			+ GraphDatabase.TABLE_GRAPH_ATTRIBUTE_VALUE
			+ "\".\"attributeId\" = \""
			+ GraphDatabase.TABLE_ATTRIBUTE
			+ "\".\"attributeId\" WHERE \"gId\" = ?";

	@Override
	public PreparedStatement selectAttributeValuesOfGraph(int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_ATTRIBUTE_VALUES_OF_GRAPH);
		statement.setInt(1, gId);
		return statement;
	}

	// --- to get a vertex -------------------------------------------

	private static final String SELECT_VERTEX_WITH_INCIDENCES = "SELECT \"typeId\", \"lambdaSeqVersion\", \""
			+ GraphDatabase.TABLE_VERTEX
			+ "\".\"sequenceNumber\", \""
			+ GraphDatabase.TABLE_INCIDENCE
			+ "\".\"sequenceNumber\", direction, \"eId\" FROM"
			+ "\""
			+ GraphDatabase.TABLE_VERTEX
			+ "\" LEFT OUTER JOIN \""
			+ GraphDatabase.TABLE_INCIDENCE
			+ "\" ON ( \""
			+ GraphDatabase.TABLE_VERTEX
			+ "\".\"vId\" = \""
			+ GraphDatabase.TABLE_INCIDENCE
			+ "\".\"vId\" AND \""
			+ GraphDatabase.TABLE_VERTEX
			+ "\".\"gId\" = \""
			+ GraphDatabase.TABLE_INCIDENCE
			+ "\".\"gId\" )"
			+ "WHERE \""
			+ GraphDatabase.TABLE_VERTEX
			+ "\".\"vId\" = ? AND \""
			+ GraphDatabase.TABLE_VERTEX
			+ "\".\"gId\" = ?"
			+ "ORDER BY \""
			+ GraphDatabase.TABLE_INCIDENCE
			+ "\".\"sequenceNumber\" ASC";

	@Override
	public PreparedStatement selectVertexWithIncidences(int vId, int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_VERTEX_WITH_INCIDENCES);
		statement.setInt(1, vId);
		statement.setInt(2, gId);
		return statement;
	}

	private static final String SELECT_ATTRIBUTE_VALUES_OF_VERTEX = "SELECT \"attributeId\", \"value\" FROM \""
			+ GraphDatabase.TABLE_VERTEX_ATTRIBUTE_VALUE
			+ "\" WHERE \"vId\" = ? AND \"gId\" = ?";

	@Override
	public PreparedStatement selectAttributeValuesOfVertex(int vId, int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_ATTRIBUTE_VALUES_OF_VERTEX);
		statement.setInt(1, vId);
		statement.setInt(2, gId);
		return statement;
	}

	// --- to get an edge --------------------------------------------

	private static final String SELECT_EDGE_WITH_INCIDENCES = "SELECT \"typeId\", \""
			+ GraphDatabase.TABLE_EDGE
			+ "\".\"sequenceNumber\", direction, \"vId\", \""
			+ GraphDatabase.TABLE_INCIDENCE + "\".\"sequenceNumber\" FROM"
			+ "\"" + GraphDatabase.TABLE_EDGE + "\" INNER JOIN \""
			+ GraphDatabase.TABLE_INCIDENCE + "\" ON ( \""
			+ GraphDatabase.TABLE_EDGE + "\".\"eId\" = \""
			+ GraphDatabase.TABLE_INCIDENCE + "\".\"eId\" AND \""
			+ GraphDatabase.TABLE_EDGE + "\".\"gId\" = \""
			+ GraphDatabase.TABLE_INCIDENCE + "\".\"gId\" )" + "WHERE \""
			+ GraphDatabase.TABLE_EDGE + "\".\"eId\" = ? AND \""
			+ GraphDatabase.TABLE_EDGE + "\".\"gId\" = ?";

	@Override
	public PreparedStatement selectEdgeWithIncidences(int eId, int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_EDGE_WITH_INCIDENCES);
		statement.setInt(1, eId);
		statement.setInt(2, gId);
		return statement;
	}

	private static final String SELECT_ATTRIBUTE_VALUES_OF_EDGE = "SELECT \"attributeId\", \"value\" FROM \""
			+ GraphDatabase.TABLE_EDGE_ATTRIBUTE_VALUE
			+ "\" WHERE \"eId\" = ? AND \"gId\" = ?";

	@Override
	public PreparedStatement selectAttributeValuesOfEdge(int eId, int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_ATTRIBUTE_VALUES_OF_EDGE);
		statement.setInt(1, eId);
		statement.setInt(2, gId);
		return statement;
	}

	// --- to delete a graph ------------------------------------------

	private static final String DELETE_ATTRIBUTE_VALUES_OF_GRAPH = "DELETE FROM \""
			+ GraphDatabase.TABLE_GRAPH_ATTRIBUTE_VALUE
			+ "\" WHERE \"gId\" = ?";

	@Override
	public PreparedStatement deleteAttributeValuesOfGraph(int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(DELETE_ATTRIBUTE_VALUES_OF_GRAPH);
		statement.setInt(1, gId);
		return statement;
	}

	private static final String DELETE_EDGE_ATTRIBUTE_VALUES_OF_GRAPH = "DELETE FROM \""
			+ GraphDatabase.TABLE_EDGE_ATTRIBUTE_VALUE
			+ "\" WHERE \"gId\" = ?";

	@Override
	public PreparedStatement deleteEdgeAttributeValuesOfGraph(int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(DELETE_EDGE_ATTRIBUTE_VALUES_OF_GRAPH);
		statement.setInt(1, gId);
		return statement;
	}

	private static final String DELETE_VERTEX_ATTRIBUTE_VALUES_OF_GRAPH = "DELETE FROM \""
			+ GraphDatabase.TABLE_VERTEX_ATTRIBUTE_VALUE
			+ "\" WHERE \"gId\" = ?";

	@Override
	public PreparedStatement deleteVertexAttributeValuesOfGraph(int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(DELETE_VERTEX_ATTRIBUTE_VALUES_OF_GRAPH);
		statement.setInt(1, gId);
		return statement;
	}

	private static final String DELETE_INCIDENCES_OF_GRAPH = "DELETE FROM \""
			+ GraphDatabase.TABLE_INCIDENCE + "\" WHERE \"gId\" = ?";

	@Override
	public PreparedStatement deleteIncidencesOfGraph(int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(DELETE_INCIDENCES_OF_GRAPH);
		statement.setInt(1, gId);
		return statement;
	}

	private static final String DELETE_VERTICES_OF_GRAPH = "DELETE FROM \""
			+ GraphDatabase.TABLE_VERTEX + "\" WHERE \"gId\" = ?";

	@Override
	public PreparedStatement deleteVerticesOfGraph(int gId) throws SQLException {
		PreparedStatement statement = getPreparedStatement(DELETE_VERTICES_OF_GRAPH);
		statement.setInt(1, gId);
		return statement;
	}

	private static final String DELETE_EDGES_OF_GRAPH = "" + "DELETE FROM \""
			+ GraphDatabase.TABLE_EDGE + "\" WHERE \"gId\" = ?";

	@Override
	public PreparedStatement deleteEdgesOfGraph(int gId) throws SQLException {
		PreparedStatement statement = getPreparedStatement(DELETE_EDGES_OF_GRAPH);
		statement.setInt(1, gId);
		return statement;
	}

	private static final String DELETE_GRAPH = "DELETE FROM \""
			+ GraphDatabase.TABLE_GRAPH + "\" WHERE \"gId\" = ?";

	@Override
	public PreparedStatement deleteGraph(int gId) throws SQLException {
		PreparedStatement statement = getPreparedStatement(DELETE_GRAPH);
		statement.setInt(1, gId);
		return statement;
	}

	// --- to delete a vertex -----------------------------------------

	private static final String DELETE_ATTRIBUTE_VALUES_OF_VERTEX = "DELETE FROM \""
			+ GraphDatabase.TABLE_VERTEX_ATTRIBUTE_VALUE
			+ "\" WHERE \"vId\" = ? AND \"gId\" = ?";

	@Override
	public PreparedStatement deleteAttributeValuesOfVertex(int vId, int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(DELETE_ATTRIBUTE_VALUES_OF_VERTEX);
		statement.setInt(1, vId);
		statement.setInt(2, gId);
		return statement;
	}

	private static final String SELECT_ID_OF_INCIDENT_EDGES_OF_VERTEX = "SELECT \"eId\" FROM \""
			+ GraphDatabase.TABLE_INCIDENCE
			+ "\" WHERE \"vId\" = ? AND \"gId\" = ?";

	@Override
	public PreparedStatement selectIncidentEIdsOfVertex(int vId, int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_ID_OF_INCIDENT_EDGES_OF_VERTEX);
		statement.setInt(1, vId);
		statement.setInt(2, gId);
		return statement;
	}

	private static final String DELETE_VERTEX = "DELETE FROM \""
			+ GraphDatabase.TABLE_VERTEX
			+ "\" WHERE \"vId\" = ? AND \"gId\" = ?";

	@Override
	public PreparedStatement deleteVertex(int vId, int gId) throws SQLException {
		PreparedStatement statement = getPreparedStatement(DELETE_VERTEX);
		statement.setInt(1, vId);
		statement.setInt(2, gId);
		return statement;
	}

	// --- to delete an edge ------------------------------------------

	private static final String DELETE_ATTRIBUTE_VALUES_OF_EDGE = "DELETE FROM \""
			+ GraphDatabase.TABLE_EDGE_ATTRIBUTE_VALUE
			+ "\" WHERE \"eId\" = ? AND \"gId\" = ?";

	@Override
	public PreparedStatement deleteAttributeValuesOfEdge(int eId, int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(DELETE_ATTRIBUTE_VALUES_OF_EDGE);
		statement.setInt(1, eId);
		statement.setInt(2, gId);
		return statement;
	}

	private static final String DELETE_INCIDENCES_OF_EDGE = "DELETE FROM \""
			+ GraphDatabase.TABLE_INCIDENCE
			+ "\" WHERE \"eId\" = ? AND \"gId\" = ?";

	@Override
	public PreparedStatement deleteIncidencesOfEdge(int eId, int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(DELETE_INCIDENCES_OF_EDGE);
		statement.setInt(1, eId);
		statement.setInt(2, gId);
		return statement;
	}

	private static final String DELETE_EDGE = "DELETE FROM \""
			+ GraphDatabase.TABLE_EDGE
			+ "\" WHERE \"eId\" = ? AND \"gId\" = ?";

	@Override
	public PreparedStatement deleteEdge(int eId, int gId) throws SQLException {
		PreparedStatement statement = getPreparedStatement(DELETE_EDGE);
		statement.setInt(1, eId);
		statement.setInt(2, gId);
		return statement;
	}

	// --- to update a graph ------------------------------------------

	private static final String UPDATE_ATTRIBUTE_VALUE_OF_GRAPH = "UPDATE \""
			+ GraphDatabase.TABLE_GRAPH_ATTRIBUTE_VALUE
			+ "\" SET value = ? WHERE \"gId\" = ? AND \"attributeId\" = ?";

	@Override
	public PreparedStatement updateAttributeValueOfGraph(int gId,
			int attributeId, String serializedValue) throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_ATTRIBUTE_VALUE_OF_GRAPH);
		statement.setString(1, serializedValue);
		statement.setInt(2, gId);
		statement.setInt(3, attributeId);
		return statement;
	}

	private static final String UPDATE_ATTRIBUTE_VALUE_OF_GRAPH_AND_GRAPH_VERSION = "UPDATE \""
			+ GraphDatabase.TABLE_GRAPH_ATTRIBUTE_VALUE
			+ "\" SET value = ? WHERE \"gId\" = ? AND \"attributeId\" = ?;"
			+ "UPDATE \""
			+ GraphDatabase.TABLE_GRAPH
			+ "\" SET version = ? WHERE \"gId\" = ?";

	@Override
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

	private static final String UPDATE_GRAPH_UID = "UPDATE \""
			+ GraphDatabase.TABLE_GRAPH
			+ "\" SET uid = ? WHERE \"gId\" = ?";

	@Override
	public PreparedStatement updateGraphId(int gId, String uid)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_GRAPH_UID);
		statement.setString(1, uid);
		statement.setInt(2, gId);
		return statement;
	}

	private static final String UPDATE_GRAPH_VERSION = "UPDATE \""
			+ GraphDatabase.TABLE_GRAPH
			+ "\" SET version = ? WHERE \"gId\" = ?";

	@Override
	public PreparedStatement updateGraphVersion(int gId, long version)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_GRAPH_VERSION);
		statement.setLong(1, version);
		statement.setInt(2, gId);
		return statement;
	}

	private static final String UPDATE_VERTEX_LIST_VERSION = "UPDATE \""
			+ GraphDatabase.TABLE_GRAPH
			+ "\" SET \"vSeqVersion\" = ? WHERE \"gId\" = ?";

	@Override
	public PreparedStatement updateVertexListVersionOfGraph(int gId,
			long version) throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_VERTEX_LIST_VERSION);
		statement.setLong(1, version);
		statement.setInt(2, gId);
		return statement;
	}

	private static final String UPDATE_EDGE_LIST_VERSION = "UPDATE \""
			+ GraphDatabase.TABLE_GRAPH
			+ "\" SET \"eSeqVersion\" = ? WHERE \"gId\" = ?";

	@Override
	public PreparedStatement updateEdgeListVersionOfGraph(int gId, long version)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_EDGE_LIST_VERSION);
		statement.setLong(1, version);
		statement.setInt(2, gId);
		return statement;
	}

	// --- to update a vertex -----------------------------------------

	private static final String UPDATE_VERTEX_ID = "UPDATE \""
			+ GraphDatabase.TABLE_VERTEX
			+ "\" SET \"vId\" = ? WHERE \"vId\" = ? AND \"gId\" = ?";

	@Override
	public PreparedStatement updateIdOfVertex(int oldVId, int gId, int newVId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_VERTEX_ID);
		statement.setInt(1, newVId);
		statement.setInt(2, oldVId);
		statement.setInt(3, gId);
		return statement;
	}

	private static final String UPDATE_SEQUENCE_NUMBER_OF_VERTEX = "UPDATE \""
			+ GraphDatabase.TABLE_VERTEX
			+ "\" SET \"sequenceNumber\" = ? WHERE \"vId\" = ? AND \"gId\" = ?";

	@Override
	public PreparedStatement updateSequenceNumberInVSeqOfVertex(int vId,
			int gId, long sequenceNumberInVSeq) throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_SEQUENCE_NUMBER_OF_VERTEX);
		statement.setLong(1, sequenceNumberInVSeq);
		statement.setInt(2, vId);
		statement.setInt(3, gId);
		return statement;
	}

	private static final String UPDATE_ATTRIBUTE_VALUE_OF_VERTEX = "UPDATE \""

			+ GraphDatabase.TABLE_VERTEX_ATTRIBUTE_VALUE
			+ "\" SET value = ? WHERE \"vId\" = ? AND \"gId\" = ? AND \"attributeId\" = ?";

	@Override
	public PreparedStatement updateAttributeValueOfVertex(int vId, int gId,
			int attributeId, String serializedValue) throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_ATTRIBUTE_VALUE_OF_VERTEX);
		statement.setString(1, serializedValue);
		statement.setInt(2, vId);
		statement.setInt(3, gId);
		statement.setInt(4, attributeId);
		return statement;
	}

	private static final String UPDATE_ATTRIBUTE_VALUE_OF_VERTEX_AND_GRAPH_VERSION = "UPDATE \""

			+ GraphDatabase.TABLE_VERTEX_ATTRIBUTE_VALUE
			+ "\" SET value = ? WHERE \"vId\" = ? AND \"gId\" = ? AND \"attributeId\" = ?;"
			+ "UPDATE \""
			+ GraphDatabase.TABLE_GRAPH
			+ "\" SET version = ? WHERE \"gId\" = ?";

	@Override
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

	private static final String UPDATE_INCIDENCE_LIST_VERSION = "UPDATE \""
			+ GraphDatabase.TABLE_VERTEX
			+ "\" SET \"lambdaSeqVersion\" = ? WHERE \"vId\" = ? AND \"gId\" = ?;";

	@Override
	public PreparedStatement updateLambdaSeqVersionOfVertex(int vId, int gId,
			long lambdaSeqVersion) throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_INCIDENCE_LIST_VERSION);
		statement.setLong(1, lambdaSeqVersion);
		statement.setInt(2, vId);
		statement.setInt(3, gId);
		return statement;
	}

	// --- to update an edge ------------------------------------------

	private static final String UPDATE_EDGE_ID = "UPDATE \"" + GraphDatabase.TABLE_EDGE
			+ "\" SET \"eId\" = ? WHERE \"eId\" = ? AND \"gId\" = ?";

	@Override
	public PreparedStatement updateIdOfEdge(int oldEId, int gId, int newEId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_EDGE_ID);
		statement.setInt(1, newEId);
		statement.setInt(2, oldEId);
		statement.setInt(3, gId);
		return statement;
	}

	private static final String UPDATE_SEQUENCE_NUMBER_IN_EDGE_LIST = "UPDATE \""
			+ GraphDatabase.TABLE_EDGE
			+ "\" SET \"sequenceNumber\" = ? WHERE \"eId\" = ? AND \"gId\" = ?";

	@Override
	public PreparedStatement updateSequenceNumberInESeqOfEdge(int eId, int gId,
			long SequenceNumberInESeq) throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_SEQUENCE_NUMBER_IN_EDGE_LIST);
		statement.setLong(1, SequenceNumberInESeq);
		statement.setInt(2, eId);
		statement.setInt(3, gId);
		return statement;
	}

	private static final String UPDATE_ATTRIBUTE_VALUE_OF_EDGE = "UPDATE \""

			+ GraphDatabase.TABLE_EDGE_ATTRIBUTE_VALUE
			+ "\" SET value = ? WHERE \"eId\" = ? AND \"gId\" = ? AND \"attributeId\" = ?";

	@Override
	public PreparedStatement updateAttributeValueOfEdge(int eId, int gId,
			int attributeId, String serializedValue) throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_ATTRIBUTE_VALUE_OF_EDGE);
		statement.setString(1, serializedValue);
		statement.setInt(2, eId);
		statement.setInt(3, gId);
		statement.setInt(4, attributeId);
		return statement;
	}

	private static final String UPDATE_ATTRIBUTE_VALUE_OF_EDGE_AND_INCREMENT_GRAPH_VERSION = "UPDATE \""

			+ GraphDatabase.TABLE_EDGE_ATTRIBUTE_VALUE
			+ "\" SET value = ? WHERE \"eId\" = ? AND \"gId\" = ? AND \"attributeId\" = ?;"
			+ "UPDATE \""
			+ GraphDatabase.TABLE_GRAPH
			+ "\" SET version = ? WHERE \"gId\" = ?;";

	@Override
	public PreparedStatement updateAttributeValueOfEdgeAndGraphVersion(int eId,
			int gId, int attributeId, String serializedValue, long graphVersion)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_ATTRIBUTE_VALUE_OF_EDGE_AND_INCREMENT_GRAPH_VERSION);
		statement.setString(1, serializedValue);
		statement.setInt(2, eId);
		statement.setInt(3, gId);
		statement.setInt(4, attributeId);
		statement.setLong(5, graphVersion);
		statement.setInt(6, gId);
		return statement;
	}

	private static final String UPDATE_INCIDENT_VERTEX = "UPDATE \""
			+ GraphDatabase.TABLE_INCIDENCE
			+ "\" SET \"vId\" = ? WHERE \"eId\" = ? AND \"gId\" = ? AND direction = ?::\"DIRECTION\"";

	@Override
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

	private static final String UPDATE_SEQUENCE_NUMBER_IN_INCIDENCE_LIST = "UPDATE \""
			+ GraphDatabase.TABLE_INCIDENCE
			+ "\" SET \"sequenceNumber\" = ? WHERE \"eId\" = ? AND \"gId\" = ? AND \"vId\" = ?";

	@Override
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

	private static final String STORED_PROCEDURE_REORGANIZE_VERTEX_LIST = "CREATE FUNCTION \"reorganizeVSeqOfGraph\"(\"graphId\" INT, start BIGINT) RETURNS INT AS $$\n"
			+ "DECLARE\n"
			+ "distance BIGINT := 4294967296;\n"
			+ "current BIGINT := start;\n"
			+ "vertex RECORD;\n"
			+ "BEGIN\n"
			+ "FOR vertex IN (SELECT \"vId\" FROM \""
			+ GraphDatabase.TABLE_VERTEX
			+ "\" WHERE \"gId\" = \"graphId\" ORDER BY \"sequenceNumber\" ASC) LOOP\n"
			+ "EXECUTE 'UPDATE \""
			+ GraphDatabase.TABLE_VERTEX
			+ "\" SET \"sequenceNumber\" = $1 WHERE \"vId\" = $2 AND \"gId\" =$3' USING current, vertex.\"vId\", \"graphId\";\n"
			+ "current := current + distance;\n"
			+ "END LOOP;\n"
			+ "RETURN 1;\n" + "END;\n" + "$$LANGUAGE plpgsql;";

	@Override
	public PreparedStatement createStoredProcedureToReorganizeVertexList()
			throws SQLException {
		return getPreparedStatement(STORED_PROCEDURE_REORGANIZE_VERTEX_LIST);
	}

	private static final String STORED_PROCEDURE_REORGANIZE_EDGE_LIST = "CREATE FUNCTION \"reorganizeESeqOfGraph\"(\"graphId\" INT, start BIGINT) RETURNS INT AS $$\n"
			+ "DECLARE\n"
			+ "distance BIGINT := 4294967296;\n"
			+ "current BIGINT := start;\n"
			+ "edge RECORD;\n"
			+ "BEGIN\n"
			+ "FOR edge IN (SELECT \"eId\" FROM \""
			+ GraphDatabase.TABLE_EDGE
			+ "\" WHERE \"gId\" = \"graphId\" ORDER BY \"sequenceNumber\" ASC) LOOP\n"
			+ "EXECUTE 'UPDATE \""
			+ GraphDatabase.TABLE_EDGE
			+ "\" SET \"sequenceNumber\" = $1 WHERE \"eId\" = $2 AND \"gId\" = $3' USING current, edge.\"eId\", \"graphId\";\n"
			+ "current := current + distance;\n"
			+ "END LOOP;\n"
			+ "RETURN 1;\n" + "END;\n" + "$$ LANGUAGE plpgsql;";

	@Override
	public PreparedStatement createStoredProcedureToReorganizeEdgeList()
			throws SQLException {
		return getPreparedStatement(STORED_PROCEDURE_REORGANIZE_EDGE_LIST);
	}

	private static final String STORED_PROCEDURE_REORGANIZE_INCIDENCE_LIST = "CREATE FUNCTION \"reorganizeLambdaSeqOfVertex\"(\"vertexId\" INT, \"graphId\" INT, start BIGINT) RETURNS INT AS $$\n"
			+ "DECLARE\n"
			+ "distance BIGINT := 4294967296;\n"
			+ "current BIGINT := start;\n"
			+ "incidence RECORD;\n"
			+ "BEGIN\n"
			+ "FOR incidence IN (SELECT \"eId\", direction FROM \""
			+ GraphDatabase.TABLE_INCIDENCE
			+ "\" WHERE \"vId\" = \"vertexId\" AND \"gId\" = \"graphId\" ORDER BY \"sequenceNumber\" ASC) LOOP\n"
			+ "EXECUTE 'UPDATE \""
			+ GraphDatabase.TABLE_INCIDENCE
			+ "\" SET \"sequenceNumber\" = $1 WHERE \"vId\" = $2 AND \"gId\" = $3 AND \"eId\" = $4 AND direction = $5' USING current, \"vertexId\", \"graphId\", incidence.\"eId\", incidence.direction;\n"
			+ "current := current + distance;\n"
			+ "END LOOP;\n"
			+ "RETURN 1;\n" + "END;\n" + "$$ LANGUAGE plpgsql;";

	@Override
	public PreparedStatement createStoredProcedureToReorganizeIncidenceList()
			throws SQLException {
		return getPreparedStatement(STORED_PROCEDURE_REORGANIZE_INCIDENCE_LIST);
	}

	private static final String STORED_PROCEDURE_INSERT_VERTEX = "CREATE FUNCTION \"insertVertex\"(\"vertexId\" INT, \"graphId\" INT, \"typeId\" INT, \"sequenceNumber\" BIGINT) RETURNS INT AS $$\n"
			+ "BEGIN\n"
			+ "EXECUTE 'INSERT INTO \""
			+ GraphDatabase.TABLE_VERTEX
			+ "\" ( \"vId\", \"gId\", \"typeId\", \"lambdaSeqVersion\", \"sequenceNumber\" ) VALUES ($1, $2, $3, 0, $4)' USING \"vertexId\", \"graphId\", \"typeId\", \"sequenceNumber\";\n"
			+ "RETURN 1;\n" + "END;\n" + "$$ LANGUAGE plpgsql;";

	public PreparedStatement createStoredProcedureToInsertVertex()
			throws SQLException {
		return getPreparedStatement(STORED_PROCEDURE_INSERT_VERTEX);
	}

	private static final String DELETE_SCHEMA = "DELETE FROM \""

	+ GraphDatabase.TABLE_SCHEMA
			+ "\" WHERE \"packagePrefix\" = ? AND name = ?";

	@Override
	public PreparedStatement deleteSchema(String prefix, String name)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(DELETE_SCHEMA);
		statement.setString(1, prefix);
		statement.setString(2, name);
		return statement;
	}

	private static final String SELECT_SCHEMA_DEFINITION = "SELECT \"serializedDefinition\" FROM \""

			+ GraphDatabase.TABLE_SCHEMA
			+ "\" WHERE \"packagePrefix\" = ? AND name = ?;";

	@Override
	public PreparedStatement selectSchemaDefinition(String packagePrefix,
			String schemaName) throws SQLException {
		PreparedStatement statement = connection
				.prepareStatement(SELECT_SCHEMA_DEFINITION);
		statement.setString(1, packagePrefix);
		statement.setString(2, schemaName);
		return statement;
	}

	private static final String CALL_REORGANIZE_V_SEQ = "{ ? = call \"reorganizeVSeqOfGraph\"(?, ?) }";

	@Override
	public CallableStatement createReorganizeVertexListCall(int gId, long start)
			throws SQLException {
		CallableStatement statement = connection
				.prepareCall(CALL_REORGANIZE_V_SEQ);
		statement.registerOutParameter(1, Types.INTEGER);
		statement.setInt(2, gId);
		statement.setLong(3, start);
		return statement;
	}

	private static final String CALL_REORGANIZE_E_SEQ = "{ ? = call \"reorganizeESeqOfGraph\"(?, ?) }";

	@Override
	public CallableStatement createReorganizeEdgeListCall(int gId, long start)
			throws SQLException {
		CallableStatement statement = connection
				.prepareCall(CALL_REORGANIZE_E_SEQ);
		statement.registerOutParameter(1, Types.INTEGER);
		statement.setInt(2, gId);
		statement.setLong(3, start);
		return statement;
	}

	private static final String CALL_REORGANIZE_LAMBDA_SEQ = "{ ? = call \"reorganizeLambdaSeqOfVertex\"(?, ?) }";

	@Override
	public CallableStatement createReorganizeIncidenceListCall(int vId,
			int gId, long start) throws SQLException {
		CallableStatement statement = connection
				.prepareCall(CALL_REORGANIZE_LAMBDA_SEQ);
		statement.registerOutParameter(1, Types.INTEGER);
		statement.setInt(2, vId);
		statement.setInt(3, gId);
		statement.setLong(4, start);
		return statement;
	}

	private static final String SELECT_ID_OF_GRAPHS = "SELECT \"uid\" FROM \""
			+ GraphDatabase.TABLE_GRAPH + "\";";

	@Override
	public PreparedStatement selectIdOfGraphs() throws SQLException {
		return getPreparedStatement(SELECT_ID_OF_GRAPHS);
	}

	private static final String CLEAR_ALL_TABLES = "TRUNCATE TABLE \""
			+ GraphDatabase.TABLE_ATTRIBUTE + "\",\""
			+ GraphDatabase.TABLE_EDGE_ATTRIBUTE_VALUE + "\",\""
			+ GraphDatabase.TABLE_EDGE + "\",\""
			+ GraphDatabase.TABLE_GRAPH_ATTRIBUTE_VALUE + "\",\""
			+ GraphDatabase.TABLE_SCHEMA + "\",\""
			+ GraphDatabase.TABLE_GRAPH + "\",\""
			+ GraphDatabase.TABLE_INCIDENCE + "\",\""
			+ GraphDatabase.TABLE_TYPE + "\",\""
			+ GraphDatabase.TABLE_VERTEX_ATTRIBUTE_VALUE + "\",\""
			+ GraphDatabase.TABLE_VERTEX + "\";";

	@Override
	public PreparedStatement clearAllTables() throws SQLException {
		return getPreparedStatement(CLEAR_ALL_TABLES);
	}
}
