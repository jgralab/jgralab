package de.uni_koblenz.jgralab.greql.parallel;

import java.util.concurrent.Callable;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.GreqlEnvironment;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;

public class GreqlEvaluatorCallable implements Callable<Object> {
	private final Vertex vertex;
	private final Graph datagraph;
	private final GreqlEnvironment environment;

	public GreqlEvaluatorCallable(Vertex v, Graph datagraph,
			GreqlEnvironment environment) {
		vertex = v;
		this.datagraph = datagraph;
		this.environment = environment;
	}

	@Override
	public Object call() throws Exception {
		Object result = new GreqlQueryImpl(
				(String) vertex
						.getAttribute(ParallelGreqlEvaluator.QUERY_TEXT_ATTRIBUTE))
				.evaluate(datagraph, environment, null);
		// for (int n = 0; n < node.get_value() * 30000; ++n) {
		// StringBuilder sb = new StringBuilder();
		// for (Vertex v : graph.vertices()) {
		// sb.append(v.toString());
		// }
		// sum += sb.toString().length();
		// }
		// sum = node.get_value();
		// synchronized (graph) {
		// for (Link l : node.getLinkIncidences(EdgeDirection.IN)) {
		// sum += result.getMark(l.getThat());
		// }
		// result.mark(node, sum);
		// }
		return result;
	}

}
