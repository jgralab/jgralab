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
package de.uni_koblenz.jgralab.algolib.algorithms.search.visitors;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

public interface DFSVisitor extends SearchVisitor {

	/**
	 * This method visits a vertex when the algorithm "leaves" it. (After the
	 * recursive call of the search)
	 * 
	 * @param v
	 *            the vertex that is currently visited
	 */
	public void leaveVertex(Vertex v);

	/**
	 * This method visits a tree edge when the algorithm "leaves" it. (After the
	 * recursive call of the search)
	 * 
	 * @param e
	 *            the tree edge that is currently visited
	 */
	public void leaveTreeEdge(Edge e);

	/**
	 * This method visits an edge if it has been classified as forward arc.
	 * 
	 * @param e
	 *            the forward arc that is currently visited
	 */
	public void visitForwardArc(Edge e);

	/**
	 * This method visits an edge if it has been classified as backward arc.
	 * 
	 * @param e
	 *            the backward arc that is currently visited
	 */
	public void visitBackwardArc(Edge e);

	/**
	 * This method visits an edge if it has been classified as crosslink.
	 * 
	 * @param e
	 *            the crosslink that is currently visited
	 */
	public void visitCrosslink(Edge e);
}
