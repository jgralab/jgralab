package de.uni_koblenz.jgralab.utilities.xml;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.grumlschema.domains.BooleanDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.Domain;
import de.uni_koblenz.jgralab.grumlschema.domains.IntegerDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.LongDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.StringDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.Annotates;
import de.uni_koblenz.jgralab.grumlschema.structure.Attribute;
import de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.Comment;
import de.uni_koblenz.jgralab.grumlschema.structure.Constraint;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.HasAttribute;
import de.uni_koblenz.jgralab.grumlschema.structure.HasConstraint;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.Schema2SchemaGraph;

public class TgSchema2XMI {

	// namespaces
	private final String XMI_NAMESPACE = "http://schema.omg.org/spec/XMI/2.1";
	private final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
	private final String EECORE_NAMESPACE = "http://www.eclipse.org/uml2/schemas/Ecore/5";
	private final String ECORE_NAMESPACE = "http://www.eclipse.org/emf/2002/Ecore";
	private final String UML_NAMESPACE = "http://schema.omg.org/spec/UML/2.1.1";

	// commen attributes
	private final String SCHEMALOCATION = "http://www.eclipse.org/uml2/schemas/Ecore/5 pathmap://UML_PROFILES/Ecore.profile.uml#_z1OFcHjqEdy8S4Cr8Rc_NA http://schema.omg.org/spec/UML/2.1.1 http://www.eclipse.org/uml2/2.1.0/UML";

	private final ArrayList<Domain> typesToBeDeclaredAtTheEnd = new ArrayList<Domain>();

	/**
	 * @param args
	 * @throws GraphIOException
	 */
	public static void main(String[] args) throws GraphIOException {

		// System.out.println("SchemaGraph to XMI");
		// System.out.println("==================");

		// new TgSchema2XMI("D:/test.xmi", "D:/graphen/BeispielGraph.tg");

		// // Retrieving all command line options
		// CommandLine cli = processCommandLineOptions(args);
		//
		// assert cli != null : "No CommandLine object has been generated!";
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
		String toolString = "java " + TgSchema2XMI.class.getName();
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

		return oh.parse(args);
	}

	public TgSchema2XMI(String xmiName, String schemaName)
			throws GraphIOException {
		new TgSchema2XMI(xmiName, GraphIO.loadSchemaFromFile(schemaName));
	}

	public TgSchema2XMI(String xmiName, Schema schema) {
		new TgSchema2XMI(xmiName, new Schema2SchemaGraph()
				.convert2SchemaGraph(schema));
	}

