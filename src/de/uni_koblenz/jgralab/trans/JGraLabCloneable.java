package de.uni_koblenz.jgralab.trans;

/**
 * This interface declares the clone()-method missing in Java-interface
 * <code>java.lang.Cloneable</code> to allow deep copying of attributes of type
 * <code>java.util.List<E></code>, <code>java.util.Set<E></code>,
 * <code>java.util.Map<K,V></code> and Record within JGraLab.
 * 
 * @author José Monte(monte@uni-koblenz)
 */
public interface JGraLabCloneable extends Cloneable {
	/**
	 * 
	 * @return a deep copy of the current object
	 */
	public Object clone();
}
