/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 * 
 *               ist@uni-koblenz.de
 * 
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package de.uni_koblenz.jgralab.graphmarker;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;

/**
 * This class is the abstract superclass of generic array graph markers.
 * 
 * @author ist@uni-koblenz.de
 * 
 * @param <T>
 */
public abstract class ArrayGraphMarker<T extends GraphElement, O> extends
		AbstractGraphMarker<T> {

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
		// for (int i = 0; i < temporaryAttributes.length; i++) {
		// newTemporaryAttributes[i] = temporaryAttributes[i];
		// }
		temporaryAttributes = newTemporaryAttributes;
	}

	public int maxSize() {
		return temporaryAttributes.length - 1;
	}

}
