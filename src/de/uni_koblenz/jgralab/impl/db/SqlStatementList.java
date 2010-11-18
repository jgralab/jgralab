package de.uni_koblenz.jgralab.impl.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.schema.Schema;

public abstract class SqlStatementList {

	/**
	 * Connection to database.
	 */
	protected Connection connection;

	/**
	 * Graph database using this factory.
	 */
	protected GraphDatabase graphDatabase;

	/**
	 * Collects all prepared statements which have been created in a sub class.
	 */
	private HashMap<String, PreparedStatement> preparedStatements = new HashMap<String, PreparedStatement>();

	/**
	 * Creates and initializes a new <code>SqlStatementList</code>. Provides
	 * default initialization for sub classes.
	 * 
	 * @param connection
	 *            Connection to graph database.
	 * @throws GraphDatabaseException
	 *             No connection given.
	 */
	protected SqlStatementList(GraphDatabase graphDatabase)	throws GraphDatabaseException {
		if (graphDatabase != null) {
			this.graphDatabase = graphDatabase;
			this.initializeWith(graphDatabase.getConnection());
		} else
			throw new GraphDatabaseException("No graph database given.");
	}

	private void initializeWith(Connection connection) throws GraphDatabaseException {
		if (connection != null)
			this.connection = connection;
		else
			throw new GraphDatabaseException("No connection to database.");

	}

	protected PreparedStatement getPreparedStatement(String sqlStatement) throws SQLException {
		if (this.preparedStatements.containsKey(sqlStatement))
			return this.preparedStatements.get(sqlStatement);
		else
			return prepareAndCacheStatement(sqlStatement);
	}

	private PreparedStatement prepareAndCacheStatement(String sqlStatement) throws SQLException {
		PreparedStatement preparedStatement = this.connection.prepareStatement(sqlStatement);
		this.preparedStatements.put(sqlStatement, preparedStatement);
		return preparedStatement;
	}

	// GraphSchema
	public abstract PreparedStatement createGraphSchemaTableWithConstraints() throws SQLException;
	
	// Type
	public abstract PreparedStatement createTypeTableWithConstraints() throws SQLException;

	// Attribute
	public abstract PreparedStatement createAttributeTableWithConstraints() throws SQLException;
	
	// Graph
	public abstract PreparedStatement createGraphTableWithConstraints() throws SQLException;
	
	// Vertex
	public abstract PreparedStatement createVertexTable() throws SQLException;
	public abstract PreparedStatement addPrimaryKeyConstraintOnVertexTable() throws SQLException;
	public abstract PreparedStatement dropPrimaryKeyConstraintFromVertexTable() throws SQLException;
	public abstract PreparedStatement addForeignKeyConstraintOnGraphColumnOfVertexTable() throws SQLException;
	public abstract PreparedStatement addForeignKeyConstraintOnTypeColumnOfVertexTable() throws SQLException;
	public abstract PreparedStatement dropForeignKeyConstraintFromGraphColumnOfVertexTable() throws SQLException;
	public abstract PreparedStatement dropForeignKeyConstraintFromTypeColumnOfVertexTable() throws SQLException;
	
	// Edge
	public abstract PreparedStatement createEdgeTable() throws SQLException;
	public abstract PreparedStatement addPrimaryKeyConstraintOnEdgeTable() throws SQLException;
	public abstract PreparedStatement dropPrimaryKeyConstraintFromEdgeTable() throws SQLException;
	public abstract PreparedStatement addForeignKeyConstraintOnGraphColumnOfEdgeTable() throws SQLException;
	public abstract PreparedStatement addForeignKeyConstraintOnTypeColumnOfEdgeTable() throws SQLException;
	public abstract PreparedStatement dropForeignKeyConstraintFromGraphColumnOfEdgeTable() throws SQLException;
	public abstract PreparedStatement dropForeignKeyConstraintFromTypeColumnOfEdgeTable() throws SQLException;
	
