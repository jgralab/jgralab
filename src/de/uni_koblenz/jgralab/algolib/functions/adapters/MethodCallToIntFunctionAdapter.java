package de.uni_koblenz.jgralab.algolib.functions.adapters;

import java.util.Iterator;

import de.uni_koblenz.jgralab.algolib.functions.IntFunction;
import de.uni_koblenz.jgralab.algolib.functions.entries.IntFunctionEntry;

public abstract class MethodCallToIntFunctionAdapter<DOMAIN> implements
		IntFunction<DOMAIN> {

	@Override
	public Iterable<DOMAIN> getDomainElements() {
		throw new UnsupportedOperationException(
				"This function is a method call and cannot iterate over the domain elements.");
	}

	@Override
	public void set(DOMAIN parameter, int value) {
		throw new UnsupportedOperationException("This function is immutable.");
	}

	@Override
	public Iterator<IntFunctionEntry<DOMAIN>> iterator() {
		throw new UnsupportedOperationException(
				"This function is a method call and has no function entries to iterate over.");
	}

}
