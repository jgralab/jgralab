/**
 * 
 */
package de.uni_koblenz.jgralabtest.greql2.evaluator;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluatorImpl;
import de.uni_koblenz.jgralab.greql2.evaluator.QueryImpl;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;

/**
 * Test for all {@link VertexEvaluator}s. Used {@link Graph} is the
 * greqltestgraph.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class VertexEvaluatorTest {

	private static Graph datagraph;

	@BeforeClass
	public static void setUpBeforeClass() throws GraphIOException {
		datagraph = GraphIO.loadGraphFromFile(
				"./testit/testgraphs/greqltestgraph.tg",
				ImplementationType.STANDARD, null);
	}

	@AfterClass
	public static void tearDownAfterClass() {
		datagraph = null;
	}

	@Test
	public void testBoolLiteralEvaluator() throws Exception {
		try {
			assertTrue((Boolean) new GreqlEvaluatorImpl(new QueryImpl("true"),
					datagraph, new HashMap<String, Object>()).getResult());
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

}
