package de.uni_koblenz.jgralabtest.undo;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.utilities.gui.undo.GraphUndoManager;
import de.uni_koblenz.jgralabtest.schemas.undo.Comp;
import de.uni_koblenz.jgralabtest.schemas.undo.Link;
import de.uni_koblenz.jgralabtest.schemas.undo.Node;
import de.uni_koblenz.jgralabtest.schemas.undo.UndoTestGraph;
import de.uni_koblenz.jgralabtest.schemas.undo.UndoTestSchema;

public class UndoTest {
	private static final boolean RESPECT_GLOBAL_ORDER = false;
	private Random rnd;

	private UndoTestGraph g;
	private GraphUndoManager mgr;

	@Before
	public void setUp() {
		g = UndoTestSchema.instance().createUndoTestGraph(
				ImplementationType.STANDARD);
		mgr = new GraphUndoManager(g);
		g.addGraphChangeListener(mgr);
		rnd = new Random(0);
	}

	@After
	public void tearDown() {
		g.removeGraphChangeListener(mgr);
		mgr = null;
		g = null;
	}

	@Test
	public void testCreateElements() {
		Map<String, String> before = storeState();
		createRandomGraph();
		mgr.undo();
		Map<String, String> after = storeState();
		assertEquals(before, after);
	}

	@Test
	public void testCascadingDelete() {
		Comp root = g.createComp();
		root.set_name("root");

		Comp c1 = g.createComp();
		c1.set_name("c1");

		Node n1 = g.createNode();
		n1.set_name("n1");
		root.add_children(c1).set_name("root<>-c1");
		root.add_children(n1).set_name("root<>-n1");

		Node n2 = g.createNode();
		n2.set_name("n2");
		c1.add_children(n2).set_name("c1<>-n2");

		Node n3 = g.createNode();
		n3.set_name("n3");
		c1.add_children(n3).set_name("c1<>-n3");

		g.createLink(n2, root).set_name("n2-root");
		g.createLink(n1, n3).set_name("n1-n3");

		Comp c2 = g.createComp();
		c2.set_name("c2");
		root.add_children(c2).set_name("root<>-c2");

		g.createLink(c1, c2).set_name("c1-c2");
		mgr.discardAllEdits();

		Map<String, String> initial = storeState();

		c1.delete(); // removes n2, n3

		Map<String, String> afterModification = storeState();

		mgr.undo();

		Map<String, String> afterUndo = storeState();
		assertEquals(initial, afterUndo);

		while (mgr.canRedo()) {
			mgr.redo();
		}
		Map<String, String> afterRedo = storeState();
		assertEquals(afterModification, afterRedo);

	}

	@Test
	public void deleteVertexTest() {
		createRandomGraph();
		mgr.discardAllEdits();

		Map<String, String> beforeModification = storeState();

		ArrayList<Node> l = new ArrayList<Node>(g.getVCount());
		for (Node v : g.getNodeVertices()) {
			l.add(v);
		}
		Collections.shuffle(l, rnd);
		for (Node v : l) {
			v.delete();
		}
		Map<String, String> afterModification = storeState();
		while (mgr.canUndo()) {
			mgr.undo();
		}
		Map<String, String> afterUndo = storeState();
		assertEquals(beforeModification, afterUndo);

		while (mgr.canRedo()) {
			mgr.redo();
		}

		Map<String, String> afterRedo = storeState();
		assertEquals(afterModification, afterRedo);

	}

	@Test
	public void deleteEdgeTest() {
		createRandomGraph();
		mgr.discardAllEdits();

		Map<String, String> beforeModification = storeState();

		ArrayList<Link> l = new ArrayList<Link>(g.getECount());
		for (Link e : g.getLinkEdges()) {
			l.add(e);
		}
		Collections.shuffle(l, rnd);
		for (Link e : l) {
			e.delete();
		}

		Map<String, String> afterModification = storeState();
		while (mgr.canUndo()) {
			mgr.undo();
		}
		Map<String, String> afterUndo = storeState();
		assertEquals(beforeModification, afterUndo);

		while (mgr.canRedo()) {
			mgr.redo();
		}

		Map<String, String> afterRedo = storeState();
		assertEquals(afterModification, afterRedo);
	}

	@Test
	public void testPutBefore() {
		g.set_name("graph");
		Node a = cv();
		Node b = cv();
		Node c = cv();
		Node d = cv();

		@SuppressWarnings("unused")
		Link ab = ce(a, b);
		Link ac = ce(a, c);
		@SuppressWarnings("unused")
		Link ad = ce(a, c);
		@SuppressWarnings("unused")
		Link cd = ce(c, d);
		@SuppressWarnings("unused")
		Link bb = ce(b, b);
		Link ca = ce(c, a);

		Map<String, String> initial = storeState();

		ca.getReversedEdge().putIncidenceBefore(ac);

		Map<String, String> afterMod = storeState();

		mgr.undo();

		Map<String, String> afterUndo = storeState();
		assertEquals(initial, afterUndo);

		mgr.redo();
		Map<String, String> afterRedo = storeState();
		assertEquals(afterMod, afterRedo);
	}

