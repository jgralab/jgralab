package de.uni_koblenz.jgralab.algolib.functions;

import java.util.Arrays;

public class ArrayFunction<RANGE> implements IntDomainFunction<RANGE> {

	private RANGE[] values;

	public ArrayFunction(RANGE[] values) {
		this.values = values;
	}

	@Override
	public RANGE get(int parameter) {
		return values[parameter];
	}

	@Override
	public boolean isDefined(int parameter) {
		return parameter >= 0 && parameter < values.length
				&& values[parameter] != null;
	}

	@Override
	public void set(int parameter, RANGE value) {
		values[parameter] = value;
	}

	@Override
	public String toString() {
		return Arrays.toString(values);
	}

}
