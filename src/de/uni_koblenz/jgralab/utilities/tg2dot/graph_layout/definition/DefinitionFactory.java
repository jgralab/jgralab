/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 * 
 * Additional permission under GNU GPL version 3 section 7
 * 
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
package de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.tg2dot.greql2.GreqlEvaluatorFacade;

/**
 * A definition factory used to create specific definitions from
 * {@link TemporaryDefinitionStruct}.
 */
public class DefinitionFactory {

	/**
	 * {@link GreqlEvaluatorFacade} used for evaluating GReQL-queries.
	 */
	private GreqlEvaluatorFacade evaluator;

	/**
	 * The current used {@link Schema}.
	 */
	private Schema schema;

	/**
	 * Constructs a DefinitionFactory with the provided
	 * {@link GreqlEvaluatorFacade}.
	 * 
	 * @param evaluator
	 *            Evaluator.
	 */
	public DefinitionFactory(GreqlEvaluatorFacade evaluator, Schema schema) {
		this.evaluator = evaluator;
		this.schema = schema;
	}

	/**
	 * Produces a definition from a {@link TemporaryDefinitionStruct}.
	 * 
	 * @param struct
	 *            {@link TemporaryDefinitionStruct} used to cread a specific
	 *            definition.
	 * @return Specific definition.
	 */
	public Definition produce(TemporaryDefinitionStruct struct) {

		DefinitionType type = determineDefinitionType(struct.name);

		return create(struct, type);
	}

	/**
	 * Determines the definition type of the current
	 * {@link TemporaryDefinitionStruct}.
	 * 
	 * @param text
	 *            Definition type as String.
	 * @return {@link DefinitionType}.
	 */
	private DefinitionType determineDefinitionType(String text) {
		boolean isTypeDefinition = isTypeDefinition(text);
		boolean isElementDefinition = isTypeDefinition ? false
				: isElementDefinition(text);

		return determineDefinitionType(isTypeDefinition, isElementDefinition);
	}

	/**
	 * Looks up in the current schema if the provided AttributedElement name
	 * exists.
	 * 
	 * @param attributedElementName
	 *            Fully qualified name of the AttributedElement.
	 * @return True in case of a TypeDefinition.
	 */
	private boolean isTypeDefinition(String attributedElementName) {
		AttributedElementClass clazz = schema
				.getAttributedElementClass(attributedElementName);
		return clazz != null;
	}

	/**
	 * Determines with the GReQL-Evaluator if the provided type is a
	 * GReQL-query.
	 * 
	 * @param text
	 *            GReQL-query describing a set of {@link AttributedElement}.
	 * @return True in case of an GReQL-query with an {@link AttributedElement}
	 *         or a collection of {@link AttributedElement}s as result.
	 */
	private boolean isElementDefinition(String text) {
		boolean isElementDefinition = false;
		try {
			JValue result = evaluator.evaluate(text);
			isElementDefinition = isElementDefinition(result);
		} catch (EvaluateException ex) {

		} catch (JValueInvalidTypeException ex) {
			errorMessageNotASet(text);
		}
		return isElementDefinition;
	}

	/**
	 * Determines if the given JValue is an {@link AttributedElement} or a
	 * collection of {@link AttributedElement}s.
	 * 
	 * @param result
	 *            JValue wrapping a query result.
	 * @return True iff JValue is a {@link AttributedElement}s or a collection
	 *         of {@link AttributedElement}s.
	 */
	private boolean isElementDefinition(JValue result) {

		boolean isValid = result.isVertex() || result.isEdge();
		if (result.isCollection()) {
			JValueSet set = result.toJValueSet();
			for (JValue element : set) {
				isValid = element.isVertex() || element.isEdge();
				break;
			}
		}
		return isValid;
	}

	/**
	 * Determines the DefinitionType with to given boolean values.
	 * 
	 * @param isTypeDefinition
	 *            Indicating that this definition is a TypeDefinition.
	 * @param isElementDefinition
	 *            Indicating that this definition is a ElementDefinition.
	 * @return DefintionType.
	 */
	private DefinitionType determineDefinitionType(boolean isTypeDefinition,
			boolean isElementDefinition) {

		DefinitionType definitionType = DefinitionType.None;

		definitionType = isTypeDefinition ? DefinitionType.Type
				: definitionType;
		definitionType = isElementDefinition ? DefinitionType.Element
				: definitionType;

		return definitionType;
	}

	/**
	 * Creates a definition from a {@link TemporaryDefinitionStruct} and a
	 * {@link DefinitionType}.
	 * 
	 * @param struct
	 *            {@link TemporaryDefinitionStruct} used to create a specific
	 *            definition.
	 * @param type
	 *            {@link DefinitionType} specifying the definition type.
	 * @return Specific Definition.
	 */
	private Definition create(TemporaryDefinitionStruct struct,
			DefinitionType type) {

		switch (type) {

		case Type:
			return createTypeDefinition(struct);
		case Element:
			return createElementDefinition(struct);
		default:
			return createEmtpyDefinition(struct);
		}
	}

	/**
	 * Creates an {@link EmptyDefinition}.
	 * 
	 * @param struct
	 *            {@link TemporaryDefinitionStruct} used to create a definition.
	 * @return {@link EmptyDefinition}.
	 */
	private Definition createEmtpyDefinition(TemporaryDefinitionStruct struct) {
		return new EmptyDefinition(struct);
	}

	/**
	 * Creates an {@link TypeDefinition}.
	 * 
	 * @param struct
	 *            {@link TemporaryDefinitionStruct} used to create a definition.
	 * @return {@link TypeDefinition}.
	 */
	private Definition createTypeDefinition(TemporaryDefinitionStruct struct) {
		return new TypeDefinition(schema, struct);
	}

	/**
	 * Creates an {@link ElementDefinition}.
	 * 
	 * @param struct
	 *            {@link TemporaryDefinitionStruct} used to create a definition.
	 * @return {@link ElementDefinition}.
	 */
	private Definition createElementDefinition(TemporaryDefinitionStruct struct) {
		return new ElementDefinition(struct);
	}

	private void errorMessageNotASet(String text) {
		System.err.println("The query << " + text
				+ " >> doesn't report a set of vertices or edges.");
	}

	/**
	 * Produces a TypeDefinition for an {@link AttributedElementClass}.
	 * 
	 * @param type
	 *            {@link AttributedElementClass}.
	 * @return TypeDefinition.
	 */
	public TypeDefinition produce(AttributedElementClass type) {
		return new TypeDefinition(type);
	}
}
