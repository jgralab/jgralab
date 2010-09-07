package de.uni_koblenz.jgralab.algolib.functions;

import de.uni_koblenz.jgralab.algolib.functions.entries.BooleanFunctionEntry;

/**
 * Behaves the same way as <code>Function</code>, except that <code>RANGE</code>
 * is replaced by <code>boolean</code>.
 * 
 * @author strauss@uni-koblenz.de
 * 
 * @param <DOMAIN>
 */
public interface BooleanFunction<DOMAIN> extends Iterable<BooleanFunctionEntry<DOMAIN>>{
	public boolean get(DOMAIN parameter);

	public void set(DOMAIN parameter, boolean value);

	public boolean isDefined(DOMAIN parameter);
	
	public Iterable<DOMAIN> getDomainElements();
}
