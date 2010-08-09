package de.uni_koblenz.jgralab.algolib.functions.pairs;

public class IntPair<A> {
	private A first;
	private int second;

	public IntPair(A first, int second) {
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