	public TgSchema2XMI(String xmiName, SchemaGraph schemaGraph) {
		try {
			createXMI(xmiName, schemaGraph);
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void createXMI(String xmiName, SchemaGraph schemaGraph)
			throws XMLStreamException, IOException {
		// create the XMLStreamWriter which creates the current xmi-file.
		OutputStream out = /* System.out; */new FileOutputStream(xmiName);
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		factory.setProperty("javax.xml.stream.isRepairingNamespaces",
				Boolean.TRUE);
		XMLStreamWriter writer = factory.createXMLStreamWriter(out);

		// write the first line
		writer.writeStartDocument("UTF-8", "1.0");
		createRootElement(writer, schemaGraph);
		// write the end of the document
		writer.writeEndDocument();

		// close the XMLStreamWriter
		writer.flush();
		writer.close();
		out.flush();
		out.close();
	}

	/**
	 * 
	 * @param writer
	 * @param schemaGraph
	 * @throws XMLStreamException
	 */
	private void createRootElement(XMLStreamWriter writer,
			SchemaGraph schemaGraph) throws XMLStreamException {
		// start root element
		writer.writeStartElement("xmi", "XMI", XMI_NAMESPACE);
		writer.writeAttribute(XMI_NAMESPACE, "version", "2.1");
		writer.setPrefix("xsi", XSI_NAMESPACE);
		writer.setPrefix("Ecore", EECORE_NAMESPACE);
		writer.setPrefix("ecore", ECORE_NAMESPACE);
		writer.setPrefix("uml", UML_NAMESPACE);
		writer.setPrefix("ecore", ECORE_NAMESPACE);
		writer.writeAttribute(XSI_NAMESPACE, "schemaLocation", SCHEMALOCATION);

		// create model element
		createModelElement(writer, schemaGraph);

		// close root element
		writer.writeEndElement();
	}

	/**
	 * @param writer
	 * @param modelName
	 * @param schemaGraph
	 * @throws XMLStreamException
	 */
	private void createModelElement(XMLStreamWriter writer,
			SchemaGraph schemaGraph) throws XMLStreamException {
		de.uni_koblenz.jgralab.grumlschema.structure.Schema schema = schemaGraph
				.getFirstSchema();

		// start model
		writer.writeStartElement(UML_NAMESPACE, "Model");
		writer.writeAttribute(XMI_NAMESPACE, "id", schema.get_packagePrefix()
				+ "." + schema.get_name());
		writer.writeAttribute("name", schema.get_packagePrefix() + "."
				+ schema.get_name());

		// convert graph class
		createClass(writer, schemaGraph.getFirstGraphClass());

		// create Types
		createTypes(writer);

		// create profileApplication
		// createProfileApplication(writer);

		// end model
		writer.writeEndElement();
	}

	@SuppressWarnings("unused")
	private void createProfileApplication(XMLStreamWriter writer)
			throws XMLStreamException {
		// start profileApplication
		writer.writeStartElement("profileApplication");
		writer.writeAttribute(XMI_NAMESPACE, "type", "uml:ProfileApplication");
		writer.writeAttribute(XMI_NAMESPACE, "id", "profileApplication"
				+ System.currentTimeMillis());

		// write content
		createExtension(writer, null);

		// end profileApplication
		writer.writeEndElement();
	}

	private void createTypes(XMLStreamWriter writer) throws XMLStreamException {
		// start packagedElement
		writer.writeStartElement("packagedElement");
		writer.writeAttribute(XMI_NAMESPACE, "type", "uml:Package");
		writer.writeAttribute(XMI_NAMESPACE, "id", "PrimitiveTypes");
		writer.writeAttribute("name", "PrimitiveTypes");

		// create entries for domains, which are not defined
		for (Domain domain : typesToBeDeclaredAtTheEnd) {
			writer.writeEmptyElement("packagedElement");
			writer.writeAttribute(XMI_NAMESPACE, "type", "uml:PrimitiveType");
			writer.writeAttribute(XMI_NAMESPACE, "id", domain
					.get_qualifiedName());
			writer.writeAttribute("name", domain.get_qualifiedName());
		}

		// end packagedElement
		writer.writeEndElement();
	}

	/**
	 * @param writer
	 * @param aeclass
	 * @throws XMLStreamException
	 */
	private void createClass(XMLStreamWriter writer,
			AttributedElementClass aeclass) throws XMLStreamException {
		// start packagedElement
		writer.writeStartElement("packagedElement");
		writer.writeAttribute(XMI_NAMESPACE, "type", "uml:Class");
		writer.writeAttribute(XMI_NAMESPACE, "id", aeclass.get_qualifiedName());
		writer.writeAttribute("name", aeclass.get_qualifiedName());

		// create <<graphclass>> for graph classes
		if (aeclass instanceof GraphClass) {
			createExtension(writer, aeclass);
		}

		// create comments
		int uniqueNumber = 0;
		for (Annotates annotates : aeclass.getAnnotatesIncidences()) {
			createComment(writer, (Comment) annotates.getThat(), aeclass
					.get_qualifiedName()
					+ "_Comment" + uniqueNumber++, aeclass.get_qualifiedName());
		}

		// create constraints
		uniqueNumber = 0;
		for (HasConstraint hasConstraint : aeclass.getHasConstraintIncidences()) {
			createConstraint(writer, (Constraint) hasConstraint.getThat(),
					aeclass.get_qualifiedName() + "_Constraint"
							+ uniqueNumber++, aeclass.get_qualifiedName());
		}

		// create attributes
		uniqueNumber = 0;
		for (HasAttribute hasAttribute : aeclass.getHasAttributeIncidences()) {
			createAttribute(writer, (Attribute) hasAttribute.getThat(), aeclass
					.get_qualifiedName()
					+ "_" + ((Attribute) hasAttribute.getThat()).get_name());
		}

		// close packagedElement
		writer.writeEndElement();
	}

	/**
	 * @param writer
	 * @param aeclass
	 *            if null references is created otherwise stereotype graphclass
	 * @throws XMLStreamException
	 */
	private void createExtension(XMLStreamWriter writer,
			AttributedElementClass aeclass) throws XMLStreamException {
		// start Extension
		writer.writeStartElement(XMI_NAMESPACE, "Extension");
		writer.writeAttribute("extender", ECORE_NAMESPACE);

		// start eAnnotations
		writer.writeStartElement("eAnnotations");
		writer.writeAttribute(XMI_NAMESPACE, "type", "ecore:EAnnotation");
		writer.writeAttribute(XMI_NAMESPACE, "id", aeclass != null ? aeclass
				.get_qualifiedName()
				+ "_EAnnotation" : "EAnnotation" + System.currentTimeMillis());
		writer
				.writeAttribute("source",
						"http://www.eclipse.org/uml2/2.0.0/UML");

		if (aeclass != null) {
			// write details
			writer.writeEmptyElement("details");
			writer.writeAttribute(XMI_NAMESPACE, "type",
					"ecore:EStringToStringMapEntry");
			writer.writeAttribute(XMI_NAMESPACE, "id", aeclass
					.get_qualifiedName()
					+ "_details");
			writer.writeAttribute("key", "graphclass");
		} else {
			// write references
			writer.writeEmptyElement("references");
			writer.writeAttribute(XMI_NAMESPACE, "type", "ecore:EPackage");
			writer
					.writeAttribute(
							"href",
							"http://schema.omg.org/spec/UML/2.1.1/StandardProfileL2.xmi#_yzU58YinEdqtvbnfB2L_5w");
		}

		// close eAnnotations
		writer.writeEndElement();

		// close Extension
		writer.writeEndElement();
	}

	private void createAttribute(XMLStreamWriter writer, Attribute attribute,
			String id) throws XMLStreamException {
		Domain domain = (Domain) attribute.getFirstHasDomain().getThat();

		// start ownedAttribute
		writer.writeStartElement("ownedAttribute");
		writer.writeAttribute(XMI_NAMESPACE, "type", "uml:Property");
		writer.writeAttribute(XMI_NAMESPACE, "id", id);
		writer.writeAttribute("name", attribute.get_name());
		writer.writeAttribute("visibility", "private");

		// create type
		if (domain instanceof BooleanDomain || domain instanceof IntegerDomain
				|| domain instanceof StringDomain) {
			createType(writer, domain);
		}

		// create default value
		if (attribute.get_defaultValue() != null
				&& !attribute.get_defaultValue().isEmpty()) {
			createDefaultValue(writer, attribute, id, domain);
		}

		// end ownedAttribute
		writer.writeEndElement();
	}

	/**
	 * @param writer
	 * @param attribute
	 * @param id
	 * @param domain
	 * @throws XMLStreamException
	 */
	private void createDefaultValue(XMLStreamWriter writer,
			Attribute attribute, String id, Domain domain)
			throws XMLStreamException {
		// start defaultValue
		if (domain instanceof LongDomain) {
			writer.writeEmptyElement("defaultValue");
		} else {
			writer.writeStartElement("defaultValue");
		}
		if (domain instanceof BooleanDomain) {
			writer.writeAttribute(XMI_NAMESPACE, "type", "uml:LiteralBoolean");
		} else if (domain instanceof IntegerDomain
				|| domain instanceof LongDomain) {
			writer.writeAttribute(XMI_NAMESPACE, "type", "uml:LiteralInteger");
		} else {
			writer
					.writeAttribute(XMI_NAMESPACE, "type",
							"uml:OpaqueExpression");
		}
		writer.writeAttribute(XMI_NAMESPACE, "id", id + "_defaultValue");
		if (domain instanceof BooleanDomain || domain instanceof IntegerDomain
				|| domain instanceof LongDomain) {
			writer.writeAttribute("value", attribute.get_defaultValue());

			// create type
			if (domain instanceof BooleanDomain
					|| domain instanceof IntegerDomain) {
				createType(writer, domain);
			} else {
				// there must be created an entry for the long domain in
				// the package primitiveTypes
				typesToBeDeclaredAtTheEnd.add(domain);
			}
		} else {
			if (domain instanceof StringDomain) {
				// create type
				createType(writer, domain);
			} else {
				// there must be created an entry for the current domain in
				// the package primitiveTypes
				typesToBeDeclaredAtTheEnd.add(domain);
				writer.writeAttribute("type", domain.get_qualifiedName());
			}
			// TODO current Element

			// start body
			writer.writeStartElement("body");
			writer.writeCharacters(attribute.get_defaultValue());
			// end body
			writer.writeEndElement();
		}

		if (!(domain instanceof LongDomain)) {
			// end defaultValue
			writer.writeEndElement();
		}
	}

	/**
	 * @param writer
	 * @param domain
	 * @throws XMLStreamException
	 */
	private void createType(XMLStreamWriter writer, Domain domain)
			throws XMLStreamException {
		// create type
		writer.writeEmptyElement("type");
		if (domain instanceof BooleanDomain) {
			writer.writeAttribute(XMI_NAMESPACE, "type", "uml:PrimitiveType");
			writer.writeAttribute("href",
					"http://schema.omg.org/spec/UML/2.1.1/uml.xml#Boolean");
		} else if (domain instanceof IntegerDomain) {
			writer.writeAttribute(XMI_NAMESPACE, "type", "uml:PrimitiveType");
			writer.writeAttribute("href",
					"http://schema.omg.org/spec/UML/2.1.1/uml.xml#Integer");
		} else if (domain instanceof StringDomain) {
			writer.writeAttribute(XMI_NAMESPACE, "type", "uml:PrimitiveType");
			writer.writeAttribute("href",
					"http://schema.omg.org/spec/UML/2.1.1/uml.xml#String");
		}
	}

	private void createConstraint(XMLStreamWriter writer,
			Constraint constraint, String id, String constrainedElement)
			throws XMLStreamException {
		// start ownedRule
		writer.writeStartElement("ownedRule");
		writer.writeAttribute(XMI_NAMESPACE, "type", "uml:Constraint");
		writer.writeAttribute(XMI_NAMESPACE, "id", id);
		writer.writeAttribute("constainedElement", constrainedElement);

		// start specification
		writer.writeStartElement("specification");
		writer.writeAttribute(XMI_NAMESPACE, "type", "uml:OpaqueExpression");
		writer.writeAttribute(XMI_NAMESPACE, "id", id + "_specification");

		// start and end language
		writer.writeStartElement("language");
		writer.writeEndElement();

		// start body
		writer.writeStartElement("body");
		writer.writeCharacters(constraint.get_message() + " "
				+ constraint.get_predicateQuery() + " "
				+ constraint.get_offendingElementsQuery());
		// TODO check if quoted and seperation is correct

		// end body
		writer.writeEndDocument();

		// end specification
		writer.writeEndElement();

		// end ownedRule
		writer.writeEndElement();
	}

	/**
	 * @param writer
	 * @param comment
	 * @param id
	 * @param annotatedElement
	 * @throws XMLStreamException
	 */
	private void createComment(XMLStreamWriter writer, Comment comment,
			String id, String annotatedElement) throws XMLStreamException {
		// start ownedComment
		writer.writeStartElement("ownedComment");
		writer.writeAttribute(XMI_NAMESPACE, "type", "uml:Comment");
		writer.writeAttribute(XMI_NAMESPACE, "id", id);
		writer.writeAttribute("annotatedElement", annotatedElement);

		// start body
		writer.writeStartElement("body");

		// write content
		writer.writeCharacters("<p>\r\n\t"
				+ comment.get_text().replaceAll(Pattern.quote("\n"),
						"\r\n&</p>\r\n<p>\r\n\t") + "\r\n</p>");

		// end body
		writer.writeEndElement();

		// end ownedComment
		writer.writeEndElement();
	}

}
