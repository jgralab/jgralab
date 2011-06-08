/**
 * 
 */
package de.uni_koblenz.jgralab.gretl;

import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;

/**
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 */
public class Assert extends CountingTransformation {

	private String greqlExpression;
	private JValue result;

	public Assert(Context c, String greqlExpression) {
		super(c);
		this.greqlExpression = greqlExpression;
	}

	public Assert(Context c, JValue result) {
		super(c);
		this.result = result;
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

		if (result == null) {
			result = context.evaluateGReQLQuery(greqlExpression);
		}

		if (!result.isBoolean()) {
			throw new GReTLException(context,
					"Assertion didn't result in a boolean: " + greqlExpression);
		}
		if (!result.toBoolean()) {
			throw new GReTLException(context, "Assertion failed: "
					+ greqlExpression);
		}
		return 0;
	}
}
