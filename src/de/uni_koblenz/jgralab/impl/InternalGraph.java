package de.uni_koblenz.jgralab.impl;

import java.util.List;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.Attribute;

public interface InternalGraph extends Graph {

	/**
	 * Changes this graph's version. graphModified() is called whenever the
	 * graph is changed, all changes like adding, creating and reordering of
	 * edges and vertices or changes of attributes of the graph, an edge or a
	 * vertex are treated as a change.
	 */
	public abstract void graphModified();

	/**
	 * Triggers ECA-rules before an Attribute is changed
	 * 
	 * @param name
	 *            of the changing Attribute
	 */
	public abstract void ecaAttributeChanging(String name, Object oldValue,
			Object newValue);

	/**
	 * Triggers ECA-rule after an Attribute is changed
	 * 
	 * @param name
	 *            of the changed Attribute
	 */
	public abstract void ecaAttributeChanged(String name, Object oldValue,
			Object newValue);

	/**
	 * Constructs incidence lists for all vertices after loading this graph.
	 * 
	 * @param firstIncidence
	 *            array of edge ids of the first incidence
	 * @param nextIncidence
	 *            array of edge ids of subsequent edges
	 */
	public abstract void internalLoadingCompleted(int[] firstIncidence,
			int[] nextIncidence);

	/**
	 * Sets the version counter of this graph. Should only be called immediately
	 * after loading.
	 * 
	 * @param graphVersion
	 *            new version value
	 */
	public abstract void setGraphVersion(long graphVersion);

	/**
	 * Sets the loading flag.
	 * 
	 * @param isLoading
	 */
	public abstract void setLoading(boolean isLoading);

	/**
	 * Checks whether this graph is currently being loaded.
	 * 
	 * @return true if the graph is currently being loaded
	 */
	public boolean isLoading();

	/**
	 * Callback method: Called immediately after loading of this graph is
	 * completed. Overwrite this method to perform user defined operations after
	 * loading a graph.
	 */
	public void loadingCompleted();

	/**
	 * Checks if the vertex sequence of this has changed with respect to the
	 * given <code>previousVersion</code>. Changes in the vertex sequence are
	 * creation and deletion as well as reordering of vertices, but not changes
	 * of attribute values.
	 * 
	 * @return <code>true</code> if the vertex list version of this graph is
	 *         different from <code>previousVersion</code>.
	 */
	public boolean isVertexListModified(long previousVersion);

	/**
	 * Returns the version counter of the vertex sequence of this graph.
	 * 
	 * @return the vertex sequence version
	 * @see #isVertexListModified(long)
	 */
	public long getVertexListVersion();

	public boolean vSeqContainsVertex(Vertex v);

	/**
	 * Checks if the edge sequence of this has changed with respect to the given
	 * <code>previousVersion</code>. Changes in the edge sequence are creation
	 * and deletion as well as reordering of edges, but not changes of attribute
	 * values.
	 * 
	 * @return <code>true</code> if the edge list version of this graph is
	 *         different from <code>previousVersion</code>.
	 */
	public boolean isEdgeListModified(long edgeListVersion);

	/**
	 * Returns the version counter of the edge sequence of this graph.
	 * 
	 * @return the edge sequence version
	 * @see #isEdgeListModified(long)
	 */
	public long getEdgeListVersion();

	public boolean eSeqContainsEdge(Edge e);

	/**
	 * The maximum number of vertices that can be stored in the graph before the
	 * internal array structures are expanded.
	 * 
	 * @return the maximum number of vertices
	 */
	public int getMaxVCount();

	public int getVCountInVSeq();

	/**
	 * Computes the new maximum number of vertices when expansion is needed.
	 * 
	 * @return the new maximum number of vertices
	 */
	public int getExpandedVertexCount();

	/**
	 * Computes the new maximum number of edges when expansion is needed.
	 * 
	 * @return the new maximum number of edges
	 */
	public int getExpandedEdgeCount();

	/**
	 * The maximum number of edges that can be stored in the graph before the
	 * internal array structures are expanded.
	 * 
	 * @return the maximum number of edges
	 */
	public int getMaxECount();

	public int getECountInESeq();

