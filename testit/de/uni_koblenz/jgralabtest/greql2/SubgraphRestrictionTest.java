package de.uni_koblenz.jgralabtest.greql2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.connections.Highway;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.junctions.Crossroad;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.localities.County;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.localities.HasCapital;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.localities.Locality;

public class SubgraphRestrictionTest extends GenericTest {

	@Test
	public void testVertexTypeRestrictedSubgraph() throws Exception {
		String queryString = "on vertexTypeSubgraph{junctions.Crossroad}() : from v:V{} report v end";
		PVector<?> result = (PVector<?>) evalTestQuery(queryString);
		int crossroads = (Integer) evalTestQuery("count(V{junctions.Crossroad})");
		int num = 0;
		for (Object val : result) {
			assertTrue(val instanceof Vertex);
			Vertex v = (Vertex) val;
			assertTrue(v instanceof Crossroad);
			num++;
		}
		assertEquals(crossroads, num);
	}

	@Test
	public void testEdgeTypeRestrictedSubgraph() throws Exception {
		String queryString = "on edgeTypeSubgraph{localities.HasCapital}() : from e:E{} report e end";
		PVector<?> result = (PVector<?>) evalTestQuery(queryString);
		int hasCapitals = (Integer) evalTestQuery("count(E{localities.HasCapital})");
		int num = 0;
		for (Object val : result) {
			assertTrue(val instanceof Edge);
			Edge e = (Edge) val;
			assertTrue(e instanceof HasCapital);
			num++;
		}
		assertEquals(hasCapitals, num);
		queryString = "on edgeTypeSubgraph{localities.HasCapital}() : from v:V{} report v end";
		result = (PVector<?>) evalTestQuery(queryString);
		int countiesOrCapitals = (Integer) evalTestQuery("count(from v:V{localities.County, localities.City} with degree{localities.HasCapital}(v)>0 report v end)");
		num = 0;
		for (Object val : result) {
			assertTrue(val instanceof Vertex);
			Vertex v = (Vertex) val;
			assertTrue((v instanceof County) || (v instanceof Locality));
			num++;
		}
		assertEquals(countiesOrCapitals, num);
	}

	@Test
	public void testEdgeSetCreatedSubgraph() throws Exception {
		String queryString = "(on edgeSetSubgraph(eSet) : from e:E report e end) where eSet := from e:E{connections.Highway} report e end";
		PVector<?> result = (PVector<?>) evalTestQuery(queryString);
		int highways = (Integer) evalTestQuery("count(E{connections.Highway})");
		int num = 0;
		for (Object val : result) {
			assertTrue(val instanceof Highway);
			num++;
		}
		assertEquals(highways, num);
		queryString = "(on edgeSetSubgraph(eSet) : from v:V report v end) where eSet := from e:E{connections.Highway} report e end";
		result = (PVector<?>) evalTestQuery(queryString);
		int junctionsAtHighways = (Integer) evalTestQuery("count(from v:V with degree{connections.Highway}(v)>0 report v end )");
		num = 0;
		for (Object val : result) {
			assertTrue(val instanceof Vertex);
			Vertex v = (Vertex) val;
			assertNotNull(v.getFirstIncidence(Highway.class));
			num++;
		}
		assertEquals(num, junctionsAtHighways);
	}

	@Test
	public void testVertexSetCreatedSubgraph() throws Exception {
		String queryString = "(on vertexSetSubgraph(vSet) : from v:V report v end) where vSet := from v:V{junctions.Crossroad} report v end";
		PVector<?> result = (PVector<?>) evalTestQuery(queryString);
		int crossroads = (Integer) evalTestQuery("count(V{junctions.Crossroad})");
		int num = 0;
		for (Object val : result) {
			assertTrue(val instanceof Crossroad);
			num++;
		}
		assertEquals(crossroads, num);
		queryString = "(on vertexSetSubgraph(vSet) : from e:E report e end) where vSet := from v:V{junctions.Crossroad} report v end";
		result = (PVector<?>) evalTestQuery(queryString);
		int edgesConnectingCrossroads = (Integer) evalTestQuery("count(from e:E with contains(V{junctions.Crossroad}, alpha(e)) and contains(V{junctions.Crossroad}, omega(e)) report e end )");
		num = 0;
		for (Object val : result) {
			assertTrue(val instanceof Edge);
			Edge e = (Edge) val;
			assertTrue((e.getAlpha() instanceof Crossroad)
					&& (e.getOmega() instanceof Crossroad));
			num++;
		}
		assertEquals(num, edgesConnectingCrossroads);
	}

	@Test
	public void testElementSetCreatedSubgraph() throws Exception {
		String queryString = "(on elementSetSubgraph(vSet,eSet) : from v:V report v end) where vSet := from v:V{junctions.Crossroad} report v end, eSet := from e:E{connections.Highway} report e end";
		PVector<?> result = (PVector<?>) evalTestQuery(queryString);
		int crossroads = (Integer) evalTestQuery("count(V{junctions.Crossroad})");
		int num = 0;
		for (Object val : result) {
			assertTrue(val instanceof Vertex);
			Vertex v = (Vertex) val;
			assertTrue(v instanceof Crossroad);
			num++;
		}
		assertEquals(crossroads, num);
	}

}
