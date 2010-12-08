package de.uni_koblenz.jgralab.utilities.csv2tg;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import de.uni_koblenz.ist.utilities.csvreader.CsvReader;
import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.NoSuchAttributeException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Schema;

public class Csv2Tg {

	private static final String CLI_OPTION_OUTPUT_FILE = "output";
	private static final String CLI_OPTION_EDGE_FILES = "edges";
	private static final String CLI_OPTION_VERTEX_FILES = "vertices";
	private static final String CLI_OPTION_SCHEMA = "schema";

	public static void main(String[] args) throws GraphIOException {
		Csv2Tg converter = new Csv2Tg();
		converter.getOptions(args);
		converter.process();

		System.out.println("Fini.");
	}

	private Schema schema;
	private String[] vertexFiles, edgeFiles;
	private String outputFile;
	private Graph graph;
	private Map<String, Vertex> vertices;
	private CsvReader openFileReader;
	private String openedFile;

	public Schema getSchema() {
		return schema;
	}

	public void setSchema(Schema schema) {
		this.schema = schema;
	}

	public Csv2Tg() {

	}

	final protected CommandLine processCommandLineOptions(String[] args) {
		OptionHandler oh = createOptionHandler();
		return oh.parse(args);
	}

	protected void getOptions(String[] args) throws GraphIOException {
		CommandLine comLine = processCommandLineOptions(args);
		assert comLine != null;

		setSchema(comLine.getOptionValue(CLI_OPTION_SCHEMA));
		setVertexFiles(comLine.getOptionValues(CLI_OPTION_VERTEX_FILES));
		setEdgeFiles(comLine.getOptionValues(CLI_OPTION_EDGE_FILES));
		setOutputFile(comLine.getOptionValue(CLI_OPTION_OUTPUT_FILE));
	}

	private void setSchema(String optionValues) throws GraphIOException {
		System.out.print("Loading Schema ... ");
		Schema schema = GraphIO
				.loadSchemaFromFile("testit/testschemas/citymapschema.tg");
		schema.commit(CodeGeneratorConfiguration.MINIMAL);
		setSchema(schema);
		System.out.println("done.");
	}

	final protected OptionHandler createOptionHandler() {
		String toolString = "java " + this.getClass().getName();
		String versionString = JGraLab.getInfo(false);
		OptionHandler oh = new OptionHandler(toolString, versionString);

		Option schema = new Option("s", CLI_OPTION_SCHEMA, true,
				"(required): the schema according to which the graph should be constructed.");
		schema.setRequired(true);
		schema.setArgName("file");
		oh.addOption(schema);

		Option vertexFiles = new Option("n", CLI_OPTION_VERTEX_FILES, true,
				"(required): set of csv-file containing vertex instance informations.");
		vertexFiles.setRequired(true);
		vertexFiles.setArgName("files");
		oh.addOption(vertexFiles);

		Option edgeFiles = new Option("e", CLI_OPTION_EDGE_FILES, true,
				"(required): set of csv-file containing edge instance informations.");
		edgeFiles.setRequired(true);
		edgeFiles.setArgName("files");
		oh.addOption(edgeFiles);

		Option output = new Option("o", CLI_OPTION_OUTPUT_FILE, true,
				"(required): the output file name, or empty for stdout");
		output.setRequired(true);
		output.setArgName("file");
		oh.addOption(output);

		return oh;
	}

	public void process() {
		System.out.print("Start processing ... ");
		setUp();
		try {
			loadVertexFiles();
			loadEdgeFiles();
			GraphIO.saveGraphToFile(outputFile, graph, null);
		} catch (NoSuchAttributeException e) {
			e.printStackTrace();
		} catch (GraphIOException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("An error occured while processing "
					+ openFileReader.getLineNumber() + " in file \""
					+ openedFile + "\".");
			e.printStackTrace();
		}
		tearDown();
		System.out.println("done.");
	}

	private void setUp() {
		vertices = new HashMap<String, Vertex>();

		Method method = schema
				.getGraphCreateMethod(ImplementationType.STANDARD);
		// TODO Graph ID
		try {
			graph = (Graph) method.invoke(null,
					new Object[] { "instance", 1, 1 });
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	private void tearDown() {
		vertices = null;
		graph = null;
	}

	private void loadVertexFiles() throws NoSuchAttributeException,
			GraphIOException, IOException {
		for (String vertexFile : vertexFiles) {
			loadVertexFile(vertexFile);
		}
	}

	private void loadVertexFile(String vertexFile)
			throws NoSuchAttributeException, GraphIOException, IOException {
		CsvReader reader = openCvsFile(vertexFile);

		while (reader.readRecord()) {
			createVertex(reader);
		}
	}

	private CsvReader openCvsFile(String csvFile) {
		try {
			// Storing as a global variable is just for debugging purposes.
			openedFile = csvFile;
			openFileReader = new CsvReader(new FileReader(csvFile),
					CsvReader.WITH_FIELDNAMES);
			return openFileReader;
		} catch (FileNotFoundException cause) {
			throw new RuntimeException(
					"An error occured while opening the file \"" + csvFile
							+ "\".", cause);
		} catch (IOException cause) {
			throw new RuntimeException(
					"An error occured while opening the file \"" + csvFile
							+ "\".", cause);
		}
	}

	private void createVertex(CsvReader reader)
			throws NoSuchAttributeException, GraphIOException {
		Vector<String> header = reader.getFieldNames();
		AttributedElementClass clazz = schema.getAttributedElementClass(header
				.get(0));

		Vertex vertex = graph.createVertex((Class<Vertex>) clazz.getM1Class());

		for (int index = 1; index < header.size(); index++) {

			String attributeName = header.get(index);
			String valueString = reader.getFieldAt(index);
			vertex.readAttributeValueFromString(attributeName, valueString);
		}

		vertices.put(reader.getFieldAt(0), vertex);
	}

	private void loadEdgeFiles() throws NoSuchAttributeException,
			GraphIOException, IOException {
		for (String edgeFile : edgeFiles) {
			loadEdgeFile(edgeFile);
		}
	}

	private void loadEdgeFile(String edgeFile) throws NoSuchAttributeException,
			GraphIOException, IOException {
		CsvReader reader = openCvsFile(edgeFile);

		while (reader.readRecord()) {
			createEdge(reader);
		}
	}

	private void createEdge(CsvReader reader) throws NoSuchAttributeException,
			GraphIOException {

		Vector<String> header = reader.getFieldNames();
		AttributedElementClass clazz = schema.getAttributedElementClass(header
				.get(0));

		Vertex alpha = getVertex(reader.getFieldAt(1));
		Vertex omega = getVertex(reader.getFieldAt(2));

		Edge edge = graph.createEdge((Class<Edge>) clazz.getM1Class(), alpha,
				omega);

		for (int index = 3; index < header.size(); index++) {

			String attributeName = header.get(index);
			String valueString = reader.getFieldAt(index).replace("\"\"\"",
					"\"");
			edge.readAttributeValueFromString(attributeName, valueString);
		}
	}

	private Vertex getVertex(String vertexName) {
		return vertices.get(vertexName);
	}

	public String[] getVertexFiles() {
		return vertexFiles;
	}

	public void setVertexFiles(String[] vertexFiles) {
		this.vertexFiles = vertexFiles;
	}

	public String[] getEdgeFiles() {
		return edgeFiles;
	}

	public void setEdgeFiles(String[] edgeFiles) {
		this.edgeFiles = edgeFiles;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}
}
