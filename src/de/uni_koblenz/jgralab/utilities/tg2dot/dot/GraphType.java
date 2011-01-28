package de.uni_koblenz.jgralab.utilities.tg2dot.dot;

/**
 * Lists supported graph types in DOT.
 * 
 * @author mmce
 */
public enum GraphType {

	Directed("digraph"), Undirected("graph");

	/**
	 * Holding the GraphType name;
	 */
	public String name;

	GraphType(String name) {
		this.name = name;
	}
}
