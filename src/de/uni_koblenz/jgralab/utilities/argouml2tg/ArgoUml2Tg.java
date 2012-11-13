package de.uni_koblenz.jgralab.utilities.argouml2tg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.pcollections.PVector;

import de.uni_koblenz.ist.utilities.option_handler.OptionHandler;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.functions.entries.FunctionEntry;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
import de.uni_koblenz.jgralab.graphmarker.IntegerVertexMarker;
import de.uni_koblenz.jgralab.graphvalidator.ConstraintViolation;
import de.uni_koblenz.jgralab.graphvalidator.GraphValidator;
import de.uni_koblenz.jgralab.grumlschema.GrumlSchema;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.grumlschema.domains.BooleanDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.Domain;
import de.uni_koblenz.jgralab.grumlschema.domains.EnumDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.HasRecordDomainComponent;
import de.uni_koblenz.jgralab.grumlschema.domains.ListDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.MapDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.RecordDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.SetDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.StringDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.AggregationKind;
import de.uni_koblenz.jgralab.grumlschema.structure.Annotates;
import de.uni_koblenz.jgralab.grumlschema.structure.Attribute;
import de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.Comment;
import de.uni_koblenz.jgralab.grumlschema.structure.Constraint;
import de.uni_koblenz.jgralab.grumlschema.structure.EdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.IncidenceClass;
import de.uni_koblenz.jgralab.grumlschema.structure.NamedElement;
import de.uni_koblenz.jgralab.grumlschema.structure.Package;
import de.uni_koblenz.jgralab.grumlschema.structure.Schema;
import de.uni_koblenz.jgralab.grumlschema.structure.SpecializesEdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.SpecializesVertexClass;
import de.uni_koblenz.jgralab.grumlschema.structure.VertexClass;
import de.uni_koblenz.jgralab.utilities.rsa2tg.ProcessingException;
import de.uni_koblenz.jgralab.utilities.rsa2tg.Rsa2Tg;
import de.uni_koblenz.jgralab.utilities.rsa2tg.SchemaGraph2Tg;
import de.uni_koblenz.jgralab.utilities.tg2dot.Tg2Dot;
import de.uni_koblenz.jgralab.utilities.xml2tg.Xml2Tg;
import de.uni_koblenz.jgralab.utilities.xml2tg.XmlGraphUtilities;
import de.uni_koblenz.jgralab.utilities.xml2tg.schema.Element;
import de.uni_koblenz.jgralab.utilities.xml2tg.schema.HasChild;

public class ArgoUml2Tg extends Xml2Tg {

	private final Logger logger = JGraLab.getLogger(ArgoUml2Tg.class);

	private static final String OPTION_FILENAME_VALIDATION = "r";
	private static final String OPTION_FILENAME_SCHEMA_GRAPH = "s";
	private static final String OPTION_FILENAME_DOT = "e";
	private static final String OPTION_FILENAME_SCHEMA = "o";
	private static final String OPTION_USE_NAVIGABILITY = "n";
	private static final String OPTION_REMOVE_UNUSED_DOMAINS = "u";
	private static final String OPTION_KEEP_EMPTY_PACKAGES = "k";
	private static final String OPTION_USE_ROLE_NAME = "f";

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

	/**
	 * Filename for validation
	 */
	private String filenameValidation;

	private static final String ST_GRAPHCLASS = "-64--88-111--125-2048530b:13717182953:-8000:0000000000000D6A";
	private static final String ST_RECORD = "-64--88-111--125-2048530b:13717182953:-8000:0000000000000D6B";
	private static final String ST_ABSTRACT = "-64--88-111--125-2048530b:13717182953:-8000:0000000000000D6C";

	private static final String DT_DOUBLE = "-115-26-95--20--17a78cb8:13718617229:-8000:0000000000000D76";
	private static final String DT_INTEGER = "-115-26-95--20--17a78cb8:13718617229:-8000:00000000000019DA";
	private static final String DT_UML_INTEGER = "-84-17--56-5-43645a83:11466542d86:-8000:000000000000087C";
	private static final String DT_LONG = "-115-26-95--20--17a78cb8:13718617229:-8000:0000000000000D77";
	private static final String DT_BOOLEAN = "-115-26-95--20--17a78cb8:13718617229:-8000:00000000000019DB";
	private static final String DT_STRING = "-115-26-95--20--17a78cb8:13718617229:-8000:00000000000019DC";
	private static final String DT_UML_STRING = "-84-17--56-5-43645a83:11466542d86:-8000:000000000000087E";

	private static final String TV_UML_DERIVED = "-64--88-0-101--2259be85:11dd526880c:-8000:000000000000E4A7";

	private XmlGraphUtilities xu;
	private HashMap<String, Vertex> qnMap;
	private HashMap<String, Package> packageMap;
	private HashMap<String, Domain> domainMap;
	private HashMap<String, Domain> profileIdMap;
	private HashMap<String, Vertex> xmiIdMap;
	private SchemaGraph sg;
	private Schema schema;
	private Package defaultPackage;
	private GraphClass graphClass;

