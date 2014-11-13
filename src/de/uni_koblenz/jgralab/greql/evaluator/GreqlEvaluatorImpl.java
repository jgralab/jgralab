/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
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

import java.util.HashMap;
import java.util.Map;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.greql.GreqlEnvironment;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.evaluator.fa.FiniteAutomaton;
import de.uni_koblenz.jgralab.greql.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql.exception.EvaluationInterruptedException;
import de.uni_koblenz.jgralab.greql.schema.GreqlExpression;
import de.uni_koblenz.jgralab.greql.schema.GreqlVertex;
import de.uni_koblenz.jgralab.greql.types.Undefined;
import de.uni_koblenz.jgralab.impl.std.GraphImpl;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.Schema;

/**
 * This is the core class of the GReQL-2 Evaluator. It takes a GReQL-2 Query as
 * String or Graph and a JGraLab-Datagraph and evaluates the Query on this
 * graph. The result is a JValue-object, it can be accessed using the method
 * <code>JValue getEvaluationResult()</code>.<br>
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class GreqlEvaluatorImpl implements InternalGreqlEvaluator {
	/**
	 * Print the current value of each variable in a declaration layer during
	 * evaluation.
	 */
	public static boolean DEBUG_DECLARATION_ITERATIONS = false;

	/**
	 * toggles the maximal size of the vertex index for each graph with respect
	 * to graph size. For instance, a value of 50 (fifty) here will allow the
	 * vertex index to need 5 (five) times more size than the vertex array in
	 * the graph. For a graph with 1.000.000 vertices, this array needs 4MB in
	 * the memory.
	 */
	public static final int VERTEX_INDEX_SIZE = 50;

	private Graph graph = null;

	private Schema schema = null;

	/**
	 * This attribute holds the result of the evaluation
	 */
	private Object result = null;

	/**
	 * Stores the evaluation result of the query vertex <code>v</code> at
	 * <code>localEvaluationResult[v.getId()]</code>
	 */
	private Object[] localEvaluationResults;

	private Map<GreqlVertex, FiniteAutomaton> localAutomatons;

	/**
	 * The progress function this evaluator uses, may be null
	 */
	private ProgressFunction progressFunction = null;

	/**
	 * holds the number of interpretetation steps that have been passed since
	 * the last call of the progress function
	 */
	private long progressStepsPassed;

	/**
	 * Holds the estimated needed for evaluation time in abstract units
	 */
	private long estimatedInterpretationSteps;

	private GreqlQueryImpl query;

	private GreqlEnvironment environment;

	private int cnt = 0;
	long stepsAtLastProgressReport = 0;

	/**
	 * should be called by every vertex evaluator to indicate a progress. The
	 * given value should be the ownEvaluationCosts of that VertexEvaluator.
	 * Calls the progress()-Method of the progress function this evaluator uses
	 */
	@Override
	public final void progress(long value) {
		// check for interruption every now and then...
		if (++cnt == 4096) {
			if (Thread.interrupted()) {
				throw new EvaluationInterruptedException();
			}
			cnt = 0;
		}
		progressStepsPassed += value;
		if (progressFunction != null) {
			if (progressStepsPassed - stepsAtLastProgressReport > progressFunction
					.getUpdateInterval()) {
				progressFunction.progress(progressStepsPassed);
				stepsAtLastProgressReport = progressStepsPassed;
			}
		}
		System.out.println(progressStepsPassed);
	}

	@Override
	public Object setLocalEvaluationResult(GreqlVertex vertex, Object value) {
		Object oldValue = localEvaluationResults[vertex.getId()];
		localEvaluationResults[vertex.getId()] = value;
		return oldValue;
	}

	@Override
	public Object getLocalEvaluationResult(GreqlVertex vertex) {
		return localEvaluationResults[vertex.getId()];
	}

	@Override
	public FiniteAutomaton setLocalAutomaton(GreqlVertex vertex,
			FiniteAutomaton value) {
		return localAutomatons.put(vertex, value);
	}

	@Override
	public FiniteAutomaton getLocalAutomaton(GreqlVertex vertex) {
		return localAutomatons.get(vertex);
	}

	@Override
	public Object removeLocalEvaluationResult(GreqlVertex vertex) {
		return setLocalEvaluationResult(vertex, null);
	}

	public Object getResult() {
		return evaluate();
	}

	@Override
	public Graph getGraph() {
		return graph;
	}

	@Override
	public Schema getSchema() {
		return schema;
	}

	@Override
	public GraphElementClass<?, ?> getGraphElementClass(String typeName) {
		return EvaluatorUtilities.getGraphElementClass(
				query.getRootExpression(), schema, typeName);
	}

	/**
	 * Creates a new GreqlEvaluator for the given Query and Datagraph
	 * 
	 * @param query
	 *            the string-representation of the query to evaluate
	 * @param datagraph
	 *            the Datagraph on which the query gets evaluated
	 * @param environment
	 *            {@link GreqlEnvironment} with the bound variables
	 * @param progressFunction
	 *            the ProgressFunction which indicates the progress, for
	 *            instance display a progress bar etc.
	 */
	public GreqlEvaluatorImpl(GreqlQuery query, Graph datagraph,
			GreqlEnvironment environment, ProgressFunction progressFunction) {
		initialize(query, datagraph, environment, progressFunction);
	}

	/**
	 * Creates a new GreqlEvaluator for the given Query and Datagraph
	 * 
	 * @param query
	 *            the string-representation of the query to evaluate
	 * @param datagraph
	 *            the Datagraph on which the query gets evaluated
	 * @param environment
	 *            {@link GreqlEnvironment} with the bound variables
	 */
	public GreqlEvaluatorImpl(GreqlQuery query, Graph datagraph,
			GreqlEnvironment environment) {
		initialize(query, datagraph, environment, null);
	}

	/**
	 * @param query
	 * @param datagraph
	 * @param variables
	 * @param progressFunction
	 */
	private void initialize(GreqlQuery query, Graph datagraph,
			GreqlEnvironment environment, ProgressFunction progressFunction) {
		this.query = (GreqlQueryImpl) query;
		this.graph = datagraph;
		if (datagraph != null) {
			schema = datagraph.getSchema();
		}
		this.environment = environment;
		localEvaluationResults = new Object[((GraphImpl) query.getQueryGraph())
				.getMaxVCount() + 1];
		localAutomatons = new HashMap<GreqlVertex, FiniteAutomaton>();
		this.progressFunction = progressFunction;
	}

	public Object evaluate(GreqlQuery query, Graph datagraph,
			GreqlEnvironment environment, ProgressFunction progressFunction) {
		initialize(query, datagraph, environment, progressFunction);
		return evaluate();
	}

	public Object evaluate() {
		query.resetVertexEvaluators(this);

		if (query.getQueryGraph().getVCount() <= 1) {
			// Graph contains only root vertex
			result = Undefined.UNDEFINED;
			return result;
		}

		// Calculate the evaluation costs
		VertexEvaluator<GreqlExpression> greql2ExpEval = query
				.getVertexEvaluator(query.getRootExpression());

		if (progressFunction != null) {
			estimatedInterpretationSteps = greql2ExpEval
					.getInitialSubtreeEvaluationCosts();

			progressFunction.init(estimatedInterpretationSteps);
		}

		result = greql2ExpEval.getResult(this);

		// last, remove all added tempAttributes, currently, this are only
		// subgraphAttributes
		if (progressFunction != null) {
			progressFunction.finished();
		}
		return result;
	}

	@Override
	public Object setVariable(String varName, Object value) {
		return environment.setVariable(varName, value);
	}

	@Override
	public Object getVariable(String varName) {
		return environment.getVariable(varName);
	}

	@Override
	public void setSchema(Schema schema) {
		this.schema = schema;
	}
}
