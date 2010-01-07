package de.uni_koblenz.jgralab.graphmarker;

import java.util.BitSet;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;

/**
 * This class can be used to "colorize" graphs, it supports only two "colors",
 * that are "marked" or "not marked".
 * 
 * @author ist@uni-koblenz.de
 * 
 * @param <T>
 *            the name of the graph element class used by this
 *            <code>BitSetGraphMarker</code>
 */
public abstract class BitSetGraphMarker<T extends GraphElement> extends
		AbstractGraphMarker<T> {
	protected final BitSet marks;

	/**
	 * Initializes a new BitSetGraphMarker with the given graph.
	 * 
	 * @param graph
	 */
	protected BitSetGraphMarker(Graph graph) {
		super(graph);
		marks = new BitSet();
	}

	/**
	 * Marks the given <code>graphElement</code>.
	 * 
	 * @param graphElement
	 *            the graph element to mark
	 * @return false if the given <code>graphElement</code> has already been
	 *         marked.
	 */
	public boolean mark(T graphElement) {
		assert (graphElement.getId() <= (graphElement instanceof Vertex ? graph
				.getMaxVCount() : graph.getMaxECount()));
		boolean out = isMarked(graphElement);
		marks.set(graphElement.getId());
		return !out;
	}

	@Override
	public boolean removeMark(T graphElement) {
		assert (graphElement.getId() <= (graphElement instanceof Vertex ? graph
				.getMaxVCount() : graph.getMaxECount()));
		boolean out = isMarked(graphElement);
		marks.clear(graphElement.getId());
		return !out;
	}

	@Override
	public int size() {
		return marks.cardinality();
	}

	@Override
	public boolean isEmpty() {
		return marks.isEmpty();
	}

	@Override
	public void clear() {
		marks.clear();
	}

	@Override
	public boolean isMarked(T graphElement) {
		assert (graphElement.getId() <= (graphElement instanceof Vertex ? graph
				.getMaxVCount() : graph.getMaxECount()));
		return marks.get(graphElement.getId());
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
