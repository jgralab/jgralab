package de.uni_koblenz.jgralab.greql.parallel;

import java.util.ConcurrentModificationException;
import java.util.concurrent.Callable;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.GreqlEnvironment;
import de.uni_koblenz.jgralab.greql.GreqlQuery;

public class GreqlEvaluatorCallable implements Callable<Object> {
	private final GreqlQuery query;
	private final Graph datagraph;
	private final GreqlEnvironment environment;
	final Vertex dependencyVertex;
	private final long graphVersion;
	final ParallelGreqlEvaluator peval;
	private Object result;
	private boolean isFinished;

	public GreqlEvaluatorCallable(GreqlQuery greqlQuery, Graph datagraph,
			GreqlEnvironment environment, Vertex dependencyVertex,
			long graphVersion, ParallelGreqlEvaluator peval) {
		query = greqlQuery;
		this.datagraph = datagraph;
		this.environment = environment;
		this.dependencyVertex = dependencyVertex;
		this.graphVersion = graphVersion;
		this.peval = peval;
	}

	@Override
	public Object call() throws Exception {
		synchronized (dependencyVertex.getGraph()) {
			if (dependencyVertex.getGraph().isGraphModified(graphVersion)) {
				ConcurrentModificationException e = new ConcurrentModificationException(
						"The dependency graph must not be modified during execution of queries.");
				peval.shutdownNow(e);
				throw e;
			}
		}
		try {
			result = query.evaluate(datagraph, environment, null);
		} catch (RuntimeException e) {
			peval.shutdownNow(e);
			throw e;
		}
		isFinished = true;
		return result;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public Object getResult() {
		return result;
	}

}
