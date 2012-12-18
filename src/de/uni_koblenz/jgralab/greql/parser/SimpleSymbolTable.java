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

package de.uni_koblenz.jgralab.greql.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.exception.DuplicateVariableException;
import de.uni_koblenz.jgralab.greql.schema.GreqlAggregation;
import de.uni_koblenz.jgralab.greql.schema.SourcePosition;
import de.uni_koblenz.jgralab.greql.schema.Variable;
import de.uni_koblenz.jgralab.schema.RecordDomain;

public class SimpleSymbolTable {

	protected LinkedList<HashMap<String, Vertex>> list = null;

	public SimpleSymbolTable() {
		list = new LinkedList<HashMap<String, Vertex>>();
	}

	public void blockBegin() {
		HashMap<String, Vertex> map = new HashMap<String, Vertex>();
		list.addFirst(map);
	}

	public void blockEnd() {
		if (!list.isEmpty()) {
			list.removeFirst();
		}

	}

	public void insert(String ident, Vertex v)
			throws DuplicateVariableException {
		Vertex existingVariable = list.getFirst().get(ident);
		if (existingVariable == null) {
			list.getFirst().put(ident, v);
		} else {
			GreqlAggregation firstIncidence = (GreqlAggregation) existingVariable
					.getFirstIncidence(EdgeDirection.OUT);
			SourcePosition previousPosition = null;
			if (firstIncidence != null) {
				previousPosition = firstIncidence.get_sourcePositions().get(0);
			} else {
				List<RecordDomain> recordDomains = v.getSchema()
						.getRecordDomains();
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("length", -1);
				map.put("offset", -1);
				for (RecordDomain dom : recordDomains) {
					if (dom.getQualifiedName().equals("SourcePosition")) {
						previousPosition = (SourcePosition) v.getGraph()
								.createRecord(dom, map);
					}
				}
			}
			throw new DuplicateVariableException(
					((Variable) existingVariable).get_name(),
					(List<SourcePosition>) null, previousPosition);
		}
	}

	public Vertex lookup(String ident) {
		for (HashMap<String, Vertex> keyMap : list) {
			if (keyMap.containsKey(ident)) {
				return keyMap.get(ident);
			}
		}
		return null;
	}

	/**
	 * @return the set of known identifiers
	 */
	public Set<String> getKnownIdentifierSet() {
		Set<String> result = new HashSet<String>();
		for (HashMap<String, Vertex> keyMap : list) {
			result.addAll(keyMap.keySet());
		}
		return result;
	}

}
