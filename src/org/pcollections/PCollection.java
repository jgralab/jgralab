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
 * An immutable, persistent collection of non-null elements of type E.
 * 
 * @author harold
 *
 * @param <E>
 */
public interface PCollection<E> extends Collection<E> {
	
	/**
	 * @param e non-null
	 * @return a collection which contains e and all of the elements of this
	 */
	public PCollection<E> plus(E e);

	/**
	 * @param list contains no null elements
	 * @return a collection which contains all of the elements of list and this
	 */
	public PCollection<E> plusAll(Collection<? extends E> list);
	
	/**
	 * @param e
	 * @return this with a single instance of e removed, if e is in this
	 */
	public PCollection<E> minus(Object e);
	
	/**
	 * @param list
	 * @return this with all elements of list completely removed
	 */
	public PCollection<E> minusAll(Collection<?> list);
	
	// TODO public PCollection<E> retainingAll(Collection<?> list);
	
	@Deprecated boolean add(E o);
	@Deprecated boolean remove(Object o);
	@Deprecated boolean addAll(Collection<? extends E> c);
	@Deprecated boolean removeAll(Collection<?> c);
	@Deprecated boolean retainAll(Collection<?> c);
	@Deprecated void clear();
}
