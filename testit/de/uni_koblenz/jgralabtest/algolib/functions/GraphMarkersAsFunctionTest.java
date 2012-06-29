package de.uni_koblenz.jgralabtest.algolib.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.ArrayVertexMarker;
import de.uni_koblenz.jgralab.graphmarker.BitSetVertexMarker;
import de.uni_koblenz.jgralab.graphmarker.DoubleVertexMarker;
import de.uni_koblenz.jgralab.graphmarker.IntegerVertexMarker;
import de.uni_koblenz.jgralab.graphmarker.LongVertexMarker;
import de.uni_koblenz.jgralabtest.algolib.algorithms.TestGraphs;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;

public class GraphMarkersAsFunctionTest {
	private SimpleGraph graph;

	private int[] intData;
	private long[] longData;
	private double[] doubleData;
	private boolean[] booleanData;
	private String[] objectData;

	private IntegerVertexMarker intMarker;
	private LongVertexMarker longMarker;
	private DoubleVertexMarker doubleMarker;
	private BitSetVertexMarker booleanMarker;
	private ArrayVertexMarker<String> objectMarker;

	@Before
	public void setUp() {
		intData = new int[] { 0, 16, 42, -47, 8 };
		longData = new long[] { 0l, 0l, 802398430298098l, -80980983245l, 7l };
		doubleData = new double[] { 0.0, 16.47, -42.33023, Math.PI, Math.E };
		booleanData = new boolean[] { false, true, true, false, false };
		objectData = new String[] { null, "eins", "zwei", "drei", "vier" };

		graph = TestGraphs.getSimpleCyclicGraph();

		initGraphMarkers();
		fillGraphMarkers();
	}

	private void initGraphMarkers() {
		intMarker = new IntegerVertexMarker(graph);
		longMarker = new LongVertexMarker(graph);
		doubleMarker = new DoubleVertexMarker(graph);
		booleanMarker = new BitSetVertexMarker(graph);
		objectMarker = new ArrayVertexMarker<String>(graph);
	}

	private void fillGraphMarkers() {
		for (Vertex v : graph.vertices()) {
			int i = v.getId();
			intMarker.set(v, intData[i]);
			longMarker.set(v, longData[i]);
			doubleMarker.set(v, doubleData[i]);
			booleanMarker.set(v, booleanData[i]);
			objectMarker.set(v, objectData[i]);
		}
	}

	@Test
	public void testIsDefined() {
		initGraphMarkers();
		for (Vertex v : graph.vertices()) {
			assertFalse(intMarker.isDefined(v));
			assertFalse(longMarker.isDefined(v));
			assertFalse(doubleMarker.isDefined(v));
			// boolean graph markers are always defined if the vertex is in the
			// same graph
			assertTrue(booleanMarker.isDefined(v));
			assertFalse(objectMarker.isDefined(v));
		}

		fillGraphMarkers();

		for (Vertex v : graph.vertices()) {
			assertTrue(intMarker.isDefined(v));
			assertTrue(longMarker.isDefined(v));
			assertTrue(doubleMarker.isDefined(v));
			assertTrue(booleanMarker.isDefined(v));
			assertTrue(objectMarker.isDefined(v));
		}

	}

	@Test
	public void testGet() {
		for (Vertex v : graph.vertices()) {
			int i = v.getId();
			assertEquals(intData[i], intMarker.get(v));
			assertEquals(longData[i], longMarker.get(v));
			assertEquals(doubleData[i], doubleMarker.get(v), 0.00001);
			assertEquals(booleanData[i], booleanMarker.get(v));
			assertEquals(objectData[i], objectMarker.get(v));
		}
	}

	@Test
	public void testGetDomainElements() {
		fail("Not implemented yet.");
	}

	@Test
	public void testIterator() {
		fail("Not implemented yet.");
	}
}
