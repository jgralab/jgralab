/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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
package de.uni_koblenz.jgralab.utilities.xml;

import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_ATTRIBUTEDELEMENTTYPE;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_ATTRIBUTE_FROM;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_ATTRIBUTE_FSEQ;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_ATTRIBUTE_ID;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_ATTRIBUTE_TO;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_ATTRIBUTE_TSEQ;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_DOMAIN_BOOLEAN;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_DOMAIN_DOUBLE;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_DOMAIN_ENUM_PREFIX;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_DOMAIN_INTEGER;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_DOMAIN_LIST;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_DOMAIN_LONG;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_DOMAIN_MAP;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_DOMAIN_RECORD_PREFIX;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_DOMAIN_SET;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_DOMAIN_STRING;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_EDGETYPE;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_GRAPHTYPE;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_PREFIX_EDGETYPE;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_PREFIX_GRAPHTYPE;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_PREFIX_VERTEXTYPE;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_VALUE_NULL;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_VERTEXTYPE;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XML_DOMAIN_ID;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XML_DOMAIN_IDREF;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XML_VALUE_TRUE;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XSD_ATTRIBUTE;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XSD_ATTRIBUTE_ABSTRACT;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XSD_ATTRIBUTE_BASE;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XSD_ATTRIBUTE_NAME;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XSD_ATTRIBUTE_TYPE;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XSD_ATTRIBUTE_USE;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XSD_ATTRIBUTE_VALUE;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XSD_CHOICE;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XSD_COMPLEXCONTENT;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XSD_COMPLEXTYPE;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XSD_DOMAIN_DOUBLE;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XSD_DOMAIN_INTEGER;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XSD_DOMAIN_LONG;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XSD_DOMAIN_STRING;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XSD_ELEMENT;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XSD_ENUMERATION;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XSD_EXTENSION;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XSD_NAMESPACE_PREFIX;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XSD_PATTERN;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XSD_REQUIRED;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XSD_RESTRICTION;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XSD_SCHEMA;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XSD_SIMPLETYPE;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XSD_VALUE_MAX_OCCURS;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XSD_VALUE_MIN_OCCURS;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;

import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.grumlschema.GrumlSchema;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.grumlschema.domains.BooleanDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.Domain;
import de.uni_koblenz.jgralab.grumlschema.domains.DoubleDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.EnumDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.HasRecordDomainComponent;
import de.uni_koblenz.jgralab.grumlschema.domains.IntegerDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.ListDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.LongDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.MapDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.RecordDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.SetDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.StringDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.Attribute;
import de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.EdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.HasAttribute;
import de.uni_koblenz.jgralab.grumlschema.structure.Schema;
import de.uni_koblenz.jgralab.grumlschema.structure.SpecializesEdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.SpecializesVertexClass;
import de.uni_koblenz.jgralab.grumlschema.structure.VertexClass;
import de.uni_koblenz.jgralab.impl.ProgressFunctionImpl;
import de.uni_koblenz.jgralab.utilities.common.OptionHandler;
import de.uni_koblenz.jgralab.utilities.common.SchemaFilter;
import de.uni_koblenz.jgralab.utilities.common.UtilityMethods;
import de.uni_koblenz.jgralab.utilities.rsa2tg.SchemaGraph2Tg;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.Tg2SchemaGraph;

/**
 * This tool generates an XML schema according to a grUML schema. The details on
 * how this mapping is done can be read in the text
 * "Abbildung von grUML nach XSD" by Eckhard Grossmann, Sascha Strauß, Volker
 * Riediger and Tassilo Horn.
 * 
 * This manual describes the usage of this tool, especially the control of the
 * include and exclude patterns.
 * 
 * The tool has a few required command line options:
 * 
 * -g --graph : This option specifies the input file containing the schemaGraph.
 * It takes one parameter which is the filename.
 * 
 * -s --schema: This option specifies the input file containing the Schema. It
 * takes one parameter which is the filename.
 * 
 * -n --namespace-prefix : These option specifies the namespace prefix. It takes
 * one parameter.
 * 
 * -o --output: This option specifies the output file. It takes one parameter
 * which is the filename.
 * 
 * 
 * With these standard options, the whole schema will be exported. To limit the
 * export, some optional arguments can be used:
 * 
 * -p --pattern-list : Takes a list of pattern of arbitrary length as
 * parameters. These patterns operate on EdgeClasses and VertexClasses. The
 * syntax is the syntax of Java's regular expressions. Each pattern has to start
 * with a "+" or a "-". Patterns with a "+" are positive patterns which specify
 * included Vertex- or EdgeClasses. Patterns with a "-" are negative patterns
 * which specify excluded Vertex- or EdgeClasses. The patters are processed in
 * the order they are given. If the first pattern is a positive pattern, the
 * whole schema is excluded by default. If the first pattern is a negative
 * pattern, the whole schema is included by default.
 * 
 * In case of included but abstract VertexClasses, additional excludes can
 * occur. If an included abstract VertexClass has no included non-abstract
 * VertexClass, it is excluded. The XSD will not contain any abstract Vertex- or
 * EdgeClass, but for for performing this pattern including or excluding
 * correctly, they have to be included or excluded.
 * 
 * In case of EdgeClasses, additional excludes can occur. If an included
 * EdgeClass has an excluded VertexClass at the from- or to end, the EdgeClass
 * will be excluded.
 * 
 * -x --implicit-exclude : If this option is given, all subclasses of excluded
 * superclasses are excluded as well. This works for both, EdgeClasses and
 * VertexClasses.
 * 
 * -d --debug : Writes debug information about included and excluded
 * VertexClasses and EdgeClasses into a file or to stdout. If a filename is
 * given, the information will be written into a file, if not, it will be
 * written to stdout.
 * 
 * @author mmce@uni-koblenz.de
 * @author strauss@uni-koblenz.de
 */
@WorkInProgress(description = "Converter from SchemaGraph to XML Schema", responsibleDevelopers = "horn, mmce, riediger", expectedFinishingDate = "2009/06/30")
public class SchemaGraph2XSD {

	/**
	 * Stores String patterns for a RecordDomain in general.
	 */
	private static final String[] RECORD_DOMAIN_PATTERNS = new String[] {
			"\\(.*\\)", GRUML_VALUE_NULL };

	/**
	 * Stores String patterns for a MapDomain in general.
	 */
	private static final String[] MAP_DOMAIN_PATTERNS = new String[] {
			"\\{.*\\}", GRUML_VALUE_NULL };

