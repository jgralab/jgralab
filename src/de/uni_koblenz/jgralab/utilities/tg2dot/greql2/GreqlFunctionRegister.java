/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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
package de.uni_koblenz.jgralab.utilities.tg2dot.greql2;

import de.uni_koblenz.jgralab.greql2.funlib.Greql2FunctionLibrary;
import de.uni_koblenz.jgralab.utilities.tg2dot.greql2.funlib.AbbreviateString;
import de.uni_koblenz.jgralab.utilities.tg2dot.greql2.funlib.AlphaIncidenceNumber;
import de.uni_koblenz.jgralab.utilities.tg2dot.greql2.funlib.AlphaRolename;
import de.uni_koblenz.jgralab.utilities.tg2dot.greql2.funlib.AttributeType;
import de.uni_koblenz.jgralab.utilities.tg2dot.greql2.funlib.FormatString;
import de.uni_koblenz.jgralab.utilities.tg2dot.greql2.funlib.Join;
import de.uni_koblenz.jgralab.utilities.tg2dot.greql2.funlib.OmegaIncidenceNumber;
import de.uni_koblenz.jgralab.utilities.tg2dot.greql2.funlib.OmegaRolename;
import de.uni_koblenz.jgralab.utilities.tg2dot.greql2.funlib.ShortenString;
import de.uni_koblenz.jgralab.utilities.tg2dot.greql2.funlib.ToDotString;

public class GreqlFunctionRegister {

	public static void registerAllKnownGreqlFunctions() {
		// Greql2FunctionLibrary.instance().registerFunctionsInDirectory(
		// "bin/de/uni_koblenz/jgralab/utilities/tg2dot/greql2/funlib");
		Greql2FunctionLibrary.instance().registerUserDefinedFunction(
				ToDotString.class);
		Greql2FunctionLibrary.instance()
				.registerUserDefinedFunction(Join.class);
		Greql2FunctionLibrary.instance().registerUserDefinedFunction(
				AlphaRolename.class);
		Greql2FunctionLibrary.instance().registerUserDefinedFunction(
				AlphaIncidenceNumber.class);
		Greql2FunctionLibrary.instance().registerUserDefinedFunction(
				OmegaRolename.class);
		Greql2FunctionLibrary.instance().registerUserDefinedFunction(
				OmegaIncidenceNumber.class);
		Greql2FunctionLibrary.instance().registerUserDefinedFunction(
				FormatString.class);

		Greql2FunctionLibrary.instance().registerUserDefinedFunction(
				AbbreviateString.class);
		Greql2FunctionLibrary.instance().registerUserDefinedFunction(
				AttributeType.class);
		Greql2FunctionLibrary.instance().registerUserDefinedFunction(
				ShortenString.class);
	}
}
