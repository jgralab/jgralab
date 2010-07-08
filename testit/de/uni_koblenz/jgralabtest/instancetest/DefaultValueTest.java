package de.uni_koblenz.jgralabtest.instancetest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.ListDomain;
import de.uni_koblenz.jgralab.schema.MapDomain;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.SetDomain;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;
import de.uni_koblenz.jgralab.trans.CommitFailedException;
import de.uni_koblenz.jgralabtest.schemas.defaultvaluetestschema.DefaultValueTestGraph;
import de.uni_koblenz.jgralabtest.schemas.defaultvaluetestschema.DefaultValueTestSchema;
import de.uni_koblenz.jgralabtest.schemas.defaultvaluetestschema.TestEdge;
import de.uni_koblenz.jgralabtest.schemas.defaultvaluetestschema.TestEnumDomain;
import de.uni_koblenz.jgralabtest.schemas.defaultvaluetestschema.TestRecordDomain;
import de.uni_koblenz.jgralabtest.schemas.defaultvaluetestschema.TestSubVertex;
import de.uni_koblenz.jgralabtest.schemas.defaultvaluetestschema.TestVertex;

@RunWith(Parameterized.class)
public class DefaultValueTest extends InstanceTest {

	public DefaultValueTest(ImplementationType implementationType) {
		super(implementationType);
	}

	@Parameters
	public static Collection<Object[]> configure() {
		return getParameters();
	}

	private DefaultValueTestGraph graph;

	@Before
	public void setUp() {
		switch (implementationType) {
		case STANDARD:
			graph = DefaultValueTestSchema.instance()
					.createDefaultValueTestGraph();
			break;
		case TRANSACTION:
			graph = DefaultValueTestSchema.instance()
					.createDefaultValueTestGraphWithTransactionSupport();
			break;
		case SAVEMEM:
			graph = DefaultValueTestSchema.instance()
					.createDefaultValueTestGraphWithSavememSupport();
			break;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}
	}

