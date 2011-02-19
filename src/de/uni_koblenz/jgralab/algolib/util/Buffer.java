/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 * 
 * Additional permission under GNU GPL version 3 section 7
 * 
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
package de.uni_koblenz.jgralab.algolib.util;

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
