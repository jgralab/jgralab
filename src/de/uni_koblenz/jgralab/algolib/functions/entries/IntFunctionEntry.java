package de.uni_koblenz.jgralab.algolib.functions.entries;

public class IntFunctionEntry<A> {
	private A first;
	private int second;

	public IntFunctionEntry(A first, int second) {
		super();
		this.first = first;
		this.second = second;
	}

	public A getFirst() {
		return first;
	}

	public int getSecond() {
		return second;
	}
}
