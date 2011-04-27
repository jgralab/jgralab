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

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.SortedSet;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.schema.Attribute;

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
		return query.replace(SqlStatementList.QUOTE, "").replace(
				SqlStatementList.EOQ, "").replace(DIRECTION_TYPE, "?");
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

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_VERTEX = "ALTER TABLE "
			+ TABLE_VERTEX
			+ " ADD CONSTRAINT "
			+ FOREIGN_KEY_VERTEX_ATTRIBUTE_TO_GRAPH
			+ " FOREIGN KEY ("
			+ COLUMN_GRAPH_ID
			+ ") REFERENCES "
			+ TABLE_GRAPH
			+ "("
			+ COLUMN_GRAPH_ID + ");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnGraphColumnOfVertexTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_VERTEX);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_VERTEX_TYPE = "ALTER TABLE "
			+ TABLE_VERTEX
			+ " ADD CONSTRAINT "
			+ FOREIGN_KEY_VERTEX_TO_TYPE
			+ " FOREIGN KEY ("
			+ COLUMN_TYPE_ID
			+ ") REFERENCES "
			+ TABLE_TYPE
			+ "(" + COLUMN_TYPE_ID + ");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnTypeColumnOfVertexTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_VERTEX_TYPE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_VERTEX = "ALTER TABLE "
			+ TABLE_VERTEX
			+ " DROP FOREIGN KEY "
			+ FOREIGN_KEY_VERTEX_TO_GRAPH
			+ ";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromGraphColumnOfVertexTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_VERTEX);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_VERTEX_TYPE = "ALTER TABLE "
			+ TABLE_VERTEX
			+ " DROP FOREIGN KEY "
			+ FOREIGN_KEY_VERTEX_TO_TYPE
			+ ";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromTypeColumnOfVertexTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_VERTEX_TYPE);
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

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_EDGE = "ALTER TABLE "
			+ TABLE_EDGE
			+ " ADD CONSTRAINT "
			+ FOREIGN_KEY_EDGE_TO_GRAPH
			+ " FOREIGN KEY ("
			+ COLUMN_GRAPH_ID
			+ ") REFERENCES "
			+ TABLE_GRAPH + " (" + COLUMN_GRAPH_ID + ");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnGraphColumnOfEdgeTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_EDGE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_EDGE_TYPE = "ALTER TABLE "
			+ TABLE_EDGE
			+ " ADD CONSTRAINT "
			+ FOREIGN_KEY_EDGE_TO_TYPE
			+ " FOREIGN KEY ("
			+ COLUMN_TYPE_ID
			+ ") REFERENCES "
			+ TABLE_TYPE
			+ " (" + COLUMN_TYPE_ID + ");";

	@Override
	public PreparedStatement addForeignKeyConstraintOnTypeColumnOfEdgeTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_EDGE_TYPE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_EDGE = "ALTER TABLE "
			+ TABLE_EDGE
			+ " DROP FOREIGN KEY "
			+ FOREIGN_KEY_EDGE_TO_GRAPH
			+ ";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromGraphColumnOfEdgeTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_EDGE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_EDGE_TYPE = "ALTER TABLE "
			+ TABLE_EDGE
			+ " DROP FOREIGN KEY "
			+ FOREIGN_KEY_EDGE_TO_TYPE
			+ ";";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromTypeColumnOfEdgeTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_EDGE_TYPE);
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

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_INCIDENCE = "ALTER TABLE "
			+ TABLE_INCIDENCE
			+ " ADD CONSTRAINT "
			+ FOREIGN_KEY_INCIDENCE_TO_GRAPH
			+ " FOREIGN KEY ("
			+ COLUMN_GRAPH_ID
			+ ") REFERENCES "
			+ TABLE_GRAPH
			+ "("
			+ COLUMN_GRAPH_ID + ")";

	@Override
	public PreparedStatement addForeignKeyConstraintOnGraphColumnOfIncidenceTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_INCIDENCE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_EDGE_OF_INCIDENCE = "ALTER TABLE "
			+ TABLE_INCIDENCE
			+ " ADD CONSTRAINT "
			+ FOREIGN_KEY_INCIDENCE_TO_EDGE
			+ " FOREIGN KEY ("
			+ COLUMN_EDGE_ID
			+ ", "
			+ COLUMN_GRAPH_ID
			+ ") REFERENCES "
			+ TABLE_EDGE
			+ "("
			+ COLUMN_EDGE_ID + ", " + COLUMN_GRAPH_ID + ")";

	@Override
	public PreparedStatement addForeignKeyConstraintOnEdgeColumnOfIncidenceTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_EDGE_OF_INCIDENCE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_VERTEX_OF_INCIDENCE = "ALTER TABLE "
			+ TABLE_INCIDENCE
			+ " ADD CONSTRAINT "
			+ FOREIGN_KEY_INCIDENCE_TO_VERTEX
			+ " FOREIGN KEY ("
			+ COLUMN_VERTEX_ID
			+ ", "
			+ COLUMN_GRAPH_ID
			+ ") REFERENCES "
			+ TABLE_VERTEX
			+ "("
			+ COLUMN_VERTEX_ID
			+ ", "
			+ COLUMN_GRAPH_ID
			+ ")";

	@Override
	public PreparedStatement addForeignKeyConstraintOnVertexColumnOfIncidenceTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_VERTEX_OF_INCIDENCE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_EDGE_OF_INCIDENCE = "ALTER TABLE "
			+ TABLE_INCIDENCE
			+ " DROP FOREIGN KEY "
			+ FOREIGN_KEY_INCIDENCE_TO_EDGE + "";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromEdgeColumnOfIncidenceTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_EDGE_OF_INCIDENCE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_INCIDENCE = "ALTER TABLE "
			+ TABLE_INCIDENCE
			+ " DROP FOREIGN KEY "
			+ FOREIGN_KEY_INCIDENCE_TO_GRAPH + "";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromGraphColumnOfIncidenceTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_INCIDENCE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_VERTEX_OF_INCIDENCE = "ALTER TABLE "
			+ TABLE_INCIDENCE
			+ " DROP FOREIGN KEY "
			+ FOREIGN_KEY_INCIDENCE_TO_VERTEX + "";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromVertexColumnOfIncidenceTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_VERTEX_OF_INCIDENCE);
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

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_VERTEX_ATTRIBUTE_VALUE = "ALTER TABLE "
			+ TABLE_VERTEX_ATTRIBUTE
			+ " ADD CONSTRAINT "
			+ FOREIGN_KEY_VERTEX_ATTRIBUTE_TO_GRAPH
			+ " FOREIGN KEY ("
			+ COLUMN_GRAPH_ID
			+ ") REFERENCES "
			+ TABLE_GRAPH
			+ "("
			+ COLUMN_GRAPH_ID + ")";

	@Override
	public PreparedStatement addForeignKeyConstraintOnGraphColumnOfVertexAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_VERTEX_ATTRIBUTE_VALUE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_VERTEX_OF_ATTRIBUTE_VALUE = "ALTER TABLE "
			+ TABLE_VERTEX_ATTRIBUTE
			+ " ADD CONSTRAINT "
			+ FOREIGN_KEY_VERTEX_ATTRIBUTE_TO_VERTEX
			+ " FOREIGN KEY ("
			+ COLUMN_VERTEX_ID
			+ ", "
			+ COLUMN_GRAPH_ID
			+ ") REFERENCES "
			+ TABLE_VERTEX
			+ "("
			+ COLUMN_VERTEX_ID
			+ ", "
			+ COLUMN_GRAPH_ID
			+ ")";

	@Override
	public PreparedStatement addForeignKeyConstraintOnVertexColumnOfVertexAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_VERTEX_OF_ATTRIBUTE_VALUE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_ATTRIBUTE_OF_VERTEX_ATTRIBUTE_VALUE = "ALTER TABLE "
			+ TABLE_VERTEX_ATTRIBUTE
			+ " ADD CONSTRAINT "
			+ FOREIGN_KEY_VERTEX_ATTRIBUTE_TO_ATTRIBUTE
			+ " FOREIGN KEY ( "
			+ COLUMN_ATTRIBUTE_ID
			+ " ) REFERENCES "
			+ TABLE_ATTRIBUTE
			+ " ("
			+ COLUMN_ATTRIBUTE_ID + ")";

	@Override
	public PreparedStatement addForeignKeyConstraintOnAttributeColumnOfVertexAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_ATTRIBUTE_OF_VERTEX_ATTRIBUTE_VALUE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_VERTEX_ATTRIBUTE_VALUE = "ALTER TABLE "
			+ TABLE_VERTEX_ATTRIBUTE
			+ " DROP FOREIGN KEY "
			+ FOREIGN_KEY_VERTEX_ATTRIBUTE_TO_GRAPH + "";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromGraphColumnOfVertexAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_VERTEX_ATTRIBUTE_VALUE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_VERTEX_OF_ATTRIBUTE_VALUE = "ALTER TABLE "
			+ TABLE_VERTEX_ATTRIBUTE
			+ " DROP FOREIGN KEY "
			+ FOREIGN_KEY_VERTEX_ATTRIBUTE_TO_VERTEX + "";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromVertexColumnOfVertexAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_VERTEX_OF_ATTRIBUTE_VALUE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_ATTRIBUTE_OF_VERTEX_ATTRIBUTE_VALUE = "ALTER TABLE "
			+ TABLE_VERTEX_ATTRIBUTE
			+ " DROP FOREIGN KEY "
			+ FOREIGN_KEY_VERTEX_ATTRIBUTE_TO_ATTRIBUTE + "";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromAttributeColumnOfVertexAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_ATTRIBUTE_OF_VERTEX_ATTRIBUTE_VALUE);
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

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_EDGE_ATTRIBUTE_VALUE = "ALTER TABLE "
			+ TABLE_EDGE_ATTRIBUTE
			+ " ADD CONSTRAINT "
			+ FOREIGN_KEY_EDGE_ATTRIBUTE_TO_GRAPH
			+ " FOREIGN KEY ("
			+ COLUMN_GRAPH_ID
			+ ") REFERENCES "
			+ TABLE_GRAPH
			+ " ("
			+ COLUMN_GRAPH_ID + ")";

	@Override
	public PreparedStatement addForeignKeyConstraintOnGraphColumnOfEdgeAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_GRAPH_OF_EDGE_ATTRIBUTE_VALUE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_EDGE_OF_ATTRIBUTE_VALUE = "ALTER TABLE "
			+ TABLE_EDGE_ATTRIBUTE
			+ " ADD CONSTRAINT "
			+ FOREIGN_KEY_EDGE_ATTRIBUTE_TO_EDGE
			+ " FOREIGN KEY ("
			+ COLUMN_EDGE_ID
			+ ") REFERENCES "
			+ TABLE_EDGE
			+ " ("
			+ COLUMN_EDGE_ID + ")";

	@Override
	public PreparedStatement addForeignKeyConstraintOnEdgeColumnOfEdgeAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_EDGE_OF_ATTRIBUTE_VALUE);
	}

	private static final String ADD_FOREIGN_KEY_CONSTRAINT_ON_EDGE_ATTRIBUTE = "ALTER TABLE "
			+ TABLE_EDGE_ATTRIBUTE
			+ " ADD CONSTRAINT "
			+ FOREIGN_KEY_EDGE_ATTRIBUTE_TO_ATTRIBUTE
			+ " FOREIGN KEY ("
			+ COLUMN_ATTRIBUTE_ID
			+ ") REFERENCES "
			+ TABLE_ATTRIBUTE
			+ " ("
			+ COLUMN_ATTRIBUTE_ID + ")";

	@Override
	public PreparedStatement addForeignKeyConstraintOnAttributeColumnOfEdgeAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(ADD_FOREIGN_KEY_CONSTRAINT_ON_EDGE_ATTRIBUTE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_EDGE_ATTRIBUTE = "ALTER TABLE "
			+ TABLE_EDGE_ATTRIBUTE
			+ " DROP FOREIGN KEY "
			+ FOREIGN_KEY_EDGE_ATTRIBUTE_TO_GRAPH + "";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromGraphColumnOfEdgeAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_GRAPH_OF_EDGE_ATTRIBUTE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_EDGE_OF_ATTRIBUTE_VALUE = "ALTER TABLE "
			+ TABLE_EDGE_ATTRIBUTE
			+ " DROP FOREIGN KEY "
			+ FOREIGN_KEY_EDGE_ATTRIBUTE_TO_EDGE + "";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromEdgeColumnOfEdgeAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_EDGE_OF_ATTRIBUTE_VALUE);
	}

	private static final String DROP_FOREIGN_KEY_CONSTRAINT_FROM_EDGE_ATTRIBUTE = "ALTER TABLE "
			+ TABLE_EDGE_ATTRIBUTE
			+ " DROP FOREIGN KEY "
			+ FOREIGN_KEY_EDGE_ATTRIBUTE_TO_ATTRIBUTE + "";

	@Override
	public PreparedStatement dropForeignKeyConstraintFromAttributeColumnOfEdgeAttributeValueTable()
			throws SQLException {
		return connection
				.prepareStatement(DROP_FOREIGN_KEY_CONSTRAINT_FROM_EDGE_ATTRIBUTE);
	}

	// --- to insert schema information -------------------------------

	// --- to insert a graph ------------------------------------------

	// --- to insert a vertex ------------------------------------------

	@Override
	public PreparedStatement insertVertex(DatabasePersistableVertex vertex)
			throws SQLException, GraphIOException {
		String sqlStatement = createSqlInsertStatementFor(vertex);
		PreparedStatement statement = getPreparedStatement(sqlStatement);
		setParametersForVertex(statement, vertex);
		setAttributeValuesForVertex(statement, vertex);
		// System.out.println(sqlStatement);
		// System.out.println(statement);
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

	private static final String SELECT_SCHEMA_ID = "SELECT " + COLUMN_SCHEMA_ID
			+ " FROM " + TABLE_SCHEMA + " WHERE "
			+ COLUMN_SCHEMA_PACKAGE_PREFIX + " = ? AND " + COLUMN_SCHEMA_NAME
			+ " = ?";

	@Override
	public PreparedStatement selectSchemaId(String packagePrefix, String name)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_SCHEMA_ID);
		statement.setString(1, packagePrefix);
		statement.setString(2, name);
		return statement;
	}

	// TODO Rewrite as join.
	private static final String SELECT_SCHEMA_DEFINITION_FOR_GRAPH = "SELECT "
			+ COLUMN_SCHEMA_TG + " " + "FROM " + TABLE_SCHEMA + " WHERE "
			+ COLUMN_SCHEMA_ID + " = (" + "SELECT " + COLUMN_SCHEMA_ID
			+ " FROM " + TABLE_TYPE + " WHERE " + COLUMN_TYPE_ID + " = ("
			+ "SELECT " + COLUMN_TYPE_ID + " FROM " + TABLE_GRAPH + " WHERE "
			+ COLUMN_GRAPH_UID + " = ?" + ")" + ")";

	@Override
	public PreparedStatement selectSchemaDefinitionForGraph(String uid)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_SCHEMA_DEFINITION_FOR_GRAPH);
		statement.setString(1, uid);
		return statement;
	}

	// TODO Rewrite as join.
	private static final String SELECT_SCHEMA_NAME = "SELECT "
			+ COLUMN_SCHEMA_PACKAGE_PREFIX + ", " + COLUMN_SCHEMA_NAME + " "
			+ "FROM " + TABLE_SCHEMA + " " + "WHERE " + COLUMN_SCHEMA_ID
			+ " = (" + "SELECT " + COLUMN_SCHEMA_ID + " FROM " + TABLE_TYPE
			+ " WHERE " + COLUMN_TYPE_ID + " = (" + "SELECT " + COLUMN_TYPE_ID
			+ " FROM " + TABLE_GRAPH + " WHERE " + COLUMN_GRAPH_UID + " = ?"
			+ ")" + ")";

	@Override
	public PreparedStatement selectSchemaNameForGraph(String uid)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_SCHEMA_NAME);
		statement.setString(1, uid);
		return statement;
	}

	// TODO Rewrite as join.
	private static final String SELECT_TYPES = "SELECT " + COLUMN_TYPE_QNAME
			+ ", " + COLUMN_TYPE_ID + " FROM " + TABLE_TYPE + " WHERE "
			+ COLUMN_SCHEMA_ID + " = (SELECT " + COLUMN_SCHEMA_ID + " FROM "
			+ TABLE_SCHEMA + " WHERE " + COLUMN_SCHEMA_PACKAGE_PREFIX
			+ " = ? AND " + COLUMN_SCHEMA_NAME + " = ?)";

	@Override
	public PreparedStatement selectTypesOfSchema(String packagePrefix,
			String name) throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_TYPES);
		statement.setString(1, packagePrefix);
		statement.setString(2, name);
		return statement;
	}

	private static final String SELECT_ATTRIBUTES = "SELECT " + TABLE_ATTRIBUTE
			+ "." + COLUMN_ATTRIBUTE_NAME + ", " + COLUMN_ATTRIBUTE_ID + " "
			+ "FROM " + TABLE_ATTRIBUTE + " JOIN " + TABLE_SCHEMA + " ON "
			+ TABLE_ATTRIBUTE + "." + COLUMN_SCHEMA_ID + " = " + TABLE_SCHEMA
			+ "." + COLUMN_SCHEMA_ID + " " + "WHERE "
			+ COLUMN_SCHEMA_PACKAGE_PREFIX + " = ? AND " + TABLE_SCHEMA + "."
			+ COLUMN_SCHEMA_NAME + " = ?";

	@Override
	public PreparedStatement selectAttributesOfSchema(String packagePrefix,
			String name) throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_ATTRIBUTES);
		statement.setString(1, packagePrefix);
		statement.setString(2, name);
		return statement;
	}

	// --- to open a graph --------------------------------------------

	private static final String SELECT_GRAPH = "SELECT " + COLUMN_GRAPH_ID
			+ ", " + COLUMN_GRAPH_VERSION + ", " + COLUMN_GRAPH_VSEQ_VERSION
			+ ", " + COLUMN_GRAPH_ESEQ_VERSION + " FROM " + TABLE_GRAPH
			+ " WHERE " + COLUMN_GRAPH_UID + " = ?";

	@Override
	public PreparedStatement selectGraph(String id) throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_GRAPH);
		statement.setString(1, id);
		return statement;
	}

	private static final String SELECT_VERTICES = "SELECT " + COLUMN_VERTEX_ID
			+ ", " + COLUMN_SEQUENCE_NUMBER + " FROM " + TABLE_VERTEX
			+ " WHERE " + COLUMN_GRAPH_ID + " = ? ORDER BY "
			+ COLUMN_SEQUENCE_NUMBER + " ASC";

	@Override
	public PreparedStatement selectVerticesOfGraph(int gId) throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_VERTICES);
		statement.setInt(1, gId);
		return statement;
	}

	private static final String SELECT_EDGES = "SELECT " + COLUMN_EDGE_ID
			+ ", " + COLUMN_SEQUENCE_NUMBER + " FROM " + TABLE_EDGE + " WHERE "
			+ COLUMN_GRAPH_ID + " = ? ORDER BY " + COLUMN_SEQUENCE_NUMBER
			+ " ASC";

	@Override
	public PreparedStatement selectEdgesOfGraph(int gId) throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_EDGES);
		statement.setInt(1, gId);
		return statement;
	}

	private static final String SELECT_ATTRIBUTE_VALUES_OF_GRAPH = "SELECT "
			+ COLUMN_ATTRIBUTE_NAME + ", " + COLUMN_ATTRIBUTE_VALUE + " FROM "
			+ TABLE_GRAPH_ATTRIBUTE + " JOIN " + TABLE_ATTRIBUTE + " ON "
			+ TABLE_GRAPH_ATTRIBUTE + "." + COLUMN_ATTRIBUTE_ID + " = "
			+ TABLE_ATTRIBUTE + "." + COLUMN_ATTRIBUTE_ID + " WHERE "
			+ COLUMN_GRAPH_ID + " = ?";

	@Override
	public PreparedStatement selectAttributeValuesOfGraph(int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_ATTRIBUTE_VALUES_OF_GRAPH);
		statement.setInt(1, gId);
		return statement;
	}

	// --- to get a vertex -------------------------------------------

	private static final String SELECT_VERTEX_WITH_INCIDENCES = "SELECT "
			+ COLUMN_TYPE_ID + ", " + COLUMN_VERTEX_LAMBDA_SEQ_VERSION + ", "
			+ TABLE_VERTEX + "." + COLUMN_SEQUENCE_NUMBER + ", "
			+ TABLE_INCIDENCE + "." + COLUMN_SEQUENCE_NUMBER + ", "
			+ COLUMN_INCIDENCE_DIRECTION + ", " + COLUMN_EDGE_ID + " "
			+ "FROM " + TABLE_VERTEX + " LEFT OUTER JOIN " + TABLE_INCIDENCE
			+ " ON ( " + TABLE_VERTEX + "." + COLUMN_VERTEX_ID + " = "
			+ TABLE_INCIDENCE + "." + COLUMN_VERTEX_ID + " AND " + TABLE_VERTEX
			+ "." + COLUMN_GRAPH_ID + " = " + TABLE_INCIDENCE + "."
			+ COLUMN_GRAPH_ID + " ) " + "WHERE " + TABLE_VERTEX + "."
			+ COLUMN_VERTEX_ID + " = ? AND " + TABLE_VERTEX + "."
			+ COLUMN_GRAPH_ID + " = ? " + "ORDER BY " + TABLE_INCIDENCE + "."
			+ COLUMN_SEQUENCE_NUMBER + " ASC";

	@Override
	public PreparedStatement selectVertexWithIncidences(int vId, int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_VERTEX_WITH_INCIDENCES);
		statement.setInt(1, vId);
		statement.setInt(2, gId);
		return statement;
	}

	private static final String SELECT_ATTRIBUTE_VALUES_OF_VERTEX = "SELECT "
			+ COLUMN_ATTRIBUTE_ID + ", " + COLUMN_ATTRIBUTE_VALUE + " FROM "
			+ TABLE_VERTEX_ATTRIBUTE + " WHERE " + COLUMN_VERTEX_ID
			+ " = ? AND " + COLUMN_GRAPH_ID + " = ?";

	@Override
	public PreparedStatement selectAttributeValuesOfVertex(int vId, int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_ATTRIBUTE_VALUES_OF_VERTEX);
		statement.setInt(1, vId);
		statement.setInt(2, gId);
		return statement;
	}

	// --- to get an edge --------------------------------------------

	private static final String SELECT_EDGE_WITH_INCIDENCES = "SELECT "
			+ COLUMN_TYPE_ID + ", " + TABLE_EDGE + "." + COLUMN_SEQUENCE_NUMBER
			+ ", " + COLUMN_INCIDENCE_DIRECTION + ", " + COLUMN_VERTEX_ID
			+ ", " + TABLE_INCIDENCE + "." + COLUMN_SEQUENCE_NUMBER + " "
			+ "FROM " + TABLE_EDGE + " INNER JOIN " + TABLE_INCIDENCE
			+ " ON ( " + TABLE_EDGE + "." + COLUMN_EDGE_ID + " = "
			+ TABLE_INCIDENCE + "." + COLUMN_EDGE_ID + " AND " + TABLE_EDGE
			+ "." + COLUMN_GRAPH_ID + " = " + TABLE_INCIDENCE + "."
			+ COLUMN_GRAPH_ID + " ) " + "WHERE " + TABLE_EDGE + "."
			+ COLUMN_EDGE_ID + " = ? AND " + TABLE_EDGE + "." + COLUMN_GRAPH_ID
			+ " = ?";

	@Override
	public PreparedStatement selectEdgeWithIncidences(int eId, int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_EDGE_WITH_INCIDENCES);
		statement.setInt(1, eId);
		statement.setInt(2, gId);
		return statement;
	}

	private static final String SELECT_ATTRIBUTE_VALUES_OF_EDGE = "SELECT "
			+ COLUMN_ATTRIBUTE_ID + ", " + COLUMN_ATTRIBUTE_VALUE
			+ " FROM EdgeAttributeValue WHERE " + COLUMN_EDGE_ID + " = ? AND "
			+ COLUMN_GRAPH_ID + " = ?";

	@Override
	public PreparedStatement selectAttributeValuesOfEdge(int eId, int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_ATTRIBUTE_VALUES_OF_EDGE);
		statement.setInt(1, eId);
		statement.setInt(2, gId);
		return statement;
	}

	// --- to delete a graph ------------------------------------------

	// --- to delete a vertex -----------------------------------------

	private static final String SELECT_ID_OF_INCIDENT_EDGES_OF_VERTEX = "SELECT "
			+ COLUMN_EDGE_ID
			+ " FROM "
			+ TABLE_INCIDENCE
			+ " WHERE "
			+ COLUMN_VERTEX_ID + " = ? AND " + COLUMN_GRAPH_ID + " = ?";

	@Override
	public PreparedStatement selectIncidentEIdsOfVertex(int vId, int gId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(SELECT_ID_OF_INCIDENT_EDGES_OF_VERTEX);
		statement.setInt(1, vId);
		statement.setInt(2, gId);
		return statement;
	}

	// --- to delete an edge ------------------------------------------

	// --- to update a graph ------------------------------------------

	private static final String UPDATE_ATTRIBUTE_VALUE_OF_GRAPH = "UPDATE "
			+ TABLE_GRAPH_ATTRIBUTE + " SET " + COLUMN_ATTRIBUTE_VALUE
			+ " = ? WHERE " + COLUMN_GRAPH_ID + " = ? AND "
			+ COLUMN_ATTRIBUTE_ID + " = ?";

	@Override
	public PreparedStatement updateAttributeValueOfGraph(int gId,
			int attributeId, String serializedValue) throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_ATTRIBUTE_VALUE_OF_GRAPH);
		statement.setString(1, serializedValue);
		statement.setInt(2, gId);
		statement.setInt(3, attributeId);
		return statement;
	}

	// TODO Remove as graph version is only written back on db closing process.
	private static final String UPDATE_ATTRIBUTE_VALUE_OF_GRAPH_AND_GRAPH_VERSION = "UPDATE "
			+ TABLE_GRAPH_ATTRIBUTE
			+ " SET "
			+ COLUMN_ATTRIBUTE_VALUE
			+ " = ? WHERE "
			+ COLUMN_GRAPH_ID
			+ " = ? AND "
			+ COLUMN_ATTRIBUTE_ID
			+ " = ?;"
			+ "UPDATE "
			+ TABLE_GRAPH
			+ " SET "
			+ COLUMN_GRAPH_VERSION + " = ? WHERE " + COLUMN_GRAPH_ID + " = ?";

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

	private static final String UPDATE_GRAPH_UID = "UPDATE " + TABLE_GRAPH
			+ " SET " + COLUMN_GRAPH_UID + " = ? WHERE " + COLUMN_GRAPH_ID
			+ " = ?";

	@Override
	public PreparedStatement updateGraphId(int gId, String uid)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_GRAPH_UID);
		statement.setString(1, uid);
		statement.setInt(2, gId);
		return statement;
	}

	private static final String UPDATE_GRAPH_VERSION = "UPDATE " + TABLE_GRAPH
			+ " SET " + COLUMN_GRAPH_VERSION + " = ? WHERE " + COLUMN_GRAPH_ID
			+ " = ?";

	@Override
	public PreparedStatement updateGraphVersion(int gId, long version)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_GRAPH_VERSION);
		statement.setLong(1, version);
		statement.setInt(2, gId);
		return statement;
	}

	private static final String UPDATE_VERTEX_LIST_VERSION = "UPDATE "
			+ TABLE_GRAPH + " SET " + COLUMN_GRAPH_VSEQ_VERSION + " = ? WHERE "
			+ COLUMN_GRAPH_ID + " = ?";

	@Override
	public PreparedStatement updateVertexListVersionOfGraph(int gId,
			long version) throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_VERTEX_LIST_VERSION);
		statement.setLong(1, version);
		statement.setInt(2, gId);
		return statement;
	}

	private static final String UPDATE_EDGE_LIST_VERSION = "" + "UPDATE "
			+ TABLE_GRAPH + " SET " + COLUMN_GRAPH_ESEQ_VERSION + " = ? WHERE "
			+ COLUMN_GRAPH_ID + " = ?";

	@Override
	public PreparedStatement updateEdgeListVersionOfGraph(int gId, long version)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_EDGE_LIST_VERSION);
		statement.setLong(1, version);
		statement.setInt(2, gId);
		return statement;
	}

	// --- to update a vertex -----------------------------------------

	private static final String UPDATE_VERTEX_ID = "UPDATE " + TABLE_VERTEX
			+ " SET " + COLUMN_VERTEX_ID + " = ? WHERE " + COLUMN_VERTEX_ID
			+ " = ? AND " + COLUMN_GRAPH_ID + " = ?";

	@Override
	public PreparedStatement updateIdOfVertex(int oldVId, int gId, int newVId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_VERTEX_ID);
		statement.setInt(1, newVId);
		statement.setInt(2, oldVId);
		statement.setInt(3, gId);
		return statement;
	}

	private static final String UPDATE_SEQUENCE_NUMBER_OF_VERTEX = "UPDATE "
			+ TABLE_VERTEX + " SET " + COLUMN_SEQUENCE_NUMBER + " = ? WHERE "
			+ COLUMN_VERTEX_ID + " = ? AND " + COLUMN_GRAPH_ID + " = ?";

	@Override
	public PreparedStatement updateSequenceNumberInVSeqOfVertex(int vId,
			int gId, long sequenceNumberInVSeq) throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_SEQUENCE_NUMBER_OF_VERTEX);
		statement.setLong(1, sequenceNumberInVSeq);
		statement.setInt(2, vId);
		statement.setInt(3, gId);
		return statement;
	}

	private static final String UPDATE_ATTRIBUTE_VALUE_OF_VERTEX = "UPDATE "
			+ TABLE_VERTEX_ATTRIBUTE + " SET " + COLUMN_ATTRIBUTE_VALUE
			+ " = ? WHERE " + COLUMN_VERTEX_ID + " = ? AND " + COLUMN_GRAPH_ID
			+ " = ? AND " + COLUMN_ATTRIBUTE_ID + " = ?";

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

	// TODO Remove as graph version is only written back on db closing process.
	private static final String UPDATE_ATTRIBUTE_VALUE_OF_VERTEX_AND_GRAPH_VERSION = "UPDATE "
			+ TABLE_VERTEX_ATTRIBUTE
			+ " SET "
			+ COLUMN_ATTRIBUTE_VALUE
			+ " = ? WHERE "
			+ COLUMN_VERTEX_ID
			+ " = ? AND "
			+ COLUMN_GRAPH_ID
			+ " = ? AND "
			+ COLUMN_ATTRIBUTE_ID
			+ " = ?;"
			+ "UPDATE "
			+ TABLE_GRAPH
			+ " SET "
			+ COLUMN_GRAPH_VERSION
			+ " = ? WHERE "
			+ COLUMN_GRAPH_ID + " = ?";

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

	// TODO Remove as version is only needed in memory for Iterator.
	private static final String UPDATE_INCIDENCE_LIST_VERSION = "UPDATE "
			+ TABLE_VERTEX + " SET " + COLUMN_VERTEX_LAMBDA_SEQ_VERSION
			+ " = ? WHERE " + COLUMN_VERTEX_ID + " = ? AND " + COLUMN_GRAPH_ID
			+ " = ?;";

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

	private static final String UPDATE_EDGE_ID = "UPDATE " + TABLE_EDGE
			+ " SET " + COLUMN_EDGE_ID + " = ? WHERE " + COLUMN_EDGE_ID
			+ " = ? AND " + COLUMN_GRAPH_ID + " = ?";

	@Override
	public PreparedStatement updateIdOfEdge(int oldEId, int gId, int newEId)
			throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_EDGE_ID);
		statement.setInt(1, newEId);
		statement.setInt(2, oldEId);
		statement.setInt(3, gId);
		return statement;
	}

	private static final String UPDATE_SEQUENCE_NUMBER_IN_EDGE_LIST = "UPDATE "
			+ TABLE_EDGE + " SET " + COLUMN_SEQUENCE_NUMBER + " = ? WHERE "
			+ COLUMN_EDGE_ID + " = ? AND " + COLUMN_GRAPH_ID + " = ?";

	@Override
	public PreparedStatement updateSequenceNumberInESeqOfEdge(int eId, int gId,
			long SequenceNumberInESeq) throws SQLException {
		PreparedStatement statement = getPreparedStatement(UPDATE_SEQUENCE_NUMBER_IN_EDGE_LIST);
		statement.setLong(1, SequenceNumberInESeq);
		statement.setInt(2, eId);
		statement.setInt(3, gId);
		return statement;
	}

	private static final String UPDATE_ATTRIBUTE_VALUE_OF_EDGE = "UPDATE "
			+ TABLE_EDGE_ATTRIBUTE + " SET " + COLUMN_ATTRIBUTE_VALUE
			+ " = ? WHERE " + COLUMN_EDGE_ID + " = ? AND " + COLUMN_GRAPH_ID
			+ " = ? AND " + COLUMN_ATTRIBUTE_ID + " = ?";

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

	private static final String UPDATE_ATTRIBUTE_VALUE_OF_EDGE_AND_INCREMENT_GRAPH_VERSION = "UPDATE "
			+ TABLE_EDGE_ATTRIBUTE
			+ " SET "
			+ COLUMN_ATTRIBUTE_VALUE
			+ " = ? WHERE "
			+ COLUMN_EDGE_ID
			+ " = ? AND "
			+ COLUMN_GRAPH_ID
			+ " = ? AND "
			+ COLUMN_ATTRIBUTE_ID
			+ " = ?;"
			+ "UPDATE "
			+ TABLE_GRAPH
			+ " SET "
			+ COLUMN_GRAPH_VERSION
			+ " = ? WHERE "
			+ COLUMN_GRAPH_ID + " = ?;";

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

	private static final String UPDATE_INCIDENT_VERTEX = "UPDATE "
			+ TABLE_INCIDENCE + " SET " + COLUMN_VERTEX_ID + " = ? WHERE "
			+ COLUMN_EDGE_ID + " = ? AND " + COLUMN_GRAPH_ID + " = ? AND "
			+ COLUMN_INCIDENCE_DIRECTION + " = ?";

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

	private static final String UPDATE_SEQUENCE_NUMBER_IN_INCIDENCE_LIST = "UPDATE "
			+ TABLE_INCIDENCE
			+ " SET "
			+ COLUMN_SEQUENCE_NUMBER
			+ " = ? WHERE "
			+ COLUMN_EDGE_ID
			+ " = ? AND "
			+ COLUMN_GRAPH_ID
			+ " = ? AND " + COLUMN_VERTEX_ID + " = ?";

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

	private static final String SELECT_SCHEMA_DEFINITION = "SELECT "
			+ COLUMN_SCHEMA_TG + " FROM " + TABLE_SCHEMA + " WHERE "
			+ COLUMN_SCHEMA_PACKAGE_PREFIX + " = ? AND " + COLUMN_SCHEMA_NAME
			+ " = ?;";

	@Override
	public PreparedStatement selectSchemaDefinition(String packagePrefix,
			String schemaName) throws SQLException {
		PreparedStatement statement = connection
				.prepareStatement(SELECT_SCHEMA_DEFINITION);
		statement.setString(1, packagePrefix);
		statement.setString(2, schemaName);
		return statement;
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

	private static final String SELECT_ID_OF_GRAPHS = "SELECT "
			+ COLUMN_GRAPH_UID + " FROM " + TABLE_GRAPH + "";

	@Override
	public PreparedStatement selectIdOfGraphs() throws SQLException {
		return getPreparedStatement(SELECT_ID_OF_GRAPHS);
	}

	private static final String CLEAR_TABLE_ATTRIBUTE = "TRUNCATE TABLE "
			+ GraphDatabase.TABLE_ATTRIBUTE;

	public PreparedStatement clearTableAttribute() throws SQLException {
		return getPreparedStatement(CLEAR_TABLE_ATTRIBUTE);
	}

	private static final String CLEAR_TABLE_EDGE_ATTRIBUTE_VALUE = "TRUNCATE TABLE "
			+ GraphDatabase.TABLE_EDGE_ATTRIBUTE;

	public PreparedStatement clearTableEdgeAttributeValue() throws SQLException {
		return getPreparedStatement(CLEAR_TABLE_EDGE_ATTRIBUTE_VALUE);
	}

	private static final String CLEAR_TABLE_EDGE = "TRUNCATE TABLE "
			+ GraphDatabase.TABLE_EDGE;

	public PreparedStatement clearTableEdge() throws SQLException {
		return getPreparedStatement(CLEAR_TABLE_EDGE);
	}

	private static final String CLEAR_TABLE_GRAPH_ATTRIBUTE_VALUE = "TRUNCATE TABLE "
			+ GraphDatabase.TABLE_GRAPH_ATTRIBUTE;

	public PreparedStatement clearTableGraphAttributeValue()
			throws SQLException {
		return getPreparedStatement(CLEAR_TABLE_GRAPH_ATTRIBUTE_VALUE);
	}

	private static final String CLEAR_TABLE_GRAPH_SCHEMA = "TRUNCATE TABLE "
			+ GraphDatabase.TABLE_SCHEMA;

	public PreparedStatement clearTableGraphSchema() throws SQLException {
		return getPreparedStatement(CLEAR_TABLE_GRAPH_SCHEMA);
	}

	private static final String CLEAR_TABLE_GRAPH = "TRUNCATE TABLE "
			+ GraphDatabase.TABLE_GRAPH;

	public PreparedStatement clearTableGraph() throws SQLException {
		return getPreparedStatement(CLEAR_TABLE_GRAPH);
	}

	private static final String CLEAR_TABLE_INCIDENCE = "TRUNCATE TABLE "
			+ GraphDatabase.TABLE_INCIDENCE;

	public PreparedStatement clearTableIncidence() throws SQLException {
		return getPreparedStatement(CLEAR_TABLE_INCIDENCE);
	}

	private static final String CLEAR_TABLE_TYPE = "TRUNCATE TABLE "
			+ GraphDatabase.TABLE_TYPE;

	public PreparedStatement clearTableType() throws SQLException {
		return getPreparedStatement(CLEAR_TABLE_TYPE);
	}

	private static final String CLEAR_TABLE_ATTRIBUTE_VALUE = "TRUNCATE TABLE "
			+ GraphDatabase.TABLE_VERTEX_ATTRIBUTE;

	public PreparedStatement clearTableAttributeValue() throws SQLException {
		return getPreparedStatement(CLEAR_TABLE_ATTRIBUTE_VALUE);
	}

	private static final String CLEAR_TABLE_VERTEX = "TRUNCATE TABLE "
			+ GraphDatabase.TABLE_VERTEX + ";";

	public PreparedStatement clearTableVertex() throws SQLException {
		return getPreparedStatement(CLEAR_TABLE_VERTEX);
	}

	@Override
	public PreparedStatement clearAllTables() throws SQLException {
		throw new UnsupportedOperationException(
				"Does not work in MySQL implementation.");
	}

}
