package de.uni_koblenz.jgralab.utilities.common.dot;

/**
 * Lists all supported graph element types in DOT.
 * 
 * @author ist@uni-koblenz.de
 */
public enum GraphElementType {

	NODE("node"), EDGE("edge"), GRAPH("graph");

	/**
	 * Attribute holding the GraphElementType name.
	 */
	public String name;

	GraphElementType(String name) {
		this.name = name;
	}
}
