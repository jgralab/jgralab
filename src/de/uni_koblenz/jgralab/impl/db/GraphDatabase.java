package de.uni_koblenz.jgralab.impl.db;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.GraphFactory;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * Database holding graphs which can be located on a PostgreSql, MySQL or Apache
 * Derby/JavaDB server. When a graph database has been created it will only work
 * with one DBMS. To change DBMS create a new graph database with
 * <code>openGraphDatabase(...)</code>. Keep in mind that a database is always
 * specific to it's schema.
 * 
 * @author ultbreit@uni-koblenz.de
 */
public abstract class GraphDatabase {

	public static final String GRAPH_SCHEMA_TABLE_NAME = "GraphSchema";
	public static final String GRAPH_ATTRIBUTE_VALUE_TABLE_NAME = "GraphAttributeValue";
	public static final String GRAPH_TABLE_NAME = "Graph";
	public static final String EDGE_ATTRIBUTE_VALUE_TABLE_NAME = "EdgeAttributeValue";
	public static final String VERTEX_ATTRIBUTE_VALUE_TABLE_NAME = "VertexAttributeValue";
	public static final String ATTRIBUTE_TABLE_NAME = "Attribute";
	public static final String EDGE_TABLE_NAME = "Edge";
	public static final String INCIDENCE_TABLE_NAME = "Incidence";
	public static final String TYPE_TABLE_NAME = "Type";
	public static final String VERTEX_TABLE_NAME = "Vertex";

	/**
	 * Holds graph databases which are still open.
	 */
	private static HashMap<String, GraphDatabase> openGraphDatabases = new HashMap<String, GraphDatabase>();

	/**
	 * Opens a graph database at location specified by an url with given
	 * credentials. Factory method churning open graph databases.
	 * 
	 * @param url
	 *            Url to graph database.
	 * @param userName
	 *            User name.
	 * @param password
	 *            Password.
	 * @return An open graph database.
	 * @throws Exception
	 */
	private static GraphDatabase openGraphDatabase(String url, String userName,
			String password) throws GraphDatabaseException {
		if (url != null) {
			return getGraphDatabase(url, userName, password);
		} else {
			throw new GraphDatabaseException(
					"No url given to connect to graph database.");
		}
	}

	// pattern is: jdbc:<driver>://<user>:<passwd>@<host[:port]/database....>
	private static final String URL_REGEX = "^(jdbc:)(.*)://(.*):(.*)@(.*)$";

	/**
	 * Opens a graph database at location specified by an url with given
	 * credentials. Factory method churning open graph databases. This method
	 * takes the url in ftp-like format for including the user name and the
	 * password in a single string.
	 * 
	 * @param url
	 *            URL to the graph database in the format
	 *            <code>driver://username:password@host:port/database</code>
	 * @return An open graph database.
	 * @throws GraphDatabaseException
	 *             if the URL is not properly formatted
	 */
	public static GraphDatabase openGraphDatabase(String url)
			throws GraphDatabaseException {
		Pattern p = Pattern.compile(URL_REGEX);
		Matcher m = p.matcher(url);
		if (m.matches()) {
			String realURL = m.group(1) + m.group(2) + "://" + m.group(5);
			String username = m.group(3);
			String password = m.group(4);
			return openGraphDatabase(realURL, username, password);
		} else {
			throw new GraphDatabaseException(
					"Invalid url given. Please use the format \"jdbc:<driver>://<user>:<passwd>@<host[:port]/database...\"");
		}
	}

	/**
	 * Opens a graph database. Factory method churning open graph databases.
	 * This method retrieves the connection information (url, user name and
	 * password) from a system property <code>jgralab_dbconnection</code>, which
	 * has to be set in order to use this method. The format of the url is the
	 * following: <code>driver://username:password@host:port/database</code>
	 * 
	 * @return An open graph database.
	 * @throws GraphDatabaseException
	 *             if there was no URL provided by the system property
	 *             <code>jgralab_dbconnection</code> or if the provided URL is
	 *             malformed.
	 */

	private static GraphDatabase getGraphDatabase(String url, String userName,
			String password) throws GraphDatabaseException {
		if (openGraphDatabases.containsKey(url)) {
			return openGraphDatabases.get(url);
		} else {
			return connectToGraphDatabase(url, userName, password);
		}
	}

	private static GraphDatabase connectToGraphDatabase(String url,
			String userName, String password) throws GraphDatabaseException {
		GraphDatabase graphDb = createVendorSpecificDb(url);
		graphDb.userName = userName;
		graphDb.password = password;
		graphDb.connect();
		graphDb.setAutoCommit(false);
		openGraphDatabases.put(url, graphDb);
		return graphDb;
	}

	private static GraphDatabase createVendorSpecificDb(String url)
			throws GraphDatabaseException {
		if (url.startsWith("jdbc:postgresql:")) {
			return new PostgreSqlDb(url);
		} else if (url.startsWith("jdbc:derby:")) {
			return new DerbyDb(url);
		} else if (url.startsWith("jdbc:mysql:")) {
			return new MySqlDb(url);
		} else {
			throw new GraphDatabaseException("Database vendor not supported.");
		}
	}

	/**
	 * Holds url to graph database.
	 */
	private String url;

	/**
	 * Holds user name.
	 */
	private String userName;

	/**
	 * Holds the password.
	 */
	private String password;

	/**
	 * JDBC connection to database.
	 */
	protected Connection connection;

	/**
	 * SQL statements written in database vendor specific SQL dialect.
	 */
	protected SqlStatementList sqlStatementList;

	/**
	 * Current optimization mode of database.
	 */
	protected OptimizationMode mode = OptimizationMode.GRAPH_TRAVERSAL;

	/**
	 * Collects graphs which were loaded from this graph database.
	 */
	private final Hashtable<String, GraphImpl> loadedGraphs = new Hashtable<String, GraphImpl>();

	/**
	 * Caches primary keys of types and attributes to save table joins.
	 */
	private final HashMap<Schema, PrimaryKeyCache> internalCache = new HashMap<Schema, PrimaryKeyCache>();

	/**
	 * Creates and initializes a new <code>GraphDatabase</code>.
	 * 
	 * @param url
	 *            Location of graph database.
	 * @throws GraphDatabaseException
	 *             Given url is malformed.
	 */
	protected GraphDatabase(String url) throws GraphDatabaseException {
		this.url = url;
	}

	/**
	 * Creates tables in database to persist graphs.
	 * 
	 * @throws GraphDatabaseException
	 *             Creation of tables not successful.
	 */
	// public abstract void applyDbSchema() throws GraphDatabaseException;

	/**
	 * Optimizes database for import of graphs.
	 */
	// public abstract void optimizeForBulkImport() throws
	// GraphDatabaseException;

	/**
	 * Optimizes database for traversal of graphs.
	 */
	// public abstract void optimizeForGraphTraversal() throws
	// GraphDatabaseException;

	/**
	 * Optimizes database for creation of graphs.
	 */
	// public abstract void optimizeForGraphCreation() throws
	// GraphDatabaseException;

	/**
	 * Cache for primary keys of attributes and types defined in a graph schema.
	 */
	private class PrimaryKeyCache {

		/**
		 * Maps type's id to it's name.
		 */
		private final HashMap<Integer, String> typeNameMap = new HashMap<Integer, String>();

		/**
		 * Maps type's name to it's id.
		 */
		private final HashMap<String, Integer> typeIdMap = new HashMap<String, Integer>();

		/**
		 * Maps attribute's id to it's name.
		 */
		private final HashMap<Integer, String> attributeNameMap = new HashMap<Integer, String>();

		/**
		 * Maps type's name to it's id.
		 */
		private final HashMap<String, Integer> attributeIdMap = new HashMap<String, Integer>();

		void addType(int typeId, String name) {
			this.typeNameMap.put(typeId, name);
			this.typeIdMap.put(name, typeId);
		}

		void addAttribute(int attributeId, String name) {
			this.attributeNameMap.put(attributeId, name);
			this.attributeIdMap.put(name, attributeId);
		}

		int getTypeId(String name) {
			return this.typeIdMap.get(name);
		}

		int getAttributeId(String name) {
			return this.attributeIdMap.get(name);
		}

		String getTypeName(int typeId) {
			return this.typeNameMap.get(typeId);
		}

		String getAttributeName(int attributeId) {
			return this.attributeNameMap.get(attributeId);
		}
	}

	/**
	 * Connects to graph database located at url specified in constructor.
	 * 
	 * @throws GraphDatabaseException
	 *             Database vendor not supported or identified. JDBC driver not
	 *             found. Connection attempt failed.
	 */
	protected abstract void connect() throws GraphDatabaseException;

	protected Connection getConnectionWithJdbcDriver(String jdbcDriverName)
			throws GraphDatabaseException {
		try {
			Class.forName(jdbcDriverName);
			return DriverManager.getConnection(this.url, this.userName,
					this.password);
		} catch (ClassNotFoundException exception) {
			throw new GraphDatabaseException(
					"JDBC driver to connect to database not found: "
							+ jdbcDriverName, exception);
		} catch (SQLException exception) {
			throw new GraphDatabaseException(
					"Could not connect to graph database at " + this.url,
					exception);
		}
	}

	/**
	 * Gets JDBC connection to database.
	 * 
	 * @return JDBC connection to database.
	 */
	protected Connection getConnection() {
		return this.connection;
	}

	/**
	 * Begins a database transaction.
	 * 
	 * @throws GraphDatabaseException
	 *             Transaction could not be begun.
	 */
	public void beginTransaction() throws GraphDatabaseException {
		try {
			this.connection.setAutoCommit(false);
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException("Could not begin transaction.",
					exception);
		}
	}

