/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
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

package de.uni_koblenz.jgralab.utilities.tg2xml;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.validation.SchemaFactory;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.GraphMarker;
import de.uni_koblenz.jgralab.M1ClassManager;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphElementClass;
import de.uni_koblenz.jgralab.impl.ProgressFunctionImpl;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;
import static javax.xml.stream.XMLStreamConstants.*;

@WorkInProgress
public class Xml2tg {

	public static final int MAX_VERTEX_COUNT = 100;
	public static final int MAX_EDGE_COUNT = 100;

	private String tgOutput;
	private Schema schema;
	private Graph graph;

	private XMLStreamReader reader;
	private Stack<AttributedElementInfo> stack;

	private Map<String, Vertex> xmlIdToVertexMap;
	// private BooleanGraphMarker dummyVertexMarker;
	// private Map<String, Vertex> dummyVertexMap;

	private Queue<AttributedElementInfo> edgesToCreate;

	private GraphMarker<IncidencePositionMark> incidencePositionMarker;

	private class IncidencePositionMark {
		public int fseq, tseq;
	}

	private class AttributedElementInfo {
		private AttributedElementClass aec;
		private Map<String, String> attributes;

		public AttributedElementInfo(AttributedElementClass aec) {
			super();
			this.aec = aec;
			attributes = new HashMap<String, String>();
			int count = reader.getAttributeCount();
			for (int i = 0; i < count; i++) {
				attributes.put(reader.getAttributeName(i).getLocalPart(),
						reader.getAttributeValue(i));
			}
		}

		public AttributedElementClass getAttributedElementClass() {
			return aec;
		}

		public String getqName() {
			return aec.getQualifiedName();
		}

		public Map<String, String> getAttributes() {
			return attributes;
		}
	}

	// public static void validate(String filename, String xsdFilename)
	// throws FileNotFoundException, XMLStreamException {
	// XMLInputFactory factory = XMLInputFactory.newInstance();
	// XMLStreamReader reader = factory
	// .createXMLStreamReader(new FileInputStream(filename));
	// SchemaFactory schemaFactory =
	// SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	// schemaFactory.setErrorHandler()
	// }

	public static void main(String[] args) throws FileNotFoundException,
			XMLStreamException, GraphIOException, ClassNotFoundException {
		Schema currentSchema = GraphIO.loadSchemaFromFile("sample-graph.tg");
		Xml2tg tester = new Xml2tg(new BufferedInputStream(new FileInputStream(
				"sample-graph.xml")), "converted-sample-graph.tg",
				currentSchema);
		tester.importXml();
		System.out.println("Fini.");
	}

	public Xml2tg(InputStream xmlInput, String tgOutput, Schema schema)
			throws XMLStreamException {
		this.tgOutput = tgOutput;
		this.schema = schema;
		xmlIdToVertexMap = new HashMap<String, Vertex>();
		XMLInputFactory factory = XMLInputFactory.newInstance();
		reader = factory.createXMLStreamReader(xmlInput);
		stack = new Stack<AttributedElementInfo>();
		// dummyVertexMap = new HashMap<String, Vertex>();
		edgesToCreate = new LinkedList<AttributedElementInfo>();
	}

	public void importXml() throws XMLStreamException, ClassNotFoundException,
			GraphIOException {
		// read root element
		// if (reader.hasNext()) {
		// int nextEvent = reader.next();
		// // assert (nextEvent == START_ELEMENT);
		// }
		int level = 0;
		while (reader.hasNext()) {
			int nextEvent = reader.next();
			switch (nextEvent) {
			case START_DOCUMENT:
				System.out.println("It begins");
				break;
			case START_ELEMENT:
				level += 1;
				if (level == 2) {
					// graph element
					String attributedElementClassName = reader.getName()
							.getLocalPart();
					// System.out.println(attributedElementClassName);
					AttributedElementClass aec = schema
							.getAttributedElementClass(attributedElementClassName);
					stack.push(new AttributedElementInfo(aec));

				} else if (level == 1) {
					// root element (graph)
					String graphClassName = reader.getName().getLocalPart();
					if (!schema.getGraphClass().getQualifiedName().equals(
							graphClassName)) {
						throw new SchemaException(
								"Name mismatch for GraphClass: should be "
										+ schema.getGraphClass()
												.getQualifiedName()
										+ " but was " + graphClassName);
					}
					stack.push(new AttributedElementInfo(schema
							.getAttributedElementClass(graphClassName)));
					String graphID = stack.peek().getAttributes().get("id");

					try {
						System.out.println("Creating instance of "
								+ graphClassName);
						graph = (Graph) schema.getGraphCreateMethod().invoke(
								null,
								new Object[] { graphID, MAX_VERTEX_COUNT,
										MAX_EDGE_COUNT });
						System.out.println("done.");
					} catch (Exception e) {
						throw new GraphIOException(
								"Unable to create instance of "
										+ graphClassName, e);
					}
					// inicialize markers
					// dummyVertexMarker = new BooleanGraphMarker(graph);
					incidencePositionMarker = new GraphMarker<IncidencePositionMark>(
							graph);

				}
				// System.out.println(reader.getName() + ":"); \\

				break;
			case END_ELEMENT:
				AttributedElementInfo current = stack.pop();
				if (current.getAttributedElementClass() instanceof VertexClass) {
					createVertex(current);
				} else if (current.getAttributedElementClass() instanceof EdgeClass) {
					edgesToCreate.add(current);
					// createEdge(current);
				} else if (current.getAttributedElementClass() instanceof GraphClass) {
					setGraphAttributes(current);
				}
				level -= 1;
			default:
			}
		}
		assert (level == 0);
		reader.close();
		// create all edges
		for (AttributedElementInfo current : edgesToCreate) {
			createEdge(current);
		}
		sortIncidenceLists();
		saveGraph();
	}

