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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.JGraLabSet;
import de.uni_koblenz.jgralab.trans.JGraLabTransactionCloneable;

/**
 * Own implementation class for attributes of type <code>java.util.Set<E></code>
 * .
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 * 
 * @param <E>
 */
public class JGraLabSetImpl<E> extends HashSet<E> implements
		JGraLabTransactionCloneable, JGraLabSet<E> {
	private static final long serialVersionUID = -8812018025682692472L;
	private transient VersionedJGraLabCloneableImpl<JGraLabSetImpl<E>> versionedSet;
	private transient Graph graph;
	private transient String name;

	/**
	 * 
	 */
	protected JGraLabSetImpl() {
		super();
	}

	/**
	 * 
	 * @param set
	 */
	protected JGraLabSetImpl(Collection<E> set) {
		super(set);
	}

	/**
	 * 
	 * @param initialSize
	 */
	protected JGraLabSetImpl(int initialSize) {
		super(initialSize);
	}

	/**
	 * 
	 * @param initialSize
	 * @param loadFactor
	 */
	protected JGraLabSetImpl(int initialSize, float loadFactor) {
		super(initialSize, loadFactor);
	}

	/**
	 * 
	 * @param g
	 */
	protected JGraLabSetImpl(Graph g) {
		super();
		init(g);
	}

	/**
	 * 
	 * @param g
	 * @param collection
	 */
	protected JGraLabSetImpl(Graph g, Collection<? extends E> collection) {
		super(collection);
		init(g);
	}

	/**
	 * 
	 * @param g
	 * @param initialCapacity
	 */
	protected JGraLabSetImpl(Graph g, int initialCapacity) {
		super(initialCapacity);
		init(g);
	}

	/**
	 * 
	 * @param g
	 * @param initialCapacity
	 * @param loadFactor
	 */
	protected JGraLabSetImpl(Graph g, int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
		init(g);
	}

	/**
	 * 
	 * @param versionedSet
	 */
	protected void setVersionedSet(
			VersionedJGraLabCloneableImpl<JGraLabSetImpl<E>> versionedSet) {
		this.versionedSet = versionedSet;
		if (versionedSet != null) {
			graph = versionedSet.getGraph();
		}
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
			versionedSet = new VersionedJGraLabCloneableImpl<JGraLabSetImpl<E>>(
					g, this);
		}
		if (versionedSet == null) {
			versionedSet = new VersionedJGraLabCloneableImpl<JGraLabSetImpl<E>>(
					graph);
		}
		versionedSet.setValidValue(this, g.getCurrentTransaction());
	}

	/**
	 * Checks whether the added elements support transactions (in the case of
	 * List, Set and Map) and belong to the same graph.
	 * 
	 * @param element
	 */
	private void isValidElementCheck(E element) {
		if ((element instanceof Map<?, ?> || element instanceof List<?> || element instanceof Set<?>)
				&& !(element instanceof JGraLabTransactionCloneable)) {
			throw new GraphException(
					"The element added to this set does not support transactions.");
		}
		if (element instanceof JGraLabTransactionCloneable) {
			if (((JGraLabTransactionCloneable) element).getGraph() != graph) {
				throw new GraphException(
						"The element added to this set is from another graph.");
			}
			if (name != null) {
				((JGraLabTransactionCloneable) element).setName(name
						+ "_setentry");
			}
		}
	}

	@Override
	public boolean add(E element) {
		if (versionedSet == null) {
			throw new GraphException("Versioning is not working for this set.");
		}
		isValidElementCheck(element);
		JGraLabSetImpl<E> validValue = versionedSet
				.getValidValueBeforeValueChange(graph.getCurrentTransaction());
		return validValue.internalAdd(element);
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
		if (versionedSet == null) {
			throw new GraphException("Versioning is not working for this set.");
		}
		Iterator<? extends E> iter = c.iterator();
		while (iter.hasNext()) {
			isValidElementCheck(iter.next());
		}
		JGraLabSetImpl<E> validValue = versionedSet
				.getValidValueBeforeValueChange(graph.getCurrentTransaction());
		return validValue.internalAddAll(c);
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
		}
		JGraLabSetImpl<E> validValue = versionedSet
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
	public boolean contains(Object o) {
		if (versionedSet == null) {
			throw new GraphException("Versioning is not working for this set.");
		}
		JGraLabSetImpl<E> validValue = versionedSet.getValidValue(graph
				.getCurrentTransaction());
		return validValue.internalContains(o);
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
		if (versionedSet == null) {
			throw new GraphException("Versioning is not working for this set.");
		}
		JGraLabSetImpl<E> validValue = versionedSet.getValidValue(graph
				.getCurrentTransaction());
		return validValue.internalContainsAll(c);
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
		if (versionedSet == null) {
			throw new GraphException("Versioning is not working for this set.");
		}
		JGraLabSetImpl<E> validValue = versionedSet.getValidValue(graph
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
	public Iterator<E> iterator() {
		if (versionedSet == null) {
			throw new GraphException("Versioning is not working for this set.");
		}
		JGraLabSetImpl<E> validValue = versionedSet.getValidValue(graph
				.getCurrentTransaction());
		return validValue.internalIterator();
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
		if (versionedSet == null) {
			throw new GraphException("Versioning is not working for this set.");
		}
		JGraLabSetImpl<E> validValue = versionedSet
				.getValidValueBeforeValueChange(graph.getCurrentTransaction());
		return validValue.internalRemove(o);
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
		if (versionedSet == null) {
			throw new GraphException("Versioning is not working for this set.");
		}
		JGraLabSetImpl<E> validValue = versionedSet
				.getValidValueBeforeValueChange(graph.getCurrentTransaction());
		return validValue.internalRemoveAll(c);
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
		if (versionedSet == null) {
			throw new GraphException("Versioning is not working for this set.");
		}
		JGraLabSetImpl<E> validValue = versionedSet
				.getValidValueBeforeValueChange(graph.getCurrentTransaction());
		return validValue.internalRetainAll(c);
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
		if (versionedSet == null) {
			throw new GraphException("Versioning is not working for this set.");
		}
		JGraLabSetImpl<E> validValue = versionedSet.getValidValue(graph
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
	public Object[] toArray() {
		if (versionedSet == null) {
			throw new GraphException("Versioning is not working for this set.");
		}
		JGraLabSetImpl<E> validValue = versionedSet.getValidValue(graph
				.getCurrentTransaction());
		return validValue.internalToArray();
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
		if (versionedSet == null) {
			throw new GraphException("Versioning is not working for this set.");
		}
		JGraLabSetImpl<E> validValue = versionedSet.getValidValue(graph
				.getCurrentTransaction());
		return validValue.internalToArray(a);
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
	public JGraLabSetImpl<E> clone() {
		JGraLabSetImpl<E> toBeCloned = null;
		assert (versionedSet != null && graph != null);
		toBeCloned = versionedSet.getValidValue(graph.getCurrentTransaction());
		JGraLabSetImpl<E> jgralabSet = new JGraLabSetImpl<E>();
		jgralabSet.setVersionedSet(versionedSet);
		if (toBeCloned != null) {
			for (E element : toBeCloned) {
				if (element instanceof JGraLabTransactionCloneable
						&& element != null) {
					jgralabSet
							.internalAdd((E) ((JGraLabTransactionCloneable) element)
									.clone());
				} else {
					jgralabSet.internalAdd(element);
				}
			}
		}
		return jgralabSet;
	}

	@Override
	public Graph getGraph() {
		return graph;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object o) {
		// if the parameter is this instance...
		if (o == this) {
			return true;
		}

		// make sure the given parameter is at least a Set
		if (!(o instanceof Set<?>)) {
			return false;
		}

		// if the parameter is an instance of JGraLabSet, we need to invoke the
		// "internal..."-methods.
		if (o instanceof JGraLabSetImpl<?>) {
			JGraLabSetImpl<E> set = (JGraLabSetImpl<E>) o;

			if (internalSize() != set.internalSize()) {
				return false;
			}
			Iterator<E> iter = this.internalIterator();
			while (iter.hasNext()) {
				if (!set.internalContains(iter.next())) {
					return false;
				}
			}
		} else {
			// if not invoke the "normal" methods.
			Set<E> set = (Set<E>) o;

			if (internalSize() != set.size()) {
				return false;
			}
			Iterator<E> iter = this.internalIterator();
			while (iter.hasNext()) {
				if (!set.contains(iter.next())) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public void setName(String name) {
		this.name = name;
		this.versionedSet.setName(this.name);
	}
}