	// Incidence
	public abstract PreparedStatement createIncidenceTable() throws SQLException;
	public abstract PreparedStatement addPrimaryKeyConstraintOnIncidenceTable() throws SQLException;
	public abstract PreparedStatement dropPrimaryKeyConstraintFromIncidenceTable() throws SQLException;
	public abstract PreparedStatement addForeignKeyConstraintOnGraphColumnOfIncidenceTable() throws SQLException;
	public abstract PreparedStatement addForeignKeyConstraintOnEdgeColumnOfIncidenceTable()	throws SQLException;
	public abstract PreparedStatement addForeignKeyConstraintOnVertexColumnOfIncidenceTable() throws SQLException;
	public abstract PreparedStatement dropForeignKeyConstraintFromEdgeColumnOfIncidenceTable()throws SQLException;
	public abstract PreparedStatement dropForeignKeyConstraintFromGraphColumnOfIncidenceTable()throws SQLException;
	public abstract PreparedStatement dropForeignKeyConstraintFromVertexColumnOfIncidenceTable()throws SQLException;

	// GraphAttributeValue
	public abstract PreparedStatement createGraphAttributeValueTableWithConstraints() throws SQLException;

	// VertexAttributeValue
	public abstract PreparedStatement createVertexAttributeValueTable() throws SQLException;
	public abstract PreparedStatement addPrimaryKeyConstraintOnVertexAttributeValueTable() throws SQLException;
	public abstract PreparedStatement dropPrimaryKeyConstraintFromVertexAttributeValueTable() throws SQLException;
	public abstract PreparedStatement addForeignKeyConstraintOnGraphColumnOfVertexAttributeValueTable() throws SQLException;
	public abstract PreparedStatement addForeignKeyConstraintOnVertexColumnOfVertexAttributeValueTable() throws SQLException;
	public abstract PreparedStatement addForeignKeyConstraintOnAttributeColumnOfVertexAttributeValueTable()	throws SQLException;
	public abstract PreparedStatement dropForeignKeyConstraintFromGraphColumnOfVertexAttributeValueTable() throws SQLException;
	public abstract PreparedStatement dropForeignKeyConstraintFromVertexColumnOfVertexAttributeValueTable() throws SQLException;
	public abstract PreparedStatement dropForeignKeyConstraintFromAttributeColumnOfVertexAttributeValueTable() throws SQLException;
	//public abstract PreparedStatement addClusteredIndexOnVertexAttributeValues()throws SQLException;
	
	// EdgeAttributeValue
	public abstract PreparedStatement createEdgeAttributeValueTable() throws SQLException;
	public abstract PreparedStatement addPrimaryKeyConstraintOnEdgeAttributeValueTable() throws SQLException;
	public abstract PreparedStatement dropPrimaryKeyConstraintFromEdgeAttributeValueTable() throws SQLException;
	public abstract PreparedStatement addForeignKeyConstraintOnGraphColumnOfEdgeAttributeValueTable() throws SQLException;
	public abstract PreparedStatement addForeignKeyConstraintOnEdgeColumnOfEdgeAttributeValueTable() throws SQLException;
	public abstract PreparedStatement addForeignKeyConstraintOnAttributeColumnOfEdgeAttributeValueTable() throws SQLException;
	public abstract PreparedStatement dropForeignKeyConstraintFromGraphColumnOfEdgeAttributeValueTable() throws SQLException;
	public abstract PreparedStatement dropForeignKeyConstraintFromEdgeColumnOfEdgeAttributeValueTable() throws SQLException;
	public abstract PreparedStatement dropForeignKeyConstraintFromAttributeColumnOfEdgeAttributeValueTable() throws SQLException;
	//public abstract PreparedStatement addClusteredIndexOnEdgeAttributeValues() throws SQLException;
	
	// to insert schema information
	public abstract PreparedStatement insertSchema(Schema schema, String serializedDefinition) throws SQLException;
	public abstract PreparedStatement deleteSchema(String prefix, String name) throws SQLException;
	public abstract PreparedStatement insertType(String qualifiedName, int schemaId) throws SQLException;
	public abstract PreparedStatement insertAttribute(String name, int schemaId) throws SQLException;

