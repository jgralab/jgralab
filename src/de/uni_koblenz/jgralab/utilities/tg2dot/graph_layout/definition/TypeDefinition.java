package de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition;

import de.uni_koblenz.jgralab.schema.AttributedElementClass;

/**
 * A TypeDefinition is a description of graph elements of a specific schema type
 * and their style attributes.
 * 
 * @author ist@uni-koblenz.de
 */
public class TypeDefinition extends AbstractDefinition {

	/**
	 * The {@link AttributedElementClass} this definition describes.
	 */
	private AttributedElementClass typeClass;

	/**
	 * Constructs a TypeDefinition for a {@link AttributedElementClass} and
	 * initializes all data structures.
	 * 
	 * @param type
	 *            {@link AttributedElementClass} this definition describes.
	 */
	public TypeDefinition(AttributedElementClass type) {
		super();

		typeClass = type;
	}

	/**
	 * Clones an existing TypeDefinition by initializing new data structures and
	 * copying all used references.
	 * 
	 * @param definition
	 *            TypeDefinition, which should be cloned.
	 */
	public TypeDefinition(TypeDefinition definition) {
		super(definition);
		typeClass = definition.typeClass;
	}

	/**
	 * Returns the AttributedElementClass this TypeDefinition defines.
	 * 
	 * @return Associated {@link AttributedElementClass}.
	 */
	public AttributedElementClass getTypeClass() {
		return typeClass;
	}

	@Override
	public TypeDefinition clone() {
		return new TypeDefinition(this);
	}
}