	/**
	 * Commits a database transaction.
	 * 
	 * @throws GraphDatabaseException
	 *             Transaction could not be committed.
	 */
	public void commitTransaction() throws GraphDatabaseException {
		try {
			this.connection.commit();
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException("Could not commit transaction.",
					exception);
		}

	}

	/**
	 * Sets auto commit mode of database.
	 * 
	 * @param autoCommit
	 *            Auto commit mode to set.
	 * @throws GraphDatabaseException
	 *             Auto commit mode could not be set.
	 */
	public void setAutoCommit(boolean autoCommitMode)
			throws GraphDatabaseException {
		try {
			this.connection.setAutoCommit(autoCommitMode);
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException("Could not set auto commit mode.",
					exception);
		}
	}

	/**
	 * Closes database, writes back graph version and commits any open
	 * transactions.
	 * 
	 * @throws GraphDatabaseException
	 *             An error occurred on close.
	 */
	public void close() throws GraphDatabaseException {
		try {
			this.writeBackVersionOfLoadedGraphs();
			this.loadedGraphs.clear();
			this.commitAnyTransactions();
			openGraphDatabases.remove(this.url);
			this.connection.close();
		} catch (SQLException exception) {
			throw new GraphDatabaseException(
					"An error occured on closing database " + this.url,
					exception);
		}
	}

	private void commitAnyTransactions() throws SQLException {
		if (!this.connection.getAutoCommit()) {
			this.connection.commit();
		}
	}

	private void writeBackVersionOfLoadedGraphs() {
		for (String id : this.loadedGraphs.keySet()) {
			GraphImpl graph = this.loadedGraphs.get(id);
			graph.writeBackVersions();
		}
	}

	/**
	 * Deletes a graph from database.
	 * 
	 * @param id
	 *            Id of graph to delete.
	 * @throws GraphDatabaseException
	 *             Deletion was not successful.
	 */
	public void deleteGraph(String id) throws GraphDatabaseException {
		DatabasePersistableGraph graph = this.getGraph(id);
		this.delete(graph);
	}

	/**
	 * Deletes a graph from database.
	 * 
	 * @param graph
	 *            Graph to delete.
	 * @throws GraphDatabaseException
	 *             Delete not successful.
	 */
	public void delete(DatabasePersistableGraph graph)
			throws GraphDatabaseException {
		try {
			this.deleteGraph(graph);
			this.loadedGraphs.remove(graph.getId());
		} catch (SQLException exception) {
			throw new GraphDatabaseException("Graph " + graph.getId()
					+ " could not be deleted from database.", exception);
		}
	}

	private void deleteGraph(DatabasePersistableGraph graph)
			throws SQLException {
		this.deleteAllAttributeValuesIn(graph);
		this.deleteAllGraphElementsIn(graph);
		this.deleteGraph(graph.getGId());
	}

	private void deleteAllAttributeValuesIn(DatabasePersistableGraph graph)
			throws SQLException {
		PreparedStatement deleteVertexAttributesStatement = this.sqlStatementList
				.deleteVertexAttributeValuesOfGraph(graph.getGId());
		deleteVertexAttributesStatement.executeUpdate();
		PreparedStatement deleteEdgeAttributesStatement = this.sqlStatementList
				.deleteEdgeAttributeValuesOfGraph(graph.getGId());
		deleteEdgeAttributesStatement.executeUpdate();
		PreparedStatement deleteGraphAttributesStatement = this.sqlStatementList
				.deleteAttributeValuesOfGraph(graph.getGId());
		deleteGraphAttributesStatement.executeUpdate();
	}

	private void deleteAllGraphElementsIn(DatabasePersistableGraph graph)
			throws SQLException {
		PreparedStatement deleteIncidencesStatement = this.sqlStatementList
				.deleteIncidencesOfGraph(graph.getGId());
		deleteIncidencesStatement.executeUpdate();
		PreparedStatement deleteEdgeStatement = this.sqlStatementList
				.deleteEdgesOfGraph(graph.getGId());
		deleteEdgeStatement.executeUpdate();
		PreparedStatement deleteVerticesStatement = this.sqlStatementList
				.deleteVerticesOfGraph(graph.getGId());
		deleteVerticesStatement.executeUpdate();
	}

	private void deleteGraph(int gId) throws SQLException {
		PreparedStatement statement = this.sqlStatementList.deleteGraph(gId);
		statement.executeUpdate();
	}

	/**
	 * Deletes a vertex from database.
	 * 
	 * @param vertex
	 *            Vertex to delete.
	 * @throws GraphDatabaseException
	 *             Delete not successful.
	 */
	public void delete(DatabasePersistableVertex vertex)
			throws GraphDatabaseException {
		try {
			this.deleteVertex(vertex);
		} catch (SQLException exception) {
			throw new GraphDatabaseException("Vertex " + vertex.getId()
					+ " in graph " + vertex.getGraph().getId()
					+ " could not be deleted from database.", exception);
		}
	}

	private void deleteVertex(DatabasePersistableVertex vertex)
			throws SQLException {
		// OPTIMIZE Can be done in one roundtrip.
		this.deleteAttributesOf(vertex);
		this.deleteIncidentEdgesOf(vertex);
		this.deleteVertex(vertex.getId(), vertex.getGId());
	}

	private void deleteAttributesOf(DatabasePersistableVertex vertex)
			throws SQLException {
		PreparedStatement deleteAttributesStatement = this.sqlStatementList
				.deleteAttributeValuesOfVertex(vertex.getId(), vertex.getGId());
		deleteAttributesStatement.executeUpdate();
	}

	private void deleteIncidentEdgesOf(DatabasePersistableVertex vertex)
			throws SQLException {
		ArrayList<Integer> edgeIds = this.getIncidentEIdsOf(vertex);
		for (Integer eId : edgeIds) {
			this.deleteIncidencesOfEdge(eId, vertex.getGId());
			this.deleteAttributeValuesOfEdge(eId, vertex.getGId());
			this.deleteEdge(eId, vertex.getGId());
		}
	}

	private ArrayList<Integer> getIncidentEIdsOf(
			DatabasePersistableVertex vertex) throws SQLException {
		PreparedStatement statement = this.sqlStatementList
				.selectIncidentEIdsOfVertex(vertex.getId(), vertex.getGId());
		ResultSet result = statement.executeQuery();
		ArrayList<Integer> edgeIds = new ArrayList<Integer>();
		while (result.next()) {
			edgeIds.add(result.getInt(1));
		}
		return edgeIds;
	}

	private void deleteIncidencesOfEdge(Integer eId, int gId)
			throws SQLException {
		PreparedStatement deleteAttributesStatement = this.sqlStatementList
				.deleteIncidencesOfEdge(eId, gId);
		deleteAttributesStatement.executeUpdate();
	}

	private void deleteVertex(int vId, int gId) throws SQLException {
		PreparedStatement deleteVertexStatement = this.sqlStatementList
				.deleteVertex(vId, gId);
		deleteVertexStatement.executeUpdate();
	}

	/**
	 * Deletes an edge from database.
	 * 
	 * @param edge
	 *            Edge to delete.
	 * @throws GraphException
	 *             Delete not successful.
	 */
	public void delete(DatabasePersistableEdge edge) throws GraphException {
		try {
			this.deleteEdge(edge);
		} catch (SQLException exception) {
			throw new GraphException(
					"Edge could not be deleted from database.", exception);
		}
	}

	private void deleteEdge(DatabasePersistableEdge edge) throws SQLException {
		this.deleteIncidencesOfEdge(edge.getId(), edge.getGId());
		this.deleteAttributeValuesOf(edge);
		this.deleteEdge(edge.getId(), edge.getGId());
	}

	private void deleteAttributeValuesOf(DatabasePersistableEdge edge)
			throws SQLException {
		this.deleteAttributeValuesOfEdge(edge.getId(), edge.getGId());
	}

	private void deleteAttributeValuesOfEdge(int eId, int gId)
			throws SQLException {
		PreparedStatement deleteAttributesStatement = this.sqlStatementList
				.deleteAttributeValuesOfEdge(eId, gId);
		deleteAttributesStatement.executeUpdate();
	}

	private void deleteEdge(int eId, int gId) throws SQLException {
		PreparedStatement deleteEdgeStatement = this.sqlStatementList
				.deleteEdge(eId, gId);
		deleteEdgeStatement.executeUpdate();
	}

	/**
	 * Inserts a new vertex without any incidences into database.
	 * 
	 * @param vertex
	 *            Vertex to insert.
	 * @throws GraphDatabaseException
	 *             Insert not successful.
	 */
	public void insert(DatabasePersistableVertex vertex)
			throws GraphDatabaseException {
		assert vertex.getIncidenceListVersion() == 0;
		try {
			if (this.sqlStatementList instanceof PostgreSqlStatementList) {
				this.reducedRoundtripInsert(vertex);
			} else {
				this.normalInsert(vertex);
			}
			vertex.setInitialized(true);
			vertex.setPersistent(true);
		} catch (Exception exception) {
			throw new GraphDatabaseException(
					"Vertex could not be inserted into database.", exception);
		}
	}

	private void normalInsert(DatabasePersistableVertex vertex)
			throws SQLException, GraphIOException {
		int typeId = this.getTypeIdOf(vertex);
		PreparedStatement insertStatement = this.sqlStatementList.insertVertex(
				vertex.getId(), typeId, vertex.getGId(), vertex
						.getIncidenceListVersion(), vertex
						.getSequenceNumberInVSeq());
		insertStatement.executeUpdate();
		SortedSet<Attribute> attributes = vertex.getAttributedElementClass()
				.getAttributeList();
		for (Attribute attribute : attributes) {
			int attributeId = this.getAttributeId(vertex.getGraph(), attribute
					.getName());
			String value = this.convertToString(vertex, attribute.getName());
			insertStatement = this.sqlStatementList.insertVertexAttributeValue(
					vertex.getId(), vertex.getGId(), attributeId, value);
			insertStatement.executeUpdate();
		}
	}

