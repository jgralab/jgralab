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
import de.uni_koblenz.jgralab.algolib.functions.BinaryFunction;

/**
 * The problem <b>simple paths</b> can be defined for directed and undirected
 * graphs. There are no further parameters. The result is a binary function
 * <i>successor</i> that assigns each vertex pair an edge, which is the next
 * edge from the first vertex to follow for reaching the second vertex. The
 * simple paths that can be extracted from this function are <b>not</b>
 * necessarily shortest paths.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface SimplePathsSolver extends ProblemSolver {

	/**
	 * Solves the problem <b>simple paths</b>.
	 * 
	 * @return this algorithm object.
	 * @throws AlgorithmTerminatedException
	 *             if this algorithm terminated before the actual execution is
	 *             completed. This can happen from inside (early termination) or
	 *             from outside (Thread interruption). The algorithm state
	 *             changes accordingly.
	 */
	public SimplePathsSolver execute();

	/**
	 * Retrieves the result <code>successor</code>.
	 * 
	 * @return the result <code>successor</code>.
	 * @throws IllegalStateException
	 *             if the result is requested without being available
	 */
	public BinaryFunction<Vertex, Vertex, Edge> getSuccessor();
}
