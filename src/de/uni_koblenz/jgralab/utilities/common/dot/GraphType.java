package de.uni_koblenz.jgralab.utilities.common.dot;

/**
 * Lists supported graph types in DOT.
 * 
 * @author mmce
 */
public enum GraphType {

	DIRECTED("digraph"), UNDIRECTED("graph");

	/**
	 * Holding the GraphType name;
	 */
	public String name;

	GraphType(String name) {
		this.name = name;
	}
}
