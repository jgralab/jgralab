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
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.Map.Entry;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.ProgressFunction;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;

@WorkInProgress
public class Xml2tg {
	private static OptionHandler optionHandler = null;

	public static final int MAX_VERTEX_COUNT = 1024;
	public static final int MAX_EDGE_COUNT = 1024;

	private String tgOutput;
	private boolean multiXml = false;
	private boolean keepGoing = false;

	private Schema schema;
	private Graph graph;

	private XMLStreamReader reader;
	private Stack<AttributedElementInfo> stack;

	private Map<String, Vertex> xmlIdToVertexMap;

	private Queue<AttributedElementInfo> edgesToCreate;

	private GraphMarker<IncidencePositionMark> incidencePositionMarker;

	private boolean assumeVerticesBeforeEdges;

	private String xmlInput;

	private static class IncidencePositionMark {
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

		public Map<String, String> getAttributes() {
			return attributes;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(aec.getQualifiedName());
			sb.append(": ");
			boolean first = true;
			for (Entry<String, String> e : attributes.entrySet()) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(e.getKey());
				sb.append(" = '");
				sb.append(e.getValue());
				sb.append("'");
			}
			return sb.toString();
		}
	}

	public static void main(String[] args) throws FileNotFoundException,
			XMLStreamException, GraphIOException, ClassNotFoundException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException, InstantiationException,
			SecurityException, NoSuchMethodException {
		CommandLine cmdl = processCommandLineOptions(args);

		String schemaName = cmdl.getOptionValue('s').trim();
		String outputFilename = cmdl.getOptionValue('o').trim();

		if (cmdl.getArgList().size() > 1 && !cmdl.hasOption('m')) {
			System.err
					.println("When multiple XML files are given, the -m option has to be specified.");
			optionHandler.printHelpAndExit(1);
		}

		Class<?> currentSchema = Class.forName(schemaName);
		Method inst = currentSchema.getMethod("instance");
		Schema schema = (Schema) inst.invoke(currentSchema);

		Xml2tg xml2tg = null;
		int i = 0;
		String[] files = cmdl.getArgs();
		for (String inputXML : files) {
			System.out.println("Importing " + inputXML + " (" + ++i + "/"
					+ files.length + ")");
			if (xml2tg == null) {
				xml2tg = new Xml2tg(inputXML, outputFilename, schema);
				xml2tg.setAssumeVerticesBeforeEdges(cmdl.hasOption('V'));
				xml2tg.setMultiXml(cmdl.hasOption('m'));
				xml2tg.setKeepGoing(cmdl.hasOption('k'));
			} else {
				xml2tg.setXmlInput(inputXML);
			}
			xml2tg.importXml();
		}
		xml2tg.saveGraph();
		System.out.println("Fini.");
	}

	private static CommandLine processCommandLineOptions(String[] args) {
		String toolString = Xml2tg.class.getSimpleName();
		String versionString = JGraLab.getInfo(false);
		optionHandler = new OptionHandler(toolString, versionString);

		Option output = new Option("o", "output", true,
				"(required): name of TG file to write graph to.");
		output.setRequired(true);
		output.setArgName("file");
		optionHandler.addOption(output);

		Option schema = new Option(
				"s",
				"schemaName",
				true,
				"(required): Name of the schema class, e.g. de.soamig.schema.SoamigSchema_0_12.  "
						+ "It has to be compiled and in the classpath.");
		schema.setRequired(true);
		schema.setArgName("name");
		optionHandler.addOption(schema);

		Option verticesBeforeEdges = new Option(
				"V",
				"vertices-first",
				false,
				"(optional): if this flag is set, the parser assumes that vertex classes are located before edge classes in the xml. "
						+ "If it is set and they are not, the parse will fail. "
						+ "It is faster then the fail proof parse version.");
		verticesBeforeEdges.setRequired(false);
		optionHandler.addOption(verticesBeforeEdges);

		Option multiXmlIntoOneGraph = new Option(
				"m",
				"multi-xml",
				false,
				"(optional): if this flag is set, then the result is one graph containing all vertices and edges from all given XML files.");
		multiXmlIntoOneGraph.setRequired(false);
		optionHandler.addOption(multiXmlIntoOneGraph);

		Option keepGoing = new Option(
				"k",
				"keep-going",
				false,
				"(optional): if this flag is set, then exceptions while creating edges won't abort.");
		keepGoing.setRequired(false);
		optionHandler.addOption(keepGoing);

		optionHandler.setArgumentCount(Option.UNLIMITED_VALUES);
		optionHandler.setArgumentName("xml-file");
		optionHandler.setOptionalArgument(false);

		return optionHandler.parse(args);
	}

	public Xml2tg(String inputXml, String tgOutput, Schema schema)
			throws XMLStreamException, FileNotFoundException {
		this.tgOutput = tgOutput;
		this.schema = schema;
		xmlIdToVertexMap = new HashMap<String, Vertex>();
		stack = new Stack<AttributedElementInfo>();
		assumeVerticesBeforeEdges = false;
		setXmlInput(inputXml);
	}

	public void setXmlInput(String fileName) throws FileNotFoundException,
			XMLStreamException, FactoryConfigurationError {
		reader = XMLInputFactory.newInstance().createXMLStreamReader(
				new FileInputStream(fileName));
		xmlInput = fileName;
		stack.clear();
		xmlIdToVertexMap.clear();

	}

	public void importXml() throws XMLStreamException, ClassNotFoundException,
			GraphIOException {
		int level = 0;
		int elementCount = 0;
		if (!assumeVerticesBeforeEdges) {
			edgesToCreate = new LinkedList<AttributedElementInfo>();
		}
		try {
			while (reader.hasNext()) {
				int nextEvent = reader.next();
				switch (nextEvent) {
				case START_DOCUMENT:
					System.out.println("Starting Document");
					break;
				case START_ELEMENT:
					level += 1;
					if (++elementCount % 1000 == 0) {
						System.out.print(".");
						System.out.flush();
					}
					if (level == 2) {
						// graph element
						String attributedElementClassName = reader.getName()
								.getLocalPart();
						// System.out.println(attributedElementClassName);
						AttributedElementClass aec = schema
								.getAttributedElementClass(attributedElementClassName);
						if (aec == null) {
							throw new RuntimeException(
									"AttributedElementClass '"
											+ attributedElementClassName
											+ "' unknown.");
						}
						stack.push(new AttributedElementInfo(aec));
					} else if (level == 1) {
						// root element (graph)
						String graphClassName = reader.getName().getLocalPart();
						if (!schema.getGraphClass().getQualifiedName()
								.equals(graphClassName)) {
							throw new SchemaException(
									"Name mismatch for GraphClass: should be "
											+ schema.getGraphClass()
													.getQualifiedName()
											+ " but was " + graphClassName);
						}
						AttributedElementClass aec = schema
								.getAttributedElementClass(graphClassName);
						if (aec == null) {
							throw new RuntimeException("GraphClass '"
									+ graphClassName + "' unknown.");
						}
						stack.push(new AttributedElementInfo(aec));
						if (graph != null && multiXml) {
							break;
						}
						String graphID = stack.peek().getAttributes()
								.get(GRUML_ATTRIBUTE_ID);
						try {
							// System.out.println("Creating instance of "
							// + graphClassName);
							graph = (Graph) schema.getGraphCreateMethod(
									ImplementationType.STANDARD).invoke(
									null,
									new Object[] { graphID, MAX_VERTEX_COUNT,
											MAX_EDGE_COUNT });
							// System.out.println("done.");
						} catch (Exception e) {
							throw new GraphIOException(
									"Unable to create instance of "
											+ graphClassName, e);
						}
						// inicialize markers
						incidencePositionMarker = new GraphMarker<IncidencePositionMark>(
								graph);

					}
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

			System.out.println();

			assert level == 0;
		} finally {
			try {
				reader.close();
			} catch (XMLStreamException ex) {
				throw new RuntimeException(
						"An exception occured while closing the stream.", ex);
			}
		}
		if (!assumeVerticesBeforeEdges) {
			// create all edges
			while (!edgesToCreate.isEmpty()) {
				createEdge(edgesToCreate.poll());
			}
		}
		sortIncidenceLists();
	}

	public void saveGraph() throws GraphIOException {
		System.out.println("Saving graph to " + tgOutput);
		GraphIO.saveGraphToFile(tgOutput, graph, new ConsoleProgressFunction());
	}

	private void sortIncidenceLists() {

		System.out.println("Sorting incidence lists.");
		ProgressFunction progress = new ConsoleProgressFunction();

		progress.init(graph.getVCount());
		long processed = 0L;
		long updateInterval = progress.getUpdateInterval();
		for (Vertex currentVertex : graph.vertices()) {
			currentVertex.sortIncidences(new Comparator<Edge>() {

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
		attributes.remove(GRUML_ATTRIBUTE_ID);
		// remove XML attribute "schemaLocation"
		attributes.remove("schemaLocation");
		setAttributes(graph, attributes);
	}

	private void createEdge(AttributedElementInfo current) {
		// System.out.println("Creating edge of type " + current.getqName());
		Map<String, String> attributes = current.getAttributes();
		String toId = attributes.get(GRUML_ATTRIBUTE_TO);
		String fromId = attributes.get(GRUML_ATTRIBUTE_FROM);

		// ensure existence of to and from vertex, if they do not exist yet,
		// create dummy vertices
		Vertex fromVertex, toVertex;
		fromVertex = xmlIdToVertexMap.get(fromId);
		toVertex = xmlIdToVertexMap.get(toId);

		// create edge
		try {
			Edge currentEdge = graph.createEdge(((EdgeClass) current
					.getAttributedElementClass()).getM1Class(), fromVertex,
					toVertex);
			// mark new edge with incidence position information
			IncidencePositionMark incidences = new IncidencePositionMark();
			String fseq = attributes.get(GRUML_ATTRIBUTE_FSEQ);
			String tseq = attributes.get(GRUML_ATTRIBUTE_TSEQ);
			incidences.fseq = fseq == null ? Integer.MAX_VALUE : Integer
					.parseInt(fseq);
			incidences.tseq = tseq == null ? Integer.MAX_VALUE : Integer
					.parseInt(tseq);
			incidencePositionMarker.mark(currentEdge, incidences);

			// delete some attributes from HashMap
			attributes.remove(GRUML_ATTRIBUTE_TO);
			attributes.remove(GRUML_ATTRIBUTE_FROM);
			attributes.remove(GRUML_ATTRIBUTE_FSEQ);
			attributes.remove(GRUML_ATTRIBUTE_TSEQ);

			// set attributes for Edge
			setAttributes(currentEdge, attributes);
		} catch (GraphException e) {
			System.err.println("In file " + xmlInput + " at edge " + current);
			e.printStackTrace();
			if (!keepGoing) {
				System.exit(1);
			}
		}
	}

	private void createVertex(AttributedElementInfo current) {
		// System.out.println("Creating vertex of type " + current.getqName());
		Map<String, String> attributes = current.getAttributes();
		Vertex currentVertex = graph.createVertex(((VertexClass) current
				.getAttributedElementClass()).getM1Class());
		String currentId = attributes.get(GRUML_ATTRIBUTE_ID);

		// add currentVertex to Map
		xmlIdToVertexMap.put(currentId, currentVertex);

		// set attributes
		attributes.remove(GRUML_ATTRIBUTE_ID);
		setAttributes(currentVertex, attributes);

	}

	private void setAttributes(AttributedElement element,
			Map<String, String> attributes) {

		for (Entry<String, String> currentEntry : attributes.entrySet()) {
			try {
				element.readAttributeValueFromString(currentEntry.getKey(),
						currentEntry.getValue());
			} catch (GraphIOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void setAssumeVerticesBeforeEdges(boolean assumeVerticesBeforeEdges) {
		this.assumeVerticesBeforeEdges = assumeVerticesBeforeEdges;
	}

	public void setMultiXml(boolean multiXml) {
		this.multiXml = multiXml;
	}

	public boolean isKeepGoing() {
		return keepGoing;
	}

	public void setKeepGoing(boolean keepGoing) {
		this.keepGoing = keepGoing;
	}
}
