package de.uni_koblenz.jgralab.algolib.algorithms.search;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.HybridGraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.Function;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;
import de.uni_koblenz.jgralab.graphmarker.ArrayVertexMarker;
import de.uni_koblenz.jgralab.graphmarker.IntegerVertexMarker;

public abstract class SearchAlgorithm extends HybridGraphAlgorithm {

	public static final BooleanFunction<Edge> DEFAULT_NAVIGABLE = new BooleanFunction<Edge>() {

		@Override
		public boolean get(Edge parameter) {
			return true;
		}

		@Override
		public boolean isDefined(Edge parameter) {
			return true;
		}

		@Override
		public void set(Edge parameter, boolean value) {
			throw new UnsupportedOperationException(
					"This function is immutable.");
		}

	};
	public static final boolean DEFAULT_COMPUTE_VERTEX_ORDER = true;
	public static final boolean DEFAULT_COMPUTE_EDGE_ORDER = true;
	public static final boolean DEFAULT_COMPUTE_LEVEL = false;
	public static final boolean DEFAULT_COMPUTE_PARENT = false;

	protected BooleanFunction<Edge> navigable;
	protected boolean computeVertexOrder;
	protected boolean computeEdgeOrder;
	protected boolean computeLevel;
	protected boolean computeParent;

	protected Vertex[] vertexOrder;
	protected Edge[] edgeOrder;
	protected IntFunction<Vertex> level;
	protected Function<Vertex, Edge> parent;

	public SearchAlgorithm(Graph graph, BooleanFunction<GraphElement> subgraph,
			boolean directed, BooleanFunction<Edge> navigable) {
		super(graph, subgraph, directed);
		this.navigable = navigable;
	}

	public SearchAlgorithm(Graph graph) {
		super(graph);
	}

	@Override
	public void reset() {
		vertexOrder = computeVertexOrder ? new Vertex[graph.getVCount() + 1]
				: null;
		edgeOrder = computeEdgeOrder ? new Edge[graph.getECount() + 1] : null;
		level = computeLevel ? new IntegerVertexMarker(graph) : null;
		parent = computeParent ? new ArrayVertexMarker<Edge>(graph) : null;
	}

	@Override
	public void resetParameters() {
		super.resetParameters();
		this.navigable = DEFAULT_NAVIGABLE;
		this.computeVertexOrder = DEFAULT_COMPUTE_VERTEX_ORDER;
		this.computeEdgeOrder = DEFAULT_COMPUTE_EDGE_ORDER;
		this.computeLevel = DEFAULT_COMPUTE_LEVEL;
		this.computeParent = DEFAULT_COMPUTE_PARENT;
	}

	public boolean isComputeVertexOrder() {
		return computeVertexOrder;
	}

	public void setComputeVertexOrder(boolean computeVertexOrder) {
		this.computeVertexOrder = computeVertexOrder;
	}

	public boolean isComputeEdgeOrder() {
		return computeEdgeOrder;
	}

	public void setComputeEdgeOrder(boolean computeEdgeOrder) {
		this.computeEdgeOrder = computeEdgeOrder;
	}

	public boolean isComputeLevel() {
		return computeLevel;
	}

	public void setComputeLevel(boolean computeLevel) {
		this.computeLevel = computeLevel;
	}

	public boolean isComputeParent() {
		return computeParent;
	}

	public void setComputeParent(boolean computeParent) {
		this.computeParent = computeParent;
	}

}
