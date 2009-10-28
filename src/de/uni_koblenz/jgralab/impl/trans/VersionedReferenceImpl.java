package de.uni_koblenz.jgralab.impl.trans;

import de.uni_koblenz.jgralab.AttributedElement;

//import de.uni_koblenz.jgralab.Graph;
//import de.uni_koblenz.jgralab.GraphElement;

/**
 * This class is responsible for the versioning of references. References are
 * also immutable types like: - String - Wrapper (Integer, Double, Long) - Enum
 * 
 * @author José Monte(monte@uni-koblenz.de)
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
	 * @param initialPersistentValue
	 * 
	 * TODO this constructor leads to errors, conflict with constructor below
	 */
	/*public VersionedReferenceImpl(
			E initialPersistentValue) {
		super(null, initialPersistentValue);
	}*/

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
}
