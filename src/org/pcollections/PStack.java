/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2012 Institute for Software Technology
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

/**
 * 
 * An immutable, persistent stack.
 * 
 * @author harold
 *
 * @param <E>
 */
public interface PStack<E> extends PSequence<E> {
	
	/**
	 * Returns a stack consisting of the elements of this with e prepended.
	 */
	//@Override
	public PStack<E> plus(E e);
	
	/**
	 * Returns a stack consisting of the elements of this with list prepended in reverse.
	 */
	//@Override
	public PStack<E> plusAll(Collection<? extends E> list);
	
	//@Override
	public PStack<E> with(int i, E e);
	
	//@Override
	public PStack<E> plus(int i, E e);
	
	//@Override
	public PStack<E> plusAll(int i, Collection<? extends E> list);
	
	//@Override
	public PStack<E> minus(Object e);
	
	//@Override
	public PStack<E> minusAll(Collection<?> list);

	//@Override
	public PStack<E> minus(int i);

	//@Override
	public PStack<E> subList(int start, int end);
	
	/**
	 * @param start
	 * @return subList(start,this.size())
	 */
	public PStack<E> subList(int start);
}