	/**
	 * Processes an XMI-file to a TG-file as schema or a schema in a grUML
	 * graph. For all command line options see
	 * {@link ArgoUML2Tg#processCommandLineOptions(String[])}.
	 * 
	 * @param args
	 *            {@link String} array of command line options.
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		System.out.println("ArgoUML to TG");
		System.out.println("=========");
		JGraLab.setLogLevel(Level.OFF);

		// Retrieving all command line options
		CommandLine cli = processCommandLineOptions(args);

		assert cli != null : "No CommandLine object has been generated!";
		// All XMI input files
		File input = new File(cli.getOptionValue('i'));

		ArgoUml2Tg a2tg = new ArgoUml2Tg();

		a2tg.setUseFromRole(cli.hasOption(OPTION_USE_ROLE_NAME));
		a2tg.setRemoveUnusedDomains(cli.hasOption(OPTION_REMOVE_UNUSED_DOMAINS));
		a2tg.setKeepEmptyPackages(cli.hasOption(OPTION_KEEP_EMPTY_PACKAGES));
		a2tg.setUseNavigability(cli.hasOption(OPTION_USE_NAVIGABILITY));

		// apply options
		a2tg.setFilenameSchema(cli.getOptionValue(OPTION_FILENAME_SCHEMA));
		a2tg.setFilenameSchemaGraph(cli
				.getOptionValue(OPTION_FILENAME_SCHEMA_GRAPH));
		a2tg.setFilenameDot(cli.getOptionValue(OPTION_FILENAME_DOT));
		a2tg.setFilenameValidation(cli
				.getOptionValue(OPTION_FILENAME_VALIDATION));

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
			a2tg.setFilenameSchema(createFilename(input));
		}

		try {
			a2tg.process(input.getPath());
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
	 * object, which holds all values included in the given {@link String}
	 * array.
	 * 
	 * @param args
	 *            {@link CommandLine} parameters.
	 * @return {@link CommandLine} object, which holds all necessary values.
	 */
	public static CommandLine processCommandLineOptions(String[] args) {

		// Creates a OptionHandler.
		String toolString = "java " + ArgoUml2Tg.class.getName();
		String versionString = JGraLab.getInfo(false);

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
				"(required): UML 1.2-XMI exchange model file of the Schema.");
		input.setRequired(true);
		input.setArgName("filename");
		oh.addOption(input);

		Option output = new Option(
				OPTION_FILENAME_SCHEMA,
				"output",
				true,
				"(optional): writes a TG-file of the Schema to the given filename. "
						+ "Free naming, but should look like this: '<filename>.argouml.tg.'");
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

		Option emptyPackages = new Option(OPTION_KEEP_EMPTY_PACKAGES,
				"keepEmptyPackages", false,
				"(optional): if this flag is set, empty packages will be retained.");
		unusedDomains.setRequired(false);
		oh.addOption(emptyPackages);

