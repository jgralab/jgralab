package de.uni_koblenz.jgralab.algolib.functions;

import de.uni_koblenz.jgralab.algolib.functions.pairs.DoublePair;

/**
 * Behaves the same way as <code>Function</code>, except that <code>RANGE</code>
 * is replaced by <code>double</code>.
 * 
 * @author strauss@uni-koblenz.de
 * 
 * @param <DOMAIN>
 */
public interface DoubleFunction<DOMAIN> extends Iterable<DoublePair<DOMAIN>>{
	public double get(DOMAIN parameter);

	public void set(DOMAIN parameter, double value);

	public boolean isDefined(DOMAIN parameter);
	
	public Iterable<DOMAIN> getDomainElements();
}
