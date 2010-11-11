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
package de.uni_koblenz.jgralab.algolib.algorithms.shortest_paths;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AbstractTraversal;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.search.BreadthFirstSearch;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.Function;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;
import de.uni_koblenz.jgralab.algolib.problems.DistanceFromVertexSolver;
import de.uni_koblenz.jgralab.algolib.problems.ShortestPathsFromVertexSolver;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

public class ShortestPathsWithBFS extends AbstractTraversal implements
		DistanceFromVertexSolver, ShortestPathsFromVertexSolver {

	private BreadthFirstSearch bfs;

	public ShortestPathsWithBFS(Graph graph, BreadthFirstSearch bfs) {
		this(graph, null, bfs, null);
	}

	public ShortestPathsWithBFS(Graph graph,
			BooleanFunction<GraphElement> subgraph, BreadthFirstSearch bfs,
			BooleanFunction<Edge> navigable) {
		super(graph, subgraph, navigable);
		this.bfs = bfs;
	}

	@Override
	public void addVisitor(Visitor visitor) {
		checkStateForSettingVisitors();
		// the algorithm is set implicitly to the bfs
		bfs.addVisitor(visitor);
	}

	@Override
	public void disableOptionalResults() {
		checkStateForSettingParameters();
		bfs.disableOptionalResults();
	}

	@Override
	protected void done() {
		state = bfs.getState();
		if (state == AlgorithmStates.STOPPED) {
			state = AlgorithmStates.FINISHED;
		}
	}

	@Override
	public ShortestPathsWithBFS normal() {
		bfs.normal();
		return this;
	}

	@Override
	public ShortestPathsWithBFS reversed() {
		bfs.reversed();
		return this;
	}

	@Override
	public ShortestPathsWithBFS undirected() {
		bfs.undirected();
		return this;
	}

	@Override
	public boolean isDirected() {
		return bfs.isDirected();
	}

	@Override
	public boolean isHybrid() {
		return bfs.isHybrid();
	}

	@Override
	public void removeVisitor(Visitor visitor) {
		checkStateForSettingVisitors();
		bfs.removeVisitor(visitor);
	}

	@Override
	public ShortestPathsWithBFS execute(Vertex start) {
		bfs.reset();
		bfs.setGraph(graph);
		bfs.setSubgraph(subgraph);
		bfs.setNavigable(navigable);
		bfs.setTraversalDirection(traversalDirection);
		startRunning();
		try {
			bfs.withLevel().withParent().execute(start);
		} catch (AlgorithmTerminatedException e) {
		}
		done();
		return this;
	}

	@Override
	public IntFunction<Vertex> getDistance() {
		checkStateForResult();
		return bfs.getLevel();
	}

	@Override
	public Function<Vertex, Edge> getParent() {
		checkStateForResult();
		return bfs.getParent();
	}

}
