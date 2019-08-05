/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
package de.uni_koblenz.jgralab.utilities.csv2tg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import de.uni_koblenz.ist.utilities.csvreader.CsvReader;
import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.exception.NoSuchAttributeException;
import de.uni_koblenz.jgralab.impl.InternalAttributedElement;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.schema.impl.compilation.SchemaClassManager;

public class Csv2Tg implements FilenameFilter {

    private static final String CLI_OPTION_OUTPUT_FILE = "output";
    private static final String CLI_OPTION_CSV_FILES = "input";
    private static final String CLI_OPTION_SCHEMA = "schema";
    private static final Object COMMENT_STRING = "#";

    public static void main(String[] args) throws GraphIOException {
        Csv2Tg converter = new Csv2Tg();
        converter.getOptions(args);
        converter.process();

        System.out.println("Fini.");
    }

    private Schema schema;
    private String[] csvFiles;
    private Map<VertexClass, CsvReader> vertexInstances;
    private Map<EdgeClass, CsvReader> edgeInstances;
    private Map<CsvReader, String> reader2FilenameMap;
    private CsvReader currentReader;
    private String outputFile;
    private Graph graph;
    private Map<String, Vertex> vertices;

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

    private void setSchema(String filename) throws GraphIOException {
        System.out.print("Loading Schema ... ");
        Schema schema = GraphIO.loadSchemaFromFile(filename);

        if (!isCompiled(schema)) {
            System.out.print("compiling ... ");
            schema.compile(CodeGeneratorConfiguration.MINIMAL);
        }
        setSchema(schema);
        System.out.println("done.");
    }

    private boolean isCompiled(Schema schema) {
        try {
            Class.forName(schema.getQualifiedName(), true, SchemaClassManager.instance(schema.getQualifiedName()));
        } catch (ClassNotFoundException ex) {
            return false;
        }
        return true;
    }

    final protected OptionHandler createOptionHandler() {
        String toolString = "java " + this.getClass()
                .getName();
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
        setUp();
        try {
            loadCsvFiles();
            processVertexFiles();
            processEdgeFiles();

            // Prints the key value from the csv-file of vertices, which do not
            // have any edge attached!
            // for (Vertex v : graph.vertices()) {
            // if (v.getDegree() == 0) {
            // for (Entry<String, Vertex> entry : vertices.entrySet()) {
            // if (v.equals(entry.getValue())) {
            // System.out.println("Missing edges: "
            // + entry.getKey());
            // }
            // }
            // }
            // }

            System.out.println("Finished Processing.");
            System.out.print("Saving Graph ...");
            GraphIO.saveGraphToFile(graph, outputFile, null);
            System.out.print("done.");
        } catch (NoSuchAttributeException e) {
            e.printStackTrace();
        } catch (GraphIOException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("An error occured while processing " + currentReader.getLineNumber() + " in file \""
                    + reader2FilenameMap.get(currentReader) + "\".");
            e.printStackTrace();
        } finally {
            try {
                tearDown();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void setUp() {
        vertices = new HashMap<>();
        vertexInstances = new HashMap<>();
        edgeInstances = new HashMap<>();
        reader2FilenameMap = new HashMap<>();
        graph = schema.createGraph(ImplementationType.GENERIC);
    }

    private void tearDown() throws IOException {
        vertices = null;
        graph = null;

        closeAllReader(vertexInstances.values());
        closeAllReader(edgeInstances.values());
        vertexInstances = null;
        edgeInstances = null;
        reader2FilenameMap = null;
    }

    private void closeAllReader(Collection<CsvReader> readers) throws IOException {
        for (CsvReader reader : readers) {
            reader.close();
        }
    }

    private void processEdgeFiles() throws NoSuchAttributeException, IOException, GraphIOException {
        for (Entry<EdgeClass, CsvReader> entry : edgeInstances.entrySet()) {

            currentReader = entry.getValue();

            System.out.println("\tprocessing file: " + reader2FilenameMap.get(currentReader));

            while (currentReader.readRecord()) {
                createEdge(currentReader, entry.getKey());
            }
        }
    }

    private void processVertexFiles() throws NoSuchAttributeException, IOException, GraphIOException {

        for (Entry<VertexClass, CsvReader> entry : vertexInstances.entrySet()) {

            currentReader = entry.getValue();
            while (currentReader.readRecord()) {
                createVertex(currentReader, entry.getKey());
            }
            System.out.println("\tprocessing file: " + reader2FilenameMap.get(currentReader));
        }
    }

    private void loadCsvFiles() throws NoSuchAttributeException, GraphIOException, IOException {
        for (String csvFile : csvFiles) {
            loadCsvFile(csvFile);
        }
    }

    private void loadCsvFile(String csvFile) throws NoSuchAttributeException, GraphIOException, IOException {
        CsvReader reader = openCvsFile(csvFile);
        reader2FilenameMap.put(reader, csvFile);

        String attributeClassName = reader.getFieldNames()
                .get(0);
        AttributedElementClass<?, ?> aec = schema.getAttributedElementClass(attributeClassName);
        if (aec instanceof VertexClass) {
            vertexInstances.put((VertexClass) aec, reader);
        } else {
            edgeInstances.put((EdgeClass) aec, reader);
        }
    }

    private CsvReader openCvsFile(String csvFile) {
        try {
            // Storing as a global variable is just for debugging purposes.
            CsvReader openFileReader = new CsvReader(
                    new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), "UTF-8")),
                    CsvReader.WITH_FIELDNAMES);
            return openFileReader;
        } catch (FileNotFoundException cause) {
            throw new RuntimeException("An error occured while opening the file \"" + csvFile + "\".", cause);
        } catch (IOException cause) {
            throw new RuntimeException("An error occured while opening the file \"" + csvFile + "\".", cause);
        }
    }

