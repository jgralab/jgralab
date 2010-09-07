package de.uni_koblenz.jgralab.algolib.functions.adapters;

import de.uni_koblenz.jgralab.algolib.functions.BinaryFunction;

public abstract class MethodCallToBinaryFunctionAdapter<DOMAIN1, DOMAIN2, RANGE>
		implements BinaryFunction<DOMAIN1, DOMAIN2, RANGE> {

	@Override
	public void set(DOMAIN1 parameter1, DOMAIN2 parameter2, RANGE value) {
		throw new UnsupportedOperationException("This function is immutable.");
	}

}
