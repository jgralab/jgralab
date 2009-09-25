/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 *
 *               ist@uni-koblenz.de
 *
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.uni_koblenz.jgralab.utilities.rsa2tg;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.ist.utilities.xml.XmlProcessor;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.GraphMarker;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.graphvalidator.ConstraintViolation;
import de.uni_koblenz.jgralab.graphvalidator.GraphValidator;
import de.uni_koblenz.jgralab.grumlschema.GrumlSchema;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.grumlschema.domains.CollectionDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.Domain;
import de.uni_koblenz.jgralab.grumlschema.domains.EnumDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.HasRecordDomainComponent;
import de.uni_koblenz.jgralab.grumlschema.domains.MapDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.RecordDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.StringDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.AggregationClass;
import de.uni_koblenz.jgralab.grumlschema.structure.Attribute;
import de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.CompositionClass;
import de.uni_koblenz.jgralab.grumlschema.structure.Constraint;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsGraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.EdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.From;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.HasAttribute;
import de.uni_koblenz.jgralab.grumlschema.structure.HasDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.Package;
import de.uni_koblenz.jgralab.grumlschema.structure.Schema;
import de.uni_koblenz.jgralab.grumlschema.structure.To;
import de.uni_koblenz.jgralab.grumlschema.structure.VertexClass;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;

/**
 * Rsa2Tg is a utility that converts XMI files exported from IBM (tm) Rational
 * Software Architect (tm) into a TG schema file. The converter is based on a
 * SAX parser. As intermediate format, a grUML schema graph is created from the
 * XMI elements.
 * 
 * @author ist@uni-koblenz.de
 * 
 *         TODO: Currently Rsa2Tg breaks if multiplicities are given for
 *         attributes. But it shouldn't do so if an attribute has the
 *         multiplicity (1,1).
 */
@WorkInProgress(description = "record comments,"
		+ "error checking and reporting", responsibleDevelopers = "riediger, mmce")
public class Rsa2Tg extends XmlProcessor {
	private static final String UML_ATTRIBUTE_CLASSIFIER = "classifier";

	private static final String UML_ATTRIBUTE_CLIENT = "client";

	private static final String UML_ATTRIBUTE_SUPPLIER = "supplier";

	private static final String UML_ATTRIBUTE_ASSOCIATION = "association";

	private static final String UML_UPPERVALUE = "upperValue";

	private static final String UML_LOWERVALUE = "lowerValue";

	private static final String UML_DETAILS = "details";

	private static final String UML_GENERALIZATION = "generalization";

	private static final String UML_EANNOTATIONS = "eAnnotations";

	private static final String XMI_EXTENSION = "xmi:Extension";

	private static final String UML_ENUMERATIONLITERAL = "uml:EnumerationLiteral";

	private static final String UML_OWNEDLITERAL = "ownedLiteral";

	private static final String UML_OWNEDATTRIBUTE = "ownedAttribute";

	private static final String UML_PROPERTY = "uml:Property";

	private static final String UML_OWNEDEND = "ownedEnd";

	private static final String UML_LANGUAGE = "language";

	private static final String UML_BODY = "body";

	private static final String UML_SPECIFICATION = "specification";

	private static final String UML_ATTRIBUTE_CONSTRAINEDELEMENT = "constrainedElement";

	private static final String UML_OWNEDRULE = "ownedRule";

	private static final String UML_REALIZATION = "uml:Realization";

	private static final String UML_PRIMITIVE_TYPE = "uml:PrimitiveType";

	private static final String UML_ENUMERATION = "uml:Enumeration";

	private static final String UML_ASSOCIATION_CLASS = "uml:AssociationClass";

	private static final String UML_ASSOCIATION = "uml:Association";

	private static final String UML_CLASS = "uml:Class";

	private static final String UML_PACKAGEDELEMENT = "packagedElement";

	private static final String UML_ATTRIBUTE_TYPE = "type";

	private static final String XMI_NAMESPACE_PREFIX = "xmi";

	private static final String UML_ATTRIBUTE_NAME = "name";

	private static final String UML_MODEL = "uml:Model";

	private static final String UML_PACKAGE = "uml:Package";

	private static final String UML_ATTRIBUTE_IS_ABSRACT = "isAbstract";

	private static final String UML_TRUE = "true";

	private static final String UML_MEMBER_END = "memberEnd";

	private static final String UML_ATTRIBUTE_VALUE = "value";

	private static final String UML_ATTRIBUTE_KEY = "key";

	private static final String UML_ATTRIBUTE_GENERAL = "general";

	private static final String UML_ATTRIBUTE_HREF = "href";

	private static final String UML_ATTRIBUTE_AGGREGATION = "aggregation";

	private static final String UML_SHARED = "shared";

	private static final Object UML_COMPOSITE = "composite";

	/**
	 * Contains XML element names in the format "name>xmiId"
	 */
	private Stack<String> xmiIdStack;

	/**
	 * The schema graph.
	 */
	private SchemaGraph sg;

	/**
	 * The Schema vertex of the schema graph.
	 */
	private Schema schema;

	/**
	 * The GraphClass vertex of the schema graph.
	 */
	private GraphClass graphClass;

	/**
	 * A Stack containing the package hierarchy. Packages and their nesting are
	 * represented as tree in XML. The top element is the current package.
	 */
	private Stack<Package> packageStack;

	/**
	 * Maps XMI-Ids to vertices and edges of the schema graph.
	 */
	private Map<String, AttributedElement> idMap;

	/**
	 * Remembers the current class id for processing of nested elements.
	 */
	private String currentClassId;

	/**
	 * Remembers the current VertexClass/EdgeClass vertex for processing of
	 * nested elements.
	 */
	private AttributedElementClass currentClass;

	/**
	 * Remembers the current RecordDomain vertex for processing of nested
	 * elements.
	 */
	private RecordDomain currentRecordDomain;

	/**
	 * Remembers the current domain component edge for processing of nested
	 * elements.
	 */
	private HasRecordDomainComponent currentRecordDomainComponent;

	/**
	 * Remembers the current Attribute vertex for processing of nested elements.
	 */
	private Attribute currentAttribute;

	/**
	 * Marks Vertex/EdgeClass vertices with a set of XMI Ids of superclasses.
	 */
	private GraphMarker<Set<String>> generalizations;

	/**
	 * Keeps track of uml:Realizations (key = client id, value = set of supplier
	 * ids) as workaround for missing generalizations between association and
	 * association class.
	 */
	private Map<String, Set<String>> realizations;

	/**
	 * Marks Attribute vertices with the XMI Id of its type if the type can not
	 * be resolved at the time the Attribute is processed.
	 */
	private GraphMarker<String> attributeType;

	/**
	 * Marks RecordDomainComponent edges with the XMI Id of its type if the type
	 * can not be resolved at the time the component is processed.
	 */
	private GraphMarker<String> recordComponentType;

	/**
	 * Maps qualified names of domains to the corresponding Domain vertex.
	 */
	private Map<String, Domain> domainMap;

	/**
	 * A set of preliminary vertices which are created to have a target vertex
	 * for edges where the real target can only be created later (i.e. forward
	 * references in XMI). After processing is finished, this set must be empty,
	 * since each preliminary vertex has to be replaced by the correct vertex.
	 */
	private Set<Vertex> preliminaryVertices;

	/**
	 * Remembers the current association end edge (To/From edge), which can be
	 * an ownedEnd or an ownedAttribute, for processing of nested elements.
	 */
	private Edge currentAssociationEnd;

	/**
	 * The XMI file that's currently processed.
	 */
	private File currentXmiFile;

	/**
	 * The set of To/From edges which are the aggregate side of an
	 * AggregationClass/CompositionClass (use to determine the aggregateFrom
	 * attribute).
	 */
	private Set<Edge> aggregateEnds;

	/**
	 * The set of To/From edges which are represented by ownedEnd elements (used
	 * to determine the direction of edges).
	 */
	private Set<Edge> ownedEnds;

	/**
	 * True if currently processing a constraint (ownedRule) element.
	 */
	private boolean inConstraint;

	/**
	 * The XMI Id of the constrained element if the constraint has exactly one
	 * constrained element, null otherwise. If set to null, the constraint will
	 * be attached to the GraphClass vertex.
	 */
	private String constrainedElementId;

	/**
	 * Maps the XMI Id of constrained elements to the list of constraints.
	 * Constrains are the character data inside a body element of ownedRule
	 * elements.
	 */
	private Map<String, List<String>> constraints;

	/**
	 * When creating EdgeClass names, also use the rolename of the "from" end.
	 */
	private boolean useFromRole;

	/**
	 * After processing is complete, remove Domain vertices which are not used
	 * by an attribute or by a record domain component.
	 */
	private boolean removeUnusedDomains;

	/**
	 * When determining the edge direction, also take navigability of
	 * associations into account (rather than the drawing direction only).
	 */
	private boolean useNavigability;

	/**
	 * Suppresses the direct output into a dot- and tg-file.
	 */
	private boolean suppressOutput;

	/**
	 * Marks whether or not the xmi-file has been processed.
	 */
	private boolean processed;

	/**
	 * Flag for writing the output as SchemaGraph.
	 */
	private boolean writeSchemaGraph;

	/**
	 * Flag for exporting to dot
	 */
	private boolean writeDot;

	/**
	 * Flag for validation
	 */
	private boolean validate;

	/**
	 * Filename for the Schema.
	 */
	private String filenameSchema;

	/**
	 * Filename for the SchemaGraph;
	 */
	private String filenameSchemaGraph;

	/**
	 * Filename for dot.
	 */
	private String filenameDot;