	/**
	 * Test if the defaultValues of the graph attributes are set.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void testGraphAttributes() throws CommitFailedException {
		createReadOnlyTransaction(graph);
		checkAttributes(graph.is_boolGraph(), graph.get_intGraph(), graph
				.get_longGraph(), graph.get_doubleGraph(), graph
				.get_stringGraph(), graph.get_enumGraph(), graph
				.get_listGraph(), graph.get_complexListGraph(), graph
				.get_setGraph(), graph.get_complexSetGraph(), graph
				.get_mapGraph(), graph.get_complexMapGraph(), graph
				.get_recordGraph());
		commit(graph);
	}

	/**
	 * Test if the defaultValues of the vertex attributes are set and differ
	 * from graph.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void testVertexAttributes() throws CommitFailedException {
		createTransaction(graph);
		TestVertex v = graph.createTestVertex();
		commit(graph);

		createReadOnlyTransaction(graph);
		checkAttributes(v.is_boolVertex(), v.get_intVertex(), v
				.get_longVertex(), v.get_doubleVertex(), v.get_stringVertex(),
				v.get_enumVertex(), v.get_listVertex(), v
						.get_complexListVertex(), v.get_setVertex(), v
						.get_complexSetVertex(), v.get_mapVertex(), v
						.get_complexMapVertex(), v.get_recordVertex());
		checkNotEqual(graph.get_listGraph(), v.get_listVertex(), graph
				.get_complexListGraph(), v.get_complexListVertex(), graph
				.get_setGraph(), v.get_setVertex(),
				graph.get_complexSetGraph(), v.get_complexSetVertex(), graph
						.get_mapGraph(), v.get_mapVertex(), graph
						.get_complexMapGraph(), v.get_complexMapVertex(), graph
						.get_recordGraph(), v.get_recordVertex());
		commit(graph);
	}

	/**
	 * Test if the defaultValues of the attributes for an vertex of an inherited
	 * VertexClass are set and differ from graph.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void testInheritedAttributes() throws CommitFailedException {
		createTransaction(graph);
		TestSubVertex v = graph.createTestSubVertex();
		commit(graph);

		createReadOnlyTransaction(graph);
		checkAttributes(v.is_boolVertex(), v.get_intVertex(), v
				.get_longVertex(), v.get_doubleVertex(), v.get_stringVertex(),
				v.get_enumVertex(), v.get_listVertex(), v
						.get_complexListVertex(), v.get_setVertex(), v
						.get_complexSetVertex(), v.get_mapVertex(), v
						.get_complexMapVertex(), v.get_recordVertex());
		checkNotEqual(graph.get_listGraph(), v.get_listVertex(), graph
				.get_complexListGraph(), v.get_complexListVertex(), graph
				.get_setGraph(), v.get_setVertex(),
				graph.get_complexSetGraph(), v.get_complexSetVertex(), graph
						.get_mapGraph(), v.get_mapVertex(), graph
						.get_complexMapGraph(), v.get_complexMapVertex(), graph
						.get_recordGraph(), v.get_recordVertex());
		commit(graph);
	}

	/**
	 * Test if the defaultValues of the edge attributes are set and differ from
	 * graph and vertex.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void testEdgeAttributes() throws CommitFailedException {
		createTransaction(graph);
		TestVertex v = graph.createTestVertex();
		TestEdge e = graph.createTestEdge(v, v);
		commit(graph);
		createReadOnlyTransaction(graph);
		checkAttributes(e.is_boolEdge(), e.get_intEdge(), e.get_longEdge(), e
				.get_doubleEdge(), e.get_stringEdge(), e.get_enumEdge(), e
				.get_listEdge(), e.get_complexListEdge(), e.get_setEdge(), e
				.get_complexSetEdge(), e.get_mapEdge(), e.get_complexMapEdge(),
				e.get_recordEdge());
		checkNotEqual(graph.get_listGraph(), e.get_listEdge(), graph
				.get_complexListGraph(), e.get_complexListEdge(), graph
				.get_setGraph(), e.get_setEdge(), graph.get_complexSetGraph(),
				e.get_complexSetEdge(), graph.get_mapGraph(), e.get_mapEdge(),
				graph.get_complexMapGraph(), e.get_complexMapEdge(), graph
						.get_recordGraph(), e.get_recordEdge());
		checkNotEqual(e.get_listEdge(), v.get_listVertex(), e
				.get_complexListEdge(), v.get_complexListVertex(), e
				.get_setEdge(), v.get_setVertex(), e.get_complexSetEdge(), v
				.get_complexSetVertex(), e.get_mapEdge(), v.get_mapVertex(), e
				.get_complexMapEdge(), v.get_complexMapVertex(), e
				.get_recordEdge(), v.get_recordVertex());
		commit(graph);
	}

	/**
	 * Test if the defaultValues of the attributes of two graphs are cloned.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void testGraphAttributesAreCloned() throws CommitFailedException {
		DefaultValueTestGraph secondGraph = null;
		switch (implementationType) {
		case STANDARD:
			secondGraph = DefaultValueTestSchema.instance()
					.createDefaultValueTestGraph();
			break;
		case TRANSACTION:
			secondGraph = DefaultValueTestSchema.instance()
					.createDefaultValueTestGraphWithTransactionSupport();
			break;
		case SAVEMEM:
			secondGraph = DefaultValueTestSchema.instance()
					.createDefaultValueTestGraphWithSavememSupport();
			break;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}
		createReadOnlyTransaction(graph);
		createReadOnlyTransaction(secondGraph);
		checkNotEqual(secondGraph.get_listGraph(), graph.get_listGraph(),
				secondGraph.get_complexListGraph(), graph
						.get_complexListGraph(), secondGraph.get_setGraph(),
				graph.get_setGraph(), secondGraph.get_complexSetGraph(), graph
						.get_complexSetGraph(), secondGraph.get_mapGraph(),
				graph.get_mapGraph(), secondGraph.get_complexMapGraph(), graph
						.get_complexMapGraph(), secondGraph.get_recordGraph(),
				graph.get_recordGraph());
		commit(secondGraph);
		commit(graph);
	}

	/**
	 * Test if the defaultValues of the attributes of two vertices of same type
	 * are cloned.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void testVertexAttributesAreCloned() throws CommitFailedException {
		createTransaction(graph);
		TestVertex v1 = graph.createTestVertex();
		TestVertex v2 = graph.createTestVertex();
		commit(graph);
		createReadOnlyTransaction(graph);
		checkNotEqual(v1.get_listVertex(), v2.get_listVertex(), v1
				.get_complexListVertex(), v2.get_complexListVertex(), v1
				.get_setVertex(), v2.get_setVertex(),
				v1.get_complexSetVertex(), v2.get_complexSetVertex(), v1
						.get_mapVertex(), v2.get_mapVertex(), v1
						.get_complexMapVertex(), v2.get_complexMapVertex(), v1
						.get_recordVertex(), v2.get_recordVertex());
		commit(graph);
	}

	/**
	 * Test if the defaultValues of the attributes of two vertices of inherited
	 * types are cloned.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void testInheritedVertexAttributesAreCloned()
			throws CommitFailedException {
		createTransaction(graph);
		TestVertex v1 = graph.createTestVertex();
		TestSubVertex v2 = graph.createTestSubVertex();
		commit(graph);
		createReadOnlyTransaction(graph);
		checkNotEqual(v1.get_listVertex(), v2.get_listVertex(), v1
				.get_complexListVertex(), v2.get_complexListVertex(), v1
				.get_setVertex(), v2.get_setVertex(),
				v1.get_complexSetVertex(), v2.get_complexSetVertex(), v1
						.get_mapVertex(), v2.get_mapVertex(), v1
						.get_complexMapVertex(), v2.get_complexMapVertex(), v1
						.get_recordVertex(), v2.get_recordVertex());
		commit(graph);
	}

	/**
	 * Test if the defaultValues of the attributes of two edges of same type are
	 * cloned.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void testEdgeAttributesAreCloned() throws CommitFailedException {
		createTransaction(graph);
		TestVertex v = graph.createTestVertex();
		TestEdge e1 = graph.createTestEdge(v, v);
		TestEdge e2 = graph.createTestEdge(v, v);
		commit(graph);
		createReadOnlyTransaction(graph);
		checkNotEqual(e1.get_listEdge(), e2.get_listEdge(), e1
				.get_complexListEdge(), e2.get_complexListEdge(), e1
				.get_setEdge(), e2.get_setEdge(), e1.get_complexSetEdge(), e2
				.get_complexSetEdge(), e1.get_mapEdge(), e2.get_mapEdge(), e1
				.get_complexMapEdge(), e2.get_complexMapEdge(), e1
				.get_recordEdge(), e2.get_recordEdge());
		commit(graph);
	}

	/**
	 * Checks if the parameters have their defaultValues.
	 * 
	 * @param booleanValue
	 * @param intValue
	 * @param longValue
	 * @param doubleValue
	 * @param stringValue
	 * @param enumValue
	 * @param simpleList
	 * @param complexList
	 * @param simpleSet
	 * @param complexSet
	 * @param simpleMap
	 * @param complexMap
	 * @param record
	 */
	private void checkAttributes(boolean booleanValue, int intValue,
			long longValue, double doubleValue, String stringValue,
			TestEnumDomain enumValue, List<Boolean> simpleList,
			List<List<Boolean>> complexList, Set<Boolean> simpleSet,
			Set<Set<Boolean>> complexSet, Map<Integer, Boolean> simpleMap,
			Map<List<Boolean>, Set<Boolean>> complexMap, TestRecordDomain record) {
		assertEquals(true, booleanValue);
		assertEquals(1, intValue);
		assertEquals(1, longValue);
		assertEquals(1.1, doubleValue, 0);
		assertEquals("test", stringValue);
		assertEquals(TestEnumDomain.FIRST, enumValue);

		assertEquals(3, simpleList.size());
		assertEquals(true, simpleList.get(0));
		assertEquals(false, simpleList.get(1));
		assertEquals(true, simpleList.get(2));

		if (complexList != null) {
			assertEquals(3, complexList.size());
			List<Boolean> contentOfComplexList = complexList.get(0);
			assertNotSame(contentOfComplexList, complexList.get(1));
			assertNotSame(contentOfComplexList, complexList.get(2));
			assertEquals(1, contentOfComplexList.size());
			assertEquals(true, contentOfComplexList.get(0));
			contentOfComplexList = complexList.get(1);
			assertNotSame(contentOfComplexList, complexList.get(0));
			assertNotSame(contentOfComplexList, complexList.get(2));
			assertEquals(1, contentOfComplexList.size());
			assertEquals(false, contentOfComplexList.get(0));
			contentOfComplexList = complexList.get(2);
			assertNotSame(contentOfComplexList, complexList.get(0));
			assertNotSame(contentOfComplexList, complexList.get(1));
			assertEquals(1, contentOfComplexList.size());
			assertEquals(true, contentOfComplexList.get(0));
		}

		assertEquals(2, simpleSet.size());
		assertTrue(simpleSet.contains(Boolean.TRUE));
		assertTrue(simpleSet.contains(Boolean.FALSE));

		if (complexSet != null) {
			assertEquals(2, complexSet.size());
			boolean foundFirst = false, foundSecond = false;
			for (Set<Boolean> element : complexSet) {
				assertEquals(1, element.size());
				foundFirst = foundFirst || element.contains(Boolean.TRUE);
				foundSecond = foundSecond || element.contains(Boolean.FALSE);
			}
			assertTrue(foundFirst);
			assertTrue(foundSecond);
		}

		assertEquals(3, simpleMap.size());
		for (Entry<Integer, Boolean> entry : simpleMap.entrySet()) {
			if (entry.getKey() == 1) {
				assertTrue(entry.getValue());
			} else if (entry.getKey() == 2) {
				assertFalse(entry.getValue());
			} else if (entry.getKey() == 3) {
				assertTrue(entry.getValue());
			} else {
				fail();
			}
		}

		if (complexMap != null) {
			assertEquals(2, complexMap.size());
			for (Entry<List<Boolean>, Set<Boolean>> entry : complexMap
					.entrySet()) {
				assertEquals(1, entry.getKey().size());
				assertEquals(1, entry.getValue().size());
				assertTrue(entry.getValue().contains(entry.getKey().get(0)));
			}
		}

		if (record != null) {
			checkAttributes(record.is_boolRecord(), record.get_intRecord(),
					record.get_longRecord(), record.get_doubleRecord(), record
							.get_stringRecord(), record.get_enumRecord(),
					record.get_listRecord(), null, record.get_setRecord(),
					null, record.get_mapRecord(), null, null);
		}
	}