	/**
	 * Stores String patterns for a SetDomain in general.
	 */
	private static final String[] SET_DOMAIN_PATTERNS = new String[] {
			"\\{.*\\}", GRUML_VALUE_NULL };

	/**
	 * Stores String patterns for a ListDomain in general.
	 */
	private static final String[] LIST_DOMAIN_PATTERNS = new String[] {
			"\\[.*\\]", GRUML_VALUE_NULL };

	/**
	 * Stores String patterns for a StringDomain in general.
	 */
	private static final String[] STRING_DOMAIN_PATTERNS = new String[] {
			"\".*\"", GRUML_VALUE_NULL };

	/**
	 * Defines the prefix for the XSD namespace.
	 */
	private static final String XSD_NS_PREFIX_PLUS_COLON = XSD_NAMESPACE_PREFIX
			.equals("") ? "" : XSD_NAMESPACE_PREFIX + ":";

	/**
	 * Defines a XMLStreamWriter, which writes all XML-data to a file.
	 */
	private final XMLStreamWriter xml;

	/**
	 * Holds the specific Schema, which should be converted, in a SchemaGraph.
	 */
	private final SchemaGraph schemaGraph;

	/**
	 * SchemaGraph2Tg is needed to provide tg-code for specific vertex- or
	 * edge-types.
	 */
	private final SchemaGraph2Tg sg2tg;

	/**
	 * Defines the a namespace prefix for all newly defined types of the given
	 * SchemaGraph.
	 */
	private final String namespacePrefix;

	/**
	 * Holds all patterns for including or excluding specific vertex- oder
	 * edge-types.
	 */
	private String[] patterns;

	/**
	 * Marks all included vertex-, edge- and domain-types.
	 */
	private BooleanGraphMarker includes;

	/**
	 * Stream output for debug information about include oder excludes types.
	 */
	private PrintStream debugOutputStream;

	/**
	 * Boolean flag for automatic exclusions.
	 */
	private boolean autoExclude = false;

	/**
	 * Links Domain-objects to existing enumeration types described by a String.
	 */
	private final Map<Domain, String> domainMap;

	private final Map<String, VertexClass> vertices;
	private final Map<String, EdgeClass> edges;

	/**
	 * Converts a given Schema from a TG-format into the XSD-format.
	 * 
	 * @param args
	 *            Command line parameters. For documentation look at
	 *            {@link SchemaGraph2XSD#processCommandLineOptions}.
	 * @throws GraphIOException
	 * @throws FactoryConfigurationError
	 * @throws XMLStreamException
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws GraphIOException,
			FileNotFoundException, XMLStreamException,
			FactoryConfigurationError {

		// Creates a CommandLine, in which all command line options have been
		// processed.
		CommandLine comLine = processCommandLineOptions(args);
		assert comLine != null;

		// Retrieving all necessary parameters filename.
		String inputFile = comLine.hasOption('g') ? comLine.getOptionValue('g')
				.trim() : comLine.getOptionValue('s');
		String namespacePrefix = comLine.getOptionValue('n').trim();
		String xsdFile = comLine.getOptionValue('o').trim();
		String[] rawPatterns = comLine.hasOption('p') ? comLine
				.getOptionValues('p') : null;

		// Loading the a Schema from the specified location. It will be
		// distinguished between an existing Schema as Graph or a Schema in a
		// tg-file.
		System.out.println("Loading Schema from file '" + inputFile + "'.");
		SchemaGraph sg = comLine.hasOption('g') ? GrumlSchema.instance()
				.loadSchemaGraph(inputFile, new ProgressFunctionImpl())
				: new Tg2SchemaGraph().process(inputFile);

		// SchemaGraph2XSD is instantiated for conversion.
		System.out.println("\nBeginning convertion:");
		SchemaGraph2XSD sg2xsd = new SchemaGraph2XSD(sg, namespacePrefix,
				xsdFile);

		// In- and exclude pattern are set.
		sg2xsd.setPatterns(rawPatterns);

		// Handling debug flag 'd'. Sets the appropriated stream for
		// debug-informations concerning include- and exclude-patterns.
		if (comLine.hasOption('d')) {
			String filename = comLine.getOptionValue('d');
			PrintStream output;
			if (filename == null) {
				output = System.out;
			} else {
				output = new PrintStream(new BufferedOutputStream(
						new FileOutputStream(filename)));
			}
			sg2xsd.setDebugOutputStream(output);
		}

		// Will set auto exclusion flag, if option 'x' is present.
		sg2xsd.setAutoExclude(comLine.hasOption('x'));

		// Starts the conversion process.
		sg2xsd.writeXSD();

		System.out.println("\nFini.");
	}

	/**
	 * Processes all command line parameters and returns a {@link CommandLine}
	 * -object. If required options are missing the following output will be
	 * generated:
	 * 
	 * <pre>
	 * usage: java de.uni_koblenz.jgralab.utilities.xml.SchemaGraph2XSD [-h ] [-v
	 *             ] -o &lt;file&gt; -n &lt;prefix&gt; [-g &lt;file&gt;] [-s &lt;file&gt;] [-p
	 *             &lt;(+|-)pattern&gt;{ &lt;(+|-)pattern&gt;}] [-d [&lt;filename&gt;]] [-x ]
	 *  -d,--debug &lt;filename&gt;              (optional): write debug information
	 *                                     for include and exclude patterns into
	 *                                     a file (optional parameter) or
	 *                                     standard out.
	 *  -g,--graph &lt;file&gt;                  (required or -s): TG-file of the
	 *                                     schemaGraph
	 *  -h,--help                          (optional): print this help message.
	 *  -n,--namespace-prefix &lt;prefix&gt;     (required): namespace prefix
	 *  -o,--output &lt;file&gt;                 (required): XSD-file to be generated
	 *  -p,--pattern-list &lt;(+|-)pattern&gt;   (optional): List of patterns. Include
	 *                                     patterns start with &quot;+&quot;, exclude
	 *                                     patterns start with &quot;-&quot;, by default
	 *                                     everything is included. If the first
	 *                                     pattern is positive, everything is
	 *                                     excluded first.
	 *  -s,--schema &lt;file&gt;                 (required or -g): TG-file of the
	 *                                     schema
	 *  -v,--version                       (optional): print version information
	 *  -x,--implicit-exclude              (optional): if this flag is set, all
	 *                                     implicitly excluded subclasses will be
	 *                                     explicitly excluded and not exported.
	 * 
	 * </pre>
	 * 
	 * 
	 * @param args
	 *            Command line parameters, which should be processed.
	 * @return {@link CommandLine}-object, which holds all values of the given
	 *         command line parameters linked with their command line options.
	 */
	private static CommandLine processCommandLineOptions(String[] args) {

		// Creates a OptionHandler.
		String toolString = "java " + SchemaGraph2XSD.class.getName();
		String versionString = JGraLab.getInfo(false);
		OptionHandler oh = new OptionHandler(toolString, versionString);

		// Several Options are declared.
		Option output = new Option("o", "output", true,
				"(required): XSD-file to be generated");
		output.setRequired(true);
		output.setArgName("file");
		oh.addOption(output);

		Option namespacePrefix = new Option("n", "namespace-prefix", true,
				"(required): namespace prefix");
		namespacePrefix.setRequired(true);
		namespacePrefix.setArgName("prefix");
		oh.addOption(namespacePrefix);

		Option graph = new Option("g", "graph", true,
				"(required or -s): TG-file of the schemaGraph");
		graph.setRequired(false);
		graph.setArgName("file");
		oh.addOption(graph);

		Option schema = new Option("s", "schema", true,
				"(required or -g): TG-file of the schema");
		schema.setRequired(false);
		schema.setArgName("file");
		oh.addOption(schema);

		// either graph or schema has to be provided
		OptionGroup input = new OptionGroup();
		input.addOption(graph);
		input.addOption(schema);
		input.setRequired(true);
		oh.addOptionGroup(input);

		Option patternList = new Option(
				"p",
				"pattern-list",
				true,
				"(optional): List of patterns. Include patterns start with \"+\", exclude patterns start with \"-\", by default everything is included. If the first pattern is positive, everything is excluded first.");
		patternList.setRequired(false);
		patternList.setArgs(Option.UNLIMITED_VALUES);
		patternList.setArgName("(+|-)pattern");
		patternList.setValueSeparator(' ');
		oh.addOption(patternList);

		Option debug = new Option(
				"d",
				"debug",
				true,
				"(optional): write debug information for include and exclude patterns into a file (optional parameter) or standard out.");
		debug.setRequired(false);
		debug.setArgs(1);
		debug.setArgName("filename");
		debug.setOptionalArg(true);
		oh.addOption(debug);

		Option implicitExclude = new Option(
				"x",
				"implicit-exclude",
				false,
				"(optional): if this flag is set, all implicitly excluded subclasses will be explicitly excluded and not exported.");
		implicitExclude.setRequired(false);
		oh.addOption(implicitExclude);

		// Parses the given command line parameters with all created Option.
		return oh.parse(args);
	}

