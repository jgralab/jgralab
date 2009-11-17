package de.uni_koblenz.jgralab.impl.trans;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set; //import java.util.Set;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.trans.JGraLabCloneable;
import de.uni_koblenz.jgralab.trans.TransactionState;

/**
 * Own implementation class for attributes of type <code>java.util.Set<E></code>.
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 * 
 * @param <E>
 * 
 * TODO maybe add check to addAll-methods?
 */
public class JGraLabSet<E> extends HashSet<E> implements JGraLabCloneable {
	private static final long serialVersionUID = -8812018025682692472L;
	private VersionedJGraLabCloneableImpl<JGraLabSet<E>> versionedSet;
	private Graph graph;
	private String name;

	/**
	 * 
	 */
	/* protected */protected JGraLabSet() {
		super();
	}

	/* protected */protected JGraLabSet(Collection<E> set) {
		super(set);
	}

	/**
	 * 
	 * @param initialSize
	 */
	/* protected */protected JGraLabSet(int initialSize) {
		super(initialSize);
	}

	/**
	 * 
	 * @param initialSize
	 * @param loadFactor
	 */
	/* protected */protected JGraLabSet(int initialSize, float loadFactor) {
		super(initialSize, loadFactor);
	}

	/**
	 * 
	 * @param g
	 */
	protected JGraLabSet(Graph g) {
		super();
		init(g);
	}

	/**
	 * 
	 * @param g
	 * @param collection
	 */
	protected JGraLabSet(Graph g, Collection<? extends E> collection) {
		super(collection);
		init(g);
	}

	/**
	 * 
	 * @param g
	 * @param initialCapacity
	 */
	public JGraLabSet(Graph g, int initialCapacity) {
		super(initialCapacity);
		init(g);
	}

	/**
	 * 
	 * @param g
	 * @param initialCapacity
	 * @param loadFactor
	 */
	public JGraLabSet(Graph g, int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
		init(g);
	}

	/**
	 * 
	 * @param versionedList
	 */
	protected void setVersionedSet(
			VersionedJGraLabCloneableImpl<JGraLabSet<E>> versionedSet) {
		this.versionedSet = versionedSet;
		if (versionedSet != null)
			graph = versionedSet.getGraph();
	}

	public void init(Graph g) {
		if (g == null)
			throw new GraphException("Given graph cannot be null.");
		if (!g.hasTransactionSupport())
			throw new GraphException(
					"An instance of JGraLabSet can only be created for graphs with transaction support");
		graph = g;
		if (graph.isLoading())
			versionedSet = new VersionedJGraLabCloneableImpl<JGraLabSet<E>>(g,
					this);
		if (versionedSet == null)
			versionedSet = new VersionedJGraLabCloneableImpl<JGraLabSet<E>>(
					graph);
		versionedSet.setValidValue(this, g.getCurrentTransaction());
	}

	// TODO this should not be necessary, but using setValidValue doesn't work
	private void hasTemporaryVersionCheck() {
		if (!graph.isLoading()) {
			if (graph.getCurrentTransaction().getState() == TransactionState.RUNNING) {
				versionedSet.handleSavepoint((TransactionImpl) graph
						.getCurrentTransaction());
				if (!versionedSet.hasTemporaryValue(graph
						.getCurrentTransaction()))
					versionedSet.createNewTemporaryValue(graph
							.getCurrentTransaction());
			}
		}
	}

	private void isValidElementCheck(E element) {
		if ((element instanceof Map || element instanceof List || element instanceof Set)
				&& !(element instanceof JGraLabCloneable))
			throw new GraphException(
					"The element added to this set does not support transactions.");
		if (element instanceof JGraLabCloneable) {
			if (((JGraLabCloneable) element).getGraph() != graph)
				throw new GraphException(
						"The element added to this set is from another graph.");
			if (name != null)
				((JGraLabCloneable) element).setName(name + "_setentry");
		}
	}

	@Override
	public boolean add(E element) {
		if (versionedSet == null)
			throw new GraphException("Versioning is not working for this set.");
		// return internalAdd(e);
		isValidElementCheck(element);
		hasTemporaryVersionCheck();
		// versionedSet.setValidValue(this, graph.getCurrentTransaction());
		return versionedSet.getValidValue(graph.getCurrentTransaction())
				.internalAdd(element);
	}

	/**
	 * 
	 * @param e
	 * @return
	 */
	private boolean internalAdd(E e) {
		return super.add(e);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		if (versionedSet == null)
			throw new GraphException("Versioning is not working for this set.");
		// return internalAddAll(c);
		Iterator<? extends E> iter = c.iterator();
		while (iter.hasNext())
			isValidElementCheck(iter.next());
		hasTemporaryVersionCheck();
		// versionedSet.setValidValue(this, graph.getCurrentTransaction());
		return versionedSet.getValidValue(graph.getCurrentTransaction())
				.internalAddAll(c);
	}

	/**
	 * 
	 * @param c
	 * @return
	 */
	private boolean internalAddAll(Collection<? extends E> c) {
		return super.addAll(c);
	}

	@Override
	public void clear() {
		if (versionedSet == null) {
			throw new GraphException("Versioning is not working for this set.");
			// internalClear();
		} else {
			// hasTemporaryVersionCheck();
			hasTemporaryVersionCheck();
			// versionedSet.setValidValue(this, graph.getCurrentTransaction());
			versionedSet.getValidValue(graph.getCurrentTransaction())
					.internalClear();
		}
	}

