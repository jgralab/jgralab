package de.uni_koblenz.jgralab;

/**
 * All implementations of <code>GraphStructureListener</code> that are
 * registered at the graph, are notified about changes in the structure of the
 * graph. These changes are:
 *<ul>
 *<li>adding a vertex</li>
 *<li>deleting a vertex</li>
 *<li>adding an edge</li>
 *<li>deleting an edge</li>
 *<li>increasing <code>maxVCount</code>
 *<li>increasing <code>maxECount</code></li>
 *</ul>
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public interface GraphStructureChangedListener {

	/**
	 * This method is called after the vertex <code>v</code> has been added to
	 * the graph.
	 * 
	 * @param v
	 *            the vertex that has been added.
	 */
	public void vertexAdded(Vertex v);

	/**
	 * This method is called before the vertex <code>v</code> is deleted.
	 * 
	 * @param v
	 *            the vertex that is about to be deleted.
	 */
	public void vertexDeleted(Vertex v);

	/**
	 * This method is called after the Edge <code>e</code> has been added to the
	 * graph.
	 * 
	 * @param e
	 *            the edge that has been added.
	 */
	public void edgeAdded(Edge e);

	/**
	 * This method is called before the edge <code>e</code> is deleted.
	 * 
	 * @param e
	 *            the edge that is about to be deleted.
	 */
	public void edgeDeleted(Edge e);

	/**
	 * This method is called after the maximum vertex count has been increased
	 * to <code>newValue</code>.
	 * 
	 * @param newValue
	 *            the new value of <code>maxVCount</code>.
	 */
	public void maxVertexCountIncreased(int newValue);

	/**
	 * This method is called after the maximum edge count has been increased to
	 * <code>newValue</code>.
	 * 
	 * @param newValue
	 *            the new value of <code>maxECount</code>.
	 */
	public void maxEdgeCountIncreased(int newValue);
}
