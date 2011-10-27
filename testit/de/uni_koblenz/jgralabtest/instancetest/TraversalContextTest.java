package de.uni_koblenz.jgralabtest.instancetest;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.TraversalContext;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.SubGraphMarker;
import de.uni_koblenz.jgralab.impl.InternalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.Link;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalSchema;
import de.uni_koblenz.jgralabtest.schemas.minimal.Node;

/**
 * This class tests various methods from Graph, Vertex, and Edge with respect to
 * the traversal context in the graph.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
@RunWith(Parameterized.class)
public class TraversalContextTest extends InstanceTest {

	public TraversalContextTest(ImplementationType implementationType,
			String dbURL) {
		super(implementationType, dbURL);
	}

	private static final String ID = "TraversalContext";

	@Parameters
	public static Collection<Object[]> configure() {
		return getParameters();
	}

	private MinimalGraph graph;
	private InternalGraph iGraph;
	private TraversalContext alwaysTrue, alwaysFalse, subgraph;

	@Before
	public void setUp() {
		// create graph

		switch (implementationType) {
		case STANDARD:
			graph = MinimalSchema.instance().createMinimalGraph();
			break;
		case TRANSACTION:
			graph = MinimalSchema.instance()
					.createMinimalGraphWithTransactionSupport();
			break;
		case DATABASE:
			graph = createMinimalGraphWithDatabaseSupport();
			break;
		}

		createGraphAndSubgraph();

		iGraph = (InternalGraph) graph;

		createDefaultTCs();
	}

	private void createGraphAndSubgraph() {
		// vertices
		Node v1 = graph.createNode();
		Node v2 = graph.createNode();
		Node v3 = graph.createNode();
		Node v4 = graph.createNode();
		Node v5 = graph.createNode();
		Node v6 = graph.createNode();
		Node v7 = graph.createNode();
		Node v8 = graph.createNode();
		Node v9 = graph.createNode();

		Link[] links = new Link[21];

		// edges
		links[1] = graph.createLink(v1, v2);
		links[2] = graph.createLink(v1, v3);
		links[3] = graph.createLink(v1, v5);
		links[4] = graph.createLink(v2, v4);
		links[5] = graph.createLink(v2, v5);
		links[6] = graph.createLink(v3, v4);
		links[7] = graph.createLink(v3, v5);
		links[8] = graph.createLink(v4, v5);
		links[9] = graph.createLink(v5, v6);
		links[10] = graph.createLink(v5, v7);
		links[11] = graph.createLink(v5, v8);
		links[12] = graph.createLink(v5, v9);
		links[13] = graph.createLink(v6, v1);
		links[14] = graph.createLink(v6, v2);
		links[15] = graph.createLink(v7, v1);
		links[16] = graph.createLink(v7, v3);
		links[17] = graph.createLink(v8, v3);
		links[18] = graph.createLink(v8, v4);
		links[19] = graph.createLink(v9, v2);
		links[20] = graph.createLink(v2, v4);

		SubGraphMarker subgraph = new SubGraphMarker(graph);

		for (int i = 1; i < links.length; i++) {
			subgraph.mark(links[i]);
		}
		subgraph.removeMark(v6);
		subgraph.removeMark(v7);
		subgraph.removeMark(v8);
		subgraph.removeMark(v9);

		this.subgraph = subgraph;
	}

	private void createDefaultTCs() {
		// TCs
		alwaysTrue = new TraversalContext() {

			@Override
			public boolean containsVertex(Vertex v) {
				return iGraph.containsVertex(v);
			}

			@Override
			public boolean containsGraphElement(GraphElement e) {
				return e.getGraph() == iGraph;
			}

			@Override
			public boolean containsEdge(Edge e) {
				return iGraph.containsEdge(e);
			}
		};

		alwaysFalse = new TraversalContext() {

			@Override
			public boolean containsVertex(Vertex v) {
				return false;
			}

			@Override
			public boolean containsGraphElement(GraphElement e) {
				return false;
			}

			@Override
			public boolean containsEdge(Edge e) {
				return false;
			}
		};
	}

	private MinimalGraph createMinimalGraphWithDatabaseSupport() {
		dbHandler.connectToDatabase();
		dbHandler.loadMinimalSchemaIntoGraphDatabase();
		return dbHandler.createMinimalGraphWithDatabaseSupport(ID);
	}

	@After
	public void tearDown() {
		if (implementationType == ImplementationType.DATABASE) {
			cleanAndCloseGraphDatabase();
		}
	}

	/*
	 * Methods to test from Graph: get/set TraversalContext, getFirstVertex (all
	 * variants), getLastVertex (all variants), getFirstEdge, getLastEdge,
	 * getECount getVCount, edges (all variants), vertices (all variants)
	 */

	/*
	 * Methods to test from Vertex: getDegree (all variants), getNextVertex(all
	 * variants), getPrevVertex, getFirstIncidence(all variants),
	 * getLastIncidence, incidences(all variants), reachable vertices (all
	 * variants), adjacences
	 */

	/*
	 * Methods to test from Edge: getNextIncidence (all variants),
	 * getPrevIncidence, getNextEdge(all variants), getPrevEdge
	 */

	/*
	 * Other methods to test: GraphIO.saveGraphToStream
	 */

	private void cleanAndCloseGraphDatabase() {
		dbHandler.clearAllTables();
		dbHandler.closeGraphdatabase();
	}
}
