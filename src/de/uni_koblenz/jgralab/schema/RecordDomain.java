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

package de.uni_koblenz.jgralab.schema;

import java.util.Collection;

/**
 * Represents a RecordDomain, instances may exist multiple times per schema.
 * 
 * @author ist@uni-koblenz.de
 */
public interface RecordDomain extends CompositeDomain {

	public static class RecordComponent {
		private String name;
		private Domain domain;

		public RecordComponent(String name, Domain domain) {
			this.name = name;
			this.domain = domain;
		}

		@Override
		public String toString() {
			return getName() + ": " + getDomain();
		}

		public String getName() {
			return name;
		}

		public Domain getDomain() {
			return domain;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof RecordComponent)) {
				return false;
			}
			RecordComponent other = (RecordComponent) obj;
			return name.equals(other.name) && domain.equals(other.domain);
		}

		@Override
		public int hashCode() {
			int x = 31;
			x = x * name.hashCode() + x;
			x = x * domain.hashCode() + x;
			return x;
		}
	}

	/**
	 * @return a map of all the record domain components
	 */
	public Collection<RecordComponent> getComponents();

	/**
	 * @param name
	 *            the name of the record domain component in the record domain
	 * @return the domain of the component with the name name
	 */
	public Domain getDomainOfComponent(String name);

	/**
	 * Adds a record domain component to the internal list
	 * 
	 * @param name
	 *            the unique name of the record domain component in the record
	 *            domain
	 * @param domain
	 *            the domain of the component
	 */
	public void addComponent(String name, Domain domain);

	/**
	 * Returns the standard-implementation-class (folder impl.std)
	 * 
	 * @return java representation of this attribute
	 */
	public String getStandardJavaAttributeImplementationTypeName(
			String schemaRootPackagePrefix);

	public String getSavememJavaAttributeImplementationTypeName(
			String schemaRootPackagePrefix);

	public Class<? extends Object> getM1Class();
}
