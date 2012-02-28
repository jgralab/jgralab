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
 * A static convenience class for creating efficient persistent sets.
 * <p>
 * This class simply creates MapPSets backed by HashTreePMaps.
 * 
 * @author harold
 */
public final class HashTreePSet {
	// not instantiable (or subclassable):
	private HashTreePSet() {}
	
	private static final MapPSet<Object> EMPTY = MapPSet.from(HashTreePMap.empty());

	/**
	 * @param <E>
	 * @return an empty set
	 */
	@SuppressWarnings("unchecked")
	public static <E> MapPSet<E> empty() {
		return (MapPSet<E>)EMPTY; }
	
	/**
	 * @param <E>
	 * @param e
	 * @return empty().plus(e)
	 */
	public static <E> MapPSet<E> singleton(final E e) {
		return HashTreePSet.<E>empty().plus(e); }
	
	/**
	 * @param <E>
	 * @param list
	 * @return empty().plusAll(map)
	 */
	public static <E> MapPSet<E> from(final Collection<? extends E> list) {
		return HashTreePSet.<E>empty().plusAll(list); }
}
