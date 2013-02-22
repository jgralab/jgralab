package de.uni_koblenz.jgralab.impl.diskv2;

import de.uni_koblenz.jgralab.impl.InternalEdge;
import de.uni_koblenz.jgralab.impl.InternalVertex;
import de.uni_koblenz.jgralab.impl.diskv2.cache.EdgeCache;
import de.uni_koblenz.jgralab.impl.diskv2.cache.VertexCache;

/**
 * This class realizes the caching of vertices, edges and incidences in memory
 * in a distributed environment. All methods may be used only with local objects
 * and local ids.
 * 
 * @author dbildh, aheld
 * 
 */
public final class MemStorageManager {

	private DiskStorageManager dsm;
	private VertexCache vertexCache;
	private EdgeCache edgeCache;

	public MemStorageManager(GraphImpl database) {
		dsm = new DiskStorageManager(database);
		vertexCache = new VertexCache(dsm);
		edgeCache = new EdgeCache(dsm);
	}

	// ---- Methods to put, get and remove Graph elements and incidences from
	// the cache ----

	/**
	 * Retrieves a Vertex from the vertex cache
	 * 
	 * @param id
	 *            the id of the Vertex to be retrieved
	 * @return the Vertex with the given id
	 */
	public final InternalVertex getVertexObject(int id) {
		return id == 0 ? null : vertexCache.get(id);
	}

	public final InternalEdge getEdgeObject(int id) {
		if (id == 0) {
			return null;
		}
		EdgeImpl e = edgeCache.get(id < 0 ? -id : id);
		return id < 0 ? (InternalEdge) e.getReversedEdge() : e;
	}

	public final EdgeTracker getEdgeTracker(int id) {
		assert id != 0;
		return (EdgeTracker) edgeCache.getTracker(id < 0 ? -id : id);
	}

	public final VertexTracker getVertexTracker(int id) {
		assert id > 0;
		return (VertexTracker) vertexCache.getTracker(id);
	}

	public final void addVertex(VertexImpl v) {
		assert v != null && v.getId() > 0;
		vertexCache.add(v);
	}

	public final void deleteVertex(int id) {
		assert id != 0;
		vertexCache.delete(id);
	}

	public final void addEdge(EdgeImpl e) {
		assert e != null && e.getId() != 0;
		edgeCache.add(e);
	}

	public final void deleteEdge(int id) {
		assert id != 0;
		edgeCache.delete(id < 0 ? -id : id);
	}
}