package de.uni_koblenz.jgralab.greql.parallel;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Waits until all queries are evaluated.
 */
public class GreqlResultCollectorCallable implements Callable<Void> {
	private final List<GreqlEvaluatorTask> finalEvaluators;

	public GreqlResultCollectorCallable(List<GreqlEvaluatorTask> finalEvaluators) {
		this.finalEvaluators = finalEvaluators;
	}

	@Override
	public Void call() throws Exception {
		for (GreqlEvaluatorTask finalEvaluator : finalEvaluators) {
			finalEvaluator.get();
		}
		return null;
	}

}
