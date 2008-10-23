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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
		dataMap = new HashMap<String, JValue>();
	}

	/**
	 * creates a new JValueREcord which contains all elements in the given
	 * collection
	 */
	public JValueRecord(JValueCollection collection) {
		super();
		this.type = JValueType.RECORD;
		dataMap = new HashMap<String, JValue>();
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

	/**
	 * Returns the hash code value for this record. To get the hash code of this
	 * record, new hash code values for every element of this record are
	 * calculated from a polynomial of 3rd order and finally summed up. This
	 * ensures that <code>s1.equals(s2)</code> implies that
	 * <code>s1.hashCode()==s2.hashCode()</code> for any two record
	 * <code>s1</code> and <code>s2</code>, as required by the general
	 * contract of <code>Object.hashCode()</code>.
	 * 
	 * @return the hash code value for this record.
	 */
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
	public JValueRecord toJValueRecord() {
		return this;
	}

	/**
	 * returns a JValueRecord-Reference to this object
	 */
	public boolean isJValueRecord() {
		return true;
	}

	/**
	 * @return the number of elements in this record
	 */
	public int size() {
		return dataMap.size();
	}

	/**
	 * inherited from JValueCollection, returns false because in a record its
	 * not possible to add an element without an id
	 * 
	 * @return false
	 */
	public boolean add(JValue value) {
		return false;
	}

	/**
	 * removes all elements from this record
	 */
	public void clear() {
		dataMap.clear();
	}

	/**
	 * Checks if this record contains the given value
	 * 
	 * @return true if the record contains the given value, false otherwise
	 */
	public boolean contains(JValue value) {
		return dataMap.containsValue(value);
	}

	/**
	 * Checks if this record is empty
	 * 
	 * @return true if the record contains no elements, false otherwise
	 */
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
	public boolean remove(JValue value) {
		return removeValue(value);
	}

	/**
	 * Returns an Iterator to iterate over all elements
	 */
	public Iterator<JValue> iterator() {
		Collection<JValue> values = dataMap.values();
		return values.iterator();
	}

	/**
	 * Replaces the given first value with the given second value. Beware, this
	 * method does not scale well
	 * 
	 * @return if valueToReplace was successfull replaced, false if it could not
	 *         be found in this record
	 */
	public boolean replace(JValue valueToReplace, JValue replacement) {
		Set<String> keySet = dataMap.keySet();
		Iterator<String> keyIter = keySet.iterator();
		while (keyIter.hasNext()) {
			String key = keyIter.next();
			if (dataMap.get(key) == valueToReplace) {
				dataMap.remove(key);
				dataMap.put(key, replacement);
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds the given value with the given key as id to this record
	 * 
	 * @return true if the element was added successfull, false if it could not
	 *         be added, maybe because of a duplicated key
	 */
	public boolean add(String key, JValue value) {
		if (dataMap.containsKey(key))
			return false;
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
		if (!dataMap.containsKey(key))
			return false;
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
		if (key instanceof JValue) {
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
	public void accept(JValueVisitor v) {
		v.visitRecord(this);
	}
}
