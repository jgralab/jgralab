/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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
import de.uni_koblenz.jgralab.algolib.visitors.GraphVisitor;

/**
 * This visitor allows visiting vertices and edges during the run of an
 * arbitrary search algorithm.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface SearchVisitor extends GraphVisitor {

	/**
	 * Visits one vertex that is the root of a search tree. Vertices should only
	 * be visited at most once by this method during a run of an algorithm.
	 * <code>v</code> should also be visited by <code>visitVertex</code>.
	 * 
	 * @param v
	 *            the root vertex of a search tree, which is currently visited
	 */
	public void visitRoot(Vertex v) throws AlgorithmTerminatedException;

	/**
	 * Visits a tree edge in the search tree. An edge is either a tree edge or a
	 * frond. So it is either visited by this method or by
	 * <code>visitFrond</code>.
	 * 
	 * @param e
	 *            the tree edge that is currently visited
	 */
	public void visitTreeEdge(Edge e) throws AlgorithmTerminatedException;

	/**
	 * Visits a frond in the search tree. An edge is either a frond or a tree
	 * edge. So it is either visited by this method or by
	 * <code>visitTreeEdge</code> (which is responsible for visiting tree
	 * edges).
	 * 
	 * @param e
	 *            the frond that is currently visited
	 */
	public void visitFrond(Edge e) throws AlgorithmTerminatedException;

}
