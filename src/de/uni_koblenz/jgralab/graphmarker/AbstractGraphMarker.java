package de.uni_koblenz.jgralab.graphmarker;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.GraphStructureChangedAdapter;

public abstract class AbstractGraphMarker<T extends GraphElement> extends
		GraphStructureChangedAdapter {
	protected final Graph graph;

	protected AbstractGraphMarker(Graph graph) {
		this.graph = graph;
	}

	/**
	 * Checks if the given <code>graphElement</code> is marked.
	 * 
	 * @param graphElement
	 *            the graph element to check.
	 * @return true if the given <code>graphElement</code> is marked.
	 */
	public abstract boolean isMarked(T graphElement);

	/**
	 * Unmarks the given <code>graphElement</code>.
	 * 
	 * @param graphElement
	 *            the graph element to unmark.
	 * @return false if the given <code>graphElement</code> has already been
	 *         unmarked.
	 */
	public abstract boolean unmark(T graphElement);

	/**
	 * Returns the number of marked graph elements.
	 * 
	 * @return the number of marked graph elements.
	 */
	public abstract int size();

	/**
	 * Checks if this graph marker is empty.
	 * 
	 * @return true if this graph marker is empty.
	 */
	public abstract boolean isEmpty();

	/**
	 * Unmarks all marked graph elements.
	 */
	public abstract void clear();

	public Graph getGraph() {
		return graph;
	}
}
