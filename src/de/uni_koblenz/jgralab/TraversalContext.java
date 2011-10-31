package de.uni_koblenz.jgralab;

/**
 * Instances of this interface are used for defining subgraphs. A traversal
 * context is passed to a graph for restricting it to a subgraph. All traversal
 * related methods are now running on the subgraph instead of the whole graph.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public interface TraversalContext {

	/**
	 * Checks if the given vertex is included in the subgraph defined by this TC
	 * and the graph this TC is passed to. This method has to ensure that it
	 * returns false if the vertex is not in vSeq of the graph.
	 * 
	 * @param v
	 *            the vertex to check
	 * @return true if the given vertex is part of the subgraph defined by this
	 *         TC and the graph
	 */
	public boolean containsVertex(Vertex v);

	/**
	 * Checks if the given edge is included in the subgraph defined by this TC
	 * and the graph this TC is passed to. This method has to ensure that it
	 * returns false if the edge is not in eSeq of the graph.
	 * 
	 * @param e
	 *            the edge to check
	 * @return true if the given edge is part of the subgraph defined by this TC
	 *         and the graph
	 */
	public boolean containsEdge(Edge e);

}
