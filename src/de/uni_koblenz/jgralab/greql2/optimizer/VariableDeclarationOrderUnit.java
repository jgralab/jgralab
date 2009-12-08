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
				.getFirstIsDeclaredVarOf(EdgeDirection.OUT).getOmega();
		this.typeExpressionOfVariable = (Expression) this.simpleDeclarationOfVariable
				.getFirstIsTypeExprOf(EdgeDirection.IN).getAlpha();

		// Collect all vertices that depend on the variable and thus need to be
		// recalculated when it changes its value.
		dependentVertices = new HashSet<Vertex>();
		addDependendVertices(variable);
		extendDependendVertices();
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
	 */
	private void extendDependendVertices() {
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
			for (Variable var : sd.getDeclaredVarList()) {
				addDependendVertices(var);
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
		if (this == o) {
			return 0;
		}
		// Units which depend on a variable of another unit have to come first,
		// no matter what the recalculation costs are. So if the other vars
		// simple decl is in my dependency set, then I have to come first. (And
		// the other way round...)
		if (dependentVertices.contains(o.simpleDeclarationOfVariable)) {
			return -1;
		}
		if (o.dependentVertices.contains(simpleDeclarationOfVariable)) {
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
