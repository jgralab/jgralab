package de.uni_koblenz.jgralab.algolib.functions;

import de.uni_koblenz.jgralab.algolib.functions.entries.LongFunctionEntry;

/**
 * Behaves the same way as <code>Function</code>, except that <code>RANGE</code>
 * is replaced by <code>long</code>.
 * 
 * @author strauss@uni-koblenz.de
 * 
 * @param <DOMAIN>
 */
public interface LongFunction<DOMAIN> extends Iterable<LongFunctionEntry<DOMAIN>> {
	public long get(DOMAIN parameter);

	public void set(DOMAIN parameter, long value);

	public boolean isDefined(DOMAIN parameter);

	public Iterable<DOMAIN> getDomainElements();
}
