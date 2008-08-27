/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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
 
package de.uni_koblenz.jgralab.impl;

import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.schema.Domain;

public class AttributeImpl implements Attribute, Comparable<Attribute> {
	
	/**
	 * the name of the attribute 
	 */
	private String name;
	
	/**
	 * the domain of the attribute 
	 */
	private Domain domain;
	
	/**
	 * defines a total order of all attributes  
	 */
	private String sortKey;
	
	/**
	 * builds a new attribute
	 * @param name the name of the attribute
	 * @param domain the domain of the attribute
	 */
	public AttributeImpl(String name, Domain domain) {
		this.name = name;
		this.domain = domain;
		this.sortKey = name + ":" + domain.getQualifiedName();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "attribute " + sortKey;
	}

	/* (non-Javadoc)
	 * @see jgralab.Attribute#getDomain()
	 */
	public Domain getDomain() {
		return domain;
	}

	/* (non-Javadoc)
	 * @see jgralab.Attribute#getName()
	 */
	public String getName() {
		return name;
	}

	public boolean equals(Object o) {
		if (!(o instanceof AttributeImpl)) return false;
		return sortKey.equals(((AttributeImpl)o).getSortKey());
	}

	public int hashCode() {
		return sortKey.hashCode();
	}
	
	public int compareTo(Attribute o) {
		return sortKey.compareTo(o.getSortKey());
	}
	
	public String getSortKey() {
		return sortKey; 
	}
}
