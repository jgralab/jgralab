package de.uni_koblenz.jgralab.algolib.functions;

import java.util.Arrays;

public class ArrayBinaryFunction<DOMAIN, RANGE> implements
		BinaryFunction<DOMAIN, DOMAIN, RANGE> {
	private RANGE[][] values;
	private IntFunction<DOMAIN> indexMapping;

	public ArrayBinaryFunction(RANGE[][] values,
			IntFunction<DOMAIN> indexMapping) {
		this.values = values;
		this.indexMapping = indexMapping;
	}

	public RANGE[][] getValues() {
		return values;
	}

	@Override
	public RANGE get(DOMAIN parameter1, DOMAIN parameter2) {
		return values[indexMapping.get(parameter1)][indexMapping
				.get(parameter2)];
	}

	@Override
	public boolean isDefined(DOMAIN parameter1, DOMAIN parameter2) {
		return indexMapping.isDefined(parameter1)
				&& indexMapping.isDefined(parameter2);
	}

	@Override
	public void set(DOMAIN parameter1, DOMAIN parameter2, RANGE value) {
		throw new UnsupportedOperationException(
				"This binary function is immutable.");
	}
	
	public String toString(){
		StringBuilder out = new StringBuilder();
		for(int i = 1; i < values.length; i++){
			out.append(Arrays.toString(values[i]));
			out.append('\n');
		}
		return out.toString();
	}
}
