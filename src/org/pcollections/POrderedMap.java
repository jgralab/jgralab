package org.pcollections;

/**
 * An interface for PMap implementations that preserve the insertion order
 * of entries.
 *
 * @author ist@uni-koblenz.de
 *
 */
public interface POrderedMap<K, V> extends PMap<K, V> {

	public K keyAt(int idx);
	public V valueAt(int idx);
	public Entry<K,V> entryAt(int idx);
}
