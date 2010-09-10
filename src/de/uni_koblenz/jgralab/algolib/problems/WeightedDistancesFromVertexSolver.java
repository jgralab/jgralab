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

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.DoubleFunction;

/**
 * The problem <b>weighted distances from vertex</b> can be defined for directed
 * and undirected graphs. The graph may not have negative cycles. Algorithms
 * solving this problem are not required to check this precondition. The only
 * further parameter is the <i>start vertex</i>. Implementations compute the
 * weighted shortest distances from this vertex to all reachable vertices. The
 * result is a function <i>weighted distance</i> that assigns each vertex the
 * weighted distance from the start vertex.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface WeightedDistancesFromVertexSolver extends ProblemSolver {

	/**
	 * Solves the problem <b>weighted distance from vertex</b>.
	 * 
	 * @param start
	 *            the start vertex.
	 * @return this algorithm object.
	 * @throws AlgorithmTerminatedException
	 *             if this algorithm terminated before the actual execution is
	 *             completed. This can happen from inside (early termination) or
	 *             from outside (Thread interruption). The algorithm state
	 *             changes accordingly.
	 */
	public WeightedDistancesFromVertexSolver execute(Vertex start);

	/**
	 * Retrieves the result <code>weightedDistance</code>.
	 * 
	 * @return the result <code>weightedDistance</code>.
	 * @throws IllegalStateException
	 *             if the result is requested without being available
	 */
	public DoubleFunction<Vertex> getWeightedDistance();
}
