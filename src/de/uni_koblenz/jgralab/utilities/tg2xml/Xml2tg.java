/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
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

package de.uni_koblenz.jgralab.utilities.tg2xml;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.GraphMarker;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.schema.Schema;
import static javax.xml.stream.XMLStreamConstants.*;

@WorkInProgress
public class Xml2tg {

	private InputStream xmlInput;
	private OutputStream tgOutput;
	private Schema schema;

	private XMLStreamReader reader;

	private Map<String, Vertex> xmlIdToVertexMap;
	private BooleanGraphMarker dummyVertexMarker;
	private GraphMarker<IncidencePositionMark> incidencePositionMarker;

	private class IncidencePositionMark {
		public int fseq, tseq;
	}

	public static void main(String[] args) throws FileNotFoundException,
			XMLStreamException, GraphIOException {
		Schema currentSchema = GraphIO.loadSchemaFromFile("sample-graph.tg");
		Xml2tg tester = new Xml2tg(new BufferedInputStream(new FileInputStream(
				"sample-graph.xml")), null, currentSchema);
		tester.importXml();
		System.out.println("Fini.");
	}

	public Xml2tg(InputStream xmlInput, OutputStream tgOutput, Schema schema)
			throws XMLStreamException {
		this.xmlInput = xmlInput;
		this.tgOutput = tgOutput;
		xmlIdToVertexMap = new HashMap<String, Vertex>();
		XMLInputFactory factory = XMLInputFactory.newInstance();
		reader = factory.createXMLStreamReader(xmlInput);
	}

	public void importXml() throws XMLStreamException {
		// read root element
		if (reader.hasNext()) {
			int nextEvent = reader.next();
			assert (nextEvent == START_ELEMENT);

		}
		while (reader.hasNext()) {
			int nextEvent = reader.next();
			switch (nextEvent) {
			case START_DOCUMENT:
				System.out.println("It begins");
				break;
			case START_ELEMENT:
				// System.out.println(reader.getName() + ":");
				// int count = reader.getAttributeCount();
				// for(int i = 0; i < count; i++){
				// System.out.print(reader.getAttributeName(i));
				// System.out.print(" = ");
				// System.out.println(reader.getAttributeValue(i));
				// }
				// System.out.println();
				// break;
			default:
			}
		}

	}
}
