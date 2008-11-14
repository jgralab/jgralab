/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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

import java.util.List;

import de.uni_koblenz.jgralab.greql2.funlib.Greql2Function;
import de.uni_koblenz.jgralab.greql2.funlib.Greql2FunctionLibrary;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.schema.SourcePosition;

/**
 * Should be thrown if a function is called with the wrong parameter count or
 * type
 *
 * @author ist@uni-koblenz.de
 *
 */
public class WrongFunctionParameterException extends QuerySourceException {

	static final long serialVersionUID = -1234561;

	private static String parametersToString(JValue[] args) {
		String argString = "(";
		for (int i = 0; i < args.length; i++) {
			JValue o = args[i];
			if (o != null) {
				if (i > 0) {
					argString += ",";
				}
				argString += o.getType().toString();
			}
		}
		argString += ")";
		return argString;
	}

	public WrongFunctionParameterException(Greql2Function function,
			List<SourcePosition> sourcePositions, JValue[] wrongArguments) {
		super("Function "
				+ Greql2FunctionLibrary.instance().toFunctionName(
						function.getClass().getCanonicalName())
				+ function.getExpectedParameters()
				+ " is not applicable for the arguments "
				+ parametersToString(wrongArguments), Greql2FunctionLibrary
				.instance().toFunctionName(
						function.getClass().getCanonicalName()),
				sourcePositions);
	}

	public WrongFunctionParameterException(Greql2Function function,
			List<SourcePosition> sourcePositions, JValue[] wrongArguments,
			Exception cause) {
		super("Function "
				+ Greql2FunctionLibrary.instance().toFunctionName(
						function.getClass().getCanonicalName())
				+ function.getExpectedParameters()
				+ " is not applicable for the arguments "
				+ parametersToString(wrongArguments), Greql2FunctionLibrary
				.instance().toFunctionName(
						function.getClass().getCanonicalName()),
				sourcePositions, cause);
	}

}
