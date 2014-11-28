package de.uni_koblenz.jgralab.utilities.argouml2tg;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.grumlschema.GrumlSchema;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.grumlschema.domains.BasicDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.BooleanDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.CollectionDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.Domain;
import de.uni_koblenz.jgralab.grumlschema.domains.DoubleDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.EnumDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.HasRecordDomainComponent;
import de.uni_koblenz.jgralab.grumlschema.domains.IntegerDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.LongDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.MapDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.RecordDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.StringDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.Annotates;
import de.uni_koblenz.jgralab.grumlschema.structure.Attribute;
import de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.Comment;
import de.uni_koblenz.jgralab.grumlschema.structure.Constraint;
import de.uni_koblenz.jgralab.grumlschema.structure.EdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.HasAttribute;
import de.uni_koblenz.jgralab.grumlschema.structure.HasConstraint;
import de.uni_koblenz.jgralab.grumlschema.structure.IncidenceClass;
import de.uni_koblenz.jgralab.grumlschema.structure.NamedElement;
import de.uni_koblenz.jgralab.grumlschema.structure.Package;
import de.uni_koblenz.jgralab.grumlschema.structure.Schema;
import de.uni_koblenz.jgralab.grumlschema.structure.SpecializesEdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.SpecializesVertexClass;
import de.uni_koblenz.jgralab.grumlschema.structure.VertexClass;
import de.uni_koblenz.jgralab.utilities.rsa2tg.Rsa2Tg;
import de.uni_koblenz.jgralab.utilities.rsa2tg.SchemaGraph2XMI;
import de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants4SchemaGraph2XMI;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.Schema2SchemaGraph;

public class Tg2ArgoUml {

	private class ReplacementOutputStream extends OutputStream {

		private final OutputStream output;

		public ReplacementOutputStream(OutputStream out) {
			output = out;
		}

		@Override
		public void write(int b) throws IOException {
			if (b == '\n') {
				output.write('&');
				output.write('#');
				output.write('x');
				output.write('0');
				output.write('a');
				output.write(';');
			} else if (b == '\r') {
				output.write('&');
				output.write('#');
				output.write('x');
				output.write('0');
				output.write('d');
				output.write(';');
			} else {
				output.write(b);
			}
		}

	}

	private static final String UML_PROFILE_PREFIX = "http://argouml.org/user-profiles/gruml-1.0.1.xmi#";
	private static final String UML_NAMESPACE_URI = "org.omg.xmi.namespace.UML";
	/**
	 * If set to <code>true</code>, the EdgeClasses are created as associations
	 * which are bidirectional navigable.
	 */
	private boolean isBidirectional = false;

	private boolean addEdgeClassGeneralizationAsComment = false;

	private final Map<Domain, String> domain2id = new HashMap<>();

	/**
	 * Processes an TG-file as schema or a schema in a grUML graph to a XMI
	 * file. For all command line options see
	 * {@link Rsa2Tg#processCommandLineOptions(String[])}.
	 * 
	 * @param args
	 *            {@link String} array of command line options.
	 * @throws GraphIOException
	 */
	public static void main(String[] args) throws GraphIOException {

		System.out.println("Tg to ArgoUML");
		System.out.println("==================");

		Tg2ArgoUml s = new Tg2ArgoUml();

		// Retrieving all command line options
		CommandLine cli = processCommandLineOptions(args);

		assert cli != null : "No CommandLine object has been generated!";

		s.isBidirectional = cli.hasOption("b");

		s.addEdgeClassGeneralizationAsComment = cli.hasOption("c");

		String outputName = cli.getOptionValue("o");
		try {
			if (cli.hasOption("ig")) {
				s.process(
						GrumlSchema.instance().loadSchemaGraph(
								cli.getOptionValue("ig")), outputName);
			} else {
				s.process(new Schema2SchemaGraph().convert2SchemaGraph(GraphIO
						.loadSchemaFromFile(cli.getOptionValue("i"))),
						outputName);
			}
		} catch (Exception e) {
			System.err.println("An Exception occured while processing "
					+ (cli.hasOption("i") ? cli.getOptionValue("i") : cli
							.getOptionValue("ig")) + ".");
			System.err.println(e.getMessage());
			e.printStackTrace();
			e.printStackTrace();
		}

		System.out.println("Fini.");
	}

