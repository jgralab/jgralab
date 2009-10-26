package de.uni_koblenz.jgralab.impl.trans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.trans.JGraLabCloneable;

/**
 * Own implementation class for attributes of type
 * <code>java.util.List<E></code>.
 * 
 * @author José Monte(monte@uni-koblenz.de)
 * 
 * @param <E>
 * 
 * TODO if E is a mutable reference type elements of List are hard to version
 * too?! (same for JGraLabSet and JGraLabMap) maybe just return a copy in getter
 * for attributes of type List, Set and Map and only allow updating these
 * attributes by using the corresponding setters?!
 */
public class JGraLabList<E> extends ArrayList<E> implements JGraLabCloneable {
	private static final long serialVersionUID = -1881528493844357904L;
	private VersionedJGraLabCloneableImpl<JGraLabList<E>> versionedList;
	private Graph graph;

	/**
	 * 
	 */
	/* protected */public JGraLabList(Graph g) {
		super();
		init(g);
		// versionedList = null;
		// graph = null;
	}

	/**
	 * 
	 * 
	 * @param initialSize
	 */
	/* protected */public JGraLabList(Graph g, int initialSize) {
		super(initialSize);
		init(g);
		// versionedList = null;
		// graph = null;
	}

	@SuppressWarnings("unchecked")
	/* protected */public JGraLabList(Graph g,
			Collection/* <E> */collection) {
		super(collection);
		init(g);
		// versionedList = null;
		// graph = null;
	}

	public JGraLabList(Collection<E> collection) {
		super(collection);
		//versionedList = null;
		//graph = null;
	}
	
	public JGraLabList() {
		super();
		//versionedList = null;
		//graph = null;
	}

	public void init(Graph g) {
		/*Graph g = null;
		if (ae instanceof Graph) {
			g = (Graph) ae;
		} else {
			g = ((GraphElement) ae).getGraph();
		}*/
		if (g == null)
			throw new GraphException(
					"Given attributed element must be instance of Graph or GraphElement.");
		if (!g.hasTransactionSupport())
			throw new GraphException(
					"An instance of JGraLabList can only be created for graphs with transaction support");
		graph = g;
		versionedList = new VersionedJGraLabCloneableImpl<JGraLabList<E>>();
	}

	/**
	 * 
	 * @param versionedList
	 */
	public void setVersionedList(
			VersionedJGraLabCloneableImpl<JGraLabList<E>> versionedList) {
		this.versionedList = versionedList;
		if (versionedList != null)
			this.graph = versionedList.getGraph();
	}

	@Override
	public boolean add(E e) {
		if (versionedList == null)
			return internalAdd(e);
		versionedList.setValidValue(this, graph.getCurrentTransaction());
		return versionedList.getValidValue(graph.getCurrentTransaction())
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
	public void add(int index, E element) {
		if (versionedList == null) {
			internalAdd(index, element);
		} else {
			versionedList.setValidValue(this, graph.getCurrentTransaction());
			versionedList.getValidValue(graph.getCurrentTransaction())
					.internalAdd(index, element);
		}
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
		if (versionedList == null)
			return internalAddAll(c);
		versionedList.setValidValue(this, graph.getCurrentTransaction());
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
		if (versionedList == null)
			return internalAddAll(index, c);
		versionedList.setValidValue(this, graph.getCurrentTransaction());
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
		if (versionedList == null) {
			internalClear();
		} else {
			versionedList.setValidValue(this, graph.getCurrentTransaction());
			versionedList.getValidValue(graph.getCurrentTransaction())
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
		if (versionedList == null)
			return internalContains(o);
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
		if (versionedList == null)
			return internalContainsAll(c);
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
	public void ensureCapacity(int minCapacity) {
		if (versionedList == null) {
			internalEnsureCapacity(minCapacity);
		} else {
			versionedList.setValidValue(this, graph.getCurrentTransaction());
			versionedList.getValidValue(graph.getCurrentTransaction())
					.internalEnsureCapacity(minCapacity);
		}
	}

	/**
	 * 
	 * @param minCapacity
	 */
	private void internalEnsureCapacity(int minCapacity) {
		super.ensureCapacity(minCapacity);
	}

	@Override
	public boolean equals(Object o) {
		if (versionedList == null)
			return internalEquals(o);
		return versionedList.getValidValue(graph.getCurrentTransaction())
				.internalEquals(o);
	}

	/**
	 * 
	 * @param o
	 * @return
	 */
	private boolean internalEquals(Object o) {
		return super.equals(o);
	}

	@Override
	public E get(int index) {
		if (versionedList == null)
			return internalGet(index);
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
		if (versionedList == null)
			return internalIndexOf(o);
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
		if (versionedList == null)
			return internalIsEmpty();
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
		if (versionedList == null)
			return internalIterator();
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
		if (versionedList == null)
			return internalLastIndexOf(o);
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
		if (versionedList == null)
			return internalListIterator();
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
		if (versionedList == null)
			return internalListIterator(index);
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
		if (versionedList == null)
			return internalRemove(index);
		versionedList.setValidValue(this, graph.getCurrentTransaction());
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
		if (versionedList == null)
			return internalRemove(o);
		versionedList.setValidValue(this, graph.getCurrentTransaction());
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
		if (versionedList == null)
			return internalRemoveAll(c);
		versionedList.setValidValue(this, graph.getCurrentTransaction());
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
		if (versionedList == null) {
			internalRemoveRange(fromIndex, toIndex);
		} else {
			versionedList.setValidValue(this, graph.getCurrentTransaction());
			versionedList.getValidValue(graph.getCurrentTransaction())
					.internalRemoveRange(fromIndex, toIndex);
		}
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
		if (versionedList == null)
			return internalRetainAll(c);
		versionedList.setValidValue(this, graph.getCurrentTransaction());
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
		if (versionedList == null)
			return internalSet(index, element);
		versionedList.setValidValue(this, graph.getCurrentTransaction());
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
		if (versionedList == null)
			return internalSize();
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
		if (versionedList == null)
			return internalSubList(fromIndex, toIndex);
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
		if (versionedList == null)
			return internalToArray();
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
		if (versionedList == null)
			return internalToArray(a);
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
		if (versionedList == null) {
			internalTrimToSize();
		} else {
			versionedList.setValidValue(this, graph.getCurrentTransaction());
			versionedList.getValidValue(graph.getCurrentTransaction())
					.internalTrimToSize();
		}
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
		if (versionedList == null && graph == null)
			toBeCloned = this;
		else
			toBeCloned = versionedList.getValidValue(graph
					.getCurrentTransaction());
		JGraLabList<E> jgralabList = new JGraLabList<E>();
		jgralabList.setVersionedList(versionedList);
		for (E element : toBeCloned) {
			if (element instanceof JGraLabCloneable)
				jgralabList.add((E) ((JGraLabCloneable) element).clone());
			else
				jgralabList.add(element);
		}
		return jgralabList;
	}
}
