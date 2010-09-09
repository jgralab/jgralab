package de.uni_koblenz.jgralab;

public class PathElement {
	public Class<? extends Edge> edgeClass;
	public EdgeDirection edgeDirection;
	public boolean strictType = false;

	public PathElement(Class<? extends Edge> ec, EdgeDirection ed) {
		this.edgeClass = ec;
		this.edgeDirection = ed;
	}

	public PathElement(Class<? extends Edge> ec, EdgeDirection ed,
			boolean strict) {
		this(ec, ed);
		this.strictType = strict;
	}
}
