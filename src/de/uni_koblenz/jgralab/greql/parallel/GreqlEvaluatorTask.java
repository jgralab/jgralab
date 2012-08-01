package de.uni_koblenz.jgralab.greql.parallel;

import java.util.concurrent.FutureTask;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.GreqlEnvironment;
import de.uni_koblenz.jgralab.greql.GreqlQuery;

public class GreqlEvaluatorTask extends FutureTask<Object> {

	private final Vertex dependencyVertex;

	private final ParallelGreqlEvaluator peval;

	public GreqlEvaluatorTask(GreqlQuery greqlQuery, Graph datagraph,
			GreqlEnvironment environment, Vertex dependencyVertex,
			long graphVersion, ParallelGreqlEvaluator peval) {
		this(new GreqlEvaluatorCallable(greqlQuery, datagraph, environment,
				dependencyVertex, graphVersion, peval));
	}

	public GreqlEvaluatorTask(GreqlEvaluatorCallable callable) {
		super(callable);
		this.dependencyVertex = callable.dependencyVertex;
		this.peval = callable.peval;
	}

	@Override
	protected void done() {
		super.done();
		peval.scheduleNext(dependencyVertex);
	}

}