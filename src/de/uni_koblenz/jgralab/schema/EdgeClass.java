/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
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

import de.uni_koblenz.jgralab.Edge;

/**
 * Interface for Edge/Aggregation/Composition classes, instances of this class
 * represent an M2 element.
 * 
 * @author ist@uni-koblenz.de
 */
public interface EdgeClass extends GraphElementClass {

	public static final String DEFAULTEDGECLASS_NAME = "Edge";

	/**
	 * adds a superclass to the list of superclasses, all attributes get
	 * inherited from those classes
	 * 
	 * @param superClass
	 *            the edge class to be added to the list of superclasses if an
	 *            attribute name exists in superClass and in this class
	 * 
	 */
	public void addSuperClass(EdgeClass superClass);

		
	public IncidenceClass getFrom();
	
	public IncidenceClass getTo();
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.schema.AttributedElementClass#getM1Class()
	 */
	public Class<? extends Edge> getM1Class();

}
