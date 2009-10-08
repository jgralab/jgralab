package de.uni_koblenz.jgralabtest.tools;

import java.util.NoSuchElementException;
import java.util.Random;

public class RandomBufferGeneric<T> {

	private Object[] data;
	private int initialSize;
	private int filled;
	private Random rnd;

	public RandomBufferGeneric(int initialSize) {
		this.initialSize = initialSize;
		data = new Object[initialSize];
		filled = 0;
		rnd = new Random();
	}

	private void expand() {
		// System.out.println("Expanding");
		Object[] newData = new Object[data.length + initialSize];
		for (int i = 0; i < data.length; i++) {
			newData[i] = data[i];
		}
		data = newData;
	}

	public T getNext() {
		if (filled == 0) {
			throw new NoSuchElementException("Buffer is empty");
		}
		int position = rnd.nextInt(filled);
		@SuppressWarnings("unchecked")
		T out = (T) data[position];
		data[position] = data[--filled];
		data[filled] = null;
		return out;
	}

	public boolean isEmpty() {
		return filled == 0;
	}

	public void put(T element) {
		if (filled == data.length) {
			expand();
		}
		data[filled++] = element;
	}

}
