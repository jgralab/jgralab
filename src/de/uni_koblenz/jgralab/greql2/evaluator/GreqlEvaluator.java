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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.GraphMarker;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.GraphIO.TGFilenameFilter;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.LogCostModel;
import de.uni_koblenz.jgralab.greql2.evaluator.logging.EvaluationLogger;
import de.uni_koblenz.jgralab.greql2.evaluator.logging.Level2LogReader;
import de.uni_koblenz.jgralab.greql2.evaluator.logging.Level2Logger;
import de.uni_koblenz.jgralab.greql2.evaluator.logging.LoggingType;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.CostModelException;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.OptimizerException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.greql2.optimizer.DefaultOptimizer;
import de.uni_koblenz.jgralab.greql2.optimizer.Optimizer;
import de.uni_koblenz.jgralab.greql2.parser.ManualGreqlParser;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.impl.ProgressFunctionImpl;
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
			datagraph = GraphIO.loadSchemaAndGraphFromFile(args[1], false,
					new ProgressFunctionImpl());
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
	 * stores the already optimized syntaxgraphs
	 */
	protected static Map<String, List<SyntaxGraphEntry>> optimizedGraphs;

	public static void resetOptimizedSyntaxGraphs() {
		synchronized (GreqlEvaluator.class) {
			if (optimizedGraphs == null) {
				optimizedGraphs = new HashMap<String, List<SyntaxGraphEntry>>();
			} else {
				optimizedGraphs.clear();
			}
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
	 * stores the graph indizes
	 */
	protected static Map<String, GraphIndex> graphIndizes;

	public static void resetGraphIndizes() {
		synchronized (GreqlEvaluator.class) {
			if (graphIndizes == null) {
				graphIndizes = new HashMap<String, GraphIndex>();
			} else {
				graphIndizes.clear();
			}
		}
	}

	/**
	 * The GraphMarker that stores all vertex evaluators
	 */
	protected GraphMarker<VertexEvaluator> vertexEvalGraphMarker;

	/**
	 * The map of SimpleName to Type of types that is known in the evaluator by
	 * import statements in the greql query
	 */
	protected Map<String, AttributedElementClass> knownTypes;

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
	public synchronized JValueSet getVertexIndex(Graph graph, String queryPart) {
		GraphIndex index = graphIndizes.get(graph.getId());
		if (index != null) {
			if (index.isValid(graph)) {
				return index.getVertexSet(queryPart);
			}
		}
		return null;
	}

	/**
	 * Adds the given vertex set as the result of the given queryPart to the
	 * index of the given graph
	 */
	public synchronized void addVertexIndex(Graph graph, String queryPart,
			JValueSet vertexSet) {
		GraphIndex index = graphIndizes.get(graph.getId());
		if (index == null) {
			index = new GraphIndex(graph);
			graphIndizes.put(graph.getId(), index);
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
		List<SyntaxGraphEntry> entryList = optimizedGraphs.get(queryString);
		if (entryList == null) {
			entryList = new ArrayList<SyntaxGraphEntry>();
		}
		if (!entryList.contains(entry)) {
			entryList.add(entry);
		}
		if (!optimizedGraphs.containsKey(queryString)) {
			optimizedGraphs.put(queryString, entryList);
		}
	}

	/**
	 * gets an unlocked syntaxgraph out of the optimzedGraph map and locks it
	 */
	protected static synchronized SyntaxGraphEntry getOptimizedSyntaxGraph(
			String queryString, Optimizer optimizer, CostModel costModel) {
		List<SyntaxGraphEntry> entryList = optimizedGraphs.get(queryString);
		if (entryList != null) {
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
				.listFiles(new TGFilenameFilter())) {
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
		for (List<SyntaxGraphEntry> entryList : optimizedGraphs.values()) {
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

	/**
	 * Reset to before-start state, so that a new query can be evaluated.
	 */
	private void reset() {
		queryGraph = null;
		result = null;
		started = false;
		vertexEvalGraphMarker = null;
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
	 * This attribute hold the schema of the GReQL-Syntaxgraph
	 */
	protected Schema greqlSchema = null;

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
	private boolean useSavedOptimizedSyntaxGraph = false;

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
	 * Holds the estimated needed for evaluation time in abstract units
	 */
	protected long estimatedInterpretationSteps = 0;

	/**
	 * Holds the already passed time in abstract time units
	 */
	protected long passedInterpretationSteps = 0;

	/**
	 * Gets the estimated number of interpretation steps the evaluation will
	 * need
	 */
	public long getEstimatedInterpretationSteps() {
		return estimatedInterpretationSteps;
	}

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

	public void setVariables(Map<String, JValue> varMap) {
		variableMap = varMap;
	}

	public void setVariable(String varName, JValue value) {
		if (variableMap == null) {
			variableMap = new HashMap<String, JValue>();
		}
		variableMap.put(varName, value);
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
			} catch (InterruptedException e) {
				// TODO (heimdall) Auto-generated catch block
				e.printStackTrace();
			}
			if (evaluationLogger.load()) {
				logger.info("Successfully loaded logger file "
						+ evaluationLogger.getLogfileName() + ".");
			} else {
				logger.info("Couldn't load logger file "
						+ evaluationLogger.getLogfileName() + ".");
			}
		}
	}

	/**
	 * sets the logger which is used to log the evlauation
	 */
	public void setEvaluationLogger(EvaluationLogger logger) {
		this.evaluationLogger = logger;
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
		if (datagraph == null) {
			this.datagraph = createMinimalGraph();
		} else {
			this.datagraph = datagraph;
		}
		this.queryString = query;
		knownTypes = new HashMap<String, AttributedElementClass>();
		normalizeQueryString();
		this.variableMap = variables;
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
			gc.createEdgeClass("Link", n, n);
			minimalSchema.compile(false);
			Method graphCreateMethod = minimalSchema.getGraphCreateMethod();

			try {
				minimalGraph = (Graph) (graphCreateMethod.invoke(null,
						new Object[] { "test", 1, 1 }));
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
		if (datagraph == null) {
			this.datagraph = createMinimalGraph();
		} else {
			this.datagraph = datagraph;
		}
		this.variableMap = variables;
		this.progressFunction = progressFunction;
		BufferedReader reader = new BufferedReader(new FileReader(queryFile));
		String line = null;
		queryString = "";
		while ((line = reader.readLine()) != null) {
			queryString += line + "\n";
		}
		reader.close();
		normalizeQueryString();
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
	 * Creates a new GreqlEvaluator for the given queryGraph and Datagraph
	 * 
	 * @param queryGraph
	 *            the graph-representation of the query to evaluate
	 * @param dataGraph
	 *            the Datagraph on which the query gets evaluated
	 * @param variables
	 *            a Map<String, JValue> of bound variables,
	 */
	public GreqlEvaluator(Greql2 queryGraph, Graph dataGraph,
			Map<String, JValue> variables) {
		if (dataGraph == null) {
			this.datagraph = createMinimalGraph();
		} else {
			this.datagraph = dataGraph;
		}
		this.queryGraph = queryGraph;
		this.variableMap = variables;
	}

	public void normalizeQueryString() {
		if (queryString == null) {
			return;
		}
		// Delete one-line-comments (//).
		queryString = queryString.replaceAll("//.*", "");
		// Replace newlines with space.
		queryString = queryString.replaceAll("[\n]+", " ");
		// Delete multi-line-comments (/* ... */).
		queryString = queryString.replaceAll("/\\*.*?\\*/", " ");
		// Replace any occurence of more than one whitespace with exactly one
		// whitespace.
		queryString = queryString.replaceAll("\\s{2,}", " ");
		// Delete the space at the front and at the end of the query.
		queryString = queryString.trim();
		// System.out.println("Normalized Query = \"" + queryString + "\".");
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
		ManualGreqlParser parser = new ManualGreqlParser(query);
		try {
			parser.parse();
		} catch (Exception e) {
			// e.printStackTrace();
			throw new EvaluateException("Error parsing query \"" + queryString
					+ "\".", e);
		}
		greqlSchema = parser.getSchema();
		queryGraph = parser.getGraph();
		return true;
	}

	/**
	 * Force parsing the query so that the query graph can be gotten.
	 * 
	 * @return <code>true</code> if parsing was successful, <code>false</code>
	 *         otherwise.
	 * 
	 * @throws EvaluateException
	 *             if an error occurs while parsing.
	 */
	public boolean parseQuery() throws EvaluateException {
		return parseQuery(queryString);
	}

	/**
	 * Creates the VertexEvaluator-Object at the vertices in the syntaxgraph
	 */
	public void createVertexEvaluators() throws EvaluateException {
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
				costModel.setGreqlEvaluator(this);
				logger.info("Using stored optimized syntax graph.");
				return;
			}
		}

		// No optimized graph for this query, optimizer and costmodel was found.
		optimizer.optimize(this, queryGraph);
		syntaxGraphEntry = new SyntaxGraphEntry(queryString, queryGraph,
				optimizer, costModel, true);
		addOptimizedSyntaxGraph(queryString, syntaxGraphEntry);
	}

	/**
	 * same as startEvaluation(false), provides for convenience
	 * 
	 * @return true if the evaluation succeeds, false otherwise
	 * @throws EvaluateException
	 */
	public boolean startEvaluation() throws EvaluateException,
			OptimizerException {
		return startEvaluation(false);
	}

	/**
	 * Starts the evaluation. If the query is a store-query, modifies the bound
	 * variables
	 * 
	 * @param log
	 *            if set to true, the evaluation will be logged. If no logger
	 *            was set before, the Level2Logger is used
	 * @return true on success, false otherwise
	 * @throws EvaluateException
	 *             if something gets wrong during evaluation
	 */
	public boolean startEvaluation(boolean log) throws EvaluateException,
			OptimizerException {
		if (started) {
			return (result != null);
		}
		started = true;

		long startTime = System.currentTimeMillis();

		if (log) {
			createEvaluationLogger();
		}

		long parseStartTime = System.currentTimeMillis();
		if (queryString != null) {
			parseQuery(queryString);
		}
		parseTime = System.currentTimeMillis() - parseStartTime;
		if (queryGraph == null) {
			throw new RuntimeException(
					"Empty query graph supplied, no evaluation possible");
		}

		createVertexEvaluators();

		// Initialize the CostModel if there's none
		if (costModel == null) {
			// costModel = new DefaultCostModel(vertexEvalGraphMarker);
			Level2LogReader logReader;
			if (evaluationLogger == null) {
				// Create only a generic log reader by default.
				evaluationLoggerDirectory = getTmpDirectory();
				logReader = new Level2LogReader(evaluationLoggerDirectory);
			} else {
				logReader = new Level2LogReader((Level2Logger) evaluationLogger);
			}
			try {
				costModel = new LogCostModel(logReader, 0.7f, this);
			} catch (CostModelException e) {
				e.printStackTrace();
			}
		}
		long optimizerStartTime = System.currentTimeMillis();

		if (optimize) {
			createOptimizedSyntaxGraph();
		}

		optimizationTime = System.currentTimeMillis() - optimizerStartTime;

		// Calculate the evaluation costs
		VertexEvaluator greql2ExpEval = vertexEvalGraphMarker
				.getMark(queryGraph.getFirstGreql2Expression());

		estimatedInterpretationSteps = greql2ExpEval
				.getInitialSubtreeEvaluationCosts(new GraphSize(datagraph));

		if (progressFunction != null) {
			progressFunction.init(estimatedInterpretationSteps);
		}

		long plainEvaluationStartTime = System.currentTimeMillis();
		result = vertexEvalGraphMarker.getMark(
				queryGraph.getFirstGreql2Expression()).getResult(null);

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

		return true;
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

	public void printEvaluationTimes() {
		logger.info("Overall evaluation took " + overallEvaluationTime / 1000d
				+ " seconds.\n" + " --> parsing time         : " + parseTime
				/ 1000d + "\n --> optimization time    : " + optimizationTime
				/ 1000d + "\n --> plain evaluation time: "
				+ plainEvaluationTime / 1000d
				+ "\nEstimated evaluation costs: "
				+ estimatedInterpretationSteps);
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
		this.evaluationLoggingType = loggerLoggingType;
	}

	public boolean isUseSavedOptimizedSyntaxGraph() {
		return useSavedOptimizedSyntaxGraph;
	}

	public void setUseSavedOptimizedSyntaxGraph(
			boolean useSavedOptimizedSyntaxGraph) {
		this.useSavedOptimizedSyntaxGraph = useSavedOptimizedSyntaxGraph;
	}
}
