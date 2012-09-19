package de.uni_koblenz.jgralabtest.greql.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.executable.ExecutableQuery;
import de.uni_koblenz.jgralab.greql.executable.GreqlCodeGenerator;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;

public class SubgraphEvaluatorTest {

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

	@SuppressWarnings("unchecked")
	private <SC extends GraphElementClass<SC, IC>, IC extends GraphElement<SC, IC>> boolean isInstanceOf(
			GraphElement<SC, IC> v, String type) {
		return v.isInstanceOf((SC) datagraph.getSchema()
				.getAttributedElementClass(type));
	}

	private String createContainmentMessage(GraphElement<?, ?> v,
			boolean showWordNot) {
		return (v instanceof Vertex ? "v" : "e") + v.getId() + " of type "
				+ v.getAttributedElementClass().getQualifiedName() + " is "
				+ (showWordNot ? "not" : "") + " contained.";
	}

	public ExecutableQuery createQueryClass(String query, String classname)
			throws InstantiationException, IllegalAccessException {
		try {
			String filePrefix = "./testit/" + classname.replace('.', '/');
			GreqlQuery.createQuery(query).getQueryGraph()
					.save(filePrefix + ".tg");
			Tg2Dot.main(new String[] { "-g", filePrefix + ".tg", "-o",
					filePrefix + ".png", "-i", "-e", "-r", "-t", "png" });
		} catch (GraphIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		GreqlCodeGenerator.generateCode(query, datagraph.getSchema(), classname
				+ "_", "./testit/");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, datagraph.getSchema(), classname);
		return generatedQuery.newInstance();
	}

	/*
	 * VertexSetSubgraph
	 */

