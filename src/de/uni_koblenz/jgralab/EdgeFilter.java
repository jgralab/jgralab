package de.uni_koblenz.jgralab;

public interface EdgeFilter<E extends Edge> {
	public boolean accepts(E edge);
}
