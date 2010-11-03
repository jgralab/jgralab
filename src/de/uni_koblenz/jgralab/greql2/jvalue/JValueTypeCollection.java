/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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

package de.uni_koblenz.jgralab.greql2.jvalue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.uni_koblenz.jgralab.schema.AttributedElementClass;

/**
 * Represents a set of allowed and forbidden types
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class JValueTypeCollection extends JValueImpl {

	/**
	 * The list of allowed types
	 */
	private Set<AttributedElementClass> allowedTypes;

	/**
	 * The list of forbidden types
	 */
	private Set<AttributedElementClass> forbiddenTypes;

	/**
	 * returns the list of allowed types. Creates a copy of that list so the
	 * internal list is not affected by changes of the returned list
	 */
	public Set<AttributedElementClass> getAllowedTypes() {
		return new HashSet<AttributedElementClass>(allowedTypes);
	}

	@Override
	public JValueTypeCollection toObject() {
		return this;
	}

	/**
	 * returns the list of forbidden types. Creates a copy of that list so the
	 * internal list is not affected by changes of the returned list
	 */
	public Set<AttributedElementClass> getForbiddenTypes() {
		return new HashSet<AttributedElementClass>(forbiddenTypes);
	}

	/**
	 * creates a new typecollection which contains no types
	 */
	public JValueTypeCollection() {
		forbiddenTypes = new HashSet<AttributedElementClass>();
		allowedTypes = new HashSet<AttributedElementClass>();
		this.type = JValueType.TYPECOLLECTION;
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
	public JValueTypeCollection(Collection<AttributedElementClass> types,
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
		if (!(o instanceof JValueTypeCollection)) {
			return false;
		}
		JValueTypeCollection col = (JValueTypeCollection) o;
		if (forbiddenTypes.size() != col.forbiddenTypes.size()) {
			return false;
		}
		if (allowedTypes.size() != col.allowedTypes.size()) {
			return false;
		}
		if (!forbiddenTypes.containsAll(col.forbiddenTypes)) {
			return false;
		}
		if (!allowedTypes.containsAll(col.allowedTypes)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int x = 379;
		x = x * forbiddenTypes.hashCode() + x;
		x = x * allowedTypes.hashCode() + x;
		return x;
	}

	/**
	 * creates a copy of the given type collection
	 */
	public JValueTypeCollection(JValueTypeCollection other) {
		this();
		addTypes(other);
	}

	/**
	 * adds the allowed and forbidden types of the given collection
	 * <code>other</code> to this collection
	 */
	public void addTypes(JValueTypeCollection other) {
		if (other != null) {
			forbiddenTypes.addAll(other.forbiddenTypes);
			allowedTypes.addAll(other.allowedTypes);
			allowedTypes.removeAll(forbiddenTypes);
		}
	}

	/**
	 * Checks wether the given type is allowed by this collection. The type T is
	 * allowed if it is part of the allowedTypeList or if the allowedTypeList is
	 * empty and T is not part of the forbidden types
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

	/**
	 * returns a string representation of this path
	 */
	@Override
	public String toString() {
		Iterator<AttributedElementClass> allowedIter = allowedTypes.iterator();
		Iterator<AttributedElementClass> forbiddenIter = forbiddenTypes
				.iterator();
		StringBuffer returnString = new StringBuffer();
		returnString.append("    Allowed Types are: \n");
		while (allowedIter.hasNext()) {
			returnString.append("        "
					+ allowedIter.next().getQualifiedName() + "\n");
		}
		returnString.append("    Forbidden Types are:  \n");
		while (forbiddenIter.hasNext()) {
			returnString.append("        "
					+ forbiddenIter.next().getQualifiedName() + "\n");
		}
		return returnString.toString();
	}

	public String typeString() {
		Iterator<AttributedElementClass> allowedIter = allowedTypes.iterator();
		Iterator<AttributedElementClass> forbiddenIter = forbiddenTypes
				.iterator();
		StringBuffer returnString = new StringBuffer();
		returnString.append("Allowed:");
		while (allowedIter.hasNext()) {
			returnString.append(":" + allowedIter.next().getQualifiedName());
		}
		returnString.append("||Forbidden:");
		while (forbiddenIter.hasNext()) {
			returnString.append(":" + forbiddenIter.next().getQualifiedName());
		}
		return returnString.toString();
	}

	/**
	 * accepts te given visitor to visit this jvalue
	 */
	@Override
	public void accept(JValueVisitor v) {
		v.visitTypeCollection(this);
	}

}
