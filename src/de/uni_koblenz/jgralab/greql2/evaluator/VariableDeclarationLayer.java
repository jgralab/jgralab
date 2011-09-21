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

import java.util.List;

import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.WrongResultTypeException;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;

/**
 * This class models all Variables of one Declaration-Vertex. It allows to
 * iterate over all possible combinations of this variables using the method
 * iterate(). The value of each variable is stored as temporary attribute at the
 * variable-vertex, so the evaluate()-methods don't need to know if the
 * expression is a variable or some other already evaluated expression.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class VariableDeclarationLayer {

	/**
	 * Holds a VariableDeclaration for each Variable which is declared in this
	 * Declaration
	 */
	private List<VariableDeclaration> variableDeclarations;

	/**
	 * this is the list of constraint vertices
	 */
	private List<VertexEvaluator> constraintList;

	/**
	 * true if the next variable iteration is the first one, that means, if
	 * there was no iteration before
	 */
	private boolean firstIteration = true;

	/**
	 * The declaration I belong to.
	 */
	private Declaration declaration = null;

	/**
	 * Creates a new {@link VariableDeclarationLayer} for iterating over all
	 * variable combinations that fulfill the constraints in constraintList.
	 * 
	 * @param vertex
	 * 
	 * @param constraintList
	 *            a list of constraints
	 */
	public VariableDeclarationLayer(Declaration vertex,
			List<VariableDeclaration> varDecls,
			List<VertexEvaluator> constraintList) {
		this.declaration = vertex;
		variableDeclarations = varDecls;
		this.constraintList = constraintList;
	}

	/**
	 * sets the next possible combination of values to the variable-vertices. If
	 * it is called the first time, it returns true if the first possible
	 * combination is valid
	 * 
	 * @return true if another possible combination was found, false otherwise
	 */
	public boolean iterate() {
		StringBuilder sb = null;
		if (GreqlEvaluator.DEBUG_DECLARATION_ITERATIONS) {
			sb = new StringBuilder();
			sb.append("### New Declaration Layer Iteration (");
			sb.append(declaration);
			sb.append(")\n");
		}
		boolean constraintsFullfilled = false;
		if (firstIteration) {
			if (!getFirstCombination()) {
				if (GreqlEvaluator.DEBUG_DECLARATION_ITERATIONS) {
					sb.append("## 1st. iteration: returning false (");
					sb.append(declaration);
					sb.append(")");
					System.out.println(sb.toString());
				}
				return false; // no more combinations exists
			}
			constraintsFullfilled = fullfillsConstraints();
			firstIteration = false;
		}
		while (!constraintsFullfilled) {
			if (!getNextCombination(false)) {
				if (GreqlEvaluator.DEBUG_DECLARATION_ITERATIONS) {
					sb.append("## nth iteration: returning false (");
					sb.append(declaration);
					sb.append(")");
					System.out.println(sb.toString());
				}
				return false; // no more combinations exists
			}
			constraintsFullfilled = fullfillsConstraints();
		}

		if (GreqlEvaluator.DEBUG_DECLARATION_ITERATIONS) {
			boolean first = true;
			for (VariableDeclaration dec : variableDeclarations) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(dec);
			}
			sb.append(" (");
			sb.append(declaration);
			sb.append(")");
			System.out.println(sb.toString());
		}

		return true;
	}

	/**
	 * Gets the first possible Variable Combination
	 * 
	 * @return true if a first combination exists, false otherwise
	 */
	private boolean getFirstCombination() {
		variableDeclarations.get(0).reset();
		return getNextCombination(true);
	}

	/**
	 * Gets the next possible variable combination
	 * 
	 * @return true if a next combination exists, false otherwise
	 */

	private boolean getNextCombination(boolean firstCombination) {

		int pointer = firstCombination ? 0 : variableDeclarations.size() - 1;

		boolean iterate;
		do {
			iterate = false;
			VariableDeclaration currDecl = null; // pointer = 0
			do {
				if (pointer < 0) {
					return false;
				}
				currDecl = variableDeclarations.get(pointer--); // pointer = -1
			} while (!currDecl.iterate());
			pointer += 2; // pointer = 3
			int size = variableDeclarations.size();
			while (pointer < size) {
				currDecl = variableDeclarations.get(pointer++);
				currDecl.reset();
				if (!currDecl.iterate()) {
					pointer -= 2;
					iterate = true;
					break;
				}
			}
		} while (iterate);
		return true;
	}

	/**
	 * Checks if the current variable combination fulfills the constraints.
	 * 
	 * @return true if the combination fulfills the constraint, false otherwise
	 */
	private boolean fullfillsConstraints() {
		if ((constraintList == null) || (constraintList.isEmpty())) {
			return true;
		}
		for (int i = 0; i < constraintList.size(); i++) {
			VertexEvaluator currentEval = constraintList.get(i);
			Object tempResult = currentEval.getResult();

			if (tempResult instanceof Boolean) {
				if ((Boolean) tempResult != Boolean.TRUE) {
					return false;
				}
			} else {
				throw new WrongResultTypeException(currentEval.getVertex(),
						"Boolean", tempResult.getClass().getSimpleName(),
						currentEval.createPossibleSourcePositions());
			}

		}
		return true;
	}

	public void reset() {
		firstIteration = true;
	}
}
