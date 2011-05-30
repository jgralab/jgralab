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

import static de.uni_koblenz.jgralab.impl.db.GraphDatabase.*;
import static de.uni_koblenz.jgralab.impl.db.PostgreSqlDb.*;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.SortedSet;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.schema.Attribute;

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

	@Override
	public String makeQueryVendorSpecific(String query) {
		return query.replace(SqlStatementList.QUOTE, "\"").replace(
				SqlStatementList.EOQ, ";").replace(DIRECTION_TYPE,
				"?::\"DIRECTION\"");
	}

	private static final String CREATE_GRAPH_SCHEMA_TABLE = "CREATE SEQUENCE \""
			+ SEQUENCE_SCHEMA
			+ "\";"
			+ "CREATE TABLE \""
			+ TABLE_SCHEMA
			+ "\"("
			+ "\""
			+ COLUMN_SCHEMA_ID
			+ "\" INT4 PRIMARY KEY DEFAULT NEXTVAL('\""
			+ SEQUENCE_SCHEMA
			+ "\"'),"
			+ "\""
			+ COLUMN_SCHEMA_PACKAGE_PREFIX
			+ "\" TEXT,"
			+ ""
			+ COLUMN_SCHEMA_NAME
			+ " TEXT,"
			+ "\""
			+ COLUMN_SCHEMA_TG
			+ "\" TEXT" + ");";

	@Override
	public PreparedStatement createGraphSchemaTableWithConstraints()
			throws SQLException {
		return connection.prepareStatement(CREATE_GRAPH_SCHEMA_TABLE);
	}

	// TODO implicit fk constraints?
	private static final String CREATE_TYPE_TABLE = "CREATE SEQUENCE \""
			+ SEQUENCE_TYPE + "\";" + "CREATE TABLE \"" + TABLE_TYPE + "\"("
			+ "\"" + COLUMN_TYPE_ID + "\" INT4 PRIMARY KEY DEFAULT NEXTVAL('\""
			+ SEQUENCE_TYPE + "\"')," + "\"" + COLUMN_TYPE_QNAME + "\" TEXT,"
			+ "\"" + COLUMN_SCHEMA_ID + "\" INT4 REFERENCES \"" + TABLE_SCHEMA
			+ "\" ON DELETE CASCADE" + ");";

	@Override
	public PreparedStatement createTypeTableWithConstraints()
			throws SQLException {
		return connection.prepareStatement(CREATE_TYPE_TABLE);
	}

	private static final String CREATE_GRAPH_TABLE = "CREATE SEQUENCE \""
			+ SEQUENCE_GRAPH + "\";" + "CREATE TABLE \"" + TABLE_GRAPH + "\"("
			+ "\"" + COLUMN_GRAPH_ID
			+ "\" INT4 PRIMARY KEY DEFAULT NEXTVAL('\"" + SEQUENCE_GRAPH
			+ "\"')," + "" + COLUMN_GRAPH_UID + " TEXT," + ""
			+ COLUMN_GRAPH_VERSION + " INT8," + "\""
			+ COLUMN_GRAPH_VSEQ_VERSION + "\" INT8," + "\""
			+ COLUMN_GRAPH_ESEQ_VERSION + "\" INT8," + "\"" + COLUMN_TYPE_ID
			+ "\" INT4 REFERENCES \"" + TABLE_TYPE + "\"(\"" + COLUMN_TYPE_ID
			+ "\")" + ");";

	@Override
	public PreparedStatement createGraphTableWithConstraints()
			throws SQLException {
		return connection.prepareStatement(CREATE_GRAPH_TABLE);
	}

	private static final String CREATE_VERTEX_TABLE = "CREATE TABLE \""
			+ TABLE_VERTEX + "\"(" + "\"" + COLUMN_VERTEX_ID + "\" INT4,"
			+ "\"" + COLUMN_GRAPH_ID + "\" INT4," + "\"" + COLUMN_TYPE_ID
			+ "\" INT4," + "\"" + COLUMN_VERTEX_LAMBDA_SEQ_VERSION + "\" INT8,"
			+ // TODO Remove as
			// this is
			// really
			// only needed while an Iterator
			// is in memory
			"\"" + COLUMN_SEQUENCE_NUMBER + "\" INT8" + ");";

	@Override
	public PreparedStatement createVertexTable() throws SQLException {
		return connection.prepareStatement(CREATE_VERTEX_TABLE);
	}

	private static final String ADD_PRIMARY_KEY_CONSTRAINT_ON_VERTEX_TABLE = "ALTER TABLE \""
			+ TABLE_VERTEX
			+ "\" ADD CONSTRAINT \""
			+ PRIMARY_KEY_VERTEX
			+ "\" PRIMARY KEY ( \""
			+ COLUMN_VERTEX_ID
			+ "\", \""
			+ COLUMN_GRAPH_ID + "\" );";

	@Override
	public PreparedStatement addPrimaryKeyConstraintOnVertexTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_PRIMARY_KEY_CONSTRAINT_ON_VERTEX_TABLE);
	}

	private static final String DROP_PRIMARY_KEY_CONSTRAINT_FROM_VERTEX_TABLE = "ALTER TABLE \""
			+ TABLE_VERTEX
			+ "\" DROP CONSTRAINT \""
			+ PRIMARY_KEY_VERTEX
			+ "\";";

	@Override
	public PreparedStatement dropPrimaryKeyConstraintFromVertexTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_PRIMARY_KEY_CONSTRAINT_FROM_VERTEX_TABLE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_VERTEX = "ALTER TABLE \""
			+ TABLE_VERTEX
			+ "\" ADD CONSTRAINT \""
			+ FOREIGN_KEY_VERTEX_TO_GRAPH
			+ "\" FOREIGN KEY (\""
			+ COLUMN_GRAPH_ID
			+ "\") REFERENCES \""
			+ TABLE_GRAPH
			+ "\" (\""
			+ COLUMN_GRAPH_ID + "\");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnGraphColumnOfVertexTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_VERTEX);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_VERTEX_TYPE = "ALTER TABLE \""
			+ TABLE_VERTEX
			+ "\" ADD CONSTRAINT \""
			+ FOREIGN_KEY_VERTEX_TO_TYPE
			+ "\" FOREIGN KEY (\""
			+ COLUMN_TYPE_ID
			+ "\") REFERENCES \""
			+ TABLE_TYPE
			+ "\" (\""
			+ COLUMN_VERTEX_ID + "\");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnTypeColumnOfVertexTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_VERTEX_TYPE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_VERTEX = "ALTER TABLE \""
			+ TABLE_VERTEX
			+ "\" DROP CONSTRAINT \""
			+ FOREIGN_KEY_VERTEX_TO_GRAPH + "\";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromGraphColumnOfVertexTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_VERTEX);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_VERTEX_TYPE = "ALTER TABLE \""
			+ TABLE_VERTEX
			+ "\" DROP CONSTRAINT \""
			+ FOREIGN_KEY_VERTEX_TO_TYPE + "\";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromTypeColumnOfVertexTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_VERTEX_TYPE);
	}

	private static final String CREATE_EDGE_TABLE = "CREATE TABLE \""
			+ TABLE_EDGE + "\"(" + "\"" + COLUMN_EDGE_ID + "\" INT4," + "\""
			+ COLUMN_GRAPH_ID + "\" INT4," + "\"" + COLUMN_TYPE_ID + "\" INT4,"
			+ "\"" + COLUMN_SEQUENCE_NUMBER + "\" INT8" + ");";

	@Override
	public PreparedStatement createEdgeTable() throws SQLException {
		return connection.prepareStatement(CREATE_EDGE_TABLE);
	}

	private static final String ADD_PRIMARY_KEY_CONSTRAINT_ON_EDGE_TABLE = "ALTER TABLE \""
			+ TABLE_EDGE
			+ "\" ADD CONSTRAINT \""
			+ PRIMARY_KEY_EDGE
			+ "\" PRIMARY KEY ( \""
			+ COLUMN_EDGE_ID
			+ "\", \""
			+ COLUMN_GRAPH_ID + "\" );";

	@Override
	public PreparedStatement addPrimaryKeyConstraintOnEdgeTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_PRIMARY_KEY_CONSTRAINT_ON_EDGE_TABLE);
	}

	private static final String DROP_PRIMARY_KEY_CONSTRAINT_FROM_EDGE_TABLE = "ALTER TABLE \""
			+ TABLE_EDGE + "\" DROP CONSTRAINT \"" + PRIMARY_KEY_EDGE + "\";";

	@Override
	public PreparedStatement dropPrimaryKeyConstraintFromEdgeTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_PRIMARY_KEY_CONSTRAINT_FROM_EDGE_TABLE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_EDGE = "ALTER TABLE \""
			+ TABLE_EDGE
			+ "\" ADD CONSTRAINT \""
			+ FOREIGN_KEY_EDGE_TO_GRAPH
			+ "\" FOREIGN KEY (\""
			+ COLUMN_GRAPH_ID
			+ "\") REFERENCES \""
			+ TABLE_GRAPH + "\" (\"" + COLUMN_GRAPH_ID + "\");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnGraphColumnOfEdgeTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_EDGE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_EDGE_TYPE = "ALTER TABLE \""
			+ TABLE_EDGE
			+ "\" ADD CONSTRAINT \""
			+ FOREIGN_KEY_EDGE_TO_TYPE
			+ "\" FOREIGN KEY (\""
			+ COLUMN_TYPE_ID
			+ "\") REFERENCES \""
			+ TABLE_TYPE + "\" (\"" + COLUMN_TYPE_ID + "\");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnTypeColumnOfEdgeTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_EDGE_TYPE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_EDGE = "ALTER TABLE \""
			+ TABLE_EDGE
			+ "\" DROP CONSTRAINT \""
			+ FOREIGN_KEY_EDGE_TO_GRAPH
			+ "\";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromGraphColumnOfEdgeTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_EDGE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_EDGE_TYPE = "ALTER TABLE \""
			+ TABLE_EDGE
			+ "\" DROP CONSTRAINT \""
			+ FOREIGN_KEY_EDGE_TO_TYPE
			+ "\";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromTypeColumnOfEdgeTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_EDGE_TYPE);
	}

	private static final String CREATE_INCIDENCE_TABLE = "CREATE TYPE \"DIRECTION\" AS ENUM( 'OUT', 'IN' );"
			+ "CREATE TABLE \""
			+ TABLE_INCIDENCE
			+ "\"("
			+ "\""
			+ COLUMN_EDGE_ID
			+ "\" INT4,"
			+ "\""
			+ COLUMN_VERTEX_ID
			+ "\" INT4,"
			+ "\""
			+ COLUMN_GRAPH_ID
			+ "\" INT4,"
			+ ""
			+ COLUMN_INCIDENCE_DIRECTION
			+ " \"DIRECTION\","
			+ "\""
			+ COLUMN_SEQUENCE_NUMBER + "\" INT8" + ");";

	@Override
	public PreparedStatement createIncidenceTable() throws SQLException {
		return connection.prepareStatement(CREATE_INCIDENCE_TABLE);
	}

	private static final String ADD_PRIMARY_KEY_CONSTRAINT_ON_INCIDENCE_TABLE = "ALTER TABLE \""
			+ TABLE_INCIDENCE
			+ "\" ADD CONSTRAINT \""
			+ PRIMARY_KEY_INCIDENCE
			+ "\" PRIMARY KEY ( \""
			+ COLUMN_EDGE_ID
			+ "\", \""
			+ COLUMN_GRAPH_ID + "\", " + COLUMN_INCIDENCE_DIRECTION + " );";

	@Override
	public PreparedStatement addPrimaryKeyConstraintOnIncidenceTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_PRIMARY_KEY_CONSTRAINT_ON_INCIDENCE_TABLE);
	}

	private static final String DROP_PRIMARY_KEY_CONSTRAINT_FROM_INCIDENCE_TABLE = "ALTER TABLE \""
			+ TABLE_INCIDENCE
			+ "\" DROP CONSTRAINT \""
			+ PRIMARY_KEY_INCIDENCE
			+ "\";";

	@Override
	public PreparedStatement dropPrimaryKeyConstraintFromIncidenceTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_PRIMARY_KEY_CONSTRAINT_FROM_INCIDENCE_TABLE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_INCIDENCE = "ALTER TABLE \""
			+ TABLE_INCIDENCE
			+ "\" ADD CONSTRAINT \""
			+ FOREIGN_KEY_INCIDENCE_TO_GRAPH
			+ "\" FOREIGN KEY (\""
			+ COLUMN_GRAPH_ID
			+ "\") REFERENCES \""
			+ TABLE_GRAPH
			+ "\" (\""
			+ COLUMN_GRAPH_ID + "\");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnGraphColumnOfIncidenceTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_INCIDENCE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_EDGE_OF_INCIDENCE = "ALTER TABLE \""
			+ TABLE_INCIDENCE
			+ "\" ADD CONSTRAINT \""
			+ FOREIGN_KEY_INCIDENCE_TO_EDGE
			+ "\" FOREIGN KEY (\""
			+ COLUMN_EDGE_ID
			+ "\", \""
			+ COLUMN_GRAPH_ID
			+ "\") REFERENCES \""
			+ TABLE_EDGE
			+ "\" (\""
			+ COLUMN_EDGE_ID
			+ "\", \""
			+ COLUMN_GRAPH_ID + "\");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnEdgeColumnOfIncidenceTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_EDGE_OF_INCIDENCE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_VERTEX_OF_INCIDENCE = "ALTER TABLE \""
			+ TABLE_INCIDENCE
			+ "\" ADD CONSTRAINT \""
			+ FOREIGN_KEY_INCIDENCE_TO_VERTEX
			+ "\" FOREIGN KEY (\""
			+ COLUMN_VERTEX_ID
			+ "\", \""
			+ COLUMN_GRAPH_ID
			+ "\") REFERENCES \""
			+ TABLE_VERTEX
			+ "\" (\""
			+ COLUMN_VERTEX_ID
			+ "\", \"" + COLUMN_GRAPH_ID + "\");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnVertexColumnOfIncidenceTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_VERTEX_OF_INCIDENCE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_EDGE_OF_INCIDENCE = "ALTER TABLE \""
			+ TABLE_INCIDENCE
			+ "\" DROP CONSTRAINT \""
			+ FOREIGN_KEY_INCIDENCE_TO_EDGE + "\";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromEdgeColumnOfIncidenceTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_EDGE_OF_INCIDENCE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_INCIDENCE = "ALTER TABLE \""
			+ TABLE_INCIDENCE
			+ "\" DROP CONSTRAINT \""
			+ FOREIGN_KEY_INCIDENCE_TO_GRAPH + "\";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromGraphColumnOfIncidenceTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_INCIDENCE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_VERTEX_OF_INCIDENCE = "ALTER TABLE \""
			+ TABLE_INCIDENCE
			+ "\" DROP CONSTRAINT \""
			+ FOREIGN_KEY_INCIDENCE_TO_VERTEX + "\";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromVertexColumnOfIncidenceTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_VERTEX_OF_INCIDENCE);
	}

	private static final String CREATE_INDEX_ON_LAMBDA_SEQ = "CREATE INDEX \""
			+ INDEX_INCIDENCE_LAMBDA_SEQ + "\" ON \"" + TABLE_INCIDENCE
			+ "\"( \"" + COLUMN_VERTEX_ID + "\", \"" + COLUMN_GRAPH_ID
			+ "\", \"" + COLUMN_SEQUENCE_NUMBER
			+ "\" ASC ) WITH (FILLFACTOR=80);" + "ALTER TABLE \""
			+ TABLE_INCIDENCE + "\" CLUSTER ON \"" + INDEX_INCIDENCE_LAMBDA_SEQ
			+ "\";" + "ANALYZE \"" + TABLE_INCIDENCE + "\";";

	@Override
	public PreparedStatement addIndexOnLambdaSeq() throws SQLException {
		return getPreparedStatement(CREATE_INDEX_ON_LAMBDA_SEQ);
	}

	private static final String DROP_INDEX_ON_LAMBDA_SEQ = "DROP INDEX IF EXISTS \""
			+ INDEX_INCIDENCE_LAMBDA_SEQ + "\";";

	@Override
	public PreparedStatement dropIndexOnLambdaSeq() throws SQLException {
		return getPreparedStatement(DROP_INDEX_ON_LAMBDA_SEQ);
	}

	private static final String CLUSTER_INCIDENCES = "CLUSTER \""
			+ TABLE_INCIDENCE + "\";";

	public PreparedStatement clusterIncidenceTable() throws SQLException {
		return getPreparedStatement(CLUSTER_INCIDENCES);
	}

	private static final String CREATE_ATTRIBUTE_TABLE = "CREATE SEQUENCE \""
			+ SEQUENCE_ATTRIBUTE + "\";" + "CREATE TABLE \"" + TABLE_ATTRIBUTE
			+ "\"(" + "\"" + COLUMN_ATTRIBUTE_ID
			+ "\" INT4 PRIMARY KEY DEFAULT NEXTVAL('\"" + SEQUENCE_ATTRIBUTE
			+ "\"')," + "" + COLUMN_ATTRIBUTE_NAME + " TEXT," + "\""
			+ COLUMN_SCHEMA_ID + "\" INT4 REFERENCES \"" + TABLE_SCHEMA
			+ "\" ON DELETE CASCADE" + ");";

	@Override
	public PreparedStatement createAttributeTableWithConstraints()
			throws SQLException {
		return connection.prepareStatement(CREATE_ATTRIBUTE_TABLE);
	}

	private static final String CREATE_GRAPH_ATTRIBUTE_VALUE_TABLE = "CREATE TABLE \""
			+ TABLE_GRAPH_ATTRIBUTE
			+ "\"("
			+ "\""
			+ COLUMN_GRAPH_ID
			+ "\" INT4,"
			+ "\""
			+ COLUMN_ATTRIBUTE_ID
			+ "\" INT4 REFERENCES \""
			+ TABLE_ATTRIBUTE
			+ "\" (\""
			+ COLUMN_ATTRIBUTE_ID
			+ "\"),"
			+ "\""
			+ COLUMN_ATTRIBUTE_VALUE
			+ "\" TEXT,"
			+ "CONSTRAINT \""
			+ PRIMARY_KEY_GRAPH_ATTRIBUTE
			+ "\" PRIMARY KEY ( \""
			+ COLUMN_GRAPH_ID + "\", \"" + COLUMN_ATTRIBUTE_ID + "\" )" + ");";

	@Override
	public PreparedStatement createGraphAttributeValueTableWithConstraints()
			throws SQLException {
		return connection.prepareStatement(CREATE_GRAPH_ATTRIBUTE_VALUE_TABLE);
	}

	private static final String CREATE_VERTEX_ATTRIBUTE_VALUE_TABLE = "CREATE TABLE \""
			+ TABLE_VERTEX_ATTRIBUTE
			+ "\"("
			+ "\""
			+ COLUMN_VERTEX_ID
			+ "\" INT4,"
			+ "\""
			+ COLUMN_GRAPH_ID
			+ "\" INT4,"
			+ "\""
			+ COLUMN_ATTRIBUTE_ID
			+ "\" INT4,"
			+ "\""
			+ COLUMN_ATTRIBUTE_VALUE
			+ "\" TEXT" + // TODO
			// Replace
			// by
			// NVARCHAR(k).
			");";

	@Override
	public PreparedStatement createVertexAttributeValueTable()
			throws SQLException {
		return connection.prepareStatement(CREATE_VERTEX_ATTRIBUTE_VALUE_TABLE);
	}

	private static final String ADD_PRIMARY_KEY_CONSTRAINT_ON_VERTEX_ATTRIBUTE_VALUE_TABLE = "ALTER TABLE \""
			+ TABLE_VERTEX_ATTRIBUTE
			+ "\" ADD CONSTRAINT \""
			+ PRIMARY_KEY_VERTEX_ATTRIBUTE
			+ "\" PRIMARY KEY ( \""
			+ COLUMN_VERTEX_ID
			+ "\", \""
			+ COLUMN_GRAPH_ID
			+ "\", \""
			+ COLUMN_ATTRIBUTE_ID + "\" );";

	@Override
	public PreparedStatement addPrimaryKeyConstraintOnVertexAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_PRIMARY_KEY_CONSTRAINT_ON_VERTEX_ATTRIBUTE_VALUE_TABLE);
	}

	private static final String DROP_PRIMARY_KEY_CONSTRAINT_FROM_VERTEX_ATTRIBUTE_VALUE_TABLE = "ALTER TABLE \""
			+ TABLE_VERTEX_ATTRIBUTE
			+ "\" DROP CONSTRAINT \""
			+ PRIMARY_KEY_VERTEX_ATTRIBUTE + "\";";

	@Override
	public PreparedStatement dropPrimaryKeyConstraintFromVertexAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_PRIMARY_KEY_CONSTRAINT_FROM_VERTEX_ATTRIBUTE_VALUE_TABLE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_VERTEX_ATTRIBUTE_VALUE = "ALTER TABLE \""
			+ TABLE_VERTEX_ATTRIBUTE
			+ "\" ADD CONSTRAINT \""
			+ FOREIGN_KEY_VERTEX_ATTRIBUTE_TO_GRAPH
			+ "\" FOREIGN KEY (\""
			+ COLUMN_GRAPH_ID
			+ "\") REFERENCES \""
			+ TABLE_GRAPH
			+ "\" (\""
			+ COLUMN_GRAPH_ID + "\");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnGraphColumnOfVertexAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_VERTEX_ATTRIBUTE_VALUE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_VERTEX_OF_ATTRIBUTE_VALUE = "ALTER TABLE \""
			+ TABLE_VERTEX_ATTRIBUTE
			+ "\" ADD CONSTRAINT \""
			+ FOREIGN_KEY_VERTEX_ATTRIBUTE_TO_VERTEX
			+ "\" FOREIGN KEY (\""
			+ COLUMN_VERTEX_ID
			+ "\", \""
			+ COLUMN_GRAPH_ID
			+ "\") REFERENCES \""
			+ TABLE_VERTEX
			+ "\" (\""
			+ COLUMN_VERTEX_ID
			+ "\", \"" + COLUMN_GRAPH_ID + "\");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnVertexColumnOfVertexAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_VERTEX_OF_ATTRIBUTE_VALUE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_ATTRIBUTE_OF_VERTEX_ATTRIBUTE_VALUE = "ALTER TABLE \""
			+ TABLE_VERTEX_ATTRIBUTE
			+ "\" ADD CONSTRAINT \""
			+ FOREIGN_KEY_VERTEX_ATTRIBUTE_TO_ATTRIBUTE
			+ "\" FOREIGN KEY (\""
			+ COLUMN_ATTRIBUTE_ID
			+ "\" ) REFERENCES \""
			+ TABLE_ATTRIBUTE
			+ "\" (\"" + COLUMN_ATTRIBUTE_ID + "\");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnAttributeColumnOfVertexAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_ATTRIBUTE_OF_VERTEX_ATTRIBUTE_VALUE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_VERTEX_ATTRIBUTE_VALUE = "ALTER TABLE \""
			+ TABLE_VERTEX_ATTRIBUTE
			+ "\" DROP CONSTRAINT \""
			+ FOREIGN_KEY_VERTEX_ATTRIBUTE_TO_GRAPH + "\";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromGraphColumnOfVertexAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_VERTEX_ATTRIBUTE_VALUE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_VERTEX_OF_ATTRIBUTE_VALUE = "ALTER TABLE \""
			+ TABLE_VERTEX_ATTRIBUTE
			+ "\" DROP CONSTRAINT \""
			+ FOREIGN_KEY_VERTEX_ATTRIBUTE_TO_VERTEX + "\";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromVertexColumnOfVertexAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_VERTEX_OF_ATTRIBUTE_VALUE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_ATTRIBUTE_OF_VERTEX_ATTRIBUTE_VALUE = "ALTER TABLE \""
			+ TABLE_VERTEX_ATTRIBUTE
			+ "\" DROP CONSTRAINT \""
			+ FOREIGN_KEY_VERTEX_ATTRIBUTE_TO_ATTRIBUTE + "\";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromAttributeColumnOfVertexAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_ATTRIBUTE_OF_VERTEX_ATTRIBUTE_VALUE);
	}

	private static final String CREATE_EDGE_ATTRIBUTE_VALUE_TABLE = "CREATE TABLE \""
			+ TABLE_EDGE_ATTRIBUTE
			+ "\"("
			+ "\""
			+ COLUMN_EDGE_ID
			+ "\" INT4,"
			+ "\""
			+ COLUMN_GRAPH_ID
			+ "\" INT4,"
			+ "\""
			+ COLUMN_ATTRIBUTE_ID
			+ "\" INT4," + "\"" + COLUMN_ATTRIBUTE_VALUE + "\" TEXT" + // TODO
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
			+ TABLE_EDGE_ATTRIBUTE
			+ "\" ADD CONSTRAINT \""
			+ PRIMARY_KEY_EDGE_ATTRIBUTE
			+ "\" PRIMARY KEY ( \""
			+ COLUMN_EDGE_ID
			+ "\", \""
			+ COLUMN_GRAPH_ID
			+ "\", \""
			+ COLUMN_ATTRIBUTE_ID + "\" );";

	@Override
	public PreparedStatement addPrimaryKeyConstraintOnEdgeAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_PRIMARY_KEY_CONSTRAINT_ON_EDGE_ATTRIBUTE_VALUE_TABLE);
	}

	private static final String DROP_PRIMARY_KEY_CONSTRAINT_FROM_EDGE_ATTRIBUTE_VALUE_TABLE = "ALTER TABLE \""
			+ TABLE_EDGE_ATTRIBUTE
			+ "\" DROP CONSTRAINT \""
			+ PRIMARY_KEY_EDGE_ATTRIBUTE + "\";";

	@Override
	public PreparedStatement dropPrimaryKeyConstraintFromEdgeAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_PRIMARY_KEY_CONSTRAINT_FROM_EDGE_ATTRIBUTE_VALUE_TABLE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_EDGE_ATTRIBUTE_VALUE = "ALTER TABLE \""
			+ TABLE_EDGE_ATTRIBUTE
			+ "\" ADD CONSTRAINT \""
			+ FOREIGN_KEY_EDGE_ATTRIBUTE_TO_GRAPH
			+ "\" FOREIGN KEY (\""
			+ COLUMN_GRAPH_ID
			+ "\") REFERENCES \""
			+ TABLE_GRAPH
			+ "\" (\""
			+ COLUMN_GRAPH_ID + "\");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnGraphColumnOfEdgeAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_EDGE_ATTRIBUTE_VALUE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_EDGE_OF_ATTRIBUTE_VALUE = "ALTER TABLE \""
			+ TABLE_EDGE_ATTRIBUTE
			+ "\" ADD CONSTRAINT \""
			+ FOREIGN_KEY_EDGE_ATTRIBUTE_TO_EDGE
			+ "\" FOREIGN KEY (\""
			+ COLUMN_EDGE_ID
			+ "\", \""
			+ COLUMN_GRAPH_ID
			+ "\") REFERENCES \""
			+ TABLE_EDGE
			+ "\" (\""
			+ COLUMN_EDGE_ID
			+ "\", \""
			+ COLUMN_GRAPH_ID + "\");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnEdgeColumnOfEdgeAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_EDGE_OF_ATTRIBUTE_VALUE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_EDGE_ATTRIBUTE = "ALTER TABLE \""
			+ TABLE_EDGE_ATTRIBUTE
			+ "\" ADD CONSTRAINT \""
			+ FOREIGN_KEY_EDGE_ATTRIBUTE_TO_ATTRIBUTE
			+ "\" FOREIGN KEY (\""
			+ COLUMN_ATTRIBUTE_ID
			+ "\" ) REFERENCES \""
			+ TABLE_ATTRIBUTE
			+ "\" (\"" + COLUMN_ATTRIBUTE_ID + "\");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnAttributeColumnOfEdgeAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_EDGE_ATTRIBUTE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_EDGE_ATTRIBUTE = "ALTER TABLE \""
			+ TABLE_EDGE_ATTRIBUTE
			+ "\" DROP CONSTRAINT \""
			+ FOREIGN_KEY_EDGE_ATTRIBUTE_TO_GRAPH + "\";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromGraphColumnOfEdgeAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_EDGE_ATTRIBUTE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_EDGE_OF_ATTRIBUTE_VALUE = "ALTER TABLE \""
			+ TABLE_EDGE_ATTRIBUTE
			+ "\" DROP CONSTRAINT \""
			+ FOREIGN_KEY_EDGE_ATTRIBUTE_TO_EDGE + "\";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromEdgeColumnOfEdgeAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_EDGE_OF_ATTRIBUTE_VALUE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_EDGE_ATTRIBUTE = "ALTER TABLE \""
			+ TABLE_EDGE_ATTRIBUTE
			+ "\" DROP CONSTRAINT \""
			+ FOREIGN_KEY_EDGE_ATTRIBUTE_TO_ATTRIBUTE + "\";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromAttributeColumnOfEdgeAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_EDGE_ATTRIBUTE);
	}

	/*
	 * private static final String ADD_CLUSTERED_INDEX_ON_EDGE_ATTRIBUTE_VALUES
	 * = "CREATE INDEX \"edgeAttributeValueIndex\" ON \""
	 * +JGraLab.getDatabaseTablePrefix() + EDGE_ATTRIBUTE_VALUE_TABLE_NAME
	 * +"\"( \""+COLUMN_EDGE_ID+"\" ASC, \""
	 * +COLUMN_GRAPH_ID+"\" ASC, \""+COLUMN_ATTRIBUTE_ID
	 * +"\" ) WITH (FILLFACTOR=80);" +
	 * "ALTER TABLE \""+JGraLab.getDatabaseTablePrefix() +
	 * EDGE_ATTRIBUTE_VALUE_TABLE_NAME
	 * +"\" CLUSTER ON \"edgeAttributeValueIndex\";" +
	 * "ANALYZE \""+JGraLab.getDatabaseTablePrefix() +
	 * EDGE_ATTRIBUTE_VALUE_TABLE_NAME +"\";";
	 * 
	 * @Override public PreparedStatement
	 * addClusteredIndexOnEdgeAttributeValues()throws SQLException { return
	 * this.getPreparedStatement(ADD_CLUSTERED_INDEX_ON_EDGE_ATTRIBUTE_VALUES);
	 * }
	 * 
	 * private static final String
	 * ADD_CLUSTERED_INDEX_ON_VERTEX_ATTRIBUTE_VALUES =
	 * "CREATE INDEX \"vertexAttributeValueIndex\" ON \""
	 * +JGraLab.getDatabaseTablePrefix() + VERTEX_ATTRIBUTE_VALUE_TABLE_NAME
	 * +"\"( \""+COLUMN_VERTEX_ID+"\" ASC, \""
	 * +COLUMN_GRAPH_ID+"\" ASC, \""+COLUMN_ATTRIBUTE_ID
	 * +"\" ) WITH (FILLFACTOR=80);" +
	 * "ALTER TABLE \""+JGraLab.getDatabaseTablePrefix() +
	 * VERTEX_ATTRIBUTE_VALUE_TABLE_NAME
	 * +"\" CLUSTER ON \"vertexAttributeValueIndex\";" +
	 * "ANALYZE \""+JGraLab.getDatabaseTablePrefix() +
	 * VERTEX_ATTRIBUTE_VALUE_TABLE_NAME +"\";";
	 * 
	 * @Override public PreparedStatement
	 * addClusteredIndexOnVertexAttributeValues()throws SQLException { return
	 * this
	 * .getPreparedStatement(ADD_CLUSTERED_INDEX_ON_VERTEX_ATTRIBUTE_VALUES); }
	 * 
	 * private static final String ADD_CLUSTERED_INDEX_ON_GRAPH_ATTRIBUTE_VALUES
	 * = "CREATE INDEX \"graphAttributeValueIndex\" ON \""
	 * +JGraLab.getDatabaseTablePrefix() + GRAPH_ATTRIBUTE_VALUE_TABLE_NAME
	 * +"\"( \""+COLUMN_GRAPH_ID+"\" ASC, \""+COLUMN_ATTRIBUTE_ID+
	 * "\" ) WITH (FILLFACTOR=80);" +
	 * "ALTER TABLE \""+JGraLab.getDatabaseTablePrefix() +
	 * GRAPH_ATTRIBUTE_VALUE_TABLE_NAME
	 * +"\" CLUSTER ON \"graphAttributeValueIndex\";" +
	 * "ANALYZE \""+JGraLab.getDatabaseTablePrefix() +
	 * GRAPH_ATTRIBUTE_VALUE_TABLE_NAME +"\";";
	 * 
	 * @Override public PreparedStatement
	 * addClusteredIndexOnGraphAttributeValues()throws SQLException { return
	 * this.getPreparedStatement(ADD_CLUSTERED_INDEX_ON_GRAPH_ATTRIBUTE_VALUES);
	 * }
	 * 
	 * private static final String DROP_CLUSTERED_INDICES_FROM_ATTRIBUTE_VALUES
	 * = "DROP INDEX IF EXISTS \"edgeAttributeValueIndex\";" +
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
	 * GRAPH_ATTRIBUTE_VALUE_TABLE_NAME +"\";" +
	 * "CLUSTER \""+JGraLab.getDatabaseTablePrefix() +
	 * VERTEX_ATTRIBUTE_VALUE_TABLE_NAME +"\";" +
	 * "CLUSTER \""+JGraLab.getDatabaseTablePrefix() +
	 * EDGE_ATTRIBUTE_VALUE_TABLE_NAME +"\";";
	 * 
	 * @Override public PreparedStatement clusterAttributeValues() throws
	 * SQLException { return
	 * this.getPreparedStatement(CLUSTER_ATTRIBUTE_VALUES); }
	 */

	// --- to insert a graph ------------------------------------------

	// --- to insert a vertex ------------------------------------------

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

	private static final String createSqlInsertStatementFor(
			DatabasePersistableVertex vertex) {
		String sqlStatement = INSERT_VERTEX;
		int attributeCount = vertex.getAttributedElementClass()
				.getAttributeList().size();
		for (int i = 0; i < attributeCount; i++) {
			sqlStatement += INSERT_VERTEX_ATTRIBUTE_VALUE;
		}
		return sqlStatement;
	}

	// --- to insert an edge -------------------------------------------

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

	private static final String createSqlInsertStatementFor(
			DatabasePersistableEdge edge) {
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

	// --- to open a graph schema -------------------------------------------

	// --- to open a graph --------------------------------------------

	// --- to get a vertex -------------------------------------------

	// --- to get an edge ---------------------------------------------

	// --- to delete a graph ------------------------------------------

	// --- to delete a vertex -----------------------------------------

	// --- to delete an edge ------------------------------------------

	// --- to update a graph ------------------------------------------

	private static final String UPDATE_VERTEX_LIST_VERSION = "UPDATE \""
			+ TABLE_GRAPH + "\" SET \"" + COLUMN_GRAPH_VSEQ_VERSION
			+ "\" = ? WHERE \"" + COLUMN_GRAPH_ID + "\" = ?";

	@Override
	public PreparedStatement updateVertexListVersionOfGraph(int gId,
			long version) throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_VERTEX_LIST_VERSION);
		statement.setLong(1, version);
		statement.setInt(2, gId);
		return statement;
	}

	// --- to update a vertex -----------------------------------------

	private static final String UPDATE_VERTEX_ID = "UPDATE \"" + TABLE_VERTEX
			+ "\" SET \"" + COLUMN_VERTEX_ID + "\" = ? WHERE \""
			+ COLUMN_VERTEX_ID + "\" = ? AND \"" + COLUMN_GRAPH_ID + "\" = ?";

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
			+ TABLE_VERTEX + "\" SET \"" + COLUMN_SEQUENCE_NUMBER
			+ "\" = ? WHERE \"" + COLUMN_VERTEX_ID + "\" = ? AND \""
			+ COLUMN_GRAPH_ID + "\" = ?";

	@Override
	public PreparedStatement updateSequenceNumberInVSeqOfVertex(int vId,
			int gId, long sequenceNumberInVSeq) throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_SEQUENCE_NUMBER_OF_VERTEX);
		statement.setLong(1, sequenceNumberInVSeq);
		statement.setInt(2, vId);
		statement.setInt(3, gId);
		return statement;
	}

	private static final String UPDATE_INCIDENCE_LIST_VERSION = "UPDATE \""
			+ TABLE_VERTEX + "\" SET \"" + COLUMN_VERTEX_LAMBDA_SEQ_VERSION
			+ "\" = ? WHERE \"" + COLUMN_VERTEX_ID + "\" = ? AND \""
			+ COLUMN_GRAPH_ID + "\" = ?;";

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

	private static final String UPDATE_SEQUENCE_NUMBER_IN_EDGE_LIST = "UPDATE \""
			+ TABLE_EDGE
			+ "\" SET \""
			+ COLUMN_SEQUENCE_NUMBER
			+ "\" = ? WHERE \""
			+ COLUMN_EDGE_ID
			+ "\" = ? AND \""
			+ COLUMN_GRAPH_ID + "\" = ?";

	@Override
	public PreparedStatement updateSequenceNumberInESeqOfEdge(int eId, int gId,
			long SequenceNumberInESeq) throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_SEQUENCE_NUMBER_IN_EDGE_LIST);
		statement.setLong(1, SequenceNumberInESeq);
		statement.setInt(2, eId);
		statement.setInt(3, gId);
		return statement;
	}

	private static final String UPDATE_SEQUENCE_NUMBER_IN_INCIDENCE_LIST = "UPDATE \""
			+ TABLE_INCIDENCE
			+ "\" SET \""
			+ COLUMN_SEQUENCE_NUMBER
			+ "\" = ? WHERE \""
			+ COLUMN_EDGE_ID
			+ "\" = ? AND \""
			+ COLUMN_GRAPH_ID + "\" = ? AND \"" + COLUMN_VERTEX_ID + "\" = ?";

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
			+ "FOR vertex IN (SELECT \""
			+ COLUMN_VERTEX_ID
			+ "\" FROM \""
			+ TABLE_VERTEX
			+ "\" WHERE \""
			+ COLUMN_GRAPH_ID
			+ "\" = \"graphId\" ORDER BY \""
			+ COLUMN_SEQUENCE_NUMBER
			+ "\" ASC) LOOP\n"
			+ "EXECUTE 'UPDATE \""
			+ TABLE_VERTEX
			+ "\" SET \""
			+ COLUMN_SEQUENCE_NUMBER
			+ "\" = $1 WHERE \""
			+ COLUMN_VERTEX_ID
			+ "\" = $2 AND \""
			+ COLUMN_GRAPH_ID
			+ "\" =$3' USING current, vertex.\""
			+ COLUMN_VERTEX_ID
			+ "\", \"graphId\";\n"
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
			+ "FOR edge IN (SELECT \""
			+ COLUMN_EDGE_ID
			+ "\" FROM \""
			+ TABLE_EDGE
			+ "\" WHERE \""
			+ COLUMN_GRAPH_ID
			+ "\" = \"graphId\" ORDER BY \""
			+ COLUMN_SEQUENCE_NUMBER
			+ "\" ASC) LOOP\n"
			+ "EXECUTE 'UPDATE \""
			+ TABLE_EDGE
			+ "\" SET \""
			+ COLUMN_SEQUENCE_NUMBER
			+ "\" = $1 WHERE \""
			+ COLUMN_EDGE_ID
			+ "\" = $2 AND \""
			+ COLUMN_GRAPH_ID
			+ "\" = $3' USING current, edge.\""
			+ COLUMN_EDGE_ID
			+ "\", \"graphId\";\n"
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
			+ "FOR incidence IN (SELECT \""
			+ COLUMN_EDGE_ID
			+ "\", "
			+ COLUMN_INCIDENCE_DIRECTION
			+ " FROM \""
			+ TABLE_INCIDENCE
			+ "\" WHERE \""
			+ COLUMN_VERTEX_ID
			+ "\" = \"vertexId\" AND \""
			+ COLUMN_GRAPH_ID
			+ "\" = \"graphId\" ORDER BY \""
			+ COLUMN_SEQUENCE_NUMBER
			+ "\" ASC) LOOP\n"
			+ "EXECUTE 'UPDATE \""
			+ TABLE_INCIDENCE
			+ "\" SET \""
			+ COLUMN_SEQUENCE_NUMBER
			+ "\" = $1 WHERE \""
			+ COLUMN_VERTEX_ID
			+ "\" = $2 AND \""
			+ COLUMN_GRAPH_ID
			+ "\" = $3 AND \""
			+ COLUMN_EDGE_ID
			+ "\" = $4 AND "
			+ COLUMN_INCIDENCE_DIRECTION
			+ " = $5' USING current, \"vertexId\", \"graphId\", incidence.\""
			+ COLUMN_EDGE_ID
			+ "\", incidence."
			+ COLUMN_INCIDENCE_DIRECTION
			+ ";\n"
			+ "current := current + distance;\n"
			+ "END LOOP;\n"
			+ "RETURN 1;\n" + "END;\n" + "$$ LANGUAGE plpgsql;";

	@Override
	public PreparedStatement createStoredProcedureToReorganizeIncidenceList()
			throws SQLException {
		return getPreparedStatement(STORED_PROCEDURE_REORGANIZE_INCIDENCE_LIST);
	}

	private static final String STORED_PROCEDURE_INSERT_VERTEX = "CREATE FUNCTION \"insertVertex\"(\"vertexId\" INT, \"graphId\" INT, \""
			+ COLUMN_TYPE_ID
			+ "\" INT, \""
			+ COLUMN_SEQUENCE_NUMBER
			+ "\" BIGINT) RETURNS INT AS $$\n"
			+ "BEGIN\n"
			+ "EXECUTE 'INSERT INTO \""
			+ TABLE_VERTEX
			+ "\" ( \""
			+ COLUMN_VERTEX_ID
			+ "\", \""
			+ COLUMN_GRAPH_ID
			+ "\", \""
			+ COLUMN_TYPE_ID
			+ "\", \""
			+ COLUMN_VERTEX_LAMBDA_SEQ_VERSION
			+ "\", \""
			+ COLUMN_SEQUENCE_NUMBER
			+ "\" ) VALUES ($1, $2, $3, 0, $4)' USING \"vertexId\", \"graphId\", \""
			+ COLUMN_TYPE_ID
			+ "\", \""
			+ COLUMN_SEQUENCE_NUMBER
			+ "\";\n"
			+ "RETURN 1;\n" + "END;\n" + "$$ LANGUAGE plpgsql;";

	public PreparedStatement createStoredProcedureToInsertVertex()
			throws SQLException {
		return getPreparedStatement(STORED_PROCEDURE_INSERT_VERTEX);
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

	private static final String CLEAR_ALL_TABLES = "TRUNCATE TABLE \""
			+ TABLE_ATTRIBUTE + "\",\"" + TABLE_EDGE_ATTRIBUTE + "\",\""
			+ TABLE_EDGE + "\",\"" + TABLE_GRAPH_ATTRIBUTE + "\",\""
			+ TABLE_SCHEMA + "\",\"" + TABLE_GRAPH + "\",\"" + TABLE_INCIDENCE
			+ "\",\"" + TABLE_TYPE + "\",\"" + TABLE_VERTEX_ATTRIBUTE + "\",\""
			+ TABLE_VERTEX + "\";";

	@Override
	public PreparedStatement clearAllTables() throws SQLException {
		return getPreparedStatement(CLEAR_ALL_TABLES);
	}
}
