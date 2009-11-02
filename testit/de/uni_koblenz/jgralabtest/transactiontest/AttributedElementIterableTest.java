package de.uni_koblenz.jgralabtest.transactiontest;

import java.util.ConcurrentModificationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.trans.AttributedElementIterable;
import de.uni_koblenz.jgralab.trans.CommitFailedException;
import de.uni_koblenz.jgralab.trans.Transaction;
import de.uni_koblenz.jgralabtest.schemas.motorwaymap.City;
import de.uni_koblenz.jgralabtest.schemas.motorwaymap.Motorway;
import de.uni_koblenz.jgralabtest.schemas.motorwaymap.MotorwayMap;
import de.uni_koblenz.jgralabtest.schemas.motorwaymap.MotorwayMapSchema;

//import junit.framework.JUnit4TestAdapter;

/**
 * Test cases for class AttributedElementIterableTest. 
 * Generalized Iterable-Implementation for transaction concept.
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 */
public class AttributedElementIterableTest {
	private MotorwayMap motorwayMap;
	private Transaction readWriteTransaction1;
	private Transaction readWriteTransaction2;

	private final int V = 1;
	private final int E = 1;
	private final int N = 10;

	@Before
	public void setUp() throws CommitFailedException {
		MotorwayMapSchema schema = MotorwayMapSchema.instance();
		motorwayMap = schema.createMotorwayMapWithTransactionSupport(V, E);
		motorwayMap.newTransaction();
		// city (v1) and motorway (v2) have 10 incidences
		// (<e1,e2,e3,e4,e5,e6,...,e10> and
		// <-e1,-e2,-e3,-e4,-e5,-e6,...,-e10> respectively)
		City city = motorwayMap.createCity();

		Motorway motorway = motorwayMap.createMotorway();
		for (int i = 0; i < N; i++) {
			motorwayMap.createExit(city, motorway);
		}
		for (int i = 0; i < N; i++) {
			city = motorwayMap.createCity();
			motorway = motorwayMap.createMotorway();
			motorwayMap.createExit(city, motorway);
		}
		motorwayMap.commit();

		readWriteTransaction1 = motorwayMap.newTransaction();
		readWriteTransaction2 = motorwayMap.newTransaction();
	}

	@After
	public void tearDown() {
		motorwayMap = null;
	}

	/**
	 * Iterate through Vseq without changing V. Should work without Exception.
	 */
	@Test
	public void testVertexIterableNoChange() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Iterable<Vertex> vertices = motorwayMap.vertices();
			assertTrue(vertices instanceof AttributedElementIterable);
			for (Vertex vertex : vertices) {
				vertex.getId();
			}
		} catch (Exception e) {
			fail();
		}
	}

	/**
	 * Iterate through Vseq and deleting the last vertex. Exception expected.
	 */
	@Test
	public void testVertexIterableChange() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Iterable<Vertex> vertices = motorwayMap.vertices();
			assertTrue(vertices instanceof AttributedElementIterable);
			for (Vertex vertex : vertices) {
				motorwayMap.getVertex(N).delete();
				vertex.getId();
			}
			fail();
		} catch (ConcurrentModificationException e) {
			System.out.println("\n- testVertexIterableChange -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * Iterable-instance created within <code>readWriteTransaction1</code> is
	 * used within <code>readWriteTransaction2</code>. Because
	 * <code>readWriteTransaction2</code> is an invalid transaction for the
	 * Iterable-instance, an exception should be thrown.
	 */
	@Test
	public void testVertexIterableInvalidTransaction() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Iterable<Vertex> vertices = motorwayMap.vertices();
			assertTrue(vertices instanceof AttributedElementIterable);
			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			for (Vertex vertex : vertices) {
				fail();
				vertex.delete();
			}
		} catch (GraphException e) {
			System.out.println("\n- testVertexIterableInvalidTransaction -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * Iterate through Eseq without changing E. Should work without exception.
	 */
	@Test
	public void testEdgeIterableNoChange() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Iterable<Edge> edges = motorwayMap.edges();
			assertTrue(edges instanceof AttributedElementIterable);
			for (Edge edge : edges) {
				edge.getId();
			}
		} catch (Exception e) {
			fail();
		}
	}

	/**
	 * Iterate through Eseq and deleting the last edge. Exception expected.
	 */
	@Test
	public void testEdgeIterableChange() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Iterable<Edge> edges = motorwayMap.edges();
			assertTrue(edges instanceof AttributedElementIterable);
			for (Edge edge : edges) {
				motorwayMap.getEdge(N).delete();
				edge.getId();
			}
			fail();
		} catch (ConcurrentModificationException e) {
			System.out.println("\n- testEdgeIterableChange -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * Iterable-instance created within <code>readWriteTransaction1</code> is
	 * used within <code>readWriteTransaction2</code>. Because
	 * <code>readWriteTransaction2</code> is an invalid transaction for the
	 * Iterable-instance, an exception should be thrown.
	 */
	@Test
	public void testEdgeIterableInvalidTransaction() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Iterable<Edge> edges = motorwayMap.edges();
			assertTrue(edges instanceof AttributedElementIterable);
			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			for (Edge edge : edges) {
				fail();
				edge.delete();
			}
			fail();
		} catch (GraphException e) {
			System.out.println("\n- testEdgeIterableInvalidTransaction -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * Iterate through Iseq(v1) without changing E. Should work without
	 * exception.
	 */
	@Test
	public void testIncidenceIterableNoChange() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Iterable<Edge> incidences = motorwayMap.getVertex(1).incidences();
			assertTrue(incidences instanceof AttributedElementIterable);
			for (Edge edge : incidences) {
				edge.getId();
			}
		} catch (Exception e) {
			fail();
		}
	}

	/**
	 * Iterate through Iseq(v1) and deleting the last incidence. Exception
	 * expected.
	 */
	@Test
	public void testIncidenceIterableChange() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Vertex v1 = motorwayMap.getVertex(1);
			Iterable<Edge> incidences = v1.incidences();
			assertTrue(incidences instanceof AttributedElementIterable);
			for (Edge edge : incidences) {
				v1.getLastEdge().delete();
				edge.getId();
			}
			fail();
		} catch (ConcurrentModificationException e) {
			System.out.println("\n- testIncidenceIterableChange -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * Iterable-instance created within <code>readWriteTransaction1</code> is
	 * used within <code>readWriteTransaction2</code>. Because
	 * <code>readWriteTransaction2</code> is an invalid transaction for the
	 * Iterable-instance, an exception should be thrown.
	 */
	@Test
	public void testIncidenceIterableInvalidTransaction() {
		try {
			motorwayMap.setCurrentTransaction(readWriteTransaction1);
			Vertex v1 = motorwayMap.getVertex(1);
			Iterable<Edge> incidences = v1.incidences();
			motorwayMap.setCurrentTransaction(readWriteTransaction2);
			assertTrue(incidences instanceof AttributedElementIterable);
			for (Edge edge : incidences) {
				fail();
				edge.delete();
			}
			fail();
		} catch (GraphException e) {
			System.out.println("\n- testIncidenceIterableInvalidTransaction -");
			System.out.println("##########################");
			System.out.println(e.getMessage());
			assertTrue(true);
		}
	}

	/**
	 * 
	 * @return
	 */
	/*
	 * public static junit.framework.Test suite() { return new
	 * JUnit4TestAdapter(AttributedElementIterableTest.class); }
	 */
}
