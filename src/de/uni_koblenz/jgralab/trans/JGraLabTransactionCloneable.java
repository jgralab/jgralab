package de.uni_koblenz.jgralab.trans;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.JGraLabCloneable;

/**
 * This interface declares the clone()-method missing in Java-interface
 * <code>java.lang.Cloneable</code> to allow deep copying of attributes of type
 * <code>java.util.List<E></code>, <code>java.util.Set<E></code>,
 * <code>java.util.Map<K,V></code> and Record within JGraLab.
 * 
 * @author Jose Monte(monte@uni-koblenz)
 */
public interface JGraLabTransactionCloneable extends JGraLabCloneable {
	
	/**
	 * 
	 * @return a reference to the graph the object belongs to
	 */
	public Graph getGraph();
	
	public void setName(String name);
}
