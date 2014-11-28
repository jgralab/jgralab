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

package de.uni_koblenz.jgralab.greql.evaluator.vertexeval;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.VertexCosts;
import de.uni_koblenz.jgralab.greql.exception.QuerySourceException;
import de.uni_koblenz.jgralab.greql.schema.GreqlAggregation;
import de.uni_koblenz.jgralab.greql.schema.GreqlVertex;
import de.uni_koblenz.jgralab.greql.schema.SourcePosition;
import de.uni_koblenz.jgralab.greql.schema.Variable;

/**
 * This is the base class for all VertexEvaluators which evaluate the vertices
 * in the GReQL Syntaxgraph
 * 
 * @author ist@uni-koblenz.de
 */
public abstract class VertexEvaluator<V extends GreqlVertex> {

	protected static Logger logger = Logger.getLogger(VertexEvaluator.class
			.getName());

	/**
	 * This classes get not evaluated
	 */
	private static ArrayList<String> unevaluatedVertices;

	/**
	 * constructs a the list of vertex classes that shouldn't get evaluated as
	 * soon as the class is loaded
	 */
	static {
		unevaluatedVertices = new ArrayList<>();
		unevaluatedVertices.add("Quantifier");
		unevaluatedVertices.add("RoleId");
		unevaluatedVertices.add("FunctionId");
		unevaluatedVertices.add("RecordId");
		unevaluatedVertices.add("Direction");
	}

	protected V vertex;

	protected GreqlQueryImpl query;

	/**
	 * The set of variables this vertex depends on
	 */
	protected Set<Variable> neededVariables = null;

	/**
	 * The set of variables this vertex defines and that are valid in all
	 * subgraphs
	 */
	protected Set<Variable> definedVariables = null;

	/*
	 * The following fields are used by Optimizer
	 */

	/**
	 * The costs for the current evaluation of the whole subtree in the abstract
	 * measurement unit "interpretation steps"
	 */
	protected long currentSubtreeEvaluationCosts = Long.MIN_VALUE;

	/**
	 * The costs for the evaluation of the whole subtree for the first time
	 */
	protected long initialSubtreeEvaluationCosts = Long.MIN_VALUE;

	/**
	 * The costs for <b>one</b> evaluation of this vertex _without_ the costs of
	 * the evaluation of the subtrees
	 */
	protected long ownEvaluationCosts = Long.MIN_VALUE;

	/**
	 * The costs for all evaluations of this vertex for all variable
	 * combinations <b>without</b> the costs for the subtree evaluation
	 */
	protected long iteratedEvaluationCosts = Long.MIN_VALUE;

	/**
	 * the costs to create one transition
	 */
	protected static final int transitionCosts = 10;

	/**
	 * describes, how much interpretation steps it takes to add a element to a
	 * list
	 */
	protected static final int addToListCosts = 5;

	/**
	 * describes, how much interpretation steps it takes to add a element to a
	 * set
	 */
	protected static final int addToSetCosts = 10;

	/**
	 * The expected cardinality of the evaluation result this evaluator creates
	 */
	protected long estimatedCardinality = Long.MIN_VALUE;

	/**
	 * The expected selectivity of this vertexevaluator
	 */
	protected double estimatedSelectivity = Double.NaN;

	/**
	 * @param vertex
	 *            the {@link GreqlVertex} this VertexEvaluator belongs to
	 * @param query
	 *            the {@link GreqlQueryImpl} this {@link VertexEvaluator}
	 *            belongs to
	 */
	protected VertexEvaluator(V vertex, GreqlQueryImpl query) {
		this.vertex = vertex;
		this.query = query;
	}

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	public V getVertex() {
		return vertex;
	}

	/**
	 * @return the name of the associated {@link GreqlVertex} used for logging.
	 *         By default this is the type name, i.e. ListComprehension, but
	 *         subclasses may override this method to get a more finegrained
	 *         control. For example {@link FunctionApplicationEvaluator}s use
	 *         the function name of the corresponding function for logging.
	 */
	public String getLoggingName() {
		return getVertex().getAttributedElementClass().getSimpleName();
	}