	/**
	 * Checks if param1 is not the same as param2.
	 * 
	 * @param list1
	 * @param list2
	 * @param complexList1
	 * @param complexList2
	 * @param set1
	 * @param set2
	 * @param complexSet1
	 * @param complexSet2
	 * @param map1
	 * @param map2
	 * @param complexMap1
	 * @param complexMap2
	 * @param record1
	 * @param record2
	 */
	private void checkNotEqual(List<Boolean> list1, List<Boolean> list2,
			List<List<Boolean>> complexList1, List<List<Boolean>> complexList2,
			Set<Boolean> set1, Set<Boolean> set2,
			Set<Set<Boolean>> complexSet1, Set<Set<Boolean>> complexSet2,
			Map<Integer, Boolean> map1, Map<Integer, Boolean> map2,
			Map<List<Boolean>, Set<Boolean>> complexMap1,
			Map<List<Boolean>, Set<Boolean>> complexMap2,
			TestRecordDomain record1, TestRecordDomain record2) {

		// list
		checkListNotSame(list1, list2);

		// complexList
		checkComplexListNotSame(complexList1, complexList2);

		// set
		checkSetNotSame(set1, set2);

		// complexSet
		checkComplexSetNotSame(complexSet1, complexSet2);

		// map
		checkMapNotSame(map1, map2);

		// complexMap
		checkComplexMapNotSame(complexMap1, complexMap2);

		// record
		checkRecordNotSame(record1, record2);
	}

