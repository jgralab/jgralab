package de.uni_koblenz.jgralab.algolib.functions.pairs;

public class DoublePair<A> {
	private A first;
	private double second;

	public DoublePair(A first, double second) {
		super();
		this.first = first;
		this.second = second;
	}

	public A getFirst() {
		return first;
	}

	public double getSecond() {
		return second;
	}
}
