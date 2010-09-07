/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
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
