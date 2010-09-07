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
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class JValueXMLOutputVisitor extends JValueDefaultVisitor {

	private IndentingXMLStreamWriter writer = null;

	public JValueXMLOutputVisitor(JValue val, String fileName)
			throws FileNotFoundException, XMLStreamException,
			FactoryConfigurationError {
		writer = new IndentingXMLStreamWriter(XMLOutputFactory.newInstance()
				.createXMLStreamWriter(new FileOutputStream(fileName), "UTF-8"));

		head();
		val.accept(this);
		foot();
	}

	@Override
	public void head() {
		try {
			writer.writeStartDocument("UTF-8", "1.0");
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void foot() {
		try {
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
			writer.writeStartElement(JValueXMLConstants.ATTRIBUTEDELEMENTCLASS);
			writer.writeAttribute(JValueXMLConstants.ATTR_NAME, aec
					.getQualifiedName());
			writer.writeAttribute(JValueXMLConstants.ATTR_SCHEMA, aec
					.getSchema().getQualifiedName());
			writeBrowsingInfo(a.getBrowsingInfo());
			writer.writeEndElement();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	private void writeBrowsingInfo(AttributedElement bi)
			throws XMLStreamException {
		if (bi == null) {
			return;
		}
		writer.writeStartElement(JValueXMLConstants.BROWSINGINFO);
		if (bi instanceof GraphElement) {
			GraphElement ge = (GraphElement) bi;
			writer
					.writeEmptyElement(ge instanceof Edge ? JValueXMLConstants.EDGE
							: JValueXMLConstants.VERTEX);
			writer.writeAttribute(JValueXMLConstants.ATTR_ID, String.valueOf(ge
					.getId()));
			writer.writeAttribute(JValueXMLConstants.ATTR_GRAPH_ID, String
					.valueOf(ge.getGraph().getId()));
		} else {
			writer.writeEmptyElement(JValueXMLConstants.GRAPH);
			writer.writeAttribute(JValueXMLConstants.ATTR_GRAPH_ID,
					((Graph) bi).getId());
		}

		writer.writeEndElement();
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
			writer.writeStartElement(JValueXMLConstants.BAG);
			writeBrowsingInfo(b.getBrowsingInfo());
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
			writer.writeStartElement(JValueXMLConstants.BOOLEAN);
			writer.writeAttribute(JValueXMLConstants.ATTR_VALUE, b.toBoolean()
					.toString());
			writeBrowsingInfo(b.getBrowsingInfo());
			writer.writeEndElement();
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
			writer.writeStartElement(JValueXMLConstants.DOUBLE);
			writer.writeAttribute(JValueXMLConstants.ATTR_VALUE, n.toDouble()
					.toString());
			writeBrowsingInfo(n.getBrowsingInfo());
			writer.writeEndElement();
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
			writer.writeStartElement(JValueXMLConstants.EDGE);
			writer.writeAttribute(JValueXMLConstants.ATTR_ID, String
					.valueOf(edge.getId()));
			writer.writeAttribute(JValueXMLConstants.ATTR_GRAPH_ID, String
					.valueOf(edge.getGraph().getId()));
			writeBrowsingInfo(e.getBrowsingInfo());
			writer.writeEndElement();
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
			writer.writeStartElement(JValueXMLConstants.ENUMVALUE);
			writer.writeAttribute(JValueXMLConstants.ATTR_VALUE, val.name());
			writer.writeAttribute(JValueXMLConstants.ATTR_TYPE, val
					.getDeclaringClass().getCanonicalName());
			writeBrowsingInfo(e.getBrowsingInfo());
			writer.writeEndElement();
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
			writer.writeStartElement(JValueXMLConstants.GRAPH);
			writer.writeAttribute(JValueXMLConstants.ATTR_GRAPH_ID, graph
					.getId());
			writeBrowsingInfo(g.getBrowsingInfo());
			writer.writeEndElement();
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
			writer.writeStartElement(JValueXMLConstants.INTEGER);
			writer.writeAttribute(JValueXMLConstants.ATTR_VALUE, n.toInteger()
					.toString());
			writeBrowsingInfo(n.getBrowsingInfo());
			writer.writeEndElement();
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
			writer.writeStartElement(JValueXMLConstants.LIST);
			writeBrowsingInfo(l.getBrowsingInfo());
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
			writer.writeStartElement(JValueXMLConstants.LONG);
			writer.writeAttribute(JValueXMLConstants.ATTR_VALUE, l.toLong()
					.toString());
			writeBrowsingInfo(l.getBrowsingInfo());
			writer.writeEndElement();
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
			writer.writeStartElement(JValueXMLConstants.MAP);
			writeBrowsingInfo(m.getBrowsingInfo());

			for (Entry<JValue, JValue> e : m.entrySet()) {
				writer.writeStartElement(JValueXMLConstants.MAP_ENTRY);
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
			writer.writeStartElement(JValueXMLConstants.RECORD);
			writeBrowsingInfo(r.getBrowsingInfo());

			for (String component : r.keySet()) {
				writer.writeStartElement(JValueXMLConstants.RECORD_COMPONENT);
				writer.writeAttribute(JValueXMLConstants.ATTR_NAME, component);
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
			writer.writeStartElement(JValueXMLConstants.SET);
			writeBrowsingInfo(s.getBrowsingInfo());
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
			writer.writeStartElement(JValueXMLConstants.STRING);
			writer.writeAttribute(JValueXMLConstants.ATTR_VALUE, s.toString());
			writeBrowsingInfo(s.getBrowsingInfo());
			writer.writeEndElement();
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
			writer.writeStartElement(JValueXMLConstants.TUPLE);
			writeBrowsingInfo(t.getBrowsingInfo());
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
			writer.writeStartElement(JValueXMLConstants.VERTEX);
			writer.writeAttribute(JValueXMLConstants.ATTR_ID, String
					.valueOf(vertex.getId()));
			writer.writeAttribute(JValueXMLConstants.ATTR_GRAPH_ID, String
					.valueOf(vertex.getGraph().getId()));
			writeBrowsingInfo(v.getBrowsingInfo());
			writer.writeEndElement();
		} catch (XMLStreamException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void visitTable(JValueTable t) {
		try {
			writer.writeStartElement(JValueXMLConstants.TABLE);
			writeBrowsingInfo(t.getBrowsingInfo());
			super.visitTable(t);
			writer.writeEndElement();
		} catch (XMLStreamException ex) {
			ex.printStackTrace();
		}
	}
}
