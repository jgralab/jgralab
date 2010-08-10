package de.uni_koblenz.jgralab.algolib.functions.entries;

public class LongFunctionEntry<A> {
	private A first;
	private long second;

	public LongFunctionEntry(A first, long second) {
		super();
		this.first = first;
		this.second = second;
	}

	public A getFirst() {
		return first;
	}

	public long getSecond() {
		return second;
	}
}
