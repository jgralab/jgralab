package de.uni_koblenz.jgralab.algolib.functions.adapters;

import de.uni_koblenz.jgralab.algolib.functions.BinaryDoubleFunction;

public abstract class MethodCallToBinaryDoubleFunctionAdapter<DOMAIN1, DOMAIN2>
		implements BinaryDoubleFunction<DOMAIN1, DOMAIN2> {

	@Override
	public void set(DOMAIN1 parameter1, DOMAIN2 parameter2, double value) {
		throw new UnsupportedOperationException("This function is immutable.");
	}

}
