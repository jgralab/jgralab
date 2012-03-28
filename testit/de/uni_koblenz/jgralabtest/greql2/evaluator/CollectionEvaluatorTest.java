package de.uni_koblenz.jgralabtest.greql2.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Record;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluatorImpl;
import de.uni_koblenz.jgralab.greql2.evaluator.QueryImpl;
import de.uni_koblenz.jgralab.greql2.types.Tuple;

public class CollectionEvaluatorTest {

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

	private Object evaluateQuery(String query) {
		return new GreqlEvaluatorImpl(new QueryImpl(query), datagraph,
				new HashMap<String, Object>()).getResult();
	}

	/*
	 * Tests of ListConstructionEvaluator
	 */

	/**
	 * Test of query:<br>
	 * list()
	 */
	@Test
	public void testListConstructionEvaluator_EmptyList() {
		Object erg = evaluateQuery("list()");
		assertNotNull(erg);
		List<?> list = (List<?>) erg;
		assertTrue(list.isEmpty());
	}

	/**
	 * Test of query:<br>
	 * list(3)
	 */
	@Test
	public void testListConstructionEvaluator_WithOneElement() {
		Object erg = evaluateQuery("list(3)");
		assertNotNull(erg);
		List<?> list = (List<?>) erg;
		assertFalse(list.isEmpty());
		assertEquals(1, list.size());
		assertEquals(3, list.get(0));
	}

	/**
	 * Test of query:<br>
	 * list(3,4)
	 */
	@Test
	public void testListConstructionEvaluator_WithSeveralElements() {
		Object erg = evaluateQuery("list(3,4)");
		assertNotNull(erg);
		List<?> list = (List<?>) erg;
		assertFalse(list.isEmpty());
		assertEquals(2, list.size());
		assertEquals(3, list.get(0));
		assertEquals(4, list.get(1));
	}

	/**
	 * Test of query:<br>
	 * list(3,"a")
	 */
	@Test
	public void testListConstructionEvaluator_WithSeveralElementsOfDifferentType() {
		Object erg = evaluateQuery("list(3,\"a\")");
		assertNotNull(erg);
		List<?> list = (List<?>) erg;
		assertFalse(list.isEmpty());
		assertEquals(2, list.size());
		assertEquals(3, list.get(0));
		assertEquals("a", list.get(1));
	}

	/*
	 * Tests of ListRangeConstructionEvaluator
	 */

	/**
	 * Test of query:<br>
	 * list(2..2)
	 */
	@Test
	public void testListConstructionEvaluator_WithRangeOfOneElem() {
		Object erg = evaluateQuery("list(2..2)");
		assertNotNull(erg);
		List<?> list = (List<?>) erg;
		assertFalse(list.isEmpty());
		assertEquals(1, list.size());
		assertEquals(2, list.get(0));
	}

	/**
	 * Test of query:<br>
	 * list(2..4)
	 */
	@Test
	public void testListConstructionEvaluator_WithIncreasingRange() {
		Object erg = evaluateQuery("list(2..4)");
		assertNotNull(erg);
		List<?> list = (List<?>) erg;
		assertFalse(list.isEmpty());
		assertEquals(3, list.size());
		assertEquals(2, list.get(0));
		assertEquals(3, list.get(1));
		assertEquals(4, list.get(2));
	}

	/**
	 * Test of query:<br>
	 * list(2..1)
	 */
	@Test
	public void testListConstructionEvaluator_WithDecreasingRange() {
		Object erg = evaluateQuery("list(2..1)");
		assertNotNull(erg);
		List<?> list = (List<?>) erg;
		assertFalse(list.isEmpty());
		assertEquals(2, list.size());
		assertEquals(2, list.get(0));
		assertEquals(1, list.get(1));
	}

	/*
	 * Tests of TupleConstructionEvaluator
	 */

	/**
	 * Test of query:<br>
	 * tup()
	 */
	@Test
	public void testTupleConstructionEvaluator_EmptyTuple() {
		Object erg = evaluateQuery("tup()");
		assertNotNull(erg);
		Tuple tuple = (Tuple) erg;
		assertTrue(tuple.isEmpty());
	}

	/**
	 * Test of query:<br>
	 * tup("a")
	 */
	@Test
	public void testTupleConstructionEvaluator_WithOneElem() {
		Object erg = evaluateQuery("tup(\"a\")");
		assertNotNull(erg);
		Tuple tuple = (Tuple) erg;
		assertFalse(tuple.isEmpty());
		assertEquals(1, tuple.size());
		assertEquals("a", tuple.get(0));
	}

	/**
	 * Test of query:<br>
	 * tup("a",3)
	 */
	@Test
	public void testTupleConstructionEvaluator_WithSeveralElems() {
		Object erg = evaluateQuery("tup(\"a\",3)");
		assertNotNull(erg);
		Tuple tuple = (Tuple) erg;
		assertFalse(tuple.isEmpty());
		assertEquals(2, tuple.size());
		assertEquals("a", tuple.get(0));
		assertEquals(3, tuple.get(1));
	}

	/*
	 * Tests of SetConstructionEvaluator
	 */

	/**
	 * Test of query:<br>
	 * set()
	 */
	@Test
	public void testSetConstructionEvaluator_EmptySet() {
		Object erg = evaluateQuery("set()");
		assertNotNull(erg);
		Set<?> set = (Set<?>) erg;
		assertTrue(set.isEmpty());
	}

