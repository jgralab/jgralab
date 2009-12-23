package de.uni_koblenz.jgralab.graphmarker;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;

/**
 * This class is the abstract superclass of generic graph markers.
 * 
 * @author ist@uni-koblenz.de
 * 
 * @param <T>
 */
public abstract class ArrayGenericGraphMarker<T extends GraphElement> extends
		AbstractGraphMarker<T> {

	/**
	 * The array of temporary attributes.
	 */
	protected Object[] temporaryAttributes;
	protected int marked;

	protected ArrayGenericGraphMarker(Graph graph, int size) {
		super(graph);
		temporaryAttributes = new Object[size];
		marked = 0;
	}

	@Override
	public void clear() {
		for (int i = 0; i < temporaryAttributes.length; i++) {
			temporaryAttributes[i] = null;
		}
		marked = 0;
	}

	@Override
	public boolean isEmpty() {
		return marked == 0;
	}

	@Override
	public boolean isMarked(T graphElement) {
		assert (graphElement.getId() <= (graphElement instanceof Vertex ? graph
				.getMaxVCount() : graph.getMaxECount()));
		return temporaryAttributes[graphElement.getId()] != null;
	}

	public T getMark(T graphElement) {
		assert (graphElement.getId() <= (graphElement instanceof Vertex ? graph
				.getMaxVCount() : graph.getMaxECount()));
		@SuppressWarnings("unchecked")
		T out = (T) temporaryAttributes[graphElement.getId()];
		marked += 1;
		return out;
	}

	@Override
	public int size() {
		return marked;
	}

	@Override
	public boolean unmark(T graphElement) {
		assert (graphElement.getId() <= (graphElement instanceof Vertex ? graph
				.getMaxVCount() : graph.getMaxECount()));
		if (temporaryAttributes[graphElement.getId()] == null) {
			return false;
		}
		temporaryAttributes[graphElement.getId()] = null;
		marked -= 1;
		return true;
	}

	@Override
	public abstract void edgeDeleted(Edge e);

	@Override
	public abstract void maxEdgeCountIncreased(int newValue);

	@Override
	public abstract void maxVertexCountIncreased(int newValue);

	@Override
	public abstract void vertexDeleted(Vertex v);

	protected void expand(int newSize) {
		assert (newSize > temporaryAttributes.length);
		Object[] newTemporaryAttributes = new Object[newSize];
		for (int i = 0; i < temporaryAttributes.length; i++) {
			newTemporaryAttributes[i] = temporaryAttributes[i];
		}
		temporaryAttributes = newTemporaryAttributes;
	}

}