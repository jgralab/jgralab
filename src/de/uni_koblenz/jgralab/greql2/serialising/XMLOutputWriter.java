/*
* JGraLab - The Java Graph Laboratory
*
* Copyright (C) 2006-2012 Institute for Software Technology
*                         University of Koblenz-Landau, Germany
*                         ist@uni-koblenz.de
*
* For bug reports, documentation and further information, visit
*
*                         https://github.com/jgralab/jgralab
*
* This program is free software; you can redistribute it and/or modify it
* under the terms of the GNU General Public License as published by the
* Free Software Foundation; either version 3 of the License, or (at your
* option) any later version.
*
* This program is distributed in the hope that it will be useful, but
* WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
* Public License for more details.
*
* You should have received a copy of the GNU General Public License along
* with this program; if not, see <http://www.gnu.org/licenses>.
*
* Additional permission under GNU GPL version 3 section 7
*
* If you modify this Program, or any covered work, by linking or combining
* it with Eclipse (or a modified version of that program or an Eclipse
* plugin), containing parts covered by the terms of the Eclipse Public
* License (EPL), the licensors of this Program grant you additional
* permission to convey the resulting work.  Corresponding Source for a
* non-source form of such a combination shall include the source code for
* the parts of JGraLab used as well as that of the covered work.
*/
package de.uni_koblenz.jgralab.greql2.serialising;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Map.Entry;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.pcollections.PMap;
import org.pcollections.PSet;
import org.pcollections.PVector;