	/**
	 * Creates a conversion object to convert a {@link SchemaGraph} into a
	 * XSD-file.
	 * 
	 * @param sg
	 *            {@link SchemaGraph}, which should be converted.
	 * @param namespacePrefix
	 *            Namespace prefix, which should be use in the generation
	 *            process of the XSD-file.
	 * @param outFile
	 *            Filename of the XSD-file, which should be generated.
	 * @throws FileNotFoundException
	 *             if the file exists but is a directory rather than a regular
	 *             file, does not exist but cannot be created, or cannot be
	 *             opened for any other reason
	 * @throws XMLStreamException
	 * @throws FactoryConfigurationError
	 */
	public SchemaGraph2XSD(SchemaGraph sg, String namespacePrefix,
			String outFile) throws FileNotFoundException, XMLStreamException,
			FactoryConfigurationError {

		// Creates an XMLStreamWriter, which is needed in the conversion
		// process.
		XMLStreamWriter writer = XMLOutputFactory.newInstance()
				.createXMLStreamWriter(new FileOutputStream(outFile));
		// Wraps an object around the XMLStreamWriter, to provide correct
		// indentation. Indentation is "    ".
		IndentingXMLStreamWriter xml = new IndentingXMLStreamWriter(writer, 4);
		xml.setIndentationChar(' ');

		// Sets the IndentingXMLStreamWriter as XMLStreamWriter.
		this.xml = xml;

		// Removes the illegal character ":" from the end of the namespacePrefix
		// String.
		if (namespacePrefix.endsWith(":")) {
			namespacePrefix = namespacePrefix.substring(0, namespacePrefix
					.length() - 1);
		}
		this.namespacePrefix = namespacePrefix;

		schemaGraph = sg;

		// Creates a SchemaGraph2Tg to provide tg-string generation for various
		// objects of a Schema.
		// TODO find a better solution
		sg2tg = new SchemaGraph2Tg(sg, null);
		sg2tg.setIsFormatted(false);

		domainMap = new HashMap<Domain, String>();
		vertices = new HashMap<String, VertexClass>();
		edges = new HashMap<String, EdgeClass>();
	}

	/**
	 * Sets the include- and exclude-patterns for the transformation.
	 * 
	 * @param patterns
	 *            Include- and exclude-patterns.
	 */
	public void setPatterns(String[] patterns) {
		this.patterns = patterns;
	}

	/**
	 * Sets the debug output stream.
	 * 
	 * @param debugOutputStream
	 *            Debug output stream, which can be a stream of a file or for a
	 *            console.
	 */
	public void setDebugOutputStream(PrintStream debugOutputStream) {
		this.debugOutputStream = debugOutputStream;
	}

	/**
	 * Sets the auto exclude flag.
	 * 
	 * @param autoExclude
	 *            true - the auto exclude is activated.
	 */
	public void setAutoExclude(boolean autoExclude) {
		this.autoExclude = autoExclude;
	}

