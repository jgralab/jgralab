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

package de.uni_koblenz.jgralab.utilities.xml;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static de.uni_koblenz.jgralab.utilities.xml.XMLexchangeConstants.*;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.Map.Entry;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.GraphMarker;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.impl.ProgressFunctionImpl;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;
import de.uni_koblenz.jgralab.utilities.common.OptionHandler;
import de.uni_koblenz.jgralab.utilities.common.UtilityMethods;

@WorkInProgress
public class Xml2tg {

	public static final int MAX_VERTEX_COUNT = 1024;
	public static final int MAX_EDGE_COUNT = 1024;

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

	private boolean assumeVerticesBeforeEdges;

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
		CommandLine cmdl = processCommandLineOptions(args);

		String schemaFilename = cmdl.getOptionValue('s').trim();
		String inputFilename = cmdl.getOptionValue('i').trim();
		String outputFilename = cmdl.getOptionValue('o').trim();

		Schema currentSchema = GraphIO.loadSchemaFromFile(schemaFilename);
		Xml2tg tester = new Xml2tg(new BufferedInputStream(new FileInputStream(
				inputFilename)), outputFilename, currentSchema);
		if (cmdl.hasOption('V')) {
			tester.setAssumeVerticesBeforeEdges(true);
		}
		tester.importXml();
		System.out.println("Fini.");
	}

	private static CommandLine processCommandLineOptions(String[] args) {
		String toolString = Xml2tg.class.getName();
		String versionString = JGraLab.getInfo(false);
		OptionHandler oh = new OptionHandler(toolString, versionString);

		Option input = new Option("i", "input", true,
				"(required): name of XML file to read from.");
		input.setRequired(true);
		input.setArgName("file");
		oh.addOption(input);

		Option output = new Option("o", "output", true,
				"(required): name of TG file to write graph to");
		output.setRequired(true);
		output.setArgName("file");
		oh.addOption(output);

		Option schema = new Option(
				"s",
				"schema",
				true,
				"(required): name of the TG file containing the schema of the resulting graph. The compiled version of this schema also has to be in the classpath.");
		schema.setRequired(true);
		schema.setArgName("file");
		oh.addOption(schema);

		Option verticesBeforeEdges = new Option(
				"V",
				"vertices-first",
				false,
				"(optional): if this flag is set, the parser assumes that vertex classes are located before edge classes in the xml. If it is set and they are not, the parse will fail. It is faster then the fail proof parse version.");
		verticesBeforeEdges.setRequired(false);
		oh.addOption(verticesBeforeEdges);

		return oh.parse(args);
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
		assumeVerticesBeforeEdges = false;
	}

	public void importXml() throws XMLStreamException, ClassNotFoundException,
			GraphIOException {
		// read root element
		// if (reader.hasNext()) {
		// int nextEvent = reader.next();
		// // assert (nextEvent == START_ELEMENT);
		// }
		int level = 0;
		if (!assumeVerticesBeforeEdges) {
			edgesToCreate = new LinkedList<AttributedElementInfo>();
		}
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
					String graphID = stack.peek().getAttributes().get(ID);

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
					if (assumeVerticesBeforeEdges) {
						createEdge(current);
					} else {
						edgesToCreate.add(current);
					}
				} else if (current.getAttributedElementClass() instanceof GraphClass) {
					setGraphAttributes(current);
				}
				level -= 1;
			default:
			}
		}

		assert (level == 0);
		reader.close();
		if (!assumeVerticesBeforeEdges) {
			// create all edges
			while (!edgesToCreate.isEmpty()) {
				createEdge(edgesToCreate.poll());
			}
		}
		sortIncidenceLists();
		saveGraph();
	}

	private void saveGraph() throws GraphIOException {
		GraphIO.saveGraphToFile(tgOutput, graph, new ProgressFunctionImpl());
	}

	private void sortIncidenceLists() {
		System.out.println("Sorting incidence lists.");
		ProgressFunction progress = new ProgressFunctionImpl();

		progress.init(graph.getVCount());
		long processed = 0L;
		long updateInterval = progress.getUpdateInterval();
		for (Vertex currentVertex : graph.vertices()) {
			UtilityMethods.sortIncidenceList(currentVertex,
					new Comparator<Edge>() {

						@Override
						public int compare(Edge e1, Edge e2) {
							IncidencePositionMark mark1 = incidencePositionMarker
									.getMark(e1);
							IncidencePositionMark mark2 = incidencePositionMarker
									.getMark(e2);
							int seq1 = e1.isNormal() ? mark1.fseq : mark1.tseq;
							int seq2 = e2.isNormal() ? mark2.fseq : mark2.tseq;
							return Double.compare(seq1, seq2);
						}
					});
			if (++processed % updateInterval == 0) {
				progress.progress(processed);
			}
		}
		progress.finished();
	}

	private void setGraphAttributes(AttributedElementInfo current) {
		Map<String, String> attributes = current.getAttributes();
		attributes.remove(ID);
		// remove XML attribute "schemaLocation"
		attributes.remove("schemaLocation");
		setAttributes(graph, attributes);
	}

	private void createEdge(AttributedElementInfo current) {
		System.out.println("Creating edge of type " + current.getqName());
		Map<String, String> attributes = current.getAttributes();
		String toId = attributes.get(TO);
		String fromId = attributes.get(FROM);

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
		String fseq = attributes.get(FSEQ);
		String tseq = attributes.get(TSEQ);
		incidences.fseq = fseq == null ? Integer.MAX_VALUE : Integer
				.parseInt(fseq);
		incidences.tseq = tseq == null ? Integer.MAX_VALUE : Integer
				.parseInt(tseq);
		incidencePositionMarker.mark(currentEdge, incidences);

		// delete some attributes from HashMap
		attributes.remove(TO);
		attributes.remove(FROM);
		attributes.remove(FSEQ);
		attributes.remove(TSEQ);

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
		String currentId = attributes.get(ID);

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
		attributes.remove(ID);
		setAttributes(currentVertex, attributes);

	}

	private void setAttributes(AttributedElement element,
			Map<String, String> attributes) {
		System.out.println("Setting attributes for instance of "
				+ element.getAttributedElementClass().getQualifiedName());
		if (element instanceof GraphElement) {
			System.out.println("Id: " + ((GraphElement) element).getId());
		}

		for (Entry<String, String> currentEntry : attributes.entrySet()) {
			try {
				element.readAttributeValueFromString(currentEntry.getKey(),
						currentEntry.getValue());
			} catch (GraphIOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void setAssumeVerticesBeforeEdges(boolean assumeVerticesBeforeEdges) {
		this.assumeVerticesBeforeEdges = assumeVerticesBeforeEdges;
	}

	// private Vertex createDummyVertex(String xmlId) {
	// Vertex dummyVertex;
	// dummyVertex = graph.createVertex(Vertex.class);
	// dummyVertexMap.put(xmlId, dummyVertex);
	// return dummyVertex;
	// }
}
