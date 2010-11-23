package de.uni_koblenz.jgralab.impl.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

// TODO Check what is faster: multidimensional index or several single indices on fks
// TODO Test with nvarchar instead of text
// TODO index on FK is good practice
public class PostgreSqlDb extends GraphDatabase{
	
	/**
	 * Creates a new <code>PostgreSqlDb</code>.
	 * @param url Url to connect to database.
	 * @throws GraphDatabaseException TODO
	 */
	protected PostgreSqlDb(String url) throws GraphDatabaseException{
		super(url);
	}
	
	@Override
	protected void connect() throws GraphDatabaseException{
		this.connection = this.getConnectionWithJdbcDriver("org.postgresql.Driver");
		this.sqlStatementList = new PostgreSqlStatementList(this);
	}
	
	@Override
	protected void setOptimalAutoCommitMode() throws GraphDatabaseException{
		this.setAutocommitMode(true);
	}
	
	@Override
	protected void applyVendorSpecificDbSchema() throws GraphDatabaseException, SQLException{
		this.addPrimaryKeyConstraints();
		//this.addForeignKeyConstraints();
		this.addIndices();
		this.addStoredProcedures();
	}

	@Override
	protected void changeFromBulkImportToGraphTraversal() throws SQLException{
		super.addPrimaryKeyConstraints();
		super.addForeignKeyConstraints();
		this.addIndices();
		this.cluster();
	}
	
	@Override
	protected void changeFromGraphCreationToGraphTraversal() throws SQLException{
		//addPrimaryKeyConstraints
		//addForeignKeyConstraintsAndIndicesOnThem
		this.cluster();
	}
	
	private void cluster() throws SQLException {
		PreparedStatement statement = ((PostgreSqlStatementList)this.sqlStatementList).clusterIncidenceTable();
		statement.execute();
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
