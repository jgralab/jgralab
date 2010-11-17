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
	protected void setOptimalAutoCommitMode() throws GraphDatabaseException{
		this.setAutocommitMode(true);
	}	
	
	@Override
	protected void applyVendorSpecificDbSchema() throws GraphDatabaseException, SQLException{
		this.addPrimaryKeyConstraints();
		this.addForeignKeyConstraints();
		this.addIndices();
		//this.addStoredProcedures();		
	}
	
	@Override
	protected void changeFromBulkImportToGraphTraversal() throws SQLException {
		super.addPrimaryKeyConstraints();
		super.addForeignKeyConstraints();
		super.addIndices();
		// MySql does not need an explicit call to cluster records
	}

	@Override
	protected void changeFromGraphCreationToGraphTraversal() {
		// TODO evaluate what has to be done
		// add more PKs
		// add FKs
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
		this.dropForeignKeyConstraints();
		this.dropPrimaryKeyConstraints();
	}
	
	@Override
	protected void changeFromGraphTraversalToGraphCreation() throws SQLException{
		//TODO drop FKs
	}
	
	@Override
	protected void changeFromBulkImportToGraphCreation() throws SQLException{
		this.addPrimaryKeyConstraints();
		//this.addForeignKeyConstraints();
		this.addIndices();
	}
}
