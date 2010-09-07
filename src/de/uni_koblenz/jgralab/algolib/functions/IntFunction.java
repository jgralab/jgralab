package de.uni_koblenz.jgralab.algolib.functions;

import de.uni_koblenz.jgralab.algolib.functions.entries.IntFunctionEntry;

/**
 * Behaves the same way as <code>Function</code>, except that <code>RANGE</code>
 * is replaced by <code>int</code>.
 * 
 * @author strauss@uni-koblenz.de
 * 
 * @param <DOMAIN>
 */
public interface IntFunction<DOMAIN> extends Iterable<IntFunctionEntry<DOMAIN>> {
	public int get(DOMAIN parameter);

	public void set(DOMAIN parameter, int value);

	public boolean isDefined(DOMAIN parameter);

	public Iterable<DOMAIN> getDomainElements();
}