	/**
	 * Processes all command line parameters and returns a {@link CommandLine}
	 * object, which holds all values included in the given {@link String}
	 * array.
	 * 
	 * @param args
	 *            {@link CommandLine} parameters.
	 * @return {@link CommandLine} object, which holds all necessary values.
	 */
	public static CommandLine processCommandLineOptions(String[] args) {

		// Creates a OptionHandler.
		String toolString = "java " + SchemaGraph2XMI.class.getName();
		String versionString = JGraLab.getInfo(false);

		OptionHandler oh = new OptionHandler(toolString, versionString);

		// Several Options are declared.
		Option output = new Option("o", "output", true,
				"(required): the output xmi file name");
		output.setRequired(true);
		output.setArgName("file");
		oh.addOption(output);

		Option schemaGraph = new Option("ig", "inputSchemaGraph", true,
				"(required or -i):if set, the schemaGraph is converted into a xmi.");
		schemaGraph.setRequired(false);
		schemaGraph.setArgs(0);
		output.setArgName("file");
		oh.addOption(schemaGraph);

		Option schema = new Option(
				"i",
				"inputSchema",
				true,
				"(required or -ig): TG-file of the schema which should be converted into a xmi.");
		schema.setRequired(false);
		schema.setArgName("file");
		oh.addOption(schema);

		// either graph or schema has to be provided
		OptionGroup input = new OptionGroup();
		input.addOption(schemaGraph);
		input.addOption(schema);
		input.setRequired(true);
		oh.addOptionGroup(input);

		Option bidirectional = new Option("b", "bidirectional", false,
				"(optional): If set the EdgeClasses are created as bidirectional associations.");
		bidirectional.setRequired(false);
		oh.addOption(bidirectional);

		Option genAsComment = new Option("c",
				"commentsForEdgeClassGeneralization", false,
				"(optional): Creates comments which show the generalization of EdgeClasses.");
		genAsComment.setRequired(false);
		oh.addOption(genAsComment);

		return oh.parse(args);
	}

