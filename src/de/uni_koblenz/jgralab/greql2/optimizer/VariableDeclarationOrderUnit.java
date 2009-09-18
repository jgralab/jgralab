/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.optimizer;

import java.util.HashSet;
import java.util.Set;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphMarker;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.IsConstraintOf;
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
				.getFirstIsDeclaredVarOf(EdgeDirection.OUT).getOmega();
		this.typeExpressionOfVariable = (Expression) this.simpleDeclarationOfVariable
				.getFirstIsTypeExprOf(EdgeDirection.IN).getAlpha();

		// Collect all expressions that have to be re-evaluated when the
		// variable changes its value.
		IsConstraintOf inc = this.declaringDeclaration
				.getFirstIsConstraintOf(EdgeDirection.IN);
		dependentVertices = new HashSet<Vertex>();
		while (inc != null) {
			// calculate the set of dependent expressions in the constraints
			addDependentVerticesBelow(inc.getAlpha());
			inc = inc.getNextIsConstraintOf(EdgeDirection.IN);
		}
	}

	/**
	 * Adds the vertices below <code>vertex</code> that depend on this
	 * {@link VariableDeclarationOrderUnit}'s {@link Variable}.
	 * 
	 * @param vertex
	 *            a {@link Vertex}
	 */
	private void addDependentVerticesBelow(Vertex vertex) {
		if (vertexEvalMarker.getMark(vertex) != null
				&& OptimizerUtility.isAbove(vertex, variable)) {
			dependentVertices.add(vertex);
			Edge inc = vertex.getFirstEdge(EdgeDirection.IN);
			while (inc != null) {
				addDependentVerticesBelow(inc.getAlpha());
				inc = inc.getNextEdge(EdgeDirection.IN);
			}
		}
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
