package de.uni_koblenz.jgralabtest.non_junit_tests;

import de.uni_koblenz.jgralab.impl.db.GraphDatabase;
import de.uni_koblenz.jgralab.impl.db.GraphDatabaseException;
import de.uni_koblenz.jgralabtest.schemas.jniclient.JniTestGraph;
import de.uni_koblenz.jgralabtest.schemas.jniclient.JniTestSchema;
import de.uni_koblenz.jgralabtest.schemas.jniclient.Node;

public class GraphDbTest {

	public static void main(String[] args) {
		GraphDatabase gdb;
		try {
			System.out.println("Connecting DB...");
			gdb = GraphDatabase
					.openGraphDatabase("jdbc:postgresql://jgtest:secret@localhost:5432/jgtest");
			// gdb = GraphDatabase
			// .openGraphDatabase("jdbc:postgresql://riediger:win4all@helena.uni-koblenz.de:5432/jgtest");
			try {
				if (!gdb.contains(JniTestSchema.instance())) {
					gdb.insertSchema(JniTestSchema.instance(), "// no text");
				}
			} catch (GraphDatabaseException e) {
				gdb.applyDbSchema();
				if (!gdb.contains(JniTestSchema.instance())) {
					gdb.insertSchema(JniTestSchema.instance(), "// no text");
				}
			}
			try {
				gdb.getGraph("gdbtest");
				System.out.println("Deleting old graph...");
				gdb.deleteGraph("gdbtest");
			} catch (GraphDatabaseException e) {
				// ignore exception
			}

			gdb.optimizeForGraphCreation();
			gdb.commitTransaction();
			System.out.println("Creating graph...");
			JniTestGraph g = JniTestSchema.instance()
					.createJniTestGraphWithDatabaseSupport("gdbtest", gdb);

			final int NV = 100000;
			final int NE = 100000;

			System.out.println("Creating " + NV + " vertices...");
			long s0 = System.currentTimeMillis();
			for (int i = 1; i <= NV; ++i) {
				g.createNode();
				if (i % 1000 == 0) {
					System.out.print(".");
				}
			}
			System.out.println();
			System.out.println(System.currentTimeMillis() - s0 + " ms");
			gdb.commitTransaction();

			System.out.println("Creating " + NE + " edges...");
			s0 = System.currentTimeMillis();
			for (int i = 1; i <= NE; ++i) {
				Node n1 = (Node) g.getVertex((int) (Math.random() * NV) + 1);
				Node n2 = (Node) g.getVertex((int) (Math.random() * NV) + 1);
				g.createLink(n1, n2);
				if (i % 1000 == 0) {
					System.out.print(".");
				}
			}
			System.out.println();
			System.out.println(System.currentTimeMillis() - s0 + " ms");
			gdb.commitTransaction();

			gdb.setAutoCommitMode(true);
			gdb.optimizeForGraphTraversal();
			System.out.println("Fini.");
		} catch (GraphDatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
