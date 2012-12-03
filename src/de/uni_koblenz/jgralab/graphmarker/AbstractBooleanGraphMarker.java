package de.uni_koblenz.jgralab.graphmarker;

import java.util.Iterator;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.entries.BooleanFunctionEntry;

public abstract class AbstractBooleanGraphMarker extends
		AbstractGraphMarker<GraphElement<?, ?>> implements
		BooleanFunction<GraphElement<?, ?>> {

	protected AbstractBooleanGraphMarker(Graph graph) {
		super(graph);
	}

	/**
	 * Returns the Graph of this GraphMarker.
	 * 
	 * @return the Graph of this GraphMarker.
	 */
	@Override
	public Graph getGraph() {
		return graph;
	}

	@Override
	public void maxEdgeCountIncreased(int newValue) {
		// do nothing
	}

	@Override
	public void maxVertexCountIncreased(int newValue) {
		// do nothing
	}

	@Override
	public boolean get(GraphElement<?, ?> parameter) {
		return isMarked(parameter);
	}

	@Override
	public boolean isDefined(GraphElement<?, ?> parameter) {
		return true;
	}

	@Override
	public void set(GraphElement<?, ?> parameter, boolean value) {
		if (value) {
			mark(parameter);
		} else {
			removeMark(parameter);
		}
	}

	public abstract boolean mark(GraphElement<?, ?> parameter);

	@Override
	public Iterable<GraphElement<?, ?>> getDomainElements() {
		return getMarkedElements();
	}

	@Override
	public Iterator<BooleanFunctionEntry<GraphElement<?, ?>>> iterator() {
		final Iterator<GraphElement<?, ?>> markedElements = getMarkedElements()
				.iterator();
		return new Iterator<BooleanFunctionEntry<GraphElement<?, ?>>>() {

			@Override
			public boolean hasNext() {
				return markedElements.hasNext();
			}

			@Override
			public BooleanFunctionEntry<GraphElement<?, ?>> next() {
				GraphElement<?, ?> currentElement = markedElements.next();
				return new BooleanFunctionEntry<GraphElement<?, ?>>(
						currentElement, get(currentElement));
			}

			@Override
			public void remove() {
				markedElements.remove();
			}

		};
	}
}
