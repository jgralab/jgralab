package de.uni_koblenz.jgralab.impl.trans;

import de.uni_koblenz.jgralab.AttributedElement;

/**
 * This class is responsible for the versioning of references. References are
 * also immutable types like: - String - Wrapper (Integer, Double, Long) - Enum
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 * 
 * @param <E>
 *            the type
 */
public class VersionedReferenceImpl<E> extends VersionedDataObjectImpl<E> {

	/**
	 * Should be used for attributes.
	 * 
	 * @param graph
	 * @param initialPersistentValue
	 * @param name
	 *            the name of the attribute
	 */
	public VersionedReferenceImpl(AttributedElement attributedElement,
			E initialPersistentValue, String name) {
		super(attributedElement, initialPersistentValue, name);
	}

	/**
	 * 
	 * @param graph
	 * @param initialPersistentValue
	 */
	public VersionedReferenceImpl(AttributedElement attributedElement,
			E initialPersistentValue) {
		super(attributedElement, initialPersistentValue);
	}

	/**
	 * 
	 * @param graph
	 */
	public VersionedReferenceImpl(AttributedElement attributedElement) {
		super(attributedElement);
	}

	@Override
	public E copyOf(E dataObject) {
		return dataObject;
	}

	@Override
	public boolean isCloneable() {
		return false;
	}
}
