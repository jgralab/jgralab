package de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.reader;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.GraphLayout;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.Definition;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.ElementDefinition;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.TypeDefinition;
import de.uni_koblenz.jgralab.utilities.tg2dot.greql2.GreqlEvaluatorFacade;

/**
 * Reads a graph layout in and produces a list of TemporaryDefinitionStructs and
 * global variables.
 * 
 * @author ist@uni-koblenz.de
 */
public abstract class AbstractTemporaryGraphLayoutReader implements
		TemporaryGraphLayoutReader {

	protected GraphLayout graphLayout;
	protected Definition currentDefinition;
	private GreqlEvaluatorFacade evaluator;

	/**
	 * Creates a AbstractGraphLayoutReader for reading a graph layout and
	 * initializes all data structures.
	 */
	public AbstractTemporaryGraphLayoutReader(GreqlEvaluatorFacade evaluator) {
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
		TypeDefinition definition = graphLayout
				.getTypeDefinition(definitionName);
		currentDefinition = definition;
		if (definition == null && isElementDefinition(definitionName)) {
			currentDefinition = new ElementDefinition(definitionName);
			graphLayout.add(definition);
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
		boolean isElementDefinition = false;
		try {
			JValue result = evaluator.evaluate(text);
			isElementDefinition = containsAttributedElements(result);
		} catch (EvaluateException ex) {
			// TODO appropriate error message
		} catch (JValueInvalidTypeException ex) {
			// TODO description
			throw new RuntimeException("");
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
	private boolean containsAttributedElements(JValue result) {

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
