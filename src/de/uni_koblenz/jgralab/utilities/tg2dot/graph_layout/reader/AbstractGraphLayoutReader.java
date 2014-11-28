/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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
package de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.reader;

import java.util.Collection;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.GraphLayout;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.Definition;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.ElementDefinition;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.TypeDefinition;
import de.uni_koblenz.jgralab.utilities.tg2dot.greql.GreqlEvaluatorFacade;

/**
 * Reads a graph layout in and produces a list of TemporaryDefinitionStructs and
 * global variables.
 * 
 * @author ist@uni-koblenz.de
 */
public abstract class AbstractGraphLayoutReader implements GraphLayoutReader {

	protected GraphLayout graphLayout;
	protected Definition currentDefinition;
	private GreqlEvaluatorFacade evaluator;

	/**
	 * Creates a AbstractGraphLayoutReader for reading a graph layout and
	 * initializes all data structures.
	 */
	public AbstractGraphLayoutReader(GreqlEvaluatorFacade evaluator) {
		this.evaluator = evaluator;
		initilizeStates();
	}

	/**
	 * Initialization all data structures to read in graph layouts as
	 * {@link TemporaryDefinitionStruct}s.
	 */
	protected void initilizeStates() {
	}

	/**
	 * A new definition with a name has been started.
	 * 
	 * @param definitionName
	 *            Name of the definition.
	 */
	protected void definitionStarted(String definitionName) {

		TypeDefinition definition = null;

		definition = graphLayout.getTypeDefinition(definitionName);

		currentDefinition = definition;
		if (definition == null && isElementDefinition(definitionName)) {
			currentDefinition = new ElementDefinition(definitionName);
			graphLayout.add(currentDefinition);
		}
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
		return containsGraphElements(evaluator.evaluate(text));
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
	private boolean containsGraphElements(Object result) {
		if (result instanceof GraphElement) {
			return true;
		}
		if (result instanceof Collection) {
			for (Object o : (Collection<?>) result) {
				if (o instanceof GraphElement) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * The current definition has ended.
	 */
	protected void definitionEnded() {
		currentDefinition = null;
	}

	/**
	 * Checks, if this given String could be a global variable.
	 * 
	 * @param string
	 *            String to check.
	 * @return Returns true, iff the String is naming a global variable.
	 */
	protected boolean isGlobalVariable(String string) {
		return string.charAt(0) == '@';
	}

	/**
	 * Processes a global variable field event and adds the variable to the
	 * global variable list.
	 * 
	 * @param name
	 *            Name of the field.
	 * @param value
	 *            Value of the field.
	 */
	public void processGlobalVariable(String name, String value) {
		if (isGlobalVariable(name)) {
			graphLayout.getGlobalVariables().put(removeFirstChar(name), value);
		} else {
			throw new RuntimeException("Field " + name
					+ " does not have a '@' as prefix. Delete it or add an @.");
		}
	}

	/**
	 * Processes a definition attribute field by adding an attribute to the
	 * current temporary definition.
	 * 
	 * @param name
	 *            Name of the field.
	 * @param value
	 *            Value of the field.
	 */
	public void processDefinitionAttribute(String name, String value) {
		currentDefinition.setAttribute(name, value);
	}

	/**
	 * Removes the first character of a String.
	 * 
	 * @param name
	 *            String with an unwanted first character.
	 * @return String without the first character.
	 */
	private String removeFirstChar(String name) {
		return name.substring(1, name.length());
	}
}
