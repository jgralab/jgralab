package de.uni_koblenz.jgralabtest.greql.funlib.strings;

import java.io.File;

public abstract class StringsTest {
	protected String[] stringValues = new String[] { "Hugo", "foo", "bar", "",
			"Hello World", "b", "H", "o", "oo", "ar", "r", "Hello", "World" };
	protected Object[] objectValues = new Object[] { new Integer(2),
			new Double(2.5), new File("."), System.class, this };
}