	/**
	 * Starts the conversion from grUML to XSD.
	 * 
	 * @throws XMLStreamException
	 */
	public void writeXSD() throws XMLStreamException {

		// Select which classes are exported into XSD.
		System.out.print("Processing patterns ...");
		processPatterns();
		System.out.println("\t\tdone.");

		// Retrieves all domains and puts a tuple of the domain and a
		// corresponding type name into the domainMap.
		System.out.print("Processing domains ...");
		for (Domain domain : schemaGraph.getDomainVertices()) {
			if (isIncluded(domain)) {
				getXSDType(domain);
			}
		}
		System.out.println("\t\tdone.");

		// Begins the XSD document.
		writeStartXSDSchema();

		// Write the default simple- and complex-types.
		System.out.print("Writing default types ...");
		xml.writeComment("Default types");
		writeDefaultSimpleTypes();
		writeDefaultComplexTypes();
		System.out.println("\tdone.");

		// write all enumeration types
		// before creating all enumerations every domain have to be queried
		// again, to make sure, that all domain objects have been gathered.
		System.out.print("Writing enumeration types ...");
		xml.writeComment("Enumeration-types");
		writeAllDomainTypes();
		System.out.println("\tdone.");

		// Writes the graph class element and type
		System.out.print("Writing graph type ...");
		xml.writeComment("Graph-type");
		writeGraphClassComplexType();
		System.out.println("\t\tdone.");

		// Writes all vertex- and edge-types.
		System.out.print("Writing vertex types ...");
		xml.writeComment("Vertex-types");
		writeVertexClassComplexTypes();
		System.out.println("\tdone.");

		System.out.print("Writing edge types ...");
		xml.writeComment("Edge-types");
		writeEdgeClassComplexTypes();
		System.out.println("\t\tdone.");

		// Ends the schema
		xml.writeEndDocument();
		xml.flush();

		// Frees resources
		domainMap.clear();
	}

	/**
	 * Handles the processing of all given include and exclude patterns.
	 */
	private void processPatterns() {
		SchemaFilter filter = new SchemaFilter(schemaGraph, patterns,
				debugOutputStream, autoExclude);
		includes = filter.processPatterns();
	}

