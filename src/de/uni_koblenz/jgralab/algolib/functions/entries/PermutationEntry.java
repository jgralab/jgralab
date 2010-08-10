package de.uni_koblenz.jgralab.algolib.functions.entries;

public class PermutationEntry<B> {
	private int first;
	private B second;

	public PermutationEntry(int first, B second) {
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
