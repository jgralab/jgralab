/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIO.TGFilenameFilter;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
import de.uni_koblenz.jgralab.greql2.SerializableGreql2;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.DefaultCostModel;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.LogCostModel;
import de.uni_koblenz.jgralab.greql2.evaluator.logging.EvaluationLogger;
import de.uni_koblenz.jgralab.greql2.evaluator.logging.Level2LogReader;
import de.uni_koblenz.jgralab.greql2.evaluator.logging.Level2Logger;
import de.uni_koblenz.jgralab.greql2.evaluator.logging.LoggingType;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.CostModelException;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.Greql2Exception;
import de.uni_koblenz.jgralab.greql2.exception.OptimizerException;
import de.uni_koblenz.jgralab.greql2.funlib.Greql2FunctionLibrary;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.optimizer.DefaultOptimizer;
import de.uni_koblenz.jgralab.greql2.optimizer.Optimizer;
import de.uni_koblenz.jgralab.greql2.parser.GreqlParser;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;

/**
 * This is the core class of the GReQL-2 Evaluator. It takes a GReQL-2 Query as
 * String or Graph and a JGraLab-Datagraph and evaluates the Query on this
 * graph. The result is a JValue-object, it can be accessed using the method
 * <code>JValue getEvaluationResult()</code>.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class GreqlEvaluator {

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
			datagraph = GraphIO.loadSchemaAndGraphFromFile(args[1],
					CodeGeneratorConfiguration.MINIMAL,
					new ConsoleProgressFunction());
		}

		GreqlEvaluator eval = new GreqlEvaluator(query, datagraph, null);
		eval.startEvaluation();
		JValue result = eval.getEvaluationResult();
		System.out.println("Evaluation Result:");
		System.out.println("==================");
		if (result.isCollection()) {
			for (JValue jv : result.toCollection()) {
				System.out.println(jv);
			}
		} else if (result.isMap()) {
			for (Entry<JValue, JValue> e : result.toJValueMap().entrySet()) {
				System.out.println(e.getKey() + " --> " + e.getValue());
			}
		} else {
			System.out.println(result);
		}
	}

	/**
	 * toggles which expressions are added to the index. Only vertex- and
	 * edgeset expressions that need more than <code>indextimeBarrier</code> ms
	 * to calculate are added
	 */
	private static final long INDEX_TIME_BARRIER = 10;

	/**
	 * Print the current value of each variable in a declaration layer during
	 * evaluation.
	 */
	public static boolean DEBUG_DECLARATION_ITERATIONS = false;

	/**
	 * Print the text representation of the optimized query after optimization.
	 */
	public static boolean DEBUG_OPTIMIZATION = Boolean.parseBoolean(System
			.getProperty("greqlDebugOptimization", "false"));

	/**
	 * toggles wether to use indexing for vertex sets or not
	 */
	public static final boolean VERTEX_INDEXING = true;

	/**
	 * toggles the maximal size of the vertex index for each graph with respect
	 * to graph size. For instance, a value of 50 (fifty) here will allow the
	 * vertex index to need 5 (five) times more size than the vertex array in
	 * the graph. For a graph with 1.000.000 vertices, this array needs 4MB in
	 * the memory.
	 */
	public static final int VERTEX_INDEX_SIZE = 50;

	/**
	 * stores the already optimized syntaxgraphs (query strings are the keys,
	 * here).
	 */
	protected static Map<String, SoftReference<List<SyntaxGraphEntry>>> optimizedGraphs;

	public static synchronized void resetOptimizedSyntaxGraphs() {
		if (optimizedGraphs == null) {
			optimizedGraphs = new HashMap<String, SoftReference<List<SyntaxGraphEntry>>>();
		} else {
			optimizedGraphs.clear();
		}
	}

	/**
	 * The directory where the GreqlEvaluator stores the optimized syntax graphs
	 */
	protected static File optimizedSyntaxGraphsDirectory = getTmpDirectory();

	/**
	 * The directory where the {@link EvaluationLogger} stores and loads its
	 * logfiles from.
	 */
	protected static File evaluationLoggerDirectory = getTmpDirectory();

	/**
	 * stores the graph indizes (maps graphId values to GraphIndizes)
	 */
	protected static Map<String, SoftReference<GraphIndex>> graphIndizes;

	public static synchronized void resetGraphIndizes() {
		if (graphIndizes == null) {
			graphIndizes = new HashMap<String, SoftReference<GraphIndex>>();
		} else {
			graphIndizes.clear();
		}
	}

	/**
	 * The GraphMarker that stores all vertex evaluators
	 */
	protected GraphMarker<VertexEvaluator> vertexEvalGraphMarker;

	protected BooleanGraphMarker subgraphMarker;

	/**
	 * @return the subgraphMarker
	 */
	public BooleanGraphMarker getSubgraphMarker() {
		return subgraphMarker;
	}

	/**
	 * Sets the marker for evaluating only on marked elements. Also sets the
	 * datagraph to the given marker's graph.
	 * 
	 * @param subgraphMarker
	 *            the subgraphMarker to set
	 */
	public void setSubgraphMarker(BooleanGraphMarker subgraphMarker) {
		this.subgraphMarker = subgraphMarker;
		if (subgraphMarker != null) {
			datagraph = subgraphMarker.getGraph();
		}
	}

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

	/**
	 * Creates the map of optimized syntaxgraphs as soon as the GreqlEvaluator
	 * gets loaded
	 */
	static {
		resetOptimizedSyntaxGraphs();
		resetGraphIndizes();
	}

	private static Logger logger = Logger.getLogger(GreqlEvaluator.class
			.getName());

	/**
	 * Gets a vertex index for a part of a query
	 * 
	 * @param graph
	 *            the graph to get an index for
	 * @param queryPart
	 *            the query part to search for in the index structure
	 * @return a JValueSet with the result of that queryPart or null if the
	 *         query part is not yet indexed
	 */
	public static synchronized JValueSet getVertexIndex(Graph graph,
			String queryPart) {
		SoftReference<GraphIndex> ref = graphIndizes.get(graph.getId());
		if (ref == null) {
			return null;
		}
		GraphIndex index = ref.get();
		if (index == null) {
			graphIndizes.remove(ref);
			return null;
		}

		if (index.isValid(graph)) {
			return index.getVertexSet(queryPart);
		}
		return null;
	}

	/**
	 * Adds the given vertex set as the result of the given queryPart to the
	 * index of the given graph
	 */
	public static synchronized void addVertexIndex(Graph graph,
			String queryPart, JValueSet vertexSet) {
		SoftReference<GraphIndex> ref = graphIndizes.get(graph.getId());
		GraphIndex index = null;

		if (ref != null) {
			index = ref.get();
			if (index == null) {
				// remove the old reference
				graphIndizes.remove(ref);
			}
		}

		if (index == null) {
			index = new GraphIndex(graph);
			graphIndizes.put(graph.getId(),
					new SoftReference<GraphIndex>(index));
		}
		index.addVertexSet(queryPart, vertexSet);
	}

	/**
	 * @return th time barrier after which unused indices were removed
	 */
	public long getIndexTimeBarrier() {
		return INDEX_TIME_BARRIER;
	}

	/**
	 * adds a optimized graph to the list of syntaxgraphs. Sets the used flag to
	 */
	protected static synchronized void addOptimizedSyntaxGraph(
			String queryString, SyntaxGraphEntry entry) {
		SoftReference<List<SyntaxGraphEntry>> ref = optimizedGraphs
				.get(queryString);
		List<SyntaxGraphEntry> entryList = null;

		if (ref != null) {
			entryList = ref.get();
			if (entryList == null) {
				optimizedGraphs.remove(ref);
			}
		}

		if (entryList == null) {
			entryList = new ArrayList<SyntaxGraphEntry>();
		}
		if (!entryList.contains(entry)) {
			entryList.add(entry);
		}

		optimizedGraphs.put(queryString,
				new SoftReference<List<SyntaxGraphEntry>>(entryList));
	}

	/**
	 * gets an unlocked syntaxgraph out of the optimzedGraph map and locks it
	 */
	protected static synchronized SyntaxGraphEntry getOptimizedSyntaxGraph(
			String queryString, Optimizer optimizer, CostModel costModel) {
		SoftReference<List<SyntaxGraphEntry>> ref = optimizedGraphs
				.get(queryString);
		List<SyntaxGraphEntry> entryList = null;

		if (ref != null) {
			entryList = ref.get();
			if (entryList == null) {
				optimizedGraphs.remove(ref);
			}
		}
		if (entryList == null) {
			return null;
		}

		for (SyntaxGraphEntry entry : entryList) {
			if (entry.getCostModel().isEquivalent(costModel)) {
				Optimizer opt = entry.getOptimizer();
				if (((opt != null) && opt.isEquivalent(optimizer))
						|| ((opt == null) && (optimizer == null))) {
					if (entry.lock()) {
						return entry;
					}
				}
			}
		}

		return null;
	}

	/**
	 * Load all optimized {@link SyntaxGraphEntry}s in
	 * optimizedSyntaxGraphsDirectory.
	 * 
	 * @throws GraphIOException
	 *             if the optimizedGraphsDirectory is not accessible.
	 * @see #setOptimizedSyntaxGraphsDirectory(File)
	 * @see #getOptimizedSyntaxGraphsDirectory()
	 */
	public static synchronized void loadOptimizedSyntaxGraphs()
			throws GraphIOException {
		if (!(optimizedSyntaxGraphsDirectory.exists()
				&& optimizedSyntaxGraphsDirectory.canRead() && optimizedSyntaxGraphsDirectory
				.canExecute())) {
			throw new GraphIOException(
					optimizedSyntaxGraphsDirectory.getPath()
							+ " is not accessible.  Does it really exist and are the permissions ok?");
		}
		for (File syntaxGraphFile : optimizedSyntaxGraphsDirectory
				.listFiles(TGFilenameFilter.instance())) {
			logger.info("Loading SyntaxGraphEntry \""
					+ syntaxGraphFile.getPath() + "\".");
			SyntaxGraphEntry entry;
			try {
				entry = new SyntaxGraphEntry(syntaxGraphFile);
				addOptimizedSyntaxGraph(entry.getQueryText(), entry);
			} catch (GraphIOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Save all {@link SyntaxGraphEntry}s in optimizedGraphs to
	 * optimizedSyntaxGraphDirectory.
	 */
	public static synchronized void saveOptimizedSyntaxGraphs() {
		for (SoftReference<List<SyntaxGraphEntry>> ref : optimizedGraphs
				.values()) {
			List<SyntaxGraphEntry> entryList = ref.get();
			if (entryList == null) {
				continue;
			}
			for (SyntaxGraphEntry entry : entryList) {
				try {
					entry.saveToDirectory(optimizedSyntaxGraphsDirectory);
				} catch (GraphIOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * This attribute holds the query-string
	 */
	protected String queryString = null;

	public void setQuery(String queryString) {
		this.queryString = queryString;
		reset();
	}

	public String getQuery() {
		return queryString;
	}

	public void setQueryFile(File query) throws IOException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(query));

			String line = null;
			StringBuffer sb = new StringBuffer();
			while ((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}

			System.out.println("Query read from file:");
			System.out.println(sb.toString());
			setQuery(sb.toString());

		} finally {
			try {
				reader.close();
			} catch (IOException ex) {
				throw new RuntimeException(
						"An exception occurred while closing the stream.", ex);
			}
		}
	}

	/**
	 * Reset to before-start state, so that a new query can be evaluated.
	 */
	private void reset() {
		queryGraph = null;
		result = null;
		started = false;
		vertexEvalGraphMarker = null;
		knownTypes.clear();
	}

	/**
	 * This attribute holds the query-graph
	 */
	protected Greql2 queryGraph = null;

	/**
	 * This attribute holds the entry of the optimizedSyntaxGraph map that is
	 * currently used
	 */
	protected SyntaxGraphEntry syntaxGraphEntry = null;

	/**
	 * This is the optimizer who optimizes the greql2 syntaxgraph
	 */
	private Optimizer optimizer = null;

	/**
	 * If set to <code>true</code>, then the query will be optimized before
	 * evaluation. Otherwise it will be evaluated without any optimizations.
	 */
	private boolean optimize = true;

	/**
	 * If set to <code>true</code>, then a stored optimized syntaxgraph will be
	 * used if <code>optimize == true</code> and such a graph exists.
	 */
	private boolean useSavedOptimizedSyntaxGraph = true;

	/**
	 * This attribute holds the datagraph
	 */
	protected Graph datagraph = null;

	/**
	 * This attribute holds the result of the evaluation
	 */
	protected JValue result = null;

	/**
	 * This attribute holds the EvaluationLogger, which logs the evaluation
	 */
	protected EvaluationLogger evaluationLogger = null;

	/**
	 * The {@link LoggingType} that should be used. It defaults to
	 * {@link LoggingType#SCHEMA}.
	 */
	protected LoggingType evaluationLoggingType = LoggingType.SCHEMA;

	/**
	 * This attribute holds the CostModel which estimates the evaluation costs
	 */
	protected CostModel costModel = null;

	/**
	 * The progress function this evaluator uses, may be null
	 */
	protected ProgressFunction progressFunction = null;

	/**
	 * holds the number of interpretetation steps that have been passed since
	 * the last call of the progress function
	 */
	protected long progressStepsPassed = 0;

	/**
	 * the time the ovarall evaluation (parsing + optimization + evluation) took
	 * in milliseconds
	 */
	protected long overallEvaluationTime = -1;

	/**
	 * The plain time needed for evaluation.
	 */
	protected long plainEvaluationTime = -1;

	/**
	 * The time needed for optimization.
	 */
	protected long optimizationTime = -1;

	/**
	 * The time needed for parsing the query.
	 */
	protected long parseTime = -1;

	/**
	 * @return the time the ovarall evaluation (parsing + optimization +
	 *         evluation) took in milliseconds
	 */
	public long getOverallEvaluationTime() {
		return overallEvaluationTime;
	}

	/**
	 * This attribute is true if the evaluation has already been started, false
	 * otherwise
	 */
	protected boolean started = false;

	/**
	 * Holds the variables that are defined via using, they are called bound or
	 * free variables
	 */
	protected Map<String, JValue> variableMap = null;

	/**
	 * Holds the greql subqueries that can be called like other greql functions.
	 */
	protected Map<String, Greql2> subQueryMap = null;

	/**
	 * Holds the estimated needed for evaluation time in abstract units
	 */
	protected long estimatedInterpretationSteps = 0;

	/**
	 * Holds the already passed time in abstract time units
	 */
	protected long passedInterpretationSteps = 0;

	/**
	 * should be called by every vertex evaluator to indicate a progress. The
	 * given value should be the ownEvaluationCosts of that VertexEvaluator.
	 * Calls the progress()-Method of the progress function this evaluator uses
	 */
	public final void progress(long value) {
		progressStepsPassed += value;
		if (progressFunction != null) {
			while (progressStepsPassed > progressFunction.getUpdateInterval()) {
				progressFunction.progress(1);
				progressStepsPassed -= progressFunction.getUpdateInterval();
			}
		}
		passedInterpretationSteps += value;
	}

	/**
	 * returns the changes variableMap
	 */
	public Map<String, JValue> getVariables() {
		return variableMap;
	}

	public JValue getVariable(String name) {
		if ((variableMap != null) && variableMap.containsKey(name)) {
			return variableMap.get(name);
		}
		return new JValueImpl();
	}

	public void setVariables(Map<String, JValue> varMap) {
		variableMap = varMap;
	}

	public void setVariable(String varName, JValue value) {
		if (variableMap == null) {
			variableMap = new HashMap<String, JValue>();
		}
		variableMap.put(varName, value);
	}

	public void setSubQuery(String name, String greqlQuery) {
		if (name == null) {
			throw new EvaluateException(
					"The name of a subquery must not be null!");
		}
		if (!name.matches("^\\w+$")) {
			throw new EvaluateException("Invalid subquery name '" + name
					+ "'. Only word chars are allowed.");
		}

		Set<String> definedSubQueries = subQueryMap.keySet();
		HashSet<String> subQueryNames = new HashSet<String>(
				definedSubQueries.size() + 1);
		subQueryNames.addAll(definedSubQueries);
		subQueryNames.add(name);
		GreqlParser parser = new GreqlParser(greqlQuery, subQueryNames);
		try {
			parser.parse();
		} catch (Exception e) {
			// e.printStackTrace();
			throw new EvaluateException("Error parsing subquery \""
					+ greqlQuery + "\".", e);
		}
		Greql2 subQueryGraph = parser.getGraph();
		if (isOptimize()) {
			if (optimizer == null) {
				optimizer = new DefaultOptimizer();
			}
			optimizer.optimize(this, subQueryGraph);
		}
		if (Greql2FunctionLibrary.instance().isGreqlFunction(name)) {
			throw new Greql2Exception("The subquery '" + name
					+ "' would shadow a GReQL function!");
		}
		for (FunctionApplication fa : subQueryGraph
				.getFunctionApplicationVertices()) {
			if (name.equals(fa.get_functionId().get_name())) {
				logger.warning("You are defining a recursive subquery '"
						+ name
						+ "'. Don't expect this to be fast or memory-efficient!");
			}
		}
		subQueryMap.put(name, subQueryGraph);
		// System.out.println("\nGiven subquery:");
		// System.out.println(greqlQuery);
		// System.out.println("\nOptimized subquery:");
		// System.out.println(Greql2Serializer.serialize(subQueryGraph));
	}

	public Greql2 getSubQuery(String name) {
		return subQueryMap.get(name);
	}

	/**
	 * returns the result of the evaluation
	 */
	public JValue getEvaluationResult() {
		return result;
	}

	/**
	 * returns the logging-component
	 */
	public final EvaluationLogger getEvaluationLogger() {
		return evaluationLogger;
	}

	/**
	 * @return the tmp directory of that system. On Unix/Linux this is /tmp.
	 */
	private static File getTmpDirectory() {
		File tmpFile = null, tmpDir = null;
		try {
			tmpFile = File.createTempFile("_tmp", "xyz");
			tmpDir = tmpFile.getParentFile();
			tmpFile.delete();
		} catch (IOException e) {
			e.printStackTrace();
			tmpDir = new File("/tmp/");
		}
		return tmpDir;
	}

	/**
	 * creates the logging-component
	 */
	protected void createEvaluationLogger() {
		if (evaluationLogger == null) {
			try {
				evaluationLogger = new Level2Logger(evaluationLoggerDirectory,
						datagraph, evaluationLoggingType);
				if (evaluationLogger.load()) {
					logger.info("Successfully loaded logger file "
							+ evaluationLogger.getLogfileName() + ".");
				} else {
					logger.info("Couldn't load logger file "
							+ evaluationLogger.getLogfileName() + ".");
				}
			} catch (InterruptedException e) {
				// TODO (heimdall) Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * sets the logger which is used to log the evlauation
	 */
	public void setEvaluationLogger(EvaluationLogger logger) {
		evaluationLogger = logger;
	}

	/**
	 * returns the CostModel which is used to estimate the evaluation costs
	 */
	public CostModel getCostModel() {
		return costModel;
	}

	/**
	 * sets the CostModel which is used to estimate the evaluation costs
	 */
	public void setCostModel(CostModel m) {
		costModel = m;
	}

	/**
	 * returns the query syntaxgraph
	 */
	public Greql2 getSyntaxGraph() {
		return queryGraph;
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
	public GreqlEvaluator(String query, Graph datagraph,
			Map<String, JValue> variables, ProgressFunction progressFunction) {
		this(datagraph, variables, progressFunction);
		setQuery(query);
	}

	private GreqlEvaluator(Graph datagraph, Map<String, JValue> variables,
			ProgressFunction progressFunction) {
		if (datagraph == null) {
			this.datagraph = createMinimalGraph();
		} else {
			this.datagraph = datagraph;
		}
		knownTypes = new HashMap<String, AttributedElementClass>();
		variableMap = variables;
		subQueryMap = new HashMap<String, Greql2>();
		this.progressFunction = progressFunction;
	}

	public void addKnownType(AttributedElementClass knownType) {
		knownTypes.put(knownType.getSimpleName(), knownType);
	}

	public AttributedElementClass getKnownType(String typeSimpleName) {
		return knownTypes.get(typeSimpleName);
	}

	private Graph minimalGraph = null;

	/**
	 * @return a minimal graph (no vertices and no edges) of a minimal schema.
	 */
	private Graph createMinimalGraph() {
		if (minimalGraph == null) {
			Schema minimalSchema = new SchemaImpl("MinimalSchema",
					"de.uni_koblenz.jgralab.greqlminschema");
			GraphClass gc = minimalSchema.createGraphClass("MinimalGraph");
			VertexClass n = gc.createVertexClass("Node");
			gc.createEdgeClass("Link", n, 0, Integer.MAX_VALUE, "",
					AggregationKind.NONE, n, 0, Integer.MAX_VALUE, "",
					AggregationKind.NONE);
			minimalSchema.compile(CodeGeneratorConfiguration.MINIMAL);
			Method graphCreateMethod = minimalSchema
					.getGraphCreateMethod(ImplementationType.STANDARD);

			try {
				minimalGraph = (Graph) graphCreateMethod.invoke(null,
						new Object[] { "test", 1, 1 });
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return minimalGraph;
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
	 */
	public GreqlEvaluator(String query, Graph datagraph,
			Map<String, JValue> variables) {
		this(query, datagraph, variables, null);
	}

	/**
	 * Creates an new GreqlEvaluator for the query in the given file and the
	 * given datagraph
	 * 
	 * @param queryFile
	 *            the name of the file whehre the query to evaluate is stored in
	 * @param datagraph
	 *            the Datagraph on which the query gets evaluated
	 * @param variables
	 *            a Map<String, JValue> of bound variables
	 * @param progressFunction
	 *            the ProgressFunction which indicates the progress, for
	 *            instance display a progress bar etc.
	 */
	public GreqlEvaluator(File queryFile, Graph datagraph,
			Map<String, JValue> variables, ProgressFunction progressFunction)
			throws FileNotFoundException, IOException {
		this(datagraph, variables, progressFunction);
		// Read query from file (afuhr)
		setQueryFile(queryFile);
	}

	/**
	 * Creates an new GreqlEvaluator for the query in the given file and the
	 * given datagraph
	 * 
	 * @param queryFile
	 *            the name of the file whehre the query to evaluate is stored in
	 * @param datagraph
	 *            the Datagraph on which the query gets evaluated
	 * @param variables
	 *            a Map<String, JValue> of bound variables
	 */
	public GreqlEvaluator(File queryFile, Graph datagraph,
			Map<String, JValue> variables) throws FileNotFoundException,
			IOException {
		this(queryFile, datagraph, variables, null);
	}

	/**
	 * returns the datagraph
	 */
	public Graph getDatagraph() {
		return datagraph;
	}

	/**
	 * Parses the given query-string and creates the query-graph out of it
	 */
	protected boolean parseQuery(String query) throws EvaluateException {
		long parseStartTime = System.currentTimeMillis();
		GreqlParser parser = new GreqlParser(query, subQueryMap.keySet());
		try {
			parser.parse();
		} catch (Exception e) {
			// e.printStackTrace();
			throw new EvaluateException("Error parsing query \"" + queryString
					+ "\".", e);
		}
		queryGraph = parser.getGraph();
		parseTime = System.currentTimeMillis() - parseStartTime;
		return true;
	}

	/**
	 * Creates the VertexEvaluator-Object at the vertices in the syntaxgraph
	 */
	public void createVertexEvaluators() throws EvaluateException {
		if (queryGraph == null) {
			return;
		}
		if (vertexEvalGraphMarker != null) {
			queryGraph
					.removeGraphStructureChangedListener(vertexEvalGraphMarker);
		}
		vertexEvalGraphMarker = new GraphMarker<VertexEvaluator>(queryGraph);
		Vertex currentVertex = queryGraph.getFirstVertex();
		while (currentVertex != null) {
			VertexEvaluator vertexEval = VertexEvaluator.createVertexEvaluator(
					currentVertex, this);
			if (vertexEval != null) {
				vertexEvalGraphMarker.mark(currentVertex, vertexEval);
			}
			currentVertex = currentVertex.getNextVertex();
		}
	}

	/**
	 * clears the tempresults that are stored in the VertexEvaluators-Objects at
	 * the syntaxgraph nodes
	 * 
	 * @param optimizer
	 */
	private void resetVertexEvaluators() {
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

	/**
	 * Transforms the textual query representation into an optimized
	 * syntaxgraph, adds the VertexEvaluator object to the vertices of that
	 * graph and stores this as attribute queryGraph.
	 */
	protected void createOptimizedSyntaxGraph() throws EvaluateException,
			OptimizerException {
		long optimizerStartTime = System.currentTimeMillis();
		if (optimizer == null) {
			optimizer = new DefaultOptimizer();
		}
		if (useSavedOptimizedSyntaxGraph
				&& optimizedGraphs.containsKey(queryString)) {
			syntaxGraphEntry = getOptimizedSyntaxGraph(queryString, optimizer,
					costModel);
			if (syntaxGraphEntry != null) {
				queryGraph = syntaxGraphEntry.getSyntaxGraph();
				createVertexEvaluators();
				logger.info("Using stored optimized syntax graph.");
				return;
			}
		}

		// No optimized graph for this query, optimizer and costmodel was found.
		if (queryGraph == null) {
			parseQuery(queryString);
		}

		if (DEBUG_OPTIMIZATION) {
			System.out
					.println("#########################################################");
			System.out
					.println("################## Unoptimized Query ####################");
			System.out
					.println("#########################################################");
			String name = "__greql-query.";
			try {
				GraphIO.saveGraphToFile(name + "tg", queryGraph,
						new ConsoleProgressFunction());
				printGraphAsDot(queryGraph, true, name + "dot");
			} catch (GraphIOException e) {
				e.printStackTrace();
			}
			System.out.println("Saved query graph to " + name + "tg/dot.");
			System.out
					.println("#########################################################");
		}

		createVertexEvaluators();
		optimizer.optimize(this, queryGraph);
		syntaxGraphEntry = new SyntaxGraphEntry(queryString, queryGraph,
				optimizer, costModel, true);
		addOptimizedSyntaxGraph(queryString, syntaxGraphEntry);
		createVertexEvaluators();
		optimizationTime = System.currentTimeMillis() - optimizerStartTime;
	}

	/**
	 * same as startEvaluation(false, true), provides for convenience
	 * 
	 * @return true if the evaluation succeeds, false otherwise
	 * @throws EvaluateException
	 */
	public boolean startEvaluation() throws EvaluateException,
			OptimizerException {
		return startEvaluation(false, false);
	}

	/**
	 * Starts the evaluation. If the query is a store-query, modifies the bound
	 * variables
	 * 
	 * @param writeLogs
	 *            if set to true, the evaluation measures will be written to
	 *            logfiles.
	 * @param readLogs
	 *            if set true, existing log files will be read to give better
	 *            cost estimations.
	 * @return true on success, false otherwise
	 * @throws EvaluateException
	 *             if something gets wrong during evaluation
	 */
	public boolean startEvaluation(boolean writeLogs, boolean readLogs)
			throws EvaluateException, OptimizerException {
		if (started) {
			return result != null;
		}

		started = true;
		parseTime = 0;
		optimizationTime = 0;
		plainEvaluationTime = 0;
		overallEvaluationTime = 0;

		long startTime = System.currentTimeMillis();

		if (datagraph == null) {
			datagraph = createMinimalGraph();
		}

		if (writeLogs) {
			createEvaluationLogger();
		}

		// Initialize the CostModel if there's none
		if (costModel == null) {
			if (readLogs) {
				Level2LogReader logReader;
				if (evaluationLogger == null) {
					evaluationLoggerDirectory = getTmpDirectory();
					logReader = new Level2LogReader(evaluationLoggerDirectory,
							datagraph);
				} else {
					logReader = new Level2LogReader(
							(Level2Logger) evaluationLogger);
				}
				try {
					costModel = new LogCostModel(logReader, 0.8f);
				} catch (CostModelException e) {
					e.printStackTrace();
				}
			} else {
				costModel = new DefaultCostModel();
			}
		}

		if (optimize) {
			createOptimizedSyntaxGraph();
			if (DEBUG_OPTIMIZATION) {
				System.out
						.println("#########################################################");
				System.out
						.println("################### Optimized Query #####################");
				System.out
						.println("#########################################################");
				if (queryGraph instanceof SerializableGreql2) {
					System.out.println(((SerializableGreql2) queryGraph)
							.serialize());
				} else {
					System.out.println("Couldn't serialize Greql2 graph...");
				}
				String name = "__optimized-greql-query.";
				try {
					GraphIO.saveGraphToFile(name + "tg", queryGraph,
							new ConsoleProgressFunction());
					printGraphAsDot(queryGraph, true, name + "dot");
				} catch (GraphIOException e) {
					e.printStackTrace();
				}
				System.out.println("Saved optimized query graph to " + name
						+ "tg/dot.");
				System.out
						.println("#########################################################");
			}

		} else {
			parseQuery(queryString);
			createVertexEvaluators();
		}

		if (queryGraph == null) {
			throw new RuntimeException(
					"Empty query graph supplied, no evaluation possible");
		}

		if (queryGraph.getVCount() <= 1) {
			// Graph contains only root vertex
			result = new JValueImpl();
			return true;
		}

		// Calculate the evaluation costs
		VertexEvaluator greql2ExpEval = vertexEvalGraphMarker
				.getMark(queryGraph.getFirstGreql2Expression());

		if (progressFunction != null) {
			estimatedInterpretationSteps = greql2ExpEval
					.getInitialSubtreeEvaluationCosts(new GraphSize(datagraph));

			progressFunction.init(estimatedInterpretationSteps);
		}

		long plainEvaluationStartTime = System.currentTimeMillis();

		result = greql2ExpEval.getResult(subgraphMarker);

		// last, remove all added tempAttributes, currently, this are only
		// subgraphAttributes
		if (progressFunction != null) {
			progressFunction.finished();
		}

		plainEvaluationTime = System.currentTimeMillis()
				- plainEvaluationStartTime;

		if (evaluationLogger != null) {
			try {
				if (evaluationLogger.store()) {
					logger.info("Successfully stored logfile to "
							+ evaluationLogger.getLogfileName() + ".");
				} else {
					logger.warning("Couldn't store logfile to "
							+ evaluationLogger.getLogfileName() + ".");
				}

			} catch (IOException ex) {
				throw new EvaluateException("Error writing log to file: "
						+ evaluationLogger.getLogfileName(), ex);
			}
		}

		resetVertexEvaluators();
		if (syntaxGraphEntry != null) {
			syntaxGraphEntry.release();
		}

		overallEvaluationTime = System.currentTimeMillis() - startTime;
		started = false;
		return true;
	}

	private void printGraphAsDot(Graph graph, boolean reversedEdges,
			String outputFilename) {

		try {
			Class<?> tg2DotClass = Class
					.forName("de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot");
			Method printMethod = tg2DotClass.getMethod("printGraphAsDot",
					Graph.class, boolean.class, String.class);
			printMethod.invoke(tg2DotClass, new Object[] { graph,
					reversedEdges, outputFilename });
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Sets the optimizer to optimize the syntaxgraph this evaluator evaluates
	 * 
	 * @param optimizer
	 *            the optimizer to use
	 */
	public void setOptimizer(Optimizer optimizer) {
		this.optimizer = optimizer;
	}

	/**
	 * Sets the optimizer to optimize the syntaxgraph this evaluator evaluates
	 */
	public Optimizer getOptimizer() {
		return optimizer;
	}

	public void setDatagraph(Graph datagraph) {
		this.datagraph = datagraph;
	}

	/**
	 * @return <code>true</code> if the query will be optimized before
	 *         evaluation, <code>false</code> otherwise.
	 */
	public boolean isOptimize() {
		return optimize;
	}

	/**
	 * @param optimize
	 *            If <code>true</code>, then the query will be optimized before
	 *            evaluation. If <code>false</code> it will be evaluated without
	 *            any optimizations.
	 */
	public void setOptimize(boolean optimize) {
		this.optimize = optimize;
	}

	/**
	 * @return the time needed for optimizing the query or -1 if no optimization
	 *         was done.
	 */
	public long getOptimizationTime() {
		return optimizationTime;
	}

	/**
	 * @return the time needed for parsing the query.
	 */
	public long getParseTime() {
		return parseTime;
	}

	/**
	 * @return the time needed for plain evaluation.
	 */
	public long getPlainEvaluationTime() {
		return plainEvaluationTime;
	}

	public void printEvaluationTimes() {
		logger.info("Overall evaluation took "
				+ overallEvaluationTime
				/ 1000d
				+ " seconds.\n"
				+ " --> parsing time         : "
				+ parseTime
				/ 1000d
				+ "\n --> optimization time    : "
				+ optimizationTime
				/ 1000d
				+ "\n --> plain evaluation time: "
				+ plainEvaluationTime
				/ 1000d
				+ "\n"
				+ (progressFunction != null ? "Estimated evaluation costs: "
						+ estimatedInterpretationSteps : ""));
	}

	public static File getOptimizedSyntaxGraphsDirectory() {
		return optimizedSyntaxGraphsDirectory;
	}

	public static void setOptimizedSyntaxGraphsDirectory(
			File optimizedSyntaxGraphsDirectory) {
		GreqlEvaluator.optimizedSyntaxGraphsDirectory = optimizedSyntaxGraphsDirectory;
	}

	/**
	 * @return The directory where the {@link EvaluationLogger} stores and loads
	 *         its logfiles.
	 */
	public static File getEvaluationLoggerDirectory() {
		return evaluationLoggerDirectory;
	}

	/**
	 * @param loggerDirectory
	 *            The directory where the {@link EvaluationLogger} stores and
	 *            loads its logfiles.
	 */
	public static void setEvaluationLoggerDirectory(File loggerDirectory) {
		GreqlEvaluator.evaluationLoggerDirectory = loggerDirectory;
	}

	public LoggingType getEvaluationLoggingType() {
		return evaluationLoggingType;
	}

	public void setEvaluationLoggingType(LoggingType loggerLoggingType) {
		evaluationLoggingType = loggerLoggingType;
		evaluationLogger = null;
		costModel = null;
	}

	public boolean isUseSavedOptimizedSyntaxGraph() {
		return useSavedOptimizedSyntaxGraph;
	}

	public void setUseSavedOptimizedSyntaxGraph(
			boolean useSavedOptimizedSyntaxGraph) {
		this.useSavedOptimizedSyntaxGraph = useSavedOptimizedSyntaxGraph;
	}
}
