package de.uni_koblenz.jgralabtest.greql.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.pcollections.POrderedSet;

import de.uni_koblenz.jgralab.Record;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.executable.ExecutableQuery;
import de.uni_koblenz.jgralab.greql.executable.GreqlCodeGenerator;
import de.uni_koblenz.jgralab.greql.types.Tuple;

public class CollectionEvaluatorTest {

	/*
	 * Tests of ListConstructionEvaluator
	 */

	public void checkList(Object erg, Object... content) {
		assertNotNull(erg);
		List<?> list = (List<?>) erg;
		assertEquals(content.length, list.size());
		for (int i = 0; i < content.length; i++) {
			assertEquals(content[i], list.get(i));
		}
	}

	/**
	 * Test of query:<br>
	 * list()
	 */
	@Test
	public void testListConstructionEvaluator_EmptyList()
			throws InstantiationException, IllegalAccessException {
		String query = "list()";
		Object erg = GreqlQuery.createQuery(query).evaluate();
		checkList(erg);
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestListConstructionEvaluatorEmptyList");
		erg = generatedQuery.newInstance().execute(null);
		checkList(erg);
	}

	/**
	 * Test of query:<br>
	 * list(3)
	 */
	@Test
	public void testListConstructionEvaluator_WithOneElement()
			throws InstantiationException, IllegalAccessException {
		String query = "list(3)";
		Object erg = GreqlQuery.createQuery(query).evaluate();
		checkList(erg, 3);
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestListConstructionEvaluatorWithOneElement");
		erg = generatedQuery.newInstance().execute(null);
		checkList(erg, 3);
	}

	/**
	 * Test of query:<br>
	 * list(3,4)
	 */
	@Test
	public void testListConstructionEvaluator_WithSeveralElements()
			throws InstantiationException, IllegalAccessException {
		String query = "list(3,4)";
		Object erg = GreqlQuery.createQuery(query).evaluate();
		checkList(erg, 3, 4);
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestListConstructionEvaluatorWithSeveralElements");
		erg = generatedQuery.newInstance().execute(null);
		checkList(erg, 3, 4);
	}

	/**
	 * Test of query:<br>
	 * list(3,"a")
	 */
	@Test
	public void testListConstructionEvaluator_WithSeveralElementsOfDifferentType()
			throws InstantiationException, IllegalAccessException {
		String query = "list(3,\"a\")";
		Object erg = GreqlQuery.createQuery(query).evaluate();
		checkList(erg, 3, "a");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestListConstructionEvaluatorWithSeveralElementsOfDifferentType");
		erg = generatedQuery.newInstance().execute(null);
		checkList(erg, 3, "a");
	}

	/*
	 * Tests of ListRangeConstructionEvaluator
	 */

	/**
	 * Test of query:<br>
	 * list(2..2)
	 */
	@Test
	public void testListConstructionEvaluator_WithRangeOfOneElem()
			throws InstantiationException, IllegalAccessException {
		String query = "list(2..2)";
		Object erg = GreqlQuery.createQuery(query).evaluate();
		checkList(erg, 2);
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestListConstructionEvaluatorWithRangeOfOneElem");
		erg = generatedQuery.newInstance().execute(null);
		checkList(erg, 2);
	}

	/**
	 * Test of query:<br>
	 * list(2..4)
	 */
	@Test
	public void testListConstructionEvaluator_WithIncreasingRange()
			throws InstantiationException, IllegalAccessException {
		String query = "list(2..4)";
		Object erg = GreqlQuery.createQuery(query).evaluate();
		checkList(erg, 2, 3, 4);
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestListConstructionEvaluatorWithIncreasingRange");
		erg = generatedQuery.newInstance().execute(null);
		checkList(erg, 2, 3, 4);
	}

	/**
	 * Test of query:<br>
	 * list(2..1)
	 */
	@Test
	public void testListConstructionEvaluator_WithDecreasingRange()
			throws InstantiationException, IllegalAccessException {
		String query = "list(2..1)";
		Object erg = GreqlQuery.createQuery(query).evaluate();
		checkList(erg, 2, 1);
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestListConstructionEvaluatorWithDecreasingRange");
		erg = generatedQuery.newInstance().execute(null);
		checkList(erg, 2, 1);
	}

	/*
	 * Tests of TupleConstructionEvaluator
	 */

	public void checkTuple(Object erg, Object... content) {
		assertNotNull(erg);
		Tuple tuple = (Tuple) erg;
		assertEquals(content.length, tuple.size());
		for (int i = 0; i < content.length; i++) {
			assertEquals(content[i], tuple.get(i));
		}
	}

	/**
	 * Test of query:<br>
	 * tup()
	 */
	@Test
	public void testTupleConstructionEvaluator_EmptyTuple()
			throws InstantiationException, IllegalAccessException {
		String query = "tup()";
		Object erg = GreqlQuery.createQuery(query).evaluate();
		checkTuple(erg);
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestTestTupleConstructionEvaluatorEmptyTuple");
		erg = generatedQuery.newInstance().execute(null);
		checkTuple(erg);
	}

