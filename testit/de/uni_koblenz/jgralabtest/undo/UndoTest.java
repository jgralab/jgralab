package de.uni_koblenz.jgralabtest.undo;

import javax.swing.undo.CompoundEdit;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.utilities.gui.undo.GraphUndoManager;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleEdge;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleSchema;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleVertex;

public class UndoTest {
	SimpleGraph g;
	GraphUndoManager mgr;
	CompoundEdit lastEdit;

	@Before
	public void setUp() {
		g = SimpleSchema.instance().createSimpleGraph(
				ImplementationType.STANDARD);
		// create gap in vertex ids
		SimpleVertex v1 = g.createSimpleVertex();
		SimpleVertex v2 = g.createSimpleVertex();
		SimpleVertex v3 = g.createSimpleVertex();
		g.createSimpleEdge(v1, v3);
		v2.delete();
		mgr = new GraphUndoManager(g);
		g.setECARuleManager(mgr);
		g.set_name("world");
	}

	private void newEdit(String name) {
		if (lastEdit != null) {
			lastEdit.end();
		}
		lastEdit = new CompoundEdit();
		mgr.addEdit(lastEdit);
	}

	private void endEdit() {
		if (lastEdit != null) {
			lastEdit.end();
			lastEdit = null;
		}
	}

	@Test
	public void testCreateVertex() {
		print();
		g.getVertex(3).setAttribute("name", "former v3");
		g.getVertex(3).delete();

		mgr.undo();
		mgr.undo();
		print();
		mgr.redo();
		mgr.redo();
		print();

		// SimpleVertex v2 = g.createSimpleVertex();
		// v2.set_name("bravo");
		// print();
		//
		// g.getVertex(3).delete();
		// SimpleVertex v1 = g.createSimpleVertex();
		// v1.set_name("alpha");
		// print();
		//
		// SimpleEdge e = g.createSimpleEdge(v1, v2);
		// e.set_name(e.getAlpha().get_name() + "->" + e.getOmega().get_name());
		// print();
		//
		// SimpleVertex v3 = g.createSimpleVertex();
		// v3.set_name("charlie");
		// print();
		//
		// g.deleteVertex(v1);
		// print();
		//
		// System.out
		// .println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		//
		// while (mgr.canUndo()) {
		// mgr.undo();
		// print();
		// }
		// System.out
		// .println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		// while (mgr.canRedo()) {
		// mgr.redo();
		// print();
		// }
	}

	private void print() {
		System.out
				.println("-----------------------------------------------------");
		// System.out.println(mgr);
		System.out.println("graph " + g.get_name());
		for (SimpleVertex v : g.getSimpleVertexVertices()) {
			System.out.println("\tv" + v.getId() + " " + v.get_name());
		}
		for (SimpleEdge e : g.getSimpleEdgeEdges()) {
			System.out.println("\te" + e.getId() + "(v" + e.getAlpha().getId()
					+ " -> v" + e.getOmega().getId() + ") " + e.get_name());
		}
	}
}
