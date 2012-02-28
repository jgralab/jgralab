/*
* JGraLab - The Java Graph Laboratory
*
* Copyright (C) 2006-2012 Institute for Software Technology
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
package de.uni_koblenz.jgralab;

/**
 * Instances of this interface are used for defining subgraphs. A traversal
 * context is passed to a graph for restricting it to a subgraph. All traversal
 * related methods are now running on the subgraph instead of the whole graph.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public interface TraversalContext {

	/**
	 * Checks if the given vertex is included in the subgraph defined by this TC
	 * and the graph this TC is passed to. This method has to ensure that it
	 * returns false if the vertex is not in vSeq of the graph.
	 * 
	 * @param v
	 *            the vertex to check
	 * @return true if the given vertex is part of the subgraph defined by this
	 *         TC and the graph
	 */
	public boolean containsVertex(Vertex v);

	/**
	 * Checks if the given edge is included in the subgraph defined by this TC
	 * and the graph this TC is passed to. This method has to ensure that it
	 * returns false if the edge is not in eSeq of the graph.
	 * 
	 * @param e
	 *            the edge to check
	 * @return true if the given edge is part of the subgraph defined by this TC
	 *         and the graph
	 */
	public boolean containsEdge(Edge e);

}
