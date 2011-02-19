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

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;

/**
 * represents an attribute in the m2 layer, consists of a name and a domain
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public interface Attribute {

	/**
	 * @return the textual representation of the attribute
	 */
	public String toString();

	/**
	 * @return the domain of the attribute
	 */
	public Domain getDomain();

	/**
	 * @return the name of the attribute
	 */
	public String getName();

	/**
	 * Returns the default value of this Attribute as String conforming to the
	 * TG representation of the default value.
	 * 
	 * 
	 * @return the default value of this Attribute, or null, if no default value
	 *         was specified
	 */
	public String getDefaultValueAsString();

	/**
	 * Sets the default value of this Attribute as String conforming to the TG
	 * representation of the default value. The default value can be set only
	 * once.
	 * 
	 * @param defaultValue
	 *            the default value of this Attribute in TG syntax
	 * 
	 * @throws SchemaException
	 *             if a default value was already set.
	 */
	public void setDefaultValueAsString(String defaultValue)
			throws SchemaException;

	/**
	 * Set default value for attributed elements with transaction support.
	 * 
	 * @param element
	 * @throws GraphIOException
	 */
	public void setDefaultTransactionValue(AttributedElement element)
			throws GraphIOException;

	/**
	 * Set default value for attributed elements without transaction support.
	 * 
	 * @param element
	 * @throws GraphIOException
	 */
	public void setDefaultValue(AttributedElement el) throws GraphIOException;

	/**
	 * @return the owning AttributedElementClass
	 */
	public AttributedElementClass getAttributedElementClass();

	/**
	 * Returns a String suitable to sort Attributes of an AttributedElement.
	 * 
	 * @return the sort key of this Attribute
	 */
	public String getSortKey();
}
