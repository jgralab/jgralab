package de.uni_koblenz.jgralab.utilities.tg2schemagraph;

import java.io.InputStream;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

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
		// define the options
		Options options = new Options();

		Option oSchema = new Option("s", "schema", true,
				"(required): the schema of which a schemaGraph should be generated");
		oSchema.setRequired(true);
		options.addOption(oSchema);

		Option oOutput = new Option("o", "output", true,
				"(required): the output file name");
		oOutput.setRequired(true);
		options.addOption(oOutput);

		Option oVersion = new Option("v", "version", false,
				"(optional): show version");
		options.addOption(oVersion);

		Option oHelp = new Option("h", "help", false, "(optional): show help");
		options.addOption(oHelp);

		Option oHelp2 = new Option("?", false, "(optional): show help");
		options.addOption(oHelp2);

		// parse arguments
		CommandLine comLine = null;
		HelpFormatter helpForm = new HelpFormatter();
		helpForm
				.setSyntaxPrefix("Usage: Tg2SchemaGraph -s <TG-Location> -o <TG-Graph-Location>"
						+ "Options are:");
		try {
			comLine = new BasicParser().parse(options, args);
		} catch (ParseException e) {

			/*
			 * If there are required options, apache.cli does not accept a
			 * single -h or -v option. It's a known bug, which will be fixed in
			 * a later version.
			 */
			if (args.length > 0
					&& (args[0].equals("-h") || args[0].equals("--help") || args[0]
							.equals("-?"))) {
				helpForm.printHelp(" ", options);
			} else if (args.length > 0
					&& (args[0].equals("-v") || args[0].equals("--version"))) {
				// TODO check version number
				System.out.println("Tg2SchemaGraph version 1.0");
			} else {
				System.err.println(e.getMessage());
				helpForm.printHelp(" ", options);
				System.exit(1);
			}
			System.exit(0);
		}

		// processing of arguments and setting member variables accordingly
		Tg2SchemaGraph graph = new Tg2SchemaGraph();
		try {
			GraphIO.saveGraphToFile(comLine.getOptionValue("o"), graph.process(comLine.getOptionValue("s")), null);
		} catch (GraphIOException e) {
			e.printStackTrace();
			System.out
					.println("\nAn error occured while trying to save the graph.");
		}		
		
		

//		if (args.length != 2) {
//			System.err
//					.println("There should be two arguments passed over.\n"
//							+ "usage: Tg2SchemaGraph <TG-Location> <TG-Graph-Location>");
//		}
//
//		Tg2SchemaGraph graph = new Tg2SchemaGraph();
//		try {
//			GraphIO.saveGraphToFile(args[1], graph.process(args[0]), null);
//		} catch (GraphIOException e) {
//			e.printStackTrace();
//			System.out
//					.println("\nAn error occured while trying to save the graph.");
//		}

	}
}