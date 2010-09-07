package de.uni_koblenz.jgralab.algolib.functions.adapters;

import java.util.Iterator;

import de.uni_koblenz.jgralab.algolib.functions.Function;
import de.uni_koblenz.jgralab.algolib.functions.entries.FunctionEntry;

public abstract class MethodCallToFunctionAdapter<DOMAIN, RANGE> implements
		Function<DOMAIN, RANGE> {

	@Override
	public Iterable<DOMAIN> getDomainElements() {
		throw new UnsupportedOperationException(
				"This function is a method call and cannot iterate over the domain elements.");
	}

	@Override
	public void set(DOMAIN parameter, RANGE value) {
		throw new UnsupportedOperationException("This function is immutable.");
	}

	@Override
	public Iterator<FunctionEntry<DOMAIN, RANGE>> iterator() {
		throw new UnsupportedOperationException(
				"This function is a method call and has no function entries to iterate over.");
	}

}
