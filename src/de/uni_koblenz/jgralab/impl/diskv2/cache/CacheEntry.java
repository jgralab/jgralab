package de.uni_koblenz.jgralab.impl.diskv2.cache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

import de.uni_koblenz.jgralab.impl.GraphElementImpl;
import de.uni_koblenz.jgralab.impl.diskv2.Tracker;

/**
 * Entry that can be stored in the MemStorageManager's cache.
 * 
 * @author aheld
 * 
 */

abstract class CacheEntry<T extends GraphElementImpl<?, ?>> extends
		SoftReference<T> {
	/**
	 * the key of this entry
	 */
	int id;

	/**
	 * Tracks the changed attributes of the referenced object
	 */
	Tracker<T> tracker;

	CacheEntry<T> next;

	/**
	 * Creates a new CacheEntry object that softly references the given value
	 * and is associated with a ReferenceQueue.
	 * 
	 * @param value
	 *            The object referenced by the entry
	 * 
	 * @param refQueue
	 *            The queue the referenence will be put into when the referenced
	 *            object is deleted by the Garbage Collector
	 */
	CacheEntry(T value, ReferenceQueue<T> refQueue) {
		super(value, refQueue);
		id = value.getId();
	}

	/**
	 * Returns the tracker. If the tracker hasn't been created yet, a new one is
	 * created and returned. Only call this method if this CacheEntry references
	 * a GraphElement.
	 * 
	 * @return @link{tracker}
	 */
	Tracker<T> getOrCreateTracker() {
		if (tracker == null) {
			T element = get();
			if (element == null) {
				return null;
			}
			tracker = createTracker();
			tracker.storeAttributes(element);
		}
		return tracker;
	}

	public Tracker<T> getTracker() {
		return tracker;
	}

	protected abstract Tracker<T> createTracker();
}
