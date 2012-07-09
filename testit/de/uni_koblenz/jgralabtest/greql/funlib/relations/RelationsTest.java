package de.uni_koblenz.jgralabtest.greql.funlib.relations;

import de.uni_koblenz.jgralabtest.greql.funlib.arithmetics.ArithmeticTest;

public class RelationsTest extends ArithmeticTest {
	public static enum Directions {
		NORDEN, SUEDEN, WESTEN, OSTEN
	}

	public static enum Temperatures {
		KALT, WARM
	}

	protected String[] objectValues = new String[] { "Hugo", "foo", "NORDEN",
			"SUEDEN", "WESTEN", "OSTEN", "", "KALT", "WARM" };
	protected Enum<?>[] enumValues = new Enum<?>[] { Directions.NORDEN,
			Directions.SUEDEN, Directions.WESTEN, Directions.OSTEN,
			Temperatures.KALT, Temperatures.WARM };
}