	/**
	 * Filename for validation
	 */
	private String filenameValidation;

	/**
	 * Filename for the xmi file
	 */
	private String filenameXmi;

	/**
	 * Processes an XMI-file to a TG-file as schema or a schema in a grUML
	 * graph. For all command line options see
	 * {@link Rsa2Tg#processCommandLineOptions(String[])}.
	 * 
	 * @param args
	 *            Command line options.
	 */
	public static void main(String[] args) {

		System.out.println("RSA to TG");
		System.out.println("=========");
		JGraLab.setLogLevel(Level.OFF);

		// Retrieving all command line options
		CommandLine cli = processCommandLineOptions(args);

		assert cli != null : "No CommandLine object has been generated!";
		// All XMI input files
		String input = cli.getOptionValue('i');

		// Debug output filenames
		String export = cli.getOptionValue('e');
		String validation = cli.getOptionValue('r');

		String output = cli.getOptionValue('o');
		String schemaGraph = cli.getOptionValue('s');

		Rsa2Tg r = new Rsa2Tg();

		r.setUseFromRole(cli.hasOption('f'));
		r.setRemoveUnusedDomains(cli.hasOption('u'));
		r.setUseNavigability(cli.hasOption('n'));

		// apply options
		r.setWriteDot(cli.hasOption('e'));
		r.setValidate(cli.hasOption('r'));
		r.setWriteSchemaGraph(cli.hasOption('s'));
		r.setFilenameSchema(output);
		r.setFilenameSchemaGraph(schemaGraph);
		r.setFilenameDot(export);
		r.setFilenameValidation(validation);

		try {
			System.out.println("processing: " + input);
			r.process(input);
		} catch (Exception e) {
			System.err.println("An Exception occured while processing " + input
					+ ".");
			System.err.println(e.getMessage());
			e.printStackTrace();
		}

		System.out.println("Fini.");
	}

	/**
	 * Processes all command line parameters and returns a {@link CommandLine}
	 * object, which holds all values included in the given String array.
	 * 
	 * @param args
	 *            {@link CommandLine} parameters.
	 * @return {@link CommandLine} object, which holds all necessary values.
	 */
	public static CommandLine processCommandLineOptions(String[] args) {

		// Creates a OptionHandler.
		String toolString = "java " + Rsa2Tg.class.getName();
		String versionString = JGraLab.getInfo(false);
		OptionHandler oh = new OptionHandler(toolString, versionString);

		// Several Options are declared.
		Option validate = new Option(
				"r",
				"report",
				true,
				"(optional): write a validation report '<filename>.html' In case of no given filename, <filename> := "
						+ "'<NAME_OF_SCHEMA>'.");
		validate.setRequired(false);
		validate.setArgName("filename");
		validate.setOptionalArg(true);
		oh.addOption(validate);

		Option export = new Option(
				"e",
				"export",
				true,
				"(optional): write a dotty-graph "
						+ "'<filename>.dot'. The <filename> is optional.  In case of no given filename, <filename> := '<NAME_OF_"
						+ "SCHEMA>.dot'.");
		export.setRequired(false);
		export.setArgName("filename");
		export.setOptionalArg(true);
		oh.addOption(export);

		Option schemaGraph = new Option(
				"s",
				"schemaGraph",
				true,
				"(optional): write a TG-file of the Schema as graph instance in '<filename>.tg'"
						+ ". <filename> is optional. In case of no given filename, <filename> := '<NAME_OF_"
						+ "SCHEMA>.gruml.tg'.");
		schemaGraph.setRequired(false);
		schemaGraph.setArgName("filename");
		schemaGraph.setOptionalArg(true);
		oh.addOption(schemaGraph);

		Option input = new Option("i", "input", true,
				"(required): UML 2.1-XMI exchange model file of the Schema.");
		input.setRequired(true);
		input.setArgName("filename");
		oh.addOption(input);

		Option output = new Option("o", "output", true,
				"(optional): write a TG-file of the Schema in '<filename>.rsa.tg.'");
		output.setRequired(false);
		output.setArgName("filename");
		oh.addOption(output);

		Option fromRole = new Option(
				"f",
				"useFromRole",
				false,
				"(optional): if this flag is set, the name of from roles will be used for creating undefined EdgeClass names.");
		fromRole.setRequired(false);
		oh.addOption(fromRole);

		Option unusedDomains = new Option(
				"u",
				"ommitUnusedDomains",
				false,
				"(optional): if this flag is set, all unused domains will not be defined in the schema.");
		unusedDomains.setRequired(false);
		oh.addOption(unusedDomains);

		Option navigability = new Option(
				"n",
				"useNavigability",
				false,
				"(optional): if this flag is set, navigability information will be interpreted as reading direction.");
		navigability.setRequired(false);
		oh.addOption(navigability);

		// Parses the given command line parameters with all created Option.
		return oh.parse(args);
	}

	/**
	 * Creates a Rsa2Tg converter.
	 */
	public Rsa2Tg() {
		// Sets all names of XML-elements, which should be ignored.
		addIgnoredElements("profileApplication", "packageImport",
				"ownedComment");
	}

	/**
	 * Sets up several a {@link SchemaGraph} and data structures before the
	 * processing can start.
	 */
	@Override
	public void startDocument() {

		sg = GrumlSchema.instance().createSchemaGraph();

		// Initializing all necessary data structures for processing purposes.
		xmiIdStack = new Stack<String>();
		idMap = new HashMap<String, AttributedElement>();
		packageStack = new Stack<Package>();
		generalizations = new GraphMarker<Set<String>>(sg);
		realizations = new HashMap<String, Set<String>>();
		attributeType = new GraphMarker<String>(sg);
		recordComponentType = new GraphMarker<String>(sg);
		domainMap = new HashMap<String, Domain>();
		preliminaryVertices = new HashSet<Vertex>();
		aggregateEnds = new HashSet<Edge>();
		ownedEnds = new HashSet<Edge>();
		constraints = new HashMap<String, List<String>>();
		// constraintsLines = new HashMap<String, Location>();
	}

	/**
	 * Processes a XML element and decides how to handle it in order to get a
	 * Schema element.
	 * 
	 * @param parser
	 * @throws XMLStreamException
	 */
	@Override
	protected void startElement(String name) throws XMLStreamException {
		String xmiId = getAttribute(XMI_NAMESPACE_PREFIX, "id");
		xmiIdStack.push(xmiId);

		Vertex vertexId = null;
		if (getNestingDepth() == 1) {
			// In case of a root element
			if (name.equals(UML_MODEL) || name.equals(UML_PACKAGE)) {
				// Allowed elements

				// Gets the Schema name, creates a Schema and processes it.
				String nm = getAttribute(UML_ATTRIBUTE_NAME);

				int p = nm.lastIndexOf('.');
				schema = sg.createSchema();
				vertexId = schema;
				schema.set_packagePrefix(nm.substring(0, p));
				schema.set_name(nm.substring(p + 1));

				// Generates a GraphClass and links it with the created Schema
				graphClass = sg.createGraphClass();
				sg.createDefinesGraphClass(schema, graphClass);

				// Creates a default Package, links it and pushes it to the
				// packageStack.
				Package defaultPackage = sg.createPackage();
				defaultPackage.set_qualifiedName("");
				sg.createContainsDefaultPackage(schema, defaultPackage);
				packageStack.push(defaultPackage);
			} else {
				// Unexpected root element
				throw new ProcessingException(getParser(), filenameXmi,
						"Root element must be " + UML_MODEL + " or "
								+ UML_PACKAGE);
			}
		} else {
			// inside top level element

			// Type is retrieved
			String type = getAttribute(XMI_NAMESPACE_PREFIX, UML_ATTRIBUTE_TYPE);

			// Package element, which
			if (name.equals(UML_PACKAGEDELEMENT)) {
				if (type.equals(UML_PACKAGE)) {
					vertexId = handlePackage();
				} else if (type.equals(UML_CLASS)) {
					vertexId = handleClass(xmiId);
				} else if (type.equals(UML_ASSOCIATION)
						|| type.equals(UML_ASSOCIATION_CLASS)) {
					vertexId = handleAssociation(xmiId);
				} else if (type.equals(UML_ENUMERATION)) {
					vertexId = handleEnumeration();
				} else if (type.equals(UML_PRIMITIVE_TYPE)) {
					vertexId = handlePrimitiveType();
				} else if (type.equals(UML_REALIZATION)) {
					handleRealization();
				} else {
					throw new ProcessingException(getParser(), filenameXmi,
							createUnexpectedElementMessage(name, type));
				}

			} else if (name.equals(UML_OWNEDRULE)) {
				// Owned rule
				inConstraint = true;
				constrainedElementId = getAttribute(UML_ATTRIBUTE_CONSTRAINEDELEMENT);
				// If the ID is null, the constraint is attached to the
				// GraphClass

				if (constrainedElementId != null) {
					// There can be more than one ID, separated by spaces ==>
					// the constraint is attached to the GraphClass.
					int p = constrainedElementId.indexOf(' ');
					if (p >= 0) {
						constrainedElementId = null;
					}
				}

			} else if (name.equals(UML_SPECIFICATION)
			// Throw an error for specification elements, which aren't contained
					// in a constraint.
					|| name.equals(UML_LANGUAGE) || name.equals(UML_BODY)) {
				if (!inConstraint) {
					throw new ProcessingException(getParser(), filenameXmi,
							createUnexpectedElementMessage(name, null));
				}

			} else if (name.equals(UML_OWNEDEND)) {
				// Owned end marks the end of the current class, which should be
				// an edgeClasss.
				if (type.equals(UML_PROPERTY)
						&& (currentClass instanceof EdgeClass)) {
					handleAssociatioEnd(xmiId);
				} else {
					throw new ProcessingException(getParser(), filenameXmi,
							createUnexpectedElementMessage(name, type));
				}

			} else if (name.equals(UML_OWNEDATTRIBUTE)) {
				// Handles the attributes of the current element
				if (type.equals(UML_PROPERTY)) {
					handleOwnedAttribute(xmiId);
				} else {
					throw new ProcessingException(getParser(), filenameXmi,
							createUnexpectedElementMessage(name, type));
				}

			} else if (name.equals(UML_ATTRIBUTE_TYPE)) {
				// Handles the type of the current attribute, which should be a
				// primitive type.
				if (type.equals(UML_PRIMITIVE_TYPE)) {
					handleNestedTypeElement(type);
				} else {
					throw new ProcessingException(getParser(), filenameXmi,
							createUnexpectedElementMessage(name, type));
				}

			} else if (name.equals(UML_OWNEDLITERAL)) {
				// Handles the literal of the current enumeration.
				if (type.equals(UML_ENUMERATIONLITERAL)) {
					handleEnumerationLiteral();
				} else {
					throw new ProcessingException(getParser(), filenameXmi,
							createUnexpectedElementMessage(name, type));
				}
			} else if (name.equals(XMI_EXTENSION)) {
				// ignore
			} else if (name.equals(UML_EANNOTATIONS)) {
				// ignore
			} else if (name.equals(UML_GENERALIZATION)) {
				handleGeneralization();
			} else if (name.equals(UML_DETAILS)) {
				handleStereotype();
			} else if (name.equals(UML_LOWERVALUE)) {
				handleLowerValue();
			} else if (name.equals(UML_UPPERVALUE)) {
				handleUpperValue();
			} else {
				// for unexpected cases
				throw new ProcessingException(getParser(), filenameXmi,
						createUnexpectedElementMessage(name, type));
			}
		}

		// Links an existing XMI-id to a Vertex-id
		if ((xmiId != null) && (vertexId != null)) {
			idMap.put(xmiId, vertexId);
		}
	}