    private void createVertex(CsvReader reader, VertexClass vc) throws NoSuchAttributeException, GraphIOException {
        Vertex vertex = graph.createVertex(vc);

        insertAttribute(vertex, reader, 1);

        String uniqueName = reader.getFieldAt(0);
        if (vertices.containsKey(uniqueName)) {
            throw new RuntimeException(
                    "The unique name \"" + uniqueName + "\" isn't in fact unique. This error occured while "
                            + "processing the file \"" + reader2FilenameMap.get(reader) + "\".");
        }
        vertices.put(uniqueName, vertex);
    }

    private void createEdge(CsvReader reader, EdgeClass ec) throws NoSuchAttributeException, GraphIOException {

        Vertex alpha = getVertex(reader, 1);
        Vertex omega = getVertex(reader, 2);

        Edge edge = graph.createEdge(ec, alpha, omega);

        insertAttribute(edge, reader, 3);

    }

    private void insertAttribute(AttributedElement<?, ?> element, CsvReader reader, int startColumnIndex)
            throws NoSuchAttributeException, GraphIOException {

        List<String> header = reader.getFieldNames();

        for (int index = startColumnIndex; index < header.size(); index++) {

            String attributeName = header.get(index);
            String valueString = reader.getFieldAt(index);

            if (attributeName.equals(COMMENT_STRING) || valueString.isEmpty()) {
                continue;
            }
            String transformedString = transformCsvStringValue(valueString);

            try {
                ((InternalAttributedElement) element).readAttributeValueFromString(attributeName, transformedString);
            } catch (NoSuchAttributeException ex) {
                throw new RuntimeException("The attribute \"" + attributeName + "\" with value \"" + transformedString
                        + "\" in line " + index + " is not a valid attribute name for " + element.getGraphClass()
                                .getQualifiedName(),
                        ex);
            }

        }
    }

    private String transformCsvStringValue(String csvStringValue) {
        if (csvStringValue.startsWith("\"") && csvStringValue.endsWith("\"")) {
            csvStringValue = GraphIO.toUtfString(csvStringValue.substring(1, csvStringValue.length() - 1)
                    .replace("\\\"", "\""));
        }
        return csvStringValue;
    }

    private Vertex getVertex(CsvReader reader, int fieldNumber) {
        String vertexName = reader.getFieldAt(fieldNumber);
        Vertex vertex = vertices.get(vertexName);
        if (vertex == null) {
            throw new RuntimeException("Couldn't find vertex \"" + vertexName + "\" in line: " + reader.getLineNumber()
                    + "in file \"" + reader2FilenameMap.get(reader) + "\".");
        }
        return vertex;
    }

    public String[] getCsvFiles() {
        return csvFiles;
    }

    public void setCsvFiles(String[] vertexFiles) {
        csvFiles = getFilesInFolder(vertexFiles);
    }

    private String[] getFilesInFolder(String[] filenames) {
        HashSet<String> fileList = new HashSet<>();
        for (String filename : filenames) {

            File file = new File(filename).getAbsoluteFile();
            if (!file.exists()) {
                throw new RuntimeException("File or folder \"" + filename + "\" does not exist!");
            }
            if (file.isDirectory()) {
                File[] content = file.listFiles(this);
                if (content != null) {
                    for (File foundFile : content) {
                        fileList.add(foundFile.getAbsolutePath());
                    }
                }
            } else {
                fileList.add(file.getAbsolutePath());
            }
        }

        if (fileList.isEmpty()) {
            throw new RuntimeException("No csv-files to convert to a tg-file.");
        }

        String[] result = new String[fileList.size()];
        return fileList.toArray(result);
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
