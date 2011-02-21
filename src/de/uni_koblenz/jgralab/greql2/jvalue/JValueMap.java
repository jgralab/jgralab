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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * @author Tassilo Horn <horn@uni-koblenz.de>
 * 
 */
public class JValueMap extends JValueImpl {

	private Map<JValue, JValue> map;

	{
		type = JValueType.MAP;
	}

	public JValueMap(int initialCapacity) {
		map = new HashMap<JValue, JValue>(initialCapacity);
	}

	public JValueMap() {
		map = new HashMap<JValue, JValue>();
	}

	/**
	 * @return <code>true</code> if this map contains no elements,
	 *         <code>false</code> otherwise.
	 */
	public boolean isEmpty() {
		return map.isEmpty();
	}

	/**
	 * @param element
	 *            element whose presence in this map's key set should be tested
	 * @return <code>true</code> if this map contains the specified element as
	 *         key, <code>false</code> otherwise.
	 */
	public boolean containsKey(JValue element) {
		return map.containsKey(element);
	}

	/**
	 * @param element
	 *            element whose presence in this map's value set should be
	 *            tested
	 * @return <code>true</code> if this map contains the specified element as
	 *         value, <code>false</code> otherwise.
	 */
	public boolean containsValue(JValue element) {
		return map.containsValue(element);
	}

	/**
	 * @return the number of key-value mappings in this map
	 */
	public int size() {
		return map.size();
	}

	/**
	 * @see Map#clear()
	 */
	public void clear() {
		map.clear();
	}

	/**
	 * @return a set of all keys of this map
	 */
	public JValueSet keySet() {
		JValueSet set = new JValueSet();
		for (JValue k : map.keySet()) {
			set.add(k);
		}
		return set;
	}

	/**
	 * @return a set of all entries of this map as key-value tuples
	 */
	public JValueSet entrySetAsJValueTupleSet() {
		JValueSet result = new JValueSet();
		for (Entry<JValue, JValue> e : map.entrySet()) {
			JValueTuple tup = new JValueTuple(2);
			tup.add(e.getKey());
			tup.add(e.getValue());
			result.add(tup);
		}
		return result;
	}

	/**
	 * @return a set of all entries of this map as key-value tuples
	 */
	public Set<Entry<JValue, JValue>> entrySet() {
		return map.entrySet();
	}

	/**
	 * @return a bag of all values of this map
	 */
	public JValueBag values() {
		JValueBag bag = new JValueBag();
		for (JValue v : map.values()) {
			bag.add(v);
		}
		return bag;
	}

	/**
	 * @param key
	 *            the key to get its value
	 * @return the value mapped from the given key
	 */
	public JValue get(JValue key) {
		return map.get(key);
	}

	/**
	 * Adds the mapping k --&gt; v to the map. If k had a mapping with another
	 * value before, then the old mapping is replaced.
	 * 
	 * @param jkey
	 *            the key
	 * @param jval
	 *            the value mapped by k
	 * @return the previous value associated with the given key <code>k</code>
	 *         or <code>null</code> if there was no association for that key (or
	 *         the key had the value <code>null</code> before).
	 */
	public JValue put(JValue jkey, JValue jval) {
		storedHashCode = 0;
		return map.put(jkey, jval);
	}

	@Override
	public boolean isMap() {
		return true;
	}

	@Override
	public JValueMap toJValueMap() {
		return this;
	}

	@Override
	public void accept(JValueVisitor v) {
		v.visitMap(this);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		boolean first = true;
		for (JValue k : map.keySet()) {
			if (!first) {
				sb.append(", ");
			} else {
				first = false;
			}
			sb.append(k);
			sb.append(" ==> ");
			JValue val = map.get(k);
			if (val == null) {
				sb.append("null");
			} else {
				sb.append(val.toString());
			}
		}
		sb.append("}");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		if (storedHashCode == 0) {
			int elementHashCode = 0;
			int newHashCode = -1;

			for (JValue currentKey : map.keySet()) {
				elementHashCode = currentKey.hashCode()
						* map.get(currentKey).hashCode();
				newHashCode += -1 + (3 + elementHashCode)
						* (7 + elementHashCode) * (11 + elementHashCode);
			}
			newHashCode += this.getClass().hashCode();
			storedHashCode = newHashCode;
		}
		return storedHashCode;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof JValueMap)) {
			return false;
		}
		JValueMap other = (JValueMap) o;
		if (size() != other.size()) {
			return false;
		}
		for (JValue k : keySet()) {
			if (!other.containsKey(k)) {
				return false;
			}

			if (!other.get(k).equals(get(k))) {
				return false;
			}
		}
		return true;
	}

	public JValueMap merge(JValueMap other) {
		JValueSet allKeys = new JValueSet();
		allKeys.addAll(keySet());
		allKeys.addAll(other.keySet());
		JValueMap newMap = new JValueMap();
		for (JValue k : allKeys) {
			JValueCollection newValue = null;
			if (containsKey(k) && other.containsKey(k)) {
				newValue = JValueCollection
						.shallowCopy((JValueCollection) get(k));
				newValue.addAll((JValueCollection) other.get(k));
			} else if (containsKey(k)) {
				newValue = JValueCollection
						.shallowCopy((JValueCollection) get(k));
			} else {
				newValue = JValueCollection
						.shallowCopy((JValueCollection) other.get(k));
			}
			newMap.put(k, newValue);
		}
		return newMap;
	}

	/**
	 * Computes the union of this JValueMap and the <code>other</code>
	 * JValueMap. The key sets must be disjoint.
	 * 
	 * @param other
	 *            another JValueMap
	 * @param forceDisjointness
	 *            if true, the function will error if the keys of the maps are
	 *            not disjoint. Else, the values of the other will win.
	 * @return the union of the two maps
	 */
	public JValueMap union(JValueMap other, boolean forceDisjointness) {
		if (!forceDisjointness) {
			JValueMap ret = new JValueMap();
			ret.map.putAll(map);
			ret.map.putAll(other.map);
			return ret;
		}
		JValueSet allKeys = new JValueSet();
		allKeys.addAll(keySet());
		allKeys.addAll(other.keySet());
		JValueMap newMap = new JValueMap();
		for (JValue k : allKeys) {
			if (containsKey(k) && other.containsKey(k)) {
				throw new RuntimeException(
						"Cannot create the union of the given two maps. "
								+ "Their key sets are not disjoint.");
			}

			if (containsKey(k)) {
				newMap.put(k, get(k));
			} else {
				newMap.put(k, other.get(k));
			}
		}
		return newMap;
	}

	/**
	 * Sorts this map according the natural ordering of its keys.
	 * 
	 * This is done by replacing the underlying HashMap with a TreeMap.
	 */
	public void sort() {
		if (map instanceof TreeMap<?, ?>) {
			return;
		}
		map = new TreeMap<JValue, JValue>(map);
	}

	@Override
	public Object toObject() {
		HashMap<Object, Object> result = new HashMap<Object, Object>(map.size());
		for (Entry<JValue, JValue> e : map.entrySet()) {
			result.put(e.getKey().toObject(), e.getValue().toObject());
		}
		return result;
	}
}