	private void saveGraph() throws GraphIOException {
		GraphIO.saveGraphToFile(tgOutput, graph, new ProgressFunctionImpl());
	}

	private void sortIncidenceLists() {
		System.out.println("Would sort incidence lists now.");
	}

	private void setGraphAttributes(AttributedElementInfo current) {
		Map<String, String> attributes = current.getAttributes();
		attributes.remove("id");
		setAttributes(graph, attributes);
	}

	private void createEdge(AttributedElementInfo current) {
		System.out.println("Creating edge of type " + current.getqName());
		Map<String, String> attributes = current.getAttributes();
		String toId = attributes.get("to");
		String fromId = attributes.get("from");

		// ensure existence of to and from vertex, if they do not exist yet,
		// create dummy vertices
		Vertex fromVertex, toVertex;
		fromVertex = xmlIdToVertexMap.get(fromId);
		// if (fromVertex == null) {
		// System.out.println("creating dummy vertex \"from\"");
		// fromVertex = createDummyVertex(fromId);
		// }
		toVertex = xmlIdToVertexMap.get(toId);
		// if (toVertex == null) {
		// System.out.println("creating dummy vertex \"to\"");
		// toVertex = createDummyVertex(toId);
		// }

		// create edge
		Edge currentEdge = graph.createEdge(((EdgeClass) current
				.getAttributedElementClass()).getM1Class(), fromVertex,
				toVertex);

		// mark new edge with incidence position information
		IncidencePositionMark incidences = new IncidencePositionMark();
		String fseq = attributes.get("fseq");
		String tseq = attributes.get("tseq");
		incidences.fseq = fseq == null ? Integer.MAX_VALUE : Integer
				.parseInt(fseq);
		incidences.tseq = tseq == null ? Integer.MAX_VALUE : Integer
				.parseInt(tseq);
		incidencePositionMarker.mark(currentEdge, incidences);

		// delete some attributes from HashMap
		attributes.remove("to");
		attributes.remove("from");
		attributes.remove("fseq");
		attributes.remove("tseq");

		// set attributes for Edge
		setAttributes(currentEdge, attributes);
	}

	// private Vertex createDummyVertex(String xmlId) {
	// Vertex dummyVertex;
	// dummyVertex = graph.createVertex(Vertex.class);
	// dummyVertexMap.put(xmlId, dummyVertex);
	// return dummyVertex;
	// }

	private void createVertex(AttributedElementInfo current) {
		System.out.println("Creating vertex of type " + current.getqName());
		Map<String, String> attributes = current.getAttributes();
		Vertex currentVertex = graph.createVertex(((VertexClass) current
				.getAttributedElementClass()).getM1Class());
		String currentId = attributes.get("id");

		// check if dummy vertex of this id exists if so, add the edges to the
		// new vertex and delet ethe dummy vertex
		// Vertex currentDummyVertex = dummyVertexMap.get(currentId);
		// if (currentDummyVertex != null) {
		// for (Edge currentIncidence : currentDummyVertex.incidences()) {
		// currentIncidence.setThis(currentVertex);
		// }
		// dummyVertexMap.remove(currentId);
		// graph.deleteVertex(currentDummyVertex);
		// System.out.println("Replaced dummy vertex.");
		// }

		// add currentVertex to Map
		xmlIdToVertexMap.put(currentId, currentVertex);

		// set attributes
		attributes.remove("id");
		setAttributes(currentVertex, attributes);

	}

	private void setAttributes(AttributedElement element,
			Map<String, String> attributes) {
		System.out.println("Would set attributes for instance of "
				+ element.getAttributedElementClass().getQualifiedName());
		if (element instanceof GraphElement) {
			System.out.println("Id: " + ((GraphElement) element).getId());
		}
	}
}
