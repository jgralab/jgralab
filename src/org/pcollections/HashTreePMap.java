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

import java.util.Map;
import java.util.Map.Entry;




/**
 *
 * A static convenience class for creating efficient persistent maps.
 * <p>
 * This class simply creates HashPMaps backed by IntTreePMaps.
 * 
 * @author harold
 */
public final class HashTreePMap {
	// not instantiable (or subclassable):
	private HashTreePMap() {}
	
	private static final HashPMap<Object,Object> EMPTY
		= HashPMap.empty(IntTreePMap.<PSequence<Entry<Object,Object>>>empty());

	/**
	 * @param <K>
	 * @param <V>
	 * @return an empty map
	 */
	@SuppressWarnings("unchecked")
	public static <K,V> HashPMap<K,V> empty() {
		return (HashPMap<K,V>)EMPTY; }
	
	/**
	 * @param <K>
	 * @param <V>
	 * @param key
	 * @param value
	 * @return empty().plus(key, value)
	 */
	public static <K,V> HashPMap<K,V> singleton(final K key, final V value) {
		return HashTreePMap.<K,V>empty().plus(key, value); }
	
	/**
	 * @param <K>
	 * @param <V>
	 * @param map
	 * @return empty().plusAll(map)
	 */
	public static <K,V> HashPMap<K,V> from(final Map<? extends K, ? extends V> map) {
		return HashTreePMap.<K,V>empty().plusAll(map); }
}
