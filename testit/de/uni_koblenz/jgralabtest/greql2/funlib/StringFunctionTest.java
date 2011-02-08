package de.uni_koblenz.jgralabtest.greql2.funlib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueList;
import de.uni_koblenz.jgralabtest.greql2.GenericTests;

public class StringFunctionTest extends GenericTests {

	@Test
	public void testConcat() throws Exception {
		String queryString = "\"foo\" ++ \"bar\" ++ \"baz\"";
		JValue result = evalTestQuery("Concat", queryString);
		assertEquals("foobarbaz", result.toString());
	}
}
