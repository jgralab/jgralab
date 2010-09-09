/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 * 
 *               ist@uni-koblenz.de
 * 
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.uni_koblenz.jgralab.utilities.tg2whatever;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;
import de.uni_koblenz.jgralab.schema.Schema;

public abstract class Tg2Whatever {

	protected Graph graph = null;

	protected String graphFileName = null;

	protected String schemaFileName = null;

	protected Schema schema = null;

	protected String outputName = null;

	protected boolean domainNames = false;

	protected boolean edgeAttributes = false;

	protected boolean reversedEdges = false;

	private int currentElementSequenceIndex = -1;

	/**
	 * @return the reversedEdges
	 */
	public boolean isReversedEdges() {
		return reversedEdges;
	}

	/**
	 * @param reversedEdges
	 *            the reversedEdges to set
	 */
	public void setReversedEdges(boolean reversedEdges) {
		this.reversedEdges = reversedEdges;
	}

	protected boolean roleNames = false;

	protected boolean shortenStrings = false;

	protected BooleanGraphMarker marker = null;

	public Tg2Whatever() {
		// System.err.println("outputName = '" + outputName + "'");
		// System.err.println("domainNames = " + domainNames);
		// System.err.println("edgeAttributes = " + edgeAttributes);
		// System.err.println("reversedEdges = " + reversedEdges);
		// System.err.println("roleNames = " + roleNames);
		// System.err.println("shortenStringsNames = " + shortenStrings);
	}

	/**
	 * sets the graph marker. If this is not null, only vertices and edges that
	 * are marked with the marker will be printed to the dot-file
	 */
	public void setGraphMarker(BooleanGraphMarker m) {
		marker = m;
	}

	/**
	 * toggles which schema to use for conversion
	 */
	public void setSchema(Schema s) {
		schema = s;
	}

	/**
	 * toggles which schema to use for conversion
	 */
	public void setSchema(String fileName) {
		schemaFileName = fileName;
	}

	/**
	 * toggles which graph to convertes
	 */
	public void setGraph(Graph g) {
		graph = g;
	}

	/**
	 * loads the graph from file
	 */
	public void setGraph(String fileName) throws GraphIOException {
		graphFileName = fileName;
		graph = GraphIO.loadSchemaAndGraphFromFile(graphFileName,
				CodeGeneratorConfiguration.WITHOUT_TRANSACTIONS,
				new ConsoleProgressFunction());
	}

	/**
	 * toggles the name of the output file
	 */
	public void setOutputFile(String file) {
		outputName = file;
	}

	/**
	 * toggles wether to print rolenames or not
	 * 
	 * @param print
	 */
	public void setPrintRoleNames(boolean print) {
		roleNames = print;
	}

	/**
	 * toggles wether to shorten long strings
	 * 
	 * @param shorten
	 */
	public void setShortenStrings(boolean shorten) {
		shortenStrings = shorten;
	}

	/**
	 * toggles wether to print edgeAttributes or not
	 * 
	 * @param print
	 */
	public void setPrintEdgeAttributes(boolean print) {
		edgeAttributes = print;
	}

	/**
	 * toggles wether to print reversed edges or not
	 * 
	 * @param print
	 */
	public void setPrintReversedEdges(boolean print) {
		reversedEdges = print;
	}

	/**
	 * toggles wether to print domain names of attributes or not
	 * 
	 * @param print
	 */
	public void setPrintDomainNames(boolean print) {
		domainNames = print;
	}

	public void printGraph() {
		initializeGraphAndSchema();
		try {
			PrintStream out = initializeOutputStream();
			graphStart(out);
			printVertices(out);
			printEdges(out);
			graphEnd(out);
		} catch (FileNotFoundException e) {
			System.err.println("File '" + outputName
					+ "' could not be created.");
			System.exit(1);
		}
	}

	private PrintStream initializeOutputStream() throws FileNotFoundException {
		PrintStream out;
		if (outputName.equals("")) {
			out = System.out;
		} else {
			out = new PrintStream(new FileOutputStream(outputName));
		}
		return out;
	}

	private void printEdges(PrintStream out) {
		currentElementSequenceIndex = 0;
		for (Edge e : graph.edges()) {
			currentElementSequenceIndex++;
			if (marker == null || marker.isMarked(e)) {
				printEdge(out, e);
			}
		}
	}

	private void printVertices(PrintStream out) {
		currentElementSequenceIndex = 0;
		for (Vertex v : graph.vertices()) {
			currentElementSequenceIndex++;
			if (marker == null || marker.isMarked(v)) {
				printVertex(out, v);
			}
		}
	}

	public int getCurrentElementSequenceIndex() {
		return currentElementSequenceIndex;
	}

	private void loadGraph() {
		try {
			System.out.println("Loading graph from file " + graphFileName);
			graph = GraphIO.loadGraphFromFileWithStandardSupport(graphFileName,
					schema, new ConsoleProgressFunction());
			System.out.println("Graph loaded");
		} catch (GraphIOException ex) {
			System.err.println("Graph in file '" + graphFileName
					+ "' could not be read.");
			ex.printStackTrace();
			System.exit(1);
		}
	}

