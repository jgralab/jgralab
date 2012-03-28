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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.pcollections.PSet;

import de.uni_koblenz.jgralab.GraphStructureChangedAdapter;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.parser.GreqlParser;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Graph;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.Identifier;
import de.uni_koblenz.jgralab.greql2.schema.Variable;
import de.uni_koblenz.jgralab.impl.GraphBaseImpl;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class QueryImpl extends GraphStructureChangedAdapter implements Query {
	private final String queryText;
	private Greql2Graph queryGraph;
	private PSet<String> usedVariables;
	private PSet<String> storedVariables;
	private final boolean optimize;
	private final long optimizationTime = -1;
	private long parseTime = -1;
	private Greql2Expression rootExpression;

	/**
	 * The {@link Map} of SimpleName to Type of types that is known in the
	 * evaluator by import statements in the greql query
	 */
	protected Map<String, AttributedElementClass<?, ?>> knownTypes = new HashMap<String, AttributedElementClass<?, ?>>();

	/**
	 * The {@link GraphMarker} that stores all vertex evaluators
	 */
	private GraphMarker<VertexEvaluator<? extends Greql2Vertex>> vertexEvaluators;

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
			return new QueryImpl(sb.toString(), optimize);
		} finally {
			try {
				reader.close();
			} catch (IOException ex) {
				throw new RuntimeException(
						"An exception occurred while closing the stream.", ex);
			}
		}
	}

	public QueryImpl(String queryText) {
		this(queryText, true);
	}

	public QueryImpl(String queryText, boolean optimize) {
		this.queryText = queryText;
		this.optimize = optimize;
		knownTypes = new HashMap<String, AttributedElementClass<?, ?>>();
	}

	@Override
	public Greql2Graph getQueryGraph() {
		initializeQueryGraph();
		return queryGraph;
	}

	@SuppressWarnings("unchecked")
	public synchronized <V extends Greql2Vertex> VertexEvaluator<V> getVertexEvaluator(
			V vertex) {
		initializeQueryGraph();
		return (VertexEvaluator<V>) vertexEvaluators.get(vertex);
	}

	private void initializeQueryGraph() {
		if (queryGraph == null) {
			QueryGraphCacheEntry e = queryGraphCache.get(queryText, optimize);
			if (e != null) {
				queryGraph = e.graph;
				vertexEvaluators = e.eval;
				rootExpression = queryGraph.getFirstGreql2Expression();
			}
		}
		if (queryGraph == null) {
			long t0 = System.currentTimeMillis();
			queryGraph = GreqlParserWithVertexEvaluatorUpdates.parse(queryText,
					this);
			long t1 = System.currentTimeMillis();
			parseTime = t1 - t0;
			// TODO [greqlevaluator] reenable optimize
			// if (optimize) {
			// DefaultOptimizer.optimizeQuery(queryGraph);
			// optimizationTime = System.currentTimeMillis() - t1;
			// }
			((GraphBaseImpl) queryGraph).defragment();
			rootExpression = queryGraph.getFirstGreql2Expression();
			initializeVertexEvaluatorsMarker(queryGraph);
			queryGraphCache.put(queryText, optimize, queryGraph,
					vertexEvaluators);
		}
	}

	private void initializeVertexEvaluatorsMarker(Greql2Graph graph) {
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
		Greql2Graph queryGraph = getQueryGraph();
		Greql2Vertex currentVertex = (Greql2Vertex) queryGraph.getFirstVertex();
		while (currentVertex != null) {
			VertexEvaluator<?> vertexEval = vertexEvaluators
					.getMark(currentVertex);
			if (vertexEval != null) {
				vertexEval.resetToInitialState(evaluator);
			}
			currentVertex = (Greql2Vertex) currentVertex.getNextVertex();
		}
	}

	@Override
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

	@Override
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

	@Override
	public String getQueryText() {
		return queryText;
	}

	@Override
	public Greql2Expression getRootExpression() {
		getQueryGraph();
		return rootExpression;
	}

	/**
	 * @return the time needed for optimizing the query or -1 if no optimization
	 *         was done.
	 */
	@Override
	public long getOptimizationTime() {
		return optimizationTime;
	}

	/**
	 * @return the time needed for parsing the query.
	 */
	@Override
	public long getParseTime() {
		return parseTime;
	}

	/**
	 * @param typeSimpleName
	 *            {@link String} the simple name of the needed
	 *            {@link AttributedElementClass}
	 * @return {@link AttributedElementClass} of the datagraph with the name
	 *         <code>name</code>
	 */
	public synchronized AttributedElementClass<?, ?> getKnownType(
			String typeSimpleName) {
		return knownTypes.get(typeSimpleName);
	}

	/**
	 * @param elem
	 *            {@link AttributedElementClass} which will be added to the
	 *            {@link #knownTypes} with its simple name as key.
	 * @return @see {@link Map#put(Object, Object)}
	 */
	public synchronized AttributedElementClass<?, ?> addKnownType(
			AttributedElementClass<?, ?> elem) {
		return knownTypes.put(elem.getSimpleName(), elem);
	}

	@Override
	public void vertexAdded(Vertex v) {
		vertexEvaluators.mark(v,
				VertexEvaluator.createVertexEvaluator((Greql2Vertex) v, this));
	}

	@Override
	public void vertexDeleted(Vertex v) {
		vertexEvaluators.removeMark(v);
	}

	private static class GreqlParserWithVertexEvaluatorUpdates extends
			GreqlParser {

		public GreqlParserWithVertexEvaluatorUpdates(String source,
				Set<String> subQueryNames, QueryImpl gscl) {
			super(source, subQueryNames);
			if (gscl != null) {
				graph.addGraphStructureChangedListener(gscl);
				gscl.initializeVertexEvaluatorsMarker(graph);
			}
		}

		public static Greql2Graph parse(String query, QueryImpl gscl) {
			return parse(query, null, gscl);
		}

		public static Greql2Graph parse(String query,
				Set<String> subQueryNames, QueryImpl gscl) {
			GreqlParser parser = new GreqlParserWithVertexEvaluatorUpdates(
					query, subQueryNames, gscl);
			parser.parse();
			return parser.getGraph();
		}

	}

}
