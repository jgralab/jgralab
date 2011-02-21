/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 * 
 * Additional permission under GNU GPL version 3 section 7
 * 
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
package de.uni_koblenz.jgralab.algolib.util;

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
