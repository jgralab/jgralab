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
	protected int checkArguments(JValue[] args) {
		for (int i = 0; i < signatures.length; i++) {
			if (signatures[i].length != args.length) {
				// The current arglist has another length than the given one, so
				// it cannot match.
				continue;
			}
			boolean mismatchFound = false;
			for (int j = 0; j < args.length; j++) {
				if (!args[j].canConvert(signatures[i][j])) {
					mismatchFound = true;
					break;
				}
			}
			if (mismatchFound) {
				continue;
			}
			// Ok, formal argument list number i matches the given
			// input.
			return i;
		}
		return -1;
	}

	@Override
	public String getExpectedParameters() {
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
