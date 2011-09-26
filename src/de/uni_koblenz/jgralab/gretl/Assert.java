/**
 * 
 */
package de.uni_koblenz.jgralab.gretl;

import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;

/**
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 */
public class Assert extends CountingTransformation {

	private String greqlExpression;
	private boolean result;

	public Assert(Context c, String greqlExpression) {
		super(c);
		this.greqlExpression = greqlExpression;
	}

	public static Assert parseAndCreate(ExecuteTransformation et) {
		et.matchTransformationArrow();
		String semExp = et.matchSemanticExpression();
		return new Assert(et.context, semExp);
	}

	@Override
	protected Integer transform() {
		if (context.phase == TransformationPhase.SCHEMA) {
			return 0;
		}

		Object r = context.evaluateGReQLQuery(greqlExpression);
		if (r instanceof Boolean) {
			Boolean res = (Boolean) r;
			result = res.booleanValue();
		} else {
			throw new GReTLException(context, "Assertion '" + greqlExpression
					+ "' didn't evaluate to a boolean but to: " + r);
		}

		if (!result) {
			throw new GReTLException(context, "Assertion failed: "
					+ greqlExpression);
		}
		return 0;
	}
}
