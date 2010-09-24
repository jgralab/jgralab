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

import java.util.Set;

import de.uni_koblenz.jgralab.Vertex;

/**
 * Represents a VertexClass in the Schema.
 * 
 * @author ist@uni-koblenz.de
 */
public interface VertexClass extends GraphElementClass {

	public final static String DEFAULTVERTEXCLASS_NAME = "Vertex";

	/**
	 * adds a superclass to the list of superclasses, all attributes get
	 * inherited from those classes
	 * 
	 * @param superClass
	 *            the vertex class to be added to the list of superclasses
	 * 
	 */
	public void addSuperClass(VertexClass superClass);

	// public Set<IncidenceClass> getOwnInIncidenceClasses();

	// public Set<IncidenceClass> getOwnOutIncidenceClasses();

	public Set<IncidenceClass> getAllInIncidenceClasses();

	public Set<IncidenceClass> getAllOutIncidenceClasses();

	public Set<IncidenceClass> getValidFromFarIncidenceClasses();

	public Set<IncidenceClass> getValidToFarIncidenceClasses();

	// public Set<IncidenceClass> getOwnAndInheritedFarIncidenceClasses();

	public void addInIncidenceClass(IncidenceClass ic);

	public void addOutIncidenceClass(IncidenceClass ic);

	@Override
	public Class<? extends Vertex> getM1Class();

	public Set<EdgeClass> getValidToEdgeClasses();

	public Set<EdgeClass> getValidFromEdgeClasses();

	public Set<EdgeClass> getConnectedEdgeClasses();

	public Set<EdgeClass> getOwnConnectedEdgeClasses();

}
