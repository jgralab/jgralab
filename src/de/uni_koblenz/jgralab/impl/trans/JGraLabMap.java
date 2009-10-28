package de.uni_koblenz.jgralab.impl.trans;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.trans.JGraLabCloneable;

/**
 * Own implementation class for attributes of type
 * <code>java.util.Map<K,V></code>.
 * 
 * @author José Monte(monte@uni-koblenz.de)
 * 
 * @param <K>
 * @param <V>
 */
public class JGraLabMap<K, V> extends HashMap<K, V> implements JGraLabCloneable {
	private static final long serialVersionUID = -9198046937053316052L;
	private VersionedJGraLabCloneableImpl<JGraLabMap<K, V>> versionedMap;
	private Graph graph;

	/**
	 * 
	 */
	/*protected*/ public JGraLabMap() {
		super();
	}

	/**
	 * 
	 * @param initialSize
	 */
	/*protected*/ public JGraLabMap(int initialSize) {
		super(initialSize);
	}

	/**
	 * 
	 * @param initialSize
	 * @param loadFactor
	 */
	/*protected*/ public JGraLabMap(int initialSize, float loadFactor) {
		super(initialSize, loadFactor);
	}

	/**
	 * 
	 * @param map
	 */
	/*protected*/ public JGraLabMap(Map<K, V> map) {
		super(map);
	}

	public JGraLabMap(Graph g) {
		super();
		init(g);
	}
	
	public JGraLabMap(Graph g, Map<? extends K, ? extends V> map) {
		super(map);
		init(g);
	}

	public JGraLabMap(Graph g, int initialCapacity) {
		super(initialCapacity);
		init(g);
	}

	public JGraLabMap(Graph g, int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
		init(g);
	}

	public void init(Graph g) {
		if (g == null)
			throw new GraphException(
					"Given graph cannot be null.");
		if (!g.hasTransactionSupport())
			throw new GraphException(
					"An instance of JGraLabMap can only be created for graphs with transaction support");
		graph = g;
		// TODO this or no this?
		versionedMap = new VersionedJGraLabCloneableImpl<JGraLabMap<K,V>>();
	}

	@Override
	public void clear() {
		if (versionedMap == null)
			throw new GraphException("Versioning is not working for this Map.");
			//internalClear();
		versionedMap.setValidValue(this, graph.getCurrentTransaction());
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
			//return internalContainsKey(key);
			throw new GraphException("Versioning is not working for this Map.");
		versionedMap.setValidValue(this, graph.getCurrentTransaction());
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
			//return internalContainsValue(value);
			throw new GraphException("Versioning is not working for this Map.");
		versionedMap.setValidValue(this, graph.getCurrentTransaction());
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
			//return internalEntrySet();
			throw new GraphException("Versioning is not working for this Map.");
		versionedMap.setValidValue(this, graph.getCurrentTransaction());
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
			//return internalGet(key);
			throw new GraphException("Versioning is not working for this Map.");
		versionedMap.setValidValue(this, graph.getCurrentTransaction());
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
			//return internalIsEmpty();
			throw new GraphException("Versioning is not working for this Map.");
		versionedMap.setValidValue(this, graph.getCurrentTransaction());
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
			//return internalKeySet();
			throw new GraphException("Versioning is not working for this Map.");
		versionedMap.setValidValue(this, graph.getCurrentTransaction());
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
			//return internalPut(key, value);
			throw new GraphException("Versioning is not working for this Map.");
		versionedMap.setValidValue(this, graph.getCurrentTransaction());
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
		versionedMap.setValidValue(this, graph.getCurrentTransaction());
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
			//return internalRemove(key);
			throw new GraphException("Versioning is not working for this Map.");
		versionedMap.setValidValue(this, graph.getCurrentTransaction());
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
			//return internalSize();
			throw new GraphException("Versioning is not working for this Map.");
		versionedMap.setValidValue(this, graph.getCurrentTransaction());
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
			//return internalValues();
			throw new GraphException("Versioning is not working for this Map.");
		versionedMap.setValidValue(this, graph.getCurrentTransaction());
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
	 */
	protected void setVersionedMap(
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
		// TODO internal or normal add?
		for (Entry<K, V> entry : toBeCloned.entrySet()) {
			K newKey = entry.getKey();
			V newValue = entry.getValue();
			if (newKey instanceof JGraLabCloneable)
				newKey = (K) ((JGraLabCloneable) newKey).clone();
			if (newValue instanceof JGraLabCloneable)
				newValue = (V) ((JGraLabCloneable) newValue).clone();
			// TODO internal or normal add?
			jgralabMap.put(newKey, newValue);
		}
		return jgralabMap;
	}

	@Override
	public Graph getGraph() {
		return graph;
	}
}
