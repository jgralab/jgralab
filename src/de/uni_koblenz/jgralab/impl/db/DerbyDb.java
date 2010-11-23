package de.uni_koblenz.jgralab.impl.db;

import java.sql.SQLException;

public class DerbyDb extends GraphDatabase {

	protected DerbyDb(String url) throws GraphDatabaseException{
		super(url);
	}
	
	@Override
	protected void connect() throws GraphDatabaseException {
		this.connection = this.getConnectionWithJdbcDriver("org.apache.derby.jdbc.ClientDriver"); 
		this.sqlStatementList = new DerbyStatementList(this);
	}
	
	@Override
	protected void setOptimalAutoCommitMode() throws GraphDatabaseException{
		this.setAutocommitMode(false);
	}
	
	@Override
	protected void applyVendorSpecificDbSchema() throws GraphDatabaseException, SQLException{
		this.addPrimaryKeyConstraints();
		//this.addForeignKeyConstraints();
		this.addIndices();
		//this.addStoredProcedures();		
	}
	
	@Override
	protected void changeFromBulkImportToGraphTraversal() throws SQLException {
		super.addPrimaryKeyConstraints();
		super.addForeignKeyConstraints();
		super.addIndices();
		// Derby does not support clustering of records, so explicit reordering of records is omitted here.
	}

	@Override
	protected void changeFromGraphCreationToGraphTraversal() throws SQLException {
		super.addForeignKeyConstraints();
	}

	@Override
	protected void changeFromGraphTraversalToBulkImport() throws SQLException {
		this.dropIndices();
		this.dropForeignKeyConstraints();
		this.dropPrimaryKeyConstraints();
	}	

	@Override
	protected void changeFromGraphCreationToBulkImport() throws SQLException {
		this.dropIndices();
		//this.dropForeignKeyConstraints();
		this.dropPrimaryKeyConstraints();
	}
	
	@Override
	protected void changeFromGraphTraversalToGraphCreation() throws SQLException{
		this.dropForeignKeyConstraints();
	}
	
	@Override
	protected void changeFromBulkImportToGraphCreation() throws SQLException{
		this.addPrimaryKeyConstraints();
		//this.addForeignKeyConstraints();
		this.addIndices();
	}	
}