	/**
	 * Processes a XML element end tag in order to
	 * 
	 * @param parser
	 *            {@link XMLStreamReader}, which points to the current element.
	 * @throws XMLStreamException
	 */
	@Override
	protected void endElement(String name, StringBuilder content)
			throws XMLStreamException {
		String xmiId = xmiIdStack.pop();

		if (inConstraint && name.equals(UML_BODY)) {
			handleConstraint(content.toString().trim().replace("\\s", " "));
		}
		AttributedElement elem = idMap.get(xmiId);
		if (elem != null) {
			if (elem instanceof Package) {

				// TODO
				// There should be at least one package element in the
				// stack.
				if (packageStack.size() <= 1) {
					throw new ProcessingException(getParser(), filenameXmi,
							"XMI file is malformed. There is probably one end element to much.");
				}
				packageStack.pop();
			} else if (elem instanceof AttributedElementClass) {
				currentClassId = null;
				currentClass = null;
			} else if (elem instanceof RecordDomain) {
				currentRecordDomain = null;
			} else if (elem instanceof Attribute) {
				currentAttribute = null;
			}
		}
		if (name.equals(UML_PACKAGE)) {
			packageStack.pop();

			// TODO
			// There should be no package left.
			if (packageStack.size() != 0) {
				throw new ProcessingException(getParser(), filenameXmi,
						"XMI file is malformed. There is probably one end element to much.");
			}
		} else if (name.equals(UML_OWNEDATTRIBUTE)) {
			currentRecordDomainComponent = null;
			currentAssociationEnd = null;
		} else if (name.equals(UML_OWNEDEND)) {
			currentAssociationEnd = null;
		} else if (name.equals(UML_OWNEDRULE)) {
			inConstraint = false;
			constrainedElementId = null;
		}
	}

	/**
	 * Finalizes the created SchemaGraph by creating missing links between
	 * several objects.
	 * 
	 * @throws XMLStreamException
	 * @throws GraphIOException
	 */
	@Override
	public void endDocument() throws XMLStreamException {
		// finalizes processing by creating missing links

		// The qualified name of the GraphClass should be set.
		if (graphClass.get_qualifiedName() == null) {
			throw new ProcessingException(filenameXmi,
					"No <<graphclass>> defined in schema '"
							+ schema.get_packagePrefix() + "."
							+ schema.get_name() + "'");
		}

		// Now the RSA XMI file has been processed, but no linkage between
		// several objects in the SchemaGraph has occurred.
		linkGeneralizations();
		linkRecordDomainComponents();
		linkAttributeDomains();
		setAggregateFromAttributes();

		if (isUseNavigability()) {
			correctEdgeDirection();
		}

		attachConstraints();
		createEdgeClassNames();

		if (isRemoveUnusedDomains()) {
			removeUnusedDomains();
		}

		removeEmptyPackages();
		// preliminaryVertices must be empty at this time of processing,
		// otherwise there is an error...
		if (!preliminaryVertices.isEmpty()) {
			System.err.println("Remaining preliminary vertices ("
					+ preliminaryVertices.size() + "):");
			for (Vertex v : preliminaryVertices) {
				try {
					System.err.println(attributedElement2String(v));
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				}
			}
		}

		if (!preliminaryVertices.isEmpty()) {
			throw new ProcessingException(filenameXmi,
					"There are still vertices left over. ");
		}

		processed = true;
		if (!suppressOutput) {
			try {
				writeOutput();
			} catch (GraphIOException e) {
				throw new XMLStreamException(e);
			}
		}
	}

	/**
	 * Writes a DOT file and a TG file out.
	 * 
	 * @throws XMLStreamException
	 * @throws GraphIOException
	 */
	public void writeOutput() throws XMLStreamException, GraphIOException {

		if (processed) {

			// TODO
			// There should exist a Schema.
			if (schema == null) {
				throw new ProcessingException(filenameXmi,
						"No Schema has been generated.");
			}
			String schemaName = schema.get_name();

			// TODO
			// A Schema name must exist.
			if (schemaName == null) {
				throw new ProcessingException(filenameXmi,
						"No Schema name is present.");
			}

			String relativePathPrefix = currentXmiFile.getParent();

			// If in current working directory parent is null.
			relativePathPrefix = relativePathPrefix == null ? ""
					: relativePathPrefix + File.separator;

			if (writeDot) {
				String dotName = createFilename(schemaName, relativePathPrefix,
						filenameDot, ".dot");
				writeDotFile(dotName);
			}

			if (writeSchemaGraph) {
				String schemaGraphName = createFilename(schemaName,
						relativePathPrefix, filenameSchemaGraph, ".gruml.tg");
				writeSchemaGraph(schemaGraphName);
			}

			validateGraph(schemaName, relativePathPrefix);

			String tgSchemaName = createFilename(schemaName,
					relativePathPrefix, filenameSchema, ".rsa.tg");
			writeSchema(tgSchemaName, false);
		}
	}

