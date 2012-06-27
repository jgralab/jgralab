package de.uni_koblenz.jgralab.greql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.greql.evaluator.QueryImpl;
import de.uni_koblenz.jgralab.greql.optimizer.Optimizer;
import de.uni_koblenz.jgralab.greql.optimizer.OptimizerUtility;
import de.uni_koblenz.jgralab.greql.schema.Greql2Expression;
import de.uni_koblenz.jgralab.greql.schema.Greql2Graph;

public abstract class Query {
	public static Query readQuery(File f) throws IOException {
		return readQuery(f, true);
	}

	public static Query readQuery(File f, boolean optimize) throws IOException {
		return readQuery(f, optimize,
				OptimizerUtility.getDefaultOptimizerInfo());
	}

	public static Query readQuery(File f, boolean optimize,
			OptimizerInfo optimizerInfo) throws IOException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));

			String line = null;
			StringBuffer sb = new StringBuffer();
			while ((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
			return createQuery(sb.toString(), optimize, optimizerInfo);
		} finally {
			try {
				reader.close();
			} catch (IOException ex) {
				throw new RuntimeException(
						"An exception occurred while closing the stream.", ex);
			}
		}
	}

	public static Query createQuery(String queryText) {
		return new QueryImpl(queryText);
	}

	public static Query createQuery(String queryText, boolean optimize) {
		return new QueryImpl(queryText, optimize);
	}

	public static Query createQuery(String queryText,
			OptimizerInfo optimizerInfo) {
		return new QueryImpl(queryText, optimizerInfo);
	}

	public static Query createQuery(String queryText, Optimizer optimizer) {
		return new QueryImpl(queryText, optimizer);
	}

	public static Query createQuery(String queryText, boolean optimize,
			OptimizerInfo optimizerInfo) {
		return new QueryImpl(queryText, optimize, optimizerInfo);
	}

	public static Query createQuery(String queryText, boolean optimize,
			OptimizerInfo optimizerInfo, Optimizer optimizer) {
		return new QueryImpl(queryText, optimize, optimizerInfo, optimizer);
	}

	public abstract Greql2Graph getQueryGraph();

	public abstract Set<String> getUsedVariables();

	public abstract Set<String> getStoredVariables();

	public abstract String getQueryText();

	public abstract Greql2Expression getRootExpression();

	/**
	 * @return the time needed for optimizing the query or -1 if no optimization
	 *         was done.
	 */
	public abstract long getOptimizationTime();

	/**
	 * @return the time needed for parsing the query.
	 */
	public abstract long getParseTime();

	public abstract Object evaluate();

	public abstract Object evaluate(Graph datagraph);

	public abstract Object evaluate(Graph datagraph,
			GreqlEnvironment environment);

	public abstract Object evaluate(Graph datagraph,
			ProgressFunction progressFunction);

	public abstract Object evaluate(Graph datagraph,
			GreqlEnvironment environment, ProgressFunction progressFunction);

	public abstract void setUseSavedOptimizedSyntaxGraph(
			boolean useSavedOptimizedSyntaxGraph);

}
