package de.uni_koblenz.jgralab.algolib.buffers;

import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;


public class UnsortedPriorityQueueBuffer<T> extends
		DynamicArrayBuffer<T> implements Buffer<T> {

	private Comparator<T> comparator;

	public UnsortedPriorityQueueBuffer(int initialSize, Comparator<T> comparator) {
		super(initialSize);
		this.comparator = comparator;
	}

	@Override
	public T getNext() {
		if (filled == 0) {
			throw new NoSuchElementException("Buffer is empty");
		}
		int minIndex = indexOfMinimum();
		@SuppressWarnings("unchecked")
		T out = (T) data[minIndex];
		data[minIndex] = data[--filled];
		data[filled] = null;
		return out;
	}

	@SuppressWarnings("unchecked")
	private int indexOfMinimum() {
		assert (filled > 0);
		T min = (T) data[0];
		int out = 0;
		for (int i = 0; i < filled; i++) {
			if (comparator.compare((T) data[i], min) < 0) {
				min = (T) data[i];
				out = i;
			}
		}
		return out;
	}
	
	@Override
	public String toString(){
		return Arrays.toString(data);
	}

}
