package de.uni_koblenz.jgralab.utilities.xml;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.Schema2SchemaGraph;

public class SchemaGraph2XMI {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("SchemaGraph to XMI");
		System.out.println("==================");

		// Retrieving all command line options
		CommandLine cli = processCommandLineOptions(args);

		assert cli != null : "No CommandLine object has been generated!";
	}

	/**
	 * Processes all command line parameters and returns a {@link CommandLine}
	 * object, which holds all values included in the given {@link String}
	 * array.
	 * 
	 * @param args
	 *            {@link CommandLine} parameters.
	 * @return {@link CommandLine} object, which holds all necessary values.
	 */
	public static CommandLine processCommandLineOptions(String[] args) {

		// Creates a OptionHandler.
		String toolString = "java " + SchemaGraph2XMI.class.getName();
		String versionString = JGraLab.getInfo(false);

		OptionHandler oh = new OptionHandler(toolString, versionString);

		// Several Options are declared.
		Option output = new Option("o", "output", true,
				"(required): the output xmi file name");
		output.setRequired(true);
		output.setArgName("file");
		oh.addOption(output);

		Option schemaGraph = new Option("ig", "inputSchemaGraph", true,
				"(required or -i):if set, the schemaGraph is converted into a xmi.");
		schemaGraph.setRequired(false);
		schemaGraph.setArgs(0);
		output.setArgName("file");
		oh.addOption(schemaGraph);

		Option schema = new Option(
				"i",
				"inputSchema",
				true,
				"(required or -ig): TG-file of the schema which should be converted into a xmi.");
		schema.setRequired(false);
		schema.setArgName("file");
		oh.addOption(schema);

		// either graph or schema has to be provided
		OptionGroup input = new OptionGroup();
		input.addOption(schemaGraph);
		input.addOption(schema);
		input.setRequired(true);
		oh.addOptionGroup(input);

		return oh.parse(args);
	}

	public SchemaGraph2XMI(String xmiName, String schemaName)
			throws GraphIOException {
		new SchemaGraph2XMI(xmiName, GraphIO.loadSchemaFromFile(schemaName));
	}

	public SchemaGraph2XMI(String xmiName, Schema schema) {
		new SchemaGraph2XMI(xmiName, new Schema2SchemaGraph()
				.convert2SchemaGraph(schema));
	}

	public SchemaGraph2XMI(String xmiName, SchemaGraph schemaGraph) {
		// TODO Auto-generated constructor stub
	}

}