	private void reducedRoundtripInsert(DatabasePersistableVertex vertex)
			throws SQLException, GraphIOException {
		PreparedStatement insertStatement = this.sqlStatementList
				.insertVertex(vertex);
		insertStatement.executeUpdate();
	}

	/**
	 * Inserts an edge into database.
	 * 
	 * @param edge
	 *            Edge to insert.
	 * @param alpha
	 *            Start vertex of edge.
	 * @param omega
	 *            End vertex of edge.
	 * @throws GraphDatabaseException
	 *             Insert not successful.
	 */
	public void insert(DatabasePersistableEdge edge,
			DatabasePersistableVertex alpha, DatabasePersistableVertex omega)
			throws GraphDatabaseException {
		try {
			if (this.sqlStatementList instanceof PostgreSqlStatementList) {
				this.reducedRoundtripInsert(edge, alpha, omega);
			} else {
				this.normalInsert(edge, alpha, omega);
			}
			edge.setInitialized(true);
			edge.setPersistent(true);
		} catch (Exception exception) {
			throw new GraphDatabaseException("Edge " + edge.getId()
					+ " could not be inserted into database.", exception);
		}
	}

	private void normalInsert(DatabasePersistableEdge edge,
			DatabasePersistableVertex alpha, DatabasePersistableVertex omega)
			throws SQLException, GraphIOException {
		assert edge.isNormal();
		int typeId = this.getTypeIdOf(edge);
		PreparedStatement insertStatement = this.sqlStatementList.insertEdge(
				edge.getId(), edge.getGId(), typeId, edge
						.getSequenceNumberInESeq());
		insertStatement.executeUpdate();

		insertStatement = this.sqlStatementList.insertIncidence(edge.getId(),
				alpha.getId(), edge.getGId(), edge
						.getSequenceNumberInLambdaSeq());
		insertStatement.executeUpdate();

		DatabasePersistableEdge reversedEdge = (DatabasePersistableEdge) edge
				.getReversedEdge();
		insertStatement = this.sqlStatementList.insertIncidence(reversedEdge
				.getId(), omega.getId(), reversedEdge.getGId(), reversedEdge
				.getSequenceNumberInLambdaSeq());
		insertStatement.executeUpdate();

		SortedSet<Attribute> attributes = edge.getAttributedElementClass()
				.getAttributeList();
		for (Attribute attribute : attributes) {
			int attributeId = this.getAttributeId(edge.getGraph(), attribute
					.getName());
			String value = this.convertToString(edge, attribute.getName());
			insertStatement = this.sqlStatementList.insertEdgeAttributeValue(
					edge.getId(), edge.getGId(), attributeId, value);
			insertStatement.executeUpdate();
		}
	}

	private void reducedRoundtripInsert(DatabasePersistableEdge edge,
			DatabasePersistableVertex alpha, DatabasePersistableVertex omega)
			throws SQLException, GraphIOException {
		assert edge.isNormal();
		PreparedStatement insertStatement = this.sqlStatementList.insertEdge(
				edge, alpha, omega);
		insertStatement.executeUpdate();
	}

	/**
	 * Gets type id of an edge.
	 * 
	 * @param edge
	 *            Edge to get it's type id.
	 * @return Type id of edge.
	 */
	protected int getTypeIdOf(DatabasePersistableEdge edge) {
		String edgeTypeName = edge.getAttributedElementClass()
				.getQualifiedName();
		return getTypeIdOfGraphElement(edge.getGraph(), edgeTypeName);
	}

	/**
	 * Updates version of a graph.
	 * 
	 * @param graph
	 *            Graph to update it's version.
	 * @throws GraphDatabaseException
	 *             Update was not successful.
	 */
	public void updateVersionOf(DatabasePersistableGraph graph)
			throws GraphDatabaseException {
		try {
			PreparedStatement statement = this.sqlStatementList
					.updateGraphVersion(graph.getGId(), graph.getGraphVersion());
			statement.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException(
					"Could not update version of graph " + graph.getId(),
					exception);
		}
	}

	/**
	 * Updates vertex list version of a graph.
	 * 
	 * @param graph
	 *            Graph to update it's vertex list version.
	 * @throws GraphDatabaseException
	 *             Update was not successful.
	 */
	public void updateVertexListVersionOf(DatabasePersistableGraph graph)
			throws GraphDatabaseException {
		try {
			PreparedStatement statement = this.sqlStatementList
					.updateVertexListVersionOfGraph(graph.getGId(), graph
							.getVertexListVersion());
			statement.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException(
					"Could not update vertex list version of graph "
							+ graph.getId(), exception);
		}
	}

	/**
	 * Updates edge list version of a graph.
	 * 
	 * @param graph
	 *            Graph to update it's edge list version.
	 * @throws GraphDatabaseException
	 *             Update was not successful.
	 */
	public void updateEdgeListVersionOf(DatabasePersistableGraph graph)
			throws GraphDatabaseException {
		try {
			PreparedStatement statement = this.sqlStatementList
					.updateEdgeListVersionOfGraph(graph.getGId(), graph
							.getEdgeListVersion());
			statement.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException(
					"Could not update edge list version of graph "
							+ graph.getId(), exception);
		}
	}

	/**
	 * Updates attribute value of a graph.
	 * 
	 * @param graph
	 *            Graph with attribute value to update.
	 * @param attributeName
	 *            Name of attribute.
	 * @throws GraphDatabaseException
	 *             Update not successful.
	 */
	public void updateAttributeValueOf(DatabasePersistableGraph graph,
			String attributeName) throws GraphDatabaseException {
		int attributeId = this.getAttributeId(graph, attributeName);
		try {
			String value = this.convertToString(graph, attributeName);
			PreparedStatement statement = this.sqlStatementList
					.updateAttributeValueOfGraph(graph.getGId(), attributeId,
							value);
			statement.executeUpdate();
		} catch (Exception exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException("Could not update attribute "
					+ attributeName + " of graph " + graph.getId(), exception);
		}
	}

	/**
	 * Updates attribute value of a vertex.
	 * 
	 * @param vertex
	 *            Vertex with attribute value to update.
	 * @param attributeName
	 *            Name of attribute
	 * @throws GraphDatabaseException
	 *             Update not successful.
	 */
	public void updateAttributeValueOf(DatabasePersistableVertex vertex,
			String attributeName) throws GraphDatabaseException {
		try {
			int attributeId = this.getAttributeId(vertex.getGraph(),
					attributeName);
			String value = this.convertToString(vertex, attributeName);
			PreparedStatement statement = this.sqlStatementList
					.updateAttributeValueOfVertex(vertex.getId(), vertex
							.getGId(), attributeId, value);
			statement.executeUpdate();
		} catch (Exception exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException(
					"Could not update value of attribute " + attributeName
							+ " of vertex " + vertex.getId(), exception);
		}
	}

	/**
	 * Updates attribute value of an edge.
	 * 
	 * @param vertex
	 *            Edge with attribute value to update.
	 * @param attributeName
	 *            Name of attribute
	 * @throws GraphDatabaseException
	 *             Update not successful.
	 */
	public void updateAttributeValueOf(DatabasePersistableEdge edge,
			String attributeName) throws GraphDatabaseException {
		try {
			String value = this.convertToString(edge, attributeName);
			int attributeId = this.getAttributeId(edge.getGraph(),
					attributeName);
			PreparedStatement statement = this.sqlStatementList
					.updateAttributeValueOfEdge(edge.getId(), edge.getGId(),
							attributeId, value);
			statement.executeUpdate();
		} catch (Exception exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException(
					"Could not update value of attribute " + attributeName
							+ " of edge " + edge.getId(), exception);
		}
	}

	/**
	 * Updates incidence list of a vertex.
	 * 
	 * @param vertex
	 *            Vertex with incidence list to update.
	 * @throws GraphDatabaseException
	 *             Update not successful.
	 */
	public void updateIncidenceListVersionOf(DatabasePersistableVertex vertex)
			throws GraphDatabaseException {
		try {
			PreparedStatement statement = this.sqlStatementList
					.updateLambdaSeqVersionOfVertex(vertex.getId(), vertex
							.getGId(), vertex.getIncidenceListVersion());
			statement.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException(
					"Could not update incidence list version of vertex "
							+ vertex.getId(), exception);
		}
	}

	/**
	 * Updates number mapping vertex's sequence in VSeq.
	 * 
	 * @param vertex
	 *            Vertex to update.
	 * @throws GraphDatabaseException
	 *             Update not successful.
	 */
	public void updateSequenceNumberInVSeqOf(DatabasePersistableVertex vertex)
			throws GraphDatabaseException {
		try {
			PreparedStatement statement = this.sqlStatementList
					.updateSequenceNumberInVSeqOfVertex(vertex.getId(), vertex
							.getGId(), vertex.getSequenceNumberInVSeq());
			statement.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException(
					"Could not update sequence number of vertex "
							+ vertex.getId(), exception);
		}
	}

	/**
	 * Updates id of vertex in graph to database.
	 * 
	 * @param vertex
	 *            Vertex to update it's id.
	 * @throws GraphDatabaseException
	 *             Update not successful.
	 */
	public void updateIdOf(int oldVId, DatabasePersistableVertex vertex)
			throws GraphDatabaseException {
		try {
			PreparedStatement statement = this.sqlStatementList
					.updateIdOfVertex(oldVId, vertex.getId(), vertex.getId());
			statement.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException(
					"Could not update vertex of old id " + oldVId
							+ " with new id " + vertex.getId(), exception);
		}
	}