	/**
	 * Test of query:<br>
	 * tup("a")
	 */
	@Test
	public void testTupleConstructionEvaluator_WithOneElem()
			throws InstantiationException, IllegalAccessException {
		String query = "tup(\"a\")";
		Object erg = GreqlQuery.createQuery(query).evaluate();
		checkTuple(erg, "a");
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestTupleConstructionEvaluatorWithOneElem");
		erg = generatedQuery.newInstance().execute(null);
		checkTuple(erg, "a");
	}

	/**
	 * Test of query:<br>
	 * tup("a",3)
	 */
	@Test
	public void testTupleConstructionEvaluator_WithSeveralElems()
			throws InstantiationException, IllegalAccessException {
		String query = "tup(\"a\",3)";
		Object erg = GreqlQuery.createQuery(query).evaluate();
		checkTuple(erg, "a", 3);
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestTupleConstructionEvaluatorWithSeveralElems");
		erg = generatedQuery.newInstance().execute(null);
		checkTuple(erg, "a", 3);
	}

	/*
	 * Tests of SetConstructionEvaluator
	 */

	public void checkSet(Object erg, Object... content) {
		assertNotNull(erg);
		POrderedSet<?> set = (POrderedSet<?>) erg;
		assertEquals(content.length, set.size());
		for (int i = 0; i < content.length; i++) {
			assertEquals(content[i], set.get(i));
		}
	}

	/**
	 * Test of query:<br>
	 * set()
	 */
	@Test
	public void testSetConstructionEvaluator_EmptySet()
			throws InstantiationException, IllegalAccessException {
		String query = "set()";
		Object erg = GreqlQuery.createQuery(query).evaluate();
		checkSet(erg);
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestSetConstructionEvaluatorEmptySet");
		erg = generatedQuery.newInstance().execute(null);
		checkSet(erg);
	}

	/**
	 * Test of query:<br>
	 * set(1)
	 */
	@Test
	public void testSetConstructionEvaluator_WithOneElement()
			throws InstantiationException, IllegalAccessException {
		String query = "set(1)";
		Object erg = GreqlQuery.createQuery(query).evaluate();
		checkSet(erg, 1);
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestSetConstructionEvaluatorWithOneElement");
		erg = generatedQuery.newInstance().execute(null);
		checkSet(erg, 1);
	}

	/**
	 * Test of query:<br>
	 * set(1,1)
	 */
	@Test
	public void testSetConstructionEvaluator_WithTwoEqualElems()
			throws InstantiationException, IllegalAccessException {
		String query = "set(1,1)";
		Object erg = GreqlQuery.createQuery(query).evaluate();
		checkSet(erg, 1);
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestSetConstructionEvaluatorWithTwoEqualElems");
		erg = generatedQuery.newInstance().execute(null);
		checkSet(erg, 1);
	}

	/**
	 * Test of query:<br>
	 * set(tup(1),tup(1))
	 */
	@Test
	public void testSetConstructionEvaluator_WithTwoEqualObjects()
			throws InstantiationException, IllegalAccessException {
		String query = "set(tup(1),tup(1))";
		Object erg = GreqlQuery.createQuery(query).evaluate();
		Tuple tup = (Tuple) GreqlQuery.createQuery("tup(1)").evaluate();
		checkSet(erg, tup);
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestSetConstructionEvaluatorWithTwoEqualObjects");
		erg = generatedQuery.newInstance().execute(null);
		checkSet(erg, tup);
	}

	/**
	 * Test of query:<br>
	 * set(1,2)
	 */
	@Test
	public void testSetConstructionEvaluator_WithSeveralElems()
			throws InstantiationException, IllegalAccessException {
		String query = "set(1,2)";
		Object erg = GreqlQuery.createQuery(query).evaluate();
		checkSet(erg, 1, 2);
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestSetConstructionEvaluatorWithSeveralElems");
		erg = generatedQuery.newInstance().execute(null);
		checkSet(erg, 1, 2);
	}

	/*
	 * Tests of MapConstructionEvaluator
	 */

	public void checkMap(Object erg, Object[] keys, Object[] values) {
		assertNotNull(erg);
		Map<?, ?> map = (Map<?, ?>) erg;
		assertEquals(keys.length, values.length);
		assertEquals(keys.length, map.size());
		for (int i = 0; i < keys.length; i++) {
			assertTrue(map.containsKey(keys[i]));
			assertTrue(map.containsValue(values[i]));
			assertEquals(values[i], map.get(keys[i]));
		}
	}

