package de.uni_koblenz.jgralabtest.greql.types;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_koblenz.jgralab.greql.types.TypeCollection;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;

public class TypeCollectionTest {
	private static Schema schema;
	private static VertexClass va;
	private static VertexClass vb;
	private static VertexClass vc;
	private static VertexClass vd;
	private static EdgeClass ee;

	@BeforeClass
	public static void setUpClass() throws Exception {
		schema = new SchemaImpl("TestSchema",
				"de.uni_koblenz.jgralab.testit.greql");
		GraphClass gc = schema.createGraphClass("TestGraph");
		va = gc.createVertexClass("A");
		vb = gc.createVertexClass("B");
		vc = gc.createVertexClass("C");
		vd = gc.createVertexClass("D");

		ee = gc.createEdgeClass("E", va, 0, 1, "", AggregationKind.NONE, va, 0,
				1, "", AggregationKind.NONE);
		vb.addSuperClass(va);
		vc.addSuperClass(va);
		vd.addSuperClass(vb);
		schema.finish();
	}

	@Test
	public void testEmptyTc() {
		TypeCollection tc = TypeCollection.empty();
		assertTrue(tc.isEmpty());
		assertTrue(tc.acceptsType(va));
		assertTrue(tc.acceptsType(vb));
		assertTrue(tc.acceptsType(vc));
		assertTrue(tc.acceptsType(ee));
	}

	@Test
	public void testWith() {
		// {A}
		TypeCollection tc = TypeCollection.empty().with(va, false, false);
		assertTrue(tc.acceptsType(va));
		assertTrue(tc.acceptsType(vb));
		assertTrue(tc.acceptsType(vc));
		assertTrue(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {A!}
		tc = TypeCollection.empty().with(va, true, false);
		assertTrue(tc.acceptsType(va));
		assertFalse(tc.acceptsType(vb));
		assertFalse(tc.acceptsType(vc));
		assertFalse(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {^A}
		tc = TypeCollection.empty().with(va, false, true);
		assertFalse(tc.acceptsType(va));
		assertFalse(tc.acceptsType(vb));
		assertFalse(tc.acceptsType(vc));
		assertFalse(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {^A!}
		tc = TypeCollection.empty().with(va, true, true);
		assertFalse(tc.acceptsType(va));
		assertTrue(tc.acceptsType(vb));
		assertTrue(tc.acceptsType(vc));
		assertTrue(tc.acceptsType(vc));
		assertFalse(tc.acceptsType(ee));

		// {E}
		tc = TypeCollection.empty().with(ee, false, false);
		assertFalse(tc.acceptsType(va));
		assertFalse(tc.acceptsType(vb));
		assertFalse(tc.acceptsType(vc));
		assertFalse(tc.acceptsType(vd));
		assertTrue(tc.acceptsType(ee));

		// {A,E}
		tc = TypeCollection.empty().with(va, false, false)
				.with(ee, false, false);
		assertTrue(tc.acceptsType(va));
		assertTrue(tc.acceptsType(vb));
		assertTrue(tc.acceptsType(vc));
		assertTrue(tc.acceptsType(vd));
		assertTrue(tc.acceptsType(ee));

		// {A,B}
		tc = TypeCollection.empty().with(va, false, false)
				.with(vb, false, false);
		assertTrue(tc.acceptsType(va));
		assertTrue(tc.acceptsType(vb));
		assertTrue(tc.acceptsType(vc));
		assertTrue(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {A,B!}
		tc = TypeCollection.empty().with(va, false, false)
				.with(vb, true, false);
		assertTrue(tc.acceptsType(va));
		assertTrue(tc.acceptsType(vb));
		assertTrue(tc.acceptsType(vc));
		assertTrue(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {A,^B}
		tc = TypeCollection.empty().with(va, false, false)
				.with(vb, false, true);
		assertTrue(tc.acceptsType(va));
		assertFalse(tc.acceptsType(vb));
		assertTrue(tc.acceptsType(vc));
		assertFalse(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {A,^B!}
		tc = TypeCollection.empty().with(va, false, false).with(vb, true, true);
		assertTrue(tc.acceptsType(va));
		assertFalse(tc.acceptsType(vb));
		assertTrue(tc.acceptsType(vc));
		assertTrue(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {A!,B}
		tc = TypeCollection.empty().with(va, true, false)
				.with(vb, false, false);
		assertTrue(tc.acceptsType(va));
		assertTrue(tc.acceptsType(vb));
		assertFalse(tc.acceptsType(vc));
		assertTrue(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {A!,B!}
		tc = TypeCollection.empty().with(va, true, false).with(vb, true, false);
		assertTrue(tc.acceptsType(va));
		assertTrue(tc.acceptsType(vb));
		assertFalse(tc.acceptsType(vc));
		assertFalse(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {A!,^B}
		tc = TypeCollection.empty().with(va, true, false).with(vb, false, true);
		assertTrue(tc.acceptsType(va));
		assertFalse(tc.acceptsType(vb));
		assertFalse(tc.acceptsType(vc));
		assertFalse(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {A!,^B!}
		tc = TypeCollection.empty().with(va, true, false).with(vb, true, true);
		assertTrue(tc.acceptsType(va));
		assertFalse(tc.acceptsType(vb));
		assertFalse(tc.acceptsType(vc));
		assertFalse(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {^A,B}
		tc = TypeCollection.empty().with(va, false, true)
				.with(vb, false, false);
		assertFalse(tc.acceptsType(va));
		assertFalse(tc.acceptsType(vb));
		assertFalse(tc.acceptsType(vc));
		assertFalse(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {^A,B!}
		tc = TypeCollection.empty().with(va, false, true).with(vb, true, false);
		assertFalse(tc.acceptsType(va));
		assertFalse(tc.acceptsType(vb));
		assertFalse(tc.acceptsType(vc));
		assertFalse(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {^A,^B}
		tc = TypeCollection.empty().with(va, false, true).with(vb, false, true);
		assertFalse(tc.acceptsType(va));
		assertFalse(tc.acceptsType(vb));
		assertFalse(tc.acceptsType(vc));
		assertFalse(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {^A,^B!}
		tc = TypeCollection.empty().with(va, false, true).with(vb, true, true);
		assertFalse(tc.acceptsType(va));
		assertFalse(tc.acceptsType(vb));
		assertFalse(tc.acceptsType(vc));
		assertFalse(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {^A!,B}
		tc = TypeCollection.empty().with(va, true, true).with(vb, false, false);
		assertFalse(tc.acceptsType(va));
		assertTrue(tc.acceptsType(vb));
		assertFalse(tc.acceptsType(vc));
		assertTrue(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {^A!,B!}
		tc = TypeCollection.empty().with(va, true, true).with(vb, true, false);
		assertFalse(tc.acceptsType(va));
		assertTrue(tc.acceptsType(vb));
		assertFalse(tc.acceptsType(vc));
		assertFalse(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {^A!,^B}
		tc = TypeCollection.empty().with(va, true, true).with(vb, false, true);
		assertFalse(tc.acceptsType(va));
		assertFalse(tc.acceptsType(vb));
		assertTrue(tc.acceptsType(vc));
		assertFalse(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {^A!,^B!}
		tc = TypeCollection.empty().with(va, true, true).with(vb, true, true);
		assertFalse(tc.acceptsType(va));
		assertFalse(tc.acceptsType(vb));
		assertTrue(tc.acceptsType(vc));
		assertTrue(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));
	}
}
