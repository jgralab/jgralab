package de.uni_koblenz.jgralab.greql2.evaluator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Set;

import org.pcollections.PSet;

import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
// TODO [greqlevaluator] readd import
// import de.uni_koblenz.jgralab.greql2.optimizer.DefaultOptimizer;
import de.uni_koblenz.jgralab.greql2.parser.GreqlParser;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Graph;
import de.uni_koblenz.jgralab.greql2.schema.Identifier;
import de.uni_koblenz.jgralab.greql2.schema.Variable;

public class Query {
	private final String queryText;
	private Greql2Graph queryGraph;
	private PSet<String> usedVariables;
	private PSet<String> storedVariables;
	private boolean optimize;
	private long optimizationTime = -1;
	private long parseTime = -1;
	private Greql2Expression rootExpression;

	/**
	 * The GraphMarker that stores all vertex evaluators
	 */
	private GraphMarker<VertexEvaluator<?>> vertexEvaluators;

	private static class QueryGraphCacheEntry {
		Greql2Graph graph;
		GraphMarker<VertexEvaluator<?>> eval;

		QueryGraphCacheEntry(Greql2Graph g, GraphMarker<VertexEvaluator<?>> e) {
			graph = g;
			eval = e;
		}
	}

	private static class QueryGraphCache {
		HashMap<String, SoftReference<QueryGraphCacheEntry>> cache = new HashMap<String, SoftReference<QueryGraphCacheEntry>>();

		QueryGraphCacheEntry get(String queryText, boolean optimize) {
			String key = optimize + "#" + queryText;
			SoftReference<QueryGraphCacheEntry> ref = cache.get(key);
			if (ref != null) {
				QueryGraphCacheEntry e = ref.get();
				if (e == null) {
					cache.remove(key);
				}
				return e;
			}
			return null;
		}

		void put(String queryText, boolean optimize, Greql2Graph queryGraph,
				GraphMarker<VertexEvaluator<?>> evaluators) {
			String key = optimize + "#" + queryText;
			cache.put(key, new SoftReference<QueryGraphCacheEntry>(
					new QueryGraphCacheEntry(queryGraph, evaluators)));
		}
	}

	private static final QueryGraphCache queryGraphCache = new QueryGraphCache();

	public static Query readQuery(File f) throws IOException {
		return readQuery(f, true);
	}

	public static Query readQuery(File f, boolean optimize) throws IOException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));

			String line = null;
			StringBuffer sb = new StringBuffer();
			while ((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
			return new Query(sb.toString(), optimize);
		} finally {
			try {
				reader.close();
			} catch (IOException ex) {
				throw new RuntimeException(
						"An exception occurred while closing the stream.", ex);
			}
		}
	}

	public Query(String queryText) {
		this(queryText, true);
	}

	public Query(String queryText, boolean optimize) {
		this.queryText = queryText;
		this.optimize = optimize;
	}

	public Greql2Graph getQueryGraph() {
		if (queryGraph == null) {
			QueryGraphCacheEntry e = queryGraphCache.get(queryText, optimize);
			if (e != null) {
				queryGraph = e.graph;
				vertexEvaluators = e.eval;
			}
		}
		if (queryGraph == null) {
			long t0 = System.currentTimeMillis();
			queryGraph = GreqlParser.parse(queryText);
			long t1 = System.currentTimeMillis();
			parseTime = t1 - t0;
			// TODO [greqlevaluator] reenable optimize
			// if (optimize) {
			// DefaultOptimizer.optimizeQuery(queryGraph);
			// optimizationTime = System.currentTimeMillis() - t1;
			// }
			rootExpression = queryGraph.getFirstGreql2Expression();
			vertexEvaluators = new GraphMarker<VertexEvaluator<?>>(queryGraph);
			queryGraphCache.put(queryText, optimize, queryGraph,
					vertexEvaluators);
		}
		return queryGraph;
	}

	public Set<String> getUsedVariables() {
		if (usedVariables == null) {
			usedVariables = JGraLab.set();
			Greql2Expression expr = getRootExpression();
			if (expr != null) {
				for (Variable v : expr.get_boundVar()) {
					usedVariables = usedVariables.plus(v.get_name());
				}
			}
		}
		return usedVariables;
	}

	public Set<String> getStoredVariables() {
		if (storedVariables == null) {
			storedVariables = JGraLab.set();
			Greql2Expression expr = getRootExpression();
			if (expr != null) {
				Identifier id = expr.get_identifier();
				if (id != null) {
					storedVariables = storedVariables.plus(id.get_name());
				}
			}
		}
		return storedVariables;
	}

	public String getQueryText() {
		return queryText;
	}

	public Greql2Expression getRootExpression() {
		getQueryGraph();
		return rootExpression;
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

}
