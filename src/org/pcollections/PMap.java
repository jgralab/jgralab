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
import java.util.Map;

/**
 * 
 * An immutable, persistent map from non-null keys of type K to non-null values of type V.
 * 
 * @author harold
 *
 * @param <K>
 * @param <V>
 */
public interface PMap<K,V> extends Map<K,V> {
	/**
	 * @param key non-null
	 * @param value non-null
	 * @return a map with the mappings of this but with key mapped to value
	 */
	public PMap<K,V> plus(K key, V value);
	
	/**
	 * @param map
	 * @return this combined with map, with map's mappings used for any keys in both map and this
	 */
	public PMap<K,V> plusAll(Map<? extends K, ? extends V> map);
	
	/**
	 * @param key
	 * @return a map with the mappings of this but with no value for key
	 */
	public PMap<K,V> minus(Object key);
	
	/**
	 * @param keys
	 * @return a map with the mappings of this but with no value for any element of keys
	 */
	public PMap<K,V> minusAll(Collection<?> keys);
	
	@Deprecated V put(K k, V v);
	@Deprecated V remove(Object k);
	@Deprecated void putAll(Map<? extends K, ? extends V> m);
	@Deprecated void clear();
}
