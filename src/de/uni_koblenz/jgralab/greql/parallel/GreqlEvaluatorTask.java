package de.uni_koblenz.jgralab.greql.parallel;

import java.util.concurrent.FutureTask;

public class GreqlEvaluatorTask extends FutureTask<Object> {

	public GreqlEvaluatorTask(GreqlEvaluatorCallable callable) {
		super(callable);
		// TODO Auto-generated constructor stub
	}

}
