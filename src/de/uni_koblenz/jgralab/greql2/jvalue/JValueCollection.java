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

import java.util.Iterator;

import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;

/**
 * This class is base for all collections of JValue-Objects.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
abstract public class JValueCollection extends JValueImpl implements
		Iterable<JValue> {

	/**
	 * Constructs a new JValueCollection
	 */
	public JValueCollection() {
		type = JValueType.COLLECTION;
	}

	/**
	 * @param c
	 * @return a shallow copy of {@code c}
	 */
	public static JValueCollection shallowCopy(JValueCollection c) {
		if (c instanceof JValueBag) {
			return new JValueBag(c);
		}
		if (c instanceof JValueList) {
			return new JValueList(c);
		}
		if (c instanceof JValueRecord) {
			return new JValueRecord(c);
		}
		if (c instanceof JValueSet) {
			return new JValueSet(c);
		}
		if (c instanceof JValueTable) {
			return new JValueTable(c);
		}

		throw new JValueInvalidTypeException("Cannot create a shallow copy of "
				+ c + ", because its type " + c.getClass().getCanonicalName()
				+ " is not recognized.");
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
	 * @param j
	 *            the JValue to be added
	 * @return true if successfull, false otherwise
	 */
	abstract public boolean add(JValue j);

	/**
	 * adds all elements of given collection to this collection
	 * 
	 * @param collection
	 *            the collection whose elements should be added
	 * @return true if successfull, false otherwise
	 */
	public boolean addAll(JValueCollection collection) {
		for (JValue j : collection) {
			add(j);
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
		for (JValue j : collection) {
			if (!contains(j)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * removes a JValue from the collection
	 * 
	 * @param j
	 *            the element to be removed
	 * @return true if successfull, false otherwise
	 */
	abstract public boolean remove(JValue j);

	/**
	 * removes a collection from this collection
	 * 
	 * @param collection
	 *            the collection whose elements should be removed
	 * @return true if have been removed, false otherwise
	 */
	public boolean removeAll(JValueCollection collection) {
		for (JValue j : collection) {
			remove(j);
		}
		return true;
	}

	/**
	 * 
	 * @return the number of elements in this collection
	 */
	abstract public int size();

	/**
	 * removes all elements from this collection
	 */
	abstract public void clear();

	@Override
	public int compareTo(JValue o) {
		// if both are the same class (e.g. two tuples)...
		if (this.getClass() != o.getClass()) {
			return super.compareTo(o);
		}
		// then compare the values (e.g. tuple components) pairwise.
		JValueCollection other = (JValueCollection) o;
		Iterator<JValue> oi = other.iterator();
		for (JValue jv : this) {
			if (!oi.hasNext()) {
				return 1;
			}
			JValue ov = oi.next();
			int val = jv.compareTo(ov);
			if (val != 0) {
				return val;
			}
		}
		return 0;
	}

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

	protected String OPENING_PAREN = "{";
	protected String CLOSING_PAREN = "}";

	/**
	 * Returns this Collection as a String representation, { arg1, arg2, arg3 }
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.OPENING_PAREN);
		boolean first = true;
		for (JValue j : this) {
			if (!first) {
				sb.append(", ");
			}
			first = false;
			sb.append(j.toString());
		}
		sb.append(this.CLOSING_PAREN);
		return sb.toString();
	}

	/**
	 * Sort this collection according the natural order of its elements.
	 */
	public abstract void sort();

}
