/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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
		TypeCollection tc = TypeCollection.empty()
				.with(va.getQualifiedName(), false, false).bindToSchema(schema);
		assertTrue(tc.acceptsType(va));
		assertTrue(tc.acceptsType(vb));
		assertTrue(tc.acceptsType(vc));
		assertTrue(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {A!}
		tc = TypeCollection.empty().with(va.getQualifiedName(), true, false)
				.bindToSchema(schema);
		assertTrue(tc.acceptsType(va));
		assertFalse(tc.acceptsType(vb));
		assertFalse(tc.acceptsType(vc));
		assertFalse(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {^A}
		tc = TypeCollection.empty().with(va.getQualifiedName(), false, true)
				.bindToSchema(schema);
		assertFalse(tc.acceptsType(va));
		assertFalse(tc.acceptsType(vb));
		assertFalse(tc.acceptsType(vc));
		assertFalse(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {^A!}
		tc = TypeCollection.empty().with(va.getQualifiedName(), true, true)
				.bindToSchema(schema);
		assertFalse(tc.acceptsType(va));
		assertTrue(tc.acceptsType(vb));
		assertTrue(tc.acceptsType(vc));
		assertTrue(tc.acceptsType(vc));
		assertFalse(tc.acceptsType(ee));

		// {E}
		tc = TypeCollection.empty().with(ee.getQualifiedName(), false, false)
				.bindToSchema(schema);
		assertFalse(tc.acceptsType(va));
		assertFalse(tc.acceptsType(vb));
		assertFalse(tc.acceptsType(vc));
		assertFalse(tc.acceptsType(vd));
		assertTrue(tc.acceptsType(ee));

		// {A,E}
		tc = TypeCollection.empty().with(va.getQualifiedName(), false, false)
				.with(ee.getQualifiedName(), false, false).bindToSchema(schema);
		assertTrue(tc.acceptsType(va));
		assertTrue(tc.acceptsType(vb));
		assertTrue(tc.acceptsType(vc));
		assertTrue(tc.acceptsType(vd));
		assertTrue(tc.acceptsType(ee));

		// {A,B}
		tc = TypeCollection.empty().with(va.getQualifiedName(), false, false)
				.with(vb.getQualifiedName(), false, false).bindToSchema(schema);
		assertTrue(tc.acceptsType(va));
		assertTrue(tc.acceptsType(vb));
		assertTrue(tc.acceptsType(vc));
		assertTrue(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {A,B!}
		tc = TypeCollection.empty().with(va.getQualifiedName(), false, false)
				.with(vb.getQualifiedName(), true, false).bindToSchema(schema);
		assertTrue(tc.acceptsType(va));
		assertTrue(tc.acceptsType(vb));
		assertTrue(tc.acceptsType(vc));
		assertTrue(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {A,^B}
		tc = TypeCollection.empty().with(va.getQualifiedName(), false, false)
				.with(vb.getQualifiedName(), false, true).bindToSchema(schema);
		assertTrue(tc.acceptsType(va));
		assertFalse(tc.acceptsType(vb));
		assertTrue(tc.acceptsType(vc));
		assertFalse(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {A,^B!}
		tc = TypeCollection.empty().with(va.getQualifiedName(), false, false)
				.with(vb.getQualifiedName(), true, true).bindToSchema(schema);
		assertTrue(tc.acceptsType(va));
		assertFalse(tc.acceptsType(vb));
		assertTrue(tc.acceptsType(vc));
		assertTrue(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {A!,B}
		tc = TypeCollection.empty().with(va.getQualifiedName(), true, false)
				.with(vb.getQualifiedName(), false, false).bindToSchema(schema);
		assertTrue(tc.acceptsType(va));
		assertTrue(tc.acceptsType(vb));
		assertFalse(tc.acceptsType(vc));
		assertTrue(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {A!,B!}
		tc = TypeCollection.empty().with(va.getQualifiedName(), true, false)
				.with(vb.getQualifiedName(), true, false).bindToSchema(schema);
		assertTrue(tc.acceptsType(va));
		assertTrue(tc.acceptsType(vb));
		assertFalse(tc.acceptsType(vc));
		assertFalse(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {A!,^B}
		tc = TypeCollection.empty().with(va.getQualifiedName(), true, false)
				.with(vb.getQualifiedName(), false, true).bindToSchema(schema);
		assertTrue(tc.acceptsType(va));
		assertFalse(tc.acceptsType(vb));
		assertFalse(tc.acceptsType(vc));
		assertFalse(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {A!,^B!}
		tc = TypeCollection.empty().with(va.getQualifiedName(), true, false)
				.with(vb.getQualifiedName(), true, true).bindToSchema(schema);
		assertTrue(tc.acceptsType(va));
		assertFalse(tc.acceptsType(vb));
		assertFalse(tc.acceptsType(vc));
		assertFalse(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {^A,B}
		tc = TypeCollection.empty().with(va.getQualifiedName(), false, true)
				.with(vb.getQualifiedName(), false, false).bindToSchema(schema);
		assertFalse(tc.acceptsType(va));
		assertFalse(tc.acceptsType(vb));
		assertFalse(tc.acceptsType(vc));
		assertFalse(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {^A,B!}
		tc = TypeCollection.empty().with(va.getQualifiedName(), false, true)
				.with(vb.getQualifiedName(), true, false).bindToSchema(schema);
		assertFalse(tc.acceptsType(va));
		assertFalse(tc.acceptsType(vb));
		assertFalse(tc.acceptsType(vc));
		assertFalse(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {^A,^B}
		tc = TypeCollection.empty().with(va.getQualifiedName(), false, true)
				.with(vb.getQualifiedName(), false, true).bindToSchema(schema);
		assertFalse(tc.acceptsType(va));
		assertFalse(tc.acceptsType(vb));
		assertFalse(tc.acceptsType(vc));
		assertFalse(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {^A,^B!}
		tc = TypeCollection.empty().with(va.getQualifiedName(), false, true)
				.with(vb.getQualifiedName(), true, true).bindToSchema(schema);
		assertFalse(tc.acceptsType(va));
		assertFalse(tc.acceptsType(vb));
		assertFalse(tc.acceptsType(vc));
		assertFalse(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {^A!,B}
		tc = TypeCollection.empty().with(va.getQualifiedName(), true, true)
				.with(vb.getQualifiedName(), false, false).bindToSchema(schema);
		assertFalse(tc.acceptsType(va));
		assertTrue(tc.acceptsType(vb));
		assertFalse(tc.acceptsType(vc));
		assertTrue(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {^A!,B!}
		tc = TypeCollection.empty().with(va.getQualifiedName(), true, true)
				.with(vb.getQualifiedName(), true, false).bindToSchema(schema);
		assertFalse(tc.acceptsType(va));
		assertTrue(tc.acceptsType(vb));
		assertFalse(tc.acceptsType(vc));
		assertFalse(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {^A!,^B}
		tc = TypeCollection.empty().with(va.getQualifiedName(), true, true)
				.with(vb.getQualifiedName(), false, true).bindToSchema(schema);
		assertFalse(tc.acceptsType(va));
		assertFalse(tc.acceptsType(vb));
		assertTrue(tc.acceptsType(vc));
		assertFalse(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));

		// {^A!,^B!}
		tc = TypeCollection.empty().with(va.getQualifiedName(), true, true)
				.with(vb.getQualifiedName(), true, true).bindToSchema(schema);
		assertFalse(tc.acceptsType(va));
		assertFalse(tc.acceptsType(vb));
		assertTrue(tc.acceptsType(vc));
		assertTrue(tc.acceptsType(vd));
		assertFalse(tc.acceptsType(ee));
	}
}
