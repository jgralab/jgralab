package de.uni_koblenz.jgralab.graphmarker;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;

public abstract class DoubleArrayGraphMarker<T extends GraphElement> extends
		AbstractGraphMarker<T> {

	private static final double DEFAULT_UNMARKED_VALUE = Double.NaN;

	protected double[] temporaryAttributes;
	protected int marked;
	protected double unmarkedValue;

	protected DoubleArrayGraphMarker(Graph graph, int size) {
		super(graph);
		unmarkedValue = DEFAULT_UNMARKED_VALUE;
		temporaryAttributes = createNewArray(size);
	}

	private double[] createNewArray(int size) {
		double[] newArray = new double[size];
		for (int i = 0; i < size; i++) {
			newArray[i] = unmarkedValue;
		}
		return newArray;
	}

	@Override
	public void clear() {
		for (int i = 0; i < temporaryAttributes.length; i++) {
			temporaryAttributes[i] = unmarkedValue;
		}
		marked = 0;
	}

	@Override
	public boolean isEmpty() {
		return marked == 0;
	}

	@Override
	public boolean isMarked(T graphElement) {
		assert (graphElement.getGraph() == graph);
		assert (graphElement.getId() <= (graphElement instanceof Vertex ? graph
				.getMaxVCount() : graph.getMaxECount()));
		return temporaryAttributes[graphElement.getId()] != unmarkedValue;
	}

	/**
	 * marks the given element with the given value
	 * 
	 * @param elem
	 *            the graph element to mark
	 * @param value
	 *            the object that should be used as marking
	 * @return The previous element the given graph element has been marked
	 *         with, <code>null</code> if the given element has not been marked.
	 */
	public double mark(T graphElement, double value) {
		assert (graphElement.getGraph() == graph);
		assert (graphElement.getId() <= (graphElement instanceof Vertex ? graph
				.getMaxVCount() : graph.getMaxECount()));
		double out = temporaryAttributes[graphElement.getId()];
		temporaryAttributes[graphElement.getId()] = value;
		marked += 1;
		return out;
	}

	public double getMark(T graphElement) {
		assert (graphElement.getGraph() == graph);
		assert (graphElement.getId() <= (graphElement instanceof Vertex ? graph
				.getMaxVCount() : graph.getMaxECount()));
		double out = temporaryAttributes[graphElement.getId()];
		return out;
	}

	@Override
	public boolean removeMark(T graphElement) {
		assert (graphElement.getGraph() == graph);
		assert (graphElement.getId() <= (graphElement instanceof Vertex ? graph
				.getMaxVCount() : graph.getMaxECount()));
		if (temporaryAttributes[graphElement.getId()] == unmarkedValue) {
			return false;
		}
		temporaryAttributes[graphElement.getId()] = unmarkedValue;
		marked -= 1;
		return true;
	}

	@Override
	public int size() {
		return marked;
	}

	public int maxSize() {
		return temporaryAttributes.length;
	}

	protected void expand(int newSize) {
		assert (newSize > temporaryAttributes.length);
		double[] newTemporaryAttributes = createNewArray(newSize);
		System.arraycopy(temporaryAttributes, 0, newTemporaryAttributes, 0,
				temporaryAttributes.length);
		// for (int i = 0; i < temporaryAttributes.length; i++) {
		// newTemporaryAttributes[i] = temporaryAttributes[i];
		// }
		temporaryAttributes = newTemporaryAttributes;
	}

	public double getUnmarkedValue() {
		return unmarkedValue;
	}

	public void setUnmarkedValue(double newUnmarkedValue) {
		for (int i = 0; i < temporaryAttributes.length; i++) {
			// keep track of implicitly unmarked values
			if (temporaryAttributes[i] == newUnmarkedValue) {
				marked -= 1;
			}
			// set all unmarked elements to new value
			if (temporaryAttributes[i] == this.unmarkedValue) {
				temporaryAttributes[i] = newUnmarkedValue;
			}

		}
		this.unmarkedValue = newUnmarkedValue;
	}

}
