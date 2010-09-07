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
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.stream.XMLStreamException;

import de.uni_koblenz.ist.utilities.xml.XmlProcessor;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.exception.JValueLoadException;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Schema;

public class JValueXMLLoader extends XmlProcessor {

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

	/**
	 * Synthetic class to ease XML parsing
	 */
	private static class JValueBrowsingInfo extends JValueImpl {
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
	protected void endElement(String arg0, StringBuilder arg1)
			throws XMLStreamException {
		JValueImpl endedElement = stack.pop();

		if (stack.isEmpty()) {
			// This was the top level element, so add it back and return.
			stack.push(endedElement);
			return;
		}

		// Ok, there was a parent, so the current element has to be added to
		// that (the parent has to be some kind of collection or map)
		JValue parentElement = stack.peek();

		// Each and every element may be have a browsing info, so check that
		// first.
		if (endedElement instanceof JValueBrowsingInfo) {
			// We ended a browsing info, so add its info to the parent.
			parentElement
					.setBrowsingInfo(((JValueBrowsingInfo) endedElement).browsingInfo);
		} else if (parentElement instanceof JValueBrowsingInfo) {
			// We ended an element inside a browsingInfo, so this has to be an
			// attributed element.
			((JValueBrowsingInfo) parentElement).browsingInfo = endedElement
					.toAttributedElement();
		} else if (parentElement.isMap()) {
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
		if (elem.equals(JValueXMLConstants.ATTRIBUTEDELEMENTCLASS)) {
			String qName = getAttribute(JValueXMLConstants.ATTR_NAME);
			String schemaName = getAttribute(JValueXMLConstants.ATTR_SCHEMA);
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
		} else if (elem.equals(JValueXMLConstants.BAG)) {
			val = new JValueBag();
			// ---------------------------------------------------------------
		} else if (elem.equals(JValueXMLConstants.BOOLEAN)) {
			val = new JValueImpl(
					Boolean.valueOf(getAttribute(JValueXMLConstants.ATTR_VALUE)));
			// ---------------------------------------------------------------
		} else if (elem.equals(JValueXMLConstants.DOUBLE)) {
			val = new JValueImpl(
					Double.valueOf(getAttribute(JValueXMLConstants.ATTR_VALUE)));
			// ---------------------------------------------------------------
		} else if (elem.equals(JValueXMLConstants.EDGE)) {
			int id = Integer.valueOf(getAttribute(JValueXMLConstants.ATTR_ID));
			String gid = getAttribute(JValueXMLConstants.ATTR_GRAPH_ID);
			Graph g = id2GraphMap.get(gid);
			if (g == null) {
				throw new JValueLoadException("There's no graph with id '"
						+ gid + "'.", null);
			}
			Edge e = g.getEdge(id);
			if (e == null) {
				throw new JValueLoadException("There's no edge with id '" + gid
						+ "' in graph '" + gid + "'.", null);
			}
			val = new JValueImpl(e);
			// ---------------------------------------------------------------
		} else if (elem.equals(JValueXMLConstants.ENUMVALUE)) {
			String litName = getAttribute(JValueXMLConstants.ATTR_VALUE);
			String enumTypeName = getAttribute(JValueXMLConstants.ATTR_TYPE);
			val = createEnum(litName, enumTypeName);
			// ---------------------------------------------------------------
		} else if (elem.equals(JValueXMLConstants.GRAPH)) {
			String gid = getAttribute(JValueXMLConstants.ATTR_GRAPH_ID);
			Graph g = id2GraphMap.get(gid);
			if (g == null) {
				throw new JValueLoadException("There's no graph with id '"
						+ gid + "'.", null);
			}
			val = new JValueImpl(g);
			// ---------------------------------------------------------------
		} else if (elem.equals(JValueXMLConstants.INTEGER)) {
			val = new JValueImpl(
					Integer.valueOf(getAttribute(JValueXMLConstants.ATTR_VALUE)));
			// ---------------------------------------------------------------
		} else if (elem.equals(JValueXMLConstants.LIST)) {
			val = new JValueList();
			// ---------------------------------------------------------------
		} else if (elem.equals(JValueXMLConstants.LONG)) {
			val = new JValueImpl(
					Long.valueOf(getAttribute(JValueXMLConstants.ATTR_VALUE)));
			// ---------------------------------------------------------------
		} else if (elem.equals(JValueXMLConstants.MAP)) {
			val = new JValueMap();
			// ---------------------------------------------------------------
		} else if (elem.equals(JValueXMLConstants.MAP_ENTRY)) {
			val = new JValueMapEntry();
			// ---------------------------------------------------------------
		} else if (elem.equals(JValueXMLConstants.RECORD)) {
			val = new JValueRecord();
			// ---------------------------------------------------------------
		} else if (elem.equals(JValueXMLConstants.RECORD_COMPONENT)) {
			val = new JValueRecordComponent(
					getAttribute(JValueXMLConstants.ATTR_NAME));
			// ---------------------------------------------------------------
		} else if (elem.equals(JValueXMLConstants.SET)) {
			val = new JValueSet();
			// ---------------------------------------------------------------
		} else if (elem.equals(JValueXMLConstants.STRING)) {
			val = new JValueImpl(getAttribute(JValueXMLConstants.ATTR_VALUE));
			// ---------------------------------------------------------------
		} else if (elem.equals(JValueXMLConstants.TABLE)) {
			JValueTable tab = new JValueTable();
			// header is empty by default, but we rely that it's not set when
			// assigning children to parent jvalues is endElement().
			tab.setHeader(null);
			tab.setData(null);
			val = tab;
			// ---------------------------------------------------------------
		} else if (elem.equals(JValueXMLConstants.TUPLE)) {
			val = new JValueTuple();
			// ---------------------------------------------------------------
		} else if (elem.equals(JValueXMLConstants.VERTEX)) {
			int id = Integer.valueOf(getAttribute(JValueXMLConstants.ATTR_ID));
			String gid = getAttribute(JValueXMLConstants.ATTR_GRAPH_ID);
			Graph g = id2GraphMap.get(gid);
			if (g == null) {
				throw new JValueLoadException("There's no graph with id '"
						+ gid + "'.", null);
			}
			Vertex v = g.getVertex(id);
			if (v == null) {
				throw new JValueLoadException("There's no vertex with id '"
						+ gid + "' in graph '" + gid + "'.", null);
			}
			val = new JValueImpl(v);
			// ---------------------------------------------------------------
		} else if (elem.equals(JValueXMLConstants.BROWSINGINFO)) {
			val = new JValueBrowsingInfo();
		} else {
			throw new JValueLoadException("Unrecognized XML element '" + elem
					+ "'.", null);
			// ---------------------------------------------------------------
		}

		// -------------------------------------------------------------------
		if (val == null) {
			throw new JValueLoadException(
					"Couldn't read the value of element '" + elem + "'.", null);
		}

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
