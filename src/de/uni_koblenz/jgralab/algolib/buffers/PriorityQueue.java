package de.uni_koblenz.jgralab.algolib.buffers;

import java.util.Comparator;

public class PriorityQueue<T> {

	private static class ValuePair<T> {
		public T element;
		public double value;

		public ValuePair(T element, double value) {
			super();
			this.element = element;
			this.value = value;
		}
	}

	private Buffer<ValuePair<T>> queue;
	private Comparator<ValuePair<T>> comparator;

	public PriorityQueue(boolean useJavaPQueue) {
		comparator = new Comparator<ValuePair<T>>() {
			@Override
			public int compare(ValuePair<T> o1, ValuePair<T> o2) {
				return Double.compare(o1.value, o2.value);
			}
		};

		if (useJavaPQueue) {
			queue = new PriorityQueueBuffer<ValuePair<T>>(comparator);
		} else {
			queue = new UnsortedPriorityQueueBuffer<ValuePair<T>>(31,
					comparator);
		}
	}

	public T getNext() {
		return queue.getNext().element;
	}

	public boolean isEmpty() {
		return queue.isEmpty();
	}

	public void put(T element, double value) {
		queue.put(new ValuePair<T>(element, value));
	}

}
