package de.uni_koblenz.jgralab.algolib.functions.entries;

public class FunctionEntry<A, B> {
	private A first;
	private B second;

	public FunctionEntry(A first, B second) {
		super();
		this.first = first;
		this.second = second;
	}

	public A getFirst() {
		return first;
	}

	public B getSecond() {
		return second;
	}

}
