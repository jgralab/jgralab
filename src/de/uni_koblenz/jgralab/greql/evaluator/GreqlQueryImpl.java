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
package de.uni_koblenz.jgralab.greql.evaluator;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.pcollections.PSet;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.GraphStructureChangedListener;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
import de.uni_koblenz.jgralab.greql.GreqlEnvironment;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.OptimizerInfo;
import de.uni_koblenz.jgralab.greql.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql.optimizer.DefaultOptimizer;
import de.uni_koblenz.jgralab.greql.optimizer.Optimizer;
import de.uni_koblenz.jgralab.greql.optimizer.OptimizerUtility;
import de.uni_koblenz.jgralab.greql.parser.GreqlParser;
import de.uni_koblenz.jgralab.greql.schema.GreqlExpression;
import de.uni_koblenz.jgralab.greql.schema.GreqlGraph;
import de.uni_koblenz.jgralab.greql.schema.GreqlVertex;
import de.uni_koblenz.jgralab.greql.schema.Identifier;
import de.uni_koblenz.jgralab.greql.schema.UndefinedLiteral;
import de.uni_koblenz.jgralab.greql.schema.Variable;
import de.uni_koblenz.jgralab.impl.std.GraphImpl;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Schema;

public class GreqlQueryImpl extends GreqlQuery implements
		GraphStructureChangedListener {
	private final String queryText;
	private GreqlGraph queryGraph;
	private PSet<String> usedVariables;
	private PSet<String> storedVariables;
	private final boolean optimize;
	private final OptimizerInfo optimizerInfo;
	private Optimizer optimizer;
	// private final boolean useSavedOptimizedSyntaxGraph = true;
	private GreqlExpression rootExpression;

	// Log levels:
	// INFO: log optimizer debugging
	// FINE: log parse/optimization times
	private static Logger logger = JGraLab.getLogger(GreqlQueryImpl.class);

	/**
	 * Print the text representation of the optimized query after optimization.
	 */
	public static boolean DEBUG_OPTIMIZATION = Boolean.parseBoolean(System
			.getProperty("greqlDebugOptimization", "false"));

	/**
	 * The {@link Map} of SimpleName to Type of types that is known in the
	 * evaluator by import statements in the greql query
	 */
	protected Map<Schema, Map<String, AttributedElementClass<?, ?>>> importedTypes;

	/**
	 * The {@link GraphMarker} that stores all vertex evaluators
	 */
	private GraphMarker<VertexEvaluator<? extends GreqlVertex>> vertexEvaluators;

	public GreqlQueryImpl(String queryText) {
		this(queryText, true);
	}

	public GreqlQueryImpl(String queryText, boolean optimize) {
		this(queryText, optimize, OptimizerUtility.getDefaultOptimizerInfo());
	}

	public GreqlQueryImpl(String queryText, OptimizerInfo optimizerInfo) {
		this(queryText, true, optimizerInfo);
	}

	public GreqlQueryImpl(String queryText, Optimizer optimizer) {
		this(queryText, optimizer != null, OptimizerUtility
				.getDefaultOptimizerInfo(), optimizer);
	}

	public GreqlQueryImpl(String queryText, boolean optimize,
			OptimizerInfo optimizerInfo) {
		this.queryText = queryText;
		this.optimize = optimize;
		this.optimizerInfo = optimizerInfo == null ? OptimizerUtility
				.getDefaultOptimizerInfo() : optimizerInfo;
		importedTypes = new HashMap<Schema, Map<String, AttributedElementClass<?, ?>>>();
		initializeQueryGraph();
	}

	public GreqlQueryImpl(String queryText, boolean optimize,
			OptimizerInfo optimizerInfo, Optimizer optimizer) {
		this.queryText = queryText;
		this.optimize = optimize;
		this.optimizerInfo = optimizerInfo == null ? OptimizerUtility
				.getDefaultOptimizerInfo() : optimizerInfo;
		importedTypes = new HashMap<Schema, Map<String, AttributedElementClass<?, ?>>>();
		this.optimizer = optimizer == null ? new DefaultOptimizer() : optimizer;
		initializeQueryGraph();
	}

	@Override
	public GreqlGraph getQueryGraph() {
		return queryGraph;
	}

	@SuppressWarnings("unchecked")
	public synchronized <V extends GreqlVertex> VertexEvaluator<V> getVertexEvaluator(
			V vertex) {
		return (VertexEvaluator<V>) vertexEvaluators.get(vertex);
	}

	private void initializeQueryGraph() {
		if (queryGraph == null) {
			long t0 = System.currentTimeMillis();
			queryGraph = GreqlParserWithVertexEvaluatorUpdates.parse(queryText,
					this, new HashSet<String>());
			if (queryGraph.getVCount() == 0) {
				// an empty query was parsed
				GreqlExpression gexpr = queryGraph.createGreqlExpression();
				UndefinedLiteral undefined = queryGraph
						.createUndefinedLiteral();
				gexpr.add_queryExpr(undefined);
			}
			long t1 = System.currentTimeMillis();
			logger.fine("GReQL parser: " + (t1 - t0) + " ms");
			if (optimize) {
				if (DEBUG_OPTIMIZATION) {
					String dirName = System.getProperty("java.io.tmpdir");
					if (!dirName.endsWith(File.separator)) {
						dirName += File.separator;
					}
					try {
						queryGraph.save(dirName + "greql-query-unoptimized.tg");
					} catch (GraphIOException e) {
						e.printStackTrace();
					}
					printGraphAsDot(queryGraph, dirName
							+ "greql-query-unoptimized.dot");
				}
				long t2 = System.currentTimeMillis();
				(optimizer == null ? new DefaultOptimizer() : optimizer)
						.optimize(this);
				long t3 = System.currentTimeMillis();
				if (DEBUG_OPTIMIZATION) {
					String dirName = System.getProperty("java.io.tmpdir");
					if (!dirName.endsWith(File.separator)) {
						dirName += File.separator;
					}
					try {
						queryGraph.save(dirName + "greql-query-optimized.tg");
					} catch (GraphIOException e) {
						e.printStackTrace();
					}
					printGraphAsDot(queryGraph, dirName
							+ "greql-query-optimized.dot");
					logger.info("Stored query graphs to " + dirName
							+ "greql-query*");
				}
				logger.fine("GReQL optimizer: " + (t3 - t2) + " ms");
			}
			((GraphImpl) queryGraph).defragment();
			rootExpression = queryGraph.getFirstGreqlExpression();
			initializeVertexEvaluatorsMarker(queryGraph);
			long t4 = System.currentTimeMillis();
			logger.fine("GReQL total: " + (t4 - t0) + " ms");
		}
	}

	/*
	 * Helper methods to print query graph as DOT file. To avoid compile-time
	 * dependencies, these methods use reflection to get a Tg2Dot instance.
	 */
	private static SoftReference<Object> tg2DotReference;
	private static Class<?> tg2DotClass;

	private void printGraphAsDot(Graph graph, String outputFilename) {
		Object t2d = null;
		if (tg2DotReference != null) {
			t2d = tg2DotReference.get();
		}
		try {
			if (t2d == null) {
				if (tg2DotClass == null) {
					tg2DotClass = Class
							.forName("de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot");
				}
				t2d = tg2DotClass.newInstance();
				tg2DotReference = new SoftReference<Object>(t2d);
			}
			tg2DotClass.getMethod("setGraph", Graph.class).invoke(t2d, graph);
			tg2DotClass.getMethod("setOutputFile", String.class).invoke(t2d,
					outputFilename);
			tg2DotClass.getMethod("setReversedEdges", boolean.class).invoke(
					t2d, true);
		} catch (Exception e) {
			// intentional catch-all block
			e.printStackTrace();
		}
	}

	private void initializeVertexEvaluatorsMarker(GreqlGraph graph) {
		if (vertexEvaluators == null) {
			vertexEvaluators = new GraphMarker<VertexEvaluator<?>>(graph);
		}
	}

	/**
	 * clears the tempresults that are stored in the GreqlEvaluators-Objects at
	 * the syntaxgraph nodes
	 * 
	 * @param optimizer
	 */
	void resetVertexEvaluators(InternalGreqlEvaluator evaluator) {
		GreqlGraph queryGraph = getQueryGraph();
		GreqlVertex currentVertex = (GreqlVertex) queryGraph.getFirstVertex();
		while (currentVertex != null) {
			VertexEvaluator<?> vertexEval = vertexEvaluators
					.getMark(currentVertex);
			if (vertexEval != null) {
				vertexEval.resetToInitialState(evaluator);
			}
			currentVertex = (GreqlVertex) currentVertex.getNextVertex();
		}
	}

	@Override
	public Set<String> getUsedVariables() {
		if (usedVariables == null) {
			usedVariables = JGraLab.set();
			GreqlExpression expr = getRootExpression();
			if (expr != null) {
				for (Variable v : expr.get_boundVar()) {
					usedVariables = usedVariables.plus(v.get_name());
				}
			}
		}
		return usedVariables;
	}

	@Override
	public Set<String> getStoredVariables() {
		if (storedVariables == null) {
			storedVariables = JGraLab.set();
			GreqlExpression expr = getRootExpression();
			if (expr != null) {
				Identifier id = expr.get_identifier();
				if (id != null) {
					storedVariables = storedVariables.plus(id.get_name());
				}
			}
		}
		return storedVariables;
	}

	@Override
	public String getQueryText() {
		return queryText;
	}

	@Override
	public GreqlExpression getRootExpression() {
		getQueryGraph();
		return rootExpression;
	}

	/**
	 * @param typeSimpleName
	 *            {@link String} the simple name of the needed
	 *            {@link AttributedElementClass}
	 * @return {@link AttributedElementClass} of the datagraph with the name
	 *         <code>name</code>
	 */
	public synchronized AttributedElementClass<?, ?> getImportedType(
			Schema schema, String typeSimpleName) {
		Map<String, AttributedElementClass<?, ?>> map = importedTypes
				.get(schema);
		return map != null ? map.get(typeSimpleName) : null;
	}

	/**
	 * @param elem
	 *            {@link AttributedElementClass} which will be added to the
	 *            {@link #importedTypes} with its simple name as key.
	 * @return @see {@link Map#put(Object, Object)}
	 */
	public synchronized AttributedElementClass<?, ?> addImportedType(
			Schema schema, AttributedElementClass<?, ?> elem) {
		Map<String, AttributedElementClass<?, ?>> map = importedTypes
				.get(schema);
		if (map == null) {
			map = new HashMap<String, AttributedElementClass<?, ?>>();
			importedTypes.put(schema, map);
		}
		return map.put(elem.getSimpleName(), elem);
	}

	@Override
	public void vertexAdded(Vertex v) {
		try {
			vertexEvaluators.mark(v, VertexEvaluator.createVertexEvaluator(
					(GreqlVertex) v, this));
		} catch (RuntimeException e) {
			if (!(e.getCause() instanceof ClassNotFoundException)) {
				// Some vertices of the query graph do not have an Evaluator
				// e.g. Definition
				throw e;
			}
		}
	}

	@Override
	public void vertexDeleted(Vertex v) {
		vertexEvaluators.removeMark(v);
	}

	@Override
	public void edgeAdded(Edge e) {
	}

	@Override
	public void edgeDeleted(Edge e) {
	}

	@Override
	public void maxEdgeCountIncreased(int newValue) {
	}

	@Override
	public void maxVertexCountIncreased(int newValue) {
	}

	public OptimizerInfo getOptimizerInfo() {
		return optimizerInfo;
	}

	private static class GreqlParserWithVertexEvaluatorUpdates extends
			GreqlParser {

		public GreqlParserWithVertexEvaluatorUpdates(String source,
				Set<String> subQueryNames, GreqlQueryImpl gscl) {
			super(source, subQueryNames);
			if (gscl != null) {
				graph.addGraphStructureChangedListener(gscl);
				gscl.initializeVertexEvaluatorsMarker(graph);
			}
		}

		public static GreqlGraph parse(String query, GreqlQueryImpl gscl,
				Set<String> subQueryNames) {
			return parse(query, subQueryNames, gscl);
		}

		public static GreqlGraph parse(String query, Set<String> subQueryNames,
				GreqlQueryImpl gscl) {
			GreqlParser parser = new GreqlParserWithVertexEvaluatorUpdates(
					query, subQueryNames, gscl);
			parser.parse();
			return parser.getGraph();
		}

	}

	@Override
	public Object evaluate() {
		return evaluate(null, new GreqlEnvironmentAdapter(), null);
	}

	@Override
	public Object evaluate(Graph datagraph) {
		return evaluate(datagraph, new GreqlEnvironmentAdapter(), null);
	}

	@Override
	public Object evaluate(Graph datagraph, GreqlEnvironment environment) {
		return evaluate(datagraph, environment, null);
	}

	@Override
	public Object evaluate(Graph datagraph, ProgressFunction progressFunction) {
		return evaluate(datagraph, new GreqlEnvironmentAdapter(),
				progressFunction);
	}

	@Override
	public Object evaluate(Graph datagraph, GreqlEnvironment environment,
			ProgressFunction progressFunction) {
		return new GreqlEvaluatorImpl(this, datagraph, environment,
				progressFunction).getResult();
	}

	@Override
	public String toString() {
		return queryText;
	}
}
