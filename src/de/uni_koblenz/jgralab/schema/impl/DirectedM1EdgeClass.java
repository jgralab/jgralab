package de.uni_koblenz.jgralab.schema.impl;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;

public class DirectedM1EdgeClass {

	Class<? extends Edge> edgeClass;

	EdgeDirection dir;

	public DirectedM1EdgeClass(Class<? extends Edge> ec, EdgeDirection dir) {
		edgeClass = ec;
		this.dir = dir;
	}

	public Class<? extends Edge> getM1Class() {
		return edgeClass;
	}

	public EdgeDirection getDirection() {
		return dir;
	}

}
