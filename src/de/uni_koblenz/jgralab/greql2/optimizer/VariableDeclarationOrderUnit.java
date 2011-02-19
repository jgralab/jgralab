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
/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.optimizer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.IsTypeExprOf;
import de.uni_koblenz.jgralab.greql2.schema.SimpleDeclaration;
import de.uni_koblenz.jgralab.greql2.schema.Variable;

/**
 * Models various aspects of {@link Variable}s used by the
 * {@link VariableDeclarationOrderOptimizer} to order them so that evaluation
 * costs are minimized.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class VariableDeclarationOrderUnit implements
		Comparable<VariableDeclarationOrderUnit> {
	private Variable variable;
	private Declaration declaringDeclaration;
	private Set<Vertex> dependentVertices;
	private long variableValueChangeCosts = Long.MIN_VALUE;
	private long typeExpressionCardinality = Long.MIN_VALUE;
	private GraphMarker<VertexEvaluator> vertexEvalMarker;
	private GraphSize graphSize;
	private SimpleDeclaration simpleDeclarationOfVariable;
	private Expression typeExpressionOfVariable;

	/**
	 * Creates a new {@link VariableDeclarationOrderUnit}.
	 * 
	 * @param var
	 *            a {@link Variable}
	 * @param declaringDecl
	 *            the {@link Declaration} in which <code>var</code> is declared
	 * @param marker
	 *            a {@link GraphMarker} of {@link VertexEvaluator}s
	 * @param graphSize
	 *            the {@link GraphSize} of the datagraph
	 */
	VariableDeclarationOrderUnit(Variable var, Declaration declaringDecl,
			GraphMarker<VertexEvaluator> marker, GraphSize graphSize) {
		this.variable = var;
		this.declaringDeclaration = declaringDecl;
		this.vertexEvalMarker = marker;
		this.graphSize = graphSize;
		this.simpleDeclarationOfVariable = (SimpleDeclaration) this.variable
				.getFirstIsDeclaredVarOfIncidence(EdgeDirection.OUT).getOmega();
		this.typeExpressionOfVariable = (Expression) this.simpleDeclarationOfVariable
				.getFirstIsTypeExprOfIncidence(EdgeDirection.IN).getAlpha();

		// Collect all vertices that depend on the variable and thus need to be
		// recalculated when it changes its value.
		dependentVertices = new HashSet<Vertex>();
		addDependendVertices(variable);
		while (extendDependendVertices()) {
			// Add dependent vertices transitively
		}
	}

	/**
	 * Adds the vertices that depend on the expression <code>vertex</code> to
	 * dependentVertices (but not vertex itself)
	 * 
	 * @param vertex
	 *            a {@link Vertex}
	 */
	private void addDependendVertices(Vertex vertex) {
		if ((vertex == simpleDeclarationOfVariable)
				|| (vertex == declaringDeclaration)) {
			return;
		}
		if (variable != vertex) {
			dependentVertices.add(vertex);
		}
		for (Edge e : vertex.incidences(EdgeDirection.OUT)) {
			addDependendVertices(e.getOmega());
		}
	}

	/**
	 * addDependentVertices(variable) added all vertices that depend on variable
	 * by simply following forward edges till declaringDeclaration. This doesn't
	 * find variables declared by other {@link SimpleDeclaration}s of
	 * declaringDeclaration, but the simple declarations are included. So add
	 * the variables of them and their dependencies, too.
	 * 
	 * @return true if extension was needed and done
	 */
	private boolean extendDependendVertices() {
		boolean extensionWasNeeded = false;
		ArrayList<Vertex> list = new ArrayList<Vertex>(dependentVertices.size());
		for (Vertex v : dependentVertices) {
			list.add(v);
		}
		for (Vertex v : list) {
			if (!(v instanceof SimpleDeclaration)) {
				continue;
			}
			SimpleDeclaration sd = (SimpleDeclaration) v;
			if (sd == simpleDeclarationOfVariable) {
				continue;
			}
			for (Variable var : sd.get_declaredVar()) {
				// if it is already in the set, then the extension was already
				// done.
				if (!dependentVertices.contains(var)) {
					extensionWasNeeded = true;
					addDependendVertices(var);
				}
			}
		}
		return extensionWasNeeded;
	}

	/**
	 * Gets the costs needed to re-evaluate all vertices that depend on this
	 * {@link VariableDeclarationOrderUnit}'s {@link Variable}.
	 * 
	 * @return the costs needed to re-evaluate all vertices that depend on this
	 *         {@link VariableDeclarationOrderUnit}'s {@link Variable}
	 */
	long getVariableValueChangeCosts() {
		if (variableValueChangeCosts == Long.MIN_VALUE) {
			variableValueChangeCosts = calculateVariableValueChangeCosts();
		}
		return variableValueChangeCosts;
	}

	/**
	 * Computes the costs needed to re-evaluate all vertices that depend on this
	 * {@link VariableDeclarationOrderUnit}'s {@link Variable}.
	 * 
	 * @return the costs needed to re-evaluate all vertices that depend on this
	 *         {@link VariableDeclarationOrderUnit}'s {@link Variable}
	 */
	private int calculateVariableValueChangeCosts() {
		int costs = 0;
		for (Vertex vertex : dependentVertices) {
			VertexEvaluator eval = vertexEvalMarker.getMark(vertex);
			assert eval != null;
			costs += eval.getOwnEvaluationCosts(graphSize);
		}
		return costs;
	}

	/**
	 * Gets this {@link VariableDeclarationOrderUnit} {@link Variable}'s type
	 * expression.
	 * 
	 * @return the {@link Expression} on the {@link IsTypeExprOf} edge of this
	 *         {@link VariableDeclarationOrderUnit}'s {@link Variable}
	 */
	Expression getTypeExpressionOfVariable() {
		return typeExpressionOfVariable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return dependentVertices.size() + " Expressions depend on " + variable
				+ " (" + variable.get_name() + ") resulting in costs of "
				+ getVariableValueChangeCosts() + " on value changes.";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(VariableDeclarationOrderUnit o) {
		if (this == o) {
			return 0;
		}
		// Units which depend on a variable of another unit have to come first,
		// no matter what the recalculation costs are. So if the other vars
		// simple decl is in my dependency set, then I have to come first. (And
		// the other way round...)
		if (dependentVertices.contains(o.variable)) {
			assert !o.dependentVertices.contains(variable) : "Circular dependency!";
			return -1;
		}
		if (o.dependentVertices.contains(variable)) {
			assert !dependentVertices.contains(o.variable) : "Circular dependency!";
			return 1;
		}

		// Sort that units with higher costs come first.
		if (getVariableValueChangeCosts() < o.getVariableValueChangeCosts()) {
			return 1;
		}
		if (getVariableValueChangeCosts() > o.getVariableValueChangeCosts()) {
			return -1;
		}

		// The costs are equal, so the one with the higher cardinality should
		// be declared after the one with the lower cardinality.
		long thisCard = getTypeExpressionCardinality();
		long otherCard = o.getTypeExpressionCardinality();
		if (thisCard > otherCard) {
			return 1;
		}
		if (thisCard < otherCard) {
			return -1;
		}

		// If there can be no decision made on costs and cardinality, then the
		// variable with the lower ID should come first.
		if (variable.getId() < o.getVariable().getId()) {
			return -1;
		} else {
			return 1;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof VariableDeclarationOrderUnit)) {
			return false;
		}
		return compareTo((VariableDeclarationOrderUnit) o) == 0;
	}

	@Override
	public int hashCode() {
		int x = 991;
		return x * variable.hashCode() + x;
	}

	/**
	 * Gets the cardinality of the type expression of the {@link Variable} of
	 * this {@link VariableDeclarationOrderUnit}
	 * 
	 * @return the cardinality of the type expression of the {@link Variable} of
	 *         this {@link VariableDeclarationOrderUnit}
	 */
	private long calculateTypeExpressionCardinality() {
		VertexEvaluator veval = vertexEvalMarker
				.getMark(typeExpressionOfVariable);
		return veval.getEstimatedCardinality(graphSize);
	}

	/**
	 * Gets the estimated cardinality of the type expression of this
	 * {@link VariableDeclarationOrderUnit}'s {@link Variable}.
	 * 
	 * @return the estimated cardinality of the type expression of this
	 *         {@link VariableDeclarationOrderUnit}'s {@link Variable}.
	 */
	long getTypeExpressionCardinality() {
		if (typeExpressionCardinality == Long.MIN_VALUE) {
			typeExpressionCardinality = calculateTypeExpressionCardinality();
		}
		return typeExpressionCardinality;
	}

	/**
	 * Gets the {@link SimpleDeclaration} that declares this
	 * {@link VariableDeclarationOrderUnit}'s {@link Variable}.
	 * 
	 * @return the {@link SimpleDeclaration} that declares this
	 *         {@link VariableDeclarationOrderUnit}'s {@link Variable}.
	 */
	SimpleDeclaration getSimpleDeclarationOfVariable() {
		return simpleDeclarationOfVariable;
	}

	/**
	 * Gets the {@link Variable} of this {@link VariableDeclarationOrderUnit}.
	 * 
	 * @return the {@link Variable} of this {@link VariableDeclarationOrderUnit}
	 *         .
	 */
	Variable getVariable() {
		return variable;
	}

	Declaration getDeclaringDeclaration() {
		return declaringDeclaration;
	}
}
