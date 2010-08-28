package de.uni_koblenz.jgralab.algolib.buffers;

import java.util.Comparator; //import java.util.LinkedList;
import java.util.Queue;

public class PriorityQueue<T> {

	private static class ValuePair<T> {
		public T element;
		public double value;

		public ValuePair(T element, double value) {
			super();
			set(element, value);
		}

		public ValuePair<T> set(T element, double value) {
			this.element = element;
			this.value = value;
			return this;
		}

	}

	// private Buffer<ValuePair<T>> queue;
	private Queue<ValuePair<T>> queue;
	private Comparator<ValuePair<T>> comparator;
	private int added;

	// private Queue<ValuePair<T>> reusableElements;

	public PriorityQueue() {
		comparator = new Comparator<ValuePair<T>>() {
			@Override
			public int compare(ValuePair<T> o1, ValuePair<T> o2) {
				return Double.compare(o1.value, o2.value);
			}
		};

		queue = new java.util.PriorityQueue<ValuePair<T>>(31, comparator);

		// reusableElements = new LinkedList<ValuePair<T>>();
	}

	public T getNext() {
		// ValuePair<T> next = queue.poll();
		// T element = next.element;
		// reusableElements.add(next);
		return queue.poll().element;
	}

	public boolean isEmpty() {
		return queue.isEmpty();
	}

	public void put(T element, double value) {
		// ValuePair<T> newElement = reusableElements.isEmpty() ? new
		// ValuePair<T>(
		// element, value)
		// : reusableElements.poll().set(element, value);
		ValuePair<T> newElement = new ValuePair<T>(element, value);
		queue.add(newElement);
		added++;
	}

	public int getAddedCount() {
		// return reusableElements.size();
		return added;
	}

	public PriorityQueue<T> clear() {
		queue.clear();
		// reusableElements.clear();
		return this;
	}

}
