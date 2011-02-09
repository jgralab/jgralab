package de.uni_koblenz.jgralab.impl.db;

import java.sql.SQLException;

public class MySqlDb extends GraphDatabase {

	protected MySqlDb(String url) throws GraphDatabaseException{
		super(url);
	}
	
	@Override
	protected void connect() throws GraphDatabaseException {
		this.connection = this.getConnectionWithJdbcDriver("com.mysql.jdbc.Driver");
		this.sqlStatementList = new MySqlStatementList(this);
	}
	
	@Override
	protected void applyVendorSpecificDbSchema() throws GraphDatabaseException, SQLException{
		super.addPrimaryKeyConstraints();
		//super.addForeignKeyConstraints();
		super.addIndices();
		super.addStoredProcedures();		
	}
	
	@Override
	protected void changeFromBulkImportToGraphTraversal() throws SQLException {
		super.addPrimaryKeyConstraints();
		super.addForeignKeyConstraints();
		super.addIndices();
		// MySql does not need an explicit call to cluster records
	}

	@Override
	protected void changeFromGraphCreationToGraphTraversal() throws SQLException {
		super.addForeignKeyConstraints();
	}

	@Override
	protected void changeFromGraphTraversalToBulkImport() throws SQLException {
		super.dropIndices();
		super.dropForeignKeyConstraints();
		super.dropPrimaryKeyConstraints();
	}	

	@Override
	protected void changeFromGraphCreationToBulkImport() throws SQLException {
		super.dropIndices();
		//this.dropForeignKeyConstraints();
		super.dropPrimaryKeyConstraints();
	}
	
	@Override
	protected void changeFromGraphTraversalToGraphCreation() throws SQLException{
		super.dropForeignKeyConstraints();
	}
	
	@Override
	protected void changeFromBulkImportToGraphCreation() throws SQLException{
		this.addPrimaryKeyConstraints();
		//this.addForeignKeyConstraints();
		this.addIndices();
	}
}
