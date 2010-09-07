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

package de.uni_koblenz.jgralab.greql2.exception;

import de.uni_koblenz.jgralab.greql2.funlib.Greql2Function;
import de.uni_koblenz.jgralab.greql2.funlib.Greql2FunctionLibrary;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;

/**
 * Should be thrown if a function is called with the wrong parameter count or
 * type
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class WrongFunctionParameterException extends EvaluateException {

	static final long serialVersionUID = -1234561;

	private static String parametersToString(JValue[] args) {
		StringBuffer sb = new StringBuffer("(");
		for (int i = 0; i < args.length; i++) {
			JValue o = args[i];
			if (o != null) {
				if (i > 0) {
					sb.append(", ");
				}
				sb.append(elide(o.toString()));
				sb.append(" : ");
				sb.append(o.getType().toString());
			}
		}
		sb.append(")");
		return sb.toString();
	}

	private static String elide(String s) {
		if (s.length() > 50) {
			return s.substring(0, 50);
		}
		return s;
	}

	public WrongFunctionParameterException(Greql2Function function,
			JValue[] wrongArguments) {
		super("Function "
				+ Greql2FunctionLibrary.instance().toFunctionName(
						function.getClass().getCanonicalName())
				+ function.getExpectedParameters()
				+ " is not applicable for the arguments "
				+ parametersToString(wrongArguments));
	}

	public WrongFunctionParameterException(Greql2Function function,
			JValue[] wrongArguments, Exception cause) {
		super("Function "
				+ Greql2FunctionLibrary.instance().toFunctionName(
						function.getClass().getCanonicalName())
				+ function.getExpectedParameters()
				+ " is not applicable for the arguments "
				+ parametersToString(wrongArguments), cause);
	}
}
