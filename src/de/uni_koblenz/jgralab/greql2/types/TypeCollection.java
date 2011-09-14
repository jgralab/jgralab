/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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

package de.uni_koblenz.jgralab.greql2.types;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import de.uni_koblenz.jgralab.schema.AttributedElementClass;

/**
 * Represents a set of allowed and forbidden types
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class TypeCollection {
	/**
	 * The set of allowed types
	 */
	private TreeSet<AttributedElementClass> allowedTypes;

	/**
	 * The set of forbidden types
	 */
	private TreeSet<AttributedElementClass> forbiddenTypes;

	/**
	 * returns the list of allowed types. Creates a copy of that list so the
	 * internal list is not affected by changes of the returned list
	 */
	public Set<AttributedElementClass> getAllowedTypes() {
		return allowedTypes;
	}

	/**
	 * returns the list of forbidden types. Creates a copy of that list so the
	 * internal list is not affected by changes of the returned list
	 */
	public Set<AttributedElementClass> getForbiddenTypes() {
		return forbiddenTypes;
	}

	/**
	 * creates a new typecollection which contains no types
	 */
	public TypeCollection() {
		forbiddenTypes = new TreeSet<AttributedElementClass>();
		allowedTypes = new TreeSet<AttributedElementClass>();
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
	public TypeCollection(Collection<AttributedElementClass> types,
			boolean forbidden) {
		this();
		if (forbidden) {
			forbiddenTypes.addAll(types);
		} else {
			allowedTypes.addAll(types);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof TypeCollection)) {
			return false;
		}
		TypeCollection col = (TypeCollection) o;
		return forbiddenTypes.equals(col.forbiddenTypes)
				&& allowedTypes.equals(col.allowedTypes);
	}

	@Override
	public int hashCode() {
		return forbiddenTypes.hashCode() + allowedTypes.hashCode();
	}

	/**
	 * creates a copy of the given type collection
	 */
	public TypeCollection(TypeCollection other) {
		this();
		forbiddenTypes = other.forbiddenTypes;
		allowedTypes = other.allowedTypes;
	}

	/**
	 * adds the allowed and forbidden types of the given collection
	 * <code>other</code> to this collection
	 */
	public void addTypes(TypeCollection other) {
		if (other != null) {
			forbiddenTypes.addAll(other.forbiddenTypes);
			allowedTypes.addAll(other.allowedTypes);
			allowedTypes.removeAll(forbiddenTypes);
		}
	}

	/**
	 * Checks wether the given <code>type</code> is allowed by this collection.
	 * The <code>type</code> is allowed if it is part of the allowed types or if
	 * the allowed types ares empty and <code>type</code> is not part of the
	 * forbidden types.
	 * 
	 * @return true if the given type is allowed, false otherwise
	 */
	public final boolean acceptsType(AttributedElementClass type) {
		if (allowedTypes.isEmpty()) {
			return (!forbiddenTypes.contains(type));
		} else {
			return allowedTypes.contains(type);
		}
	}

	@Override
	public String toString() {
		if (allowedTypes.isEmpty() && forbiddenTypes.isEmpty()) {
			return "{}";
		} else {
			StringBuffer sb = new StringBuffer();
			String delim = "{";
			for (AttributedElementClass aec : allowedTypes) {
				sb.append(delim).append("+").append(aec.getQualifiedName());
				delim = " ";
			}
			for (AttributedElementClass aec : forbiddenTypes) {
				sb.append(delim).append("-").append(aec.getQualifiedName());
				delim = " ";
			}
			return sb.append("}").toString();
		}
	}
}
