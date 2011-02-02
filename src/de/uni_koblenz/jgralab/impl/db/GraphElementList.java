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
	 * Sorted map, mapping sequence numbers of graph elements to their graph
	 * element ID.
	 */
	protected TreeMap<Long, Integer> sequenceNumberToIdMap;

	/**
	 * Bitset reflecting the value range of <code>vertexIdMap</code> for
	 * avoiding the call of <code>TreeSet.containsValue()</code>.
	 */
	protected BitSet usedIDs;

	/**
	 * Creates and initializes a new <code>List</code>.
	 */
	protected GraphElementList() {
		version = 0;
		sequenceNumberToIdMap = new TreeMap<Long, Integer>();
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

	public abstract T getFirst();

	public abstract T getLast();

	public abstract T getPrev(T element);

	public abstract T getNext(T element);

	public abstract void prepend(T element);

	public abstract void append(T element);

	public abstract void putAfter(T targetElement, T movedElement);

	public abstract void putBefore(T targetElement, T movedElement);

	public abstract boolean contains(T element);

	public abstract void remove(T element);

	/**
	 * Counts elements in list.
	 * 
	 * @return Count of elements in list.
	 */
	public abstract int size();

	/**
	 * Checks whether list is empty or not.
	 * 
	 * @return true if list is empty, false otherwise.
	 */
	public abstract boolean isEmpty();

	/**
	 * Reorganizes list.
	 */
	abstract void reorganize();

	/**
	 * Empties list.
	 */
	protected abstract void clear();

	protected long getRegularSequenceNumberBeforeFirstElementOf() {
		assert !minBorderOfNumberSpaceReached();
		if (!sequenceNumberToIdMap.isEmpty()) {
			return sequenceNumberToIdMap.firstKey()
					- SequenceNumber.REGULAR_DISTANCE;
		} else {
			return SequenceNumber.DEFAULT_START_SEQUENCE_NUMBER;
		}
	}

	protected void assureThatElementCanBePrepended() {
		if (!this.isEmpty() && minBorderOfNumberSpaceReached()) {
			this.reorganize();
		}
	}

	protected long getRegularSequenceNumberBehindLastElementOf() {
		assert !maxBorderOfNumberSpaceReached();
		if (!sequenceNumberToIdMap.isEmpty()) {
			return sequenceNumberToIdMap.lastKey()
					+ SequenceNumber.REGULAR_DISTANCE;
		} else {
			return SequenceNumber.DEFAULT_START_SEQUENCE_NUMBER;
		}
	}

	protected void assureThatElementCanBeAppended() {
		if (!this.isEmpty() && maxBorderOfNumberSpaceReached()) {
			this.reorganize();
		}
	}

	protected boolean minBorderOfNumberSpaceReached() {
		return sequenceNumberToIdMap.firstKey() < SequenceNumber.MIN_BORDER_OF_NUMBER_SPACE;
	}

	protected boolean maxBorderOfNumberSpaceReached() {
		return sequenceNumberToIdMap.lastKey() > SequenceNumber.MAX_BORDER_OF_NUMBER_SPACE;
	}

	protected long getPrevFreeSequenceNumber(long sequenceNumber) {
		if (sequenceNumberToIdMap.firstKey() == sequenceNumber) {
			return sequenceNumber - SequenceNumber.REGULAR_DISTANCE;
		}
		// lowerKey can return null if sequenceNumber is already lowest one ==
		// first element
		long prevTakenSequenceNumber = sequenceNumberToIdMap
				.lowerKey(sequenceNumber);
		long distance = sequenceNumber - prevTakenSequenceNumber;
		assert distance > 0;
		if (distance > 3) {
			return prevTakenSequenceNumber + distance / 2;
		} else if (distance > 1) {
			return prevTakenSequenceNumber + 1;
		} else if (distance == 1) {
			this.reorganize();
			return this.getPrevFreeSequenceNumber(sequenceNumber);
		} else if (distance == 0) {
			throw new GraphException("Two elements have same sequence number.");
		} else {
			throw new GraphException(
					"Distance of two elements cannot be negative.");
		}
	}

	protected long getNextFreeSequenceNumber(TreeMap<Long, ?> sequenceNumberToIdMap,
			long sequenceNumber) {
		if (sequenceNumberToIdMap.lastKey() == sequenceNumber) {
			return sequenceNumber + SequenceNumber.REGULAR_DISTANCE;
		}
		long nextTakenSequenceNumber = sequenceNumberToIdMap.higherKey(sequenceNumber);
		long distance = nextTakenSequenceNumber - sequenceNumber;
		assert distance > 0;
		if (distance > 3) {
			return nextTakenSequenceNumber - distance / 2;
		} else if (distance > 1) {
			return nextTakenSequenceNumber - 1;
		} else if (distance == 1) {
			this.reorganize();
			return this.getNextFreeSequenceNumber(sequenceNumberToIdMap, sequenceNumber);
		} else if (distance == 0) {
			throw new GraphException("Two elements have same sequence number.");
		} else {
			throw new GraphException(
					"Distance of two elements cannot be negative.");
		}
	}
}
