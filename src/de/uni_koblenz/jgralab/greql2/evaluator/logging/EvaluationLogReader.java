/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.evaluator.logging;

import java.util.List;

/**
 * An {@link EvaluationLogReader} is used to get the average input and result
 * sizes and the selectivity of the vertex types. In contrast to the
 * {@link EvaluationLogger}s, where only one logger may access a logfile, any
 * number of readers may use the same logfile.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public interface EvaluationLogReader {
	/**
	 * returns the average selectivity of the given vertex
	 * 
	 * @param name
	 *            The vertex or function name for which to get the average
	 *            selectivity
	 * @return the average selectivity of the given vertex or function name
	 */
	public double getAvgSelectivity(String name);

	/**
	 * returns the average result size of the given vertex
	 * 
	 * @param name
	 *            The vertex or function name for which to get the average
	 *            result size
	 * @return the average result size of the given vertex or function name
	 */
	public double getAvgResultSize(String name);

	/**
	 * returns an array of average size of input elements
	 * 
	 * @param name
	 *            The vertex or function name for which to get the average input
	 *            sizes
	 * @return an array of average size of input elements
	 */
	public List<Double> getAvgInputSize(String name);

	/**
	 * loads the log from the default filename of this logger
	 * 
	 * @return true if load was successfull, false otherwise
	 */
	public boolean load();
}
