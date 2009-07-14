package de.uni_koblenz.jgralab.utilities.tg2xml;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.GraphMarker;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.ProgressFunctionImpl;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.jgralab2owl.IndentingXMLStreamWriter;

public class Tg2xml extends GraphVisitor {

	private String prefix;
	private String schemaLocation;
	private OutputStream outputStream;
	private IndentingXMLStreamWriter writer;
	// private GraphMarker<Integer> fromEdgeMarker;
	// private GraphMarker<Integer> toEdgeMarker;
	private GraphMarker<IncidencePositionMark> incidencePositionMarker;

	private class IncidencePositionMark {
		public int fseq, tseq;
	}

	private String namespaceURI;

	public Tg2xml(OutputStream outputStream, Graph graph,
			String nameSpacePrefix, String schemaLocation) throws IOException,
			XMLStreamException {
		super(graph);
		incidencePositionMarker = new GraphMarker<IncidencePositionMark>(graph);

		Schema schema = graph.getSchema();
		String qualifiedName = schema.getQualifiedName();

		this.prefix = nameSpacePrefix;
		this.schemaLocation = schemaLocation;

		this.namespaceURI = generateURI(qualifiedName);
		this.outputStream = outputStream;
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		writer = new IndentingXMLStreamWriter(factory.createXMLStreamWriter(
				outputStream, "UTF-8"), 1);
		writer.setIndentationChar('\t');

	}

	public static String generateURI(String qualifiedName) {
		qualifiedName = qualifiedName.replace('_', '-');
		String[] uri = qualifiedName.split("\\.");

		String namespaceURI = "http://";
		if (uri.length > 1) {
			namespaceURI += uri[1] + "." + uri[0];

			for (int i = 2; i < uri.length; i++) {
				namespaceURI += "/" + uri[i];
			}

		}

		return namespaceURI;
	}

	protected void preVisitor() {
		try {
			writer.writeStartDocument("UTF-8", "1.0");

			writer.writeStartElement(prefix, graph.getAttributedElementClass()
					.getQualifiedName(), namespaceURI);
			writer.writeNamespace(prefix, namespaceURI);
			writer.writeNamespace("xsi",
					XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
			writer.writeAttribute("xsi:schemaLocation", namespaceURI + " "
					+ schemaLocation);
			writer.writeAttribute("id", "g" + graph.getId());
			writeAttributes(graph);

		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void visitVertex(Vertex v) throws XMLStreamException {
		// write vertex
		writer.writeEmptyElement(v.getAttributedElementClass()
				.getQualifiedName());
		writer.writeAttribute("id", "v" + v.getId());
		writeAttributes(v);
		// iterate over incidences and mark these edges
		int i = 1;
		for (Edge currentIncidentEdge : v.incidences()) {
			IncidencePositionMark currentMark = incidencePositionMarker
					.getMark(currentIncidentEdge);
			if (currentMark == null) {
				currentMark = new IncidencePositionMark();
				incidencePositionMarker.mark(currentIncidentEdge, currentMark);
			}
			if (currentIncidentEdge.isNormal()) {
				currentMark.fseq = i;
			} else {
				currentMark.tseq = i;
			}
			i++;
		}
	}

	protected void visitEdge(Edge e) throws XMLStreamException {
		IncidencePositionMark currentMark = incidencePositionMarker.getMark(e);
		writer.writeEmptyElement(e.getAttributedElementClass()
				.getQualifiedName());
		writer.writeAttribute("from", "v" + e.getAlpha().getId());
		writer.writeAttribute("fseq", Integer.toString(currentMark.fseq));
		writer.writeAttribute("to", "v" + e.getOmega().getId());
		writer.writeAttribute("tseq", Integer.toString(currentMark.tseq));
		writeAttributes(e);
	}

	protected void postVisitor() throws XMLStreamException, IOException {
		writer.writeEndDocument();
		writer.flush();
		writer.close();
		outputStream.flush();
		outputStream.close();
	}

	private void writeAttributes(AttributedElement element)
			throws XMLStreamException {
		for (Attribute currentAttribute : element.getAttributedElementClass()
				.getAttributeList()) {
			String currentName = currentAttribute.getName();
			try {
				Object currentValue = element.getAttribute(currentName);
				// TODO make it better
				writer.writeAttribute(currentName,
						currentValue == null ? "\\null" : currentValue
								.toString());

			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		}

	}

	public static void main(String[] args) throws Exception {
		CommandLine comLine = processCommandLineOptions(args);
		if (comLine == null) {
			return;
		}
		if(comLine.hasOption("v")){
			System.out.println(JGraLab.getInfo(false));
		}
		String graphFile = comLine.getOptionValue("g").trim();
		String namespacePrefix = comLine.getOptionValue("n").trim();
		String xsdLocation = comLine.getOptionValue("x").trim();
		String outputFile = comLine.getOptionValue("o").trim();

		Graph theGraph = null;
		try {
			theGraph = GraphIO.loadGraphFromFile(graphFile,
					new ProgressFunctionImpl());
		} catch (GraphIOException e) {
			System.out.println("Schema not found.");
			compileSchema(graphFile);
			theGraph = GraphIO.loadGraphFromFile(graphFile,
					new ProgressFunctionImpl());
		}

		Tg2xml converter = new Tg2xml(new BufferedOutputStream(
				new FileOutputStream(outputFile)), theGraph, namespacePrefix,
				xsdLocation);
		converter.visitAll();
		System.out.println("Fini.");
	}

	private static void compileSchema(String graphFile) throws GraphIOException {
		// compile the schema
		Schema schema = GraphIO.loadSchemaFromFile(graphFile);
		System.out.println("Compiling schema");
		schema.compile();
	}

	private static CommandLine processCommandLineOptions(String[] args) {
		Options options = new Options();

		Option output = new Option("o", "output", true,
				"(required): output XML file");
		output.setRequired(true);
		options.addOption(output);

		Option namespacePrefix = new Option("n", "namespace-prefix", true,
				"(required): namespace prefix");
		namespacePrefix.setRequired(true);
		options.addOption(namespacePrefix);

		Option graph = new Option("g", "graph", true,
				"(required): TG-file of the graph");
		graph.setRequired(true);
		options.addOption(graph);

		Option xsdLocation = new Option("x", "xsd-location", true,
				"(required): the location of the XSD schema");
		xsdLocation.setRequired(true);
		options.addOption(xsdLocation);
		
		// parse arguments
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
						.printHelp(Tg2xml.class.getSimpleName(), options);
				System.exit(1);
			}
			System.exit(0);
		}
		return comLine;
	}

}