		Option navigability = new Option(
				OPTION_USE_NAVIGABILITY,
				"useNavigability",
				false,
				"(optional): if this flag is set, navigability information will be interpreted as reading direction.");
		navigability.setRequired(false);
		oh.addOption(navigability);

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
		filenameBuilder.append(".argouml.tg");
		return filenameBuilder.toString();
	}

	public ArgoUml2Tg() {
		setIgnoreCharacters(false);
		addIgnoredElements("XMI.header");
		addIdAttributes("*/xmi.id");
		addIdRefAttributes("*/xmi.idref");
	}

	@Override
	public void process(String fileName) throws FileNotFoundException,
			XMLStreamException {
		System.out.println("Process " + fileName + "...");
		super.process(fileName);
		convertToTg(getFilenameSchema());
	}

	private void convertToTg(String filename) {
		qnMap = new HashMap<String, Vertex>();
		packageMap = new HashMap<String, Package>();
		domainMap = new HashMap<String, Domain>();
		profileIdMap = new HashMap<String, Domain>();
		xmiIdMap = new HashMap<String, Vertex>();

		xu = new XmlGraphUtilities(getXmlGraph());
		Element model = xu.firstChildWithName(
				xu.firstChildWithName(xu.getRootElement(), "XMI.content"),
				"UML:Model");
		String schemaName = xu.getAttributeValue(model, "name");

		sg = GrumlSchema.instance().createSchemaGraph(
				ImplementationType.STANDARD,
				schemaName + "#" + xu.getAttributeValue(model, "xmi.id"), 100,
				100);

		schema = sg.createSchema();
		int p = schemaName.lastIndexOf('.');
		if (p == -1) {
			throw new ProcessingException(getParser(), getFileName(),
					"A Schema must have a package prefix!\nProcessed qualified name: "
							+ schemaName);
		}
		schema.set_name(schemaName.substring(p + 1));
		schema.set_packagePrefix(schemaName.substring(0, p));

		defaultPackage = sg.createPackage();
		defaultPackage.set_qualifiedName("");
		sg.createContainsDefaultPackage(schema, defaultPackage);
		packageMap.put("", defaultPackage);

		createPrimitiveDomains();
		createEnumDomains();
		createRecordDomains();
		createGraphClass();
		createVertexClasses();
		createEdgeClasses();
		createGeneralizations();
		createCommentsAndConstraints();

		removeRedundantGeneralization();
		checkAttributes();

		if (isRemoveUnusedDomains()) {
			removeUnusedDomains();
		}

		handlesEmptyPackages();

		if (!suppressOutput) {
			try {
				writeOutput();
			} catch (GraphIOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void checkAttributes() {
		GraphClass graphClass = sg.getFirstGraphClass();
		Map<String, AttributedElementClass> definedAttributes = new HashMap<String, AttributedElementClass>();
		for (Attribute a : graphClass.get_attribute()) {
			if (definedAttributes.containsKey(a)) {
				throw new RuntimeException("Attribute " + a.get_name() + " at "
						+ graphClass.get_qualifiedName() + " is duplicate.");
			}
			definedAttributes.put(a.get_name(), graphClass);
		}

		for (GraphElementClass gec : sg.getGraphElementClassVertices()) {
			boolean isVertexClass = gec.isInstanceOf(VertexClass.VC);
			definedAttributes = new HashMap<String, AttributedElementClass>();
			BooleanGraphMarker alreadyChecked = new BooleanGraphMarker(sg);
			Queue<GraphElementClass> queue = new LinkedList<GraphElementClass>();
			queue.add(gec);
			while (!queue.isEmpty()) {
				GraphElementClass current = queue.poll();
				if (alreadyChecked.isMarked(current)) {
					continue;
				}
				for (Attribute att : current.get_attribute()) {
					if (definedAttributes.containsKey(att.get_name())) {
						AttributedElementClass childClass = definedAttributes
								.get(att.get_name());
						throw new RuntimeException(
								"The name of the "
										+ (childClass == gec && current != gec ? ""
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

	private void removeRedundantGeneralization() {
		for (GraphElementClass gec : sg.getGraphElementClassVertices()) {
			boolean isVertexClass = gec.isInstanceOf(VertexClass.VC);
			if (gec.getDegree(isVertexClass ? SpecializesVertexClass.EC
					: SpecializesEdgeClass.EC, EdgeDirection.OUT) <= 1) {
				continue;
			}
			// perform a breadth first search towards the superclasses in the
			// generalization hierarchy.
			Queue<GraphElementClass> queue = new LinkedList<GraphElementClass>();
			IntegerVertexMarker longestDistance = new IntegerVertexMarker(sg);
			GraphMarker<Edge> startGeneralization = new GraphMarker<Edge>(sg);

			longestDistance.mark(gec, 0);
			// initialize marker
			Edge spezializesEdgeClass = gec.getFirstIncidence(
					isVertexClass ? SpecializesVertexClass.EC
							: SpecializesEdgeClass.EC, EdgeDirection.OUT);
			while (spezializesEdgeClass != null) {
				Edge next = spezializesEdgeClass.getNextIncidence(
						isVertexClass ? SpecializesVertexClass.EC
								: SpecializesEdgeClass.EC, EdgeDirection.OUT);
				GraphElementClass directSuperClass = (GraphElementClass) spezializesEdgeClass
						.getThat();
				if (directSuperClass == this) {
					spezializesEdgeClass.delete();
				} else if (longestDistance.isMarked(directSuperClass)) {
					// there exists two generalizations to the same direct
					// superclass
					spezializesEdgeClass.delete();
				} else {
					queue.add(directSuperClass);
					longestDistance.mark(directSuperClass, 1);
					startGeneralization.mark(directSuperClass,
							spezializesEdgeClass);
				}
				spezializesEdgeClass = next;
			}

			// perform a breadth first search
			while (!queue.isEmpty()) {
				GraphElementClass current = queue.poll();
				Edge toSuperClass = current.getFirstIncidence(
						isVertexClass ? SpecializesVertexClass.EC
								: SpecializesEdgeClass.EC, EdgeDirection.OUT);
				int distance = longestDistance.get(current) + 1;
				Edge startGen = startGeneralization.get(current);
				while (toSuperClass != null) {
					Edge next = toSuperClass.getNextIncidence(
							isVertexClass ? SpecializesVertexClass.EC
									: SpecializesEdgeClass.EC,
							EdgeDirection.OUT);
					GraphElementClass superClass = (GraphElementClass) toSuperClass
							.getThat();
					if (longestDistance.isMarked(superClass)) {
						int prevDistance = longestDistance.getMark(superClass);
						if (distance > prevDistance) {
							// a longer specialization from gec to superClass is
							// found
							queue.add(superClass);
							longestDistance.mark(superClass, distance);
							startGeneralization.mark(superClass, startGen);
						}
					} else {
						queue.add(superClass);
						longestDistance.mark(superClass, distance);
						startGeneralization.mark(superClass, startGen);
					}
					toSuperClass = next;
				}
			}

			// if a specializes edge (e) of gec is not used as a
			// startGeneralization, then it is a redundant generalization
			// because there exist already a generalization of a subclass of
			// e.getOmega();
			Set<Edge> importantSpecializations = new HashSet<Edge>();
			Iterator<FunctionEntry<AttributedElement<?, ?>, Edge>> it = startGeneralization
					.iterator();
			while (it.hasNext()) {
				FunctionEntry<AttributedElement<?, ?>, Edge> entry = it.next();
				importantSpecializations.add(entry.getSecond());
			}
			spezializesEdgeClass = gec.getFirstIncidence(
					isVertexClass ? SpecializesVertexClass.EC
							: SpecializesEdgeClass.EC, EdgeDirection.OUT);
			while (spezializesEdgeClass != null) {
				Edge next = spezializesEdgeClass.getNextIncidence(
						isVertexClass ? SpecializesVertexClass.EC
								: SpecializesEdgeClass.EC, EdgeDirection.OUT);
				if (!importantSpecializations.contains(spezializesEdgeClass)) {
					spezializesEdgeClass.delete();
				}
				spezializesEdgeClass = next;
			}

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
				logger.fine("...remove unused domain '" + d.get_qualifiedName()
						+ "'");

				// remove possible comments
				List<? extends Comment> comments = d.remove_comment();
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
	 * Writes a DOT file and a TG file out.
	 * 
	 * @throws XMLStreamException
	 * @throws GraphIOException
	 */
	public void writeOutput() throws GraphIOException {

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
	private boolean validateGraph(String filePath) {
		if (filePath != null) {
			printTypeAndFilename("validation report", filePath);
		}
		System.out.println("Validate schema graph...");
		GraphValidator gv = new GraphValidator(sg);
		Set<ConstraintViolation> violations = gv.validate();
		if (violations.size() > 0) {
			try {
				System.out.println("Schema graph is invalid. Please look at "
						+ filePath);
				gv.createValidationReport(filePath);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Creates empty {@link Package} objects from the {@link SchemaGraph}.
	 */
	private void handlesEmptyPackages() {
		// create empty packages except the default package
		if (!isKeepEmptyPackages()) {
			System.out.println("Remove empty packages...");
			Package p = sg.getFirstPackage();
			int removed = 0;
			while (p != null) {
				Package n = p.getNextPackage();
				int commentCount = p.getDegree(Annotates.EC);
				if (((p.getDegree() - commentCount) == 1)
						&& (p.get_qualifiedName().length() > 0)) {
					logger.fine("\t- empty package '"
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
			logger.fine("Removed " + removed + " package"
					+ (removed == 1 ? "" : "s") + ".");
		} else {
			System.out.println("Create empty packages...");
			for (Element el : xu.elementsWithName("UML:Package")) {
				String qn = getQualifiedName(el, false);
				if (!packageMap.containsKey(qn)) {
					logger.fine("Created empty package " + qn + ".");
				}
				getPackage(qn);
			}
		}
	}

	private void createCommentsAndConstraints() {
		for (Element el : xu.elementsWithName("UML:Comment")) {
			if (!xu.hasAttribute(el, "body")) {
				continue;
			}
			NamedElement annotatedNamedElement = null;
			Element annotatedElement = xu.firstChildWithName(el,
					"UML:Comment.annotatedElement");
			if (annotatedElement == null) {
				annotatedNamedElement = graphClass;
			} else {
				assert annotatedElement.getDegree(HasChild.EC,
						EdgeDirection.OUT) == 1;
				annotatedElement = (Element) annotatedElement
						.getFirstHasChildIncidence(EdgeDirection.OUT).getThat();
				assert annotatedElement.get_name().equals("UML:Class")
						|| annotatedElement.get_name().equals(
								"UML:AssociationClass")
						|| annotatedElement.get_name()
								.equals("UML:Association")
						|| annotatedElement.get_name()
								.equals("UML:Enumeration")
						|| annotatedElement.get_name().equals("UML:Package");
				annotatedElement = xu.getReferencedElement(annotatedElement,
						"xmi.idref");
				assert annotatedElement != null;

				String qn = getQualifiedName(annotatedElement,
						!annotatedElement.get_name().equals("UML:Package"));
				annotatedNamedElement = (NamedElement) qnMap.get(qn);
				if (annotatedNamedElement == null) {
					annotatedNamedElement = graphClass;
				}
			}

			String commentContent = xu.getAttributeValue(el, "body");
			if (!annotatedNamedElement.isInstanceOf(AttributedElementClass.VC)
					|| !addGreqlConstraint(
							(AttributedElementClass) annotatedNamedElement,
							commentContent)) {
				Comment comment = sg.createComment();
				comment.set_text(commentContent);
				sg.createAnnotates(comment, annotatedNamedElement);
			}
		}
	}

	/**
	 * Adds a Greql constraint to a {@link AttributedElementClass} object.
	 * 
	 * @param constrainedClass
	 *            {@link AttributedElementClass}, which should be constraint.
	 * @param text
	 *            Constraint as String.
	 */
	private boolean addGreqlConstraint(AttributedElementClass constrainedClass,
			String text) {
		text = text.trim();
		if (!(text.startsWith("{") && text.endsWith("}"))) {
			return false;
		}
		text = text.substring(1, text.length() - 1).trim();

		assert constrainedClass != null;
		Constraint constraint = sg.createConstraint();

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
						// this was no constraint
						constraint.delete();
						return false;
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
						// this was no constraint
						constraint.delete();
						return false;
					}
				}
			}
		}
		if (inString || escape || (stringCount < 2) || (stringCount > 3)) {
			// this was no constraint
			constraint.delete();
			return false;
		}
		sg.createHasConstraint(constrainedClass, constraint);
		return true;
	}

	private void createGeneralizations() {
		logger.fine("Creating generalization:");
		for (Element el : xu.elementsWithName("UML:Generalization")) {
			if (!xu.hasAttribute(el, "xmi.id")) {
				continue;
			}
			Element child = xu.firstChildWithName(el,
					"UML:Generalization.child");
			Element e = xu.firstChildWithName(child, "UML:Class");
			if (e == null) {
				e = xu.firstChildWithName(child, "UML:Association");
			}
			if (e == null) {
				e = xu.firstChildWithName(child, "UML:AssociationClass");
			}
			if (e == null) {
				throw new RuntimeException("Unexpected Generalization child "
						+ el);
			}
			child = e;

			Vertex cv = xmiIdMap.get(xu.getAttributeValue(child, "xmi.idref"));
			assert cv != null;

			Element parent = xu.firstChildWithName(el,
					"UML:Generalization.parent");
			e = xu.firstChildWithName(parent, "UML:Class");
			if (e == null) {
				e = xu.firstChildWithName(parent, "UML:Association");
			}
			if (e == null) {
				e = xu.firstChildWithName(parent, "UML:AssociationClass");
			}
			if (e == null) {
				throw new RuntimeException("Unexpected Generalization parent "
						+ el);
			}
			parent = e;

			Vertex pv = xmiIdMap.get(xu.getAttributeValue(parent, "xmi.idref"));
			assert pv != null;

			assert cv.getAttributedElementClass() == pv
					.getAttributedElementClass();

			if (pv.isInstanceOf(VertexClass.VC)) {
				sg.createSpecializesVertexClass((VertexClass) cv,
						(VertexClass) pv);
				logger.fine("\t" + ((VertexClass) cv).get_qualifiedName()
						+ ": " + ((VertexClass) pv).get_qualifiedName());
			} else if (pv.isInstanceOf(EdgeClass.VC)) {
				logger.fine("\t" + ((EdgeClass) cv).get_qualifiedName() + ": "
						+ ((EdgeClass) pv).get_qualifiedName());
				EdgeClass sub = (EdgeClass) cv;
				EdgeClass sup = (EdgeClass) pv;
				sg.createSpecializesEdgeClass(sub, sup);
			} else {
				throw new RuntimeException("Unexpected generalization between "
						+ pv.getSchemaClass().getName() + " vertices.");
			}

		}
	}

	private void createGraphClass() {
		for (Element el : xu.elementsWithName("UML:Class")) {
			if (hasStereotype(el, ST_GRAPHCLASS)) {
				assert !hasStereotype(el, ST_ABSTRACT);
				assert !hasStereotype(el, ST_RECORD);
				if (graphClass != null) {
					throw new RuntimeException(
							"Multiple classes marked with <<graphclass>>, only one is allowed. Offending element: "
									+ el
									+ " (name="
									+ xu.getAttributeValue(el, "name") + ")");
				}
				graphClass = sg.createGraphClass();
				sg.createDefinesGraphClass(schema, graphClass);
				graphClass.set_qualifiedName(xu.getAttributeValue(el, "name",
						true));
				qnMap.put(graphClass.get_qualifiedName(), graphClass);
				xmiIdMap.put(xu.getAttributeValue(el, "xmi.id"), graphClass);

				logger.fine("GraphClass " + graphClass.get_qualifiedName());
				createAttributes(el, graphClass);
			}
		}
	}

	private void createEdgeClasses() {
		for (Element el : xu.elementsWithName("UML:Association")) {
			if (xu.hasAttribute(el, "xmi.id") && !isDerived(el)) {
				createEdgeClass(el);
			}
		}
		for (Element el : xu.elementsWithName("UML:AssociationClass")) {
			if (xu.hasAttribute(el, "xmi.id") && !isDerived(el)) {
				createEdgeClass(el);
			}
		}
	}

	private void createEdgeClass(Element el) {
		Package pkg = getPackage(getPackageName(el));
		String qn = getQualifiedName(el, true);
		assert qn != null && !qn.isEmpty() : "The EdgeClass "
				+ xu.getAttributeValue(el, "xmi.id") + " must have a name.";
		String name = xu.getAttributeValue(el, "name", true);
		if (name.length() == 0) {
			name = null;
		}

		boolean isAbstract = hasStereotype(el, ST_ABSTRACT)
				|| (xu.hasAttribute(el, "isAbstract") && xu.getAttributeValue(
						el, "isAbstract").equals("true"));

		logger.fine((isAbstract ? "abstract " : "") + "EdgeClass "
				+ (name != null ? qn : "<<name will be generated>>"));

		EdgeClass ec = sg.createEdgeClass();
		ec.set_abstract(isAbstract);
		sg.createContainsGraphElementClass(pkg, ec);
		createAttributes(el, ec);

		Element conn = xu.firstChildWithName(el, "UML:Association.connection");
		assert conn != null;
		Iterator<Element> it = xu.childrenWithName(conn, "UML:AssociationEnd")
				.iterator();
		assert it.hasNext();
		Element from = it.next();
		IncidenceClass icFrom = createIncidenceClass(from);
		assert it.hasNext();
		Element to = it.next();
		IncidenceClass icTo = createIncidenceClass(to);

		if (useNavigability) {
			boolean fn = xu.hasAttribute(from, "isNavigable")
					&& xu.getAttributeValue(from, "isNavigable").equals("true");

			boolean tn = xu.hasAttribute(to, "isNavigable")
					&& xu.getAttributeValue(to, "isNavigable").equals("true");

			if (fn && !tn) {
				IncidenceClass tmp = icFrom;
				icFrom = icTo;
				icTo = tmp;
			}
		}

		// flip aggregation types, since ArgoUML seems to annotate the opposite
		// end
		AggregationKind tmp = icFrom.get_aggregation();
		icFrom.set_aggregation(icTo.get_aggregation());
		icTo.set_aggregation(tmp);

		ec.add_from(icFrom);
		ec.add_to(icTo);

		// generate EdgeClass name if none is present
		if (name == null) {
			String toRole = icTo.get_roleName();
			if ((toRole == null) || toRole.equals("")) {
				toRole = ((VertexClass) icTo.getFirstEndsAtIncidence()
						.getThat()).get_qualifiedName();
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
				throw new RuntimeException(
						"Undefined 'to' role name for EdgeClass " + ec);
			}

			if ((icFrom.get_aggregation() != AggregationKind.NONE)
					|| (icTo.get_aggregation() != AggregationKind.NONE)) {
				if (icTo.get_aggregation() != AggregationKind.NONE) {
					name = "Contains" + toRole;
				} else {
					name = "IsPartOf" + toRole;
				}
			} else {
				name = "LinksTo" + toRole;
			}

			if (useFromRole) {
				String fromRole = icFrom.get_roleName();
				if ((fromRole == null) || fromRole.equals("")) {
					fromRole = ((VertexClass) icFrom.getFirstEndsAtIncidence()
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
				if ((fromRole == null) || (fromRole.length() == 0)) {
					throw new RuntimeException(
							"Undefined 'from' role name for EdgeClass " + ec);
				}
				name = fromRole + name;
			}
			qn = pkg.get_qualifiedName() + "." + name;
		}
		ec.set_qualifiedName(qn);
		assert qnMap.get(qn) == null : "There already exists an EdgeClass with name: "
				+ qn;
		qnMap.put(qn, ec);
		xmiIdMap.put(xu.getAttributeValue(el, "xmi.id"), ec);
	}

	private IncidenceClass createIncidenceClass(Element associationEnd) {
		IncidenceClass ic = sg.createIncidenceClass();

		// role name
		String role = xu.hasAttribute(associationEnd, "name") ? xu
				.getAttributeValue(associationEnd, "name") : "";
		ic.set_roleName(role.trim());

		// aggregation kind
		String aggregation = xu
				.getAttributeValue(associationEnd, "aggregation");
		if (aggregation.equals("none")) {
			ic.set_aggregation(AggregationKind.NONE);
		} else if (aggregation.equals("aggregate")) {
			ic.set_aggregation(AggregationKind.SHARED);
		} else if (aggregation.equals("composite")) {
			ic.set_aggregation(AggregationKind.COMPOSITE);
		} else {
			throw new RuntimeException("Unexpected aggregation value '"
					+ aggregation + "'");
		}

		// multiplicity
		int min = 0;
		int max = Integer.MAX_VALUE;
		Element mult = xu.firstChildWithName(associationEnd,
				"UML:AssociationEnd.multiplicity");
		if (mult != null) {
			mult = xu.firstChildWithName(mult, "UML:Multiplicity");
		}
		if (mult != null) {
			mult = xu.firstChildWithName(mult, "UML:Multiplicity.range");
		}
		if (mult != null) {
			mult = xu.firstChildWithName(mult, "UML:MultiplicityRange");
		}
		if (mult != null) {
			min = Integer.parseInt(xu.getAttributeValue(mult, "lower"));
			max = Integer.parseInt(xu.getAttributeValue(mult, "upper"));
			if (max < 0) {
				max = Integer.MAX_VALUE;
			}
		}
		ic.set_min(min);
		ic.set_max(max);

		// target vertex class
		Element participant = xu.firstChildWithName(associationEnd,
				"UML:AssociationEnd.participant");
		assert participant != null;
		VertexClass vc = (VertexClass) xmiIdMap.get(xu.getAttributeValue(
				xu.firstChildWithName(participant, "UML:Class"), "xmi.idref"));
		ic.add_targetclass(vc);
		checkMultiplicities(ic);
		return ic;
	}

	private void checkMultiplicities(IncidenceClass inc) {
		int min = inc.get_min();
		int max = inc.get_max();
		assert min >= 0;
		assert max > 0;
		if (min == Integer.MAX_VALUE) {
			throw new ProcessingException(getFileName(),
					"Error in multiplicities: lower bound must not be *"
							+ " at association end " + inc);
		}
		if (min > max) {
			throw new ProcessingException(getFileName(),
					"Error in multiplicities: lower bound (" + min
							+ ") must be <= upper bound (" + max
							+ ") at association end " + inc);
		}
	}

	private void createVertexClasses() {
		for (Element el : xu.elementsWithName("UML:Class")) {
			if (xu.hasAttribute(el, "name") && !hasStereotype(el, ST_RECORD)
					&& !hasStereotype(el, ST_GRAPHCLASS) && !isDerived(el)) {

				boolean isAbstract = hasStereotype(el, ST_ABSTRACT)
						|| (xu.hasAttribute(el, "isAbstract") && xu
								.getAttributeValue(el, "isAbstract").equals(
										"true"));

				Package pkg = getPackage(getPackageName(el));
				String qn = getQualifiedName(el, true);
				assert qn != null && !qn.isEmpty() : "The VertexClass "
						+ xu.getAttributeValue(el, "xmi.id")
						+ " must have a name.";

				assert qnMap.get(qn) == null;

				logger.fine((isAbstract ? "abstract " : "") + "VertexClass "
						+ qn);

				VertexClass vc = sg.createVertexClass();
				vc.set_qualifiedName(qn);
				vc.set_abstract(isAbstract);
				sg.createContainsGraphElementClass(pkg, vc);
				qnMap.put(qn, vc);
				xmiIdMap.put(xu.getAttributeValue(el, "xmi.id"), vc);
				createAttributes(el, vc);
			}
		}
	}

	private void createAttributes(Element el, AttributedElementClass aec) {
		Element sf = xu.firstChildWithName(el, "UML:Classifier.feature");
		if (sf != null) {
			for (Element at : xu.childrenWithName(sf, "UML:Attribute")) {
				if (isDerived(at)) {
					continue;
				}
				Domain dom = getAttributeDomain(at);
				assert dom != null;
				logger.fine("\t" + xu.getAttributeValue(at, "name") + ": "
						+ dom.get_qualifiedName());
				Attribute attr = sg.createAttribute();
				attr.set_name(xu.getAttributeValue(at, "name"));
				attr.set_defaultValue(getDefaultValue(at, dom));
				attr.add_domain(dom);
				aec.add_attribute(attr);
			}
		}
	}

	private boolean isDerived(Element element) {
		Element modelElementTaggedValue = xu.firstChildWithName(element,
				"UML:ModelElement.taggedValue");
		if (modelElementTaggedValue == null) {
			return false;
		}
		for (Element taggedValue : xu.childrenWithName(modelElementTaggedValue,
				"UML:TaggedValue")) {
			Element taggedValueType = xu.firstChildWithName(taggedValue,
					"UML:TaggedValue.type");
			Element tagDefinition = xu.firstChildWithName(taggedValueType,
					"UML:TagDefinition");
			if (!xu.getAttributeValue(tagDefinition, "href").endsWith(
					"#" + TV_UML_DERIVED)) {
				continue;
			}
			Element taggedValueDataValue = xu.firstChildWithName(taggedValue,
					"UML:TaggedValue.dataValue");
			return !xu.getText(taggedValueDataValue).equals("false");
		}
		return false;
	}

	private String getDefaultValue(Element attribute, Domain domain) {
		Element defaultValue = xu.firstChildWithName(attribute,
				"UML:Attribute.initialValue");
		if (defaultValue == null) {
			return null;
		}
		Element defaultValueExpression = xu.firstChildWithName(defaultValue,
				"UML:Expression");
		assert defaultValueExpression != null;
		String value = xu.getAttributeValue(defaultValueExpression, "body");
		if (value != null) {
			if (domain.isInstanceOf(BooleanDomain.VC)) {
				assert value.equals("true") || value.equals("false");
				// true/false => t/f
				value = value.substring(0, 1);
			} else if (domain.isInstanceOf(StringDomain.VC)) {
				if (!value.startsWith("\"")) {
					value = "\"" + value + "\"";
				}
			}
			if (value.equals("null")) {
				value = "n";
			}
		}
		return value;
	}

	private void createRecordDomains() {
		for (Element el : xu.elementsWithName("UML:Class")) {
			if (hasStereotype(el, ST_RECORD)) {
				assert !hasStereotype(el, ST_GRAPHCLASS);
				assert !hasStereotype(el, ST_ABSTRACT);

				Package pkg = getPackage(getPackageName(el));
				String qn = getQualifiedName(el, true);
				assert qn != null && !qn.isEmpty() : "The RecordDomain "
						+ xu.getAttributeValue(el, "xmi.id")
						+ " must have a name.";

				// check whether a prelminiary vertex for this record domain was
				// already created, create it if not
				RecordDomain rd = (RecordDomain) domainMap.get(qn);
				if (rd == null) {
					assert qnMap.get(qn) == null;
					rd = sg.createRecordDomain();
					rd.set_qualifiedName(qn);
					sg.createContainsDomain(pkg, rd);
					qnMap.put(qn, rd);
					domainMap.put(qn, rd);
					xmiIdMap.put(xu.getAttributeValue(el, "xmi.id"), rd);
				}

				logger.fine("RecordDomain " + qn);

				Element sf = xu
						.firstChildWithName(el, "UML:Classifier.feature");
				if (sf != null) {
					for (Element at : xu.childrenWithName(sf, "UML:Attribute")) {
						logger.fine("\t" + xu.getAttributeValue(at, "name"));
						Domain dom = getAttributeDomain(at);
						assert dom != null;
						HasRecordDomainComponent c = sg
								.createHasRecordDomainComponent(rd, dom);
						c.set_name(xu.getAttributeValue(at, "name"));
					}
				}
			}
		}
	}

	private Domain getAttributeDomain(Element attr) {
		Element type = xu
				.firstChildWithName(attr, "UML:StructuralFeature.type");
		assert type != null;
		Element dt = xu.firstChildWithName(type, "UML:DataType");
		if (dt != null) {
			// UML primitive type
			// - either defined in this xmi (xmi.idref), then it has to be a
			// grUML composite domain
			// - or defined in profile (href)
			if (xu.hasAttribute(dt, "xmi.idref")) {
				Element dom = xu.getReferencedElement(dt, "xmi.idref");
				return createCompositeDomain(xu.getAttributeValue(dom, "name"));
			} else if (xu.hasAttribute(dt, "href")) {
				return getPrimitiveDomainByProfileId(xu.getAttributeValue(dt,
						"href"));
			} else {
				throw new RuntimeException("FIXME: Unhandled UML:DataType");
			}
		}
		dt = xu.firstChildWithName(type, "UML:Enumeration");
		if (dt != null) {
			// domain is an enumeration
			return (Domain) xmiIdMap.get(xu.getAttributeValue(dt, "xmi.idref"));
		}
		dt = xu.firstChildWithName(type, "UML:Class");
		if (dt != null) {
			// domain is a record domain
			Vertex v = xmiIdMap.get(xu.getAttributeValue(dt, "xmi.idref"));
			if (v != null) {
				// record domain already exists
				assert v.isInstanceOf(RecordDomain.VC);
				return (Domain) v;
			}
			// otherwise create a preliminary vertex without the components
			Element el = xu.getReferencedElement(dt, "xmi.idref");
			assert el.get_name().equals("UML:Class")
					&& hasStereotype(el, ST_RECORD)
					&& !hasStereotype(el, ST_ABSTRACT)
					&& !hasStereotype(el, ST_GRAPHCLASS) : dt;
			Package pkg = getPackage(getPackageName(el));
			String qn = getQualifiedName(el, true);
			assert qn != null && !qn.isEmpty() : "The domain of attribute "
					+ xu.getAttributeValue(el, "xmi.id") + " must have a name.";

			RecordDomain rd = sg.createRecordDomain();
			rd.set_qualifiedName(qn);
			domainMap.put(qn, rd);
			qnMap.put(qn, rd);
			xmiIdMap.put(xu.getAttributeValue(el, "xmi.id"), rd);
			sg.createContainsDomain(pkg, rd);
			return rd;
		}
		throw new RuntimeException("FIXME: Unhandled UML:DataType");
	}

	private Domain createCompositeDomain(String name) {
		name = name.trim().replaceAll("\\s+", "");
		Domain dom = domainMap.get(name);
		if (dom != null) {
			return dom;
		}
		logger.fine("CompositeDomain " + name);
		if (name.startsWith("Map<")) {
			MapDomain md = sg.createMapDomain();
			dom = md;
			int p = 4;
			int b = 0;
			while (p < name.length()) {
				char c = name.charAt(p);
				if (b == 0 && c == ',') {
					break;
				}
				if (c == '<') {
					++b;
				} else if (c == '>') {
					--b;
				}
				++p;
			}
			String keyName = name.substring(4, p);
			String valName = name.substring(p + 1, name.length() - 1);
			md.add_keydomain(createCompositeDomain(keyName));
			md.add_valuedomain(createCompositeDomain(valName));
		} else if (name.startsWith("List<")) {
			ListDomain ld = sg.createListDomain();
			dom = ld;
			String compName = name.substring(5, name.length() - 1);
			ld.add_basedomain(createCompositeDomain(compName));
		} else if (name.startsWith("Set<")) {
			SetDomain sd = sg.createSetDomain();
			dom = sd;
			String compName = name.substring(4, name.length() - 1);
			sd.add_basedomain(createCompositeDomain(compName));
		} else {
			throw new RuntimeException("Unknown domain name '" + name + "'");
		}
		dom.set_qualifiedName(name.replaceAll(",", ", "));
		domainMap.put(name, dom);
		sg.createContainsDomain(defaultPackage, dom);
		return dom;
	}

	private Domain getPrimitiveDomainByProfileId(String href) {
		int p = href.indexOf('#');
		if (p >= 0) {
			href = href.substring(p + 1);
		}
		return profileIdMap.get(href);
	}

	private boolean hasStereotype(Element el, String st_id) {
		Element st = xu.firstChildWithName(el, "UML:ModelElement.stereotype");
		if (st == null) {
			return false;
		}
		st = xu.firstChildWithName(st, "UML:Stereotype");
		return xu.getAttributeValue(st, "href").endsWith("#" + st_id);
	}

	private void createPrimitiveDomains() {
		createPrimitiveDomain(sg.createBooleanDomain(), "Boolean", DT_BOOLEAN);
		createPrimitiveDomain(sg.createIntegerDomain(), "Integer", DT_INTEGER,
				DT_UML_INTEGER);
		createPrimitiveDomain(sg.createLongDomain(), "Long", DT_LONG);
		createPrimitiveDomain(sg.createDoubleDomain(), "Double", DT_DOUBLE);
		createPrimitiveDomain(sg.createStringDomain(), "String", DT_STRING,
				DT_UML_STRING);
	}

	private void createPrimitiveDomain(Domain d, String qn,
			String... profileIds) {
		logger.fine("PrimitiveDomain " + qn);
		d.set_qualifiedName(qn);
		sg.createContainsDomain(packageMap.get(""), d);
		for (String id : profileIds) {
			profileIdMap.put(id, d);
		}
		domainMap.put(qn, d);
		qnMap.put(qn, d);
	}

	private void createEnumDomains() {
		for (Element el : xu.elementsWithName("UML:Enumeration")) {
			if (!xu.hasAttribute(el, "xmi.id")) {
				continue;
			}
			String qn = getQualifiedName(el, true);
			assert qn != null && !qn.isEmpty() : "The EnumDomain "
					+ xu.getAttributeValue(el, "xmi.id") + " must have a name.";
			assert qnMap.get(qn) == null;

			logger.fine("EnumDomain " + qn);
			EnumDomain ed = sg.createEnumDomain();
			ed.set_qualifiedName(qn);
			domainMap.put(qn, ed);
			qnMap.put(qn, ed);
			xmiIdMap.put(xu.getAttributeValue(el, "xmi.id"), ed);

			sg.createContainsDomain(getPackage(getPackageName(el)), ed);

			Element literals = xu.firstChildWithName(el,
					"UML:Enumeration.literal");
			PVector<String> constants = JGraLab.vector();
			for (Element enumLiteral : xu.childrenWithName(literals,
					"UML:EnumerationLiteral")) {
				String cn = xu.getAttributeValue(enumLiteral, "name");
				logger.fine("\t" + cn);
				constants = constants.plus(cn);
			}
			ed.set_enumConstants(constants);
		}
	}

	private String getQualifiedName(Element el, boolean upperCaseFirstLetter) {
		String pkgName = getPackageName(el);
		Package pkg = getPackage(pkgName);
		String name = xu.getAttributeValue(el, "name", upperCaseFirstLetter);
		return (pkg == defaultPackage ? name : pkg.get_qualifiedName() + "."
				+ name);
	}

	private Package getPackage(String qn) {
		Package pkg = packageMap.get(qn);
		if (pkg == null) {
			if (qn.length() == 0) {
				pkg = packageMap.get("");
			} else {
				pkg = sg.createPackage();
				pkg.set_qualifiedName(qn);

				int p = qn.lastIndexOf('.');
				Package parentPackage = getPackage(p < 0 ? "" : qn.substring(0,
						p));
				parentPackage.add_subpackage(pkg);
				packageMap.put(qn, pkg);
				qnMap.put(qn, pkg);
			}
		}
		return pkg;
	}

	private String getPackageName(Element el) {
		String result = "";
		el = el.get_parent();
		while (el != null) {
			if (el.get_name().equals("UML:Package")) {
				if (result.length() == 0) {
					result = xu.getAttributeValue(el, "name");
				} else {
					result = xu.getAttributeValue(el, "name") + "." + result;
				}
			}
			el = el.get_parent();
		}
		return result;
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
