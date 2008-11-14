/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.exception;

import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel;

/**
 * Should be thrown on errors in a {@link CostModel}.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class CostModelException extends Greql2Exception {

	private static final long serialVersionUID = 1863638881642741417L;

	public CostModelException(String message) {
		super(message);
	}

}
