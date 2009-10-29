package de.uni_koblenz.jgralab.impl.trans;

import de.uni_koblenz.jgralab.AttributedElement; //import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.trans.JGraLabCloneable;

/**
 * This class is responsible for the versioning of cloneable classes in the
 * context of JGraLab. These can be attributes of type JGraLabList, JGraLabSet,
 * JGraLabMap and Record-classes.
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 * 
 * @param <E>
 *            the type
 */
public class VersionedJGraLabCloneableImpl<E extends JGraLabCloneable> extends
		VersionedDataObjectImpl<E> {

	/**
	 * Should be used for attributes.
	 * 
	 * @param graph
	 * @param initialPersistentValue
	 * @param name
	 *            the name of the attribute
	 */
	public VersionedJGraLabCloneableImpl(AttributedElement attributedElement,
			E initialPersistentValue, String name) {
		super(attributedElement, initialPersistentValue, name);
	}

	/**
	 * 
	 * @param graph
	 * @param initialPersistentValue
	 */
	public VersionedJGraLabCloneableImpl(AttributedElement attributedElement,
			E initialPersistentValue) {
		super(attributedElement, initialPersistentValue);
	}
	
	/**
	 * 
	 * @param graph
	 * @param initialPersistentValue
	 * 
	 * TODO this constructor leads to conflict together with the constructor below
	 */
	/*public VersionedJGraLabCloneableImpl(
			E initialPersistentValue) {
		super(null, initialPersistentValue);
	}*/

	/**
	 * 
	 * @param graph
	 * @param name
	 */
	public VersionedJGraLabCloneableImpl(AttributedElement attributedElement,
			String name) {
		super(attributedElement, name);
	}

	/**
	 * 
	 * @param graph
	 */
	public VersionedJGraLabCloneableImpl(AttributedElement attributedElement) {
		super(attributedElement);
	}
	
	public VersionedJGraLabCloneableImpl() {
		super();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E copyOf(E dataObject) {
		if (dataObject == null)
			return null;
		return (E) dataObject.clone();
	}
	
	@Override
	public boolean isCloneable() {
		return true;
	}
}
