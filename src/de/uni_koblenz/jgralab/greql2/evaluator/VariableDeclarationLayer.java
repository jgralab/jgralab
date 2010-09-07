/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 * 
 *               ist@uni-koblenz.de
 * 
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.uni_koblenz.jgralab.greql2.evaluator;

import java.util.List;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.logging.EvaluationLogger;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.DeclarationEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.exception.WrongResultTypeException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;

/**
 * This class models all Variables of one Declaration-Vertex. It allowes to
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

	private EvaluationLogger logger;
	private int possibleCombinations = 0;

	/**
	 * The declaration I belong to.
	 */
	private Declaration declaration = null;

	/**
	 * Creates a new {@link VariableDeclarationLayer} for iterating over all
	 * variable combinations that fulfil the constraints in constraintList.
	 * 
	 * @param vertex
	 * 
	 * @param constraintList
	 *            a list of constraints
	 * @param logger
	 *            since the result of a {@link DeclarationEvaluator} is a
	 *            {@link VariableDeclarationLayer} object, the
	 *            {@link EvaluationLogger} cannot log the result size (which is
	 *            the number of variable combinations that fulfil the
	 *            constraints) before iterating the layer. So the logging of the
	 *            result size {@link Declaration} is done here.
	 */
	public VariableDeclarationLayer(Declaration vertex,
			List<VariableDeclaration> varDecls,
			List<VertexEvaluator> constraintList, EvaluationLogger logger) {
		this.declaration = vertex;
		variableDeclarations = varDecls;
		this.constraintList = constraintList;
		this.logger = logger;
	}

	/**
	 * sets the next possible combination of values to the variable-vertices. If
	 * it is called the first time, it returns true if the first possible
	 * combination is valid
	 * 
	 * @return true if another possible combination was found, false otherwise
	 */
	public boolean iterate(AbstractGraphMarker<AttributedElement> subgraph)
			throws EvaluateException {
		StringBuilder sb = null;
		if (GreqlEvaluator.DEBUG_DECLARATION_ITERATIONS) {
			sb = new StringBuilder();
			sb.append("### New Declaration Layer Iteration (");
			sb.append(declaration);
			sb.append(")\n");
		}
		boolean constraintsFullfilled = false;
		if (firstIteration) {
			if (!getFirstCombination(subgraph)) {
				if (logger != null) {
					logger.logResultSize("Declaration", possibleCombinations);
				}
				if (GreqlEvaluator.DEBUG_DECLARATION_ITERATIONS) {
					sb.append("## 1st. iteration: returning false (");
					sb.append(declaration);
					sb.append(")");
					System.out.println(sb.toString());
				}
				return false; // no more combinations exists
			}
			constraintsFullfilled = fullfillsConstraints(subgraph);
			firstIteration = false;
		}
		while (!constraintsFullfilled) {
			if (!getNextCombination(subgraph, false)) {
				if (logger != null) {
					logger.logResultSize("Declaration", possibleCombinations);
				}
				if (GreqlEvaluator.DEBUG_DECLARATION_ITERATIONS) {
					sb.append("## nth iteration: returning false (");
					sb.append(declaration);
					sb.append(")");
					System.out.println(sb.toString());
				}
				return false; // no more combinations exists
			}
			constraintsFullfilled = fullfillsConstraints(subgraph);
		}

		possibleCombinations++;

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
	 * @param subgraph
	 * @return true if a first combination exists, false otherwise
	 * @throws EvaluateException
	 */
	private boolean getFirstCombination(AbstractGraphMarker<?> subgraph)
			throws EvaluateException {
		variableDeclarations.get(0).reset();
		return getNextCombination(subgraph, true);
	}

	/**
	 * Gets the next possible variable combination
	 * 
	 * @param subgraph
	 * @return true if a next combination exists, false otherwise
	 * @throws EvaluateException
	 */
	
	
	private boolean getNextCombination(AbstractGraphMarker<?> subgraph,
			boolean firstCombination) throws EvaluateException {
		
		int pointer = firstCombination ? 0 : variableDeclarations.size() - 1;
		
		boolean iterate;
		do {
			iterate = false;
			VariableDeclaration currDecl = null; //pointer = 0
			do {
				if (pointer < 0) {
					return false;
				}
				currDecl = variableDeclarations.get(pointer--); //pointer = -1 
			} while (!currDecl.iterate());
			pointer += 2; //pointer = 3
			int size = variableDeclarations.size();
			while (pointer < size) {
				currDecl = variableDeclarations.get(pointer++); 
				currDecl.reset();
				if (!currDecl.iterate()) {
					pointer-= 2;
					iterate = true;
					break;
				}
			}
		} while (iterate);
		return true;
	}

	/**
	 * Checks if the current variable combination fullfills the constraints.
	 * 
	 * @param subgraph
	 * @return true if the combination fullfills the constraint, false otherwise
	 * @throws EvaluateException
	 */
	private boolean fullfillsConstraints(AbstractGraphMarker<AttributedElement> subgraph)
			throws EvaluateException {
		if ((constraintList == null) || (constraintList.isEmpty())) {
			return true;
		}
		for (int i = 0; i < constraintList.size(); i++) {
			VertexEvaluator currentEval = constraintList.get(i);
			JValue tempResult = currentEval.getResult(subgraph);
			try {
				if (tempResult.isBoolean()) {
					if (tempResult.toBoolean() != Boolean.TRUE) {
						return false;
					}
				} else {
					throw new WrongResultTypeException(currentEval.getVertex(),
							"Boolean", tempResult.getClass().getSimpleName(),
							currentEval.createPossibleSourcePositions());
				}
			} catch (JValueInvalidTypeException ex) {
				throw new WrongResultTypeException(currentEval.getVertex(),
						"Boolean", tempResult.getClass().getSimpleName(),
						currentEval.createPossibleSourcePositions());
			}
		}
		return true;
	}

	public void reset() {
		firstIteration = true;
		possibleCombinations = 0;
	}

}
