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

package de.uni_koblenz.jgralab.utilities.xml;

import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_ATTRIBUTE_FROM;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_ATTRIBUTE_FSEQ;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_ATTRIBUTE_ID;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_ATTRIBUTE_TO;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_ATTRIBUTE_TSEQ;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_ID_PREFIX_GRAPH;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_ID_PREFIX_VERTEX;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.ist.utilities.xml.IndentingXMLStreamWriter;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.common.GraphVisitor;
import de.uni_koblenz.jgralab.utilities.common.UtilityMethods;

@WorkInProgress(description = "Attribute values missing, testing required, command line parameter checks missing", responsibleDevelopers = "strauss, riediger", expectedFinishingDate = "2009/08")
public class Tg2xml extends GraphVisitor {

	private final String prefix;
	private final String schemaLocation;
	private final OutputStream outputStream;
	private final IndentingXMLStreamWriter writer;
	private final GraphMarker<IncidencePositionMark> incidencePositionMarker;

	private static class IncidencePositionMark {
		public int fseq, tseq;
	}

	private final String namespaceURI;

	public Tg2xml(OutputStream outputStream, Graph graph,
			String nameSpacePrefix, String schemaLocation) throws IOException,
			XMLStreamException {
		super(graph);
		incidencePositionMarker = new GraphMarker<IncidencePositionMark>(graph);

		Schema schema = graph.getSchema();
		String qualifiedName = schema.getQualifiedName();

		if (nameSpacePrefix.equals(XMLConstants.XML_NS_PREFIX)
				|| nameSpacePrefix.equals(XMLConstants.XML_NS_PREFIX)) {
			throw new RuntimeException(
					"The namespace prefixes xml and xsi must not be used!");
		}
		this.prefix = nameSpacePrefix;
		this.schemaLocation = schemaLocation;

		this.namespaceURI = UtilityMethods.generateURI(qualifiedName);
		this.outputStream = outputStream;
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		writer = new IndentingXMLStreamWriter(factory.createXMLStreamWriter(
				outputStream, "UTF-8"));
	}

	public Tg2xml(BufferedOutputStream outputStream, BooleanGraphMarker marker,
			String nameSpacePrefix, String schemaLocation) throws IOException,
			XMLStreamException {
		this(outputStream, marker.getGraph(), nameSpacePrefix, schemaLocation);
		this.marker = marker;
	}

	@Override
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
			writer.writeAttribute(GRUML_ATTRIBUTE_ID, GRUML_ID_PREFIX_GRAPH
					+ graph.getId());
			writeAttributes(graph);

		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void visitVertex(Vertex v) throws XMLStreamException {
		// write vertex
		writer.writeEmptyElement(v.getAttributedElementClass()
				.getQualifiedName());
		writer.writeAttribute(GRUML_ATTRIBUTE_ID, GRUML_ID_PREFIX_VERTEX
				+ v.getId());
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

	@Override
	protected void visitEdge(Edge e) throws XMLStreamException {
		IncidencePositionMark currentMark = incidencePositionMarker.getMark(e);
		writer.writeEmptyElement(e.getAttributedElementClass()
				.getQualifiedName());
		writer.writeAttribute(GRUML_ATTRIBUTE_FROM, GRUML_ID_PREFIX_VERTEX
				+ e.getAlpha().getId());
		writer.writeAttribute(GRUML_ATTRIBUTE_FSEQ, Integer
				.toString(currentMark.fseq));
		writer.writeAttribute(GRUML_ATTRIBUTE_TO, GRUML_ID_PREFIX_VERTEX
				+ e.getOmega().getId());
		writer.writeAttribute(GRUML_ATTRIBUTE_TSEQ, Integer
				.toString(currentMark.tseq));
		writeAttributes(e);
	}

	@Override
	protected void postVisitor() throws XMLStreamException, IOException {
		writer.writeEndDocument();
		writer.writeCharacters("\n");
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
				writer.writeAttribute(currentName, element
						.writeAttributeValueToString(currentName));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GraphIOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static void main(String[] args) throws Exception {
		CommandLine comLine = processCommandLineOptions(args);
		assert comLine != null;
		String graphFile = comLine.getOptionValue("g").trim();
		String namespacePrefix = comLine.getOptionValue("n").trim();
		String xsdLocation = comLine.getOptionValue("x").trim();
		String outputFile = comLine.getOptionValue("o").trim();

		Graph theGraph = GraphIO.loadGraphFromFileWithStandardSupport(
				graphFile, new ConsoleProgressFunction());

		Tg2xml converter = new Tg2xml(new BufferedOutputStream(
				new FileOutputStream(outputFile)), theGraph, namespacePrefix,
				xsdLocation);
		converter.visitAll();
		System.out.println("Fini.");
	}

	private static CommandLine processCommandLineOptions(String[] args) {
		String toolString = "java " + Tg2xml.class.getName();
		String versionString = JGraLab.getInfo(false);
		OptionHandler oh = new OptionHandler(toolString, versionString);

		Option output = new Option("o", "output", true,
				"(required): output XML file");
		output.setRequired(true);
		output.setArgName("file");
		oh.addOption(output);

		Option namespacePrefix = new Option("n", "namespace-prefix", true,
				"(required): namespace prefix");
		namespacePrefix.setRequired(true);
		namespacePrefix.setArgName("prefix");
		oh.addOption(namespacePrefix);

		Option graph = new Option("g", "graph", true,
				"(required): TG-file of the graph");
		graph.setRequired(true);
		graph.setArgName("file");
		oh.addOption(graph);

		Option xsdLocation = new Option("x", "xsd-location", true,
				"(required): the location of the XSD schema");
		xsdLocation.setRequired(true);
		xsdLocation.setArgName("file_or_url");
		oh.addOption(xsdLocation);

		return oh.parse(args);
	}

}
