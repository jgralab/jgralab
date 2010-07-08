package de.uni_koblenz.jgralab.utilities.xml;

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
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
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
import de.uni_koblenz.jgralab.grumlschema.structure.AggregationKind;
import de.uni_koblenz.jgralab.grumlschema.structure.Annotates;
import de.uni_koblenz.jgralab.grumlschema.structure.Attribute;
import de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.Comment;
import de.uni_koblenz.jgralab.grumlschema.structure.Constraint;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsGraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsSubPackage;
import de.uni_koblenz.jgralab.grumlschema.structure.EdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.EndsAt;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.HasAttribute;
import de.uni_koblenz.jgralab.grumlschema.structure.HasConstraint;
import de.uni_koblenz.jgralab.grumlschema.structure.IncidenceClass;
import de.uni_koblenz.jgralab.grumlschema.structure.NamedElement;
import de.uni_koblenz.jgralab.grumlschema.structure.Package;
import de.uni_koblenz.jgralab.grumlschema.structure.Redefines;
import de.uni_koblenz.jgralab.grumlschema.structure.SpecializesEdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.SpecializesVertexClass;
import de.uni_koblenz.jgralab.grumlschema.structure.VertexClass;
import de.uni_koblenz.jgralab.utilities.rsa2tg.Rsa2Tg;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.Schema2SchemaGraph;

