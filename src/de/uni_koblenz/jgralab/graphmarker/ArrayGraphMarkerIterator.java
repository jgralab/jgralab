package de.uni_koblenz.jgralab.graphmarker;

import java.util.Iterator;

import de.uni_koblenz.jgralab.GraphElement;

public abstract class ArrayGraphMarkerIterator<T extends GraphElement> implements Iterator<T> {
	protected int index;
	protected long version;
	protected static String MODIFIED_ERROR_MESSAGE = "The graph marker was modified during current iteration.";
	protected static String NO_MORE_ELEMENTS_ERROR_MESSAGE = "No more elements.";

	protected ArrayGraphMarkerIterator(long version) {
		index = 0;
		this.version = version;
		moveIndex();
	}

	protected abstract void moveIndex();

	@Override
	public abstract boolean hasNext();

	@Override
	public abstract T next();

	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove is not supported.");
	}
}