	/**
	 * Converts the {@link SchemaGraph} to the XMI file <code>xmiName</code>.
	 * 
	 * @param schemaGraph
	 *            {@link SchemaGraph} the schema graph to be converted.
	 * @param xmiName
	 *            {@link String} the path of the xmi file to be created.
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public void process(SchemaGraph schemaGraph, String xmiName)
			throws XMLStreamException, IOException {
		createXMI(xmiName, schemaGraph);
	}

	/**
	 * This method creates the XMI file. Created content is:<br/>
	 * <code>&lt;?xml version="{@link XMIConstants4SchemaGraph2XMI#XML_VERSION}" encoding="{@link XMIConstants4SchemaGraph2XMI#XML_ENCODING}"?&gt;<br/>
	 * &lt;!-- content created by {@link SchemaGraph2XMI#createRootElement(XMLStreamWriter, SchemaGraph)} --&gt;
	 * </code>
	 * 
	 * @param xmiName
	 *            {@link String} the path of the XMI file
	 * @param schemaGraph
	 *            {@link SchemaGraph} to be converted into an XMI
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	private void createXMI(String xmiName, SchemaGraph schemaGraph)
			throws XMLStreamException, IOException {
		Writer out = null;
		XMLStreamWriter writer = null;
		try {
			// create the XMLStreamWriter which creates the current xmi-file.
			out = new BufferedWriter(new OutputStreamWriter(
					new ReplacementOutputStream(new FileOutputStream(xmiName)),
					"UTF-8"));
			XMLOutputFactory factory = XMLOutputFactory.newInstance();
			factory.setProperty("javax.xml.stream.isRepairingNamespaces",
					Boolean.TRUE);
			writer = factory.createXMLStreamWriter(out);

			// write the first line
			writer.writeStartDocument("UTF-8", "1.0");
			createRootElement(writer, schemaGraph);
			// write the end of the document
			writer.writeEndDocument();

			// close the XMLStreamWriter
			writer.flush();
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// no handling of exceptions, because they are throw as mentioned in
			// the declaration.
			if (writer != null) {
				writer.close();
			}
			if (out != null) {
				out.close();
			}
		}
	}

	private void createRootElement(XMLStreamWriter writer,
			SchemaGraph schemaGraph) throws XMLStreamException {
		writer.writeStartElement("XMI");
		writer.writeAttribute("xmi.version", "1.2");
		writer.setPrefix("UML", UML_NAMESPACE_URI);
		writer.writeAttribute("tiemstamp", getCurrentTime());

		writer.writeStartElement("XMI.header");
		writer.writeStartElement("XMI.documentation");

		String[] versionParts = JGraLab.getVersionInfo(false).split("\\n");

		writer.writeStartElement("XMI.exporter");
		writer.writeCharacters(Tg2ArgoUml.class.getName() + " (part of "
				+ versionParts[0].trim() + ")");
		writer.writeEndElement();

		writer.writeStartElement("XMI.exporterVersion");
		writer.writeCharacters(versionParts[1].split("\\s*\\:\\s*")[1] + " ("
				+ versionParts[2].split("\\s*\\:\\s*")[1] + ")");
		writer.writeEndElement();

		writer.writeEndElement();

		writer.writeStartElement("XMI.metamodel");
		writer.writeAttribute("xmi.name", "UML");
		writer.writeAttribute("xmi.version", "1.4");
		writer.writeEndElement();

		writer.writeEndElement();

		writer.writeStartElement("XMI.content");

		createSchema(writer, schemaGraph.getFirstSchema());

		writer.writeEndElement();

		writer.writeEndElement();
	}

	private String getCurrentTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
		return dateFormat.format(new Date(System.currentTimeMillis()));
	}

	private void createSchema(XMLStreamWriter writer, Schema schema)
			throws XMLStreamException {
		Map<Vertex, String> comment2id = new HashMap<>();

		writer.writeStartElement(UML_NAMESPACE_URI, "Model");
		writer.writeAttribute("xmi.id", schema.get_packagePrefix() + "."
				+ schema.get_name());
		writer.writeAttribute("name",
				schema.get_packagePrefix() + "." + schema.get_name());
		writer.writeAttribute("isSpecification", "false");
		writer.writeAttribute("isRoot", "false");
		writer.writeAttribute("isLeaf", "false");
		writer.writeAttribute("isAbstract", "false");

		writer.writeStartElement(UML_NAMESPACE_URI, "Namespace.ownedElement");

		createGraphClass(writer, schema.get_graphclass(), comment2id);

		createPackage(writer, schema.get_defaultpackage(), comment2id);

		createCommentsAndConstraints(writer, comment2id);

		writer.writeEndElement();

		writer.writeEndElement();
	}

	private void createGraphClass(XMLStreamWriter writer,
			GraphClass graphClass, Map<Vertex, String> comment2id)
			throws XMLStreamException {
		writer.writeStartElement(UML_NAMESPACE_URI, "Class");
		writer.writeAttribute("xmi.id", graphClass.get_qualifiedName());
		writer.writeAttribute("name",
				getSimpleName(graphClass.get_qualifiedName()));
		writer.writeAttribute("visibility", "public");
		writer.writeAttribute("isSpecification", "false");
		writer.writeAttribute("isRoot", "false");
		writer.writeAttribute("isLeaf", "false");
		writer.writeAttribute("isAbstract", "false");
		writer.writeAttribute("isActive", "false");

		attachCommentsAndConstraints(writer, graphClass, comment2id);

		attachStereotype(writer, ArgoUml2Tg.ST_GRAPHCLASS);

		createAttributes(writer, graphClass);

		writer.writeEndElement();
	}

	private void createPackage(XMLStreamWriter writer, Package pack,
			Map<Vertex, String> comment2id) throws XMLStreamException {
		if (pack.get_qualifiedName() != null
				&& !pack.get_qualifiedName().isEmpty()) {
			writer.writeStartElement(UML_NAMESPACE_URI, "Package");
			writer.writeAttribute("xmi.id", pack.get_qualifiedName());
			writer.writeAttribute("name",
					getSimpleName(pack.get_qualifiedName()));
			writer.writeAttribute("isSpecification", "false");
			writer.writeAttribute("isRoot", "false");
			writer.writeAttribute("isLeaf", "false");
			writer.writeAttribute("isAbstract", "false");

			attachCommentsAndConstraints(writer, pack, comment2id);

			comment2id = new HashMap<>();

			writer.writeStartElement(UML_NAMESPACE_URI,
					"Namespace.ownedElement");
		}

		for (Domain dom : pack.get_domains()) {
			createDomain(writer, dom, comment2id);
		}

		Set<Edge> specializations = new HashSet<>();

		for (GraphElementClass gec : pack.get_graphelementclasses()) {
			if (gec.isInstanceOf(VertexClass.VC)) {
				createVertexClass(writer, (VertexClass) gec, specializations,
						comment2id);
			} else {
				createEdgeClass(writer, (EdgeClass) gec, specializations,
						comment2id);
			}
		}

		createGeneralization(writer, specializations);

		for (Package subpackage : pack.get_subpackages()) {
			createPackage(writer, subpackage, comment2id);
		}

		if (pack.get_qualifiedName() != null
				&& !pack.get_qualifiedName().isEmpty()) {
			createCommentsAndConstraints(writer, comment2id);

			writer.writeEndElement();

			writer.writeEndElement();
		}
	}

	private void createGeneralization(XMLStreamWriter writer,
			Set<Edge> specializations) throws XMLStreamException {
		for (Edge edge : specializations) {
			GraphElementClass subClass = (GraphElementClass) edge.getAlpha();
			GraphElementClass superClass = (GraphElementClass) edge.getOmega();
			boolean isAssociation = subClass.isInstanceOf(VertexClass.VC);
			String generalizationType = isAssociation ? "Class" : "Association";

			writer.writeStartElement(UML_NAMESPACE_URI, "Generalization");
			writer.writeAttribute("xmi.id", subClass.get_qualifiedName()
					+ "_specializes_" + superClass.get_qualifiedName());

			writer.writeStartElement(UML_NAMESPACE_URI, "Generalization.child");
			writer.writeEmptyElement(
					UML_NAMESPACE_URI,
					generalizationType
							+ (isAssociation
									&& subClass.getDegree(HasAttribute.EC) > 0 ? "Class"
									: ""));
			writer.writeAttribute("xmi.idref", subClass.get_qualifiedName());
			writer.writeEndElement();

			writer.writeStartElement(UML_NAMESPACE_URI, "Generalization.parent");
			writer.writeEmptyElement(
					UML_NAMESPACE_URI,
					generalizationType
							+ (isAssociation
									&& superClass.getDegree(HasAttribute.EC) > 0 ? "Class"
									: ""));
			writer.writeAttribute("xmi.idref", superClass.get_qualifiedName());
			writer.writeEndElement();

			writer.writeEndElement();
		}
	}

	private void createDomain(XMLStreamWriter writer, Domain dom,
			Map<Vertex, String> comment2id) throws XMLStreamException {
		if (dom.isInstanceOf(BasicDomain.VC)) {
			return;
		} else if (dom.isInstanceOf(RecordDomain.VC)) {
			createRecordDomain(writer, (RecordDomain) dom, comment2id);
		} else if (dom.isInstanceOf(EnumDomain.VC)) {
			createEnumDomain(writer, (EnumDomain) dom, comment2id);
		} else {
			// collection or map
			String qualifiedDomainName = getDomainId(dom);

			if (dom.getDegree(Annotates.EC) > 0) {
				writer.writeStartElement(UML_NAMESPACE_URI, "DataType");
			} else {
				writer.writeEmptyElement(UML_NAMESPACE_URI, "DataType");
			}
			writer.writeAttribute("xmi.id", qualifiedDomainName);
			writer.writeAttribute("name", qualifiedDomainName);
			writer.writeAttribute("isSpecification", "false");
			writer.writeAttribute("isRoot", "false");
			writer.writeAttribute("isLeaf", "false");
			writer.writeAttribute("isAbstract", "false");

			if (dom.getDegree(Annotates.EC) > 0) {
				attachCommentsAndConstraints(writer, dom, comment2id);
				writer.writeEndElement();
			}
		}
	}

	private void createRecordDomain(XMLStreamWriter writer,
			RecordDomain recordDomain, Map<Vertex, String> comment2id)
			throws XMLStreamException {
		// record domains must have at least one attribute
		writer.writeStartElement(UML_NAMESPACE_URI, "Class");
		writer.writeAttribute("xmi.id", recordDomain.get_qualifiedName());
		writer.writeAttribute("name",
				getSimpleName(recordDomain.get_qualifiedName()));
		writer.writeAttribute("visibility", "public");
		writer.writeAttribute("isSpecification", "false");
		writer.writeAttribute("isRoot", "false");
		writer.writeAttribute("isLeaf", "false");
		writer.writeAttribute("isAbstract", "false");
		writer.writeAttribute("isActive", "false");

		attachCommentsAndConstraints(writer, recordDomain, comment2id);

		attachStereotype(writer, ArgoUml2Tg.ST_RECORD);

		writer.writeStartElement(UML_NAMESPACE_URI, "Classifier.feature");

		for (HasRecordDomainComponent hrdc : recordDomain
				.getHasRecordDomainComponentIncidences(EdgeDirection.OUT)) {
			Domain dom = (Domain) hrdc.getThat();
			String attributeName = hrdc.get_name();
			String attributeId = recordDomain.get_qualifiedName()
					+ "_attribute_" + attributeName + ":"
					+ dom.get_qualifiedName();

			attacheAttribute(writer, dom, attributeName, attributeId, null);
		}

		writer.writeEndElement();

		writer.writeEndElement();
	}

	private void createEnumDomain(XMLStreamWriter writer,
			EnumDomain enumDomain, Map<Vertex, String> comment2id)
			throws XMLStreamException {
		String enumId = getDomainId(enumDomain);

		writer.writeStartElement(UML_NAMESPACE_URI, "Enumeration");
		writer.writeAttribute("xmi.id", enumId);
		writer.writeAttribute("name", getSimpleName(enumId));
		writer.writeAttribute("isSpecification", "false");
		writer.writeAttribute("isRoot", "false");
		writer.writeAttribute("isLeaf", "false");
		writer.writeAttribute("isAbstract", "false");

		attachCommentsAndConstraints(writer, enumDomain, comment2id);

		writer.writeStartElement(UML_NAMESPACE_URI, "Enumeration.literal");

		for (String enumConstant : enumDomain.get_enumConstants()) {
			writer.writeEmptyElement(UML_NAMESPACE_URI, "EnumerationLiteral");
			writer.writeAttribute("xmi.id", enumId + "_constant_"
					+ enumConstant);
			writer.writeAttribute("name", enumConstant);
			writer.writeAttribute("isSpecification", "false");
		}

		writer.writeEndElement();

		writer.writeEndElement();
	}

	private void createVertexClass(XMLStreamWriter writer,
			VertexClass vertexClass, Set<Edge> specializations,
			Map<Vertex, String> comment2id) throws XMLStreamException {
		if (hasVertexClassRepresentationChildren(vertexClass)) {
			writer.writeStartElement(UML_NAMESPACE_URI, "Class");
		} else {
			writer.writeEmptyElement(UML_NAMESPACE_URI, "Class");
		}
		writer.writeAttribute("xmi.id", vertexClass.get_qualifiedName());
		writer.writeAttribute("name",
				getSimpleName(vertexClass.get_qualifiedName()));
		writer.writeAttribute("visibility", "public");
		writer.writeAttribute("isSpecification", "false");
		writer.writeAttribute("isRoot", "false");
		writer.writeAttribute("isLeaf", "false");
		writer.writeAttribute("isAbstract", vertexClass.is_abstract() ? "true"
				: "false");
		writer.writeAttribute("isActive", "false");

		attachCommentsAndConstraints(writer, vertexClass, comment2id);

		if (vertexClass.is_abstract()) {
			attachStereotype(writer, ArgoUml2Tg.ST_ABSTRACT);
		}

		createAttributes(writer, vertexClass);

		attachGeneralization(writer, vertexClass, specializations);

		if (hasVertexClassRepresentationChildren(vertexClass)) {
			writer.writeEndElement();
		}
	}

	private boolean hasVertexClassRepresentationChildren(VertexClass vertexClass) {
		return vertexClass.is_abstract()
				|| vertexClass.getDegree(HasAttribute.EC) > 0
				|| vertexClass.getDegree(HasConstraint.EC) > 0
				|| vertexClass.getDegree(Annotates.EC) > 0
				|| vertexClass.getDegree(SpecializesVertexClass.EC,
						EdgeDirection.OUT) > 0;
	}

	private void createEdgeClass(XMLStreamWriter writer, EdgeClass edgeClass,
			Set<Edge> specializations, Map<Vertex, String> comment2id)
			throws XMLStreamException {
		boolean isAssociation = edgeClass.getDegree(HasAttribute.EC) == 0;

		writer.writeStartElement(UML_NAMESPACE_URI,
				isAssociation ? "Association" : "AssociationClass");
		writer.writeAttribute("xmi.id", edgeClass.get_qualifiedName());
		writer.writeAttribute("name",
				getSimpleName(edgeClass.get_qualifiedName()));
		if (!isAssociation) {
			writer.writeAttribute("visibility", "public");
		}
		writer.writeAttribute("isSpecification", "false");
		writer.writeAttribute("isRoot", "false");
		writer.writeAttribute("isLeaf", "false");
		writer.writeAttribute("isAbstract", edgeClass.is_abstract() ? "true"
				: "false");
		if (!isAssociation) {
			writer.writeAttribute("isActive", "false");
		}

		attachCommentsAndConstraints(writer, edgeClass, comment2id);
		if (addEdgeClassGeneralizationAsComment
				&& edgeClass.getDegree(SpecializesEdgeClass.EC,
						EdgeDirection.OUT) > 0) {
			writer.writeStartElement(UML_NAMESPACE_URI, "ModelElement.comment");
			attachCommentAndConstraints(writer, edgeClass,
					"generalizationComment_" + edgeClass.get_qualifiedName(),
					"_comment_", 0, comment2id);
			writer.writeEndElement();
		}

		if (edgeClass.is_abstract()) {
			attachStereotype(writer, ArgoUml2Tg.ST_ABSTRACT);
		}

		writer.writeStartElement(UML_NAMESPACE_URI, "Association.connection");
		attacheAssociationEnd(writer, edgeClass, edgeClass.get_from());
		attacheAssociationEnd(writer, edgeClass, edgeClass.get_to());
		writer.writeEndElement();

		createAttributes(writer, edgeClass);

		attachGeneralization(writer, edgeClass, specializations);

		writer.writeEndElement();
	}

	private void attacheAssociationEnd(XMLStreamWriter writer,
			EdgeClass edgeClass, IncidenceClass incidenceClass)
			throws XMLStreamException {
		boolean isAlpha = edgeClass.get_from() == incidenceClass;
		String aggregation;
		switch ((isAlpha ? edgeClass.get_to() : edgeClass.get_from())
				.get_aggregation()) {
		case COMPOSITE:
			aggregation = "composite";
			break;
		case SHARED:
			aggregation = "aggregate";
			break;
		default:
			aggregation = "none";
		}
		String associationEndId = (isAlpha ? "alpha" : "omega") + "_incidence_"
				+ edgeClass.get_qualifiedName() + "_"
				+ incidenceClass.get_targetclass().get_qualifiedName();

		writer.writeStartElement(UML_NAMESPACE_URI, "AssociationEnd");
		writer.writeAttribute("xmi.id", associationEndId);
		if (incidenceClass.get_roleName() != null
				&& !incidenceClass.get_roleName().isEmpty()) {
			writer.writeAttribute("name", incidenceClass.get_roleName());
		}
		writer.writeAttribute("visibility", "public");
		writer.writeAttribute("isSpecification", "false");
		writer.writeAttribute("isNavigable",
				isBidirectional || !isAlpha ? "true" : "false");
		writer.writeAttribute("ordering", "unordered");
		writer.writeAttribute("aggregation", aggregation);
		writer.writeAttribute("targetScope", "instance");
		writer.writeAttribute("changeability", "changeable");

		writer.writeStartElement(UML_NAMESPACE_URI,
				"AssociationEnd.multiplicity");
		createMultiplicity(writer, associationEndId, incidenceClass.get_min(),
				incidenceClass.get_max());
		writer.writeEndElement();

		writer.writeStartElement(UML_NAMESPACE_URI,
				"AssociationEnd.participant");
		writer.writeEmptyElement(UML_NAMESPACE_URI, "Class");
		writer.writeAttribute("xmi.idref", incidenceClass.get_targetclass()
				.get_qualifiedName());
		writer.writeEndElement();

		writer.writeEndElement();
	}

	private void attachGeneralization(XMLStreamWriter writer,
			GraphElementClass graphElementClass, Set<Edge> specializations)
			throws XMLStreamException {
		boolean hasSpecializations = false;
		for (Edge svc : graphElementClass.incidences(graphElementClass
				.isInstanceOf(VertexClass.VC) ? SpecializesVertexClass.EC
				: SpecializesEdgeClass.EC, EdgeDirection.OUT)) {
			specializations.add(svc);
			if (!hasSpecializations) {
				writer.writeStartElement(UML_NAMESPACE_URI,
						"GeneralizableElement.generalization");
				hasSpecializations = true;
			}
			writer.writeEmptyElement(UML_NAMESPACE_URI, "Generalization");
			writer.writeAttribute(
					"xmi.idref",
					((GraphElementClass) svc.getAlpha()).get_qualifiedName()
							+ "_specializes_"
							+ ((GraphElementClass) svc.getOmega())
									.get_qualifiedName());
		}
		if (hasSpecializations) {
			writer.writeEndElement();
		}
	}

	private void createAttributes(XMLStreamWriter writer,
			AttributedElementClass attrElementClass) throws XMLStreamException {
		boolean hasAttributes = false;
		for (Attribute attr : attrElementClass.get_attributes()) {
			if (!hasAttributes) {
				writer.writeStartElement(UML_NAMESPACE_URI,
						"Classifier.feature");
				hasAttributes = true;
			}
			Domain dom = attr.get_domain();
			String attributeName = attr.get_name();
			String attributeId = attrElementClass.get_qualifiedName()
					+ "_attribute_" + attributeName + ":"
					+ dom.get_qualifiedName();

			attacheAttribute(writer, dom, attributeName, attributeId,
					attr.get_defaultValue());
		}
		if (hasAttributes) {
			writer.writeEndElement();
		}
	}

	private void attacheAttribute(XMLStreamWriter writer, Domain dom,
			String attributeName, String attributeId, String defaultValue)
			throws XMLStreamException {
		String type = "";
		String nameOfAttributeOfDomainRef = "";
		String referencedDomainId = getDomainId(dom);
		if (dom.isInstanceOf(BasicDomain.VC)) {
			type = "DataType";
			nameOfAttributeOfDomainRef = "href";
		} else if (dom.isInstanceOf(EnumDomain.VC)) {
			type = "Enumeration";
			nameOfAttributeOfDomainRef = "xmi.idref";
		} else if (dom.isInstanceOf(RecordDomain.VC)) {
			type = "Class";
			nameOfAttributeOfDomainRef = "xmi.idref";
		} else {
			// collection or map
			type = "DataType";
			nameOfAttributeOfDomainRef = "xmi.idref";
		}

		writer.writeStartElement(UML_NAMESPACE_URI, "Attribute");
		writer.writeAttribute("xmi.id", attributeId);
		writer.writeAttribute("name", attributeName);
		writer.writeAttribute("visibility", "public");
		writer.writeAttribute("isSpecification", "false");
		writer.writeAttribute("ownerScope", "instance");
		writer.writeAttribute("changeability", "changeable");
		writer.writeAttribute("targetScope", "instance");

		writer.writeStartElement(UML_NAMESPACE_URI,
				"StructuralFeature.multiplicity");
		createMultiplicity(writer, attributeId, 1, 1);
		writer.writeEndElement();

		// default value
		if (defaultValue != null) {
			writer.writeStartElement(UML_NAMESPACE_URI,
					"Attribute.initialValue");

			writer.writeEmptyElement(UML_NAMESPACE_URI, "Expression");
			writer.writeAttribute("xmi.id", attributeId + "_defaultValue");
			writer.writeAttribute("language", "");
			writer.writeAttribute("body", defaultValue);

			writer.writeEndElement();
		}

		// attribute type
		writer.writeStartElement(UML_NAMESPACE_URI, "StructuralFeature.type");

		writer.writeEmptyElement(UML_NAMESPACE_URI, type);
		writer.writeAttribute(nameOfAttributeOfDomainRef, referencedDomainId);

		writer.writeEndElement();

		writer.writeEndElement();
	}

	private String getDomainId(Domain dom) {
		String id = domain2id.get(dom);
		if (id == null) {
			if (dom.isInstanceOf(BooleanDomain.VC)) {
				id = UML_PROFILE_PREFIX + ArgoUml2Tg.DT_BOOLEAN;
			} else if (dom.isInstanceOf(IntegerDomain.VC)) {
				id = UML_PROFILE_PREFIX + ArgoUml2Tg.DT_INTEGER;
			} else if (dom.isInstanceOf(LongDomain.VC)) {
				id = UML_PROFILE_PREFIX + ArgoUml2Tg.DT_LONG;
			} else if (dom.isInstanceOf(DoubleDomain.VC)) {
				id = UML_PROFILE_PREFIX + ArgoUml2Tg.DT_DOUBLE;
			} else if (dom.isInstanceOf(StringDomain.VC)) {
				id = UML_PROFILE_PREFIX + ArgoUml2Tg.DT_STRING;
			} else {
				id = dom.get_qualifiedName();
			}
			domain2id.put(dom, id);
		}
		return id;
	}

	private void createMultiplicity(XMLStreamWriter writer, String baseId,
			int min, int max) throws XMLStreamException {
		writer.writeStartElement(UML_NAMESPACE_URI, "Multiplicity");
		writer.writeAttribute("xmi.id", baseId + "_multiplicity");

		writer.writeStartElement(UML_NAMESPACE_URI, "Multiplicity.range");

		writer.writeEmptyElement(UML_NAMESPACE_URI, "MultiplicityRange");
		writer.writeAttribute("xmi.id", baseId + "_multiplicityRange");
		writer.writeAttribute("lower", Integer.toString(min));
		writer.writeAttribute("upper",
				Integer.toString(max == Integer.MAX_VALUE ? -1 : max));

		writer.writeEndElement();

		writer.writeEndElement();
	}

	private void attachCommentsAndConstraints(XMLStreamWriter writer,
			NamedElement namedElement, Map<Vertex, String> comment2id)
			throws XMLStreamException {
		if (namedElement.getDegree(Annotates.EC) == 0
				&& namedElement.getDegree(HasConstraint.EC) == 0) {
			return;
		}
		int i = 0;
		writer.writeStartElement(UML_NAMESPACE_URI, "ModelElement.comment");
		String idInfix = "_comment_";
		for (Comment comment : namedElement.get_comments()) {
			i = attachCommentAndConstraints(writer, comment,
					namedElement.get_qualifiedName(), idInfix, i, comment2id);
		}
		idInfix = "_constraint_";
		if (namedElement.isInstanceOf(AttributedElementClass.VC)) {
			for (Constraint constraint : ((AttributedElementClass) namedElement)
					.get_constraints()) {
				i = attachCommentAndConstraints(writer, constraint,
						namedElement.get_qualifiedName(), idInfix, i,
						comment2id);
			}
		}
		writer.writeEndElement();
	}

	private int attachCommentAndConstraints(XMLStreamWriter writer,
			Vertex vertex, String idPrefix, String idInfix, int idUnifier,
			Map<Vertex, String> comment2id) throws XMLStreamException {
		String commentId = comment2id.get(vertex);
		if (commentId == null) {
			commentId = idPrefix + idInfix + idUnifier++;
			comment2id.put(vertex, commentId);
		}
		writer.writeEmptyElement(UML_NAMESPACE_URI, "Comment");
		writer.writeAttribute("xmi.idref", commentId);
		return idUnifier;
	}

	private void createCommentsAndConstraints(XMLStreamWriter writer,
			Map<Vertex, String> comment2id) throws XMLStreamException {
		for (Entry<Vertex, String> entry : comment2id.entrySet()) {
			boolean isComment = entry.getKey().isInstanceOf(Comment.VC);
			String annotatedElementType = "";
			String qualifiedName = "";
			String commentBody = "";
			if (entry.getKey() instanceof EdgeClass) {
				// create comment which describes superclasses
				if (entry.getKey().getDegree(HasAttribute.EC) == 0) {
					annotatedElementType = "Association";
				} else {
					annotatedElementType = "AssociationClass";
				}
				qualifiedName = ((EdgeClass) entry.getKey())
						.get_qualifiedName();
				String delim = ": ";
				StringBuilder sb = new StringBuilder(qualifiedName);
				for (EdgeClass superclass : ((EdgeClass) entry.getKey())
						.get_superclasses()) {
					sb.append(delim).append(superclass.get_qualifiedName());
					delim = ", ";
				}
				commentBody = sb.toString();
			} else {
				NamedElement annotatedElement = isComment ? ((Comment) entry
						.getKey()).get_annotatedelement() : ((Constraint) entry
						.getKey()).get_constrainedelement();
				qualifiedName = annotatedElement.get_qualifiedName();
				if (annotatedElement.isInstanceOf(Package.VC)) {
					annotatedElementType = "Package";
				} else if (annotatedElement.isInstanceOf(EdgeClass.VC)) {
					if (annotatedElement.getDegree(HasAttribute.EC) == 0) {
						annotatedElementType = "Association";
					} else {
						annotatedElementType = "AssociationClass";
					}
				} else if (annotatedElement.isInstanceOf(EnumDomain.VC)) {
					annotatedElementType = "Enumeration";
				} else if (annotatedElement.isInstanceOf(CollectionDomain.VC)
						|| annotatedElement.isInstanceOf(MapDomain.VC)) {
					annotatedElementType = "DataType";
				} else {
					annotatedElementType = "Class";
				}
				if (isComment) {
					commentBody = ((Comment) entry.getKey()).get_text();
				} else {
					Constraint constraint = (Constraint) entry.getKey();
					commentBody = "{\"" + constraint.get_message() + "\" \""
							+ constraint.get_predicateQuery() + "\" \""
							+ constraint.get_offendingElementsQuery() + "\"}";
				}
			}

			writer.writeStartElement(UML_NAMESPACE_URI, "Comment");
			writer.writeAttribute("xmi.id", entry.getValue());
			writer.writeAttribute("isSpecification", "false");
			writer.writeAttribute("body", commentBody);

			writer.writeStartElement(UML_NAMESPACE_URI,
					"Comment.annotatedElement");

			writer.writeEmptyElement(UML_NAMESPACE_URI, annotatedElementType);
			writer.writeAttribute("xmi.idref", qualifiedName);

			writer.writeEndElement();

			writer.writeEndElement();
		}
	}

	private void attachStereotype(XMLStreamWriter writer, String stereotype)
			throws XMLStreamException {
		writer.writeStartElement(UML_NAMESPACE_URI, "ModelElement.stereotype");
		writer.writeEmptyElement(UML_NAMESPACE_URI, "Stereotype");
		writer.writeAttribute("href", UML_PROFILE_PREFIX + stereotype);
		writer.writeEndElement();
	}

	private String getSimpleName(String qualifiedName) {
		String simpleName = qualifiedName;
		int lastIndex = qualifiedName.lastIndexOf('.');
		if (lastIndex >= 0) {
			simpleName = simpleName.substring(lastIndex + 1);
		}
		return simpleName;
	}
}
