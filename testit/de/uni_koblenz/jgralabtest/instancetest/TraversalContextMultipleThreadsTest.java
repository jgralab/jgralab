package de.uni_koblenz.jgralabtest.instancetest;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.TraversalContext;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class TraversalContextMultipleThreadsTest {

	@Test
	public void test() throws GraphIOException, InterruptedException {
		String filename = "testit/testgraphs/greqltestgraph.tg";
		Schema s = GraphIO.loadSchemaFromFile(filename);
		Graph g = GraphIO.loadGraphFromFile(filename, s,
				ImplementationType.GENERIC, null);

		TestThread t1 = new TestThread(s.getGraphClass().getVertexClass(
				"localities.County"), g);

		TestThread t2 = new TestThread(s.getGraphClass().getVertexClass(
				"localities.Village"), g);

		t1.start();
		t2.start();

		t1.join();
		t2.join();

		assertTrue(t1.isValid());
		assertTrue(t2.isValid());
	}

	class TestThread extends Thread {
		private VertexClass vc;
		private Graph graph;

		private List<Vertex> list = new ArrayList<>();

		public TestThread(VertexClass vc, Graph g) {
			this.vc = vc;
			this.graph = g;
		}

		@Override
		public void run() {
			graph.setTraversalContext(new TraversalContext() {

				@Override
				public boolean containsVertex(Vertex v) {
					if (v.getAttributedElementClass().equals(vc)
							|| v.getAttributedElementClass().isSubClassOf(vc)) {
						return true;
					} else {
						return false;
					}
				}

				@Override
				public boolean containsEdge(Edge e) {
					return this.containsVertex(e.getAlpha())
							&& this.containsVertex(e.getOmega());
				}
			});

			for (int i = 0; i < 10; i++) {

				System.out.println("Starting thread for " + vc);
				System.out.println("TraversalContext for " + vc + " is: "
						+ graph.getTraversalContext());

				for (Vertex v : graph.vertices()) {
					System.out.println(vc + "  Vertex: " + v);
					list.add(v);
				}

				System.out.println("sleep");

				try {
					sleep((int) (Math.random() * 1000));
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}

		public boolean isValid() {
			for (Vertex v : list) {
				if (!v.getAttributedElementClass().equals(vc)
						&& !v.getAttributedElementClass().isSubClassOf(vc)) {
					return false;
				}
			}
			return true;
		}
	}

}
