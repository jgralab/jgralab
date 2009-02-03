/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
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

import java.util.ArrayList;

import de.uni_koblenz.jgralab.BooleanGraphMarker;
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
public class VariableDeclarationLayer implements
		Comparable<VariableDeclarationLayer> {

	/**
	 * Holds a VariableDeclaration for each Variable which is declared in this
	 * Declaration
	 */
	private ArrayList<VariableDeclaration> variableDeclarations;

	/**
	 * this is the list of constraint vertices
	 */
	private ArrayList<VertexEvaluator> constraintList;

	/**
	 * true if the next variable iteration is the first one, that means, if
	 * there was no iteration before
	 */
	private boolean firstIteration = true;

	/**
	 * holds a number which identifies this declaration layer
	 */
	private int identifier = 0;

	private EvaluationLogger logger;
	private int possibleCombinations = 0;

	/**
	 * Creates a new {@link VariableDeclarationLayer} for iterating over all
	 * variable combinations that fulfil the constraints in constraintList.
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
	public VariableDeclarationLayer(ArrayList<VertexEvaluator> constraintList,
			EvaluationLogger logger) {
		variableDeclarations = new ArrayList<VariableDeclaration>();
		this.constraintList = constraintList;
		this.logger = logger;
	}

	/**
	 * Adds the given VariableDeclaration to the DeclarationLayer
	 * 
	 * @return true if the VariableDeclaration was added successfull, false
	 *         otherwise
	 */
	public boolean addVariableDeclaration(VariableDeclaration d) {
		if (firstIteration == true) {
			variableDeclarations.add(d);
			d.reset();
			return true;
		}
		return false;
	}

	/**
	 * sets the next possible combination of values to the variable-vertices. If
	 * it is called the first time, it returns true if the first possible
	 * combination is valid
	 * 
	 * @return true if another possible combination was found, false otherwise
	 */
	public boolean iterate(BooleanGraphMarker subgraph)
			throws EvaluateException {
		boolean constraintsFullfilled = false;
		if (firstIteration) {
			if (!getFirstCombination(subgraph)) {
				if (logger != null) {
					logger.logResultSize("Declaration", possibleCombinations);
				}
				return false; // no more combinations exists
			}
			constraintsFullfilled = fullfillsConstraints(subgraph);
			firstIteration = false;
		}
		while (!constraintsFullfilled) {
			if (!getNextCombination(subgraph)) {
				if (logger != null) {
					logger.logResultSize("Declaration", possibleCombinations);
				}
				return false; // no more combinations exists
			}
			constraintsFullfilled = fullfillsConstraints(subgraph);
		}
		possibleCombinations++;
		return true;
	}

	/**
	 * Gets the first possible Variable Combination
	 * 
	 * @param subgraph
	 * @return true if a first combination exists, false otherwise
	 * @throws EvaluateException
	 */
	private boolean getFirstCombination(BooleanGraphMarker subgraph)
			throws EvaluateException {
		for (int i = 0; i < variableDeclarations.size(); i++) {
			VariableDeclaration currDecl = variableDeclarations.get(i);
			if (!currDecl.iterate())
				return false;
		}
		return true;
	}
	


	/**
	 * Gets the next possible variable combination
	 * 
	 * @param subgraphMarker
	 * @return true if a next combination exists, false otherwise
	 * @throws EvaluateException
	 */
	private boolean getNextCombination(BooleanGraphMarker subgraphMarker)
			throws EvaluateException {
		for (int i = variableDeclarations.size() - 1; i >= 0; i--) {
			VariableDeclaration currDecl = variableDeclarations.get(i);
			if (currDecl.iterate()) {
				return true;
			} else {
				/* reset this variabledeclaration and sets it to the first value */
				currDecl.reset();
				currDecl.iterate();
			}
		}
		return false;
	}

	/**
	 * Checks if the current variable combination fullfills the constraints.
	 * 
	 * @param subgraphMarker
	 * @return true if the combination fullfills the constraint, false otherwise
	 * @throws EvaluateException
	 */
	private boolean fullfillsConstraints(BooleanGraphMarker subgraphMarker)
			throws EvaluateException {
		if ((constraintList == null) || (constraintList.isEmpty()))
			return true;
		for (VertexEvaluator currentEval : constraintList) {
			JValue tempResult = currentEval.getResult(subgraphMarker);
			try {
				if (tempResult.isBoolean()) {
					if (tempResult.toBoolean() != Boolean.TRUE)  {
						return false;
					}	
				} else {
					throw new WrongResultTypeException(currentEval.getClass()
							.getSimpleName(), "Boolean",
							tempResult.getClass().getName(), currentEval
									.createPossibleSourcePositions());
				}
			} catch (JValueInvalidTypeException ex) {
				throw new WrongResultTypeException(currentEval.getClass()
						.getSimpleName(), "Boolean",
						tempResult.getClass().getName(), currentEval
								.createPossibleSourcePositions());
			}
		}
		return true;
	}

	public int getId() {
		if (identifier == 0) {
			for (VertexEvaluator currentEval : constraintList)
				identifier += currentEval.getVertex().getId();
			for (VariableDeclaration currDecl : variableDeclarations)
				identifier += currDecl.hashCode();
		}
		return identifier;
	}

	public int compareTo(VariableDeclarationLayer l) {
		return getId() - l.getId();
	}

}
