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
 
package de.uni_koblenz.jgralab.impl;

import de.uni_koblenz.jgralab.Domain;

public abstract class DomainImpl implements Domain, Comparable<Domain> {

	private String name;
	
	protected DomainImpl(String name) {
		this.name = name;
	}
	
	/* (non-Javadoc)
	 * @see jgralab.Domain#equals(jgralab.Domain)
	 */
	public boolean equals(Domain o) {
		if (o instanceof Domain) {
			return name.equals(((Domain)o).getName());
		}
		return false;
	}
	
	public int hashCode() {
		return name.hashCode();
	}
	
	/* (non-Javadoc)
	 * @see jgralab.Domain#toJavaString()
	 */
	public abstract String getJavaAttributeImplementationTypeName();
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "domain " + name;
	}

	
	public int compareTo(Domain o) {
		return name.compareTo(o.getName());
	}

	public String getName() {
		return name;
	}
}
