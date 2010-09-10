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
import de.uni_koblenz.jgralab.algolib.functions.DoubleFunction;

/**
 * This is the super interface for all problem solvers that have an edge weight
 * function as input parameter. This function assigns each edge a weight.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface WeightedProblemSolver extends ProblemSolver {

	/**
	 * Sets the parameter <i>edge weight</i>. If this parameter is not set (set
	 * to <code>null</code>), the weight is assumed to be 1 for all edges.
	 * 
	 * @param edgeWeight
	 *            the edge weight function.
	 */
	public void setEdgeWeight(DoubleFunction<Edge> edgeWeight);

}
