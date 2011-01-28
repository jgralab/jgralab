package de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition;

import java.util.HashMap;
import java.util.Map;

/**
 * A TemporaryDefinitionStruct serves as a preliminary storage object for
 * {@link Definition}s.
 * 
 * @author ist@uni-koblenz.de
 */
public class TemporaryDefinitionStruct {

	/**
	 * Name of the {@link Definition}.
	 */
	public String name;

	/**
	 * Map of attribute names as key and GReQL-queries as values.
	 */
	private Map<String, String> attributeList;

	/**
	 * Constructs and initializes all data structures.
	 */
	{
		attributeList = new HashMap<String, String>();
	}

	/**
	 * Adds an attribute to the {@link TemporaryDefinitionStruct#attributeList}.
	 * Adding an existing attribute will result in an overwrite of the
	 * pre-existing attribute.
	 * 
	 * @param name
	 *            Name of the attribute.
	 * @param value
	 *            Value of the attribute.
	 */
	public void addAttribute(String name, String value) {
		attributeList.put(name, value);
	}

	/**
	 * Returns the attribute list as Map.
	 * 
	 * @return Map of attribute names as key and GReQL-queries as values.
	 */
	public Map<String, String> getAttributeList() {
		return attributeList;
	}
}
