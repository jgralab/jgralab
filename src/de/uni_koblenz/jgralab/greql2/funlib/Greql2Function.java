/**
 *
 */
package de.uni_koblenz.jgralab.greql2.funlib;

import java.util.ArrayList;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * This abstract class is extended by every class, that implements a
 * GReQL-Function. The method <code>evaluate(...)</code> can be used to evaluate
 * the GReQL2 Function Each Function will get the following parameters:
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
 * @author ist@uni-koblenz.de
 * 
 */
public abstract class Greql2Function {

	public enum Category {
		DEBUGGING, LOGICS, COMPARISONS, ARITHMETICS, STRINGS, COLLECTIONS_AND_MAPS, PATHS_AND_PATHSYSTEMS_AND_SLICES, SCHEMA_ACCESS, GRAPH, UNDEFINED
	};

	/**
	 * The description string of this function. The first line should be a brief
	 * description, additional details should be given in following lines.
	 */
	protected String description = "TODO: No description set for this function.";

	/**
	 * Represents a list of allowed signatures for this {@link Greql2Function}.
	 * Each signature is an array of JValueTypes. The first describe the input
	 * paramereters, the last is the return type.
	 */
	protected JValueType[][] signatures;

	/**
	 * The categories this function belongs to.
	 */
	protected Category[] categories = { Category.UNDEFINED };

	/**
	 * @param args
	 *            the actual parameters given to that function
	 * @return the index in <code>signatures</code> that matches
	 *         <code>args</code>, or a negative value, if no signature matches.
	 */
	protected final int checkArguments(JValue[] args) {
		int bestIndex = -1;
		int bestIndexCosts = Integer.MAX_VALUE;

		out: for (int i = 0; i < signatures.length; i++) {
			if (signatures[i].length - 1 != args.length) {
				// The current arglist has another length than the given one, so
				// it cannot match.
				continue;
			}
			int conversionCosts = 0;
			for (int j = 0; j < args.length; j++) {
				int thisArgsCosts = args[j].conversionCosts(signatures[i][j]);
				if (thisArgsCosts == -1) {
					// conversion is not possible
					conversionCosts = Integer.MAX_VALUE;
					continue out;
				}
				conversionCosts += thisArgsCosts;
			}
			if (conversionCosts == 0) {
				// this signature was a perfect match!
				return i;
			} else if (conversionCosts < bestIndexCosts) {
				// this signature can at least be converted and is the best till
				// now
				bestIndex = i;
				bestIndexCosts = conversionCosts;
			}
		}
		return bestIndex;
	}

	protected final void printArguments(JValue[] args) {
		for (int i = 0; i < args.length; i++) {
			System.out.println("  args[" + i + "] = " + args[i]);
		}
	}

	public final String getExpectedParameters() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < signatures.length; i++) {
			sb.append("(");
			for (int j = 0; j < signatures[i].length - 1; j++) {
				sb.append(signatures[i][j]);
				if (j != signatures[i].length - 2) {
					sb.append(", ");
				}
			}
			sb.append(")");
			if (i < signatures.length - 1) {
				sb.append(" or ");
			}
		}
		return sb.toString();
	}

	/**
	 * evaluates this GReQL-Function
	 * 
	 * @param arguments
	 *            the arguments this function expects
	 * @return the result of this function as JValue
	 * @throws EvaluateException
	 *             if something went wrong
	 */
	public abstract JValue evaluate(Graph graph, BooleanGraphMarker subgraph,
			JValue[] arguments) throws EvaluateException;

	/**
	 * Calculates the estimated cost for the evaluation of this greql function
	 * 
	 * @param inElements
	 *            the number of input elements
	 * @return The estimated costs in the abstract measure-unit "interpretation
	 *         steps"
	 */
	public abstract long getEstimatedCosts(ArrayList<Long> inElements);

	/**
	 * Calculates the estimated selectivity of this boolean function. If this
	 * function does not return a boolean value, this method should return 1
	 * 
	 * @return the selectivity of this function, 0 < selectivity <= 1
	 */
	public abstract double getSelectivity();

	/**
	 * Calculates the estimated result size for the given number of input
	 * elements
	 * 
	 * @param inElements
	 *            the number of input elements to calculate the result size for
	 * @return the estimated number of elements in the result
	 */
	public abstract long getEstimatedCardinality(int inElements);
}
