package de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.uni_koblenz.jgralab.utilities.dot.DotWriter;

/**
 * Provides the general implementation for {@link Definition}.
 * 
 * @author ist@uni-koblenz.de
 */
public abstract class AbstractDefinition implements Definition {

	/**
	 * A map storing all attributes with their name as key and their GReQL-query
	 * as value.
	 */
	protected Map<String, String> attributes;

	/**
	 * Constructs an AbstractDefinition and initializes all data structures.
	 */
	protected AbstractDefinition() {

		attributes = new HashMap<String, String>();
	}

	/**
	 * Constructs an AbstractDefinition from a AbstractDefinition and copys all
	 * data structures.
	 */
	public AbstractDefinition(AbstractDefinition definition) {
		this();
		attributes = new HashMap<String, String>(definition.attributes);
	}

	@Override
	public String getAttributeValue(String name) {
		return attributes.get(name);
	}

	@Override
	public Set<String> getAttributeNames() {
		return attributes.keySet();
	}

	@Override
	public void setAttribute(String name, String value) {
		attributes.put(name, value);
	}

	// public abstract void validateAttributeNames();

	// TODO implement this mechanism to prevent wrong attribute to occur.
	public void validateAttributeNames(Set<String> allowedNames) {
		for (String attributeName : attributes.keySet()) {
			allowedNames.contains(attributeName);
		}
	}

	@Override
	public void addNonExistingAttributes(Definition spec) {
		for (String name : spec.getAttributeNames()) {
			if (attributes.containsKey(name)) {
				continue;
			}
			attributes.put(name, spec.getAttributeValue(name));
		}
	}

	@Override
	public void overwriteAttributes(Definition spec) {
		for (String name : spec.getAttributeNames()) {
			attributes.put(name, spec.getAttributeValue(name));
		}
	}

	@Override
	public abstract Definition clone();
}
