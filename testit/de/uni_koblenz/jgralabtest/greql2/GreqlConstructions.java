package de.uni_koblenz.jgralabtest.greql2;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.pcollections.PCollection;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.greql2.types.Path;
import de.uni_koblenz.jgralab.greql2.types.PathSystem;

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
		Object result = evalTestQuery("PathSystemConstruction", queryString,
				TestVersion.ROUTE_MAP_GRAPH);
		@SuppressWarnings("unchecked")
		PCollection<PathSystem> list = ((PCollection<PathSystem>) result);
		assertEquals(1, list.size());
		for (PathSystem v : list) {
			PathSystem sys = v;
			assertEquals(2, sys.getDepth());
			assertEquals(3, sys.getWeight());
		}
	}

	@Test
	public void testPathSystemOnGreqlGraph() throws Exception {
		String queryString = "extractPath(pathSystem(getVertex(1), (<>--|(<-- <->))*), getVertex(34))";
		Object result = evalTestQuery("PathSystemOnGreqlGraph", queryString,
				loadTestGraph());
		// TODO test seriously
		@SuppressWarnings("unused")
		Path path = (Path) result;
		// System.out.println("Path has length " + path.toPath().pathLength());
		// System.out.println(path);
	}

	@Test
	public void testPathSystemOnGreqlGraph2() throws Exception {
		String queryString = "extractPath(pathSystem(getVertex(1), (<>--|(<-- <->))*))";
		// TODO test seriously
		@SuppressWarnings("unused")
		Object result = evalTestQuery("PathSystemOnGreqlGraph", queryString,
				loadTestGraph());
		// for (JValue e : result.toJValueSet()) {
		// JValuePath path = e.toPath();
		// System.out.println("Path has length " + path.toPath().pathLength());
		// System.out.println(path);
		// }
	}

	private Graph loadTestGraph() throws GraphIOException {
		return GraphIO.loadGraphFromFileWithStandardSupport(
				"testit/testgraphs/greqltestgraph.tg", null);

	}
}
