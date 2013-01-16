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

/**
 * 
 * An immutable, persistent list.
 * 
 * @author harold
 *
 * @param <E>
 */
public interface PVector<E> extends PSequence<E> {
	
	/**
	 * Returns a vector consisting of the elements of this with e appended.
	 */
	//@Override
	public PVector<E> plus(E e);
	
	/**
	 * Returns a vector consisting of the elements of this with list appended.
	 */
	//@Override
	public PVector<E> plusAll(Collection<? extends E> list);
	
	//@Override
	public PVector<E> with(int i, E e);
	
	//@Override
	public PVector<E> plus(int i, E e);

	//@Override
	public PVector<E> plusAll(int i, Collection<? extends E> list);
	
	//@Override
	public PVector<E> minus(Object e);
	
	//@Override
	public PVector<E> minusAll(Collection<?> list);

	//@Override
	public PVector<E> minus(int i);

	//@Override
	public PVector<E> subList(int start, int end);
}
