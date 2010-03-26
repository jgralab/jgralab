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

package de.uni_koblenz.jgralab.greql2.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.exception.DuplicateVariableException;

public class EasySymbolTable {

	protected LinkedList<HashMap<String, Vertex>> list = null;

	public EasySymbolTable() {
		list = new LinkedList<HashMap<String, Vertex>>();
	}

	public void blockBegin() {
		HashMap<String, Vertex> map = new HashMap<String, Vertex>();
		list.addFirst(map);
	}

	public void blockEnd() {
		if (!list.isEmpty())
			list.removeFirst();

	}

	public void insert(String ident, Vertex v)
			throws DuplicateVariableException {
		if (list.getFirst().get(ident) == null) {
			list.getFirst().put(ident, v);
		} 
	}

	public Vertex lookup(String ident) {
		for (HashMap<String, Vertex> keyMap : list) {
			if (keyMap.containsKey(ident))
				return keyMap.get(ident);
		}
		return null;
	}

	/**
	 * returns a set of known identifiers
	 * 
	 * @return
	 */
	public Set<String> getKnownIdentifierSet() {
		Set<String> result = new HashSet<String>();
		for (HashMap<String, Vertex> keyMap : list) {
			result.addAll(keyMap.keySet());
		}
		return result;
	}

}