	/**
	 * Gets the result of the evaluation of this vertex on the given subgraph
	 * 
	 * @return the evaluation result
	 */
	public Object getResult(InternalGreqlEvaluator evaluator) {
		if (evaluator != null) {
			Object result = evaluator.getLocalEvaluationResult(vertex);
			if (result != null) {
				return result;
			}
		}
		try {
			Object result = evaluate(evaluator);
			if (evaluator != null) {
				evaluator.setLocalEvaluationResult(vertex, result);
			}
			return result;
		} catch (QuerySourceException ex) {
			removeInvalidSourcePosition(ex);
			throw ex;
		}
	}

	/**
	 * @return true, if this expression has already been evaluated, useful
	 *         mostly for debugging
	 */
	public boolean isEvaluated(InternalGreqlEvaluator evaluator) {
		return (evaluator.getLocalEvaluationResult(vertex) != null);
	}

	/**
	 * this method does the evaluation. It must be implemented by concrete
	 * evaluators
	 */
	public abstract Object evaluate(InternalGreqlEvaluator evaluator);

	/**
	 * clears the evaluation result
	 */
	public final void clear(InternalGreqlEvaluator evaluator) {
		evaluator.removeLocalEvaluationResult(vertex);
	}

	/**
	 * resets the VertexEvaluators internal state (evaluation result, costs,
	 * etc) to the initial one, that means, sets all variables of this
	 * vertexevaluator to values, that it is in the same state like it was
	 * directly after creation
	 * 
	 * @param evaluator
	 */
	public void resetToInitialState(InternalGreqlEvaluator evaluator) {
		if (evaluator != null) {
			evaluator.removeLocalEvaluationResult(vertex);
		}
		currentSubtreeEvaluationCosts = Long.MIN_VALUE;
		initialSubtreeEvaluationCosts = Long.MIN_VALUE;
		ownEvaluationCosts = Long.MIN_VALUE;
		iteratedEvaluationCosts = Long.MIN_VALUE;
		estimatedCardinality = Long.MIN_VALUE;
		estimatedSelectivity = Double.NaN;
	}

	public void resetSubtreeToInitialState(InternalGreqlEvaluator evaluator) {
		resetToInitialState(evaluator);
		for (Edge e : getVertex().incidences(EdgeDirection.IN)) {
			GreqlVertex vertex = (GreqlVertex) e.getThat();
			VertexEvaluator<?> eval = query.getVertexEvaluator(vertex);
			if (eval != null) {
				eval.resetSubtreeToInitialState(evaluator);
			}
		}
	}

	/**
	 * This method must be overwritten by every subclass. It should call the
	 * right method of the GreqlEvaluators costmodel.
	 * 
	 * @return a 3-Tupel (ownCosts, iteratedCosts, subtreeCosts) of costs the
	 *         evaluation of the subtree with this vertex as root causes
	 */
	protected abstract VertexCosts calculateSubtreeEvaluationCosts();

	/**
	 * Calculates the costs the current evaluation of the subtree causes. These
	 * cost differ from the initialEvaluationCosts, because only for the first
	 * evaluation, the result really gets evaluated, for all other evaluations,
	 * the evaluated result only gets copied, these costs are 1
	 * 
	 * @return the costs of this evaluation of the subtree the vertex this
	 *         evaluator evaluates is root of
	 */
	public long getCurrentSubtreeEvaluationCosts() {
		if (currentSubtreeEvaluationCosts == Long.MIN_VALUE) {
			return getInitialSubtreeEvaluationCosts();
		} else {
			return 1;
		}
	}

	/**
	 * Calculates the costs the first evaluation of the subtree causes. These
	 * cost differ from the second "evaluation", because for the second one, the
	 * already evaluated result only gets copied, these costs are 1
	 * 
	 * @return the costs of the first evaluation of the subgraph the vertex this
	 *         evaluator evaluates is root of
	 */
	public long getInitialSubtreeEvaluationCosts() {
		if (initialSubtreeEvaluationCosts > 0) {
			return initialSubtreeEvaluationCosts;
		} else {
			VertexCosts costs = calculateSubtreeEvaluationCosts();
			ownEvaluationCosts = costs.ownEvaluationCosts;
			iteratedEvaluationCosts = costs.iteratedEvaluationCosts;
			currentSubtreeEvaluationCosts = costs.subtreeEvaluationCosts;
			initialSubtreeEvaluationCosts = costs.subtreeEvaluationCosts;
			return initialSubtreeEvaluationCosts;
		}
	}

