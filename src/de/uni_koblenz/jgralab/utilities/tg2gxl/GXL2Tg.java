package de.uni_koblenz.jgralab.utilities.tg2gxl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.codegenerator.CodeGeneratorConfiguration;
import de.uni_koblenz.jgralab.grumlschema.GrumlSchema;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.grumlschema.domains.Domain;
import de.uni_koblenz.jgralab.grumlschema.domains.EnumDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.AggregationKind;
import de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.ComesFrom;
import de.uni_koblenz.jgralab.grumlschema.structure.EdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GoesTo;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.IncidenceClass;
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
	private final HashMap<EdgeClass, AggregationKind> ec2agg = new HashMap<EdgeClass, AggregationKind>();
	private final HashSet<String> classNames = new HashSet<String>();
	{
		classNames.add("Edge");
		classNames.add("Vertex");
	}

	private de.uni_koblenz.jgralab.schema.Schema schema = null;
	private Graph graph;
	private final HashMap<Integer, GraphElement> id2GraphElement = new HashMap<String, GraphElement>();
	private final Map<String, Method> createMethods = new HashMap<String, Method>();

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
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static void main(String[] args) throws FileNotFoundException,
			XMLStreamException, GraphIOException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
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
			throws FileNotFoundException, XMLStreamException, GraphIOException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		graphInputName = inputGraphName;
		graphOutputName = outputGraph;
		convert();
	}

	public void convert() throws FileNotFoundException, XMLStreamException,
			GraphIOException, IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		inputReader = factory.createXMLEventReader(new FileReader(
				graphInputName));

		// initialize SchemaGraph
		schemaGraph = GrumlSchema.instance().createSchemaGraph();

		convertSchemaGraph();
		schema = new SchemaGraph2Schema().convert(schemaGraph);

		convertGraph();

		inputReader.close();

		// write tg
		if (graph != null) {
			GraphIO.saveGraphToFile(graphOutputName, graph, null);
		} else {
			GraphIO.saveSchemaToFile(graphOutputName, schema);
		}
	}

	/*
	 * schema specific methods
	 */

	private void convertSchemaGraph() throws XMLStreamException,
			GraphIOException {
		while (inputReader.hasNext()) {
			XMLEvent event = inputReader.nextEvent();
			switch (event.getEventType()) {
			case XMLEvent.START_ELEMENT:
				StartElement startElement = event.asStartElement();
				if (startElement.getName().getLocalPart().equals("graph")) {
					createSchema(startElement);
				} else if (startElement.getName().getLocalPart().equals("node")
						|| startElement.getName().getLocalPart().equals("edge")) {
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
					for (Entry<EdgeClass, AggregationKind> entry : ec2agg
							.entrySet()) {
						// the kinds of aggregation are set to the alpha
						// IncidenceClasses
						for (ComesFrom cf : entry.getKey()
								.getComesFromIncidences()) {
							((IncidenceClass) cf.getThat())
									.set_aggregation(entry.getValue());
						}
					}
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
		schema.set_packagePrefix(getPackagePrefixOfSchema());
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

	private String getPackagePrefixOfSchema() {
		String packagePrefix = graphInputName;
		int start = packagePrefix.lastIndexOf(File.separator);
		int end = packagePrefix.lastIndexOf(".");
		return packagePrefix.substring(start + 1, end);
	}

	private void handleNodeOrEdge(StartElement element)
			throws XMLStreamException, GraphIOException {
		String type = null;
		Attribute idAttribute = element.getAttributeByName(new QName("id"));
		String id = idAttribute != null ? idAttribute.getValue() : null;
		Attribute from = element.getAttributeByName(new QName("from"));
		Attribute to = element.getAttributeByName(new QName("to"));
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
					} else if (type.split(TYPE_PREFIX)[1].equals("from")
							|| type.split(TYPE_PREFIX)[1].equals("to")) {
						createIncidenceClass(
								type.split(TYPE_PREFIX)[1].equals("from"),
								from, to);
					} else if (id != null) {
						createNamedElement(type, id, from, to);
					}
					handleAttr(id,
							startElement.getAttributeByName(new QName("id")),
							startElement.getAttributeByName(new QName("name")),
							startElement.getAttributeByName(new QName("kind")),
							from, to);
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
					if (!type.split(TYPE_PREFIX)[1].equals("AttributeClass")
							&& !type.split(TYPE_PREFIX)[1].equals("from")
							&& !type.split(TYPE_PREFIX)[1].equals("to")) {
						createNamedElement(type, id, from, to);
					}
					return;
				}
				break;
			}
		}
	}

	private void handleAttr(String aecId, Attribute id, Attribute name,
			Attribute kind, Attribute edgeClassId, Attribute vertexClassId)
			throws XMLStreamException, GraphIOException {
		AttributedElementClass aec = aecId != null ? (AttributedElementClass) id2Class
				.get(aecId) : null;
		String content = "";
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
					content += event.asCharacters().getData();
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
						if (name.getValue().equals("name")) {
							// this is an attribute
							id2Attribute.get(aecId).set_name(content);
						} else if (name.getValue().equals("limits")) {
							// this is the multiplicity of an IncidenceClass
							setMultiplicities(edgeClassId, vertexClassId,
									content);
						} else if (name.getValue().equals("isordered")) {
							// TODO in JGraLab all Incidences are ordered
						} else {
							throw new GraphIOException("\"" + name.getValue()
									+ "\" is an unknown attribute.");
						}
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

	private void setMultiplicities(Attribute edgeClassId,
			Attribute vertexClassId, String content) {
		IncidenceClass ic = getIncidenceClass(edgeClassId, vertexClassId);
		String[] multiplicities = content.split("</?int>");
		ic.set_min(Integer.parseInt(multiplicities[1]));
		ic.set_max(multiplicities[3].equals("-1") ? Integer.MAX_VALUE : Integer
				.parseInt(multiplicities[3]));
	}

	private IncidenceClass getIncidenceClass(Attribute edgeClassId,
			Attribute vertexClassId) {
		assert edgeClassId != null : "To get the IncidenceClass the id of the corresponding EdgeClass is needed.";
		EdgeClass ec = (EdgeClass) id2Class.get(edgeClassId.getValue());
		assert ec != null : "There does not exist an EdgeClass with the id \""
				+ edgeClassId.getValue() + "\".";
		assert vertexClassId != null : "To get the IncidenceClass the id of the corresponding VertexClass is needed.";
		VertexClass vc = (VertexClass) id2Class.get(vertexClassId.getValue());
		assert vc != null : "There does not exist an VertexClass with the id \""
				+ vertexClassId.getValue() + "\".";

		IncidenceClass ic = (IncidenceClass) ec.getFirstComesFromIncidence()
				.getThat();
		if (ic.getFirstEndsAtIncidence().getThat() == vc) {
			return ic;
		} else {
			ic = (IncidenceClass) ec.getFirstGoesToIncidence().getThat();
			assert ic.getFirstEndsAtIncidence().getThat() == vc : "There does not exist an IncidenceClass which connects the EdgeClass \""
					+ ec.get_qualifiedName()
					+ "\" with the VertexClass \""
					+ vc.get_qualifiedName() + "\".";
			return ic;
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
		} else if (attributeName.equals("isdirected")) {
			// TODO in JGraLab every Edge is directed
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
		// simpleName of VertexClass and EdgeClass must start with upper case
		if (Character.isLowerCase(cont[pos])) {
			cont[pos] = Character.toUpperCase(cont[pos]);
		}
		// TODO check if name already exists
		// this avoid reserved names like Edge or Vertex
		String baseName = new String(cont);
		String name = baseName;
		int counter = 0;
		while (classNames.contains(name)) {
			name = baseName + counter++;
		}
		return name;
	}

	private void createNamedElement(String type, String id,
			Attribute fromAttribute, Attribute toAttribute)
			throws GraphIOException {
		if (fromAttribute == null && toAttribute == null) {
			createNamedElement(type, id);
			return;
		}
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
			throw new GraphIOException("\"" + type + "\" is an unknown type.");
		}
	}

	private void createIncidenceClass(boolean isFrom, Attribute from,
			Attribute to) {
		String f = from.getValue();
		String t = to.getValue();

		EdgeClass ec = (EdgeClass) id2Class.get(f);
		if (ec == null) {
			ec = createEdgeClass();
			id2Class.put(f, ec);
		}

		VertexClass vc = (VertexClass) id2Class.get(t);
		if (vc == null) {
			vc = createVertexClass();
			id2Class.put(t, vc);
		}

		if (!existsIncidenceClass(isFrom, ec, vc)) {
			IncidenceClass ic = schemaGraph.createIncidenceClass();
			ic.set_aggregation(AggregationKind.NONE);
			ic.set_roleName("");
			schemaGraph.createEndsAt(ic, vc);
			if (isFrom) {
				schemaGraph.createComesFrom(ec, ic);
			} else {
				schemaGraph.createGoesTo(ec, ic);
			}
		}
	}

	private boolean existsIncidenceClass(boolean isFrom, EdgeClass ec,
			VertexClass vc) {
		if (isFrom) {
			ComesFrom cf = ec.getFirstComesFromIncidence();
			if (cf == null) {
				return false;
			} else {
				return ((IncidenceClass) cf.getThat())
						.getFirstEndsAtIncidence().getThat() == vc;
			}
		} else {
			GoesTo gt = ec.getFirstGoesToIncidence();
			if (gt == null) {
				return false;
			} else {
				return ((IncidenceClass) gt.getThat())
						.getFirstEndsAtIncidence().getThat() == vc;
			}
		}
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
				f = createVertexClass();
			} else {
				f = createEdgeClass();
			}
			id2Class.put(from, f);
		} else if (t == null) {
			if (f.getM1Class() == VertexClass.class) {
				t = createVertexClass();
			} else {
				t = createEdgeClass();
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

	private void createNamedElement(String type, String id)
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
					ne = createVertexClass();
				} else if (type.equals("EdgeClass")) {
					ne = createEdgeClass();
				} else if (type.equals("AggregationClass")) {
					ne = createEdgeClass();
					ec2agg.put((EdgeClass) ne, AggregationKind.SHARED);
				} else if (type.equals("CompositionClass")) {
					ne = createEdgeClass();
					ec2agg.put((EdgeClass) ne, AggregationKind.COMPOSITE);
				} else if (type.equals("Bool")) {
					if (schemaGraph.getFirstBooleanDomain() == null) {
						ne = schemaGraph.createBooleanDomain();
						ne.set_qualifiedName("Boolean");
						schemaGraph.createContainsDomain(currentPackage,
								(Domain) ne);
					}
				} else if (type.equals("Int")) {
					if (schemaGraph.getFirstIntegerDomain() == null) {
						ne = schemaGraph.createIntegerDomain();
						ne.set_qualifiedName("Integer");
						schemaGraph.createContainsDomain(currentPackage,
								(Domain) ne);
					}
				} else if (type.equals("Float")) {
					if (schemaGraph.getFirstDoubleDomain() == null) {
						ne = schemaGraph.createDoubleDomain();
						ne.set_qualifiedName("Double");
						schemaGraph.createContainsDomain(currentPackage,
								(Domain) ne);
					}
				} else if (type.equals("String")) {
					if (schemaGraph.getFirstStringDomain() == null) {
						ne = schemaGraph.createStringDomain();
						ne.set_qualifiedName("String");
						schemaGraph.createContainsDomain(currentPackage,
								(Domain) ne);
					}
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
	}

	private VertexClass createVertexClass() {
		VertexClass vc;
		vc = schemaGraph.createVertexClass();
		schemaGraph.createContainsGraphElementClass(currentPackage, vc);
		return vc;
	}

	private EdgeClass createEdgeClass() {
		EdgeClass ec;
		ec = schemaGraph.createEdgeClass();
		schemaGraph.createContainsGraphElementClass(currentPackage, ec);
		return ec;
	}

	private void createDefaultPackage() {
		currentPackage = schemaGraph.createPackage();
		schemaGraph.createContainsDefaultPackage(schemaGraph.getFirstSchema(),
				currentPackage);
	}

	/*
	 * graph instance specific methods
	 */

	private void convertGraph() throws XMLStreamException, GraphIOException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		while (inputReader.hasNext()) {
			XMLEvent event = inputReader.nextEvent();
			switch (event.getEventType()) {
			case XMLEvent.START_ELEMENT:
				StartElement startElement = event.asStartElement();
				if (startElement.getName().getLocalPart().equals("graph")) {
					schema.compile(new CodeGeneratorConfiguration());
					createGraph(startElement);
				} else if (startElement.getName().getLocalPart().equals("node")) {
					createVertex(startElement);
				} else if (startElement.getName().getLocalPart().equals("edge")) {
					// TODO handle edge
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
					// graph is completely loaded
					return;
				}
				break;
			}
		}

	}

	private void createVertex(StartElement element) throws XMLStreamException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		String vcName = className();
		Method createMethod = createMethods.get(vcName);
		if (createMethod == null) {
			createMethod = schema.getVertexCreateMethod(vcName,
					ImplementationType.STANDARD);
			createMethods.put(vcName, createMethod);
		}
		int id = getId(element);
		Vertex vertex = (Vertex) createMethod
				.invoke(graph, new Object[] { id });
		assert id2GraphElement.get(id) == null : "There already exists a Vertex with id "
				+ id;
		id2GraphElement.put(id, vertex);

		// TODO handle Attributes
	}

	/**
	 * the id is the id of GXL reduced by (n|e)-
	 * 
	 * @param element
	 * @return
	 */
	private int getId(StartElement element) {
		String id = element.getAttributeByName(new QName("id")).getValue()
				.substring(1);
		return Integer.parseInt(id.charAt(0) == '-' ? id.substring(1) : id);
	}

	private String className() throws XMLStreamException {
		return getPackagePrefixOfSchema() + "." + extractType();
	}

	private void createGraph(StartElement element) throws XMLStreamException,
			GraphIOException, IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {

		// extract name of GraphClass
		String nameOfGraphClass = extractType();

		// create graph
		de.uni_koblenz.jgralab.schema.GraphClass gc = schema.getGraphClass();
		if (!gc.getQualifiedName().equals(nameOfGraphClass)) {
			throw new GraphIOException("The GraphClass \"" + nameOfGraphClass
					+ "\" is undefined.");
		}

		graph = (Graph) schema
				.getGraphCreateMethod(ImplementationType.STANDARD).invoke(
						null,
						new Object[] {
								element.getAttributeByName(new QName("id"))
										.getValue(), 100, 100 });
		Attribute role = element.getAttributeByName(new QName("role"));
		if (role != null) {
			// TODO the role of a graph is not defined in JGraLab
		}
		Attribute edgeids = element.getAttributeByName(new QName("edgeids"));
		if (edgeids != null) {
			// TODO JGraLab uses it own ids
		}
		Attribute hypergraph = element.getAttributeByName(new QName(
				"hypergraph"));
		if (hypergraph != null) {
			if (Boolean.parseBoolean(hypergraph.getValue())) {
				throw new GraphIOException("Hypergraphs are not supported yet.");
			}
		}
		Attribute edgemode = element.getAttributeByName(new QName("edgemode"));
		if (edgemode != null) {
			// TODO in JGraLab all edges are directed
		}
	}

	private String extractType() throws XMLStreamException {
		String nameOfGraphClass = null;
		if (inputReader.hasNext()) {
			XMLEvent event = inputReader.peek();
			switch (event.getEventType()) {
			case XMLEvent.CHARACTERS:
				event = inputReader.nextEvent();
				event = inputReader.peek();
			case XMLEvent.START_ELEMENT:
				StartElement startElement = event.asStartElement();
				if (startElement.getName().getLocalPart().equals("type")) {
					nameOfGraphClass = startElement.getAttributeByName(
							new QName("http://www.w3.org/1999/xlink", "href"))
							.getValue();
					// skip '#'
					nameOfGraphClass = nameOfGraphClass.substring(1);
					event = inputReader.nextEvent();
				}
			}
		}
		return nameOfGraphClass;
	}
}
