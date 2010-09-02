package de.uni_koblenz.jgralab.algolib.functions.adapters;

import java.util.Iterator;

import de.uni_koblenz.jgralab.algolib.functions.DoubleFunction;
import de.uni_koblenz.jgralab.algolib.functions.entries.DoubleFunctionEntry;

public abstract class MethodCallToDoubleFunctionAdapter<DOMAIN> implements
		DoubleFunction<DOMAIN> {
	@Override
	public Iterable<DOMAIN> getDomainElements() {
		throw new UnsupportedOperationException(
				"This function is a method call and cannot iterate over the domain elements.");
	}

	@Override
	public void set(DOMAIN parameter, double value) {
		throw new UnsupportedOperationException("This function is immutable.");
	}

	@Override
	public Iterator<DoubleFunctionEntry<DOMAIN>> iterator() {
		throw new UnsupportedOperationException(
				"This function is a method call and has no function entries to iterate over.");
	}
}
