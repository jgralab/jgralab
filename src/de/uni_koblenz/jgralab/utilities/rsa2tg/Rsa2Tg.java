/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */

package de.uni_koblenz.jgralab.utilities.rsa2tg;

import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.DEFAULT_MAX_MULTIPLICITY;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.DEFAULT_MIN_MULTIPLICITY;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_ANNOTATED_ELEMENT;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_ASSOCIATION;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_ASSOCIATION_CLASS;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_ATTRIBUTE_AGGREGATION;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_ATTRIBUTE_ASSOCIATION;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_ATTRIBUTE_CLASSIFIER;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_ATTRIBUTE_CLIENT;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_ATTRIBUTE_CONSTRAINED_ELEMENT;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_ATTRIBUTE_GENERAL;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_ATTRIBUTE_HREF;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_ATTRIBUTE_ISDERIVED;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_ATTRIBUTE_IS_ABSRACT;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_ATTRIBUTE_KEY;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_ATTRIBUTE_NAME;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_ATTRIBUTE_SUPPLIER;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_ATTRIBUTE_TYPE;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_ATTRIBUTE_VALUE;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_BODY;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_CLASS;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_COMPOSITE;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_DEFAULT_VALUE;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_DETAILS;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_ENUMERATION;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_ENUMERATION_LITERAL;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_E_ANNOTATIONS;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_GENERALIZATION;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_INSTANCE_VALUE;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_LANGUAGE;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_LITERAL_BOOLEAN;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_LITERAL_INTEGER;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_LITERAL_STRING;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_LOWER_VALUE;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_MEMBER_END;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_MODEL;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_OPAQUE_EXPRESSION;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_OWNEDEND;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_OWNEDRULE;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_OWNED_ATTRIBUTE;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_OWNED_COMMENT;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_OWNED_LITERAL;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_PACKAGE;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_PACKAGED_ELEMENT;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_PRIMITIVE_TYPE;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_PROPERTY;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_REALIZATION;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_SHARED;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_SPECIFICATION;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_TRUE;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.UML_UPPER_VALUE;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.XMI_EXTENSION;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.XMI_NAMESPACE_PREFIX;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.XMI_TYPE;
import static de.uni_koblenz.jgralab.utilities.rsa2tg.XMIConstants.XMI_XMI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.pcollections.PVector;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.ist.utilities.xml.XmlProcessor;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
import de.uni_koblenz.jgralab.graphvalidator.ConstraintViolation;
import de.uni_koblenz.jgralab.graphvalidator.GraphValidator;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.grumlschema.GrumlSchema;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.grumlschema.domains.CollectionDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.Domain;
import de.uni_koblenz.jgralab.grumlschema.domains.EnumDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.HasRecordDomainComponent;
import de.uni_koblenz.jgralab.grumlschema.domains.MapDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.RecordDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.StringDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.AggregationKind;
import de.uni_koblenz.jgralab.grumlschema.structure.Annotates;
import de.uni_koblenz.jgralab.grumlschema.structure.Attribute;
import de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.ComesFrom;
import de.uni_koblenz.jgralab.grumlschema.structure.Comment;
import de.uni_koblenz.jgralab.grumlschema.structure.Constraint;
import de.uni_koblenz.jgralab.grumlschema.structure.ContainsGraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.EdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.EndsAt;
import de.uni_koblenz.jgralab.grumlschema.structure.GoesTo;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.HasAttribute;
import de.uni_koblenz.jgralab.grumlschema.structure.HasDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.IncidenceClass;
import de.uni_koblenz.jgralab.grumlschema.structure.NamedElement;
import de.uni_koblenz.jgralab.grumlschema.structure.Package;
import de.uni_koblenz.jgralab.grumlschema.structure.Schema;
import de.uni_koblenz.jgralab.grumlschema.structure.SpecializesEdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.SpecializesVertexClass;
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
 */
public class Rsa2Tg extends XmlProcessor {

	private static final String OPTION_FILENAME_VALIDATION = "r";

	private static final String OPTION_FILENAME_SCHEMA_GRAPH = "s";

	private static final String OPTION_FILENAME_DOT = "e";

	private static final String OPTION_FILENAME_SCHEMA = "o";

	private static final String OPTION_USE_NAVIGABILITY = "n";

	private static final String OPTION_REMOVE_UNUSED_DOMAINS = "u";

	private static final String OPTION_KEEP_EMPTY_PACKAGES = "k";

	private static final String OPTION_USE_ROLE_NAME = "f";

	private static final String OPTION_REMOVE_COMMENTS = "c";

	private static final String OPTION_IGNORE_UNKNOWN_STEREOTYPES = "c";

	/**
	 * Contains XML element names in the format "name>xmiId"
	 */
	private Stack<String> xmiIdStack;

	/**
	 * The schema graph.
	 */
	private SchemaGraph sg;

	/**
	 * The {@link Schema} vertex of the schema graph.
	 */
	private Schema schema;

	/**
	 * The {@link GraphClass} vertex of the schema graph.
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
	private Map<String, Vertex> idMap;

	/**
	 * Remembers the current class id for processing of nested elements.
	 */
	private String currentClassId;

	/**
	 * Remembers the current {@link VertexClass}/{@link EdgeClass} vertex for
	 * processing of nested elements.
	 */
	private AttributedElementClass currentClass;

	/**
	 * Remembers the current {@link RecordDomain} vertex for processing of
	 * nested elements.
	 */
	private RecordDomain currentRecordDomain;

	/**
	 * Remembers the current domain component edge for processing of nested
	 * elements.
	 */
	private HasRecordDomainComponent currentRecordDomainComponent;

	/**
	 * Remembers the current {@link Attribute} vertex for processing of nested
	 * elements.
	 */
	private Attribute currentAttribute;

	/**
	 * Marks {@link VertexClass} and {@link EdgeClass} vertices with a set of
	 * XMI Ids of superclasses.
	 */
	private GraphMarker<Set<String>> generalizations;

	/**
	 * Keeps track of 'uml:Realization's (key = client id, value = set of
	 * supplier ids) as workaround for missing generalizations between
	 * association and association class.
	 */
	private Map<String, Set<String>> realizations;

	/**
	 * Marks {@link Attribute} vertices with the XMI Id of its type if the type
	 * can not be resolved at the time the Attribute is processed.
	 */
	private GraphMarker<String> attributeType;

	/**
	 * Marks {@link HasRecordDomainComponent} edges with the XMI Id of its type
	 * if the type can not be resolved at the time the component is processed.
	 */
	private GraphMarker<String> recordComponentType;

	/**
	 * Maps qualified names of domains to the corresponding {@link Domain}
	 * vertex.
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
	 * Remembers the current association end edge ({@link To}/{@link From}
	 * edge), which can be an ownedEnd or an ownedAttribute, for processing of
	 * nested elements.
	 */
	private IncidenceClass currentAssociationEnd;

	/**
	 * The Set of {@link To}/{@link From} edges, which are represented by
	 * ownedEnd elements (used to determine the direction of edges).
	 */
	private Set<IncidenceClass> ownedEnds;

	/**
	 * True if currently processing a constraint (ownedRule) element.
	 */
	private boolean inConstraint;

	/**
	 * The XMI Id of the constrained element if the constraint has exactly one
	 * constrained element, null otherwise. If set to null, the constraint will
	 * be attached to the {@link GraphClass} vertex.
	 */
	private String constrainedElementId;

	/**
	 * Maps the XMI Id of constrained elements to the list of constraints.
	 * Constrains are the character data inside a body element of ownedRule
	 * elements.
	 */
	private Map<String, List<String>> constraints;

	/**
	 * Maps the XMI Id of commented elements to the list of comments.
	 */
	private Map<String, List<String>> comments;

	/**
	 * When creating {@link EdgeClass} names, also use the role name of the
	 * 'from' end.
	 */
	private boolean useFromRole;

	/**
	 * After processing is complete, remove {@link Domain} vertices which are
	 * not used by an attribute or by a record domain component.
	 */
	private boolean removeUnusedDomains;

	private boolean removeComments;

	/**
	 * After processing is complete, also keep {@link Package} vertices which
	 * contain no {@link Domains} and no {@link GraphElementClass}es.
	 */
	private boolean keepEmptyPackages;

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
	 * Filename for the {@link Schema}.
	 */
	private String filenameSchema;

	/**
	 * Filename for the {@link SchemaGraph};
	 */
	private String filenameSchemaGraph;

	/**
	 * Filename for dot.
	 */
	private String filenameDot;

	private boolean ignoreUnknownStereotypes;

	/**
	 * Filename for validation
	 */
	private String filenameValidation;

	private String annotatedElementId;

	private boolean inComment;

	private boolean inOwnedAttribute;

	private GreqlQuery edgeClassAcyclicQuery;
	private GreqlQuery vertexClassAcyclicQuery;

	private boolean inDefaultValue;

	private int modelRootElementNestingDepth;

	private Set<Package> ignoredPackages;

	private boolean inSpecification;

	private Set<GraphElementClass> derivedGraphElementClasses;

