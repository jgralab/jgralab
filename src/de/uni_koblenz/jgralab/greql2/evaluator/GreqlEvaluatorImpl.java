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
import java.util.HashMap;
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
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Graph;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.types.Undefined;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

/**
 * This is the core class of the GReQL-2 Evaluator. It takes a GReQL-2 Query as
 * String or Graph and a JGraLab-Datagraph and evaluates the Query on this
 * graph. The result is a JValue-object, it can be accessed using the method
 * <code>JValue getEvaluationResult()</code>. TODO [greqlevaluator] Make all
 * occurences of GreqlEvaluatorImpl to use the public Interface.
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

		GreqlEvaluatorImpl eval = new GreqlEvaluatorImpl(new Query(query),
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

	/**
	 * The GraphMarker that stores all vertex evaluators
	 */
	protected GraphMarker<VertexEvaluator> vertexEvalGraphMarker;

	/**
	 * The map of SimpleName to Type of types that is known in the evaluator by
	 * import statements in the greql query
	 */
	protected Map<String, AttributedElementClass> knownTypes = new HashMap<String, AttributedElementClass>(); // initial

	/**
	 * returns the vertexEvalGraph marker that is used
	 */
	public final GraphMarker<VertexEvaluator> getVertexEvaluatorGraphMarker() {
		return vertexEvalGraphMarker;
	}

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
	 * This attribute holds the CostModel which estimates the evaluation costs
	 */
	private final CostModel costModel = null;

	/**
	 * The progress function this evaluator uses, may be null
	 */
	// private ProgressFunction progressFunction = null;

	/**
	 * holds the number of interpretetation steps that have been passed since
	 * the last call of the progress function
	 */
	private long progressStepsPassed;

	/**
	 * The plain time needed for evaluation.
	 */
	private long evaluationTime;

	/**
	 * Holds the variables that are defined via using, they are called bound or
	 * free variables
	 */
	private Map<String, Object> variableMap;

	/**
	 * Holds the estimated needed for evaluation time in abstract units
	 */
	private long estimatedInterpretationSteps;

	/**
	 * Holds the already passed time in abstract time units
	 */
	private long passedInterpretationSteps;

	private final Query query;

	/**
	 * should be called by every vertex evaluator to indicate a progress. The
	 * given value should be the ownEvaluationCosts of that VertexEvaluator.
	 * Calls the progress()-Method of the progress function this evaluator uses
	 */
	// public final void progress(long value) {
	// progressStepsPassed += value;
	// if (progressFunction != null) {
	// while (progressStepsPassed > progressFunction.getUpdateInterval()) {
	// progressFunction.progress(1);
	// progressStepsPassed -= progressFunction.getUpdateInterval();
	// }
	// }
	// passedInterpretationSteps += value;
	// }

	/**
	 * returns the changes variableMap
	 */
	public Map<String, Object> getVariables() {
		return variableMap;
	}

	public Object getVariable(String name) {
		return variableMap == null ? null : variableMap.get(name);
	}

	public void setVariables(Map<String, Object> varMap) {
		variableMap = varMap;
	}

	public void setVariable(String varName, Object value) {
		if (variableMap == null) {
			variableMap = new HashMap<String, Object>();
		}
		variableMap.put(varName, value);
	}

	public Object getResult() {
		return evaluate();
	}

	@SuppressWarnings("unchecked")
	public <T> T getSingleResult() {
		return (T) evaluate();
	}

	@SuppressWarnings("unchecked")
	public <T> PVector<T> getResultList() {
		return (PVector<T>) evaluate();
	}

	@SuppressWarnings("unchecked")
	public <K, V> PMap<K, V> getResultMap() {
		return (PMap<K, V>) evaluate();
	}

	@SuppressWarnings("unchecked")
	public <T> POrderedSet<T> getResultSet() {
		return (POrderedSet<T>) evaluate();
	}

	/**
	 * Creates a new GreqlEvaluator for the given Query and Datagraph
	 *
	 * @param query
	 *            the string-representation of the query to evaluate
	 * @param datagraph
	 *            the Datagraph on which the query gets evaluated
	 * @param variables
	 *            a Map<String, JValue> of bound variables
	 * @param progressFunction
	 *            the ProgressFunction which indicates the progress, for
	 *            instance display a progress bar etc.
	 */
	public GreqlEvaluatorImpl(Query query, Graph datagraph,
			Map<String, Object> variables, ProgressFunction progressFunction) {
		this.query = query;
		this.datagraph = datagraph;
		knownTypes = new HashMap<String, AttributedElementClass>();
		variableMap = variables;
		// this.progressFunction = progressFunction;
	}

	// public void addKnownType(AttributedElementClass knownType) {
	// knownTypes.put(knownType.getSimpleName(), knownType);
	// }
	//
	// public AttributedElementClass getKnownType(String typeSimpleName) {
	// return knownTypes.get(typeSimpleName);
	// }

	/**
	 * Creates the VertexEvaluator-Object at the vertices in the syntaxgraph
	 */
	private void createVertexEvaluators() {
		Greql2Graph queryGraph = query.getQueryGraph();
		vertexEvalGraphMarker = new GraphMarker<VertexEvaluator>(queryGraph);
		Greql2Vertex currentVertex = queryGraph.getFirstGreql2Vertex();
		while (currentVertex != null) {
			VertexEvaluator vertexEval = VertexEvaluator.createVertexEvaluator(
					currentVertex, this);
			if (vertexEval != null) {
				vertexEvalGraphMarker.mark(currentVertex, vertexEval);
			}
			currentVertex = currentVertex.getNextGreql2Vertex();
		}
	}

	/**
	 * clears the tempresults that are stored in the VertexEvaluators-Objects at
	 * the syntaxgraph nodes
	 *
	 * @param optimizer
	 */
	private void resetVertexEvaluators() {
		Greql2Graph queryGraph = query.getQueryGraph();
		Vertex currentVertex = queryGraph.getFirstVertex();
		while (currentVertex != null) {
			VertexEvaluator vertexEval = vertexEvalGraphMarker
					.getMark(currentVertex);
			if (vertexEval != null) {
				vertexEval.resetToInitialState();
			}
			currentVertex = currentVertex.getNextVertex();
		}
	}

	private Object evaluate() {
		long startTime = System.currentTimeMillis();
		evaluationTime = -1;
		resetVertexEvaluators();

		createVertexEvaluators();

		if (query.getQueryGraph().getVCount() <= 1) {
			// Graph contains only root vertex
			result = Undefined.UNDEFINED;
			return true;
		}

		// Calculate the evaluation costs
		VertexEvaluator greql2ExpEval = vertexEvalGraphMarker.getMark(query
				.getRootExpression());

		// if (progressFunction != null) {
		// estimatedInterpretationSteps = greql2ExpEval
		// .getInitialSubtreeEvaluationCosts(new GraphSize(datagraph));
		//
		// progressFunction.init(estimatedInterpretationSteps);
		// }

		result = greql2ExpEval.getResult(datagraph);

		// last, remove all added tempAttributes, currently, this are only
		// subgraphAttributes
		// if (progressFunction != null) {
		// progressFunction.finished();
		// }

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
		logger.info("Evaluation took " + evaluationTime + "ms."
		// + (progressFunction != null ? " Estimated evaluation costs: "
		// + estimatedInterpretationSteps : ""));
		);
	}
}