	/**
	 * Writes all predefined domain type definitions to the document. Predefined
	 * types are: Boolean-, String-, Integer-, Long-, Double-, List-, Set- and
	 * MapDomain. For Record- and EnumDomain, look at
	 * {@link SchemaGraph2XSD#writeAllDomainTypes}.
	 * 
	 * @throws XMLStreamException
	 */
	private void writeDefaultSimpleTypes() throws XMLStreamException {

		// BOOLEAN
		writeStartXSDSimpleType(GRUML_DOMAIN_BOOLEAN);
		writeStartXSDRestriction(XSD_NS_PREFIX_PLUS_COLON + XSD_DOMAIN_STRING,
				true);
		// Loop over all enumeration constant strings
		for (String enumConst : new String[] { "t", "f" }) {
			xml.writeEmptyElement(XSD_NAMESPACE_PREFIX, XSD_ENUMERATION,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
			xml.writeAttribute(XSD_ATTRIBUTE_VALUE, enumConst);
		}
		writeEndXSDElement(2);

		// STRING
		writeXSDRestrictedSimpleType(GRUML_DOMAIN_STRING,
				XSD_NS_PREFIX_PLUS_COLON + XSD_DOMAIN_STRING,
				STRING_DOMAIN_PATTERNS, null);
		// INTEGER
		writeXSDRestrictedSimpleType(GRUML_DOMAIN_INTEGER,
				XSD_NS_PREFIX_PLUS_COLON + XSD_DOMAIN_INTEGER, null, null);
		// LONG
		writeXSDRestrictedSimpleType(GRUML_DOMAIN_LONG,
				XSD_NS_PREFIX_PLUS_COLON + XSD_DOMAIN_LONG, null, null);
		// DOUBLE
		writeXSDRestrictedSimpleType(GRUML_DOMAIN_DOUBLE,
				XSD_NS_PREFIX_PLUS_COLON + XSD_DOMAIN_DOUBLE, null, null);

		// LIST
		writeXSDRestrictedSimpleType(GRUML_DOMAIN_LIST,
				XSD_NS_PREFIX_PLUS_COLON + XSD_DOMAIN_STRING,
				LIST_DOMAIN_PATTERNS, null);
		// SET
		writeXSDRestrictedSimpleType(GRUML_DOMAIN_SET, XSD_NS_PREFIX_PLUS_COLON
				+ XSD_DOMAIN_STRING, SET_DOMAIN_PATTERNS, null);
		// MAP
		writeXSDRestrictedSimpleType(GRUML_DOMAIN_MAP, XSD_NS_PREFIX_PLUS_COLON
				+ XSD_DOMAIN_STRING, MAP_DOMAIN_PATTERNS, null);

		// RECORD & ENUM are written in method "writeAllDomainTypes"
	}

	/**
	 * Writes the predefined graph-, vertex and edge-type definitions.
	 * 
	 * @throws XMLStreamException
	 */
	private void writeDefaultComplexTypes() throws XMLStreamException {

		String integer = namespacePrefix + ":" + GRUML_DOMAIN_INTEGER;
		String id = XSD_NAMESPACE_PREFIX + ":" + XML_DOMAIN_ID;
		String idRef = XSD_NAMESPACE_PREFIX + ":" + XML_DOMAIN_IDREF;

		writeStartXSDComplexType(GRUML_ATTRIBUTEDELEMENTTYPE, true, false);

		// Graph type
		writeStartXSDComplexType(GRUML_GRAPHTYPE, true, true);
		writeStartXSDExtension(GRUML_ATTRIBUTEDELEMENTTYPE, true);
		writeXSDAttribute(GRUML_ATTRIBUTE_ID, id, XSD_REQUIRED);
		writeEndXSDElement(3);

		// Vertex type
		writeStartXSDComplexType(GRUML_VERTEXTYPE, true, true);
		writeStartXSDExtension(GRUML_ATTRIBUTEDELEMENTTYPE, true);
		writeXSDAttribute(GRUML_ATTRIBUTE_ID, id, XSD_REQUIRED);
		writeEndXSDElement(3);

		// Edge type
		writeStartXSDComplexType(GRUML_EDGETYPE, true, true);
		writeStartXSDExtension(GRUML_ATTRIBUTEDELEMENTTYPE, true);
		writeXSDAttribute(GRUML_ATTRIBUTE_FROM, idRef, XSD_REQUIRED);
		writeXSDAttribute(GRUML_ATTRIBUTE_TO, idRef, XSD_REQUIRED);
		writeXSDAttribute(GRUML_ATTRIBUTE_FSEQ, integer, null);
		writeXSDAttribute(GRUML_ATTRIBUTE_TSEQ, integer, null);
		writeEndXSDElement(3);
	}

	/**
	 * Writes the element and complexType for the GraphClass of the SchemaGraph.
	 * 
	 * @throws XMLStreamException
	 */
	private void writeGraphClassComplexType() throws XMLStreamException {
		GraphClass gc = schemaGraph.getFirstGraphClass();

		// Writes an element for the GraphClass
		writeXSDElement(gc.get_qualifiedName(), GRUML_PREFIX_GRAPHTYPE
				+ gc.get_qualifiedName());

		// Writes a complexType for the GraphClass and extending from the
		// predefined GraphClass type.
		writeStartXSDComplexType(GRUML_PREFIX_GRAPHTYPE
				+ gc.get_qualifiedName(), false, true);
		writeStartXSDExtension(GRUML_GRAPHTYPE, true);

		// Writes all attributes.
		HashMap<Attribute, AttributedElementClass> attributes = new HashMap<Attribute, AttributedElementClass>();
		collectAttributes(gc, attributes);
		writeAttributes(attributes.keySet());

		// Writes a choice element provide a choice for several elements and
		// setting the minimal to '0' and maximal to 'unbounded'.
		xml.writeStartElement(XSD_NAMESPACE_PREFIX, XSD_CHOICE,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeAttribute(XSD_VALUE_MIN_OCCURS, "0");
		xml.writeAttribute(XSD_VALUE_MAX_OCCURS, "unbounded");

		// Loops over all Vertex- and EdgeClasses and writes an element for
		// every non-abstract type.
		String type;
		for (VertexClass vc : schemaGraph.getVertexClassVertices()) {
			if (vc.is_abstract() || !isIncluded(vc)) {
				continue;
			}

			type = GRUML_PREFIX_VERTEXTYPE + vc.get_qualifiedName();
			writeXSDElement(vc.get_qualifiedName(), type);
			vertices.put(type, vc);
		}
		for (EdgeClass ec : schemaGraph.getEdgeClassVertices()) {
			if (ec.is_abstract() || !isIncluded(ec)) {
				continue;
			}

			type = GRUML_PREFIX_EDGETYPE + ec.get_qualifiedName();
			writeXSDElement(ec.get_qualifiedName(), type);
			edges.put(type, ec);
		}

		writeEndXSDElement(4); // Closing 'choice', 'complexContent',
		// 'extension' and 'complexType'.
	}

	/**
	 * Writes all VertexClasses as XSD complexTypes with their attributes.
	 * 
	 * @throws XMLStreamException
	 */
	private void writeVertexClassComplexTypes() throws XMLStreamException {
		for (Entry<String, VertexClass> entry : vertices.entrySet()) {

			writeGraphElementClassComplexType(entry.getValue(), entry.getKey(),
					GRUML_VERTEXTYPE);
		}
	}

	/**
	 * Writes all EdgeClasses as XSD complexTypes with their attributes.
	 * 
	 * @throws XMLStreamException
	 */
	private void writeEdgeClassComplexTypes() throws XMLStreamException {
		for (Entry<String, EdgeClass> entry : edges.entrySet()) {

			writeGraphElementClassComplexType(entry.getValue(), entry.getKey(),
					GRUML_EDGETYPE);
		}
	}

	/**
	 * Writes all GraphElementClass as XSD types with their attributes.
	 * 
	 * @param element
	 *            GraphElementClass, which should be written as a XSD
	 *            complexType.
	 * @param type
	 *            Type name of the GraphElementClass.
	 * @param baseType
	 *            Base type name of this GraphElementClass.
	 * @throws XMLStreamException
	 */
	private void writeGraphElementClassComplexType(GraphElementClass element,
			String type, String baseType) throws XMLStreamException {

		// Collects all Attributes in a Map.
		HashMap<Attribute, AttributedElementClass> attributes = new HashMap<Attribute, AttributedElementClass>();
		collectAttributes(element, attributes);

		// Writes the a tg-comment of the GraphElementClass.
		xml.writeComment(createGraphElementClassComment(element, attributes));

		// Writes the complex type first.
		writeStartXSDComplexType(type, false, true);

		boolean hasAttributes = attributes.size() > 0;

		// Writes always an extension but only a complexContent, when there are
		// Attributes.
		writeStartXSDExtension(baseType, hasAttributes);
		writeAttributes(attributes.keySet());

		// ends complexContent (hasAttributes==true), extension and
		// complexType
		writeEndXSDElement(hasAttributes ? 3 : 2);
	}

	/**
	 * Writes all Domain types contained in the map <code>domainMap</code>.
	 * 
	 * @throws XMLStreamException
	 */
	private void writeAllDomainTypes() throws XMLStreamException {
		// Loop over all existing EnumDomains and RecordDomains.
		for (Entry<Domain, String> entry : domainMap.entrySet()) {

			// Leaves not marked Domains out.
			if (!includes.isMarked(entry.getKey())) {
				continue;
			}

			writeDomainType(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Writes a given Domain as XSD simpleType.
	 * 
	 * @param domain
	 *            Domain, which should be written as XSD simpleType.
	 * @param typeName
	 *            XSD type name of the Domain.
	 * @throws XMLStreamException
	 */
	private void writeDomainType(Domain domain, String typeName)
			throws XMLStreamException {
		// A comment should be written and patterns should be defined.
		// Only EnumDomain and RecordDomain are handled at the moment.
		if (domain instanceof EnumDomain) {
			writeEnumerationDomain((EnumDomain) domain, typeName);
		} else if (domain instanceof RecordDomain) {
			writeXSDRestrictedSimpleType(typeName, XSD_NS_PREFIX_PLUS_COLON
					+ XSD_DOMAIN_STRING, RECORD_DOMAIN_PATTERNS,
					getRecordDomainComment((RecordDomain) domain));
		} else {
			// Handles unforeseen left out Domain types.
			throw new RuntimeException("The type '" + domain.getClass()
					+ "' of domain '" + domain.get_qualifiedName()
					+ "' is not supported.");
		}
	}

	private void writeEnumerationDomain(EnumDomain domain, String typeName)
			throws XMLStreamException {
		writeStartXSDSimpleType(typeName);
		writeStartXSDRestriction(XSD_NS_PREFIX_PLUS_COLON + XSD_DOMAIN_STRING,
				true);
		// Loop over all enumeration constant strings
		for (String enumConst : domain.get_enumConstants()) {
			// Writes a enumeration element, which contains the current
			// enumeration
			// constant string as value of the attribute "value".
			xml.writeEmptyElement(XSD_NAMESPACE_PREFIX, XSD_ENUMERATION,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
			xml.writeAttribute(XSD_ATTRIBUTE_VALUE, enumConst);
		}
		writeEndXSDElement(2);
	}

	/**
	 * Writes a restricted simpleType with patterns.
	 * 
	 * @param name
	 *            Name of the type.
	 * @param type
	 *            Name of the base type.
	 * @param patterns
	 *            Pattern, which should be included.
	 * @param comment
	 *            an optional comment
	 * @throws XMLStreamException
	 */
	private void writeXSDRestrictedSimpleType(String name, String type,
			String[] patterns, String comment) throws XMLStreamException {

		boolean hasContent = patterns != null;
		// Starts simpleType with the Name of the String 'name'.
		writeStartXSDSimpleType(name);

		if (comment != null) {
			xml.writeComment(comment);
		}

		// Adds a restriction of the base type in 'type'.
		writeStartXSDRestriction(type, hasContent);
		// Adds patterns.
		writeXSDPatterns(patterns);
		// Closes all open XML elements.
		writeEndXSDElement(hasContent ? 2 : 1);
	}

	/**
	 * Writes the header of the XSD document.
	 * 
	 * @throws XMLStreamException
	 */
	private void writeStartXSDSchema() throws XMLStreamException {

		xml.writeStartDocument();

		// First XML element is "schema".
		xml.writeStartElement(XSD_NAMESPACE_PREFIX, XSD_SCHEMA,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);

		// The namespace of XSD have to be used.
		xml.writeNamespace(XSD_NAMESPACE_PREFIX,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);

		// Creates a corresponding XML namespace for the Schema
		Schema schema = schemaGraph.getFirstSchema();
		String namespace = schema.get_packagePrefix() + "." + schema.get_name();
		namespace = UtilityMethods.generateURI(namespace);

		// Writes the generated namespace
		xml.writeNamespace(namespacePrefix, namespace);
		// Defines the targetNamespace, which means that generated
		// XML-instance-documents have to include this exact namespace.
		xml.writeAttribute("targetNamespace", namespace);
	}

	/**
	 * Ends the XML element on top of element stack for <code>i</code> times.
	 * 
	 * @param i
	 *            Defines how often the top element should be ended.
	 * 
	 * @throws XMLStreamException
	 */
	private void writeEndXSDElement(int i) throws XMLStreamException {

		for (int j = 0; j < i; j++) {
			xml.writeEndElement();
		}
	}

	/**
	 * Starts a simpleType definition with a given name.
	 * 
	 * @param name
	 *            Name of the simpleType.
	 * @throws XMLStreamException
	 */
	private void writeStartXSDSimpleType(String name) throws XMLStreamException {

		// Starts a simpleType element.
		xml.writeStartElement(XSD_NAMESPACE_PREFIX, XSD_SIMPLETYPE,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		// Adds the name of the simpleType element.
		xml.writeAttribute(XSD_ATTRIBUTE_NAME, name);
	}

	/**
	 * Starts a XSD complexType definition with a given name.
	 * 
	 * @param name
	 *            Name of the complexType.
	 * @param isAbstract
	 *            If it's true, this complexType will be marked as abstract.
	 * @param hasContent
	 *            If it's true, this complexType will be represented with an
	 *            empty element.
	 * @throws XMLStreamException
	 */
	private void writeStartXSDComplexType(String name, boolean isAbstract,
			boolean hasContent) throws XMLStreamException {

		// Starts a complexType definition with . . .
		if (hasContent) {
			// with a XML element, which can contain other elements.
			xml.writeStartElement(XSD_NAMESPACE_PREFIX, XSD_COMPLEXTYPE,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
		} else {
			// with a XML element, which can not contain any elements.
			xml.writeEmptyElement(XSD_NAMESPACE_PREFIX, XSD_COMPLEXTYPE,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
		}

		// Adds the name of this complexType and weather it is abstract or not.
		xml.writeAttribute(XSD_ATTRIBUTE_NAME, name);

		if (isAbstract) {
			xml.writeAttribute(XSD_ATTRIBUTE_ABSTRACT, XML_VALUE_TRUE);
		}
	}

	/**
	 * Writes a XSD element definition with a given name and it's type.
	 * 
	 * @param name
	 *            Name of the element definition.
	 * @param type
	 *            Name of the complexType, which should be the type of this
	 *            element.
	 * @throws XMLStreamException
	 */
	private void writeXSDElement(String name, String type)
			throws XMLStreamException {

		// Writes an empty element definition, because the type of the element
		// is defined global.
		xml.writeEmptyElement(XSD_NAMESPACE_PREFIX, XSD_ELEMENT,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);

		// Adds the name and type of this element definition.
		xml.writeAttribute(XSD_ATTRIBUTE_NAME, name);
		xml.writeAttribute(XSD_ATTRIBUTE_TYPE, namespacePrefix + ":" + type);
	}

	/**
	 * Writes all given String pattern as XSD patterns.
	 * 
	 * @param patterns
	 *            String array of patterns.
	 * @throws XMLStreamException
	 */
	private void writeXSDPatterns(String[] patterns) throws XMLStreamException {

		// If there are no pattern, quit.
		if (patterns == null) {
			return;
		}

		// Loop over all patterns.
		for (String pattern : patterns) {
			// Writes a empty XML element 'pattern' with the attribute 'value',
			// which contains one pattern.
			xml.writeEmptyElement(XSD_NAMESPACE_PREFIX, XSD_PATTERN,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
			xml.writeAttribute(XSD_ATTRIBUTE_VALUE, pattern);
		}

	}

	/**
	 * Starts a restriction of a given type.
	 * 
	 * @param type
	 *            The type, which should be restricted.
	 * @param hasContent
	 *            Defines, weather this restriction can have sub elements or
	 *            not.
	 * @throws XMLStreamException
	 */
	private void writeStartXSDRestriction(String type, boolean hasContent)
			throws XMLStreamException {

		if (hasContent) {
			xml.writeStartElement(XSD_NAMESPACE_PREFIX, XSD_RESTRICTION,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
		} else {
			xml.writeEmptyElement(XSD_NAMESPACE_PREFIX, XSD_RESTRICTION,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
		}
		xml.writeAttribute(XSD_ATTRIBUTE_BASE, type);
	}

	/**
	 * Writes a XSD extension of a given type.
	 * 
	 * @param type
	 *            Type, which should be extended.
	 * @param hasContent
	 *            Defines, weather this restriction can have sub elements or
	 *            not.
	 * @throws XMLStreamException
	 */
	private void writeStartXSDExtension(String type, boolean hasContent)
			throws XMLStreamException {

		// Is needed for an extension
		xml.writeStartElement(XSD_NAMESPACE_PREFIX, XSD_COMPLEXCONTENT,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);

		// Decides weather this extension element can have sub elements.
		if (hasContent) {
			xml.writeStartElement(XSD_NAMESPACE_PREFIX, XSD_EXTENSION,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
		} else {
			xml.writeEmptyElement(XSD_NAMESPACE_PREFIX, XSD_EXTENSION,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
		}

		// Defines the type, which should be extended.
		xml.writeAttribute(XSD_ATTRIBUTE_BASE, namespacePrefix + ":" + type);
	}

	/**
	 * Writes the XSD attribute definition with the given name and type. The use
	 * attribute can be null, 'optional', 'prohibited' or 'required'. In case of
	 * null, the attribute 'use' will be left out.
	 * 
	 * @param name
	 *            Name of the attribute.
	 * @param type
	 *            Type of the attribute
	 * @param use
	 *            Use flag of this attribute.
	 * @throws XMLStreamException
	 */
	private void writeXSDAttribute(String name, String type, String use)
			throws XMLStreamException {

		// Starts the empty attribute definition.
		xml.writeEmptyElement(XSD_NAMESPACE_PREFIX, XSD_ATTRIBUTE,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);

		// Writes the name and type of this attribute.
		xml.writeAttribute(XSD_ATTRIBUTE_NAME, name);
		xml.writeAttribute(XSD_ATTRIBUTE_TYPE, type);
		// Writes the use Flag.
		if (use != null) {
			xml.writeAttribute(XSD_ATTRIBUTE_USE, use);
		}

	}

	/**
	 * Writes a grUML attribute as XSD attribute.
	 * 
	 * @param attribute
	 *            Attribute, which should be converted.
	 * @throws XMLStreamException
	 */
	private void writeAttribute(Attribute attribute) throws XMLStreamException {
		String name = attribute.get_name();
		Domain type = (Domain) attribute.getFirstHasDomain(EdgeDirection.OUT)
				.getOmega();
		writeXSDAttribute(name, getXSDType(type), XSD_REQUIRED);
	}

	/**
	 * Writes all grUML attributes contained in the given Set of Attributes.
	 * 
	 * @param attrs
	 *            Set of Attributes, which should be written as XSD attributes.
	 * @throws XMLStreamException
	 */
	private void writeAttributes(Set<Attribute> attrs)
			throws XMLStreamException {
		for (Attribute attribute : attrs) {
			writeAttribute(attribute);
		}
	}

	/**
	 * Collects all Attributes of a given AttributeElementClass and puts them
	 * into a HashMap.
	 * 
	 * @param attrElemClass
	 *            AttributeElementClass, of which all Attributes should be
	 *            collected.
	 * @param attributes
	 *            HashMap of Attributes, which will be fill with all Attributes
	 *            of the given AttributedElementClass.
	 */
	private void collectAttributes(AttributedElementClass attrElemClass,
			HashMap<Attribute, AttributedElementClass> attributes) {

		// Loop over all Attributes, which are only defined in the current
		// AttributedElementClass. No inherited Attributes.
		for (HasAttribute ha : attrElemClass
				.getHasAttributeIncidences(EdgeDirection.OUT)) {
			// Adds the Attribute with its corresponding AttributedElementClass,
			// in which it's defined.
			attributes.put((Attribute) ha.getOmega(), attrElemClass);
		}

		// Loop over all AttributedElementClasses, which are extended by the
		// current AttributedElementClass. A distinction is needed between
		// VertexClass, EdgeClass or GraphClass.
		if (attrElemClass instanceof VertexClass) {
			for (SpecializesVertexClass s : ((VertexClass) attrElemClass)
					.getSpecializesVertexClassIncidences(EdgeDirection.OUT)) {
				// Recursive call of this method with a specialized VertexClass.
				collectAttributes((AttributedElementClass) s.getOmega(),
						attributes);
			}
		} else if (attrElemClass instanceof EdgeClass) {
			for (SpecializesEdgeClass s : ((EdgeClass) attrElemClass)
					.getSpecializesEdgeClassIncidences(EdgeDirection.OUT)) {
				// Recursive call of this method with a specialized EdgeClass.
				collectAttributes((AttributedElementClass) s.getOmega(),
						attributes);
			}
		} else if (attrElemClass instanceof GraphClass) {
			// nothing to do here
		} else {
			throw new RuntimeException("Don't know what to do with '"
					+ attrElemClass.get_qualifiedName() + "'.");
		}
	}

	/**
	 * Creates an appropriate comment for a given GraphElementClass and with a
	 * map of Attributes and AttributedElementClasses.
	 * 
	 * @param geClass
	 *            GraphElementClass, for which a comment is created.
	 * @param attributes
	 *            Map of Attributes and their corresponding
	 *            AttributedElementClass.
	 * @return A comment String for the given GraphElementClass.
	 */
	private String createGraphElementClassComment(GraphElementClass geClass,
			HashMap<Attribute, AttributedElementClass> attributes) {

		// A StringWriter is created to hold the comment string.
		StringWriter stringWriter = new StringWriter();
		// The StringWriter is set as OutputStream for the SchemaGraph2Tg
		// object, which is used for first step.
		sg2tg.setStream(stringWriter);

		// A Distinction between VertexClass and EdgeClass is needed.
		// The appropriate methode is call to print the class definition in a
		// tg-format to the StringWriter. 'false' means, that no Constraints are
		// written.
		if (geClass instanceof VertexClass) {
			sg2tg.printVertexClassDefinition((VertexClass) geClass, false);
		} else if (geClass instanceof EdgeClass) {
			sg2tg.printEdgeClassDefinition((EdgeClass) geClass, false);
		}

		// A StringBuffer is used in the next steps. It's providing more
		// methods.
		StringBuffer sb = stringWriter.getBuffer();
		// Deletes the ';' at the end of the current comment string.
		sb.deleteCharAt(sb.length() - 1);

		// Flag, which controls whether the heading or not
		// "Inherited Attributes:" will be written.
		boolean writeHeading = true;
		// Loop over all entries of Attributes with their corresponding
		// AttributedElementClass.
		for (Entry<Attribute, AttributedElementClass> e : attributes.entrySet()) {

			Attribute a = e.getKey();
			// Only AttributedElementClass objects, which are not equal to the
			// current GraphElementClass object, are used.
			if (geClass != e.getValue()) {
				// The heading will only be written once at max.
				if (writeHeading) {
					stringWriter.append("\n    Inherited Attributes:");
					// Ensures, that this heading will only be written once.
					writeHeading = false;
				}
				stringWriter.append("\n        ");

				// Every Attribute will be written as followed:
				/*
				 * 'attribute' : 'domain' (from 'attributedElementClass')
				 * 
				 * attribute := Name of the Attribute domain := Name of the
				 * Domain attributedElementClass := The name of the
				 * AttributedElementClass, which defines the current Attribute.
				 */
				stringWriter.append(a.get_name());
				stringWriter.append(" : ");
				stringWriter.append(((Domain) a.getFirstHasDomain().getOmega())
						.get_qualifiedName());
				stringWriter.append(" (from ");
				stringWriter.append(e.getValue().get_qualifiedName());
				stringWriter.append(")");
			}
		}

		return stringWriter.toString();
	}

	/**
	 * Creates an appropriate comment for a given RecordDomain.
	 * 
	 * @param domain
	 *            RecordDomain, for which a comment string should be created.
	 * @return A comment String of the given RecordDomain.
	 */
	private String getRecordDomainComment(RecordDomain domain) {

		StringBuilder sb = new StringBuilder();
		// Begins the comment with ' alphabetically ordered:\n\n'.
		sb.append(" alphabetically ordered:\n\n                    ");

		// Creates a sorted map, which will sort automatically all Attributes as
		// String with their Domain also as String.
		SortedMap<String, String> map = new TreeMap<String, String>();

		// Loop over all all Attributes of the given RecordDomain.
		for (HasRecordDomainComponent component : domain
				.getHasRecordDomainComponentIncidences(EdgeDirection.OUT)) {
			// Puts the current Attribute as String with it's corresponding
			// Domain as String in the SortedMap.
			map.put(component.get_name(),
					getXSDTypeWithoutPrefix((Domain) component.getOmega()));
		}

		// Loop over all entries in alphabetically order (key).
		for (Entry<String, String> entry : map.entrySet()) {
			sb.append(entry.getKey());
			sb.append(':');
			sb.append(entry.getValue());
			sb.append("\n                    ");
		}

		return sb.toString();
	}

	/**
	 * Returns the corresponding XSD simpleType name with the used namespace
	 * prefix of a given Domain.
	 * 
	 * @param domain
	 *            Domain, to which the corresponding XSD simpleType name should
	 *            be returned.
	 * @return XSD simpleType name with namespace prefix.
	 */
	private String getXSDType(Domain domain) {
		return namespacePrefix + ":" + getXSDTypeWithoutPrefix(domain);
	}

	private String getXSDTypeWithoutPrefix(Domain domain) {

		// A distinction between all possible occurring Domains is needed.
		// Integer, Long, Boolean, Double, String, Set, List and Map are
		// predefined.
		// Record and Enum are handled differently.
		if (domain instanceof IntegerDomain) {
			return GRUML_DOMAIN_INTEGER;
		} else if (domain instanceof LongDomain) {
			return GRUML_DOMAIN_LONG;
		} else if (domain instanceof BooleanDomain) {
			return GRUML_DOMAIN_BOOLEAN;
		} else if (domain instanceof DoubleDomain) {
			return GRUML_DOMAIN_DOUBLE;
		} else if (domain instanceof StringDomain) {
			return GRUML_DOMAIN_STRING;
		} else if (domain instanceof SetDomain) {
			return GRUML_DOMAIN_SET;
		} else if (domain instanceof ListDomain) {
			return GRUML_DOMAIN_LIST;
		} else if (domain instanceof MapDomain) {
			return GRUML_DOMAIN_MAP;
		} else if (domain instanceof RecordDomain) {
			return queryDomainType(domain);
		} else if (domain instanceof EnumDomain) {
			return queryDomainType(domain);
		}
		// This case ensures, that not handled domain types will be pointed out.
		throw new RuntimeException("Unknown domain '"
				+ domain.get_qualifiedName() + "'.");
	}

	/**
	 * Queries for a Domain the corresponding type string. In the case of no
	 * existing match, a new type string is created and stored in the used map
	 * <code>enumMap</code>.
	 * 
	 * @param domain
	 *            Domain for which the corresponding type string is queried.
	 * @return Type string.
	 */
	private String queryDomainType(Domain domain) {

		// Returns an existing mapping.
		if (domainMap.containsKey(domain)) {
			return domainMap.get(domain);
		}

		// Creates a new type string.

		String qualifiedName;

		// Distinction between EnumDomain and RecordDomain.
		// A prefix, depending of the type, will be added.
		if (domain instanceof EnumDomain) {
			qualifiedName = GRUML_DOMAIN_ENUM_PREFIX;
		} else if (domain instanceof RecordDomain) {
			qualifiedName = GRUML_DOMAIN_RECORD_PREFIX;
		} else {
			// Not handled types will be pointed out.
			throw new RuntimeException("Unknown domain '"
					+ domain.get_qualifiedName() + "'.");
		}

		// The Qualified Name will be added to.
		qualifiedName += domain.get_qualifiedName();
		assert (!domainMap.values().contains(qualifiedName)) : "FIXME! \"domainMap\" already contains a string \""
				+ qualifiedName + "\" of the Domain '" + domain + "'!";

		// Stores the new type string.
		domainMap.put(domain, qualifiedName);

		return qualifiedName;
	}

	/**
	 * Checks if a AttributedElementClass should be includes in the output of
	 * the XSD.
	 * 
	 * @param element
	 *            AttributedElementClass, which should be checked.
	 * @return true, if the AttributedElementClass should be include in the XSD
	 *         output.
	 */
	private boolean isIncluded(AttributedElementClass element) {
		return includes.isMarked(element);
	}

	/**
	 * Checks if a Domain should be includes in the output of the XSD.
	 * 
	 * @param element
	 *            Domain, which should be checked.
	 * @return true, if the Domain should be include in the XSD output.
	 */
	private boolean isIncluded(Domain d) {
		if ((d instanceof RecordDomain) || (d instanceof EnumDomain)) {
			return includes.isMarked(d);
		}
		return false;
	}
}
