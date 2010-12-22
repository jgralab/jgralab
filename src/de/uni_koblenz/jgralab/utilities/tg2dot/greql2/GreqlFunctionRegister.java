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
