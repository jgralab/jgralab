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

import java.util.HashMap;

import de.uni_koblenz.jgralab.impl.ReversedEdgeBaseImpl;

/**
 * This class can be used to "colorize" graphs, edges and vertices.  If a algorithm only needs
 * to distinguish between "marked" and "not marked", a look at the class <code>BooleanGraphMarker</code> may be reasonable. 
 * If a specific kind of marking is used, it may be reasonalbe to extends this GraphMarker. 
 * A example how that could be done is located in the tutorial in the class <code>DijkstraVertexMarker</code>. 
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> 
 * November 2006
 *
 */
public class GraphMarker<T> {

		/*
		 * Stores the mapping between Graph,Edge or Vertex and the attribute 
		 */
		private HashMap<AttributedElement, T> tempAttributeMap;
		
		/**
		 * Creates a new GraphMarker 
		 */
		public GraphMarker() {
			tempAttributeMap = new HashMap<AttributedElement, T>();
		}
		
		/**
		 * returns the object that marks the given Graph, Edge or Vertex in this marking.
		 * @param elem the element to get the marking for
		 * @return the object that marks the givne element or <code>null</code> if the given element is not marked in this marking. 
		 */
		public T getMark(AttributedElement elem) {
			if (elem == null)
				return null;
			if (elem instanceof ReversedEdgeBaseImpl) {
				elem = ((ReversedEdgeBaseImpl)elem).getNormalEdge();
			}
			return tempAttributeMap.get(elem);
		}	

		/**
		 * marks the given element with the given value
		 * @param elem the element (Graph, Vertex or Edge) to mark
		 * @param value the object that should be used as marking
		 * @return true on success, false if the given element already contains a marking 
		 */
		public T mark(AttributedElement elem, T value) {
			if (elem instanceof ReversedEdgeBaseImpl) {
				elem = ((ReversedEdgeBaseImpl)elem).getNormalEdge();
			}
			return tempAttributeMap.put(elem, value);
		}

	}