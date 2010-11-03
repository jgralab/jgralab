package de.uni_koblenz.jgralab.impl.db;

import java.util.TreeMap;
import de.uni_koblenz.jgralab.GraphException;

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
	 * Creates and initializes a new <code>List</code>.
	 */
	protected GraphElementList() {
		this.version = 0;
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

	protected long getRegularSequenceNumberBeforeFirstElementOf(TreeMap<Long, ?> map) {
		assert !minBorderOfNumberSpaceReached(map);
		if (!map.isEmpty())
			return map.firstKey() - SequenceNumber.REGULAR_DISTANCE;
		else
			return SequenceNumber.DEFAULT_START_SEQUENCE_NUMBER;
	}

	protected void assureThatElementCanBePrepended(TreeMap<Long, ?> map) {
		if (!this.isEmpty() && minBorderOfNumberSpaceReached(map))
			this.reorganize();
	}

	protected long getRegularSequenceNumberBehindLastElementOf(
			TreeMap<Long, ?> map) {
		assert !maxBorderOfNumberSpaceReached(map);
		if (!map.isEmpty())
			return map.lastKey() + SequenceNumber.REGULAR_DISTANCE;
		else
			return SequenceNumber.DEFAULT_START_SEQUENCE_NUMBER;
	}

	protected void assureThatElementCanBeAppended(TreeMap<Long, ?> map) {
		if (!this.isEmpty() && maxBorderOfNumberSpaceReached(map))
			this.reorganize();
	}

	protected static boolean minBorderOfNumberSpaceReached(TreeMap<Long, ?> map) {
		return map.firstKey() < SequenceNumber.MIN_BORDER_OF_NUMBER_SPACE;
	}

	protected static boolean maxBorderOfNumberSpaceReached(TreeMap<Long, ?> map) {
		return map.lastKey() > SequenceNumber.MAX_BORDER_OF_NUMBER_SPACE;
	}

	protected long getPrevFreeSequenceNumber(TreeMap<Long, ?> map,	long sequenceNumber) {
		if (map.firstKey() == sequenceNumber)
			return sequenceNumber - SequenceNumber.REGULAR_DISTANCE;
		// lowerKey can return null if sequenceNumber is already lowest one == first element
		long prevTakenSequenceNumber = map.lowerKey(sequenceNumber);
		long distance = sequenceNumber - prevTakenSequenceNumber;
		assert distance > 0;
		if (distance > 3)
			return prevTakenSequenceNumber + distance / 2;
		else if (distance > 1)
			return prevTakenSequenceNumber + 1;
		else if (distance == 1) {
			this.reorganize();
			return this.getPrevFreeSequenceNumber(map, sequenceNumber);
		} else if (distance == 0)
			throw new GraphException("Two elements have same sequence number.");
		else
			throw new GraphException(
					"Distance of two elements cannot be negative.");
	}

	protected long getNextFreeSequenceNumber(TreeMap<Long, ?> map, long sequenceNumber) {
		if (map.lastKey() == sequenceNumber)
			return sequenceNumber + SequenceNumber.REGULAR_DISTANCE;
		long nextTakenSequenceNumber = map.higherKey(sequenceNumber);
		long distance = nextTakenSequenceNumber - sequenceNumber;
		assert distance > 0;
		if (distance > 3)
			return nextTakenSequenceNumber - distance / 2;
		else if (distance > 1)
			return nextTakenSequenceNumber - 1;
		else if (distance == 1) {
			this.reorganize();
			return this.getNextFreeSequenceNumber(map, sequenceNumber);
		} else if (distance == 0)
			throw new GraphException("Two elements have same sequence number.");
		else
			throw new GraphException(
					"Distance of two elements cannot be negative.");
	}
}
