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
 * "Abbildung von grUML nach XSD" by Eckhardt Grossmann, Volker Riediger and
 * Tassilo Horn
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
	 * Links Domain-objects to existing enumeration types described by a string.
	 */
	private final Map<Domain, String> domainMap;

	/**
	 * @param args
	 * @throws GraphIOException
	 * @throws FactoryConfigurationError
	 * @throws XMLStreamException
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws GraphIOException,
			FileNotFoundException, XMLStreamException,
			FactoryConfigurationError {
		CommandLine comLine = processCommandLineOptions(args);
		assert comLine != null;
		String inputFile = comLine.hasOption('g') ? comLine.getOptionValue('g')
				.trim() : comLine.getOptionValue('s');
		String namespacePrefix = comLine.getOptionValue('n').trim();
		String xsdFile = comLine.getOptionValue('o').trim();
		String[] rawPatterns = comLine.hasOption('p') ? comLine
				.getOptionValues('p') : null;

		System.out
				.println("Loading SchemaGraph from file '" + inputFile + "'.");
		SchemaGraph sg = comLine.hasOption('g') ? GrumlSchema.instance()
				.loadSchemaGraph(inputFile, new ProgressFunctionImpl())
				: new Tg2SchemaGraph().process(inputFile);

		System.out.println("\nBeginning convertion:");
		SchemaGraph2XSD sg2xsd = new SchemaGraph2XSD(sg, namespacePrefix,
				xsdFile);

		sg2xsd.setPatterns(rawPatterns);

		if (comLine.hasOption('d')) {
			String filename = comLine.getOptionValue('d');
			if (filename == null) {
				sg2xsd.setDebugOutputStream(System.out);
			} else {
				sg2xsd
						.setDebugOutputStream(new PrintStream(
								new BufferedOutputStream(new FileOutputStream(
										filename))));
			}
		}

		if (comLine.hasOption('x')) {
			sg2xsd.setAutoExclude(true);
		}

		sg2xsd.writeXSD();

		System.out.println("\nFini.");
	}

	private static CommandLine processCommandLineOptions(String[] args) {
		String toolString = "java " + SchemaGraph2XSD.class.getName();
		String versionString = JGraLab.getInfo(false);
		OptionHandler oh = new OptionHandler(toolString, versionString);

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
		// TODO when OptionHandler has been fixed, set back to true
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

		return oh.parse(args);
	}

	public SchemaGraph2XSD(SchemaGraph sg, String namespacePrefix,
			String outFile) throws FileNotFoundException, XMLStreamException,
			FactoryConfigurationError {
		XMLStreamWriter writer = XMLOutputFactory.newInstance()
				.createXMLStreamWriter(new FileOutputStream(outFile));
		IndentingXMLStreamWriter xml = new IndentingXMLStreamWriter(writer, 1);

		xml.setIndentationChar('\t');

		this.xml = xml;

		if (namespacePrefix.endsWith(":")) {
			namespacePrefix = namespacePrefix.substring(0, namespacePrefix
					.length() - 1);
		}
		this.namespacePrefix = namespacePrefix;
		schemaGraph = sg;
		sg2tg = new SchemaGraph2Tg(sg, null);
		sg2tg.setIsFormatted(false);
		domainMap = new HashMap<Domain, String>();
	}

	public void setPatterns(String[] patterns) {
		this.patterns = patterns;
	}

	public void setDebugOutputStream(PrintStream debugOutputStream) {
		this.debugOutputStream = debugOutputStream;
	}

	public void setAutoExclude(boolean autoExclude) {
		this.autoExclude = autoExclude;
	}

	public void writeXSD() throws XMLStreamException {

		// select which classes are exported into the XSD
		System.out.print("Processing patterns ...");
		processPatterns();
		System.out.println("\t\tdone.");

		System.out.print("Processing domains ...");
		for (Domain domain : schemaGraph.getDomainVertices()) {
			if (isIncluded(domain)) {
				getXSDType(domain);
			}
		}
		System.out.println("\t\tdone.");

		writeStartXSDSchema();

		// write the default complex types
		System.out.print("Writing default types ...");
		xml.writeComment("Default types");
		writeDefaultSimpleTypes();
		writeDefaultComplexTypes();
		System.out.println("\tdone.");

		// now the graph class
		System.out.print("Writing graph type ...");
		xml.writeComment("Graph-type");
		writeGraphClass();
		System.out.println("\t\tdone.");

		// now vertex and edge classes
		System.out.print("Writing vertex types ...");
		xml.writeComment("Vertex-types");
		writeVertexClassComplexTypes();
		System.out.println("\tdone.");

		System.out.print("Writing edge types ...");
		xml.writeComment("Edge-types");
		writeEdgeClassComplexTypes();
		System.out.println("\t\tdone.");

		// write all enumeration types
		// before creating all enumerations every domain have to be queried
		// again, to make sure, that all domain objects have been gathered.
		System.out.print("Writing enumeration types ...");
		xml.writeComment("Enumeration-types");
		writeAllDomainTypes();
		System.out.println("\tdone.");

		// Ends the schema
		xml.writeEndDocument();
		xml.flush();

		// Frees resources
		domainMap.clear();
	}

	/**
	 * Handles the processing of all given include and exclude patterns
	 */
	private void processPatterns() {
		SchemaFilter filter = new SchemaFilter(schemaGraph, patterns,
				debugOutputStream, autoExclude);
		includes = filter.processPatterns();
	}

	private void writeStartXSDSchema() throws XMLStreamException {

		xml.writeStartDocument();

		xml.writeStartElement(XSD_NAMESPACE_PREFIX, XSD_SCHEMA,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);

		xml.writeNamespace(XSD_NAMESPACE_PREFIX,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);

		Schema schema = schemaGraph.getFirstSchema();

		String namespace = schema.getPackagePrefix() + "." + schema.getName();

		namespace = UtilityMethods.generateURI(namespace);

		xml.writeNamespace(namespacePrefix, namespace);
		xml.writeAttribute("targetNamespace", namespace);
	}

	/**
	 * @throws XMLStreamException
	 */
	private void writeEndXSDElement() throws XMLStreamException {
		xml.writeEndElement();
	}

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

		// RECORD & ENUM are written in method "writeAllDomainTypes"

		// LIST
		writeRestrictedSimpleType(GRUML_DOMAIN_LIST, XSD_NS_PREFIX_PLUS_COLON
				+ XSD_DOMAIN_STRING, LIST_DOMAIN_PATTERNS);
		// SET
		writeRestrictedSimpleType(GRUML_DOMAIN_SET, XSD_NS_PREFIX_PLUS_COLON
				+ XSD_DOMAIN_STRING, SET_DOMAIN_PATTERNS);
		// MAP
		writeRestrictedSimpleType(GRUML_DOMAIN_MAP, XSD_NS_PREFIX_PLUS_COLON
				+ XSD_DOMAIN_STRING, MAP_DOMAIN_PATTERNS);
	}

	private void writeDefaultComplexTypes() throws XMLStreamException {
		String attElem = GRUML_ATTRIBUTEDELEMENTTYPE;
		String integer = namespacePrefix + ":" + GRUML_DOMAIN_INTEGER;
		String id = XSD_NAMESPACE_PREFIX + ":" + XML_DOMAIN_ID;
		String idRef = XSD_NAMESPACE_PREFIX + ":" + XML_DOMAIN_IDREF;

		writeStartXSDComplexType(attElem, true, false);

		writeStartXSDComplexType(GRUML_GRAPHTYPE, true, true);
		writeStartXSDExtension(attElem, true);
		writeXSDAttribute(GRUML_ATTRIBUTE_ID, id, XSD_REQUIRED);
		writeEndXSDElement();
		writeEndXSDElement();
		writeEndXSDElement();

		writeStartXSDComplexType(GRUML_VERTEXTYPE, true, true);
		writeStartXSDExtension(attElem, true);
		writeXSDAttribute(GRUML_ATTRIBUTE_ID, id, XSD_REQUIRED);
		writeEndXSDElement();
		writeEndXSDElement();
		writeEndXSDElement();

		writeStartXSDComplexType(GRUML_COMPLEXTYPE, true, true);
		writeStartXSDExtension(attElem, true);
		writeXSDAttribute(GRUML_ATTRIBUTE_FROM, idRef, XSD_REQUIRED);
		writeXSDAttribute(GRUML_ATTRIBUTE_TO, idRef, XSD_REQUIRED);
		writeXSDAttribute(GRUML_ATTRIBUTE_FSEQ, integer);
		writeXSDAttribute(GRUML_ATTRIBUTE_TSEQ, integer);
		writeEndXSDElement();
		writeEndXSDElement();
		writeEndXSDElement();
	}

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

		HashMap<Attribute, AttributedElementClass> attributes = new HashMap<Attribute, AttributedElementClass>();
		collectAttributes(gc, attributes);
		writeAttributes(attributes.keySet());

		writeEndXSDElement();
		writeEndXSDElement();
		writeEndXSDElement();
	}

	private void writeVertexClassComplexTypes() throws XMLStreamException {
		for (VertexClass vc : schemaGraph.getVertexClassVertices()) {

			if (vc.isIsAbstract() || !isIncluded(vc)) {
				continue;
			}

			HashMap<Attribute, AttributedElementClass> attributes = new HashMap<Attribute, AttributedElementClass>();
			collectAttributes(vc, attributes);

			xml.writeComment(createGraphElementClassComment(vc, attributes));

			// first the complex type
			writeStartXSDComplexType(GRUML_PREFIX_VERTEXTYPE
					+ vc.getQualifiedName(), false, true);

			if (attributes.size() > 0) {
				writeStartXSDExtension(GRUML_VERTEXTYPE, true);

				writeAttributes(attributes.keySet());

				writeEndXSDElement(); // ends extension
			} else {
				writeStartXSDExtension(GRUML_VERTEXTYPE, false);
			}
			writeEndXSDElement(); // ends complexContent
			writeEndXSDElement(); // ends complexType
		}
	}

	private void writeEdgeClassComplexTypes() throws XMLStreamException {
		for (EdgeClass ec : schemaGraph.getEdgeClassVertices()) {
			if (ec.isIsAbstract() || !isIncluded(ec)) {
				continue;
			}

			HashMap<Attribute, AttributedElementClass> attributes = new HashMap<Attribute, AttributedElementClass>();
			collectAttributes(ec, attributes);

			xml.writeComment(createGraphElementClassComment(ec, attributes));

			// first the complex type
			writeStartXSDComplexType(GRUML_PREFIX_EDGETYPE
					+ ec.getQualifiedName(), false, true);

			if (attributes.size() > 0) {
				writeStartXSDExtension(GRUML_COMPLEXTYPE, true);
				writeAttributes(attributes.keySet());
				writeEndXSDElement(); // ends extension
			} else {
				writeStartXSDExtension(GRUML_COMPLEXTYPE, false);
			}
			writeEndXSDElement(); // ends complexContent
			writeEndXSDElement(); // ends complexType
		}
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

	private void writeAttributes(Set<Attribute> attrs)
			throws XMLStreamException {
		for (Attribute attribute : attrs) {
			writeAttribute(attribute);
		}
	}

	private void collectAttributes(AttributedElementClass attrElemClass,
			HashMap<Attribute, AttributedElementClass> attributes) {

		for (HasAttribute ha : attrElemClass
				.getHasAttributeIncidences(EdgeDirection.OUT)) {
			attributes.put((Attribute) ha.getOmega(), attrElemClass);
		}

		if (attrElemClass instanceof VertexClass) {
			for (SpecializesVertexClass s : ((VertexClass) attrElemClass)
					.getSpecializesVertexClassIncidences(EdgeDirection.OUT)) {
				collectAttributes((AttributedElementClass) s.getOmega(),
						attributes);
			}
		} else if (attrElemClass instanceof EdgeClass) {
			for (SpecializesEdgeClass s : ((EdgeClass) attrElemClass)
					.getSpecializesEdgeClassIncidences(EdgeDirection.OUT)) {
				collectAttributes((AttributedElementClass) s.getOmega(),
						attributes);
			}
		} else if (attrElemClass instanceof GraphClass) {
			// nothing to do here
		} else {
			throw new RuntimeException("Don't know what to do with '"
					+ attrElemClass.getQualifiedName() + "'.");
		}
	}

	private String createGraphElementClassComment(GraphElementClass geClass,
			HashMap<Attribute, AttributedElementClass> attributes) {
		StringWriter stringWriter = new StringWriter();
		sg2tg.setStream(stringWriter);

		if (geClass instanceof VertexClass) {
			sg2tg.printVertexClassDefinition((VertexClass) geClass, false);
		} else if (geClass instanceof EdgeClass) {
			sg2tg.printEdgeClassDefinition((EdgeClass) geClass, false);
		}

		StringBuffer sb = stringWriter.getBuffer();
		sb.deleteCharAt(sb.length() - 1);

		boolean writeHeading = true;
		for (Entry<Attribute, AttributedElementClass> e : attributes.entrySet()) {
			Attribute a = e.getKey();
			if (geClass != e.getValue()) {
				if (writeHeading) {
					stringWriter.append("\n    Inherited Attributes:");
					writeHeading = false;
				}
				stringWriter.append("\n        ");

				stringWriter.append(a.getName());
				stringWriter.append(" : ");
				stringWriter.append(((Domain) a.getFirstHasDomain().getOmega())
						.getQualifiedName());
				stringWriter.append(" (from ");
				stringWriter.append(e.getValue().getQualifiedName());
				stringWriter.append(")");
			}
		}

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