	/**
	 * Performs a graph validation and writes a report in a file.
	 * 
	 * @param schemaName
	 *            Name of the Schema.
	 * @param relativePathPrefix
	 *            Relative path to a folder.
	 */
	private void validateGraph(String schemaName, String relativePathPrefix) {

		try {
			GraphValidator validator = new GraphValidator(sg);
			Set<ConstraintViolation> s;
			String validateName = null;
			if (validate) {
				validateName = createFilename(schemaName, relativePathPrefix,
						filenameValidation, ".html");

				s = validator.createValidationReport(validateName);

			} else {
				s = validator.validate();
			}
			if (!s.isEmpty()) {
				System.err.println("The schema graph is not valid:");
				for (ConstraintViolation currentViolation : s) {
					// print out violations
					System.err.println(currentViolation.getMessage());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Out of the given String objects a file name is created, which looks like
	 * this:
	 * 
	 * <pre>
	 * &lt;pathPrefix&gt;&lt;name&gt;
	 * </pre>
	 * 
	 * In case of 'name == null' the file name looks like this:
	 * 
	 * <pre>
	 * &lt;pathPrefix&gt;&lt;schemaName&gt;&lt;extension&gt;
	 * </pre>
	 * 
	 * @param schemaName
	 *            Name of the Schema.
	 * @param pathPrefix
	 *            Relative path to a folder.
	 * @param name
	 *            Name of the file.
	 * @param extension
	 *            Default extension of the file.
	 * @return Created file name.
	 */
	private String createFilename(String schemaName, String pathPrefix,
			String name, String extension) {

		StringBuilder out = new StringBuilder();
		out.append(pathPrefix);
		if (name == null) {
			out.append(schemaName);
			out.append(extension);
		} else {
			out.append(name);
		}
		return out.toString();
	}

	/**
	 * Creates a message for an unexpected element and includes its type.
	 * 
	 * @param name
	 *            Name of the unexpected element.
	 * @param type
	 *            Type of the unexpected element.
	 * @return Created message.
	 */
	private String createUnexpectedElementMessage(String name, String type) {

		String typeInsertion = (type != null) ? " of type " + type : "";
		return "Unexpected element <" + name + ">" + typeInsertion + ".";
	}

	/**
	 * Handles a 'uml:Package' element by creating a corresponding grUML Package
	 * element.
	 * 
	 * @param parser
	 *            XMLStreamReader, which points to the current XML element.
	 * @return Created Package object as Vertex.
	 */
	private Vertex handlePackage() throws XMLStreamException {

		Package pkg = sg.createPackage();
		pkg
				.set_qualifiedName(getQualifiedName(getAttribute(UML_ATTRIBUTE_NAME)));
		sg.createContainsSubPackage(packageStack.peek(), pkg);
		packageStack.push(pkg);
		return pkg;
	}

	/**
	 * Handles a 'uml:Class' element by creating a corresponding grUML
	 * VertexClass element.
	 * 
	 * @param parser
	 *            XMLStreamReader, which points to the current XML element.
	 * @param xmiId
	 *            XMI ID in RSA XMI file.
	 * @return Created VertexClass as Vertex.
	 * @throws XMLStreamException
	 */
	private Vertex handleClass(String xmiId) throws XMLStreamException {

		AttributedElement ae = idMap.get(xmiId);
		VertexClass vc = null;
		if (ae != null) {

			// TODO
			// Element with ID xmiID must be a VertexClass
			if (!(ae instanceof VertexClass)) {
				throw new ProcessingException(getParser(), filenameXmi,
						"The element with ID '" + xmiId
								+ "' is not a class. (VertexClass)");
			}

			assert preliminaryVertices.contains(ae);
			preliminaryVertices.remove(ae);
			vc = (VertexClass) ae;
		} else {
			vc = sg.createVertexClass();
		}
		currentClassId = xmiId;
		currentClass = vc;
		String abs = getAttribute(UML_ATTRIBUTE_IS_ABSRACT);
		vc.set_abstract((abs != null) && abs.equals(UML_TRUE));
		vc
				.set_qualifiedName(getQualifiedName(getAttribute(UML_ATTRIBUTE_NAME)));
		sg.createContainsGraphElementClass(packageStack.peek(), vc);

		// System.out.println("currentClass = " + currentClass + " "
		// + currentClass.getQualifiedName());
		return vc;
	}

	/**
	 * Handles a 'uml:Association' or a 'uml:AssociationClass' element by
	 * creating a corresponding EdgeClass element.
	 * 
	 * @param parser
	 *            XMLStreamReader, which points to the current XML element.
	 * @param xmiId
	 *            XMI ID in XMI file.
	 * @return Created EdgeClass as Vertex.
	 */
	private Vertex handleAssociation(String xmiId) throws XMLStreamException {

		// create an EdgeClass at first, probably, this has to
		// become an Aggregation or Composition later...
		AttributedElement ae = idMap.get(xmiId);
		EdgeClass ec = null;
		if (ae != null) {

			// TODO s.o.
			// Element with ID xmiID must be a EdgeClass
			if (!(ae instanceof EdgeClass)) {
				throw new ProcessingException(getParser(), filenameXmi,
						"The element with ID '" + xmiId
								+ "' is not a association. (EdgeClass)");
			}

			assert preliminaryVertices.contains(ae);
			preliminaryVertices.remove(ae);
			ec = (EdgeClass) ae;
		} else {
			ec = sg.createEdgeClass();
		}
		currentClassId = xmiId;
		currentClass = ec;
		String abs = getAttribute(UML_ATTRIBUTE_IS_ABSRACT);
		ec.set_abstract((abs != null) && abs.equals(UML_TRUE));
		String n = getAttribute(UML_ATTRIBUTE_NAME);
		n = (n == null) ? "" : n.trim();
		if (n.length() > 0) {
			n = Character.toUpperCase(n.charAt(0)) + n.substring(1);
		}
		ec.set_qualifiedName(getQualifiedName(n));
		sg.createContainsGraphElementClass(packageStack.peek(), ec);

		String memberEnd = getAttribute(UML_MEMBER_END);

		// TODO
		// An association have to have a end member.
		if (memberEnd == null) {
			throw new ProcessingException(getParser(), filenameXmi,
					"The association with ID '" + xmiId
							+ "' has no end member. (EdgeClass)");
		}
		memberEnd = memberEnd.trim().replaceAll("\\s+", " ");
		int p = memberEnd.indexOf(' ');
		String targetEnd = memberEnd.substring(0, p);
		String sourceEnd = memberEnd.substring(p + 1);

		Edge e = (Edge) idMap.get(sourceEnd);
		if (e == null) {
			VertexClass vc = sg.createVertexClass();
			preliminaryVertices.add(vc);
			vc.set_qualifiedName("preliminary for source end " + sourceEnd);
			e = sg.createFrom(ec, vc);
			idMap.put(sourceEnd, e);
		}

		e = (Edge) idMap.get(targetEnd);
		if (e != null) {
			assert e.isValid();
			assert e instanceof From;
			From from = (From) e;
			To to = sg.createTo(ec, (VertexClass) from.getOmega());
			to.set_max(from.get_max());
			to.set_min(from.get_min());
			to.set_roleName(from.get_roleName());
			to.set_redefinedRoles(from.get_redefinedRoles());
			if (ownedEnds.contains(from)) {
				ownedEnds.remove(from);
				ownedEnds.add(to);
			}
			if (aggregateEnds.contains(from)) {
				aggregateEnds.remove(from);
				aggregateEnds.add(to);
			}
			e.delete();
			idMap.put(targetEnd, to);
		} else {
			VertexClass vc = sg.createVertexClass();
			preliminaryVertices.add(vc);
			vc.set_qualifiedName("preliminary for target end " + targetEnd);
			e = sg.createTo(ec, vc);
			idMap.put(targetEnd, e);
		}
		return ec;
	}

	/**
	 * Handles a 'uml:Enumeration' element by creating a corresponding
	 * EnumDomain element.
	 * 
	 * @param parser
	 *            XMLStreamReader, which points to the current XML element.
	 * @return Created EnumDomain as Vertex.
	 * @throws XMLStreamException
	 */
	private Vertex handleEnumeration() throws XMLStreamException {

		EnumDomain ed = sg.createEnumDomain();
		de.uni_koblenz.jgralab.grumlschema.structure.Package p = packageStack
				.peek();
		ed
				.set_qualifiedName(getQualifiedName(getAttribute(UML_ATTRIBUTE_NAME)));
		sg.createContainsDomain(p, ed);
		ed.set_enumConstants(new ArrayList<String>());
		Domain dom = domainMap.get(ed.get_qualifiedName());
		if (dom != null) {
			// there was a preliminary vertex for this domain
			// link the edges to the correct one

			assert preliminaryVertices.contains(dom);
			reconnectEdges(dom, ed);
			// delete preliminary vertex
			dom.delete();
			preliminaryVertices.remove(dom);
		}
		domainMap.put(ed.get_qualifiedName(), ed);
		return ed;
	}

	/**
	 * Handles a 'uml:PrimitiveType' element by creating a corresponding Domain
	 * element.
	 * 
	 * @return Created Domain as Vertex.
	 */
	private Vertex handlePrimitiveType() throws XMLStreamException {

		String typeName = getAttribute(UML_ATTRIBUTE_NAME);

		// TODO
		// A type name must be defined.
		if (typeName == null) {
			throw new ProcessingException(getParser(), filenameXmi,
					"No type name declared.");
		}
		typeName = typeName.replaceAll("\\s", "");

		// TODO Exception "Type name is empty"
		if (typeName.length() <= 0) {
			throw new ProcessingException(getParser(), filenameXmi,
					"The current type is empty.");
		}
		Domain dom = createDomain(typeName);

		assert dom != null;
		return dom;
	}

	/**
	 * Handles a 'uml:Realization' by putting it into a map of realizations. By
	 * this, missing generalizations can be traced.
	 * 
	 * @param parser
	 *            XMLStreamReader, which points to the current XML element.
	 */
	private void handleRealization() throws XMLStreamException {

		String supplier = getAttribute(UML_ATTRIBUTE_SUPPLIER);
		String client = getAttribute(UML_ATTRIBUTE_CLIENT);
		Set<String> reals = realizations.get(client);
		if (reals == null) {
			reals = new TreeSet<String>();
			realizations.put(client, reals);
		}
		reals.add(supplier);
	}

	/**
	 * Creates a String for a AttributedElementClass by writing the
	 * AttributedElementClass name first and than a list of attributes with
	 * their values.
	 * 
	 * @param attributedElement
	 *            AttributedElement, of which a String representation should be
	 *            created.
	 * @return A String representing the given AttributedElement.
	 * @throws NoSuchFieldException
	 */
	private String attributedElement2String(AttributedElement attributedElement)
			throws NoSuchFieldException {

		StringBuilder sb = new StringBuilder();

		de.uni_koblenz.jgralab.schema.AttributedElementClass aec = attributedElement
				.getAttributedElementClass();
		sb.append(attributedElement);
		sb.append(" { ");

		for (de.uni_koblenz.jgralab.schema.Attribute attr : aec
				.getAttributeList()) {
			sb.append(attr.getName());
			sb.append(" = ");
			sb.append(attributedElement.getAttribute(attr.getName()));
			sb.append("; ");
		}
		sb.append("}\n");

		return sb.toString();
	}

	/**
	 * Handles a 'uml:EnumerationLiteral' by creating a corresponding
	 * enumeration literal and adding it to its EnumDomain.
	 */
	private void handleEnumerationLiteral() throws XMLStreamException {

		String s = getAttribute(UML_ATTRIBUTE_NAME);

		// TODO
		// A Literal must be declared.
		if (s == null) {
			throw new ProcessingException(getParser(), filenameXmi,
					"No Literal declared.");
		}
		s = s.trim();

		// TODO
		// Literal must not be empty.
		if (s.length() <= 0) {
			throw new ProcessingException(getParser(), filenameXmi,
					"Literal is empty.");
		}

		String classifier = getAttribute(UML_ATTRIBUTE_CLASSIFIER);

		// TODO
		// Exception "No Enum found for Literal " ... " found.
		if (classifier == null) {
			throw new ProcessingException(getParser(), filenameXmi,
					"No Enumeration found for Literal '" + s + "'.");
		}
		EnumDomain ed = (EnumDomain) idMap.get(classifier);

		ed.get_enumConstants().add(s);
	}

	/**
	 * Writes the current processed Schema as a Schema to a TG file.
	 * 
	 * @param schemaName
	 *            Name of the Schema.
	 * @param formatTg
	 *            Flag to format the TG file.
	 */
	private void writeSchema(String schemaName, boolean formatTg) {

		try {
			SchemaGraph2Tg sg2tg = new SchemaGraph2Tg(sg, schemaName);
			sg2tg.setIsFormatted(formatTg);
			sg2tg.run();
		} catch (IOException e) {
			throw new RuntimeException(
					"SchemaGraph2Tg failed with an IOException!", e);
		}
	}

	/**
	 * Corrects the current edge direction of every EdgeClass by using the
	 * navigability.
	 */
	private void correctEdgeDirection() {

		if (!isUseNavigability()) {
			return;
		}

		for (EdgeClass e : sg.getEdgeClassVertices()) {
			From from = e.getFirstFrom();
			To to = e.getFirstTo();

			// TODO
			// 'from' and 'to' Edge have to be declared.
			if (from == null || to == null) {
				throw new ProcessingException(filenameXmi,
						"No 'from'-, 'to'-Edge or both are declared.");
			}
			boolean fromIsNavigable = !ownedEnds.contains(from);
			boolean toIsNavigable = !ownedEnds.contains(to);
			if (fromIsNavigable == toIsNavigable) {
				// no navigability specified or both ends navigable:
				// do nothing, edge direction is determined by order of memerEnd
				// in association
				continue;
			}
			if (toIsNavigable) {
				// "to" end is marked navigable, nothing to change
				continue;
			}

			// "from" end is marked navigable, swap edge direction
			VertexClass vc = (VertexClass) to.getThat();
			to.setThat(from.getThat());
			from.setThat(vc);

			int h = to.get_min();
			to.set_min(from.get_min());
			from.set_min(h);

			h = to.get_max();
			to.set_max(from.get_max());
			from.set_max(h);

			String r = to.get_roleName();
			to.set_roleName(from.get_roleName());
			from.set_roleName(r);

			Set<String> rd = to.get_redefinedRoles();
			to.set_redefinedRoles(from.get_redefinedRoles());
			from.set_redefinedRoles(rd);

			if (e instanceof AggregationClass) {
				AggregationClass ac = (AggregationClass) e;
				ac.set_aggregateFrom(!ac.is_aggregateFrom());
			}
		}

	}

	/**
	 * Attaches all Constraints to their corresponding AttributedElementClass.
	 * 
	 * @throws XMLStreamException
	 */
	private void attachConstraints() throws XMLStreamException {
		for (String constrainedElementId : constraints.keySet()) {
			List<String> l = constraints.get(constrainedElementId);
			AttributedElement ae = idMap.get(constrainedElementId);
			if (ae == null) {
				ae = graphClass;
			}

			// TODO
			// Constraint are attached to GraphClass, VertexClass, EdgeClass or
			// Association Ends.
			if (!(ae instanceof AttributedElementClass)
					&& !(ae instanceof From) && !(ae instanceof To)) {
				throw new ProcessingException(filenameXmi,
						"Constraint can only be attached to GraphClass, "
								+ "VertexClass, EdgeClass or association ends.");
			}
			if (ae instanceof AttributedElementClass) {
				for (String text : l) {
					addGreqlConstraint((AttributedElementClass) ae, text);
				}
			} else {
				// TODO Exception
				if (l.size() != 1) {
					throw new ProcessingException(filenameXmi,
							"Only one redefines Constraint allowed");
				}
				addRedefinesConstraint((Edge) ae, l.get(0));
			}
		}
	}

	/**
	 * Sets of all aggregated Ends the aggregatedFrom Attribute.
	 */
	private void setAggregateFromAttributes() {
		for (Edge e : aggregateEnds) {
			((AggregationClass) e.getAlpha())
					.set_aggregateFrom(e instanceof To);
		}
		aggregateEnds.clear();
	}

	private void createEdgeClassNames() {
		for (EdgeClass ec : sg.getEdgeClassVertices()) {
			String name = ec.get_qualifiedName().trim();
			if (!name.equals("") && !name.endsWith(".")) {
				continue;
			}

			// System.err.print("createEdgeClassName for '" + name + "'");
			String ecName = null;
			// invent edgeclass name
			String toRole = ec.getFirstTo().get_roleName();
			if ((toRole == null) || toRole.equals("")) {
				toRole = ((VertexClass) ec.getFirstTo().getOmega())
						.get_qualifiedName();
				int p = toRole.lastIndexOf('.');
				if (p >= 0) {
					toRole = toRole.substring(p + 1);
				}
			} else {
				toRole = Character.toUpperCase(toRole.charAt(0))
						+ toRole.substring(1);
			}

			// TODO Exception
			if (toRole == null || toRole.length() <= 0) {
				throw new ProcessingException(filenameXmi,
						"There is no role name 'to' for the edge '" + name
								+ "' defined.");
			}
			if (ec instanceof AggregationClass) {
				if (((AggregationClass) ec).is_aggregateFrom()) {
					ecName = "Contains" + toRole;
				} else {
					ecName = "IsPartOf" + toRole;
				}
			} else {
				ecName = "LinksTo" + toRole;
			}
			if (isUseFromRole()) {
				String fromRole = ec.getFirstFrom().get_roleName();
				if ((fromRole == null) || fromRole.equals("")) {
					fromRole = ((VertexClass) ec.getFirstFrom().getOmega())
							.get_qualifiedName();
					int p = fromRole.lastIndexOf('.');
					if (p >= 0) {
						fromRole = fromRole.substring(p + 1);
					}
				} else {
					fromRole = Character.toUpperCase(fromRole.charAt(0))
							+ fromRole.substring(1);
				}

				// TODO Exception
				if (fromRole == null || fromRole.length() <= 0) {
					throw new ProcessingException(filenameXmi,
							"There is no role name of 'from' for the edge '"
									+ name + "' defined.");
				}
				name += fromRole;
			}
			// System.err.println(" ==> '" + name + "' + '" + ecName + "'");

			assert (ecName != null) && (ecName.length() > 0);
			ec.set_qualifiedName(name + ecName);
		}
	}

	private void removeUnusedDomains() {
		if (!isRemoveUnusedDomains()) {
			return;
		}
		Domain d = sg.getFirstDomain();
		while (d != null) {
			Domain n = d.getNextDomain();
			// unused if degree <=1 (one incoming edge is the ContainsDomain
			// edge from a Package)
			if (d.getDegree(EdgeDirection.IN) <= 1) {
				// System.out.println("...remove unused domain '"
				// + d.getQualifiedName() + "'");
				d.delete();
				d = sg.getFirstDomain();
			} else {
				d = n;
			}
		}
	}

	private void linkRecordDomainComponents() {
		for (HasRecordDomainComponent comp : sg
				.getHasRecordDomainComponentEdges()) {
			String domainId = recordComponentType.getMark(comp);
			if (domainId == null) {
				continue;
			}
			Domain dom = (Domain) idMap.get(domainId);
			if (dom != null) {
				Domain d = (Domain) comp.getOmega();

				// preliminary domain vertex exists and has type StringDomain,
				// but the name of the StringDomain is the "real" domain name
				assert (d instanceof StringDomain)
						&& d.get_qualifiedName().equals(domainId)
						&& preliminaryVertices.contains(d);
				comp.setOmega(dom);
				d.delete();
				preliminaryVertices.remove(d);
				recordComponentType.removeMark(comp);
			} else {
				throw new ProcessingException(filenameXmi,
						"Undefined Domain with ID '" + domainId + "' found.");
			}
		}

		// TODO Exception, es gibt RecordDomains mit Komponenten, deren Domain
		// nicht aufgelöst wurde
		if (!recordComponentType.isEmpty()) {
			throw new ProcessingException(filenameXmi,
					"There are some RocordDomain objects, whos domains are not resolved.");
		}
	}

	private void linkAttributeDomains() {
		for (Attribute att : sg.getAttributeVertices()) {
			String domainId = attributeType.getMark(att);
			if (domainId == null) {
				assert att.getDegree(HasDomain.class, EdgeDirection.OUT) == 1;
				continue;
			}
			Domain dom = (Domain) idMap.get(domainId);
			if (dom != null) {
				sg.createHasDomain(att, dom);
				attributeType.removeMark(att);
			} else {
				// TODO Exception
				throw new ProcessingException(filenameXmi,
						"Undefined Domain with ID '" + domainId + "' found.");
			}

			assert att.getDegree(HasDomain.class, EdgeDirection.OUT) == 1;
		}

		// TODO Exception, es gibt Attribute, deren Domain nicht aufgelöst wurde
		if (!attributeType.isEmpty()) {
			throw new ProcessingException(filenameXmi,
					"There are some Attribute objects, whos domains are not resolved.");
		}
	}

	private void writeDotFile(String dotName) {
		Tg2Dot tg2Dot = new Tg2Dot();
		tg2Dot.setGraph(sg);
		tg2Dot.setPrintEdgeAttributes(true);
		tg2Dot.setOutputFile(dotName);
		tg2Dot.printGraph();
	}

	private void writeSchemaGraph(String schemaGraphName)
			throws GraphIOException {
		GraphIO.saveGraphToFile(schemaGraphName, sg, null);
	}

	private void linkGeneralizations() {
		for (String clientId : realizations.keySet()) {
			Set<String> suppliers = realizations.get(clientId);
			AttributedElementClass client = (AttributedElementClass) idMap
					.get(clientId);
			if (suppliers.size() > 0) {
				Set<String> superClasses = generalizations.getMark(client);
				if (superClasses == null) {
					superClasses = new TreeSet<String>();
					generalizations.mark(client, superClasses);
				}
				superClasses.addAll(suppliers);
			}
		}
		for (AttributedElement ae : generalizations.getMarkedElements()) {
			Set<String> superclasses = generalizations.getMark(ae);
			for (String id : superclasses) {
				AttributedElementClass sup = (AttributedElementClass) idMap
						.get(id);

				// TODO Exception, Superklasse mit ID nicht gefunden
				if (sup == null) {
					throw new ProcessingException(filenameXmi,
							"The superclass with ID '" + id
									+ "' could not be found.");
				}
				if (sup instanceof VertexClass) {

					// TODO VertexClass kann nur von VertexClass spezialisiert
					// werden, nicht von (klasse von AE)
					if (!(ae instanceof VertexClass)) {
						throw new ProcessingException(
								filenameXmi,
								"The superclasses do not share the same base type (Either EdgeClass or VertexClass).");
					}

					sg.createSpecializesVertexClass((VertexClass) ae,
							(VertexClass) sup);
				} else if (sup instanceof EdgeClass) {

					// TODO EdgeClass kann nur ... s.o.
					if (!(ae instanceof EdgeClass)) {
						throw new ProcessingException(
								filenameXmi,
								"The superclasses do not share the same base type (Either EdgeClass or VertexClass).");
					}

					sg.createSpecializesEdgeClass((EdgeClass) ae,
							(EdgeClass) sup);
				} else {
					// should not get here
					throw new RuntimeException(
							"FIXME: Unexpected super class type. Super class must be VertexClass or EdgeClass!");
				}
			}
		}
		generalizations.clear();
	}

	private void removeEmptyPackages() {
		// remove all empty packages except the default package
		de.uni_koblenz.jgralab.grumlschema.structure.Package p = sg
				.getFirstPackage();
		while (p != null) {
			de.uni_koblenz.jgralab.grumlschema.structure.Package n = p
					.getNextPackage();
			if ((p.getDegree() == 1) && (p.get_qualifiedName().length() > 0)) {
				// System.out.println("...remove empty package '"
				// + p.getQualifiedName() + "'");
				p.delete();
				// start over to capture packages that become empty after
				// deletion of p
				p = sg.getFirstPackage();
			} else {
				p = n;
			}
		}
	}

	private void handleConstraint(String text) throws XMLStreamException {
		if (text.startsWith("redefines") || text.startsWith("\"")) {
			List<String> l = constraints.get(constrainedElementId);
			if (l == null) {
				l = new LinkedList<String>();
				constraints.put(constrainedElementId, l);
			}
			l.add(text);
		} else {
			throw new ProcessingException(filenameXmi, getParser()
					.getLocation().getLineNumber(),
					"Illegal constraint format: " + text);
		}
	}

	private void addRedefinesConstraint(Edge constrainedEnd, String text)
			throws XMLStreamException {

		// TODO: Exception, redefines constraint kann nur an association ends
		// stehen
		if (!(constrainedEnd instanceof From)
				&& !(constrainedEnd instanceof To)) {
			throw new ProcessingException(filenameXmi,
					"Redefines constraints must be attached to one association end.");
		}

		text = text.trim().replaceAll("\\s+", " ");
		if (!text.startsWith("redefines ")) {
			throw new ProcessingException(filenameXmi,
					"Wrong redefines constraint format.");
		}
		String[] roles = text.substring(10).split("\\s*,\\s*");

		// TODO Exception
		if (roles.length < 1) {
			throw new ProcessingException(filenameXmi,
					"No role defined. At least one role have to be defined.");
		}
		Set<String> redefinedRoles = new TreeSet<String>();
		for (String role : roles) {

			// TODO Exception
			if (role.length() < 1) {
				throw new ProcessingException(filenameXmi,
						"the role name is empty.");
			}
			redefinedRoles.add(role);
		}

		// TODO UNNECESSARY
		if (redefinedRoles.size() < 1) {
			throw new ProcessingException(filenameXmi,
					"No redefined role has been added!");
		}

		if (constrainedEnd instanceof From) {
			((From) constrainedEnd).set_redefinedRoles(redefinedRoles);
		} else {
			((To) constrainedEnd).set_redefinedRoles(redefinedRoles);
		}
	}

	private void addGreqlConstraint(AttributedElementClass constrainedClass,
			String text) throws XMLStreamException {

		assert constrainedClass != null;
		Constraint constraint = sg.createConstraint();
		sg.createHasConstraint(constrainedClass, constraint);

		// the "text" must contain 2 or 3 space-separated quoted ("...") strings
		int stringCount = 0;
		char[] ch = text.toCharArray();
		boolean inString = false;
		boolean escape = false;
		int beginIndex = 0;
		for (int i = 0; i < ch.length; ++i) {
			char c = ch[i];
			if (inString) {
				if (c == '\\') {
					escape = true;
				} else if (!escape && (c == '"')) {
					++stringCount;
					switch (stringCount) {
					case 1:
						constraint.set_message(text
								.substring(beginIndex + 1, i).trim());
						break;
					case 2:
						constraint.set_predicateQuery(text.substring(
								beginIndex + 1, i).trim());
						break;
					case 3:
						constraint.set_offendingElementsQuery(text.substring(
								beginIndex + 1, i).trim());
						break;
					default:
						throw new ProcessingException(filenameXmi,
								"Illegal constraint format. The constraint text was '"
										+ text + "'.");
					}
					inString = false;
				} else if (escape && (c == '"')) {
					escape = false;
				}
			} else {
				if (Character.isWhitespace(c)) {
					// ignore
				} else {
					if (c == '"') {
						inString = true;
						beginIndex = i;
					} else {
						throw new ProcessingException(filenameXmi,
								"Illegal constraint format. The constraint text was '"
										+ text + "'.  Expected '\"' but got '"
										+ c + "'.  (position = " + i + ").");
					}
				}
			}
		}
		if (inString || escape || (stringCount < 2) || (stringCount > 3)) {
			throw new ProcessingException(filenameXmi,
					"Illegal constraint format.  The constraint text was '"
							+ text + "'.");
		}
	}

	private void handleUpperValue() throws XMLStreamException {
		int n = getValue();
		if (currentAssociationEnd instanceof From) {
			((From) currentAssociationEnd).set_max(n);
		} else {
			((To) currentAssociationEnd).set_max(n);
		}
	}

	private int getValue() throws XMLStreamException {
		assert currentAssociationEnd != null;
		String val = getAttribute(UML_ATTRIBUTE_VALUE);
		return (val == null) ? 0 : val.equals("*") ? Integer.MAX_VALUE
				: Integer.parseInt(val);
	}

	private void handleLowerValue() throws XMLStreamException {
		int n = getValue();
		if (currentAssociationEnd instanceof From) {
			((From) currentAssociationEnd).set_min(n);
		} else {
			((To) currentAssociationEnd).set_min(n);
		}
	}

	private void handleStereotype() throws XMLStreamException {
		String key = getAttribute(UML_ATTRIBUTE_KEY);
		if (key.equals("graphclass")) {
			// convert currentClass to graphClass

			// TODO Exception, <<graphclass>> kann nur an einer (UML)Klasse
			// stehen,
			// nicht an anderen Elementen
			if (currentClass == null || !(currentClass instanceof VertexClass)) {
				throw new ProcessingException(getParser(), filenameXmi,
						"The stereotype <<graphclass>> is only allow for UML-classes.");
			}

			AttributedElementClass aec = (AttributedElementClass) idMap
					.get(currentClassId);
			graphClass.set_qualifiedName(aec.get_qualifiedName());
			Edge e = aec.getFirstEdge();
			while (e != null) {
				Edge n = e.getNextEdge();
				if (e instanceof ContainsGraphElementClass) {
					e.delete();
				} else {
					e.setThis(graphClass);
				}
				e = n;
			}
			aec.delete();
			currentClass = graphClass;

			// System.out.println("currentClass = " + currentClass + " "
			// + currentClass.getQualifiedName());

		} else if (key.equals("record")) {
			// convert current class to RecordDomain

			// Exception, <<record>> kann nur an einer (UML)Klasse stehen,
			// nicht an anderen Elementen
			assert currentClass != null;
			assert currentClass instanceof VertexClass;

			RecordDomain rd = sg.createRecordDomain();
			rd.set_qualifiedName(currentClass.get_qualifiedName());
			Edge e = currentClass.getFirstEdge();
			while (e != null) {
				Edge n = e.getNextEdge();
				if (e instanceof ContainsGraphElementClass) {
					sg
							.createContainsDomain(
									(de.uni_koblenz.jgralab.grumlschema.structure.Package) e
											.getThat(), rd);
					e.delete();
				} else if (e instanceof HasAttribute) {
					Attribute att = (Attribute) e.getThat();
					Edge d = att.getFirstHasDomain();
					if (d != null) {
						Domain dom = (Domain) e.getThat();
						HasRecordDomainComponent comp = sg
								.createHasRecordDomainComponent(rd, dom);
						comp.set_name(att.get_name());
					} else {
						String typeId = attributeType.getMark(att);

						// TODO
						if (typeId == null) {
							throw new ProcessingException(getParser(),
									filenameXmi, "No type id has been defined.");
						}
						Domain dom = sg.createStringDomain();
						dom.set_qualifiedName(typeId);
						preliminaryVertices.add(dom);
						HasRecordDomainComponent comp = sg
								.createHasRecordDomainComponent(rd, dom);
						recordComponentType.mark(comp, typeId);
						attributeType.removeMark(att);
					}
					att.delete();
				} else {
					System.err.println("Can't handle " + e);
				}
				e = n;
			}

			// TODO Exception: Klasser <<record>> blabla darf keine
			// Assoziationen haben
			if (currentClass.getDegree() != 0) {
				throw new ProcessingException(getParser(), filenameXmi,
						"A <<record>>-class must not have any association.");
			}
			domainMap.put(rd.get_qualifiedName(), rd);
			idMap.put(currentClassId, rd);
			currentRecordDomain = rd;
			currentClass.delete();
			currentClass = null;
			currentClassId = null;

			// System.out
			// .println("currentClass = null, currentRecordDomain = "
			// + rd + " " + rd.getQualifiedName());
		} else if (key.equals("abstract")) {

			// TODO Abstract darf nur an Klassen und Assoziationen stehen
			if (currentClass == null) {
				throw new ProcessingException(getParser(), filenameXmi,
						"The modifier 'abstract' is only allowed for classes or associations.");
			}
			currentClass.set_abstract(true);
		} else {
			throw new ProcessingException(getParser(), filenameXmi,
					"Unexpected stereotype '" + key + "'.");
		}
	}

	private void handleGeneralization() throws XMLStreamException {
		String general = getAttribute(UML_ATTRIBUTE_GENERAL);
		Set<String> gens = generalizations.getMark(currentClass);
		if (gens == null) {
			gens = new TreeSet<String>();
			generalizations.mark(currentClass, gens);
		}
		gens.add(general);
	}

	private void handleNestedTypeElement(String type) throws XMLStreamException {

		// TODO UNNECESSARY
		// Dokumentstruktur, <type> nur in Attribut oder <<record>> Klasse
		// erlaubt
		if (currentAttribute == null && currentRecordDomain == null) {
			throw new ProcessingException(
					getParser(),
					filenameXmi,
					"The element <type> should only be included in Attributes or <<record>>-classes.");
		}
		String href = getAttribute(UML_ATTRIBUTE_HREF);

		// TODO Exc. Typname fehlt
		if (href == null) {
			throw new ProcessingException(getParser(), filenameXmi,
					"No type name defined.");
		}
		Domain dom = null;
		if (href.endsWith("#String")) {
			dom = createDomain("String");
		} else if (href.endsWith("#Integer")) {
			dom = createDomain("Integer");
		} else if (href.endsWith("#Boolean")) {
			dom = createDomain("Boolean");
		} else {
			throw new ProcessingException(getParser(), filenameXmi,
					"Unexpected '" + type + "' with href '" + href + "'.");
		}

		if (currentRecordDomain != null) {
			// type of record domain component

			assert currentRecordDomainComponent != null;
			if (dom != null) {
				Domain d = (Domain) currentRecordDomainComponent.getOmega();
				assert (d instanceof StringDomain)
						&& (d.get_qualifiedName() == null)
						&& preliminaryVertices.contains(d);
				currentRecordDomainComponent.setOmega(dom);
				d.delete();
				preliminaryVertices.remove(d);
				recordComponentType.removeMark(currentRecordDomainComponent);
			}
		} else {
			// type of an attribute of an AttributedElementClass

			// TODO Exc. type muss in Attribute sein
			if (currentAttribute == null) {
				throw new ProcessingException(
						getParser(),
						filenameXmi,
						"The element <type> should only be included in Attributes or <<record>>-classes.");
			}
			if (dom != null) {
				sg.createHasDomain(currentAttribute, dom);
				attributeType.removeMark(currentAttribute);
			}
		}
	}

	private void handleOwnedAttribute(String xmiId) throws XMLStreamException {

		// TODO Exc. OwnedAttribute muss in einer Knoten/Kantenklasse oder einer
		// <<record>>-Klasse stehen
		if (currentClass == null && currentRecordDomain == null) {
			throw new ProcessingException(
					getParser(),
					filenameXmi,
					"An owned attribute have to be defined in a VertexClass, EdgeClass or a RecordDomain.");
		}
		String association = getAttribute(UML_ATTRIBUTE_ASSOCIATION);
		if (association == null) {
			String attrName = getAttribute(UML_ATTRIBUTE_NAME);

			// TODO Exc.
			if (attrName == null) {
				throw new ProcessingException(getParser(), filenameXmi,
						"No attribute name defined.");
			}
			attrName = attrName.trim();

			// TODO Exception
			if (attrName.length() <= 0) {
				throw new ProcessingException(getParser(), filenameXmi,
						"The attribute name is empty.");
			}

			String typeId = getAttribute(UML_ATTRIBUTE_TYPE);

			if (currentClass != null) {
				// property is an "ordinary" attribute
				Attribute att = sg.createAttribute();
				currentAttribute = att;
				att.set_name(attrName);
				sg.createHasAttribute(currentClass, att);
				if (typeId != null) {
					attributeType.mark(att, typeId);
				}
			} else {
				// property is a record component
				assert currentRecordDomain != null;
				currentAttribute = null;
				currentRecordDomainComponent = null;
				if (typeId != null) {
					Vertex v = (Vertex) idMap.get(typeId);
					if (v != null) {
						assert v instanceof Domain;
						currentRecordDomainComponent = sg
								.createHasRecordDomainComponent(
										currentRecordDomain, (Domain) v);
					} else {
						Domain dom = sg.createStringDomain();
						dom.set_qualifiedName(typeId);
						preliminaryVertices.add(dom);
						currentRecordDomainComponent = sg
								.createHasRecordDomainComponent(
										currentRecordDomain, dom);
						recordComponentType.mark(currentRecordDomainComponent,
								typeId);
					}
				} else {
					Domain dom = sg.createStringDomain();
					preliminaryVertices.add(dom);
					currentRecordDomainComponent = sg
							.createHasRecordDomainComponent(
									currentRecordDomain, dom);
				}
				currentRecordDomainComponent.set_name(attrName);
			}
		} else {

			// TODO Exc. (s.o.)
			if (currentClass == null || currentRecordDomain != null) {
				throw new ProcessingException(
						getParser(),
						filenameXmi,
						"An owned attribute have to be defined in a VertexClass, EdgeClass or a RecordDomain.");
			}
			handleAssociatioEnd(xmiId);
		}
	}

	private void handleAssociatioEnd(String xmiId) throws XMLStreamException {
		String endName = getAttribute(UML_ATTRIBUTE_NAME);
		String agg = getAttribute(UML_ATTRIBUTE_AGGREGATION);
		boolean aggregation = (agg != null) && agg.equals(UML_SHARED);
		boolean composition = (agg != null) && agg.equals(UML_COMPOSITE);

		Edge e = (Edge) idMap.get(xmiId);
		if (e == null) {
			// try to find the end's VertexClass
			// if not found, create a preliminary VertexClass
			VertexClass vc = null;
			// we have an "ownedEnd", vertex class id is in "type" attribute
			String typeId = getAttribute(UML_ATTRIBUTE_TYPE);

			// TODO Exception, weil der Fehler vom Input verursacht wird und
			// wahrscheinlich deshalb nicht mehr weiter gearbeitet werden kann.
			if (typeId == null) {
				throw new ProcessingException(getParser(), filenameXmi,
						"No type has been defined.");
			}
			AttributedElement ae = idMap.get(typeId);
			if (ae != null) {
				// VertexClass found

				// TODO Exc. (s.o.)
				if (!(ae instanceof VertexClass)) {
					throw new ProcessingException(getParser(), filenameXmi,
							"Both types must share the same base class. (Either VertexClass or EdgeClass)");
				}
				vc = (VertexClass) ae;
			} else {
				// create a preliminary vertex class
				vc = sg.createVertexClass();
				preliminaryVertices.add(vc);
				vc.set_qualifiedName(typeId);
				idMap.put(typeId, vc);
			}

			// try to find the end's EdgeClass
			EdgeClass ec = null;
			if (currentClass instanceof EdgeClass) {
				// we have an "ownedEnd", so the end's Edge is the
				// currentClass
				ec = correctAggregationAndComposition((EdgeClass) currentClass,
						aggregation, composition);
				currentClass = ec;
				idMap.put(currentClassId, currentClass);
			} else {
				// we have an ownedAttribute
				// edge class id is in "association"
				String associationId = getAttribute(UML_ATTRIBUTE_ASSOCIATION);

				// TODO Exception, input error
				if (associationId == null) {
					throw new ProcessingException(getParser(), filenameXmi,
							"No Edge ID present.");
				}
				ae = idMap.get(associationId);

				if (ae != null) {
					// EdgeClass found

					// TODO s.o.
					if (!(ae instanceof EdgeClass)) {
						throw new ProcessingException(getParser(), filenameXmi,
								"Both types must share the same base class. (Either VertexClass or EdgeClass)");
					}
					ec = correctAggregationAndComposition((EdgeClass) ae,
							aggregation, composition);
				} else {
					// create a preliminary edge class
					ec = composition ? sg.createCompositionClass()
							: aggregation ? sg.createAggregationClass() : sg
									.createEdgeClass();
				}
				preliminaryVertices.add(ec);
				idMap.put(associationId, ec);
			}

			assert (vc != null) && (ec != null);
			e = sg.createFrom(ec, vc);
		} else {
			EdgeClass ec = (EdgeClass) e.getAlpha();
			String id = null;
			for (Entry<String, AttributedElement> idEntry : idMap.entrySet()) {
				if (idEntry.getValue() == ec) {
					id = idEntry.getKey();
					break;
				}
			}

			assert id != null;
			ec = correctAggregationAndComposition(ec, aggregation, composition);
			idMap.put(id, ec);

			// an ownedEnd of an association or an ownedAttribute of a class
			// with a possibly preliminary vertex class
			VertexClass vc = (VertexClass) e.getOmega();
			if (preliminaryVertices.contains(vc)) {
				String typeId = getAttribute(UML_ATTRIBUTE_TYPE);

				// TODO Exception
				if (typeId == null) {
					throw new ProcessingException(getParser(), filenameXmi,
							"No type ID found.");
				}
				AttributedElement ae = idMap.get(typeId);

				if ((ae != null) && !vc.equals(ae)) {

					// TODO s.o.
					if (!(ae instanceof VertexClass)) {
						throw new ProcessingException(getParser(), filenameXmi,
								"Both types must share the same base class. (Either VertexClass or EdgeClass)");
					}
					e.setOmega((VertexClass) ae);
					vc.delete();
					preliminaryVertices.remove(vc);
				} else if (ae == null) {
					idMap.put(typeId, vc);
				} else {
					throw new RuntimeException(
							"FIXME: Unexpected type. You should not get here!");
				}
			}
		}

		assert e != null;
		assert (e instanceof From) || (e instanceof To);
		currentAssociationEnd = e;
		if (aggregation || composition) {
			aggregateEnds.add(e);
		}
		if (currentClass instanceof EdgeClass) {
			ownedEnds.add(e);
		}
		idMap.put(xmiId, e);
		if (e instanceof To) {
			((To) e).set_roleName(endName);
		} else if (e instanceof From) {
			((From) e).set_roleName(endName);
		} else {
			throw new RuntimeException(
					"FIXME! Unexpected type. Should never get here.");
		}
	}

	private EdgeClass correctAggregationAndComposition(EdgeClass ec,
			boolean aggregation, boolean composition) {
		if (composition && (ec.getM1Class() != CompositionClass.class)) {
			EdgeClass cls = sg.createCompositionClass();
			cls.set_qualifiedName(ec.get_qualifiedName());
			cls.set_abstract(ec.is_abstract());
			reconnectEdges(ec, cls);
			ec.delete();
			if (preliminaryVertices.contains(ec)) {
				preliminaryVertices.remove(ec);
				preliminaryVertices.add(cls);
			}
			return cls;
		}
		if (aggregation && (ec.getM1Class() != AggregationClass.class)) {
			EdgeClass cls = sg.createAggregationClass();
			cls.set_qualifiedName(ec.get_qualifiedName());
			cls.set_abstract(ec.is_abstract());
			reconnectEdges(ec, cls);
			ec.delete();
			if (preliminaryVertices.contains(ec)) {
				preliminaryVertices.remove(ec);
				preliminaryVertices.add(cls);
			}
			return cls;
		}
		return ec;
	}

	private void reconnectEdges(Vertex oldVertex, Vertex newVertex) {
		Edge curr = oldVertex.getFirstEdge();
		while (curr != null) {
			Edge next = curr.getNextEdge();
			curr.setThis(newVertex);
			curr = next;
		}
	}

	/**
	 * Creates a Domain vertex corresponding to the specified
	 * <code>typeName</code>.
	 * 
	 * This vertex can also be a preliminary vertex which has to be replaced by
	 * the correct Domain later. In this case, there is no "ContainsDomain"
	 * edge, and the type is "StringDomain".
	 * 
	 * @param typeName
	 * @return
	 */
	private Domain createDomain(String typeName) {
		Domain dom = domainMap.get(typeName);
		if (dom != null) {
			return dom;
		}

		if (typeName.equals("String")) {
			dom = sg.createStringDomain();
		} else if (typeName.equals("Integer")) {
			dom = sg.createIntegerDomain();
		} else if (typeName.equals("Double")) {
			dom = sg.createDoubleDomain();
		} else if (typeName.equals("Long")) {
			dom = sg.createLongDomain();
		} else if (typeName.equals("Boolean")) {
			dom = sg.createBooleanDomain();
		} else if (typeName.startsWith("Map<") && typeName.endsWith(">")) {
			dom = sg.createMapDomain();
			String keyValueDomains = typeName.substring(4,
					typeName.length() - 1);
			char[] c = keyValueDomains.toCharArray();
			// find the delimiting ',' and take into account nested domains
			int p = 0;
			for (int i = 0; i < c.length; ++i) {
				if ((c[i] == ',') && (p == 0)) {
					p = i;
					break;
				}
				if (c[i] == '<') {
					++p;
				} else if (c[i] == '>') {
					--p;
				}

				// TODO Fehler im Typename (z.B. Map< List<bla, blubb>)
				if (p < 0) {
					throw new ProcessingException(filenameXmi,
							"Error in type name '" + typeName
									+ "' of a Domain.");
				}
			}

			// TODO s.o.
			if (p <= 0 || (p >= c.length - 1)) {
				throw new ProcessingException(filenameXmi,
						"Error in type name '" + typeName + "' of a Domain.");
			}
			String keyDomainName = keyValueDomains.substring(0, p);
			Domain keyDomain = createDomain(keyDomainName);
			assert keyDomain != null;

			String valueDomainName = keyValueDomains.substring(p + 1);
			Domain valueDomain = createDomain(valueDomainName);
			assert valueDomain != null;

			sg.createHasKeyDomain((MapDomain) dom, keyDomain);
			sg.createHasValueDomain((MapDomain) dom, valueDomain);

			// Adds a space between
			typeName = "Map<" + keyDomainName + ", " + valueDomainName + '>';

		} else if (typeName.startsWith("List<") && typeName.endsWith(">")) {
			dom = sg.createListDomain();
			String compTypeName = typeName.substring(5, typeName.length() - 1);
			Domain compDomain = createDomain(compTypeName);
			assert compDomain != null;

			sg.createHasBaseDomain((CollectionDomain) dom, compDomain);
		} else if (typeName.startsWith("Set<") && typeName.endsWith(">")) {
			dom = sg.createSetDomain();
			String compTypeName = typeName.substring(4, typeName.length() - 1);
			Domain compDomain = createDomain(compTypeName);
			assert compDomain != null;

			sg.createHasBaseDomain((CollectionDomain) dom, compDomain);
		}
		if (dom != null) {
			sg.createContainsDomain(packageStack.get(0), dom);
		} else {
			// there must exist a named domain (Enum or Record)
			// but this was not yet created in the graph
			// create preliminary domain vertex which will
			// later be re-linked and deleted
			dom = sg.createStringDomain();
			preliminaryVertices.add(dom);
		}

		assert dom != null;
		dom.set_qualifiedName(typeName);
		domainMap.put(typeName, dom);
		return dom;
	}

	/**
	 * Returns the qualified name for the simple name <code>simpleName</code>.
	 * The qualified name consists the (already qualified) name of the package
	 * on top of the package stack and the name <code>simpleName</code>,
	 * separated by a dot. If the top package is the default package, the name
	 * <code>simpleName</code> is already the qualified name. If the package
	 * stack is empty
	 * 
	 * @param simpleName
	 *            a simple name of a class or package
	 * @return the qualified name for the simple name
	 */
	private String getQualifiedName(String simpleName) {

		assert simpleName != null;
		simpleName = simpleName.trim();
		Package p = packageStack.peek();

		assert p != null;
		if (p.get_qualifiedName().equals("")) {
			return simpleName;
		} else {
			return p.get_qualifiedName() + "." + simpleName;
		}
	}

	public void setUseFromRole(boolean useFromRole) {
		this.useFromRole = useFromRole;
	}

	private boolean isUseFromRole() {
		return useFromRole;
	}

	public void setRemoveUnusedDomains(boolean removeUnusedDomains) {
		this.removeUnusedDomains = removeUnusedDomains;
	}

	private boolean isRemoveUnusedDomains() {
		return removeUnusedDomains;
	}

	public void setUseNavigability(boolean useNavigability) {
		this.useNavigability = useNavigability;
	}

	private boolean isUseNavigability() {
		return useNavigability;
	}

	public SchemaGraph getSchemaGraph() {
		return sg;
	}

	public void setSuppressOutput(boolean suppressOutput) {
		this.suppressOutput = suppressOutput;
	}

	public boolean isWriteSchemaGraph() {
		return writeSchemaGraph;
	}

	public void setWriteSchemaGraph(boolean writeSchemaGraph) {
		this.writeSchemaGraph = writeSchemaGraph;
	}

	public boolean isWriteDot() {
		return writeDot;
	}

	public void setWriteDot(boolean writeDebug) {
		this.writeDot = writeDebug;
	}

	public String getFilenameSchema() {
		return filenameSchema;
	}

	public void setFilenameSchema(String filenameSchema) {
		this.filenameSchema = filenameSchema;
	}

	public String getFilenameSchemaGraph() {
		return filenameSchemaGraph;
	}

	public void setFilenameSchemaGraph(String filenameSchemaGraph) {
		this.filenameSchemaGraph = filenameSchemaGraph;
	}

	public String getFilenameXmi() {
		return filenameXmi;
	}

	public String getFilenameDot() {
		return filenameDot;
	}

	public void setFilenameDot(String filenameDot) {
		this.filenameDot = filenameDot;
	}

	public String getFilenameValidation() {
		return filenameValidation;
	}

	public void setFilenameValidation(String filenameValidation) {
		this.filenameValidation = filenameValidation;
	}

	public boolean isValidate() {
		return validate;
	}

	public void setValidate(boolean validate) {
		this.validate = validate;
	}
}
