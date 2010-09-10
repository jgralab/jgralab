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

import java.util.HashMap;
import java.util.Iterator;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.algolib.functions.Function;
import de.uni_koblenz.jgralab.algolib.functions.entries.FunctionEntry;
import de.uni_koblenz.jgralab.impl.ReversedEdgeBaseImpl;

/**
 * This class can be used to "colorize" graphs, edges and vertices. If a
 * algorithm only needs to distinguish between "marked" and "not marked", a look
 * at the class <code>BooleanGraphMarker</code> may be reasonable. If a specific
 * kind of marking is used, it may be reasonalbe to extends this GraphMarker. A
 * example how that could be done is located in the tutorial in the class
 * <code>DijkstraVertexMarker</code>.
 * 
 * This marker class allows a stricter limitation to specific
 * <code>AttributedElement</code>s. For example a vertex function can be
 * realized by<br/>
 * <code>GenericGraphMarker vertexFunction = new GenericGraphMarker<Vertex,Object>();</code>
 * <br/>
 * <br/>
 * 
 *Edge functions can be created in analogy to vertex functions.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public abstract class MapGraphMarker<T extends AttributedElement, O> extends
		AbstractGraphMarker<T> implements Function<T, O> {

	/**
	 * Stores the mapping between Graph, Edge or Vertex and the attribute
	 */
	protected HashMap<T, O> tempAttributeMap;

	/**
	 * Creates a new GraphMarker
	 */
	protected MapGraphMarker(Graph g) {
		super(g);
		tempAttributeMap = new HashMap<T, O>();
	}

	/**
	 * returns the object that marks the given Graph, Edge or Vertex in this
	 * marking.
	 * 
	 * @param elem
	 *            the element to get the marking for
	 * @return the object that marks the given element or <code>null</code> if
	 *         the given element is not marked in this marking.
	 */
	public O getMark(T elem) {
		if (elem == null) {
			return null;
		}
		assert ((elem instanceof GraphElement && ((GraphElement) elem)
				.getGraph() == graph) || elem == graph);
		if (elem instanceof ReversedEdgeBaseImpl) {
			elem = getNormalEdge(elem);
		}
		return tempAttributeMap.get(elem);
	}

	/**
	 * marks the given element with the given value
	 * 
	 * @param elem
	 *            the element (Graph, Vertex or Edge) to mark
	 * @param value
	 *            the object that should be used as marking
	 * @return The previous element the given graph element has been marked
	 *         with, <code>null</code> if the given element has not been marked.
	 */
	public O mark(T elem, O value) {
		assert ((elem instanceof GraphElement && ((GraphElement) elem)
				.getGraph() == graph) || elem == graph);

		if (elem instanceof ReversedEdgeBaseImpl) {
			elem = getNormalEdge(elem);
		}

		return tempAttributeMap.put(elem, value);

	}

	@SuppressWarnings("unchecked")
	private T getNormalEdge(T elem) {
		elem = (T) ((ReversedEdgeBaseImpl) elem).getNormalEdge();
		return elem;
	}

	/**
	 * Returns the number of marked elements in this GraphMarker.
	 * 
	 * @return The number of marked elements.
	 */
	@Override
	public int size() {
		return tempAttributeMap.size();
	}

	/**
	 * Returns <code>true</code> if nothing is marked by this GraphMarker.
	 * 
	 * @return <code>true</code> if no graph element is marked by this
	 *         GraphMarker.
	 */
	@Override
	public boolean isEmpty() {
		return tempAttributeMap.isEmpty();
	}

	/**
	 * Clears this GraphMarker such that no element is marked.
	 */
	@Override
	public void clear() {
		tempAttributeMap.clear();
	}

	/**
	 * @return An {@link Iterable} of all {@link T}s in the {@link Graph} that
	 *         are marked by this marker.
	 */
	@Override
	public Iterable<T> getMarkedElements() {
		return tempAttributeMap.keySet();
	}

	@Override
	public boolean isMarked(T elem) {
		assert ((elem instanceof GraphElement && ((GraphElement) elem)
				.getGraph() == graph) || elem == graph);
		if (elem instanceof ReversedEdgeBaseImpl) {
			elem = getNormalEdge(elem);
		}
		return tempAttributeMap.containsKey(elem);
	}

	@Override
	public boolean removeMark(T elem) {
		assert ((elem instanceof GraphElement && ((GraphElement) elem)
				.getGraph() == graph) || elem == graph);
		if (elem instanceof ReversedEdgeBaseImpl) {
			elem = getNormalEdge(elem);
		}
		return tempAttributeMap.remove(elem) != null;
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
