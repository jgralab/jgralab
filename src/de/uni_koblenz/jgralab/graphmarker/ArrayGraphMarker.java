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
package de.uni_koblenz.jgralab.graphmarker;

import java.util.Iterator;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.Function;
import de.uni_koblenz.jgralab.algolib.functions.entries.FunctionEntry;

/**
 * This class is the abstract superclass of generic array graph markers.
 * 
 * @author ist@uni-koblenz.de
 * 
 * @param <T>
 */
public abstract class ArrayGraphMarker<T extends GraphElement, O> extends
		AbstractGraphMarker<T> implements Function<T, O> {

	/**
	 * The array of temporary attributes.
	 */
	protected Object[] temporaryAttributes;
	protected int marked;
	protected long version;

	protected ArrayGraphMarker(Graph graph, int size) {
		super(graph);
		temporaryAttributes = new Object[size];
		marked = 0;
	}

	@Override
	public void clear() {
		for (int i = 0; i < temporaryAttributes.length; i++) {
			temporaryAttributes[i] = null;
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
		return temporaryAttributes[graphElement.getId()] != null;
	}

	public O getMark(T graphElement) {
		assert (graphElement.getGraph() == graph);
		assert (graphElement.getId() <= (graphElement instanceof Vertex ? graph
				.getMaxVCount() : graph.getMaxECount()));
		@SuppressWarnings("unchecked")
		O out = (O) temporaryAttributes[graphElement.getId()];
		return out;
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
	public O mark(T graphElement, O value) {
		assert (graphElement.getGraph() == graph);
		assert (graphElement.getId() <= (graphElement instanceof Vertex ? graph
				.getMaxVCount() : graph.getMaxECount()));
		@SuppressWarnings("unchecked")
		O out = (O) temporaryAttributes[graphElement.getId()];
		temporaryAttributes[graphElement.getId()] = value;
		marked += 1;
		version++;
		return out;
	}

	@Override
	public int size() {
		return marked;
	}

	@Override
	public boolean removeMark(T graphElement) {
		assert (graphElement.getGraph() == graph);
		assert (graphElement.getId() <= (graphElement instanceof Vertex ? graph
				.getMaxVCount() : graph.getMaxECount()));
		if (temporaryAttributes[graphElement.getId()] == null) {
			return false;
		}
		temporaryAttributes[graphElement.getId()] = null;
		marked -= 1;
		version++;
		return true;
	}

	protected void expand(int newSize) {
		assert (newSize > temporaryAttributes.length);
		Object[] newTemporaryAttributes = new Object[newSize];
		System.arraycopy(temporaryAttributes, 0, newTemporaryAttributes, 0,
				temporaryAttributes.length);
		temporaryAttributes = newTemporaryAttributes;
	}

	public int maxSize() {
		return temporaryAttributes.length - 1;
	}

	@Override
	public O get(T parameter) {
		return getMark(parameter);
	}

	@Override
	public boolean isDefined(T parameter) {
		return isMarked(parameter);
	}

	@Override
	public void set(T parameter, O value) {
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
	public Iterable<T> getDomainElements() {
		return getMarkedElements();
	}

	@Override
	public Iterator<FunctionEntry<T, O>> iterator() {
		final Iterator<T> markedElements = getMarkedElements().iterator();
		return new Iterator<FunctionEntry<T,O>>() {

			@Override
			public boolean hasNext() {
				return markedElements.hasNext();
			}

			@Override
			public FunctionEntry<T, O> next() {
				T currentElement = markedElements.next();
				return new FunctionEntry<T, O>(currentElement, get(currentElement));
			}

			@Override
			public void remove() {
				markedElements.remove();
			}
			
		};
	}

}
