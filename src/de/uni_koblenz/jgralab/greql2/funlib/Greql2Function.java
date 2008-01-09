/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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

package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBoolean;
import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Graph;

/**
 * This interface is implemented by every class, that implements a
 * GReQL-Function. The method <code>evaluate(...)</code> can be used to
 * evaluate the GReQL2 Function Each Function will get the following parameters:
 * <ul>
 * <li><strong>graph</strong>: A reference to the datagraph on which the
 * function will be evaluated. For a lot of functions, this reference is not
 * really needed, but to hold the interface between FunctionApplication-Vertex
 * in the GReQL syntaxgraph and the GreqlFunction as simple as possible, it will
 * be provided everytime.</li>
 * <li><strong>subgraph</strong>: A SubgraphTempAttribute, which may bound the
 * number of graphelements that are valid for the function. Again, many
 * functions don't need it, but for simplicity, it will be provided everytime as
 * first parameter.</li>
 * <li><strong>arguments</strong> This is an array of function parameters. The
 * function itself is responsible for the correct usage and the correct casting
 * to the "real" parameters.</li>
 * </ul>
 * <strong>Returns</strong> a JValue, if the result is a boolean value,
 * jvalue.JValueBoolean is used, because the function may return "unknown"
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */
public interface Greql2Function {

	/**
	 * evaluates this GReQL-Function
	 * 
	 * @param arguments
	 *            the arguments this function expects
	 * @return the result of this function as JValue
	 * @throws EvaluateException
	 *             if something went wrong
	 */
	public JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException;

	/**
	 * Calculates the estimated cost for the evaluation of this greql function
	 * 
	 * @param inElements
	 *            the number of input elements
	 * @return The estimated costs in the abstract measure-unit "interpretation
	 *         steps"
	 */
	public int getEstimatedCosts(ArrayList<Integer> inElements);

	/**
	 * Calculates the estimated selectivity of this boolean function. If this
	 * function does not return a boolean value, this method should return 1
	 * 
	 * @return the selectivity of this function, 0 < selectivity <= 1
	 */
	public double getSelectivity();

	/**
	 * Calculates the estimated result size for the given number of input
	 * elements
	 * 
	 * @param inElements
	 *            the number of input elements to calculate the result size for
	 * @return the estimated number of elements in the result
	 */
	public int getEstimatedCardinality(int inElements);

	/**
	 * @return the expected parameters for this function as string. This is
	 *         needed to create helpful outputs like "Function X(a,b) is not
	 *         applicable for the arguments (a,c)"
	 */
	public String getExpectedParameters();

	/**
	 * @return <code>true</code> if this function is a predicate, meaning if
	 *         it returns a {@link JValueBoolean}, <code>false</code>
	 *         otherwise.
	 */
	public boolean isPredicate();
}
