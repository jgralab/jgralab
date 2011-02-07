package de.uni_koblenz.jgralabtest.greql2.funlib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralabtest.greql2.GenericTests;

public class LogicFunctionTest extends GenericTests {

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateSequentialPathDescription(SequentialPathDescription,
	 * Graph)'
	 */
	@Test
	public void testAnd() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from var: V{Variable}, def: V{WhereExpression} with var  -->{IsVarOf} -->{IsDefinitionOf} def and 1=1 report var end";
		JValue result = evalTestQuery("and", queryString);
		assertEquals(4, result.toCollection().size());
	}

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateSequentialPathDescription(SequentialPathDescription,
	 * Graph)'
	 */
	@Test
	public void testOr() throws Exception {
		// TODO: Broken, because the GReQL parser removes all WhereExpressions
		// and LetExpressions!
		String queryString = "from var: V{Variable}, def: V{WhereExpression} with var  -->{IsVarOf} -->{IsDefinitionOf} def or 1=2 report var end";
		JValue result = evalTestQuery("Or", queryString);
		assertEquals(4, result.toCollection().size());
	}

}
