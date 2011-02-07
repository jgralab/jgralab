package de.uni_koblenz.jgralabtest.greql2.funlib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBoolean;
import de.uni_koblenz.jgralabtest.greql2.GenericTests;

public class SchemaFunctionTest extends GenericTests {

	@Test
	public void testHasAttribute() throws Exception {
		String queryString = "from x : V{Variable} report hasAttribute(x, \"name\") end";
		JValue result = evalTestQuery("HasAttribute", queryString);
		for (JValue v : result.toCollection()) {
			assertEquals(JValueBoolean.getTrueValue(), v.toBoolean());
		}
	}

	@Test
	public void testHasAttribute2() throws Exception {
		String queryString = "from x : V{Variable} report hasAttribute(type(x), \"name\") end";
		JValue result = evalTestQuery("HasAttribute2", queryString);
		for (JValue v : result.toCollection()) {
			assertEquals(JValueBoolean.getTrueValue(), v.toBoolean());
		}
	}

	@Test
	public void testHasType() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from x : V{WhereExpression} report hasType(x, \"WhereExpression\") end";
		JValue result = evalTestQuery("HasType", queryString);
		assertEquals(1, result.toCollection().size());
		assertEquals(true, (boolean) getNthValue(result.toCollection(), 0)
				.toBoolean());
	}

	@Test
	public void testHasType2() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from x : V{WhereExpression} report hasType(x, \"Variable\") end";
		JValue result = evalTestQuery("HasType2", queryString);
		assertEquals(1, result.toCollection().size());
		assertEquals(false, (boolean) getNthValue(result.toCollection(), 0)
				.toBoolean());
	}

	@Test
	public void testHasType3() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from x : V{WhereExpression} report hasType{WhereExpression, Definition}(x) end";
		JValue result = evalTestQuery("HasType3", queryString);
		assertEquals(1, result.toCollection().size());
		for (JValue j : result.toCollection()) {
			assertEquals(true, (boolean) j.toBoolean());
		}
	}

	@Test
	public void testIsA() throws Exception {
		String queryString = "isA(\"Variable\", \"Identifier\")";
		JValue result = evalTestQuery("IsA", queryString);
		assertEquals(JValueBoolean.getTrueValue(), result.toBoolean());
	}

	@Test
	public void testTypes() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from x : V{WhereExpression} report types(edgesConnected(x)) end";
		JValue result = evalTestQuery("EdgeTypeSet", queryString);
		assertEquals(3, getNthValue(result.toCollection(), 0).toCollection()
				.size());
	}

}
