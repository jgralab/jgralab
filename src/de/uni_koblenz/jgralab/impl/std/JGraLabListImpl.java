package de.uni_koblenz.jgralab.impl.std;

import java.util.ArrayList;
import java.util.Collection;

import de.uni_koblenz.jgralab.JGraLabCloneable;

/**
 * 
 * @author
 * 
 * @param <E>
 */
public class JGraLabListImpl<E> extends ArrayList<E> implements
		de.uni_koblenz.jgralab.JGraLabList<E> {

	public JGraLabListImpl(int initialCapacity) {
		super(initialCapacity);
	}

	public JGraLabListImpl(Collection<? extends E> collection) {
		super(collection);
	}

	public JGraLabListImpl() {
		super();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	@Override
	public Object clone() {
		JGraLabListImpl<E> copy = new JGraLabListImpl<E>();
		for (E element : this) {
			if (element instanceof JGraLabCloneable) {
				copy.add((E) ((JGraLabCloneable) element).clone());
			} else {
				copy.add(element);
			}
		}
		return copy;
	}
}