	/**
	 * Sets the <code>id</code> of this Graph.
	 * 
	 * Precondition: id != null && id.equals(id.trim()) && !id.equals("")
	 * 
	 * @param id
	 *            the new id
	 */
	public void setId(String id);

	/**
	 * Optimizes edge and vertex ids such that after defragmentation
	 * getMaxECount() == getECount() and getMaxVCount() == getVCount(). That
	 * means that gaps in the vertex and edge IDs are deleted (defragmented) and
	 * that the internal arrays are shortened such that they hold exactly the
	 * required number of vertices/edges.
	 * 
	 * <b>Attention:</b> defragment() possibly changes vertex and edge IDs! *
	 * <b>Attention:</b> Not supported within when using transactions!
	 */
	public void defragment();

	/**
	 * Delegates to {@link Graph#getCurrentTransaction()
	 * getCurrentTransaction()}.
	 * 
	 * @return if there have been conflicts
	 */
	public boolean isInConflict();

	/**
	 * Returns the first Vertex in the vertex sequence of this Graph.
	 * 
	 * @return the first Vertex, or null if this graph contains no vertices.
	 */
	public InternalVertex getFirstVertexInVSeq();

	/**
	 * Returns the last Vertex in the vertex sequence of this Graph.
	 * 
	 * @return the last Vertex, or null if this graph contains no vertices.
	 */
	public InternalVertex getLastVertexInVSeq();

	/**
	 * Returns the first Edge in the edge sequence of this Graph.
	 * 
	 * @return the first Edge, or null if this graph contains no edges.
	 */
	public InternalEdge getFirstEdgeInESeq();

	/**
	 * Returns the last Edge in the edge sequence of this Graph.
	 * 
	 * @return the last Edge, or null if this graph contains no edges.
	 */
	public InternalEdge getLastEdgeInESeq();

	public Edge internalCreateEdge(Class<? extends Edge> cls, Vertex alpha,
			Vertex omega);

	public Vertex internalCreateVertex(Class<? extends Vertex> cls);

	public void internalEdgeAdded(InternalEdge e);

	public void internalVertexAdded(InternalVertex v);

	public void internalEdgeDeleted(InternalEdge e);

	/**
	 * 
	 * @param attr
	 * @throws GraphIOException
	 */
	public void internalSetDefaultValue(Attribute attr) throws GraphIOException;

	/**
	 * Adds a vertex to this graph. If the vertex' id is 0, a valid id is set,
	 * otherwise the vertex' current id is used if possible. Should only be used
	 * by m1-Graphs derived from Graph. To create a new Vertex as user, use the
	 * appropriate methods from the derived Graphs like
	 * <code>createStreet(...)</code>
	 * 
	 * @param newVertex
	 *            the Vertex to add
	 * 
	 * @throws GraphException
	 *             if a vertex with the same id already exists
	 */
	public void addVertex(Vertex newVertex);

	/**
	 * Adds an edge to this graph. If the edges id is 0, a valid id is set,
	 * otherwise the edges current id is used if possible. Should only be used
	 * by m1-Graphs derived from Graph. To create a new Edge as user, use the
	 * appropriate methods from the derived Graphs like
	 * <code>createStreet(...)</code>
	 * 
	 * @param newEdge
	 *            Edge to add
	 * @param alpha
	 *            Vertex new edge should start at.
	 * @param omega
	 *            Vertex new edge should end at.
	 * @throws GraphException
	 *             vertices do not suit the edge, an edge with same id already
	 *             exists in graph, id of edge greater than possible count of
	 *             edges in graph
	 */
	public void addEdge(Edge newEdge, Vertex alpha, Vertex omega);

	/**
	 * Use to free an <code>Edge</code>-index
	 * 
	 * @param index
	 */
	public void freeEdgeIndex(int index);

	/**
	 * Use to free a <code>Vertex</code>-index.
	 * 
	 * @param index
	 */
	public void freeVertexIndex(int index);

	/**
	 * Use to allocate a <code>Vertex</code>-index.
	 * 
	 * @param currentId
	 *            needed for transaction support
	 */
	public int allocateVertexIndex(int currentId);

	/**
	 * Use to allocate a <code>Edge</code>-index.
	 * 
	 * @param currentId
	 *            needed for transaction support
	 */
	public int allocateEdgeIndex(int currentId);

