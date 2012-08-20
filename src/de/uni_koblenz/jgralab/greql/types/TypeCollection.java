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

package de.uni_koblenz.jgralab.greql.types;

import java.util.Set;
import java.util.TreeSet;

import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;

/**
 * Represents a set of allowed and forbidden types
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class TypeCollection {
	/**
	 * The set of types
	 */
	private TreeSet<GraphElementClass<?, ?>> types;
	private boolean allowed; // types are allowed
	private boolean forbidden; // types are forbidden

	/**
	 * returns the list of allowed types. Creates a copy of that list so the
	 * internal list is not affected by changes of the returned list
	 */
	@Deprecated
	public Set<GraphElementClass<?, ?>> getAllowedTypes() {
		return allowed ? types : new TreeSet<GraphElementClass<?, ?>>();
	}

	/**
	 * returns the list of forbidden types. Creates a copy of that list so the
	 * internal list is not affected by changes of the returned list
	 */
	@Deprecated
	public Set<GraphElementClass<?, ?>> getForbiddenTypes() {
		return forbidden ? types : new TreeSet<GraphElementClass<?, ?>>();
	}

	public boolean isEmpty() {
		return types == null;
	}

	private static TypeCollection empty = new TypeCollection();

	public static TypeCollection empty() {
		return empty;
	}

	public TypeCollection with(GraphElementClass<?, ?> cls, boolean exactType,
			boolean forbidden) {
		return new TypeCollection(cls, exactType, forbidden);
	}

	/**
	 * adds the allowed and forbidden types of the given collection
	 * <code>other</code> to this collection
	 */
	public TypeCollection join(TypeCollection other) {
		if (other == null || other.isEmpty()) {
			return this;
		} else if (isEmpty()) {
			return other;
		} else {
			TypeCollection result = new TypeCollection();
			if (allowed) {
				result.allowed = true;
				result.types = new TreeSet<GraphElementClass<?, ?>>(types);
				if (other.allowed) {
					result.types.addAll(other.types);
				}
			} else {
				if (other.allowed) {
					result.allowed = true;
					result.types = other.types;
				} else {
					result.forbidden = true;
					result.types = new TreeSet<GraphElementClass<?, ?>>(types);
					result.types.addAll(other.types);
				}
			}
			return result;
		}
	}

	/**
	 * creates a new typecollection which contains no types
	 */
	private TypeCollection() {
	}

	/**
	 * creates a new typecollection which contains the given type list.
	 * 
	 * @param types
	 *            the list of types
	 * @param forbidden
	 *            toggles wether the given types should be added to the allowed
	 *            or forbidden types
	 */
	private TypeCollection(GraphElementClass<?, ?> cls, boolean exactType,
			boolean forbidden) {
		if (forbidden) {
			forbidden = true;
		} else {
			allowed = true;
		}

		types = new TreeSet<GraphElementClass<?, ?>>();
		types.add(cls);
		if (!exactType) {
			types.addAll(cls.getAllSubClasses());
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof TypeCollection)) {
			return false;
		}
		TypeCollection col = (TypeCollection) o;
		if (col == this) {
			return true;
		}
		return allowed == col.allowed && forbidden == col.forbidden
				&& types.equals(col.types);
	}

	@Override
	public int hashCode() {
		return (types == null ? 0 : types.hashCode()) ^ (allowed ? 1 : 0)
				^ (forbidden ? 2 : 0);
	}

	/**
	 * Checks wether the given <code>type</code> is allowed by this collection.
	 * The <code>type</code> is allowed if it is part of the allowed types or if
	 * the allowed types ares empty and <code>type</code> is not part of the
	 * forbidden types.
	 * 
	 * @return true if the given type is allowed, false otherwise
	 */
	public final boolean acceptsType(GraphElementClass<?, ?> type) {
		if (types == null) {
			return true;
		}
		return allowed ? types.contains(type) : !types.contains(type);
	}

	@Override
	public String toString() {
		if (types == null || types.isEmpty()) {
			return "{}";
		} else {
			StringBuffer sb = new StringBuffer();
			String delim = "{";
			for (AttributedElementClass<?, ?> aec : types) {
				sb.append(delim).append(allowed ? "+" : "-")
						.append(aec.getQualifiedName());
				delim = " ";
			}
			return sb.append("}").toString();
		}
	}
}
