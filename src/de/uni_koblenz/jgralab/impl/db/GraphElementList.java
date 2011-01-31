/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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

import java.util.BitSet;
import java.util.TreeMap;
import de.uni_koblenz.jgralab.GraphException;

/* TODO refactor this class:
 * - move tree set from specializations to this class
 * - move all common methods, accessing the tree set, to this class 
 */

// TODO improve this class (after refactoring it). Is the TreeSet really required?

/**
 * Aggregates common attributes and methods for lists in graphs. A list has a
 * version and maps it's elements sequence on sequence numbers.
 * 
 * @author ultbreit@uni-koblenz.de
 * 
 * @param <T>
 *            Type of elements in list.
 */
public abstract class GraphElementList<T> {

	/**
	 * Version of list.
	 */
	protected long version;

	/**
	 * Bitset reflecting the value range of <code>vertexIdMap</code> for
	 * avoiding the call of <code>TreeSet.containsValue()</code>.
	 */
	protected BitSet usedIDs;

	/**
	 * Creates and initializes a new <code>List</code>.
	 */
	protected GraphElementList() {
		this.version = 0;
		usedIDs = new BitSet();
	}

	/**
	 * Gets version of list.
	 * 
	 * @return Version of list.
	 */
	public long getVersion() {
		return this.version;
	}

	/**
	 * Sets version of list.
	 * 
	 * @param version
	 *            Version list should have.
	 */
	public void setVersion(long version) {
		if (this.version != version) {
			this.version = version;
			this.updateVersion(version);
		}
	}

	/**
	 * Signals list that is has been modified.
	 */
	public void modified() {
		this.setVersion(this.version + 1);
	}

	/**
	 * Updates version of list.
	 * 
	 * @param version
	 *            Version of list.
	 */
	protected abstract void updateVersion(long version);

	abstract T getFirst();

	abstract T getLast();

	abstract T getPrev(T element);

	abstract T getNext(T element);

	abstract void prepend(T element);

	abstract void append(T element);

	abstract void putAfter(T targetElement, T movedElement);

	abstract void putBefore(T targetElement, T movedElement);

	abstract boolean contains(T element);

	abstract void remove(T element);

	/**
	 * Counts elements in list.
	 * 
	 * @return Count of elements in list.
	 */
	abstract int size();

	/**
	 * Checks whether list is empty or not.
	 * 
	 * @return true if list is empty, false otherwise.
	 */
	abstract boolean isEmpty();

	/**
	 * Reorganizes list.
	 */
	abstract void reorganize();

	/**
	 * Empties list.
	 */
	abstract void clear();

	protected long getRegularSequenceNumberBeforeFirstElementOf(
			TreeMap<Long, ?> map) {
		assert !minBorderOfNumberSpaceReached(map);
		if (!map.isEmpty()) {
			return map.firstKey() - SequenceNumber.REGULAR_DISTANCE;
		} else {
			return SequenceNumber.DEFAULT_START_SEQUENCE_NUMBER;
		}
	}

	protected void assureThatElementCanBePrepended(TreeMap<Long, ?> map) {
		if (!this.isEmpty() && minBorderOfNumberSpaceReached(map)) {
			this.reorganize();
		}
	}

	protected long getRegularSequenceNumberBehindLastElementOf(
			TreeMap<Long, ?> map) {
		assert !maxBorderOfNumberSpaceReached(map);
		if (!map.isEmpty()) {
			return map.lastKey() + SequenceNumber.REGULAR_DISTANCE;
		} else {
			return SequenceNumber.DEFAULT_START_SEQUENCE_NUMBER;
		}
	}

	protected void assureThatElementCanBeAppended(TreeMap<Long, ?> map) {
		if (!this.isEmpty() && maxBorderOfNumberSpaceReached(map)) {
			this.reorganize();
		}
	}

	protected static boolean minBorderOfNumberSpaceReached(TreeMap<Long, ?> map) {
		return map.firstKey() < SequenceNumber.MIN_BORDER_OF_NUMBER_SPACE;
	}

	protected static boolean maxBorderOfNumberSpaceReached(TreeMap<Long, ?> map) {
		return map.lastKey() > SequenceNumber.MAX_BORDER_OF_NUMBER_SPACE;
	}

	protected long getPrevFreeSequenceNumber(TreeMap<Long, ?> map,
			long sequenceNumber) {
		if (map.firstKey() == sequenceNumber) {
			return sequenceNumber - SequenceNumber.REGULAR_DISTANCE;
		}
		// lowerKey can return null if sequenceNumber is already lowest one ==
		// first element
		long prevTakenSequenceNumber = map.lowerKey(sequenceNumber);
		long distance = sequenceNumber - prevTakenSequenceNumber;
		assert distance > 0;
		if (distance > 3) {
			return prevTakenSequenceNumber + distance / 2;
		} else if (distance > 1) {
			return prevTakenSequenceNumber + 1;
		} else if (distance == 1) {
			this.reorganize();
			return this.getPrevFreeSequenceNumber(map, sequenceNumber);
		} else if (distance == 0) {
			throw new GraphException("Two elements have same sequence number.");
		} else {
			throw new GraphException(
					"Distance of two elements cannot be negative.");
		}
	}

	protected long getNextFreeSequenceNumber(TreeMap<Long, ?> map,
			long sequenceNumber) {
		if (map.lastKey() == sequenceNumber) {
			return sequenceNumber + SequenceNumber.REGULAR_DISTANCE;
		}
		long nextTakenSequenceNumber = map.higherKey(sequenceNumber);
		long distance = nextTakenSequenceNumber - sequenceNumber;
		assert distance > 0;
		if (distance > 3) {
			return nextTakenSequenceNumber - distance / 2;
		} else if (distance > 1) {
			return nextTakenSequenceNumber - 1;
		} else if (distance == 1) {
			this.reorganize();
			return this.getNextFreeSequenceNumber(map, sequenceNumber);
		} else if (distance == 0) {
			throw new GraphException("Two elements have same sequence number.");
		} else {
			throw new GraphException(
					"Distance of two elements cannot be negative.");
		}
	}
}
