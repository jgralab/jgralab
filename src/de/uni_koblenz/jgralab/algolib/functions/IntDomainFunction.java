package de.uni_koblenz.jgralab.algolib.functions;

/**
 * Behaves the same way as <code>Function</code>, except that
 * <code>DOMAIN</code> is replaced by <code>int</code>.
 * 
 * @author strauss@uni-koblenz.de
 * 
 * @param <RANGE>
 *            the range of the function.
 */
public interface IntDomainFunction<RANGE> {

	public RANGE get(int parameter);

	public void set(int parameter, RANGE value);

	public boolean isDefined(int parameter);
}
