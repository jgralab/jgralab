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
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_COMPLEXTYPE;
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
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_GRAPHTYPE;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_PREFIX_EDGETYPE;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_PREFIX_GRAPHTYPE;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_PREFIX_VERTEXTYPE;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_VALUE_FALSE;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_VALUE_NULL;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_VALUE_TRUE;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.GRUML_VERTEXTYPE;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XML_DOMAIN_ID;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XML_DOMAIN_IDREF;
import static de.uni_koblenz.jgralab.utilities.xml.XMLConstants.XML_VALUE_FALSE;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	 * Stores Attributes and is defined once to suppress object creation.
	 */
	private final ArrayList<Attribute> attributes;

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
		attributes = new ArrayList<Attribute>();
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

		// Writes the graph class element and type
		System.out.print("Writing graph type ...");
		xml.writeComment("Graph-type");
		writeGraphClass();
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

		// Write all enumeration types
		System.out.print("Writing enumeration types ...");
		xml.writeComment("Enumeration-types");
		writeAllDomainTypes();
		System.out.println("\tdone.");

		// Ends the schema
		xml.writeEndDocument();
		xml.flush();

		// Frees resources
		attributes.clear();
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
		String namespace = schema.getPackagePrefix() + "." + schema.getName();
		namespace = UtilityMethods.generateURI(namespace);

		// Writes the generated namespace
		xml.writeNamespace(namespacePrefix, namespace);
		// Defines the targetNamespace, which means that generated
		// XML-instance-documents have to include this exact namespace.
		xml.writeAttribute("targetNamespace", namespace);
	}

	/**
	 * Ends the XML element on top of element stack.
	 * 
	 * @throws XMLStreamException
	 */
	private void writeEndXSDElement() throws XMLStreamException {
		xml.writeEndElement();
	}

	/**
	 * Ends the XML element on top of element stack for <code>i</code> times.
	 * 
	 * @param i
	 *            Defines how often the top element should be ended.
	 * 
	 * @throws XMLStreamException
	 */
	public void writeEndXSDElement(int i) throws XMLStreamException {
		for (int j = 0; j < i; j++) {
			xml.writeEndElement();
		}
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
		ArrayList<String> constants = new ArrayList<String>(2);
		constants.add(GRUML_VALUE_TRUE);
		constants.add(GRUML_VALUE_FALSE);

		createEnumDomainType(constants, GRUML_DOMAIN_BOOLEAN, false);

		// STRING
		writeRestrictedSimpleType(GRUML_DOMAIN_STRING, XSD_NS_PREFIX_PLUS_COLON
				+ XSD_DOMAIN_STRING, STRING_DOMAIN_PATTERNS);
		// INTEGER
		writeRestrictedSimpleType(GRUML_DOMAIN_INTEGER,
				XSD_NS_PREFIX_PLUS_COLON + XSD_DOMAIN_INTEGER, null);
		// LONG
		writeRestrictedSimpleType(GRUML_DOMAIN_LONG, XSD_NS_PREFIX_PLUS_COLON
				+ XSD_DOMAIN_LONG, null);
		// DOUBLE
		writeRestrictedSimpleType(GRUML_DOMAIN_DOUBLE, XSD_NS_PREFIX_PLUS_COLON
				+ XSD_DOMAIN_DOUBLE, null);

		// LIST
		writeRestrictedSimpleType(GRUML_DOMAIN_LIST, XSD_NS_PREFIX_PLUS_COLON
				+ XSD_DOMAIN_STRING, LIST_DOMAIN_PATTERNS);
		// SET
		writeRestrictedSimpleType(GRUML_DOMAIN_SET, XSD_NS_PREFIX_PLUS_COLON
				+ XSD_DOMAIN_STRING, SET_DOMAIN_PATTERNS);
		// MAP
		writeRestrictedSimpleType(GRUML_DOMAIN_MAP, XSD_NS_PREFIX_PLUS_COLON
				+ XSD_DOMAIN_STRING, MAP_DOMAIN_PATTERNS);

		// RECORD & ENUM are written in method "writeAllDomainTypes"
	}

	/**
	 * Writes the predefined graph-, vertex and edge-type definitions.
	 * 
	 * @throws XMLStreamException
	 */
	private void writeDefaultComplexTypes() throws XMLStreamException {
		String attElem = GRUML_ATTRIBUTEDELEMENTTYPE;
		String integer = namespacePrefix + ":" + GRUML_DOMAIN_INTEGER;
		String id = XSD_NAMESPACE_PREFIX + ":" + XML_DOMAIN_ID;
		String idRef = XSD_NAMESPACE_PREFIX + ":" + XML_DOMAIN_IDREF;

		writeStartXSDComplexType(attElem, true, false);

		// Graph type
		writeStartXSDComplexType(GRUML_GRAPHTYPE, true, true);
		writeStartXSDExtension(attElem, true);
		writeXSDAttribute(GRUML_ATTRIBUTE_ID, id, XSD_REQUIRED);
		writeEndXSDElement(3);

		// Vertex type
		writeStartXSDComplexType(GRUML_VERTEXTYPE, true, true);
		writeStartXSDExtension(attElem, true);
		writeXSDAttribute(GRUML_ATTRIBUTE_ID, id, XSD_REQUIRED);
		writeEndXSDElement(3);

		// Edge type
		writeStartXSDComplexType(GRUML_COMPLEXTYPE, true, true);
		writeStartXSDExtension(attElem, true);
		writeXSDAttribute(GRUML_ATTRIBUTE_FROM, idRef, XSD_REQUIRED);
		writeXSDAttribute(GRUML_ATTRIBUTE_TO, idRef, XSD_REQUIRED);
		writeXSDAttribute(GRUML_ATTRIBUTE_FSEQ, integer);
		writeXSDAttribute(GRUML_ATTRIBUTE_TSEQ, integer);
		writeEndXSDElement(3);
	}

	/**
	 * 
	 * @throws XMLStreamException
	 */
	private void writeGraphClass() throws XMLStreamException {
		GraphClass gc = schemaGraph.getFirstGraphClass();

		// create an element for the graph class
		writeStartXSDElement(gc.getQualifiedName(), GRUML_PREFIX_GRAPHTYPE
				+ gc.getQualifiedName(), false);

		writeStartXSDComplexType(
				GRUML_PREFIX_GRAPHTYPE + gc.getQualifiedName(), false, true);

		writeStartXSDExtension(GRUML_GRAPHTYPE, true);

		xml.writeStartElement(XSD_NAMESPACE_PREFIX, XSD_CHOICE,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeAttribute(XSD_VALUE_MIN_OCCURS, "0");
		xml.writeAttribute(XSD_VALUE_MAX_OCCURS, "unbounded");

		for (VertexClass vc : schemaGraph.getVertexClassVertices()) {
			if (vc.isIsAbstract() || !isIncluded(vc)) {
				continue;
			}
			writeStartXSDElement(vc.getQualifiedName(), GRUML_PREFIX_VERTEXTYPE
					+ vc.getQualifiedName(), false);
		}
		for (EdgeClass ec : schemaGraph.getEdgeClassVertices()) {
			if (ec.isIsAbstract() || !isIncluded(ec)) {
				continue;
			}
			writeStartXSDElement(ec.getQualifiedName(), GRUML_PREFIX_EDGETYPE
					+ ec.getQualifiedName(), false);
		}

		writeEndXSDElement();

		attributes.clear();
		collectAttributes(gc, attributes);
		writeAttributes(attributes);

		writeEndXSDElement(3);
	}

	private void writeVertexClassComplexTypes() throws XMLStreamException {
		for (VertexClass vc : schemaGraph.getVertexClassVertices()) {

			if (vc.isIsAbstract() || !isIncluded(vc)) {
				continue;
			}

			xml.writeComment(createVertexClassComment(vc));

			// first the complex type
			writeStartXSDComplexType(GRUML_PREFIX_VERTEXTYPE
					+ vc.getQualifiedName(), false, true);

			attributes.clear();
			collectAttributes(vc, attributes);

			if (attributes.size() > 0) {
				writeStartXSDExtension(GRUML_VERTEXTYPE, true);

				writeAttributes(attributes);

				writeEndXSDElement(); // ends extension
			} else {
				writeStartXSDExtension(GRUML_VERTEXTYPE, false);
			}
			writeEndXSDElement(2); // ends complexContent and complexType
		}
	}

	private void writeEdgeClassComplexTypes() throws XMLStreamException {
		for (EdgeClass ec : schemaGraph.getEdgeClassVertices()) {
			if (ec.isIsAbstract() || !isIncluded(ec)) {
				continue;
			}

			xml.writeComment(createEdgeClassComment(ec));

			// first the complex type
			writeStartXSDComplexType(GRUML_PREFIX_EDGETYPE
					+ ec.getQualifiedName(), false, true);

			attributes.clear();
			collectAttributes(ec, attributes);

			if (attributes.size() > 0) {
				writeStartXSDExtension(GRUML_COMPLEXTYPE, true);

				writeAttributes(attributes);

				writeEndXSDElement(); // ends extension
			} else {
				writeStartXSDExtension(GRUML_COMPLEXTYPE, false);
			}
			writeEndXSDElement(2); // ends complexContent and complexType
		}
	}

	private void writeRecordDomainType(Domain domain, String typeName)
			throws XMLStreamException {

		writeStartXSDSimpleType(typeName);

		String[] pattern = null;
		if (domain instanceof RecordDomain) {
			xml.writeComment(createRecordDomainComment((RecordDomain) domain));
			pattern = RECORD_DOMAIN_PATTERNS;
		} else {
			throw new RuntimeException("The type '" + domain.getClass()
					+ "' of domain '" + domain.getQualifiedName()
					+ "' is not supported.");
		}
		writeStartXSDRestriction(XSD_NS_PREFIX_PLUS_COLON + XSD_DOMAIN_STRING,
				pattern);
		writeEndXSDElement();
	}

	private void writeRestrictedSimpleType(String name, String type,
			String[] patterns) throws XMLStreamException {
		writeStartXSDSimpleType(name);
		writeStartXSDRestriction(type, patterns);
		writeEndXSDElement();
	}

	/**
	 * Creates all Domain types contained in the map <code>enumMap</code>.
	 * 
	 * @throws XMLStreamException
	 */
	private void writeAllDomainTypes() throws XMLStreamException {
		// Loop over all existing EnumDomains and RecordDomains.
		for (Entry<Domain, String> entry : domainMap.entrySet()) {
			Domain d = entry.getKey();
			if (includes.isMarked(d)) {
				if (d instanceof EnumDomain) {
					createEnumDomainType((EnumDomain) entry.getKey(), entry
							.getValue());
				} else if (d instanceof RecordDomain) {
					writeRecordDomainType(entry.getKey(), entry.getValue());
				} else {
					throw new RuntimeException("Unknown domain " + d + " ("
							+ d.getQualifiedName() + ")");
				}
			}
		}
	}

	private void writeStartXSDSimpleType(String name) throws XMLStreamException {
		xml.writeStartElement(XSD_NAMESPACE_PREFIX, XSD_SIMPLETYPE,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeAttribute(XSD_ATTRIBUTE_NAME, name);
	}

	private void writeStartXSDComplexType(String name, boolean isAbstract,
			boolean hasContent) throws XMLStreamException {

		if (hasContent) {
			xml.writeStartElement(XSD_NAMESPACE_PREFIX, XSD_COMPLEXTYPE,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
		} else {
			xml.writeEmptyElement(XSD_NAMESPACE_PREFIX, XSD_COMPLEXTYPE,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
		}

		xml.writeAttribute(XSD_ATTRIBUTE_NAME, name);
		xml.writeAttribute(XSD_ATTRIBUTE_ABSTRACT, isAbstract ? XML_VALUE_TRUE
				: XML_VALUE_FALSE);
	}

	private void writeStartXSDElement(String name, String type,
			boolean withContent) throws XMLStreamException {

		if (withContent) {
			xml.writeStartElement(XSD_NAMESPACE_PREFIX, XSD_ELEMENT,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
		} else {
			xml.writeEmptyElement(XSD_NAMESPACE_PREFIX, XSD_ELEMENT,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
		}

		xml.writeAttribute(XSD_ATTRIBUTE_NAME, name);
		xml.writeAttribute(XSD_ATTRIBUTE_TYPE, namespacePrefix + ":" + type);
	}

	private void writeStartXSDRestriction(String type, String[] patterns)
			throws XMLStreamException {

		if (patterns == null) {
			xml.writeEmptyElement(XSD_NAMESPACE_PREFIX, XSD_RESTRICTION,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
		} else {
			xml.writeStartElement(XSD_NAMESPACE_PREFIX, XSD_RESTRICTION,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
		}
		xml.writeAttribute(XSD_ATTRIBUTE_BASE, type);

		if (patterns != null) {
			for (String pattern : patterns) {
				xml.writeEmptyElement(XSD_NAMESPACE_PREFIX, XSD_PATTERN,
						XMLConstants.W3C_XML_SCHEMA_NS_URI);
				xml.writeAttribute(XSD_ATTRIBUTE_VALUE, pattern);
			}
			xml.writeEndElement();
		}
	}

	private void writeStartXSDExtension(String extendedType,
			boolean complexContent) throws XMLStreamException {

		// Is needed for an extension
		xml.writeStartElement(XSD_NAMESPACE_PREFIX, XSD_COMPLEXCONTENT,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);

		if (complexContent) {
			xml.writeStartElement(XSD_NAMESPACE_PREFIX, XSD_EXTENSION,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
		} else {
			xml.writeEmptyElement(XSD_NAMESPACE_PREFIX, XSD_EXTENSION,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
		}

		xml.writeAttribute(XSD_ATTRIBUTE_BASE, namespacePrefix + ":"
				+ extendedType);
	}

	/**
	 * Creates a new EnumDomain in XSD with the name of <code>value</code> and
	 * constants of the Domain <code>key</code>.
	 * 
	 * @param domain
	 *            Domain which is transformed to a XSD representation.
	 * @param typeName
	 *            Name of the new XSD type.
	 * @throws XMLStreamException
	 */
	private void createEnumDomainType(EnumDomain domain, String typeName)
			throws XMLStreamException {
		createEnumDomainType(domain.getEnumConstants(), typeName, true);
	}

	/**
	 * Creates a new EnumDomain in XSD with the name of <code>value</code> and
	 * constants of the Domain <code>key</code>.
	 * 
	 * @param domain
	 *            Domain which is transformed to a XSD representation.
	 * @param typeName
	 *            Name of the new XSD type.
	 * @throws XMLStreamException
	 */
	private void createEnumDomainType(List<String> constants, String typeName,
			boolean nullable) throws XMLStreamException {

		xml.writeStartElement(XSD_NAMESPACE_PREFIX, XSD_SIMPLETYPE,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeAttribute(XSD_ATTRIBUTE_NAME, typeName);

		xml.writeStartElement(XSD_NAMESPACE_PREFIX, XSD_RESTRICTION,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeAttribute(XSD_ATTRIBUTE_BASE, XSD_NS_PREFIX_PLUS_COLON
				+ XSD_DOMAIN_STRING);

		for (String enumConst : constants) {
			xml.writeEmptyElement(XSD_NAMESPACE_PREFIX, XSD_ENUMERATION,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
			if (enumConst.equals("n")) {
				throw new RuntimeException("The enumeration as Type '"
						+ typeName + "' alreay defines the constant \"n\".");
			}
			xml.writeAttribute(XSD_ATTRIBUTE_VALUE, enumConst);

		}
		if (nullable) {
			xml.writeEmptyElement(XSD_NAMESPACE_PREFIX, XSD_ENUMERATION,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
			xml.writeAttribute(XSD_ATTRIBUTE_VALUE, "n");
		}

		xml.writeEndElement();
		xml.writeEndElement();
	}

	/**
	 * write definition of an optional attribute
	 * 
	 * @param name
	 * @param type
	 * @throws XMLStreamException
	 */
	private void writeXSDAttribute(String name, String type)
			throws XMLStreamException {
		writeXSDAttribute(name, type, null);
	}

	private void writeXSDAttribute(String name, String type, String use)
			throws XMLStreamException {
		xml.writeEmptyElement(XSD_NAMESPACE_PREFIX, XSD_ATTRIBUTE,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeAttribute(XSD_ATTRIBUTE_NAME, name);
		xml.writeAttribute(XSD_ATTRIBUTE_TYPE, type);
		if (use != null) {
			xml.writeAttribute(XSD_ATTRIBUTE_USE, use);
		}

	}

	private void writeAttribute(Attribute attribute) throws XMLStreamException {
		String name = attribute.getName();
		Domain type = (Domain) attribute.getFirstHasDomain(EdgeDirection.OUT)
				.getOmega();
		writeXSDAttribute(name, getXSDType(type), XSD_REQUIRED);
	}

	private void writeAttributes(ArrayList<Attribute> attributeList)
			throws XMLStreamException {
		for (Attribute attribute : attributeList) {
			writeAttribute(attribute);
		}
	}

	private void collectAttributes(AttributedElementClass attrElemClass,
			ArrayList<Attribute> attributesList) {

		for (HasAttribute ha : attrElemClass
				.getHasAttributeIncidences(EdgeDirection.OUT)) {
			attributesList.add((Attribute) ha.getOmega());
		}

		if (attrElemClass instanceof VertexClass) {
			for (SpecializesVertexClass s : ((VertexClass) attrElemClass)
					.getSpecializesVertexClassIncidences(EdgeDirection.OUT)) {
				collectAttributes((AttributedElementClass) s.getOmega(),
						attributesList);
			}
		} else if (attrElemClass instanceof EdgeClass) {
			for (SpecializesEdgeClass s : ((EdgeClass) attrElemClass)
					.getSpecializesEdgeClassIncidences(EdgeDirection.OUT)) {
				collectAttributes((AttributedElementClass) s.getOmega(),
						attributesList);
			}
		} else if (attrElemClass instanceof GraphClass) {
			// nothing to do here
		} else {
			throw new RuntimeException("Don't know what to do with '"
					+ attrElemClass.getQualifiedName() + "'.");
		}
	}

	private String createEdgeClassComment(EdgeClass edgeClass) {
		StringWriter stringWriter = new StringWriter();

		sg2tg.setStream(stringWriter);
		sg2tg.printEdgeClassDefinition(edgeClass, false);

		StringBuffer sb = stringWriter.getBuffer();
		sb.deleteCharAt(sb.length() - 1);
		writeInheritedAttributes(edgeClass, stringWriter);

		return stringWriter.toString();
	}

	private String createVertexClassComment(VertexClass vertexClass) {
		StringWriter stringWriter = new StringWriter();

		sg2tg.setStream(stringWriter);
		sg2tg.printVertexClassDefinition(vertexClass, false);

		StringBuffer sb = stringWriter.getBuffer();
		sb.deleteCharAt(sb.length() - 1);
		writeInheritedAttributes(vertexClass, stringWriter);

		return stringWriter.toString();
	}

	private String createRecordDomainComment(RecordDomain domain) {
		StringBuilder sb = new StringBuilder();

		sb.append(" alphabetically ordered: ");

		SortedMap<String, String> map = new TreeMap<String, String>();

		for (HasRecordDomainComponent component : domain
				.getHasRecordDomainComponentIncidences(EdgeDirection.OUT)) {
			map.put(component.getName(),
					getXSDTypeWithoutPrefix((Domain) component.getOmega()));
		}

		for (Entry<String, String> entry : map.entrySet()) {
			sb.append(entry.getKey());
			sb.append(':');
			sb.append(entry.getValue());
			sb.append(' ');
		}

		return sb.toString();
	}

	private String getXSDType(Domain domain) {
		return namespacePrefix + ":" + getXSDTypeWithoutPrefix(domain);
	}

	private String getXSDTypeWithoutPrefix(Domain domain) {

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
		throw new RuntimeException("Unknown domain '"
				+ domain.getQualifiedName() + "'.");
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

		if (domain instanceof EnumDomain) {
			qualifiedName = GRUML_DOMAIN_ENUM_PREFIX;
		} else if (domain instanceof RecordDomain) {
			qualifiedName = GRUML_DOMAIN_RECORD_PREFIX;
		} else {
			throw new RuntimeException("Unknown domain '"
					+ domain.getQualifiedName() + "'.");
		}

		qualifiedName += domain.getQualifiedName();
		assert (!domainMap.values().contains(qualifiedName)) : "FIXME! \"domainMap\" already contains a string \""
				+ qualifiedName + "\" of the Domain '" + domain + "'!";

		// Stores the new type string.
		domainMap.put(domain, qualifiedName);

		return qualifiedName;
	}

	private void writeInheritedAttributes(EdgeClass edgeClass, StringWriter w) {
		for (SpecializesEdgeClass specializes : edgeClass
				.getSpecializesEdgeClassIncidences(EdgeDirection.OUT)) {
			EdgeClass superClass = (EdgeClass) specializes.getOmega();
			if (!(superClass.getFirstHasAttribute(EdgeDirection.OUT) == null)) {
				w.append("\nInherited attributes from "
						+ superClass.getQualifiedName() + ":");
			}
			sg2tg.printAttributes(superClass
					.getFirstHasAttribute(EdgeDirection.OUT));
			writeInheritedAttributes(superClass, w);
		}
	}

	private void writeInheritedAttributes(VertexClass vertexClass,
			StringWriter w) {
		for (SpecializesVertexClass specializes : vertexClass
				.getSpecializesVertexClassIncidences(EdgeDirection.OUT)) {
			VertexClass superClass = (VertexClass) specializes.getOmega();
			if (!(superClass.getFirstHasAttribute(EdgeDirection.OUT) == null)) {
				w.append("\nInherited attributes from "
						+ superClass.getQualifiedName() + ":");
			}
			sg2tg.printAttributes(superClass
					.getFirstHasAttribute(EdgeDirection.OUT));
			writeInheritedAttributes(superClass, w);
		}
	}

	private boolean isIncluded(AttributedElementClass aec) {
		return includes.isMarked(aec);
	}

	private boolean isIncluded(Domain d) {
		if ((d instanceof RecordDomain) || (d instanceof EnumDomain)) {
			return includes.isMarked(d);
		}
		return false;
	}
}
