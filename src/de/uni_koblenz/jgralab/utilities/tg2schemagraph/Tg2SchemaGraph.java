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

		CommandLine comLine = null;
		try {
			comLine = new BasicParser().parse(options, args);
		} catch (ParseException e) {
			HelpFormatter helpForm = new HelpFormatter();

			/*
			 * If there are required options, apache.cli does not accept a
			 * single -h or -v option. It's a known bug, which will be fixed in
			 * a later version.
			 */
			boolean vFlag = false;
			for (String s : args) {
				vFlag = vFlag || s.equals("-v") || s.equals("--version");
			}
			if (vFlag) {
				System.out.println(JGraLab.getInfo(false));
			} else {
				System.err.println(e.getMessage());
				helpForm
						.printHelp(Tg2SchemaGraph.class.getSimpleName(), options);
				System.exit(1);
			}
			System.exit(0);
		}

		// processing of arguments and setting member variables accordingly
		if(comLine.hasOption("v")){
			System.out.println(JGraLab.getInfo(false));
		}
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