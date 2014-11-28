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
package de.uni_koblenz.jgralab.algolib.algorithms.search.visitors;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;

public interface DFSVisitor extends SearchVisitor {

	/**
	 * This method visits a vertex when the algorithm "leaves" it. (After the
	 * recursive call of the search)
	 * 
	 * @param v
	 *            the vertex that is currently visited
	 */
	public void leaveVertex(Vertex v) throws AlgorithmTerminatedException;

	/**
	 * This method visits a tree edge when the algorithm "leaves" it. (After the
	 * recursive call of the search)
	 * 
	 * @param e
	 *            the tree edge that is currently visited
	 */
	public void leaveTreeEdge(Edge e) throws AlgorithmTerminatedException;

	/**
	 * This method visits an edge if it has been classified as forward arc.
	 * 
	 * @param e
	 *            the forward arc that is currently visited
	 */
	public void visitForwardArc(Edge e) throws AlgorithmTerminatedException;

	/**
	 * This method visits an edge if it has been classified as backward arc.
	 * 
	 * @param e
	 *            the backward arc that is currently visited
	 */
	public void visitBackwardArc(Edge e) throws AlgorithmTerminatedException;

	/**
	 * This method visits an edge if it has been classified as crosslink.
	 * 
	 * @param e
	 *            the crosslink that is currently visited
	 */
	public void visitCrosslink(Edge e) throws AlgorithmTerminatedException;
}
