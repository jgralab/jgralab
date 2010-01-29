package de.uni_koblenz.jgralab.impl.trans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.trans.JGraLabCloneable;
import de.uni_koblenz.jgralab.trans.TransactionState;

/**
 * Own implementation class for attributes of type
 * <code>java.util.List<E></code>.
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 * 
 * @param <E>
 * 
 */
public class JGraLabList<E> extends ArrayList<E> implements JGraLabCloneable {
	private static final long serialVersionUID = -1881528493844357904L;
	private VersionedJGraLabCloneableImpl<JGraLabList<E>> versionedList;

	private Graph graph;
	private String name;

	/**
	 * @param g
	 */
	protected JGraLabList(Graph g) {
		super();
		init(g);
	}

	/**
	 * 
	 * 
	 * @param initialSize
	 */
	protected JGraLabList(Graph g, int initialSize) {
		super(initialSize);
		init(g);
	}

	/**
	 * 
	 * @param g
	 * @param collection
	 */
	protected JGraLabList(Graph g, Collection<? extends E> collection) {
		super(collection);
		init(g);
	}

	/**
	 * 
	 * @param collection
	 */
	protected JGraLabList(Collection<E> collection) {
		super(collection);
	}

	/**
	 * 
	 */
	protected JGraLabList() {
		super();
	}

