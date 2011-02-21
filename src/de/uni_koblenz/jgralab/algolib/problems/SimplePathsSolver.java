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
package de.uni_koblenz.jgralab.algolib.problems;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
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
	public SimplePathsSolver execute() throws AlgorithmTerminatedException;

	/**
	 * Retrieves the result <code>successor</code>.
	 * 
	 * @return the result <code>successor</code>.
	 * @throws IllegalStateException
	 *             if the result is requested without being available
	 */
	public BinaryFunction<Vertex, Vertex, Edge> getSuccessor();
}
