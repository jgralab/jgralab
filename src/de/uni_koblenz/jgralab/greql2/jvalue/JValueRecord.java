/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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

package de.uni_koblenz.jgralab.greql2.jvalue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class JValueRecord extends JValueCollection implements
		Map<String, JValue> {

	/**
	 * This map is the internal data structure
	 * 
	 */
	private Map<String, JValue> dataMap;

	/**
	 * creates a new empty JValueRecord
	 * 
	 */
	public JValueRecord() {
		super();
		this.type = JValueType.RECORD;
		dataMap = new TreeMap<String, JValue>();
	}

	/**
	 * creates a new JValueREcord which contains all elements in the given
	 * collection
	 */
	public JValueRecord(JValueCollection collection) {
		super();
		this.type = JValueType.RECORD;
		dataMap = new TreeMap<String, JValue>();
		if (collection.isJValueRecord()) {
			JValueRecord rec = (JValueRecord) collection;
			dataMap.putAll(rec.dataMap);
		} else {
			Iterator<JValue> iter = collection.iterator();
			int i = 0;
			while (iter.hasNext()) {
				JValue curElem = iter.next();
				dataMap.put(String.valueOf(i), curElem);
				i++;
			}
		}
		addAll(collection);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof JValueRecord) {
			JValueRecord other = (JValueRecord) o;
			return dataMap.equals(other.dataMap);
		}
		return false;
	}

	/**
	 * Returns the hash code value for this record. To get the hash code of this
	 * record, new hash code values for every element of this record are
	 * calculated from a polynomial of 3rd order and finally summed up. This
	 * ensures that <code>s1.equals(s2)</code> implies that
	 * <code>s1.hashCode()==s2.hashCode()</code> for any two record
	 * <code>s1</code> and <code>s2</code>, as required by the general contract
	 * of <code>Object.hashCode()</code>.
	 * 
	 * @return the hash code value for this record.
	 */
	@Override
	public int hashCode() {
		if (storedHashCode == 0) {
			int elementHashCode = 0;
			int newHashCode = -1;

			for (Entry<String, JValue> currentEntry : dataMap.entrySet()) {
				elementHashCode = currentEntry.getKey().hashCode()
						* currentEntry.getValue().hashCode();
				newHashCode += -1 + (3 + elementHashCode)
						* (7 + elementHashCode) * (11 + elementHashCode);
			}
			newHashCode += this.getClass().hashCode();
			storedHashCode = newHashCode;
		}
		return storedHashCode;
	}

	/**
	 * returns a JValueRecord-Reference to this object
	 */
	@Override
	public JValueRecord toJValueRecord() {
		return this;
	}

	@Override
	public Map<String, Object> toObject() {
		Map<String, Object> result = new HashMap<String, Object>(dataMap.size());
		for (Entry<String, JValue> e : dataMap.entrySet()) {
			result.put(e.getKey(), e.getValue().toObject());
		}
		return result;
	}

	/**
	 * returns a JValueRecord-Reference to this object
	 */
	@Override
	public boolean isJValueRecord() {
		return true;
	}

	/**
	 * @return the number of elements in this record
	 */
	@Override
	public int size() {
		return dataMap.size();
	}

	/**
	 * inherited from JValueCollection, returns false because in a record its
	 * not possible to add an element without an id
	 * 
	 * @return false
	 */
	@Override
	public boolean add(JValue value) {
		return false;
	}

	/**
	 * removes all elements from this record
	 */
	@Override
	public void clear() {
		dataMap.clear();
	}

	/**
	 * Checks if this record contains the given value
	 * 
	 * @return true if the record contains the given value, false otherwise
	 */
	@Override
	public boolean contains(JValue value) {
		return dataMap.containsValue(value);
	}

	/**
	 * Checks if this record is empty
	 * 
	 * @return true if the record contains no elements, false otherwise
	 */
	@Override
	public boolean isEmpty() {
		return dataMap.isEmpty();
	}

	/**
	 * Removes the given value from this record
	 * 
	 * @return true if the value was successfull removes, false if the value is
	 *         not in this record or if it cannot be removed
	 */
	public boolean removeValue(JValue value) {
		Collection<JValue> values = dataMap.values();
		Iterator<JValue> iter = values.iterator();
		while (iter.hasNext()) {
			if (iter.next() == value) {
				iter.remove();
				return true;
			}
		}
		return false;
	}

	/**
	 * Removes the given value from this record
	 * 
	 * @return true if the value was successfull removes, false if the value is
	 *         not in this record or if it cannot be removed
	 */
	@Override
	public boolean remove(JValue value) {
		return removeValue(value);
	}

	/**
	 * Returns an Iterator to iterate over all elements
	 */
	@Override
	public Iterator<JValue> iterator() {
		Collection<JValue> values = dataMap.values();
		return values.iterator();
	}

	/**
	 * Adds the given value with the given key as id to this record
	 * 
	 * @return true if the element was added successfull, false if it could not
	 *         be added, maybe because of a duplicated key
	 */
	public boolean add(String key, JValue value) {
		if (dataMap.containsKey(key)) {
			return false;
		}
		dataMap.put(key, value);
		return true;
	}

	/**
	 * Replaces the element with the given key with the given replacement
	 * 
	 * @return true if the element was replaced successfull, false if it could
	 *         not be replaced, maybe because there exist no such key
	 */
	public boolean replace(String key, JValue value) {
		if (!dataMap.containsKey(key)) {
			return false;
		}
		dataMap.put(key, value);
		return true;
	}

	/**
	 * Puts the given value with the given key in this map. If there exists also
	 * such a key, returns the value which is associated with that key. Behaves
	 * exactly like <code>java.util.Map.put()</code>.
	 * 
	 * @return the JValue which was associated with that key before, or null if
	 *         there is none
	 */
	public JValue put(String key, JValue value) {
		return dataMap.put(key, value);
	}

	/**
	 * Checks if this record contains the given key
	 * 
	 * @return true if it contains this key, false otherwise
	 */
	public boolean containsKey(Object key) {
		return dataMap.containsKey(key);
	}

	/**
	 * Checks if this record contains the given value. Behaves exactly like
	 * <code>contains(JValue value)</code> but is defined in the Interface Map
	 * 
	 * @return true if this record contains the given value, false otherwise
	 */
	public boolean containsValue(Object value) {
		return dataMap.containsValue(value);
	}

	/**
	 * Returns the object associated with the given key or null if this key
	 * doesn't exist
	 */
	public JValue get(Object key) {
		return dataMap.get(key);
	}

	/**
	 * If key is a String, removes the object associated with the given key from
	 * this record If key is a JValue, calls remove(JValue) If key is something
	 * else, does nothing If possible, use removeKey() or removeValue() instead
	 * 
	 * @return If key is a String, the value associated with this string, null
	 *         otherwise
	 */
	public JValue remove(Object key) {
		if (key instanceof JValueImpl) {
			removeValue((JValue) key);
			return null;
		}
		if (key instanceof String) {
			return removeKey((String) key);
		}
		return null;
	}

	/**
	 * Removes the object associated with the given key from this record
	 * 
	 * @return The value associated with this string, null if this key doesn't
	 *         exist
	 */
	public JValue removeKey(String key) {
		return dataMap.remove(key);
	}

	/**
	 * Returns a collection-view of the values in this Record.
	 */
	public Collection<JValue> values() {
		return dataMap.values();
	}

	/**
	 * Puts all elements from the given Map to this map. Elements with the same
	 * key that are already existing in this record are deleted
	 */
	public void putAll(Map<? extends String, ? extends JValue> foreignMap)
			throws UnsupportedOperationException, ClassCastException,
			NullPointerException, IllegalArgumentException {
		dataMap.putAll(foreignMap);
	}

	/**
	 * Returns a set of keys in this Record
	 */
	public Set<String> keySet() {
		return dataMap.keySet();
	}

	/**
	 * Returns a set of values
	 */
	public Set<Map.Entry<String, JValue>> entrySet() {
		return dataMap.entrySet();
	}

	/**
	 * accepts te given visitor to visit this jvalue
	 */
	@Override
	public void accept(JValueVisitor v) {
		v.visitRecord(this);
	}

	/**
	 * Returns this Record as a String representation, { comp1=arg1, comp2=arg2,
	 * comp3=arg3 }
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(OPENING_PAREN);
		boolean first = true;
		for (Entry<String, JValue> e : dataMap.entrySet()) {
			if (!first) {
				sb.append(", ");
			}
			first = false;
			sb.append(e.getKey() + " = " + e.getValue().toString());
		}
		sb.append(CLOSING_PAREN);
		return sb.toString();
	}

	@Override
	public void sort() {
		// We use a TreeMap as backend here, so a record is always sorted by its
		// keys (compontent names)
	}
}
