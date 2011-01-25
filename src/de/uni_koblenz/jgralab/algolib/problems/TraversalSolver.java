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
