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

import java.util.List;

import de.uni_koblenz.jgralab.Graph;

/**
 * Represents a <code>GraphClass</code> in the <code>Schema</code>, that holds
 * all <code>GraphElementClasses</code>.
 * 
 * <p>
 * <b>Note:</b> in the following, <code>graphClass</code>, and
 * <code>graphClass'</code>, will represent the states of the given
 * <code>GraphClass</code> before, respectively after, any operation.
 * </p>
 * 
 * <p>
 * <b>Note:</b> in the following it is understood that method arguments differ
 * from <code>null</code>. Therefore there will be no preconditions addressing
 * this matter.
 * </p>
 * 
 * @author ist@uni-koblenz.de
 */
public interface GraphClass extends AttributedElementClass<GraphClass, Graph> {

	/**
	 * creates a vertex class with the vertexclassname name
	 * 
	 * @param qualifiedName
	 *            the qualified name of the vertex class to be created
	 * @return the created vertex class
	 */
	public VertexClass createVertexClass(String qualifiedName);

	/**
	 * @return the default VertexClass of the schema, that is the VertexClass
	 *         with the name "Vertex"
	 */
	public VertexClass getDefaultVertexClass();

	public VertexClass getTemporaryVertexClass();

	/**
	 * creates an edge class between vertex class from, multiplicity fromMin and
	 * fromMax with the rolename fromRoleName, and vertex class to, multiplicity
	 * toMin and toMax with the rolename toRoleName and the edgeclassname name
	 * 
	 * @param qualifiedName
	 *            a unique name in the schema
	 * @param from
	 *            the vertex class where the edge class starts
	 * @param fromMin
	 *            the minimum multiplicity of the edge class on the 'from'-end
	 * @param fromMax
	 *            the maximum multiplicity of the edge class on the 'from'-end
	 * @param fromRoleName
	 *            the unique rolename of the 'from'-end
	 * @param aggrFrom
	 *            the aggregation kind of the 'from' end
	 * @param to
	 *            the vertex class where the edge class ends
	 * @param toMin
	 *            the minimum multiplicity of the edge class on the 'to'-end
	 * @param toMax
	 *            the maximum multiplicity of the edge class on the 'to-end
	 * @param toRoleName
	 *            the unique rolename of the 'to'-end
	 * @param aggrTo
	 *            the aggregation kind of the 'to' end
	 * @return the created edge class
	 */
	public EdgeClass createEdgeClass(String qualifiedName, VertexClass from,
			int fromMin, int fromMax, String fromRoleName,
			AggregationKind aggrFrom, VertexClass to, int toMin, int toMax,
			String toRoleName, AggregationKind aggrTo);

	/**
	 * @return the default EdgeClass of the schema, that is the EdgeClass with
	 *         the name "Edge"
	 */
	public EdgeClass getDefaultEdgeClass();

	public EdgeClass getTemporaryEdgeClass();

	/**
	 * @param name
	 *            the name to search for
	 * @return the contained graph element class with the name name
	 */
	public GraphElementClass<?, ?> getGraphElementClass(String name);

	/**
	 * @return a list of all EdgeClasses of this GraphClass. This list is sorted
	 *         topologically according to the inheritance hierarchy and only
	 *         includes the classes of this schema, i.e, not the default and
	 *         temporary edge classes
	 */
	public List<EdgeClass> getEdgeClasses();

	/**
	 * @return a list of all the vertex classes of this graph class. This list
	 *         is sorted topologically according to the inheritance hierarchy
	 *         and only includes the classes of this schema, i.e, not the
	 *         default and temporary vertex classes.
	 */
	public List<VertexClass> getVertexClasses();

	/**
	 * @return a list of all vertex and edge classes of this graph class in
	 *         topological order, i.e. the concatenation of
	 *         {@link #getVertexClasses()} and {@link #getEdgeClasses()}.
	 */
	public List<GraphElementClass<?, ?>> getGraphElementClasses();

	/**
	 * Returns the VertexClass with the given name. This GraphClass and the
	 * superclasses will be searched for a VertexClass with this name
	 * 
	 * @param name
	 *            the name of the VertexClass to search for
	 * @return the VertexClass with the given name or null, if no such
	 *         VertexClass exists
	 */
	public VertexClass getVertexClass(String name);

	/**
	 * Returns the number of VertexClasses defined in this GraphClass excluding
	 * the default vertex class.
	 * 
	 * @return the number of VertexClasses defined in this GraphClass.
	 */
	public int getVertexClassCount();

	/**
	 * Returns the EdgeClass with the given name. This GraphClass and the
	 * superclasses will be searched for a EdgeClass with this name
	 * 
	 * @param name
	 *            the name of the EdgeClass to search for
	 * @return the EdgeClass with the given name or null, if no such EdgeClass
	 *         exists
	 */
	public EdgeClass getEdgeClass(String name);

	/**
	 * Returns the number of EdgeClasses defined in this GraphClass excluding
	 * the default edge class.
	 * 
	 * @return the number of EdgeClasses defined in this GraphClass.
	 */
	public int getEdgeClassCount();

}
