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
package de.uni_koblenz.jgralab.algolib.problems;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;

/**
 * This interface is the super interface for all problem solvers that solve
 * traversal problems. It does not specify the traversal problems, but it
 * introduces a further parameter, the <i>navigability</i> of edges. In some
 * situations it does not only depend on the edge for deciding whether it is
 * navigable but also on other factors like its attributes or intermediate
 * results. For this purpose, all traversal solvers provide an additional
 * parameter for allowing the decision if an edge is navigable based on such
 * information.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface TraversalSolver extends ProblemSolver {

	/**
	 * Sets the parameter <i>navigable</i>. If this parameter is not set (or set
	 * to <code>null</code>), all edges that are concerned are defined as
	 * navigable edges. This function has to be defined for all edges of the
	 * graph or else this <code>TraversalSolver</code> might fail.
	 * 
	 * @param navigable
	 *            a function that defines whether an edge is navigable or not.
	 */
	public void setNavigable(BooleanFunction<Edge> navigable);
}
