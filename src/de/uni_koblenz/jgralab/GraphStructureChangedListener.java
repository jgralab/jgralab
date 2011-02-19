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
package de.uni_koblenz.jgralab;

/**
 * All implementations of <code>GraphStructureListener</code> that are
 * registered at the graph, are notified about changes in the structure of the
 * graph. These changes are:
 *<ul>
 *<li>adding a vertex</li>
 *<li>deleting a vertex</li>
 *<li>adding an edge</li>
 *<li>deleting an edge</li>
 *<li>increasing <code>maxVCount</code>
 *<li>increasing <code>maxECount</code></li>
 *</ul>
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public interface GraphStructureChangedListener {

	/**
	 * This method is called after the vertex <code>v</code> has been added to
	 * the graph.
	 * 
	 * @param v
	 *            the vertex that has been added.
	 */
	public void vertexAdded(Vertex v);

	/**
	 * This method is called before the vertex <code>v</code> is deleted.
	 * 
	 * @param v
	 *            the vertex that is about to be deleted.
	 */
	public void vertexDeleted(Vertex v);

	/**
	 * This method is called after the Edge <code>e</code> has been added to the
	 * graph.
	 * 
	 * @param e
	 *            the edge that has been added.
	 */
	public void edgeAdded(Edge e);

	/**
	 * This method is called before the edge <code>e</code> is deleted.
	 * 
	 * @param e
	 *            the edge that is about to be deleted.
	 */
	public void edgeDeleted(Edge e);

	/**
	 * This method is called after the maximum vertex count has been increased
	 * to <code>newValue</code>.
	 * 
	 * @param newValue
	 *            the new value of <code>maxVCount</code>.
	 */
	public void maxVertexCountIncreased(int newValue);

	/**
	 * This method is called after the maximum edge count has been increased to
	 * <code>newValue</code>.
	 * 
	 * @param newValue
	 *            the new value of <code>maxECount</code>.
	 */
	public void maxEdgeCountIncreased(int newValue);
}
