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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.grumlschema.domains.BooleanDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.Domain;
import de.uni_koblenz.jgralab.grumlschema.domains.DoubleDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.EnumDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.IntDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.LongDomain;
import de.uni_koblenz.jgralab.grumlschema.domains.StringDomain;
import de.uni_koblenz.jgralab.grumlschema.structure.Attribute;
import de.uni_koblenz.jgralab.grumlschema.structure.AttributedElementClass;
import de.uni_koblenz.jgralab.grumlschema.structure.EdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.GraphClass;
import de.uni_koblenz.jgralab.grumlschema.structure.HasAttribute;
import de.uni_koblenz.jgralab.grumlschema.structure.SpecializesEdgeClass;
import de.uni_koblenz.jgralab.grumlschema.structure.SpecializesVertexClass;
import de.uni_koblenz.jgralab.grumlschema.structure.VertexClass;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.Schema2SchemaGraph;

/**
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 */
public class SchemaGraph2XSD {

	private static final String XSD_ENUMERATION_VALUE = "value";
	private static final String XSD_ENUMERATION = "enumeration";
	private static final String XSD_RESTRICTION = "restriction";
	private static final String XSD_SIMPLETYPE = "simpleType";
	private static final String XSD_SCHEMA = "schema";
	private final static String XSD_NS_PREFIX = "xsd";
	private static final String XSD_DOMAIN_STRING = XSD_NS_PREFIX + ":"
			+ "string";
	private static final String XSD_DOMAIN_DECIMAL = XSD_NS_PREFIX + ":"
			+ "decimal";
	private static final String XSD_DOMAIN_BOOLEAN = XSD_NS_PREFIX + ":"
			+ "boolean";
	private static final String XSD_DOMAIN_LONG = XSD_NS_PREFIX + ":" + "long";
	private static final String XSD_DOMAIN_INTEGER = XSD_NS_PREFIX + ":"
			+ "integer";

	private static final String XML_IDREF = XSD_NS_PREFIX + ":" + "IDREF";
	private static final String XML_ID = XSD_NS_PREFIX + ":" + "ID";

	private static final String XSD_ATTRIBUTE_MAX_OCCURS = "maxOccurs";
	private static final String XSD_ATTRIBUTE_MIN_OCCURS = "minOccurs";
	private static final String XSD_CHOICE = "choice";
	private static final String XSD_SEQUENCE = "sequence";
	private static final String XSD_ATTRIBUTE_BASE = "base";
	private static final String XSD_EXTENSION = "extension";
	private static final String XSD_ATTRIBUTE = "attribute";
	private static final String XSD_ATTRIBUTE_TO = "to";
	private static final String XSD_ATTRIBUTE_FROM = "from";
	private static final String XSD_COMPLEXTYPE_GRAPH = "CT_Graph";
	private static final String XSD_ATTRIBUTE_ID = "id";
	private static final String XSD_COMPLEXTYPE_ATTRIBUTED_ELEMENT = "CT_AttributedElement";
	private static final String XSD_ELEMENT = "element";
	private static final String XSD_ATTRIBUTE_TYPE = "type";
	private static final String XSD_ATTRIBUTE_NAME = "name";
	private static final String XSD_COMPLEXTYPE = "complexType";
	private static final String XSD_COMPLEXTYPE_VERTEX = "CT_Vertex";
	private static final String XSD_COMPLEXTYPE_EDGE = "CT_Edge";
	private static final String XSD_COMPLEXTYPE_PREFIX = "CT_";

	protected XMLStreamWriter xml;
	protected SchemaGraph schemaGraph;

	/**
	 * This map links Domain-objects to existing enumeration types discribed by
	 * a string.
	 */
	private final Map<EnumDomain, String> enumMap;

	public SchemaGraph2XSD(SchemaGraph sg, String outFile)
			throws FileNotFoundException, XMLStreamException,
			FactoryConfigurationError {
		xml = XMLOutputFactory.newInstance().createXMLStreamWriter(
				new FileOutputStream(outFile));
		schemaGraph = sg;

		enumMap = new HashMap<EnumDomain, String>();

	}

	public void writeXSD() throws XMLStreamException {
		xml.writeStartDocument();
		writeStartXSDSchema();

		// now vertex and edge classes
		writeVertexClassComplexTypes();
		writeEdgeClassComplexTypes();

		// now the graph class
		writeGraphClass();

		// write the default complex types
		writeDefaultComplexTypes();
		// write all enumeration types
		writeAllEnumDomainTypes();

		// ends the schema
		writeEndXSDSchema();
		xml.writeEndDocument();
		xml.flush();
	}

