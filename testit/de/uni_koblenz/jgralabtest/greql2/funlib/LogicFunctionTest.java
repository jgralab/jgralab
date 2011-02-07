package de.uni_koblenz.jgralabtest.greql2.funlib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralabtest.greql2.GenericTests;

// TODO the functions "not" and "xor" haven't been tested.
public class LogicFunctionTest extends GenericTests {

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateSequentialPathDescription(SequentialPathDescription,
	 * Graph)'
	 */
	@Test
	public void testAnd() throws Exception {
		String queryString = "from el:list(1..100) "
				+ "with el % 2 = 0 and el % 3 = 0 report el end";
		JValue result = evalTestQuery("and", queryString,
				TestVersion.CITY_MAP_GRAPH);
		assertEquals(16, result.toCollection().size());
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateSequentialPathDescription(SequentialPathDescription,
	 * Graph)'
	 */
	@Test
	public void testOr() throws Exception {
		String queryString = "from el:list(1..100) "
				+ "with el % 2 = 0 or el % 3 = 0 report el end";
		JValue result = evalTestQuery("Or", queryString,
				TestVersion.CITY_MAP_GRAPH);
		assertEquals(50 + 17, result.toCollection().size());
	}

}
