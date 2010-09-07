/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 * 
 *               ist@uni-koblenz.de
 * 
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
