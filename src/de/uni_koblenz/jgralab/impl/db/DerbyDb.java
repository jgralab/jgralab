package de.uni_koblenz.jgralab.impl.db;

import java.sql.SQLException;

public class DerbyDb extends GraphDatabase {

	protected DerbyDb(String url) throws GraphDatabaseException {
		super(url);
	}

	@Override
	protected void connect() throws GraphDatabaseException {
		connection = getConnectionWithJdbcDriver("org.apache.derby.jdbc.ClientDriver");
		sqlStatementList = new DerbyStatementList(this);
	}

	@Override
	protected void applyVendorSpecificDbSchema() throws GraphDatabaseException,
			SQLException {
		addPrimaryKeyConstraints();
		// this.addForeignKeyConstraints();
		addIndices();
		// this.addStoredProcedures();
	}

	@Override
	protected void changeFromBulkImportToGraphTraversal() throws SQLException {
		super.addPrimaryKeyConstraints();
		super.addForeignKeyConstraints();
		super.addIndices();
		// Derby does not support clustering of records, so explicit reordering
		// of records is omitted here.
	}

	@Override
	protected void changeFromGraphCreationToGraphTraversal()
			throws SQLException {
		super.addForeignKeyConstraints();
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
		// this.dropForeignKeyConstraints();
		dropPrimaryKeyConstraints();
	}

	@Override
	protected void changeFromGraphTraversalToGraphCreation()
			throws SQLException {
		dropForeignKeyConstraints();
	}

	@Override
	protected void changeFromBulkImportToGraphCreation() throws SQLException {
		addPrimaryKeyConstraints();
		// this.addForeignKeyConstraints();
		addIndices();
	}
}
