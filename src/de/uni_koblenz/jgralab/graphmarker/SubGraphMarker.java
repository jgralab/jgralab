package de.uni_koblenz.jgralab.graphmarker;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;

/**
 * This class serves as a special <code>BitSetGraphmarker</code>, although it
 * does not extend it. It is capable of marking both vertices and edges. This is
 * necessary for defining subgraphs. Internally all calls are delegated to an
 * instance of <code>BitSetVertexGraphMarker</code> and an instance of
 * <code>BitSetEdgeGraphMarker</code>.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class SubGraphMarker extends AbstractGraphMarker<GraphElement> {

	// TODO maybe replace with BitSets

	private final BitSetEdgeMarker edgeGraphMarker;
	private final BitSetVertexMarker vertexGraphMarker;

	public SubGraphMarker(Graph graph) {
		super(graph);
		edgeGraphMarker = new BitSetEdgeMarker(graph);
		vertexGraphMarker = new BitSetVertexMarker(graph);
	}

	@Override
	public void clear() {
		edgeGraphMarker.clear();
		vertexGraphMarker.clear();
	}

	@Override
	public boolean isEmpty() {
		return edgeGraphMarker.isEmpty() && vertexGraphMarker.isEmpty();
	}

	@Override
	public boolean isMarked(GraphElement graphElement) {
		if (graphElement instanceof Edge) {
			return edgeGraphMarker.isMarked((Edge) graphElement);
		} else {
			return vertexGraphMarker.isMarked((Vertex) graphElement);
		}
	}

	@Override
	public int size() {
		return edgeGraphMarker.size() + vertexGraphMarker.size();
	}

	@Override
	public boolean removeMark(GraphElement graphElement) {
		if (graphElement instanceof Edge) {
			return edgeGraphMarker.removeMark((Edge) graphElement);
		} else {
			return vertexGraphMarker.removeMark((Vertex) graphElement);
		}
	}

	/**
	 * Does the same as <code>unmark</code> but without performing an
	 * <code>instanceof</code> check. It is recommended to use this method
	 * instead.
	 * 
	 * @param e
	 *            the edge to unmark
	 * @return false if the given edge has already been unmarked.
	 */
	public boolean removeMark(Edge e) {
		return edgeGraphMarker.removeMark(e);
	}

	/**
	 * Does the same as <code>unmark</code> but without performing an
	 * <code>instanceof</code> check. It is recommended to use this method
	 * instead.
	 * 
	 * @param v
	 *            the vertex to unmark
	 * @return false if the given vertex has already been unmarked.
	 */
	public boolean removeMark(Vertex v) {
		return vertexGraphMarker.removeMark(v);
	}

	/**
	 * Marks the given <code>graphElement</code>.
	 * 
	 * @param graphElement
	 *            the graph element to mark
	 * @return false if the given <code>graphElement</code> has already been
	 *         marked.
	 */
	public boolean mark(GraphElement graphElement) {
		if (graphElement instanceof Edge) {
			return edgeGraphMarker.mark((Edge) graphElement);
		} else {
			return vertexGraphMarker.mark((Vertex) graphElement);
		}
	}

	/**
	 * Does the same as <code>mark</code> but without performing an
	 * <code>instanceof</code> check. It is recommended to use this method
	 * instead.
	 * 
	 * @param e
	 *            the edge to mark
	 * @return false if the given edge has already been marked.
	 */
	public boolean mark(Edge e) {
		return edgeGraphMarker.mark(e);
	}

	/**
	 * Does the same as <code>mark</code> but without performing an
	 * <code>instanceof</code> check. It is recommended to use this method
	 * instead.
	 * 
	 * @param v
	 *            the vertex to mark
	 * @return false if the given vertex has already been marked.
	 */
	public boolean mark(Vertex v) {
		return vertexGraphMarker.mark(v);
	}

	@Override
	public void edgeDeleted(Edge e) {
		edgeGraphMarker.edgeDeleted(e);
	}

	@Override
	public void vertexDeleted(Vertex v) {
		vertexGraphMarker.vertexDeleted(v);
	}

	@Override
	public void maxEdgeCountIncreased(int newValue) {
		// do nothing
	}

	@Override
	public void maxVertexCountIncreased(int newValue) {
		// do nothing
	}
}
