/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2012 Institute for Software Technology
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
package de.uni_koblenz.jgralab.impl;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.pcollections.ArrayPSet;
import org.pcollections.PMap;

import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.NoSuchAttributeException;

public class RecordImpl implements de.uni_koblenz.jgralab.Record {
	private PMap<String, Object> entries;

	private RecordImpl() {
		entries = JGraLab.map();
	}

	private RecordImpl(PMap<String, Object> m) {
		entries = m;
	}

	private static RecordImpl empty = new RecordImpl();

	public static RecordImpl empty() {
		return empty;
	}

	public RecordImpl plus(String name, Object value) {
		return new RecordImpl(entries.plus(name, value));
	}

	@Override
	public Object getComponent(String name) {
		if (entries.containsKey(name)) {
			return entries.get(name);
		}
		throw new NoSuchAttributeException(
				"Record doesn't contain a component '" + name + "'");
	}

	@Override
	public void writeComponentValues(GraphIO io) throws IOException,
			GraphIOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasComponent(String name) {
		return entries.containsKey(name);
	}

	@Override
	public List<String> getComponentNames() {
		return ((ArrayPSet<String>) entries.keySet()).toPVector();
	}

	@Override
	public int size() {
		return entries.size();
	}

	@Override
	public int hashCode() {
		int h = 0;
		for (Object v : entries.values()) {
			h += v.hashCode();
		}
		return h;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof RecordImpl) {
			return entries.equals(((RecordImpl) obj).entries);
		}
		if (obj instanceof de.uni_koblenz.jgralab.Record) {
			de.uni_koblenz.jgralab.Record r = (de.uni_koblenz.jgralab.Record) obj;
			if (size() != r.size()) {
				return false;
			}
			try {
				Iterator<Object> v = entries.values().iterator();
				for (String k : entries.keySet()) {
					if (!r.getComponent(k).equals(v.next())) {
						return false;
					}
				}
				return true;
			} catch (NoSuchAttributeException e) {
				return false;
			}
		}
		return false;
	}

	@Override
	public PMap<String, Object> toPMap() {
		return entries;
	}
}