	// to insert a graph
	public abstract PreparedStatement insertGraph(String id, long graphVersion,	long vertexListVersion, long edgeListVersion, int typeId) throws SQLException;
	public abstract PreparedStatement insertGraphAttributeValue(int gId, int attributeId, String value) throws SQLException;

	// to insert a vertex
	public abstract PreparedStatement insertVertex(int vId, int typeId,	int gId, long incidenceListVersion, long sequenceNumberInVSeq) throws SQLException;
	public abstract PreparedStatement insertVertex(DatabasePersistableVertex vertex) throws SQLException, GraphIOException;
	public abstract PreparedStatement insertVertexAttributeValue(int vId, int gId, int attributeId, String value) throws SQLException;

	// to insert an edge
	public abstract PreparedStatement insertEdge(int eId, int gId, int typeId, long sequenceNumberInLambdaSeq) throws SQLException;
	public abstract PreparedStatement insertEdge(DatabasePersistableEdge edge, DatabasePersistableVertex alpha, DatabasePersistableVertex omega) throws SQLException, GraphIOException;
	public abstract PreparedStatement insertEdgeAttributeValue(int eId,	int gId, int attributeId, String value) throws SQLException;
	
	public abstract PreparedStatement insertIncidence(int eId, int vId,	int gId, long sequenceNumberInLambdaSeq) throws SQLException;

	// to preload schema information when opening a graph
	public abstract PreparedStatement selectSchemaId(String packagePrefix, String name) throws SQLException;

	public abstract PreparedStatement selectSchemaNameForGraph(String uid) throws SQLException;
	public abstract PreparedStatement selectSchemaDefinition(String packagePrefix, String schemaName) throws SQLException;
	public abstract PreparedStatement selectSchemaDefinitionForGraph(String uid) throws SQLException;
	public abstract PreparedStatement selectTypesOfSchema(String packagePrefix,	String name) throws SQLException;
	public abstract PreparedStatement selectAttributesOfSchema(String packagePrefix, String name) throws SQLException;

	// to open a graph
	public abstract PreparedStatement selectGraph(String id) throws SQLException;
	public abstract PreparedStatement selectVerticesOfGraph(int gId) throws SQLException;
	public abstract PreparedStatement selectEdgesOfGraph(int gId) throws SQLException;
	public abstract PreparedStatement selectAttributeValuesOfGraph(int gId)	throws SQLException;
	public abstract PreparedStatement countVerticesOfGraph(int gId)	throws SQLException;
	public abstract PreparedStatement countEdgesOfGraph(int gId) throws SQLException;

	// to open a vertex
	public abstract PreparedStatement selectVertexWithIncidences(int vId, int gId) throws SQLException;
	public abstract PreparedStatement selectAttributeValuesOfVertex(int vId, int gId) throws SQLException;

	// to open an edge
	public abstract PreparedStatement selectEdgeWithIncidences(int eId, int gId) throws SQLException;
	public abstract PreparedStatement selectAttributeValuesOfEdge(int eId, int gId) throws SQLException;

	// to delete a graph
	public abstract PreparedStatement deleteGraph(int gId) throws SQLException;
	public abstract PreparedStatement deleteIncidencesOfGraph(int gId) throws SQLException;
	public abstract PreparedStatement deleteAttributeValuesOfGraph(int gId)	throws SQLException;
	public abstract PreparedStatement deleteVertexAttributeValuesOfGraph(int gId) throws SQLException;
	public abstract PreparedStatement deleteEdgeAttributeValuesOfGraph(int gId)	throws SQLException;
	public abstract PreparedStatement deleteVerticesOfGraph(int gId) throws SQLException;
	public abstract PreparedStatement deleteEdgesOfGraph(int gId) throws SQLException;

	// to delete a vertex
	public abstract PreparedStatement deleteAttributeValuesOfVertex(int vId, int gId) throws SQLException;
	public abstract PreparedStatement deleteVertex(int vId, int gId) throws SQLException;
	public abstract PreparedStatement selectIncidentEIdsOfVertex(int vId, int gId) throws SQLException;

