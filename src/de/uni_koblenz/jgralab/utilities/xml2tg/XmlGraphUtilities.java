package de.uni_koblenz.jgralab.utilities.xml2tg;

import java.util.Iterator;
import java.util.NoSuchElementException;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.NoSuchAttributeException;
import de.uni_koblenz.jgralab.VertexFilter;
import de.uni_koblenz.jgralab.utilities.xml2tg.schema.Attribute;
import de.uni_koblenz.jgralab.utilities.xml2tg.schema.Element;
import de.uni_koblenz.jgralab.utilities.xml2tg.schema.References;
import de.uni_koblenz.jgralab.utilities.xml2tg.schema.XMLGraph;

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

	private static class VertexFilterIterable<T> implements Iterable<T> {

		private Iterable<T> baseIterable;

		public VertexFilterIterable(Iterable<T> iterable) {
			this.baseIterable = iterable;
		}

		protected boolean accept(T vertex) {
			return true;
		}

		public Iterable<T> getBaseIterable() {
			return baseIterable;
		}

		@Override
		public Iterator<T> iterator() {
			return new VertexFilterIterator<T>(this);
		}
	}

	private static class VertexFilterIterator<T> implements Iterator<T> {
		VertexFilterIterable<T> filterIterable;
		Iterator<T> baseIterator;
		T current;

		public VertexFilterIterator(VertexFilterIterable<T> baseIterable) {
			this.filterIterable = baseIterable;
			baseIterator = filterIterable.getBaseIterable().iterator();
			getNext();
		}

		private void getNext() {
			while (baseIterator.hasNext()) {
				current = baseIterator.next();
				if (filterIterable.accept(current)) {
					return;
				}
			}
			current = null;
		}

		@Override
		public boolean hasNext() {
			return current != null;
		}

		@Override
		public T next() {
			if (current == null) {
				throw new NoSuchElementException();
			}
			T result = current;
			getNext();
			return result;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
