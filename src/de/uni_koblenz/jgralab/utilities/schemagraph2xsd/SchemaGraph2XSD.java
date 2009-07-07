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
package de.uni_koblenz.jgralab.utilities.schemagraph2xsd;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.grumlschema.GrumlSchema;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.grumlschema.domains.BooleanDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.Domain;
import de.uni_koblenz.jgralab.grumlschema.domains.DoubleDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.EnumDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.IntDomain;
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
import de.uni_koblenz.jgralab.utilities.jgralab2owl.IndentingXMLStreamWriter;
import de.uni_koblenz.jgralab.utilities.rsa2tg.SchemaGraph2Tg;
import de.uni_koblenz.jgralab.utilities.tg2xml.Tg2xml;

/**
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 */
@WorkInProgress(description = "Converter from SchemaGraph to XML Schema", responsibleDevelopers = "horn, mmce, riediger", expectedFinishingDate = "2009/06/30")
public class SchemaGraph2XSD {

	private static final String DOMAIN_RECORD = "ST_RECORD";
	private static final String DOMAIN_SET = "ST_SET";
	private static final String DOMAIN_LIST = "ST_LIST";
	private static final String DOMAIN_MAP = "ST_MAP";
	private static final String XSD_SIMPLETYPE_ENUM_PREFIX = "ST_ENUM_";

