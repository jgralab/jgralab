/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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

package de.uni_koblenz.jgralab;

import java.util.HashSet;

import de.uni_koblenz.jgralab.impl.EdgeImpl;
import de.uni_koblenz.jgralab.impl.ReversedEdgeImpl;
import de.uni_koblenz.jgralab.impl.VertexImpl;

/**
 * This class can be used to "colorize" graphs, it supports only two "colors",
 * that are "marked" or "not marked". If one need to mark graphs or
 * graphelements with more specific "colors", have a look at the class
 * <code>GraphMarker</code>
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> November 2006
 * 
 */
public class BooleanGraphMarker {

	private HashSet<AttributedElement> markedElements;

	private Graph graph;

	/**
	 * creates a new boolean graph marker
	 * 
	 */
	public BooleanGraphMarker(Graph g) {
		graph = g;
		markedElements = new HashSet<AttributedElement>();
	}

	/**
	 * Adds a marking to the given Graph or GraphElement.
	 * 
	 * @param elem
	 *            the Graph, Vertex or Edge to mark
	 * @return true if the element has been marked successfull, false if this
	 *         element is already marked by this GraphMarker
	 */
	public boolean isMarked(AttributedElement elem) {
		if (elem instanceof ReversedEdgeImpl) {
			elem = ((ReversedEdgeImpl) elem).getNormalEdge();
		}
		return markedElements.contains(elem);
	}

	/**
	 * Checks whether this marker is a marking of the given Graph or GraphElement
	 * 
	 * @param elem
	 *            the Graph, Vertex or Edge to check for a marking
	 * @return true if this GraphMarker marks the given element, false otherwise
	 */
	public boolean mark(AttributedElement elem) {
		if ((elem instanceof Vertex && ((Vertex) elem).getGraph() == graph)
				|| (elem instanceof Edge && ((Edge) elem).getGraph() == graph)
				|| elem == graph) {
			return markedElements.add(elem);
		}
		throw new GraphException("Can't mark the element " + elem
				+ ", because it belongs to a different graph.");
	}

	/**
	 * Returns the number of marked elements in this GraphMarker.
	 * 
	 * @return The number of marked elements.
	 */
	public int size() {
		return markedElements.size();
	}

	/**
	 * Clears this GraphMarker such that no element is marked.
	 */
	public void clear() {
		markedElements.clear();
	}

	/**
	 * Returns the Graph of this GraphMarker.
	 * 
	 * @return the Graph of this GraphMarker.
	 */
	public Graph getGraph() {
		return graph;
	}
}