package de.uni_koblenz.jgralab.algolib.buffers;

import java.util.NoSuchElementException;
import java.util.Random;

public class RandomBuffer<T> extends DynamicArrayBuffer<T> {

	private Random rng;

	public RandomBuffer(int initialSize) {
		super(initialSize);
		rng = new Random();
	}

	public T getNext() {
		if (filled == 0) {
			throw new NoSuchElementException("Buffer is empty");
		}
		int position = rng.nextInt(filled);
		@SuppressWarnings("unchecked")
		T out = (T) data[position];
		data[position] = data[--filled];
		data[filled] = null;
		return out;
	}

}
