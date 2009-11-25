package de.uni_koblenz.jgralab.graphmarker;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.Vertex;

/**
 * Marks directed graphs with arbitrary objects. In contrast to the marking
 * mechanism implemented by {@link GraphMarker}, in this case the edges are
 * marked taking their direction into account. That means, a incomming edge and
 * the same edge in the outgoing direction are marked independently
 * 
 * @author ist@uni-koblenz.de
 * 
 * @param <T>
 */
public class DirectedGraphMarker<T> extends GraphMarker<T> {

	/**
	 * Creates a new GraphMarker
	 */
	public DirectedGraphMarker(Graph g) {
		super(g);
	}

	/**
	 * returns the object that marks the given Graph, Edge or Vertex in this
	 * marking.
	 * 
	 * @param elem
	 *            the element to get the marking for
	 * @return the object that marks the given element or <code>null</code> if
	 *         the given element is not marked in this marking.
	 */
	public T getMark(AttributedElement elem) {
		if (elem == null)
			return null;
		return tempAttributeMap.get(elem);
	}

	/**
	 * marks the given element with the given value
	 * 
	 * @param elem
	 *            the element (Graph, Vertex or Edge) to mark
	 * @param value
	 *            the object that should be used as marking
	 * @return true on success, false if the given element already contains a
	 *         marking
	 */
	public T mark(AttributedElement elem, T value) {

		if ((elem instanceof Vertex && ((Vertex) elem).getGraph() == graph)
				|| (elem instanceof Edge && ((Edge) elem).getGraph() == graph)
				|| elem == graph) {
			return tempAttributeMap.put(elem, value);
		}
		throw new GraphException("Can't mark the element " + elem
				+ ", because it belongs to a different graph.");
	}

}
