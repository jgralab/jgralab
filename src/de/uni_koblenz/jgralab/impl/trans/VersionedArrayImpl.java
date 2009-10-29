package de.uni_koblenz.jgralab.impl.trans;

import java.lang.reflect.Method;

import de.uni_koblenz.jgralab.AttributedElement; //import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;

/**
 * This class is responsible for the versioning of references. References are
 * also immutable types like: - String - Wrapper (Integer, Double, Long) - Enum
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 * 
 * @param <E>
 *            the type (<b>must be an Array-type</b>)
 * 
 * TODO is it possible to check if E really is of Array-type?
 */
public class VersionedArrayImpl<E> extends VersionedDataObjectImpl<E> {

	/**
	 * Should be used for attributes.
	 * 
	 * @param graph
	 * @param initialPersistentValue
	 * @param name
	 *            the name of the attribute
	 */
	protected VersionedArrayImpl(AttributedElement attributedElement,
			E initialPersistentValue, String name) {
		super(attributedElement, initialPersistentValue, name);
	}

	/**
	 * 
	 * @param graph
	 * @param initialPersistentValue
	 */
	protected VersionedArrayImpl(AttributedElement attributedElement,
			E initialPersistentValue) {
		super(attributedElement, initialPersistentValue);
	}

	/**
	 * 
	 * @param graph
	 * @param name
	 */
	protected VersionedArrayImpl(AttributedElement attributedElement,
			String name) {
		super(attributedElement, name);
	}

	/**
	 * 
	 * @param graph
	 * @param name
	 */
	protected VersionedArrayImpl(AttributedElement attributedElement) {
		super(attributedElement);
	}

	@SuppressWarnings("unchecked")
	@Override
	public E copyOf(E dataObject) {
		if (dataObject == null)
			return null;
		try {
			// superclass of every Array is Object.
			Method cloneMethod = dataObject.getClass().getSuperclass()
					.getDeclaredMethod("clone");
			// / !!! set accessible to true, otherwise it won't work
			cloneMethod.setAccessible(true);
			return (E) cloneMethod.invoke(dataObject, new Object[] {});
		} catch (Exception e) {
			e.printStackTrace();
			throw new GraphException(e.getMessage());
		}
	}

	@Override
	public boolean isCloneable() {
		return false;
	}
}
