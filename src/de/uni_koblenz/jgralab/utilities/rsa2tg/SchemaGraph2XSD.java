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

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.grumlschema.SchemaGraph;
import de.uni_koblenz.jgralab.utilities.tg2schemagraph.Tg2SchemaGraph;

/**
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 *
 */
public class SchemaGraph2XSD {

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
		startSchema();

		// write the default complex types
		writeDefaultComplexTypes();

		startGraphClass();

		xml.writeEndElement(); // ends the graphclass
		xml.writeEndElement(); // ends the schema
		xml.writeEndDocument();
	}

	private void writeStartComplexType(String name) throws XMLStreamException {
		xml.writeStartElement("complexType");
		xml.writeAttribute("name", name);
	}

	private void writeStartElement(String name, String type)
			throws XMLStreamException {
		xml.writeStartElement("element");
		xml.writeAttribute("name", name);
		xml.writeAttribute("type", type);
	}

	private void writeDefaultComplexTypes() throws XMLStreamException {
		String attElem = "AttributedElement";
		writeStartComplexType(attElem);
		writeAttributeElement("id", "ID");
		xml.writeEndElement();

		writeStartComplexType("Graph");
		writeExtension(attElem);
		xml.writeEndElement();

		writeStartComplexType("Vertex");
		writeExtension(attElem);
		xml.writeEndElement();

		writeStartComplexType("Edge");
		writeExtension(attElem);
		writeAttributeElement("from", "IDREF");
		writeAttributeElement("to", "IDREF");
		xml.writeEndElement();
	}

	private void writeAttributeElement(String name, String type)
			throws XMLStreamException {
		xml.writeStartElement("attribute");
		xml.writeAttribute("name", name);
		xml.writeAttribute("type", type);
		xml.writeEndElement();
	}

	private void writeExtension(String extendedType) throws XMLStreamException {
		xml.writeStartElement("complexContent");
		xml.writeStartElement("extension");
		xml.writeAttribute("base", extendedType);
		xml.writeEndElement();
		xml.writeEndElement();
	}

	private void startGraphClass() throws XMLStreamException {
		xml.writeStartElement(schemaGraph.getFirstGraphClass()
				.getQualifiedName());
	}

	protected void startSchema() throws XMLStreamException {
		xml.writeStartElement("schema");
		// TODO: I don't know which attributes the schema element has... At
		// least something like http://jgralab.uni-koblenz.de/SoamigSchema
		// should be given here...
		xml.writeAttribute("xmlns", "http://www.w3.org/2001/XMLSchema");
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

		Tg2SchemaGraph t2sg = new Tg2SchemaGraph(s);
		SchemaGraph sg = t2sg.getSchemaGraph();
		SchemaGraph2XSD t2xsd = new SchemaGraph2XSD(sg, xsdFile);
		t2xsd.writeXSD();
	}

	private static void usage() {
		System.err
				.println("Usage: java SchemaGraph2XSD graphOrSchema.tg my-xml-schema.xsd");
		System.exit(1);
	}

}
