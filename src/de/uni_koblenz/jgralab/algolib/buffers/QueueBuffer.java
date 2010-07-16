package de.uni_koblenz.jgralab.algolib.buffers;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

public class QueueBuffer<T> implements Buffer<T> {

	private Queue<T> queue;

	public QueueBuffer() {
		queue = new LinkedList<T>();
	}

	@Override
	public T getNext() {
		T out = queue.poll();
		if (out != null) {
			return out;
		}
		throw new NoSuchElementException("The queue was empty.");
	}

	@Override
	public boolean isEmpty() {
		return queue.isEmpty();
	}

	@Override
	public void put(T element) {
		queue.add(element);
	}

}
