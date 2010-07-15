package de.uni_koblenz.jgralab.algolib.buffers;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;

public class PriorityQueueBuffer<T> implements Buffer<T> {

	private PriorityQueue<T> queue;

	public PriorityQueueBuffer(Comparator<T> comparator) {
		queue = new PriorityQueue<T>(31, comparator);
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