	/**
	 * 
	 * @param freeVertexList
	 */
	public void setFreeVertexList(FreeIndexList freeVertexList);

	/**
	 * 
	 * @param freeEdgeList
	 */
	public void setFreeEdgeList(FreeIndexList freeEdgeList);

	/**
	 * Callback function for triggered actions just after the edge
	 * <code>e</code> was deleted from this Graph. Override this method to
	 * implement user-defined behaviour upon deletion of edges. Note that any
	 * changes to this graph are forbidden.
	 * 
	 * Needed for transaction support.
	 * 
	 * @param e
	 *            the deleted Edge
	 * @param oldAlpha
	 *            the alpha-vertex before deletion
	 * @param oldOmega
	 *            the omega-vertex before deletion
	 */
	public void edgeAfterDeleted(Edge e, Vertex oldAlpha, Vertex oldOmega);

	/**
	 * Changes the graph structure version, should be called whenever the
	 * structure of the graph is changed, for instance by creation and deletion
	 * or reordering of vertices and edges
	 */
	public void edgeListModified();

	/**
	 * Changes the vertex sequence version of this graph. Should be called
	 * whenever the vertex list of this graph is changed, for instance by
	 * creation and deletion or reordering of vertices.
	 */
	public void vertexListModified();

	/**
	 * Changes the size of the edge array of this graph to newSize.
	 * 
	 * @param newSize
	 *            the new size of the edge array
	 */
	public void expandEdgeArray(int newSize);

	/**
	 * Changes the size of the vertex array of this graph to newSize.
	 * 
	 * @param newSize
	 *            the new size of the vertex array
	 */
	public void expandVertexArray(int newSize);

	public void internalVertexDeleted(InternalVertex v);

	/**
	 * Removes the vertex v from the global vertex sequence of this graph.
	 * 
	 * @param v
	 *            a vertex
	 */
	public void removeVertexFromVSeq(InternalVertex v);

	/**
	 * number of vertices in the graph
	 */
	public void setVCount(int count);

	/**
	 * indexed with vertex-id, holds the actual vertex-object itself
	 */
	public InternalVertex[] getVertex();

	public void setVertex(InternalVertex[] vertex);

	public FreeIndexList getFreeVertexList();

	/**
	 * holds the id of the first vertex in Vseq
	 */
	public void setFirstVertex(InternalVertex firstVertex);

	/**
	 * holds the id of the last vertex in Vseq
	 */
	public void setLastVertex(InternalVertex lastVertex);

	/**
	 * Sets version of VSeq if it is different than previous version.
	 * 
	 * @param vertexListVersion
	 *            Version of VSeq.
	 */
	public void setVertexListVersion(long vertexListVersion);

	/**
	 * List of vertices to be deleted by a cascading delete caused by deletion
	 * of a composition "parent".
	 */
	public List<InternalVertex> getDeleteVertexList();

	public void setDeleteVertexList(List<InternalVertex> deleteVertexList);

	/**
	 * number of edges in the graph
	 */
	public void setECount(int count);

	/**
	 * indexed with edge-id, holds the actual edge-object itself
	 */
	public InternalEdge[] getEdge();

	public void setEdge(InternalEdge[] edge);

	public InternalEdge[] getRevEdge();

	public void setRevEdge(InternalEdge[] revEdge);

	public FreeIndexList getFreeEdgeList();

	/**
	 * holds the id of the first edge in Eseq
	 */
	public void setFirstEdgeInGraph(InternalEdge firstEdge);

	/**
	 * holds the id of the last edge in Eseq
	 */
	public void setLastEdgeInGraph(InternalEdge lastEdge);

	/**
	 * Sets version of ESeq.
	 * 
	 * @param edgeListVersion
	 *            Version to set.
	 */
	public void setEdgeListVersion(long edgeListVersion);

	/**
	 * Notifies all registered <code>GraphStructureChangedListener</code> that
	 * the given edge <code>e</code> has been created. All invalid
	 * <code>WeakReference</code>s are deleted automatically from the internal
	 * listener list.
	 * 
	 * @param e
	 *            the edge that has been created.
	 */
	public void notifyEdgeAdded(Edge e);

