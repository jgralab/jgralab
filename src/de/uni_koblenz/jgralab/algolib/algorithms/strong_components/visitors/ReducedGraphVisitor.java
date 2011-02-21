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
package de.uni_koblenz.jgralab.algolib.algorithms.strong_components.visitors;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

/**
 * This visitor visits all graph elements of a graph that are relevant for
 * computing a reduced graph. It can be used during the computation of strong
 * components for performing tasks whenever a representative vertex of a strong
 * component or a reduced edge (edge connecting two strong components) is
 * encountered. Each graph element can be visited at most once.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface ReducedGraphVisitor extends Visitor {

	/**
	 * Visits the representative vertex of a strong component.
	 * 
	 * @param v
	 *            the representative vertex to visit
	 */
	public void visitRepresentativeVertex(Vertex v);

	/**
	 * Visits a reduced edge.
	 * 
	 * @param e
	 *            the reduced edge to visit
	 */
	public void visitReducedEdge(Edge e);
}