/**
 * SchemaGraph2XMI is a utility that converts a SchemaGraph or a TG schema file
 * into a XMI file which can be imported by IBM (tm) Rational Software Architect
 * (tm). The converter the StAX writer to create the xmi. As intermediate
 * format, a grUML schema graph is created from the TG schema file.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class SchemaGraph2XMI {

	/**
	 * This {@link ArrayList} stores all domains which have to have an entry in
	 * the package PrimitiveTypes, because they are no default attribute types
	 * of UML and have no explicitly defined UML element in the XMI. Those
	 * domains are the LongDomain, the DoubleDomain, all CollectionDomains and
	 * all MapDomains.
	 */
	private final ArrayList<Domain> typesToBeDeclaredAtTheEnd = new ArrayList<Domain>();

	/**
	 * If set to <code>true</code>, the EdgeClasses are created as associations
	 * which are bidirectional navigable.
	 */
	private boolean isBidirectional = false;

	/**
	 * If set to <code>true</code>, the EdgeClasses are created as associations
	 * which are navigable from TO to FROM.
	 */
	private boolean isReverted = false;

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

		System.out.println("SchemaGraph to XMI");
		System.out.println("==================");

		SchemaGraph2XMI s = new SchemaGraph2XMI();

		// Retrieving all command line options
		CommandLine cli = processCommandLineOptions(args);

		assert cli != null : "No CommandLine object has been generated!";

		s.isBidirectional = cli.hasOption("b");
		s.isReverted = cli.hasOption("r");

		String outputName = cli.getOptionValue("o");
		try {
			if (cli.hasOption("ig")) {
				s.process((SchemaGraph) GraphIO.loadGraphFromFile(cli
						.getOptionValue("ig"), null), outputName);
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

		Option revertedDirections = new Option("r", "revert", false,
				"(optional): If set the EdgeClasses are created as navigable from To to From.");
		revertedDirections.setRequired(false);
		oh.addOption(revertedDirections);

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
		assert !(isBidirectional && isReverted) : "The associations can't be created bidirectional navigable and navigable from FROM to TO at the same time.";
		createXMI(xmiName, schemaGraph);
	}

	/**
	 * This method creates the XMI file. Created content is:<br/>
	 * <code>&lt;?xml version="{@link XMIConstants#XML_VERSION}" encoding="{@link XMIConstants#XML_ENCODING}"?&gt;<br/>
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
		// create the XMLStreamWriter which creates the current xmi-file.
		OutputStream out = new FileOutputStream(xmiName);
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		factory.setProperty("javax.xml.stream.isRepairingNamespaces",
				Boolean.TRUE);
		XMLStreamWriter writer = factory.createXMLStreamWriter(out);

		// write the first line
		writer.writeStartDocument(XMIConstants.XML_ENCODING,
				XMIConstants.XML_VERSION);
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
	 * Creates the root <code>XMI</code> tag of the whole document. It calls
	 * {@link SchemaGraph2XMI#createModelElement(XMLStreamWriter, SchemaGraph)}
	 * to create its content.
	 * 
	 * @param writer
	 *            {@link XMLStreamWriter} of the current XMI file
	 * @param schemaGraph
	 *            {@link SchemaGraph} to be converted into an XMI
	 * @throws XMLStreamException
	 */
	private void createRootElement(XMLStreamWriter writer,
			SchemaGraph schemaGraph) throws XMLStreamException {
		// start root element
		writer.writeStartElement(XMIConstants.NAMESPACE_PREFIX_XMI,
				XMIConstants.XMI_TAG_XMI, XMIConstants.NAMESPACE_XMI);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_VERSION,
				XMIConstants.XMI_ATTRIBUTE_VERSION_VALUE);
		writer.setPrefix(XMIConstants.NAMESPACE_PREFIX_XSI,
				XMIConstants.NAMESPACE_XSI);
		writer.setPrefix(XMIConstants.NAMESPACE_PREFIX_EECORE,
				XMIConstants.NAMESPACE_EECORE);
		writer.setPrefix(XMIConstants.NAMESPACE_PREFIX_ECORE,
				XMIConstants.NAMESPACE_ECORE);
		writer.setPrefix(XMIConstants.NAMESPACE_PREFIX_UML,
				XMIConstants.NAMESPACE_UML);
		writer.writeAttribute(XMIConstants.NAMESPACE_XSI,
				XMIConstants.XSI_ATTRIBUTE_SCHEMALOCATION,
				XMIConstants.SCHEMALOCATION);

		// create model element
		createModelElement(writer, schemaGraph);

		// close root element
		writer.writeEndElement();
	}

	/**
	 * Creates the <code>Model</code> tag and its content consisting of
	 * <ul>
	 * <li>the {@link GraphClass},</li>
	 * <li>the defaultPackage and its content,</li>
	 * <li>the representation of the domains in
	 * {@link SchemaGraph2XMI#typesToBeDeclaredAtTheEnd} and</li>
	 * <li>and the profile application</li>
	 * </ul>
	 * by calling the corresponding methods.
	 * 
	 * @param writer
	 *            {@link XMLStreamWriter} of the current XMI file
	 * @param schemaGraph
	 *            {@link SchemaGraph} to be converted into an XMI
	 * @throws XMLStreamException
	 */
	private void createModelElement(XMLStreamWriter writer,
			SchemaGraph schemaGraph) throws XMLStreamException {
		de.uni_koblenz.jgralab.grumlschema.structure.Schema schema = schemaGraph
				.getFirstSchema();

		// start model
		writer.writeStartElement(XMIConstants.NAMESPACE_UML,
				XMIConstants.UML_TAG_MODEL);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_ID, schema.get_packagePrefix() + "."
						+ schema.get_name());
		writer.writeAttribute(XMIConstants.ATTRIBUTE_NAME, schema
				.get_packagePrefix()
				+ "." + schema.get_name());

		// convert graph class
		createAttributedElementClass(writer, schemaGraph.getFirstGraphClass());

		createPackage(writer, (Package) schemaGraph.getFirstSchema()
				.getFirstContainsDefaultPackage().getThat());

		// create Types
		if (!typesToBeDeclaredAtTheEnd.isEmpty()) {
			createTypes(writer);
		}

		// create profileApplication
		createProfileApplication(writer);

		// end model
		writer.writeEndElement();
	}

	private void createPackage(XMLStreamWriter writer, Package pack)
			throws XMLStreamException {
		boolean packageTagHasToBeClosed = false;

		if (!isPackageEmpty(pack)) {

			if (!pack.get_qualifiedName().equals(
					de.uni_koblenz.jgralab.schema.Package.DEFAULTPACKAGE_NAME)) {
				packageTagHasToBeClosed = pack.getFirstAnnotates() != null
						|| pack.getFirstContainsDomain() != null
						|| pack.getFirstContainsGraphElementClass() != null
						|| pack.getFirstContainsSubPackage(EdgeDirection.OUT) != null;
				if (packageTagHasToBeClosed) {
					// start package
					writer.writeStartElement(XMIConstants.TAG_PACKAGEDELEMENT);
				} else {
					// create empty package
					writer.writeEmptyElement(XMIConstants.TAG_PACKAGEDELEMENT);
				}
				writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
						XMIConstants.XMI_ATTRIBUTE_TYPE,
						XMIConstants.PACKAGEDELEMENT_TYPE_VALUE_PACKAGE);
				writer
						.writeAttribute(XMIConstants.NAMESPACE_XMI,
								XMIConstants.XMI_ATTRIBUTE_ID, pack
										.get_qualifiedName());
				writer.writeAttribute(XMIConstants.ATTRIBUTE_NAME,
						extractSimpleName(pack.get_qualifiedName()));
			}

			// create comments
			createComments(writer, pack);

			// create domains
			for (ContainsDomain cd : pack.getContainsDomainIncidences()) {
				Domain domain = (Domain) cd.getThat();
				if (!domain
						.get_qualifiedName()
						.equals(
								de.uni_koblenz.jgralab.schema.BooleanDomain.BOOLEANDOMAIN_NAME)
						&& !domain
								.get_qualifiedName()
								.equals(
										de.uni_koblenz.jgralab.schema.DoubleDomain.DOUBLEDOMAIN_NAME)
						&& !domain
								.get_qualifiedName()
								.equals(
										de.uni_koblenz.jgralab.schema.IntegerDomain.INTDOMAIN_NAME)
						&& !domain
								.get_qualifiedName()
								.equals(
										de.uni_koblenz.jgralab.schema.LongDomain.LONGDOMAIN_NAME)
						&& !domain
								.get_qualifiedName()
								.equals(
										de.uni_koblenz.jgralab.schema.StringDomain.STRINGDOMAIN_NAME)) {
					// skip basic domains
					if (domain instanceof EnumDomain) {
						createEnum(writer, (EnumDomain) domain);
					} else if (domain instanceof RecordDomain) {
						createRecordDomain(writer, (RecordDomain) domain);
					}
				}
			}

			// create GraphElementClasses
			for (ContainsGraphElementClass cgec : pack
					.getContainsGraphElementClassIncidences()) {
				GraphElementClass gec = (GraphElementClass) cgec.getThat();
				createAttributedElementClass(writer, gec);
			}

			// create subpackages
			for (ContainsSubPackage csp : pack
					.getContainsSubPackageIncidences(EdgeDirection.OUT)) {
				// create subpackages
				createPackage(writer, (Package) csp.getThat());
			}

			if (packageTagHasToBeClosed) {
				// close packagedElement
				writer.writeEndElement();
			}
		}
	}

	/**
	 * Returns true if the package <code>pack</code> does not contain a comment,
	 * a domain, a GraphElementClass and no nonempty package.
	 * 
	 * @param pack
	 * @return
	 */
	private boolean isPackageEmpty(Package pack) {
		boolean isPackageEmpty = pack.getFirstAnnotates() == null
				&& pack.getFirstContainsDomain() == null
				&& pack.getFirstContainsGraphElementClass() == null;
		if (!isPackageEmpty) {
			return false;
		} else {
			for (ContainsSubPackage csp : pack
					.getContainsSubPackageIncidences(EdgeDirection.OUT)) {
				if (!isPackageEmpty((Package) csp.getThat())) {
					return false;
				}
			}
			return true;
		}
	}

	private void createRecordDomain(XMLStreamWriter writer, RecordDomain domain)
			throws XMLStreamException {
		// start packagedElement
		writer.writeStartElement(XMIConstants.TAG_PACKAGEDELEMENT);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_TYPE,
				XMIConstants.PACKAGEDELEMENT_TYPE_VALUE_CLASS);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_ID, domain.get_qualifiedName());
		writer.writeAttribute(XMIConstants.ATTRIBUTE_NAME,
				extractSimpleName(domain.get_qualifiedName()));

		// create stereotype <<record>>
		createExtension(writer, domain, "record");

		// create comments
		createComments(writer, domain);

		// create attributes
		for (HasRecordDomainComponent hrdc : domain
				.getHasRecordDomainComponentIncidences(EdgeDirection.OUT)) {
			createAttribute(writer, hrdc.get_name(), null, (Domain) hrdc
					.getThat(), domain.get_qualifiedName() + "_"
					+ hrdc.get_name());
		}

		// end packagededElement
		writer.writeEndElement();
	}

	private void createEnum(XMLStreamWriter writer, EnumDomain domain)
			throws XMLStreamException {
		// start packagedElement
		writer.writeStartElement(XMIConstants.TAG_PACKAGEDELEMENT);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_TYPE,
				XMIConstants.PACKAGEDELEMENT_TYPE_VALUE_ENUMERATION);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_ID, domain.get_qualifiedName());
		writer.writeAttribute(XMIConstants.ATTRIBUTE_NAME,
				extractSimpleName(domain.get_qualifiedName()));

		// create comments
		createComments(writer, domain);

		// create enumeration constants
		for (String enumConst : domain.get_enumConstants()) {
			// create ownedLiteral
			writer.writeEmptyElement(XMIConstants.TAG_OWNEDLITERAL);
			writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
					XMIConstants.XMI_ATTRIBUTE_TYPE,
					XMIConstants.OWNEDLITERAL_TYPE_VALUE);
			writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
					XMIConstants.XMI_ATTRIBUTE_ID, domain.get_qualifiedName()
							+ "_" + enumConst);
			writer.writeAttribute(XMIConstants.ATTRIBUTE_NAME, enumConst);
			writer.writeAttribute(
					XMIConstants.OWNEDLITERAL_ATTRIBUTE_CLASSIFIER, domain
							.get_qualifiedName());
		}

		// end packagedElement
		writer.writeEndElement();
	}

	private void createProfileApplication(XMLStreamWriter writer)
			throws XMLStreamException {
		// start profileApplication
		writer.writeStartElement(XMIConstants.TAG_PROFILEAPPLICATION);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_TYPE,
				XMIConstants.PROFILEAPPLICATION_TYPE_VALUE);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_ID,
				XMIConstants.TAG_PROFILEAPPLICATION
						+ System.currentTimeMillis());

		// create content
		createExtension(writer, null, null);

		// create appliedProfile
		writer.writeEmptyElement(XMIConstants.TAG_APPLIEDPROFILE);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_TYPE,
				XMIConstants.APPLIEDPROFILE_TYPE_VALUE);
		writer.writeAttribute(XMIConstants.ATTRIBUTE_HREF,
				XMIConstants.APPLIEDPROFILE_HREF_VALUE);

		// end profileApplication
		writer.writeEndElement();
	}

	private void createTypes(XMLStreamWriter writer) throws XMLStreamException {
		// start packagedElement
		writer.writeStartElement(XMIConstants.TAG_PACKAGEDELEMENT);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_TYPE,
				XMIConstants.PACKAGEDELEMENT_TYPE_VALUE_PACKAGE);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_ID,
				XMIConstants.PACKAGE_PRIMITIVETYPES_NAME);
		writer.writeAttribute(XMIConstants.ATTRIBUTE_NAME,
				XMIConstants.PACKAGE_PRIMITIVETYPES_NAME);

		// create entries for domains, which are not defined
		for (Domain domain : typesToBeDeclaredAtTheEnd) {
			writer.writeEmptyElement(XMIConstants.TAG_PACKAGEDELEMENT);
			writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
					XMIConstants.XMI_ATTRIBUTE_TYPE,
					XMIConstants.TYPE_VALUE_PRIMITIVETYPE);
			writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
					XMIConstants.XMI_ATTRIBUTE_ID, domain.get_qualifiedName()
							.replaceAll("\\s", "").replaceAll("<", "_")
							.replaceAll(">", "_"));
			writer.writeAttribute(XMIConstants.ATTRIBUTE_NAME,
					extractSimpleName(domain.get_qualifiedName()));
		}

		// end packagedElement
		writer.writeEndElement();
	}

	/**
	 * @param writer
	 * @param aeclass
	 * @throws XMLStreamException
	 */
	private void createAttributedElementClass(XMLStreamWriter writer,
			AttributedElementClass aeclass) throws XMLStreamException {

		// if aeclass is a GraphElementClass without any attributes, comments
		// and constraints then an empty tag is created. Furthermore an
		// EdgeClass could only be empty if the associations are created
		// bidirectional.
		boolean isEmptyGraphElementClass = aeclass.getFirstAnnotates() == null
				&& aeclass.getFirstHasAttribute() == null
				&& aeclass.getFirstHasConstraint() == null
				&& (((aeclass instanceof VertexClass)
						&& ((VertexClass) aeclass)
								.getFirstSpecializesVertexClass(EdgeDirection.OUT) == null && !hasChildIncidence((VertexClass) aeclass)) || ((aeclass instanceof EdgeClass) && ((EdgeClass) aeclass)
						.getFirstSpecializesEdgeClass(EdgeDirection.OUT) == null)
						&& isBidirectional);

		// start packagedElement
		if (isEmptyGraphElementClass) {
			writer.writeEmptyElement(XMIConstants.TAG_PACKAGEDELEMENT);
		} else {
			writer.writeStartElement(XMIConstants.TAG_PACKAGEDELEMENT);
		}
		// set type
		if (aeclass instanceof EdgeClass) {
			if (aeclass.getFirstHasAttribute() == null) {
				writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
						XMIConstants.XMI_ATTRIBUTE_TYPE,
						XMIConstants.PACKAGEDELEMENT_TYPE_VALUE_ASSOCIATION);
			} else {
				writer
						.writeAttribute(
								XMIConstants.NAMESPACE_XMI,
								XMIConstants.XMI_ATTRIBUTE_TYPE,
								XMIConstants.PACKAGEDELEMENT_TYPE_VALUE_ASSOCIATIONCLASS);
			}
		} else {
			writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
					XMIConstants.XMI_ATTRIBUTE_TYPE,
					XMIConstants.PACKAGEDELEMENT_TYPE_VALUE_CLASS);
		}
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_ID, aeclass.get_qualifiedName());
		writer.writeAttribute(XMIConstants.ATTRIBUTE_NAME,
				extractSimpleName(aeclass.get_qualifiedName()));

		// set abstract
		if (aeclass instanceof GraphElementClass
				&& ((GraphElementClass) aeclass).is_abstract()) {
			writer.writeAttribute(
					XMIConstants.PACKAGEDELEMENT_ATTRIBUTE_ISABSTRACT,
					XMIConstants.ATTRIBUTE_VALUE_TRUE);
		}

		// set EdgeClass specific memberEnd
		if (aeclass instanceof EdgeClass) {
			EdgeClass ec = (EdgeClass) aeclass;
			writer.writeAttribute(
					XMIConstants.PACKAGEDELEMENT_ATTRIBUTE_MEMBEREND,
					((VertexClass) (((IncidenceClass) ec.getFirstComesFrom()
							.getThat()).getFirstEndsAt().getThat()))
							.get_qualifiedName()
							+ "_incidence_"
							+ ec.get_qualifiedName()
							+ " "
							+ ((VertexClass) ((IncidenceClass) ec
									.getFirstGoesTo().getThat())
									.getFirstEndsAt().getThat())
									.get_qualifiedName()
							+ "_incidence_"
							+ ec.get_qualifiedName());
		}

		// create <<graphclass>> for graph classes
		if (aeclass instanceof GraphClass) {
			createExtension(writer, aeclass, "graphclass");
		}

		// create comments
		createComments(writer, aeclass);

		// create constraints
		createConstraints(writer, aeclass);

		// create generalization
		if (aeclass instanceof VertexClass) {
			for (SpecializesVertexClass svc : ((VertexClass) aeclass)
					.getSpecializesVertexClassIncidences(EdgeDirection.OUT)) {
				createGeneralization(writer, "generalization_"
						+ aeclass.get_qualifiedName(), ((VertexClass) svc
						.getThat()).get_qualifiedName());
			}
		} else if (aeclass instanceof EdgeClass) {
			for (SpecializesEdgeClass svc : ((EdgeClass) aeclass)
					.getSpecializesEdgeClassIncidences(EdgeDirection.OUT)) {
				createGeneralization(writer, "generalization_"
						+ aeclass.get_qualifiedName(), ((EdgeClass) svc
						.getThat()).get_qualifiedName());
			}
		}

		// create attributes
		createAttributes(writer, aeclass);

		// create incidences of EdgeClasses at VertexClass aeclass
		if (aeclass instanceof VertexClass) {
			createIncidences(writer, (VertexClass) aeclass);
		} else if (aeclass instanceof EdgeClass) {
			createIncidences(writer, (EdgeClass) aeclass);
		}

		// close packagedElement
		if (!isEmptyGraphElementClass) {
			writer.writeEndElement();
		}
	}

	/**
	 * Returns true, iff there has to be created an incidence-child in the xmi
	 * for the current VertexClass.
	 * 
	 * @param aeclass
	 * @return
	 */
	private boolean hasChildIncidence(VertexClass aeclass) {
		if (aeclass.getFirstEndsAt() == null) {
			// VertexClass has no incidences
			return false;
		}
		for (EndsAt ea : aeclass.getEndsAtIncidences()) {
			IncidenceClass ic = (IncidenceClass) ea.getThat();
			if (hasToBeCreatedAtVertex(ic)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true, iff the incidence has to be created at the VertexClass, to
	 * which the IncidenceClass is connected via an EndsAt edge.
	 * 
	 * @param incidence
	 * @return
	 */
	private boolean hasToBeCreatedAtVertex(IncidenceClass incidence) {
		boolean isAlphaVertexClass = incidence.getFirstComesFrom() != null;
		if (isBidirectional) {
			// in the case of bidirectional edges, all incidences have to be
			// created at the vertices
			return true;
		} else {
			if (!isReverted && isAlphaVertexClass) {
				// the edge has a navigable incidence (aeclass-->otherEnd)
				return true;
			} else if (isReverted && !isAlphaVertexClass) {
				// the edge has a navigable incidence (aeclass-->otherEnd)
				// because the direction is reverted
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates the IncidenceClasses for AssociationClasses ( = EdgeClass with
	 * attributes)
	 * 
	 * @param writer
	 * @param edgeClass
	 * @throws XMLStreamException
	 */
	private void createIncidences(XMLStreamWriter writer, EdgeClass edgeClass)
			throws XMLStreamException {
		IncidenceClass alphaIncidence = (IncidenceClass) edgeClass
				.getFirstComesFrom().getThat();
		IncidenceClass omegaIncidence = (IncidenceClass) edgeClass
				.getFirstGoesTo().getThat();
		VertexClass alphaVertex = (VertexClass) alphaIncidence.getFirstEndsAt()
				.getThat();
		VertexClass omegaVertex = (VertexClass) omegaIncidence.getFirstEndsAt()
				.getThat();
		// create incidence which contains the information for the alpha
		// vertex (=omegaIncidence)
		if (!hasToBeCreatedAtVertex(alphaIncidence)) {
			createIncidence(writer, omegaIncidence, edgeClass, alphaIncidence,
					omegaVertex, alphaVertex.get_qualifiedName(), true);
		}
		// create incidence which contains the information for the omega
		// vertex (=alphaIncidence)
		if (!hasToBeCreatedAtVertex(omegaIncidence)) {
			createIncidence(writer, alphaIncidence, edgeClass, omegaIncidence,
					alphaVertex, omegaVertex.get_qualifiedName(), true);
		}
	}

	/**
	 * Creates the IncidenceClasses for VertexClasses
	 * 
	 * @param writer
	 * @param vertexClass
	 * @throws XMLStreamException
	 */
	private void createIncidences(XMLStreamWriter writer,
			VertexClass vertexClass) throws XMLStreamException {
		for (EndsAt ea : vertexClass.getEndsAtIncidences()) {
			// find incident EdgeClass and adjacent VertexClass
			IncidenceClass incidence = (IncidenceClass) ea.getThat();
			if (hasToBeCreatedAtVertex(incidence)) {
				boolean isVertexClassAlphaVertex = incidence
						.getFirstComesFrom() != null;
				EdgeClass edgeClass = (EdgeClass) (isVertexClassAlphaVertex ? incidence
						.getFirstComesFrom()
						: incidence.getFirstGoesTo()).getThat();
				IncidenceClass otherIncidence = (IncidenceClass) (isVertexClassAlphaVertex ? edgeClass
						.getFirstGoesTo()
						: edgeClass.getFirstComesFrom()).getThat();
				VertexClass connectedVertexClass = (VertexClass) otherIncidence
						.getFirstEndsAt().getThat();
				// create incidence representation
				if (edgeClass != null) {
					createIncidence(writer, otherIncidence, edgeClass,
							incidence, connectedVertexClass, vertexClass
									.get_qualifiedName(), false);
				}
			}
		}
	}

	/**
	 * roleName, redefines, min, max must be taken from otherIncidence
	 * 
	 * @param writer
	 * @param otherIncidence
	 *            the IncidenceClass which contains the information for the
	 *            incidence corresponding to the VertexClass of
	 *            <code>qualifiedNameOfVertexClass</code> in the xmi.
	 * @param connectedVertexClass
	 * @param incidence
	 * @param edgeClass
	 * @param qualifiedNameOfVertexClass
	 * @param createOwnedEnd
	 *            if set to true, the tag ownedEnd is used instead of
	 *            ownedAttribute (ownedEnd has to be created, if you create an
	 *            incidence at an Association.)
	 * @throws XMLStreamException
	 */
	private void createIncidence(XMLStreamWriter writer,
			IncidenceClass otherIncidence, EdgeClass edgeClass,
			IncidenceClass incidence, VertexClass connectedVertexClass,
			String qualifiedNameOfVertexClass, boolean createOwnedEnd)
			throws XMLStreamException {

		String incidenceId = qualifiedNameOfVertexClass + "_incidence_"
				+ edgeClass.get_qualifiedName();

		// redefines
		int i = 0;
		for (Redefines red : otherIncidence
				.getRedefinesIncidences(EdgeDirection.OUT)) {
			createConstraint(writer, "redefines "
					+ ((IncidenceClass) red.getThat()).get_roleName(),
					qualifiedNameOfVertexClass + "_redefines" + i + "_"
							+ edgeClass.get_qualifiedName(), incidenceId);
		}

		// start ownedattribute
		writer.writeStartElement(createOwnedEnd ? XMIConstants.TAG_OWNEDEND
				: XMIConstants.TAG_OWNEDATTRIBUTE);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_TYPE,
				XMIConstants.OWNEDATTRIBUTE_TYPE_VALUE);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_ID, incidenceId);
		// set rolenames
		if (otherIncidence.get_roleName() != null
				&& !otherIncidence.get_roleName().isEmpty()) {
			writer.writeAttribute(XMIConstants.ATTRIBUTE_NAME, otherIncidence
					.get_roleName());
		} else if (edgeClass.getFirstHasAttribute() == null) {
			writer.writeAttribute(XMIConstants.ATTRIBUTE_NAME,
					qualifiedNameOfVertexClass);
		} else {
			writer.writeAttribute(XMIConstants.ATTRIBUTE_NAME, "");
		}
		writer.writeAttribute(XMIConstants.OWNEDATTRIBUTE_ATTRIBUTE_VISIBILITY,
				XMIConstants.OWNEDATTRIBUTE_VISIBILITY_VALUE_PRIVATE);
		writer.writeAttribute(XMIConstants.PACKAGEDELEMENT_ATTRIBUTE_TYPE,
				connectedVertexClass.get_qualifiedName());
		// set composite or shared
		if (otherIncidence.get_aggregation() == AggregationKind.SHARED) {
			writer
					.writeAttribute(
							XMIConstants.OWNEDATTRIBUTE_ATTRIBUTE_AGGREGATION,
							XMIConstants.OWNEDATTRIBUTE_ATTRIBUTE_AGGREGATION_VALUE_SHARED);
		} else if (otherIncidence.get_aggregation() == AggregationKind.COMPOSITE) {
			writer
					.writeAttribute(
							XMIConstants.OWNEDATTRIBUTE_ATTRIBUTE_AGGREGATION,
							XMIConstants.OWNEDATTRIBUTE_ATTRIBUTE_AGGREGATION_VALUE_COMPOSITE);
		}
		writer.writeAttribute(
				XMIConstants.PACKAGEDELEMENT_ATTRIBUTE_ASSOCIATION, edgeClass
						.get_qualifiedName());

		// create upperValue
		writer.writeEmptyElement(XMIConstants.TAG_UPPERVALUE);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_TYPE,
				XMIConstants.TYPE_VALUE_LITERALUNLIMITEDNATURAL);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_ID, incidenceId + "_uppervalue");
		writer.writeAttribute(XMIConstants.ATTRIBUTE_VALUE, otherIncidence
				.get_max() == Integer.MAX_VALUE ? "*" : Integer
				.toString(otherIncidence.get_max()));

		// create lowerValue
		writer.writeEmptyElement(XMIConstants.TAG_LOWERVALUE);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_TYPE,
				XMIConstants.TYPE_VALUE_LITERALINTEGER);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_ID, incidenceId + "_lowervalue");
		writer.writeAttribute(XMIConstants.ATTRIBUTE_VALUE,
				incidence.get_min() == Integer.MAX_VALUE ? "*" : Integer
						.toString(otherIncidence.get_min()));

		// close ownedattribute
		writer.writeEndElement();
	}

	private void createGeneralization(XMLStreamWriter writer, String id,
			String idOfSpecializedClass) throws XMLStreamException {
		// create generalization
		writer.writeEmptyElement(XMIConstants.TAG_GENERALIZATION);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_TYPE,
				XMIConstants.GENERALIZATION_TYPE_VALUE);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_ID, id);
		writer.writeAttribute(XMIConstants.GENERALIZATION_ATTRIBUTE_GENERAL,
				idOfSpecializedClass);
	}

	/**
	 * @param writer
	 * @param nelement
	 *            if null references is created otherwise stereotype graphclass
	 * @param string
	 * @throws XMLStreamException
	 */
	private void createExtension(XMLStreamWriter writer, NamedElement nelement,
			String keyValue) throws XMLStreamException {
		// start Extension
		writer.writeStartElement(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_TAG_EXTENSION);
		writer.writeAttribute(XMIConstants.ATTRIBUTE_EXTENDER,
				XMIConstants.NAMESPACE_ECORE);

		// start eAnnotations
		writer.writeStartElement(XMIConstants.TAG_EANNOTATIONS);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_TYPE,
				XMIConstants.EANNOTATIONS_TYPE_VALUE);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_ID, nelement != null ? nelement
						.get_qualifiedName()
						+ "_" + XMIConstants.TAG_EANNOTATIONS
						: XMIConstants.TAG_EANNOTATIONS
								+ System.currentTimeMillis());
		writer.writeAttribute(XMIConstants.EANNOTATIONS_ATTRIBUTE_SOURCE,
				XMIConstants.EANNOTATIONS_ATTRIBUTE_SOURCE_VALUE);

		if (nelement != null) {
			// write details
			writer.writeEmptyElement(XMIConstants.TAG_DETAILS);
			writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
					XMIConstants.XMI_ATTRIBUTE_TYPE,
					XMIConstants.DETAILS_ATTRIBUTE_TYPE_VALUE);
			writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
					XMIConstants.XMI_ATTRIBUTE_ID, nelement.get_qualifiedName()
							+ "_" + XMIConstants.TAG_DETAILS);
			writer.writeAttribute(XMIConstants.DETAILS_ATTRIBUTE_KEY, keyValue);
		} else {
			// write references
			writer.writeEmptyElement(XMIConstants.TAG_REFERENCES);
			writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
					XMIConstants.XMI_ATTRIBUTE_TYPE,
					XMIConstants.REFERENCES_TYPE_VALUE);
			writer.writeAttribute(XMIConstants.ATTRIBUTE_HREF,
					XMIConstants.REFERENCES_HREF_VALUE);
		}

		// close eAnnotations
		writer.writeEndElement();

		// close Extension
		writer.writeEndElement();
	}

	/**
	 * @param writer
	 * @param aeclass
	 * @throws XMLStreamException
	 */
	private void createAttributes(XMLStreamWriter writer,
			AttributedElementClass aeclass) throws XMLStreamException {
		for (HasAttribute hasAttribute : aeclass.getHasAttributeIncidences()) {
			Attribute attribute = (Attribute) hasAttribute.getThat();
			createAttribute(writer, attribute.get_name(), attribute
					.get_defaultValue(), (Domain) attribute.getFirstHasDomain()
					.getThat(), aeclass.get_qualifiedName() + "_"
					+ ((Attribute) hasAttribute.getThat()).get_name());
		}
	}

	private void createAttribute(XMLStreamWriter writer, String attributeName,
			String defaultValue, Domain domain, String id)
			throws XMLStreamException {

		boolean hasDefaultValue = defaultValue != null
				&& !defaultValue.isEmpty();

		// start ownedAttribute
		if (!hasDefaultValue
				&& !(domain instanceof BooleanDomain
						|| domain instanceof IntegerDomain || domain instanceof StringDomain)) {
			writer.writeEmptyElement(XMIConstants.TAG_OWNEDATTRIBUTE);
		} else {
			writer.writeStartElement(XMIConstants.TAG_OWNEDATTRIBUTE);
		}
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_TYPE,
				XMIConstants.OWNEDATTRIBUTE_TYPE_VALUE);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_ID, id);
		writer.writeAttribute(XMIConstants.ATTRIBUTE_NAME, attributeName);
		writer.writeAttribute(XMIConstants.OWNEDATTRIBUTE_ATTRIBUTE_VISIBILITY,
				XMIConstants.OWNEDATTRIBUTE_VISIBILITY_VALUE_PRIVATE);

		// create type
		if (domain instanceof BooleanDomain || domain instanceof IntegerDomain
				|| domain instanceof StringDomain) {
			createType(writer, domain);
		} else {
			writer.writeAttribute(XMIConstants.XMI_ATTRIBUTE_TYPE, domain
					.get_qualifiedName().replaceAll("\\s", "").replaceAll("<",
							"_").replaceAll(">", "_"));
		}

		// create default value
		if (hasDefaultValue) {
			createDefaultValue(writer, defaultValue, id, domain);
		}

		if (hasDefaultValue || domain instanceof BooleanDomain
				|| domain instanceof IntegerDomain
				|| domain instanceof StringDomain) {
			// end ownedAttribute
			writer.writeEndElement();
		}

		// if domain is a LongDomain, a DoubleDomain, a CollectionDomain or a
		// MapDomain there has to be created an entry in the package
		// PrimitiveTypes
		if (domain instanceof LongDomain || domain instanceof DoubleDomain
				|| domain instanceof CollectionDomain
				|| domain instanceof MapDomain) {
			typesToBeDeclaredAtTheEnd.add(domain);
		}
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
			String defaultValue, String id, Domain domain)
			throws XMLStreamException {
		// start defaultValue
		if (domain instanceof LongDomain || domain instanceof EnumDomain) {
			writer.writeEmptyElement(XMIConstants.TAG_DEFAULTVALUE);
		} else {
			writer.writeStartElement(XMIConstants.TAG_DEFAULTVALUE);
		}
		if (domain instanceof BooleanDomain) {
			writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
					XMIConstants.XMI_ATTRIBUTE_TYPE,
					XMIConstants.TYPE_VALUE_LITERALBOOLEAN);
		} else if (domain instanceof IntegerDomain
				|| domain instanceof LongDomain) {
			writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
					XMIConstants.XMI_ATTRIBUTE_TYPE,
					XMIConstants.TYPE_VALUE_LITERALINTEGER);
		} else if (domain instanceof EnumDomain) {
			writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
					XMIConstants.XMI_ATTRIBUTE_TYPE,
					XMIConstants.TYPE_VALUE_INSTANCEVALUE);
		} else {
			writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
					XMIConstants.XMI_ATTRIBUTE_TYPE,
					XMIConstants.TYPE_VALUE_OPAQUEEXPRESSION);
		}
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_ID, id + "_defaultValue");
		if (domain instanceof BooleanDomain || domain instanceof IntegerDomain
				|| domain instanceof LongDomain || domain instanceof EnumDomain) {
			if (domain instanceof BooleanDomain) {
				writer.writeAttribute(XMIConstants.ATTRIBUTE_VALUE,
						defaultValue.equals("t") ? "true" : "false");
			} else if (domain instanceof EnumDomain) {
				writer
						.writeAttribute(XMIConstants.ATTRIBUTE_NAME,
								defaultValue);
				writer.writeAttribute(XMIConstants.XMI_ATTRIBUTE_TYPE, domain
						.get_qualifiedName());
				writer.writeAttribute(
						XMIConstants.DEFAULTVALUE_ATTRIBUTE_INSTANCE, domain
								.get_qualifiedName()
								+ "_" + defaultValue);
			} else {
				writer.writeAttribute(XMIConstants.ATTRIBUTE_VALUE,
						defaultValue);
			}

			// create type
			if (domain instanceof BooleanDomain
					|| domain instanceof IntegerDomain) {
				createType(writer, domain);
			}
		} else {
			if (domain instanceof StringDomain) {
				// create type
				createType(writer, domain);
			} else {
				// there must be created an entry for the current domain in
				// the package primitiveTypes
				writer.writeAttribute(XMIConstants.XMI_ATTRIBUTE_TYPE, domain
						.get_qualifiedName().replaceAll("\\s", "").replaceAll(
								"<", "_").replaceAll(">", "_"));
			}

			// start body
			writer.writeStartElement(XMIConstants.TAG_BODY);
			writer.writeCharacters(defaultValue);
			// end body
			writer.writeEndElement();
		}

		if (!(domain instanceof LongDomain || domain instanceof EnumDomain)) {
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
					XMIConstants.XMI_ATTRIBUTE_TYPE,
					XMIConstants.TYPE_VALUE_PRIMITIVETYPE);
			writer.writeAttribute(XMIConstants.ATTRIBUTE_HREF,
					XMIConstants.TYPE_HREF_VALUE_BOOLEAN);
		} else if (domain instanceof IntegerDomain) {
			writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
					XMIConstants.XMI_ATTRIBUTE_TYPE,
					XMIConstants.TYPE_VALUE_PRIMITIVETYPE);
			writer.writeAttribute(XMIConstants.ATTRIBUTE_HREF,
					XMIConstants.TYPE_HREF_VALUE_INTEGER);
		} else if (domain instanceof StringDomain) {
			writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
					XMIConstants.XMI_ATTRIBUTE_TYPE,
					XMIConstants.TYPE_VALUE_PRIMITIVETYPE);
			writer.writeAttribute(XMIConstants.ATTRIBUTE_HREF,
					XMIConstants.TYPE_HREF_VALUE_STRING);
		}
	}

	/**
	 * @param writer
	 * @param aeclass
	 * @throws XMLStreamException
	 */
	private void createConstraints(XMLStreamWriter writer,
			AttributedElementClass aeclass) throws XMLStreamException {
		int uniqueNumber = 0;
		for (HasConstraint hasConstraint : aeclass.getHasConstraintIncidences()) {
			createConstraint(writer, (Constraint) hasConstraint.getThat(),
					aeclass.get_qualifiedName() + "_"
							+ XMIConstants.TAG_OWNEDRULE + uniqueNumber++,
					aeclass.get_qualifiedName());
		}
	}

	private void createConstraint(XMLStreamWriter writer,
			Constraint constraint, String id, String constrainedElement)
			throws XMLStreamException {
		createConstraint(writer, "\"" + constraint.get_message() + "\" \""
				+ constraint.get_predicateQuery() + "\" \""
				+ constraint.get_offendingElementsQuery() + "\"", id,
				constrainedElement);
	}

	private void createConstraint(XMLStreamWriter writer,
			String constraintContent, String id, String constrainedElement)
			throws XMLStreamException {
		// start ownedRule
		writer.writeStartElement(XMIConstants.TAG_OWNEDRULE);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_TYPE,
				XMIConstants.OWNEDRULE_TYPE_VALUE);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_ID, id);
		writer.writeAttribute(
				XMIConstants.OWNEDRULE_ATTRIBUTE_CONSTRAINEDELEMENT,
				constrainedElement);

		// start specification
		writer.writeStartElement(XMIConstants.TAG_SPECIFICATION);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_TYPE,
				XMIConstants.TYPE_VALUE_OPAQUEEXPRESSION);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_ID, id + "_"
						+ XMIConstants.TAG_SPECIFICATION);

		// start and end language
		writer.writeStartElement(XMIConstants.TAG_LANGUAGE);
		writer.writeEndElement();

		// start body
		writer.writeStartElement(XMIConstants.TAG_BODY);
		writer.writeCharacters(constraintContent);

		// end body
		writer.writeEndElement();

		// end specification
		writer.writeEndElement();

		// end ownedRule
		writer.writeEndElement();
	}

	/**
	 * @param writer
	 * @param nelement
	 * @param uniqueNumber
	 * @return
	 * @throws XMLStreamException
	 */
	private void createComments(XMLStreamWriter writer, NamedElement nelement)
			throws XMLStreamException {
		int uniqueNumber = 0;
		for (Annotates annotates : nelement.getAnnotatesIncidences()) {
			createComment(writer, (Comment) annotates.getThat(), nelement
					.get_qualifiedName()
					+ "_" + XMIConstants.TAG_OWNEDCOMMENT + uniqueNumber++,
					nelement.get_qualifiedName());
		}
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
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_TYPE,
				XMIConstants.OWNEDCOMMENT_TYPE_VALUE);
		writer.writeAttribute(XMIConstants.NAMESPACE_XMI,
				XMIConstants.XMI_ATTRIBUTE_ID, id);
		writer.writeAttribute(
				XMIConstants.OWNEDCOMMENT_ATTRIBUTE_ANNOTATEDELEMENT,
				annotatedElement);

		// start body
		writer.writeStartElement(XMIConstants.TAG_BODY);

		// write content
		writer.writeCharacters(XMIConstants.COMMENT_START
				+ comment.get_text().replaceAll(Pattern.quote("\n"),
						XMIConstants.COMMENT_NEWLINE)
				+ XMIConstants.COMMENT_END);

		// end body
		writer.writeEndElement();

		// end ownedComment
		writer.writeEndElement();
	}

	private String extractSimpleName(String qualifiedName) {
		int lastIndexOfDot = qualifiedName.lastIndexOf('.');
		if (lastIndexOfDot >= 0) {
			return qualifiedName.substring(lastIndexOfDot + 1);
		} else {
			return qualifiedName;
		}
	}

}
