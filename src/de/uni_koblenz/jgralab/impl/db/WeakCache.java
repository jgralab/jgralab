package de.uni_koblenz.jgralab.impl.db;

import java.lang.ref.WeakReference;
import java.util.Hashtable;

import de.uni_koblenz.jgralab.Graph;

/**
 * Cache for a graph.
 * Only one graph per cache as cached data is more easy to manage this way.
 * 
 * @author ultbreit@uni-koblenz.de
 * 
 * TODO Put something into removed vertex and edges.
 * TODO Add reference quees to clear unneeded weak references.
 */
public class WeakCache implements GraphCache{
	
	/**
	 * Contains cached primary keys of attributes mapped by their name.
	 */
	private Hashtable<String, Integer> attributePrimaryKeyCache;
	
	/**
	 * Contains cached primary keys of types mapped by their name.
	 */	
	private Hashtable<String, Integer> typePrimaryKeyCache;

	/**
	 * Contains cached vertices mapped by their vId.
	 */
	private Hashtable<Integer, DatabasePersistableVertex> cachedVertices;
	
	/**
	 * Contains cached edges mapped by their eId. 
	 */
	private Hashtable<Integer, DatabasePersistableEdge> cachedEdges;
	
	/**
	 * Contains weak references to removed vertices so they are still reachable as long as they have not been
	 * destroyed by garbage collector.
	 */
	private Hashtable<Integer, WeakReference<DatabasePersistableVertex>> removedVertices;

	/**
	 * Contains weak references to removed edges so they are still reachable as long as they have not been
	 * destroyed by garbage collector.
	 */
	private Hashtable<Integer, WeakReference<DatabasePersistableEdge>> removedEdges;
	
	// TODO Remove when pk of vertex has been removed from schema.
	private Hashtable<Integer, Integer> vertexPrimaryKeyCache;
	
	// TODO Remove when pk of vertex has been removed from schema.	
	private Hashtable<Integer, Integer> edgePrimaryKeyCache;
	
	/**
	 * Creates and initializes a new <code>WeakCache</graph>.
	 */
	public WeakCache(){
		this.typePrimaryKeyCache = new Hashtable<String, Integer>();
		this.vertexPrimaryKeyCache = new Hashtable<Integer, Integer>();
		this.edgePrimaryKeyCache = new Hashtable<Integer, Integer>();
		this.cachedVertices = new Hashtable<Integer, DatabasePersistableVertex>();
		this.cachedEdges = new Hashtable<Integer, DatabasePersistableEdge>();
		this.removedVertices = new Hashtable<Integer, WeakReference<DatabasePersistableVertex>>();
		this.removedEdges = new Hashtable<Integer, WeakReference<DatabasePersistableEdge>>();
	}
	
	public boolean hasPrimaryKeyOfType(String qualifiedName){
		return this.typePrimaryKeyCache.containsKey(qualifiedName);
	}
	
	public int getPrimaryKeyOfType(String qualifiedTypeName){
		return this.typePrimaryKeyCache.get(qualifiedTypeName);
	}
	
	
	public void addPrimaryKeyOfType(String qualifiedTypeName, int primaryKey){
		this.typePrimaryKeyCache.put(qualifiedTypeName, primaryKey);
	}
	
	// TODO Remove when pk of vertex has been removed from schema.
	public int getPrimaryKeyOfVertex(int vId){
		return this.vertexPrimaryKeyCache.get(vId);
	}
	
	// TODO Remove when pk of vertex has been removed from schema.
	public void addPrimaryKeyOfVertex(int vId, int primaryKey){
		this.vertexPrimaryKeyCache.put(vId, primaryKey);
	}

	// TODO Remove when pk of edge has been removed from schema.
	public int getPrimaryKeyOfEdge(int eId){
		return this.edgePrimaryKeyCache.get(eId);
	}
	
	// TODO Remove when pk of edge has been removed from schema.
	public void addPrimaryKeyOfEdge(int eId, int primaryKey){
		this.edgePrimaryKeyCache.put(eId, primaryKey);
	}

	// TODO Remove when pk of edge has been removed from schema.
	public boolean hasPrimaryKeyOfEdge(int eId) {
		return this.edgePrimaryKeyCache.containsKey(eId);
	}

	// TODO Remove when pk of vertex has been removed from schema.
	public boolean hasPrimaryKeyOfVertex(int vId) {
		return this.vertexPrimaryKeyCache.containsKey(vId);
	}

	public boolean hasPrimaryKeyOfAttribute(String attributeName) {
		return this.attributePrimaryKeyCache.containsKey(attributeName);
	}

	public int getPrimaryKeyOfAttribute(String attributeName) {
		return this.attributePrimaryKeyCache.get(attributeName);
	}

	public void addPrimaryKeyOfAttribute(String attributeName, int primaryKeyOfAttribute){
		this.attributePrimaryKeyCache.put(attributeName, primaryKeyOfAttribute);
	}
	
	public void addVertex(DatabasePersistableVertex vertex){
		this.cachedVertices.put(vertex.getId(), vertex);
	}
	
	public boolean containsVertex(int vId){
		return this.cachedVertices.containsKey(vId) || this.isVertexWeaklyReachable(vId);
	}
	
	private boolean isVertexWeaklyReachable(int vId){
		return this.removedVertices.containsKey(vId) && ( this.removedVertices.get(vId).get() != null );
	}
	
	public DatabasePersistableVertex getVertex(int vId){
		DatabasePersistableVertex vertex = this.cachedVertices.get(vId);
		if(vertex == null)
			vertex = this.getWeakVertex(vId);
		return vertex;
	}
	
	private DatabasePersistableVertex getWeakVertex(int vId){
		if(this.removedVertices.containsKey(vId))
			return this.removedVertices.get(vId).get();
		else
			return null;
	}
	
	public void addEdge(DatabasePersistableEdge edge){
		this.cachedEdges.put(edge.getId(), edge);
	}

	public boolean containsEdge(int eId) {
		return this.cachedEdges.containsKey(eId) || this.isEdgeWeaklyReachable(eId);
	}
	
	private boolean isEdgeWeaklyReachable(int eId){
		return this.removedEdges.containsKey(eId) && ( this.removedEdges.get(eId).get() != null );
	}	
	
	public DatabasePersistableEdge getEdge(int eId){
		DatabasePersistableEdge edge = this.cachedEdges.get(eId);
		if( edge == null )
			edge = this.getWeakEdge(eId);
		return edge;
	}

	private DatabasePersistableEdge getWeakEdge(int eId) {
		if(this.removedEdges.containsKey(eId))
			return this.removedEdges.get(eId).get();
		else
			return null;
	}

	@Override
	public boolean containsVertex(Graph graph, int vId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DatabasePersistableVertex getVertex(Graph graph, int vId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeVertex(DatabasePersistableGraph graph, int vId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean containsEdge(Graph graph, int eId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DatabasePersistableEdge getEdge(Graph graph, int eId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeEdge(DatabasePersistableGraph graph, int eId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
}
