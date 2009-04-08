package de.uni_koblenz.jgralab.utilities.tg2schemagraph;

import java.io.IOException;
import java.util.logging.Level;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.rsa2tg.SchemaGraph2Tg;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;

public class TestClass {

	public static void main(String[] args) {
		try {

			System.out.print("Reading TG-File ... \t\t");
			Schema schema = GraphIO.loadSchemaFromFile(args[0]);
			System.out.println("done.");

			System.out.print("Converting Graph ... \t\t");
			JGraLab.setLogLevel(Level.OFF);
			Schema2SchemaGraph converter = new Schema2SchemaGraph();
			SchemaGraph sg = converter.convert2SchemaGraph(schema);
			System.out.println("done.");

			System.out.print("Creating Dot-File ... \t\t");
			createDotFile(args[0], sg);
			System.out.println("done.");

			System.out.print("Writing Schema to TG ... \t");
			new SchemaGraph2Tg(sg, args[0] + ".testSCHEMA").run();
			System.out.println("done.");
		} catch (GraphIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("\n\t\t\t\tFini.");
	}

	private static void createDotFile(String filename, SchemaGraph sg) {
		Tg2Dot tg2Dot = new Tg2Dot();
		tg2Dot.setGraph(sg);
		tg2Dot.setPrintEdgeAttributes(true);
		tg2Dot.setOutputFile(filename + ".gruml.dot");
		tg2Dot.printGraph();
	}
}
