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

		result = context.evaluateGReQLQuery(greqlExpression);

		if (!result) {
			throw new GReTLException(context, "Assertion failed: "
					+ greqlExpression);
		}
		return 0;
	}
}
