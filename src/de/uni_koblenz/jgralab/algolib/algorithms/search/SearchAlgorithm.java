package de.uni_koblenz.jgralab.algolib.algorithms.search;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.HybridGraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.visitors.SearchVisitor;

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

	protected BooleanFunction<Edge> navigable;

	protected Vertex[] vertexOrder;
	protected Edge[] edgeOrder;

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
		vertexOrder =  new Vertex[graph.getVCount() + 1];
		edgeOrder = new Edge[graph.getECount() + 1];
		// reset visitors
	}

	@Override
	public void resetParameters() {
		super.resetParameters();
		this.navigable = DEFAULT_NAVIGABLE;
	}
	
	public abstract void addSearchVisitor(SearchVisitor visitor);
}
