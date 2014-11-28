/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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

import java.util.HashSet;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.ReversedEdgeBaseImpl;

/**
 * This class can be used to "colorize" graphs, it supports only two "colors",
 * that are "marked" or "not marked". If you need to mark graphs or
 * graphelements with more specific "colors", have a look at the class
 * <code>GraphMarker</code>
 * 
 * @author ist@uni-koblenz.de
 */
public class BooleanGraphMarker extends AbstractBooleanGraphMarker {

	private final HashSet<GraphElement<?, ?>> markedElements;

	/**
	 * creates a new boolean graph marker
	 * 
	 */
	public BooleanGraphMarker(Graph g) {
		super(g);
		markedElements = new HashSet<>();
	}

	/**
	 * Checks whether this marker is a marking of the given GraphElement
	 * 
	 * @param elem
	 *            the Vertex or Edge to check for a marking
	 * @return true if this GraphMarker marks the given element, false otherwise
	 */
	@Override
	public final boolean isMarked(GraphElement<?, ?> elem) {
		assert elem.getGraph() == graph;
		if (elem instanceof ReversedEdgeBaseImpl) {
			elem = ((ReversedEdgeBaseImpl) elem).getNormalEdge();
		}
		return markedElements.contains(elem);
	}

	/**
	 * Adds a marking to the given GraphElement.
	 * 
	 * @param elem
	 *            the Vertex or Edge to mark
	 * @return true if the element has been marked successful, false if this
	 *         element is already marked by this GraphMarker
	 */
	@Override
	public final boolean mark(GraphElement<?, ?> elem) {
		assert elem.getGraph() == graph;
		if (elem instanceof ReversedEdgeBaseImpl) {
			elem = ((ReversedEdgeBaseImpl) elem).getNormalEdge();
		}

		return markedElements.add(elem);

	}

	/**
	 * Remove the mark from the given element.
	 * 
	 * @param elem
	 *            an {@link GraphElement}
	 * @return <code>true</code> it the given element was marked,
	 *         <code>false</code> otherwise
	 */
	@Override
	public final boolean removeMark(GraphElement<?, ?> elem) {
		assert elem.getGraph() == graph;
		if (elem instanceof ReversedEdgeBaseImpl) {
			elem = ((ReversedEdgeBaseImpl) elem).getNormalEdge();
		}
		return markedElements.remove(elem);
	}

	/**
	 * Return a set of all marked {@link GraphElement}s.
	 * 
	 * @return the markedElements
	 */
	@Override
	public Iterable<GraphElement<?, ?>> getMarkedElements() {
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

	@Override
	public void edgeDeleted(Edge e) {
		markedElements.remove(e);
	}

	@Override
	public void vertexDeleted(Vertex v) {
		markedElements.remove(v);
	}

}
