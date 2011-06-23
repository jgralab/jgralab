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

/**
 * Factory that creates MySQL specific prepared statements.
 * 
 * @author ultbreit@uni-koblenz.de
 * 
 *         TODO Activate Multiple Statement Execution!
 */
public class MySqlStatementList extends SqlStatementList {

	public MySqlStatementList(GraphDatabase graphDatabase)
			throws GraphDatabaseException {
		super(graphDatabase);
	}

	@Override
	public String makeQueryVendorSpecific(String query) {
		return query.replace(SqlStatementList.QUOTE, "`").replace(
				SqlStatementList.EOQ, ";").replace(DIRECTION_TYPE, "?");
	}

	private static final String CREATE_GRAPH_SCHEMA_TABLE = "CREATE TABLE "
			+ TABLE_SCHEMA + "(" + "" + COLUMN_SCHEMA_ID
			+ " INT AUTO_INCREMENT," + "" + COLUMN_SCHEMA_PACKAGE_PREFIX
			+ " TEXT," + "" + COLUMN_SCHEMA_NAME + " TEXT," + ""
			+ COLUMN_SCHEMA_TG + " TEXT," + "PRIMARY KEY(" + COLUMN_SCHEMA_ID
			+ ")" + ");";

	@Override
	public PreparedStatement createGraphSchemaTableWithConstraints()
			throws SQLException {
		return connection.prepareStatement(CREATE_GRAPH_SCHEMA_TABLE);
	}

	private static final String CREATE_TYPE_TABLE = "CREATE TABLE "
			+ TABLE_TYPE + "(" + "" + COLUMN_TYPE_ID + " INT AUTO_INCREMENT,"
			+ "" + COLUMN_TYPE_QNAME + " TEXT," + "" + COLUMN_SCHEMA_ID
			+ " INT REFERENCES " + TABLE_SCHEMA + "," + "PRIMARY KEY("
			+ COLUMN_TYPE_ID + ")" + ");";

	@Override
	public PreparedStatement createTypeTableWithConstraints()
			throws SQLException {
		return connection.prepareStatement(CREATE_TYPE_TABLE);
	}

	private static final String CREATE_GRAPH_TABLE = "CREATE TABLE "
			+ TABLE_GRAPH + "(" + "" + COLUMN_GRAPH_ID + " INT AUTO_INCREMENT,"
			+ "" + COLUMN_GRAPH_UID + " TEXT," + "" + COLUMN_GRAPH_VERSION
			+ " BIGINT," + "" + COLUMN_GRAPH_VSEQ_VERSION + " BIGINT," + ""
			+ COLUMN_GRAPH_ESEQ_VERSION + " BIGINT," + "" + COLUMN_TYPE_ID
			+ " INT," + "PRIMARY KEY(" + COLUMN_GRAPH_ID + ")" + ");";

	@Override
	public PreparedStatement createGraphTableWithConstraints()
			throws SQLException {
		return connection.prepareStatement(CREATE_GRAPH_TABLE);
	}

	private static final String CREATE_VERTEX_TABLE = "CREATE TABLE "
			+ TABLE_VERTEX + "(" + "" + COLUMN_VERTEX_ID + " INT," + ""
			+ COLUMN_GRAPH_ID + " INT," + "" + COLUMN_TYPE_ID + " INT," + ""
			+ COLUMN_VERTEX_LAMBDA_SEQ_VERSION + " BIGINT," + ""
			+ COLUMN_SEQUENCE_NUMBER + " BIGINT" + ");";

	@Override
	public PreparedStatement createVertexTable() throws SQLException {
		return connection.prepareStatement(CREATE_VERTEX_TABLE);
	}

	private static final String ADD_PRIMARY_KEY_CONSTRAINT_ON_VERTEX_TABLE = "ALTER TABLE "
			+ TABLE_VERTEX
			+ " ADD CONSTRAINT "
			+ PRIMARY_KEY_VERTEX
			+ " PRIMARY KEY ( "
			+ COLUMN_VERTEX_ID
			+ ", "
			+ COLUMN_GRAPH_ID
			+ " );";

