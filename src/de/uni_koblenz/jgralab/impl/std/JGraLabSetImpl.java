package de.uni_koblenz.jgralab.impl.std;

import java.util.Collection;
import java.util.HashSet;

import de.uni_koblenz.jgralab.JGraLabCloneable;
import de.uni_koblenz.jgralab.JGraLabSet;

/**
 * 
 * @author
 * 
 * @param <E>
 */
public class JGraLabSetImpl<E> extends HashSet<E> implements JGraLabSet<E> {

	private static final long serialVersionUID = 5890950480302617008L;

	public JGraLabSetImpl(Collection<? extends E> collection) {
		super(collection);
	}

	public JGraLabSetImpl(int initialCapacity) {
		super(initialCapacity);
	}

	public JGraLabSetImpl() {
		super();
	}

	public JGraLabSetImpl(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	@SuppressWarnings("unchecked")
	@Override
	public JGraLabSetImpl<E> clone() {
		JGraLabSetImpl<E> copy = new JGraLabSetImpl<E>();
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
