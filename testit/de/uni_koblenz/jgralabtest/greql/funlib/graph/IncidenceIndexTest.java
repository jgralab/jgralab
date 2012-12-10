package de.uni_koblenz.jgralabtest.greql.funlib.graph;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.greql.funlib.FunLib;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.RouteSchema;

public class IncidenceIndexTest {

	private static Graph g;

	@BeforeClass
	public static void init() {
		try {
			g = RouteSchema.instance().loadRouteMap(
					"testit/testgraphs/greqltestgraph.tg");
		} catch (GraphIOException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void test1() {
		for (Vertex v : g.vertices()) {
			int i = 0;
			for (Edge inc : v.incidences()) {
				assertEquals(i,
						FunLib.apply("incidenceIndex", inc, inc.getThis()));
				assertEquals(i, FunLib.apply("thisIncidenceIndex", inc));
				if (inc.isNormal()) {
					assertEquals(i, FunLib.apply("alphaIncidenceIndex", inc));
				} else {
					assertEquals(i, FunLib.apply("omegaIncidenceIndex", inc));
				}
				i++;
			}
		}
	}
}