	private void loadSchemaFromGraph() {
		try {
			System.out.println("Loading Schema from Graph");
			schema = GraphIO.loadSchemaFromFile(graphFileName);
			schema.compile(CodeGeneratorConfiguration.MINIMAL);
			System.out.println("Schema loaded");
		} catch (GraphIOException ex) {
			System.err.println("Graph in file '" + graphFileName
					+ "' could not be read.");
			ex.printStackTrace();
			System.exit(1);
		}
	}

	private void loadSchema() {
		try {
			System.out.println("Loaded schema");
			schema = GraphIO.loadSchemaFromFile(schemaFileName);
			System.out.println("Schema loaded");
		} catch (GraphIOException ex) {
			System.err.println("Schema in file '" + schemaFileName
					+ "' could not be read.");
			System.exit(1);
		}
	}

	protected abstract void graphStart(PrintStream out);

	protected abstract void graphEnd(PrintStream out);

	protected abstract void printVertex(PrintStream out, Vertex v);

	protected abstract void printEdge(PrintStream out, Edge e);

	/**
	 * replaces characters in the given string by the escape sequences that are
	 * appropriate for the specific output format
	 * 
	 * @param s
	 * @return
	 */
	protected abstract String stringQuote(String s);

	protected void getOptions(String[] args) {
		CommandLine comLine = processCommandLineOptions(args);
		assert comLine != null;

		// processing of arguments and setting member variables accordingly
		String graphName = null;
		String schemaName = null;
		if (comLine.hasOption("g")) {
			try {
				graphName = comLine.getOptionValue("g");
				setGraph(graphName);
			} catch (GraphIOException e) {
				System.err.println("Coundn't load graph in file '" + graphName
						+ "': " + e.getMessage());
				if (e.getCause() != null) {
					e.getCause().printStackTrace();
				}
				System.exit(1);
			}
		}
		if (comLine.hasOption("o")) {
			outputName = comLine.getOptionValue("o");
		}
		if (outputName == null) {
			outputName = "";
		}
		if (comLine.hasOption("a")) {
			schemaName = comLine.getOptionValue("a");
			setSchema(schemaName);
		}
		reversedEdges = comLine.hasOption("r");
		domainNames = comLine.hasOption("d");
		edgeAttributes = comLine.hasOption("e");
		roleNames = comLine.hasOption("n");
		shortenStrings = comLine.hasOption("s");

		getAdditionalOptions(comLine);
	}

	protected void getAdditionalOptions(CommandLine comLine) {
	}

	protected void initializeGraphAndSchema() {
		if (graph == null) {
			if (schema == null) {
				if (schemaFileName != null) {
					loadSchema();
				} else {
					loadSchemaFromGraph();
				}

			}
			loadGraph();
		}
	}

	final protected CommandLine processCommandLineOptions(String[] args) {
		OptionHandler oh = createOptionHandler();
		addAdditionalOptions(oh);
		return oh.parse(args);
	}

	protected void addAdditionalOptions(OptionHandler optionHandler) {
	}

	final protected OptionHandler createOptionHandler() {
		String toolString = "java " + this.getClass().getName();
		String versionString = JGraLab.getInfo(false);
		OptionHandler oh = new OptionHandler(toolString, versionString);

		Option graph = new Option("g", "graph", true,
				"(required): the graph to be converted");
		graph.setRequired(true);
		graph.setArgName("file");
		oh.addOption(graph);

		Option alternativeSchema = new Option(
				"a",
				"alternative-schema",
				true,
				"(optional): the schema that should be used instead of the one included in the graph file");
		alternativeSchema.setRequired(false);
		graph.setArgName("file");
		oh.addOption(alternativeSchema);

		Option domains = new Option("d", "domains", false,
				"(optional): if set, domain names of attributes will be printed");
		domains.setRequired(false);
		oh.addOption(domains);

		Option edgeAttributes = new Option("e", "edgeattr", false,
				"(optional): if set, edge attributes will be printed");
		edgeAttributes.setRequired(false);
		oh.addOption(edgeAttributes);

		Option rolenames = new Option("n", "rolenames", false,
				"(optional): if set, role names will be printed");
		rolenames.setRequired(false);
		oh.addOption(rolenames);

		Option output = new Option("o", "output", true,
				"(optional): the output file name, or empty for stdout");
		output.setRequired(true);
		output.setArgName("file");
		oh.addOption(output);

		Option reversed = new Option(
				"r",
				"reversed",
				false,
				"(optional): useful if edges run from child nodes to their parents results in a tree with root node at top");
		reversed.setRequired(false);
		oh.addOption(reversed);

		Option shortenStrings = new Option("s", "shorten-strings", false,
				"(optional): if set, strings are shortened");
		shortenStrings.setRequired(false);
		oh.addOption(shortenStrings);
		return oh;
	}
}
