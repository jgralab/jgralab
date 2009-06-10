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
package de.uni_koblenz.jgralab.utilities.rsa2tg;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

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
import de.uni_koblenz.jgralab.grumlschema.structure.VertexClass;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.Schema2SchemaGraph;

/**
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 *
 */
public class SchemaGraph2XSD {

	private final static String XSD_NS_PREFIX = "xsd";
	protected XMLStreamWriter xml;
	protected SchemaGraph schemaGraph;

	public SchemaGraph2XSD(SchemaGraph sg, String outFile)
			throws FileNotFoundException, XMLStreamException,
			FactoryConfigurationError {
		xml = XMLOutputFactory.newInstance().createXMLStreamWriter(
				new FileOutputStream(outFile));
		schemaGraph = sg;
	}

	public void writeXSD() throws XMLStreamException {
		xml.writeStartDocument();
		writeStartXSDSchema();

		// write the default complex types
		writeDefaultComplexTypes();

		// now vertex and edge classes
		writeVertexClassComplexTypes();
		writeEdgeClassComplexTypes();

		// now the graph class
		writeGraphClass();

		writeEndXSDElement(); // ends the schema
		xml.writeEndDocument();
		xml.flush();
	}

	private void writeEndXSDElement() throws XMLStreamException {
		xml.writeEndElement();
		xml.writeCharacters("\n");
	}

	private void writeEdgeClassComplexTypes() throws XMLStreamException {
		for (EdgeClass ec : schemaGraph.getEdgeClassVertices()) {
			if (ec.isIsAbstract()) {
				continue;
			}
			// first the complex type
			writeStartXSDComplexType("CT_" + ec.getQualifiedName());
			writeXSDExtension("CT_Edge");
			writeAttributes(ec);
			writeEndXSDElement();
		}
	}

	private void writeVertexClassComplexTypes() throws XMLStreamException {
		for (VertexClass vc : schemaGraph.getVertexClassVertices()) {
			if (vc.isIsAbstract()) {
				continue;
			}
			// first the complex type
			writeStartXSDComplexType("CT_" + vc.getQualifiedName());
			writeXSDExtension("CT_Vertex");
			writeAttributes(vc);
			writeEndXSDElement();
		}
	}

	private void writeStartXSDComplexType(String name)
			throws XMLStreamException {
		xml.writeStartElement(XSD_NS_PREFIX, "complexType",
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeAttribute("name", name);
		xml.writeCharacters("\n");
	}

	private void writeStartXSDElement(String name, String type)
			throws XMLStreamException {
		xml.writeStartElement(XSD_NS_PREFIX, "element",
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeAttribute("name", name);
		xml.writeAttribute("type", type);
		xml.writeCharacters("\n");
	}

	private void writeDefaultComplexTypes() throws XMLStreamException {
		String attElem = "CT_AttributedElement";
		writeStartXSDComplexType(attElem);
		writeXSDAttribute("id", XSD_NS_PREFIX + ":ID");
		writeEndXSDElement();

		writeStartXSDComplexType("CT_Graph");
		writeXSDExtension(attElem);
		writeEndXSDElement();

		writeStartXSDComplexType("CT_Vertex");
		writeXSDExtension(attElem);
		writeEndXSDElement();

		writeStartXSDComplexType("CT_Edge");
		writeXSDExtension(attElem);
		writeXSDAttribute("from", XSD_NS_PREFIX + ":IDREF");
		writeXSDAttribute("to", XSD_NS_PREFIX + ":IDREF");
		writeEndXSDElement();

	}

	private void writeXSDAttribute(String name, String type)
			throws XMLStreamException {
		xml.writeStartElement(XSD_NS_PREFIX, "attribute",
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeAttribute("name", name);
		xml.writeAttribute("type", type);
		writeEndXSDElement();

	}

	private void writeXSDExtension(String extendedType)
			throws XMLStreamException {
		xml.writeStartElement(XSD_NS_PREFIX, "complexContent",
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeStartElement(XSD_NS_PREFIX, "extension",
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeAttribute("base", extendedType);
		writeEndXSDElement();

		writeEndXSDElement();

	}

	private void writeGraphClass() throws XMLStreamException {
		GraphClass gc = schemaGraph.getFirstGraphClass();
		writeStartXSDComplexType("CT_" + gc.getQualifiedName());
		writeXSDExtension("CT_Graph");
		writeAttributes(gc);

		xml.writeStartElement(XSD_NS_PREFIX, "sequence",
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeStartElement(XSD_NS_PREFIX, "choice",
				XMLConstants.W3C_XML_SCHEMA_NS_URI);
		xml.writeAttribute("minOccurs", "0");
		xml.writeAttribute("maxOccurs", "unbounded");
		for (VertexClass vc : schemaGraph.getVertexClassVertices()) {
			if (vc.isIsAbstract()) {
				continue;
			}
			writeStartXSDElement(vc.getQualifiedName(), "CT_"
					+ vc.getQualifiedName());
			writeEndXSDElement();

		}
		for (EdgeClass ec : schemaGraph.getEdgeClassVertices()) {
			if (ec.isIsAbstract()) {
				continue;
			}
			writeStartXSDElement(ec.getQualifiedName(), "CT_"
					+ ec.getQualifiedName());
			writeEndXSDElement();
		}
		writeEndXSDElement(); // end sequence
		writeEndXSDElement(); // end choice
		writeEndXSDElement(); // end complexType

		// finally create an element for the graph class
		writeStartXSDElement(gc.getQualifiedName(), "CT_"
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
	}

	private String getXSDType(Domain domain) {
		if (domain instanceof IntDomain) {
			return XSD_NS_PREFIX + ":integer";
		}
		if (domain instanceof LongDomain) {
			return XSD_NS_PREFIX + ":long";
		}
		if (domain instanceof BooleanDomain) {
			return XSD_NS_PREFIX + ":boolean";
		}
		if (domain instanceof DoubleDomain) {
			return XSD_NS_PREFIX + ":decimal";
		}
		if (domain instanceof StringDomain) {
			return XSD_NS_PREFIX + ":string";
		}
		if (domain instanceof EnumDomain) {
			// TODO: Implement this string restriction thing!
			return XSD_NS_PREFIX
					+ "TODO: implement enums as string restrictions";
		}

		return XSD_NS_PREFIX + ":string";
	}

	private void writeStartXSDSchema() throws XMLStreamException {
		xml.writeStartElement(XSD_NS_PREFIX, "schema",
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
