package de.uni_koblenz.jgralabtest.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.EnumDomain;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.NamedElement;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.RecordDomain;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;

public class SchemaModificationTests {

	static GraphClass createSchemaWithGraphClass() {
		Schema s = new SchemaImpl("TestSchema", "de.testschema");
		GraphClass gc = s.createGraphClass("TestGraph");
		return gc;
	}

	@Test
	public void testUniqueName() {
		GraphClass gc = createSchemaWithGraphClass();
		VertexClass p1foo = gc.createVertexClass("p1.Foo");
		// Now, Foo is unique
		assertEquals("Foo", p1foo.getUniqueName());
		VertexClass p2foo = gc.createVertexClass("p2.Foo");
		// Now, they aren't anymore
		assertEquals("p1$Foo", p1foo.getUniqueName());
		assertEquals("p2$Foo", p2foo.getUniqueName());

		// Test the same for EdgeClasses
		EdgeClass p1bar = gc.createEdgeClass("p1.Bar", p1foo, 0, 1, "",
				AggregationKind.NONE, p2foo, 0, 1, "", AggregationKind.NONE);
		// Now, Bar is unique
		assertEquals("Bar", p1bar.getUniqueName());
		EdgeClass p1p3bar = gc.createEdgeClass("p1.p3.Bar", p1foo, 0, 1, "",
				AggregationKind.NONE, p2foo, 0, 1, "", AggregationKind.NONE);
		assertEquals("p1$Bar", p1bar.getUniqueName());
		assertEquals("p1$p3$Bar", p1p3bar.getUniqueName());
	}

	@Test
	public void testRenamingVC1() {
		// do a very basic rename
		GraphClass gc = createSchemaWithGraphClass();
		VertexClass vc = gc.createVertexClass("TestVC");
		vc.setQualifiedName("VCTest");
		assertEquals("VCTest", vc.getQualifiedName());
		assertEquals("VCTest", vc.getSimpleName());
		assertEquals("VCTest", vc.getUniqueName());
		assertEquals(gc.getSchema().getDefaultPackage(), vc.getPackage());
	}

	@Test
	public void testRenamingVC2() {
		// rename with implicit creation of a new package
		GraphClass gc = createSchemaWithGraphClass();
		VertexClass vc = gc.createVertexClass("TestVC");
		vc.setQualifiedName("p1.VCTest");
		assertEquals("p1.VCTest", vc.getQualifiedName());
		assertEquals("VCTest", vc.getSimpleName());
		assertEquals("VCTest", vc.getUniqueName());
		assertEquals(gc.getSchema().getPackage("p1"), vc.getPackage());
	}

	@Test(expected = SchemaException.class)
	public void testRenamingVC3() {
		// rename to existing VC must throw an exception
		GraphClass gc = createSchemaWithGraphClass();
		VertexClass vc = gc.createVertexClass("TestVC");
		gc.createVertexClass("p1.VCTest");
		vc.setQualifiedName("p1.VCTest");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testRenameBasicDomain() {
		// Basic domains cannot be renamed
		GraphClass gc = createSchemaWithGraphClass();
		gc.getSchema().getBooleanDomain().setQualifiedName("BOOL");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testRenameCollectionDomain() {
		// Collection domains cannot be renamed
		GraphClass gc = createSchemaWithGraphClass();
		gc.getSchema().createListDomain(gc.getSchema().getIntegerDomain())
				.setQualifiedName("ListOfInt");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testRenameMapDomain() {
		// Map domains cannot be renamed
		GraphClass gc = createSchemaWithGraphClass();
		gc.getSchema()
				.createMapDomain(gc.getSchema().getIntegerDomain(),
						gc.getSchema().getStringDomain())
				.setQualifiedName("MapFromIntToString");
	}

	@Test
	public void testRenameGraphClass() {
		GraphClass gc = createSchemaWithGraphClass();
		gc.setQualifiedName("FooBarGraph");
		assertEquals("FooBarGraph", gc.getQualifiedName());
		assertEquals("FooBarGraph", gc.getSimpleName());
		assertEquals("FooBarGraph", gc.getUniqueName());
	}

	@Test(expected = SchemaException.class)
	public void testRenameGraphClassToNonDefaultPkg() {
		GraphClass gc = createSchemaWithGraphClass();
		gc.setQualifiedName("p1.FooBarGraph");
	}

	@Test
	public void testRenamingRecordEnumDomain() {
		// do a basic rename
		GraphClass gc = createSchemaWithGraphClass();
		Schema s = gc.getSchema();
		RecordDomain rd = s.createRecordDomain("p.MyRec");

		rd.setQualifiedName("p2.MyRec");
		assertEquals("p2.MyRec", rd.getQualifiedName());
		assertEquals("MyRec", rd.getSimpleName());
		// the unique name of domains is always the qualified name
		assertEquals("p2.MyRec", rd.getUniqueName());

		EnumDomain ed = s.createEnumDomain("MyEnum");
		ed.setQualifiedName("p2.MyEnumeration");
		assertEquals("p2.MyEnumeration", ed.getQualifiedName());
		assertEquals("MyEnumeration", ed.getSimpleName());
		assertEquals("p2.MyEnumeration", ed.getUniqueName());
	}

	@Test
	public void testRenamingPackage() {
		GraphClass gc = createSchemaWithGraphClass();
		VertexClass foo = gc.createVertexClass("p1.Foo");
		VertexClass bar = gc.createVertexClass("p1.Bar");
		EdgeClass baz = gc.createEdgeClass("p1.Baz", foo, 0, 1, "",
				AggregationKind.NONE, bar, 0, 1, "", AggregationKind.NONE);

		Package p1 = gc.getSchema().getPackage("p1");
		assertNull(gc.getSchema().getPackage("p2"));
		p1.setQualifiedName("p2");
		assertEquals(p1, gc.getSchema().getPackage("p2"));
		for (NamedElement ne : new NamedElement[] { foo, bar, baz }) {
			assertEquals("p2." + ne.getSimpleName(), ne.getQualifiedName());
		}
		// now we have 2 packages
		assertNotNull(gc.getSchema().getPackage("p1"));
		assertNotNull(gc.getSchema().getPackage("p2"));
		// and p1 is empty
		assertEquals(0, gc.getSchema().getPackage("p1").getVertexClasses()
				.keySet().size()
				+ gc.getSchema().getPackage("p1").getEdgeClasses().keySet()
						.size());
		// and p2 contains 2 vertex classes and 1 edge class
		assertEquals(2, gc.getSchema().getPackage("p2").getVertexClasses()
				.keySet().size());
		assertEquals(1, gc.getSchema().getPackage("p2").getEdgeClasses()
				.keySet().size());
	}

	@Test(expected = SchemaException.class)
	public void testRenamingDefaultPackage() {
		// The default package must not be renamed
		GraphClass gc = createSchemaWithGraphClass();
		gc.getPackage().setQualifiedName("foo");
	}

	@Test
	public void testDeleteVertexClass() {
		GraphClass gc = createSchemaWithGraphClass();
		VertexClass vc1 = gc.createVertexClass("VC1");
		VertexClass vc2 = gc.createVertexClass("VC2");
		vc1.delete();

		assertFalse(gc.getSchema().knows("VC1"));
		// 1 or 2? Should getVertexClasses really return Vertex?
		assertEquals(2, gc.getVertexClasses().size());
		assertTrue(gc.getGraphElementClasses().contains(vc2));
		assertFalse(gc.getGraphElementClasses().contains(vc1));
	}
}
