/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 * 
 *               ist@uni-koblenz.de
 * 
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