	/**
	 * Processes an XMI-file to a TG-file as schema or a schema in a grUML
	 * graph. For all command line options see
	 * {@link Rsa2Tg#processCommandLineOptions(String[])}.
	 * 
	 * @param args
	 *            {@link String} array of command line options.
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		System.out.println("RSA to TG");
		System.out.println("=========");
		JGraLab.setLogLevel(Level.OFF);

		// Retrieving all command line options
		CommandLine cli = processCommandLineOptions(args);

		assert cli != null : "No CommandLine object has been generated!";
		// All XMI input files
		File input = new File(cli.getOptionValue('i'));

		Rsa2Tg r = new Rsa2Tg();

		r.setUseFromRole(cli.hasOption(OPTION_USE_ROLE_NAME));
		r.setRemoveUnusedDomains(cli.hasOption(OPTION_REMOVE_UNUSED_DOMAINS));
		r.setKeepEmptyPackages(cli.hasOption(OPTION_KEEP_EMPTY_PACKAGES));
		r.setUseNavigability(cli.hasOption(OPTION_USE_NAVIGABILITY));
		r.setRemoveComments(cli.hasOption(OPTION_REMOVE_COMMENTS));
		r.setIgnoreUnknownStereotypes(cli
				.hasOption(OPTION_IGNORE_UNKNOWN_STEREOTYPES));

		// apply options
		r.setFilenameSchema(cli.getOptionValue(OPTION_FILENAME_SCHEMA));
		r.setFilenameSchemaGraph(cli
				.getOptionValue(OPTION_FILENAME_SCHEMA_GRAPH));
		r.setFilenameDot(cli.getOptionValue(OPTION_FILENAME_DOT));
		r.setFilenameValidation(cli.getOptionValue(OPTION_FILENAME_VALIDATION));

		// If no output option is selected, Rsa2Tg will write at least the
		// schema file.
		boolean noOutputOptionSelected = !cli.hasOption(OPTION_FILENAME_SCHEMA)
				&& !cli.hasOption(OPTION_FILENAME_SCHEMA_GRAPH)
				&& !cli.hasOption(OPTION_FILENAME_DOT)
				&& !cli.hasOption(OPTION_FILENAME_VALIDATION);
		if (noOutputOptionSelected) {
			System.out.println("No output option has been selected. "
					+ "A TG-file for the Schema will be written.");

			// filename have to be set
			r.setFilenameSchema(createFilename(input));
		}

		try {
			System.out.println("processing: " + input.getPath() + "\n");
			r.process(input.getPath());
		} catch (Exception e) {
			System.err.println("An Exception occured while processing " + input
					+ ".");
			System.err.println(e.getMessage());
			e.printStackTrace();
		}

		System.out.println("Fini.");
	}

	public void setRemoveComments(boolean removeComments) {
		this.removeComments = removeComments;
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
		String toolString = "java " + Rsa2Tg.class.getName();
		String versionString = JGraLab.getInfo(false);

		// TODO Add an additional help string to the help page.
		// This String needs to be included into the OptionHandler, but
		// the functionality is not present.

		// String additional =
		// "If no optional output option is selected, a file with the name "
		// + "\"<InputFileName>.rsa.tg\" will be written."
		// + "\n\n"
		// + toolString;

		OptionHandler oh = new OptionHandler(toolString, versionString);

		// Several Options are declared.
		Option validate = new Option(
				OPTION_FILENAME_VALIDATION,
				"report",
				true,
				"(optional): writes a validation report to the given filename. "
						+ "Free naming, but should look like this: '<filename>.html'");
		validate.setRequired(false);
		validate.setArgName("filename");
		oh.addOption(validate);

		Option export = new Option(
				OPTION_FILENAME_DOT,
				"export",
				true,
				"(optional): writes a GraphViz DOT file to the given filename. "
						+ "Free naming, but should look like this: '<filename>.dot'");
		export.setRequired(false);
		export.setArgName("filename");
		oh.addOption(export);

		Option schemaGraph = new Option(
				OPTION_FILENAME_SCHEMA_GRAPH,
				"schemaGraph",
				true,
				"(optional): writes a TG-file of the Schema as graph instance to the given filename. "
						+ "Free naming, but should look like this:  '<filename>.tg'");
		schemaGraph.setRequired(false);
		schemaGraph.setArgName("filename");
		oh.addOption(schemaGraph);

		Option input = new Option("i", "input", true,
				"(required): UML 2.1-XMI exchange model file of the Schema.");
		input.setRequired(true);
		input.setArgName("filename");
		oh.addOption(input);

		Option output = new Option(
				OPTION_FILENAME_SCHEMA,
				"output",
				true,
				"(optional): writes a TG-file of the Schema to the given filename. "
						+ "Free naming, but should look like this: '<filename>.rsa.tg.'");
		output.setRequired(false);
		output.setArgName("filename");
		oh.addOption(output);

		Option fromRole = new Option(
				OPTION_USE_ROLE_NAME,
				"useFromRole",
				false,
				"(optional): if this flag is set, the name of from roles will be used for creating undefined EdgeClass names.");
		fromRole.setRequired(false);
		oh.addOption(fromRole);

		Option unusedDomains = new Option(OPTION_REMOVE_UNUSED_DOMAINS,
				"removeUnusedDomains", false,
				"(optional): if this flag is set, all unused domains be deleted.");
		unusedDomains.setRequired(false);
		oh.addOption(unusedDomains);

		Option removeComments = new Option(OPTION_REMOVE_COMMENTS,
				"removeComments", false,
				"(optional): if this flag is set, all comments are removed.");
		removeComments.setRequired(false);
		oh.addOption(removeComments);

		Option emptyPackages = new Option(OPTION_KEEP_EMPTY_PACKAGES,
				"keepEmptyPackages", false,
				"(optional): if this flag is set, empty packages will be retained.");
		emptyPackages.setRequired(false);
		oh.addOption(emptyPackages);

		Option navigability = new Option(
				OPTION_USE_NAVIGABILITY,
				"useNavigability",
				false,
				"(optional): if this flag is set, navigability information will be interpreted as reading direction.");
		navigability.setRequired(false);
		oh.addOption(navigability);

		Option ignoreStereotypes = new Option(
				OPTION_IGNORE_UNKNOWN_STEREOTYPES, "ignoreUnknownStereotypes",
				false,
				"(optional): if this flag is set, unknown stereotypes are ignored.");
		ignoreStereotypes.setRequired(false);
		oh.addOption(ignoreStereotypes);

		// Parses the given command line parameters with all created Option.
		return oh.parse(args);
	}

	/**
	 * Creates a file path similar to the of <code>inputFile</code>, but with
	 * the file extension '.rsa.tg'.
	 * 
	 * @param file
	 *            Is a File object, which is path used to created the new Path.
	 * @return New generated Path with the extension '.rsa.tg'.
	 */
	public static String createFilename(File file) {
		StringBuilder filenameBuilder = new StringBuilder();

		// The path of the input XMI-file is used.
		filenameBuilder.append(file.getParent());
		filenameBuilder.append(File.separatorChar);

		String filename = file.getName();
		int periodePosition = filename.lastIndexOf('.');
		if (periodePosition != -1) {
			filename = filename.substring(0, periodePosition);
		}

		// The simple name of the Schema will be the filename.
		// filenameBuilder.append(r.getSchemaGraph().getFirstSchema()
		// .get_name());
		filenameBuilder.append(filename);
		// The extension is ....
		filenameBuilder.append(".rsa.tg");
		return filenameBuilder.toString();
	}

	/**
	 * Creates a Rsa2Tg converter.
	 */
	public Rsa2Tg() {
		// Sets all names of XML-elements, which should be ignored.
		addIgnoredElements("profileApplication", "packageImport",
				"Ecore:EReference");
	}

	/**
	 * Sets up several a {@link SchemaGraph} and data structures before the
	 * processing can start.
	 */
	@Override
	public void startDocument() {

		sg = GrumlSchema.instance().createSchemaGraph(
				ImplementationType.STANDARD);

		// Initializing all necessary data structures for processing purposes.
		xmiIdStack = new Stack<>();
		idMap = new HashMap<>();
		packageStack = new Stack<>();
		generalizations = new GraphMarker<>(sg);
		realizations = new HashMap<>();
		attributeType = new GraphMarker<>(sg);
		recordComponentType = new GraphMarker<>(sg);
		domainMap = new HashMap<>();
		preliminaryVertices = new HashSet<>();
		ownedEnds = new HashSet<>();
		constraints = new HashMap<>();
		comments = new HashMap<>();
		ignoredPackages = new HashSet<>();
		modelRootElementNestingDepth = 1;
		derivedGraphElementClasses = new HashSet<>();
	}

	/**
	 * Processes a XML element and decides how to handle it in order to get a
	 * {@link Schema} element.
	 * 
	 * @throws XMLStreamException
	 */
	@Override
	protected void startElement(String name) throws XMLStreamException {

		// TODO Comment the Meaning of this action. Does '1' or '2' have a
		// meaning?
		if ((getNestingDepth() == 1) && name.equals(XMI_XMI)) {
			modelRootElementNestingDepth = 2;
			return;
		}

		String xmiId = getAttribute(XMI_NAMESPACE_PREFIX, "id");
		xmiIdStack.push(xmiId);

		Vertex vertexId = null;
		if (getNestingDepth() == modelRootElementNestingDepth) {
			// In case of a root element
			vertexId = createDefaultElements(name);
		} else {
			vertexId = processXMIElements(name, xmiId);
		}

		// Links an existing XMI-id to a Vertex-id
		if ((xmiId != null) && (vertexId != null)) {
			idMap.put(xmiId, vertexId);
		}
	}

	private Vertex createDefaultElements(String name) throws XMLStreamException {
		if (name.equals(UML_MODEL) || name.equals(UML_PACKAGE)) {
			setSchemaQualifiedName();
			createGraphClass();
			createDefaultPackage();
		} else {
			// Unexpected root element
			throw new ProcessingException(getParser(), getFilename(),
					"Root element must be " + UML_MODEL + " or " + UML_PACKAGE
							+ ", buf was " + name);
		}
		return schema;
	}

	private void setSchemaQualifiedName() throws XMLStreamException {
		// Gets the Schema name, creates a Schema and processes it.
		String nm = getAttribute(UML_ATTRIBUTE_NAME);

		int p = nm.lastIndexOf('.');
		schema = sg.createSchema();

		// In case nm (:= Schema-name) contains only a name and not a
		// package prefix
		if (p == -1) {
			throw new ProcessingException(getParser(), getFilename(),
					"A Schema must have a package prefix!\nProcessed qualified name: "
							+ nm);
		}

		schema.set_packagePrefix(nm.substring(0, p));
		schema.set_name(nm.substring(p + 1));
	}

	private void createGraphClass() {
		// Generates a GraphClass and links it with the created Schema
		graphClass = sg.createGraphClass();
		sg.createDefinesGraphClass(schema, graphClass);
	}

	private void createDefaultPackage() {
		// Creates a default Package, links it and pushes it to the
		// packageStack.
		Package defaultPackage = sg.createPackage();
		defaultPackage.set_qualifiedName("");
		sg.createContainsDefaultPackage(schema, defaultPackage);
		packageStack.push(defaultPackage);
	}