	/**
	 * Test of query:<br>
	 * set(1)
	 */
	@Test
	public void testSetConstructionEvaluator_WithOneEleme() {
		Object erg = evaluateQuery("set(1)");
		assertNotNull(erg);
		Set<?> set = (Set<?>) erg;
		assertFalse(set.isEmpty());
		assertEquals(1, set.size());
		assertTrue(set.contains(1));
	}

	/**
	 * Test of query:<br>
	 * set(1,1)
	 */
	@Test
	public void testSetConstructionEvaluator_WithTwoEqualElems() {
		Object erg = evaluateQuery("set(1,1)");
		assertNotNull(erg);
		Set<?> set = (Set<?>) erg;
		assertFalse(set.isEmpty());
		assertEquals(1, set.size());
		assertTrue(set.contains(1));
	}

	/**
	 * Test of query:<br>
	 * set(tup(1),tup(1))
	 */
	@Test
	public void testSetConstructionEvaluator_WithTwoEqualObjects() {
		Object erg = evaluateQuery("set(tup(1),tup(1))");
		assertNotNull(erg);
		Set<?> set = (Set<?>) erg;
		assertFalse(set.isEmpty());
		assertEquals(1, set.size());
		Tuple tup = (Tuple) evaluateQuery("tup(1)");
		assertTrue(set.contains(tup));
	}

	/**
	 * Test of query:<br>
	 * set(1,2)
	 */
	@Test
	public void testSetConstructionEvaluator_WithSeveralElems() {
		Object erg = evaluateQuery("set(1,2)");
		assertNotNull(erg);
		Set<?> set = (Set<?>) erg;
		assertFalse(set.isEmpty());
		assertEquals(2, set.size());
		assertTrue(set.contains(1));
		assertTrue(set.contains(2));
	}

	/*
	 * Tests of MapConstructionEvaluator
	 */

	/**
	 * Test of query:<br>
	 * map()
	 */
	@Test
	public void testMapConstructionEvaluator_EmptyMap() {
		Object erg = evaluateQuery("map()");
		assertNotNull(erg);
		Map<?, ?> map = (Map<?, ?>) erg;
		assertTrue(map.isEmpty());
	}

	/**
	 * Test of query:<br>
	 * map(1-&gt;2)
	 */
	@Test
	public void testMapConstructionEvaluator_MapWithOneEntry() {
		Object erg = evaluateQuery("map(1->2)");
		assertNotNull(erg);
		Map<?, ?> map = (Map<?, ?>) erg;
		assertFalse(map.isEmpty());
		assertEquals(1, map.size());

		assertTrue(map.containsKey(1));
		assertTrue(map.containsValue(2));
		assertEquals(2, map.get(1));
	}

	/**
	 * Test of query:<br>
	 * map(1-&gt;2,2-&gt;"a")
	 */
	@Test
	public void testMapConstructionEvaluator_MapWithTwoEntries() {
		Object erg = evaluateQuery("map(1->2,2->\"a\")");
		assertNotNull(erg);
		Map<?, ?> map = (Map<?, ?>) erg;
		assertFalse(map.isEmpty());
		assertEquals(2, map.size());

		assertTrue(map.containsKey(1));
		assertTrue(map.containsValue(2));
		assertEquals(2, map.get(1));

		assertTrue(map.containsKey(2));
		assertTrue(map.containsValue("a"));
		assertEquals("a", map.get(2));
	}

	/**
	 * Test of query:<br>
	 * map(1-&gt;2,1-&gt;"a")
	 */
	@Test
	public void testMapConstructionEvaluator_MapWithTwoEntriesWithSameKey() {
		Object erg = evaluateQuery("map(1->2,1->\"a\")");
		assertNotNull(erg);
		Map<?, ?> map = (Map<?, ?>) erg;
		assertFalse(map.isEmpty());
		assertEquals(1, map.size());

		assertTrue(map.containsKey(1));
		assertTrue(map.containsValue("a"));
		assertEquals("a", map.get(1));
	}

	/*
	 * Tests of RecordConstructionEvaluator
	 */

	/**
	 * Test of query:<br>
	 * rec(a:3)
	 */
	@Test
	public void testRecordConstructionEvaluator_WithOneEntry() {
		Object erg = evaluateQuery("rec(a:3)");
		assertNotNull(erg);
		Record rec = (Record) erg;
		assertEquals(1, rec.size());
		assertEquals(3, rec.getComponent("a"));
	}

	/**
	 * Test of query:<br>
	 * rec(a:3,b:"a")
	 */
	@Test
	public void testRecordConstructionEvaluator_WithTwoEntries() {
		Object erg = evaluateQuery("rec(a:3,b:\"a\")");
		assertNotNull(erg);
		Record rec = (Record) erg;
		assertEquals(2, rec.size());
		assertEquals(3, rec.getComponent("a"));
		assertEquals("a", rec.getComponent("b"));
	}

	/**
	 * Test of query:<br>
	 * rec(a:3,a:"a")
	 */
	@Test
	public void testRecordConstructionEvaluator_WithTwoEntriesWithSameId() {
		Object erg = evaluateQuery("rec(a:3,a:\"a\")");
		assertNotNull(erg);
		Record rec = (Record) erg;
		assertEquals(1, rec.size());
		assertEquals("a", rec.getComponent("a"));
	}
}
