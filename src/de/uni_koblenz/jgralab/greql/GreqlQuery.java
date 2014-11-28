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
package de.uni_koblenz.jgralab.greql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import org.pcollections.PCollection;
import org.pcollections.PMap;
import org.pcollections.POrderedSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlEnvironmentAdapter;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.optimizer.DefaultOptimizer;
import de.uni_koblenz.jgralab.greql.optimizer.DefaultOptimizerInfo;
import de.uni_koblenz.jgralab.greql.optimizer.Optimizer;
import de.uni_koblenz.jgralab.greql.parallel.EvaluationEnvironment;
import de.uni_koblenz.jgralab.greql.parallel.ParallelGreqlEvaluatorCallable;
import de.uni_koblenz.jgralab.greql.schema.GreqlExpression;
import de.uni_koblenz.jgralab.greql.schema.GreqlGraph;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;

public abstract class GreqlQuery implements ParallelGreqlEvaluatorCallable {

	private String name;

	protected GreqlQuery() {
		// protected constructor, GreqlQueris can only be created by factory
		// methods
	}

	public static GreqlQuery readQuery(File f) throws IOException {
		return readQuery(f, new DefaultOptimizer(new DefaultOptimizerInfo()));
	}

	public static GreqlQuery readQuery(File f, Optimizer optimizer)
			throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(f));
		try {
			StringBuilder queryText = new StringBuilder();
			for (String line = reader.readLine(); line != null; line = reader
					.readLine()) {
				queryText.append(line).append('\n');
			}
			return createQuery(queryText.toString(), optimizer);
		} finally {
			try {
				reader.close();
			} catch (IOException ex) {
				throw new RuntimeException(
						"An exception occurred while closing the stream.", ex);
			}
		}
	}

	public static GreqlQuery createQuery(String queryText) {
		return new GreqlQueryImpl(queryText);
	}

	public static GreqlQuery createQuery(String queryText, Optimizer optimizer) {
		return new GreqlQueryImpl(queryText, optimizer);
	}

	/**
	 * @return the name of this GreqlQuery
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of this GreqlQuery to <code>name</code>. The name can be
	 * used to store human-readable short identifiers. It is not used anywhere
	 * in GReQL.
	 * 
	 * @param name
	 *            the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	public abstract GreqlGraph getQueryGraph();

	@Override
	public abstract Set<String> getUsedVariables();

	@Override
	public abstract Set<String> getStoredVariables();

	public abstract String getQueryText();

	public abstract GreqlExpression getRootExpression();

	public Object evaluate() {
		return evaluate(null, new GreqlEnvironmentAdapter(), null);
	}

	public Object evaluate(Graph datagraph) {
		return evaluate(datagraph, new GreqlEnvironmentAdapter(), null);
	}

	public Object evaluate(Graph datagraph, GreqlEnvironment environment) {
		return evaluate(datagraph, environment, null);
	}

	public Object evaluate(Graph datagraph, ProgressFunction progressFunction) {
		return evaluate(datagraph, new GreqlEnvironmentAdapter(),
				progressFunction);
	}

	public abstract Object evaluate(Graph datagraph,
			GreqlEnvironment environment, ProgressFunction progressFunction);

	@SuppressWarnings("unchecked")
	public <T> T getSingleResult(Graph datagraph) {
		return (T) evaluate(datagraph);
	}

	@SuppressWarnings("unchecked")
	public <T> T getSingleResult(Graph datagraph, GreqlEnvironment environment) {
		return (T) evaluate(datagraph, environment);
	}

	@SuppressWarnings("unchecked")
	public <T> PVector<T> getResultList(Graph datagraph) {
		return (PVector<T>) evaluate(datagraph);
	}

	@SuppressWarnings("unchecked")
	public <T> PVector<T> getResultList(Graph datagraph,
			GreqlEnvironment environment) {
		return (PVector<T>) evaluate(datagraph, environment);
	}

	@SuppressWarnings("unchecked")
	public <K, V> PMap<K, V> getResultMap(Graph datagraph) {
		return (PMap<K, V>) evaluate(datagraph);
	}

	@SuppressWarnings("unchecked")
	public <K, V> PMap<K, V> getResultMap(Graph datagraph,
			GreqlEnvironment environment) {
		return (PMap<K, V>) evaluate(datagraph, environment);
	}

	@SuppressWarnings("unchecked")
	public <T> POrderedSet<T> getResultSet(Graph datagraph) {
		return (POrderedSet<T>) evaluate(datagraph);
	}

	@SuppressWarnings("unchecked")
	public <T> POrderedSet<T> getResultSet(Graph datagraph,
			GreqlEnvironment environment) {
		return (POrderedSet<T>) evaluate(datagraph, environment);
	}

	/*
	 * Simple main function to evaluate GReQL queries from the command line.
	 */
	public static void main(String[] args) throws FileNotFoundException,
			IOException, GraphIOException {
		if ((args.length < 1) || (args.length > 2)) {
			System.err.println("Usage: java GreqlQuery <query> [<graphfile>]");
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

		Object result = GreqlQuery.createQuery(query).evaluate(datagraph);
		System.out.println("Evaluation Result:");
		System.out.println("==================");

		if (result instanceof Map) {
			for (Entry<?, ?> e : ((Map<?, ?>) result).entrySet()) {
				System.out.println(e.getKey() + " --> " + e.getValue());
			}
		} else if (result instanceof PCollection) {
			PCollection<?> coll = (PCollection<?>) result;
			for (Object jv : coll) {
				System.out.println(jv);
			}
		} else {
			System.out.println(result);
		}
	}

	@Override
	public Object call(EvaluationEnvironment environment) throws Exception {
		return evaluate(environment.getDatagraph(),
				environment.getGreqlEnvironment());
	}
}
