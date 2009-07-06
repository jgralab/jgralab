package de.uni_koblenz.jgralab.utilities.tg2xml;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphMarker;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.jgralab2owl.IndentingXMLStreamWriter;

public class Tg2xml extends GraphVisitor {

	private static String PREFIX = "jgralab";
	private BufferedOutputStream outputStream;
	private IndentingXMLStreamWriter writer;
	private GraphMarker<Integer> fromEdgeMarker;
	private GraphMarker<Integer> toEdgeMarker;

	private String namespaceURI;

	public Tg2xml(String filename, Graph graph) throws IOException,
			XMLStreamException {
		super(graph);
		fromEdgeMarker = new GraphMarker<Integer>(graph);
		toEdgeMarker = new GraphMarker<Integer>(graph);

		this.namespaceURI = generateURI(graph);
		outputStream = new BufferedOutputStream(new FileOutputStream(filename));
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		writer = new IndentingXMLStreamWriter(factory.createXMLStreamWriter(
				outputStream, "UTF-8"), 1);
		writer.setIndentationChar('\t');

	}

	public static String generateURI(Graph graph) {
		Schema schema = graph.getSchema();
		String qualifiedName = schema.getQualifiedName();
		qualifiedName = qualifiedName.replace('_', '-');
		System.out.println(qualifiedName);
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

			writer.writeStartElement(PREFIX,
					graph.getM1Class().getSimpleName(), namespaceURI);
			writer.writeNamespace(PREFIX, namespaceURI);
			writeAttributes(graph);

		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void visitVertex(Vertex v) throws XMLStreamException {
		// write vertex
		writer.writeEmptyElement(v.getM1Class().getSimpleName());
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
		writer.writeEmptyElement(e.getM1Class().getSimpleName());
		writeAttributes(e);
		writer.writeAttribute("fseq", fromEdgeMarker.getMark(e).toString());
		writer.writeAttribute("tseq", toEdgeMarker.getMark(e).toString());
	}

	protected void postVisitor() throws XMLStreamException, IOException {
		writer.writeEndDocument();
		writer.flush();
		writer.close();
		outputStream.flush();
		outputStream.close();
	}

	private void writeAttributes(AttributedElement element) {
		element.getAttributedElementClass().getAttributeList(); // iterieren und
		// über
		// element.getAttribute
		// gehen

	}

}