	/**
	 * Get the costs for evaluating the associated vertex one time. No subtree
	 * or iteration costs are taken into account.
	 * 
	 * @return the costs for evaluating the associated vertex one time excluding
	 *         subtree and iteration costs
	 */
	public long getOwnEvaluationCosts() {
		if (ownEvaluationCosts == Long.MIN_VALUE) {
			// call for side-effects
			getInitialSubtreeEvaluationCosts();
		}
		return ownEvaluationCosts;
	}

	/**
	 * calculate the set of needed and defined variables
	 */
	public void calculateNeededAndDefinedVariables() {
		neededVariables = new HashSet<>();
		definedVariables = new HashSet<>();
		Edge inc = getVertex().getFirstIncidence(EdgeDirection.IN);
		while (inc != null) {
			VertexEvaluator<?> veval = query
					.getVertexEvaluator((GreqlVertex) inc.getAlpha());
			if (veval != null) {
				neededVariables.addAll(veval.getNeededVariables());
				definedVariables.addAll(veval.getDefinedVariables());
			}
			inc = inc.getNextIncidence(EdgeDirection.IN);
		}
		HashSet<Variable> bothVariables = new HashSet<>();
		bothVariables.addAll(neededVariables);
		neededVariables.removeAll(definedVariables);
		definedVariables.removeAll(bothVariables);
	}

	/**
	 * Calculates the set of variables this vertex depends on
	 * 
	 * @return the set of variables this vertex depends on
	 */
	public Set<Variable> getNeededVariables() {
		if (neededVariables == null) {
			calculateNeededAndDefinedVariables();
		}
		return neededVariables;
	}

	/**
	 * Calculates the set of variables this vertex (or even a vertex in a
	 * subgraph) defines and that is valid in the whole subtree with this vertex
	 * as head.
	 * 
	 * @return the set of variables this vertex defines and that are valid
	 */
	public Set<Variable> getDefinedVariables() {
		if (definedVariables == null) {
			calculateNeededAndDefinedVariables();
		}
		return definedVariables;
	}

	/**
	 * Returns the number of combinations of the variables this vertex depends
	 * on
	 */
	public long getVariableCombinations() {
		int combinations = 1;
		Iterator<Variable> iter = getNeededVariables().iterator();
		while (iter.hasNext()) {
			VariableEvaluator<? extends Variable> veval = (VariableEvaluator<? extends Variable>) query
					.getVertexEvaluator(iter.next());
			// combinations *= veval.getEstimatedCardinality();
			combinations *= veval.getVariableCombinations();
		}
		return combinations;
	}

	/**
	 * returns the estimated size of the result.
	 */
	public long getEstimatedCardinality() {
		if (estimatedCardinality == Long.MIN_VALUE) {
			estimatedCardinality = calculateEstimatedCardinality();
		}
		return estimatedCardinality;
	}

	/**
	 * calculates the estimated cardinality of the evaluationeresult this
	 * vertexevaluator creates. By default, this size is 1, if a VertexEvaluator
	 * has bigger resultsizes, it should override this method
	 */
	public long calculateEstimatedCardinality() {
		return 1;
	}

	/**
	 * returns the estimated selectivity of the vertex evaluation.
	 */
	public double getEstimatedSelectivity() {
		if (Double.isNaN(estimatedSelectivity)) {
			estimatedSelectivity = calculateEstimatedSelectivity();
		}
		return estimatedSelectivity;
	}

	/**
	 * calculates the estimated selectivity for this vertex.By default, this is
	 * 1, if a VertexEvaluator has an other selectivity, it should override this
	 * method
	 */
	public double calculateEstimatedSelectivity() {
		return 1;
	}

