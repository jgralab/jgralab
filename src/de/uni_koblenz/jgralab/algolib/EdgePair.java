package de.uni_koblenz.jgralab.algolib;

import de.uni_koblenz.jgralab.Edge;

public class EdgePair {
	private Edge e1;
	private Edge e2;

	public EdgePair(Edge e1, Edge e2) {
		super();
		this.e1 = e1;
		this.e2 = e2;
	}

	public Edge getE1() {
		return e1;
	}

	public void setE1(Edge e1) {
		this.e1 = e1;
	}

	public Edge getE2() {
		return e2;
	}

	public void setE2(Edge e2) {
		this.e2 = e2;
	}

}
