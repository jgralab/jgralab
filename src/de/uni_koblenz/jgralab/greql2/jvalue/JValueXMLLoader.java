/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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

package de.uni_koblenz.jgralab.greql2.jvalue;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.stream.XMLStreamException;

import de.uni_koblenz.ist.utilities.xml.XmlProcessor;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.exception.JValueLoadException;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Schema;

public class JValueXMLLoader extends XmlProcessor implements JValueXMLConstants {
	private Graph defaultGraph;

	/**
	 * Synthetic class to ease XML parsing
	 */
	private static class JValueRecordComponent extends JValueImpl {
		String componentName;
		JValueImpl jvalue;

		JValueRecordComponent(String compName) {
			componentName = compName;
		}
	}

	/**
	 * Synthetic class to ease XML parsing
	 */
	private static class JValueMapEntry extends JValueImpl {
		JValueImpl key = null;
		JValueImpl value = null;
	}

	private Map<String, Graph> id2GraphMap = null;
	private Map<String, Schema> schemaName2Schema = null;
	private Stack<JValueImpl> stack = new Stack<JValueImpl>();

	public JValueXMLLoader(Graph... graphs) {
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

	public JValue load(String fileName) throws FileNotFoundException,
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
		if (name.equals(JVALUE)) {
			return;
		}

		JValueImpl endedElement = stack.pop();

		if (stack.isEmpty()) {
			// This was the top level element, so add it back and return.
			stack.push(endedElement);
			return;
		}

		// Ok, there was a parent, so the current element has to be added to
		// that (the parent has to be some kind of collection or map)
		JValue parentElement = stack.peek();

		// TODO
		// // Each and every element may be have a browsing info, so check that
		// // first.
		// if (endedElement instanceof JValueBrowsingInfo) {
		// // We ended a browsing info, so add its info to the parent.
		// parentElement
		// .setBrowsingInfo(((JValueBrowsingInfo) endedElement).browsingInfo);
		// } else if (parentElement instanceof JValueBrowsingInfo) {
		// // We ended an element inside a browsingInfo, so this has to be an
		// // attributed element.
		// ((JValueBrowsingInfo) parentElement).browsingInfo = endedElement
		// .toAttributedElement();
		// } else

		if (parentElement.isMap()) {
			// Parent is a Map, so the current element has to be a mapEntry
			JValueMapEntry jme = (JValueMapEntry) endedElement;
			parentElement.toJValueMap().put(jme.key, jme.value);
		} else if (parentElement instanceof JValueMapEntry) {
			// Parent is a map entry, so the current elem is a key or a value of
			// the entry.
			JValueMapEntry jme = (JValueMapEntry) parentElement;
			if (jme.key == null) {
				jme.key = endedElement;
			} else if (jme.value == null) {
				jme.value = endedElement;
			} else {
				throw new JValueLoadException(
						"Encountered MapEntry with more than 2 elements!", null);
			}
		} else if (parentElement instanceof JValueRecordComponent) {
			// Parent is a record component, so this has to be its value.
			JValueRecordComponent rc = (JValueRecordComponent) parentElement;
			rc.jvalue = endedElement;
		} else if (parentElement.isCollection()) {
			// ok, parent is a collection, so we can simply add with the
			// exception of records and tables
			JValueCollection coll = parentElement.toCollection();
			if (coll.isJValueRecord()) {
				JValueRecord rec = coll.toJValueRecord();
				JValueRecordComponent comp = (JValueRecordComponent) endedElement;
				rec.add(comp.componentName, comp.jvalue);
			} else if (coll.isJValueTable()) {
				JValueTable tab = coll.toJValueTable();
				if (tab.getHeader() == null) {
					tab.setHeader(endedElement.toJValueTuple());
				} else if (tab.getData() == null) {
					tab.setData(endedElement.toCollection());
				} else {
					throw new JValueLoadException(
							"Table containing more children than header and data!",
							null);
				}
			} else {
				coll.add(endedElement);
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
		JValueImpl val = null;
		if (elem.equals(JVALUE)) {
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
			val = new JValueImpl(aec);
			// ---------------------------------------------------------------
		} else if (elem.equals(BAG)) {
			val = new JValueBag();
			// ---------------------------------------------------------------
		} else if (elem.equals(BOOLEAN)) {
			val = new JValueImpl(Boolean.valueOf(getAttribute(ATTR_VALUE)));
			// ---------------------------------------------------------------
		} else if (elem.equals(DOUBLE)) {
			val = new JValueImpl(Double.valueOf(getAttribute(ATTR_VALUE)));
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
			val = new JValueImpl(e);
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
			val = new JValueImpl(g);
			// ---------------------------------------------------------------
		} else if (elem.equals(INTEGER)) {
			val = new JValueImpl(Integer.valueOf(getAttribute(ATTR_VALUE)));
			// ---------------------------------------------------------------
		} else if (elem.equals(LIST)) {
			val = new JValueList();
			// ---------------------------------------------------------------
		} else if (elem.equals(LONG)) {
			val = new JValueImpl(Long.valueOf(getAttribute(ATTR_VALUE)));
			// ---------------------------------------------------------------
		} else if (elem.equals(MAP)) {
			val = new JValueMap();
			// ---------------------------------------------------------------
		} else if (elem.equals(MAP_ENTRY)) {
			val = new JValueMapEntry();
			// ---------------------------------------------------------------
		} else if (elem.equals(RECORD)) {
			val = new JValueRecord();
			// ---------------------------------------------------------------
		} else if (elem.equals(RECORD_COMPONENT)) {
			val = new JValueRecordComponent(getAttribute(ATTR_NAME));
			// ---------------------------------------------------------------
		} else if (elem.equals(SET)) {
			val = new JValueSet();
			// ---------------------------------------------------------------
		} else if (elem.equals(STRING)) {
			val = new JValueImpl(getAttribute(ATTR_VALUE));
			// ---------------------------------------------------------------
		} else if (elem.equals(TABLE)) {
			JValueTable tab = new JValueTable();
			// header is empty by default, but we rely that it's not set when
			// assigning children to parent jvalues is endElement().
			tab.setHeader(null);
			tab.setData(null);
			val = tab;
			// ---------------------------------------------------------------
		} else if (elem.equals(TUPLE)) {
			val = new JValueTuple();
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
			val = new JValueImpl(v);
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

		// process browsing info
		AttributedElement browsingInfo = null;
		String vl = getAttribute(ATTR_VERTEX_LINK);
		if (vl != null) {
			Graph g = defaultGraph;
			String gl = getAttribute(ATTR_GRAPH_LINK);
			if (gl != null) {
				g = id2GraphMap.get(gl);
			}
			if (g != null) {
				browsingInfo = g.getVertex(Integer.parseInt(vl));
			}
		} else {
			String el = getAttribute(ATTR_EDGE_LINK);
			String gl = getAttribute(ATTR_GRAPH_LINK);
			if (el != null) {
				Graph g = defaultGraph;
				if (gl != null) {
					g = id2GraphMap.get(gl);
				}
				if (g != null) {
					browsingInfo = g.getEdge(Integer.parseInt(el));
				}
			} else if (gl != null) {
				browsingInfo = id2GraphMap.get(gl);
			}
		}
		val.setBrowsingInfo(browsingInfo);

		stack.push(val);
	}

	@SuppressWarnings("unchecked")
	private JValueImpl createEnum(String litName, String enumTypeName) {
		JValueImpl val = null;
		try {
			@SuppressWarnings("rawtypes")
			Class e = Class.forName(enumTypeName);
			val = new JValueImpl(Enum.valueOf(e, litName));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new JValueLoadException("The Enum class '" + enumTypeName
					+ "' could not be loaded.", e);
		}
		return val;
	}
}
