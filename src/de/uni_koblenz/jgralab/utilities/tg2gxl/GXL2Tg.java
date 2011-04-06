package de.uni_koblenz.jgralab.utilities.tg2gxl;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import de.uni_koblenz.jgralab.grumlschema.domains.EnumDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.EdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.NamedElement;
import de.uni_koblenz.jgralab.grumlschema.structure.Package;
import de.uni_koblenz.jgralab.grumlschema.structure.Schema;
import de.uni_koblenz.jgralab.grumlschema.structure.VertexClass;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.SchemaGraph2Schema;

public class GXL2Tg {

	private final String TYPE_PREFIX = "http://www.gupro.de/GXL/gxl-1.0.gxl#";

	private String graphOutputName;
	private String graphInputName;

	private SchemaGraph schemaGraph;
	private Package currentPackage;
	private final HashMap<String, NamedElement> id2Class = new HashMap<String, NamedElement>();
	private final HashMap<String, de.uni_koblenz.jgralab.grumlschema.structure.Attribute> id2Attribute = new HashMap<String, de.uni_koblenz.jgralab.grumlschema.structure.Attribute>();
	private final HashMap<String, String> id2EnumValues = new HashMap<String, String>();

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
				System.out
						.println(startElement.getName()
								+ " "
								+ (startElement.getAttributeByName(new QName(
										"id")) != null ? startElement
										.getAttributeByName(new QName("id"))
										.getValue() : "null"));// TODO
				if (startElement.getName().getLocalPart().equals("graph")) {
					createSchema(startElement);
				} else if (startElement.getName().getLocalPart().equals("node")) {
					handleNodeOrEdge(startElement);
				} else if (startElement.getName().getLocalPart().equals("edge")) {
					handleNodeOrEdge(startElement);
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

	private void handleNodeOrEdge(StartElement element)
			throws XMLStreamException, GraphIOException {
		String type = null;
		Attribute idAttribute = element.getAttributeByName(new QName("id"));
		String id = idAttribute != null ? idAttribute.getValue() : null;
		while (inputReader.hasNext()) {
			XMLEvent event = inputReader.nextEvent();
			switch (event.getEventType()) {
			case XMLEvent.START_ELEMENT:
				StartElement startElement = event.asStartElement();
				if (startElement.getName().getLocalPart().equals("graph")) {
					throw new GraphIOException(
							"Hierarchical graphs are not supported.");
				} else if (startElement.getName().getLocalPart().equals("type")) {
					type = startElement.getAttributeByName(
							new QName("http://www.w3.org/1999/xlink", "href"))
							.getValue();
				} else if (startElement.getName().getLocalPart().equals("attr")) {
					if (type.split(TYPE_PREFIX)[1].equals("EnumVal")) {
						assert id2EnumValues.get(id) == null : "the enum constant with id \""
								+ id + "\" already exists.";
						id2EnumValues.put(id, "");
					} else if (type.split(TYPE_PREFIX)[1]
							.equals("AttributeClass")) {
						id2Attribute.put(id, schemaGraph.createAttribute());
					} else {
						createNamedElement(type, id,
								element.getAttributeByName(new QName("from")),
								element.getAttributeByName(new QName("to")));
					}
					handleAttr(id,
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
				if (endElement.getName().getLocalPart().equals("node")
						|| endElement.getName().getLocalPart().equals("edge")) {
					if (!type.split(TYPE_PREFIX)[1].equals("AttributeClass")) {
						createNamedElement(type, id,
								element.getAttributeByName(new QName("from")),
								element.getAttributeByName(new QName("to")));
					}
					return;
				}
				break;
			}
		}
	}

	private void handleAttr(String aecId, Attribute id, Attribute name,
			Attribute kind) throws XMLStreamException, GraphIOException {
		AttributedElementClass aec = (AttributedElementClass) id2Class
				.get(aecId);
		String content = null;
		String type = null;
		boolean updateContent = false;
		while (inputReader.hasNext()) {
			XMLEvent event = inputReader.nextEvent();
			switch (event.getEventType()) {
			case XMLEvent.START_ELEMENT:
				StartElement startElement = event.asStartElement();
				if (startElement.getName().getLocalPart().equals("attr")) {
					throw new GraphIOException(
							"Attributes in Attributes are not yet supported.");
				}
				updateContent = isCorrectAtributeValue(startElement.getName()
						.getLocalPart());
				type = null;
				break;
			case XMLEvent.CHARACTERS:
				if (updateContent) {
					content = event.asCharacters().getData();
				}
				break;
			case XMLEvent.END_ELEMENT:
				EndElement endElement = event.asEndElement();
				if (endElement.getName().getLocalPart().equals("attr")) {
					// this attribute is finished
					if (name.getValue().equals("value")) {
						// this is an enumeration constant
						if (!type.equals("string")) {
							throw new GraphIOException(
									"The enum constant must be of type \"string\".");
						}
						id2EnumValues.put(aecId, content);
					} else if (aec == null) {
						// this is an attribute
						if (!name.getValue().equals("name")) {
							throw new GraphIOException(
									"An attribute does not have a field with name \""
											+ name.getValue() + "\".");
						}
						id2Attribute.get(aecId).set_name(content);
					} else {
						setFieldOfAttributedElementClass(aec, name.getValue(),
								content, type);
					}
					return;
				} else if (isCorrectAtributeValue(endElement.getName()
						.getLocalPart())) {
					type = endElement.getName().getLocalPart();
					updateContent = false;
				} else {
					throw new GraphIOException("\""
							+ endElement.getName().getLocalPart()
							+ "\" is not expected in an \"attr\" tag.");
				}
			}
		}
	}

	private boolean isCorrectAtributeValue(String value) {
		return value.equals("locator") || value.equals("bool")
				|| value.equals("int") || value.equals("float")
				|| value.equals("string") || value.equals("enum")
				|| value.equals("seq") || value.equals("set")
				|| value.equals("bag") || value.equals("tup");
	}

	private void setFieldOfAttributedElementClass(AttributedElementClass aec,
			String attributeName, String content, String type)
			throws GraphIOException {
		if (attributeName.equals("name")) {
			if (!type.equals("string")) {
				throw new GraphIOException(
						"The qualifiedName value must be of type \"string\".");
			}
			aec.set_qualifiedName(transferIntoQualifiendName(content));
		} else if (attributeName.equals("isabstract")) {
			if (!type.equals("bool")) {
				throw new GraphIOException(
						"The abstract value must be of type \"bool\".");
			}
			((GraphElementClass) aec).set_abstract(Boolean
					.parseBoolean(content));
		} else {
			throw new GraphIOException("\"" + attributeName
					+ "\" is an unknown attribute.");
		}
	}

	private String transferIntoQualifiendName(String content)
			throws GraphIOException {
		if (content.startsWith("#")) {
			throw new GraphIOException("\"" + content
					+ "\" is not an allowed qualified name.");
		}
		int pos = content.lastIndexOf('.') + 1;
		char[] cont = content.toCharArray();
		if (Character.isLowerCase(cont[pos])) {
			cont[pos] = Character.toUpperCase(cont[pos]);
		}
		return new String(cont);
	}

	private NamedElement createNamedElement(String type, String id,
			Attribute fromAttribute, Attribute toAttribute)
			throws GraphIOException {
		if (fromAttribute == null && toAttribute == null) {
			return createNamedElement(type, id);
		}
		EdgeClass ec = (EdgeClass) id2Class.get(id);
		if (ec == null && !id2EnumValues.containsKey(id)) {
			String from = fromAttribute.getValue();
			String to = toAttribute.getValue();
			type = type.split(TYPE_PREFIX)[1];
			if (type.equals("containsValue")) {
				// this edge connects an enumeration constant to an enumeration
				addEnumConstant(from, to);
			} else if (type.equals("hasDomain")) {
				// this edge connects an attribute with a domain
				createHasDomain(from, to);
			} else if (type.equals("contains")) {
				// TODO there does not exist an equivalent element in the tg
				// schema, which expresses, that an GraphElementClass belongs to
				// a graph
			} else if (type.equals("isA")) {
				createSpecialization(from, to);
			} else if (type.equals("hasAttribute")) {
				createHasAttribute(from, to);
			} else {
				throw new GraphIOException("\"" + type
						+ "\" is an unknown type.");
			}
		}
		return ec;
	}

	private void createHasAttribute(String from, String to) {
		AttributedElementClass aec = (AttributedElementClass) id2Class
				.get(from);
		assert aec != null;
		de.uni_koblenz.jgralab.grumlschema.structure.Attribute attribute = id2Attribute
				.get(to);
		if (attribute == null) {
			attribute = schemaGraph.createAttribute();
			id2Attribute.put(to, attribute);
		}
		schemaGraph.createHasAttribute(aec, attribute);
	}

	private void addEnumConstant(String from, String to) {
		EnumDomain enumDomain = (EnumDomain) id2Class.get(from);
		if (enumDomain == null) {
			enumDomain = schemaGraph.createEnumDomain();
			id2Class.put(from, enumDomain);
		}
		String enumConstant = id2EnumValues.get(to);
		if (enumConstant == null) {
			enumConstant = "";
			id2EnumValues.put(to, enumConstant);
		}
		List<String> constants = enumDomain.get_enumConstants();
		if (constants == null) {
			constants = new ArrayList<String>();
			enumDomain.set_enumConstants(constants);
		}
		constants.add(enumConstant);
	}

	private void createHasDomain(String from, String to) {
		de.uni_koblenz.jgralab.grumlschema.structure.Attribute attribute = id2Attribute
				.get(from);
		if (attribute == null) {
			attribute = schemaGraph.createAttribute();
			id2Attribute.put(from, attribute);
		}
		Domain domain = (Domain) id2Class.get(to);
		assert domain != null;
		schemaGraph.createHasDomain(attribute, domain);
	}

	private void createSpecialization(String from, String to) {
		GraphElementClass f = (GraphElementClass) id2Class.get(from);
		GraphElementClass t = (GraphElementClass) id2Class.get(to);
		assert f != null || t != null;
		// create missing class
		if (f == null) {
			if (t.getM1Class() == VertexClass.class) {
				f = schemaGraph.createVertexClass();
			} else {
				f = schemaGraph.createEdgeClass();
			}
			id2Class.put(from, f);
		} else if (t == null) {
			if (f.getM1Class() == VertexClass.class) {
				t = schemaGraph.createVertexClass();
			} else {
				t = schemaGraph.createEdgeClass();
			}
			id2Class.put(to, t);
		}
		if (f.getM1Class() == VertexClass.class) {
			schemaGraph.createSpecializesVertexClass((VertexClass) f,
					(VertexClass) t);
		} else {
			schemaGraph
					.createSpecializesEdgeClass((EdgeClass) f, (EdgeClass) t);
		}
	}

	private NamedElement createNamedElement(String type, String id)
			throws GraphIOException {
		// create NamedElement
		NamedElement ne = id2Class.get(id);
		if (ne == null && !id2EnumValues.containsKey(id)) {
			type = type.split(TYPE_PREFIX)[1];
			if (type.equals("GraphClass")) {
				ne = schemaGraph.createGraphClass();
				schemaGraph.createDefinesGraphClass(
						schemaGraph.getFirstSchema(), (GraphClass) ne);
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
				} else if (type.equals("EdgeClass")) {
					// TODO work at EdgeClass
					ne = schemaGraph.createEdgeClass();
					schemaGraph.createContainsGraphElementClass(currentPackage,
							(GraphElementClass) ne);
				} else if (type.equals("AggregationClass")) {
					// TODO work at AggregationClass
					ne = schemaGraph.createEdgeClass();
					schemaGraph.createContainsGraphElementClass(currentPackage,
							(GraphElementClass) ne);
				} else if (type.equals("CompositionClass")) {
					// TODO work at CompositionClass
					ne = schemaGraph.createEdgeClass();
					schemaGraph.createContainsGraphElementClass(currentPackage,
							(GraphElementClass) ne);
				} else if (type.equals("Bool")) {
					ne = schemaGraph.createBooleanDomain();
					ne.set_qualifiedName("Boolean");
					schemaGraph.createContainsDomain(currentPackage,
							(Domain) ne);
				} else if (type.equals("Int")) {
					ne = schemaGraph.createIntegerDomain();
					ne.set_qualifiedName("Integer");
					schemaGraph.createContainsDomain(currentPackage,
							(Domain) ne);
				} else if (type.equals("Float")) {
					ne = schemaGraph.createDoubleDomain();
					ne.set_qualifiedName("Double");
					schemaGraph.createContainsDomain(currentPackage,
							(Domain) ne);
				} else if (type.equals("String")) {
					ne = schemaGraph.createStringDomain();
					ne.set_qualifiedName("String");
					schemaGraph.createContainsDomain(currentPackage,
							(Domain) ne);
				} else if (type.equals("Enum")) {
					ne = schemaGraph.createEnumDomain();
					ne.set_qualifiedName(id);
					schemaGraph.createContainsDomain(currentPackage,
							(Domain) ne);
				} else {
					throw new GraphIOException("\"" + type
							+ "\" is an unknown type.");
				}
			}
			id2Class.put(id, ne);
		}
		return ne;
	}

	private void createDefaultPackage() {
		currentPackage = schemaGraph.createPackage();
		schemaGraph.createContainsDefaultPackage(schemaGraph.getFirstSchema(),
				currentPackage);
	}
}
