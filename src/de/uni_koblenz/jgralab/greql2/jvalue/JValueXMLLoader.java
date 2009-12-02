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

package de.uni_koblenz.jgralab.greql2.jvalue;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.stream.XMLStreamException;

import de.uni_koblenz.ist.utilities.xml.XmlProcessor;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.WorkInProgress;
import de.uni_koblenz.jgralab.greql2.exception.JValueLoadException;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Schema;

@WorkInProgress(description = "Don't use, this will be completely rewritten!", responsibleDevelopers = "horn")
public class JValueXMLLoader extends XmlProcessor {

	/**
	 * Synthetic class to ease XML parsing
	 */
	private class JValueRecordComponent extends JValue {
		String componentName;
		JValue jvalue;

		public JValueRecordComponent(String compName, JValue v) {
			componentName = compName;
			jvalue = v;
		}
	}

	/**
	 * Synthetic class to ease XML parsing
	 */
	private class JValueMapEntry extends JValueTuple {
	}

	private Map<String, Graph> id2GraphMap = null;
	private Map<String, Schema> schemaName2Schema = null;
	private Stack<JValue> stack = new Stack<JValue>();

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

	public JValue load(String fileName) {
		// TODO: implement me!
		return null;
	}

	@Override
	protected void endDocument() throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void endElement(String arg0, StringBuilder arg1)
			throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void startDocument() throws XMLStreamException {
		stack.clear();
	}

	@Override
	protected void startElement(String elem) throws XMLStreamException {
		JValue val = null;
		if (elem.equals(JValueXMLConstants.ATTRIBUTEDELEMENTCLASS)) {
			String qName = getAttribute(JValueXMLConstants.ATTR_NAME);
			String schema = getAttribute(JValueXMLConstants.ATTR_SCHEMA);
			AttributedElementClass aec = schemaName2Schema.get(schema)
					.getAttributedElementClass(qName);
			val = new JValue(aec);
			// ---------------------------------------------------------------
		} else if (elem.equals(JValueXMLConstants.BAG)) {
			val = new JValueBag();
			// ---------------------------------------------------------------
		} else if (elem.equals(JValueXMLConstants.BOOLEAN)) {
			val = new JValue(Boolean
					.valueOf(getAttribute(JValueXMLConstants.ATTR_VALUE)));
			// ---------------------------------------------------------------
		} else if (elem.equals(JValueXMLConstants.DOUBLE)) {
			val = new JValue(Double
					.valueOf(getAttribute(JValueXMLConstants.ATTR_VALUE)));
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
			val = new JValue(e);
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
			val = new JValue(g);
			// ---------------------------------------------------------------
		} else if (elem.equals(JValueXMLConstants.INTEGER)) {
			val = new JValue(Integer
					.valueOf(getAttribute(JValueXMLConstants.ATTR_VALUE)));
			// ---------------------------------------------------------------
		}

		// -------------------------------------------------------------------
		if (val == null) {
			throw new JValueLoadException("Unrecognized XML element '" + elem
					+ "'.", null);
		}
	}

	@SuppressWarnings("unchecked")
	private JValue createEnum(String litName, String enumTypeName) {
		JValue val = null;
		try {
			Class<? extends Enum> e = (Class<? extends Enum>) Class
					.forName(enumTypeName);
			val = new JValue(Enum.valueOf(e, litName));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new JValueLoadException("The Enum class '" + enumTypeName
					+ "' could not be loaded.", e);
		}
		return val;
	}
}