import de.uni_koblenz.ist.utilities.xml.IndentingXMLStreamWriter;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Record;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.exception.SerialisingException;
import de.uni_koblenz.jgralab.greql2.types.Path;
import de.uni_koblenz.jgralab.greql2.types.Table;
import de.uni_koblenz.jgralab.greql2.types.Tuple;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class XMLOutputWriter extends DefaultWriter implements XMLConstants {

	private IndentingXMLStreamWriter writer = null;

	public XMLOutputWriter() {
		this(null);
	}

	public XMLOutputWriter(Graph g) {
		super(g);
	}

	public void writeValue(Object value, File file) throws XMLStreamException {
		try {
			writer = new IndentingXMLStreamWriter(
					XMLOutputFactory.newInstance()
							.createXMLStreamWriter(
									new BufferedOutputStream(
											new FileOutputStream(file)),
									"UTF-8"), "\t");
			writeValue(value);
		} catch (FactoryConfigurationError e) {
			throw e;
		} catch (FileNotFoundException e) {
			throw new XMLStreamException(e);
		} catch (SerialisingException e) {
			throw e;
		} catch (Exception e) {
			throw new SerialisingException("Unhandled Exception", rootValue, e);
		} finally {
			try {
				writer.close();
			} catch (XMLStreamException ex) {
				throw new RuntimeException(
						"An exception occured while closing the stream", ex);
			}
		}
	}

	@Override
	protected void write(Object o) throws XMLStreamException {
		try {
			super.write(o);
		} catch (XMLStreamException e) {
			throw e;
		} catch (SerialisingException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Unexpected Exception", e);
		}
	}

	@Override
	protected void head() throws XMLStreamException {
		writer.writeStartDocument("UTF-8", "1.0");
		writer.writeStartElement(OBJECT);
		if (getGraph() != null) {
			writer.writeAttribute(ATTR_GRAPH_ID, getGraph().getId());
		}
	}

	@Override
	protected void foot() throws XMLStreamException {
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.writeCharacters("\n");
		writer.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.jvalue.JValueDefaultVisitor#
	 * visitAttributedElementClass(de.uni_koblenz.jgralab.greql2.jvalue.JValue)
	 */
	@Override
	protected void writeAttributedElementClass(AttributedElementClass<?, ?> aec)
			throws XMLStreamException {
		writer.writeEmptyElement(ATTRIBUTEDELEMENTCLASS);
		writer.writeAttribute(ATTR_NAME, aec.getQualifiedName());
		writer.writeAttribute(ATTR_SCHEMA, aec.getSchema().getQualifiedName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValueDefaultVisitor#visitBoolean
	 * (de.uni_koblenz.jgralab.greql2.jvalue.JValue)
	 */
	@Override
	protected void writeBoolean(Boolean b) throws XMLStreamException {
		writer.writeEmptyElement(BOOLEAN);
		writer.writeAttribute(ATTR_VALUE, b.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValueDefaultVisitor#visitDouble
	 * (de.uni_koblenz.jgralab.greql2.jvalue.JValue)
	 */
	@Override
	protected void writeDouble(Double n) throws XMLStreamException {
		writer.writeEmptyElement(DOUBLE);
		writer.writeAttribute(ATTR_VALUE, n.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValueDefaultVisitor#visitEdge(de
	 * .uni_koblenz.jgralab.greql2.jvalue.JValue)
	 */
	@Override
	protected void writeEdge(Edge edge) throws XMLStreamException {
		writer.writeEmptyElement(EDGE);
		writer.writeAttribute(ATTR_ID, Integer.toString(edge.getId()));
		if (edge.getGraph() != getGraph()) {
			writer.writeAttribute(ATTR_GRAPH_ID,
					String.valueOf(edge.getGraph().getId()));
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
	protected void writeEnum(Enum<?> val) throws XMLStreamException {
		writer.writeEmptyElement(ENUM);
		writer.writeAttribute(ATTR_VALUE, val.name());
		writer.writeAttribute(ATTR_TYPE, val.getDeclaringClass()
				.getCanonicalName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValueDefaultVisitor#visitGraph(
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValue)
	 */
	@Override
	protected void writeGraph(Graph graph) throws XMLStreamException {
		writer.writeEmptyElement(GRAPH);
		writer.writeAttribute(ATTR_GRAPH_ID, graph.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValueDefaultVisitor#visitInt(de
	 * .uni_koblenz.jgralab.greql2.jvalue.JValue)
	 */
	@Override
	protected void writeInteger(Integer n) throws XMLStreamException {
		writer.writeEmptyElement(INTEGER);
		writer.writeAttribute(ATTR_VALUE, n.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValueDefaultVisitor#visitList(de
	 * .uni_koblenz.jgralab.greql2.jvalue.JValueList)
	 */
	@Override
	protected void writePVector(PVector<?> l) throws XMLStreamException {
		writer.writeStartElement(LIST);
		for (Object o : l) {
			write(o);
		}
		writer.writeEndElement();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValueDefaultVisitor#visitLong(de
	 * .uni_koblenz.jgralab.greql2.jvalue.JValue)
	 */
	@Override
	protected void writeLong(Long l) throws XMLStreamException {
		writer.writeEmptyElement(LONG);
		writer.writeAttribute(ATTR_VALUE, l.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValueDefaultVisitor#visitMap(de
	 * .uni_koblenz.jgralab.greql2.jvalue.JValueMap)
	 */
	@Override
	protected void writePMap(PMap<?, ?> m) throws XMLStreamException {
		writer.writeStartElement(MAP);
		for (Entry<?, ?> e : m.entrySet()) {
			writer.writeStartElement(MAP_ENTRY);
			write(e.getKey());
			write(e.getValue());
			writer.writeEndElement();
		}
		writer.writeEndElement();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValueDefaultVisitor#visitRecord
	 * (de.uni_koblenz.jgralab.greql2.jvalue.JValueRecord)
	 */
	@Override
	protected void writeRecord(Record r) throws XMLStreamException {
		writer.writeStartElement(RECORD);

		for (String component : r.getComponentNames()) {
			writer.writeStartElement(RECORD_COMPONENT);
			writer.writeAttribute(ATTR_NAME, component);
			write(r.getComponent(component));
			writer.writeEndElement();
		}

		writer.writeEndElement();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValueDefaultVisitor#visitSet(de
	 * .uni_koblenz.jgralab.greql2.jvalue.JValueSet)
	 */
	@Override
	protected void writePSet(PSet<?> s) throws XMLStreamException {
		writer.writeStartElement(SET);
		try {
			super.writePSet(s);
		} catch (XMLStreamException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Unexpected Exception", e);
		}
		writer.writeEndElement();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValueDefaultVisitor#visitString
	 * (de.uni_koblenz.jgralab.greql2.jvalue.JValue)
	 */
	@Override
	protected void writeString(String s) throws XMLStreamException {
		writer.writeEmptyElement(STRING);
		writer.writeAttribute(ATTR_VALUE, s.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.serialising.DefaultWriter#writeTuple(
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValueTuple)
	 */
	@Override
	protected void writeTuple(Tuple t) throws XMLStreamException {
		writer.writeStartElement(TUPLE);
		try {
			super.writeTuple(t);
		} catch (XMLStreamException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Unexpected Exception", e);
		}
		writer.writeEndElement();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.serialising.DefaultWriter#writeVertex
	 */
	@Override
	protected void writeVertex(Vertex vertex) throws XMLStreamException {
		writer.writeEmptyElement(VERTEX);
		writer.writeAttribute(ATTR_ID, String.valueOf(vertex.getId()));
		if (vertex.getGraph() != getGraph()) {
			writer.writeAttribute(ATTR_GRAPH_ID,
					String.valueOf(vertex.getGraph().getId()));
		}
	}

	@Override
	protected void writeTable(Table<?> t) throws XMLStreamException {
		writer.writeStartElement(TABLE);
		try {
			super.writeTable(t);
		} catch (XMLStreamException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Unexpected Exception", e);
		}
		writer.writeEndElement();
	}

	@Override
	protected void writePath(Path p) throws XMLStreamException {
		writer.writeStartElement(PATH);
		write(p.getVertexTrace());
		write(p.getEdgeTrace());
		writer.writeEndElement();
	}

	@Override
	protected void writeUndefined() throws Exception {
		writer.writeEmptyElement(UNDEFINED);
	}
}
