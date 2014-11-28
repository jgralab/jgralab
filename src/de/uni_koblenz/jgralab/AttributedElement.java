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

import de.uni_koblenz.jgralab.exception.NoSuchAttributeException;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;

/**
 * superclass of graphs, edges and vertices
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public interface AttributedElement<SC extends AttributedElementClass<SC, IC>, IC extends AttributedElement<SC, IC>>
		extends Comparable<AttributedElement<SC, IC>> {
	/**
	 * @return the {@link AttributedElementClass} of this
	 *         {@link AttributedElement}
	 */
	public SC getAttributedElementClass();

	/**
	 * @return the schema class of this {@link AttributedElement}
	 */
	public Class<? extends IC> getSchemaClass();

	/**
	 * returns the graph containing this element
	 * 
	 * @return the graph containing this element
	 */
	public Graph getGraph();

	/**
	 * @return the {@link GraphClass} of this {@link AttributedElement}
	 */
	public GraphClass getGraphClass();

	/**
	 * Returns the value of an {@link de.uni_koblenz.jgralab.schema.Attribute}
	 * specified by its {@code name}.
	 * 
	 * @param name
	 *            the name of the
	 *            {@link de.uni_koblenz.jgralab.schema.Attribute} to get the
	 *            value for
	 * @return the found {@link de.uni_koblenz.jgralab.schema.Attribute} value
	 * 
	 * @throws NoSuchAttributeException
	 *             if the Attribute {@code name} is not the name of a valid
	 *             {@link de.uni_koblenz.jgralab.schema.Attribute}
	 */
	public <T> T getAttribute(String name) throws NoSuchAttributeException;

	/**
	 * Sets the {@link de.uni_koblenz.jgralab.schema.Attribute} specified by the
	 * given {@code name} to the given value.
	 * 
	 * @param name
	 *            the name of the
	 *            {@link de.uni_koblenz.jgralab.schema.Attribute} to set
	 * @param data
	 *            the new value of
	 *            {@link de.uni_koblenz.jgralab.schema.Attribute}
	 * @throws NoSuchAttributeException
	 */
	public <T> void setAttribute(String name, T data)
			throws NoSuchAttributeException;

	/**
	 * Checks if the given attribute is unset, i.e., it has not aquired a value
	 * in terms of setAttribute().  Default values are considered unset.
	 * 
	 * @param name
	 *            the name of the attribute
	 * @return true, if the attribute value hasn't been set explicitly, false
	 *         otherwise
	 * @throws NoSuchAttributeException
	 *             if the Attribute {@code name} is not the name of a valid
	 *             {@link de.uni_koblenz.jgralab.schema.Attribute}
	 */
	public boolean isUnsetAttribute(String name)
			throws NoSuchAttributeException;

	/**
	 * @return the {@link de.uni_koblenz.jgralab.schema.Schema} this
	 *         {@link AttributedElement} belongs to
	 */
	public Schema getSchema();

	/**
	 * @param cls
	 *            schema class to test
	 * @return true, iff this {@link AttributedElement} is an instance of the
	 *         given schema class {@code cls}.
	 */
	public boolean isInstanceOf(SC cls);
}
