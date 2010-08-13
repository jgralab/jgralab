package de.uni_koblenz.jgralab.algolib.algorithms.acyclicity;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.functions.ArrayPermutation;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;
import de.uni_koblenz.jgralab.algolib.functions.Permutation;
import de.uni_koblenz.jgralab.algolib.problems.directed.AcyclicitySolver;
import de.uni_koblenz.jgralab.algolib.problems.directed.TopologicalOrderSolver;
import de.uni_koblenz.jgralab.algolib.visitors.SimpleVisitorComposition;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;
import de.uni_koblenz.jgralab.graphmarker.IntegerVertexMarker;

public class KahnKnuthAlgorithm extends GraphAlgorithm implements
		AcyclicitySolver, TopologicalOrderSolver {

	private SimpleVisitorComposition visitors;
	private int tnum;
	private int firstV;
	private Vertex[] torder;
	private IntFunction<Vertex> tnumber;
	private IntFunction<Vertex> inDegree;
	private boolean acyclic;
	private EdgeDirection searchDirection;
	private EdgeDirection degreeDirection;

	public KahnKnuthAlgorithm(Graph graph,
			BooleanFunction<GraphElement> subgraph) {
		super(graph, subgraph);
	}

	public KahnKnuthAlgorithm(Graph graph) {
		super(graph);
	}

	public KahnKnuthAlgorithm withTNumber() {
		tnumber = new IntegerVertexMarker(graph);
		return this;
	}

	public KahnKnuthAlgorithm normal() {
		searchDirection = EdgeDirection.OUT;
		degreeDirection = EdgeDirection.IN;
		return this;
	}

	public KahnKnuthAlgorithm reversed() {
		searchDirection = EdgeDirection.IN;
		degreeDirection = EdgeDirection.OUT;
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
	public void removeVisitor(Visitor visitor){
		checkStateForSettingParameters();
		visitors.removeVisitor(visitor);
	}

	@Override
	public void reset() {
		super.reset();
		tnum = 1;
		firstV = 1;
		torder = new Vertex[graph.getVCount() + 1];
		inDegree = new IntegerVertexMarker(graph);
		acyclic = true;
		visitors.reset();
	}

	@Override
	public void resetParameters() {
		super.resetParameters();
		visitors = new SimpleVisitorComposition();
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
	public void setDirected(boolean directed) {
		throw new UnsupportedOperationException(
				"This algorithm only works for directed graphs.");
	}

	@Override
	public KahnKnuthAlgorithm execute() {
		startRunning();

		// store actual in degree for all vertices
		for (Vertex currentVertex : graph.vertices()) {
			if (subgraph == null || subgraph.get(currentVertex)) {
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
		}
		
		while (firstV < tnum) {
			Vertex currentVertex = torder[firstV++];
			visitors.visitVertex(currentVertex);
			for (Edge currentEdge : currentVertex.incidences(searchDirection)) {
				visitors.visitEdge(currentEdge);
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
		if (tnum < graph.getVCount()) {
			acyclic = false;
		}
		done();
		return this;
	}

}
