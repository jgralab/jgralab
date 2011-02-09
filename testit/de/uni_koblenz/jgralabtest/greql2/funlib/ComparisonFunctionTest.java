package de.uni_koblenz.jgralabtest.greql2.funlib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralabtest.greql2.GenericTests;

public class ComparisonFunctionTest extends GenericTests {

	private static final String EQUALS = "Equals";

	@Test
	public void testEquals1() throws Exception {
		assertQueryEquals(EQUALS, "5 = 9", false);
		assertQueryEquals(EQUALS, "equals(5, 9)", false);
	}

	@Test
	public void testEquals2() throws Exception {
		assertQueryEquals(EQUALS, "'' = 'a'", false);
		assertQueryEquals(EQUALS, "equals('', 'a')", false);
	}

	@Test
	public void testEquals3() throws Exception {
		assertQueryEquals(EQUALS, "'a' = ''", false);
		assertQueryEquals(EQUALS, "equals('a', '')", false);
	}

	@Test
	public void testEquals4() throws Exception {
		assertQueryEquals(EQUALS, "'' = ''", true);
		assertQueryEquals(EQUALS, "equals('', '')", true);
	}

	@Test
	public void testEquals5() throws Exception {
		assertQueryEquals(EQUALS, "'a' = 'a'", true);
		assertQueryEquals(EQUALS, "equals('a', 'a')", true);
	}

	@Test
	public void testEquals6() throws Exception {
		assertQueryEquals(EQUALS, "99.001 = 99.001", true);
		assertQueryEquals(EQUALS, "equals(99.001, 99.001)", true);
	}

	@Test
	public void testEquals7() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from x : V{WhereExpression}, y : V{WhereExpression} report equals(x,y) end";
		assertQueryEquals(EQUALS, queryString, true);
	}

	@Test
	public void testEquals8() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from x : V{WhereExpression}, y : E{IsBoundExprOfDefinition} report equals(x,y) end";
		assertQueryEquals(EQUALS, queryString, true);
	}

	public void testEquals9() throws Exception {
		String queryString = "from x : V, y : E report equals(x,y) end";
		JValue result = evalTestQuery("Equals5", queryString);
		for (JValue v : result.toCollection()) {
			assertEquals(Boolean.FALSE, v.toBoolean());
		}
	}

	@Test
	public void testGrEqual1() throws Exception {
		assertQueryEquals("GrEqual", "3 >= 2", true);
		assertQueryEquals("GrEqual", "grEqual(3, 2)", true);
	}

	@Test
	public void testGrEqual2() throws Exception {
		assertQueryEquals("GrEqual", "17 >= 17", true);
		assertQueryEquals("GrEqual", "grEqual(17, 17.0)", true);
	}

	@Test
	public void testGrEqual3() throws Exception {
		assertQueryEquals("GrEqual", "0.000000000000001 >= 0", true);
		assertQueryEquals("GrEqual", "grEqual(0.000000000000001, 0)", true);
	}

	@Test
	public void testGrEqual4() throws Exception {
		assertQueryEquals("GrEqual", "17 >= 199", false);
		assertQueryEquals("GrEqual", "grEqual(17, 199)", false);
	}

	@Test
	public void testGrEqual5() throws Exception {
		assertQueryEquals("GrEqual", "5.50 >= 4.701", true);
		assertQueryEquals("GrEqual", "grEqual(5.50, 4.701)", true);
	}

	@Test
	public void testGrEqual6() throws Exception {
		assertQueryEquals("GrEqual", "33.1 >= 33.1", true);
		assertQueryEquals("GrEqual", "grEqual(33.1, 33.1)", true);
	}

	@Test
	public void testGrEqual7() throws Exception {
		assertQueryEquals("GrEqual", "117.4 >= 111", true);
		assertQueryEquals("GrEqual", "grEqual(117.4, 111)", true);
	}

	@Test
	public void testGrEqual8() throws Exception {
		assertQueryEquals("GrEqual", "3 >= 187.00001", false);
		assertQueryEquals("GrEqual", "grEqual(3, 187.00001)", false);
	}

	@Test
	public void testNotEquals() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from x : V{WhereExpression}, y : V{Variable} reportSet x <> y end";
		JValue result = evalTestQuery("NotEquals", queryString);
		assertEquals(1, result.toCollection().size());
		assertEquals(true, (boolean) getNthValue(result.toCollection(), 0)
				.toBoolean());
	}

	@Test
	public void testNotEquals2() throws Exception {
		String queryString = "from x : V{Greql2Expression}, y : V{Greql2Expression} reportSet x <> y end";
		JValue result = evalTestQuery("NotEquals2", queryString);
		assertEquals(1, result.toCollection().size());
		assertEquals(false, (boolean) getNthValue(result.toCollection(), 0)
				.toBoolean());
	}
}