package de.uni_koblenz.jgralabtest.schema;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;

public class NamedElementTests {

	@Test
	public void testUniqueName() {
		Schema s = new SchemaImpl("TestSchema", "de.testschema");

		GraphClass gc = s.createGraphClass("TestGraph");
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
}