	/**
	 * creates a list of possible source positions for the current vertex
	 */
	public List<SourcePosition> createPossibleSourcePositions() {
		GreqlAggregation inc = (GreqlAggregation) getVertex()
				.getFirstIncidence(EdgeDirection.OUT);
		List<SourcePosition> possibleSourcePositions = new ArrayList<>();
		while (inc != null) {
			List<SourcePosition> sourcePositions = inc.get_sourcePositions();
			possibleSourcePositions.addAll(sourcePositions);
			inc = inc.getNextGreqlAggregationIncidence(EdgeDirection.OUT);
		}
		return possibleSourcePositions;
	}

	/**
	 * creates the sourcepositions for the given edge
	 */
	protected List<SourcePosition> createSourcePositions(GreqlAggregation edge) {
		List<SourcePosition> possibleSourcePositions = new ArrayList<>();
		List<SourcePosition> sourcePositions = edge.get_sourcePositions();
		possibleSourcePositions.addAll(sourcePositions);
		return possibleSourcePositions;
	}

	/**
	 * eliminates all sourcepoistions at the given exception, that are not
	 * possible if the vertex, that throwed the exception, was accessed via this
	 * vertex
	 */
	private void removeInvalidSourcePosition(QuerySourceException ex) {
		GreqlAggregation inc = (GreqlAggregation) getVertex()
				.getFirstIncidence(EdgeDirection.OUT);
		List<SourcePosition> possibleSourcePositions = new ArrayList<>();
		while (inc != null) {
			List<SourcePosition> sourcePositions = inc.get_sourcePositions();
			possibleSourcePositions.addAll(sourcePositions);
			inc = inc.getNextGreqlAggregationIncidence(EdgeDirection.OUT);
		}
		if (possibleSourcePositions.size() == 0) {
			return; // maybe the vertex is the root vertex, than it has no
		}
		Iterator<SourcePosition> iter = ex.getSourcePositions().iterator();
		while (iter.hasNext()) {
			boolean accepted = false;
			SourcePosition currentPosition = iter.next();
			Iterator<SourcePosition> availableIter = possibleSourcePositions
					.iterator();
			while (availableIter.hasNext()) {
				SourcePosition availablePosition = availableIter.next();
				if ((availablePosition.get_offset() <= currentPosition
						.get_offset())
						&& ((availablePosition.get_offset() + availablePosition
								.get_length()) >= (currentPosition.get_offset() + currentPosition
								.get_length()))) {
					accepted = true;
					break;
				}
			}
			if (!accepted) {
				// GreqlEvaluator.println("SourcePosition: (" +
				// currentPosition.offset + ", " + currentPosition.length + ")
				// is not accepted");
				iter.remove();
			}
		}

	}

	/**
	 * creates a vertex evaluator for the given vertex
	 */
	public static <V extends GreqlVertex> VertexEvaluator<V> createVertexEvaluator(
			V vertex, GreqlQueryImpl query) {
		Class<?> vertexClass = vertex.getClass();
		String fullClassName = vertexClass.getName();
		// remove the "Impl" ...
		fullClassName = fullClassName.substring(0, fullClassName.length() - 4);
		fullClassName = fullClassName.replaceFirst(".impl.std.", ".");
		// remove the packages
		String className = fullClassName.substring(fullClassName
				.lastIndexOf(".") + 1);

		if (unevaluatedVertices.contains(className)) {
			return null;
		}
		String evalName = className + "Evaluator";
		evalName = evalName.substring(className.lastIndexOf(".") + 1);
		evalName = VertexEvaluator.class.getPackage().getName() + "."
				+ evalName;
		try {
			Class<?>[] argsClass = new Class[] { Class.forName(fullClassName),
					GreqlQueryImpl.class };
			Class<?> evalClass = Class.forName(evalName);
			Constructor<?> constructor = evalClass.getConstructor(argsClass);
			@SuppressWarnings("unchecked")
			VertexEvaluator<V> vertexEval = (VertexEvaluator<V>) constructor
					.newInstance(vertex, query);
			return vertexEval;
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException(className, ex);
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException(className, ex);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(className, ex);
		} catch (InstantiationException ex) {
			throw new RuntimeException(className, ex);
		} catch (InvocationTargetException ex) {
			throw new RuntimeException(className, ex);
		}
	}

}