	/**
	 * Test of query:<br>
	 * map()
	 */
	@Test
	public void testMapConstructionEvaluator_EmptyMap()
			throws InstantiationException, IllegalAccessException {
		String query = "map()";
		Object erg = GreqlQuery.createQuery(query).evaluate();
		checkMap(erg, new Object[] {}, new Object[] {});
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestMapConstructionEvaluatorEmptyMap");
		erg = generatedQuery.newInstance().execute(null);
		checkMap(erg, new Object[] {}, new Object[] {});
	}

	/**
	 * Test of query:<br>
	 * map(1-&gt;2)
	 */
	@Test
	public void testMapConstructionEvaluator_MapWithOneEntry()
			throws InstantiationException, IllegalAccessException {
		String query = "map(1->2)";
		Object erg = GreqlQuery.createQuery(query).evaluate();
		checkMap(erg, new Object[] { 1 }, new Object[] { 2 });
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestMapConstructionEvaluatorMapWithOneEntry");
		erg = generatedQuery.newInstance().execute(null);
		checkMap(erg, new Object[] { 1 }, new Object[] { 2 });
	}

	/**
	 * Test of query:<br>
	 * map(1-&gt;2,2-&gt;"a")
	 */
	@Test
	public void testMapConstructionEvaluator_MapWithTwoEntries()
			throws InstantiationException, IllegalAccessException {
		String query = "map(1->2,2->\"a\")";
		Object erg = GreqlQuery.createQuery(query).evaluate();
		checkMap(erg, new Object[] { 1, 2 }, new Object[] { 2, "a" });
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestMapConstructionEvaluatorMapWithTwoEntries");
		erg = generatedQuery.newInstance().execute(null);
		checkMap(erg, new Object[] { 1, 2 }, new Object[] { 2, "a" });
	}

	/**
	 * Test of query:<br>
	 * map(1-&gt;2,1-&gt;"a")
	 */
	@Test
	public void testMapConstructionEvaluator_MapWithTwoEntriesWithSameKey()
			throws InstantiationException, IllegalAccessException {
		String query = "map(1->2,1->\"a\")";
		Object erg = GreqlQuery.createQuery(query).evaluate();
		checkMap(erg, new Object[] { 1 }, new Object[] { "a" });
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestMapConstructionEvaluatorMapWithTwoEntriesWithSameKey");
		erg = generatedQuery.newInstance().execute(null);
		checkMap(erg, new Object[] { 1 }, new Object[] { "a" });
	}

	/*
	 * Tests of RecordConstructionEvaluator
	 */

	public void checkRecord(Object erg, String[] componentNames, Object[] values) {
		assertNotNull(erg);
		Record rec = (Record) erg;
		assertEquals(componentNames.length, values.length);
		assertEquals(componentNames.length, rec.size());
		for (int i = 0; i < componentNames.length; i++) {
			assertEquals(values[i], rec.getComponent(componentNames[i]));
		}
	}

	/**
	 * Test of query:<br>
	 * rec(a:3)
	 */
	@Test
	public void testRecordConstructionEvaluator_WithOneEntry()
			throws InstantiationException, IllegalAccessException {
		String query = "rec(a:3)";
		Object erg = GreqlQuery.createQuery(query).evaluate();
		checkRecord(erg, new String[] { "a" }, new Object[] { 3 });
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestRecordConstructionEvaluatorWithOneEntry");
		erg = generatedQuery.newInstance().execute(null);
		checkRecord(erg, new String[] { "a" }, new Object[] { 3 });
	}

	/**
	 * Test of query:<br>
	 * rec(a:3,b:"a")
	 */
	@Test
	public void testRecordConstructionEvaluator_WithTwoEntries()
			throws InstantiationException, IllegalAccessException {
		String query = "rec(a:3,b:\"a\")";
		Object erg = GreqlQuery.createQuery(query).evaluate();
		checkRecord(erg, new String[] { "a", "b" }, new Object[] { 3, "a" });
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestRecordConstructionEvaluatorWithTwoEntries");
		erg = generatedQuery.newInstance().execute(null);
		checkRecord(erg, new String[] { "a", "b" }, new Object[] { 3, "a" });
	}

	/**
	 * Test of query:<br>
	 * rec(a:3,a:"a")
	 */
	@Test
	public void testRecordConstructionEvaluator_WithTwoEntriesWithSameId()
			throws InstantiationException, IllegalAccessException {
		String query = "rec(a:3,a:\"a\")";
		Object erg = GreqlQuery.createQuery(query).evaluate();
		checkRecord(erg, new String[] { "a" }, new Object[] { "a" });
		Class<ExecutableQuery> generatedQuery = GreqlCodeGenerator
				.generateCode(query, null,
						"testdata.TestRecordConstructionEvaluatorWithTwoEntriesWithSameId");
		erg = generatedQuery.newInstance().execute(null);
		checkRecord(erg, new String[] { "a" }, new Object[] { "a" });
	}
}
