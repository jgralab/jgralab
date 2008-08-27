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
 
package de.uni_koblenz.jgralab;

import java.io.IOException;

import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;

/**
 * aggregates graphs, edges and vertices 
 * @author Steffen Kahle
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
	
	public void writeAttributeValues(GraphIO io) throws IOException, GraphIOException;
	
	public void readAttributeValues(GraphIO io) throws GraphIOException;
	
	public Object getAttribute(String name) throws NoSuchFieldException;
	
	public void setAttribute(String name, Object data) throws NoSuchFieldException;
	
	/**
	 * @return the schema this AttributedElement belongs to
	 */
	public Schema getSchema();
	
	
}