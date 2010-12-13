package de.uni_koblenz.jgralab.utilities.csv2tg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import de.uni_koblenz.ist.utilities.csvreader.CsvReader;
import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.AttributedElement;
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

public class Csv2Tg implements FilenameFilter {

	private static final String CLI_OPTION_OUTPUT_FILE = "output";
	private static final String CLI_OPTION_CSV_FILES = "input";
	private static final String CLI_OPTION_SCHEMA = "schema";

	public static void main(String[] args) throws GraphIOException {
		Csv2Tg converter = new Csv2Tg();
		converter.getOptions(args);
		converter.process();

		System.out.println("Fini.");
	}

	private Schema schema;
	private String[] csvFiles;
	private Map<Class<? extends Vertex>, CsvReader> vertexInstances;
	private Map<Class<? extends Edge>, CsvReader> edgeInstances;
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
		setCsvFiles(comLine.getOptionValues(CLI_OPTION_CSV_FILES));
		setOutputFile(comLine.getOptionValue(CLI_OPTION_OUTPUT_FILE));
	}

	private void setSchema(String optionValues) throws GraphIOException {
		System.out.print("Loading Schema ... ");
		Schema schema = GraphIO.loadSchemaFromFile(optionValues);
		// TODO compile only if classes are not present
		schema.compile(CodeGeneratorConfiguration.FULL);
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

		Option csvFiles = new Option("i", CLI_OPTION_CSV_FILES, true,
				"(required): set of csv-file containing vertex / edge instance informations.");
		csvFiles.setRequired(true);
		csvFiles.setArgs(Option.UNLIMITED_VALUES);
		csvFiles.setArgName("files_or_folder");
		csvFiles.setValueSeparator(' ');
		oh.addOption(csvFiles);

		Option output = new Option("o", CLI_OPTION_OUTPUT_FILE, true,
				"(required): the output file name, or empty for stdout");
		output.setRequired(true);
		output.setArgName("file");
		oh.addOption(output);

		return oh;
	}

	public void process() {
		System.out.println("Start processing ... ");
		setUp();
		try {
			loadCsvFiles();
			processVertexFiles();
			processEdgeFiles();
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
		} finally {
			try {
				tearDown();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("done.");
	}

	private void setUp() {
		vertices = new HashMap<String, Vertex>();

		Method method = schema
				.getGraphCreateMethod(ImplementationType.STANDARD);
		vertexInstances = new HashMap<Class<? extends Vertex>, CsvReader>();
		edgeInstances = new HashMap<Class<? extends Edge>, CsvReader>();
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

	private void tearDown() throws IOException {
		vertices = null;
		graph = null;

		closeAllReader(vertexInstances.values());
		closeAllReader(edgeInstances.values());
		vertexInstances = null;
		edgeInstances = null;
	}

	private void closeAllReader(Collection<CsvReader> readers)
			throws IOException {
		for (CsvReader reader : readers) {
			reader.close();
		}
	}

	private void processEdgeFiles() throws NoSuchAttributeException,
			IOException, GraphIOException {
		for (Entry<Class<? extends Edge>, CsvReader> entry : edgeInstances
				.entrySet()) {

			CsvReader reader = entry.getValue();
			while (reader.readRecord()) {
				createEdge(reader, entry.getKey());
			}
		}
	}

	private void processVertexFiles() throws NoSuchAttributeException,
			IOException, GraphIOException {

		for (Entry<Class<? extends Vertex>, CsvReader> entry : vertexInstances
				.entrySet()) {

			CsvReader reader = entry.getValue();
			while (reader.readRecord()) {
				createVertex(reader, entry.getKey());
			}
		}
	}

	private void loadCsvFiles() throws NoSuchAttributeException,
			GraphIOException, IOException {
		for (String csvFile : csvFiles) {
			System.out.println("\tprocessing file: " + csvFile);
			loadCsvFile(csvFile);
		}
		System.out.println("Finished Processing.");
	}

	@SuppressWarnings("unchecked")
	private void loadCsvFile(String csvFile) throws NoSuchAttributeException,
			GraphIOException, IOException {
		CsvReader reader = openCvsFile(csvFile);

		String attributeClassName = reader.getFieldNames().get(0);
		AttributedElementClass clazz = schema
				.getAttributedElementClass(attributeClassName);

		Class<? extends AttributedElement> vertexClass = clazz.getM1Class();
		boolean isVertexClass = clazz.isSubClassOf(schema
				.getDefaultVertexClass());

		if (isVertexClass) {
			vertexInstances.put((Class<? extends Vertex>) vertexClass, reader);
		} else {
			edgeInstances.put((Class<? extends Edge>) vertexClass, reader);
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

	private void createVertex(CsvReader reader, Class<? extends Vertex> clazz)
			throws NoSuchAttributeException, GraphIOException {
		Vertex vertex = graph.createVertex(clazz);

		insertAttribute(vertex, reader, 1);

		String uniqueName = reader.getFieldAt(0);
		if (vertices.containsKey(uniqueName)) {
			throw new RuntimeException("The unique name \"" + uniqueName
					+ "\" isn't in fact unique. This error occured while "
					+ "processing the file \"" + openedFile + "\".");
		}
		vertices.put(uniqueName, vertex);
	}

	private void createEdge(CsvReader reader, Class<? extends Edge> clazz)
			throws NoSuchAttributeException, GraphIOException {

		Vertex alpha = getVertex(reader, 1);
		Vertex omega = getVertex(reader, 2);

		Edge edge = graph.createEdge(clazz, alpha, omega);

		insertAttribute(edge, reader, 3);

	}

	private void insertAttribute(AttributedElement edge, CsvReader reader,
			int startColumnIndex) throws NoSuchAttributeException,
			GraphIOException {

		Vector<String> header = reader.getFieldNames();

		for (int index = startColumnIndex; index < header.size(); index++) {

			String attributeName = header.get(index);
			String valueString = reader.getFieldAt(index);
			String transformedString = transformCsvStringValue(valueString);

			edge.readAttributeValueFromString(attributeName, transformedString);
		}
	}

	private String transformCsvStringValue(String csvStringValue) {
		if (csvStringValue.startsWith("\"") && csvStringValue.endsWith("\"")) {
			csvStringValue = csvStringValue.substring(1,
					csvStringValue.length() - 1);
		}
		csvStringValue = csvStringValue.replace("\"\"", "\"");
		return csvStringValue;
	}

	private Vertex getVertex(CsvReader reader, int fieldNumber) {
		String vertexName = reader.getFieldAt(fieldNumber);
		Vertex vertex = vertices.get(vertexName);
		if (vertex == null) {
			throw new RuntimeException("Couldn't find vertex \"" + vertexName
					+ "\" in line: " + reader.getLineNumber());
			// + " in file \"" + reader.toString() + "\".");
		}
		return vertex;
	}

	public String[] getCsvFiles() {
		return csvFiles;
	}

	public void setCsvFiles(String[] vertexFiles) {
		System.out.println(Arrays.toString(vertexFiles));
		this.csvFiles = getFilesInFolder(vertexFiles);
	}

	private String[] getFilesInFolder(String[] filenames) {
		HashSet<String> fileList = new HashSet<String>();
		for (String filename : filenames) {

			File file = new File(filename).getAbsoluteFile();
			if (!file.exists()) {
				throw new RuntimeException("File or folder \"" + filename
						+ "\" does not exist!");
			}
			if (file.isDirectory()) {
				for (File foundFile : file.listFiles(this)) {
					fileList.add(foundFile.getAbsolutePath());
				}
			} else {
				fileList.add(file.getAbsolutePath());
			}
		}

		if (fileList.isEmpty()) {
			throw new RuntimeException("No csv-files to convert to a tg-file.");
		}

		return fileList.toArray(new String[0]);
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	@Override
	public boolean accept(File dir, String name) {
		return name.endsWith(".csv");
	}
}
