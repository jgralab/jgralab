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
package de.uni_koblenz.ist.utilities.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class StringListPreferences {
	protected ArrayList<String> entries;
	private Preferences prefs;
	private String key;

	public StringListPreferences(Preferences prefs, String key) {
		this.prefs = prefs;
		this.key = key;
	}

	public void setEntries(List<String> l) {
		entries.clear();
		entries.addAll(l);
		save();
	}

	public List<String> getEntries() {
		return Collections.unmodifiableList(entries);
	}

	public int size() {
		return entries.size();
	}

	public String get(int index) {
		return entries.get(index);
	}

	public boolean contains(String s) {
		return entries.contains(s);
	}

	public int indexOf(String s) {
		return entries.indexOf(s);
	}

	public void sort() {
		Collections.sort(entries);
		save();
	}

	public void add(int index, String s) {
		entries.add(index, s);
		save();
	}

	public void add(String s) {
		entries.add(s);
		save();
	}

	public void remove(String s) {
		while (entries.remove(s)) {
		}
		save();
	}

	public String remove(int index) {
		String s = entries.remove(index);
		save();
		return s;
	}

	public void clear() {
		entries.clear();
		save();
	}

	public void save() {
		int n = 0;
		while (n < entries.size()) {
			prefs.put(key + n, entries.get(n));
			++n;
		}
		while (prefs.get(key + n, null) != null) {
			prefs.remove(key + n);
			++n;
		}
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
		}
	}

	public void load(int maxEntries) {
		entries = new ArrayList<>();
		for (int n = 0; n < maxEntries; ++n) {
			String s = prefs.get(key + n, null);
			if (s == null) {
				break;
			}
			entries.add(s);
		}
	}

	public void load() {
		load(Integer.MAX_VALUE);
	}
}
