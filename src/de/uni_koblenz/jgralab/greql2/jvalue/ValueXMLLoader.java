/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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

import java.util.Stack;

import org.riediger.plist.MinimalSAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.exception.JValueLoadException;

public class ValueXMLLoader extends DefaultHandler {

	/**
	 * This class modells a entry on the stack
	 */
	private class StackEntry {

		String tag;

		JValueCollection collection;

		AttributedElement browsingInfo = null;

		String value;

		public void setBrowsingEdge(Graph graph, String edgeIdString) {
			int edgeId = Integer.parseInt(edgeIdString);
			browsingInfo = graph.getEdge(edgeId);
		}

		public void setBrowsingVertex(Graph graph, String vertexIdString) {
			int vertexId = Integer.parseInt(vertexIdString);
			browsingInfo = graph.getVertex(vertexId);
		}

		StackEntry(String tagName) {
			tag = tagName;
			this.value = "";
			this.collection = null;
		}

		StackEntry(String tagName, JValueCollection col) {
			tag = tagName;
			this.value = "";
			this.collection = col;
		}

	}

	/**
	 * This stack holds all jvalue object that were created but not finished
	 */
	private Stack<StackEntry> valueStack;

	/**
	 * The Graph all elements in the created jvalue belong to. Without this
	 * graph, it is not possible to load a JValue which contains vertices, edges
	 * etc.
	 */
	private Graph graph;

	/**
	 * The JValue which was loaded
	 */
	private JValue loadedValue = null;

	/**
	 * The String of the current Data
	 */
	private String currentData = "";

	/**
	 * creates a new JValue out of the given xmlfile
	 */
	public ValueXMLLoader(Graph graph) {
		valueStack = new Stack<StackEntry>();
		this.graph = graph;
	}

	public JValue load(String pathToXMLFile) throws JValueLoadException {
		MinimalSAXParser parser = new MinimalSAXParser();
		try {
			parser.parse(pathToXMLFile, this);
		} catch (Exception e) {
			throw new JValueLoadException("Error reading JValue from '"
					+ pathToXMLFile + "'", e);
		}
		return loadedValue;
	}

	public void characters(char[] ch, int start, int length) {
		String s = new String(ch, start, length);
		currentData = s;
		// if (!valueStack.empty()) {
		// valueStack.peek().value = s;
		// }
	}

	/**
	 * This method is called by the SAX-Parser
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) {
		// GreqlEvaluator.println("Starting: '" + qName + "'");
		JValueCollection col = null;
		if (qName.equals("set"))
			col = new JValueSet();
		else if (qName.equals("bag"))
			col = new JValueBag();
		else if (qName.equals("list") || qName.equals("tabledata"))
			col = new JValueList();
		else if ((qName.equals("tuple")) || (qName.equals("header")))
			col = new JValueTuple();
		else if (qName.equals("table"))
			col = new JValueTable();
		else if (qName.equals("record"))
			col = new JValueRecord();
		if (col != null) {
			valueStack.push(new StackEntry(qName, col));
		} else {
			if (!qName.equals("value") && !qName.equals("browsevertex")
					&& !qName.equals("browseedge")
					&& !qName.equals("tabledata"))
				valueStack.push(new StackEntry(qName));
		}

	}

	/**
	 * This method is called by the SAX-Parser
	 */
	public void endElement(String uri, String localName, String qName) {
		// GreqlEvaluator.println("CurrentData is: '" + currentData + "'");
		// GreqlEvaluator.println("Loaded Value: " + loadedValue);
		JValue value = null;
		// GreqlEvaluator.println("endElement: '" + qName + "'");
		if (qName.equals("xmlvalue"))
			return;
		StackEntry entry = valueStack.peek();
		if (qName.equals("value")) {
			entry.value = currentData;
		} else if (qName.equals("browsevertex")) {
			entry.setBrowsingVertex(graph, currentData);
		} else if (qName.equals("browseedge")) {
			entry.setBrowsingEdge(graph, currentData);
		} else {
			entry = valueStack.pop();
			if ((qName.equals("set")) || (qName.equals("bag"))
					|| (qName.equals("list")) || (qName.equals("tuple"))
					|| (qName.equals("table")) || (qName.equals("record"))) {
				if (!valueStack.empty()) {
					entry.collection.setBrowsingInfo(entry.browsingInfo);
					value = entry.collection;
				}
			} else if (qName.equals("header")) {
				value = null;
				JValueTable tab = (JValueTable) valueStack.peek().collection;
				tab.setHeader((JValueTuple) entry.collection);
			} else if (qName.equals("tabledata")) {
				value = null;
				JValueTable tab = (JValueTable) valueStack.peek().collection;
				JValueList list = (JValueList) entry.collection;
				JValueCollection col = (JValueCollection) list.get(0);
				tab.setData(col);
			} else if ((qName.equals("vertex"))) {
				int vertexId = Integer.parseInt(entry.value);
				Vertex vertex = graph.getVertex(vertexId);
				value = new JValue(vertex);
			} else if ((qName.equals("edge"))) {
				int edgeId = Integer.parseInt(entry.value);
				Edge edge = graph.getEdge(edgeId);
				value = new JValue(edge);
			} else if ((qName.equals("bool"))) {
				value = new JValue(Boolean.parseBoolean(entry.value));
			} else if ((qName.equals("integer"))) {
				value = new JValue(Integer.parseInt(entry.value));
			} else if ((qName.equals("long"))) {
				value = new JValue(Long.parseLong(entry.value));
			} else if ((qName.equals("double"))) {
				value = new JValue(Double.parseDouble(entry.value));
			} else if ((qName.equals("string"))) {
				value = new JValue(entry.value);
			} else if ((qName.equals("enumvalue"))) {
				// TODO change load method in a way that the enum is restored
				value = new JValue(entry.value);
			} else if ((qName.equals("char"))) {
				value = new JValue(entry.value.charAt(0));
			}
			if (value != null) {
				value.setBrowsingInfo(entry.browsingInfo);
				loadedValue = value;
				addElement(value);
			}
		}
		currentData = "";
	}

	/**
	 * Adds the given JValue to the JValue on top of the vertex
	 */
	public void addElement(JValue element) {
		if (!valueStack.empty()) {
			JValueCollection col = valueStack.peek().collection;
			if (col != null) {
				col.add(element);
			}
		}
	}

}
