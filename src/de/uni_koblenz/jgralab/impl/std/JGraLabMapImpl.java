package de.uni_koblenz.jgralab.impl.std;

import java.util.HashMap;
import java.util.Map;

import de.uni_koblenz.jgralab.JGraLabCloneable;
import de.uni_koblenz.jgralab.JGraLabMap;

/**
 * 
 * @author
 * 
 * @param <K>
 * @param <V>
 */
public class JGraLabMapImpl<K, V> extends HashMap<K, V> implements
		JGraLabMap<K, V> {

	private static final long serialVersionUID = 7484092853864016267L;

	public JGraLabMapImpl(Map<? extends K, ? extends V> map) {
		super(map);
	}

	public JGraLabMapImpl(int initialCapacity) {
		super(initialCapacity);
	}

	public JGraLabMapImpl() {
		super();
	}

	public JGraLabMapImpl(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	@SuppressWarnings("unchecked")
	@Override
	public JGraLabMapImpl<K, V> clone() {
		JGraLabMapImpl<K, V> copy = new JGraLabMapImpl<K, V>();
		for (java.util.Map.Entry<K, V> entry : entrySet()) {
			K keyClone = null;
			V valueClone = null;
			// clone key
			if (entry.getKey() instanceof JGraLabCloneable) {
				keyClone = (K) ((JGraLabCloneable) entry.getKey()).clone();
			} else {
				keyClone = entry.getKey();
			}
			// clone value
			if (entry.getValue() instanceof JGraLabCloneable) {
				valueClone = (V) ((JGraLabCloneable) entry.getValue()).clone();
			} else {
				valueClone = entry.getValue();
			}
			copy.put(keyClone, valueClone);
		}
		return copy;
	}
}
