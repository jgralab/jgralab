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
import de.uni_koblenz.jgralab.grumlschema.domains.DoubleDomain;
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

public class SchemaGraph2XMI {

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

		return oh.parse(args);
	}

	public SchemaGraph2XMI(String schemaName, String xmiName)
			throws GraphIOException {
		new SchemaGraph2XMI(GraphIO.loadSchemaFromFile(schemaName), xmiName);
	}

	public SchemaGraph2XMI(Schema schema, String xmiName) {
		new SchemaGraph2XMI(new Schema2SchemaGraph()
				.convert2SchemaGraph(schema), xmiName);
	}

	public SchemaGraph2XMI(SchemaGraph schemaGraph, String xmiName) {
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
		writer.writeStartDocument(XMIConstants.XML_ENCODING, XMIConstants.XML_VERSION);
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
		writer.writeStartElement(XMIConstants.NAMESPACE_PREFIX_XMI,
				XMIConstants.XMI_TAG_XMI, XMIConstants.NAMESPACE_XMI);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_VERSION, XMIConstants.XMI_ATTRIBUTE_VERSION_VALUE);
		writer.setPrefix(XMIConstants.NAMESPACE_PREFIX_XSI, XMIConstants.NAMESPACE_XSI);
		writer
				.setPrefix(XMIConstants.NAMESPACE_PREFIX_EECORE,
						XMIConstants.NAMESPACE_EECORE);
		writer.setPrefix(XMIConstants.NAMESPACE_PREFIX_ECORE, XMIConstants.NAMESPACE_ECORE);
		writer.setPrefix(XMIConstants.NAMESPACE_PREFIX_UML, XMIConstants.NAMESPACE_UML);
		writer.writeAttribute(XMIConstants.NAMESPACE_XSI,
				XMIConstants.XSI_ATTRIBUTE_SCHEMALOCATION, XMIConstants.SCHEMALOCATION);

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
		writer.writeStartElement(XMIConstants.NAMESPACE_UML, XMIConstants.UML_TAG_MODEL);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI, XMIConstants.XMI_ATTRIBUTE_ID,
				schema.get_packagePrefix() + "." + schema.get_name());
		writer.writeAttribute(XMIConstants.ATTRIBUTE_NAME, schema.get_packagePrefix() + "."
				+ schema.get_name());

		// convert graph class
		createClass(writer, schemaGraph.getFirstGraphClass());

		// create Types
		createTypes(writer);

		// create profileApplication
		createProfileApplication(writer);

		// end model
		writer.writeEndElement();
	}

	private void createProfileApplication(XMLStreamWriter writer)
			throws XMLStreamException {
		// start profileApplication
		writer.writeStartElement(XMIConstants.TAG_PROFILEAPPLICATION);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI, XMIConstants.XMI_ATTRIBUTE_TYPE,
				XMIConstants.PROFILEAPPLICATION_TYPE_VALUE);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI, XMIConstants.XMI_ATTRIBUTE_ID,
				XMIConstants.TAG_PROFILEAPPLICATION + System.currentTimeMillis());

		// create content
		createExtension(writer, null);

		// create appliedProfile
		writer.writeEmptyElement(XMIConstants.TAG_APPLIEDPROFILE);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI, XMIConstants.XMI_ATTRIBUTE_TYPE,
				XMIConstants.APPLIEDPROFILE_TYPE_VALUE);
		writer.writeAttribute(XMIConstants.ATTRIBUTE_HREF, XMIConstants.APPLIEDPROFILE_HREF_VALUE);

		// end profileApplication
		writer.writeEndElement();
	}

	private void createTypes(XMLStreamWriter writer) throws XMLStreamException {
		// start packagedElement
		writer.writeStartElement(XMIConstants.TAG_PACKAGEDELEMENT);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI, XMIConstants.XMI_ATTRIBUTE_TYPE,
				XMIConstants.PACKEGEDELEMENT_TYPE_VALUE_PACKAGE);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI, XMIConstants.XMI_ATTRIBUTE_ID,
				XMIConstants.PACKAGE_PRIMITIVETYPES_NAME);
		writer.writeAttribute(XMIConstants.ATTRIBUTE_NAME, XMIConstants.PACKAGE_PRIMITIVETYPES_NAME);

		// create entries for domains, which are not defined
		for (Domain domain : typesToBeDeclaredAtTheEnd) {
			writer.writeEmptyElement(XMIConstants.TAG_PACKAGEDELEMENT);
			writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
					XMIConstants.XMI_ATTRIBUTE_TYPE, XMIConstants.TYPE_VALUE_PRIMITIVETYPE);
			writer.writeAttribute(XMIConstants.NAMESPACE_XMI, XMIConstants.XMI_ATTRIBUTE_ID,
					domain.get_qualifiedName());
			writer.writeAttribute(XMIConstants.ATTRIBUTE_NAME, domain.get_qualifiedName());
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
		writer.writeStartElement(XMIConstants.TAG_PACKAGEDELEMENT);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI, XMIConstants.XMI_ATTRIBUTE_TYPE,
				XMIConstants.PACKAGEDELEMENT_TYPE_VALUE_CLASS);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI, XMIConstants.XMI_ATTRIBUTE_ID,
				aeclass.get_qualifiedName());
		writer.writeAttribute(XMIConstants.ATTRIBUTE_NAME, aeclass.get_qualifiedName());

		// create <<graphclass>> for graph classes
		if (aeclass instanceof GraphClass) {
			createExtension(writer, aeclass);
		}

		// create comments
		int uniqueNumber = 0;
		for (Annotates annotates : aeclass.getAnnotatesIncidences()) {
			createComment(writer, (Comment) annotates.getThat(), aeclass
					.get_qualifiedName()
					+ "_" + XMIConstants.TAG_OWNEDCOMMENT + uniqueNumber++, aeclass
					.get_qualifiedName());
		}

		// create constraints
		uniqueNumber = 0;
		for (HasConstraint hasConstraint : aeclass.getHasConstraintIncidences()) {
			createConstraint(writer, (Constraint) hasConstraint.getThat(),
					aeclass.get_qualifiedName() + "_" + XMIConstants.TAG_OWNEDRULE
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
		writer.writeStartElement(XMIConstants.NAMESPACE_XMI, XMIConstants.XMI_TAG_EXTENSION);
		writer.writeAttribute(XMIConstants.ATTRIBUTE_EXTENDER, XMIConstants.NAMESPACE_ECORE);

		// start eAnnotations
		writer.writeStartElement(XMIConstants.TAG_EANNOTATIONS);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI, XMIConstants.XMI_ATTRIBUTE_TYPE,
				XMIConstants.EANNOTATIONS_TYPE_VALUE);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI, XMIConstants.XMI_ATTRIBUTE_ID,
				aeclass != null ? aeclass.get_qualifiedName() + "_"
						+ XMIConstants.TAG_EANNOTATIONS : XMIConstants.TAG_EANNOTATIONS
						+ System.currentTimeMillis());
		writer.writeAttribute(XMIConstants.EANNOTATIONS_ATTRIBUTE_SOURCE,
				XMIConstants.EANNOTATIONS_ATTRIBUTE_SOURCE_VALUE);

		if (aeclass != null) {
			// write details
			writer.writeEmptyElement(XMIConstants.TAG_DETAILS);
			writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
					XMIConstants.XMI_ATTRIBUTE_TYPE, XMIConstants.DETAILS_ATTRIBUTE_TYPE_VALUE);
			writer.writeAttribute(XMIConstants.NAMESPACE_XMI, XMIConstants.XMI_ATTRIBUTE_ID,
					aeclass.get_qualifiedName() + "_" + XMIConstants.TAG_DETAILS);
			writer.writeAttribute(XMIConstants.DETAILS_ATTRIBUTE_KEY,
					XMIConstants.DETAILS_ATTRIBUTE_KEY_VALUE);
		} else {
			// write references
			writer.writeEmptyElement(XMIConstants.TAG_REFERENCES);
			writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
					XMIConstants.XMI_ATTRIBUTE_TYPE, XMIConstants.REFERENCES_TYPE_VALUE);
			writer.writeAttribute(XMIConstants.ATTRIBUTE_HREF, XMIConstants.REFERENCES_HREF_VALUE);
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
		writer.writeStartElement(XMIConstants.TAG_OWNEDATTRIBUTE);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI, XMIConstants.XMI_ATTRIBUTE_TYPE,
				XMIConstants.OWNEDATTRIBUTE_TYPE_VALUE);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI, XMIConstants.XMI_ATTRIBUTE_ID, id);
		writer.writeAttribute(XMIConstants.ATTRIBUTE_NAME, attribute.get_name());
		writer.writeAttribute(XMIConstants.OWNEDATTRIBUTE_ATTRIBUTE_VISIBILITY,
				XMIConstants.OWNEDATTRIBUTE_VISIBILITY_VALUE_PRIVATE);
		if (domain instanceof DoubleDomain || domain instanceof LongDomain) {
			writer.writeAttribute(XMIConstants.XMI_ATTRIBUTE_TYPE, domain
					.get_qualifiedName());
		}

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
	 * Creates the default values.
	 * 
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
			writer.writeEmptyElement(XMIConstants.TAG_DEFAULTVALUE);
		} else {
			writer.writeStartElement(XMIConstants.TAG_DEFAULTVALUE);
		}
		if (domain instanceof BooleanDomain) {
			writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
					XMIConstants.XMI_ATTRIBUTE_TYPE, XMIConstants.TYPE_VALUE_LITERALBOOLEAN);
		} else if (domain instanceof IntegerDomain
				|| domain instanceof LongDomain) {
			writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
					XMIConstants.XMI_ATTRIBUTE_TYPE, XMIConstants.TYPE_VALUE_LITERALINTEGER);
		} else {
			writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
					XMIConstants.XMI_ATTRIBUTE_TYPE, XMIConstants.TYPE_VALUE_OPAQUEEXPRESSION);
		}
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI, XMIConstants.XMI_ATTRIBUTE_ID, id
				+ "_defaultValue");
		if (domain instanceof BooleanDomain || domain instanceof IntegerDomain
				|| domain instanceof LongDomain) {
			if (domain instanceof BooleanDomain) {
				writer.writeAttribute(XMIConstants.DEFAULTVALUE_ATTRIBUTE_VALUE, attribute
						.get_defaultValue().equals("t") ? "true" : "false");
			} else {
				writer.writeAttribute(XMIConstants.DEFAULTVALUE_ATTRIBUTE_VALUE, attribute
						.get_defaultValue());
			}

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
				writer.writeAttribute(XMIConstants.XMI_ATTRIBUTE_TYPE, domain
						.get_qualifiedName());
			}
			// TODO current Element

			// start body
			writer.writeStartElement(XMIConstants.TAG_BODY);
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
		writer.writeEmptyElement(XMIConstants.TAG_TYPE);
		if (domain instanceof BooleanDomain) {
			writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
					XMIConstants.XMI_ATTRIBUTE_TYPE, XMIConstants.TYPE_VALUE_PRIMITIVETYPE);
			writer.writeAttribute(XMIConstants.ATTRIBUTE_HREF, XMIConstants.TYPE_HREF_VALUE_BOOLEAN);
		} else if (domain instanceof IntegerDomain) {
			writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
					XMIConstants.XMI_ATTRIBUTE_TYPE, XMIConstants.TYPE_VALUE_PRIMITIVETYPE);
			writer.writeAttribute(XMIConstants.ATTRIBUTE_HREF, XMIConstants.TYPE_HREF_VALUE_INTEGER);
		} else if (domain instanceof StringDomain) {
			writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
					XMIConstants.XMI_ATTRIBUTE_TYPE, XMIConstants.TYPE_VALUE_PRIMITIVETYPE);
			writer.writeAttribute(XMIConstants.ATTRIBUTE_HREF, XMIConstants.TYPE_HREF_VALUE_STRING);
		}
	}

	private void createConstraint(XMLStreamWriter writer,
			Constraint constraint, String id, String constrainedElement)
			throws XMLStreamException {
		// start ownedRule
		writer.writeStartElement(XMIConstants.TAG_OWNEDRULE);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI, XMIConstants.XMI_ATTRIBUTE_TYPE,
				XMIConstants.OWNEDRULE_TYPE_VALUE);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI, XMIConstants.XMI_ATTRIBUTE_ID, id);
		writer.writeAttribute(XMIConstants.OWNEDRULE_ATTRIBUTE_CONSTRAINEDELEMENT,
				constrainedElement);

		// start specification
		writer.writeStartElement(XMIConstants.TAG_SPECIFICATION);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI, XMIConstants.XMI_ATTRIBUTE_TYPE,
				XMIConstants.TYPE_VALUE_OPAQUEEXPRESSION);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI, XMIConstants.XMI_ATTRIBUTE_ID, id
				+ "_" + XMIConstants.TAG_SPECIFICATION);

		// start and end language
		writer.writeStartElement(XMIConstants.TAG_LANGUAGE);
		writer.writeEndElement();

		// start body
		writer.writeStartElement(XMIConstants.TAG_BODY);
		writer.writeCharacters("\"" + constraint.get_message() + "\" \""
				+ constraint.get_predicateQuery() + "\" \""
				+ constraint.get_offendingElementsQuery() + "\"");

		// end body
		writer.writeEndElement();

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
		writer.writeStartElement(XMIConstants.TAG_OWNEDCOMMENT);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI, XMIConstants.XMI_ATTRIBUTE_TYPE,
				XMIConstants.OWNEDCOMMENT_TYPE_VALUE);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI, XMIConstants.XMI_ATTRIBUTE_ID, id);
		writer.writeAttribute(XMIConstants.OWNEDCOMMENT_ATTRIBUTE_ANNOTATEDELEMENT,
				annotatedElement);

		// start body
		writer.writeStartElement(XMIConstants.TAG_BODY);

		// write content
		writer.writeCharacters(XMIConstants.COMMENT_START
				+ comment.get_text().replaceAll(Pattern.quote("\n"),
						XMIConstants.COMMENT_NEWLINE) + XMIConstants.COMMENT_END);

		// end body
		writer.writeEndElement();

		// end ownedComment
		writer.writeEndElement();
	}

}
