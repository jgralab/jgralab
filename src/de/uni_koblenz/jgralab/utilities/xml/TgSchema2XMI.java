package de.uni_koblenz.jgralab.utilities.xml;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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

	/**
	 * @param args
	 * @throws GraphIOException
	 */
	public static void main(String[] args) throws GraphIOException {

		// System.out.println("SchemaGraph to XMI");
		// System.out.println("==================");

		new TgSchema2XMI("/windowsD/test.xmi",
				"/windowsD/graphen/BeispielGraph.tg");

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

		writer.writeStartElement(UML_NAMESPACE, "Model");
		writer.writeAttribute(XMI_NAMESPACE, "id", schema.get_packagePrefix()
				+ "." + schema.get_name());
		writer.writeAttribute("name", schema.get_packagePrefix() + "."
				+ schema.get_name());

		// convert graph class
		GraphClass graphClass = schemaGraph.getFirstGraphClass();
		createClass(writer, graphClass);

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
		writer.writeAttribute(XMI_NAMESPACE, "name", aeclass
				.get_qualifiedName());

		// create <<GraphClass>> for graph classes
		if (aeclass instanceof GraphClass) {
			// start Extension
			writer.writeStartElement(XMI_NAMESPACE, "Extension");
			writer.writeAttribute("extender", EECORE_NAMESPACE);

			// start eAnnotations
			writer.writeStartElement("eAnnotations");
			writer.writeAttribute(XMI_NAMESPACE, "type", "ecore:EAnnotation");
			writer.writeAttribute(XMI_NAMESPACE, "id", aeclass
					.get_qualifiedName()
					+ "_EAnnotation");
			writer.writeAttribute("source",
					"http://www.eclipse.org/uml2/2.0.0/UML");

			// write details
			writer.writeEmptyElement("details");
			writer.writeAttribute(XMI_NAMESPACE, "type",
					"ecore:EStringToStringMapEntry");
			writer.writeAttribute(XMI_NAMESPACE, "id", aeclass
					.get_qualifiedName()
					+ "_details");
			writer.writeAttribute("key", aeclass.get_qualifiedName());

			// close eAnnotations
			writer.writeEndElement();

			// close Extension
			writer.writeEndElement();
		}

		// create comment
		for (Annotates annotates : aeclass.getAnnotatesIncidences()) {
			createComment((Comment) annotates.getThat());
		}

		// create constraints
		for (HasConstraint hasConstraint : aeclass.getHasConstraintIncidences()) {
			createConstraint((Constraint) hasConstraint.getThat());
		}

		// create attributes
		for (HasAttribute hasAttribute : aeclass.getHasAttributeIncidences()) {
			createAttribute((Attribute) hasAttribute.getThat());
		}

		// close packagedElement
		writer.writeEndElement();
	}

	private void createAttribute(Attribute that) {
		// TODO Auto-generated method stub

	}

	private void createConstraint(Constraint constraint) {
		// TODO Auto-generated method stub

	}

	private void createComment(Comment that) {
		// TODO Auto-generated method stub

	}

}
