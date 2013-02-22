package de.uni_koblenz.jgralab.impl.diskv2.cache;

import java.lang.ref.ReferenceQueue;
import java.util.NoSuchElementException;

import de.uni_koblenz.jgralab.impl.GraphElementImpl;
import de.uni_koblenz.jgralab.impl.diskv2.DiskStorageManager;
import de.uni_koblenz.jgralab.impl.diskv2.Tracker;

public abstract class ElementCache<T extends GraphElementImpl<?, ?>> {
	private static final int INITIAL_ENTRIES = 1024;
	private static final double DEFAULT_LOAD_FACTOR = 0.7;
	private static final int MAX_ENTRIES = 2 * 1024 * 1024;
	private CacheEntry<T>[] entries;
	private int entryCount;
	private int maxLoad;
	private int size;
	protected ReferenceQueue<T> refQueue;
	private double loadFactor;
	protected DiskStorageManager dsm;

	public ElementCache(DiskStorageManager dsm) {
		this(dsm, INITIAL_ENTRIES, DEFAULT_LOAD_FACTOR);
	}

	public ElementCache(DiskStorageManager dsm, int initialEntries) {
		this(dsm, initialEntries, DEFAULT_LOAD_FACTOR);
	}

	public ElementCache(DiskStorageManager dsm, int initialEntries,
			double loadFactor) {
		this.dsm = dsm;
		this.entryCount = initialEntries;
		this.loadFactor = loadFactor;
		this.maxLoad = (int) (initialEntries * loadFactor);
		refQueue = new ReferenceQueue<>();
		@SuppressWarnings("unchecked")
		CacheEntry<T>[] cacheEntries = new CacheEntry[entryCount];
		entries = cacheEntries;
		size = 0;
	}

	public final void add(T element) {
		// System.out.println(getClass().getSimpleName() + ": add(" + element
		// + ") " + size);
		cleanup();
		if (size > maxLoad && entryCount < MAX_ENTRIES) {
			rehash();
		}
		CacheEntry<T> ce = createEntry(element);
		int bucket = hash(ce.id);
		ce.next = entries[bucket];
		entries[bucket] = ce;
		++size;
	}

	public final void delete(int id) {
		// System.out.println(getClass().getSimpleName() + ": delete(" + id +
		// ") "
		// + size);
		// remove *all* cache entries for the specified id
		// without save to disk because element was deleted
		int bucket = hash(id);
		CacheEntry<T> prev = null;
		CacheEntry<T> curr = entries[bucket];
		while (curr != null) {
			if (curr.id == id) {
				curr = curr.next;
				if (prev == null) {
					entries[bucket] = curr;
				} else {
					prev.next = curr;
				}
				--size;
			} else {
				prev = curr;
				curr = curr.next;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private final void cleanup() {
		CacheEntry<T> entry = (CacheEntry<T>) refQueue.poll();
		if (entry == null) {
			return;
		}
		// System.out.println(getClass().getSimpleName() + ": cleanup() " +
		// size);
		while (entry != null) {
			if (entry.tracker != null) {
				// System.out.println(getClass().getSimpleName()
				// + ": clean+writeElementToDisk(" + entry.id + ")");
				writeElementToDisk(entry.id, entry.tracker);
				entry.tracker = null;
			} else {
				// System.out.println(getClass().getSimpleName() + ": clean("
				// + entry.id + ")");
			}
			int bucket = hash(entry.id);
			CacheEntry<T> prev = null;
			CacheEntry<T> curr = entries[bucket];
			while (curr != null) {
				if (curr == entry) {
					if (prev == null) {
						entries[bucket] = curr;
					} else {
						prev.next = curr;
					}
					--size;
					break;
				} else {
					prev = curr;
					curr = curr.next;
				}
			}
			entry = (CacheEntry<T>) refQueue.poll();
		}
		// System.out.println(getClass().getSimpleName() + ": after cleanup() "
		// + size);
	}

	public final T get(int id) {
		assert id > 0;
		cleanup();
		CacheEntry<T> entry = getEntry(id);
		if (entry == null) {
			throw new NoSuchElementException(getClass().getSimpleName()
					+ ": Element " + id + " not in this "
					+ getClass().getSimpleName());
		}
		T element = entry.get();
		if (element == null) {
			// element was discarded by GC
			if (entry.tracker != null) {
				// element was not yet written to disk
				// System.out.println(getClass().getSimpleName()
				// + ": writeElementToDisk(" + curr.id + ")");
				writeElementToDisk(id, entry.tracker);
				entry.tracker = null;
			}
			// restore element from disk
			// System.out.println(getClass().getSimpleName()
			// + ": readElementFromDisk(" + id + ")");
			element = readElementFromDisk(id);
			// and create new cache entry and put it in front of bucket to
			// assure that is it found first
			entry = createEntry(element);
			int bucket = hash(entry.id);
			entry.next = entries[bucket];
			entries[bucket] = entry;
			++size;
		}
		return element;
	}

	private CacheEntry<T> getEntry(int id) {
		int bucket = hash(id);
		CacheEntry<T> curr = entries[bucket];

		while (curr != null && curr.id != id) {
			curr = curr.next;
		}
		return curr;
	}

	private final int hash(int id) {
		return (id * 31) % entryCount;
	}

	private final void rehash() {
		// System.out.println(getClass().getSimpleName() + ": rehash()");
		if (entryCount == MAX_ENTRIES) {
			maxLoad = MAX_ENTRIES;
			return;
		}
		int oldCapacity = entryCount;
		entryCount = (entryCount < MAX_ENTRIES) ? entryCount *= 2 : MAX_ENTRIES;
		maxLoad = (int) (entryCount * loadFactor);

		@SuppressWarnings("unchecked")
		CacheEntry<T>[] newEntries = new CacheEntry[entryCount];

		for (int i = 0; i < oldCapacity; i++) {
			CacheEntry<T> ce = entries[i];
			if (ce == null) {
				continue;
			}
			entries[i] = null;
			// reverse order of entries in the bucket "in place"
			// because re-adding must ensure the same relative order
			CacheEntry<T> curr = ce.next;
			ce.next = null;
			while (curr != null) {
				CacheEntry<T> next = curr.next;
				curr.next = ce;
				ce = curr;
				curr = next;
			}
			// add the entries to their new buckets
			while (ce != null) {
				CacheEntry<T> next = ce.next;
				int bucket = hash(ce.id);
				ce.next = newEntries[bucket];
				newEntries[bucket] = ce;
				ce = next;
			}
		}
		entries = newEntries;
		// System.out.println(getClass().getSimpleName()
		// + ": after rehash() entries=" + entryCount + ", size=" + size);
	}

	public final Tracker<T> getTracker(int id) {
		CacheEntry<T> entry = getEntry(id);
		return entry == null ? null : entry.getOrCreateTracker();
	}

	protected abstract CacheEntry<T> createEntry(T element);

	protected abstract T readElementFromDisk(int id);

	protected abstract void writeElementToDisk(int id, Tracker<T> tracker);
}
