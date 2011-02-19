/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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

package de.uni_koblenz.jgralab.greql2.evaluator;

import java.util.Iterator;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VariableEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.schema.SimpleDeclaration;
import de.uni_koblenz.jgralab.greql2.schema.Variable;

/**
 * This class models the declaration of one variable. It allowes the iteration
 * over all possible values using the method iterate(). THe current value of the
 * variable is stored as temporary attribute at the variable vertex
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class VariableDeclaration {

	/**
	 * Holds the set of possible values the variable may have
	 */
	private JValueCollection definitionSet;

	private AbstractGraphMarker<AttributedElement> subgraph;

	/**
	 * Holds the variable-vertex of this declaration.
	 */
	private VariableEvaluator variableEval;

	/**
	 * @return the variableEval
	 */
	VariableEvaluator getVariableEval() {
		return variableEval;
	}

	private VertexEvaluator definitionSetEvaluator;

	/**
	 * Used for simple Iteration over the possible values
	 */
	private Iterator<JValue> iter = null;

	/**
	 * Creates a new VariableDeclaration for the given Variable and the given
	 * JValue
	 * 
	 * @param var
	 *            the Variable-Vertex in the GReQL-Syntaxgraph to create a
	 *            VariableDeclaration for
	 * @param definitionSet
	 *            the set of possible values this variable may have
	 * @param decl
	 *            the SimpleDeclaration which declares the variable
	 * @param eval
	 *            the GreqlEvaluator which is used to evaluate the query
	 */
	public VariableDeclaration(Variable var,
			VertexEvaluator definitionSetEvaluator,
			AbstractGraphMarker<AttributedElement> subgraph2,
			SimpleDeclaration decl, GreqlEvaluator eval) {
		variableEval = (VariableEvaluator) definitionSetEvaluator
				.getVertexEvalMarker().getMark(var);
		this.definitionSetEvaluator = definitionSetEvaluator;
		this.subgraph = subgraph2;
	}

	/**
	 * The current iteration number. counts from 1 to definitionSet.size().
	 */
	private int iterationNumber = 0;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(((Variable) variableEval.getVertex()).get_name());
		sb.append(" = ");
		sb.append(getVariableValue());
		sb.append(" [");
		sb.append(iterationNumber);
		sb.append('/');
		sb.append(definitionSet.size());
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Iterates over all possible values for this variable. Returns true if
	 * another value was found, false otherwise
	 */
	public boolean iterate() {
		iterationNumber++;
		if ((iter != null) && (iter.hasNext())) {
			// JValue old = getVariableValue();
			variableEval.setValue(iter.next());
			// assert !getVariableValue().equals(old) :
			// "Iterating over the same element twice!!!";
			return true;
		}
		return false;
	}

	/**
	 * returns the current value of the represented variable. used only for
	 * debugging
	 */
	public JValue getVariableValue() {
		return variableEval.getValue();
	}

	/**
	 * Resets the iterator to the first element
	 */
	protected void reset() {
		iterationNumber = 0;
		variableEval.setValue(null);
		JValue tempAttribute = definitionSetEvaluator.getResult(subgraph);
		if (tempAttribute.isCollection()) {
			try {
				JValueCollection col = tempAttribute.toCollection();
				definitionSet = col.toJValueSet();
				if (col.size() > definitionSet.size()) {
					throw new EvaluateException(
							"A collection that doesn't fulfill the set property is used as variable range definition");
				}
			} catch (JValueInvalidTypeException exception) {
				throw new EvaluateException(
						"Error evaluating a SimpleDeclaration : "
								+ exception.toString());
			}
		} else {
			definitionSet = new JValueSet();
			definitionSet.add(tempAttribute);
		}
		iter = definitionSet.iterator();
	}

	/**
	 * Returns the cardinality of the collection this variable is bound to
	 * 
	 * @return the cardinality of the collection this variable is bound to
	 */
	public int getDefinitionCardinality() {
		// return definitionSet.size();
		return 40;
	}

}
