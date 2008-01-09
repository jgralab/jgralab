/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 *
 *               ist@uni-koblenz.de
 *
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
 
package de.uni_koblenz.jgralab.greql2.jvalue;

import java.util.*;
import de.uni_koblenz.jgralab.*;

/**
 * Represents a set of allowed and forbidden types
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> November 2006
 * 
 */
public class JValueTypeCollection extends JValue {

	/**
	 * The list of allowed types
	 */
	private Set<AttributedElementClass> allowedTypes;

	/**
	 * The list of forbidden types
	 */
	private Set<AttributedElementClass> forbiddenTypes;
	
	/**
	 * returns the list of forbidden types. Creates a copy of that list so the
	 * internal list is not affected by changes of the returned list
	 */
	public Set<AttributedElementClass> getAllowedTypes() {
		return new HashSet<AttributedElementClass>(allowedTypes);
	}
	
	/**
	 * returns the list of forbidden types
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
	 * @param types the list of types
	 * @param forbidden toggles wether the given types should be added to the allowed or forbidden types 
	 */
	public JValueTypeCollection(Collection<AttributedElementClass> types, boolean forbidden) {
		this();
		if (forbidden) {
			forbiddenTypes.addAll(types);
		} else {
			allowedTypes.addAll(types);
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (! (o instanceof JValueTypeCollection))
			return false;
		JValueTypeCollection col = (JValueTypeCollection) o;
		if (forbiddenTypes.size() != col.forbiddenTypes.size())
			return false;
		if (allowedTypes.size() != col.allowedTypes.size())
			return false;
		if (!forbiddenTypes.containsAll(col.forbiddenTypes))
			return false;
		if (!allowedTypes.containsAll(col.allowedTypes))
			return false;
		return true;
	}
	
	/**
	 * creates a copy of the given type collection
	 */
	public JValueTypeCollection(JValueTypeCollection other) {
		this();
		addTypes(other);
	}
	
	/**
	 * adds the allowed and forbidden types of the given collection <code>other</code> to this collection
	 */
	public void addTypes(JValueTypeCollection other) {
		if (other != null) {
			forbiddenTypes.addAll(other.forbiddenTypes);
			allowedTypes.addAll(other.allowedTypes);
			allowedTypes.removeAll(forbiddenTypes);
		}	
	}
	
	
	/**
	 * Checks wether the given type is allowed by this collection.
	 * The type T is allowed if it is part of the allowedTypeList or if the 
	 * allowedTypeList is empty and T is not part of the forbidden types
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
	public String toString() {
		Iterator<AttributedElementClass> allowedIter = allowedTypes.iterator();
		Iterator<AttributedElementClass> forbiddenIter = forbiddenTypes.iterator();
		StringBuffer returnString = new StringBuffer();
		returnString.append("    Allowed Types are: \n");
		while (allowedIter.hasNext()) {
			returnString.append("        " + allowedIter.next().getName() + "\n");
		}
		returnString.append("    Forbidden Types are:  \n");
		while (forbiddenIter.hasNext()) {
			returnString.append("        " + forbiddenIter.next().getName() + "\n");
		}
		return returnString.toString();
	}
	
	public String typeString() {
		Iterator<AttributedElementClass> allowedIter = allowedTypes.iterator();
		Iterator<AttributedElementClass> forbiddenIter = forbiddenTypes.iterator();
		StringBuffer returnString = new StringBuffer();
		returnString.append("Allowed:");
		while (allowedIter.hasNext()) {
			returnString.append(":" + allowedIter.next().getName());
		}
		returnString.append("||Forbidden:");
		while (forbiddenIter.hasNext()) {
			returnString.append(":" + forbiddenIter.next().getName());
		}
		return returnString.toString();
	}

	
	/**
	 * accepts te given visitor to visit this jvalue
	 */
	public void accept(JValueVisitor v)  throws Exception {
		v.visitTypeCollection(this);
	}

}