	@Test
	public void testVertexSetSubgraph_empty() throws InstantiationException,
			IllegalAccessException {
		String queryText = "on vertexSetSubgraph(set()): V{}";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestVertexSetSubgraph_empty") }) {
			@SuppressWarnings("unchecked")
			Set<Vertex> ergSet = (Set<Vertex>) query.evaluate(datagraph);
			assertTrue(ergSet.isEmpty());
		}
	}

	@Test
	public void testVertexSetSubgraph_oneVertex()
			throws InstantiationException, IllegalAccessException {
		String queryText = "on vertexSetSubgraph(set(getVertex(1))): V{}";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.VertexSetSubgraph_oneVertex") }) {
			@SuppressWarnings("unchecked")
			Set<Vertex> ergSet = (Set<Vertex>) query.evaluate(datagraph);
			assertEquals(1, ergSet.size());
			assertTrue(ergSet.contains(datagraph.getVertex(1)));
		}
	}

	@Test
	public void testVertexSetSubgraph_twoVertices()
			throws InstantiationException, IllegalAccessException {
		String queryText = "on vertexSetSubgraph(set(getVertex(1),getVertex(23))): V{}";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestVertexSetSubgraph_twoVertices") }) {
			@SuppressWarnings("unchecked")
			Set<Vertex> ergSet = (Set<Vertex>) query.evaluate(datagraph);
			assertEquals(2, ergSet.size());
			assertTrue(ergSet.contains(datagraph.getVertex(1)));
			assertTrue(ergSet.contains(datagraph.getVertex(23)));
		}
	}

	@Test
	public void testVertexSetSubgraph_empty_noEdge()
			throws InstantiationException, IllegalAccessException {
		String queryText = "on vertexSetSubgraph(set()): E{}";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.VertexSetSubgraph_empty_noEdge") }) {
			@SuppressWarnings("unchecked")
			Set<Edge> ergSet = (Set<Edge>) query.evaluate(datagraph);
			assertTrue(ergSet.isEmpty());
		}
	}

	@Test
	public void testVertexSetSubgraph_oneVertex_noEdge()
			throws InstantiationException, IllegalAccessException {
		String queryText = "on vertexSetSubgraph(set(getVertex(1))): E{}";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestVertexSetSubgraph_oneVertex_noEdge") }) {
			@SuppressWarnings("unchecked")
			Set<Edge> ergSet = (Set<Edge>) query.evaluate(datagraph);
			assertTrue(ergSet.isEmpty());
		}
	}

	@Test
	public void testVertexSetSubgraph_twoVertices_oneEdge()
			throws InstantiationException, IllegalAccessException {
		String queryText = "on vertexSetSubgraph(set(getVertex(1),getVertex(23))): E{}";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestVertexSetSubgraph_twoVertices_oneEdge") }) {
			@SuppressWarnings("unchecked")
			Set<Edge> ergSet = (Set<Edge>) query.evaluate(datagraph);
			assertEquals(1, ergSet.size());
			assertTrue(ergSet.contains(datagraph.getEdge(135)));
		}
	}

	/*
	 * EdgeSetSubgraph
	 */

	@Test
	public void testEdgeSetSubgraph_empty() throws InstantiationException,
			IllegalAccessException {
		String queryText = "on edgeSetSubgraph(set()): E{}";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestEdgeSetSubgraph_empty") }) {
			@SuppressWarnings("unchecked")
			Set<Edge> ergSet = (Set<Edge>) query.evaluate(datagraph);
			assertTrue(ergSet.isEmpty());
		}
	}

	@Test
	public void testEdgeSetSubgraph_oneEdge() throws InstantiationException,
			IllegalAccessException {
		String queryText = "on edgeSetSubgraph(set(getEdge(333))): E{}";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestEdgeSetSubgraph_oneEdge") }) {
			@SuppressWarnings("unchecked")
			Set<Edge> ergSet = (Set<Edge>) query.evaluate(datagraph);
			assertEquals(1, ergSet.size());
			assertTrue(ergSet.contains(datagraph.getEdge(333)));
		}
	}

	@Test
	public void testEdgeSetSubgraph_twoEdges() throws InstantiationException,
			IllegalAccessException {
		String queryText = "on edgeSetSubgraph(set(getEdge(333),getEdge(334))): E{}";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestEdgeSetSubgraph_twoEdges") }) {
			@SuppressWarnings("unchecked")
			Set<Edge> ergSet = (Set<Edge>) query.evaluate(datagraph);
			assertEquals(2, ergSet.size());
			assertTrue(ergSet.contains(datagraph.getEdge(333)));
			assertTrue(ergSet.contains(datagraph.getEdge(334)));
		}
	}

	@Test
	public void testEdgeSetSubgraph_empty_twoVertices()
			throws InstantiationException, IllegalAccessException {
		String queryText = "on edgeSetSubgraph(set()): V{}";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestEdgeSetSubgraph_empty_twoVertices") }) {
			@SuppressWarnings("unchecked")
			Set<Vertex> ergSet = (Set<Vertex>) query.evaluate(datagraph);
			assertTrue(ergSet.isEmpty());
		}
	}

	@Test
	public void testEdgeSetSubgraph_oneEdge_twoVertices()
			throws InstantiationException, IllegalAccessException {
		String queryText = "on edgeSetSubgraph(set(getEdge(333))): V{}";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestEdgeSetSubgraph_oneEdge_twoVertices") }) {
			@SuppressWarnings("unchecked")
			Set<Vertex> ergSet = (Set<Vertex>) query.evaluate(datagraph);
			assertEquals(2, ergSet.size());
			assertTrue(ergSet.contains(datagraph.getVertex(4)));
			assertTrue(ergSet.contains(datagraph.getVertex(5)));
		}
	}

	@Test
	public void testEdgeSetSubgraph_twoEdges_threeVertices()
			throws InstantiationException, IllegalAccessException {
		String queryText = "on edgeSetSubgraph(set(getEdge(333),getEdge(334))): V{}";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestEdgeSetSubgraph_twoEdges_threeVertices") }) {
			@SuppressWarnings("unchecked")
			Set<Vertex> ergSet = (Set<Vertex>) query.evaluate(datagraph);
			assertEquals(3, ergSet.size());
			assertTrue(ergSet.contains(datagraph.getVertex(4)));
			assertTrue(ergSet.contains(datagraph.getVertex(5)));
			assertTrue(ergSet.contains(datagraph.getVertex(12)));
		}
	}

	/*
	 * VertexTypeSubgraph
	 */

	@Test
	public void testVertexTypeSubgraph_vertices()
			throws InstantiationException, IllegalAccessException {
		String queryText = "import junctions.*; on vertexTypeSubgraph{Plaza}(): V{}";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestVertexTypeSubgraph_vertices") }) {
			@SuppressWarnings("unchecked")
			Set<Vertex> ergSet = (Set<Vertex>) query.evaluate(datagraph);
			for (Vertex v : datagraph.vertices()) {
				if (isInstanceOf(v, "junctions.Plaza")) {
					assertTrue(createContainmentMessage(v, true),
							ergSet.contains(v));
				} else {
					assertFalse(createContainmentMessage(v, false),
							ergSet.contains(v));
				}
			}
		}
	}

	@Test
	public void testVertexTypeSubgraph_vertices_wrongTypes()
			throws InstantiationException, IllegalAccessException {
		String queryText = "import junctions.*; on vertexTypeSubgraph{^Plaza}(): V{}";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestVertexTypeSubgraph_vertices_wrongTypes") }) {
			@SuppressWarnings("unchecked")
			Set<Vertex> ergSet = (Set<Vertex>) query.evaluate(datagraph);
			for (Vertex v : datagraph.vertices()) {
				if (!isInstanceOf(v, "junctions.Plaza")) {
					assertTrue(createContainmentMessage(v, true),
							ergSet.contains(v));
				} else {
					assertFalse(createContainmentMessage(v, false),
							ergSet.contains(v));
				}
			}
		}
	}

	@Test
	public void testVertexTypeSubgraph_vertices_differentTypes()
			throws InstantiationException, IllegalAccessException {
		String queryText = "import junctions.*;import localities.*; on vertexTypeSubgraph{Plaza,City}(): V{}";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestVertexTypeSubgraph_vertices_differentTypes") }) {
			@SuppressWarnings("unchecked")
			Set<Vertex> ergSet = (Set<Vertex>) query.evaluate(datagraph);
			for (Vertex v : datagraph.vertices()) {
				if (isInstanceOf(v, "junctions.Plaza")
						|| isInstanceOf(v, "localities.City")) {
					assertTrue(createContainmentMessage(v, true),
							ergSet.contains(v));
				} else {
					assertFalse(createContainmentMessage(v, false),
							ergSet.contains(v));
				}
			}
		}
	}

	@Test
	public void testVertexTypeSubgraph_edges() throws InstantiationException,
			IllegalAccessException {
		String queryText = "import junctions.*; on vertexTypeSubgraph{Plaza}(): E{}";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestVertexTypeSubgraph_edges") }) {
			@SuppressWarnings("unchecked")
			Set<Edge> ergSet = (Set<Edge>) query.evaluate(datagraph);
			for (Edge e : datagraph.edges()) {
				if (isInstanceOf(e.getAlpha(), "junctions.Plaza")
						&& isInstanceOf(e.getOmega(), "junctions.Plaza")) {
					assertTrue(createContainmentMessage(e, true),
							ergSet.contains(e));
				} else {
					assertFalse(createContainmentMessage(e, false),
							ergSet.contains(e));
				}
			}
		}
	}

	/*
	 * EdgeTypeSubgraph
	 */

	@Test
	public void testEdgeTypeSubgraph_edges() throws InstantiationException,
			IllegalAccessException {
		String queryText = "import connections.*; on edgeTypeSubgraph{Connection}(): E{}";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestEdgeTypeSubgraph_edges") }) {
			@SuppressWarnings("unchecked")
			Set<Edge> ergSet = (Set<Edge>) query.evaluate(datagraph);
			for (Edge e : datagraph.edges()) {
				if (isInstanceOf(e, "connections.Connection")) {
					assertTrue(createContainmentMessage(e, true),
							ergSet.contains(e));
				} else {
					assertFalse(createContainmentMessage(e, false),
							ergSet.contains(e));
				}
			}
		}
	}

	@Test
	public void testEdgeTypeSubgraph_vertices() throws InstantiationException,
			IllegalAccessException {
		String queryText = "import connections.*; on edgeTypeSubgraph{Connection}(): V{}";
		for (GreqlQuery query : new GreqlQuery[] {
				GreqlQuery.createQuery(queryText),
				(GreqlQuery) createQueryClass(queryText,
						"testdata.TestEdgeTypeSubgraph_vertices") }) {
			@SuppressWarnings("unchecked")
			Set<Vertex> ergSet = (Set<Vertex>) query.evaluate(datagraph);
			for (Vertex v : datagraph.vertices()) {
				if (v.getDegree((EdgeClass) datagraph.getSchema()
						.getAttributedElementClass("connections.Connection")) > 0) {
					assertTrue(createContainmentMessage(v, true),
							ergSet.contains(v));
				} else {
					assertFalse(createContainmentMessage(v, false),
							ergSet.contains(v));
				}
			}
		}
	}
}
