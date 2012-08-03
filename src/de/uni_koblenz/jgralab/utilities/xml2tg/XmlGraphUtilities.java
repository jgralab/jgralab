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

import java.util.Iterator;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.NoSuchAttributeException;
import de.uni_koblenz.jgralab.VertexFilter;
import de.uni_koblenz.jgralab.utilities.xml2tg.schema.Attribute;
import de.uni_koblenz.jgralab.utilities.xml2tg.schema.Element;
import de.uni_koblenz.jgralab.utilities.xml2tg.schema.References;
import de.uni_koblenz.jgralab.utilities.xml2tg.schema.Text;
import de.uni_koblenz.jgralab.utilities.xml2tg.schema.XMLGraph;

/**
 * XmlGraphUtilities provides access methods to support processing of XMLGraphs.
 * 
 * @author ist@uni-koblenz.de
 */
public class XmlGraphUtilities {
	private XMLGraph xg;

	public XmlGraphUtilities(XMLGraph xg) {
		this.xg = xg;
	}

	public Iterable<Element> elementsWithName(final String name) {
		return xg.getElementVertices(new VertexFilter<Element>() {
			@Override
			public boolean accepts(Element vertex) {
				return vertex.get_name().equals(name);
			}
		});
	}

	public Iterable<Element> childrenWithName(Element parent, final String name) {
		return parent.get_children(new VertexFilter<Element>() {
			@Override
			public boolean accepts(Element vertex) {
				return vertex.get_name().equals(name);
			}
		});
	}

	public Element firstChildWithName(Element parent, String name) {
		Iterator<Element> it = childrenWithName(parent, name).iterator();
		return it.hasNext() ? it.next() : null;
	}

	public Element getRootElement() {
		for (Element el : xg.getElementVertices()) {
			if (el.getFirstHasChildIncidence(EdgeDirection.IN) == null) {
				return el;
			}
		}
		return null;
	}

	public String getAttributeValue(Element el, String attrName) {
		for (Attribute attr : el.get_attributes()) {
			if (attr.get_name().equals(attrName)) {
				return attr.get_value();
			}
		}
		throw new NoSuchAttributeException("Element " + el
				+ " has no attribute '" + attrName + "'");
	}

	public String getAttributeValue(Element el, String attrName,
			boolean upperCaseFirstLetter) {
		String result = getAttributeValue(el, attrName);
		if (upperCaseFirstLetter) {
			result = result.trim();
			if (result.length() > 0) {
				result = Character.toUpperCase(result.charAt(0))
						+ result.substring(1);
			}
		}
		return result;
	}

	public Element getReferencedElement(Element el, String attrName) {
		for (Attribute attr : el.get_attributes()) {
			if (attr.get_name().equals(attrName)) {
				References r = attr.getFirstReferencesIncidence();
				return r == null ? null : (Element) r.getThat();
			}
		}
		throw new NoSuchAttributeException("Element " + el
				+ " has no attribute '" + attrName + "'");
	}

	public Iterable<Element> getReferencedElements(Element el, String attrName) {
		for (Attribute attr : el.get_attributes()) {
			if (attr.get_name().equals(attrName)) {
				return attr.get_refs();
			}
		}
		throw new NoSuchAttributeException("Element " + el
				+ " has no attribute '" + attrName + "'");
	}

	public boolean hasAttribute(Element el, String attrName) {
		for (Attribute attr : el.get_attributes()) {
			if (attr.get_name().equals(attrName)) {
				return true;
			}
		}
		return false;
	}

	public String getText(Element el) {
		StringBuilder sb = new StringBuilder();
		for (Text t : el.get_texts()) {
			sb.append(t.get_content());
		}
		return sb.toString();
	}

}
