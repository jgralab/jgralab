/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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
import java.util.Set;

/**
 * represents a vertex class in the schema, instances represent m2 classes
 * 
 * @author Steffen Kahle
 * 
 */
public interface VertexClass extends GraphElementClass {

	/**
	 * adds a superclass to the list of superclasses, all attributes get
	 * inherited from those classes
	 * 
	 * @param superClass
	 *            the vertex class to be added to the list of superclasses
	 * 
	 */
	public void addSuperClass(VertexClass superClass);

	/**
	 * adds an edge class to the list of edge classes to which the vertex class
	 * may be connected to, only used internally!
	 * 
	 * @param anEdgeClass
	 */
	public void addEdgeClass(EdgeClass anEdgeClass);

	/**
	 * @return the edge classes to which the vertex class may be connected to
	 */
	public Set<EdgeClass> getOwnEdgeClasses();

	/**
	 * @return all EdgeClasses (including subclasses) which may connect to this
	 *         vertexclass
	 */
	public Set<EdgeClass> getEdgeClasses();

	/**
	 * @return the set of EdgeClasses that may start at this VertexClass
	 */
	public Set<EdgeClass> getValidFromEdgeClasses();

	/**
	 * @return the set of EdgeClasses that may end at this VertexClass
	 */
	public Set<EdgeClass> getValidToEdgeClasses();

	/**
	 * @return the own set of directed edge classes that may be connected to a
	 *         vertex of this class. The own set means that only edges that may
	 *         connect to a vertex of this class but not a superclass are
	 *         returned
	 */
	public Set<DirectedEdgeClass> getOwnDirectedEdgeClasses();

	/**
	 * @return the set of all directed edge classes that may be connected to a
	 *         vertex of this class. The set means that edges that may connect
	 *         to a vertex of this class or a superclass are returned
	 */
	public Set<DirectedEdgeClass> getDirectedEdgeClasses();

	/**
	 * @return a map from String to RolenameEntry that contains all rolenames
	 *         that are related for this VertexClass or a superclass
	 */
	public Map<String, RolenameEntry> getRolenameMap();


	public Set<DirectedEdgeClass> getValidDirectedEdgeClasses();

}