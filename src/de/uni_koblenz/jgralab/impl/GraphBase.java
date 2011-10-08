package de.uni_koblenz.jgralab.impl;

import de.uni_koblenz.jgralab.Graph;

public interface GraphBase extends Graph {

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

	/**
	 * The maximum number of vertices that can be stored in the graph before the
	 * internal array structures are expanded.
	 * 
	 * @return the maximum number of vertices
	 */
	public int getMaxVCount();

	public int getBaseVCount();

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

	public int getBaseECount();

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
	public VertexBase getFirstBaseVertex();

	/**
	 * Returns the last Vertex in the vertex sequence of this Graph.
	 * 
	 * @return the last Vertex, or null if this graph contains no vertices.
	 */
	public VertexBase getLastBaseVertex();

	/**
	 * Returns the first Edge in the edge sequence of this Graph.
	 * 
	 * @return the first Edge, or null if this graph contains no edges.
	 */
	public EdgeBase getFirstBaseEdge();

	/**
	 * Returns the last Edge in the edge sequence of this Graph.
	 * 
	 * @return the last Edge, or null if this graph contains no edges.
	 */
	public EdgeBase getLastBaseEdge();

}