package de.uni_koblenz.jgralab.algolib.functions.entries;

public class DoubleFunctionEntry<A> {
	private A first;
	private double second;

	public DoubleFunctionEntry(A first, double second) {
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
