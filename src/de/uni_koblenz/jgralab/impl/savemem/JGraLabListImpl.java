package de.uni_koblenz.jgralab.impl.savemem;

import java.util.ArrayList;
import java.util.Collection;

import de.uni_koblenz.jgralab.JGraLabCloneable;

/**
 * FIXME This is a 1:1 clone of the code found in std.
 * 
 * @author
 * 
 * @param <E>
 */
public class JGraLabListImpl<E> extends ArrayList<E> implements
		de.uni_koblenz.jgralab.JGraLabList<E> {

	/**
	 * The generated id for serialization.
	 */
	private static final long serialVersionUID = -3622764334130460297L;

	public JGraLabListImpl(int initialCapacity) {
		super(initialCapacity);
	}

	public JGraLabListImpl(Collection<? extends E> collection) {
		super(collection);
	}

	public JGraLabListImpl() {
		super();
	}

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
