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
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.Function;

/**
 * The problem <b>shortest paths from vertex</b> is defined for directed and
 * undirected graphs. The only further parameter is the <i>vertex</i> from which
 * the shortest paths to all reachable vertices has to be computed. The result
 * is the function parent that describes a path system (tree) that contains all
 * shortest paths from the given vertex to all reachable vertices.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface ShortestPathsFromVertexSolver extends ProblemSolver {

	/**
	 * Solves the problem <b>shortest paths from vertex</b>.
	 * 
	 * @param start
	 *            the vertex to start at.
	 * @return this algorithm object
	 * @throws AlgorithmTerminatedException
	 *             if this algorithm terminated before the actual execution is
	 *             completed. This can happen from inside (early termination) or
	 *             from outside (Thread interruption). The algorithm state
	 *             changes accordingly.
	 */
	public ShortestPathsFromVertexSolver execute(Vertex start);

	/**
	 * Retrieves the result <code>parent</code> as function.
	 * 
	 * @return the result <code>parent</code>.
	 * @throws IllegalStateException
	 *             if the result is requested without being available
	 */
	public Function<Vertex, Edge> getParent();

}
