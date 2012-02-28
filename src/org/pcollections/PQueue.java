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
/**
 * 
 */
package org.pcollections;

import java.util.Collection;
import java.util.Queue;

/**
 * 
 * A persistent queue.
 * 
 * @author mtklein
 */
public interface PQueue<E> extends PCollection<E>, Queue<E> {
	// TODO i think PQueue should extend PSequence,
	// even though the methods will be inefficient -- H

	/* Guaranteed to stay as a PQueue, i.e. guaranteed-fast methods */
	public PQueue<E> minus();

	public PQueue<E> plus(E e);

	public PQueue<E> plusAll(Collection<? extends E> list);

	/* May switch to other PCollection, i.e. may-be-slow methods */
	public PCollection<E> minus(Object e);

	public PCollection<E> minusAll(Collection<?> list);

	@Deprecated
	boolean offer(E o);

	@Deprecated
	E poll();

	@Deprecated
	E remove();
}
