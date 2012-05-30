package de.uni_koblenz.jgralab.greql2.evaluator;

import java.util.Set;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Graph;

public interface Query {

	public Greql2Graph getQueryGraph();

	public Set<String> getUsedVariables();

	public Set<String> getStoredVariables();

	public String getQueryText();

	public Greql2Expression getRootExpression();

	/**
	 * @return the time needed for optimizing the query or -1 if no optimization
	 *         was done.
	 */
	public long getOptimizationTime();

	/**
	 * @return the time needed for parsing the query.
	 */
	public long getParseTime();

	public Object evaluate();

	public Object evaluate(Graph datagraph);

	public Object evaluate(Graph datagraph, GreqlEnvironment environment);

	public Object evaluate(Graph datagraph, ProgressFunction progressFunction);

	public Object evaluate(Graph datagraph, GreqlEnvironment environment,
			ProgressFunction progressFunction);

	public void setSubQuery(String name, String greqlQuery);

}
