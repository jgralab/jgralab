/**
 * 
 */
package de.uni_koblenz.jgralab.gretl;

import de.uni_koblenz.jgralab.gretl.Context.TransformationPhase;

/**
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 */
public class SysOut extends CountingTransformation {
	private String greqlExpression;
	protected Object result;

	public SysOut(Context c, String greqlExpression) {
		super(c);
		this.greqlExpression = greqlExpression;
	}

	public SysOut(Context c, Object result) {
		super(c);
		this.result = result;
	}

	public static SysOut parseAndCreate(ExecuteTransformation et) {
		et.matchTransformationArrow();
		String semExp = et.matchSemanticExpression();
		return new SysOut(et.context, semExp);
	}

	@Override
	protected Integer transform() {
		if (context.phase == TransformationPhase.SCHEMA) {
			return null;
		}

		if (result == null) {
			result = context.evaluateGReQLQuery(greqlExpression);
		}

		doPrint();

		return 0;
	}

	protected void doPrint() {
		System.out.println(result);
	}

}
