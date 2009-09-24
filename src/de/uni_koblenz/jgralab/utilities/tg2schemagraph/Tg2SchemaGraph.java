package de.uni_koblenz.jgralab.utilities.tg2schemagraph;

import java.io.InputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;
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
		CommandLine comLine = processCommandLineOptions(args);
		assert comLine != null;
		Tg2SchemaGraph graph = new Tg2SchemaGraph();
		try {
			GraphIO.saveGraphToFile(comLine.getOptionValue("o"), graph
					.process(comLine.getOptionValue("s")), null);
		} catch (GraphIOException e) {
			e.printStackTrace();
			System.out
					.println("\nAn error occured while trying to save the graph.");
		}
		// if (args.length != 2) {
		// System.err
		// .println("There should be two arguments passed over.\n"
		// + "usage: Tg2SchemaGraph <TG-Location> <TG-Graph-Location>");
		// }
		//
		// Tg2SchemaGraph graph = new Tg2SchemaGraph();
		// try {
		// GraphIO.saveGraphToFile(args[1], graph.process(args[0]), null);
		// } catch (GraphIOException e) {
		// e.printStackTrace();
		// System.out
		// .println("\nAn error occured while trying to save the graph.");
		// }
	}

	private static CommandLine processCommandLineOptions(String[] args) {
		String toolString = "java " + Tg2SchemaGraph.class.getName();
		String versionString = JGraLab.getInfo(false);
		OptionHandler oh = new OptionHandler(toolString, versionString);

		Option output = new Option("o", "output", true,
				"(required): the output file name");
		output.setRequired(true);
		output.setArgName("file");
		oh.addOption(output);

		Option schema = new Option("s", "schema", true,
				"(required): the schema of which a schemaGraph should be generated");
		schema.setRequired(true);
		schema.setArgName("file");
		oh.addOption(schema);

		return oh.parse(args);
	}
}