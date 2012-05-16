/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
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

package de.uni_koblenz.jgralab.greql2.evaluator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.pcollections.PCollection;
import org.pcollections.PMap;
import org.pcollections.POrderedSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.types.Undefined;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Schema;

/**
 * This is the core class of the GReQL-2 Evaluator. It takes a GReQL-2 Query as
 * String or Graph and a JGraLab-Datagraph and evaluates the Query on this
 * graph. The result is a JValue-object, it can be accessed using the method
 * <code>JValue getEvaluationResult()</code>.<br>
 * TODO [greqlevaluator] Make all occurences of GreqlEvaluatorImpl to use the
 * public Interface.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class GreqlEvaluatorImpl implements InternalGreqlEvaluator,
		GreqlEvaluator {

	public static void main(String[] args) throws FileNotFoundException,
			IOException, GraphIOException {
		if ((args.length < 1) || (args.length > 2)) {
			System.err
					.println("Usage: java GreqlEvaluator <query> [<graphfile>]");
			System.exit(1);
		}
		JGraLab.setLogLevel(Level.OFF);

		String query = args[0];
		Graph datagraph = null;
		if (args.length == 2) {
			datagraph = GraphIO.loadGraphFromFile(args[1],
					ImplementationType.GENERIC, new ConsoleProgressFunction(
							"Loading"));
		}

		GreqlEvaluatorImpl eval = new GreqlEvaluatorImpl(new QueryImpl(query),
				datagraph, null, null);
		Object result = eval.getResult();
		System.out.println("Evaluation Result:");
		System.out.println("==================");

		if (result instanceof PCollection) {
			PCollection<?> coll = (PCollection<?>) result;
			for (Object jv : coll) {
				System.out.println(jv);
			}
		} else if (result instanceof Map) {
			for (Entry<?, ?> e : ((Map<?, ?>) result).entrySet()) {
				System.out.println(e.getKey() + " --> " + e.getValue());
			}
		} else {
			System.out.println(result);
		}
	}

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

	private static Logger logger = Logger.getLogger(GreqlEvaluatorImpl.class
			.getName());

	/**
	 * This attribute holds the datagraph
	 */
	private Graph datagraph = null;

	/**
	 * This attribute holds the result of the evaluation
	 */
	private Object result = null;

	/**
	 * The plain time needed for evaluation.
	 */
	private long evaluationTime;

	/**
	 * Stores the evaluation result of the query vertex <code>v</code> at
	 * <code>localEvaluationResult[v.getId()]</code>
	 */
	private Object[] localEvaluationResults;

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

	/**
	 * Holds the already passed time in abstract time units
	 */
	// private long passedInterpretationSteps;

	private QueryImpl query;

	private GreqlEnvironment environment;

	/**
	 * should be called by every vertex evaluator to indicate a progress. The
	 * given value should be the ownEvaluationCosts of that VertexEvaluator.
	 * Calls the progress()-Method of the progress function this evaluator uses
	 */
	@Override
	public final void progress(long value) {
		progressStepsPassed += value;
		if (progressFunction != null) {
			while (progressStepsPassed > progressFunction.getUpdateInterval()) {
				progressFunction.progress(1);
				progressStepsPassed -= progressFunction.getUpdateInterval();
			}
		}
		// passedInterpretationSteps += value;
	}

	@Override
	public Object setLocalEvaluationResult(Greql2Vertex vertex, Object value) {
		Object oldValue = localEvaluationResults[vertex.getId()];
		localEvaluationResults[vertex.getId()] = value;
		return oldValue;
	}

	@Override
	public Object getLocalEvaluationResult(Greql2Vertex vertex) {
		return localEvaluationResults[vertex.getId()];
	}

	@Override
	public Object removeLocalEvaluationResult(Greql2Vertex vertex) {
		return setLocalEvaluationResult(vertex, null);
	}

	@Override
	public Object getResult() {
		return evaluate();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getSingleResult() {
		return (T) evaluate();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> PVector<T> getResultList() {
		return (PVector<T>) evaluate();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <K, V> PMap<K, V> getResultMap() {
		return (PMap<K, V>) evaluate();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> POrderedSet<T> getResultSet() {
		return (POrderedSet<T>) evaluate();
	}

	@Override
	public Graph getDataGraph() {
		return datagraph;
	}

	@Override
	public Schema getSchemaOfDataGraph() {
		return datagraph == null ? null : datagraph.getSchema();
	}

	@Override
	public AttributedElementClass<?, ?> getAttributedElementClass(
			String qualifiedName) {
		return datagraph.getSchema().getAttributedElementClass(qualifiedName);
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
	public GreqlEvaluatorImpl(Query query, Graph datagraph,
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
	public GreqlEvaluatorImpl(Query query, Graph datagraph,
			GreqlEnvironment environment) {
		initialize(query, datagraph, environment, null);
	}

	public GreqlEvaluatorImpl() {

	}

	/**
	 * @param query
	 * @param datagraph
	 * @param variables
	 * @param progressFunction
	 */
	private void initialize(Query query, Graph datagraph,
			GreqlEnvironment environment, ProgressFunction progressFunction) {
		this.query = (QueryImpl) query;
		this.datagraph = datagraph;
		this.environment = environment;
		localEvaluationResults = new Object[query.getQueryGraph().getVCount() + 1];
		this.progressFunction = progressFunction;
	}

	@Override
	public Object evaluate(QueryImpl query, Graph datagraph,
			GreqlEnvironment environment, ProgressFunction progressFunction) {
		initialize(query, datagraph, environment, progressFunction);
		return evaluate();
	}

	private Object evaluate() {
		long startTime = System.currentTimeMillis();
		evaluationTime = -1;
		query.resetVertexEvaluators(this);

		if (query.getQueryGraph().getVCount() <= 1) {
			// Graph contains only root vertex
			result = Undefined.UNDEFINED;
			return result;
		}

		// Calculate the evaluation costs
		VertexEvaluator<Greql2Expression> greql2ExpEval = query
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

		evaluationTime = System.currentTimeMillis() - startTime;
		return result;
	}

	/**
	 * @return the time needed for plain evaluation.
	 */
	public long getEvaluationTime() {
		return evaluationTime;
	}

	public void printEvaluationTimes() {
		logger.info("Evaluation took "
				+ evaluationTime
				+ "ms."
				+ (progressFunction != null ? " Estimated evaluation costs: "
						+ estimatedInterpretationSteps : ""));
	}

	@Override
	public Object setVariable(String varName, Object value) {
		return environment.setVariable(varName, value);
	}

	@Override
	public Object getVariable(String varName) {
		return environment.getVariable(varName);
	}

}
