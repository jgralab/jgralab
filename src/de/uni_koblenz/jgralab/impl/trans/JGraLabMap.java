package de.uni_koblenz.jgralab.impl.trans;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.trans.JGraLabCloneable;
import de.uni_koblenz.jgralab.trans.TransactionState;

/**
 * Own implementation class for attributes of type
 * <code>java.util.Map<K,V></code>.
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 * 
 * @param <K>
 * @param <V>
 * 
 * TODO maybe add check to putAll-methods?
 */
public class JGraLabMap<K, V> extends HashMap<K, V> implements JGraLabCloneable {
	private static final long serialVersionUID = -9198046937053316052L;
	private VersionedJGraLabCloneableImpl<JGraLabMap<K, V>> versionedMap;
	private Graph graph;
	private String name;

	/**
	 * 
	 */
	protected JGraLabMap() {
		super();
	}

	/**
	 * 
	 * @param initialSize
	 */
	protected JGraLabMap(int initialSize) {
		super(initialSize);
	}

	/**
	 * 
	 * @param initialSize
	 * @param loadFactor
	 */
	protected JGraLabMap(int initialSize, float loadFactor) {
		super(initialSize, loadFactor);
	}

	/**
	 * 
	 * @param map
	 *
	 * visibility set to public (afuhr)
	 * 
	 * TODO ask why it must be public ;-)
	 */
	protected JGraLabMap(Map<K, V> map) {
		super(map);
	}

	/**
	 * 
	 * @param g
	 */
	protected JGraLabMap(Graph g) {
		super();
		init(g);
	}

	/**
	 * 
	 * @param g
	 * @param map
	 */
	protected JGraLabMap(Graph g, Map<? extends K, ? extends V> map) {
		super(map);
		init(g);
	}

	/**
	 * 
	 * @param g
	 * @param initialCapacity
	 */
	protected JGraLabMap(Graph g, int initialCapacity) {
		super(initialCapacity);
		init(g);
	}

	/**
	 * 
	 * @param g
	 * @param initialCapacity
	 * @param loadFactor
	 */
	protected JGraLabMap(Graph g, int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
		init(g);
	}

	/**
	 * 
	 * @param g
	 */
	public void init(Graph g) {
		if (g == null)
			throw new GraphException("Given graph cannot be null.");
		if (!g.hasTransactionSupport())
			throw new GraphException(
					"An instance of JGraLabSet can only be created for graphs with transaction support");
		graph = g;
		if (graph.isLoading())
			versionedMap = new VersionedJGraLabCloneableImpl<JGraLabMap<K, V>>(
					g, this);
		if (versionedMap == null)
			versionedMap = new VersionedJGraLabCloneableImpl<JGraLabMap<K, V>>(
					graph);
		versionedMap.setValidValue(this, g.getCurrentTransaction());
	}

	/**
	 * Check whether a temporary has already been created and initiates
	 * savepoint-issues if needed.
	 * 
	 * TODO try to extract this excerpt into the VersionDataObjectImpl-class.
	 */
	private void hasTemporaryVersionCheck() {
		if (!graph.isLoading()) {
			if (graph.getCurrentTransaction().getState() == TransactionState.RUNNING) {
				versionedMap.handleSavepoint((TransactionImpl) graph
						.getCurrentTransaction());
				if (!versionedMap.hasTemporaryValue(graph
						.getCurrentTransaction()))
					versionedMap.createNewTemporaryValue(graph
							.getCurrentTransaction());
			}
		}
	}

	/**
	 * Checks whether the added value support transactions (in the case of
	 * List, Set and Map) and belongs to the same graph.
	 * 
	 * @param value
	 */
	private void isValidValueCheck(V value) {
		if ((value instanceof Map<?,?> || value instanceof List<?> || value instanceof Set<?>)
				&& !(value instanceof JGraLabCloneable))
			throw new GraphException(
					"The value added to this map does not support transactions.");
		if (value instanceof JGraLabCloneable) {
			if (((JGraLabCloneable) value).getGraph() != graph)
				throw new GraphException(
						"The value added to this map is from another graph.");
			if (name != null)
				((JGraLabCloneable) value).setName(name + "_valueentry");
		}
	}

	/**
	 * Checks whether the added key support transactions (in the case of
	 * List, Set and Map) and belongs to the same graph.
	 * 
	 * @param key
	 */
	private void isValidKeyCheck(K key) {
		if ((key instanceof Map<?,?> || key instanceof List<?> || key instanceof Set<?>)
				&& !(key instanceof JGraLabCloneable))
			throw new GraphException(
					"The key added to this map does not support transactions.");
		if (key instanceof JGraLabCloneable) {
			if (((JGraLabCloneable) key).getGraph() != graph)
				throw new GraphException(
						"The key added to this map is from another graph.");
			if (name != null)
				((JGraLabCloneable) key).setName(name + "_keyentry");
		}
	}