	private void checkRecordNotSame(TestRecordDomain record1,
			TestRecordDomain record2) {
		if (record1 != null || record2 != null) {
			assertNotSame(record1, record2);
			// list
			checkListNotSame(record1.get_listRecord(), record2.get_listRecord());

			// set
			checkSetNotSame(record1.get_setRecord(), record2.get_setRecord());

			// map
			checkMapNotSame(record1.get_mapRecord(), record2.get_mapRecord());
		}
	}

	private void checkComplexMapNotSame(
			Map<List<Boolean>, Set<Boolean>> complexMap1,
			Map<List<Boolean>, Set<Boolean>> complexMap2) {
		if (complexMap1 != null) {
			assertNotSame(complexMap1, complexMap2);
			Set<List<Boolean>> keySet1 = complexMap1.keySet();
			Collection<Set<Boolean>> valueSet1 = complexMap1.values();
			Set<List<Boolean>> keySet2 = complexMap2.keySet();
			Collection<Set<Boolean>> valueSet2 = complexMap2.values();
			for (List<Boolean> list : keySet1) {
				notContainsTheSame(list, keySet2);
				notContainsTheSame(complexMap1.get(list), valueSet2);
			}
			for (List<Boolean> list : keySet2) {
				notContainsTheSame(list, keySet1);
				notContainsTheSame(complexMap2.get(list), valueSet1);
			}
		}
	}

