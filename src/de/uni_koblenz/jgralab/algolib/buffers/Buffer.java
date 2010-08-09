package de.uni_koblenz.jgralab.algolib.buffers;

/**
 * This class specifies a generic buffer that can be used for generic graph
 * search. E.g. the BFS problem can be solved by wrapping a Queue in a class
 * implementing this interface.
 * 
 * @author strauss@uni-koblenz.de
 * 
 * @param <T>
 *            the type of objects that this buffer can hold.
 */
public interface Buffer<T> {

	/**
	 * Checks if this Buffer is empty.
	 * 
	 * @return true if the Buffer is empty.
	 */
	public boolean isEmpty();

	/**
	 * Inserts a new element to this Buffer.
	 * 
	 * @param element
	 *            the element to insert.F
	 */
	public void put(T element);

	/**
	 * Retrieves and removes the next element from this Buffer.
	 * 
	 * @return the next element.
	 */
	public T getNext();

}