	@Override
	public void clear() {
		if (versionedMap == null)
			throw new GraphException("Versioning is not working for this Map.");
		hasTemporaryVersionCheck();
		versionedMap.getValidValue(graph.getCurrentTransaction())
				.internalClear();
	}

	/**
	 * 
	 */
	private void internalClear() {
		super.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		if (versionedMap == null)
			throw new GraphException("Versioning is not working for this Map.");
		return versionedMap.getValidValue(graph.getCurrentTransaction())
				.internalContainsKey(key);
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
		if (versionedMap == null)
			throw new GraphException("Versioning is not working for this Map.");
		return versionedMap.getValidValue(graph.getCurrentTransaction())
				.internalContainsValue(value);
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
	public Set<Entry<K, V>> entrySet() {
		if (versionedMap == null)
			throw new GraphException("Versioning is not working for this Map.");
		return versionedMap.getValidValue(graph.getCurrentTransaction())
				.internalEntrySet();
	}

	/**
	 * 
	 * @return
	 */
	private Set<Entry<K, V>> internalEntrySet() {
		return super.entrySet();
	}

	@Override
	public V get(Object key) {
		if (versionedMap == null)
			throw new GraphException("Versioning is not working for this Map.");
		return versionedMap.getValidValue(graph.getCurrentTransaction())
				.internalGet(key);
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	private V internalGet(Object key) {
		return super.get(key);
	}

	public boolean isEmpty() {
		if (versionedMap == null)
			throw new GraphException("Versioning is not working for this Map.");
		return versionedMap.getValidValue(graph.getCurrentTransaction())
				.internalIsEmpty();
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
		if (versionedMap == null)
			throw new GraphException("Versioning is not working for this Map.");
		return versionedMap.getValidValue(graph.getCurrentTransaction())
				.internalKeySet();
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
		if (versionedMap == null)
			throw new GraphException("Versioning is not working for this Map.");
		isValidKeyCheck(key);
		isValidValueCheck(value);
		hasTemporaryVersionCheck();
		return versionedMap.getValidValue(graph.getCurrentTransaction())
				.internalPut(key, value);
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
		if (versionedMap == null)
			internalPutAll(m);
		for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
			isValidKeyCheck(entry.getKey());
			isValidValueCheck(entry.getValue());
		}
		hasTemporaryVersionCheck();
		versionedMap.getValidValue(graph.getCurrentTransaction())
				.internalPutAll(m);
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
		if (versionedMap == null)
			throw new GraphException("Versioning is not working for this Map.");
		hasTemporaryVersionCheck();
		return versionedMap.getValidValue(graph.getCurrentTransaction())
				.internalRemove(key);
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
		if (versionedMap == null)
			throw new GraphException("Versioning is not working for this Map.");
		return versionedMap.getValidValue(graph.getCurrentTransaction())
				.internalSize();
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
		if (versionedMap == null)
			throw new GraphException("Versioning is not working for this Map.");
		return versionedMap.getValidValue(graph.getCurrentTransaction())
				.internalValues();
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
	 * visibility set to public (afuhr)
	 */
	public void setVersionedMap(
			VersionedJGraLabCloneableImpl<JGraLabMap<K, V>> versionedMap) {
		this.versionedMap = versionedMap;
		if (versionedMap != null)
			this.graph = versionedMap.getGraph();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object clone() {
		JGraLabMap<K, V> toBeCloned = null;
		if (versionedMap == null && graph == null)
			toBeCloned = this;
		else
			toBeCloned = versionedMap.getValidValue(graph
					.getCurrentTransaction());
		JGraLabMap<K, V> jgralabMap = new JGraLabMap<K, V>();
		jgralabMap.setVersionedMap(versionedMap);
		for (Entry<K, V> entry : toBeCloned.entrySet()) {
			K newKey = entry.getKey();
			V newValue = entry.getValue();
			if (newKey instanceof JGraLabCloneable && newKey != null)
				newKey = (K) ((JGraLabCloneable) newKey).clone();
			if (newValue instanceof JGraLabCloneable && newValue != null)
				newValue = (V) ((JGraLabCloneable) newValue).clone();
			jgralabMap.internalPut(newKey, newValue);
		}
		return jgralabMap;
	}

	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		if (!(o instanceof JGraLabMap))
			return false;
		JGraLabMap<K, V> object = (JGraLabMap<K, V>) o;
		if (this == object)
			return true;
		if (!internalKeySet().equals(object.internalKeySet()))
			return false;
		if (internalSize() != object.internalSize())
			return false;
		for (Entry<K, V> entry : this.internalEntrySet()) {
			if (!object.internalEntrySet().contains(entry))
				return false;
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
