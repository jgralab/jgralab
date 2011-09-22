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
import de.uni_koblenz.jgralab.greql2.exception.SerialisingException;
import de.uni_koblenz.jgralab.greql2.types.Path;
import de.uni_koblenz.jgralab.greql2.types.Record;
import de.uni_koblenz.jgralab.greql2.types.Table;
import de.uni_koblenz.jgralab.greql2.types.Tuple;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;



public class XMLOutputWriter extends DefaultWriter implements XMLConstants {

	private IndentingXMLStreamWriter writer = null;
	private Graph graph;

	public XMLOutputWriter(Object val, String fileName) {
		this(val, fileName, null);
	}

	public XMLOutputWriter(Object val, String fileName, Graph g) {
		graph = g;
		try {
			writer = new IndentingXMLStreamWriter(XMLOutputFactory
					.newInstance().createXMLStreamWriter(
							new BufferedOutputStream(new FileOutputStream(
									fileName))));
		} catch (FileNotFoundException e) {
			throw new SerialisingException("Can't create XML output", null, e);
		} catch (XMLStreamException e) {
			throw new SerialisingException("Can't create XML output", null, e);
		} catch (FactoryConfigurationError e) {
			throw new SerialisingException("Can't create XML output", null, e);
		}

		head();
		this.write(val);
		foot();
	}

	@Override
	public void head() {
		try {
			writer.writeStartDocument("UTF-8", "1.0");
			// writer.writeDTD("<!DOCTYPE jvalue [\n"
			// +
			// "<!ENTITY % value \"integer|long|double|string|boolean|list|set|tuple\">\n"
			// + "<!ENTITY % bi \"(browsingInfo?)\">\n"
			// + "<!ELEMENT jvalue (value)>\n" + "<!ATTLIST jvalue\n"
			// + "graphId CDATA #IMPLIED\n" + ">\n"
			// + "<!ELEMENT integer %bi;>\n" + "<!ATTLIST integer\n"
			// + "value PCDATA #REQUIRED\n" + ">\n" + "]>");

			writer.writeStartElement(OBJECT);
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
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (XMLStreamException ex) {
				throw new RuntimeException(
						"An exception occured while closing the stream", ex);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.jvalue.JValueDefaultVisitor#
	 * visitAttributedElementClass(de.uni_koblenz.jgralab.greql2.jvalue.JValue)
	 */
	@Override
	public void writeAttributedElementClass(AttributedElementClass aec) {
		try {
			writer.writeEmptyElement(ATTRIBUTEDELEMENTCLASS);
			writer.writeAttribute(ATTR_NAME, aec.getQualifiedName());
			writer.writeAttribute(ATTR_SCHEMA, aec.getSchema()
					.getQualifiedName());
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
	public void writeBoolean(Boolean b) {
		try {
			writer.writeEmptyElement(BOOLEAN);
			writer.writeAttribute(ATTR_VALUE, b.toString());
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
	public void writeDouble(Double n) {
		try {
			writer.writeEmptyElement(DOUBLE);
			writer.writeAttribute(ATTR_VALUE, n.toString());
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
	public void writeEdge(Edge edge) {
		try {
			writer.writeEmptyElement(EDGE);
			writer.writeAttribute(ATTR_ID, Integer.toString(edge.getId()));
			if (edge.getGraph() != graph) {
				writer.writeAttribute(ATTR_GRAPH_ID,
						String.valueOf(edge.getGraph().getId()));
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
	public void writeEnum(Enum<?> val) {
		try {
			writer.writeEmptyElement(ENUM);
			writer.writeAttribute(ATTR_VALUE, val.name());
			writer.writeAttribute(ATTR_TYPE, val.getDeclaringClass()
					.getCanonicalName());
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
	public void writeGraph(Graph graph) {
		try {
			writer.writeEmptyElement(GRAPH);
			writer.writeAttribute(ATTR_GRAPH_ID, graph.getId());
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
	public void writeInteger(Integer n) {
		try {
			writer.writeEmptyElement(INTEGER);
			writer.writeAttribute(ATTR_VALUE, n.toString());
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
	public void writePVector(PVector<?> l) {
		try {
			writer.writeStartElement(LIST);
			super.writePVector(l);
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
	public void writeLong(Long l) {
		try {
			writer.writeEmptyElement(LONG);
			writer.writeAttribute(ATTR_VALUE, l.toString());
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
	public void writePMap(PMap<?,?> m) {
		try {
			writer.writeStartElement(MAP);
			for (Entry<?, ?> e : m.entrySet()) {
				writer.writeStartElement(MAP_ENTRY);
				this.write(e.getKey());
				this.write(e.getValue());
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
	public void writeRecord(Record r) {
		try {
			writer.writeStartElement(RECORD);

			for (String component : r.getComponentNames()) {
				writer.writeStartElement(RECORD_COMPONENT);
				writer.writeAttribute(ATTR_NAME, component);
				this.write(r.getComponent(component));
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
	public void writePSet(PSet<?> s) {
		try {
			writer.writeStartElement(SET);
			super.writePSet(s);
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
	public void writeString(String s) {
		try {
			writer.writeEmptyElement(STRING);
			writer.writeAttribute(ATTR_VALUE, s.toString());
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.serialising.DefaultWriter#writeTuple(
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValueTuple)
	 */
	@Override
	public void writeTuple(Tuple t) {
		try {
			writer.writeStartElement(TUPLE);
			super.writeTuple(t);
			writer.writeEndElement();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.serialising.DefaultWriter#writeVertex
	 */
	@Override
	public void writeVertex(Vertex vertex) {
		try {
			writer.writeEmptyElement(VERTEX);
			writer.writeAttribute(ATTR_ID, String.valueOf(vertex.getId()));
			if (vertex.getGraph() != graph) {
				writer.writeAttribute(ATTR_GRAPH_ID,
						String.valueOf(vertex.getGraph().getId()));
			}
		} catch (XMLStreamException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void writeTable(Table<?> t) {
		try {
			writer.writeStartElement(TABLE);
			super.writeTable(t);
			writer.writeEndElement();
		} catch (XMLStreamException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void writePath(Path p){
		try{
			writer.writeStartElement(PATH);
			this.write(p.getVertexTrace());
			this.write(p.getEdgeTrace());
			writer.writeEndElement();
		}catch(XMLStreamException ex){
			ex.printStackTrace();
		}
	}
	
}
