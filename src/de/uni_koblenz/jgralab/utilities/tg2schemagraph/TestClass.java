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
		for (String filename : args) {
			try {

				System.out.println("Processing File: " + filename);
				System.out.print("Reading TG-File ... \t\t");
				Schema schema = GraphIO.loadSchemaFromFile(filename);
				System.out.println("done.");

				System.out.print("Converting Graph ... \t\t");
				JGraLab.setLogLevel(Level.OFF);
				Schema2SchemaGraph converter = new Schema2SchemaGraph();
				SchemaGraph sg = converter.convert2SchemaGraph(schema);
				System.out.println("done.");

				System.out.print("Creating Dot-File ... \t\t");
				createDotFile(filename, sg);
				System.out.println("done.");

				System.out.print("Writing Schema to TG ... \t");
				SchemaGraph2Tg converter2 = new SchemaGraph2Tg(sg, filename
						+ ".testSCHEMA", true);
				converter2.setIsFormatted(false);
				converter2.run();

				System.out.println("done.\n");
			} catch (GraphIOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
