/**
 *
 */
package de.uni_koblenz.jgralab.greql2.optimizer;

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
}
