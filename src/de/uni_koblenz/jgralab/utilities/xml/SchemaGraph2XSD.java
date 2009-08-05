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
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.grumlschema.GrumlSchema;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.grumlschema.domains.BooleanDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.CollectionDomain;
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
import de.uni_koblenz.jgralab.utilities.common.UtilityMethods;
import de.uni_koblenz.jgralab.utilities.jgralab2owl.IndentingXMLStreamWriter;
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

	private static final String[] RECORD_DOMAIN_PATTERNS = new String[] {
			"\\(.*\\)", "n" };
	private static final String[] MAP_DOMAIN_PATTERNS = new String[] {
			"\\{.*\\}", "n" };
	private static final String[] SET_DOMAIN_PATTERNS = new String[] {
			"\\{.*\\}", "n" };
	private static final String[] LIST_DOMAIN_PATTERNS = new String[] {
			"\\[.*\\]", "n" };
	private static final String[] STRING_DOMAIN_PATTERNS = new String[] {
			"\".*\"", "n" };
	private static final String DOMAIN_PREFIX = "ST_";
	private static final String DOMAIN_RECORD_PREFIX = DOMAIN_PREFIX
			+ "RECORD_";
	private static final String DOMAIN_SET = DOMAIN_PREFIX + "SET";
	private static final String DOMAIN_LIST = DOMAIN_PREFIX + "LIST";
	private static final String DOMAIN_MAP = DOMAIN_PREFIX + "MAP";
	private static final String DOMAIN_BOOLEAN = DOMAIN_PREFIX + "BOOLEAN";
	private static final String DOMAIN_STRING = DOMAIN_PREFIX + "STRING";
	private static final String DOMAIN_INTEGER = DOMAIN_PREFIX + "INTEGER";
	private static final String DOMAIN_LONG = DOMAIN_PREFIX + "LONG";
	private static final String DOMAIN_DOUBLE = DOMAIN_PREFIX + "DOUBLE";
	private static final String DOMAIN_ENUM_PREFIX = DOMAIN_PREFIX + "ENUM_";

	private static final String XSD_COMPLEXCONTENT = "complexContent";
	private static final String TRUE = "true";
	private static final String FALSE = "false";
	private static final String XSD_ATTRIBUTE_ABSTRACT = "abstract";
	private static final String XSD_ENUMERATION_VALUE = "value";
	private static final String XSD_ENUMERATION = "enumeration";
	private static final String XSD_RESTRICTION = "restriction";
	private static final String XSD_SIMPLETYPE = "simpleType";
	private static final String XSD_REQUIRED = "required";
	private static final String XSD_SCHEMA = "schema";
	private static final String XSD_NS_PREFIX = "xsd"; // No need to use a
	// prefix
	private static final String XSD_NS_PREFIX_PLUS_COLON = XSD_NS_PREFIX
			+ (XSD_NS_PREFIX.equals("") ? "" : ":");
	private static final String XSD_DOMAIN_STRING = XSD_NS_PREFIX_PLUS_COLON
			+ "string";
	private static final String XSD_DOMAIN_DOUBLE = XSD_NS_PREFIX_PLUS_COLON
			+ "double";
	private static final String XSD_DOMAIN_LONG = XSD_NS_PREFIX_PLUS_COLON
			+ "long";
	private static final String XSD_DOMAIN_INTEGER = XSD_NS_PREFIX_PLUS_COLON
			+ "integer";
	private static final String XML_IDREF = XSD_NS_PREFIX_PLUS_COLON + "IDREF";
	private static final String XML_ID = XSD_NS_PREFIX_PLUS_COLON + "ID";

	private static final String XSD_ATTRIBUTE_MAX_OCCURS = "maxOccurs";
	private static final String XSD_ATTRIBUTE_MIN_OCCURS = "minOccurs";
	private static final String XSD_CHOICE = "choice";
	private static final String XSD_ATTRIBUTE_BASE = "base";
	private static final String XSD_EXTENSION = "extension";
	private static final String XSD_PATTERN = "pattern";
	private static final String XSD_ATTRIBUTE = "attribute";
	private static final String XSD_ATTRIBUTE_TO = "to";
	private static final String XSD_ATTRIBUTE_TSEQ = "tseq";
	private static final String XSD_ATTRIBUTE_FROM = "from";
	private static final String XSD_ATTRIBUTE_FSEQ = "fseq";
	private static final String XSD_ATTRIBUTE_ID = "id";
	private static final String XSD_ELEMENT = "element";
	private static final String XSD_ATTRIBUTE_TYPE = "type";
	private static final String XSD_ATTRIBUTE_NAME = "name";
	private static final String XSD_ATTRIBUTE_VALUE = "value";
	private static final String XSD_ATTRIBUTE_USE = "use";
	private static final String XSD_COMPLEXTYPE = "complexType";
	private static final String XSD_COMPLEX_GRAPHTYPE_PREFIX = "GT_";
	private static final String XSD_COMPLEX_VERTEXTYPE_PREFIX = "VT_";
	private static final String XSD_COMPLEX_EDGETYPE_PREFIX = "ET_";
	private static final String XSD_COMPLEXTYPE_ATTRIBUTED_ELEMENT = "BT_AttributedElement";
	private static final String XSD_COMPLEXTYPE_GRAPH = "BT_Graph";
	private static final String XSD_COMPLEXTYPE_VERTEX = "BT_Vertex";
	private static final String XSD_COMPLEXTYPE_EDGE = "BT_Edge";

	private final XMLStreamWriter xml;
	private final SchemaGraph schemaGraph;
	private final SchemaGraph2Tg sg2tg;
	private final String namespacePrefix;

	/**
	 * Stores Attributes and is defined once to suppress object creation.
	 */
	private final ArrayList<Attribute> attributes;
	private String[] patterns;
	private BooleanGraphMarker includes;
	private PrintStream debugOutputStream;
	private boolean autoExclude = false;

	public void setPatterns(String[] patterns) {
		this.patterns = patterns;
	}

	private boolean isIncluded(AttributedElementClass aec) {
		return includes.isMarked(aec);
	}

	private boolean isIncluded(Domain d) {
		if (d instanceof RecordDomain || d instanceof EnumDomain) {
			return includes.isMarked(d);
		}
		return false;
	}

	/**
	 * Links Domain-objects to existing enumeration types described by a string.
	 */
	private final Map<Domain, String> domainMap;

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
		domainMap = new HashMap<Domain, String>();
		attributes = new ArrayList<Attribute>();
	}

	/**
	 * Handles the processing of all given include and exclude patterns
	 */
	private void processPatterns() {
		includes = new BooleanGraphMarker(schemaGraph);

		if (patterns != null) {
			// always include the GraphClass
			includes.mark(schemaGraph);
			// accept everything by default
			Pattern matchesAll = Pattern.compile(".*");

			if (patterns.length <= 0 || patterns[0].trim().startsWith("-")) {
				includeOrExcludeAllGraphElements(true, matchesAll);
			}

			Pattern validPattern = Pattern.compile("^[+\\-]");
			for (String currentRawPattern : patterns) {
				Pattern currentPattern = Pattern.compile(validPattern
						.split(currentRawPattern)[1]);
				if (currentRawPattern.trim().startsWith("+")) {
					includeOrExcludeAllGraphElements(true, currentPattern);
				} else if (currentRawPattern.trim().startsWith("-")) {
					includeOrExcludeAllGraphElements(false, currentPattern);
				}
			}

			if (autoExclude) {
				explicitlyExcludeImplicitlyExcludedClasses();
			}
			excludeUnnecessaryAbstractVertexClasses();

			excludeUnecessaryEdgeClasses();

			includeAllNecessaryDomains();

			if (debugOutputStream != null) {
				writeDebugInformation();
			}

		} else {
			// if no patterns are set include everything
			for (AttributedElementClass currentAttributedElement : schemaGraph
					.getAttributedElementClassVertices()) {
				includes.mark(currentAttributedElement);
			}
			for (EnumDomain currentEnumDomain : schemaGraph
					.getEnumDomainVertices()) {
				includes.mark(currentEnumDomain);
			}
			for (RecordDomain currentRecordDomain : schemaGraph
					.getRecordDomainVertices()) {
				includes.mark(currentRecordDomain);
			}
		}
	}

	/**
	 * Handles the writing of the debug information.
	 */
	private void writeDebugInformation() {
		debugOutputStream.println("[VertexClasses]");
		for (VertexClass current : schemaGraph.getVertexClassVertices()) {
			writeElementDebugInformation(current);
		}
		debugOutputStream.println();
		debugOutputStream.println("[EdgeClasses]");
		for (EdgeClass current : schemaGraph.getEdgeClassVertices()) {
			writeElementDebugInformation(current);
		}
		debugOutputStream.println();
		debugOutputStream.println("[Domains]");
		for (EnumDomain current : schemaGraph.getEnumDomainVertices()) {
			writeDomainDebugInformation(current);
		}
		for (RecordDomain current : schemaGraph.getRecordDomainVertices()) {
			writeDomainDebugInformation(current);
		}
		debugOutputStream.flush();
		debugOutputStream.close();
	}

	/**
	 * Writes the debug information of the given domain.
	 * 
	 * @param d
	 *            the domain to write the information of.
	 */
	private void writeDomainDebugInformation(Domain d) {
		writeIncludeOrExcludeInformation(d);
		debugOutputStream.println(d.getQualifiedName());
	}

	/**
	 * Writes debug information of the given GraphElementClass.
	 * 
	 * @param gec
	 *            the GraphElementClass to write the information of.
	 */
	private void writeElementDebugInformation(GraphElementClass gec) {
		writeIncludeOrExcludeInformation(gec);
		debugOutputStream.println(gec.getQualifiedName());
	}

	/**
	 * Writes "IN: " or "OUT :" at the beginning of each line in the debug file.
	 * 
	 * @param ae
	 *            the element to decide whether to write "IN: " or "OUT: "
	 */
	private void writeIncludeOrExcludeInformation(AttributedElement ae) {
		if (includes.isMarked(ae)) {
			debugOutputStream.print("IN: ");
		} else {
			debugOutputStream.print("EX: ");
		}
	}

	/**
	 * Excludes all subclasses of excluded superclasses for EdgeClasses and
	 * VertexClasses.
	 */
	private void explicitlyExcludeImplicitlyExcludedClasses() {
		BooleanGraphMarker processed = new BooleanGraphMarker(schemaGraph);
		for (VertexClass currentVertexClass : schemaGraph
				.getVertexClassVertices()) {
			if (!processed.isMarked(currentVertexClass)
					&& !includes.isMarked(currentVertexClass)) {
				excludeGraphElementClass(processed, currentVertexClass);
			}
		}
		for (EdgeClass currentEdgeClass : schemaGraph.getEdgeClassVertices()) {
			if (!processed.isMarked(currentEdgeClass)
					&& !includes.isMarked(currentEdgeClass)) {
				excludeGraphElementClass(processed, currentEdgeClass);
			}
		}
	}

	/**
	 * Excludes the given GraphElementClass and all its subclasses.
	 * 
	 * @param processed
	 *            marks already processed Elements.
	 * @param currentGraphElementClass
	 *            the GraphElementClass to exclude.
	 */
	private void excludeGraphElementClass(BooleanGraphMarker processed,
			VertexClass currentGraphElementClass) {
		processed.mark(currentGraphElementClass);
		includes.unmark(currentGraphElementClass);
		for (SpecializesVertexClass current : currentGraphElementClass
				.getSpecializesVertexClassIncidences(EdgeDirection.IN)) {
			VertexClass superclass = (VertexClass) current.getThat();
			excludeGraphElementClass(processed, superclass);
		}
	}

	/**
	 * Excludes the given GraphElementClass and all its subclasses.
	 * 
	 * @param processed
	 *            marks already processed Elements.
	 * @param currentGraphElementClass
	 *            the GraphElementClass to exclude.
	 */
	private void excludeGraphElementClass(BooleanGraphMarker processed,
			EdgeClass currentGraphElementClass) {
		processed.mark(currentGraphElementClass);
		includes.unmark(currentGraphElementClass);
		for (SpecializesEdgeClass current : currentGraphElementClass
				.getSpecializesEdgeClassIncidences(EdgeDirection.IN)) {
			EdgeClass superclass = (EdgeClass) current.getThat();
			excludeGraphElementClass(processed, superclass);
		}
	}

	/**
	 * Includes all necessary domains according to the included
	 * GraphElementClasses.
	 */
	private void includeAllNecessaryDomains() {
		for (AttributedElementClass currentAttributedElementClass : schemaGraph
				.getAttributedElementClassVertices()) {
			if (includes.isMarked(currentAttributedElementClass)) {
				for (HasAttribute currentAttributeLink : currentAttributedElementClass
						.getHasAttributeIncidences()) {
					Domain currentDomain = (Domain) ((Attribute) currentAttributeLink
							.getThat()).getFirstHasDomain().getThat();
					includeDomain(currentDomain);
				}
			}
		}
	}

	/**
	 * Includes the given domain.
	 * 
	 * @param d
	 *            the domain to include.
	 */
	private void includeDomain(Domain d) {
		if (d instanceof EnumDomain) {
			includeDomain((EnumDomain) d);
		} else if (d instanceof RecordDomain) {
			includeDomain((RecordDomain) d);
		} else if (d instanceof CollectionDomain) {
			includeDomain((CollectionDomain) d);
		} else if (d instanceof MapDomain) {
			includeDomain((MapDomain) d);
		}
	}

	/**
	 * Includes the given MapDomain.
	 * 
	 * @param md
	 *            the MapDomain to include.
	 */
	private void includeDomain(MapDomain md) {
		includeDomain((Domain) md.getFirstHasKeyDomain().getThat());
		includeDomain((Domain) md.getFirstHasValueDomain().getThat());
	}

	/**
	 * Includes the given CollectionDomain (Set or List).
	 * 
	 * @param cd
	 *            the CollectionDomain to include.
	 */
	private void includeDomain(CollectionDomain cd) {
		includeDomain((Domain) cd.getFirstHasBaseDomain().getThat());
	}

	/**
	 * Includes the given EnumDomain.
	 * 
	 * @param ed
	 *            the EnumDomain to include.
	 */
	private void includeDomain(EnumDomain ed) {
		includes.mark(ed);
	}

	/**
	 * Includes the given RecordDomain.
	 * 
	 * @param rd
	 *            the RecordDomain to exclude.
	 */
	private void includeDomain(RecordDomain rd) {
		includes.mark(rd);
		// recursively include all RecordDomainComponentDomains
		for (HasRecordDomainComponent currentRecordDomainComponentEdgeClass : rd
				.getHasRecordDomainComponentIncidences()) {
			includeDomain((Domain) currentRecordDomainComponentEdgeClass
					.getThat());
		}
	}

	/**
	 * Excludes all EdgeClasses that have an excluded to or from VertexClass.
	 */
	private void excludeUnecessaryEdgeClasses() {
		for (EdgeClass currentEdgeClass : schemaGraph.getEdgeClassVertices()) {
			if (includes.isMarked(currentEdgeClass)) {
				// only look at included EdgeClasses
				if (!includes.isMarked(currentEdgeClass.getFirstTo().getThat())
						|| !includes.isMarked(currentEdgeClass.getFirstFrom()
								.getThat())) {
					// exclude all EdgeClasses whose to or from VertexClasses
					// are already excluded
					includes.unmark(currentEdgeClass);
				}
			}
		}
	}

	/**
	 * Excludes all VertexClasses that have only excluded subclasses.
	 */
	private void excludeUnnecessaryAbstractVertexClasses() {
		BooleanGraphMarker processed = new BooleanGraphMarker(schemaGraph);
		for (VertexClass currentVertexClass : schemaGraph
				.getVertexClassVertices()) {
			if (currentVertexClass.isIsAbstract()) {
				// only process abstract VertexClasses
				if (isVertexClassExcluded(processed, currentVertexClass)) {
					includes.unmark(currentVertexClass);
				}
			}
		}
	}

	/**
	 * Checks if a VertexClass should be excluded.
	 * 
	 * @param processed
	 *            marks already processed elements.
	 * @param currentVertexClass
	 *            the VertexClass to check.
	 * @return true if the given VertexClass should be excluded
	 */
	private boolean isVertexClassExcluded(BooleanGraphMarker processed,
			VertexClass currentVertexClass) {
		if (processed.isMarked(currentVertexClass)
				|| !currentVertexClass.isIsAbstract()) {
			return !includes.isMarked(currentVertexClass);
		}
		processed.mark(currentVertexClass);
		if (!includes.isMarked(currentVertexClass)) {
			// abstract and already excluded
			return false;
		}
		for (SpecializesVertexClass current : currentVertexClass
				.getSpecializesVertexClassIncidences(EdgeDirection.IN)) {
			if (!isVertexClassExcluded(processed, (VertexClass) current
					.getThat())) {
				// at least one subclass is not excluded
				return false;
			}
		}
		// all subclasses are excluded or is abstract leaf (which should not
		// occur)
		return true;
	}

	/**
	 * Matches the given pattern and either includes or excludes all matching
	 * GraphElements.
	 * 
	 * @param include
	 *            if true, this method includes, if false, it excludes.
	 * @param currentPattern
	 *            the pattern to match.
	 */
	private void includeOrExcludeAllGraphElements(boolean include,
			Pattern currentPattern) {
		for (GraphElementClass gec : schemaGraph.getGraphElementClassVertices()) {
			includeOrExcludeIfMatches(include, gec, currentPattern);
		}
	}

	/**
	 * Includes or excludes the given GraphElementClass according to the given
	 * pattern.
	 * 
	 * @param include
	 *            flag to decide whether to include or exclude the given
	 *            GraphElementClass if it matches the given pattern.
	 * @param gec
	 *            the GraphElementClass to include or exclude.
	 * @param currentPattern
	 *            the pattern to match.
	 */
	private void includeOrExcludeIfMatches(boolean include,
			GraphElementClass gec, Pattern currentPattern) {
		if (currentPattern.matcher(gec.getQualifiedName()).matches()) {
			if (include) {
				includes.mark(gec);
			} else {
				includes.unmark(gec);
			}
		}
	}

	public void writeXSD() throws XMLStreamException {

		// select which classes are exported into the XSD
		processPatterns();

		for (Domain domain : schemaGraph.getDomainVertices()) {
			if (isIncluded(domain)) {
				getXSDType(domain);
			}
		}

		writeStartXSDSchema();

		// write the default complex types
		xml.writeComment("Default types");
		writeDefaultComplexTypes();
		writeDefaultSimpleTypes();

		// now the graph class
		xml.writeComment("Graph-type");
		writeGraphClass();

		// now vertex and edge classes
		xml.writeComment("Vertex-types");
		writeVertexClassComplexTypes();
		xml.writeComment("Edge-types");
		writeEdgeClassComplexTypes();

		// write all enumeration types
		// before creating all enumerations every domain have to be queried
		// again, to make sure, that all domain objects have been gathered.
		xml.writeComment("Enumeration-types");
		writeAllDomainTypes();

		// Ends the schema
		xml.writeEndDocument();
		xml.flush();

		// Frees resources
		attributes.clear();
		domainMap.clear();
	}

	private void writeDefaultSimpleTypes() throws XMLStreamException {

		// BOOLEAN
		ArrayList<String> constants = new ArrayList<String>(2);
		constants.add("t");
		constants.add("f");

		createEnumDomainType(constants, DOMAIN_BOOLEAN, false);

		// STRING
		writeRestrictedSimpleType(DOMAIN_STRING, XSD_DOMAIN_STRING,
				STRING_DOMAIN_PATTERNS);
		// INTEGER
		writeRestrictedSimpleType(DOMAIN_INTEGER, XSD_DOMAIN_INTEGER, null);
		// LONG
		writeRestrictedSimpleType(DOMAIN_LONG, XSD_DOMAIN_LONG, null);
		// DOUBLE
		writeRestrictedSimpleType(DOMAIN_DOUBLE, XSD_DOMAIN_DOUBLE, null);

		// RECORD & ENUM are written in method "writeAllDomainTypes"

		// LIST
		writeRestrictedSimpleType(DOMAIN_LIST, XSD_DOMAIN_STRING,
				LIST_DOMAIN_PATTERNS);
		// SET
		writeRestrictedSimpleType(DOMAIN_SET, XSD_DOMAIN_STRING,
				SET_DOMAIN_PATTERNS);
		// MAP
		writeRestrictedSimpleType(DOMAIN_MAP, XSD_DOMAIN_STRING,
				MAP_DOMAIN_PATTERNS);
	}

	private void writeRestrictedSimpleType(String name, String type,
			String[] patterns) throws XMLStreamException {
		writeStartXSDSimpleType(name);
		writeStartXSDRestriction(type, patterns);
		writeEndXSDElement();
	}

	private void writeStartXSDRestriction(String type, String[] patterns)
			throws XMLStreamException {

		if (patterns == null) {
			xml.writeEmptyElement(XSD_NS_PREFIX, XSD_RESTRICTION,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
		} else {
			xml.writeStartElement(XSD_NS_PREFIX, XSD_RESTRICTION,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
		}
		xml.writeAttribute(XSD_ATTRIBUTE_BASE, type);

		if (patterns != null) {
			for (String pattern : patterns) {
				xml.writeEmptyElement(XSD_NS_PREFIX, XSD_PATTERN,
						XMLConstants.W3C_XML_SCHEMA_NS_URI);
				xml.writeAttribute(XSD_ATTRIBUTE_VALUE, pattern);
			}
			xml.writeEndElement();
		}
	}

	private void writeStartXSDSimpleType(String name) throws XMLStreamException {
		xml.writeStartElement(XSD_NS_PREFIX, XSD_SIMPLETYPE,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeAttribute(XSD_ATTRIBUTE_NAME, name);
	}

	private void writeGraphClass() throws XMLStreamException {
		GraphClass gc = schemaGraph.getFirstGraphClass();

		// create an element for the graph class
		writeStartXSDElement(gc.getQualifiedName(),
				XSD_COMPLEX_GRAPHTYPE_PREFIX + gc.getQualifiedName(), false);

		writeStartXSDComplexType(XSD_COMPLEX_GRAPHTYPE_PREFIX
				+ gc.getQualifiedName(), false, true);

		writeStartXSDExtension(XSD_COMPLEXTYPE_GRAPH, true);

		xml.writeStartElement(XSD_NS_PREFIX, XSD_CHOICE,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeAttribute(XSD_ATTRIBUTE_MIN_OCCURS, "0");
		xml.writeAttribute(XSD_ATTRIBUTE_MAX_OCCURS, "unbounded");

		for (VertexClass vc : schemaGraph.getVertexClassVertices()) {
			if (vc.isIsAbstract() || !isIncluded(vc)) {
				continue;
			}
			writeStartXSDElement(vc.getQualifiedName(),
					XSD_COMPLEX_VERTEXTYPE_PREFIX + vc.getQualifiedName(),
					false);
		}
		for (EdgeClass ec : schemaGraph.getEdgeClassVertices()) {
			if (ec.isIsAbstract() || !isIncluded(ec)) {
				continue;
			}
			writeStartXSDElement(ec.getQualifiedName(),
					XSD_COMPLEX_EDGETYPE_PREFIX + ec.getQualifiedName(), false);
		}

		writeEndXSDElement();

		attributes.clear();
		collectAttributes(gc, attributes);
		writeAttributes(attributes);

		writeEndXSDElement();
		writeEndXSDElement();
		writeEndXSDElement();
	}

	private void writeDefaultComplexTypes() throws XMLStreamException {
		String attElem = XSD_COMPLEXTYPE_ATTRIBUTED_ELEMENT;
		writeStartXSDComplexType(attElem, true, false);

		writeStartXSDComplexType(XSD_COMPLEXTYPE_GRAPH, true, true);
		writeStartXSDExtension(attElem, true);
		writeXSDAttribute(XSD_ATTRIBUTE_ID, XML_ID, XSD_REQUIRED);
		writeEndXSDElement();
		writeEndXSDElement();
		writeEndXSDElement();

		writeStartXSDComplexType(XSD_COMPLEXTYPE_VERTEX, true, true);
		writeStartXSDExtension(attElem, true);
		writeXSDAttribute(XSD_ATTRIBUTE_ID, XML_ID, XSD_REQUIRED);
		writeEndXSDElement();
		writeEndXSDElement();
		writeEndXSDElement();

		String integer = namespacePrefix + ":" + DOMAIN_INTEGER;
		writeStartXSDComplexType(XSD_COMPLEXTYPE_EDGE, true, true);
		writeStartXSDExtension(attElem, true);
		writeXSDAttribute(XSD_ATTRIBUTE_FROM, XML_IDREF, XSD_REQUIRED);
		writeXSDAttribute(XSD_ATTRIBUTE_TO, XML_IDREF, XSD_REQUIRED);
		writeXSDAttribute(XSD_ATTRIBUTE_FSEQ, integer);
		writeXSDAttribute(XSD_ATTRIBUTE_TSEQ, integer);
		writeEndXSDElement();
		writeEndXSDElement();
		writeEndXSDElement();
	}

	private void writeEdgeClassComplexTypes() throws XMLStreamException {
		for (EdgeClass ec : schemaGraph.getEdgeClassVertices()) {
			if (ec.isIsAbstract() || !isIncluded(ec)) {
				continue;
			}

			xml.writeComment(commentEdgeClass(ec));

			// first the complex type
			writeStartXSDComplexType(XSD_COMPLEX_EDGETYPE_PREFIX
					+ ec.getQualifiedName(), false, true);

			attributes.clear();
			collectAttributes(ec, attributes);

			if (attributes.size() > 0) {
				writeStartXSDExtension(XSD_COMPLEXTYPE_EDGE, true);

				writeAttributes(attributes);

				writeEndXSDElement(); // ends extension
			} else {
				writeStartXSDExtension(XSD_COMPLEXTYPE_EDGE, false);
			}
			writeEndXSDElement(); // ends complexContent
			writeEndXSDElement(); // ends complexType
		}
	}

	private void writeAttributes(ArrayList<Attribute> attributeList)
			throws XMLStreamException {
		for (Attribute attribute : attributeList) {
			writeAttribute(attribute);
		}
	}

	private String commentEdgeClass(EdgeClass edgeClass) {
		StringWriter stringWriter = new StringWriter();

		sg2tg.setStream(stringWriter);
		sg2tg.printEdgeClassDefinition(edgeClass);

		StringBuffer sb = stringWriter.getBuffer();
		sb.deleteCharAt(sb.length() - 1);
		writeInheritedAttributes(edgeClass, stringWriter);
		return stringWriter.toString();
	}

	private String commentVertexClass(VertexClass vertexClass) {
		StringWriter stringWriter = new StringWriter();

		sg2tg.setStream(stringWriter);
		sg2tg.printVertexClassDefinition(vertexClass);

		StringBuffer sb = stringWriter.getBuffer();
		sb.deleteCharAt(sb.length() - 1);
		writeInheritedAttributes(vertexClass, stringWriter);
		return stringWriter.toString();
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

	private void writeVertexClassComplexTypes() throws XMLStreamException {
		for (VertexClass vc : schemaGraph.getVertexClassVertices()) {

			if (vc.isIsAbstract() || !isIncluded(vc)) {
				continue;
			}

			xml.writeComment(commentVertexClass(vc));

			// first the complex type
			writeStartXSDComplexType(XSD_COMPLEX_VERTEXTYPE_PREFIX
					+ vc.getQualifiedName(), false, true);

			attributes.clear();
			collectAttributes(vc, attributes);

			if (attributes.size() > 0) {
				writeStartXSDExtension(XSD_COMPLEXTYPE_VERTEX, true);

				writeAttributes(attributes);

				writeEndXSDElement(); // ends extension
			} else {
				writeStartXSDExtension(XSD_COMPLEXTYPE_VERTEX, false);
			}
			writeEndXSDElement(); // ends complexContent
			writeEndXSDElement(); // ends complexType
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

	private void writeAttribute(Attribute attribute) throws XMLStreamException {

		String name = attribute.getName();
		Domain type = (Domain) attribute.getFirstHasDomain(EdgeDirection.OUT)
				.getOmega();
		writeXSDAttribute(name, getXSDType(type));
	}

	private void writeStartXSDComplexType(String name, boolean isAbstract,
			boolean hasContent) throws XMLStreamException {

		if (hasContent) {
			xml.writeStartElement(XSD_NS_PREFIX, XSD_COMPLEXTYPE,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
		} else {
			xml.writeEmptyElement(XSD_NS_PREFIX, XSD_COMPLEXTYPE,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
		}

		xml.writeAttribute(XSD_ATTRIBUTE_NAME, name);
		xml.writeAttribute(XSD_ATTRIBUTE_ABSTRACT, isAbstract ? TRUE : FALSE);
	}

	private void writeStartXSDElement(String name, String type,
			boolean withContent) throws XMLStreamException {

		if (withContent) {
			xml.writeStartElement(XSD_NS_PREFIX, XSD_ELEMENT,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
		} else {
			xml.writeEmptyElement(XSD_NS_PREFIX, XSD_ELEMENT,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
		}

		xml.writeAttribute(XSD_ATTRIBUTE_NAME, name);
		xml.writeAttribute(XSD_ATTRIBUTE_TYPE, namespacePrefix + ":" + type);
	}

	private void writeStartXSDExtension(String extendedType,
			boolean complexContent) throws XMLStreamException {

		// Is needed for an extension
		xml.writeStartElement(XSD_NS_PREFIX, XSD_COMPLEXCONTENT,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);

		if (complexContent) {
			xml.writeStartElement(XSD_NS_PREFIX, XSD_EXTENSION,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
		} else {
			xml.writeEmptyElement(XSD_NS_PREFIX, XSD_EXTENSION,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
		}

		xml.writeAttribute(XSD_ATTRIBUTE_BASE, namespacePrefix + ":"
				+ extendedType);
	}

	/**
	 * @throws XMLStreamException
	 */
	private void writeEndXSDElement() throws XMLStreamException {
		xml.writeEndElement();
	}

	private void writeXSDAttribute(String name, String type)
			throws XMLStreamException {
		writeXSDAttribute(name, type, XSD_REQUIRED);
	}

	private void writeXSDAttribute(String name, String type, String use)
			throws XMLStreamException {
		xml.writeEmptyElement(XSD_NS_PREFIX, XSD_ATTRIBUTE,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeAttribute(XSD_ATTRIBUTE_NAME, name);
		xml.writeAttribute(XSD_ATTRIBUTE_TYPE, type);
		if (use != null) {
			xml.writeAttribute(XSD_ATTRIBUTE_USE, use);
		}

	}

	private String getXSDTypeWithoutPrefix(Domain domain) {

		if (domain instanceof IntegerDomain) {
			return DOMAIN_INTEGER;
		} else if (domain instanceof LongDomain) {
			return DOMAIN_LONG;
		} else if (domain instanceof BooleanDomain) {
			return DOMAIN_BOOLEAN;
		} else if (domain instanceof DoubleDomain) {
			return DOMAIN_DOUBLE;
		} else if (domain instanceof StringDomain) {
			return DOMAIN_STRING;
		} else if (domain instanceof SetDomain) {
			return DOMAIN_SET;
		} else if (domain instanceof ListDomain) {
			return DOMAIN_LIST;
		} else if (domain instanceof MapDomain) {
			return DOMAIN_MAP;
		} else if (domain instanceof RecordDomain) {
			return queryDomainType(domain);
		} else if (domain instanceof EnumDomain) {
			return queryDomainType(domain);
		}
		throw new RuntimeException("Unknown domain '"
				+ domain.getQualifiedName() + "'.");
	}

	private String getXSDType(Domain domain) {
		return namespacePrefix + ":" + getXSDTypeWithoutPrefix(domain);
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
			qualifiedName = DOMAIN_ENUM_PREFIX;
		} else if (domain instanceof RecordDomain) {
			qualifiedName = DOMAIN_RECORD_PREFIX;
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
					createRecordDomainType(entry.getKey(), entry.getValue());
				} else {
					throw new RuntimeException("Unknown domain " + d + " ("
							+ d.getQualifiedName() + ")");
				}
			}
		}
	}

	private void createRecordDomainType(Domain domain, String typeName)
			throws XMLStreamException {

		writeStartXSDSimpleType(typeName);

		String[] pattern = null;
		if (domain instanceof RecordDomain) {
			xml
					.writeComment(generateRecordDomainComment((RecordDomain) domain));
			pattern = RECORD_DOMAIN_PATTERNS;
		} else {
			throw new RuntimeException("The type '" + domain.getClass()
					+ "' of domain '" + domain.getQualifiedName()
					+ "' is not supported.");
		}
		writeStartXSDRestriction(XSD_DOMAIN_STRING, pattern);
		writeEndXSDElement();
	}

	private String generateRecordDomainComment(RecordDomain domain) {
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

		xml.writeStartElement(XSD_NS_PREFIX, XSD_SIMPLETYPE,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeAttribute(XSD_ATTRIBUTE_NAME, typeName);

		xml.writeStartElement(XSD_NS_PREFIX, XSD_RESTRICTION,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeAttribute(XSD_ATTRIBUTE_BASE, XSD_DOMAIN_STRING);

		for (String enumConst : constants) {
			xml.writeEmptyElement(XSD_NS_PREFIX, XSD_ENUMERATION,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
			if (enumConst.equals("n")) {
				throw new RuntimeException("The enumeration as Type '"
						+ typeName + "' alreay defines the constant \"n\".");
			}
			xml.writeAttribute(XSD_ENUMERATION_VALUE, enumConst);

		}
		if (nullable) {
			xml.writeEmptyElement(XSD_NS_PREFIX, XSD_ENUMERATION,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
			xml.writeAttribute(XSD_ENUMERATION_VALUE, "n");
		}

		xml.writeEndElement();
		xml.writeEndElement();
	}

	public boolean add(Attribute e) {
		return attributes.add(e);
	}

	private void writeStartXSDSchema() throws XMLStreamException {

		xml.writeStartDocument();

		xml.writeStartElement(XSD_NS_PREFIX, XSD_SCHEMA,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);

		xml.writeNamespace(XSD_NS_PREFIX, XMLConstants.W3C_XML_SCHEMA_NS_URI);

		Schema schema = schemaGraph.getFirstSchema();

		String namespace = schema.getPackagePrefix() + "." + schema.getName();

		namespace = UtilityMethods.generateURI(namespace);

		xml.writeNamespace(namespacePrefix, namespace);
		xml.writeAttribute("targetNamespace", namespace);
	}

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

		SchemaGraph sg = comLine.hasOption('g') ? GrumlSchema.instance()
				.loadSchemaGraph(inputFile, new ProgressFunctionImpl())
				: new Tg2SchemaGraph().process(inputFile);

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

		System.out.println("Fini.");
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
		input.setRequired(false);
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

	public void setDebugOutputStream(PrintStream debugOutputStream) {
		this.debugOutputStream = debugOutputStream;
	}

	public void setAutoExclude(boolean autoExclude) {
		this.autoExclude = autoExclude;
	}
}
