package de.uni_koblenz.jgralab.algolib;

import de.uni_koblenz.jgralab.Vertex;

public class VertexPair {
	private Vertex v1;
	private Vertex v2;

	public VertexPair(Vertex v1, Vertex v2) {
		super();
		this.v1 = v1;
		this.v2 = v2;
	}

	public Vertex getV1() {
		return v1;
	}

	public void setV1(Vertex v1) {
		this.v1 = v1;
	}

	public Vertex getV2() {
		return v2;
	}

	public void setV2(Vertex v2) {
		this.v2 = v2;
	}

}
