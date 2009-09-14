/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
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

package de.uni_koblenz.jgralab.schema;

import java.util.Map;

/**
 * Represents a RecordDomain, instances may exist multiple times per schema.
 * 
 * @author ist@uni-koblenz.de
 */
public interface RecordDomain extends CompositeDomain {

	/**
	 * @return a map of all the record domain components
	 */
	public Map<String, Domain> getComponents();

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
}
