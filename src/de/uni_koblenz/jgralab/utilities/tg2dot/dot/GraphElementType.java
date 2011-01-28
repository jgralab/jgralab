package de.uni_koblenz.jgralab.utilities.tg2dot.dot;

/**
 * Lists all supported graph element types in DOT.
 * 
 * @author ist@uni-koblenz.de
 */
public enum GraphElementType {

	Node("node"), Edge("edge"), Graph("graph");

	/**
	 * Attribute holding the GraphElementType name.
	 */
	public String name;

	GraphElementType(String name) {
		this.name = name;
	}
}
