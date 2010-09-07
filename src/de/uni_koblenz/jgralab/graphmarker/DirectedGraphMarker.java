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

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;

/**
 * Marks directed graphs with arbitrary objects. In contrast to the marking
 * mechanism implemented by {@link GraphMarker}, in this case the edges are
 * marked taking their direction into account. That means, an incoming edge and
 * the same edge in the outgoing direction are marked independently
 * 
 * @author ist@uni-koblenz.de
 * 
 * @param <O>
 */
public class DirectedGraphMarker<O> extends
		MapGraphMarker<AttributedElement, O> {

	/**
	 * Creates a new GraphMarker
	 */
	public DirectedGraphMarker(Graph g) {
		super(g);
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
	@Override
	public O getMark(AttributedElement elem) {
		if (elem == null) {
			return null;
		}
		assert ((elem instanceof GraphElement && ((GraphElement) elem)
				.getGraph() == graph) || elem == graph);
		return tempAttributeMap.get(elem);
	}

	/**
	 * marks the given element with the given value
	 * 
	 * @param elem
	 *            the element (Graph, Vertex or Edge) to mark
	 * @param value
	 *            the object that should be used as marking
	 * @return true on success, false if the given element already contains a
	 *         marking
	 */
	@Override
	public O mark(AttributedElement elem, O value) {
		assert ((elem instanceof GraphElement && ((GraphElement) elem)
				.getGraph() == graph) || elem == graph);

		return tempAttributeMap.put(elem, value);

	}

	@Override
	public void edgeDeleted(Edge e) {
		tempAttributeMap.remove(e);
		tempAttributeMap.remove(e.getReversedEdge());
	}

	@Override
	public boolean isMarked(AttributedElement elem) {
		assert ((elem instanceof GraphElement && ((GraphElement) elem)
				.getGraph() == graph) || elem == graph);
		return tempAttributeMap.containsKey(elem);
	}

	@Override
	public boolean removeMark(AttributedElement elem) {
		assert ((elem instanceof GraphElement && ((GraphElement) elem)
				.getGraph() == graph) || elem == graph);
		return tempAttributeMap.remove(elem) != null;
	}

	@Override
	public void vertexDeleted(Vertex v) {
		tempAttributeMap.remove(v);
	}

}