	/**
	 * Notifies all registered <code>GraphStructureChangedListener</code> that
	 * the given edge <code>e</code> is about to be deleted. All invalid
	 * <code>WeakReference</code>s are deleted automatically from the internal
	 * listener list.
	 * 
	 * @param e
	 *            the edge that is about to be deleted.
	 */
	public void notifyEdgeDeleted(Edge e);

	/**
	 * Notifies all registered <code>GraphStructureChangedListener</code> that
	 * the given vertex <code>v</code> has been created. All invalid
	 * <code>WeakReference</code>s are deleted automatically from the internal
	 * listener list.
	 * 
	 * @param v
	 *            the vertex that has been created.
	 */
	public void notifyVertexAdded(Vertex v);

	/**
	 * Notifies all registered <code>GraphStructureChangedListener</code> that
	 * the given vertex <code>v</code> is about to be deleted. All invalid
	 * <code>WeakReference</code>s are deleted automatically from the internal
	 * listener list.
	 * 
	 * @param v
	 *            the vertex that is about to be deleted.
	 */
	public void notifyVertexDeleted(Vertex v);

	/**
	 * Notifies all registered <code>GraphStructureChangedListener</code> that
	 * the maximum vertex count has been increased to the given
	 * <code>newValue</code>. All invalid <code>WeakReference</code>s are
	 * deleted automatically from the internal listener list.
	 * 
	 * @param newValue
	 *            the new maximum vertex count.
	 */
	public void notifyMaxVertexCountIncreased(int newValue);

	/**
	 * Notifies all registered <code>GraphStructureChangedListener</code> that
	 * the maximum edge count has been increased to the given
	 * <code>newValue</code>. All invalid <code>WeakReference</code>s are
	 * deleted automatically from the internal listener list.
	 * 
	 * @param newValue
	 *            the new maximum edge count.
	 */
	public void notifyMaxEdgeCountIncreased(int newValue);

	/**
	 * Modifies eSeq such that the movedEdge is immediately after the
	 * targetEdge.
	 * 
	 * @param targetEdge
	 *            an edge
	 * @param movedEdge
	 *            the edge to be moved
	 */
	public void putEdgeAfterInGraph(InternalEdge targetEdge,
			InternalEdge movedEdge);

	/**
	 * Modifies eSeq such that the movedEdge is immediately before the
	 * targetEdge.
	 * 
	 * @param targetEdge
	 *            an edge
	 * @param movedEdge
	 *            the edge to be moved
	 */
	public void putEdgeBeforeInGraph(InternalEdge targetEdge,
			InternalEdge movedEdge);

	/**
	 * Modifies vSeq such that the movedVertex is immediately after the
	 * targetVertex.
	 * 
	 * @param targetVertex
	 *            a vertex
	 * @param movedVertex
	 *            the vertex to be moved
	 */
	public void putVertexAfter(InternalVertex targetVertex,
			InternalVertex movedVertex);

	/**
	 * Modifies vSeq such that the movedVertex is immediately before the
	 * targetVertex.
	 * 
	 * @param targetVertex
	 *            a vertex
	 * @param movedVertex
	 *            the vertex to be moved
	 */
	public void putVertexBefore(InternalVertex targetVertex,
			InternalVertex movedVertex);

	/**
	 * Removes the edge e from the global edge sequence of this graph.
	 * 
	 * @param e
	 *            an edge
	 */
	public void removeEdgeFromESeq(InternalEdge e);

	/**
	 * Callback function for triggered actions just after the vertex
	 * <code>v</code> was deleted from this Graph. Override this method to
	 * implement user-defined behaviour upon deletion of vertices. Note that any
	 * changes to this graph are forbidden.
	 * 
	 * @param v
	 *            the deleted vertex
	 */
	public void vertexAfterDeleted(Vertex v);

	/**
	 * Appends the edge e to the global edge sequence of this graph.
	 * 
	 * @param e
	 *            an edge
	 */
	public void appendEdgeToESeq(InternalEdge e);

	/**
	 * Appends the vertex v to the global vertex sequence of this graph.
	 * 
	 * @param v
	 *            a vertex
	 */
	public void appendVertexToVSeq(InternalVertex v);
}