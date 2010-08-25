package de.uni_koblenz.jgralab.algolib.algorithms.topological_order;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.search.AbstractTraversal;
import de.uni_koblenz.jgralab.algolib.algorithms.topological_order.visitors.TopologicalOrderVisitorComposition;
import de.uni_koblenz.jgralab.algolib.functions.ArrayPermutation;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;
import de.uni_koblenz.jgralab.algolib.functions.Permutation;
import de.uni_koblenz.jgralab.algolib.problems.directed.AcyclicitySolver;
import de.uni_koblenz.jgralab.algolib.problems.directed.TopologicalOrderSolver;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;
import de.uni_koblenz.jgralab.graphmarker.IntegerVertexMarker;

public class KahnKnuthAlgorithm extends AbstractTraversal implements
		AcyclicitySolver, TopologicalOrderSolver {

	private TopologicalOrderVisitorComposition visitors;
	private int tnum;
	private int firstV;
	private Vertex[] torder;
	private IntFunction<Vertex> tnumber;
	private IntFunction<Vertex> inDegree;
	private boolean acyclic;
	private EdgeDirection degreeDirection;

	public KahnKnuthAlgorithm(Graph graph,
			BooleanFunction<GraphElement> subgraph,
			BooleanFunction<Edge> navigable) {
		super(graph, subgraph, navigable);
	}

	public KahnKnuthAlgorithm(Graph graph) {
		this(graph, null, null);
	}

	public KahnKnuthAlgorithm withTNumber() {
		checkStateForSettingParameters();
		tnumber = new IntegerVertexMarker(graph);
		return this;
	}

	@Override
	public KahnKnuthAlgorithm normal() {
		super.normal();
		degreeDirection = EdgeDirection.IN;
		return this;
	}

	@Override
	public KahnKnuthAlgorithm reversed() {
		super.reversed();
		degreeDirection = EdgeDirection.OUT;
		return this;
	}

	public KahnKnuthAlgorithm withTnumber() {
		checkStateForSettingParameters();
		tnumber = new IntegerVertexMarker(graph);
		return this;
	}

	public KahnKnuthAlgorithm withoutTnumber() {
		checkStateForSettingParameters();
		tnumber = null;
		return this;
	}

	@Override
	public void disableOptionalResults() {
		checkStateForSettingParameters();
		tnumber = null;
	}

	@Override
	public void addVisitor(Visitor visitor) {
		checkStateForSettingParameters();
		visitor.setAlgorithm(this);
		visitors.addVisitor(visitor);
	}

	@Override
	public void removeVisitor(Visitor visitor) {
		checkStateForSettingParameters();
		visitors.removeVisitor(visitor);
	}

	@Override
	public void reset() {
		super.reset();
		tnum = 1;
		firstV = 1;
		torder = new Vertex[getVertexCount() + 1];
		inDegree = new IntegerVertexMarker(graph);
		acyclic = true;
		visitors.reset();
	}

	@Override
	public void resetParameters() {
		super.resetParameters();
		visitors = new TopologicalOrderVisitorComposition();
		normal();
	}

	@Override
	protected void done() {
		state = AlgorithmStates.FINISHED;
	}

	@Override
	public boolean isDirected() {
		return true;
	}

	@Override
	public boolean isAcyclic() {
		checkStateForResult();
		return acyclic;
	}

	@Override
	public Permutation<Vertex> getTopologicalOrder() {
		checkStateForResult();
		return new ArrayPermutation<Vertex>(torder);
	}

	@Override
	public boolean isHybrid() {
		return false;
	}

	@Override
	public KahnKnuthAlgorithm execute() {
		startRunning();

		// store actual in degree for all vertices
		for (Vertex currentVertex : graph.vertices()) {
			if (subgraph != null && !subgraph.get(currentVertex)) {
				continue;
			}
			// TODO replace with proper degree computation with respect to the
			// subgraph and the function navigable
			int degree = currentVertex.getDegree(degreeDirection);

			inDegree.set(currentVertex, degree);
			if (degree == 0) {
				torder[tnum] = currentVertex;
				if (tnumber != null) {
					tnumber.set(currentVertex, tnum);
				}
				tnum++;
			}

		}
		while (firstV < tnum) {
			Vertex currentVertex = torder[firstV++];
			visitors.visitVertexInTopologicalOrder(currentVertex);
			for (Edge currentEdge : currentVertex.incidences(searchDirection)) {
				if (subgraph != null && !subgraph.get(currentEdge)) {
					continue;
				}
				Vertex nextVertex = currentEdge.getThat();
				inDegree.set(nextVertex, inDegree.get(nextVertex) - 1);
				if (inDegree.get(nextVertex) == 0) {
					torder[tnum] = nextVertex;
					if (tnumber != null) {
						tnumber.set(nextVertex, tnum);
					}
					tnum++;
				}
			}
		}
		if (tnum < getVertexCount()) {
			acyclic = false;
		}
		done();
		return this;
	}

}
