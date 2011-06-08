package de.uni_koblenz.jgralab.gretl;

/**
 * A {@link Transformation} that returns the number of applications. That number
 * is used at {@link Iteratively} to check if another iteration should be
 * performed. No-ops (in terms of graph changes) like {@link SysOut} or
 * {@link PrintGraph} should always return zero.
 * 
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 */
public abstract class CountingTransformation extends Transformation<Integer> {
	public CountingTransformation(Context c) {
		super(c);
	}
}
