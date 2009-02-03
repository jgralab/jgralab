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

import java.util.Iterator;

/**
 * This class is base for all collections of JValue-Objects.
 *
 * @author ist@uni-koblenz.de
 *
 */
abstract public class JValueCollection extends JValue implements
		Iterable<JValue> {

	/**
	 * Constructs a new JValueCollection
	 */
	public JValueCollection() {
		type = JValueType.COLLECTION;
	}

	/**
	 * returns a JValueCollection-Reference of this Collection
	 */
	@Override
	public JValueCollection toCollection() {
		return this;
	}

	/**
	 * @return true always, because a JValueCollection is always a collection
	 */
	@Override
	public boolean isCollection() {
		return true;
	}

	/**
	 * @return true if this collection is ordered
	 */
	public boolean isOrderedCollection() {
		return false;
	}

	/**
	 * adds a JValue to the collection
	 *
	 * @param element
	 *            the JValue to be added
	 * @return true if successfull, false otherwise
	 */
	abstract public boolean add(JValue element);

	/**
	 * adds all elements of given collection to this collection
	 *
	 * @param collection
	 *            the collection whose elements should be added
	 * @return true if successfull, false otherwise
	 */
	public boolean addAll(JValueCollection collection) {
		Iterator<JValue> collectionIterator = collection.iterator();
		while (collectionIterator.hasNext()) {
			add(collectionIterator.next());
		}
		return true;
	}

	/**
	 * @return true if the collection contains the given element, false
	 *         otherwise
	 */
	abstract public boolean contains(JValue element);

	/**
	 * @param collection
	 *            the collection whose elements should be checked
	 * @return true if this collection contains all elements of the given
	 *         collection, false otherwise
	 */
	public boolean containsAll(JValueCollection collection) {
		Iterator<JValue> collectionIterator = collection.iterator();
		while (collectionIterator.hasNext()) {
			if (!contains(collectionIterator.next())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * removes a JValue from the collection
	 *
	 * @param element
	 *            the element to be removed
	 * @return true if successfull, false otherwise
	 */
	abstract public boolean remove(JValue element);

	/**
	 * removes a collection from this collection
	 *
	 * @param collection
	 *            the collection whose elements should be removed
	 * @return true if have been removed, false otherwise
	 */
	public boolean removeAll(JValueCollection collection) {
		Iterator<JValue> collectionIterator = collection.iterator();
		while (collectionIterator.hasNext()) {
			remove(collectionIterator.next());
		}
		return true;
	}

	/**
	 * replaces the old element the given newElement
	 *
	 * @param oldElement
	 *            the element which should be replaced
	 * @param newElement
	 *            the element which should replace the old one
	 * @return true if successfull, false otherwise
	 */
	abstract public boolean replace(JValue oldElement, JValue newElement);

	/**
	 *
	 * @return the number of elements in this collection
	 */
	abstract public int size();

	/**
	 * removes all elements from this collection
	 */
	abstract public void clear();

	/**
	 * @return true if this collection contains no elements, false otherwise
	 */
	abstract public boolean isEmpty();

	/**
	 * @return an Iterator to navigate through the collection
	 */
	abstract public Iterator<JValue> iterator();

	/**
	 * returns true if this Collection is a table
	 */
	public boolean isJValueTable() {
		return false;
	}

	/**
	 * transforms this JValueCollection to a JValueTable. Creates a table
	 * without headers
	 */
	@Override
	public JValueTable toJValueTable() {
		return new JValueTable(this);
	}

	/**
	 * returns true if this Collection is a set
	 */
	public boolean isJValueSet() {
		return false;
	}

	/**
	 * transforms this collection into a JValueSet
	 *
	 * @return a JValueSet which contains the same elements as this collection,
	 *         duplicates will be eliminated
	 */
	@Override
	public JValueSet toJValueSet() {
		return new JValueSet(this);
	}

	/**
	 * returns true if this Collection is a bag
	 */
	public boolean isJValueBag() {
		return false;
	}

	/**
	 * transforms this collection into a JValueBag
	 *
	 * @return a JValueBag which contains the same elements as this collection,
	 *         duplicates won't be eliminated
	 */
	@Override
	public JValueBag toJValueBag() {
		return new JValueBag(this);
	}

	/**
	 * returns true if this Collection is a list
	 */
	public boolean isJValueList() {
		return false;
	}

	/**
	 * transforms this collection into a JValueList. Beware, the order of the
	 * elements is random, the are _not_ sorted
	 *
	 * @return a JValueList which contains the same elements as this collecton,
	 *         duplicates won't be eliminated
	 */
	@Override
	public JValueList toJValueList() {
		return new JValueList(this);
	}

	/**
	 * returns true if this Collection is a tuple
	 */
	public boolean isJValueTuple() {
		return false;
	}

	/**
	 * transforms this collection into a JValueTupel. Beware, the order of the
	 * elements is random, the are _not_ sorted and the sequence of the resultig
	 * tupel will be random. Use this with care
	 *
	 * @return a JValueTupel which contains the same elements as this collecton,
	 *         duplicates won't be eliminated
	 */
	@Override
	public JValueTuple toJValueTuple() {
		return new JValueTuple(this);
	}

	/**
	 * returns true if this Collection is a record
	 */
	public boolean isJValueRecord() {
		return false;
	}

	/**
	 * transforms this collection into a JValueRecord. If the collection is not
	 * already a record, the attribute names are 1, 2 ...
	 *
	 * @return a JValueRecord which contains the same elements as this
	 *         collecton, duplicates won't be eliminated
	 */
	@Override
	public JValueRecord toJValueRecord() {
		return new JValueRecord(this);
	}

	/**
	 * Returns this Collection as a String representation, { arg1, arg2, arg3 }
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		Iterator<JValue> iter = this.iterator();
		boolean first = true;
		while (iter.hasNext()) {
			if (!first) {
				sb.append(", ");
			}
			first = false;
			sb.append(iter.next().toString());
		}
		sb.append("}");
		return sb.toString();
	}

}