	private Vertex processXMIElements(String name, String xmiId)
			throws XMLStreamException {
		// inside top level element

		// Type is retrieved
		String type = getAttribute(XMI_NAMESPACE_PREFIX, UML_ATTRIBUTE_TYPE);
		Vertex vertexId = null;
		// Package element, which
		if (name.equals(UML_PACKAGED_ELEMENT)) {
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
				vertexId = handlePrimitiveType(xmiId);
			} else if (type.equals(UML_REALIZATION)) {
				handleRealization();
			} else {
				throw new ProcessingException(getParser(), getFilename(),
						createUnexpectedElementMessage(name, type));
			}

		} else if (name.equals(UML_OWNEDRULE)) {
			// Owned rule
			inConstraint = true;
			constrainedElementId = getAttribute(UML_ATTRIBUTE_CONSTRAINED_ELEMENT);
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
		} else if (name.equals(UML_BODY)) {
			if (!inConstraint && !inComment && !inDefaultValue
					&& !inSpecification) {
				// Throw an error for body elements, which aren't
				// contained in a constraint or comment
				throw new ProcessingException(getParser(), getFilename(),
						createUnexpectedElementMessage(name, null));
			}
		} else if (name.equals(UML_SPECIFICATION)) {
			// Specification is ignored for most elements
			inSpecification = true;
		} else if (name.equals(UML_LANGUAGE)) {
			if (!inConstraint) {
				// Throw an error for specification elements, which aren't
				// contained in a constraint.
				throw new ProcessingException(getParser(), getFilename(),
						createUnexpectedElementMessage(name, null));
			}
		} else if (name.equals(UML_OWNEDEND)) {
			// Owned end marks the end of the current class, which should be
			// an edgeClasss.
			if (type.equals(UML_PROPERTY)
					&& (currentClass instanceof EdgeClass)) {
				handleAssociationEnd(xmiId);
			} else {
				throw new ProcessingException(getParser(), getFilename(),
						createUnexpectedElementMessage(name, type));
			}

		} else if (name.equals(UML_OWNED_ATTRIBUTE)) {
			inOwnedAttribute = true;
			// Handles the attributes of the current element
			if (type.equals(UML_PROPERTY)) {
				handleOwnedAttribute(xmiId);
			} else {
				throw new ProcessingException(getParser(), getFilename(),
						createUnexpectedElementMessage(name, type));
			}

		} else if (name.equals(UML_ATTRIBUTE_TYPE)) {
			// Handles the type of the current attribute, which should be a
			// primitive type.
			if (!inDefaultValue) {
				if (type.equals(UML_PRIMITIVE_TYPE)) {
					handleNestedTypeElement(xmiId);
				} else {
					throw new ProcessingException(getParser(), getFilename(),
							createUnexpectedElementMessage(name, type));
				}
			}
		} else if (name.equals(UML_OWNED_LITERAL)) {
			// Handles the literal of the current enumeration.
			if (type.equals(UML_ENUMERATION_LITERAL)) {
				handleEnumerationLiteral();
			} else {
				throw new ProcessingException(getParser(), getFilename(),
						createUnexpectedElementMessage(name, type));
			}
		} else if (name.equals(XMI_EXTENSION)) {
			// ignore
		} else if (name.equals(UML_E_ANNOTATIONS)) {
			// ignore
		} else if (name.equals(UML_GENERALIZATION)) {
			handleGeneralization();
		} else if (name.equals(UML_DETAILS)) {
			handleStereotype();
		} else if (name.equals(UML_LOWER_VALUE)) {
			handleLowerValue();
		} else if (name.equals(UML_UPPER_VALUE)) {
			handleUpperValue();
		} else if (name.equals(UML_OWNED_COMMENT)) {
			annotatedElementId = getAttribute(UML_ANNOTATED_ELEMENT);
			inComment = true;
		} else if (name.equals(UML_DEFAULT_VALUE)) {
			String xmiType = getAttribute(XMI_NAMESPACE_PREFIX, XMI_TYPE);
			if (isPrimitiveDefaultValue(xmiType)) {
				// boolean, integer, string, or enumeration value
				handlePrimitiveDefaultValue(xmiId, xmiType);
			} else if (!xmiType.equals(UML_OPAQUE_EXPRESSION)) {
				System.out.println("Warning: Unexpected default value type '"
						+ xmiType + "' for attribute '"
						+ currentAttribute.get_name() + "' of "
						+ currentClass.getSchemaClass().getSimpleName() + " '"
						+ currentClass.get_qualifiedName() + "' in file '"
						+ getFilename() + "' at line "
						+ getParser().getLocation().getLineNumber());
				// throw new ProcessingException(getParser(), getFileName(),
				// "Unexpected default value type " + xmiType
				// + " for attribute '"
				// + currentAttribute.get_name() + "' of "
				// + currentClass.getSchemaClass().getSimpleName()
				// + " '" + currentClass.get_qualifiedName() + "'");
			}
			inDefaultValue = true;
		} else {
			// for unexpected XMI tags
			throw new ProcessingException(getParser(), getFilename(),
					createUnexpectedElementMessage(name, type));
		}
		return vertexId;
	}

	private boolean isPrimitiveDefaultValue(String xmiType) {
		return xmiType.equals(UML_LITERAL_STRING)
				|| xmiType.equals(UML_LITERAL_INTEGER)
				|| xmiType.equals(UML_LITERAL_BOOLEAN)
				|| xmiType.equals(UML_INSTANCE_VALUE);
	}

	private void handlePrimitiveDefaultValue(String xmiId, String xmiType)
			throws XMLStreamException {
		if (xmiType.equals(UML_INSTANCE_VALUE)) {
			String value = getAttribute(UML_ATTRIBUTE_NAME);
			handleDefaultValue(xmiId, value);
			return;
		}

		String value = getAttribute(UML_ATTRIBUTE_VALUE);
		if (xmiType.equals(UML_LITERAL_BOOLEAN)) {
			if (value == null) {
				value = "f"; // XML schema default
			} else {
				assert value.equals("true") || value.equals("false");
				// true/false => t/f
				value = value.substring(0, 1);
			}
			handleDefaultValue(xmiId, value);
			return;
		} else if (xmiType.equals(UML_LITERAL_INTEGER)) {
			if (value == null) {
				value = "0"; // XML schema default
			}
			handleDefaultValue(xmiId, value);
			return;
		}

		if (value == null) {
			System.out
					.println("Warning: Undefined default value for attribute '"
							+ currentAttribute.get_name() + "' of "
							+ currentClass.getSchemaClass().getSimpleName()
							+ " '" + currentClass.get_qualifiedName()
							+ "' in file '" + getFilename() + "' at line "
							+ getParser().getLocation().getLineNumber());
			return;
		}

		if (xmiType.equals(UML_LITERAL_STRING)) {
			value = "\"" + value + "\"";
			handleDefaultValue(xmiId, value);
		} else {
			System.out.println("Warning: Undefined default value type '"
					+ xmiType + "' for attribute '"
					+ currentAttribute.get_name() + "' of "
					+ currentClass.getSchemaClass().getSimpleName() + " '"
					+ currentClass.get_qualifiedName() + "' in file '"
					+ getFilename() + "' at line "
					+ getParser().getLocation().getLineNumber());
		}
	}

	private void handleDefaultValue(String xmiId, String value) {
		if (currentAttribute == null) {
			throw new ProcessingException(
					getParser(),
					getFilename(),
					"Found a <defaultValue> tag (XMI id "
							+ xmiId
							+ ") outside an attribute definition (e.g. in a <<record>> class)");
		}
		currentAttribute.set_defaultValue(value);
	}

	/**
	 * Processes a XML end element tags in order to set internal states.
	 * 
	 * @param name
	 *            Name of the XML element, which will be closed.
	 * @param content
	 *            StringBuilder object, which holds the contents of the current
	 *            end element.
	 * 
	 * @throws XMLStreamException
	 */
	@Override
	protected void endElement(String name, StringBuilder content)
			throws XMLStreamException {
		if (getNestingDepth() < modelRootElementNestingDepth) {
			return;
		}

		String xmiId = xmiIdStack.pop();

		if (name.equals(UML_BODY)) {
			if (inConstraint) {
				assert !inComment && !inDefaultValue;
				handleConstraint(content.toString().trim().replace("\\s+", " "));
			} else if (inComment) {
				assert !inDefaultValue;
				handleComment(content.toString());
			} else if (inDefaultValue) {
				handleDefaultValue(xmiId, content.toString().trim());
			}
		}
		AttributedElement<?, ?> elem = idMap.get(xmiId);
		if (elem != null) {
			if (elem instanceof Package) {

				// There should be at least one package element in the
				// stack.
				if (packageStack.size() <= 1) {
					throw new ProcessingException(getParser(), getFilename(),
							"XMI file is malformed. There is probably one end element to much.");
				}
				packageStack.pop();
			} else if (elem instanceof AttributedElementClass) {
				currentClassId = null;
				currentClass = null;
				currentAttribute = null;
			} else if (elem instanceof RecordDomain) {
				currentRecordDomain = null;
				currentAttribute = null;
			} else if (elem instanceof Attribute) {
				currentAttribute = null;
			}
		}
		if (name.equals(UML_PACKAGE)) {
			packageStack.pop();

			// There should be no packages left over.
			if (packageStack.size() != 0) {
				throw new ProcessingException(getParser(), getFilename(),
						"XMI file is malformed. There is probably one end element to much.");
			}
		} else if (name.equals(UML_OWNED_ATTRIBUTE)) {
			currentRecordDomainComponent = null;
			if (currentAssociationEnd != null) {
				checkMultiplicities(currentAssociationEnd);
				currentAssociationEnd = null;
			}
			inOwnedAttribute = false;
		} else if (name.equals(UML_OWNEDEND)) {
			checkMultiplicities(currentAssociationEnd);
			currentAssociationEnd = null;
		} else if (name.equals(UML_OWNEDRULE)) {
			inConstraint = false;
			constrainedElementId = null;
		} else if (name.equals(UML_OWNED_COMMENT)) {
			inComment = false;
			annotatedElementId = null;
		} else if (name.equals(UML_DEFAULT_VALUE)) {
			inDefaultValue = false;
		} else if (name.equals(UML_SPECIFICATION)) {
			inSpecification = false;
		}
	}

	private void checkMultiplicities(IncidenceClass inc) {
		int min = inc.get_min();
		int max = inc.get_max();
		assert min >= 0;
		assert max > 0;
		if (min == Integer.MAX_VALUE) {
			throw new ProcessingException(getFilename(),
					"Error in multiplicities: lower bound must not be *"
							+ " at association end " + inc);
		}
		if (min > max) {
			throw new ProcessingException(getFilename(),
					"Error in multiplicities: lower bound (" + min
							+ ") must be <= upper bound (" + max
							+ ") at association end " + inc);
		}
	}

	private void handleComment(String body) {
		// decode RSA's clumsy HTML-like comments...
		body = body.replace("<p>", " ");
		body = body.replace("</p>", "\n");
		body = body.replace("<br>", "\n");
		// RSA uses \u00a0 (NO-BREAK SPACE) sometimes. Use a normal space
		// instead.
		body = body.replace('\u00a0', ' ');
		String[] lines = body.split("\n");
		StringBuilder text = new StringBuilder();
		for (String line : lines) {
			line = line.replaceAll("\\s+", " ").trim();
			if (line.length() > 0) {
				if (text.length() > 0) {
					text.append("\n");
				}
				text.append(line);
			}
		}
		if (text.length() == 0) {
			return;
		}
		List<String> commentList = comments.get(annotatedElementId);
		if (commentList == null) {
			commentList = new LinkedList<>();
			comments.put(annotatedElementId, commentList);
		}
		commentList.add(text.toString());
	}

	/**
	 * Finalizes the created {@link SchemaGraph} by creating missing links
	 * between several objects.
	 * 
	 * @throws XMLStreamException
	 * @throws GraphIOException
	 */
	@Override
	public void endDocument() throws XMLStreamException {
		// finalizes processing by creating missing links
		assert schema != null;
		assert graphClass != null;
		// The qualified name of the GraphClass should be set.
		if (graphClass.get_qualifiedName() == null) {
			throw new ProcessingException(getParser(), getFilename(),
					"No <<graphclass>> defined in schema '"
							+ schema.get_packagePrefix() + "."
							+ schema.get_name() + "'");
		}

		// Checks whether each enum domain has at least one literal
		checkEnumDomains();

		// Now the RSA XMI file has been processed, pending actions to link
		// elements can be performed
		linkGeneralizations();
		linkRecordDomainComponents();
		linkAttributeDomains();

		removeIgnoredPackages();

		if (isUseNavigability()) {
			correctEdgeDirection();
		}

		// the following depends on correct subsets relations between incidences
		attachConstraints();

		// the following depends on correct edge directions and edgeclass
		// generalizations
		checkSubsettingOfAllIncidenceClasses();

		// delete derived GraphElementClasses
		for (GraphElementClass gec : derivedGraphElementClasses) {
			assert gec.isValid();
			Edge current = gec.getFirstIncidence();
			while (current != null) {
				Edge next = current.getNextIncidence();
				if (current.isInstanceOf(Annotates.EC)) {
					Vertex comment = next.getThat();
					if (comment.getDegree() == 1) {
						comment.delete();
					}
				}
				current = next;
			}
			gec.delete();
		}

		createEdgeClassNames();

		checkAttributes();

		if (isRemoveUnusedDomains()) {
			removeUnusedDomains();
		}

		if (!isRemoveComments()) {
			attachComments();
		}

		if (!isKeepEmptyPackages()) {
			removeEmptyPackages();
		}

		// preliminaryVertices must be empty at this time of processing,
		// otherwise there is an error...
		if (!preliminaryVertices.isEmpty()) {
			System.err.println("Remaining preliminary vertices ("
					+ preliminaryVertices.size() + "):");
			for (Vertex v : preliminaryVertices) {
				System.err.println(attributedElement2String(v));
			}
			throw new ProcessingException(getFilename(),
					"There are still vertices left over. ");
		}

		if (!suppressOutput) {
			try {
				writeOutput();
			} catch (GraphIOException e) {
				throw new XMLStreamException(e);
			}
		}
	}

	private boolean isRemoveComments() {
		return removeComments;
	}

	private void checkAttributes() {
		GraphClass graphClass = sg.getFirstGraphClass();
		Map<String, AttributedElementClass> definedAttributes = new HashMap<>();
		for (Attribute a : graphClass.get_attributes()) {
			if (definedAttributes.containsKey(a.get_name())) {
				throw new RuntimeException("Attribute " + a.get_name() + " at "
						+ graphClass.get_qualifiedName() + " is duplicate.");
			}
			definedAttributes.put(a.get_name(), graphClass);
		}

		for (GraphElementClass gec : sg.getGraphElementClassVertices()) {
			boolean isVertexClass = gec.isInstanceOf(VertexClass.VC);
			definedAttributes = new HashMap<>();
			BooleanGraphMarker alreadyChecked = new BooleanGraphMarker(sg);
			Queue<GraphElementClass> queue = new LinkedList<>();
			queue.add(gec);
			while (!queue.isEmpty()) {
				GraphElementClass current = queue.poll();
				if (alreadyChecked.isMarked(current)) {
					continue;
				}
				for (Attribute att : current.get_attributes()) {
					if (definedAttributes.containsKey(att.get_name())) {
						AttributedElementClass childClass = definedAttributes
								.get(att.get_name());
						throw new RuntimeException(
								"The name of the "
										+ ((childClass == gec)
												&& (current != gec) ? ""
												: "inherited ")
										+ "attribute "
										+ att.get_name()
										+ " of "
										+ (isVertexClass ? "VertexClass"
												: "EdgeClass")
										+ " "
										+ childClass.get_qualifiedName()
										+ (current == gec ? " is duplicate"
												: (" is the same name as the inherited attribute of "
														+ (isVertexClass ? "VertexClass"
																: "EdgeClass")
														+ " " + current
														.get_qualifiedName()))
										+ ".");
					} else {
						definedAttributes.put(att.get_name(), current);
					}
				}
				alreadyChecked.mark(current);
				for (Edge toSuperClass : current.incidences(
						isVertexClass ? SpecializesVertexClass.EC
								: SpecializesEdgeClass.EC, EdgeDirection.OUT)) {
					GraphElementClass superClass = (GraphElementClass) toSuperClass
							.getThat();
					if (!alreadyChecked.isMarked(superClass)) {
						queue.add(superClass);
					}
				}
			}
		}
	}

	/**
	 * Removes all GraphElementClasses in ignored Packages from the schema
	 * graph.
	 */
	private void removeIgnoredPackages() {
		System.out.println("Removing ignored packages...");
		int n = 0;
		for (Package pkg : ignoredPackages) {
			n += removePackage(pkg);
		}
		System.out.println("\tRemoved " + n + " package" + (n == 1 ? "" : "s")
				+ ".");
	}

	/**
	 * Removes the GraphElementClasses in the Package <code>pkg</code> from the
	 * schema graph, including subpackages.
	 * 
	 * @param pkg
	 *            a Package
	 */
	private int removePackage(Package pkg) {
		if (!pkg.isValid()) {
			// possibly alread deleted
			return 0;
		}
		int n = 0;
		// recursively descend into subpackages
		List<Package> subPackages = new ArrayList<>();
		for (Package sub : pkg.get_subpackages()) {
			subPackages.add(sub);
		}
		for (Package sub : subPackages) {
			n += removePackage(sub);
		}

		// remove all GraphElementClasses
		for (ContainsGraphElementClass c = pkg
				.getFirstContainsGraphElementClassIncidence(EdgeDirection.OUT); c != null; c = pkg
				.getFirstContainsGraphElementClassIncidence(EdgeDirection.OUT)) {
			GraphElementClass gec = (GraphElementClass) c.getThat();

			if (gec instanceof EdgeClass) {
				// in case of an EdgeClass, also remove IncidenceClasses
				EdgeClass ec = (EdgeClass) gec;
				ec.get_to().delete();
				ec.get_from().delete();
			} else if (gec instanceof VertexClass) {
				// in case of an EdgeClass, also remove incident EdgeClasses
				VertexClass vc = (VertexClass) gec;
				for (EndsAt e = vc.getFirstEndsAtIncidence(EdgeDirection.IN); e != null; e = vc
						.getFirstEndsAtIncidence(EdgeDirection.IN)) {
					EdgeClass ec;
					// the EdgeClass can be either outgoing (ComesFrom) or
					// ingoing (GoesTo) to this VertexClass
					ComesFrom cf = ((IncidenceClass) e.getThat())
							.getFirstComesFromIncidence();
					if (cf == null) {
						GoesTo gt = ((IncidenceClass) e.getThat())
								.getFirstGoesToIncidence();
						ec = (EdgeClass) gt.getThat();
					} else {
						ec = (EdgeClass) cf.getThat();
					}
					// remove IncidenceClasses
					ec.get_to().delete();
					ec.get_from().delete();
					// remove Attributes of EdgeClass
					removeAttributes(ec);
					ec.delete();
				}
			}
			// remove Attributes of GraphElementClass
			removeAttributes(gec);
			gec.delete();
		}
		// remove the package itself if it's totally empty (degree is 1 since
		// the ContainsSubpackage edge to the parent package still exists)
		if (pkg.getDegree() == 1) {
			++n;
			System.out.println("\t- removing " + pkg.get_qualifiedName());
			pkg.delete();
		}
		return n;
	}

	/**
	 * Removes all Attribute vertices of AttributedElementClass <code>aec</code>
	 * from the schema graph
	 * 
	 * @param aec
	 */
	private void removeAttributes(AttributedElementClass aec) {
		for (HasAttribute ha = aec
				.getFirstHasAttributeIncidence(EdgeDirection.OUT); ha != null; ha = aec
				.getFirstHasAttributeIncidence(EdgeDirection.OUT)) {
			ha.getThat().delete();
		}
	}

	/**
	 * Checks whether all Enumeration domains contain at least one literal.
	 * 
	 * @throws ProcessingException
	 *             if any enumeration is empty
	 */
	private void checkEnumDomains() {
		System.out.println("Checking enumeration domains...");
		ArrayList<String> faultyDomains = new ArrayList<>();
		for (EnumDomain ed : sg.getEnumDomainVertices()) {
			if (ed.get_enumConstants().size() < 1) {
				faultyDomains.add(ed.get_qualifiedName());
			}
		}
		if (faultyDomains.size() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("The following enumeration domain")
					.append(faultyDomains.size() == 1 ? " has" : "s have")
					.append(" no literals");
			String delim = ": ";
			for (String name : faultyDomains) {
				sb.append(delim).append(name);
				delim = ", ";
			}
			throw new ProcessingException(getFilename(), sb.toString());
		}
	}

	private void attachComments() {
		System.out.println("Attaching comments to annotated elements...");
		for (String id : comments.keySet()) {
			NamedElement annotatedElement = null;
			if (domainMap.containsKey(id)) {
				annotatedElement = domainMap.get(id);
			} else if (idMap.containsKey(id)) {
				Vertex v = idMap.get(id);
				annotatedElement = (NamedElement) v;
			}
			if (annotatedElement == null) {
				System.out
						.println("\t- Couldn't find annotated element for XMI id "
								+ id
								+ "\n\t  => attaching to GraphClass (Comment starts with '"
								+ comments.get(id).get(0) + "'");
				annotatedElement = graphClass;
			}
			assert annotatedElement != null;
			if (annotatedElement.isValid()) {
				List<String> lines = comments.get(id);
				for (String line : lines) {
					Comment c = sg.createComment();
					c.set_text(line);
					sg.createAnnotates(c, annotatedElement);
				}
			}
		}
	}

	private void checkSubsettingOfAllIncidenceClasses() {
		System.out.println("Checking subsets relationships...");
		// for each specialisation between edge classes, add a subsets edge
		// between their incidence classes
		SpecializesEdgeClass spec = sg.getFirstSpecializesEdgeClass();
		while (spec != null) {
			EdgeClass subClass = spec.getAlpha();
			EdgeClass superClass = spec.getOmega();

			assert subClass.getFirstComesFromIncidence() != null;
			assert superClass.getFirstComesFromIncidence() != null;
			checkSubsettingOfIncidences(subClass, superClass,
					(IncidenceClass) subClass.getFirstComesFromIncidence()
							.getThat(), (IncidenceClass) superClass
							.getFirstComesFromIncidence().getThat());

			assert subClass.getFirstGoesToIncidence() != null;
			assert superClass.getFirstGoesToIncidence() != null;
			checkSubsettingOfIncidences(subClass, superClass,
					(IncidenceClass) subClass.getFirstGoesToIncidence()
							.getThat(), (IncidenceClass) superClass
							.getFirstGoesToIncidence().getThat());
			spec = spec.getNextSpecializesEdgeClassInGraph();
		}

		// Generalisation hierarchy is complete
	}

	private void checkSubsettingOfIncidences(EdgeClass subClass,
			EdgeClass superClass, IncidenceClass subInc, IncidenceClass superInc) {
		assert getDirection(subInc) != null;
		assert getDirection(superInc) != null;

		// Check incidence directions
		if (getDirection(subInc) != getDirection(superInc)) {
			throw new ProcessingException(getFilename(),
					"Incompatible incidence direction in specialisation "
							+ subClass + " --> " + superClass);
		}

		// Check multiplicities: Subclass must not have greater upper bound than
		// superclass
		if (subInc.get_max() > superInc.get_max()) {
			throw new ProcessingException(getFilename(),
					"Subclass has higher upper bound (" + subInc.get_max()
							+ ") than superclass (" + superInc.get_max()
							+ ") in specialisation "
							+ subClass.get_qualifiedName() + " --> "
							+ superClass.get_qualifiedName());
		}

		// COMPOSITE end may specialize any other end
		// SHARED end may specialize only SHARED and NONE ends
		// NONE end may specialize only NONE ends
		AggregationKind subAgg = subInc.get_aggregation();
		AggregationKind superAgg = superInc.get_aggregation();
		if (((subAgg == AggregationKind.SHARED) && (superAgg == AggregationKind.COMPOSITE))
				|| ((subAgg == AggregationKind.NONE) && (superAgg != AggregationKind.NONE))) {
			throw new ProcessingException(getFilename(),
					"Incompatible aggregation kinds (" + subAgg
							+ " specialises " + superAgg
							+ ") in generalisation "
							+ subClass.get_qualifiedName() + " --> "
							+ superClass.get_qualifiedName());

		}
	}

	private EdgeDirection getDirection(IncidenceClass inc) {
		assert (inc.getFirstComesFromIncidence() == null)
				|| (inc.getFirstGoesToIncidence() == null);
		if (inc.getFirstComesFromIncidence() != null) {
			return EdgeDirection.OUT;
		} else if (inc.getFirstGoesToIncidence() != null) {
			return EdgeDirection.IN;
		} else {
			return null;
		}
	}

	/**
	 * Writes a DOT file and a TG file out.
	 * 
	 * @throws XMLStreamException
	 * @throws GraphIOException
	 */
	public void writeOutput() throws XMLStreamException, GraphIOException {

		boolean fileCreated = false;

		if (filenameDot != null) {
			try {
				printTypeAndFilename("GraphvViz DOT file", filenameDot);
				writeDotFile(filenameDot);
				fileCreated = true;
			} catch (IOException e) {
				System.out.println("Could not create DOT file.");
				System.out.println("Exception was " + e);
			}
		}

		if (filenameSchemaGraph != null) {
			printTypeAndFilename("schemagraph", filenameSchemaGraph);
			writeSchemaGraph(filenameSchemaGraph);
			fileCreated = true;
		}

		// The Graph is always validated, but not always written to a hard
		// drive.
		System.out.println("Validating schema graph...");
		validateGraph(filenameValidation);
		if (filenameValidation != null) {
			fileCreated = true;
		}

		if (filenameSchema != null) {
			printTypeAndFilename("schema", filenameSchema);
			writeSchema(filenameSchema);
			fileCreated = true;
		}

		if (!fileCreated) {
			System.out.println("No files have been created.");
		}
	}

	private void printTypeAndFilename(String type, String filename) {
		System.out.println("Writing " + type + " to: " + filename);
	}

	/**
	 * Performs a graph validation and writes a report in a file.
	 * 
	 * @param schemaName
	 *            Name of the Schema.
	 * @param relativePathPrefix
	 *            Relative path to a folder.
	 */
	private void validateGraph(String filePath) {
		if (filePath != null) {
			printTypeAndFilename("validation report", filePath);
		}

		try {
			GraphValidator validator = new GraphValidator(sg);
			Set<ConstraintViolation> s;
			if (filePath != null) {
				s = validator.createValidationReport(filePath);
			} else {
				s = validator.validate();
			}
			if (!s.isEmpty()) {
				System.err.println("The schema graph is not valid:");
				for (ConstraintViolation currentViolation : s) {
					// print out violations
					System.err.println(currentViolation);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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

		String typeInsertion = type != null ? " of type " + type : "";
		return "Unexpected element <" + name + ">" + typeInsertion + ".";
	}

	/**
	 * Handles a 'uml:Package' element by creating a corresponding grUML Package
	 * element.
	 * 
	 * @return Created Package object as Vertex.
	 */
	private Vertex handlePackage() throws XMLStreamException {

		Package pkg = sg.createPackage();
		pkg.set_qualifiedName(getQualifiedName(getAttribute(UML_ATTRIBUTE_NAME)));
		sg.createContainsSubPackage(packageStack.peek(), pkg);
		packageStack.push(pkg);
		return pkg;
	}

	/**
	 * Handles a 'uml:Class' element by creating a corresponding grUML
	 * {@link VertexClass} element.
	 * 
	 * @param xmiId
	 *            XMI ID in RSA XMI file.
	 * @return Created VertexClass as {@link Vertex}.
	 * @throws XMLStreamException
	 */
	private Vertex handleClass(String xmiId) throws XMLStreamException {

		AttributedElement<?, ?> ae = idMap.get(xmiId);
		VertexClass vc = null;
		if (ae != null) {

			// Element with ID xmiID must be a VertexClass
			if (!(ae instanceof VertexClass)) {
				throw new ProcessingException(getParser(), getFilename(),
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
		vc.set_qualifiedName(getQualifiedName(getAttribute(UML_ATTRIBUTE_NAME)));
		sg.createContainsGraphElementClass(packageStack.peek(), vc);

		String isDerived = getAttribute(UML_ATTRIBUTE_ISDERIVED);
		boolean derived = (isDerived != null) && isDerived.equals(UML_TRUE);

		if (derived) {
			derivedGraphElementClasses.add(vc);
		}

		// System.out.println("currentClass = " + currentClass + " "
		// + currentClass.getQualifiedName());
		return vc;
	}

	/**
	 * Handles a 'uml:Association' or a 'uml:AssociationClass' element by
	 * creating a corresponding {@link EdgeClass} element.
	 * 
	 * @param xmiId
	 *            XMI ID in XMI file.
	 * @return Created EdgeClass as {@link Vertex}.
	 * @throws XMLStreamException
	 */
	private Vertex handleAssociation(String xmiId) throws XMLStreamException {

		// create an EdgeClass at first, probably, this has to
		// become an Aggregation or Composition later...
		AttributedElement<?, ?> ae = idMap.get(xmiId);
		EdgeClass ec = null;
		if (ae != null) {
			if (!(ae instanceof EdgeClass)) {
				throw new ProcessingException(getParser(), getFilename(),
						"The XMI id "
								+ xmiId
								+ " must denonte an EdgeClass, but is "
								+ ae.getAttributedElementClass()
										.getQualifiedName());
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
		n = n == null ? "" : n.trim();
		if (n.length() > 0) {
			n = Character.toUpperCase(n.charAt(0)) + n.substring(1);
		}
		ec.set_qualifiedName(getQualifiedName(n));
		sg.createContainsGraphElementClass(packageStack.peek(), ec);

		String memberEnd = getAttribute(UML_MEMBER_END);
		// An association have to have a end member.
		if (memberEnd == null) {
			throw new ProcessingException(getParser(), getFilename(),
					"The association with ID '" + xmiId
							+ "' has no end member. (EdgeClass)");
		}
		memberEnd = memberEnd.trim().replaceAll("\\s+", " ");
		int p = memberEnd.indexOf(' ');
		String targetEnd = memberEnd.substring(0, p);
		String sourceEnd = memberEnd.substring(p + 1);

		IncidenceClass inc = (IncidenceClass) idMap.get(sourceEnd);
		if (inc == null) {
			VertexClass vc = sg.createVertexClass();
			preliminaryVertices.add(vc);
			vc.set_qualifiedName("preliminary for source end " + sourceEnd);
			inc = sg.createIncidenceClass();
			inc.set_aggregation(AggregationKind.NONE);
			inc.set_min(DEFAULT_MIN_MULTIPLICITY);
			inc.set_max(DEFAULT_MAX_MULTIPLICITY);
			sg.createComesFrom(ec, inc);
			sg.createEndsAt(inc, vc);
			idMap.put(sourceEnd, inc);
		}

		inc = (IncidenceClass) idMap.get(targetEnd);
		if (inc != null) {
			assert inc.isValid();
			assert getDirection(inc) == EdgeDirection.OUT;

			IncidenceClass to = sg.createIncidenceClass();

			IncidenceClass from = inc;
			sg.createGoesTo(ec, to);
			sg.createEndsAt(to, (VertexClass) from.getFirstEndsAtIncidence()
					.getThat());

			to.set_aggregation(from.get_aggregation());
			to.set_max(from.get_max());
			to.set_min(from.get_min());
			to.set_roleName(from.get_roleName());

			if (ownedEnds.contains(from)) {
				ownedEnds.remove(from);
				ownedEnds.add(to);
			}
			inc.delete();
			idMap.put(targetEnd, to);
		} else {
			VertexClass vc = sg.createVertexClass();
			preliminaryVertices.add(vc);
			vc.set_qualifiedName("preliminary for target end " + targetEnd);
			inc = sg.createIncidenceClass();
			inc.set_aggregation(AggregationKind.NONE);
			inc.set_min(DEFAULT_MIN_MULTIPLICITY);
			inc.set_max(DEFAULT_MAX_MULTIPLICITY);
			sg.createGoesTo(ec, inc);
			sg.createEndsAt(inc, vc);
			idMap.put(targetEnd, inc);
		}

		String isDerived = getAttribute(UML_ATTRIBUTE_ISDERIVED);
		boolean derived = (isDerived != null) && isDerived.equals(UML_TRUE);

		if (derived) {
			derivedGraphElementClasses.add(ec);
		}
		return ec;
	}

	/**
	 * Handles a 'uml:Enumeration' element by creating a corresponding
	 * {@link EnumDomain} element.
	 * 
	 * @return Created EnumDomain as {@link Vertex}.
	 * @throws XMLStreamException
	 */
	private Vertex handleEnumeration() throws XMLStreamException {
		EnumDomain ed = sg.createEnumDomain();
		Package p = packageStack.peek();
		ed.set_qualifiedName(getQualifiedName(getAttribute(UML_ATTRIBUTE_NAME)));
		sg.createContainsDomain(p, ed);
		PVector<String> empty = JGraLab.vector();
		ed.set_enumConstants(empty);
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
	 * Handles a 'uml:PrimitiveType' element by creating a corresponding
	 * {@link Domain} element.
	 * 
	 * @return Created Domain as Vertex.
	 */
	private Vertex handlePrimitiveType(String xmiId) throws XMLStreamException {

		String typeName = getAttribute(UML_ATTRIBUTE_NAME);

		if (typeName == null) {
			throw new ProcessingException(getParser(), getFilename(),
					"No type name in primitive type. XMI ID: " + xmiId);
		}
		typeName = typeName.replaceAll("\\s", "");

		if (typeName.length() == 0) {
			throw new ProcessingException(getParser(), getFilename(),
					"Type name in primitive type is empty. XMI ID: " + xmiId);
		}
		Domain dom = createDomain(typeName);

		assert dom != null;
		return dom;
	}

	/**
	 * Handles a 'uml:Realization' by putting it into a map of realizations. By
	 * this, missing generalizations can be traced.
	 * 
	 * @throws XMLStreamException
	 */
	private void handleRealization() throws XMLStreamException {

		String supplier = getAttribute(UML_ATTRIBUTE_SUPPLIER);
		String client = getAttribute(UML_ATTRIBUTE_CLIENT);
		Set<String> reals = realizations.get(client);
		if (reals == null) {
			reals = new TreeSet<>();
			realizations.put(client, reals);
		}
		reals.add(supplier);
	}

	/**
	 * Creates a String for a {@link AttributedElementClass} by writing the
	 * AttributedElementClass name first and than a list of attributes with
	 * their values of the AttributedElementClass.
	 * 
	 * @param attributedElement
	 *            {@link AttributedElement}, of which a {@link String}
	 *            representation should be created.
	 * @return A String representing the given AttributedElement.
	 */
	private String attributedElement2String(
			AttributedElement<?, ?> attributedElement) {

		StringBuilder sb = new StringBuilder();

		de.uni_koblenz.jgralab.schema.AttributedElementClass<?, ?> aec = attributedElement
				.getAttributedElementClass();
		sb.append(attributedElement);
		sb.append(" { ");

		for (de.uni_koblenz.jgralab.schema.Attribute attr : aec
				.getAttributeList()) {
			sb.append(attr.getName());
			sb.append(" = ");
			sb.append(attributedElement.getAttribute(attr.getName()).toString());
			sb.append("; ");
		}
		sb.append("}\n");

		return sb.toString();
	}

	/**
	 * Handles a 'uml:EnumerationLiteral' by creating a corresponding
	 * enumeration literal and adding it to its {@link EnumDomain}.
	 * 
	 * @param xmiId
	 * 
	 * @throws XMLStreamException
	 */
	private void handleEnumerationLiteral() throws XMLStreamException {

		String s = getAttribute(UML_ATTRIBUTE_NAME);

		// A Literal must be declared.
		if (s == null) {
			throw new ProcessingException(getParser(), getFilename(),
					"No Literal declared.");
		}
		s = s.trim();

		// Literal must not be empty.
		if (s.length() <= 0) {
			throw new ProcessingException(getParser(), getFilename(),
					"Literal is empty.");
		}

		String classifier = getAttribute(UML_ATTRIBUTE_CLASSIFIER);

		// Exception "No Enum found for Literal " ... " found.
		if (classifier == null) {
			throw new ProcessingException(getParser(), getFilename(),
					"No Enumeration found for Literal '" + s + "'.");
		}
		EnumDomain ed = (EnumDomain) idMap.get(classifier);

		if (!s.equals(s.toUpperCase())) {
			System.out.println("Warning: Enumeration literal '" + s
					+ "' in enumeration + '" + ed.get_qualifiedName()
					+ "' should be all uppercase letters.");
		}

		ed.set_enumConstants(ed.get_enumConstants().plus(s));
	}

	/**
	 * Writes the current processed {@link Schema} as a Schema to a TG file.
	 * 
	 * @param schemaName
	 *            Name of the Schema.
	 */
	private void writeSchema(String schemaName) {
		try {
			SchemaGraph2Tg sg2tg = new SchemaGraph2Tg(sg, schemaName);
			sg2tg.process();
		} catch (IOException e) {
			throw new RuntimeException(
					"SchemaGraph2Tg failed with an IOException!", e);
		}
	}

	/**
	 * Corrects the current edge direction of every {@link EdgeClass} by using
	 * the navigability.
	 */
	private void correctEdgeDirection() {
		if (!isUseNavigability()) {
			return;
		}
		System.out
				.println("Correcting edge directions according to navigability...");
		for (EdgeClass e : sg.getEdgeClassVertices()) {
			ComesFrom cf = e.getFirstComesFromIncidence();
			if (cf == null) {
				throw new ProcessingException(getFilename(), "EdgeClass "
						+ e.get_qualifiedName() + " has no ComesFrom incidence");
			}
			GoesTo gt = e.getFirstGoesToIncidence();
			if (gt == null) {
				throw new ProcessingException(getFilename(), "EdgeClass "
						+ e.get_qualifiedName() + " has no GoesTo incidence");
			}

			IncidenceClass from = (IncidenceClass) cf.getThat();
			IncidenceClass to = (IncidenceClass) gt.getThat();

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
			assert getDirection(to) == EdgeDirection.IN;
			assert getDirection(from) == EdgeDirection.OUT;

			IncidenceClass inc = (IncidenceClass) cf.getThat();
			cf.setThat(gt.getThat());
			gt.setThat(inc);
		}

	}

	/**
	 * Attaches all Constraint objects to their corresponding
	 * {@link AttributedElementClass}.
	 * 
	 * @throws XMLStreamException
	 */
	private void attachConstraints() throws XMLStreamException {
		System.out.println("Attaching constraints...");
		for (String constrainedElementId : constraints.keySet()) {
			List<String> l = constraints.get(constrainedElementId);
			if (l.size() == 0) {
				continue;
			}
			Vertex ae = idMap.get(constrainedElementId);
			if (ae == null) {
				ae = graphClass;
			}

			if (!ae.isValid()) {
				// vertex has been removed
				continue;
			}

			// Constraint are attached to GraphClass, VertexClass, EdgeClass or
			// Association Ends.
			if (!(ae instanceof AttributedElementClass)
					&& !(ae instanceof IncidenceClass)) {
				throw new ProcessingException(
						getFilename(),
						"Constraint can only be attached to GraphClass, "
								+ "VertexClass, EdgeClass or association ends. Offending element is "
								+ ae + " (XMI id " + constrainedElementId + ")");
			}

			if (ae instanceof AttributedElementClass) {
				if (((AttributedElementClass) ae).isValid()) {
					for (String text : l) {
						addGreqlConstraint((AttributedElementClass) ae, text);
					}
				}
			} else {
				throw new ProcessingException(getFilename(),
						"Don't know what to do with constraint(s) at element "
								+ ae + " (XMI id " + constrainedElementId + ")");
			}
		}
	}

	/*
	 * EdgeClasses with a simple name of the form $<numbers>$ will be renamed as
	 * if there was no name. $<number>:<midName>$ will be renamed as if there
	 * was no name, but the middle part is fixed to <midName> instead of using
	 * Contains/IsPartOf/LinksTo. That allows for "unnamed" association classes.
	 */
	private static final Pattern GENNAME_PATTERN = Pattern
			.compile("(.*)\\$\\p{Digit}+(:(\\w+))?\\$$");

	/**
	 * Creates {@link EdgeClass} names for all EdgeClass objects, which do have
	 * an empty String or a String, which ends with a '.'.
	 */
	private void createEdgeClassNames() {
		System.out.println("Creating missing edge class names...");
		for (EdgeClass ec : sg.getEdgeClassVertices()) {
			String name = ec.get_qualifiedName().trim();

			// invent an edgeclass name
			String ecName = null;

			Matcher m = GENNAME_PATTERN.matcher(name);
			if (m.matches()) {
				name = m.group(1);
				ecName = m.group(m.groupCount());
			}

			if (!name.equals("") && !name.endsWith(".")) {
				continue;
			}

			IncidenceClass to = (IncidenceClass) ec.getFirstGoesToIncidence()
					.getThat();
			IncidenceClass from = (IncidenceClass) ec
					.getFirstComesFromIncidence().getThat();

			String toRole = to.get_roleName();
			if ((toRole == null) || toRole.equals("")) {
				toRole = ((VertexClass) to.getFirstEndsAtIncidence().getThat())
						.get_qualifiedName();
				int p = toRole.lastIndexOf('.');
				if (p >= 0) {
					toRole = toRole.substring(p + 1);
				}
			} else {
				toRole = Character.toUpperCase(toRole.charAt(0))
						+ toRole.substring(1);
			}

			// There must be a 'to' role name, which is different than null and
			// not empty.
			if ((toRole == null) || (toRole.length() <= 0)) {
				throw new ProcessingException(getFilename(),
						"There is no role name 'to' for the edge '" + name
								+ "' defined.");
			}

			if (ecName == null) {
				if ((from.get_aggregation() != AggregationKind.NONE)
						|| (to.get_aggregation() != AggregationKind.NONE)) {
					if (to.get_aggregation() != AggregationKind.NONE) {
						ecName = "Contains" + toRole;
					} else {
						ecName = "IsPartOf" + toRole;
					}
				} else {
					ecName = "LinksTo" + toRole;
				}
			} else {
				ecName += toRole;
			}

			if (isUseFromRole()) {
				String fromRole = from.get_roleName();
				if ((fromRole == null) || fromRole.equals("")) {
					fromRole = ((VertexClass) from.getFirstEndsAtIncidence()
							.getThat()).get_qualifiedName();
					int p = fromRole.lastIndexOf('.');
					if (p >= 0) {
						fromRole = fromRole.substring(p + 1);
					}
				} else {
					fromRole = Character.toUpperCase(fromRole.charAt(0))
							+ fromRole.substring(1);
				}

				// There must be a 'from' role name, which is different than
				// null and not empty.
				if ((fromRole == null) || (fromRole.length() <= 0)) {
					throw new ProcessingException(getFilename(),
							"There is no role name of 'from' for the edge '"
									+ name + "' defined.");
				}
				name += fromRole;
			}

			assert (ecName != null) && (ecName.length() > 0);
			ec.set_qualifiedName(name + ecName);
		}
	}

	/**
	 * Removes unused {@link Domain} objects, which are included in the current
	 * {@link SchemaGraph}.
	 */
	private void removeUnusedDomains() {
		System.out.println("Removing unused domains...");
		Domain d = sg.getFirstDomain();
		while (d != null) {
			Domain n = d.getNextDomain();
			// unused if in-degree of all but Annotates edges is <=1 (one
			// incoming edge is the ContainsDomain edge from a Package)
			if ((d.getDegree(EdgeDirection.IN) - d.getDegree(Annotates.EC,
					EdgeDirection.IN)) <= 1) {
				// System.out.println("...remove unused domain '"
				// + d.getQualifiedName() + "'");

				// remove possible comments
				List<? extends Comment> comments = d.remove_comments();
				for (Comment c : comments) {
					c.delete();
				}
				d.delete();
				d = sg.getFirstDomain();
			} else {
				d = n;
			}
		}
	}

	/**
	 * Resolves all preliminary {@link StringDomain}, which store the domain id,
	 * to existing {@link Domain} objects and links them to their corresponding
	 * {@link RecordDomain} objects.
	 */
	private void linkRecordDomainComponents() {

		for (HasRecordDomainComponent comp : sg
				.getHasRecordDomainComponentEdges()) {

			String domainId = recordComponentType.getMark(comp);
			if (domainId == null) {
				recordComponentType.removeMark(comp);
				continue;
			}

			Domain dom = (Domain) idMap.get(domainId);
			if (dom != null) {
				Domain d = comp.getOmega();

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
				throw new ProcessingException(getFilename(),
						"Undefined Domain with ID '" + domainId + "' found.");
			}
		}

		if (!recordComponentType.isEmpty()) {
			throw new ProcessingException(getFilename(),
					"Some RecordDomains have unresolved component types.");
		}
	}

	/**
	 * Links {@link Attribute} and {@link Domain} objects to each other by
	 * creating a {@link HasDomain} edge.
	 */
	private void linkAttributeDomains() {

		for (Attribute att : sg.getAttributeVertices()) {
			String domainId = attributeType.getMark(att);
			if (domainId == null) {
				assert att.getDegree(HasDomain.EC, EdgeDirection.OUT) == 1 : "Attribute '"
						+ att.get_name()
						+ "' of "
						+ att.getFirstHasAttributeIncidence().getThat()
								.getSchemaClass().getSimpleName()
						+ " '"
						+ ((AttributedElementClass) att
								.getFirstHasAttributeIncidence().getThat())
								.get_qualifiedName()
						+ "' has "
						+ att.getDegree(HasDomain.EC, EdgeDirection.OUT)
						+ " domain(s)";
				continue;
			}
			Domain dom = (Domain) idMap.get(domainId);
			if (dom != null) {
				sg.createHasDomain(att, dom);
				attributeType.removeMark(att);
			} else {
				// Every Attribute must have a Domain.
				throw new ProcessingException(getFilename(),
						"Undefined Domain with ID '" + domainId + "' found.");
			}

			assert att.getDegree(HasDomain.EC, EdgeDirection.OUT) == 1;
		}

		// If 'attributeType' is not empty, there will be a Domain objects
		// left over.
		if (!attributeType.isEmpty()) {
			throw new ProcessingException(getFilename(),
					"There are some Attribute objects, whos domains are not resolved.");
		}
	}

	/**
	 * Writes the {@link SchemaGraph} as Dotty-Graph to a DOT file with the name
	 * of 'dotName'.
	 * 
	 * @param dotName
	 *            File name of the DOT output file.
	 * @throws IOException
	 */
	private void writeDotFile(String dotName) throws IOException {
		Tg2Dot tg2Dot = new Tg2Dot();
		tg2Dot.setGraph(sg);
		tg2Dot.setPrintEdgeAttributes(true);
		tg2Dot.setOutputFile(dotName);
		tg2Dot.convert();
	}

	/**
	 * Writes the {@link SchemaGraph} as a Graph to a TG file with the specified
	 * file name <code>schemaGraphName</code>.
	 * 
	 * @param schemaGraphName
	 *            File name of the TG output file.
	 * @throws GraphIOException
	 */
	private void writeSchemaGraph(String schemaGraphName)
			throws GraphIOException {
		sg.save(schemaGraphName);
	}

	/**
	 * Realizes the Generalization relationship by linking
	 * {@link AttributedElementClass} objects to their direct superclass(es).
	 */
	private void linkGeneralizations() {
		for (String clientId : realizations.keySet()) {
			Set<String> suppliers = realizations.get(clientId);
			AttributedElementClass client = (AttributedElementClass) idMap
					.get(clientId);
			if (suppliers.size() > 0) {
				Set<String> superClasses = generalizations.getMark(client);
				if (superClasses == null) {
					superClasses = new TreeSet<>();
					generalizations.mark(client, superClasses);
				}
				superClasses.addAll(suppliers);
			}
		}

		for (AttributedElement<?, ?> ae : generalizations.getMarkedElements()) {
			AttributedElementClass sub = (AttributedElementClass) ae;

			Set<String> superclasses = generalizations.getMark(sub);
			for (String id : superclasses) {
				AttributedElementClass sup = (AttributedElementClass) idMap
						.get(id);

				if (sup == null) {
					// No superclass with the specified ID has been found.
					throw new ProcessingException(getFilename(),
							"The superclass with XMI id '" + id
									+ "' could not be found.");
				}
				if (sup instanceof VertexClass) {
					// VertexClass can only specialize a VertexClass
					if (!(sub instanceof VertexClass)) {
						throw new ProcessingException(getFilename(),
								"Different types in generalization: "
										+ sub.getSchemaClass().getSimpleName()
										+ " '" + sub.get_qualifiedName()
										+ "' can not be subclass of "
										+ sub.getSchemaClass().getSimpleName()
										+ " '" + sup.get_qualifiedName() + "'");
					}

					sg.createSpecializesVertexClass((VertexClass) ae,
							(VertexClass) sup);
					if (!vertexClassHierarchyIsAcyclic()) {
						throw new ProcessingException(getFilename(),
								"Cycle in vertex class hierarchy. Involved classes are '"
										+ sub.get_qualifiedName() + "' and '"
										+ sup.get_qualifiedName() + "'");
					}
				} else if (sup instanceof EdgeClass) {
					// EdgeClass can only specialize an EdgeClass
					if (!(sub instanceof EdgeClass)) {
						throw new ProcessingException(getFilename(),
								"Different types in generalization: "
										+ sub.getSchemaClass().getSimpleName()
										+ " '" + sub.get_qualifiedName()
										+ "' can not be subclass of "
										+ sub.getSchemaClass().getSimpleName()
										+ " '" + sup.get_qualifiedName() + "'");
					}

					sg.createSpecializesEdgeClass((EdgeClass) ae,
							(EdgeClass) sup);
					if (!edgeClassHierarchyIsAcyclic()) {
						throw new ProcessingException(getFilename(),
								"Cycle in edge class hierarchy. Involved classes are '"
										+ sub.get_qualifiedName() + "' and '"
										+ sup.get_qualifiedName() + "'");
					}
				} else {
					// Should not get here
					throw new RuntimeException(
							"FIXME: Unexpected super class type. Super class must be VertexClass or EdgeClass!");
				}
			}
		}
		generalizations.clear();
	}

	/**
	 * Checks whether the edge class generalization hierarchy is acyclic.
	 * 
	 * @return true iff the edge class generalization hierarchy is acyclic.
	 */
	private boolean edgeClassHierarchyIsAcyclic() {
		if (edgeClassAcyclicQuery == null) {
			edgeClassAcyclicQuery = GreqlQuery
					.createQuery("on edgeTypeSubgraph{structure.SpecializesEdgeClass}(): isAcyclic()");
		}
		return (Boolean) edgeClassAcyclicQuery.evaluate(sg);
	}

	/**
	 * Checks whether the vertex class generalization hierarchy is acyclic.
	 * 
	 * @return true iff the vertex class generalization hierarchy is acyclic.
	 */
	private boolean vertexClassHierarchyIsAcyclic() {
		if (vertexClassAcyclicQuery == null) {
			vertexClassAcyclicQuery = GreqlQuery
					.createQuery("on edgeTypeSubgraph{structure.SpecializesVertexClass}(): isAcyclic()");
		}
		return (Boolean) vertexClassAcyclicQuery.evaluate(sg);
	}

	/**
	 * Removes empty {@link Package} objects from the {@link SchemaGraph}.
	 */
	private void removeEmptyPackages() {
		// remove all empty packages except the default package
		System.out.println("Removing empty packages...");
		Package p = sg.getFirstPackage();
		int removed = 0;
		while (p != null) {
			Package n = p.getNextPackage();
			int commentCount = p.getDegree(Annotates.EC);
			if (((p.getDegree() - commentCount) == 1)
					&& (p.get_qualifiedName().length() > 0)) {
				System.out
						.println("\t- empty package '"
								+ p.get_qualifiedName()
								+ "' removed"
								+ (commentCount > 0 ? commentCount == 1 ? " including 1 comment"
										: " including " + commentCount
												+ " comments"
										: ""));
				if (commentCount > 0) {
					for (Annotates a = p.getFirstAnnotatesIncidence(); a != null; a = p
							.getFirstAnnotatesIncidence()) {
						a.getThat().delete();
					}
				}
				p.delete();
				++removed;
				// start over to capture packages that become empty after
				// deletion of p
				p = sg.getFirstPackage();
			} else {
				p = n;
			}
		}
		System.out.println("\tRemoved " + removed + " package"
				+ (removed == 1 ? "" : "s") + ".");
	}

	/**
	 * Handles a {@link Constraint} by adding it to a preliminary {@link Map} of
	 * Constraints and their ids.
	 * 
	 * @param text
	 *            Constraint as {@link String}.
	 * @param line
	 *            Line number, at which the current Constraint has been found.
	 *            Only needed for exception purposes.
	 * @throws XMLStreamException
	 */
	private void handleConstraint(String text) throws XMLStreamException {
		if (text.startsWith("\"")) {
			List<String> l = constraints.get(constrainedElementId);
			if (l == null) {
				l = new LinkedList<>();
				constraints.put(constrainedElementId, l);
			}
			l.add(text);
		} else if (text.startsWith("subsets")) {
			System.err
					.println("warning: {subsets ...} constraint at element "
							+ constrainedElementId
							+ " ignored (don't forget to model generalizations between associations)");
		} else if (text.startsWith("union")) {
			System.err
					.println("warning: {union} constraint at element "
							+ constrainedElementId
							+ " ignored (don't forget to add an <<abstract>> stereotype to the association)");
		} else if (text.startsWith("ordered")) {
			System.err.println("warning: {ordered} constraint at element "
					+ constrainedElementId
					+ " ignored (TGraphs are ordered by default)");
		} else {
			throw new ProcessingException(getFilename(), getParser()
					.getLocation().getLineNumber(),
					"Illegal constraint format: " + text);
		}
	}

	/**
	 * Adds a Greql constraint to a {@link AttributedElementClass} object.
	 * 
	 * @param constrainedClass
	 *            {@link AttributedElementClass}, which should be constraint.
	 * @param text
	 *            Constraint as String.
	 * @throws XMLStreamException
	 */
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
					String constraintText = text.substring(beginIndex + 1, i)
							.trim().replaceAll("\\\\(.)", "$1");
					if (constraintText.isEmpty()) {
						constraintText = null;
					}
					switch (stringCount) {
					case 1:
						constraint.set_message(constraintText);
						break;
					case 2:
						constraint.set_predicateQuery(constraintText);
						break;
					case 3:
						constraint.set_offendingElementsQuery(constraintText);
						break;
					default:
						throw new ProcessingException(getFilename(),
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
						throw new ProcessingException(getFilename(),
								"Illegal constraint format. The constraint text was '"
										+ text + "'.  Expected '\"' but got '"
										+ c + "'.  (position = " + i + ").");
					}
				}
			}
		}
		if (inString || escape || (stringCount < 2) || (stringCount > 3)) {
			throw new ProcessingException(getFilename(),
					"Illegal constraint format.  The constraint text was '"
							+ text + "'.");
		}
	}

	/**
	 * Sets the upper bound of the multiplicity of an {@link Edge} as the 'max'
	 * value of the current 'from' or 'to' Edge.
	 * 
	 * @throws XMLStreamException
	 */
	private void handleUpperValue() throws XMLStreamException {
		assert (currentAssociationEnd != null) || inOwnedAttribute;
		int n = getValue();
		if ((currentAssociationEnd == null) && inOwnedAttribute) {
			if (n != 1) {
				throw new ProcessingException(getParser(), getFilename(),
						"grUML does not support attribute multiplicities other than 1..1");
			}
		} else {
			assert currentAssociationEnd != null;
			assert n >= 1;
			currentAssociationEnd.set_max(n);
		}
	}

	/**
	 * Retrieves the value of the 'value' attribute of the current XML element
	 * and returns it.
	 * 
	 * @throws XMLStreamException
	 * @return Retrieved integer value.
	 */
	private int getValue() throws XMLStreamException {
		String val = getAttribute(UML_ATTRIBUTE_VALUE);
		return val == null ? 0 : val.equals("*") ? Integer.MAX_VALUE : Integer
				.parseInt(val);
	}

	/**
	 * Sets the lower bound of the multiplicity of an {@link Edge} as the 'min'
	 * value of the current 'from' or 'to' Edge.
	 * 
	 * @throws XMLStreamException
	 */
	private void handleLowerValue() throws XMLStreamException {
		assert (currentAssociationEnd != null) || inOwnedAttribute;
		int n = getValue();
		if ((currentAssociationEnd == null) && inOwnedAttribute) {
			if (n != 1) {
				throw new ProcessingException(getParser(), getFilename(),
						"grUML does not support attribute multiplicities other than 1..1");
			}
		} else {
			assert currentAssociationEnd != null;
			assert n >= 0;
			currentAssociationEnd.set_min(n);
		}
	}

	/**
	 * Handles the stereotypes '<<graphclass>>', '<<record>>' and '<<abstract>>'
	 * by taking the appropriate action for every stereotype.
	 * 
	 * '<<graphclass>>': The GraphClass will get the qualified name and all edge
	 * of the stereotyped class. The stereotyped class will be deleted.
	 * 
	 * '<<record>>': A RecordDomain will be created and the qualified name and
	 * all attributes will be transfered to it. The stereotyped class will be
	 * deleted.
	 * 
	 * '<<abstract>>': The stereotype will be set to abstract.
	 * 
	 * @throws XMLStreamException
	 */
	private void handleStereotype() throws XMLStreamException {
		String key = getAttribute(UML_ATTRIBUTE_KEY);

		if ((currentClass == null) && (currentClassId == null)
				&& (currentAssociationEnd == null)
				&& (currentAttribute == null) && (currentRecordDomain == null)
				&& (currentRecordDomainComponent == null)) {
			if (key.equals("rsa2tg_ignore")) {
				ignoredPackages.add(packageStack.peek());
				return;
			} else {
				throw new ProcessingException(getParser(), getFilename(),
						"Unexpected stereotype <<" + key + ">>");
			}
		}

		if (currentClass == null) {
			throw new ProcessingException(getParser(), getFilename(),
					"Unexpected stereotype <<" + key + ">>");
		}

		if (key.equals("graphclass")) {
			// convert currentClass to graphClass

			// The stereotype '<<graphclass>>' can only be attached to UML
			// classes.
			if (!(currentClass instanceof VertexClass)) {
				throw new ProcessingException(getParser(), getFilename(),
						"The stereotype '<<graphclass>>' is only valid for a UML class.");
			}

			AttributedElementClass aec = (AttributedElementClass) idMap
					.get(currentClassId);
			assert graphClass != null;
			graphClass.set_qualifiedName(aec.get_qualifiedName());
			Edge e = aec.getFirstIncidence();
			while (e != null) {
				Edge n = e.getNextIncidence();
				if (e instanceof ContainsGraphElementClass) {
					e.delete();
				} else {
					e.setThis(graphClass);
				}
				e = n;
			}
			aec.delete();
			idMap.put(currentClassId, graphClass);
			currentClass = graphClass;

		} else if (key.equals("record")) {
			// convert current class to RecordDomain

			// The stereotype '<<record>>' can only be attached to UML classes.
			if (!(currentClass instanceof VertexClass)) {
				throw new ProcessingException(getParser(), getFilename(),
						"The stereotype '<<record>>' is only allow for UML-classes.");
			}

			RecordDomain rd = sg.createRecordDomain();
			rd.set_qualifiedName(currentClass.get_qualifiedName());
			Edge e = currentClass.getFirstIncidence();
			while (e != null) {
				Edge n = e.getNextIncidence();
				if (e instanceof ContainsGraphElementClass) {
					sg.createContainsDomain((Package) e.getThat(), rd);
					e.delete();
				} else if (e instanceof HasAttribute) {
					Attribute att = (Attribute) e.getThat();
					Edge d = att.getFirstHasDomainIncidence();
					if (d != null) {
						Domain dom = (Domain) e.getThat();
						HasRecordDomainComponent comp = sg
								.createHasRecordDomainComponent(rd, dom);
						comp.set_name(att.get_name());
					} else {
						String typeId = attributeType.getMark(att);

						// There have to be a typeId.
						if (typeId == null) {
							throw new ProcessingException(getParser(),
									getFilename(),
									"No type id has been defined.");
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
				}
				e = n;
			}
			if (currentClass.getDegree() != 0) {
				throw new ProcessingException(getParser(), getFilename(),
						"The <<record>> class '"
								+ currentClass.get_qualifiedName()
								+ "' must not have any association.");
			}
			domainMap.put(rd.get_qualifiedName(), rd);
			idMap.put(currentClassId, rd);
			currentRecordDomain = rd;
			currentClass.delete();
			currentClass = null;
			currentClassId = null;
		} else if (key.equals("abstract")) {
			if (currentClass instanceof GraphElementClass) {
				GraphElementClass gec = (GraphElementClass) currentClass;
				gec.set_abstract(true);
			} else {
				throw new ProcessingException(
						getParser(),
						getFilename(),
						"The stereotype <<abstract>> can only be specified for vertex and edge classes, but not for class '"
								+ currentClass.get_qualifiedName() + "'");
			}
		} else {
			if (ignoreUnknownStereotypes()) {
				System.out.println("Ignored stereotype <<" + key + ">> of "
						+ currentClass);
			} else {
				throw new ProcessingException(getParser(), getFilename(),
						"Unexpected stereotype '<<" + key + ">>'.");
			}
		}
	}

	/**
	 * Handles a 'generalization' XML element by marking the current class.
	 * 
	 * @param parser
	 *            {@link XMLStreamReader}, which points to the current XML
	 *            element.
	 */
	private void handleGeneralization() throws XMLStreamException {
		String general = getAttribute(UML_ATTRIBUTE_GENERAL);
		Set<String> gens = generalizations.getMark(currentClass);
		if (gens == null) {
			gens = new TreeSet<>();
			generalizations.mark(currentClass, gens);
		}
		gens.add(general);
	}

	/**
	 * Handles a nested 'uml:PrimitivType' XML element by creating a
	 * corresponding {@link Domain}.
	 * 
	 * @param xmiId
	 *            XMI id of corresponding attribute
	 * @throws XMLStreamException
	 */
	private void handleNestedTypeElement(String xmiId)
			throws XMLStreamException {
		if ((currentAttribute == null) && (currentRecordDomain == null)) {
			throw new ProcessingException(getParser(), getFilename(),
					"unexpected primitive type in element (XMI id " + xmiId
							+ ")");
		}
		String href = getAttribute(UML_ATTRIBUTE_HREF);
		if (href == null) {
			throw new ProcessingException(getParser(), getFilename(),
					"No type name specified in primitive type href of attribute (XMI id "
							+ xmiId + ")");
		}
		Domain dom = null;
		if (href.endsWith("#String")) {
			dom = createDomain("String");
		} else if (href.endsWith("#Integer")) {
			dom = createDomain("Integer");
		} else if (href.endsWith("#Boolean")) {
			dom = createDomain("Boolean");
		} else if (href.endsWith("#double")) {
			dom = createDomain("Double");
		} else {
			throw new ProcessingException(getParser(), getFilename(),
					"Unknown primitive type with href '" + href
							+ "' in attribute (XMI id " + xmiId + ")");
		}

		assert dom != null;
		if (currentRecordDomain != null) {
			// type of record domain component
			assert currentRecordDomainComponent != null;
			Domain d = currentRecordDomainComponent.getOmega();
			assert (d instanceof StringDomain)
					&& (d.get_qualifiedName() == null)
					&& preliminaryVertices.contains(d);
			currentRecordDomainComponent.setOmega(dom);
			d.delete();
			preliminaryVertices.remove(d);
			recordComponentType.removeMark(currentRecordDomainComponent);
		} else {
			// type of an attribute of an AttributedElementClass
			assert currentAttribute != null;
			sg.createHasDomain(currentAttribute, dom);
			attributeType.removeMark(currentAttribute);
		}
	}

	/**
	 * Handles a 'ownedAttribute' XML element of type 'uml:Property' by creating
	 * a {@link Attribute} and linking it with its
	 * {@link AttributedElementClass}.
	 * 
	 * @param parser
	 *            {@link XMLStreamReader}, which points to the current XML
	 *            element.
	 * @param xmiId
	 *            XMI id of the current XML element.
	 */
	private void handleOwnedAttribute(String xmiId) throws XMLStreamException {
		String association = getAttribute(UML_ATTRIBUTE_ASSOCIATION);
		if (association == null) {
			String attrName = getAttribute(UML_ATTRIBUTE_NAME);

			if ((currentClass == null) && (currentRecordDomain == null)) {
				throw new ProcessingException(getParser(), getFilename(),
						"Found an attribute '" + attrName + "' (XMI id "
								+ xmiId + ") outside a class!");
			}

			if (attrName == null) {
				throw new ProcessingException(getParser(), getFilename(),
						"No attribute name in ownedAttribute (XMI id " + xmiId
								+ ")");
			}
			attrName = attrName.trim();

			if (attrName.length() == 0) {
				throw new ProcessingException(getParser(), getFilename(),
						"Empty attribute name in ownedAttribute (XMI id "
								+ xmiId + ")");
			}

			String isDerived = getAttribute(UML_ATTRIBUTE_ISDERIVED);
			boolean derived = (isDerived != null) && isDerived.equals(UML_TRUE);

			if (derived) {
				// ignore derived attributes
				return;
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
					Vertex v = idMap.get(typeId);
					if (v != null) {
						assert v instanceof Domain : "typeID says " + typeId
								+ " which is no Domain!";
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
			handleAssociationEnd(xmiId);
		}
	}

	/**
	 * Handles a 'ownedEnd' XML element of type 'uml:Property' by creating an
	 * appropriate {@link From} edge.
	 * 
	 * @param xmiId
	 * @throws XMLStreamException
	 */
	private void handleAssociationEnd(String xmiId) throws XMLStreamException {
		String endName = getAttribute(UML_ATTRIBUTE_NAME);
		if ((currentClass == null) || (currentRecordDomain != null)) {
			throw new ProcessingException(getParser(), getFilename(),
					"Found an association end '" + endName + "' (XMI id "
							+ xmiId + ") outside a class or in a record domain");
		}

		String agg = getAttribute(UML_ATTRIBUTE_AGGREGATION);
		boolean aggregation = (agg != null) && agg.equals(UML_SHARED);
		boolean composition = (agg != null) && agg.equals(UML_COMPOSITE);

		String typeId = getAttribute(UML_ATTRIBUTE_TYPE);

		if (typeId == null) {
			throw new ProcessingException(getParser(), getFilename(),
					"No type attribute in association end (XMI id" + xmiId
							+ ")");
		}

		IncidenceClass inc = (IncidenceClass) idMap.get(xmiId);
		if (inc == null) {
			// try to find the end's VertexClass
			// if not found, create a preliminary VertexClass
			VertexClass vc = null;

			// we have an "ownedEnd", vertex class id is in "type" attribute

			AttributedElement<?, ?> ae = idMap.get(typeId);
			if (ae != null) {
				if (!(ae instanceof VertexClass)) {
					throw new ProcessingException(getParser(), getFilename(),
							"Type attribute of association end (XMI id "
									+ xmiId
									+ ") must denote a VertexClass, but is "
									+ ae.getAttributedElementClass()
											.getQualifiedName());
				}
				// VertexClass found
				vc = (VertexClass) ae;
			} else {
				// create a preliminary vertex class
				vc = sg.createVertexClass();
				vc.set_qualifiedName(typeId);
				preliminaryVertices.add(vc);
				idMap.put(typeId, vc);
			}

			// try to find the end's EdgeClass
			EdgeClass ec = null;
			if (currentClass instanceof EdgeClass) {
				// we have an "ownedEnd", so the end's Edge is the
				// currentClass
				ec = (EdgeClass) currentClass;
				idMap.put(currentClassId, currentClass);
			} else {
				// we have an ownedAttribute
				// edge class id is in "association"
				String associationId = getAttribute(UML_ATTRIBUTE_ASSOCIATION);

				if (associationId == null) {
					throw new ProcessingException(getParser(), getFilename(),
							"No assiocation attribute in association end (XMI id "
									+ xmiId + ")");
				}
				ae = idMap.get(associationId);

				if (ae != null) {
					if (!(ae instanceof EdgeClass)) {
						throw new ProcessingException(getParser(),
								getFilename(),
								"Assiocation attribute of association end (XMI id "
										+ xmiId
										+ ") must denote an EdgeClass, but is "
										+ ae.getAttributedElementClass()
												.getQualifiedName());
					}
					// EdgeClass found
					ec = (EdgeClass) ae;
				} else {
					// create a preliminary edge class
					ec = sg.createEdgeClass();
				}

				preliminaryVertices.add(ec);
				idMap.put(associationId, ec);
			}

			assert (vc != null) && (ec != null);
			inc = sg.createIncidenceClass();
			inc.set_min(DEFAULT_MIN_MULTIPLICITY);
			inc.set_max(DEFAULT_MAX_MULTIPLICITY);
			sg.createComesFrom(ec, inc);
			sg.createEndsAt(inc, vc);
		} else {
			EdgeClass ec = (EdgeClass) (inc.getFirstComesFromIncidence() != null ? inc
					.getFirstComesFromIncidence() : inc
					.getFirstGoesToIncidence()).getThat();
			String id = null;
			for (Entry<String, Vertex> idEntry : idMap.entrySet()) {
				if (idEntry.getValue() == ec) {
					id = idEntry.getKey();
					break;
				}
			}

			assert id != null;
			idMap.put(id, ec);

			// an ownedEnd of an association or an ownedAttribute of a class
			// with a possibly preliminary vertex class
			VertexClass vc = (VertexClass) inc.getFirstEndsAtIncidence()
					.getThat();
			if (preliminaryVertices.contains(vc)) {

				AttributedElement<?, ?> ae = idMap.get(typeId);

				if ((ae != null) && !vc.equals(ae)) {
					if (!(ae instanceof VertexClass)) {
						throw new ProcessingException(
								getParser(),
								getFilename(),
								"Type attribute of association end (XMI id "
										+ xmiId
										+ ") must denote a VertexClass, but is "
										+ ae.getAttributedElementClass()
												.getQualifiedName());
					}
					inc.getFirstEndsAtIncidence().setOmega((VertexClass) ae);

					Set<String> gens = generalizations.getMark(vc);
					if (gens != null) {
						generalizations.removeMark(vc);
						generalizations.mark(ae, gens);
					}

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

		assert inc != null;
		currentAssociationEnd = inc;
		if (currentClass instanceof EdgeClass) {
			ownedEnds.add(inc);
		}
		inc.set_aggregation(aggregation ? AggregationKind.SHARED
				: composition ? AggregationKind.COMPOSITE
						: AggregationKind.NONE);
		idMap.put(xmiId, inc);
		inc.set_roleName(endName);
	}

	/**
	 * Reconnects all edges of an <code>oldVertex</code> to
	 * <code>newVertex</code>.
	 * 
	 * @param oldVertex
	 *            Old {@link Vertex}, of which all edge should be reattached.
	 * @param newVertex
	 *            New {@link Vertex}, to which all edge should be attached.
	 */
	private void reconnectEdges(Vertex oldVertex, Vertex newVertex) {
		Edge curr = oldVertex.getFirstIncidence();
		while (curr != null) {
			Edge next = curr.getNextIncidence();
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
	 *            Describes the Domain, which should be created.
	 * @return Created Domain.
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
				if (p < 0) {
					throw new ProcessingException(getFilename(),
							"Error in primitive type name: '" + typeName + "'");
				}
			}

			if ((p <= 0) || (p >= (c.length - 1))) {
				throw new ProcessingException(getFilename(),
						"Error in primitive type name: '" + typeName + "'");
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

	/**
	 * <code>true</code> indicates, that the roles from edges should be used.
	 * 
	 * @param useFromRole
	 *            Value for the <code>useFromRole</code> flag.
	 */
	public void setUseFromRole(boolean useFromRole) {
		this.useFromRole = useFromRole;
	}

	/**
	 * Will return <code>true</code>, if the roles from the from edge should be
	 * used.
	 * 
	 * @return Value of the <code>useFromRole</code> flag.
	 */
	public boolean isUseFromRole() {
		return useFromRole;
	}

	public boolean ignoreUnknownStereotypes() {
		return ignoreUnknownStereotypes;
	}

	public void setIgnoreUnknownStereotypes(boolean ignore) {
		ignoreUnknownStereotypes = ignore;
	}

	/**
	 * <code>true</code> forces the removal of all unlinked {@link Domain}
	 * objects.
	 * 
	 * @param removeUnusedDomains
	 *            Value of the <code>removeUnusedDomain</code> flag.
	 */
	public void setRemoveUnusedDomains(boolean removeUnusedDomains) {
		this.removeUnusedDomains = removeUnusedDomains;
	}

	/**
	 * Will return <code>true</code>, if unlinked {@link Domain} objects should
	 * be removed in the last processing step.
	 * 
	 * @return Value of the <code>removeUnusedDoimain</code> flag.
	 */
	public boolean isRemoveUnusedDomains() {
		return removeUnusedDomains;
	}

	/**
	 * <code>true</code> indicates, that the navigability of edges should be
	 * used.
	 * 
	 * @param useNavigability
	 *            Value for the <code>useNavigability</code> flag.
	 */
	public void setUseNavigability(boolean useNavigability) {
		this.useNavigability = useNavigability;
	}

	/**
	 * Will return <code>true</code>, if the navigability of edges should be
	 * used.
	 * 
	 * @return Value of the <code>useNavigability</code> flag.
	 */
	public boolean isUseNavigability() {
		return useNavigability;
	}

	/**
	 * Returns the {@link SchemaGraph}, which has been created after executing
	 * {@link Rsa2Tg#process(String)}.
	 * 
	 * @return Created SchemaGraph.
	 */
	public SchemaGraph getSchemaGraph() {
		return sg;
	}

	/**
	 * Determines whether or not all output will be suppressed.
	 * 
	 * @param suppressOutput
	 *            Value for the <code>suppressOutput</code> flag.
	 */
	public void setSuppressOutput(boolean suppressOutput) {
		this.suppressOutput = suppressOutput;
	}

	/**
	 * Returns the file name of the TG Schema file.
	 * 
	 * @return File name as {@link String}.
	 */
	public String getFilenameSchema() {
		return filenameSchema;
	}

	/**
	 * Sets the file name of the TG Schema file.
	 * 
	 * @param filenameSchema
	 *            File name as {@link String}.
	 */
	public void setFilenameSchema(String filenameSchema) {
		this.filenameSchema = filenameSchema;
	}

	/**
	 * Returns the file name of the TG grUML SchemaGraph file.
	 * 
	 * @return File name as {@link String}.
	 */
	public String getFilenameSchemaGraph() {
		return filenameSchemaGraph;
	}

	/**
	 * Sets the file name of the TG grUML SchemaGraph file.
	 * 
	 * @param filenameSchemaGraph
	 *            file name as {@link String}.
	 */
	public void setFilenameSchemaGraph(String filenameSchemaGraph) {
		this.filenameSchemaGraph = filenameSchemaGraph;
	}

	/**
	 * Returns the file name of the DOT file.
	 * 
	 * @return File name as {@link String}.
	 */
	public String getFilenameDot() {
		return filenameDot;
	}

	/**
	 * Sets the file name of the DOT file.
	 * 
	 * @param filenameDot
	 *            File name as {@link String}.
	 */
	public void setFilenameDot(String filenameDot) {
		this.filenameDot = filenameDot;
	}

	/**
	 * Returns the file name of the HTML validation file.
	 * 
	 * @return File name as {@link String}.
	 */
	public String getFilenameValidation() {
		return filenameValidation;
	}

	/**
	 * Sets the file name of the HTML validation file.
	 * 
	 * @param filenameValidation
	 *            File name as {@link String}.
	 */
	public void setFilenameValidation(String filenameValidation) {
		this.filenameValidation = filenameValidation;
	}

	public boolean isKeepEmptyPackages() {
		return keepEmptyPackages;
	}

	public void setKeepEmptyPackages(boolean removeEmptyPackages) {
		keepEmptyPackages = removeEmptyPackages;
	}

}
