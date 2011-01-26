package de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition;

import java.util.List;

import de.uni_koblenz.jgralab.AttributedElement;

/**
 * A ElementDefinition is a description of a set of graph elements described by
 * GReQL-query and their style attributes.
 * 
 * @author ist@uni-koblenz.de
 */
public class ElementDefinition extends AbstractDefinition {

	/**
	 * GReQL-string describing the set of elements specified by this
	 * ElementDefinition.
	 */
	private String elementDefinitionQuery;

	/**
	 * List of all AttributedElements described by the GReQL-query stored in
	 * {@link ElementDefinition#elementDefinitionQuery}.
	 */
	protected List<AttributedElement> containedElements;

	/**
	 * 
	 */
	public ElementDefinition(String elementDefinitionQuery) {
		this.elementDefinitionQuery = elementDefinitionQuery;
	}

	/**
	 * Clones an ElementDefinition by initializing all data structures and
	 * copying all references.
	 * 
	 * @param definition
	 *            ElementDefinition, which is cloned.
	 */
	public ElementDefinition(ElementDefinition definition) {
		super(definition);
		elementDefinitionQuery = definition.elementDefinitionQuery;
	}

	/**
	 * Returns the GReQL-string describing the current set of
	 * {@link AttributedElement} this definition describes.
	 * 
	 * @return The GReQL-string.
	 */
	public String getGreqlString() {
		return elementDefinitionQuery;
	}

	/**
	 * Determines whether or not the given {@link AttributedElement} is included
	 * in this ElementDefinition.
	 * 
	 * @param element
	 *            AttributedElement in question.
	 * @return True, iff this definition contains the given element.
	 */
	public boolean hasElement(AttributedElement element) {
		return containedElements.contains(element);
	}

	/**
	 * Adds an AttributedElement to the list of contained elements.
	 * 
	 * @param element
	 *            {@link AttributedElement} to be added.
	 */
	public void add(AttributedElement element) {
		containedElements.add(element);
	}

	@Override
	public Definition clone() {
		return new ElementDefinition(this);
	}
}
