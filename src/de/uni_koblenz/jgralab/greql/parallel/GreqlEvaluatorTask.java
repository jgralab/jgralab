package de.uni_koblenz.jgralab.greql.parallel;

import java.util.concurrent.FutureTask;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.GreqlEnvironment;
import de.uni_koblenz.jgralab.greql.GreqlQuery;

public class GreqlEvaluatorTask extends FutureTask<Object> {

	private final Vertex dependencyVertex;

	private final ParallelGreqlEvaluator peval;

	private final String queryString;

	public GreqlEvaluatorTask(GreqlQuery greqlQuery, Graph datagraph,
			GreqlEnvironment environment, Vertex dependencyVertex,
			long graphVersion, ParallelGreqlEvaluator peval) {
		super(new GreqlEvaluatorCallable(greqlQuery, datagraph, environment,
				dependencyVertex, graphVersion, peval));
		this.dependencyVertex = dependencyVertex;
		this.peval = peval;
		queryString = greqlQuery.getQueryText();
	}

	@Override
	public void run() {
		System.out.println("start " + dependencyVertex + " query: "
				+ queryString);
		super.run();
	}

	@Override
	protected void done() {
		if (peval.getException() == null) {
			System.out.println("\tdone " + dependencyVertex + " query: "
					+ queryString);
		}
		super.done();
		peval.scheduleNext(dependencyVertex);
	}

}