	private void checkMapNotSame(Map<Integer, Boolean> map1,
			Map<Integer, Boolean> map2) {
		assertNotSame(map1, map2);
	}

	private void checkComplexSetNotSame(Set<Set<Boolean>> complexSet1,
			Set<Set<Boolean>> complexSet2) {
		if (complexSet1 != null) {
			assertNotSame(complexSet1, complexSet2);
			for (Set<Boolean> set : complexSet1) {
				notContainsTheSame(set, complexSet2);
			}
			for (Set<Boolean> set : complexSet2) {
				notContainsTheSame(set, complexSet1);
			}
		}
	}

	private void checkSetNotSame(Set<Boolean> set1, Set<Boolean> set2) {
		assertNotSame(set1, set2);
	}

	private void checkComplexListNotSame(List<List<Boolean>> complexList1,
			List<List<Boolean>> complexList2) {
		if (complexList1 != null) {
			assertNotSame(complexList1, complexList2);
			for (List<Boolean> list : complexList1) {
				notContainsTheSame(list, complexList2);
			}
			for (List<Boolean> list : complexList2) {
				notContainsTheSame(list, complexList1);
			}
		}
	}

	private void checkListNotSame(List<Boolean> list1, List<Boolean> list2) {
		assertNotSame(list1, list2);
	}

	/**
	 * Checks if forall x in iter: elem!=x.
	 * 
	 * @param <T>
	 * @param elem
	 * @param iter
	 */
	private <T> void notContainsTheSame(T elem, Iterable<T> iter) {
		for (T e : iter) {
			assertNotSame(e, elem);
		}
	}

