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
package de.uni_koblenz.jgralab.algolib.algorithms.strong_components;

import static java.lang.Math.min;

import java.util.Stack;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AbstractTraversal;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.search.DepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.DFSVisitor;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.DFSVisitorAdapter;
import de.uni_koblenz.jgralab.algolib.algorithms.strong_components.visitors.ReducedGraphVisitor;
import de.uni_koblenz.jgralab.algolib.algorithms.strong_components.visitors.ReducedGraphVisitorComposition;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.Function;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;
import de.uni_koblenz.jgralab.algolib.problems.directed.StrongComponentsSolver;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;
import de.uni_koblenz.jgralab.graphmarker.ArrayVertexMarker;
import de.uni_koblenz.jgralab.graphmarker.IntegerVertexMarker;

public class StrongComponentsWithDFS extends AbstractTraversal implements
		StrongComponentsSolver {

	private DepthFirstSearch dfs;
	private Stack<Vertex> vertexStack;
	private IntFunction<Vertex> lowlink;
	private Function<Vertex, Vertex> strongComponents;
	private DFSVisitor lowlinkVisitor;
	private ReducedGraphVisitorComposition visitors;

	public StrongComponentsWithDFS(Graph graph, DepthFirstSearch dfs) {
		this(graph, null, dfs, null);
	}

	public StrongComponentsWithDFS(Graph graph,
			BooleanFunction<GraphElement> subgraph, DepthFirstSearch dfs,
			BooleanFunction<Edge> navigable) {
		super(graph, subgraph, navigable);
		this.dfs = dfs;
	}

	@Override
	public void addVisitor(Visitor visitor) {
		checkStateForSettingVisitors();
		if (visitor instanceof ReducedGraphVisitor) {
			visitor.setAlgorithm(this);
			visitors.addVisitor(visitor);
		} else {
			// the algorithm is set implicitly to the dfs
			dfs.addVisitor(visitor);
		}
	}

	@Override
	public void disableOptionalResults() {
	}

	@Override
	protected void done() {
		state = dfs.getState();
	}

	@Override
	public StrongComponentsWithDFS normal() {
		super.normal();
		dfs.normal();
		return this;
	}

	@Override
	public AbstractTraversal reversed() {
		super.reversed();
		dfs.reversed();
		return this;
	}

	@Override
	public boolean isDirected() {
		return true;
	}

	@Override
	public boolean isHybrid() {
		return false;
	}

	@Override
	public void removeVisitor(Visitor visitor) {
		checkStateForSettingVisitors();
		if (visitor instanceof ReducedGraphVisitor) {
			visitors.removeVisitor(visitor);
		} else {
			dfs.removeVisitor(visitor);
		}
	}

	@Override
	public Function<Vertex, Vertex> getStrongComponents() {
		checkStateForResult();
		return strongComponents;
	}

	public IntFunction<Vertex> getLowlink() {
		checkStateForResult();
		return lowlink;
	}

	@Override
	public void resetParameters() {
		super.resetParameters();
		vertexStack = new Stack<Vertex>();
		lowlink = new IntegerVertexMarker(graph);
		strongComponents = new ArrayVertexMarker<Vertex>(graph);
		visitors = new ReducedGraphVisitorComposition();
		lowlinkVisitor = new DFSVisitorAdapter() {

			IntFunction<Vertex> number;

			@Override
			public void setAlgorithm(GraphAlgorithm algorithm) {
				super.setAlgorithm(algorithm);
				number = this.algorithm.getInternalNumber();
			}

			@Override
			public void visitVertex(Vertex v) {
				vertexStack.push(v);
				lowlink.set(v, number.get(v));
			}

			public void maybeVisitReducedEdge(Edge e) {
				if (strongComponents.isDefined(e.getThat())) {
					visitors.visitReducedEdge(e);
				}
			}

			@Override
			public void leaveTreeEdge(Edge e) {
				Vertex v = e.getThis();
				Vertex w = e.getThat();
				lowlink.set(v, min(lowlink.get(v), lowlink.get(w)));
				maybeVisitReducedEdge(e);
			}

			@Override
			public void visitForwardArc(Edge e) {
				maybeVisitReducedEdge(e);
			}

			@Override
			public void visitBackwardArc(Edge e) {
				Vertex v = e.getThis();
				Vertex w = e.getThat();
				lowlink.set(v, min(lowlink.get(v), number.get(w)));
			}

			@Override
			public void visitCrosslink(Edge e) {
				Vertex v = e.getThis();
				Vertex w = e.getThat();
				if (vertexStack.contains(w)) {
					lowlink.set(v, min(lowlink.get(v), number.get(w)));
				}
				maybeVisitReducedEdge(e);
			}

			@Override
			public void leaveVertex(Vertex v) {
				if (lowlink.get(v) == number.get(v)) {
					Vertex x;
					do {
						x = vertexStack.pop();
						strongComponents.set(x, v);
					} while (x != v);
					visitors.visitRepresentativeVertex(v);
				}
			}

		};
	}

	@Override
	public void reset() {
		super.reset();
		vertexStack.clear();
	}

	@Override
	public StrongComponentsSolver execute() {
		dfs.reset();
		dfs.setGraph(graph);
		dfs.setSubgraph(subgraph);
		dfs.setNavigable(navigable);
		dfs.setSearchDirection(searchDirection);
		dfs.addVisitor(lowlinkVisitor);
		try {
			startRunning();
		} catch (AlgorithmTerminatedException e) {
		}
		dfs.execute();
		done();
		dfs.removeVisitor(lowlinkVisitor);
		return this;
	}

	public IntFunction<Vertex> getInternalLowlink() {
		return lowlink;
	}

	public Function<Vertex, Vertex> getInternalStrongComponents() {
		return strongComponents;
	}

	public Stack<Vertex> getVertexStack() {
		return vertexStack;
	}

}
