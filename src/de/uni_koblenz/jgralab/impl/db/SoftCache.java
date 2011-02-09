package de.uni_koblenz.jgralab.impl.db;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Hashtable;

import de.uni_koblenz.jgralab.Graph;

/**
 * Global cache for graphs using weak references.
 * 
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
		verticesOfGraphs = new Hashtable<Graph, Hashtable<Integer, SoftReference<DatabasePersistableVertex>>>();
		vertexReferenceQueue = new ReferenceQueue<DatabasePersistableVertex>();
		edgesOfGraphs = new Hashtable<Graph, Hashtable<Integer, SoftReference<DatabasePersistableEdge>>>();
		edgeReferenceQueue = new ReferenceQueue<DatabasePersistableEdge>();
	}

	private void checkThreshold() {
		accessCounter++;
		if (accessCounter > THRESHOLD) {
			clearReferenceQueues();
			accessCounter = 0;
		}
	}

	@Override
	public void addVertex(DatabasePersistableVertex vertex) {
		checkThreshold();
		Hashtable<Integer, SoftReference<DatabasePersistableVertex>> vertices;
		if (verticesOfGraphs.containsKey(vertex.getGraph())) {
			vertices = verticesOfGraphs.get(vertex.getGraph());
		} else {
			vertices = new Hashtable<Integer, SoftReference<DatabasePersistableVertex>>();
			verticesOfGraphs.put(vertex.getGraph(), vertices);
		}
		vertices.put(vertex.getId(),
				new SoftReference<DatabasePersistableVertex>(vertex,
						vertexReferenceQueue));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.impl.db.GraphCache#containsVertex(de.uni_koblenz
	 * .jgralab.Graph, int)
	 */
	@Override
	public boolean containsVertex(Graph graph, int vId) {
		checkThreshold();
		if (verticesOfGraphs.containsKey(graph)) {
			Hashtable<Integer, SoftReference<DatabasePersistableVertex>> vertices = verticesOfGraphs
					.get(graph);
			return vertices.containsKey(vId) && vertices.get(vId).get() != null;
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.impl.db.GraphCache#getVertex(de.uni_koblenz.jgralab
	 * .Graph, int)
	 */
	@Override
	public DatabasePersistableVertex getVertex(Graph graph, int vId) {
		checkThreshold();
		if (verticesOfGraphs.containsKey(graph)) {
			Hashtable<Integer, SoftReference<DatabasePersistableVertex>> vertices = verticesOfGraphs
					.get(graph);
			return vertices.get(vId).get();
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.impl.db.GraphCache#removeVertex(de.uni_koblenz
	 * .jgralab.impl.db.DatabasePersistableGraph, int)
	 */
	@Override
	public void removeVertex(DatabasePersistableGraph graph, int vId) {
		checkThreshold();
		Hashtable<Integer, SoftReference<DatabasePersistableVertex>> vertices = verticesOfGraphs
				.get(graph);
		vertices.remove(vId);
	}

	@Override
	public void addEdge(DatabasePersistableEdge edge) {
		checkThreshold();
		Hashtable<Integer, SoftReference<DatabasePersistableEdge>> edges;
		if (edgesOfGraphs.containsKey(edge.getGraph())) {
			edges = edgesOfGraphs.get(edge.getGraph());
		} else {
			edges = new Hashtable<Integer, SoftReference<DatabasePersistableEdge>>();
			edgesOfGraphs.put(edge.getGraph(), edges);
		}
		edges.put(edge.getId(), new SoftReference<DatabasePersistableEdge>(
				edge, edgeReferenceQueue));
	}

	@Override
	public boolean containsEdge(Graph graph, int eId) {
		checkThreshold();
		if (edgesOfGraphs.containsKey(graph)) {
			Hashtable<Integer, SoftReference<DatabasePersistableEdge>> edges = edgesOfGraphs
					.get(graph);
			return edges.containsKey(Math.abs(eId))
					&& edges.get(Math.abs(eId)).get() != null;
		} else {
			return false;
		}
	}

	@Override
	public DatabasePersistableEdge getEdge(Graph graph, int eId) {
		checkThreshold();
		if (edgesOfGraphs.containsKey(graph)) {
			Hashtable<Integer, SoftReference<DatabasePersistableEdge>> edges = edgesOfGraphs
					.get(graph);
			return edges.get(eId).get();
		} else {
			return null;
		}
	}

	@Override
	public void removeEdge(DatabasePersistableGraph graph, int eId) {
		checkThreshold();
		Hashtable<Integer, SoftReference<DatabasePersistableEdge>> edges = edgesOfGraphs
				.get(graph);
		edges.remove(eId);
	}

	@Override
	public void clear() {
		edgesOfGraphs.clear();
		clearReferenceQueues();
		verticesOfGraphs.clear();
	}

	/**
	 * Clears reference queue from dead WeakReferences by polling it.
	 */
	private void clearReferenceQueues() {
		while (vertexReferenceQueue.poll() != null) {
			;
		}
		while (edgeReferenceQueue.poll() != null) {
			;
		}
	}
}
