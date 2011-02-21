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

package de.uni_koblenz.jgralab;

import java.io.IOException;

import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;

/**
 * aggregates graphs, edges and vertices
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public interface AttributedElement extends Comparable<AttributedElement> {
	/**
	 * @return the corresponding m2-element to this m1-element
	 */
	public AttributedElementClass getAttributedElementClass();

	/**
	 * 
	 * @return the m1-class of this attributedelement
	 */
	public Class<? extends AttributedElement> getM1Class();

	public GraphClass getGraphClass();

	public void readAttributeValueFromString(String attributeName, String value)
			throws GraphIOException, NoSuchAttributeException;

	public String writeAttributeValueToString(String attributeName)
			throws IOException, GraphIOException, NoSuchAttributeException;

	public void writeAttributeValues(GraphIO io) throws IOException,
			GraphIOException;

	public void readAttributeValues(GraphIO io) throws GraphIOException;

	public Object getAttribute(String name) throws NoSuchAttributeException;

	public void setAttribute(String name, Object data)
			throws NoSuchAttributeException;

	/**
	 * @return the schema this AttributedElement belongs to
	 */
	public Schema getSchema();

	void initializeAttributesWithDefaultValues();
}
