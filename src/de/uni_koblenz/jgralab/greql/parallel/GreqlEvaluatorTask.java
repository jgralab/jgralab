package de.uni_koblenz.jgralab.greql.parallel;

import java.util.concurrent.FutureTask;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.GreqlEnvironment;

public class GreqlEvaluatorTask extends FutureTask<Object> {

	public GreqlEvaluatorTask(Vertex v, Graph datagraph,
			GreqlEnvironment environment) {
		super(new GreqlEvaluatorCallable(v, datagraph, environment));
	}

}