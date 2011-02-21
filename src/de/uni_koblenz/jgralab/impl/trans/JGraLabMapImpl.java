/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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
package de.uni_koblenz.jgralab.impl.trans;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.JGraLabMap;
import de.uni_koblenz.jgralab.trans.JGraLabTransactionCloneable;

/**
 * Own implementation class for attributes of type
 * <code>java.util.Map<K,V></code>.
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 * 
 * @param <K>
 * @param <V>
 * 
 */
public class JGraLabMapImpl<K, V> extends HashMap<K, V> implements
		JGraLabTransactionCloneable, JGraLabMap<K, V> {
	private static final long serialVersionUID = -9198046937053316052L;
	private transient VersionedJGraLabCloneableImpl<JGraLabMapImpl<K, V>> versionedMap;
	private transient Graph graph;
	private transient String name;

	/**
	 * 
	 */
	protected JGraLabMapImpl() {
		super();
	}

	/**
	 * 
	 * @param initialSize
	 */
	protected JGraLabMapImpl(int initialSize) {
		super(initialSize);
	}

	/**
	 * 
	 * @param initialSize
	 * @param loadFactor
	 */
	protected JGraLabMapImpl(int initialSize, float loadFactor) {
		super(initialSize, loadFactor);
	}

	/**
	 * 
	 * @param map
	 * 
	 *            visibility set to public (afuhr)
	 * 
	 *            TODO ask why it must be public ;-)
	 */
	protected JGraLabMapImpl(Map<K, V> map) {
		super(map);
	}

	/**
	 * 
	 * @param g
	 */
	protected JGraLabMapImpl(Graph g) {
		super();
		init(g);
	}

	/**
	 * 
	 * @param g
	 * @param map
	 */
	protected JGraLabMapImpl(Graph g, Map<? extends K, ? extends V> map) {
		super(map);
		init(g);
	}

	/**
	 * 
	 * @param g
	 * @param initialCapacity
	 */
	protected JGraLabMapImpl(Graph g, int initialCapacity) {
		super(initialCapacity);
		init(g);
	}

	/**
	 * 
	 * @param g
	 * @param initialCapacity
	 * @param loadFactor
	 */
	protected JGraLabMapImpl(Graph g, int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
		init(g);
	}

	/**
	 * 
	 * @param g
	 */
	protected void init(Graph g) {
		if (g == null) {
			throw new GraphException("Given graph cannot be null.");
		}
		if (!g.hasTransactionSupport()) {
			throw new GraphException(
					"An instance of JGraLabSet can only be created for graphs with transaction support");
		}
		graph = g;
		if (graph.isLoading()) {
			versionedMap = new VersionedJGraLabCloneableImpl<JGraLabMapImpl<K, V>>(
					g, this);
		}
		if (versionedMap == null) {
			versionedMap = new VersionedJGraLabCloneableImpl<JGraLabMapImpl<K, V>>(
					graph);
		}
		versionedMap.setValidValue(this, g.getCurrentTransaction());
	}

	/**
	 * Checks whether the added value support transactions (in the case of List,
	 * Set and Map) and belongs to the same graph.
	 * 
	 * @param value
	 */
	private void isValidValueCheck(V value) {
		if (((value instanceof Map<?, ?>) || (value instanceof List<?>) || (value instanceof Set<?>))
				&& !(value instanceof JGraLabTransactionCloneable)) {
			throw new GraphException(
					"The value added to this map does not support transactions.");
		}
		if (value instanceof JGraLabTransactionCloneable) {
			if (((JGraLabTransactionCloneable) value).getGraph() != graph) {
				throw new GraphException(
						"The value added to this map is from another graph.");
			}
			if (name != null) {
				((JGraLabTransactionCloneable) value).setName(name
						+ "_valueentry");
			}
		}
	}

	/**
	 * Checks whether the added key support transactions (in the case of List,
	 * Set and Map) and belongs to the same graph.
	 * 
	 * @param key
	 */
	private void isValidKeyCheck(K key) {
		if (((key instanceof Map<?, ?>) || (key instanceof List<?>) || (key instanceof Set<?>))
				&& !(key instanceof JGraLabTransactionCloneable)) {
			throw new GraphException(
					"The key added to this map does not support transactions.");
		}
		if (key instanceof JGraLabTransactionCloneable) {
			if (((JGraLabTransactionCloneable) key).getGraph() != graph) {
				throw new GraphException(
						"The key added to this map is from another graph.");
			}
			if (name != null) {
				((JGraLabTransactionCloneable) key).setName(name + "_keyentry");
			}
		}
	}

	@Override
	public void clear() {
		if (versionedMap == null) {
			throw new GraphException("Versioning is not working for this Map.");
		}
		JGraLabMapImpl<K, V> validValue = versionedMap
				.getValidValueBeforeValueChange(graph.getCurrentTransaction());
		validValue.internalClear();
	}

	/**
	 * 
	 */
	private void internalClear() {
		super.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		if (versionedMap == null) {
			throw new GraphException("Versioning is not working for this Map.");
		}
		JGraLabMapImpl<K, V> validValue = versionedMap.getValidValue(graph
				.getCurrentTransaction());
		return validValue.internalContainsKey(key);
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	private boolean internalContainsKey(Object key) {
		return super.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		if (versionedMap == null) {
			throw new GraphException("Versioning is not working for this Map.");
		}
		JGraLabMapImpl<K, V> validValue = versionedMap.getValidValue(graph
				.getCurrentTransaction());
		return validValue.internalContainsValue(value);
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	private boolean internalContainsValue(Object value) {
		return super.containsValue(value);
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		if (versionedMap == null) {
			throw new GraphException("Versioning is not working for this Map.");
		}
		JGraLabMapImpl<K, V> validValue = versionedMap.getValidValue(graph
				.getCurrentTransaction());
		return validValue.internalEntrySet();
	}

	/**
	 * 
	 * @return
	 */
	private Set<java.util.Map.Entry<K, V>> internalEntrySet() {
		return super.entrySet();
	}

	@Override
	public V get(Object key) {
		if (versionedMap == null) {
			throw new GraphException("Versioning is not working for this Map.");
		}
		JGraLabMapImpl<K, V> validValue = versionedMap.getValidValue(graph
				.getCurrentTransaction());
		return validValue.internalGet(key);
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	private V internalGet(Object key) {
		return super.get(key);
	}

	@Override
	public boolean isEmpty() {
		if (versionedMap == null) {
			throw new GraphException("Versioning is not working for this Map.");
		}
		JGraLabMapImpl<K, V> validValue = versionedMap.getValidValue(graph
				.getCurrentTransaction());
		return validValue.internalIsEmpty();
	}

	/**
	 * 
	 * @return
	 */
	private boolean internalIsEmpty() {
		return super.isEmpty();
	}

	@Override
	public Set<K> keySet() {
		if (versionedMap == null) {
			throw new GraphException("Versioning is not working for this Map.");
		}
		JGraLabMapImpl<K, V> validValue = versionedMap.getValidValue(graph
				.getCurrentTransaction());
		return validValue.internalKeySet();
	}

	/**
	 * 
	 * @return
	 */
	private Set<K> internalKeySet() {
		return super.keySet();
	}

	@Override
	public V put(K key, V value) {
		if (versionedMap == null) {
			throw new GraphException("Versioning is not working for this Map.");
		}
		isValidKeyCheck(key);
		isValidValueCheck(value);
		JGraLabMapImpl<K, V> validValue = versionedMap
				.getValidValueBeforeValueChange(graph.getCurrentTransaction());
		return validValue.internalPut(key, value);
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	private V internalPut(K key, V value) {
		return super.put(key, value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		if (versionedMap == null) {
			internalPutAll(m);
		}
		for (java.util.Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
			isValidKeyCheck(entry.getKey());
			isValidValueCheck(entry.getValue());
		}
		JGraLabMapImpl<K, V> validValue = versionedMap
				.getValidValueBeforeValueChange(graph.getCurrentTransaction());
		validValue.internalPutAll(m);
	}

	/**
	 * 
	 * @param m
	 */
	private void internalPutAll(Map<? extends K, ? extends V> m) {
		super.putAll(m);
	}

	@Override
	public V remove(Object key) {
		if (versionedMap == null) {
			throw new GraphException("Versioning is not working for this Map.");
		}
		JGraLabMapImpl<K, V> validValue = versionedMap
				.getValidValueBeforeValueChange(graph.getCurrentTransaction());
		return validValue.internalRemove(key);
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	private V internalRemove(Object key) {
		return super.remove(key);
	}

	@Override
	public int size() {
		if (versionedMap == null) {
			throw new GraphException("Versioning is not working for this Map.");
		}
		JGraLabMapImpl<K, V> validValue = versionedMap.getValidValue(graph
				.getCurrentTransaction());
		return validValue.internalSize();
	}

	/**
	 * 
	 * @return
	 */
	private int internalSize() {
		return super.size();
	}

	@Override
	public Collection<V> values() {
		if (versionedMap == null) {
			throw new GraphException("Versioning is not working for this Map.");
		}
		JGraLabMapImpl<K, V> validValue = versionedMap.getValidValue(graph
				.getCurrentTransaction());
		return validValue.internalValues();
	}

	/**
	 * 
	 * @return
	 */
	private Collection<V> internalValues() {
		return super.values();
	}

	/**
	 * 
	 * @param versionedList
	 * 
	 *            visibility set to public (afuhr)
	 */
	public void setVersionedMap(
			VersionedJGraLabCloneableImpl<JGraLabMapImpl<K, V>> versionedMap) {
		this.versionedMap = versionedMap;
		if (versionedMap != null) {
			this.graph = versionedMap.getGraph();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public JGraLabMapImpl<K, V> clone() {
		JGraLabMapImpl<K, V> toBeCloned = null;
		assert ((versionedMap != null) && (graph != null));
		toBeCloned = versionedMap.getValidValue(graph.getCurrentTransaction());
		JGraLabMapImpl<K, V> jgralabMap = new JGraLabMapImpl<K, V>();
		jgralabMap.setVersionedMap(versionedMap);
		if (toBeCloned != null) {
			for (java.util.Map.Entry<K, V> entry : toBeCloned.entrySet()) {
				K newKey = entry.getKey();
				V newValue = entry.getValue();
				if ((newKey instanceof JGraLabTransactionCloneable)
						&& (newKey != null)) {
					newKey = (K) ((JGraLabTransactionCloneable) newKey).clone();
				}
				if ((newValue instanceof JGraLabTransactionCloneable)
						&& (newValue != null)) {
					newValue = (V) ((JGraLabTransactionCloneable) newValue)
							.clone();
				}
				jgralabMap.internalPut(newKey, newValue);
			}
		}
		return jgralabMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object o) {
		// if the parameter is this instance...
		if (this == o) {
			return true;
		}

		// make sure the given parameter is at least a Map
		if (!(o instanceof Map<?, ?>)) {
			return false;
		}

		// if the parameter is an instance of JGraLabMap, we need to invoke the
		// "internal..."-methods.
		if (o instanceof JGraLabMapImpl<?, ?>) {
			JGraLabMapImpl<K, V> map = (JGraLabMapImpl<K, V>) o;

			if (!internalKeySet().equals(map.internalKeySet())) {
				return false;
			}
			if (internalSize() != map.internalSize()) {
				return false;
			}
			for (java.util.Map.Entry<K, V> entry : this.internalEntrySet()) {
				if (!map.internalEntrySet().contains(entry)) {
					return false;
				}
			}
		} else {
			// if not invoke the "normal" methods.
			Map<K, V> map = (Map<K, V>) o;

			if (!internalKeySet().equals(map.keySet())) {
				return false;
			}
			if (internalSize() != map.size()) {
				return false;
			}
			for (java.util.Map.Entry<K, V> entry : this.internalEntrySet()) {
				if (!map.entrySet().contains(entry)) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public Graph getGraph() {
		return graph;
	}

	@Override
	public void setName(String name) {
		this.name = name;
		this.versionedMap.setName(name);
	}
}
