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
package de.uni_koblenz.jgralab.utilities.tg2dot.graph_layout.definition;

import java.util.Set;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

/**
 * A Definition holds attributes defining the style of one set of elements. The
 * set of elements consists of either elements of type {@link Vertex} or
 * {@link Edge}.
 * 
 * @author ist@uni-koblenz.de
 */
public interface Definition {

	/**
	 * Returns to a given attribute name its associated attribute GReQl-query as
	 * string.
	 * 
	 * @param name
	 *            Name of the attribute.
	 * @return Value of the attribute.
	 */
	public String getAttributeValue(String name);

	/**
	 * Sets a attribute with the given name and value. This method will
	 * overwrite any pre-existing attribute with the same attribute name.
	 * 
	 * @param name
	 *            Name of the attribute.
	 * @param value
	 *            Value of the attribute.
	 */
	public void setAttribute(String name, String value);

	/**
	 * Returns a set of attribute names defined by this Definition.
	 * 
	 * @return Set of attribute names.
	 */
	public Set<String> getAttributeNames();

	/**
	 * Overwrites every attribute of this definitions with all attributes of the
	 * given Definition.
	 * 
	 * @param definition
	 *            Definition with additional or even more important attributes.
	 */
	public void overwriteAttributes(Definition definition);

	/**
	 * Adds only non existing attributes from the given definition to his
	 * definition.
	 * 
	 * @param definition
	 *            Definition with additional but less important attributes.
	 */
	public void addNonExistingAttributes(Definition definition);

	/**
	 * Will return a clone of the current definition. All data structures are
	 * clones but not their stores elements are only referenced.
	 * 
	 * @return A cloned definition.
	 */
	public Definition clone();
}
