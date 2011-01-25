/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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
package de.uni_koblenz.jgralab.graphmarker;

import java.util.Iterator;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;
import de.uni_koblenz.jgralab.algolib.functions.entries.IntFunctionEntry;

public abstract class IntegerGraphMarker<T extends GraphElement> extends
		AbstractGraphMarker<T> implements IntFunction<T> {

	private static final int DEFAULT_UNMARKED_VALUE = Integer.MIN_VALUE;

	protected int[] temporaryAttributes;
	protected int marked;
	protected int unmarkedValue;
	protected long version;

	protected IntegerGraphMarker(Graph graph, int size) {
		super(graph);
		unmarkedValue = DEFAULT_UNMARKED_VALUE;
		temporaryAttributes = createNewArray(size);
		marked = 0;
	}

	private int[] createNewArray(int size) {
		int[] newArray = new int[size];
		for (int i = 0; i < size; i++) {
			newArray[i] = unmarkedValue;
		}
		return newArray;
	}

	@Override
	public void clear() {
		for (int i = 0; i < temporaryAttributes.length; i++) {
			temporaryAttributes[i] = unmarkedValue;
		}
		marked = 0;
	}

	@Override
	public boolean isEmpty() {
		return marked == 0;
	}

	@Override
	public boolean isMarked(T graphElement) {
		assert (graphElement.getGraph() == graph);
		assert (graphElement.getId() <= (graphElement instanceof Vertex ? graph
				.getMaxVCount() : graph.getMaxECount()));
		return temporaryAttributes[graphElement.getId()] != unmarkedValue;
	}

	/**
	 * marks the given element with the given value
	 * 
	 * @param elem
	 *            the graph element to mark
	 * @param value
	 *            the object that should be used as marking
	 * @return The previous element the given graph element has been marked
	 *         with, <code>null</code> if the given element has not been marked.
	 */
	public int mark(T graphElement, int value) {
		assert (graphElement.getGraph() == graph);
		assert (graphElement.getId() <= (graphElement instanceof Vertex ? graph
				.getMaxVCount() : graph.getMaxECount()));
		int out = temporaryAttributes[graphElement.getId()];
		temporaryAttributes[graphElement.getId()] = value;
		marked += 1;
		version++;
		return out;
	}

	public int getMark(T graphElement) {
		assert (graphElement.getGraph() == graph);
		assert (graphElement.getId() <= (graphElement instanceof Vertex ? graph
				.getMaxVCount() : graph.getMaxECount()));
		int out = temporaryAttributes[graphElement.getId()];
		return out;
	}

	@Override
	public boolean removeMark(T graphElement) {
		assert (graphElement.getGraph() == graph);
		assert (graphElement.getId() <= (graphElement instanceof Vertex ? graph
				.getMaxVCount() : graph.getMaxECount()));
		if (temporaryAttributes[graphElement.getId()] == unmarkedValue) {
			return false;
		}
		temporaryAttributes[graphElement.getId()] = unmarkedValue;
		marked -= 1;
		version++;
		return true;
	}

	@Override
	public int size() {
		return marked;
	}

	public int maxSize() {
		return temporaryAttributes.length - 1;
	}

	protected void expand(int newSize) {
		assert (newSize > temporaryAttributes.length);
		int[] newTemporaryAttributes = createNewArray(newSize);
		System.arraycopy(temporaryAttributes, 0, newTemporaryAttributes, 0,
				temporaryAttributes.length);
		temporaryAttributes = newTemporaryAttributes;
	}

	public int getUnmarkedValue() {
		return unmarkedValue;
	}

	public void setUnmarkedValue(int newUnmarkedValue) {
		if (newUnmarkedValue != this.unmarkedValue) {
			for (int i = 0; i < temporaryAttributes.length; i++) {
				// keep track of implicitly unmarked values
				if (temporaryAttributes[i] == newUnmarkedValue) {
					marked -= 1;
				}
				// set all unmarked elements to new value
				if (temporaryAttributes[i] == this.unmarkedValue) {
					temporaryAttributes[i] = newUnmarkedValue;
				}

			}
			this.unmarkedValue = newUnmarkedValue;
		}
	}

	@Override
	public int get(T parameter) {
		return getMark(parameter);
	}

	@Override
	public boolean isDefined(T parameter) {
		return isMarked(parameter);
	}

	@Override
	public void set(T parameter, int value) {
		mark(parameter, value);
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append("[");
		Iterator<T> iter = getMarkedElements().iterator();
		if (iter.hasNext()) {
			T next = iter.next();
			out.append(next);
			out.append(" -> ");
			out.append(get(next));
			while (iter.hasNext()) {
				out.append(",\n");
				next = iter.next();
				out.append(next);
				out.append(" -> ");
				out.append(get(next));
			}
		}
		out.append("]");
		return out.toString();
	}

	@Override
	public Iterator<IntFunctionEntry<T>> iterator() {
		final Iterator<T> markedElements = getMarkedElements().iterator();
		return new Iterator<IntFunctionEntry<T>>() {

			@Override
			public boolean hasNext() {
				return markedElements.hasNext();
			}

			@Override
			public IntFunctionEntry<T> next() {
				T currentElement = markedElements.next();
				return new IntFunctionEntry<T>(currentElement,
						get(currentElement));
			}

			@Override
			public void remove() {
				markedElements.remove();
			}
		};
	}

	@Override
	public Iterable<T> getDomainElements() {
		return getMarkedElements();
	}

}
