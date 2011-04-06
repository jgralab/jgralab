package de.uni_koblenz.jgralab.utilities.tg2gxl;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;

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
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.grumlschema.GrumlSchema;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.grumlschema.domains.Domain;
import de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.NamedElement;
import de.uni_koblenz.jgralab.grumlschema.structure.Package;
import de.uni_koblenz.jgralab.grumlschema.structure.Schema;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.SchemaGraph2Schema;

public class GXL2Tg {

	private String graphOutputName;
	private String graphInputName;

	private SchemaGraph schemaGraph;
	private Package currentPackage;
	private final HashMap<String, NamedElement> id2Class = new HashMap<String, NamedElement>();

	private final boolean isGraphPresent = false;

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
		de.uni_koblenz.jgralab.schema.Schema schema = new SchemaGraph2Schema()
				.convert(schemaGraph);

		inputReader.close();

		// write tg
		if (isGraphPresent) {
			// GraphIO.saveGraphToFile(graphOutputName, null, null);
		} else {
			GraphIO.saveSchemaToFile(graphOutputName, schema);
		}
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
						ne = createNamedElement(type, nodeElement
								.getAttributeByName(new QName("id")).getValue());
					}
					handleAttr((AttributedElementClass) ne,
							startElement.getAttributeByName(new QName("id")),
							startElement.getAttributeByName(new QName("name")),
							startElement.getAttributeByName(new QName("kind")));
				} else {
					throw new GraphIOException("\""
							+ startElement.getName().getLocalPart()
							+ "\" is an unexpected tag in \"node\".");
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

	private void handleAttr(AttributedElementClass aec, Attribute id,
			Attribute name, Attribute kind) throws XMLStreamException,
			GraphIOException {
		while (inputReader.hasNext()) {
			XMLEvent event = inputReader.nextEvent();
			String content = null;
			switch (event.getEventType()) {
			case XMLEvent.START_ELEMENT:
				StartElement startElement = event.asStartElement();
				if (startElement.getName().getLocalPart().equals("attr")) {
					throw new GraphIOException(
							"Attributes in Attributes are not yet supported.");
				}
				break;
			case XMLEvent.CHARACTERS:
				content = event.asCharacters().getData();
				break;
			case XMLEvent.END_ELEMENT:
				EndElement endElement = event.asEndElement();
				if (endElement.getName().getLocalPart().equals("attr")) {
					// this attribute is finished
					return;
				} else {
					setFieldOfAttributedElementClass(aec, name.getValue(),
							content, endElement.getName().getLocalPart());
				}
			}
		}
	}

	private void setFieldOfAttributedElementClass(AttributedElementClass aec,
			String attributeName, String content, String type)
			throws GraphIOException {
		if (attributeName.equals("name")) {
			if (!type.equals("string")) {
				throw new GraphIOException(
						"The qualifiedName value must be of type string.");
			}
			aec.set_qualifiedName(content);
		} else if (attributeName.equals("isabstract")) {
			if (!type.equals("bool")) {
				throw new GraphIOException(
						"The abstract value must be of type bool.");
			}
			((GraphElementClass) aec).set_abstract(Boolean
					.parseBoolean(content));
		} else {
			throw new GraphIOException("\"" + attributeName
					+ "\" is an unknown attribute.");
		}
	}

	private NamedElement createNamedElement(String type, String id) {
		// create NamedElement
		NamedElement ne = null;
		if (type.equals("GraphClass")) {
			ne = schemaGraph.createGraphClass();
			schemaGraph.createDefinesGraphClass(schemaGraph.getFirstSchema(),
					(GraphClass) ne);
		} else {
			if (schemaGraph.getFirstSchema()
					.getFirstContainsDefaultPackageIncidence() == null) {
				// the default package has not been created yet
				createDefaultPackage();
			}
			if (type.equals("NodeClass")) {
				ne = schemaGraph.createVertexClass();
				schemaGraph.createContainsGraphElementClass(currentPackage,
						(GraphElementClass) ne);
			} else if (type.equals("Bool")) {
				ne = schemaGraph.createBooleanDomain();
				schemaGraph.createContainsDomain(currentPackage, (Domain) ne);
			} else if (type.equals("Int")) {
				ne = schemaGraph.createIntegerDomain();
				schemaGraph.createContainsDomain(currentPackage, (Domain) ne);
			} else if (type.equals("Float")) {
				if (schemaGraph.getFirstDoubleDomain() == null) {
					ne = schemaGraph.createDoubleDomain();
					schemaGraph.createContainsDomain(currentPackage,
							(Domain) ne);
				}
			} else if (type.equals("String")) {
				ne = schemaGraph.createStringDomain();
				schemaGraph.createContainsDomain(currentPackage, (Domain) ne);
			} else if (type.equals("Enum")) {
				// TODO atWork
				ne = schemaGraph.createEnumDomain();
				schemaGraph.createContainsDomain(currentPackage, (Domain) ne);
			}
		}
		id2Class.put(id, ne);
		return ne;
	}

	private void createDefaultPackage() {
		currentPackage = schemaGraph.createPackage();
		schemaGraph.createContainsDefaultPackage(schemaGraph.getFirstSchema(),
				currentPackage);
	}
}
