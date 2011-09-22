package de.uni_koblenz.jgralab.greql2.serialising;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.stream.XMLStreamException;

import org.pcollections.ArrayPMap;
import org.pcollections.ArrayPSet;
import org.pcollections.PCollection;
import org.pcollections.PMap;
import org.pcollections.PVector;
import org.pcollections.ArrayPVector;

import de.uni_koblenz.ist.utilities.xml.XmlProcessor;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.exception.JValueLoadException;
import de.uni_koblenz.jgralab.greql2.types.Record;
import de.uni_koblenz.jgralab.greql2.types.Table;
import de.uni_koblenz.jgralab.greql2.types.Tuple;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Schema;

public class XMLLoader extends XmlProcessor implements XMLConstants {
	
	private Graph defaultGraph;

	/**
	 * Synthetic class to ease XML parsing
	 */
	private static class RecordComponent  {
		String componentName;
		Object jvalue;

		RecordComponent(String compName) {
			componentName = compName;
		}
	}

	/**
	 * Synthetic class to ease XML parsing
	 */
	private static class MapEntry  {
		Object key = null;
		Object value = null;
	}

	private Map<String, Graph> id2GraphMap = null;
	private Map<String, Schema> schemaName2Schema = null;
	private Stack<Object> stack = new Stack<Object>();

	public XMLLoader(Graph... graphs) {
		id2GraphMap = new HashMap<String, Graph>(graphs.length);
		for (Graph g : graphs) {
			id2GraphMap.put(g.getId(), g);
		}

		schemaName2Schema = new HashMap<String, Schema>(graphs.length);
		for (Graph g : graphs) {
			Schema s = g.getSchema();
			schemaName2Schema.put(s.getQualifiedName(), s);
		}
	}

	public Object load(String fileName) throws FileNotFoundException,
			XMLStreamException {
		process(fileName);
		if (stack.size() != 1) {
			throw new JValueLoadException(
					"Something went wrong.  stack.size() = " + stack.size()
							+ " != 1.  This must not happen!", null);
		}
		return stack.firstElement();
	}

	@Override
	protected void endDocument() throws XMLStreamException {

	}

	@Override
	protected void endElement(String name, StringBuilder content)
			throws XMLStreamException {
		if (name.equals(OBJECT)) {
			return;
		}

		Object endedElement = stack.pop();

		if (stack.isEmpty()) {
			// This was the top level element, so add it back and return.
			stack.push(endedElement);
			return;
		}

		// Ok, there was a parent, so the current element has to be added to
		// that (the parent has to be some kind of collection or map)
		Object parentElement = stack.peek();

		if (parentElement instanceof PMap) {
			// Parent is a Map, so the current element has to be a mapEntry
			MapEntry jme = (MapEntry) endedElement;
			parentElement = ((PMap<Object,Object>)parentElement).plus(jme.key, jme.value);
		} else if (parentElement instanceof MapEntry) {
			// Parent is a map entry, so the current elem is a key or a value of
			// the entry.
			MapEntry jme = (MapEntry) parentElement;
			if (jme.key == null) {
				jme.key = endedElement;
			} else if (jme.value == null) {
				jme.value = endedElement;
			} else {
				throw new JValueLoadException(
						"Encountered MapEntry with more than 2 elements!", null);
			}
		} else if (parentElement instanceof RecordComponent) {
			// Parent is a record component, so this has to be its value.
			RecordComponent rc = (RecordComponent) parentElement;
			rc.jvalue = endedElement;
		} else if (parentElement instanceof PCollection) {
			// ok, parent is a collection, so we can simply add with the
			// exception of records and tables
			PCollection<Object> coll = (PCollection<Object>)parentElement;
			if (coll instanceof Record) {
				Record rec = (Record)coll;
				RecordComponent comp = (RecordComponent) endedElement;
				rec = rec.plus(comp.componentName, comp.jvalue);
			} else if (coll instanceof Table) {
				Table<?> tab = (Table<?>)coll;
				if (tab.getTitles() == null) {
					tab = tab.withTitles((PVector<String>) endedElement);
				} else if (tab.toPVector() == null) {
					tab = tab.plusAll((PCollection)endedElement);
				} else {
					throw new JValueLoadException(
							"Table containing more children than header and data!",
							null);
				}
			} else {
				coll = coll.plus(endedElement);
			}
		} else {
			throw new JValueLoadException("The element '" + endedElement
					+ "' couldn't be added to its parent.", null);
		}
	}

	@Override
	protected void startDocument() throws XMLStreamException {
		stack.clear();
	}

