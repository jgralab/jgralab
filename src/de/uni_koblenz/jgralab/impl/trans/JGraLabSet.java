package de.uni_koblenz.jgralab.impl.trans;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
//import java.util.Set;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.trans.JGraLabCloneable;

/**
 * Own implementation class for attributes of type <code>java.util.Set<E></code>
 * .
 * 
 * @author José Monte(monte@uni-koblenz.de)
 * 
 * @param <E>
 */
public class JGraLabSet<E> extends HashSet<E> implements JGraLabCloneable {
	private static final long serialVersionUID = -8812018025682692472L;
	private VersionedJGraLabCloneableImpl<JGraLabSet<E>> versionedSet;
	private Graph graph;

	/**
	 * 
	 */
	/*protected*/ public JGraLabSet() {
		super();
	}

	@SuppressWarnings("unchecked")
	/*protected*/ public JGraLabSet(Collection/*<E>*/ set) {
		super(set);
	}
	
	/**
	 * 
	 */
	/*public JGraLabSet(Set<E> set) {
		super(set);
	}*/

	/**
	 * 
	 * @param initialSize
	 */
	/*protected*/ public JGraLabSet(int initialSize) {
		super(initialSize);
	}

	/**
	 * 
	 * @param initialSize
	 * @param loadFactor
	 */
	/*protected*/ public JGraLabSet(int initialSize, float loadFactor) {
		super(initialSize, loadFactor);
	}

	/**
	 * 
	 * @param versionedList
	 */
	public void setVersionedSet(
			VersionedJGraLabCloneableImpl<JGraLabSet<E>> versionedSet) {
		this.versionedSet = versionedSet;
		if (versionedSet != null)
			graph = versionedSet.getGraph();
	}

	@Override
	public boolean add(E e) {
		if (versionedSet == null)
			return internalAdd(e);
		versionedSet.setValidValue(this, graph.getCurrentTransaction());
		return versionedSet.getValidValue(graph.getCurrentTransaction())
				.internalAdd(e);
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
			return internalAddAll(c);
		versionedSet.setValidValue(this, graph.getCurrentTransaction());
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
			internalClear();
		} else {
			versionedSet.setValidValue(this, graph.getCurrentTransaction());
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
			return internalContains(o);
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
			return internalContainsAll(c);
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
			return internalIsEmpty();
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
			return internalIterator();
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
			return internalRemove(o);
		versionedSet.setValidValue(this, graph.getCurrentTransaction());
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
			return internalRemoveAll(c);
		versionedSet.setValidValue(this, graph.getCurrentTransaction());
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
			return internalRetainAll(c);
		versionedSet.setValidValue(this, graph.getCurrentTransaction());
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
			return internalSize();
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
			return internalToArray();
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
			return internalToArray(a);
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
		JGraLabSet<E> toBeCloned = null;
		if (versionedSet == null && graph == null)
			toBeCloned = this;
		else
			toBeCloned = versionedSet.getValidValue(graph
					.getCurrentTransaction());
		JGraLabSet<E> jgralabSet = new JGraLabSet<E>();
		jgralabSet.setVersionedSet(versionedSet);
		for (E element : toBeCloned) {
			if (element instanceof JGraLabCloneable)
				jgralabSet.add((E) ((JGraLabCloneable) element).clone());
			else
				jgralabSet.add(element);
		}
		return jgralabSet;
	}
}
