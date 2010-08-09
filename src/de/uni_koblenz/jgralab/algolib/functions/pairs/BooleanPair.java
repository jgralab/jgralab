package de.uni_koblenz.jgralab.algolib.functions.pairs;

public class BooleanPair<A> {
	private A first;
	private boolean second;

	public BooleanPair(A first, boolean second) {
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
