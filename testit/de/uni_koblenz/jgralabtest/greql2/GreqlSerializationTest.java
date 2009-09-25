/**
 * 
 */
package de.uni_koblenz.jgralabtest.greql2;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.uni_koblenz.jgralab.greql2.EnhancedGreql2;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;

/**
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 */
public class GreqlSerializationTest {

	private void check(String query) {
		GreqlEvaluator e1 = new GreqlEvaluator(query, null, null);
		e1.parseQuery();
		e1.setDatagraph(e1.getSyntaxGraph());

		GreqlEvaluator e2 = new GreqlEvaluator(((EnhancedGreql2) e1
				.getSyntaxGraph()).serialize(), e1.getSyntaxGraph(), null);

		e1.startEvaluation();
		e2.startEvaluation();

		JValue r1 = e1.getEvaluationResult();
		JValue r2 = e2.getEvaluationResult();

		assertEquals(r1, r2);
	}

	private String[] queries = {
			"from i : list(1..10) with i*i < 15 report i end",
			"forall i : list(1..100), i <> 10, i < 90 @ i * i + 1 % 4 = 17",
			"from i : from i : list(30..40) with i < 35 reportSet i end, x : list(1, 3, 17, 19) with isPrime(x+i) report i, x end" };

	@Test
	public void testAll() {
		for (String q : queries) {
			check(q);
		}
	}
}
