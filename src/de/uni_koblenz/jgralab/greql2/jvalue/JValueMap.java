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

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

/**
 * @author Tassilo Horn <horn@uni-koblenz.de>
 *
 */
public class JValueMap extends JValue {

	private HashMap<JValue, JValue> map;

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
	 * @return a set of all entries of this map
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
	 * @param k
	 *            the key
	 * @param v
	 *            the value mapped by k
	 * @return the previous value associated with the given key <code>k</code>
	 *         or <code>null</code> if there was no association for that key (or
	 *         the key had the value <code>null</code> before).
	 */
	public JValue put(JValue k, JValue v) {
		storedHashCode = 0;
		return map.put(k, v);
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
			sb.append(k.toString());
			sb.append(" ==> ");
			sb.append(map.get(k).toString());
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
}
