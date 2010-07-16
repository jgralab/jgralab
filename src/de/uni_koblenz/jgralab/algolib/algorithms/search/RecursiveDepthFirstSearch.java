package de.uni_koblenz.jgralab.algolib.algorithms.search;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;
import de.uni_koblenz.jgralab.algolib.visitors.DFSVisitor;
import de.uni_koblenz.jgralab.algolib.visitors.DFSVisitorComposition;
import de.uni_koblenz.jgralab.algolib.visitors.SearchVisitor;
import de.uni_koblenz.jgralab.algolib.visitors.SimpleVisitor;
import de.uni_koblenz.jgralab.graphmarker.IntegerVertexMarker;

public class RecursiveDepthFirstSearch extends SearchAlgorithm {

	private DFSVisitorComposition visitors;
	private ComputeNumberVisitor cnv;
	private IntFunction<Vertex> number;

	public RecursiveDepthFirstSearch(Graph graph,
			BooleanFunction<GraphElement> subgraph, boolean directed,
			BooleanFunction<Edge> navigable) {
		super(graph, subgraph, directed, navigable);
	}

	public RecursiveDepthFirstSearch(Graph graph) {
		super(graph);
	}

	protected int rNum;
	private IntFunction<Vertex> rnumber;

	@Override
	public void reset() {
		super.reset();
		visitors.reset();
		rNum = 1;
		rnumber = new IntegerVertexMarker(graph);
		number = cnv.getIntermediateNumber();
	}

	@Override
	public void resetParameters() {
		super.resetParameters();
		visitors = new DFSVisitorComposition();
		cnv = new ComputeNumberVisitor();
		cnv.setAlgorithm(this);
		visitors.addSearchVisitor(cnv);

	}

	@Override
	public void addSearchVisitor(SearchVisitor visitor) {
		visitor.setAlgorithm(this);
		visitors.addSearchVisitor(visitor);
	}

	@Override
	public void addSimpleVisitor(SimpleVisitor visitor) {
		visitor.setAlgorithm(this);
		visitors.addSimpleVisitor(visitor);
	}

	public void addDFSVisitor(DFSVisitor visitor) {
		visitor.setAlgorithm(this);
		visitors.addDFSVisitor(visitor);
	}

	@Override
	public void solveTraversalFromVertex(Vertex root) {
		if (visitedVertices.get(root) || !subgraph.get(root)) {
			return;
		}
		visitors.visitRoot(root);
		dfs(root);
		state = num < graph.getVCount() + 1 ? AlgorithmStates.STOPPED
				: AlgorithmStates.FINISHED;
	}

	private void dfs(Vertex currentVertex) {
		vertexOrder[num] = currentVertex;
		visitors.visitVertex(currentVertex);
		visitedVertices.set(currentVertex, true);
		num++;

		for (Edge currentEdge : currentVertex.incidences(searchDirection)) {
			if (subgraph.get(currentEdge) && navigable.get(currentEdge)
					&& !visitedEdges.get(currentEdge)) {
				Vertex nextVertex = currentEdge.getThat();
				if (subgraph.get(nextVertex)) {
					edgeOrder[eNum] = currentEdge;
					visitors.visitEdge(currentEdge);
					visitedEdges.set(currentEdge, true);
					eNum++;
					if (!visitedVertices.get(nextVertex)) {
						visitors.visitTreeEdge(currentEdge);

						// recursive call
						dfs(nextVertex);

						visitors.leaveTreeEdge(currentEdge);
					} else {
						visitors.visitFrond(currentEdge);
						if (!rnumber.isDefined(nextVertex)) {
							visitors.visitBackwardArc(currentEdge);
						} else if (number.get(nextVertex) > number
								.get(currentVertex)) {
							visitors.visitForwardArc(currentEdge);
						} else {
							visitors.visitCrosslink(currentEdge);
						}
					}
				}
			}
		}
		rnumber.set(currentVertex, rNum);
		visitors.leaveVertex(currentVertex);
		rNum++;
	}

	public int getIntermediateRNum() {
		return rNum;
	}

	public IntFunction<Vertex> getIntermediateRnumber() {
		return rnumber;
	}

	public IntFunction<Vertex> getIntermediateNumber() {
		return number;
	}

	public IntFunction<Vertex> getNumber() {
		if (state == AlgorithmStates.FINISHED
				|| state == AlgorithmStates.STOPPED) {
			return number;
		} else {
			throw new IllegalStateException(
					"The result cannot be obtained while in this state: "
							+ state);
		}
	}

	public IntFunction<Vertex> getRnumber() {
		if (state == AlgorithmStates.FINISHED
				|| state == AlgorithmStates.STOPPED) {
			return rnumber;
		} else {
			throw new IllegalStateException(
					"The result cannot be obtained while in this state: "
							+ state);
		}
	}

}