	@Test
	public void testPutAfter() {
		Node a = cv();
		Node b = cv();
		Node c = cv();
		Node d = cv();

		Link ab = ce(a, b);
		@SuppressWarnings("unused")
		Link ac = ce(a, c);
		@SuppressWarnings("unused")
		Link ad = ce(a, c);
		@SuppressWarnings("unused")
		Link cd = ce(c, d);
		@SuppressWarnings("unused")
		Link bb = ce(b, b);
		Link ca = ce(c, a);

		Map<String, String> initial = storeState();

		ca.getReversedEdge().putIncidenceBefore(ab);

		Map<String, String> afterMod = storeState();

		mgr.undo();

		Map<String, String> afterUndo = storeState();
		assertEquals(initial, afterUndo);

		mgr.redo();
		Map<String, String> afterRedo = storeState();
		assertEquals(afterMod, afterRedo);
	}

	@Test
	public void testSetAlpha() {
		Node a = cv();
		Node b = cv();
		Node c = cv();
		Node d = cv();

		@SuppressWarnings("unused")
		Link ab = ce(a, b);
		@SuppressWarnings("unused")
		Link ac = ce(a, c);
		@SuppressWarnings("unused")
		Link ad = ce(a, c);
		@SuppressWarnings("unused")
		Link cd = ce(c, d);
		@SuppressWarnings("unused")
		Link bb = ce(b, b);
		Link ca = ce(c, a);

		Map<String, String> initial = storeState();

		ca.setAlpha(b);

		Map<String, String> afterMod = storeState();

		mgr.undo();

		Map<String, String> afterUndo = storeState();
		assertEquals(initial, afterUndo);

		mgr.redo();
		Map<String, String> afterRedo = storeState();
		assertEquals(afterMod, afterRedo);
	}

	@Test
	public void testSetOmega() {
		Node a = cv();
		Node b = cv();
		Node c = cv();
		Node d = cv();

		@SuppressWarnings("unused")
		Link ab = ce(a, b);
		@SuppressWarnings("unused")
		Link ac = ce(a, c);
		@SuppressWarnings("unused")
		Link ad = ce(a, c);
		@SuppressWarnings("unused")
		Link cd = ce(c, d);
		@SuppressWarnings("unused")
		Link bb = ce(b, b);
		Link ca = ce(c, a);

		Map<String, String> initial = storeState();

		ca.setOmega(b);

		Map<String, String> afterMod = storeState();

		mgr.undo();

		Map<String, String> afterUndo = storeState();
		assertEquals(initial, afterUndo);

		mgr.redo();
		Map<String, String> afterRedo = storeState();
		assertEquals(afterMod, afterRedo);
	}

	@Test
	public void testChangeAttribute() {
		Node a = cv();
		Link l = ce(a, a);
		{
			// change graph attribute
			String oldName = g.get_name();
			g.set_name("hugo");
			mgr.undo();
			assertEquals(oldName, g.get_name());
			mgr.redo();
			assertEquals("hugo", g.get_name());
		}
		{
			// change vertex attribute
			String oldName = a.get_name();
			a.set_name("hugo");
			mgr.undo();
			assertEquals(oldName, a.get_name());
			mgr.redo();
			assertEquals("hugo", a.get_name());
		}
		{
			// change edge attribute
			String oldName = l.get_name();
			l.set_name("hugo");
			mgr.undo();
			assertEquals(oldName, l.get_name());
			mgr.redo();
			assertEquals("hugo", l.get_name());
		}
	}

	// internal stuff

	private char vn = 'a';

	private Node cv() {
		Node v = g.createNode();
		v.set_name("" + vn);
		++vn;
		return v;
	}

	private int en = 0;

	private Link ce(Node a, Node o) {
		++en;
		Link e = g.createLink(a, o);
		e.set_name(a.get_name() + o.get_name() + en);
		return e;
	}

	private void createRandomGraph() {
		mgr.beginEdit("create random graph");
		try {
			ArrayList<Node> v = new ArrayList<Node>();
			for (int i = 1; i <= 10; ++i) {
				v.add(cv());
			}
			for (int i = 1; i <= 30; ++i) {
				Node a = v.get(rnd.nextInt(v.size()));
				Node o = v.get(rnd.nextInt(v.size()));
				ce(a, o);
			}
		} finally {
			mgr.endEdit();
		}
	}

	private Map<String, String> storeState() {
		// System.out
		// .println("-------------------------------------------------------------");
		Map<String, String> state = new TreeMap<String, String>();
		@SuppressWarnings("unused")
		int i = 0;
		for (Vertex v : g.vertices()) {
			++i;
			String key = "v" + v.getAttribute("name")
					+ (RESPECT_GLOBAL_ORDER ? "-" + i : "");
			StringBuilder sb = new StringBuilder().append("v").append(
					v.getAttribute("name"));
			for (Edge e : v.incidences()) {
				sb.append(" ").append(e.isNormal() ? "+" : "-")
						.append(e.getAttribute("name"));
			}
			String val = sb.toString();
			state.put(key, val);
			// System.out.println(v + "\t" + val);
		}
		i = 0;
		for (Edge e : g.edges()) {
			++i;
			String key = "e" + e.getAttribute("name")
					+ (RESPECT_GLOBAL_ORDER ? "-" + i : "");
			String val = "e" + e.getAttribute("name") + " "
					+ e.getAlpha().getAttribute("name") + "->"
					+ e.getOmega().getAttribute("name");
			state.put(key, val);
			// System.out.println(e + "\t" + val);
		}
		return state;
	}
}
