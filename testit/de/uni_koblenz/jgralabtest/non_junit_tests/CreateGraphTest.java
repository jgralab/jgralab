package de.uni_koblenz.jgralabtest.non_junit_tests;

import java.util.logging.Level;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.impl.SchemaImpl;

public class CreateGraphTest {
	public static void main(String[] args) {
		System.out.println("Test for in-memory compilation");

		System.out.println("Create schema...");
		Schema schema = new SchemaImpl("SimpleSchema", "de.uni_koblenz.demo");
		GraphClass gc = schema.createGraphClass("SimpleGraph");
		VertexClass node = gc.createVertexClass("Node");

		Level l = JGraLab.setLogLevel(Level.FINE);
		System.out.println("Compile schema classes in memory...");
		System.out.flush();
		schema.compile(CodeGeneratorConfiguration.MINIMAL);

		System.err.flush();
		System.out.println("Create " + gc.getQualifiedName());
		Graph g = schema.createGraph(ImplementationType.STANDARD, 10, 10);
		g.createVertex(node.getSchemaClass());
		g.createVertex(node.getSchemaClass());
		JGraLab.setLogLevel(l);
		try {
			System.out.println("Save graph...");
			g.save("testit/testdata/graph.tg");
		} catch (GraphIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Fini.");
	}
}
