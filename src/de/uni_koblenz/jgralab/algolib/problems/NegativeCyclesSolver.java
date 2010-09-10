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

/**
 * The problem <b>negative cycles</b> can be defined for directed and undirected
 * graphs. There are no further parameters. The result is a boolean value
 * <i>negative cycles</i> that is true if the graph has negative cycles.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface NegativeCyclesSolver extends WeightedProblemSolver {

	/**
	 * Solves the problem <b>negative cycles</b>.
	 * 
	 * @return this algorithm object.
	 * @throws AlgorithmTerminatedException
	 *             if this algorithm terminated before the actual execution is
	 *             completed. This can happen from inside (early termination) or
	 *             from outside (Thread interruption). The algorithm state
	 *             changes accordingly.
	 */
	public NegativeCyclesSolver execute();

	/**
	 * Retrieves the result <code>negativeCycles</code>.
	 * 
	 * @return the result <code>negativeCycles</code>.
	 * @throws IllegalStateException
	 *             if the result is requested without being available
	 */
	public boolean hasNegativeCycles();
}