	private void writeEndXSDSchema() throws XMLStreamException {
		xml.writeEndElement();
		xml.writeCharacters("\n");
	}

	private void writeEdgeClassComplexTypes() throws XMLStreamException {
		for (EdgeClass ec : schemaGraph.getEdgeClassVertices()) {
			if (ec.isIsAbstract()) {
				continue;
			}
			// first the complex type
			writeStartXSDComplexType(XSD_COMPLEXTYPE_PREFIX
					+ ec.getQualifiedName());
			writeXSDExtension(XSD_COMPLEXTYPE_EDGE);
			writeAttributes(ec);
			writeEndXSDSchema();
		}
	}

	private void writeVertexClassComplexTypes() throws XMLStreamException {
		for (VertexClass vc : schemaGraph.getVertexClassVertices()) {
			if (vc.isIsAbstract()) {
				continue;
			}
			// first the complex type
			writeStartXSDComplexType(XSD_COMPLEXTYPE_PREFIX
					+ vc.getQualifiedName());
			writeXSDExtension(XSD_COMPLEXTYPE_VERTEX);
			writeAttributes(vc);
			writeEndXSDSchema();
		}
	}

	private void writeStartXSDComplexType(String name)
			throws XMLStreamException {
		xml.writeStartElement(XSD_NS_PREFIX, XSD_COMPLEXTYPE,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeAttribute(XSD_ATTRIBUTE_NAME, name);
		xml.writeCharacters("\n");
	}

	private void writeStartXSDElement(String name, String type)
			throws XMLStreamException {
		xml.writeStartElement(XSD_NS_PREFIX, XSD_ELEMENT,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeAttribute(XSD_ATTRIBUTE_NAME, name);
		xml.writeAttribute(XSD_ATTRIBUTE_TYPE, type);
		xml.writeCharacters("\n");
	}

	private void writeDefaultComplexTypes() throws XMLStreamException {
		String attElem = XSD_COMPLEXTYPE_ATTRIBUTED_ELEMENT;
		writeStartXSDComplexType(attElem);
		writeXSDAttribute(XSD_ATTRIBUTE_ID, XML_ID);
		writeEndXSDSchema();

		writeStartXSDComplexType(XSD_COMPLEXTYPE_GRAPH);
		writeXSDExtension(attElem);
		writeEndXSDSchema();

		writeStartXSDComplexType(XSD_COMPLEXTYPE_VERTEX);
		writeXSDExtension(attElem);
		writeEndXSDSchema();

		writeStartXSDComplexType(XSD_COMPLEXTYPE_EDGE);
		writeXSDExtension(attElem);
		writeXSDAttribute(XSD_ATTRIBUTE_FROM, XML_IDREF);
		writeXSDAttribute(XSD_ATTRIBUTE_TO, XML_IDREF);
		writeEndXSDSchema();

	}

	private void writeXSDAttribute(String name, String type)
			throws XMLStreamException {
		xml.writeStartElement(XSD_NS_PREFIX, XSD_ATTRIBUTE,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeAttribute(XSD_ATTRIBUTE_NAME, name);
		xml.writeAttribute(XSD_ATTRIBUTE_TYPE, type);
		writeEndXSDSchema();

	}

	private void writeXSDExtension(String extendedType)
			throws XMLStreamException {
		// xml.writeStartElement(XSD_NS_PREFIX, "complexContent",
		// XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeStartElement(XSD_NS_PREFIX, XSD_EXTENSION,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeAttribute(XSD_ATTRIBUTE_BASE, extendedType);
		writeEndXSDSchema();

		// writeEndXSDElement();

	}

	private void writeGraphClass() throws XMLStreamException {
		GraphClass gc = schemaGraph.getFirstGraphClass();
		writeStartXSDComplexType(XSD_COMPLEXTYPE_PREFIX + gc.getQualifiedName());
		writeXSDExtension(XSD_COMPLEXTYPE_GRAPH);
		writeAttributes(gc);

		xml.writeStartElement(XSD_NS_PREFIX, XSD_SEQUENCE,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeStartElement(XSD_NS_PREFIX, XSD_CHOICE,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeAttribute(XSD_ATTRIBUTE_MIN_OCCURS, "0");
		xml.writeAttribute(XSD_ATTRIBUTE_MAX_OCCURS, "unbounded");
		for (VertexClass vc : schemaGraph.getVertexClassVertices()) {
			if (vc.isIsAbstract()) {
				continue;
			}
			writeStartXSDElement(vc.getQualifiedName(), XSD_COMPLEXTYPE_PREFIX
					+ vc.getQualifiedName());
			writeEndXSDSchema();

		}
		for (EdgeClass ec : schemaGraph.getEdgeClassVertices()) {
			if (ec.isIsAbstract()) {
				continue;
			}
			writeStartXSDElement(ec.getQualifiedName(), XSD_COMPLEXTYPE_PREFIX
					+ ec.getQualifiedName());
			writeEndXSDSchema();
		}
		writeEndXSDSchema(); // end sequence
		writeEndXSDSchema(); // end choice
		writeEndXSDSchema(); // end complexType

		// finally create an element for the graph class
		writeStartXSDElement(gc.getQualifiedName(), XSD_COMPLEXTYPE_PREFIX
				+ gc.getQualifiedName());
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

	private String getXSDType(Domain domain) {
		if (domain instanceof IntDomain) {
			return XSD_DOMAIN_INTEGER;
		}
		if (domain instanceof LongDomain) {
			return XSD_DOMAIN_LONG;
		}
		if (domain instanceof BooleanDomain) {
			return XSD_DOMAIN_BOOLEAN;
		}
		if (domain instanceof DoubleDomain) {
			return XSD_DOMAIN_DECIMAL;
		}
		if (domain instanceof StringDomain) {
			return XSD_DOMAIN_STRING;
		}
		if (domain instanceof EnumDomain) {
			// TODO: Implement this string restriction thing!
			return queryEnumType((EnumDomain) domain);
		}

		return XSD_DOMAIN_STRING;
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
	private String queryEnumType(EnumDomain domain) {

		// Returns an existing mapping.
		if (enumMap.containsKey(domain)) {
			return enumMap.get(domain);
		}

		// Creates a new type string.
		String enumTypeName = XSD_COMPLEXTYPE_PREFIX
				+ domain.getQualifiedName();
		assert (!enumMap.values().contains(enumTypeName)) : "FIXME! \"enumMap\" already contains a string \""
				+ enumTypeName + "\"!";

		// Stores the new type string.
		enumMap.put(domain, enumTypeName);

		return enumTypeName;
	}

	/**
	 * Creates all EnumDomain types contained in the map <code>enumMap</code>.
	 * 
	 * @throws XMLStreamException
	 */
	private void writeAllEnumDomainTypes() throws XMLStreamException {

		// Loop over all existing EnumDomains.
		for (Entry<EnumDomain, String> entry : enumMap.entrySet()) {
			createEnumDomainType(entry.getKey(), entry.getValue());
		}
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

		// 
		xml.writeStartElement(XSD_NS_PREFIX, XSD_SIMPLETYPE);
		xml.writeAttribute(XSD_ATTRIBUTE_NAME, typeName);

		xml.writeStartElement(XSD_NS_PREFIX, XSD_RESTRICTION);
		xml.writeAttribute(XSD_ATTRIBUTE_BASE, XSD_DOMAIN_STRING);

		for (String enumConst : domain.getEnumConstants()) {
			xml.writeStartElement(XSD_NS_PREFIX, XSD_ENUMERATION);
			xml.writeAttribute(XSD_ENUMERATION_VALUE, enumConst);
			xml.writeEndElement();
		}

		xml.writeEndElement();
		xml.writeEndElement();
	}

	private void writeStartXSDSchema() throws XMLStreamException {
		xml.writeStartElement(XSD_NS_PREFIX, XSD_SCHEMA,
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		// TODO: I don't know which attributes the schema element has... At
		// least something like http://jgralab.uni-koblenz.de/SoamigSchema
		// should be given here...
		xml.writeNamespace(XSD_NS_PREFIX, XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeAttribute("elementFormDefault", "qualified");
		xml.writeAttribute("attributeFormDefault", "qualified");
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
		if (args.length != 2) {
			usage();
		}
		String schemaFile = args[0];
		String xsdFile = args[1];

		de.uni_koblenz.jgralab.schema.Schema s = GraphIO
				.loadSchemaFromFile(schemaFile);

		Schema2SchemaGraph s2sg = new Schema2SchemaGraph();
		SchemaGraph sg = s2sg.convert2SchemaGraph(s);
		SchemaGraph2XSD t2xsd = new SchemaGraph2XSD(sg, xsdFile);
		t2xsd.writeXSD();
	}

	private static void usage() {
		System.err
				.println("Usage: java SchemaGraph2XSD graphOrSchema.tg my-xml-schema.xsd");
		System.exit(1);
	}

}
