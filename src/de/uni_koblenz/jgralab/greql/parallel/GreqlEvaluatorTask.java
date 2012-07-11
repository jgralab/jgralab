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
		super(new GreqlEvaluatorCallable(greqlQuery, datagraph, environment,
				dependencyVertex, graphVersion));
		this.dependencyVertex = dependencyVertex;
		this.peval = peval;
	}

	@Override
	public void run() {
		System.out.println("start " + dependencyVertex);
		super.run();
	}

	@Override
	protected void done() {
		System.out.println("\tdone " + dependencyVertex);
		super.done();
		peval.scheduleNext(dependencyVertex);
	}

	@Override
	protected void setException(Throwable t) {
		super.setException(t);
		peval.shutdownNow(t);
		// TODO check Exceptions
		System.out.println();
	}

}