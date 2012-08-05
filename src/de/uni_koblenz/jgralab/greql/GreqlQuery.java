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
package de.uni_koblenz.jgralab.greql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlEnvironmentAdapter;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.optimizer.Optimizer;
import de.uni_koblenz.jgralab.greql.optimizer.OptimizerUtility;
import de.uni_koblenz.jgralab.greql.schema.GreqlExpression;
import de.uni_koblenz.jgralab.greql.schema.GreqlGraph;

public abstract class GreqlQuery {
	public static GreqlQuery readQuery(File f) throws IOException {
		return readQuery(f, true);
	}

	public static GreqlQuery readQuery(File f, boolean optimize)
			throws IOException {
		return readQuery(f, optimize,
				OptimizerUtility.getDefaultOptimizerInfo());
	}

	public static GreqlQuery readQuery(File f, boolean optimize,
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

	public static GreqlQuery createQuery(String queryText) {
		return new GreqlQueryImpl(queryText);
	}

	public static GreqlQuery createQuery(String queryText, boolean optimize) {
		return new GreqlQueryImpl(queryText, optimize);
	}

	public static GreqlQuery createQuery(String queryText,
			OptimizerInfo optimizerInfo) {
		return new GreqlQueryImpl(queryText, optimizerInfo);
	}

	public static GreqlQuery createQuery(String queryText, Optimizer optimizer) {
		return new GreqlQueryImpl(queryText, optimizer);
	}

	public static GreqlQuery createQuery(String queryText, boolean optimize,
			OptimizerInfo optimizerInfo) {
		return new GreqlQueryImpl(queryText, optimize, optimizerInfo);
	}

	public static GreqlQuery createQuery(String queryText, boolean optimize,
			OptimizerInfo optimizerInfo, Optimizer optimizer) {
		return new GreqlQueryImpl(queryText, optimize, optimizerInfo, optimizer);
	}

	public GreqlGraph getQueryGraph() {
		return null;
	}

	public abstract Set<String> getUsedVariables();

	public abstract Set<String> getStoredVariables();

	public String getQueryText() {
		return null;
	}

	public GreqlExpression getRootExpression() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return the time needed for optimizing the query or -1 if no optimization
	 *         was done.
	 */
	public long getOptimizationTime() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return the time needed for parsing the query.
	 */
	public long getParseTime() {
		throw new UnsupportedOperationException();
	}

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

}