	/**
	 * 
	 */
	private void internalClear() {
		super.clear();
	}

	@Override
	public boolean contains(Object o) {
		if (versionedSet == null)
			throw new GraphException("Versioning is not working for this set.");
		// return internalContains(o);
		// transactionIsNullCheck();
		return versionedSet.getValidValue(graph.getCurrentTransaction())
				.internalContains(o);
	}

	/**
	 * 
	 * @param o
	 * @return
	 */
	private boolean internalContains(Object o) {
		return super.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		if (versionedSet == null)
			throw new GraphException("Versioning is not working for this set.");
		// return internalContainsAll(c);
		return versionedSet.getValidValue(graph.getCurrentTransaction())
				.internalContainsAll(c);
	}

	/**
	 * 
	 * @param c
	 * @return
	 */
	private boolean internalContainsAll(Collection<?> c) {
		return super.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		if (versionedSet == null)
			throw new GraphException("Versioning is not working for this set.");
		// return internalIsEmpty();
		return versionedSet.getValidValue(graph.getCurrentTransaction())
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
	public Iterator<E> iterator() {
		if (versionedSet == null)
			throw new GraphException("Versioning is not working for this set.");
		// return internalIterator();
		return versionedSet.getValidValue(graph.getCurrentTransaction())
				.internalIterator();
	}

	/**
	 * 
	 * @return
	 */
	private Iterator<E> internalIterator() {
		return super.iterator();
	}

	@Override
	public boolean remove(Object o) {
		if (versionedSet == null)
			throw new GraphException("Versioning is not working for this set.");
		// return internalRemove(o);
		// versionedSet.setValidValue(this, graph.getCurrentTransaction());
		hasTemporaryVersionCheck();
		return versionedSet.getValidValue(graph.getCurrentTransaction())
				.internalRemove(o);
	}

	/**
	 * 
	 * @param o
	 * @return
	 */
	private boolean internalRemove(Object o) {
		return super.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		if (versionedSet == null)
			throw new GraphException("Versioning is not working for this set.");
		// return internalRemoveAll(c);
		// versionedSet.setValidValue(this, graph.getCurrentTransaction());
		hasTemporaryVersionCheck();
		return versionedSet.getValidValue(graph.getCurrentTransaction())
				.internalRemoveAll(c);
	}

	/**
	 * 
	 * @param c
	 * @return
	 */
	private boolean internalRemoveAll(Collection<?> c) {
		return super.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		if (versionedSet == null)
			throw new GraphException("Versioning is not working for this set.");
		// return internalRetainAll(c);
		// versionedSet.setValidValue(this, graph.getCurrentTransaction());
		hasTemporaryVersionCheck();
		return versionedSet.getValidValue(graph.getCurrentTransaction())
				.internalRetainAll(c);
	}

	/**
	 * 
	 * @param c
	 * @return
	 */
	private boolean internalRetainAll(Collection<?> c) {
		return super.retainAll(c);
	}

	@Override
	public int size() {
		if (versionedSet == null && graph == null)
			throw new GraphException("Versioning is not working for this set.");
		// return internalSize();
		return versionedSet.getValidValue(graph.getCurrentTransaction())
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
	public Object[] toArray() {
		if (versionedSet == null && graph == null)
			throw new GraphException("Versioning is not working for this set.");
		// return internalToArray();
		return versionedSet.getValidValue(graph.getCurrentTransaction())
				.internalToArray();
	}

	/**
	 * 
	 * @return
	 */
	private Object[] internalToArray() {
		return super.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		if (versionedSet == null && graph == null)
			throw new GraphException("Versioning is not working for this set.");
		// return internalToArray(a);
		return versionedSet.getValidValue(graph.getCurrentTransaction())
				.internalToArray(a);
	}

	/**
	 * 
	 * @return
	 */
	private <T> T[] internalToArray(T[] a) {
		return super.toArray(a);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object clone() {
		super.clone();
		JGraLabSet<E> toBeCloned = null;
		if (versionedSet == null && graph == null)
			toBeCloned = this;
		else
			toBeCloned = versionedSet.getValidValue(graph
					.getCurrentTransaction());
		JGraLabSet<E> jgralabSet = new JGraLabSet<E>();
		jgralabSet.setVersionedSet(versionedSet);
		for (E element : toBeCloned) {
			if (element instanceof JGraLabCloneable && element != null)
				jgralabSet
						.internalAdd((E) ((JGraLabCloneable) element).clone());
			else
				jgralabSet.internalAdd(element);
		}
		return jgralabSet;
	}

	@Override
	public Graph getGraph() {
		return graph;
	}

	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		if (!(o instanceof JGraLabSet))
			return false;
		JGraLabSet<E> set = (JGraLabSet<E>) o;
		if (set == this)
			return true;
		if (internalSize() != set.internalSize())
			return false;
		Iterator<E> iter = this.internalIterator();
		while (iter.hasNext()) {
			if (!set.internalContains(iter.next()))
				return false;
		}
		return true;
	}

	@Override
	public void setName(String name) {
		this.name = name;
		this.versionedSet.setName(this.name);
	}
}
