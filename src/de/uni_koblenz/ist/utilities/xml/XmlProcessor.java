/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
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
package de.uni_koblenz.ist.utilities.xml;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public abstract class XmlProcessor {

	/**
	 * The STAX reader for parsing the XML file to convert.
	 */
	private XMLStreamReader parser;

	/**
	 * Contains XML element names.
	 */
	private Stack<String> elementNameStack;

	/**
	 * Collects character data per element.
	 */
	private Stack<StringBuilder> elementContentStack;

	/**
	 * Names of XML elements which are completely ignored (including children).
	 */
	private final Set<String> ignoredElements;

	/**
	 * Counter for ignored state. <br>
	 * ignore > 0 ==> elements are ignored, <br>
	 * ignore == 0 ==> elements are processed.
	 */
	private int ignoreCounter;

	private String fileName;

	public XmlProcessor() {
		ignoredElements = new TreeSet<String>();
	}

	public void process(String fileName) throws FileNotFoundException,
			XMLStreamException {
		this.fileName = fileName;
		InputStream in = new BufferedInputStream(new FileInputStream(fileName));
		XMLInputFactory factory = XMLInputFactory.newInstance();
		parser = factory.createXMLStreamReader(in, "UTF-8");
		for (int event = parser.getEventType(); event != XMLStreamConstants.END_DOCUMENT; event = parser
				.next()) {
			switch (event) {
			case XMLStreamConstants.START_DOCUMENT:
				startDocumentEvent();
				break;
			case XMLStreamConstants.START_ELEMENT:
				startElementEvent();
				break;
			case XMLStreamConstants.END_ELEMENT:
				endElementEvent();
				break;
			case XMLStreamConstants.CHARACTERS:
				if (ignoreCounter == 0) {
					String t = parser.getText();
					characters(t);
					elementContentStack.peek().append(t);
				}
				break;
			}
		}
		endDocumentEvent();
	}

	private void startDocumentEvent() throws XMLStreamException {
		elementNameStack = new Stack<String>();
		elementContentStack = new Stack<StringBuilder>();
		ignoreCounter = 0;
		startDocument();
	}

	private void endDocumentEvent() throws XMLStreamException {
		if (!elementNameStack.isEmpty()) {
			StringBuilder openElements = new StringBuilder(
					"Missing end tags for");
			String delim = ": ";
			for (String s : elementNameStack) {
				openElements.append(delim).append(s);
				delim = ", ";
			}
			throw new XMLStreamException(openElements.toString());
		}
		assert ignoreCounter == 0;
		endDocument();
	}

	private void startElementEvent() throws XMLStreamException {
		QName qname = parser.getName();
		String name;
		if ((qname.getPrefix() == null) || qname.getPrefix().isEmpty()) {
			name = qname.getLocalPart();
		} else {
			name = qname.getPrefix() + ":" + qname.getLocalPart();
		}

		elementNameStack.push(name);
		elementContentStack.push(new StringBuilder());

		if (ignoredElements.contains(name)) {
			++ignoreCounter;
		}
		if (ignoreCounter == 0) {
			startElement(name);
		}
	}

	private void endElementEvent() throws XMLStreamException {
		QName qname = parser.getName();
		String name;
		if ((qname.getPrefix() == null) || qname.getPrefix().isEmpty()) {
			name = qname.getLocalPart();
		} else {
			name = qname.getPrefix() + ":" + qname.getLocalPart();
		}

		if (elementNameStack.isEmpty()) {
			throw new XMLStreamException("Unexpected end element </" + name
					+ "> in line " + parser.getLocation().getLineNumber());
		}

		String s = elementNameStack.peek();

		if (!s.equals(name)) {
			throw new XMLStreamException("Element <" + s
					+ "> is terminated by </" + name + "> in line "
					+ parser.getLocation().getLineNumber());
		}

		if (ignoreCounter == 0) {
			endElement(name, elementContentStack.peek());
		}
		if (ignoredElements.contains(name)) {
			assert ignoreCounter > 0;
			--ignoreCounter;
		}
		elementNameStack.pop();
		elementContentStack.pop();
	}

	protected void characters(String text) {
	}

	protected abstract void startElement(String name) throws XMLStreamException;

	protected abstract void endElement(String name, StringBuilder content)
			throws XMLStreamException;

	protected abstract void startDocument() throws XMLStreamException;

	protected abstract void endDocument() throws XMLStreamException;

	protected String getAttribute(String nsPrefix, String name)
			throws XMLStreamException {
		return parser.getAttributeValue(parser.getNamespaceURI(nsPrefix), name);
	}

	protected Set<String> getAttributeNames() {
		LinkedHashSet<String> attrNames = new LinkedHashSet<String>();
		int n = parser.getAttributeCount();
		for (int i = 0; i < n; ++i) {
			String ns = parser.getAttributePrefix(i);
			String localName = parser.getAttributeLocalName(i);
			attrNames.add((ns == null || ns.isEmpty()) ? localName : ns + ":"
					+ localName);
		}
		return attrNames;
	}

	protected String getAttribute(String name) throws XMLStreamException {
		int p = name.indexOf(":");
		if (p >= 0) {
			if (p > 0) {
				return getAttribute(name.substring(0, p - 1),
						name.substring(p + 1));
			} else {
				name = name.substring(p + 1);
			}
		}

		// TODO: The following line should work, but with java 1.6.0.18 it
		// returns the first attribute named `name' and doesn't care about
		// namespaces. Thus in <a ns1:x="..." x="..."/> there's no way of
		// getting the unprefixed attribute value.

		// String value = parser.getAttributeValue(null, name);
		// if (value != null) {
		// return value;
		// }

		// This is a workaround... Iterate over all attributes and return the
		// unprefixed attribute with the given name.
		for (int i = 0; i < parser.getAttributeCount(); i++) {
			if (((parser.getAttributeNamespace(i) == null) || XMLConstants.NULL_NS_URI
					.equals(parser.getAttributeNamespace(i)))
					&& name.equals(parser.getAttributeLocalName(i))) {
				return parser.getAttributeValue(i);
			}
		}
		return null;
	}

	protected void addIgnoredElements(String... names) {
		for (String name : names) {
			if (name != null) {
				name = name.trim();
				if (name.length() > 0) {
					ignoredElements.add(name);
				}
			}
		}
	}

	public int getNestingDepth() {
		return elementNameStack.size();
	}

	public String getFileName() {
		return fileName;
	}

	public Stack<String> getElementNameStack() {
		return elementNameStack;
	}

	public XMLStreamReader getParser() {
		return parser;
	}
}
