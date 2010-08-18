package de.uni_koblenz.jgralab;

/**
 * Interface to mark instances of classes as cloneable.
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 *
 */
public interface JGraLabCloneable extends Cloneable {
	/**
	 * 
	 * @return a deep copy of the current object
	 */
	public JGraLabCloneable clone();
}
