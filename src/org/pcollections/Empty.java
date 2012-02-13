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

/* Mike Klein, 2/27/2009 */

/* Empty remembers which classes implement the interface you want,
 * so you don't have to.
 */

/**
 * A static utility class for getting empty PCollections backed by the 'default'
 * implementations.
 * 
 * @author mtklein
 * 
 */
public final class Empty {
	// non-instantiable:
	private Empty() {
	}

	public static <E> PStack<E> stack() {
		return ConsPStack.empty();
	}

	public static <E> PQueue<E> queue() {
		return AmortizedPQueue.empty();
	}

	public static <E> PVector<E> vector() {
		return TreePVector.empty();
	}

	public static <E> PSet<E> set() {
		return HashTreePSet.empty();
	}

	public static <E> POrderedSet<E> orderedSet() {
		return OrderedPSet.empty();
	}

	public static <K, V> POrderedMap<K, V> orderedMap() {
		return ArrayPMap.empty();
	}

	public static <E> PBag<E> bag() {
		return HashTreePBag.empty();
	}

	public static <K, V> PMap<K, V> map() {
		return HashTreePMap.empty();
	}
}
