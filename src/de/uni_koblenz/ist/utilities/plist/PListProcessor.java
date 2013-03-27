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
package de.uni_koblenz.ist.utilities.plist;

import java.text.ParseException;
import java.util.Stack;
import java.util.Vector;

import javax.xml.stream.XMLStreamException;

import de.uni_koblenz.ist.utilities.xml.XmlProcessor;

class PListProcessor extends XmlProcessor {
	private PListDict dict;

	private static class StackEntry {
		String tag;
		String lastKey;
		PListDict dict;
		Vector<Object> array;
		Object value;
	}

	private Stack<StackEntry> objectStack;

	@Override
	protected void startElement(String name) throws XMLStreamException {
		if (objectStack == null) {
			throw new IllegalStateException(
					"startElement called before startDocument");
		}
		StackEntry e = new StackEntry();
		e.tag = name;
		if (e.tag.equals("dict")) {
			e.dict = new PListDict();
		} else if (e.tag.equals("array")) {
			e.array = new Vector<Object>();
		}
		objectStack.push(e);
	}

	@Override
	protected void endElement(String name, StringBuilder content)
			throws XMLStreamException {
		if (objectStack == null) {
			throw new IllegalStateException(
					"endElement called before startDocument");
		}
		StackEntry e = objectStack.pop();
		StackEntry top = null;
		if (objectStack.size() > 0) {
			top = (objectStack.peek());
		}
		if (e.tag.equals("string")) {
			e.value = content.toString();
		} else if (e.tag.equals("key")) {
			top.lastKey = content.toString();
		} else if (e.tag.equals("integer")) {
			e.value = Integer.parseInt(content.toString());
		} else if (e.tag.equals("real")) {
			e.value = Double.parseDouble(content.toString());
		} else if (e.tag.equals("date")) {
			try {
				synchronized (PList.dateFormat) {
					e.value = PList.dateFormat.parse(content.toString());
				}
			} catch (ParseException e1) {
				e.value = null;
			}
		} else if (e.tag.equals("data")) {
			throw new XMLStreamException(
					"property list element 'data' not yet implemented");
		} else if (e.tag.equals("true")) {
			e.value = true;
		} else if (e.tag.equals("false")) {
			e.value = false;
		} else if (e.tag.equals("dict")) {
			e.value = e.dict;
		} else if (e.tag.equals("array")) {
			e.value = e.array;
		}
		if (top != null) {
			if (top.dict != null && top.lastKey != null && !e.tag.equals("key")) {
				top.dict.put(top.lastKey, e.value);
				top.lastKey = null;
			} else if (top.array != null) {
				top.array.add(e.value);
			} else if (top.tag.equals("plist")) {
				dict = (PListDict) (e.value);
			}
		}
	}

	@Override
	protected void startDocument() throws XMLStreamException {
		if (objectStack != null) {
			throw new IllegalStateException(
					"startDocument called multiple times");
		}
		objectStack = new Stack<StackEntry>();
	}

	@Override
	protected void endDocument() throws XMLStreamException {
		if (objectStack == null) {
			throw new IllegalStateException(
					"endDocument called before startDocument");
		}
		objectStack = null;
	}

	public PListDict getDict() {
		return dict;
	}
}