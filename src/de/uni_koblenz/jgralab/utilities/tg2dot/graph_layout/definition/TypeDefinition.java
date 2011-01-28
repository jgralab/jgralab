package de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition;

import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Schema;

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
	protected TypeDefinition(AttributedElementClass type) {
		super();

		typeClass = type;
	}

	/**
	 * Constructs a TypeDefinition from a {@link TemporaryDefinitionStruct} and
	 * its corresponding {@link Schema} and copy all attributes.
	 * 
	 * @param schema
	 *            Schema corresponding to the type specified by the
	 *            {@link TemporaryDefinitionStruct}.
	 * @param struct
	 *            {@link TemporaryDefinitionStruct} from which this definition
	 *            is created from.
	 */
	protected TypeDefinition(Schema schema, TemporaryDefinitionStruct struct) {
		super(struct);

		typeClass = schema.getAttributedElementClass(struct.name);
		if (typeClass == null) {
			throw new RuntimeException(
					"There is no AttributedElementClass this TemporaryDefinitionStruct can be associated to. Its type is: "
							+ struct.name);
		}
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
