package de.uni_koblenz.jgralab.impl.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

// TODO Check what is faster: multidimensional index or several single indices on fks
// TODO Test with nvarchar instead of text
// TODO index on FK is good practice
public class PostgreSqlDb extends GraphDatabase {

	/**
	 * Creates a new <code>PostgreSqlDb</code>.
	 * 
	 * @param url
	 *            Url to connect to database.
	 * @throws GraphDatabaseException
	 *             TODO
	 */
	protected PostgreSqlDb(String url) throws GraphDatabaseException {
		super(url);
	}

	@Override
	protected void connect() throws GraphDatabaseException {
		connection = getConnectionWithJdbcDriver("org.postgresql.Driver");
		sqlStatementList = new PostgreSqlStatementList(this);
	}

	@Override
	protected void applyVendorSpecificDbSchema() throws GraphDatabaseException,
			SQLException {
		addPrimaryKeyConstraints();
		// this.addForeignKeyConstraints();
		addIndices();
		addStoredProcedures();
	}

	@Override
	protected void changeFromBulkImportToGraphTraversal() throws SQLException {
		super.addPrimaryKeyConstraints();
		super.addForeignKeyConstraints();
		addIndices();
		cluster();
	}

	@Override
	protected void changeFromGraphCreationToGraphTraversal()
			throws SQLException {
		// addPrimaryKeyConstraints
		// addForeignKeyConstraintsAndIndicesOnThem
		cluster();
	}

	private void cluster() throws SQLException {
		PreparedStatement statement = ((PostgreSqlStatementList) sqlStatementList)
				.clusterIncidenceTable();
		statement.execute();
	}

	@Override
	protected void changeFromGraphTraversalToBulkImport() throws SQLException {
		dropIndices();
		dropForeignKeyConstraints();
		dropPrimaryKeyConstraints();
	}

	@Override
	protected void changeFromGraphCreationToBulkImport() throws SQLException {
		dropIndices();
		dropForeignKeyConstraints();
		dropPrimaryKeyConstraints();
	}

	@Override
	protected void changeFromGraphTraversalToGraphCreation()
			throws SQLException {
		// TODO drop FKs
	}

	@Override
	protected void changeFromBulkImportToGraphCreation() throws SQLException {
		addPrimaryKeyConstraints();
		// this.addForeignKeyConstraints();
		addIndices();
	}
}
