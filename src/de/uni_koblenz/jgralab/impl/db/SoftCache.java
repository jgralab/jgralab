package de.uni_koblenz.jgralab.impl.db;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Hashtable;

import de.uni_koblenz.jgralab.Graph;

/**
 * Global cache for graphs using weak references.
 * @author ultbreit@uni-koblenz.de
 */
public class SoftCache implements GraphCache {

	private Hashtable<Graph, Hashtable<Integer, SoftReference<DatabasePersistableVertex>>> verticesOfGraphs;

	private ReferenceQueue<DatabasePersistableVertex> vertexReferenceQueue;

	private Hashtable<Graph, Hashtable<Integer, SoftReference<DatabasePersistableEdge>>> edgesOfGraphs;

	private ReferenceQueue<DatabasePersistableEdge> edgeReferenceQueue;

	private int THRESHOLD = 10000;

	private int accessCounter = 0;

	/**
	 * Creates and initializes a new <code>WeakCache</graph>.
	 */
	public SoftCache() {
		this.verticesOfGraphs = new Hashtable<Graph, Hashtable<Integer, SoftReference<DatabasePersistableVertex>>>();
		this.vertexReferenceQueue = new ReferenceQueue<DatabasePersistableVertex>();
		this.edgesOfGraphs = new Hashtable<Graph, Hashtable<Integer, SoftReference<DatabasePersistableEdge>>>();
		this.edgeReferenceQueue = new ReferenceQueue<DatabasePersistableEdge>();
	}

	private void checkThreshold() {
		this.accessCounter++;
		if (this.accessCounter > THRESHOLD) {
			this.clearReferenceQueues();
			this.accessCounter = 0;
		}
	}

	@Override
	public void addVertex(DatabasePersistableVertex vertex) {
		checkThreshold();
		Hashtable<Integer, SoftReference<DatabasePersistableVertex>> vertices;
		if (this.verticesOfGraphs.containsKey(vertex.getGraph()))
			vertices = this.verticesOfGraphs.get(vertex.getGraph());
		else {
			vertices = new Hashtable<Integer, SoftReference<DatabasePersistableVertex>>();
			this.verticesOfGraphs.put(vertex.getGraph(), vertices);
		}
		vertices.put(vertex.getId(), new SoftReference<DatabasePersistableVertex>(vertex, this.vertexReferenceQueue));
	}

	/* (non-Javadoc)
	 * @see de.uni_koblenz.jgralab.impl.db.GraphCache#containsVertex(de.uni_koblenz.jgralab.Graph, int)
	 */
	@Override
	public boolean containsVertex(Graph graph, int vId) {
		checkThreshold();
		if (this.verticesOfGraphs.containsKey(graph)) {
			Hashtable<Integer, SoftReference<DatabasePersistableVertex>> vertices = this.verticesOfGraphs
					.get(graph);
			return vertices.containsKey(vId) && vertices.get(vId).get() != null;
		} else
			return false;
	}

	/* (non-Javadoc)
	 * @see de.uni_koblenz.jgralab.impl.db.GraphCache#getVertex(de.uni_koblenz.jgralab.Graph, int)
	 */
	@Override
	public DatabasePersistableVertex getVertex(Graph graph, int vId) {
		checkThreshold();
		if (this.verticesOfGraphs.containsKey(graph)) {
			Hashtable<Integer, SoftReference<DatabasePersistableVertex>> vertices = this.verticesOfGraphs
					.get(graph);
			return vertices.get(vId).get();
		} else
			return null;
	}

	/* (non-Javadoc)
	 * @see de.uni_koblenz.jgralab.impl.db.GraphCache#removeVertex(de.uni_koblenz.jgralab.impl.db.DatabasePersistableGraph, int)
	 */
	@Override
	public void removeVertex(DatabasePersistableGraph graph, int vId) {
		checkThreshold();
		Hashtable<Integer, SoftReference<DatabasePersistableVertex>> vertices = this.verticesOfGraphs.get(graph);
		vertices.remove(vId);
	}

	@Override
	public void addEdge(DatabasePersistableEdge edge) {
		checkThreshold();
		Hashtable<Integer, SoftReference<DatabasePersistableEdge>> edges;
		if (this.edgesOfGraphs.containsKey(edge.getGraph()))
			edges = this.edgesOfGraphs.get(edge.getGraph());
		else {
			edges = new Hashtable<Integer, SoftReference<DatabasePersistableEdge>>();
			this.edgesOfGraphs.put(edge.getGraph(), edges);
		}
		edges.put(edge.getId(), new SoftReference<DatabasePersistableEdge>(edge, this.edgeReferenceQueue));
	}

	@Override
	public boolean containsEdge(Graph graph, int eId) {
		checkThreshold();
		if (this.edgesOfGraphs.containsKey(graph)) {
			Hashtable<Integer, SoftReference<DatabasePersistableEdge>> edges = this.edgesOfGraphs.get(graph);
			return edges.containsKey(Math.abs(eId))	&& edges.get(Math.abs(eId)).get() != null;
		} else
			return false;
	}

	@Override
	public DatabasePersistableEdge getEdge(Graph graph, int eId) {
		checkThreshold();
		if (this.edgesOfGraphs.containsKey(graph)) {
			Hashtable<Integer, SoftReference<DatabasePersistableEdge>> edges = this.edgesOfGraphs.get(graph);
			return edges.get(eId).get();
		} else
			return null;
	}

	@Override
	public void removeEdge(DatabasePersistableGraph graph, int eId) {
		checkThreshold();
		Hashtable<Integer, SoftReference<DatabasePersistableEdge>> edges = this.edgesOfGraphs.get(graph);
		edges.remove(eId);
	}

	@Override
	public void clear() {
		this.edgesOfGraphs.clear();
		this.clearReferenceQueues();
		this.verticesOfGraphs.clear();
	}

	/**
	 * Clears reference queue from dead WeakReferences by polling it.
	 */
	private void clearReferenceQueues() {
		while (this.vertexReferenceQueue.poll() != null)
			;
		while (this.edgeReferenceQueue.poll() != null)
			;
	}
}