	/**
	 * Updates id of edge in graph in database.
	 * 
	 * @param edge
	 *            Edge to update.
	 * @throws GraphDatabaseException
	 *             Update not successful.
	 */
	public void updateIdOf(DatabasePersistableEdge edge, int newEId)
			throws GraphDatabaseException {
		try {
			PreparedStatement statement = this.sqlStatementList.updateIdOfEdge(
					edge.getId(), edge.getGId(), newEId);
			statement.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException("Cannot update edge id "
					+ edge.getId() + " in graph " + edge.getGraph().getId()
					+ " to database.", exception);
		}
	}

	/**
	 * Updates id of a graph.
	 * 
	 * @param graph
	 *            Graph to update.
	 * @throws GraphDatabaseException
	 *             Update not successful.
	 */
	public void updateIdOf(DatabasePersistableGraph graph)
			throws GraphDatabaseException {
		PreparedStatement statement;
		try {
			statement = this.sqlStatementList.updateGraphId(graph.getGId(),
					graph.getId());
			statement.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException("", exception);
		}
	}

	/**
	 * Updates incident vertex of an edge.
	 * 
	 * @param edge
	 *            Edge to update.
	 * @throws GraphDatabaseException
	 *             Update not successful.
	 */
	public void updateIncidentVIdOf(DatabasePersistableIncidence incidence)
			throws GraphDatabaseException {
		PreparedStatement statement;
		try {
			statement = this.sqlStatementList.updateIncidentVIdOfIncidence(
					incidence.getIncidentEId(), incidence.getIncidentVId(),
					incidence.getGId());
			statement.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException(
					"Cannot update id of incident vertex "
							+ incidence.getIncidentVId()
							+ " with edge "
							+ incidence.getIncidentEId()
							+ " in graph "
							+ ((DatabasePersistableEdge) incidence).getGraph()
									.getId() + " to database.", exception);
		}
	}

	/**
	 * Updates number mapping edge's sequence in ESeq.
	 * 
	 * @param edge
	 *            Edge to update.
	 * @throws GraphDatabaseException
	 *             Could not update sequence number.
	 */
	public void updateSequenceNumberInESeqOf(DatabasePersistableEdge edge)
			throws GraphDatabaseException {
		PreparedStatement statement;
		try {
			statement = this.sqlStatementList
					.updateSequenceNumberInESeqOfEdge(edge.getId(), edge
							.getGId(), edge.getSequenceNumberInESeq());
			statement.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException(
					"Cannot update sequence number of edge " + edge.getId()
							+ " in graph " + edge.getGraph().getId()
							+ " to database.", exception);
		}
	}

	/**
	 * Updates number mapping edge's sequence in LambdaSeq of incident vertex.
	 * 
	 * @param edge
	 *            Edge to update.
	 * @throws GraphDatabaseException
	 *             Could not update sequence number.
	 */
	public void updateSequenceNumberInLambdaSeqOf(
			DatabasePersistableIncidence incidence)
			throws GraphDatabaseException {
		PreparedStatement statement;
		try {
			statement = this.sqlStatementList
					.updateSequenceNumberInLambdaSeqOfIncidence(Math
							.abs(incidence.getIncidentEId()), incidence
							.getIncidentVId(), incidence.getGId(), incidence
							.getSequenceNumberInLambdaSeq());
			statement.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException(
					"Cannot update sequence number of incidence with vertex "
							+ incidence.getIncidentVId() + " and edge "
							+ incidence.getIncidentEId() + " in graph "
							+ ((DatabasePersistableEdge) incidence).getId()
							+ " to database.", exception);
		}
	}

	/**
	 * Gets a vertex.
	 * 
	 * @param vId
	 *            Identifier of vertex to get.
	 * @return A vertex.
	 * @throws GraphDatabaseException
	 *             Could not get vertex.
	 */
	public DatabasePersistableVertex getVertex(int vId,
			DatabasePersistableGraph graph) throws GraphDatabaseException {
		try {
			return getVertexWithLessRoundTrips(graph, vId);
		} catch (Exception exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException("Cannot get vertex " + vId
					+ " in graph " + graph.getId() + " from database.",
					exception);
		}
	}

	private DatabasePersistableVertex getVertexWithLessRoundTrips(
			DatabasePersistableGraph graph, int vId) throws Exception {
		ResultSet vertexData = getVertexAndIncidenceData(graph.getGId(), vId);
		Class<? extends Vertex> vertexClass = getVertexClassFrom(graph,
				vertexData);
		GraphFactory graphFactory = graph.getSchema().getGraphFactory();
		DatabasePersistableVertex vertex = (DatabasePersistableVertex) graphFactory
				.createVertexWithDatabaseSupport(vertexClass, vId, graph);
		long incidenceListVersion = vertexData.getLong(2);
		vertex.setIncidenceListVersion(incidenceListVersion);
		long sequenceNumber = vertexData.getLong(3);
		vertex.setSequenceNumberInVSeq(sequenceNumber);
		if (vertexData.getString(5) != null) {
			do {
				long sequenceNumberInLambdaSeq = vertexData.getLong(4);
				EdgeDirection direction = EdgeDirection.parse(vertexData
						.getString(5));
				int eId = vertexData.getInt(6);
				if (direction == EdgeDirection.OUT) {
					vertex.addIncidence(eId, sequenceNumberInLambdaSeq);
				} else {
					vertex.addIncidence(-eId, sequenceNumberInLambdaSeq);
				}
			} while (vertexData.next());
		}
		if (vertex.getAttributedElementClass().hasAttributes()) {
			setAttributesOf(vertex);
		}
		vertex.setInitialized(true);
		return vertex;
	}

	@SuppressWarnings("unchecked")
	private Class<? extends Vertex> getVertexClassFrom(
			DatabasePersistableGraph graph, ResultSet vertexData)
			throws SQLException {
		int typeId = vertexData.getInt(1);
		String qualifiedTypeName = this.getTypeName(graph, typeId);
		Schema schema = graph.getSchema();
		AttributedElementClass aec = schema
				.getAttributedElementClass(qualifiedTypeName);
		return (Class<? extends Vertex>) aec.getM1Class();
	}

