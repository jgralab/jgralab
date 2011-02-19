/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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

package de.uni_koblenz.jgralab.greql2.evaluator.logging;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This interface must be implemented by all classes which can be used for
 * evaluatin logging. An {@link EvaluationLogger} has to make sure that only one
 * writes to a specific logfile.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public interface EvaluationLogger {

	/**
	 * logs, if a vertex was evaluated to <code>true</code> or
	 * <code>false</code>
	 * 
	 * @param name
	 *            The vertex or function name which was evaluated
	 * @param wasSelected
	 *            was the vertex evaluated to true or false
	 */
	public void logSelectivity(String name, boolean wasSelected);

	/**
	 * logs the resultsize of a collection-construction (ValueConstruction,
	 * Comprehension)
	 * 
	 * @param name
	 *            The vertex or function name which was evaluated
	 * @param resultSize
	 *            The size of the resulting collection
	 */
	public void logResultSize(String name, long resultSize);

	/**
	 * logs the number of input elements for a specific vertex. Because a vertex
	 * may have more inputs (a function may have n arguments etc.) the number is
	 * stored in an array
	 * 
	 * @param name
	 *            The vertex or function name which was evaluated
	 * @param inputSize
	 *            The size of the input
	 */
	public void logInputSize(String name, ArrayList<Long> inputSize);

	/**
	 * stores the log to the default filename of this logger
	 * 
	 * @return true if log was stored successfull, false otherwise
	 */
	public boolean store() throws IOException;

	/**
	 * loads the log from the default filename of this logger
	 * 
	 * @return true if load was successfull, false otherwise
	 */
	public boolean load();

	/**
	 * @return the filename this logger will use to load and save its results
	 *         to.
	 */
	public String getLogfileName();

}
