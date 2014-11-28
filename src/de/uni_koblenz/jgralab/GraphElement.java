/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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

package de.uni_koblenz.jgralab;

import de.uni_koblenz.jgralab.exception.TemporaryGraphElementException;
import de.uni_koblenz.jgralab.schema.GraphElementClass;

/**
 * aggregates vertices and edges
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public interface GraphElement<SC extends GraphElementClass<SC, IC>, IC extends GraphElement<SC, IC>>
		extends AttributedElement<SC, IC> {

	/**
	 * returns the id of this graph element
	 * 
	 * @return the id of this graph element
	 */
	public int getId();

	/**
	 * returns true if this GraphElement is still present in the Graph (i.e. not
	 * deleted). This check is equivalent to getGraph().containsVertex(this) or
	 * getGraph().containsEdge(this).
	 */
	public boolean isValid();

	/**
	 * removes this graph element
	 */
	public void delete();

	/**
	 * @return true if this GraphElement is temporary
	 */
	public boolean isTemporary();

	/**
	 * For temporary elements: returns a new graph element of the set
	 * preliminary type that takes the place of this temporary element, throws
	 * an exception, if the preliminary type is not set. For non-temporary
	 * elements: returns itself
	 * 
	 * @throws TemporaryGraphElementException
	 *             if it is not possible to bless the element to schemaClass
	 */
	public IC bless();

	/**
	 * For temporary elements: returns a new graph element of
	 * <code>schemaClass</code> that takes the place of this temporary element.
	 * For non-temporary elements: returns itself if schemaClass equals the
	 * schema class of the element, otherwise it throws an exception
	 * 
	 * @param schemaClass
	 * @return a new graph element of <code>schemaClass</code> that takes the
	 *         place of this temporary element.
	 * 
	 * @throws TemporaryGraphElementException
	 *             if it is not possible to bless the element to schemaClass
	 */
	public IC bless(SC schemaClass);

}