	// to delete an edge
	public abstract PreparedStatement deleteAttributeValuesOfEdge(int eId,	int gId) throws SQLException;
	public abstract PreparedStatement deleteIncidencesOfEdge(int eId, int gId) throws SQLException;
	public abstract PreparedStatement deleteEdge(int eId, int gId) throws SQLException;

	// to update a graph
	public abstract PreparedStatement updateGraphId(int gId, String uid) throws SQLException;
	public abstract PreparedStatement updateGraphVersion(int gId, long version) throws SQLException;
	public abstract PreparedStatement updateVertexListVersionOfGraph(int gId, long version) throws SQLException;
	public abstract PreparedStatement updateEdgeListVersionOfGraph(int gId,	long version) throws SQLException;
	public abstract PreparedStatement updateAttributeValueOfGraph(int gId,	int attributeId, String serializedValue) throws SQLException;
	public abstract PreparedStatement updateAttributeValueOfGraphAndGraphVersion(int gId, int attributeId, String serializedValue, long graphVersion) throws SQLException;

	// to update a vertex
	public abstract PreparedStatement updateIdOfVertex(int oldVId, int gId,	int newVId) throws SQLException;
	public abstract PreparedStatement updateSequenceNumberInVSeqOfVertex(int vId, int gId, long sequenceNumberInVSeq) throws SQLException;
	public abstract PreparedStatement updateLambdaSeqVersionOfVertex(int vId, int gId, long lambdaSeqVersion) throws SQLException;
	public abstract PreparedStatement updateAttributeValueOfVertex(int vId,	int gId, int attributeId, String serializedValue) throws SQLException;
	public abstract PreparedStatement updateAttributeValueOfVertexAndGraphVersion(int vId, int gId, int attributeId, String serializedValue, long graphVersion) throws SQLException;

	// to update an edge
	public abstract PreparedStatement updateIdOfEdge(int oldEId, int gId, int newEId) throws SQLException;
	public abstract PreparedStatement updateIncidentVIdOfIncidence(int eId, int vId, int gId) throws SQLException;
	public abstract PreparedStatement updateSequenceNumberInLambdaSeqOfIncidence(int eId, int vId, int gId, long sequenceNumberInLambdaSeq) throws SQLException;
	public abstract PreparedStatement updateSequenceNumberInESeqOfEdge(int eId,	int gId, long SequenceNumberInESeq) throws SQLException;
	public abstract PreparedStatement updateAttributeValueOfEdge(int eId, int gId, int attributeId, String serializedValue)	throws SQLException;
	public abstract PreparedStatement updateAttributeValueOfEdgeAndGraphVersion(int eId, int gId, int attributeId, String serializedValue, long graphVersion) throws SQLException;

	// stored procedures to reorganize sequence numbers in sequences of graph
	public abstract PreparedStatement createStoredProcedureToReorganizeVertexList() throws SQLException;
	public abstract PreparedStatement createStoredProcedureToReorganizeEdgeList() throws SQLException;
	public abstract PreparedStatement createStoredProcedureToReorganizeIncidenceList() throws SQLException;
	public abstract CallableStatement createReorganizeVertexListCall(int gId, long start) throws SQLException;
	public abstract CallableStatement createReorganizeEdgeListCall(int gId, long start) throws SQLException;
	public abstract CallableStatement createReorganizeIncidenceListCall(int vId, int gId, long start) throws SQLException;

	// to increase performance
	public abstract PreparedStatement addIndexOnLambdaSeq() throws SQLException;
	public abstract PreparedStatement dropIndexOnLambdaSeq() throws SQLException;
	
	//public abstract PreparedStatement clusterIncidenceTable() throws SQLException; // TODO Must be PostgreSql specific.
	//public abstract PreparedStatement dropClusteredIndicesOnAttributeValues() throws SQLException;
	//public abstract PreparedStatement clusterAttributeValues() throws SQLException;
	//public abstract PreparedStatement createStoredProcedureToInsertVertex() throws SQLException;
	
	public abstract PreparedStatement selectIdOfGraphs() throws SQLException;
}
