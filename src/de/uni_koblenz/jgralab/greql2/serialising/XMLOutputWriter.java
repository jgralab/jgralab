package de.uni_koblenz.jgralab.greql2.serialising;

import java.io.BufferedOutputStream;
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
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.types.Path;
import de.uni_koblenz.jgralab.greql2.types.Record;
import de.uni_koblenz.jgralab.greql2.types.Table;
import de.uni_koblenz.jgralab.greql2.types.Tuple;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class XMLOutputWriter extends DefaultWriter implements XMLConstants {

	private IndentingXMLStreamWriter writer = null;
	private Graph graph;

	public XMLOutputWriter(Object val, String fileName)
			throws XMLStreamException {
		this(val, fileName, null);
	}

	@Override
	public void write(Object o) throws XMLStreamException {
		try {
			super.write(o);
		} catch (XMLStreamException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Unexpected Exception", e);
		}
	}

	public XMLOutputWriter(Object val, String fileName, Graph g)
			throws XMLStreamException {
		graph = g;
		try {
			writer = new IndentingXMLStreamWriter(XMLOutputFactory
					.newInstance().createXMLStreamWriter(
							new BufferedOutputStream(new FileOutputStream(
									fileName)), "UTF-8"), "\t");
			head();
			write(val);
			foot();
		} catch (FactoryConfigurationError e) {
			throw e;
		} catch (FileNotFoundException e) {
			throw new XMLStreamException(e);
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
	public void head() throws XMLStreamException {
		writer.writeStartDocument("UTF-8", "1.0");
		writer.writeStartElement(OBJECT);
		if (graph != null) {
			writer.writeAttribute(ATTR_GRAPH_ID, graph.getId());
		}
	}

	@Override
	public void foot() throws XMLStreamException {
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
	public void writeAttributedElementClass(AttributedElementClass aec)
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
	public void writeBoolean(Boolean b) throws XMLStreamException {
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
	public void writeDouble(Double n) throws XMLStreamException {
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
	public void writeEdge(Edge edge) throws XMLStreamException {
		writer.writeEmptyElement(EDGE);
		writer.writeAttribute(ATTR_ID, Integer.toString(edge.getId()));
		if (edge.getGraph() != graph) {
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
	public void writeEnum(Enum<?> val) throws XMLStreamException {
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
	public void writeGraph(Graph graph) throws XMLStreamException {
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
	public void writeInteger(Integer n) throws XMLStreamException {
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
	public void writePVector(PVector<?> l) throws XMLStreamException {
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
	public void writeLong(Long l) throws XMLStreamException {
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
	public void writePMap(PMap<?, ?> m) throws XMLStreamException {
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
	public void writeRecord(Record r) throws XMLStreamException {
		writer.writeStartElement(RECORD);

		for (String component : r.getComponentNames()) {
			writer.writeStartElement(RECORD_COMPONENT);
			writer.writeAttribute(ATTR_NAME, component);
			this.write(r.getComponent(component));
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
	public void writePSet(PSet<?> s) throws XMLStreamException {
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
	public void writeString(String s) throws XMLStreamException {
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
	public void writeTuple(Tuple t) throws XMLStreamException {
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
	public void writeVertex(Vertex vertex) throws XMLStreamException {
		writer.writeEmptyElement(VERTEX);
		writer.writeAttribute(ATTR_ID, String.valueOf(vertex.getId()));
		if (vertex.getGraph() != graph) {
			writer.writeAttribute(ATTR_GRAPH_ID,
					String.valueOf(vertex.getGraph().getId()));
		}
	}

	@Override
	public void writeTable(Table<?> t) throws XMLStreamException {
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
	public void writePath(Path p) throws XMLStreamException {
		writer.writeStartElement(PATH);
		this.write(p.getVertexTrace());
		this.write(p.getEdgeTrace());
		writer.writeEndElement();
	}

	@Override
	public void writeUndefined() throws Exception {
		writer.writeEmptyElement(UNDEFINED);
	}
}
