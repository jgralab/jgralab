package de.uni_koblenz.jgralab.algolib.functions.adapters;

import java.util.Iterator;

import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.entries.BooleanFunctionEntry;

public abstract class MethodCallToBooleanFunctionAdapter<DOMAIN> implements
		BooleanFunction<DOMAIN> {

	@Override
	public Iterable<DOMAIN> getDomainElements() {
		throw new UnsupportedOperationException(
				"This function is a method call and cannot iterate over the domain elements.");
	}

	@Override
	public void set(DOMAIN parameter, boolean value) {
		throw new UnsupportedOperationException("This function is immutable.");
	}

	@Override
	public Iterator<BooleanFunctionEntry<DOMAIN>> iterator() {
		throw new UnsupportedOperationException(
				"This function is a method call and has no function entries to iterate over.");
	}

}
