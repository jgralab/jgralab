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

package de.uni_koblenz.jgralab.greql.evaluator;

import java.util.Iterator;

import org.pcollections.PSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql.evaluator.vertexeval.VariableEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql.exception.GreqlException;
import de.uni_koblenz.jgralab.greql.schema.Expression;
import de.uni_koblenz.jgralab.greql.schema.Variable;
import de.uni_koblenz.jgralab.greql.types.Undefined;

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
	private PSet<Object> definitionSet;

	/**
	 * Holds the variable-vertex of this declaration.
	 */
	private final VariableEvaluator<Variable> variableEval;

	/**
	 * @return the variableEval
	 */
	VariableEvaluator<Variable> getVariableEval() {
		return variableEval;
	}

	private final VertexEvaluator<? extends Expression> definitionSetEvaluator;

	/**
	 * Used for simple Iteration over the possible values
	 */
	private Iterator<Object> iter = null;

	/**
	 * Creates a new VariableDeclaration for the given Variable and the given
	 * JValue
	 * 
	 * @param var
	 *            the Variable-Vertex in the GReQL-Syntaxgraph to create a
	 *            VariableDeclaration for
	 * @param definitionSetEvaluator
	 *            the evaluator for the set of possible values this variable may
	 *            have
	 * @param variableEvaluator
	 *            the {@link VariableEvaluator} of the represented Variable
	 *            vertex
	 */
	public VariableDeclaration(Variable var,
			VertexEvaluator<? extends Expression> definitionSetEvaluator,
			VariableEvaluator<Variable> variableEvaluator) {
		variableEval = variableEvaluator;
		definitionSet = JGraLab.set();
		this.definitionSetEvaluator = definitionSetEvaluator;
	}

	/**
	 * The current iteration number. counts from 1 to definitionSet.size().
	 */
	private int iterationNumber = 0;

	@Override
	public String toString() {
		System.out
				.println("Warning: Use the toString(GreqlEvaluatorImpl) method for VariableDeclarations.");
		return super.toString();
	}

	public String toString(InternalGreqlEvaluator evaluator) {
		StringBuilder sb = new StringBuilder();
		sb.append(variableEval.getVertex().get_name());
		sb.append(" = ");
		sb.append(getVariableValue(evaluator));
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
	 * 
	 * @param evaluator
	 */
	public boolean iterate(InternalGreqlEvaluator evaluator) {
		iterationNumber++;
		if ((iter != null) && (iter.hasNext())) {
			// JValue old = getVariableValue();
			variableEval.setValue(iter.next(), evaluator);
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
	public Object getVariableValue(InternalGreqlEvaluator evaluator) {
		return variableEval.getValue(evaluator);
	}

	/**
	 * Resets the iterator to the first element
	 */
	protected void reset(InternalGreqlEvaluator evaluator) {
		iterationNumber = 0;
		variableEval.setValue(Undefined.UNDEFINED, evaluator);
		Object tempAttribute = definitionSetEvaluator.getResult(evaluator);
		if (tempAttribute instanceof PVector) {
			PVector<?> col = (PVector<?>) tempAttribute;
			definitionSet = JGraLab.set().plusAll(col);
			if (col.size() > definitionSet.size()) {
				throw new GreqlException(
						"A collection that doesn't fulfill the set property is used as variable range definition");
			}

		} else if (tempAttribute instanceof PSet) {
			@SuppressWarnings("unchecked")
			PSet<Object> s = (PSet<Object>) tempAttribute;
			definitionSet = s;
		} else {
			definitionSet = JGraLab.set().plus(tempAttribute);
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
