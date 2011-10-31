package de.uni_koblenz.jgralab.greql2.types;

public final class Undefined {
	public static final Undefined UNDEFINED = new Undefined();

	private Undefined() {
	}

	@Override
	public boolean equals(Object arg0) {
		return false;
	}

	@Override
	public String toString() {
		return "Undefined";
	}
}
