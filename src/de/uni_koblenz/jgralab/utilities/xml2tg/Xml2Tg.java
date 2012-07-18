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

package de.uni_koblenz.jgralab.utilities.xml2tg;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.xml.stream.XMLStreamException;

import de.uni_koblenz.ist.utilities.xml.XmlProcessor;
import de.uni_koblenz.jgralab.ImplementationType;
import de.uni_koblenz.jgralab.TemporaryVertex;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.utilities.xml2tg.schema.Attribute;
import de.uni_koblenz.jgralab.utilities.xml2tg.schema.Element;
import de.uni_koblenz.jgralab.utilities.xml2tg.schema.References;
import de.uni_koblenz.jgralab.utilities.xml2tg.schema.Text;
import de.uni_koblenz.jgralab.utilities.xml2tg.schema.XMLGraph;
import de.uni_koblenz.jgralab.utilities.xml2tg.schema.XMLSchema;

/**
 * Xml2Tg reads an XML file and builds a DOM-like XMLGraph.
 *
 * Xml2Tg tries to resolve IDREF and IDREFS attributes by creating References
 * edges from the attribute vertices to the referenced elements. The attribute
 * names for ID, IDREF and IDREFS attributes can be defined globally or per
 * element name.
 *
 * Optionally, the text contained in the elements can be skippped.
 *
 * @author ist@uni-koblenz.de
 */
public class Xml2Tg extends XmlProcessor {
	private Set<String> idAttributes;
	private Set<String> idRefAttributes;
	private Set<String> idRefsAttributes;
	private boolean ignoreCharacters;
	private XMLGraph xmlGraph;
	private Map<String, Vertex> idMap;
	private Stack<Element> elementStack;
	private boolean finished;
	private String fileName;

	public Xml2Tg() {
		idAttributes = new HashSet<String>();
		idRefAttributes = new HashSet<String>();
		idRefsAttributes = new HashSet<String>();
		idMap = new HashMap<String, Vertex>();
		elementStack = new Stack<Element>();
	}

	public XMLGraph getXmlGraph() {
		if (!finished) {
			throw new IllegalStateException(
					"getXmlGraph can only be called after the XML document was fully processed");
		}
		return xmlGraph;
	}

	public Element getElementById(String id) {
		if (!finished) {
			throw new IllegalStateException(
					"getElementById can only be called after the XML document was fully processed");
		}
		return (Element) idMap.get(id);
	}

	public void addIdAttributes(String... attrNames) {
		for (String s : attrNames) {
			s = s.trim();
			if (!s.isEmpty()) {
				idAttributes.add(s);
			}
		}
	}

	public void addIdRefAttributes(String... attrNames) {
		for (String s : attrNames) {
			s = s.trim();
			if (!s.isEmpty()) {
				idRefAttributes.add(s);
			}
		}
	}

	public void addIdRefsAttributes(String... attrNames) {
		for (String s : attrNames) {
			s = s.trim();
			if (!s.isEmpty()) {
				idRefsAttributes.add(s);
			}
		}
	}

	public boolean isIgnoreCharacters() {
		return ignoreCharacters;
	}

	public void setIgnoreCharacters(boolean ignoreCharacters) {
		this.ignoreCharacters = ignoreCharacters;
	}

	@Override
	protected void startElement(String name) throws XMLStreamException {
		Element el = null;
		Set<String> attributeNames = getAttributeNames();
		for (String attrName : attributeNames) {
			if (idAttributes.contains("*/" + attrName)
					|| idAttributes.contains(name + "/" + attrName)) {
				String id = getAttribute(attrName);
				Vertex v = idMap.get(id);
				if (v != null) {
					if (v.isTemporary()) {
						// System.out
						// .println("convert temporary element with id '"
						// + id + "'");
						el = (Element) (((TemporaryVertex) v).bless(Element.VC));
					} else {
						// System.out.println("use cached element with id '" +
						// id
						// + "'");
						el = (Element) v;
					}
				} else {
					// System.out.println("create element with id '" + id +
					// "'");
					el = xmlGraph.createElement();
				}
				idMap.put(id, el);
			}
		}
		if (el == null) {
			// System.out.println("create element");
			el = xmlGraph.createElement();
		}

		assert el != null;
		// System.out.println(name + " " + el);
		el.set_name(name);
		if (getNestingDepth() > 1) {
			elementStack.peek().add_children(el);
		}
		elementStack.push(el);
		for (String attrName : attributeNames) {
			Attribute attr = xmlGraph.createAttribute();
			attr.set_name(attrName);
			attr.set_value(getAttribute(attrName));
			el.add_attributes(attr);
			if (idRefAttributes.contains("*/" + attrName)
					|| idRefAttributes.contains(name + "/" + attrName)) {
				Vertex ref = idMap.get(attr.get_value());
				if (ref == null) {
					ref = xmlGraph.createTemporaryVertex();
					idMap.put(attr.get_value(), ref);
				}
				xmlGraph.createEdge(References.EC, attr, ref);
				// System.out.println("REF(" + el + ") " + name + "/" + attrName
				// + " -> " + ref);
			} else if (idRefsAttributes.contains("*/" + attrName)
					|| idRefsAttributes.contains(name + "/" + attrName)) {
				for (String val : attr.get_value().split("\\s+")) {
					val = val.trim();
					if (val.isEmpty()) {
						continue;
					}
					Vertex ref = idMap.get(val);
					if (ref == null) {
						ref = xmlGraph.createTemporaryVertex();
						idMap.put(val, ref);
					}
					xmlGraph.createEdge(References.EC, attr, ref);
				}
			}
		}
	}

	@Override
	protected void characters(String text) {
		if (ignoreCharacters) {
			return;
		}
		super.characters(text);
		Element el = elementStack.peek();
		assert el != null;
		Text t = xmlGraph.createText();
		t.set_content(text);
		el.add_texts(t);
	}

	@Override
	protected void endElement(String name, StringBuilder content)
			throws XMLStreamException {
		Element el = elementStack.pop();
		assert el != null;
		assert el.get_name().equals(name);
		// System.out.println("/" + name + " " + el);
	}

	@Override
	public void process(String fileName) throws FileNotFoundException,
			XMLStreamException {
		this.fileName = fileName;
		super.process(fileName);
	}

	@Override
	protected void startDocument() throws XMLStreamException {
		xmlGraph = XMLSchema.instance().createXMLGraph(
				ImplementationType.STANDARD, fileName, 100, 100);
		idMap.clear();
		elementStack.clear();
		finished = false;
	}

	@Override
	protected void endDocument() throws XMLStreamException {
		ArrayList<String> undefinedRefs = new ArrayList<String>();
		if (elementStack.size() > 0) {
			throw new RuntimeException("XML document is malformed");
		}
		for (String id : idMap.keySet()) {
			if (idMap.get(id).isTemporary()) {
				undefinedRefs.add(id);
			}
		}
		if (undefinedRefs.size() > 0) {
			StringBuilder sb = new StringBuilder();
			String delim = "There are undefined references in the XML document: ";
			for (String id : undefinedRefs) {
				sb.append(delim).append(id);
				delim = ", ";
			}
			throw new RuntimeException(sb.toString());
		}
		finished = true;
	}
}
