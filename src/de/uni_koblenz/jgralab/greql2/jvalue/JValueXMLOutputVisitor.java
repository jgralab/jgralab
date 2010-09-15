/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
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

package de.uni_koblenz.jgralab.greql2.jvalue;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Map.Entry;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import de.uni_koblenz.ist.utilities.xml.IndentingXMLStreamWriter;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.exception.JValueVisitorException;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class JValueXMLOutputVisitor extends JValueDefaultVisitor implements
		JValueXMLConstants {

	private IndentingXMLStreamWriter writer = null;
	private Graph graph;

	public JValueXMLOutputVisitor(JValue val, String fileName) {
		this(val, fileName, null);
	}

	public JValueXMLOutputVisitor(JValue val, String fileName, Graph g) {
		graph = g;
		try {
			writer = new IndentingXMLStreamWriter(XMLOutputFactory
					.newInstance().createXMLStreamWriter(
							new BufferedOutputStream(new FileOutputStream(
									fileName))));
		} catch (FileNotFoundException e) {
			throw new JValueVisitorException("Can't create XML output", null, e);
		} catch (XMLStreamException e) {
			throw new JValueVisitorException("Can't create XML output", null, e);
		} catch (FactoryConfigurationError e) {
			throw new JValueVisitorException("Can't create XML output", null, e);
		}

		head();
		val.accept(this);
		foot();
	}

	@Override
	public void head() {
		try {
			writer.writeStartDocument("UTF-8", "1.0");
			// writer.writeDTD("<!DOCTYPE jvalue [\n"
			// +
			// "<!ENTITY % value \"integer|long|double|string|boolean|list|bag|set|tuple\">\n"
			// + "<!ENTITY % bi \"(browsingInfo?)\">\n"
			// + "<!ELEMENT jvalue (value)>\n" + "<!ATTLIST jvalue\n"
			// + "graphId CDATA #IMPLIED\n" + ">\n"
			// + "<!ELEMENT integer %bi;>\n" + "<!ATTLIST integer\n"
			// + "value PCDATA #REQUIRED\n" + ">\n" + "]>");

			writer.writeStartElement(JVALUE);
			if (graph != null) {
				writer.writeAttribute(ATTR_GRAPH_ID, graph.getId());
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void foot() {
		try {
			writer.writeEndElement();
			writer.writeEndDocument();
			writer.writeCharacters("\n");
			writer.flush();
			writer.close();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.jvalue.JValueDefaultVisitor#
	 * visitAttributedElementClass(de.uni_koblenz.jgralab.greql2.jvalue.JValue)
	 */
	@Override
	public void visitAttributedElementClass(JValue a) {
		AttributedElementClass aec = a.toAttributedElementClass();
		try {
			writer.writeEmptyElement(ATTRIBUTEDELEMENTCLASS);
			writer.writeAttribute(ATTR_NAME, aec.getQualifiedName());
			writer.writeAttribute(ATTR_SCHEMA, aec.getSchema()
					.getQualifiedName());
			writeBrowsingInfo(a);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	private void writeBrowsingInfo(JValue v) throws XMLStreamException {
		AttributedElement bi = v.getBrowsingInfo();
		if (bi == null) {
			return;
		}
		if (bi instanceof GraphElement) {
			GraphElement ge = (GraphElement) bi;
			writer.writeAttribute(ge instanceof Edge ? ATTR_EDGE_LINK
					: ATTR_VERTEX_LINK, Integer.toString(ge.getId()));
			if (ge.getGraph() != graph) {
				writer.writeAttribute(ATTR_GRAPH_LINK,
						String.valueOf(ge.getGraph().getId()));
			}
		} else {
			writer.writeAttribute(ATTR_GRAPH_LINK, ((Graph) bi).getId());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValueDefaultVisitor#visitBag(de
	 * .uni_koblenz.jgralab.greql2.jvalue.JValueBag)
	 */
	@Override
	public void visitBag(JValueBag b) {
		try {
			writer.writeStartElement(BAG);
			writeBrowsingInfo(b);
			super.visitBag(b);
			writer.writeEndElement();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValueDefaultVisitor#visitBoolean
	 * (de.uni_koblenz.jgralab.greql2.jvalue.JValue)
	 */
	@Override
	public void visitBoolean(JValue b) {
		try {
			writer.writeEmptyElement(BOOLEAN);
			writer.writeAttribute(ATTR_VALUE, b.toBoolean().toString());
			writeBrowsingInfo(b);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValueDefaultVisitor#visitDouble
	 * (de.uni_koblenz.jgralab.greql2.jvalue.JValue)
	 */
	@Override
	public void visitDouble(JValue n) {
		try {
			writer.writeEmptyElement(DOUBLE);
			writer.writeAttribute(ATTR_VALUE, n.toDouble().toString());
			writeBrowsingInfo(n);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValueDefaultVisitor#visitEdge(de
	 * .uni_koblenz.jgralab.greql2.jvalue.JValue)
	 */
	@Override
	public void visitEdge(JValue e) {
		Edge edge = e.toEdge();
		try {
			writer.writeEmptyElement(EDGE);
			writer.writeAttribute(ATTR_ID, Integer.toString(edge.getId()));
			if (edge.getGraph() != graph) {
				writer.writeAttribute(ATTR_GRAPH_ID,
						String.valueOf(edge.getGraph().getId()));
			}
			if (edge != e.getBrowsingInfo()) {
				writeBrowsingInfo(e);
			}
		} catch (XMLStreamException ex) {
			ex.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValueDefaultVisitor#visitEnumValue
	 * (de.uni_koblenz.jgralab.greql2.jvalue.JValue)
	 */
	@Override
	public void visitEnumValue(JValue e) {
		Enum<?> val = e.toEnum();
		try {
			writer.writeEmptyElement(ENUM);
			writer.writeAttribute(ATTR_VALUE, val.name());
			writer.writeAttribute(ATTR_TYPE, val.getDeclaringClass()
					.getCanonicalName());
			writeBrowsingInfo(e);
		} catch (XMLStreamException ex) {
			ex.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValueDefaultVisitor#visitGraph(
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValue)
	 */
	@Override
	public void visitGraph(JValue g) {
		Graph graph = g.toGraph();
		try {
			writer.writeEmptyElement(GRAPH);
			writer.writeAttribute(ATTR_GRAPH_ID, graph.getId());
			if (graph != g.getBrowsingInfo()) {
				writeBrowsingInfo(g);
			}
		} catch (XMLStreamException ex) {
			ex.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValueDefaultVisitor#visitInt(de
	 * .uni_koblenz.jgralab.greql2.jvalue.JValue)
	 */
	@Override
	public void visitInt(JValue n) {
		try {
			writer.writeEmptyElement(INTEGER);
			writer.writeAttribute(ATTR_VALUE, n.toInteger().toString());
			writeBrowsingInfo(n);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValueDefaultVisitor#visitList(de
	 * .uni_koblenz.jgralab.greql2.jvalue.JValueList)
	 */
	@Override
	public void visitList(JValueList l) {
		try {
			writer.writeStartElement(LIST);
			writeBrowsingInfo(l);
			super.visitList(l);
			writer.writeEndElement();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValueDefaultVisitor#visitLong(de
	 * .uni_koblenz.jgralab.greql2.jvalue.JValue)
	 */
	@Override
	public void visitLong(JValue l) {
		try {
			writer.writeEmptyElement(LONG);
			writer.writeAttribute(ATTR_VALUE, l.toLong().toString());
			writeBrowsingInfo(l);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValueDefaultVisitor#visitMap(de
	 * .uni_koblenz.jgralab.greql2.jvalue.JValueMap)
	 */
	@Override
	public void visitMap(JValueMap m) {
		try {
			writer.writeStartElement(MAP);
			writeBrowsingInfo(m);
			for (Entry<JValue, JValue> e : m.entrySet()) {
				writer.writeStartElement(MAP_ENTRY);
				e.getKey().accept(this);
				e.getValue().accept(this);
				writer.writeEndElement();
			}
			writer.writeEndElement();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValueDefaultVisitor#visitRecord
	 * (de.uni_koblenz.jgralab.greql2.jvalue.JValueRecord)
	 */
	@Override
	public void visitRecord(JValueRecord r) {
		try {
			writer.writeStartElement(RECORD);
			writeBrowsingInfo(r);

			for (String component : r.keySet()) {
				writer.writeStartElement(RECORD_COMPONENT);
				writer.writeAttribute(ATTR_NAME, component);
				r.get(component).accept(this);
				writer.writeEndElement();
			}

			writer.writeEndElement();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValueDefaultVisitor#visitSet(de
	 * .uni_koblenz.jgralab.greql2.jvalue.JValueSet)
	 */
	@Override
	public void visitSet(JValueSet s) {
		try {
			writer.writeStartElement(SET);
			writeBrowsingInfo(s);
			super.visitSet(s);
			writer.writeEndElement();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValueDefaultVisitor#visitString
	 * (de.uni_koblenz.jgralab.greql2.jvalue.JValue)
	 */
	@Override
	public void visitString(JValue s) {
		try {
			writer.writeEmptyElement(STRING);
			writer.writeAttribute(ATTR_VALUE, s.toString());
			writeBrowsingInfo(s);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValueDefaultVisitor#visitTuple(
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValueTuple)
	 */
	@Override
	public void visitTuple(JValueTuple t) {
		try {
			writer.writeStartElement(TUPLE);
			writeBrowsingInfo(t);
			super.visitTuple(t);
			writer.writeEndElement();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValueDefaultVisitor#visitVertex
	 * (de.uni_koblenz.jgralab.greql2.jvalue.JValue)
	 */
	@Override
	public void visitVertex(JValue v) {
		Vertex vertex = v.toVertex();
		try {
			writer.writeEmptyElement(VERTEX);
			writer.writeAttribute(ATTR_ID, String.valueOf(vertex.getId()));
			if (vertex.getGraph() != graph) {
				writer.writeAttribute(ATTR_GRAPH_ID,
						String.valueOf(vertex.getGraph().getId()));
			}
			if (vertex != v.getBrowsingInfo()) {
				writeBrowsingInfo(v);
			}
		} catch (XMLStreamException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void visitTable(JValueTable t) {
		try {
			writer.writeStartElement(TABLE);
			writeBrowsingInfo(t);
			super.visitTable(t);
			writer.writeEndElement();
		} catch (XMLStreamException ex) {
			ex.printStackTrace();
		}
	}
}
