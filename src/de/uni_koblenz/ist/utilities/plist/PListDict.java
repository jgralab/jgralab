/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class PListDict {
	Map<String, Object> map;

	public PListDict() {
		map = new TreeMap<>();
	}

	public int size() {
		return map.size();
	}

	public Set<Entry<String, Object>> entrySet() {
		return map.entrySet();
	}

	public void put(String key, Object value) {
		map.put(key, value);
	}

	public void remove(String key) {
		map.remove(key);
	}

	public Object get(String key) {
		return map.get(key);
	}

	public boolean containsKey(String key) {
		return map.containsKey(key);
	}

	public boolean getBoolean(String key) {
		return (((Boolean) (map.get(key))).booleanValue());
	}

	public boolean getBoolean(String key, boolean v) {
		return map.containsKey(key) ? (((Boolean) (map.get(key)))
				.booleanValue()) : v;
	}

	public int getInteger(String key) {
		return (((Integer) (map.get(key))).intValue());
	}

	public int getInteger(String key, int v) {
		return map.containsKey(key) ? (((Integer) (map.get(key))).intValue())
				: v;
	}

	public double getDouble(String key) {
		return (((Double) (map.get(key))).doubleValue());
	}

	public double getDouble(String key, double v) {
		return map.containsKey(key) ? (((Double) (map.get(key))).doubleValue())
				: v;
	}

	public Date getDate(String key) {
		return (Date) (map.get(key));
	}

	public Date getDate(String key, Date v) {
		return map.containsKey(key) ? (Date) (map.get(key)) : v;
	}

	public void putBoolean(String key, boolean value) {
		map.put(key, value);
	}

	public void putInteger(String key, int value) {
		map.put(key, value);
	}

	public void putDouble(String key, double value) {
		map.put(key, value);
	}

	public void putDate(String key, Date value) {
		map.put(key, value);
	}

	public void putString(String key, String value) {
		map.put(key, value);
	}

	public PListDict getDict(String key) {
		return (PListDict) (map.get(key));
	}

	public void putDict(String key, PListDict value) {
		map.put(key, value);
	}

	@SuppressWarnings("unchecked")
	public <E> List<E> getArray(String key) {
		return (List<E>) (map.get(key));
	}

	@SuppressWarnings("unchecked")
	public <E> List<E> getArray(String key, List<E> v) {
		return map.containsKey(key) ? (List<E>) (map.get(key)) : v;
	}

	public <E> void putArray(String key, List<E> value) {
		map.put(key, value);
	}

	public String getString(String key) {
		return (String) (map.get(key));
	}

	public String getString(String key, String v) {
		return map.containsKey(key) ? (String) (map.get(key)) : v;
	}

	public Collection<Object> values() {
		return map.values();
	}
}
