package org.pcollections;

import java.util.LinkedHashSet;

/**
 * Like {@link PSet} but preserves insertion order. Persistent equivalent of
 * {@link LinkedHashSet}.
 * 
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 * @param <E>
 */
public interface POrderedSet<E> extends PSet<E> {
	E get(int index);

	int indexOf(Object o);
}