	@Override
	protected void startElement(String elem) throws XMLStreamException {
		Object val = null;
		if (elem.equals(OBJECT)) {
			String gid = getAttribute(ATTR_GRAPH_ID);
			if (gid != null) {
				defaultGraph = id2GraphMap.get(gid);
				if (defaultGraph == null) {
					throw new JValueLoadException("There's no graph with id '"
							+ gid + "'.", null);
				}
			}
			return;
		} else if (elem.equals(ATTRIBUTEDELEMENTCLASS)) {
			String qName = getAttribute(ATTR_NAME);
			String schemaName = getAttribute(ATTR_SCHEMA);
			Schema schema = schemaName2Schema.get(schemaName);
			if (schema == null) {
				throw new JValueLoadException("Couldn't retrieve Schema '"
						+ schemaName + "'", null);
			}
			AttributedElementClass aec = schema
					.getAttributedElementClass(qName);
			if (aec == null) {
				throw new JValueLoadException(
						"Couldn't retrieve attributed element '" + qName
								+ "' from schema '" + schemaName + "'.", null);
			}
			val = aec;
			// ---------------------------------------------------------------
		}else if (elem.equals(BOOLEAN)) {
			val = Boolean.valueOf(getAttribute(ATTR_VALUE));
			// ---------------------------------------------------------------
		} else if (elem.equals(DOUBLE)) {
			val = Double.valueOf(getAttribute(ATTR_VALUE));
			// ---------------------------------------------------------------
		} else if (elem.equals(EDGE)) {
			int id = Integer.valueOf(getAttribute(ATTR_ID));
			Graph g = defaultGraph;
			String gid = getAttribute(ATTR_GRAPH_ID);
			if (gid != null) {
				g = id2GraphMap.get(gid);
				if (g == null) {
					throw new JValueLoadException("There's no graph with id '"
							+ gid + "'.", null);
				}
			}
			Edge e = g.getEdge(id);
			if (e == null) {
				throw new JValueLoadException("There's no edge with id '" + id
						+ "' in graph '" + g.getId() + "'.", null);
			}
			val = e;
			// ---------------------------------------------------------------
		} else if (elem.equals(ENUM)) {
			String litName = getAttribute(ATTR_VALUE);
			String enumTypeName = getAttribute(ATTR_TYPE);
			val = createEnum(litName, enumTypeName);
			// ---------------------------------------------------------------
		} else if (elem.equals(GRAPH)) {
			String gid = getAttribute(ATTR_GRAPH_ID);
			Graph g = id2GraphMap.get(gid);
			if (g == null) {
				throw new JValueLoadException("There's no graph with id '"
						+ gid + "'.", null);
			}
			val = g;
			// ---------------------------------------------------------------
		} else if (elem.equals(INTEGER)) {
			val = Integer.valueOf(getAttribute(ATTR_VALUE));
			// ---------------------------------------------------------------
		} else if (elem.equals(LIST)) {
			val = ArrayPVector.empty();
			// ---------------------------------------------------------------
		} else if (elem.equals(LONG)) {
			val = Long.valueOf(getAttribute(ATTR_VALUE));
			// ---------------------------------------------------------------
		} else if (elem.equals(MAP)) {
			val = ArrayPMap.empty();
			// ---------------------------------------------------------------
		} else if (elem.equals(MAP_ENTRY)) {
			val = new MapEntry();
			// ---------------------------------------------------------------
		} else if (elem.equals(RECORD)) {
			val = Record.empty();
			// ---------------------------------------------------------------
		} else if (elem.equals(RECORD_COMPONENT)) {
			val = new RecordComponent(getAttribute(ATTR_NAME));
			// ---------------------------------------------------------------
		} else if (elem.equals(SET)) {
			val = ArrayPSet.empty();
			// ---------------------------------------------------------------
		} else if (elem.equals(STRING)) {
			val = getAttribute(ATTR_VALUE);
			// ---------------------------------------------------------------
		} else if (elem.equals(TABLE)) {
			Table<?> tab = Table.empty();
			// header is empty by default, but we rely that it's not set when
			// assigning children to parent jvalues is endElement().
			tab = tab.withTitles(null);
			//TODO find other method to check null on data
			//tab = tab.
			val = tab;
			// ---------------------------------------------------------------
		} else if (elem.equals(TUPLE)) {
			val = Tuple.empty();
			// ---------------------------------------------------------------
		} else if (elem.equals(VERTEX)) {
			int id = Integer.valueOf(getAttribute(ATTR_ID));
			Graph g = defaultGraph;
			String gid = getAttribute(ATTR_GRAPH_ID);
			if (gid != null) {
				g = id2GraphMap.get(gid);
				if (g == null) {
					throw new JValueLoadException("There's no graph with id '"
							+ gid + "'.", null);
				}
			}
			Vertex v = g.getVertex(id);
			if (v == null) {
				throw new JValueLoadException("There's no vertex with id '"
						+ id + "' in graph '" + g.getId() + "'.", null);
			}
			val = v;
			// ---------------------------------------------------------------
		} else {
			throw new JValueLoadException("Unrecognized XML element '" + elem
					+ "'.", null);
		}

		// -------------------------------------------------------------------
		if (val == null) {
			throw new JValueLoadException(
					"Couldn't read the value of element '" + elem + "'.", null);
		}

		stack.push(val);
	}

	@SuppressWarnings("unchecked")
	private Object createEnum(String litName, String enumTypeName) {
		Object val = null;
		try {
			@SuppressWarnings("rawtypes")
			Class e = Class.forName(enumTypeName);
			val = Enum.valueOf(e, litName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new JValueLoadException("The Enum class '" + enumTypeName
					+ "' could not be loaded.", e);
		}
		return val;
	}
}
