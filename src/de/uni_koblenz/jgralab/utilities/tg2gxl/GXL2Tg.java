package de.uni_koblenz.jgralab.utilities.tg2gxl;

import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.grumlschema.GrumlSchema;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.NamedElement;
import de.uni_koblenz.jgralab.grumlschema.structure.Schema;

public class GXL2Tg {

	private String graphOutputName;
	private String graphInputName;

	private SchemaGraph schemaGraph;

	private XMLEventReader inputReader;

	/**
	 * You can launch this tool from the command-line.
	 * 
	 * i.e. java GXL2Tg -i /myTg/myGraph.gxl -o /myGxl/myGraph.tg -c
	 * 
	 * @param args
	 *            the command-line option set processed by
	 *            <code>getOptions(String[] args)</code>
	 * @throws XMLStreamException
	 * @throws FileNotFoundException
	 * @throws GraphIOException
	 */
	public static void main(String[] args) throws FileNotFoundException,
			XMLStreamException, GraphIOException {
		GXL2Tg converter = new GXL2Tg();
		converter.getOptions(args);
		converter.convert();
	}

	protected void getOptions(String[] args) {
		CommandLine comLine = processCommandlineOptions(args);
		assert comLine != null;
		if (comLine.hasOption("i")) {
			graphInputName = comLine.getOptionValue("i");
		}
		if (comLine.hasOption("o")) {
			graphOutputName = comLine.getOptionValue("o");
			if (!graphOutputName.endsWith(".tg")
					&& !graphOutputName.endsWith(".gz")) {
				graphOutputName = graphOutputName + ".tg";
			}
		}
	}

	protected CommandLine processCommandlineOptions(String[] args) {
		String toolString = "java " + Tg2GXL.class.getName();
		String versionString = JGraLab.getInfo(false);
		OptionHandler oh = new OptionHandler(toolString, versionString);

		Option graph = new Option("i", "input", true,
				"(required): the GXL-graph to be converted");
		graph.setRequired(true);
		graph.setArgName("file");
		oh.addOption(graph);

		Option output = new Option("o", "output", true,
				"(required): the output file name");
		output.setRequired(true);
		output.setArgName("file");
		oh.addOption(output);

		return oh.parse(args);
	}

	public void convert(String inputGraphName, String outputGraph)
			throws FileNotFoundException, XMLStreamException, GraphIOException {
		graphInputName = inputGraphName;
		graphOutputName = outputGraph;
		convert();
	}

	public void convert() throws FileNotFoundException, XMLStreamException,
			GraphIOException {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		inputReader = factory.createXMLEventReader(new FileReader(
				graphInputName));

		// initialize SchemaGraph
		schemaGraph = GrumlSchema.instance().createSchemaGraph();

		convertSchemaGraph();

		inputReader.close();
	}

	private void convertSchemaGraph() throws XMLStreamException,
			GraphIOException {
		while (inputReader.hasNext()) {
			XMLEvent event = inputReader.nextEvent();
			switch (event.getEventType()) {
			case XMLEvent.START_ELEMENT:
				StartElement startElement = event.asStartElement();
				if (startElement.getName().getLocalPart().equals("graph")) {
					createSchema(startElement);
				} else if (startElement.getName().getLocalPart().equals("node")) {
					handleNode(startElement);
				} else if (startElement.getName().getLocalPart().equals("edge")) {
					// TODO handle edges
				} else if (startElement.getName().getLocalPart().equals("rel")) {
					throw new GraphIOException("Hypergraphs are not supported.");
				} else {
					if (!startElement.getName().getLocalPart().equals("gxl")) {
						throw new GraphIOException("Invalid GXL: \""
								+ startElement.getName().getLocalPart()
								+ "\" not possible at this position.");
					}
				}
				break;
			case XMLEvent.END_ELEMENT:
				EndElement endElement = event.asEndElement();
				if (endElement.getName().getLocalPart().equals("graph")) {
					// schema is completely parsed
					return;
				}
				break;
			}
		}
	}

	private void createSchema(StartElement schemaElement)
			throws XMLStreamException, GraphIOException {
		Schema schema = schemaGraph.createSchema();
		schema.set_name(schemaElement.getAttributeByName(new QName("id"))
				.getValue());
		// check if this is the schema part
		if (inputReader.hasNext()) {
			XMLEvent event = inputReader.peek();
			switch (event.getEventType()) {
			case XMLEvent.CHARACTERS:
				event = inputReader.nextEvent();
				event = inputReader.peek();
			case XMLEvent.START_ELEMENT:
				StartElement startElement = event.asStartElement();
				if (startElement.getName().getLocalPart().equals("type")) {
					if (!startElement
							.getAttributeByName(
									new QName("http://www.w3.org/1999/xlink",
											"href")).getValue()
							.endsWith("#gxl-1.0")) {
						throw new GraphIOException("This is no schema part.");
					}
					event = inputReader.nextEvent();
				}
			}

		}
	}

	private void handleNode(StartElement nodeElement)
			throws XMLStreamException, GraphIOException {
		String name = nodeElement.getAttributeByName(new QName("id"))
				.getValue();
		String type = null;
		NamedElement ne = null;
		while (inputReader.hasNext()) {
			XMLEvent event = inputReader.nextEvent();
			switch (event.getEventType()) {
			case XMLEvent.START_ELEMENT:
				StartElement startElement = event.asStartElement();
				if (startElement.getName().getLocalPart().equals("graph")) {
					throw new GraphIOException(
							"Hierarchical graphs are not supported.");
				} else if (startElement.getName().getLocalPart().equals("type")) {
					type = startElement
							.getAttributeByName(
									new QName("http://www.w3.org/1999/xlink",
											"href")).getValue().split("#")[1];
				} else if (startElement.getName().getLocalPart().equals("attr")) {
					if (ne == null) {
						ne = createNamedElement(type, name);
					}
					createAttribute(ne,
							startElement.getAttributeByName(new QName("id")),
							startElement.getAttributeByName(new QName("name")),
							startElement.getAttributeByName(new QName("kind")));
				} else {
					// TODO
					System.out.println(startElement.getName().getLocalPart());
				}
				break;
			case XMLEvent.END_ELEMENT:
				EndElement endElement = event.asEndElement();
				if (endElement.getName().getLocalPart().equals("node")) {
					return;
				}
				break;
			}
		}
	}

	private void createAttribute(NamedElement ne, Attribute id, Attribute name,
			Attribute kind) throws XMLStreamException {
		// TODO Auto-generated method stub

		while (inputReader.hasNext()) {
			XMLEvent event = inputReader.nextEvent();
			switch (event.getEventType()) {
			case XMLEvent.START_ELEMENT:
				break;
			}
		}
	}

	private NamedElement createNamedElement(String type, String name) {
		if (type == null) {
			// TODO handle null value
		}
		// create NamedElement
		NamedElement ne = null;
		if (name.equals("GraphClass")) {
			ne = schemaGraph.createGraphClass();
			schemaGraph.createDefinesGraphClass(schemaGraph.getFirstSchema(),
					(GraphClass) ne);
		}
		ne.set_qualifiedName(name);
		// TODO do not forget default package
		return ne;
	}
}
