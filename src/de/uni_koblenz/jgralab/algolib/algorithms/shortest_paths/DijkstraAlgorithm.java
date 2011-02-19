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
package de.uni_koblenz.jgralab.algolib.algorithms.shortest_paths;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.DoubleFunction;
import de.uni_koblenz.jgralab.algolib.problems.DistancesFromVertexSolver;
import de.uni_koblenz.jgralab.algolib.problems.ShortestPathsFromVertexSolver;

public class DijkstraAlgorithm extends AStarSearch implements
		DistancesFromVertexSolver, ShortestPathsFromVertexSolver {

	public DijkstraAlgorithm(Graph graph,
			BooleanFunction<GraphElement> subgraph,
			BooleanFunction<Edge> navigable, DoubleFunction<Edge> edgeWeight) {
		super(graph, subgraph, navigable, edgeWeight, null);
	}

	public DijkstraAlgorithm(Graph graph) {
		this(graph, null, null, null);
	}

	@Override
	public DijkstraAlgorithm normal() {
		super.normal();
		return this;
	}

	@Override
	public DijkstraAlgorithm reversed() {
		super.reversed();
		return this;
	}

	@Override
	public DijkstraAlgorithm undirected() {
		super.undirected();
		return this;
	}

	@Override
	public DijkstraAlgorithm execute(Vertex start)
			throws AlgorithmTerminatedException {
		internalExecute(start, null);
		return this;
	}

	@Override
	public DoubleFunction<Vertex> getDistance() {
		checkStateForResult();
		return super.getDistance();
	}

	public DoubleFunction<Vertex> getInternalDistance() {
		return super.getDistance();
	}

}