	public static void main(String[] args) throws GraphIOException {
		Schema schema = new SchemaImpl("DefaultValueTestSchema",
				"de.uni_koblenz.jgralabtest.schemas.defaultvaluetestschema");

		EnumDomain enumDomain = schema.createEnumDomain("TestEnumDomain");
		enumDomain.addConst("FIRST");
		enumDomain.addConst("SECOND");
		enumDomain.addConst("THIRD");

		ListDomain simpleListDomain = schema.createListDomain(schema
				.getBooleanDomain());
		ListDomain complexListDomain = schema
				.createListDomain(simpleListDomain);

		SetDomain simpleSetDomain = schema.createSetDomain(schema
				.getBooleanDomain());
		SetDomain complexSetDomain = schema.createSetDomain(simpleSetDomain);

		MapDomain simpleMapDomain = schema.createMapDomain(schema
				.getIntegerDomain(), schema.getBooleanDomain());
		MapDomain complexMapDomain = schema.createMapDomain(simpleListDomain,
				simpleSetDomain);

		RecordDomain recordDomain = schema
				.createRecordDomain("TestRecordDomain");
		recordDomain.addComponent("boolRecord", schema.getBooleanDomain());
		recordDomain.addComponent("doubleRecord", schema.getDoubleDomain());
		recordDomain.addComponent("enumRecord", enumDomain);
		recordDomain.addComponent("intRecord", schema.getIntegerDomain());
		recordDomain.addComponent("listRecord", simpleListDomain);
		recordDomain.addComponent("longRecord", schema.getLongDomain());
		recordDomain.addComponent("mapRecord", simpleMapDomain);
		recordDomain.addComponent("setRecord", simpleSetDomain);
		recordDomain.addComponent("stringRecord", schema.getStringDomain());

		GraphClass graphClass = schema
				.createGraphClass("DefaultValueTestGraph");
		graphClass.addAttribute("boolGraph", schema.getBooleanDomain(), "t");
		graphClass.addAttribute("intGraph", schema.getIntegerDomain(), "1");
		graphClass.addAttribute("doubleGraph", schema.getDoubleDomain(), "1.1");
		graphClass.addAttribute("longGraph", schema.getLongDomain(), "1");
		graphClass.addAttribute("stringGraph", schema.getStringDomain(),
				"\"test\"");
		graphClass.addAttribute("enumGraph", enumDomain, "FIRST");
		graphClass.addAttribute("listGraph", simpleListDomain, "[t f t]");
		graphClass.addAttribute("complexListGraph", complexListDomain,
				"[[t] [f] [t]]");
		graphClass.addAttribute("setGraph", simpleSetDomain, "{t f}");
		graphClass.addAttribute("complexSetGraph", complexSetDomain,
				"{{t} {f}}");
		graphClass.addAttribute("mapGraph", simpleMapDomain,
				"{1 - t 2 - f 3 - t}");
		graphClass.addAttribute("complexMapGraph", complexMapDomain,
				"{[t] - {t} [f] - {f}}");
		graphClass.addAttribute("recordGraph", recordDomain,
				"(t 1.1 FIRST 1 [t f t] 1 {1 - t 2 - f 3 - t} {t f} \"test\")");

		VertexClass vertexClass = graphClass.createVertexClass("TestVertex");
		vertexClass.addAttribute("boolVertex", schema.getBooleanDomain(), "t");
		vertexClass.addAttribute("intVertex", schema.getIntegerDomain(), "1");
		vertexClass.addAttribute("doubleVertex", schema.getDoubleDomain(),
				"1.1");
		vertexClass.addAttribute("longVertex", schema.getLongDomain(), "1");
		vertexClass.addAttribute("stringVertex", schema.getStringDomain(),
				"\"test\"");
		vertexClass.addAttribute("enumVertex", enumDomain, "FIRST");
		vertexClass.addAttribute("listVertex", simpleListDomain, "[t f t]");
		vertexClass.addAttribute("complexListVertex", complexListDomain,
				"[[t] [f] [t]]");
		vertexClass.addAttribute("setVertex", simpleSetDomain, "{t f}");
		vertexClass.addAttribute("complexSetVertex", complexSetDomain,
				"{{t} {f}}");
		vertexClass.addAttribute("mapVertex", simpleMapDomain,
				"{1 - t 2 - f 3 - t}");
		vertexClass.addAttribute("complexMapVertex", complexMapDomain,
				"{[t] - {t} [f] - {f}}");
		vertexClass.addAttribute("recordVertex", recordDomain,
				"(t 1.1 FIRST 1 [t f t] 1 {1 - t 2 - f 3 - t} {t f} \"test\")");

		VertexClass subVertexClass = graphClass
				.createVertexClass("TestSubVertex");
		subVertexClass.addSuperClass(vertexClass);

		EdgeClass edgeClass = graphClass.createEdgeClass("TestEdge",
				vertexClass, 0, Integer.MAX_VALUE, "start",
				AggregationKind.NONE, vertexClass, 0, Integer.MAX_VALUE, "end",
				AggregationKind.NONE);
		edgeClass.addAttribute("boolEdge", schema.getBooleanDomain(), "t");
		edgeClass.addAttribute("intEdge", schema.getIntegerDomain(), "1");
		edgeClass.addAttribute("doubleEdge", schema.getDoubleDomain(), "1.1");
		edgeClass.addAttribute("longEdge", schema.getLongDomain(), "1");
		edgeClass.addAttribute("stringEdge", schema.getStringDomain(),
				"\"test\"");
		edgeClass.addAttribute("enumEdge", enumDomain, "FIRST");
		edgeClass.addAttribute("listEdge", simpleListDomain, "[t f t]");
		edgeClass.addAttribute("complexListEdge", complexListDomain,
				"[[t] [f] [t]]");
		edgeClass.addAttribute("setEdge", simpleSetDomain, "{t f}");
		edgeClass.addAttribute("complexSetEdge", complexSetDomain, "{{t} {f}}");
		edgeClass.addAttribute("mapEdge", simpleMapDomain,
				"{1 - t 2 - f 3 - t}");
		edgeClass.addAttribute("complexMapEdge", complexMapDomain,
				"{[t] - {t} [f] - {f}}");
		edgeClass.addAttribute("recordEdge", recordDomain,
				"(t 1.1 FIRST 1 [t f t] 1 {1 - t 2 - f 3 - t} {t f} \"test\")");

		schema.commit(new CodeGeneratorConfiguration()
				.withoutTypeSpecificMethodSupport());
		GraphIO.saveSchemaToFile(
				"./testit/testschemas/DefaultValueTestSchema.tg", schema);
		GraphIO
				.loadSchemaFromFile("./testit/testschemas/DefaultValueTestSchema.tg");
	}
}
