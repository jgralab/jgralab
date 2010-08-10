package de.uni_koblenz.jgralab.algolib.functions;

/**
 * Interface for creating objects defining binary functions. Unlike unary
 * functions defined by the interface <code>Function</code>, this interface does
 * not provide iterators. They would be too expensive. For boolean ranges, the
 * interface <code>Relation</code> should be used. The methods work in analogy
 * to the methods in the interface <code>Function</code>.
 * 
 * @author strauss@uni-koblenz.de
 * 
 * @param <DOMAIN1>
 *            the first domain of this binary function
 * @param <DOMAIN2>
 *            the second domain of this binary function
 * @param <RANGE>
 *            the range of this binary function
 */
public interface BinaryFunction<DOMAIN1, DOMAIN2, RANGE> {
	public RANGE get(DOMAIN1 parameter1, DOMAIN2 parameter2);

	public void set(DOMAIN1 parameter1, DOMAIN2 parameter2, RANGE value);

	public void isDefined(DOMAIN1 parameter1, DOMAIN2 parameter2);

}
