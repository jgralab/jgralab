/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
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
package org.pcollections;

import java.util.Collection;
import java.util.List;

/**
 * 
 * An immutable, persistent indexed collection.
 * 
 * @author harold
 *
 * @param <E>
 */
public interface PSequence<E> extends PCollection<E>, List<E> {
	
	//@Override
	public PSequence<E> plus(E e);
	
	//@Override
	public PSequence<E> plusAll(Collection<? extends E> list);
	
	/**
	 * @param i
	 * @param e
	 * @return a sequence consisting of the elements of this with e replacing the element at index i.
	 * @throws IndexOutOfBOundsException if i&lt;0 || i&gt;=this.size()
	 */
	public PSequence<E> with(int i, E e);
	
	/**
	 * @param i
	 * @param e non-null
	 * @return a sequence consisting of the elements of this with e inserted at index i.
	 * @throws IndexOutOfBOundsException if i&lt;0 || i&gt;this.size()
	 */
	public PSequence<E> plus(int i, E e);
	
	/**
	 * @param i
	 * @param list
	 * @return a sequence consisting of the elements of this with list inserted at index i.
	 * @throws IndexOutOfBOundsException if i&lt;0 || i&gt;this.size()
	 */
	public PSequence<E> plusAll(int i, Collection<? extends E> list);
	
	/**
	 * Returns a sequence consisting of the elements of this without the first occurrence of e.
	 */
	//@Override
	public PSequence<E> minus(Object e);
	
	//@Override
	public PSequence<E> minusAll(Collection<?> list);
	
	/**
	 * @param i
	 * @return a sequence consisting of the elements of this with the element at index i removed.
	 * @throws IndexOutOfBOundsException if i&lt;0 || i&gt;=this.size()
	 */
	public PSequence<E> minus(int i);

	//@Override
	public PSequence<E> subList(int start, int end);
	
	@Deprecated boolean addAll(int index, Collection<? extends E> c);
	@Deprecated E set(int index, E element);
	@Deprecated void add(int index, E element);
	@Deprecated E remove(int index);
}
