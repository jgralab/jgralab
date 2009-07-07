package de.uni_koblenz.jgralab.utilities.tg2xml;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphMarker;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.jgralab2owl.IndentingXMLStreamWriter;

public class Tg2xml extends GraphVisitor {

	private String prefix;
	private String schemaLocation;
	private OutputStream outputStream;
	private IndentingXMLStreamWriter writer;
	private GraphMarker<Integer> fromEdgeMarker;
	private GraphMarker<Integer> toEdgeMarker;

	private String namespaceURI;

	public Tg2xml(OutputStream outputStream, Graph graph,
			String nameSpacePrefix, String schemaLocation) throws IOException,
			XMLStreamException {
		super(graph);
		fromEdgeMarker = new GraphMarker<Integer>(graph);
		toEdgeMarker = new GraphMarker<Integer>(graph);

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
			if (currentIncidentEdge.isNormal()) {
				fromEdgeMarker.mark(currentIncidentEdge, i);
			} else {
				toEdgeMarker.mark(currentIncidentEdge, i);
			}
			i++;
		}
	}

	protected void visitEdge(Edge e) throws XMLStreamException {
		writer.writeEmptyElement(e.getAttributedElementClass()
				.getQualifiedName());
		writer.writeAttribute("from", "v" + e.getAlpha().getId());
		writer.writeAttribute("fseq", fromEdgeMarker.getMark(e).toString());
		writer.writeAttribute("to", "v" + e.getOmega().getId());
		writer.writeAttribute("tseq", toEdgeMarker.getMark(e).toString());
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

		// iterieren und
		// über
		// element.getAttribute
		// gehen

	}

}
