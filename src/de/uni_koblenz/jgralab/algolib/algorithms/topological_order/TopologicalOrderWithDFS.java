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
package de.uni_koblenz.jgralab.algolib.algorithms.topological_order;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.StructureOrientedAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.search.DepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.DFSVisitorAdapter;
import de.uni_koblenz.jgralab.algolib.algorithms.topological_order.visitors.TopologicalOrderVisitor;
import de.uni_koblenz.jgralab.algolib.algorithms.topological_order.visitors.TopologicalOrderVisitorList;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.Permutation;
import de.uni_koblenz.jgralab.algolib.problems.AcyclicitySolver;
import de.uni_koblenz.jgralab.algolib.problems.TopologicalOrderSolver;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

public class TopologicalOrderWithDFS extends StructureOrientedAlgorithm implements
		AcyclicitySolver, TopologicalOrderSolver {

	private DepthFirstSearch dfs;
	private DFSVisitorAdapter torderVisitorAdapter;
	private boolean acyclic;
	private TopologicalOrderVisitorList visitors;

	public TopologicalOrderWithDFS(Graph graph,
			BooleanFunction<GraphElement> subgraph, DepthFirstSearch dfs,
			BooleanFunction<Edge> navigable) {
		super(graph, subgraph, navigable);
		this.dfs = dfs;
	}

	public TopologicalOrderWithDFS(Graph graph, DepthFirstSearch dfs) {
		this(graph, null, dfs, null);
	}

	@Override
	public void addVisitor(Visitor visitor) {
		checkStateForSettingVisitors();
		if (visitor instanceof TopologicalOrderVisitor) {
			visitor.setAlgorithm(this);
			visitors.addVisitor(visitor);
		} else {
			// the algorithm is set implicitly to the dfs
			dfs.addVisitor(visitor);
		}
	}

	@Override
	public void removeVisitor(Visitor visitor) {
		checkStateForSettingVisitors();
		if (visitor instanceof TopologicalOrderVisitor) {
			visitors.removeVisitor(visitor);
		} else {
			dfs.removeVisitor(visitor);
		}
	}

	@Override
	public void disableOptionalResults() {
	}

	@Override
	public StructureOrientedAlgorithm normal() {
		super.normal();
		return this;
	}

	@Override
	public StructureOrientedAlgorithm reversed() {
		super.reversed();
		return this;
	}

	@Override
	public boolean isHybrid() {
		return false;
	}

	@Override
	public void resetParameters() {
		super.resetParameters();
		visitors = new TopologicalOrderVisitorList();
		torderVisitorAdapter = new DFSVisitorAdapter() {
			@Override
			public void visitBackwardArc(Edge e) throws AlgorithmTerminatedException {
				acyclic = false;
				dfs.terminate();
			}

			@Override
			public void leaveVertex(Vertex v) throws AlgorithmTerminatedException {
				visitors.visitVertexInTopologicalOrder(v);
			}
		};
		this.normal();
		dfs.reversed();
	}

	@Override
	public void reset() {
		super.reset();
		acyclic = true;
	}

	@Override
	public TopologicalOrderWithDFS execute() {
		dfs.reset();
		dfs.setGraph(graph);
		dfs.setSubgraph(subgraph);
		dfs.setNavigable(navigable);
		if(traversalDirection == EdgeDirection.OUT){ //normal
			dfs.reversed();
		} else { // reversed
			assert traversalDirection == EdgeDirection.IN;
			dfs.normal();
		}
		dfs.addVisitor(torderVisitorAdapter);
		startRunning();
		try {
			dfs.withRorder().execute();
		} catch (AlgorithmTerminatedException e) {
		}
		done();
		dfs.removeVisitor(torderVisitorAdapter);
		return this;
	}

	@Override
	protected void done() {
		state = dfs.getState();
	}

	@Override
	public Permutation<Vertex> getTopologicalOrder() {
		checkStateForResult();
		return dfs.getRorder();
	}

	@Override
	public boolean isAcyclic() {
		checkStateForResult();
		return acyclic;
	}
}
