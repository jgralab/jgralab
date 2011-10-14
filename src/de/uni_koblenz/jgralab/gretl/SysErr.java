/**
 * 
 */
package de.uni_koblenz.jgralab.gretl;

/**
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 */
public class SysErr extends SysOut {
	public SysErr(Context c, String greqlExpression) {
		super(c, greqlExpression);
	}

	public SysErr(Context c, Object result) {
		super(c, result);
	}

	public static SysErr parseAndCreate(ExecuteTransformation et) {
		et.matchTransformationArrow();
		String semExp = et.matchSemanticExpression();
		return new SysErr(et.context, semExp);
	}

	@Override
	protected void doPrint() {
		System.err.println(result);
	}

}
