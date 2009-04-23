/**
 *
 */
package de.uni_koblenz.jgralab.greql2.funlib;

import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueType;

/**
 * @author Tassilo Horn <horn@uni-koblenz.de>
 *
 */
public abstract class AbstractGreql2Function implements Greql2Function {

	/**
	 * Represents a list of allowed signatures for this {@link Greql2Function}.
	 */
	protected JValueType[][] signatures;

	/**
	 * @param args
	 *            the actual parameters given to that function
	 * @return the index in <code>signatures</code> that matches
	 *         <code>args</code>
	 */
	protected final int checkArguments(JValue[] args) {
		int[] indexAndCosts = { -1, Integer.MAX_VALUE };
		for (int i = 0; i < signatures.length; i++) {
			if (signatures[i].length != args.length) {
				// The current arglist has another length than the given one, so
				// it cannot match.
				continue;
			}
			int conversionCosts = 0;
			for (int j = 0; j < signatures[i].length; j++) {
				int thisArgsCosts = args[j].conversionCosts(signatures[i][j]);
				if (thisArgsCosts == -1) {
					// conversion is not possible
					conversionCosts = Integer.MAX_VALUE;
					break;
				}
				conversionCosts += thisArgsCosts;
			}
			if (conversionCosts == 0) {
				// this signature was a perfect match!
				return i;
			} else if (conversionCosts > 0
					&& conversionCosts < indexAndCosts[1]) {
				// this signature can at least be converted and is the best till
				// now
				indexAndCosts[0] = i;
				indexAndCosts[1] = conversionCosts;
			}
		}
		return indexAndCosts[0];
	}

	protected final void printArguments(JValue[] args) {
		for (int i = 0; i < args.length; i++) {
			System.out.println("  args[" + i + "] = " + args[i]);
		}
	}

	@Override
	public final String getExpectedParameters() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < signatures.length; i++) {
			sb.append("(");
			for (int j = 0; j < signatures[i].length; j++) {
				sb.append(signatures[i][j]);
				if (j != signatures[i].length - 1) {
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
}
