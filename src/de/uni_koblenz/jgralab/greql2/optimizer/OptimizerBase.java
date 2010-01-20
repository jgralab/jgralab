/**
 *
 */
package de.uni_koblenz.jgralab.greql2.optimizer;

import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.OptimizerException;

/**
 * Base class for all {@link Optimizer}s which defines some useful methods that
 * are needed in derived Classes.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public abstract class OptimizerBase implements Optimizer {

	protected String optimizerHeaderString() {
		return "*** " + this.getClass().getSimpleName() + ": ";
	}

	protected void recreateVertexEvaluators(GreqlEvaluator eval) {
		try {
			eval.createVertexEvaluators();
		} catch (EvaluateException e) {
			e.printStackTrace();
			throw new OptimizerException(
					"Exception while re-creating VertexEvaluators.", e);
		}
	}
}
