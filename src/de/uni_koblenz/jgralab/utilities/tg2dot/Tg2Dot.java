/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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
package de.uni_koblenz.jgralab.utilities.tg2dot;

import static de.uni_koblenz.jgralab.utilities.tg2dot.greql2.GreqlEvaluatorFacade.ABBREVIATE_EDGE_ATTRIBUTE_NAMES;
import static de.uni_koblenz.jgralab.utilities.tg2dot.greql2.GreqlEvaluatorFacade.PRINT_DOMAIN_NAMES;
import static de.uni_koblenz.jgralab.utilities.tg2dot.greql2.GreqlEvaluatorFacade.PRINT_EDGE_ATTRIBUTES;
import static de.uni_koblenz.jgralab.utilities.tg2dot.greql2.GreqlEvaluatorFacade.PRINT_ELEMENT_SEQUENCE_INDICES;
import static de.uni_koblenz.jgralab.utilities.tg2dot.greql2.GreqlEvaluatorFacade.PRINT_INCIDENCE_INDICES;
import static de.uni_koblenz.jgralab.utilities.tg2dot.greql2.GreqlEvaluatorFacade.PRINT_ROLENAMES;
import static de.uni_koblenz.jgralab.utilities.tg2dot.greql2.GreqlEvaluatorFacade.SHORTEN_STRINGS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.utilities.common.dot.DotWriter;
import de.uni_koblenz.jgralab.utilities.common.dot.GraphType;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.GraphLayout;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.GraphLayoutFactory;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.Definition;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.ElementDefinition;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition.TypeDefinition;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.writer.AbstractGraphLayoutWriter;
import de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.writer.json.JsonGraphLayoutWriter;
import de.uni_koblenz.jgralab.utilities.tg2dot.greql2.GreqlEvaluatorFacade;
import de.uni_koblenz.jgralab.utilities.tg2whatever.Tg2Whatever;

