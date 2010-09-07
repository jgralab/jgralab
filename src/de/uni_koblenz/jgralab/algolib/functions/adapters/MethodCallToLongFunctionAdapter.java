package de.uni_koblenz.jgralab.algolib.functions.adapters;

import java.util.Iterator;

import de.uni_koblenz.jgralab.algolib.functions.LongFunction;
import de.uni_koblenz.jgralab.algolib.functions.entries.LongFunctionEntry;

public abstract class MethodCallToLongFunctionAdapter<DOMAIN> implements
		LongFunction<DOMAIN> {

	
	@Override
	public Iterable<DOMAIN> getDomainElements() {
		throw new UnsupportedOperationException(
				"This function is a method call and cannot iterate over the domain elements.");
	}

	@Override
	public void set(DOMAIN parameter, long value) {
		throw new UnsupportedOperationException("This function is immutable.");
	}

	@Override
	public Iterator<LongFunctionEntry<DOMAIN>> iterator() {
		throw new UnsupportedOperationException(
				"This function is a method call and has no function entries to iterate over.");
	}
}
