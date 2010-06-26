package de.uni_koblenz.jgralab.impl.savemem;

import java.util.HashMap;
import java.util.Map;

import de.uni_koblenz.jgralab.JGraLabCloneable;
import de.uni_koblenz.jgralab.JGraLabMap;

/**
 * FIXME This is a 1:1 clone of the code found in std.
 * 
 * @author
 * 
 * @param <K>
 * @param <V>
 */
public class JGraLabMapImpl<K, V> extends HashMap<K, V> implements
		JGraLabMap<K, V> {
	/**
	 * The generated id for serialization.
	 */
	private static final long serialVersionUID = -2183772013612864647L;

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
	public Object clone() {
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
