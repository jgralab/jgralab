package de.uni_koblenz.jgralab.algolib.functions.entries;

public class BooleanFunctionEntry<A> {
	private A first;
	private boolean second;

	public BooleanFunctionEntry(A first, boolean second) {
		super();
		this.first = first;
		this.second = second;
	}

	public A getFirst() {
		return first;
	}

	public boolean getSecond() {
		return second;
	}
}
