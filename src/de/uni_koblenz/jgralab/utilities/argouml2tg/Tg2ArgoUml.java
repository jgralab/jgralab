package de.uni_koblenz.jgralab.utilities.argouml2tg;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
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
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.grumlschema.GrumlSchema;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.grumlschema.domains.Domain;
import de.uni_koblenz.jgralab.grumlschema.structure.Annotates;
import de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass;
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

	private static final String UML_NAMESPACE_URI = "org.omg.xmi.namespace.UML";
	/**
	 * If set to <code>true</code>, the EdgeClasses are created as associations
	 * which are bidirectional navigable.
	 */
	private boolean isBidirectional = false;

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
					new FileOutputStream(xmiName), "UTF-8"));
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
			try {
				writer.close();
			} finally {
				if (out != null) {
					out.close();
				}
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

		createGraphClass(writer, schema.get_graphclass());

		createPackage(writer, schema.get_defaultpackage());

		writer.writeEndElement();

		writer.writeEndElement();
	}

	private void createGraphClass(XMLStreamWriter writer, GraphClass graphClass)
			throws XMLStreamException {
		writer.writeStartElement(UML_NAMESPACE_URI, "Class");
		writer.writeAttribute("xmi.id", graphClass.get_qualifiedName());
		writer.writeAttribute("name", graphClass.get_qualifiedName());
		writer.writeAttribute("visibility", "public");
		writer.writeAttribute("isSpecification", "false");
		writer.writeAttribute("isRoot", "false");
		writer.writeAttribute("isLeaf", "false");
		writer.writeAttribute("isAbstract", "false");
		writer.writeAttribute("isActive", "false");

		attachStereotype(writer, ArgoUml2Tg.ST_GRAPHCLASS);

		createAttributes(writer, graphClass);

		createComments(writer, graphClass);

		createConstraints(writer, graphClass);

		writer.writeEndElement();
	}

	private void createPackage(XMLStreamWriter writer, Package pack)
			throws XMLStreamException {
		if (pack.get_qualifiedName() != null
				&& !pack.get_qualifiedName().isEmpty()) {
			writer.writeStartElement(UML_NAMESPACE_URI, "Package");
			writer.writeAttribute("xmi.id", pack.get_qualifiedName());
			writer.writeAttribute("name", pack.get_qualifiedName());
			writer.writeAttribute("isSpecification", "false");
			writer.writeAttribute("isRoot", "false");
			writer.writeAttribute("isLeaf", "false");
			writer.writeAttribute("isAbstract", "false");

			writer.writeStartElement(UML_NAMESPACE_URI,
					"Namespace.ownedElement");
		}

		createComments(writer, pack);

		for (Domain dom : pack.get_domains()) {
			createDomain(writer, dom);
		}

		Set<Edge> specializations = new HashSet<Edge>();

		for (GraphElementClass gec : pack.get_graphelementclasses()) {
			if (gec.isInstanceOf(VertexClass.VC)) {
				createVertexClass(writer, (VertexClass) gec, specializations);
			} else {
				createEdgeClass(writer, (EdgeClass) gec, specializations);
			}
		}

		for (Edge edge : specializations) {
			GraphElementClass subClass = (GraphElementClass) edge.getAlpha();
			GraphElementClass superClass = (GraphElementClass) edge.getOmega();
			// TODO check Class/Association/AssociationClass
			String generalizationType = "Class";

			writer.writeStartElement(UML_NAMESPACE_URI, "Generalization");
			writer.writeAttribute("xmi.id", subClass.get_qualifiedName()
					+ "_specializes_" + superClass.get_qualifiedName());

			writer.writeStartElement(UML_NAMESPACE_URI, "Generalization.child");
			writer.writeEmptyElement(UML_NAMESPACE_URI, generalizationType);
			writer.writeAttribute("xmi.id", subClass.get_qualifiedName());
			writer.writeEndElement();

			writer.writeStartElement(UML_NAMESPACE_URI, "Generalization.parent");
			writer.writeEmptyElement(UML_NAMESPACE_URI, generalizationType);
			writer.writeAttribute("xmi.id", superClass.get_qualifiedName());
			writer.writeEndElement();

			writer.writeEndElement();
		}

		for (Package subpackage : pack.get_subpackages()) {
			createPackage(writer, subpackage);
		}

		if (pack.get_qualifiedName() != null
				&& !pack.get_qualifiedName().isEmpty()) {
			writer.writeEndElement();

			writer.writeEndElement();
		}
	}

	private void createDomain(XMLStreamWriter writer, Domain dom)
			throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	private void createVertexClass(XMLStreamWriter writer,
			VertexClass vertexClass, Set<Edge> specializations)
			throws XMLStreamException {
		if (hasVertexClassRepresentationChildren(vertexClass)) {
			writer.writeStartElement(UML_NAMESPACE_URI, "Class");
		} else {
			writer.writeEmptyElement(UML_NAMESPACE_URI, "Class");
		}
		writer.writeAttribute("xmi.id", vertexClass.get_qualifiedName());
		writer.writeAttribute("name", vertexClass.get_qualifiedName());
		writer.writeAttribute("visibility", "public");
		writer.writeAttribute("isSpecification", "false");
		writer.writeAttribute("isRoot", "false");
		writer.writeAttribute("isLeaf", "false");
		writer.writeAttribute("isAbstract", vertexClass.is_abstract() ? "true"
				: "false");
		writer.writeAttribute("isActive", "false");

		if (vertexClass.is_abstract()) {
			attachStereotype(writer, ArgoUml2Tg.ST_ABSTRACT);
		}

		createAttributes(writer, vertexClass);
		createComments(writer, vertexClass);
		createConstraints(writer, vertexClass);

		createGeneralization(writer, vertexClass, specializations);
		// TODO

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
			Set<Edge> specializations) throws XMLStreamException {
		boolean isAssociation = edgeClass.getDegree(HasAttribute.EC) == 0;

		writer.writeStartElement(UML_NAMESPACE_URI,
				isAssociation ? "Association" : "AssociationClass");
		writer.writeAttribute("xmi.id", edgeClass.get_qualifiedName());
		writer.writeAttribute("name", edgeClass.get_qualifiedName());
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

		if (edgeClass.is_abstract()) {
			attachStereotype(writer, ArgoUml2Tg.ST_ABSTRACT);
		}

		writer.writeStartElement(UML_NAMESPACE_URI, "Association.connection");
		attacheAssociationEnd(writer, edgeClass, edgeClass.get_from());
		attacheAssociationEnd(writer, edgeClass, edgeClass.get_to());
		writer.writeEndElement();

		createAttributes(writer, edgeClass);
		createComments(writer, edgeClass);
		createConstraints(writer, edgeClass);

		createGeneralization(writer, edgeClass, specializations);
		// TODO Auto-generated method stub

		writer.writeEndElement();
	}

	private void attacheAssociationEnd(XMLStreamWriter writer,
			EdgeClass edgeClass, IncidenceClass incidenceClass)
			throws XMLStreamException {
		boolean isAlpha = edgeClass.get_from() == incidenceClass;
		String aggregation = "none";
		switch ((isAlpha ? edgeClass.get_to() : edgeClass.get_from())
				.get_aggregation()) {
		case COMPOSITE:
			aggregation = "composite";
			break;
		case SHARED:
			aggregation = "aggregate";
			break;
		}

		writer.writeStartElement(UML_NAMESPACE_URI, "AssociationEnd");
		writer.writeAttribute("xmi.id", (isAlpha ? "alpha" : "omega")
				+ "_incidence_" + edgeClass.get_qualifiedName() + "_"
				+ incidenceClass.get_targetclass().get_qualifiedName());
		writer.writeAttribute("visibility", "public");
		writer.writeAttribute("isSpecification", "false");
		writer.writeAttribute("isNavigable",
				isBidirectional || !isAlpha ? "true" : "false");
		writer.writeAttribute("ordering", "unordered");
		writer.writeAttribute("aggregation", aggregation);
		writer.writeAttribute("targetScope", "instance");
		writer.writeAttribute("changeability", "changeable");

		writer.writeStartElement(UML_NAMESPACE_URI,
				"AssociationEnd.participant");
		writer.writeEmptyElement(UML_NAMESPACE_URI, "Class");
		writer.writeAttribute("xmi.idref", incidenceClass.get_targetclass()
				.get_qualifiedName());
		writer.writeEndElement();
		// TODO Auto-generated method stub

		writer.writeEndElement();
	}

	private void createGeneralization(XMLStreamWriter writer,
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
		// TODO Auto-generated method stub

	}

	private void createComments(XMLStreamWriter writer,
			NamedElement namedElement) throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	private void createConstraints(XMLStreamWriter writer,
			AttributedElementClass attrElementClass) throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	private void attachStereotype(XMLStreamWriter writer, String stereotype)
			throws XMLStreamException {
		writer.writeStartElement(UML_NAMESPACE_URI, "ModelElement.stereotype");
		writer.writeEmptyElement(UML_NAMESPACE_URI, "Stereotype");
		writer.writeAttribute("href",
				"http://argouml.org/user-profiles/gruml-1.0.1.xmi#"
						+ stereotype);
		writer.writeEndElement();
	}
}
