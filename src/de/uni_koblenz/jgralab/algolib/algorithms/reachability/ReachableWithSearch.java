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
package de.uni_koblenz.jgralab.algolib.algorithms.reachability;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AbstractTraversal;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.search.SearchAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.SearchVisitor;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.SearchVisitorAdapter;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.problems.ReachableSolver;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

public class ReachableWithSearch extends AbstractTraversal implements
		ReachableSolver {

	private SearchAlgorithm search;
	private SearchVisitor reachableVisitor;
	private boolean reachable;
	private Vertex target;

	public ReachableWithSearch(Graph graph,
			BooleanFunction<GraphElement> subgraph, SearchAlgorithm search,
			BooleanFunction<Edge> navigable) {
		super(graph, subgraph, navigable);
		this.search = search;
	}

	public ReachableWithSearch(Graph graph, SearchAlgorithm search) {
		this(graph, null, search, null);
	}

	@Override
	public void addVisitor(Visitor visitor) {
		checkStateForSettingVisitors();
		// the algorithm is implicitly set to the search algorithm
		search.addVisitor(visitor);
	}

	@Override
	public void removeVisitor(Visitor visitor) {
		checkStateForSettingVisitors();
		search.removeVisitor(visitor);
	}

	@Override
	public void disableOptionalResults() {
		search.disableOptionalResults();
	}

	@Override
	protected void done() {
		state = search.getState();
		if (state == AlgorithmStates.STOPPED) {
			state = AlgorithmStates.FINISHED;
		}
	}

	@Override
	public ReachableWithSearch normal() {
		search.normal();
		return this;
	}

	@Override
	public ReachableWithSearch reversed() {
		search.reversed();
		return this;
	}

	@Override
	public ReachableWithSearch undirected() {
		search.undirected();
		return this;
	}

	@Override
	public boolean isDirected() {
		return search.isDirected();
	}

	@Override
	public boolean isHybrid() {
		return search.isHybrid();
	}

	@Override
	public void reset() {
		super.reset();
		target = null;
		reachable = false;
	}

	@Override
	public void resetParameters() {
		reachableVisitor = new SearchVisitorAdapter() {

			@Override
			public void visitVertex(Vertex v) {
				if (v == target) {
					reachable = true;
					search.terminate();
				}
			}

		};
	}

	@Override
	public ReachableWithSearch execute(Vertex start, Vertex target) {
		search.reset();
		search.setGraph(graph);
		search.setSubgraph(subgraph);
		search.setNavigable(navigable);
		search.setTraversalDirection(traversalDirection);
		search.addVisitor(reachableVisitor);
		startRunning();
		this.target = target;
		try {
			search.execute(start);
		} catch (AlgorithmTerminatedException e) {
		}
		done();
		search.removeVisitor(reachableVisitor);
		return this;
	}

	@Override
	public boolean isReachable() {
		checkStateForResult();
		return reachable;
	}

	public boolean getInternalReachable() {
		return reachable;
	}

}