	private static final String XSD_COMPLEXCONTENT = "complexContent";
	private static final String TRUE = "true";
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
	private static final String XSD_DOMAIN_BOOLEAN = XSD_NS_PREFIX_PLUS_COLON
			+ "boolean";
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
	private static final String XSD_ATTRIBUTE = "attribute";
	private static final String XSD_ATTRIBUTE_TO = "to";
	private static final String XSD_ATTRIBUTE_TSEQ = "tseq";
	private static final String XSD_ATTRIBUTE_FROM = "from";
	private static final String XSD_ATTRIBUTE_FSEQ = "fseq";
	private static final String XSD_ATTRIBUTE_ID = "id";
	private static final String XSD_ELEMENT = "element";
	private static final String XSD_ATTRIBUTE_TYPE = "type";
	private static final String XSD_ATTRIBUTE_NAME = "name";
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
	 * This map links Domain-objects to existing enumeration types described by
	 * a string.
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
	}

	public void writeXSD() throws XMLStreamException {

		for (Domain domain : schemaGraph.getDomainVertices()) {
			getXSDType(domain);
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
		xml.writeComment("Enumeration types");
		writeAllDomainTypes();

		// ends the schema
		xml.writeEndDocument();
		xml.flush();
	}

	private void writeDefaultSimpleTypes() throws XMLStreamException {

		writeRestrictedString(DOMAIN_MAP);
		writeRestrictedString(DOMAIN_LIST);
		writeRestrictedString(DOMAIN_SET);
		writeRestrictedString(DOMAIN_RECORD);
	}

	private void writeRestrictedString(String string) throws XMLStreamException {
		writeStartXSDSimpleType(string);
		writeStartXSDRestriction(XSD_DOMAIN_STRING, true);
		writeEndXSDElement();
	}

	private void writeStartXSDRestriction(String type, boolean empty)
			throws XMLStreamException {

		if (empty) {
			xml.writeEmptyElement(XSD_NS_PREFIX, XSD_RESTRICTION,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
		} else {
			xml.writeStartElement(XSD_NS_PREFIX, XSD_RESTRICTION,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
		}
		xml.writeAttribute(XSD_ATTRIBUTE_BASE, type);
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
			if (vc.isIsAbstract()) {
				continue;
			}
			writeStartXSDElement(vc.getQualifiedName(),
					XSD_COMPLEX_VERTEXTYPE_PREFIX + vc.getQualifiedName(),
					false);
		}
		for (EdgeClass ec : schemaGraph.getEdgeClassVertices()) {
			if (ec.isIsAbstract()) {
				continue;
			}
			writeStartXSDElement(ec.getQualifiedName(),
					XSD_COMPLEX_EDGETYPE_PREFIX + ec.getQualifiedName(), false);
		}

		writeEndXSDElement();

		writeAttributes(gc);

		writeEndXSDElement();
		writeEndXSDElement();
		writeEndXSDElement();
	}

	private void writeDefaultComplexTypes() throws XMLStreamException {
		String attElem = XSD_COMPLEXTYPE_ATTRIBUTED_ELEMENT;
		writeStartXSDComplexType(attElem, false, false);

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

		writeStartXSDComplexType(XSD_COMPLEXTYPE_EDGE, true, true);
		writeStartXSDExtension(attElem, true);
		writeXSDAttribute(XSD_ATTRIBUTE_FROM, XML_IDREF, XSD_REQUIRED);
		writeXSDAttribute(XSD_ATTRIBUTE_TO, XML_IDREF, XSD_REQUIRED);
		writeXSDAttribute(XSD_ATTRIBUTE_FSEQ, XSD_DOMAIN_INTEGER);
		writeXSDAttribute(XSD_ATTRIBUTE_TSEQ, XSD_DOMAIN_INTEGER);
		writeEndXSDElement();
		writeEndXSDElement();
		writeEndXSDElement();
	}

	private void writeEdgeClassComplexTypes() throws XMLStreamException {
		for (EdgeClass ec : schemaGraph.getEdgeClassVertices()) {
			if (ec.isIsAbstract()) {
				continue;
			}

			xml.writeComment(commentEdgeClass(ec));

			// first the complex type
			writeStartXSDComplexType(XSD_COMPLEX_EDGETYPE_PREFIX
					+ ec.getQualifiedName(), false, true);

			if (ec.getDegree(HasAttribute.class, EdgeDirection.OUT) > 0) {
				writeStartXSDExtension(XSD_COMPLEXTYPE_EDGE, true);
				writeAttributes(ec);
				writeEndXSDElement(); // ends extension
			} else {
				writeStartXSDExtension(XSD_COMPLEXTYPE_EDGE, false);
			}
			writeEndXSDElement(); // ends complexContent
			writeEndXSDElement(); // ends complexType
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
			if (vc.isIsAbstract()) {
				continue;
			}

			xml.writeComment(commentVertexClass(vc));

			// first the complex type
			writeStartXSDComplexType(XSD_COMPLEX_VERTEXTYPE_PREFIX
					+ vc.getQualifiedName(), false, true);
			if (vc.getDegree(HasAttribute.class, EdgeDirection.OUT) > 0) {
				writeStartXSDExtension(XSD_COMPLEXTYPE_VERTEX, true);

				writeAttributes(vc);

				writeEndXSDElement(); // ends extension
			} else {
				writeStartXSDExtension(XSD_COMPLEXTYPE_VERTEX, false);
			}
			writeEndXSDElement(); // ends complexContent
			writeEndXSDElement(); // ends complexType
		}
	}

	private void writeAttributes(AttributedElementClass attrElemClass)
			throws XMLStreamException {
		for (HasAttribute ha : attrElemClass
				.getHasAttributeIncidences(EdgeDirection.OUT)) {
			Attribute attr = (Attribute) ha.getOmega();
			String name = attr.getName();
			Domain type = (Domain) attr.getFirstHasDomain(EdgeDirection.OUT)
					.getOmega();
			writeXSDAttribute(name, getXSDType(type));
		}
		if (attrElemClass instanceof VertexClass) {
			for (SpecializesVertexClass s : ((VertexClass) attrElemClass)
					.getSpecializesVertexClassIncidences(EdgeDirection.OUT)) {
				writeAttributes((VertexClass) s.getOmega());
			}
		} else if (attrElemClass instanceof EdgeClass) {
			for (SpecializesEdgeClass s : ((EdgeClass) attrElemClass)
					.getSpecializesEdgeClassIncidences(EdgeDirection.OUT)) {
				writeAttributes((EdgeClass) s.getOmega());
			}
		} else if (attrElemClass instanceof GraphClass) {
			// nothing to do here
		} else {
			throw new RuntimeException("Don't know what to do with '"
					+ attrElemClass.getQualifiedName() + "'.");
		}
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
		if (isAbstract) {
			xml.writeAttribute(XSD_ATTRIBUTE_ABSTRACT, TRUE);
		}
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

	private void writeEndXSDElement() throws XMLStreamException {
		xml.writeEndElement();
	}

	private void writeXSDAttribute(String name, String type)
			throws XMLStreamException {
		writeXSDAttribute(name, type, null);
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

	private String getXSDType(Domain domain) {
		if (domain instanceof IntDomain) {
			return XSD_DOMAIN_INTEGER;
		} else if (domain instanceof LongDomain) {
			return XSD_DOMAIN_LONG;
		} else if (domain instanceof BooleanDomain) {
			return XSD_DOMAIN_BOOLEAN;
		} else if (domain instanceof DoubleDomain) {
			return XSD_DOMAIN_DOUBLE;
		} else if (domain instanceof StringDomain) {
			return XSD_DOMAIN_STRING;
		} else if (domain instanceof SetDomain) {
			return namespacePrefix + ":" + DOMAIN_SET;
		} else if (domain instanceof ListDomain) {
			return namespacePrefix + ":" + DOMAIN_LIST;
		} else if (domain instanceof MapDomain) {
			return namespacePrefix + ":" + DOMAIN_MAP;
		} else if (domain instanceof RecordDomain) {
			return namespacePrefix + ":" + DOMAIN_RECORD;
		}

		return namespacePrefix + ":" + queryDomainType(domain);
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
		String qualifiedName = XSD_SIMPLETYPE_ENUM_PREFIX
				+ domain.getQualifiedName();
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

		// Loop over all existing EnumDomains.
		for (Entry<Domain, String> entry : domainMap.entrySet()) {
			Domain d = entry.getKey();
			if (d instanceof EnumDomain) {
				createEnumDomainType((EnumDomain) entry.getKey(), entry
						.getValue());
			} else {
				createComplexDomain(entry.getKey(), entry.getValue());
			}
		}
	}

	private void createComplexDomain(Domain domain, String typeName)
			throws XMLStreamException {
		//
		xml.writeStartElement(XSD_NS_PREFIX, XSD_SIMPLETYPE,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeAttribute(XSD_ATTRIBUTE_NAME, typeName);

		xml.writeStartElement(XSD_NS_PREFIX, XSD_RESTRICTION,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeAttribute(XSD_ATTRIBUTE_BASE, XSD_DOMAIN_STRING);

		xml.writeEndElement();
		xml.writeEndElement();
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

		xml.writeStartElement(XSD_NS_PREFIX, XSD_SIMPLETYPE,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeAttribute(XSD_ATTRIBUTE_NAME, typeName);

		xml.writeStartElement(XSD_NS_PREFIX, XSD_RESTRICTION,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeAttribute(XSD_ATTRIBUTE_BASE, XSD_DOMAIN_STRING);

		for (String enumConst : domain.getEnumConstants()) {
			xml.writeEmptyElement(XSD_NS_PREFIX, XSD_ENUMERATION,
					XMLConstants.W3C_XML_SCHEMA_NS_URI);
			xml.writeAttribute(XSD_ENUMERATION_VALUE, enumConst);
		}

		xml.writeEndElement();
		xml.writeEndElement();
	}

	private void writeStartXSDSchema() throws XMLStreamException {

		xml.writeStartDocument();

		xml.writeStartElement(XSD_NS_PREFIX, XSD_SCHEMA,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);

		xml.writeNamespace(XSD_NS_PREFIX, XMLConstants.W3C_XML_SCHEMA_NS_URI);

		Schema schema = schemaGraph.getFirstSchema();

		String namespace = schema.getPackagePrefix() + "." + schema.getName();

		namespace = Tg2xml.generateURI(namespace);

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
		if (args.length != 3) {
			usage();
		}
		String schemaGraphFile = args[0].trim();
		String namespacePrefix = args[1].trim();
		String xsdFile = args[2].trim();

		SchemaGraph sg = GrumlSchema.instance().loadSchemaGraph(
				schemaGraphFile, new ProgressFunctionImpl());
		SchemaGraph2XSD t2xsd = new SchemaGraph2XSD(sg, namespacePrefix,
				xsdFile);
		t2xsd.writeXSD();

		System.out.println("Fini.");
	}

	private static void usage() {
		System.err
				.println("Usage: java SchemaGraph2XSD SchemaGraphFile.tg NamespacePrefix  XsdFile.xsd");
		System.exit(1);
	}

}