	/**
	 * 
	 * @param g
	 */
	public void init(Graph g) {
		if (g == null) {
			throw new GraphException("Given graph cannot be null.");
		}
		if (!g.hasTransactionSupport()) {
			throw new GraphException(
					"An instance of JGraLabList can only be created for graphs with transaction support.");
		}
		graph = g;
		if (graph.isLoading())
			versionedList = new VersionedJGraLabCloneableImpl<JGraLabList<E>>(
					g, this);
		if (versionedList == null)
			versionedList = new VersionedJGraLabCloneableImpl<JGraLabList<E>>(g);
		versionedList.setValidValue(this, g.getCurrentTransaction());
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
				versionedList.handleSavepoint((TransactionImpl) graph
						.getCurrentTransaction());
				if (!versionedList.hasTemporaryValue(graph
						.getCurrentTransaction()))
					versionedList.createNewTemporaryValue(graph
							.getCurrentTransaction());
			}
		}
	}

	/**
	 * Checks whether the added elements support transactions (in the case of
	 * List, Set and Map) and belong to the same graph.
	 * 
	 * @param element
	 */
	private void isValidElementCheck(E element) {
		if ((element instanceof Map<?, ?> || element instanceof List<?> || element instanceof Set<?>)
				&& !(element instanceof JGraLabCloneable))
			throw new GraphException(
					"The element added to this list does not support transactions.");
		if (element instanceof JGraLabCloneable) {
			if (((JGraLabCloneable) element).getGraph() != graph)
				throw new GraphException(
						"The element added to this list is from another graph.");
			if (name != null)
				((JGraLabCloneable) element).setName(name + "_listentry");
		}
	}

	/**
	 * 
	 * @param versionedList
	 */
	protected void setVersionedList(
			VersionedJGraLabCloneableImpl<JGraLabList<E>> versionedList) {
		this.versionedList = versionedList;
		if (versionedList != null) {
			this.graph = versionedList.getGraph();
		}
	}

	@Override
	public boolean add(E element) {
		if (versionedList == null) {
			throw new GraphException("Versioning is not working for this list.");
		}
		isValidElementCheck(element);
		hasTemporaryVersionCheck();
		return versionedList.getValidValue(graph.getCurrentTransaction())
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
	public void add(int index, E element) {
		if (versionedList == null)
			throw new GraphException("Versioning is not working for this list.");
		isValidElementCheck(element);
		hasTemporaryVersionCheck();
		versionedList.getValidValue(graph.getCurrentTransaction()).internalAdd(
				index, element);
	}

	/**
	 * 
	 * @param index
	 * @param element
	 */
	private void internalAdd(int index, E element) {
		super.add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		if (versionedList == null) {
			throw new GraphException("Versioning is not working for this list.");
		}
		Iterator<? extends E> iter = c.iterator();
		while (iter.hasNext())
			isValidElementCheck(iter.next());
		hasTemporaryVersionCheck();
		return versionedList.getValidValue(graph.getCurrentTransaction())
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
	public boolean addAll(int index, Collection<? extends E> c) {
		if (versionedList == null) {
			throw new GraphException("Versioning is not working for this list.");
		}
		Iterator<? extends E> iter = c.iterator();
		while (iter.hasNext())
			isValidElementCheck(iter.next());
		hasTemporaryVersionCheck();
		return versionedList.getValidValue(graph.getCurrentTransaction())
				.internalAddAll(index, c);
	}

	/**
	 * 
	 * @param index
	 * @param c
	 * @return
	 */
	private boolean internalAddAll(int index, Collection<? extends E> c) {
		return super.addAll(index, c);
	}

	@Override
	public void clear() {
		if (versionedList == null)
			throw new GraphException("Versioning is not working for this list.");
		hasTemporaryVersionCheck();
		versionedList.getValidValue(graph.getCurrentTransaction())
				.internalClear();
		// }
	}

	/**
	 * 
	 */
	private void internalClear() {
		super.clear();
	}

	@Override
	public boolean contains(Object o) {
		if (versionedList == null) {
			throw new GraphException("Versioning is not working for this list.");
		}
		return versionedList.getValidValue(graph.getCurrentTransaction())
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
		if (versionedList == null) {
			throw new GraphException("Versioning is not working for this list.");
		}
		return versionedList.getValidValue(graph.getCurrentTransaction())
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
	// TODO try to make it work somehow?!
	public void ensureCapacity(int minCapacity) {
		// simply do nothing
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object o) {
		// if the parameter is this instance...
		if (o == this)
			return true;

		// make sure the given parameter is at least a List
		if (!(o instanceof List<?>))
			return false;

		// if the parameter is an instance of JGraLabList, we need to invoke the
		// "internal..."-methods.
		if (o instanceof JGraLabList<?>) {
			JGraLabList<E> list = (JGraLabList<E>) o;
			if (internalSize() != list.internalSize())
				return false;
			for (int i = 0; i < internalSize(); i++) {
				if (internalGet(i) == null) {
					// if both values are null, then they are equal
					if (list.internalGet(i) == null)
						return true;
					// ..otherwise not
					return false;
				} else if (!internalGet(i).equals(list.internalGet(i)))
					return false;
			}
		} else {
			// if not invoke the "normal" methods.
			List list = (List) o;
			if (internalSize() != list.size())
				return false;
			for (int i = 0; i < internalSize(); i++) {
				if (internalGet(i) == null) {
					// if both values are null, then they are equal
					if (list.get(i) == null)
						return true;
					// ..otherwise not
					return false;
				} else if (!internalGet(i).equals(list.get(i)))
					return false;
			}
		}

		return true;
	}

	@Override
	public E get(int index) {
		if (versionedList == null) {
			throw new GraphException("Versioning is not working for this list.");
		}
		return versionedList.getValidValue(graph.getCurrentTransaction())
				.internalGet(index);
	}

	/**
	 * 
	 * @param index
	 * @return
	 */
	private E internalGet(int index) {
		return super.get(index);
	}

	@Override
	public int indexOf(Object o) {
		if (versionedList == null) {
			throw new GraphException("Versioning is not working for this list.");
		}
		return versionedList.getValidValue(graph.getCurrentTransaction())
				.internalIndexOf(o);
	}

	/**
	 * 
	 * @param o
	 * @return
	 */
	private int internalIndexOf(Object o) {
		return super.indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		if (versionedList == null) {
			throw new GraphException("Versioning is not working for this list.");
		}
		return versionedList.getValidValue(graph.getCurrentTransaction())
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
		if (versionedList == null) {
			throw new GraphException("Versioning is not working for this list.");
		}
		return versionedList.getValidValue(graph.getCurrentTransaction())
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
	public int lastIndexOf(Object o) {
		if (versionedList == null) {
			throw new GraphException("Versioning is not working for this list.");
		}
		return versionedList.getValidValue(graph.getCurrentTransaction())
				.internalLastIndexOf(o);
	}

	/**
	 * 
	 * @param o
	 * @return
	 */
	private int internalLastIndexOf(Object o) {
		return super.lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		if (versionedList == null) {
			throw new GraphException("Versioning is not working for this list.");
		}
		return versionedList.getValidValue(graph.getCurrentTransaction())
				.internalListIterator();
	}

	/**
	 * 
	 * @return
	 */
	private ListIterator<E> internalListIterator() {
		return super.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		if (versionedList == null) {
			throw new GraphException("Versioning is not working for this list.");
		}
		return versionedList.getValidValue(graph.getCurrentTransaction())
				.internalListIterator(index);
	}

	/**
	 * 
	 * @param index
	 * @return
	 */
	private ListIterator<E> internalListIterator(int index) {
		return super.listIterator(index);
	}

	@Override
	public E remove(int index) {
		if (versionedList == null) {
			throw new GraphException("Versioning is not working for this list.");
		}
		hasTemporaryVersionCheck();
		return versionedList.getValidValue(graph.getCurrentTransaction())
				.internalRemove(index);
	}

	/**
	 * 
	 * @param index
	 * @return
	 */
	private E internalRemove(int index) {
		return super.remove(index);
	}

	@Override
	public boolean remove(Object o) {
		if (versionedList == null) {
			throw new GraphException("Versioning is not working for this list.");
		}
		hasTemporaryVersionCheck();
		return versionedList.getValidValue(graph.getCurrentTransaction())
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
		if (versionedList == null) {
			throw new GraphException("Versioning is not working for this list.");
		}
		hasTemporaryVersionCheck();
		return versionedList.getValidValue(graph.getCurrentTransaction())
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
	public void removeRange(int fromIndex, int toIndex) {
		if (versionedList == null)
			throw new GraphException("Versioning is not working for this list.");
		hasTemporaryVersionCheck();
		versionedList.getValidValue(graph.getCurrentTransaction())
				.internalRemoveRange(fromIndex, toIndex);
	}

	/**
	 * 
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	private void internalRemoveRange(int fromIndex, int toIndex) {
		super.removeRange(fromIndex, toIndex);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		if (versionedList == null) {
			throw new GraphException("Versioning is not working for this list.");
		}
		hasTemporaryVersionCheck();
		return versionedList.getValidValue(graph.getCurrentTransaction())
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
	public E set(int index, E element) {
		if (versionedList == null) {
			throw new GraphException("Versioning is not working for this list.");
		}
		hasTemporaryVersionCheck();
		return versionedList.getValidValue(graph.getCurrentTransaction())
				.internalSet(index, element);
	}

	/**
	 * 
	 * @param index
	 * @param element
	 * @return
	 */
	private E internalSet(int index, E element) {
		return super.set(index, element);
	}

	@Override
	public int size() {
		if (versionedList == null) {
			throw new GraphException("Versioning is not working for this list.");
		}
		return versionedList.getValidValue(graph.getCurrentTransaction())
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
	public List<E> subList(int fromIndex, int toIndex) {
		if (versionedList == null) {
			throw new GraphException("Versioning is not working for this list.");
		}
		return versionedList.getValidValue(graph.getCurrentTransaction())
				.internalSubList(fromIndex, toIndex);
	}

	/**
	 * 
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	private List<E> internalSubList(int fromIndex, int toIndex) {
		return super.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		if (versionedList == null) {
			throw new GraphException("Versioning is not working for this list.");
		}
		return versionedList.getValidValue(graph.getCurrentTransaction())
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
		if (versionedList == null) {
			throw new GraphException("Versioning is not working for this list.");
		}
		return versionedList.getValidValue(graph.getCurrentTransaction())
				.internalToArray(a);
	}

	/**
	 * 
	 * @return
	 */
	private <T> T[] internalToArray(T[] a) {
		return super.toArray(a);
	}

	@Override
	public void trimToSize() {
		if (versionedList == null)
			throw new GraphException("Versioning is not working for this list.");
		versionedList.setValidValue(this, graph.getCurrentTransaction());
		versionedList.getValidValue(graph.getCurrentTransaction())
				.internalTrimToSize();
	}

	/**
	 * 
	 */
	private void internalTrimToSize() {
		super.trimToSize();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object clone() {
		JGraLabList<E> toBeCloned = null;
		if (versionedList == null && graph == null) {
			toBeCloned = this;
		} else {
			toBeCloned = versionedList.getValidValue(graph
					.getCurrentTransaction());
		}
		JGraLabList<E> jgralabList = new JGraLabList<E>();
		jgralabList.setVersionedList(versionedList);
		for (E element : toBeCloned) {
			if (element instanceof JGraLabCloneable && element != null) {
				element = (E) ((JGraLabCloneable) element).clone();
				jgralabList.internalAdd(element);
			} else {
				jgralabList.internalAdd(element);
			}
		}
		return jgralabList;
	}

	@Override
	public Graph getGraph() {
		return graph;
	}

	/**
	 * 
	 * @param String
	 *            name
	 */
	public void setName(String name) {
		this.name = name;
		this.versionedList.setName(this.name);
	}
}
