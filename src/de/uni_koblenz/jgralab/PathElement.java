package de.uni_koblenz.jgralab;

/**
 * Represents a path element in a path description given to
 * {@link Vertex#reachableVertices(Class, PathElement...)}.
 * 
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 */
public class PathElement {
	public Class<? extends Edge> edgeClass;
	public EdgeDirection edgeDirection;
	public boolean strictType = false;

	/**
	 * see {@link #PathElement(Class, EdgeDirection, boolean)}.
	 * <code>stricts</code> defaults to false.
	 */
	public PathElement(Class<? extends Edge> ec, EdgeDirection ed) {
		this.edgeClass = ec;
		this.edgeDirection = ed;
	}

	/**
	 * @param ec
	 *            the class of allowed edges
	 * @param ed
	 *            the direction of allowed edges
	 * @param strict
	 *            allow only the exact type
	 */
	public PathElement(Class<? extends Edge> ec, EdgeDirection ed,
			boolean strict) {
		this(ec, ed);
		this.strictType = strict;
	}
}
