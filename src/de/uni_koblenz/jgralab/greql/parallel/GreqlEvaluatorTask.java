package de.uni_koblenz.jgralab.greql.parallel;

import java.util.concurrent.FutureTask;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql.GreqlEnvironment;
import de.uni_koblenz.jgralab.greql.GreqlQuery;

public class GreqlEvaluatorTask extends FutureTask<Object> {

	public GreqlEvaluatorTask(GreqlQuery greqlQuery, Graph datagraph,
			GreqlEnvironment environment) {
		super(new GreqlEvaluatorCallable(greqlQuery, datagraph, environment));
	}

}