/**
 * Tg2Dot2 takes a graph layout and a JGraLab graph and transforms the graph
 * into a DOT-graph of GraphViz.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class Tg2Dot extends Tg2Whatever {

	private static final int EXIT_ALL_FINE = 0;

	/**
	 * Indicates to abbreviate all edge attribute names.
	 */
	private boolean abbreviateEdgeAttributeNames = false;

	/**
	 * Indicates to print the incidence indices.
	 */
	private boolean printIncidenceIndices = false;

	/**
	 * Indicates to print the element sequence index number.
	 */
	private boolean printElementSequenceIndices = false;

	/**
	 * Name of the graph layout file.
	 */
	private String graphLayoutFilename;

	/**
	 * Holds the loaded graph layout.
	 */
	private GraphLayout layout;

	/**
	 * Provides a GreqlEvaluator with convenient functionality.
	 */
	private GreqlEvaluatorFacade evaluator;

	/**
	 * Provides a dot file writer with a simple interface.
	 */
	private DotWriter writer;

	/**
	 * A set of AttributedElementClasses of Edges, which should be printed as
	 * reversed dot edges. This will not affect the appearance in dot, but will
	 * affect the layout process of GraphViz.
	 */
	private Set<AttributedElementClass> reversedEdgeClasses;

	/**
	 * Specifies the type of file, which will be passed to dot in order to
	 * generate an output.
	 */
	private String dotBuildOutputType;

	private boolean useJsonGraphLayoutReader;

	private boolean debugIterations;

	private boolean debugOptimization;

	private Level jGraLabLogLevel;

	/**
	 * @param args
	 * @throws IOException
	 * @throws JsonParseException
	 * @throws GraphIOException
	 */
	public static void main(String[] args) throws JsonParseException,
			IOException, GraphIOException {

		Tg2Dot converter = new Tg2Dot();
		converter.getOptions(args);

		System.out.print("Starting processing of graph...");
		converter.convert();
		System.out.println("Finished Processing.");
	}

	public static Tg2Dot createConverterAndSetAttributes(Graph graph,
			boolean reversedEdges, String outputFileName) {

		Tg2Dot converter = new Tg2Dot();
		converter.setGraph(graph);
		converter.setReversedEdges(reversedEdges);
		converter.setPrintEdgeAttributes(true);
		// TODO RANKSEP
		// t2d.setRanksep(0.5);
		converter.setOutputFile(outputFileName);

		return converter;
	}

	public static void convertGraph(Graph graph, String outputFileName,
			boolean reversedEdges,
			Class<? extends AttributedElement>... reversedEdgeTypes) {

		Tg2Dot converter = createConverterAndSetAttributes(graph,
				reversedEdges, outputFileName);

		if (reversedEdgeTypes != null) {
			HashSet<Class<? extends AttributedElement>> revEdgeTypes = new HashSet<Class<? extends AttributedElement>>();
			Collections.addAll(revEdgeTypes, reversedEdgeTypes);
			converter.setReversedEdgeTypes(revEdgeTypes);
		}

		converter.convert();
	}

	public static void convertGraph(Graph graph, String outputFileName,
			boolean reversedEdges) {
		convertGraph(graph, outputFileName, reversedEdges,
				(Class<? extends AttributedElement>[]) null);
	}

	public static void convertGraph(BooleanGraphMarker marker,
			String outputFileName, boolean reversedEdges) {

		Tg2Dot converter = createConverterAndSetAttributes(marker.getGraph(),
				reversedEdges, outputFileName);

		converter.setGraphMarker(marker);

	}

	/**
	 * Initializes all data structures.
	 */
	public Tg2Dot() {
		reversedEdgeClasses = new HashSet<AttributedElementClass>();
	}

	@Override
	protected void getAdditionalOptions(CommandLine comLine) {
		initializeGraphAndSchema();

		if (comLine.hasOption('j') && comLine.hasOption('p')) {
			throw new RuntimeException(
					"Only a JSON- or a PList-layout file can be declared. Not both!");
		}
		useJsonGraphLayoutReader = comLine.hasOption('j');
		graphLayoutFilename = useJsonGraphLayoutReader ? comLine
				.getOptionValue('j') : comLine.getOptionValue('p');

		printIncidenceIndices = comLine.hasOption('i');
		printElementSequenceIndices = comLine.hasOption('m');
		dotBuildOutputType = comLine.hasOption('t') ? comLine
				.getOptionValue('t') : null;
	}

	@Override
	protected void addAdditionalOptions(OptionHandler optionHandler) {

		Option pListLayout = new Option(
				"p",
				"pListLayout",
				true,
				"(optional): declares a PList-layout file, which should be used to lay out the given graph.");
		pListLayout.setRequired(false);
		optionHandler.addOption(pListLayout);

		Option jsonLayout = new Option(
				"j",
				"jsonLayout",
				true,
				"(optional): declares a JSON-layout file, which should be used to lay out the given graph.");
		jsonLayout.setRequired(false);
		optionHandler.addOption(jsonLayout);

		Option incidenceIndices = new Option("i", "incidenceIndices", false,
				"(optional): prints the incidence index to every edge.");
		incidenceIndices.setRequired(false);
		optionHandler.addOption(incidenceIndices);

		Option elementSequenceIndices = new Option("m",
				"elementSequenceIndices", false,
				"(optional): prints the element sequence index of every vertex and edge.");
		elementSequenceIndices.setRequired(false);
		optionHandler.addOption(elementSequenceIndices);

		Option dotBuildOutputType = new Option(
				"t",
				"dotBuildOutputType",
				true,
				"(optional): determins the output format in order to build graphical file from the generated dot file.");
		dotBuildOutputType.setRequired(false);
		optionHandler.addOption(dotBuildOutputType);
	}

	@Override
	protected void graphStart(PrintStream out) {
		// Disable debugging to prevent recursive execution and restore it in
		// graphEnd()
		debugIterations = GreqlEvaluator.DEBUG_DECLARATION_ITERATIONS;
		debugOptimization = GreqlEvaluator.DEBUG_OPTIMIZATION;
		GreqlEvaluator.DEBUG_DECLARATION_ITERATIONS = false;
		GreqlEvaluator.DEBUG_OPTIMIZATION = false;
		jGraLabLogLevel = JGraLab.getRootLogger().getLevel();
		JGraLab.setLogLevel(Level.OFF);

		initializeEvaluator();
		initializeGraphLayout();

		setGlobalVariables();
		setCommandLineVariables();

		try {
			createDotWriter(out);

			startDotGraph();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initializes the GreqlEvaluator and sets all known variables.
	 */
	private void initializeEvaluator() {
		evaluator = new GreqlEvaluatorFacade(graph);
	}

	/**
	 * Creates a GraphLayoutFactory and loads the GraphLayout.
	 */
	private void initializeGraphLayout() {
		GraphLayoutFactory factory = new GraphLayoutFactory(evaluator);

		File layoutFile = new File(graphLayoutFilename);
		if (useJsonGraphLayoutReader) {
			factory.setJsonGraphLayoutFilename(layoutFile);
		} else {
			factory.setPListGraphLayoutFilename(layoutFile);
		}

		layout = factory.createGraphLayout();
	}

	/**
	 * Sets all known global variables in the {@link GreqlEvaluator}.
	 */
	private void setGlobalVariables() {
		evaluator.setVariablesWithGreqlValues(layout.getGlobalVariables());
	}

	/**
	 * Sets all provided command line switch in the {@link GreqlEvaluator}.
	 */
	private void setCommandLineVariables() {
		evaluator.setVariable(PRINT_ROLENAMES, roleNames);
		evaluator.setVariable(PRINT_INCIDENCE_INDICES, printIncidenceIndices);
		evaluator.setVariable(PRINT_ELEMENT_SEQUENCE_INDICES,
				printElementSequenceIndices);
		evaluator.setVariable(PRINT_DOMAIN_NAMES, domainNames);
		evaluator.setVariable(SHORTEN_STRINGS, shortenStrings);
		evaluator.setVariable(ABBREVIATE_EDGE_ATTRIBUTE_NAMES,
				abbreviateEdgeAttributeNames);
		evaluator.setVariable(PRINT_EDGE_ATTRIBUTES, edgeAttributes);
	}

	/**
	 * Creates a {@link DotWriter}.
	 * 
	 * @param out
	 *            Provides stream, the DotWriter will use.
	 * @throws FileNotFoundException
	 */
	private void createDotWriter(PrintStream out) throws FileNotFoundException {
		writer = new DotWriter(out);
	}

	/**
	 * Starts the Graph in the output file.
	 */
	private void startDotGraph() {
		StringBuilder sb = new StringBuilder();
		// Names have to start with a character
		sb.append(graph.getM1Class().getSimpleName());
		sb.append("_");
		sb.append(graph.getId().replace('-', '_'));
		sb.append("__");
		sb.append(graph.getGraphVersion());
		writer.startGraph(GraphType.DIRECTED, sb.toString());
	}

	@Override
	protected void printVertex(PrintStream out, Vertex vertex) {

		Definition definition = getCorrespondingDefinition(vertex);
		evaluator.setStaticVariablesOfGreqlEvaluator(vertex
				.getAttributedElementClass());
		writeLayoutedVertex(vertex, definition);
	}

	/**
	 * Returns the responsible {@link TypeDefinition} or
	 * {@link ElementDefinition} for the specified {@link AttributedElement}.
	 * 
	 * @param attributedElement
	 *            Given {@link AttributedElement}.
	 * @return Responsible {@link Definition}.
	 */
	private Definition getCorrespondingDefinition(
			AttributedElement attributedElement) {
		if (layout.isDefinedbyElementDefinitions(attributedElement)) {
			return constructSpecificElementDefinition(attributedElement);
		} else {
			TypeDefinition definition = layout
					.getTypeDefinition(attributedElement);
			return definition;
		}
	}

	/**
	 * Constructs an {@link ElementDefinition} for the given
	 * {@link AttributedElement}.
	 * 
	 * @param element
	 *            Given AttributedElement.
	 * @return {@link ElementDefinition} for the provided
	 *         {@link AttributedElement}.
	 */
	private Definition constructSpecificElementDefinition(
			AttributedElement element) {

		// Retrieves corresponding underlying TypeDefinition
		Definition definition = layout.getTypeDefinition(element);
		definition = definition.clone();

		// Overwrites all Attributes redefined by ElementDefinitions in the
		// order of declaration
		for (ElementDefinition elementDefinition : layout
				.getElementDefinitions()) {
			if (elementDefinition.hasElement(element)) {
				definition.overwriteAttributes(elementDefinition);
			}
		}
		return definition;
	}

	/**
	 * Evaluates all attributes of a given {@link Vertex} and prints via the
	 * {@link DotWriter}.
	 * 
	 * @param vertex
	 *            Provides Vertex.
	 * @param definition
	 *            Definition object used to layout the given Vertex.
	 */
	private void writeLayoutedVertex(Vertex vertex, Definition definition) {

		// Resets changed variables in the GreqlEvaluator
		evaluator.setVariablesOfGreqlEvaluator(vertex,
				getCurrentElementSequenceIndex());

		// Retrieves the unified vertex name ("v1", "v2", ... ) and the
		// evaluated style attribute list.
		String name = getVertexName(vertex);
		Map<String, String> evalutatedList = createEvaluatedStyleAttributeList(definition);

		writer.writeNode(name, evalutatedList);
	}

	/**
	 * Returns unified vertex names. A vertex name has the prefix 'v' followed
	 * by the {@link Vertex} id.
	 * 
	 * @param vertex
	 *            Given {@link Vertex}
	 * @return Unified vertex name.
	 */
	private String getVertexName(Vertex vertex) {
		return "v" + vertex.getId();
	}

	/**
	 * Creates a evaluated style attribute list of the given {@link Definition}.
	 * <b>Note:</b><br>
	 * The current element has been set to the {@link GreqlEvaluator} via the
	 * method
	 * {@link GreqlEvaluatorFacade#setVariablesOfGreqlEvaluator(AttributedElement, int)}
	 * .
	 * 
	 * @param spec
	 *            Given {@link Definition} with the style attributes and their
	 *            queries.
	 * @return Evaluated style attribute list with attribute names as key and
	 *         the evaluated value string as value.
	 */
	private Map<String, String> createEvaluatedStyleAttributeList(
			Definition spec) {

		Map<String, String> evaluatedList = new HashMap<String, String>();

		for (String attributeName : spec.getAttributeNames()) {
			try {
				String query = spec.getAttributeValue(attributeName);
				String result = evaluator.evaluateToString(query);
				evaluatedList.put(attributeName, result);
			} catch (EvaluateException ex) {
				throw new RuntimeException("Error: The query of attribute "
						+ attributeName + " has produced an error!", ex);
			}
		}
		return evaluatedList;
	}

	@Override
	protected void printEdge(PrintStream out, Edge edge) {
		Definition definition = getCorrespondingDefinition(edge);
		evaluator.setStaticVariablesOfGreqlEvaluator(edge
				.getAttributedElementClass());
		writeLayoutedEdge(edge, definition);
	}

	/**
	 * Evaluates all attributes of a given {@link Edge} and prints via the
	 * {@link DotWriter}.
	 * 
	 * @param edge
	 *            Provides Edge.
	 * @param definition
	 *            Definition object used to layout the given Vertex.
	 */
	private void writeLayoutedEdge(Edge edge, Definition definition) {

		// Resets changed variables in the GreqlEvaluator
		evaluator.setVariablesOfGreqlEvaluator(edge,
				getCurrentElementSequenceIndex());

		// Reverts the direction of the if isReversedEdge is true
		// This will not change the style, but will change the layout process in
		// GraphViz
		boolean isReversedEdge = isReversedEdge(edge);

		// Simple swap
		Vertex alpha = !isReversedEdge ? edge.getAlpha() : edge.getOmega();
		Vertex omega = !isReversedEdge ? edge.getOmega() : edge.getAlpha();

		// Retrieves the unified names of the alpha and omega edges.
		String alphaVertex = getVertexName(alpha);
		String omegaVertex = getVertexName(omega);

		// Retrieves the evaluated style attribute list
		Map<String, String> evaluatedList = createEvaluatedStyleAttributeList(definition);
		// Swaps all reversible style attributes.
		if (isReversedEdge) {
			reverseEdgeAttributes(evaluatedList);
		}

		writer.writeEdge(alphaVertex, omegaVertex, evaluatedList);
	}

	/**
	 * Checks whether or not the given {@link Edge} belongs to a
	 * {@link EdgeClass}, which should be reversed.
	 * 
	 * @param e
	 *            Given Edge, which should be checked.
	 * @return Return true, if the given Edge should be reversed.
	 */
	private boolean isReversedEdge(Edge e) {

		Boolean isReversed = reversedEdgeClasses.contains(e
				.getAttributedElementClass());
		return reversedEdges ^ isReversed;
	}

	/**
	 * Reverses all reversible style attributes in the provided evaluted style
	 * attribute list.
	 * 
	 * @param evaluatedList
	 *            Given evaluated style attribute list as {@link Map}.
	 */
	private void reverseEdgeAttributes(Map<String, String> evaluatedList) {

		for (Entry<String, String> entry : DotWriter.reversableEdgeAttributePairs
				.entrySet()) {
			swapAttributes(entry.getKey(), entry.getValue(), evaluatedList);
		}
	}

	/**
	 * Swaps two key-value pairs in a map.
	 * 
	 * @param head
	 *            Key of the first key-value pair.
	 * @param tail
	 *            Key of the second key-value pair.
	 * @param evaluatedList
	 *            Evaluated style attribute list.
	 */
	private void swapAttributes(String head, String tail,
			Map<String, String> evaluatedList) {
		String headValue = evaluatedList.remove(head);
		String tailValue = evaluatedList.remove(tail);

		if (headValue != null) {
			evaluatedList.put(tail, headValue);
		}
		if (tailValue != null) {
			evaluatedList.put(head, tailValue);
		}
	}

	@Override
	protected void graphEnd(PrintStream out) {
		closeOutputStream();

		// writeGraphLayoutToJsonFile();
		executeDot();

		GreqlEvaluator.DEBUG_DECLARATION_ITERATIONS = debugIterations;
		GreqlEvaluator.DEBUG_OPTIMIZATION = debugOptimization;
		JGraLab.setLogLevel(jGraLabLogLevel);
	}

	/**
	 * Closes the output stream in the {@link DotWriter}.
	 */
	private void closeOutputStream() {
		writer.close();
		writer = null;
	}

	/**
	 * Executes the Dot program of GraphViz.
	 */
	private void executeDot() {
		if (dotBuildOutputType == null) {
			return;
		}

		System.out.print("Processing dot-file with GraphViz as \""
				+ dotBuildOutputType + "\"...");

		try {
			File outputFile = new File(outputName);
			String dotFile = outputFile.getAbsolutePath();
			String formatedFile = dotFile + "." + dotBuildOutputType;
			int lastIntPosition = dotFile.lastIndexOf('.');

			if (lastIntPosition != -1) {
				formatedFile = dotFile.substring(0, lastIntPosition) + "."
						+ dotBuildOutputType;
			}

			String executionString = "dot -T" + dotBuildOutputType + " "
					+ dotFile + " -o" + formatedFile;
			Process p = Runtime.getRuntime().exec(executionString);
			p.waitFor();
			if (p.exitValue() == EXIT_ALL_FINE) {
				System.out.println(" done.");
			} else {
				System.out.println(" error ocurred while executing DOT!");
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected String stringQuote(String s) {
		throw new RuntimeException("This method should have been called!");
	}

	/**
	 * Writes the current GraphLayout to a JsonFile. <b>Note:</b><br>
	 * The written file will not be identical to the read graph layout.
	 */
	public void writeGraphLayoutToJsonFile() {
		if (layout == null) {
			throw new RuntimeException("There is no graph layout present.");
		}

		AbstractGraphLayoutWriter writer = new JsonGraphLayoutWriter();
		try {
			writer.startProcessing(graphLayoutFilename + ".parsed", layout);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * All edge instances of an edge type contained in the given set
	 * <code>reversedEdgeTypes</code> (or subtypes) will be printed reversed.
	 * This is especially useful when certain conceptual edges are modeled as
	 * nodes, like: State <--{ComesFrom} Transition -->{GoesTo}. Here, reversing
	 * the direction of either ComesFrom or GoesTo results in much nicer
	 * layouts.
	 * 
	 * @param reversedEdgeTypes
	 *            the set of edge types whose instances should be printed
	 *            reversed
	 */
	public void setReversedEdgeTypes(
			Set<Class<? extends AttributedElement>> reversedEdgeTypes) {

		// Copies the current set in order to manipulate it.
		reversedEdgeTypes = new HashSet<Class<? extends AttributedElement>>(
				reversedEdgeTypes);

		buildReversedEdgeClassSet(reversedEdgeTypes);
		checkForMissedClasses(reversedEdgeTypes);
		// apply hierarchy
		addAllSubClassesOfAllReversedEdgeClasses();
	}

	/**
	 * Converts the existing set of classes into a set of
	 * {@link AttributedElementClass}es.
	 * 
	 * @param reversedEdgeTypes
	 *            Set of classes of Edges, which should be reversed.
	 */
	private void buildReversedEdgeClassSet(
			Set<Class<? extends AttributedElement>> reversedEdgeTypes) {
		reversedEdgeClasses = new HashSet<AttributedElementClass>();
		for (EdgeClass edgeClass : graph.getSchema()
				.getEdgeClassesInTopologicalOrder()) {
			if (!edgeClass.isInternal()
					&& reversedEdgeTypes.remove(edgeClass.getM1Class())) {
				reversedEdgeClasses.add(edgeClass);
			}
		}
		if (reversedEdgeTypes.remove(Edge.class)) {
			reversedEdgeClasses.add(graph.getSchema()
					.getAttributedElementClass("Edge"));
		}
	}

	/**
	 * Checks for missed classes
	 * 
	 * @param reversedEdgeTypes
	 */
	private void checkForMissedClasses(
			Set<Class<? extends AttributedElement>> reversedEdgeTypes) {
		if (!reversedEdgeTypes.isEmpty()) {
			throw new RuntimeException(
					"Warning: Several specified class could be associated with an AttributedElementClass.\nList: "
							+ reversedEdgeTypes);
		}
	}

	/**
	 * Adds sub classes of reversed Edges.
	 */
	private void addAllSubClassesOfAllReversedEdgeClasses() {
		Set<AttributedElementClass> classes = new HashSet<AttributedElementClass>(
				reversedEdgeClasses);
		for (AttributedElementClass attr : classes) {
			reversedEdgeClasses.addAll(attr.getAllSubClasses());
		}
	}

	/**
	 * Return a flag indicating that incidence numbers should be included in the
	 * graph layout process.
	 * 
	 * @return True, if incidence numbers should be be printed.
	 */
	public boolean printsIncidenceNumbers() {
		return printIncidenceIndices;
	}

	/**
	 * @param printIncidenceNumbers
	 *            If true, then the incidence numbers will be printed near the
	 *            start and end points of edges.
	 */
	public void setPrintIncidenceNumbers(boolean printIncidenceNumbers) {
		printIncidenceIndices = printIncidenceNumbers;
	}

	public boolean isAbbreviateAttributeNames() {
		return abbreviateEdgeAttributeNames;
	}

	public void setAbbreviateAttributeNames(boolean abbreviateAttributeNames) {
		abbreviateEdgeAttributeNames = abbreviateAttributeNames;
	}

	public GraphLayout getGraphLayout() {
		return layout;
	}

	public void setGraphLayout(GraphLayout layout) {
		this.layout = layout;
	}

	public boolean isAbbreviateEdgeAttributeNames() {
		return abbreviateEdgeAttributeNames;
	}

	public void setAbbreviateEdgeAttributeNames(
			boolean abbreviateEdgeAttributeNames) {
		this.abbreviateEdgeAttributeNames = abbreviateEdgeAttributeNames;
	}

	public boolean isPrintIncidenceIndices() {
		return printIncidenceIndices;
	}

	public void setPrintIncidenceIndices(boolean printIncidenceIndices) {
		this.printIncidenceIndices = printIncidenceIndices;
	}

	public boolean isPrintElementSequenceIndices() {
		return printElementSequenceIndices;
	}

	public void setPrintElementSequenceIndices(
			boolean printElementSequenceIndices) {
		this.printElementSequenceIndices = printElementSequenceIndices;
	}

	public String getGraphLayoutFilename() {
		return graphLayoutFilename;
	}

	public void setJsonGraphLayoutFilename(String graphLayoutFilename) {
		useJsonGraphLayoutReader = true;
		this.graphLayoutFilename = graphLayoutFilename;
	}

	public void setPListGraphLayoutFilename(String graphLayoutFilename) {
		useJsonGraphLayoutReader = false;
		this.graphLayoutFilename = graphLayoutFilename;
	}

	public Set<AttributedElementClass> getReversedEdgeClasses() {
		return reversedEdgeClasses;
	}

	public void setReversedEdgeClasses(
			Set<AttributedElementClass> reversedEdgeClasses) {
		this.reversedEdgeClasses = reversedEdgeClasses;
	}

	public String getDotBuildOutputType() {
		return dotBuildOutputType;
	}

	public void setDotBuildOutputType(String dotBuildOutputType) {
		this.dotBuildOutputType = dotBuildOutputType;
	}
}