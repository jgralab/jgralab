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

package de.uni_koblenz.jgralab.schema;

import java.util.Set;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.impl.DirectedSchemaEdgeClass;

/**
 * Represents a VertexClass in the Schema.
 *
 * @author ist@uni-koblenz.de
 */
public interface VertexClass extends GraphElementClass<VertexClass, Vertex> {

	public final static String DEFAULTVERTEXCLASS_NAME = "Vertex";
	public final static String TEMPORARYVERTEXCLASS_NAME = "TemporaryVertexClass";
	
	/**
	 * adds a superclass to the list of superclasses, all attributes get
	 * inherited from those classes
	 *
	 * @param superClass
	 *            the vertex class to be added to the list of superclasses
	 *
	 */
	public void addSuperClass(VertexClass superClass);

	public Set<IncidenceClass> getAllInIncidenceClasses();

	public Set<IncidenceClass> getAllOutIncidenceClasses();

	public Set<IncidenceClass> getValidFromFarIncidenceClasses();

	public Set<IncidenceClass> getValidToFarIncidenceClasses();

	/**
	 * @return The set of {@link IncidenceClass}es that can be accessed by role
	 *         name from instances of this vertex class
	 */
	public Set<IncidenceClass> getOwnAndInheritedFarIncidenceClasses();

	/**
	 * @param roleName
	 * @return the {@link EdgeClass} corresponding to the far-end
	 *         <code>roleName</code> including its direction from the view of
	 *         this vertex class
	 */
	public DirectedSchemaEdgeClass getDirectedEdgeClassForFarEndRole(
			String roleName);

	/**
	 * @param ec
	 * @return true, iff edges of class <code>ec</code> may start at vertices of
	 *         this vertex class
	 */
	public boolean isValidFromFor(EdgeClass ec);

	/**
	 * @param ec
	 * @return true, iff edges of class <code>ec</code> may end at vertices of
	 *         this vertex class
	 */
	public boolean isValidToFor(EdgeClass ec);

	public Set<EdgeClass> getValidToEdgeClasses();

	public Set<EdgeClass> getValidFromEdgeClasses();

	public Set<EdgeClass> getConnectedEdgeClasses();

	public Set<EdgeClass> getOwnConnectedEdgeClasses();

}
