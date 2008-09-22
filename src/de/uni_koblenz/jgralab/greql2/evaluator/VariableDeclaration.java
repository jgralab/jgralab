/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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
import java.util.Iterator;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VariableEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.schema.SimpleDeclaration;
import de.uni_koblenz.jgralab.greql2.schema.Variable;

/**
 * This class models the declaration of one variable. It allowes the iteration
 * over all possible values using the method iterate(). THe current value of the
 * variable is stored as temporary attribute at the variable vertex
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */
public class VariableDeclaration implements Comparable<VariableDeclaration> {

	/**
	 * Holds the set of possible values the variable may have
	 */
	private JValueCollection definitionSet;

	/**
	 * Holds the variable-vertex of this declaration.
	 */
	private VariableEvaluator variableEval;

	/**
	 * The simpledeclaration vertex the variable vertex is part of
	 */
	private SimpleDeclaration parentDeclaration;

	/**
	 * Used for simple Iteration over the possible values
	 */
	private Iterator<JValue> iter;

	/**
	 * Stores a reference to the Evaluator instance which is used to evaluate
	 * the query
	 */
	private GreqlEvaluator greqlEvaluator;

	/**
	 * Holds all Vertices in the greql-syntaxgraph whose result depends on this
	 * variable
	 */
	private ArrayList<VertexEvaluator> dependingExpressions;

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
	public VariableDeclaration(Variable var, JValueCollection definitionSet,
			SimpleDeclaration decl, GreqlEvaluator eval) {
		greqlEvaluator = eval;
		variableEval = (VariableEvaluator) eval.getVertexEvaluatorGraphMarker()
				.getMark(var);
		parentDeclaration = decl;
		this.definitionSet = definitionSet;
		iter = definitionSet.iterator();
		dependingExpressions = new ArrayList<VertexEvaluator>();
		addExpressionsDependingOnExpression(var);
	}

	/**
	 * Iterates over all possible values for this variable. Returns true if
	 * another value was found, false otherwise
	 */
	public boolean iterate() {
		if (iter.hasNext()) {
			deleteDependingResults();
			variableEval.setValue(iter.next());
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
		iter = definitionSet.iterator();
	}

	/**
	 * Adds all expressions which depends on the given expression to the list of
	 * expressions which depends on this variable
	 * 
	 * @param exp
	 *            all vertices that depend on the given expression will be added
	 * @param listToAdd
	 *            the depending expressions will be added to this list
	 * @param deniedList
	 *            if exp is part of this list, the recursion will stop
	 */
	protected void addExpressionsDependingOnExpression(Vertex exp) {
		VertexEvaluator eval = greqlEvaluator.getVertexEvaluatorGraphMarker()
				.getMark(exp);
		if (eval != null) {
			dependingExpressions.add(eval);
			Edge inc = exp.getFirstEdge(EdgeDirection.OUT);
			while (inc != null) {
				Vertex nextExpression = inc.getOmega();
				if ((nextExpression != exp)
						&& (nextExpression != parentDeclaration)
						&& (!dependingExpressions.contains(nextExpression))) {
					addExpressionsDependingOnExpression(nextExpression);
				}
				inc = inc.getNextEdge(EdgeDirection.OUT);
			}
		}
	}

	/**
	 * deletes all intermediate results that depend on this variable
	 */
	private void deleteDependingResults() {
		for (VertexEvaluator eval : dependingExpressions) {
			eval.clear();
		}
	}

	public int compareTo(VariableDeclaration d) {
		return variableEval.getVertex().getId()
				- d.variableEval.getVertex().getId();
	}

	/**
	 * Returns the cardinality of the collection this variable is bound to
	 * 
	 * @return the cardinality of the collection this variable is bound to
	 */
	public int getDefinitionCardinality() {
		return definitionSet.size();
	}

}
