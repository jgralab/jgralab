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
