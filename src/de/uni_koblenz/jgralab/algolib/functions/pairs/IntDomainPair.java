package de.uni_koblenz.jgralab.algolib.functions.pairs;

public class IntDomainPair<B> {
	private int first;
	private B second;

	public IntDomainPair(int first, B second) {
		this.first = first;
		this.second = second;
	}

	public int getFirst() {
		return first;
	}

	public B getSecond() {
		return second;
	}
}
