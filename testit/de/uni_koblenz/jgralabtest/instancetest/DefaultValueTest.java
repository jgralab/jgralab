/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2012 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         https://github.com/jgralab/jgralab
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 * 
 * Additional permission under GNU GPL version 3 section 7
 * 
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
package de.uni_koblenz.jgralabtest.instancetest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.pcollections.ArrayPMap;
import org.pcollections.PMap;
import org.pcollections.PSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.impl.db.GraphDatabaseException;
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

	public DefaultValueTest(ImplementationType implementationType, String dbURL) {
		super(implementationType, dbURL);
	}

	@Parameters
	public static Collection<Object[]> configure() {
		return getParameters();
	}

	private DefaultValueTestGraph graph;

	private DefaultValueTestGraph createDefaultValueTestGraphWithDatabaseSupport() {
		dbHandler.connectToDatabase();
		loadDefaultValueTestSchemaIntoGraphDatabase();
		return this
				.createDefaultValueTestGraphWithDatabaseSupport("DefaultValueTest");
	}

	private void loadDefaultValueTestSchemaIntoGraphDatabase() {
		try {
			if (!dbHandler.getGraphDatabase().contains(
					DefaultValueTestSchema.instance())) {
				dbHandler
						.loadTestSchemaIntoGraphDatabase(DefaultValueTestSchema
								.instance());
			}
		} catch (GraphDatabaseException e) {
			e.printStackTrace();
		}
	}

	private DefaultValueTestGraph createDefaultValueTestGraphWithDatabaseSupport(
			String id) {
		try {
			return DefaultValueTestSchema.instance()
					.createDefaultValueTestGraph(id,
							dbHandler.getGraphDatabase());
		} catch (Exception exception) {
			fail("Could not create test graph");
			return null;
		}
	}

	@Before
	public void setUp() {
		switch (implementationType) {
		case STANDARD:
			graph = DefaultValueTestSchema.instance()
					.createDefaultValueTestGraph(ImplementationType.STANDARD);
			break;
		case TRANSACTION:
			graph = DefaultValueTestSchema.instance()
					.createDefaultValueTestGraph(ImplementationType.TRANSACTION);
			break;
		case DATABASE:
			graph = createDefaultValueTestGraphWithDatabaseSupport();
			break;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}
	}

	@After
	public void tearDown() {
		if (implementationType == ImplementationType.DATABASE) {
			cleanAndCloseDatabase();
		}
	}

	private void cleanAndCloseDatabase() {
		dbHandler.clearAllTables();
		// dbHandler.cleanDatabaseOfTestGraph(graph);
		// dbHandler.cleanDatabaseOfTestGraph("secondGraph");
		// TODO
		// dbHandler.cleanDatabaseOfTestSchema(DefaultValueTestSchema.instance());
		dbHandler.closeGraphdatabase();
	}

	/**
	 * Test if the defaultValues of the graph attributes are set.
	 * 
	 * @throws CommitFailedException
	 */
	@Test
	public void testGraphAttributes() throws CommitFailedException {
		createReadOnlyTransaction(graph);
		checkAttributes(graph.is_boolGraph(), graph.get_intGraph(),
				graph.get_longGraph(), graph.get_doubleGraph(),
				graph.get_stringGraph(), graph.get_enumGraph(),
				graph.get_listGraph(), graph.get_complexListGraph(),
				graph.get_setGraph(), graph.get_complexSetGraph(),
				graph.get_mapGraph(), graph.get_complexMapGraph(),
				graph.get_recordGraph());
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
		checkAttributes(v.is_boolVertex(), v.get_intVertex(),
				v.get_longVertex(), v.get_doubleVertex(), v.get_stringVertex(),
				v.get_enumVertex(), v.get_listVertex(),
				v.get_complexListVertex(), v.get_setVertex(),
				v.get_complexSetVertex(), v.get_mapVertex(),
				v.get_complexMapVertex(), v.get_recordVertex());
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
		checkAttributes(v.is_boolVertex(), v.get_intVertex(),
				v.get_longVertex(), v.get_doubleVertex(), v.get_stringVertex(),
				v.get_enumVertex(), v.get_listVertex(),
				v.get_complexListVertex(), v.get_setVertex(),
				v.get_complexSetVertex(), v.get_mapVertex(),
				v.get_complexMapVertex(), v.get_recordVertex());
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
		checkAttributes(e.is_boolEdge(), e.get_intEdge(), e.get_longEdge(),
				e.get_doubleEdge(), e.get_stringEdge(), e.get_enumEdge(),
				e.get_listEdge(), e.get_complexListEdge(), e.get_setEdge(),
				e.get_complexSetEdge(), e.get_mapEdge(),
				e.get_complexMapEdge(), e.get_recordEdge());
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
		case TRANSACTION:
			secondGraph = DefaultValueTestSchema.instance()
					.createDefaultValueTestGraph(ImplementationType.TRANSACTION);
			break;
		case STANDARD:
		case DATABASE:
			// cloning not supported except in TRANSACTION implementation
			return;
		default:
			fail("Implementation " + implementationType
					+ " not yet supported by this test.");
		}
		createReadOnlyTransaction(graph);
		createReadOnlyTransaction(secondGraph);
		commit(secondGraph);
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
			TestEnumDomain enumValue, PVector<Boolean> simpleList,
			PVector<PVector<Boolean>> complexList, PSet<Boolean> simpleSet,
			PSet<PSet<Boolean>> complexSet, PMap<Integer, Boolean> simpleMap,
			PMap<PVector<Boolean>, PSet<Boolean>> complexMap,
			TestRecordDomain record) {
		assertTrue(booleanValue);
		assertEquals(1, intValue);
		assertEquals(1, longValue);
		assertEquals(1.1, doubleValue, 0);
		assertEquals("test", stringValue);
		assertEquals(TestEnumDomain.FIRST, enumValue);

		assertEquals(3, simpleList.size());
		assertTrue(simpleList.get(0));
		assertFalse(simpleList.get(1));
		assertTrue(simpleList.get(2));

		if (complexList != null) {
			assertEquals(3, complexList.size());
			List<Boolean> contentOfComplexList = complexList.get(0);
			assertEquals(1, contentOfComplexList.size());
			assertTrue(contentOfComplexList.get(0));
			contentOfComplexList = complexList.get(1);
			assertEquals(1, contentOfComplexList.size());
			assertFalse(contentOfComplexList.get(0));
			contentOfComplexList = complexList.get(2);
			assertEquals(1, contentOfComplexList.size());
			assertTrue(contentOfComplexList.get(0));
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
			for (SimpleImmutableEntry<PVector<Boolean>, PSet<Boolean>> entry : (ArrayPMap<PVector<Boolean>, PSet<Boolean>>) complexMap) {
				assertEquals(1, entry.getKey().size());
				assertEquals(1, entry.getValue().size());
				assertTrue(entry.getValue().contains(entry.getKey().get(0)));
			}
		}

		if (record != null) {
			checkAttributes(record.is_boolRecord(), record.get_intRecord(),
					record.get_longRecord(), record.get_doubleRecord(),
					record.get_stringRecord(), record.get_enumRecord(),
					record.get_listRecord(), null, record.get_setRecord(),
					null, record.get_mapRecord(), null, null);
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

		MapDomain simpleMapDomain = schema.createMapDomain(
				schema.getIntegerDomain(), schema.getBooleanDomain());
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
		GraphIO.saveSchemaToFile(schema,
				"./testit/testschemas/DefaultValueTestSchema.tg");
		GraphIO.loadSchemaFromFile("./testit/testschemas/DefaultValueTestSchema.tg");
	}
}
