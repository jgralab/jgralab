package de.uni_koblenz.jgralabtest.undo;

import java.util.ArrayList;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.utilities.gui.undo.GraphUndoManager;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleEdge;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleSchema;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleVertex;

public class UndoTest {
	private SimpleGraph g;
	private GraphUndoManager mgr;

	@Before
	public void setUp() {
		g = SimpleSchema.instance().createSimpleGraph(
				ImplementationType.STANDARD);
		mgr = new GraphUndoManager(g);
		g.addGraphChangeListener(mgr);
	}

	@After
	public void tearDown() {
		g.removeGraphChangeListener(mgr);
		mgr = null;
		g = null;
	}

	private char vn = 'a';

	private SimpleVertex cv() {
		SimpleVertex v = g.createSimpleVertex();
		v.set_name("" + vn);
		++vn;
		return v;
	}

	private SimpleEdge ce(SimpleVertex a, SimpleVertex o) {
		SimpleEdge e = g.createSimpleEdge(a, o);
		e.set_name(a.get_name() + o.get_name());
		return e;
	}

	public void createRandomGraph(long seed) {
		Random rnd = new Random(seed);
		ArrayList<SimpleVertex> v = new ArrayList<SimpleVertex>();
		for (int i = 1; i <= 10; ++i) {
			v.add(cv());
		}
		for (int i = 1; i <= 20; ++i) {
			SimpleVertex a = v.get(rnd.nextInt(v.size()));
			SimpleVertex o = v.get(rnd.nextInt(v.size()));
			ce(a, o);
		}
		mgr.discardAllEdits();
	}

	@Test
	public void deleteVertexTest() {
		createRandomGraph(0);
		print();
	}

	// @Test
	public void testCreateVertex() {
		g.set_name("world");
		{// create gap in vertex ids
			SimpleVertex v1 = cv();
			SimpleVertex v2 = cv();
			SimpleVertex v3 = cv();
			SimpleEdge e = ce(v1, v3);
			v3.set_name("o3");
			e.setAlpha(v2);
			e.set_name(e.getAlpha().get_name() + e.getOmega().get_name());
			e.setOmega(v2);
			e.set_name(e.getAlpha().get_name() + e.getOmega().get_name());
			v2.delete();
		}

		print();
		g.getVertex(3).setAttribute("name",
				"former " + g.getVertex(3).getAttribute("name"));
		g.getVertex(3).delete();

		mgr.undo(); // delete
		mgr.undo();
		print();
		mgr.redo();
		mgr.redo();
		print();

		SimpleVertex v2 = cv();
		print();

		SimpleVertex v1 = cv();
		print();

		SimpleEdge e = ce(v1, v2);
		print();

		SimpleVertex v3 = cv();
		print();

		g.deleteVertex(v1);
		print();

		System.out
				.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

		while (mgr.canUndo()) {
			mgr.undo();
			print();
		}
		System.out
				.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		while (mgr.canRedo()) {
			mgr.redo();
			print();
		}
	}

	private void print() {
		System.out
				.println("-----------------------------------------------------");
		// System.out.println(mgr);
		System.out.println("graph " + g.get_name());
		for (SimpleVertex v : g.getSimpleVertexVertices()) {
			System.out.println("\tv" + v.getId() + " " + v.get_name());
			for (SimpleEdge e : v.getSimpleEdgeIncidences()) {
				System.out.println("\t\te" + e.getId() + " " + e.get_name());

			}
		}
		for (SimpleEdge e : g.getSimpleEdgeEdges()) {
			System.out.println("\te" + e.getId() + "(v" + e.getAlpha().getId()
					+ " -> v" + e.getOmega().getId() + ") " + e.get_name());
		}
	}
}