	@Override
	public PreparedStatement addPrimaryKeyConstraintOnVertexTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_PRIMARY_KEY_CONSTRAINT_ON_VERTEX_TABLE);
	}

	private static final String DROP_PRIMARY_KEY_CONSTRAINT_FROM_VERTEX_TABLE = "ALTER TABLE "
			+ TABLE_VERTEX + " DROP CONSTRAINT " + PRIMARY_KEY_VERTEX + ";";

	@Override
	public PreparedStatement dropPrimaryKeyConstraintFromVertexTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_PRIMARY_KEY_CONSTRAINT_FROM_VERTEX_TABLE);
	}

	private static final String CREATE_EDGE_TABLE = "CREATE TABLE "
			+ TABLE_EDGE + "(" + "" + COLUMN_EDGE_ID + " INT," + ""
			+ COLUMN_GRAPH_ID + " INT," + "" + COLUMN_TYPE_ID + " INT," + ""
			+ COLUMN_SEQUENCE_NUMBER + " BIGINT" + ");";

	@Override
	public PreparedStatement createEdgeTable() throws SQLException {
		return connection.prepareStatement(CREATE_EDGE_TABLE);
	}

	private static final String ADD_PRIMARY_KEY_CONSTRAINT_ON_EDGE_TABLE = "ALTER TABLE "
			+ TABLE_EDGE
			+ " ADD CONSTRAINT "
			+ PRIMARY_KEY_EDGE
			+ " PRIMARY KEY ( "
			+ COLUMN_EDGE_ID
			+ ", "
			+ COLUMN_GRAPH_ID
			+ " );";

	@Override
	public PreparedStatement addPrimaryKeyConstraintOnEdgeTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_PRIMARY_KEY_CONSTRAINT_ON_EDGE_TABLE);
	}

	private static final String DROP_PRIMARY_KEY_CONSTRAINT_FROM_EDGE_TABLE = "ALTER TABLE "
			+ TABLE_EDGE + " DROP CONSTRAINT " + PRIMARY_KEY_EDGE + ";";

	@Override
	public PreparedStatement dropPrimaryKeyConstraintFromEdgeTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_PRIMARY_KEY_CONSTRAINT_FROM_EDGE_TABLE);
	}

	private static final String CREATE_INCIDENCE_TABLE = "CREATE TABLE "
			+ TABLE_INCIDENCE + "(" + "" + COLUMN_EDGE_ID + " INT," + ""
			+ COLUMN_VERTEX_ID + " INT," + "" + COLUMN_GRAPH_ID + " INT," + ""
			+ COLUMN_INCIDENCE_DIRECTION + " ENUM('IN', 'OUT')," + ""
			+ COLUMN_SEQUENCE_NUMBER + " BIGINT" + ");";

	@Override
	public PreparedStatement createIncidenceTable() throws SQLException {
		return connection.prepareStatement(CREATE_INCIDENCE_TABLE);
	}

	private static final String ADD_PRIMARY_KEY_CONSTRAINT_ON_INCIDENCE_TABLE = "ALTER TABLE "
			+ TABLE_INCIDENCE
			+ " ADD CONSTRAINT "
			+ PRIMARY_KEY_INCIDENCE
			+ " PRIMARY KEY ( "
			+ COLUMN_EDGE_ID
			+ ", "
			+ COLUMN_GRAPH_ID
			+ ", " + COLUMN_INCIDENCE_DIRECTION + " )";

	@Override
	public PreparedStatement addPrimaryKeyConstraintOnIncidenceTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_PRIMARY_KEY_CONSTRAINT_ON_INCIDENCE_TABLE);
	}

	private static final String DROP_PRIMARY_KEY_CONSTRAINT_FROM_INCIDENCE_TABLE = "ALTER TABLE "
			+ TABLE_INCIDENCE
			+ " DROP CONSTRAINT "
			+ PRIMARY_KEY_INCIDENCE
			+ "";

	@Override
	public PreparedStatement dropPrimaryKeyConstraintFromIncidenceTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_PRIMARY_KEY_CONSTRAINT_FROM_INCIDENCE_TABLE);
	}

	private static final String CREATE_INDEX_ON_LAMBDA_SEQ = "CREATE INDEX "
			+ INDEX_INCIDENCE_LAMBDA_SEQ + " ON " + TABLE_INCIDENCE + "("
			+ COLUMN_VERTEX_ID + ", " + COLUMN_GRAPH_ID + ", "
			+ COLUMN_SEQUENCE_NUMBER + " ASC )";

	@Override
	public PreparedStatement addIndexOnLambdaSeq() throws SQLException {
		return getPreparedStatement(CREATE_INDEX_ON_LAMBDA_SEQ);
	}

	private static final String DROP_INDEX_ON_LAMBDA_SEQ = "DROP INDEX "
			+ INDEX_INCIDENCE_LAMBDA_SEQ + " ON " + TABLE_INCIDENCE + "";

	@Override
	public PreparedStatement dropIndexOnLambdaSeq() throws SQLException {
		return getPreparedStatement(DROP_INDEX_ON_LAMBDA_SEQ);
	}

	private static final String CREATE_ATTRIBUTE_TABLE = "CREATE TABLE "
			+ TABLE_ATTRIBUTE + "(" + "" + COLUMN_ATTRIBUTE_ID
			+ " INT AUTO_INCREMENT," + "" + COLUMN_ATTRIBUTE_NAME + " TEXT,"
			+ "" + COLUMN_SCHEMA_ID + " INT REFERENCES " + TABLE_SCHEMA + ","
			+ "PRIMARY KEY(" + COLUMN_ATTRIBUTE_ID + ")" + ");";

	@Override
	public PreparedStatement createAttributeTableWithConstraints()
			throws SQLException {
		return connection.prepareStatement(CREATE_ATTRIBUTE_TABLE);
	}

	private static final String CREATE_GRAPH_ATTRIBUTE_VALUE_TABLE = "CREATE TABLE "
			+ TABLE_GRAPH_ATTRIBUTE
			+ "("
			+ ""
			+ COLUMN_GRAPH_ID
			+ " INT REFERENCES "
			+ TABLE_GRAPH
			+ ","
			+ ""
			+ COLUMN_ATTRIBUTE_ID
			+ " INT REFERENCES "
			+ TABLE_ATTRIBUTE
			+ ","
			+ ""
			+ COLUMN_ATTRIBUTE_VALUE
			+ " TEXT,"
			+ "CONSTRAINT "
			+ PRIMARY_KEY_GRAPH_ATTRIBUTE
			+ " PRIMARY KEY ( "
			+ COLUMN_GRAPH_ID
			+ ", " + COLUMN_ATTRIBUTE_ID + " )" + ");";

	@Override
	public PreparedStatement createGraphAttributeValueTableWithConstraints()
			throws SQLException {
		return connection.prepareStatement(CREATE_GRAPH_ATTRIBUTE_VALUE_TABLE);
	}

	private static final String CREATE_VERTEX_ATTRIBUTE_VALUE_TABLE = "CREATE TABLE "
			+ TABLE_VERTEX_ATTRIBUTE
			+ "("
			+ ""
			+ COLUMN_VERTEX_ID
			+ " INT,"
			+ ""
			+ COLUMN_GRAPH_ID
			+ " INT,"
			+ ""
			+ COLUMN_ATTRIBUTE_ID
			+ " INT," + "" + COLUMN_ATTRIBUTE_VALUE + " TEXT" + ");";

	@Override
	public PreparedStatement createVertexAttributeValueTable()
			throws SQLException {
		return connection.prepareStatement(CREATE_VERTEX_ATTRIBUTE_VALUE_TABLE);
	}

	private static final String ADD_PRIMARY_KEY_CONSTRAINT_ON_VERTEX_ATTRIBUTE_VALUE_TABLE = "ALTER TABLE "
			+ TABLE_VERTEX_ATTRIBUTE
			+ " ADD CONSTRAINT "
			+ PRIMARY_KEY_VERTEX_ATTRIBUTE
			+ " PRIMARY KEY ( "
			+ COLUMN_VERTEX_ID
			+ ", "
			+ COLUMN_GRAPH_ID
			+ ", "
			+ COLUMN_ATTRIBUTE_ID + " )";

	@Override
	public PreparedStatement addPrimaryKeyConstraintOnVertexAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_PRIMARY_KEY_CONSTRAINT_ON_VERTEX_ATTRIBUTE_VALUE_TABLE);
	}

	private static final String DROP_PRIMARY_KEY_CONSTRAINT_FROM_VERTEX_ATTRIBUTE_VALUE_TABLE = "ALTER TABLE "
			+ TABLE_VERTEX_ATTRIBUTE
			+ " DROP CONSTRAINT "
			+ PRIMARY_KEY_VERTEX_ATTRIBUTE + "";

	@Override
	public PreparedStatement dropPrimaryKeyConstraintFromVertexAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_PRIMARY_KEY_CONSTRAINT_FROM_VERTEX_ATTRIBUTE_VALUE_TABLE);
	}

	private static final String CREATE_EDGE_ATTRIBUTE_VALUE_TABLE = "CREATE TABLE "
			+ TABLE_EDGE_ATTRIBUTE
			+ "("
			+ ""
			+ COLUMN_EDGE_ID
			+ " INT,"
			+ ""
			+ COLUMN_GRAPH_ID
			+ " INT,"
			+ ""
			+ COLUMN_ATTRIBUTE_ID
			+ " INT,"
			+ "" + COLUMN_ATTRIBUTE_VALUE + " TEXT" + ")";

	@Override
	public PreparedStatement createEdgeAttributeValueTable()
			throws SQLException {
		return connection.prepareStatement(CREATE_EDGE_ATTRIBUTE_VALUE_TABLE);
	}

	private static final String ADD_PRIMARY_KEY_CONSTRAINT_ON_EDGE_ATTRIBUTE_VALUE_TABLE = "ALTER TABLE "
			+ TABLE_EDGE_ATTRIBUTE
			+ " ADD CONSTRAINT "
			+ PRIMARY_KEY_VERTEX_ATTRIBUTE
			+ " PRIMARY KEY ( "
			+ COLUMN_EDGE_ID
			+ ", " + COLUMN_GRAPH_ID + ", " + COLUMN_ATTRIBUTE_ID + " )";

	@Override
	public PreparedStatement addPrimaryKeyConstraintOnEdgeAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_PRIMARY_KEY_CONSTRAINT_ON_EDGE_ATTRIBUTE_VALUE_TABLE);
	}

	private static final String DROP_PRIMARY_KEY_CONSTRAINT_FROM_EDGE_ATTRIBUTE_VALUE_TABLE = "ALTER TABLE "
			+ TABLE_EDGE_ATTRIBUTE
			+ " DROP CONSTRAINT "
			+ PRIMARY_KEY_EDGE_ATTRIBUTE + "";

	/**
	 * Beware: if there is no primary key defined on a table MySql will generate
	 * a hidden clustered index, see:
	 * http://dev.mysql.com/doc/refman/5.5/en/innodb-index-types.html
	 */
	@Override
	public PreparedStatement dropPrimaryKeyConstraintFromEdgeAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_PRIMARY_KEY_CONSTRAINT_FROM_EDGE_ATTRIBUTE_VALUE_TABLE);
	}

	// --- to insert schema information -------------------------------

	// --- to insert a graph ------------------------------------------

	// --- to insert a vertex ------------------------------------------

	// --- to insert an edge -------------------------------------------

	// --- to open a graph schema -------------------------------------------

	// --- to open a graph --------------------------------------------

	// --- to get a vertex -------------------------------------------

	// --- to get an edge ---------------------------------------------

	// --- to delete a graph ------------------------------------------

	// --- to delete a vertex -----------------------------------------

	// --- to delete an edge ------------------------------------------

	// --- to update a graph ------------------------------------------

	// --- to update a vertex -----------------------------------------

	// --- to update an edge ------------------------------------------

	private static final String STORED_PROCEDURE_REORGANIZE_VERTEX_LIST = "CREATE PROCEDURE reorganizeVSeqOfGraph( graphId INT, start BIGINT ) "
			+ "BEGIN "
			+ "DECLARE distance BIGINT;"
			+ "DECLARE currentSequenceNumber BIGINT;"
			+ "DECLARE currentVId INT;" + "DECLARE vertex CURSOR FOR SELECT "
			+ COLUMN_VERTEX_ID
			+ " FROM "
			+ TABLE_VERTEX
			+ " WHERE "
			+ COLUMN_GRAPH_ID
			+ " = graphId ORDER BY "
			+ COLUMN_SEQUENCE_NUMBER
			+ " ASC;"
			+

			"SET distance = 4294967296, currentSequenceNumber = start;"
			+ "OPEN vertex;"
			+ "BEGIN "
			+ "DECLARE EXIT HANDLER FOR NOT FOUND BEGIN END;"
			+ "LOOP"
			+ "FETCH vertex INTO currentVId;"
			+ "UPDATE "
			+ TABLE_VERTEX
			+ " SET "
			+ COLUMN_SEQUENCE_NUMBER
			+ " = currentSequenceNumber WHERE "
			+ COLUMN_VERTEX_ID
			+ " = currentVId;"
			+ "SET currentSequenceNumber = currentSequenceNumber + distance;"
			+ "END LOOP;" + "END;" + "CLOSE vertex;" + "END";

	@Override
	public PreparedStatement createStoredProcedureToReorganizeVertexList()
			throws SQLException {
		return getPreparedStatement(STORED_PROCEDURE_REORGANIZE_VERTEX_LIST);
	}

	private static final String STORED_PROCEDURE_REORGANIZE_EDGE_LIST = "CREATE PROCEDURE reorganizeESeqOfGraph( graphId INT, start BIGINT ) "
			+ "BEGIN "
			+ "DECLARE distance BIGINT;"
			+ "DECLARE currentSequenceNumber BIGINT;"
			+ "DECLARE currentEId INT;" + "DECLARE edge CURSOR FOR SELECT "
			+ COLUMN_EDGE_ID
			+ " FROM "
			+ TABLE_EDGE
			+ " WHERE "
			+ COLUMN_GRAPH_ID
			+ " = graphId ORDER BY "
			+ COLUMN_SEQUENCE_NUMBER
			+ " ASC;"
			+

			"SET distance = 4294967296, currentSequenceNumber = start;"
			+ "OPEN edge;"
			+ "BEGIN "
			+ "DECLARE EXIT HANDLER FOR NOT FOUND BEGIN END;"
			+ "LOOP"
			+ "FETCH edge INTO currentEId;"
			+ "UPDATE "
			+ TABLE_EDGE
			+ " SET "
			+ COLUMN_SEQUENCE_NUMBER
			+ " = currentSequenceNumber WHERE "
			+ COLUMN_EDGE_ID
			+ " = currentEId;"
			+ "SET currentSequenceNumber = currentSequenceNumber + distance;"
			+ "END LOOP;" + "END;" + "CLOSE edge;" + "END";

	@Override
	public PreparedStatement createStoredProcedureToReorganizeEdgeList()
			throws SQLException {
		return getPreparedStatement(STORED_PROCEDURE_REORGANIZE_EDGE_LIST);
	}

	private static final String STORED_PROCEDURE_REORGANIZE_INCIDENCE_LIST = "CREATE PROCEDURE reorganizeLambdaSeqOfVertex( vertexId INT, graphId INT, start BIGINT ) "
			+ "BEGIN "
			+ "DECLARE distance BIGINT;"
			+ "DECLARE currentSequenceNumber BIGINT;"
			+ "DECLARE currentEId INT;"
			+ "DECLARE currentDirection ENUM('IN', 'OUT');"
			+ "DECLARE incidence CURSOR FOR SELECT "
			+ COLUMN_EDGE_ID
			+ ", "
			+ COLUMN_INCIDENCE_DIRECTION
			+ " FROM "
			+ TABLE_INCIDENCE
			+ " WHERE "
			+ COLUMN_VERTEX_ID
			+ " = vertexId AND "
			+ COLUMN_GRAPH_ID
			+ " = graphId ORDER BY "
			+ COLUMN_SEQUENCE_NUMBER
			+ " ASC;"
			+ "SET incidence = 4294967296, currentSequenceNumber = start;"
			+ "OPEN edge;"
			+ "BEGIN "
			+ "DECLARE EXIT HANDLER FOR NOT FOUND BEGIN END;"
			+ "LOOP"
			+ "FETCH incidence INTO currentEId, currentDirection;"
			+ "UPDATE "
			+ TABLE_INCIDENCE
			+ " SET "
			+ COLUMN_SEQUENCE_NUMBER
			+ " = currentSequenceNumber WHERE "
			+ COLUMN_EDGE_ID
			+ " = currentEId AND "
			+ COLUMN_VERTEX_ID
			+ " = vertexId AND "
			+ COLUMN_GRAPH_ID
			+ " = graphId AND "
			+ COLUMN_INCIDENCE_DIRECTION
			+ " = currentDirection;"
			+ "SET currentSequenceNumber = currentSequenceNumber + distance;"
			+ "END LOOP;" + "END;" + "CLOSE edge;" + "END";

	@Override
	public PreparedStatement createStoredProcedureToReorganizeIncidenceList()
			throws SQLException {
		return getPreparedStatement(STORED_PROCEDURE_REORGANIZE_INCIDENCE_LIST);
	}

	private static final String CALL_REORGANIZE_V_SEQ = "CALL reorganizeVSeqOfGraph( ?, ? )";

	@Override
	public CallableStatement createReorganizeVertexListCall(int gId, long start)
			throws SQLException {
		CallableStatement statement = connection
				.prepareCall(CALL_REORGANIZE_V_SEQ);
		statement.setInt(1, gId);
		statement.setLong(2, start);
		return statement;
	}

	private static final String CALL_REORGANIZE_E_SEQ = "CALL reorganizeESeqOfGraph( ?, ? )";

	@Override
	public CallableStatement createReorganizeEdgeListCall(int gId, long start)
			throws SQLException {
		CallableStatement statement = connection
				.prepareCall(CALL_REORGANIZE_E_SEQ);
		statement.setInt(1, gId);
		statement.setLong(2, start);
		return statement;
	}

	private static final String CALL_REORGANIZE_LAMBDA_SEQ = "CALL reorganizeLambdaSeqOfVertex( ?, ?, ? )";

	@Override
	public CallableStatement createReorganizeIncidenceListCall(int vId,
			int gId, long start) throws SQLException {
		CallableStatement statement = connection
				.prepareCall(CALL_REORGANIZE_LAMBDA_SEQ);
		statement.setInt(1, vId);
		statement.setInt(2, gId);
		statement.setLong(3, start);
		return statement;
	}

	private static final String CLEAR_TABLE_ATTRIBUTE = "TRUNCATE TABLE "
			+ TABLE_ATTRIBUTE;

	public PreparedStatement clearTableAttribute() throws SQLException {
		return getPreparedStatement(CLEAR_TABLE_ATTRIBUTE);
	}

	private static final String CLEAR_TABLE_EDGE_ATTRIBUTE_VALUE = "TRUNCATE TABLE "
			+ TABLE_EDGE_ATTRIBUTE;

	public PreparedStatement clearTableEdgeAttributeValue() throws SQLException {
		return getPreparedStatement(CLEAR_TABLE_EDGE_ATTRIBUTE_VALUE);
	}

	private static final String CLEAR_TABLE_EDGE = "TRUNCATE TABLE "
			+ TABLE_EDGE;

	public PreparedStatement clearTableEdge() throws SQLException {
		return getPreparedStatement(CLEAR_TABLE_EDGE);
	}

	private static final String CLEAR_TABLE_GRAPH_ATTRIBUTE_VALUE = "TRUNCATE TABLE "
			+ TABLE_GRAPH_ATTRIBUTE;

	public PreparedStatement clearTableGraphAttributeValue()
			throws SQLException {
		return getPreparedStatement(CLEAR_TABLE_GRAPH_ATTRIBUTE_VALUE);
	}

	private static final String CLEAR_TABLE_GRAPH_SCHEMA = "TRUNCATE TABLE "
			+ TABLE_SCHEMA;

	public PreparedStatement clearTableGraphSchema() throws SQLException {
		return getPreparedStatement(CLEAR_TABLE_GRAPH_SCHEMA);
	}

	private static final String CLEAR_TABLE_GRAPH = "TRUNCATE TABLE "
			+ TABLE_GRAPH;

	public PreparedStatement clearTableGraph() throws SQLException {
		return getPreparedStatement(CLEAR_TABLE_GRAPH);
	}

	private static final String CLEAR_TABLE_INCIDENCE = "TRUNCATE TABLE "
			+ TABLE_INCIDENCE;

	public PreparedStatement clearTableIncidence() throws SQLException {
		return getPreparedStatement(CLEAR_TABLE_INCIDENCE);
	}

	private static final String CLEAR_TABLE_TYPE = "TRUNCATE TABLE "
			+ TABLE_TYPE;

	public PreparedStatement clearTableType() throws SQLException {
		return getPreparedStatement(CLEAR_TABLE_TYPE);
	}

	private static final String CLEAR_TABLE_ATTRIBUTE_VALUE = "TRUNCATE TABLE "
			+ TABLE_VERTEX_ATTRIBUTE;

	public PreparedStatement clearTableAttributeValue() throws SQLException {
		return getPreparedStatement(CLEAR_TABLE_ATTRIBUTE_VALUE);
	}

	private static final String CLEAR_TABLE_VERTEX = "TRUNCATE TABLE "
			+ TABLE_VERTEX + ";";

	public PreparedStatement clearTableVertex() throws SQLException {
		return getPreparedStatement(CLEAR_TABLE_VERTEX);
	}

	@Override
	public PreparedStatement clearAllTables() throws SQLException {
		throw new UnsupportedOperationException(
				"Does not work in MySQL implementation.");
	}
}
