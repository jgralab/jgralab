package de.uni_koblenz.jgralabtest.greql2;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueBag;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePath;
import de.uni_koblenz.jgralab.greql2.jvalue.JValuePathSystem;

public class GreqlConstructions extends GenericTest {

	/*
	 * Test method for
	 * 'greql2.evaluator.GreqlEvaluator.evaluateForwardVertexSet(ForwardVertexSet,
	 * Graph)'
	 */
	@Test
	public void testPathSystemConstruction() throws Exception {
		String queryString = "from c: V{localities.County} "
				+ "with c.name = 'Rheinland-Pfalz' "
				+ "report pathSystem(c, -->{localities.ContainsLocality} -->{connections.AirRoute}) "
				+ "end";
		JValue result = evalTestQuery("PathSystemConstruction", queryString,
				TestVersion.ROUTE_MAP_GRAPH);
		JValueBag bag = result.toCollection().toJValueBag();
		System.out.println(bag);
		assertEquals(1, bag.size());
		for (JValue v : bag) {
			JValuePathSystem sys = v.toPathSystem();
			assertEquals(2, sys.depth());
			assertEquals(3, sys.weight());
		}
	}

	@Test
	public void testPathSystemOnGreqlGraph() throws Exception {
		String queryString = "extractPath(pathSystem(getVertex(1), (<>--|(<-- <->))*), getVertex(34))";
		JValue result = evalTestQuery("PathSystemOnGreqlGraph", queryString,
				loadTestGraph());
		JValuePath path = result.toPath();
		System.out.println("Path has length " + path.toPath().pathLength());
		System.out.println(path);
	}

	@Test
	public void testPathSystemOnGreqlGraph2() throws Exception {
		String queryString = "extractPath(pathSystem(getVertex(1), (<>--|(<-- <->))*))";
		JValue result = evalTestQuery("PathSystemOnGreqlGraph", queryString,
				loadTestGraph());
		for (JValue e : result.toJValueSet()) {
			JValuePath path = e.toPath();
			System.out.println("Path has length " + path.toPath().pathLength());
			System.out.println(path);
		}

	}

	private Graph loadTestGraph() throws GraphIOException {
		return GraphIO
				.loadGraphFromFileWithStandardSupport(
						"/Users/dbildh/repositories/ist/jgralab/testit/testgraphs/greqltestgraph.tg",
						null);

	}
}
