package de.uni_koblenz.jgralabtest.greql2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.junctions.Crossroad;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.localities.County;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.localities.HasCapital;
import de.uni_koblenz.jgralabtest.schemas.greqltestschema.localities.Locality;


public class SubgraphRestrictionTest extends GenericTest {

	@Test
	public void testVertexTypeRestrictedSubgraph() throws Exception {
		String queryString = "on vertexTypeSubgraph{junctions.Crossroad}() : from v:V{} report v end";
		PVector result = (PVector) evalTestQuery(queryString);
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
		PVector result = (PVector) evalTestQuery(queryString);
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
		result = (PVector) evalTestQuery(queryString);
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
	public void testExpressionCreatedSubgraph() throws Exception {
		String queryString = "on vertexTypeSubgraph{junctions.Crossroad}() : from v:V report v end";
		PVector result = (PVector) evalTestQuery(queryString);
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
	public void testElementSetCreatedSubgraph() throws Exception {
		String queryString = "(on elementSetSubgraph(vSet,eSet) : from v:V report v end) where vSet := from v:V{junctions.Crossroad} report v end, eSet := from e:E{connections.Highway} report e end";
		PVector result = (PVector) evalTestQuery(queryString);
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
