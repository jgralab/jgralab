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
	protected SqlStatementList(GraphDatabase graphDatabase)
			throws GraphDatabaseException {
		if (graphDatabase != null) {
			this.graphDatabase = graphDatabase;
			this.initializeWith(graphDatabase.getConnection());
		} else
			throw new GraphDatabaseException("No graph database given.");
	}

	private void initializeWith(Connection connection)
			throws GraphDatabaseException {
		if (connection != null)
			this.connection = connection;
		else
			throw new GraphDatabaseException("No connection to database.");

	}

	protected PreparedStatement getPreparedStatement(String sqlStatement)
			throws SQLException {
		if (this.preparedStatements.containsKey(sqlStatement))
			return this.preparedStatements.get(sqlStatement);
		else
			return prepareAndCacheStatement(sqlStatement);
	}

	protected PreparedStatement prepareAndCacheStatement(String sqlStatement)
			throws SQLException {
		PreparedStatement preparedStatement = this.connection
				.prepareStatement(sqlStatement);
		this.preparedStatements.put(sqlStatement, preparedStatement);
		return preparedStatement;
	}

	// to populate db with tables
	public abstract PreparedStatement createGraphSchemaTable() throws SQLException;
	public abstract PreparedStatement createTypeTable() throws SQLException;
	public abstract PreparedStatement createGraphTable() throws SQLException;
	public abstract PreparedStatement addForeignKeyConstraintsOnGraphTable() throws SQLException;
	public abstract PreparedStatement dropForeignKeyConstraintsOnGraphTable() throws SQLException;
	
	public abstract PreparedStatement createVertexTable() throws SQLException;
	public abstract PreparedStatement addPrimaryKeyConstraintOnVertexTable() throws SQLException;
	public abstract PreparedStatement dropPrimaryKeyConstraintFromVertexTable() throws SQLException;
	public abstract PreparedStatement addForeignKeyConstraintsOnVertexTable() throws SQLException;
	public abstract PreparedStatement dropForeignKeyConstraintFromVertexTable() throws SQLException;
	
	public abstract PreparedStatement createEdgeTable() throws SQLException;
	public abstract PreparedStatement addPrimaryKeyConstraintOnEdgeTable() throws SQLException;
	public abstract PreparedStatement dropPrimaryKeyConstraintFromEdgeTable() throws SQLException;
	public abstract PreparedStatement addForeignKeyConstraintsOnEdgeTable() throws SQLException;
	public abstract PreparedStatement dropForeignKeyConstraintFromEdgeTable() throws SQLException;
	
	public abstract PreparedStatement createIncidenceTable() throws SQLException;
	public abstract PreparedStatement addPrimaryKeyConstraintOnIncidenceTable() throws SQLException;
	public abstract PreparedStatement dropPrimaryKeyConstraintFromIncidenceTable() throws SQLException;
	public abstract PreparedStatement addForeignKeyConstraintsOnIncidenceTable() throws SQLException;
	public abstract PreparedStatement dropForeignKeyConstraintsFromIncidenceTable() throws SQLException;
	
	public abstract PreparedStatement createAttributeTable() throws SQLException;
	
	public abstract PreparedStatement createGraphAttributeValueTable() throws SQLException;
	public abstract PreparedStatement addClusteredIndexOnGraphAttributeValues()throws SQLException;
	
	public abstract PreparedStatement createVertexAttributeValueTable() throws SQLException;
	public abstract PreparedStatement addPrimaryKeyConstraintOnVertexAttributeValueTable() throws SQLException;
	public abstract PreparedStatement dropPrimaryKeyConstraintFromVertexAttributeValueTable() throws SQLException;
	public abstract PreparedStatement addForeignKeyConstraintsOnVertexAttributeValueTable() throws SQLException;
	public abstract PreparedStatement dropForeignKeyConstraintsFromVertexAttributeValueTable() throws SQLException;
	public abstract PreparedStatement addClusteredIndexOnVertexAttributeValues()throws SQLException;
	
	public abstract PreparedStatement createEdgeAttributeValueTable() throws SQLException;
	public abstract PreparedStatement addPrimaryKeyConstraintOnEdgeAttributeValueTable() throws SQLException;
	public abstract PreparedStatement dropPrimaryKeyConstraintFromEdgeAttributeValueTable() throws SQLException;
	public abstract PreparedStatement addForeignKeyConstraintsOnEdgeAttributeValueTable() throws SQLException;
	public abstract PreparedStatement dropForeignKeyConstraintsFromEdgeAttributeValueTable() throws SQLException;	
	public abstract PreparedStatement addClusteredIndexOnEdgeAttributeValues()throws SQLException;
	
	// to insert schema information
	public abstract PreparedStatement insertSchema(Schema schema, String serializedDefinition) throws SQLException;

	public abstract PreparedStatement insertType(String qualifiedName,
			int schemaId) throws SQLException;

	public abstract PreparedStatement insertAttribute(String name, int schemaId)
			throws SQLException;

	// to insert a graph
	public abstract PreparedStatement insertGraph(String id, long graphVersion,
			long vertexListVersion, long edgeListVersion, int typeId)
			throws SQLException;

	public abstract PreparedStatement insertGraphAttributeValue(int gId,
			int attributeId, String value) throws SQLException;

	// to insert a vertex
	public abstract PreparedStatement insertVertex(int vId, int typeId,
			int gId, long incidenceListVersion, long sequenceNumberInVSeq)
			throws SQLException;

	public abstract PreparedStatement insertVertex(
			DatabasePersistableVertex vertex) throws SQLException,
			GraphIOException;

	public abstract PreparedStatement insertVertexAttributeValue(int vId,
			int gId, int attributeId, String value) throws SQLException;

	public abstract PreparedStatement createStoredProcedureToInsertVertex()
			throws SQLException;

	// to insert an edge
	public abstract PreparedStatement insertEdge(int eId, int gId, int typeId,
			long sequenceNumberInLambdaSeq) throws SQLException;

	public abstract PreparedStatement insertEdge(DatabasePersistableEdge edge,
			DatabasePersistableVertex alpha, DatabasePersistableVertex omega)
			throws SQLException, GraphIOException;

	public abstract PreparedStatement insertIncidence(int eId, int vId,
			int gId, long sequenceNumberInLambdaSeq) throws SQLException;

	public abstract PreparedStatement insertEdgeAttributeValue(int eId,
			int gId, int attributeId, String value) throws SQLException;

	// to preload schema information when opening a graph
	public abstract PreparedStatement selectSchemaId(String packagePrefix,
			String name) throws SQLException;

	public abstract PreparedStatement selectSchemaNameForGraph(String uid)
			throws SQLException;
	
	public abstract PreparedStatement selectSchemaDefinition(String packagePrefix, String schemaName) throws SQLException;

	public abstract PreparedStatement selectSchemaDefinitionForGraph(String uid)
			throws SQLException;

	public abstract PreparedStatement selectTypesOfSchema(String packagePrefix,
			String name) throws SQLException;

	public abstract PreparedStatement selectAttributesOfSchema(
			String packagePrefix, String name) throws SQLException;

	// to open a graph
	public abstract PreparedStatement selectGraph(String id)
			throws SQLException;

	public abstract PreparedStatement selectVerticesOfGraph(int gId)
			throws SQLException;

	public abstract PreparedStatement selectEdgesOfGraph(int gId)
			throws SQLException;

	public abstract PreparedStatement selectAttributeValuesOfGraph(int gId)
			throws SQLException;

	public abstract PreparedStatement countVerticesOfGraph(int gId)
			throws SQLException;

	public abstract PreparedStatement countEdgesOfGraph(int gId)
			throws SQLException;

	// to open a vertex
	public abstract PreparedStatement selectVertexWithIncidences(int vId,
			int gId) throws SQLException;

	public abstract PreparedStatement selectAttributeValuesOfVertex(int vId,
			int gId) throws SQLException;

	// to open an edge
	public abstract PreparedStatement selectEdgeWithIncidences(int eId, int gId)
			throws SQLException;

	public abstract PreparedStatement selectAttributeValuesOfEdge(int eId,
			int gId) throws SQLException;

	// to delete a graph
	public abstract PreparedStatement deleteIncidencesOfGraph(int gId)
			throws SQLException;

	public abstract PreparedStatement deleteAttributeValuesOfGraph(int gId)
			throws SQLException;

	public abstract PreparedStatement deleteVertexAttributeValuesOfGraph(int gId)
			throws SQLException;

	public abstract PreparedStatement deleteEdgeAttributeValuesOfGraph(int gId)
			throws SQLException;

	public abstract PreparedStatement deleteVerticesOfGraph(int gId)
			throws SQLException;

	public abstract PreparedStatement deleteEdgesOfGraph(int gId)
			throws SQLException;

	public abstract PreparedStatement deleteGraph(int gId) throws SQLException;

	// to delete a vertex
	public abstract PreparedStatement deleteAttributeValuesOfVertex(int vId,
			int gId) throws SQLException;

	public abstract PreparedStatement deleteVertex(int vId, int gId)
			throws SQLException;

	public abstract PreparedStatement selectIncidentEIdsOfVertex(int vId,
			int gId) throws SQLException;

	// to delete an edge
	public abstract PreparedStatement deleteAttributeValuesOfEdge(int eId,
			int gId) throws SQLException;

	public abstract PreparedStatement deleteIncidencesOfEdge(int eId, int gId)
			throws SQLException;

	public abstract PreparedStatement deleteEdge(int eId, int gId)
			throws SQLException;

	// to update a graph
	public abstract PreparedStatement updateGraphId(int gId, String uid)
			throws SQLException;

	public abstract PreparedStatement updateGraphVersion(int gId, long version)
			throws SQLException;

	public abstract PreparedStatement updateVertexListVersionOfGraph(int gId,
			long version) throws SQLException;

	public abstract PreparedStatement updateEdgeListVersionOfGraph(int gId,
			long version) throws SQLException;

	public abstract PreparedStatement updateAttributeValueOfGraph(int gId,
			int attributeId, String serializedValue) throws SQLException;

	public abstract PreparedStatement updateAttributeValueOfGraphAndGraphVersion(
			int gId, int attributeId, String serializedValue, long graphVersion)
			throws SQLException;

	// to update a vertex
	public abstract PreparedStatement updateIdOfVertex(int oldVId, int gId,
			int newVId) throws SQLException;

	public abstract PreparedStatement updateSequenceNumberInVSeqOfVertex(
			int vId, int gId, long sequenceNumberInVSeq) throws SQLException;

	public abstract PreparedStatement updateLambdaSeqVersionOfVertex(int vId,
			int gId, long lambdaSeqVersion) throws SQLException;

	public abstract PreparedStatement updateAttributeValueOfVertex(int vId,
			int gId, int attributeId, String serializedValue)
			throws SQLException;

	public abstract PreparedStatement updateAttributeValueOfVertexAndGraphVersion(
			int vId, int gId, int attributeId, String serializedValue,
			long graphVersion) throws SQLException;

	// to update an edge
	public abstract PreparedStatement updateIdOfEdge(int oldEId, int gId,
			int newEId) throws SQLException;

	public abstract PreparedStatement updateIncidentVIdOfIncidence(int eId,
			int vId, int gId) throws SQLException;

	public abstract PreparedStatement updateSequenceNumberInLambdaSeqOfIncidence(
			int eId, int vId, int gId, long sequenceNumberInLambdaSeq)
			throws SQLException;

	public abstract PreparedStatement updateSequenceNumberInESeqOfEdge(int eId,
			int gId, long SequenceNumberInESeq) throws SQLException;

	public abstract PreparedStatement updateAttributeValueOfEdge(int eId,
			int gId, int attributeId, String serializedValue)
			throws SQLException;

	public abstract PreparedStatement updateAttributeValueOfEdgeAndGraphVersion(
			int eId, int gId, int attributeId, String serializedValue,
			long graphVersion) throws SQLException;

	// stored procedures to reorganize sequence numbers in sequences of graph
	public abstract PreparedStatement createStoredProcedureToReorganizeVertexList()
			throws SQLException;

	public abstract PreparedStatement createStoredProcedureToReorganizeEdgeList()
			throws SQLException;

	public abstract PreparedStatement createStoredProcedureToReorganizeIncidenceList()
			throws SQLException;

	// to increase performance
	public abstract PreparedStatement addClusteredIndexOnLambdaSeq()
			throws SQLException;

	public abstract PreparedStatement dropClusteredIndicesOnAttributeValues()
			throws SQLException;

	public abstract PreparedStatement clusterIncidences() throws SQLException;

	public abstract PreparedStatement clusterAttributeValues()
			throws SQLException;

	public abstract PreparedStatement deleteSchema(String prefix, String name)
			throws SQLException;
	
	public abstract CallableStatement createReorganizeVertexListCall(int gId, long start) throws SQLException;
	public abstract CallableStatement createReorganizeEdgeListCall(int gId, long start) throws SQLException;
	public abstract CallableStatement createReorganizeIncidenceListCall(int vId, int gId, long start) throws SQLException;
	
	public abstract PreparedStatement selectIdOfGraphs() throws SQLException;
}
