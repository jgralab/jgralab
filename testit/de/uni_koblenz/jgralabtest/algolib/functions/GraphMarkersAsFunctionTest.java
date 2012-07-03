package de.uni_koblenz.jgralabtest.algolib.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.entries.BooleanFunctionEntry;
import de.uni_koblenz.jgralab.algolib.functions.entries.DoubleFunctionEntry;
import de.uni_koblenz.jgralab.algolib.functions.entries.FunctionEntry;
import de.uni_koblenz.jgralab.algolib.functions.entries.IntFunctionEntry;
import de.uni_koblenz.jgralab.algolib.functions.entries.LongFunctionEntry;
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
		booleanData = new boolean[] { false, true, false, true, false };
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
		Iterable<Vertex> elements;

		elements = intMarker.getDomainElements();
		assertDomainElementOrder(elements);

		elements = longMarker.getDomainElements();
		assertDomainElementOrder(elements);

		elements = doubleMarker.getDomainElements();
		assertDomainElementOrder(elements);

		elements = booleanMarker.getDomainElements();
		assertDomainElementOrder(elements);

		elements = objectMarker.getDomainElements();
		assertDomainElementOrder(elements);
	}

	public void assertDomainElementOrder(Iterable<Vertex> vertexIterable) {
		Iterable<Vertex> graphVertices = graph.vertices();
		Iterator<Vertex> expected = graphVertices.iterator();
		Iterator<Vertex> seen = vertexIterable.iterator();
		while (expected.hasNext()) {
			Vertex expectedVertex = expected.next();
			Vertex seenVertex = seen.next();
			assertEquals(expectedVertex, seenVertex);
		}
		try {
			seen.next();
			fail("There should not be any more vertices in the function domain.");
		} catch (NoSuchElementException e) {
		}
	}

	@Test
	public void testIterator() {
		Iterator<IntFunctionEntry<Vertex>> intIter = intMarker.iterator();
		Iterator<LongFunctionEntry<Vertex>> longIter = longMarker.iterator();
		Iterator<DoubleFunctionEntry<Vertex>> doubleIter = doubleMarker
				.iterator();
		Iterator<BooleanFunctionEntry<Vertex>> booleanIter = booleanMarker
				.iterator();
		Iterator<FunctionEntry<Vertex, String>> objectIter = objectMarker
				.iterator();

		for (int i = 1; i <= 4; i++) {
			System.out.println(i);
			assertItersHaveNext(intIter, longIter, doubleIter, booleanIter,
					objectIter);

			Vertex expectedVertex = graph.getVertex(i);

			IntFunctionEntry<Vertex> nextIntEntry = intIter.next();
			assertEquals(expectedVertex, nextIntEntry.getFirst());
			assertEquals(intData[i], nextIntEntry.getSecond());

			LongFunctionEntry<Vertex> nextLongEntry = longIter.next();
			assertEquals(expectedVertex, nextLongEntry.getFirst());
			assertEquals(longData[i], nextLongEntry.getSecond());

			DoubleFunctionEntry<Vertex> nextDoubleEntry = doubleIter.next();
			assertEquals(expectedVertex, nextDoubleEntry.getFirst());
			assertEquals(doubleData[i], nextDoubleEntry.getSecond(), 0.00001);

			BooleanFunctionEntry<Vertex> nextBooleanEntry = booleanIter.next();
			assertEquals(expectedVertex, nextBooleanEntry.getFirst());
			assertEquals(booleanData[i], nextBooleanEntry.getSecond());

			FunctionEntry<Vertex, String> nextObjectEntry = objectIter.next();
			assertEquals(expectedVertex, nextObjectEntry.getFirst());
			assertEquals(objectData[i], nextObjectEntry.getSecond());

		}

		try {
			intIter.next();
			fail("There should not be any more entries in the int function.");
		} catch (NoSuchElementException e) {
		}

		try {
			longIter.next();
			fail("There should not be any more entries in the long function.");
		} catch (NoSuchElementException e) {
		}

		try {
			doubleIter.next();
			fail("There should not be any more entries in the double function.");
		} catch (NoSuchElementException e) {
		}

		try {
			booleanIter.next();
			fail("There should not be any more entries in the boolean function.");
		} catch (NoSuchElementException e) {
		}

		try {
			objectIter.next();
			fail("There should not be any more entries in the object function.");
		} catch (NoSuchElementException e) {
		}

	}

	private void assertItersHaveNext(
			Iterator<IntFunctionEntry<Vertex>> intIter,
			Iterator<LongFunctionEntry<Vertex>> longIter,
			Iterator<DoubleFunctionEntry<Vertex>> doubleIter,
			Iterator<BooleanFunctionEntry<Vertex>> booleanIter,
			Iterator<FunctionEntry<Vertex, String>> objectIter) {
		assertTrue(intIter.hasNext());
		assertTrue(longIter.hasNext());
		assertTrue(doubleIter.hasNext());
		assertTrue(booleanIter.hasNext());
		assertTrue(objectIter.hasNext());
	}
}
