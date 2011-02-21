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
package de.uni_koblenz.jgralab.impl.db;

import java.util.TreeMap;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.GraphException;

/**
 * Client-sided reorganizer of sorted collections with <code>long</code> keys
 * emulating lists in a graph. Works on internal representation of lists to keep
 * all updates on client side.
 * 
 * @author ultbreit@uni-koblenz.de
 * 
 * @param <V>
 *            Type of values in sorted collection.
 */
public abstract class Reorganizer<V> {

	protected GraphImpl graph;

	public Reorganizer(GraphImpl graph) {
		this.graph = graph;
	}

	public TreeMap<Long, V> getReorganisedMap(TreeMap<Long, V> mapToReorganize)
			throws Exception {
		assert this.graph != null;
		assert !mapToReorganize.isEmpty();
		Entry<Long, V> element = this.getCentralElement(mapToReorganize);
		TreeMap<Long, V> reorganizedSequence = new TreeMap<Long, V>();
		long newKey = 0;
		while (element != null) {
			long removedElementKey = element.getKey();
			mapToReorganize.remove(removedElementKey);
			reorganizedSequence.put(newKey, element.getValue());
			this.updateCachedElement(element.getValue(), newKey);
			newKey += SequenceNumber.REGULAR_DISTANCE;
			element = mapToReorganize.higherEntry(removedElementKey);
		}
		element = mapToReorganize.lastEntry();
		newKey = 0 - SequenceNumber.REGULAR_DISTANCE;
		while (element != null) {
			long removedElementKey = element.getKey();
			mapToReorganize.remove(removedElementKey);
			reorganizedSequence.put(newKey, element.getValue());
			// graph.updateVertexSequenceNumberInCache(element.getValue(),
			// newKey);
			newKey -= SequenceNumber.REGULAR_DISTANCE;
			element = mapToReorganize.lowerEntry(removedElementKey);
		}
		if (mapToReorganize.isEmpty()) {
			return reorganizedSequence;
		} else {
			throw new GraphException(
					"List could not be completely reorganized.");
		}
	}

	private Entry<Long, V> getCentralElement(TreeMap<Long, V> map) {
		// TODO Assert if a reorganisation is possible
		Long firstKey = map.firstKey();
		Long lastKey = map.lastKey();
		// TODO Check for SequenceNumber.MIN_BORDER
		Long distance = Math.abs(firstKey) + Math.abs(lastKey);
		Long centralKey = firstKey + distance / 2;
		return map.higherEntry(centralKey);
	}

	protected abstract void updateCachedElement(V id, long newKey);
}