	private ResultSet getVertexAndIncidenceData(int gId, int vId)
			throws GraphDatabaseException, SQLException {
		PreparedStatement statement = this.sqlStatementList
				.selectVertexWithIncidences(vId, gId);
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.next()) {
			return resultSet;
		} else {
			throw new GraphDatabaseException("No vertex " + vId + " for graph "
					+ gId + " stored in database.");
		}
	}

	private void setAttributesOf(DatabasePersistableVertex vertex)
			throws SQLException, NoSuchFieldException {
		ResultSet attributeData = this.getAttributeDataOf(vertex);
		while (attributeData.next()) {
			int attributeId = attributeData.getInt(1);
			String attributeName = getAttributeName(vertex.getSchema(),
					attributeId);
			String serializedAttributeValue = attributeData.getString(2);
			try {
				if (vertex.getAttributedElementClass().containsAttribute(
						attributeName)) {
					vertex.readAttributeValueFromString(attributeName,
							serializedAttributeValue);
				}
			} catch (GraphIOException e) {
				e.printStackTrace();
			}
		}
	}

	private String getAttributeName(Schema schema, int attributeId) {
		PrimaryKeyCache cache = this.internalCache.get(schema);
		return cache.getAttributeName(attributeId);
	}

	private ResultSet getAttributeDataOf(DatabasePersistableVertex vertex)
			throws SQLException {
		PreparedStatement statement = this.sqlStatementList
				.selectAttributeValuesOfVertex(vertex.getId(), vertex.getGId());
		return statement.executeQuery();
	}

	/**
	 * Use it to get prev/next edge in graph as no incident vertices have to be
	 * known.
	 * 
	 * @param eId
	 *            Id of edge to get.
	 * @param graph
	 *            Graph edge belongs to.
	 * @return An edge.
	 * @throws GraphDatabaseException
	 *             Getting edge not succesful.
	 */
	public DatabasePersistableEdge getEdge(int eId,
			DatabasePersistableGraph graph) throws GraphDatabaseException {
		try {
			ResultSet edgeData = this.getEdgeAndIncidenceData(graph, eId);
			DatabasePersistableEdge edge = this.instanceEdgeFrom(graph,
					edgeData, eId);
			if (edge.getAttributedElementClass().hasAttributes()) {
				this.setAttributesOf(edge);
			}
			edge.setInitialized(true);
			return edge;
		} catch (Exception exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException("Could not get edge with id "
					+ eId + " of graph " + graph.getId(), exception);
		}
	}

	private ResultSet getEdgeAndIncidenceData(DatabasePersistableGraph graph,
			int eId) throws SQLException {
		PreparedStatement statement = this.sqlStatementList
				.selectEdgeWithIncidences(eId, graph.getGId());
		ResultSet edgeData = statement.executeQuery();
		if (edgeData.next()) {
			return edgeData;
		} else {
			throw new GraphException("No edge with id " + eId + " of graph "
					+ graph.getId() + " in database.");
		}
	}

	private DatabasePersistableEdge instanceEdgeFrom(
			DatabasePersistableGraph graph, ResultSet edgeData, int eId)
			throws Exception {
		Class<? extends Edge> edgeClass = getEdgeClassFrom(graph, edgeData
				.getInt(1));

		long sequenceNumberInESeq = edgeData.getLong(2);
		Vertex alpha = null;
		long alphaSeqNumber = 0;
		Vertex omega = null;
		long omegaSeqNumber = 0;
		do {
			EdgeDirection direction = EdgeDirection
					.parse(edgeData.getString(3));
			if (direction == EdgeDirection.OUT) {
				alpha = graph.getVertex(edgeData.getInt(4));
				alphaSeqNumber = edgeData.getLong(5);
			} else {
				omega = graph.getVertex(edgeData.getInt(4));
				omegaSeqNumber = edgeData.getLong(5);
			}
		} while (edgeData.next());
		GraphFactory graphFactory = graph.getSchema().getGraphFactory();
		DatabasePersistableEdge edge = (DatabasePersistableEdge) graphFactory
				.createEdgeWithDatabaseSupport(edgeClass, eId, graph, alpha,
						omega);
		edge.setSequenceNumberInESeq(sequenceNumberInESeq);
		((DatabasePersistableEdge) edge.getNormalEdge())
				.setSequenceNumberInLambdaSeq(alphaSeqNumber);
		((DatabasePersistableEdge) edge.getNormalEdge()).setIncidentVId(alpha
				.getId());
		((DatabasePersistableEdge) edge.getReversedEdge())
				.setSequenceNumberInLambdaSeq(omegaSeqNumber);
		((DatabasePersistableEdge) edge.getReversedEdge()).setIncidentVId(omega
				.getId());
		return edge;
	}

	@SuppressWarnings("unchecked")
	private Class<? extends Edge> getEdgeClassFrom(
			DatabasePersistableGraph graph, int typeId) throws SQLException {
		String qualifiedTypeName = this.getTypeName(graph, typeId);
		Schema schema = graph.getSchema();
		AttributedElementClass aec = schema
				.getAttributedElementClass(qualifiedTypeName);
		return (Class<? extends Edge>) aec.getM1Class();
	}

	private void setAttributesOf(DatabasePersistableEdge edge)
			throws SQLException, NoSuchFieldException {
		ResultSet attributeData = this.getAttributeDataOf(edge);
		while (attributeData.next()) {
			String attributeName = this.getAttributeName(edge.getGraph()
					.getSchema(), attributeData.getInt(1));
			String serializedAttributeValue = attributeData.getString(2);
			try {
				if (edge.getAttributedElementClass().containsAttribute(
						attributeName)) {
					edge.readAttributeValueFromString(attributeName,
							serializedAttributeValue);
				}
			} catch (GraphIOException e) {
				e.printStackTrace();
			}
		}
	}

	private ResultSet getAttributeDataOf(DatabasePersistableEdge edge)
			throws SQLException {
		PreparedStatement statement = this.sqlStatementList
				.selectAttributeValuesOfEdge(edge.getId(), edge.getGId());
		return statement.executeQuery();
	}

	/**
	 * Counts vertices of a graph in database.
	 * 
	 * @param graph
	 *            Graph to count it's vertices.
	 * @return Amount of vertices in graph.
	 * @throws GraphDatabaseException
	 *             Count not successful.
	 */
	public int countVerticesOf(DatabasePersistableGraph graph)
			throws GraphDatabaseException {
		try {
			PreparedStatement statement = this.sqlStatementList
					.countVerticesOfGraph(graph.getGId());
			ResultSet result = statement.executeQuery();
			result.next();
			return result.getInt(1);
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException(
					"Could not count vertices in graph " + graph.getId(),
					exception);
		}
	}

	/**
	 * Counts edges of a graph in database.
	 * 
	 * @param graph
	 *            Graph to count it's edges.
	 * @return Amount of edges in graph.
	 * @throws GraphDatabaseException
	 *             Count not successful.
	 */
	public int countEdgesOf(DatabasePersistableGraph graph)
			throws GraphDatabaseException {
		try {
			PreparedStatement statement = this.sqlStatementList
					.countEdgesOfGraph(graph.getGId());
			ResultSet result = statement.executeQuery();
			result.next();
			return result.getInt(1);
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException("Could not count edges in graph "
					+ graph.getId(), exception);
		}
	}

	/**
	 * Gets a graph from database.
	 * 
	 * @param id
	 *            Identifier of graph to get.
	 * @return Graph with given identifier.
	 * @throws GraphDatabaseException
	 *             Getting graph not successful.
	 */
	public DatabasePersistableGraph getGraph(String id)
			throws GraphDatabaseException {
		if (this.loadedGraphs.containsKey(id)) {
			return this.loadedGraphs.get(id);
		} else {
			return this.loadAndCacheGraph(id);
		}
	}

	private DatabasePersistableGraph loadAndCacheGraph(String id)
			throws GraphDatabaseException {
		DatabasePersistableGraph graph = this.loadGraph(id);
		this.loadedGraphs.put(id, (GraphImpl) graph);
		return graph;
	}

	private DatabasePersistableGraph loadGraph(String id)
			throws GraphDatabaseException {
		try {
			GraphDAO graphDAO = new GraphDAO(id);
			GraphImpl graph = getEmptyGraphInstance(id);
			graphDAO.restoreStateInto(graph);
			if (!haveTypesAndAttributesBeenPreloaded(graph)) {
				preloadTypesAndAttributes(graph);
			}
			return graph;
		} catch (Exception exception) {
			throw new GraphDatabaseException("Could not get graph " + id
					+ " from database.", exception);
		}
	}

	/**
	 * Access to a persisted graph state in database.
	 */
	private class GraphDAO {

		private final ResultSet result;

		GraphDAO(String id) throws SQLException {
			this.result = this.getGraphRecord(id);
		}

		private ResultSet getGraphRecord(String uid) throws SQLException {
			PreparedStatement statement = sqlStatementList.selectGraph(uid);
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				return resultSet;
			} else {
				throw new GraphException("No graph with id " + uid
						+ " in database.");
			}
		}

		private int getGId() throws SQLException {
			return result.getInt(1);
		}

		private long getGraphVersion() throws SQLException {
			return result.getLong(2);
		}

		private long getVertexListVersion() throws SQLException {
			return result.getLong(3);
		}

		private long getEdgeListVersion() throws SQLException {
			return result.getLong(4);
		}

		void restoreStateInto(GraphImpl graph) throws SQLException,
				NoSuchFieldException, GraphDatabaseException {
			graph.setLoading(true);
			graph.setPersistent(true);
			setInstanceVariables(graph);
			this.restoreVertexList(graph);
			this.restoreEdgeList(graph);
			graph.setVCount(countVerticesOf(graph));
			graph.setECount(countEdgesOf(graph));
			restoreAttributes(graph);
			graph.setLoading(false);
			graph.loadingCompleted();
		}

		private void setInstanceVariables(GraphImpl graph) throws SQLException {
			graph.setGId(this.getGId());
			graph.setGraphVersion(this.getGraphVersion());
			graph.setVertexListVersion(this.getVertexListVersion());
			graph.setEdgeListVersion(this.getEdgeListVersion());
		}

		private void restoreAttributes(DatabasePersistableGraph graph)
				throws SQLException, NoSuchFieldException {
			ResultSet graphAttributes = this.getGraphAttributes(this.getGId());
			while (graphAttributes.next()) {
				String attributeName = graphAttributes.getString(1);
				String serializedValue = graphAttributes.getString(2);
				try {
					graph.readAttributeValueFromString(attributeName,
							serializedValue);
				} catch (GraphIOException e) {
					e.printStackTrace();
				}
			}
		}

		private ResultSet getGraphAttributes(int gId) throws SQLException {
			PreparedStatement statement = sqlStatementList
					.selectAttributeValuesOfGraph(gId);
			return statement.executeQuery();
		}

		private void restoreVertexList(DatabasePersistableGraph graph)
				throws SQLException {
			PreparedStatement statement = sqlStatementList
					.selectVerticesOfGraph(graph.getGId());
			ResultSet vertexRecords = statement.executeQuery();
			while (vertexRecords.next()) {
				int vId = vertexRecords.getInt(1);
				long sequenceNumber = vertexRecords.getLong(2);
				graph.addVertex(vId, sequenceNumber);
			}
		}

		private void restoreEdgeList(DatabasePersistableGraph graph)
				throws SQLException {
			PreparedStatement statement = sqlStatementList
					.selectEdgesOfGraph(graph.getGId());
			ResultSet edgeRecords = statement.executeQuery();
			while (edgeRecords.next()) {
				int eId = edgeRecords.getInt(1);
				long sequenceNumber = edgeRecords.getLong(2);
				graph.addEdge(eId, sequenceNumber);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private GraphImpl getEmptyGraphInstance(String id) throws SQLException,
			GraphIOException {
		Schema schema = this.getSchemaForGraph(id);
		GraphFactory graphFactory = schema.getGraphFactory();
		GraphClass graphClass = schema.getGraphClass();
		try {
			return (GraphImpl) graphFactory.createGraphWithDatabaseSupport(
					(Class<? extends Graph>) graphClass.getM1Class(), this, id);
		} catch (Exception e) {
			throw new GraphIOException("Could not create an instance of "
					+ graphClass.getM1Class().getName());
		}
	}

	private Schema getSchemaForGraph(String uid) throws SQLException,
			GraphIOException {
		PreparedStatement statement = this.sqlStatementList
				.selectSchemaNameForGraph(uid);
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.next()) {
			return createSchema(resultSet.getString(1), resultSet.getString(2));
		} else {
			throw new GraphIOException("No schema for graph in database.");
		}
	}

	@SuppressWarnings("unchecked")
	private Schema createSchema(String packagePrefix, String name)
			throws GraphIOException {
		try {
			Class<Schema> schemaClass = (Class<Schema>) Class
					.forName(packagePrefix + "." + name);
			Class<?>[] params = {};
			Method instanceMethod = schemaClass.getMethod("instance", params);
			Object[] args = {};
			return (Schema) instanceMethod.invoke(this, args);
		} catch (Exception exception) {
			throw new GraphIOException("Could not create schema "
					+ packagePrefix + "." + name, exception);
		}
	}

	/**
	 * Checks if database contains a graph.
	 * 
	 * @param id
	 *            Id of graph to check for.
	 * @throws GraphDatabaseException
	 *             Check could not be performed.
	 */
	public boolean containsGraph(String id) throws GraphDatabaseException {
		try {
			PreparedStatement statement = this.sqlStatementList.selectGraph(id);
			ResultSet result = statement.executeQuery();
			return result.next();
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException(
					"Could not determine if database contains graph " + id,
					exception);
		}
	}

	/**
	 * Inserts a freshly created graph that has no vertices and edges into
	 * database.
	 * 
	 * Precondition: A suiting schema exists in database.
	 * 
	 * Postcondition: State of graph has been persisted to database.
	 * 
	 * @throws GraphDatabaseException
	 */
	public void insert(GraphImpl graph) throws GraphDatabaseException {
		if (this.contains(graph.getSchema())) {
			this.insertAndCacheGraph(graph);
		} else {
			throw new GraphException(
					"No schema stored in database for graph. First persist one with GraphIO.loadSchemaIntoGraphDatabase(...)");
		}
	}

	/**
	 * Checks if database contains schema.
	 * 
	 * @param schema
	 *            Schema to check for.
	 * @return true if graph contains schema, otherwise false.
	 * @throws GraphDatabaseException
	 *             Check not successful.
	 */
	public boolean contains(Schema schema) throws GraphDatabaseException {
		return this.containsSchema(schema.getPackagePrefix(), schema.getName());
	}

	/**
	 * Checks if database contains schema.
	 * 
	 * @param packagePrefix
	 *            Package prefix of schema.
	 * @param name
	 *            Name of schema.
	 * @return true if graph contains schema, otherwise false.
	 * @throws GraphDatabaseException
	 *             Check not successful.
	 */
	public boolean containsSchema(String packagePrefix, String name)
			throws GraphDatabaseException {
		try {
			PreparedStatement statement = this.sqlStatementList.selectSchemaId(
					packagePrefix, name);
			ResultSet result = statement.executeQuery();
			return result.next();
		} catch (SQLException exception) {
			throw new GraphDatabaseException(
					"Could not determine if database contains schema "
							+ packagePrefix + "." + name, exception);
		}
	}

	private void insertAndCacheGraph(GraphImpl graph)
			throws GraphDatabaseException {
		try {
			this.insertGraph(graph);
			this.cacheGraph(graph);
		} catch (Exception exception) {
			throw new GraphDatabaseException("Graph " + graph.getId()
					+ " could not be inserted into database.", exception);
		}
	}

	private void insertGraph(DatabasePersistableGraph graph)
			throws SQLException, GraphIOException {
		if (!this.haveTypesAndAttributesBeenPreloaded(graph)) {
			preloadTypesAndAttributes(graph);
		}
		this.getTypeIdAndInsertGraph(graph);
		this.insertAttributeValuesOf(graph);
		graph.setPersistent(true);
		graph.setInitialized(true);
	}

	private void cacheGraph(GraphImpl graph) {
		this.loadedGraphs.put(graph.getId(), graph);
	}

	private boolean haveTypesAndAttributesBeenPreloaded(
			DatabasePersistableGraph graph) {
		return this.internalCache.containsKey(graph.getSchema());
	}

	private void preloadTypesAndAttributes(DatabasePersistableGraph graph)
			throws SQLException {
		PrimaryKeyCache preloadedTypesAndAttributes = new PrimaryKeyCache();
		preloadTypesOf(graph.getSchema(), preloadedTypesAndAttributes);
		preloadAttributesOf(graph.getSchema(), preloadedTypesAndAttributes);
		this.internalCache.put(graph.getSchema(), preloadedTypesAndAttributes);
	}

	private void preloadTypesOf(Schema schema, PrimaryKeyCache typeCollector)
			throws SQLException {
		PreparedStatement statement = this.sqlStatementList
				.selectTypesOfSchema(schema.getPackagePrefix(), schema
						.getName());
		ResultSet result = statement.executeQuery();
		while (result.next()) {
			typeCollector.addType(result.getInt(2), result.getString(1));
		}
	}

	private void preloadAttributesOf(Schema schema,
			PrimaryKeyCache attributeCollector) throws SQLException {
		PreparedStatement statement = this.sqlStatementList
				.selectAttributesOfSchema(schema.getPackagePrefix(), schema
						.getName());
		ResultSet result = statement.executeQuery();
		while (result.next()) {
			attributeCollector.addAttribute(result.getInt(2), result
					.getString(1));
		}
	}

	private void getTypeIdAndInsertGraph(DatabasePersistableGraph graph)
			throws SQLException {
		int typeId = getTypeIdOf(graph);
		int gId = insertGraphRecord(graph, typeId);
		graph.setGId(gId);
	}

	private int getTypeIdOf(Graph graph) {
		PrimaryKeyCache cache = this.internalCache.get(graph.getSchema());
		return cache.getTypeId(graph.getGraphClass().getQualifiedName());
	}

	private String getTypeName(Graph graph, int typeId) {
		PrimaryKeyCache cache = this.internalCache.get(graph.getSchema());
		return cache.getTypeName(typeId);
	}

	/**
	 * Gets type id of a vertex.
	 * 
	 * @param vertex
	 *            Vertex to get type id for.
	 * @return Type id of vertex.
	 */
	protected int getTypeIdOf(DatabasePersistableVertex vertex) {
		String vertexTypeName = vertex.getAttributedElementClass()
				.getQualifiedName();
		return this.getTypeIdOfGraphElement(vertex.getGraph(), vertexTypeName);
	}

	private int getTypeIdOfGraphElement(Graph graph, String name) {
		PrimaryKeyCache cache = this.internalCache.get(graph.getSchema());
		return cache.getTypeId(name);
	}

	private void insertAttributeValuesOf(DatabasePersistableGraph graph)
			throws SQLException, GraphIOException {
		SortedSet<Attribute> attributes = graph.getAttributedElementClass()
				.getAttributeList();
		for (Attribute attribute : attributes) {
			this.insertAttributeValue(graph, attribute.getName());
		}
	}

	private void insertAttributeValue(DatabasePersistableGraph graph,
			String attributeName) throws SQLException, GraphIOException {
		int attributeId = this.getAttributeId(graph, attributeName);
		String value = this.convertToString(graph, attributeName);
		PreparedStatement statement = this.sqlStatementList
				.insertGraphAttributeValue(graph.getGId(), attributeId, value);
		statement.executeUpdate();
	}

	/**
	 * Gets if of a graph attribute.
	 * 
	 * @param graph
	 *            Graph to get id of attribute.
	 * @param attributeName
	 *            Name of attribute.
	 * @return id of graph attribute.
	 */
	protected int getAttributeId(Graph graph, String attributeName) {
		PrimaryKeyCache cache = this.internalCache.get(graph.getSchema());
		return cache.getAttributeId(attributeName);
	}

	/**
	 * Converts an attribute value to string.
	 * 
	 * @param attributedElement
	 *            Element with attribute to convert.
	 * @param attributeName
	 *            Name of attribute.
	 * @return Serialized value of attribute.
	 * @throws GraphDatabaseException
	 *             Conversion not successful.
	 */
	protected String convertToString(AttributedElement attributedElement,
			String attributeName) throws GraphDatabaseException {
		try {
			return attributedElement.writeAttributeValueToString(attributeName);
		} catch (Exception exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException(
					"Could not convert value of attribute " + attributeName
							+ " to string.");
		}
	}

	private int insertGraphRecord(DatabasePersistableGraph graph, int typeId)
			throws SQLException {
		PreparedStatement statement = this.sqlStatementList.insertGraph(graph
				.getId(), graph.getGraphVersion(),
				graph.getVertexListVersion(), graph.getEdgeListVersion(),
				typeId);
		statement.executeUpdate();
		return getGeneratedGId(statement);
	}

	private int getGeneratedGId(PreparedStatement statement)
			throws SQLException {
		ResultSet result = statement.getGeneratedKeys();
		if (result.next()) {
			return result.getInt(1);
		} else {
			throw new GraphException("Graph could not be stored to database.");
		}
	}

	// ----------------- INSERT SCHEMA ---------------------------

	/**
	 * Inserts a schema.
	 * 
	 * @param schema
	 *            Schema to insert.
	 * @param schemaDefinition
	 *            Definition of schema in TG notation.
	 */
	public void insertSchema(Schema schema) throws GraphDatabaseException {
		if (!this.containsSchema(schema.getPackagePrefix(), schema.getName())) {
			this.insertSchemaInTransaction(schema);
		} else {
			throw new GraphDatabaseException("A schema with name "
					+ schema.getPackagePrefix() + "." + schema.getName()
					+ " already exists in database");
		}
	}

	private void insertSchemaInTransaction(Schema schema)
			throws GraphDatabaseException {
		try {
			int schemaId = insertSchemaRecord(schema);
			this.insertDefinedTypesOf(schema, schemaId);
		} catch (Exception exception) {
			throw new GraphDatabaseException(
					"Schema could not be inserted into database.", exception);
		}
	}
	
	private String schemaToString(final Schema schema){
		String schemaDefinition = null;

		try {
			PipedOutputStream out = new PipedOutputStream();
			PipedInputStream in = new PipedInputStream(out);
			final DataOutputStream dout = new DataOutputStream(out);

			Thread graphIOThread = new Thread() {
				@Override
				public void run() {
					try {
						GraphIO.saveSchemaToStream(dout, schema);
						dout.close();
					} catch (GraphIOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};

			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			StringWriter stringWriter = new StringWriter();
			PrintWriter writer = new PrintWriter(stringWriter);

			graphIOThread.start();
			String currentLine = "";
			while (currentLine != null) {
				currentLine = reader.readLine();
				if (currentLine != null) {
					writer.println(currentLine);
				}
			}
			graphIOThread.join();
			reader.close();
			writer.close();

			schemaDefinition = stringWriter.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return schemaDefinition;
	}

	private int insertSchemaRecord(Schema schema) throws SQLException {
		PreparedStatement statement = this.sqlStatementList.insertSchema(
				schema, schemaToString(schema));
		statement.executeUpdate();
		ResultSet result = statement.getGeneratedKeys();
		if (result.next()) {
			return result.getInt(1);
		} else {
			throw new GraphException("No key generated for inserted schema.");
		}
	}

	private final HashSet<String> attributeNames = new HashSet<String>();

	private void insertDefinedTypesOf(Schema schema, int schemaId)
			throws SQLException {
		this.insertGraphClass(schema.getGraphClass(), schemaId);
		this.insertVertexClasses(schema.getVertexClassesInTopologicalOrder(),
				schemaId);
		this.insertEdgeClasses(schema.getEdgeClassesInTopologicalOrder(),
				schemaId);
		this.insertAttributes(schemaId);
	}

	private void insertGraphClass(GraphClass graphClass, int schemaId)
			throws SQLException {
		this.insertType(graphClass, schemaId);
		this.collectAttributeNamesOf(graphClass);
	}

	private void insertType(AttributedElementClass attributedElementClass,
			int schemaId) throws SQLException {
		PreparedStatement statement = this.sqlStatementList.insertType(
				attributedElementClass.getQualifiedName(), schemaId);
		statement.executeUpdate();
	}

	private void collectAttributeNamesOf(
			AttributedElementClass attributedElementClass) {
		for (Attribute attribute : attributedElementClass.getAttributeList()) {
			this.attributeNames.add(attribute.getName());
		}
	}

	private void insertAttribute(String attributeName, int schemaId)
			throws SQLException {
		PreparedStatement statement = this.sqlStatementList.insertAttribute(
				attributeName, schemaId);
		statement.executeUpdate();
	}

	private void insertVertexClasses(List<VertexClass> vertexClasses,
			int schemaId) throws SQLException {
		for (VertexClass vertexClass : vertexClasses) {
			this.insertType(vertexClass, schemaId);
			this.collectAttributeNamesOf(vertexClass);
		}
	}

	private void insertEdgeClasses(List<EdgeClass> edgeClasses, int schemaId)
			throws SQLException {
		for (EdgeClass edgeClass : edgeClasses) {
			this.insertType(edgeClass, schemaId);
			this.collectAttributeNamesOf(edgeClass);
		}
	}

	private void insertAttributes(int schemaId) throws SQLException {
		for (String attributeName : this.attributeNames) {
			this.insertAttribute(attributeName, schemaId);
		}
	}

	/**
	 * Reorganizes a vertex list.
	 * 
	 * @param graph
	 *            Graph with vertex list to reorganize.
	 * @param start
	 *            Sequence number at which reorganized list will start.
	 * @throws GraphDatabaseException
	 *             Reorganization not successful.
	 */
	public void reorganizeVertexList(GraphImpl graph, long start)
			throws GraphDatabaseException {
		try {
			CallableStatement statement = this.sqlStatementList
					.createReorganizeVertexListCall(graph.getGId(), start);
			statement.execute();
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException(
					"Could not call stored procedure to reorganize vertex list in database.",
					exception);
		}
	}

	/**
	 * Reorganizes an edge list.
	 * 
	 * @param graph
	 *            Graph with edge list to reorganize.
	 * @param start
	 *            Sequence number at which reorganized list will start.
	 * @throws GraphDatabaseException
	 * @throws GraphDatabaseException
	 *             Reorganization not successful.
	 */
	public void reorganizeEdgeList(GraphImpl graph, long start)
			throws GraphDatabaseException {
		try {
			CallableStatement statement = this.sqlStatementList
					.createReorganizeEdgeListCall(graph.getGId(), start);
			statement.execute();
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException(
					"Could not call stored procedure to reorganize edge list in database.",
					exception);
		}

	}

	/**
	 * Reorganizes a vertex list.
	 * 
	 * @param vertex
	 *            Vertex with incidence list to reorganize.
	 * @param start
	 *            Sequence number at which reorganized list will start.
	 * @throws GraphDatabaseException
	 *             Reorganization not successful.
	 */
	public void reorganizeIncidenceList(DatabasePersistableVertex vertex,
			long start) throws GraphDatabaseException {
		try {
			CallableStatement statement = this.sqlStatementList
					.createReorganizeIncidenceListCall(vertex.getId(), vertex
							.getGId(), start);
			statement.execute();
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException(
					"Could not call stored procedure to reorganize incidence list in database.",
					exception);
		}
	}

	/**
	 * Checks if connection to database is still upheld.
	 * 
	 * @return true if connection is upheld, otherwise false.
	 * @throws GraphDatabaseException
	 *             Check could not performed.
	 */
	public boolean isConnected() throws GraphDatabaseException {
		try {
			return !this.connection.isClosed();
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException(
					"Could not check if connection is still open.", exception);
		}
	}

	/**
	 * Deletes a schema from database.
	 * 
	 * @param prefix
	 *            Package prefix of schema.
	 * @param name
	 *            Name of schema to delete.
	 * @throws GraphDatabaseException
	 *             Deletion not successful.
	 */
	public void deleteSchema(String prefix, String name)
			throws GraphDatabaseException {
		try {
			// TODO
			// get graphs of this schema
			// delete graphs
			// then delete types
			// delete them from memory and from database
			// then attributes
			// delete them from memory and from database
			this.deleteSchemaRecord(prefix, name);
		} catch (SQLException exception) {
			throw new GraphDatabaseException("Graph schema " + prefix + "."
					+ name + " could not be deleted.", exception);
		}
	}

	private void deleteSchemaRecord(String prefix, String name)
			throws SQLException {
		PreparedStatement statement = this.sqlStatementList.deleteSchema(
				prefix, name);
		statement.executeUpdate();
	}

	/**
	 * Gets list of ids of contained graphs.
	 * 
	 * @return A list of ids which can be empty if no graphs have been persisted
	 *         in database.
	 * @throws GraphDatabaseException
	 *             Getting list not successful.
	 */
	public ArrayList<String> getIdsOfContainedGraphs()
			throws GraphDatabaseException {
		try {
			ArrayList<String> ids = new ArrayList<String>();
			PreparedStatement statement = this.sqlStatementList
					.selectIdOfGraphs();
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				ids.add(result.getString(1));
			}
			return ids;
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException(
					"Could not get list of graph ids.", exception);
		}
	}

	/**
	 * Gets schema definition.
	 * 
	 * @param packagePrefix
	 *            Package prefix of schema.
	 * @param schemaName
	 *            Name of schema
	 * @return Schema definition.
	 * @throws GraphDatabaseException
	 */
	public String getSchemaDefinition(String packagePrefix, String schemaName)
			throws GraphDatabaseException {
		try {
			PreparedStatement statement = this.sqlStatementList
					.selectSchemaDefinition(packagePrefix, schemaName);
			statement.executeQuery();
			ResultSet result = statement.getResultSet();
			if (result.next()) {
				return result.getString(1);
			} else {
				return null;
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException(
					"Could not get definition of schema " + packagePrefix + "."
							+ schemaName, exception);
		}
	}

	public String getUrl() {
		return this.url;
	}

	protected enum OptimizationMode {
		GRAPH_TRAVERSAL, GRAPH_CREATION, BULK_IMPORT
	}

	public void rollback() throws GraphDatabaseException {
		try {
			this.connection.rollback();
		} catch (SQLException exception) {
			exception.printStackTrace();
			throw new GraphDatabaseException(
					"Transaction could not be rolled back.", exception);
		}
	}

	protected void addPrimaryKeyConstraints() throws SQLException {
		PreparedStatement statement = this.sqlStatementList
				.addPrimaryKeyConstraintOnVertexTable();
		statement.execute();
		statement = this.sqlStatementList.addPrimaryKeyConstraintOnEdgeTable();
		statement.execute();
		statement = this.sqlStatementList
				.addPrimaryKeyConstraintOnIncidenceTable();
		statement.execute();
		statement = this.sqlStatementList
				.addPrimaryKeyConstraintOnVertexAttributeValueTable();
		statement.execute();
		statement = this.sqlStatementList
				.addPrimaryKeyConstraintOnEdgeAttributeValueTable();
		statement.execute();
	}

	protected void addForeignKeyConstraints() throws SQLException {
		PreparedStatement statement = this.sqlStatementList
				.addForeignKeyConstraintOnGraphColumnOfVertexTable();
		statement.execute();
		statement = this.sqlStatementList
				.addForeignKeyConstraintOnTypeColumnOfVertexTable();
		statement.execute();
		statement = this.sqlStatementList
				.addForeignKeyConstraintOnGraphColumnOfEdgeTable();
		statement.execute();
		statement = this.sqlStatementList
				.addForeignKeyConstraintOnTypeColumnOfEdgeTable();
		statement.execute();
		statement = this.sqlStatementList
				.addForeignKeyConstraintOnGraphColumnOfIncidenceTable();
		statement.execute();
		statement = this.sqlStatementList
				.addForeignKeyConstraintOnEdgeColumnOfIncidenceTable();
		statement.execute();
		statement = this.sqlStatementList
				.addForeignKeyConstraintOnVertexColumnOfIncidenceTable();
		statement.execute();
		statement = this.sqlStatementList
				.addForeignKeyConstraintOnGraphColumnOfVertexAttributeValueTable();
		statement.execute();
		statement = this.sqlStatementList
				.addForeignKeyConstraintOnVertexColumnOfVertexAttributeValueTable();
		statement.execute();
		statement = this.sqlStatementList
				.addForeignKeyConstraintOnAttributeColumnOfVertexAttributeValueTable();
		statement.execute();
		statement = this.sqlStatementList
				.addForeignKeyConstraintOnGraphColumnOfEdgeAttributeValueTable();
		statement.execute();
		statement = this.sqlStatementList
				.addForeignKeyConstraintOnEdgeColumnOfEdgeAttributeValueTable();
		statement.execute();
		statement = this.sqlStatementList
				.addForeignKeyConstraintOnAttributeColumnOfEdgeAttributeValueTable();
		statement.execute();
	}

	protected void addIndices() throws SQLException {
		PreparedStatement statement = this.sqlStatementList
				.addIndexOnLambdaSeq();
		statement.execute();
	}

	public void optimizeForGraphTraversal() throws GraphDatabaseException {
		if (mode != OptimizationMode.GRAPH_TRAVERSAL) {
			this.changeModeToGraphTraversal();
		}
	}

	public void optimizeForBulkImport() throws GraphDatabaseException {
		if (mode != OptimizationMode.BULK_IMPORT) {
			this.changeModeToBulkImport();
		}
	}

	public void optimizeForGraphCreation() throws GraphDatabaseException {
		if (this.mode != OptimizationMode.GRAPH_CREATION) {
			this.changeModeToGraphCreation();
		}
	}

	private void changeModeToGraphTraversal() throws GraphDatabaseException {
		try {
			this.changeModeToGraphTraversalInTransaction();
			this.mode = OptimizationMode.GRAPH_TRAVERSAL;
		} catch (SQLException exception) {
			exception.printStackTrace();
			this.rollback();
			throw new GraphDatabaseException("Could not optimize database "
					+ this.getUrl() + " for graph traversal.", exception);
		}
	}

	private void changeModeToBulkImport() throws GraphDatabaseException {
		try {
			this.changeModeToBulkImportInTransaction();
			this.mode = OptimizationMode.BULK_IMPORT;
		} catch (SQLException exception) {
			exception.printStackTrace();
			this.rollback();
			throw new GraphDatabaseException("Could not optimize database "
					+ this.getUrl() + " for bulk import.", exception);
		}
	}

	private void changeModeToGraphCreation() throws GraphDatabaseException {
		try {
			this.changeModeToGraphCreationInTransaction();
			this.mode = OptimizationMode.GRAPH_CREATION;
		} catch (SQLException exception) {
			exception.printStackTrace();
			this.rollback();
			throw new GraphDatabaseException(
					"Could not optimize database for graph creation.",
					exception);
		}
	}

	private void changeModeToGraphTraversalInTransaction()
			throws GraphDatabaseException, SQLException {
		this.beginTransaction();
		if (this.mode == OptimizationMode.BULK_IMPORT) {
			this.changeFromBulkImportToGraphTraversal();
		} else if (this.mode == OptimizationMode.GRAPH_CREATION) {
			this.changeFromGraphCreationToGraphTraversal();
		} else {
			throw new GraphDatabaseException("Undefined optimization mode.");
		}
		this.commitTransaction();
	}

	private void changeModeToBulkImportInTransaction()
			throws GraphDatabaseException, SQLException {
		this.beginTransaction();
		if (this.mode == OptimizationMode.GRAPH_CREATION) {
			this.changeFromGraphCreationToBulkImport();
		} else if (this.mode == OptimizationMode.GRAPH_TRAVERSAL) {
			this.changeFromGraphTraversalToBulkImport();
		} else {
			throw new GraphDatabaseException("Undefined optimization mode.");
		}
		this.commitTransaction();
	}

	private void changeModeToGraphCreationInTransaction()
			throws GraphDatabaseException, SQLException {
		this.beginTransaction();
		if (this.mode == OptimizationMode.GRAPH_TRAVERSAL) {
			this.changeFromGraphTraversalToGraphCreation();
		} else if (this.mode == OptimizationMode.BULK_IMPORT) {
			this.changeFromBulkImportToGraphCreation();
		} else {
			throw new GraphDatabaseException("Undefined optimization mode.");
		}
		this.commitTransaction();
	}

	protected abstract void changeFromBulkImportToGraphTraversal()
			throws SQLException;

	protected abstract void changeFromGraphCreationToGraphTraversal()
			throws SQLException;

	protected abstract void changeFromGraphCreationToBulkImport()
			throws SQLException;

	protected abstract void changeFromGraphTraversalToBulkImport()
			throws SQLException;

	protected abstract void changeFromGraphTraversalToGraphCreation()
			throws SQLException;

	protected abstract void changeFromBulkImportToGraphCreation()
			throws SQLException;

	public void applyDbSchema() throws GraphDatabaseException {
		try {
			this.applyDbSchemaInTransaction();
			this.mode = OptimizationMode.GRAPH_CREATION;
		} catch (SQLException exception) {
			exception.printStackTrace();
			this.rollback();
			throw new GraphDatabaseException(
					"Generic database schema could not be applied to database "
							+ this.getUrl(), exception);
		}
	}

	private void applyDbSchemaInTransaction() throws GraphDatabaseException,
			SQLException {
		this.beginTransaction();
		this.createTables();
		this.applyVendorSpecificDbSchema();
		this.commitTransaction();
	}

	protected void createTables() throws SQLException {
		PreparedStatement statement = this.sqlStatementList
				.createGraphSchemaTableWithConstraints();
		statement.execute();
		statement = this.sqlStatementList.createTypeTableWithConstraints();
		statement.execute();
		statement = this.sqlStatementList.createGraphTableWithConstraints();
		statement.execute();
		statement = this.sqlStatementList.createVertexTable();
		statement.execute();
		statement = this.sqlStatementList.createEdgeTable();
		statement.execute();
		statement = this.sqlStatementList.createIncidenceTable();
		statement.execute();
		statement = this.sqlStatementList.createAttributeTableWithConstraints();
		statement.execute();
		statement = this.sqlStatementList
				.createGraphAttributeValueTableWithConstraints();
		statement.execute();
		statement = this.sqlStatementList.createVertexAttributeValueTable();
		statement.execute();
		statement = this.sqlStatementList.createEdgeAttributeValueTable();
		statement.execute();
	}

	protected abstract void applyVendorSpecificDbSchema()
			throws GraphDatabaseException, SQLException;

	protected void addStoredProcedures() throws SQLException {
		this.sqlStatementList.createStoredProcedureToReorganizeEdgeList();
		this.sqlStatementList.createStoredProcedureToReorganizeVertexList();
		this.sqlStatementList.createStoredProcedureToReorganizeIncidenceList();
	}

	protected void dropPrimaryKeyConstraints() throws SQLException {
		PreparedStatement statement = this.sqlStatementList
				.dropPrimaryKeyConstraintFromVertexAttributeValueTable();
		statement.execute();
		statement = this.sqlStatementList
				.dropPrimaryKeyConstraintFromEdgeAttributeValueTable();
		statement.execute();
		statement = this.sqlStatementList
				.dropPrimaryKeyConstraintFromIncidenceTable();
		statement.execute();
		statement = this.sqlStatementList
				.dropPrimaryKeyConstraintFromEdgeTable();
		statement.execute();
		statement = this.sqlStatementList
				.dropPrimaryKeyConstraintFromVertexTable();
		statement.execute();
		statement = this.sqlStatementList
				.dropPrimaryKeyConstraintFromIncidenceTable();
		statement.execute();
	}

	protected void dropIndices() throws SQLException {
		PreparedStatement statement = this.sqlStatementList
				.dropIndexOnLambdaSeq();
		statement.execute();
	}

	public void clearAllTables() throws SQLException {
		PreparedStatement statement = this.sqlStatementList.clearAllTables();
		statement.execute();
	}

	protected void dropForeignKeyConstraints() throws SQLException {
		PreparedStatement statement = this.sqlStatementList
				.dropForeignKeyConstraintFromGraphColumnOfVertexTable();
		statement.execute();
		statement = this.sqlStatementList
				.dropForeignKeyConstraintFromTypeColumnOfVertexTable();
		statement.execute();
		statement = this.sqlStatementList
				.dropForeignKeyConstraintFromGraphColumnOfEdgeTable();
		statement.execute();
		statement = this.sqlStatementList
				.dropForeignKeyConstraintFromTypeColumnOfEdgeTable();
		statement.execute();
		statement = this.sqlStatementList
				.dropForeignKeyConstraintFromEdgeColumnOfIncidenceTable();
		statement.execute();
		statement = this.sqlStatementList
				.dropForeignKeyConstraintFromGraphColumnOfIncidenceTable();
		statement.execute();
		statement = this.sqlStatementList
				.dropForeignKeyConstraintFromVertexColumnOfIncidenceTable();
		statement.execute();
		statement = this.sqlStatementList
				.dropForeignKeyConstraintFromGraphColumnOfVertexAttributeValueTable();
		statement.execute();
		statement = this.sqlStatementList
				.dropForeignKeyConstraintFromVertexColumnOfVertexAttributeValueTable();
		statement.execute();
		statement = this.sqlStatementList
				.dropForeignKeyConstraintFromAttributeColumnOfVertexAttributeValueTable();
		statement.execute();
		statement = this.sqlStatementList
				.dropForeignKeyConstraintFromGraphColumnOfEdgeAttributeValueTable();
		statement.execute();
		statement = this.sqlStatementList
				.dropForeignKeyConstraintFromEdgeColumnOfEdgeAttributeValueTable();
		statement.execute();
		statement = this.sqlStatementList
				.dropForeignKeyConstraintFromAttributeColumnOfEdgeAttributeValueTable();
		statement.execute();
	}
}
