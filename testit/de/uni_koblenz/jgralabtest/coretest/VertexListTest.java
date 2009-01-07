package de.uni_koblenz.jgralabtest.coretest;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalGraph;
import de.uni_koblenz.jgralabtest.schemas.minimal.MinimalSchema;

public class VertexListTest {
	final int V = 4;
	final int E = 4;
	final int N = 10;
	MinimalGraph g;

	@Before
	public void setup() {
		g = MinimalSchema.instance().createMinimalGraph(V, E);
		for (int i = 0; i < N; ++i) {
			g.createNode();
		}
	}

	@Test
	public void addVertexTest() throws Exception {
		assertEquals(10, g.getVCount());
		assertEquals("v1 v2 v3 v4 v5 v6 v7 v8 v9 v10", getVSeq());
	}

	private String getVSeq() {
		StringBuilder sb = new StringBuilder();
		for (Vertex v : g.vertices()) {
			sb.append('v').append(v.getId()).append(' ');
		}
		return sb.toString().trim();
	}

	@Test
	public void putBeforeTest() throws Exception {
		Vertex v5 = g.getVertex(5);
		v5.putBefore(g.getVertex(6));
		assertTrue(v5.isBefore(g.getVertex(6)));
		assertEquals("v1 v2 v3 v4 v5 v6 v7 v8 v9 v10", getVSeq());

		assertTrue(v5.isAfter(g.getVertex(4)));
		assertFalse(v5.isBefore(g.getVertex(4)));
		v5.putBefore(g.getVertex(4));
		assertEquals("v1 v2 v3 v5 v4 v6 v7 v8 v9 v10", getVSeq());
		assertFalse(v5.isAfter(g.getVertex(4)));
		assertTrue(v5.isBefore(g.getVertex(4)));

		v5.putBefore(g.getVertex(10));
		assertEquals("v1 v2 v3 v4 v6 v7 v8 v9 v5 v10", getVSeq());
		assertFalse(v5.isAfter(g.getVertex(10)));
		assertTrue(v5.isBefore(g.getVertex(10)));

		assertFalse(v5.isBefore(g.getVertex(1)));
		assertTrue(g.getVertex(1).isBefore(v5));
		v5.putBefore(g.getVertex(1));
		assertEquals("v5 v1 v2 v3 v4 v6 v7 v8 v9 v10", getVSeq());
		assertTrue(v5.isBefore(g.getVertex(1)));
		assertFalse(v5.isAfter(g.getVertex(1)));
		assertTrue(g.getVertex(1).isAfter(v5));
	}

	@Test
	public void putAfterTest() throws Exception {
		Vertex v5 = g.getVertex(5);

		v5.putAfter(g.getVertex(4));
		assertEquals("v1 v2 v3 v4 v5 v6 v7 v8 v9 v10", getVSeq());

		v5.putAfter(g.getVertex(6));
		assertEquals("v1 v2 v3 v4 v6 v5 v7 v8 v9 v10", getVSeq());

		v5.putAfter(g.getVertex(10));
		assertEquals("v1 v2 v3 v4 v6 v7 v8 v9 v10 v5", getVSeq());

		v5.putAfter(g.getVertex(1));
		assertEquals("v1 v5 v2 v3 v4 v6 v7 v8 v9 v10", getVSeq());
	}

	@Test
	public void deleteVertexTest() throws Exception {
		Vertex v = g.getVertex(5);
		v.delete();
		assertFalse(v.isValid());
		assertEquals(null, g.getVertex(5));
		assertEquals(9, g.getVCount());
		assertEquals("v1 v2 v3 v4 v6 v7 v8 v9 v10", getVSeq());

		v = g.getFirstVertex();
		v.delete();
		assertFalse(v.isValid());
		assertEquals(null, g.getVertex(1));
		assertEquals(8, g.getVCount());
		assertEquals("v2 v3 v4 v6 v7 v8 v9 v10", getVSeq());

		v = g.getVertex(10);
		v.delete();
		assertFalse(v.isValid());
		assertEquals(null, g.getVertex(1));
		assertEquals(7, g.getVCount());
		assertEquals("v2 v3 v4 v6 v7 v8 v9", getVSeq());

		g.createNode();
		assertEquals("v2 v3 v4 v6 v7 v8 v9 v1", getVSeq());

		g.createNode();
		assertEquals("v2 v3 v4 v6 v7 v8 v9 v1 v5", getVSeq());

		g.createNode();
		assertEquals("v2 v3 v4 v6 v7 v8 v9 v1 v5 v10", getVSeq());

		g.createNode();
		assertEquals("v2 v3 v4 v6 v7 v8 v9 v1 v5 v10 v11", getVSeq());
	}
}
