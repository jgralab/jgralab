/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2012 Institute for Software Technology
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

import java.io.IOException;

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
	 * @return the {@link GraphClass} of this {@link AttributedElement}
	 */
	public GraphClass getGraphClass();

	/**
	 * Reads a new value for an {@link de.uni_koblenz.jgralab.schema.Attribute} from a 
	 * {@code String} value and sets it
	 * 
	 * @param attributeName 
	 * 				name of the {@link  de.uni_koblenz.jgralab.schema.Attribute} to set
	 * @param value
	 * 				the {@code String} value to read
	 * 
	 * @throws GraphIOException
	 * 				if the {@code value} can not become parsed correctly
	 * @throws NoSuchAttributeException 
	 * 				if the {@code attributeName} is not the name of a valid 
	 * 				{@link  de.uni_koblenz.jgralab.schema.Attribute}
	 */
	public void readAttributeValueFromString(String attributeName, String value)
			throws GraphIOException, NoSuchAttributeException;

	/**
	 * Creates a new {@code String} value from the current value of the given 
	 * {@link de.uni_koblenz.jgralab.schema.Attribute}
	 * 
	 * @param attributeName 
	 * 				name of the {@link  de.uni_koblenz.jgralab.schema.Attribute} 
	 * 				to create a {@link java.lang.String} value of
	 * @return the String representation of the given 
	 * 			{@link  de.uni_koblenz.jgralab.schema.Attribute} value
	 * 
	 * @throws IOException
	 * @throws GraphIOException
	 * @throws NoSuchAttributeException
	 * 				if the {@code attributeName} is not the name of a valid 
	 * 				{@link  de.uni_koblenz.jgralab.schema.Attribute}
	 */
	public String writeAttributeValueToString(String attributeName)
			throws IOException, GraphIOException, NoSuchAttributeException;

	/**Creates a new {@code String} value from the current value of the given 
	 * {@link de.uni_koblenz.jgralab.schema.Attribute} with the given {@link GraphIO}
	 * 
	 * @param io 
	 * 				the {@link GraphIO} to create the {@code String} value with
	 * @throws IOException
	 * @throws GraphIOException
	 */
	public void writeAttributeValues(GraphIO io) throws IOException,
			GraphIOException;

	/**
	 * Reads a new value for an {@link de.uni_koblenz.jgralab.schema.Attribute} from a 
	 * {@link GraphIO} value and sets it
	 * 
	 * @param io
	 * 				the {@link GraphIO} object to read from
	 * @throws GraphIOException
	 * 				if the {@link GraphIO} can not become read correctly
	 */
	public void readAttributeValues(GraphIO io) throws GraphIOException;

	/**
	 * Returns the value of an {@link de.uni_koblenz.jgralab.schema.Attribute}
	 * specified by its {@code name}.
	 * 
	 * @param name
	 * 				the name of the {@link de.uni_koblenz.jgralab.schema.Attribute} 
	 * 				to get the value for
	 * @return the found {@link de.uni_koblenz.jgralab.schema.Attribute} value
	 *  
	 * @throws NoSuchAttributeException
	 * 				if the {@code attributeName} is not the name of a valid 
	 * 				{@link  de.uni_koblenz.jgralab.schema.Attribute}
	 */
	public <T> T getAttribute(String name) throws NoSuchAttributeException;

	/**
	 * Sets the {@link de.uni_koblenz.jgralab.schema.Attribute} specified by the
	 * given {@code name} to the given value.
	 *  
	 * @param name
	 * 				the name of the {@link de.uni_koblenz.jgralab.schema.Attribute}
	 * 				to set
	 * @param data 
	 * 				the new value of {@link de.uni_koblenz.jgralab.schema.Attribute}
	 * @throws NoSuchAttributeException
	 */
	public <T> void setAttribute(String name, T data)
			throws NoSuchAttributeException;

	/**
	 * @return the {@link de.uni_koblenz.jgralab.schema.Schema} this 
	 * 			{@link AttributedElement} belongs to
	 */
	public Schema getSchema();

	/**
	 * Initializes all values for all {@link de.uni_koblenz.jgralab.schema.Attribute}s 
	 * with their {@code default} values.
	 */
	void initializeAttributesWithDefaultValues();

	/**
	 * @param cls
	 * 				schema class to test
	 * @return true, iff this {@link AttributedElement} is an instance of the 
	 * 			given schema class {@code cls}.
	 */
	public boolean isInstanceOf(SC cls);
}
