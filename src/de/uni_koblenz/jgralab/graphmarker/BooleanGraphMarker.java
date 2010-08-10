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

import java.util.HashSet;
import java.util.Iterator;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.entries.BooleanFunctionEntry;
import de.uni_koblenz.jgralab.impl.ReversedEdgeBaseImpl;

/**
 * This class can be used to "colorize" graphs, it supports only two "colors",
 * that are "marked" or "not marked". If you need to mark graphs or
 * graphelements with more specific "colors", have a look at the class
 * <code>GraphMarker</code>
 * 
 * @author ist@uni-koblenz.de
 */
public class BooleanGraphMarker extends AbstractGraphMarker<AttributedElement>
		implements BooleanFunction<AttributedElement> {

	private final HashSet<AttributedElement> markedElements;

	/**
	 * creates a new boolean graph marker
	 * 
	 */
	public BooleanGraphMarker(Graph g) {
		super(g);
		markedElements = new HashSet<AttributedElement>();
	}

	/**
	 * Checks whether this marker is a marking of the given Graph or
	 * GraphElement
	 * 
	 * @param elem
	 *            the Graph, Vertex or Edge to check for a marking
	 * @return true if this GraphMarker marks the given element, false otherwise
	 */
	@Override
	public final boolean isMarked(AttributedElement elem) {
		assert ((elem instanceof GraphElement && ((GraphElement) elem)
				.getGraph() == graph) || elem == graph);
		if (elem instanceof ReversedEdgeBaseImpl) {
			elem = ((ReversedEdgeBaseImpl) elem).getNormalEdge();
		}
		return markedElements.contains(elem);
	}

	/**
	 * Adds a marking to the given Graph or GraphElement.
	 * 
	 * @param elem
	 *            the Graph, Vertex or Edge to mark
	 * @return true if the element has been marked successfull, false if this
	 *         element is already marked by this GraphMarker
	 */
	public final boolean mark(AttributedElement elem) {
		assert ((elem instanceof GraphElement && ((GraphElement) elem)
				.getGraph() == graph) || elem == graph);
		if (elem instanceof ReversedEdgeBaseImpl) {
			elem = ((ReversedEdgeBaseImpl) elem).getNormalEdge();
		}

		return markedElements.add(elem);

	}

	/**
	 * Remove the mark from the given element.
	 * 
	 * @param elem
	 *            an {@link AttributedElement}
	 * @return <code>true</code> it the given element was marked,
	 *         <code>false</code> otherwise
	 */
	@Override
	public final boolean removeMark(AttributedElement elem) {
		assert ((elem instanceof GraphElement && ((GraphElement) elem)
				.getGraph() == graph) || elem == graph);
		if (elem instanceof ReversedEdgeBaseImpl) {
			elem = ((ReversedEdgeBaseImpl) elem).getNormalEdge();
		}
		return markedElements.remove(elem);
	}

	/**
	 * Return a set of all marked {@link AttributedElement}s.
	 * 
	 * @return the markedElements
	 */
	@Override
	public Iterable<AttributedElement> getMarkedElements() {
		return markedElements;
	}

	/**
	 * Returns the number of marked elements in this GraphMarker.
	 * 
	 * @return The number of marked elements.
	 */
	@Override
	public int size() {
		return markedElements.size();
	}

	/**
	 * Returns <code>true</code> if nothing is marked by this GraphMarker.
	 * 
	 * @return <code>true</code> if no graph element is marked by this
	 *         GraphMarker.
	 */
	@Override
	public boolean isEmpty() {
		return markedElements.isEmpty();
	}

	/**
	 * Clears this GraphMarker such that no element is marked.
	 */
	@Override
	public void clear() {
		markedElements.clear();
	}

	/**
	 * Returns the Graph of this GraphMarker.
	 * 
	 * @return the Graph of this GraphMarker.
	 */
	@Override
	public Graph getGraph() {
		return graph;
	}

	@Override
	public void edgeDeleted(Edge e) {
		markedElements.remove(e);
	}

	@Override
	public void maxEdgeCountIncreased(int newValue) {
		// do nothing
	}

	@Override
	public void maxVertexCountIncreased(int newValue) {
		// do nothing
	}

	@Override
	public void vertexDeleted(Vertex v) {
		markedElements.remove(v);
	}

	@Override
	public boolean get(AttributedElement parameter) {
		return isMarked(parameter);
	}

	@Override
	public boolean isDefined(AttributedElement parameter) {
		return true;
	}

	@Override
	public void set(AttributedElement parameter, boolean value) {
		if (value) {
			mark(parameter);
		} else {
			removeMark(parameter);
		}
	}

	@Override
	public Iterator<BooleanFunctionEntry<AttributedElement>> iterator() {
		final Iterator<AttributedElement> markedElements = getMarkedElements()
				.iterator();
		return new Iterator<BooleanFunctionEntry<AttributedElement>>() {

			@Override
			public boolean hasNext() {
				return markedElements.hasNext();
			}

			@Override
			public BooleanFunctionEntry<AttributedElement> next() {
				AttributedElement currentElement = markedElements.next();
				return new BooleanFunctionEntry<AttributedElement>(currentElement,
						get(currentElement));
			}

			@Override
			public void remove() {
				markedElements.remove();
			}

		};
	}

	@Override
	public Iterable<AttributedElement> getDomainElements() {
		return getMarkedElements();
	}

}