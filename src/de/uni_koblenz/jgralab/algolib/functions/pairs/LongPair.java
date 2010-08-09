package de.uni_koblenz.jgralab.algolib.functions.pairs;

public class LongPair<A> {
	private A first;
	private long second;

	public LongPair(A first, long second) {
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
