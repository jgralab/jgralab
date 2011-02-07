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

	@Test
	public void testConcat2() throws Exception {
		String queryString = "list(1..3) ++ list(4..6)";
		JValue result = evalTestQuery("Concat", queryString);
		JValueList l = new JValueList();
		l.add(new JValueImpl(1));
		l.add(new JValueImpl(2));
		l.add(new JValueImpl(3));
		l.add(new JValueImpl(4));
		l.add(new JValueImpl(5));
		l.add(new JValueImpl(6));
		assertEquals(l, result);
	}

}
