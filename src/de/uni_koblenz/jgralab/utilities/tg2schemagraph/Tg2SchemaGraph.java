package de.uni_koblenz.jgralab.utilities.tg2schemagraph;

import java.io.InputStream;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.schema.Schema;

public class Tg2SchemaGraph {

	public Tg2SchemaGraph() {
	}

	public SchemaGraph process(String filename) throws GraphIOException {
		Schema2SchemaGraph s2sg = new Schema2SchemaGraph();
		Schema schema = GraphIO.loadSchemaFromFile(filename);
		return s2sg.convert2SchemaGraph(schema);
	}

	public SchemaGraph process(InputStream in) throws GraphIOException {
		Schema2SchemaGraph s2sg = new Schema2SchemaGraph();
		Schema schema = GraphIO.loadSchemaFromStream(in);
		return s2sg.convert2SchemaGraph(schema);
	}

	public static void main(String[] args) {

		if (args.length != 2) {
			System.err
					.println("There should be two arguments passed over.\n"
							+ "usage: Tg2SchemaGraph <TG-Location> <TG-Graph-Location>");
		}

		Tg2SchemaGraph graph = new Tg2SchemaGraph();
		try {
			GraphIO.saveGraphToFile(args[1], graph.process(args[0]), null);
		} catch (GraphIOException e) {
			e.printStackTrace();
			System.out
					.println("\nAn error occured while trying to save the graph.");
		}

	}
}