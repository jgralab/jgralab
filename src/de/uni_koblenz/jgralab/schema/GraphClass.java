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

package de.uni_koblenz.jgralab.schema;

import java.util.List;

/**
 * represents a graph class in the schema, holds all graph element classes
 * 
 * @author Steffen Kahle
 * 
 */
public interface GraphClass extends AttributedElementClass {

	/**
	 * creates an edge class between from and to with the edgeclassname name
	 * 
	 * @param name
	 *            a unique name in the schema
	 * @param from
	 *            the vertex class where the edge class starts
	 * @param to
	 *            the vertex class where the edge class ends
	 * @return the created edge class
	 */
	public EdgeClass createEdgeClass(QualifiedName name, VertexClass from,
			VertexClass to);

	/**
	 * creates an edge class between vertex class from with the rolename
	 * fromRoleName and vertex class to with the rolename toRoleName and the
	 * edgeclassname name
	 * 
	 * @param name
	 *            a unique name in the schema
	 * @param from
	 *            the vertex class where the edge class starts
	 * @param fromRoleName
	 *            the unique rolename of the 'from'-end
	 * @param to
	 *            the vertex class where the edge class ends
	 * @param toRoleName
	 *            the unique rolename of the 'to'-end
	 * @return the created edge class
	 */
	public EdgeClass createEdgeClass(QualifiedName name, VertexClass from,
			String fromRoleName, VertexClass to, String toRoleName);

	/**
	 * creates an edge class between vertex class from, multiplicity fromMin and
	 * fromMax, and vertex class to, multiplicity toMin and toMax with the
	 * edgeclassname name
	 * 
	 * @param name
	 *            a unique name in the schema
	 * @param from
	 *            the vertex class where the edge class starts
	 * @param fromMin
	 *            the minimum multiplicity of the edge class on the 'from'-end
	 * @param fromMax
	 *            the maximum multiplicity of the edge class on the 'from'-end
	 * @param to
	 *            the vertex class where the edge class ends
	 * @param toMin
	 *            the minimum multiplicity of the edge class on the 'to'-end
	 * @param toMax
	 *            the maximum multiplicity of the edge class on the 'to-end
	 * @return the created edge class
	 */
	public EdgeClass createEdgeClass(QualifiedName name, VertexClass from,
			int fromMin, int fromMax, VertexClass to, int toMin, int toMax);

	/**
	 * creates an edge class between vertex class from, multiplicity fromMin and
	 * fromMax with the rolename fromRoleName, and vertex class to, multiplicity
	 * toMin and toMax with the rolename toRoleName and the edgeclassname name
	 * 
	 * @param name
	 *            a unique name in the schema
	 * @param from
	 *            the vertex class where the edge class starts
	 * @param fromMin
	 *            the minimum multiplicity of the edge class on the 'from'-end
	 * @param fromMax
	 *            the maximum multiplicity of the edge class on the 'from'-end
	 * @param fromRoleName
	 *            the unique rolename of the 'from'-end
	 * @param to
	 *            the vertex class where the edge class ends
	 * @param toMin
	 *            the minimum multiplicity of the edge class on the 'to'-end
	 * @param toMax
	 *            the maximum multiplicity of the edge class on the 'to-end
	 * @param toRoleName
	 *            the unique rolename of the 'to'-end
	 * @return the created edge class
	 */
	public EdgeClass createEdgeClass(QualifiedName name, VertexClass from,
			int fromMin, int fromMax, String fromRoleName, VertexClass to,
			int toMin, int toMax, String toRoleName);

	/**
	 * Creates an aggregation class between two vertices.
	 * 
	 * <p>
	 * <b>Pattern:</b>
	 * <code>graphClass.createAggregationClass(name, from, aggregateFrom, to)</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b>
	 * <ul>
	 * <li><code>name</code> must not be empty</li>
	 * <li><code>name</code> must be unique in the schema</li>
	 * <li><code>from</code> must be a valid (either new and unique, or an
	 * existing) VertexClass</li>
	 * <li><code>to</code> must be a valid (either new and unique, or an
	 * existing) VertexClass</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b>
	 * <ul>
	 * <li><code>graphClass</code> must have one issue of freshly created
	 * AggregationClass</li>
	 * </ul>
	 * </p>
	 * 
	 * @param name
	 *            a unique name in the schema
	 * @param from
	 *            the vertex class where the aggregation class starts
	 * @param aggregateFrom
	 *            set to TRUE, if the aggregation is on the 'from'-side of the
	 *            aggregation class, set to FALSE, if the aggregation is on the
	 *            'to'-side of the aggregation class
	 * @param to
	 *            the vertex class where the aggregation class ends
	 * @throws SchemaException
	 *             if there already is an element with the given
	 *             <code>name</code> in the schema
	 * @return the created aggregation class
	 */
	public AggregationClass createAggregationClass(QualifiedName name,
			VertexClass from, boolean aggregateFrom, VertexClass to);

	/**
	 * Creates an aggregation class between two vertices.
	 * 
	 * <p>
	 * <b>Pattern:</b>
	 * <code>graphClass.createAggregationClass(name, from, fromRoleName, aggregateFrom, to, toRoleName)</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b>
	 * <ul>
	 * <li><code>name</code> must not be empty</li>
	 * <li><code>name</code> must be unique in the schema</li>
	 * <li><code>from</code> must be a valid (either new and unique, or an
	 * existing) VertexClass</li>
	 * <li><code>to</code> must be a valid (either new and unique, or an
	 * existing) VertexClass</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b>
	 * <ul>
	 * <li><code>graphClass</code> must have one issue of freshly created
	 * AggregationClass</li>
	 * </ul>
	 * </p>
	 * 
	 * @param name
	 *            a unique name in the schema
	 * @param from
	 *            the vertex class where the aggregation class starts
	 * @param fromRoleName
	 *            the unique rolename of the 'from'-end
	 * @param aggregateFrom
	 *            set to TRUE, if the aggregation is on the 'from'-side of the
	 *            aggregation class, set to FALSE, if the aggregation is on the
	 *            'to'-side of the aggregation class
	 * @param to
	 *            the vertex class where the aggregation class ends
	 * @param toRoleName
	 *            the unique rolename of the 'to'-end
	 * @throws SchemaException
	 *             if there already is an element with the given
	 *             <code>name</code> in the schema
	 * @return the created aggregation class
	 */
	public AggregationClass createAggregationClass(QualifiedName name,
			VertexClass from, String fromRoleName, boolean aggregateFrom,
			VertexClass to, String toRoleName);

	/**
	 * Creates an aggregation class between two vertices.
	 * 
	 * <p>
	 * <b>Pattern:</b>
	 * <code>graphClass.createAggregationClass(name, from, fromMin, fromMax, aggregateFrom, to, toMin, toMax)</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b>
	 * <ul>
	 * <li><code>name</code> must not be empty</li>
	 * <li><code>name</code> must be unique in the schema</li>
	 * <li><code>from</code> must be a valid (either new and unique, or an
	 * existing) VertexClass</li>
	 * <li><code>to</code> must be a valid (either new and unique, or an
	 * existing) VertexClass</li>
	 * <li><code>0 <= fromMin <= fromMax <= Integer.maxValue</code></li>
	 * <li><code>0 <= toMin <= toMax <= Integer.maxValue</code></li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b>
	 * <ul>
	 * <li><code>graphClass</code> must have one issue of freshly created
	 * AggregationClass</li>
	 * </ul>
	 * </p>
	 * 
	 * @param name
	 *            a unique name in the schema
	 * @param from
	 *            the vertex class where the aggregation class starts
	 * @param fromMin
	 *            the minimum multiplicity of the aggregation class on the
	 *            'from'-end
	 * @param fromMax
	 *            the maximum multiplicity of the aggregation class on the
	 *            'from'-end
	 * @param aggregateFrom
	 *            set to TRUE, if the aggregation is on the 'from'-side of the
	 *            aggregation class, set to FALSE, if the aggregation is on the
	 *            'to'-side of the aggregation class
	 * @param to
	 *            the vertex class where the aggregation class ends
	 * @param toMin
	 *            the minimum multiplicity of the aggregation class on the
	 *            'to'-end
	 * @param toMax
	 *            the maximum multiplicity of the aggregation class on the
	 *            'to-end
	 * @throws SchemaException
	 *             if there already is an element with the given
	 *             <code>name</code> in the schema
	 * @return the created aggregation class
	 */
	public AggregationClass createAggregationClass(QualifiedName name,
			VertexClass from, int fromMin, int fromMax, boolean aggregateFrom,
			VertexClass to, int toMin, int toMax);

	/**
	 * Creates an aggregation class between two vertices.
	 * 
	 * <p>
	 * <b>Pattern:</b>
	 * 
	 * <code>graphClass.createAggregationClass(name, from, fromMin, fromMax, fromRoleName, aggregateFrom, to, toMin, toMax, toRoleName)</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b>
	 * <ul>
	 * <li><code>name</code> must not be empty</li>
	 * <li><code>name</code> must be unique in the schema</li>
	 * <li><code>from</code> must be a valid (either new and unique, or an
	 * existing) VertexClass</li>
	 * <li><code>to</code> must be a valid (either new and unique, or an
	 * existing) VertexClass</li>
	 * <li><code>0 <= fromMin <= fromMax <= Integer.maxValue</code></li>
	 * <li><code>0 <= toMin <= toMax <= Integer.maxValue</code></li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * <b>Postconditions:</b>
	 * <ul>
	 * <li><code>graphClass</code> must have one issue of freshly created
	 * AggregationClass</li>
	 * </ul>
	 * </p>
	 * 
	 * 
	 * @param name
	 *            a unique name in the schema
	 * @param from
	 *            the vertex class where the aggregation class starts
	 * @param fromMin
	 *            the minimum multiplicity of the aggregation class on the
	 *            'from'-end
	 * @param fromMax
	 *            the maximum multiplicity of the aggregation class on the
	 *            'from'-end
	 * @param fromRoleName
	 *            the unique rolename of the 'from'-end
	 * @param aggregateFrom
	 *            set to TRUE, if the aggregation is on the 'from'-side of the
	 *            aggregation class, set to FALSE, if the aggregation is on the
	 *            'to'-side of the aggregation class
	 * @param to
	 *            the vertex class where the aggregation class ends
	 * @param toMin
	 *            the minimum multiplicity of the aggregation class on the
	 *            'to'-end
	 * @param toMax
	 *            the maximum multiplicity of the aggregation class on the
	 *            'to-end
	 * @param toRoleName
	 *            the unique rolename of the 'to'-end
	 * @throws SchemaException
	 *             if there already is an element with the given
	 *             <code>name</code> in the schema
	 * @return the created aggregation class
	 */
	public AggregationClass createAggregationClass(QualifiedName name,
			VertexClass from, int fromMin, int fromMax, String fromRoleName,
			boolean aggregateFrom, VertexClass to, int toMin, int toMax,
			String toRoleName);

	/**
	 * creates an composition class between from and to with the
	 * compositionclassname name
	 * 
	 * @param name
	 *            a unique name in the schema
	 * @param from
	 *            the vertex class where the composition class starts
	 * @param compositeFrom
	 *            set to TRUE, if the composition is on the 'from'-side of the
	 *            composition class, set to FALSE, if the composition is on the
	 *            'to'-side of the composition class
	 * @param to
	 *            the vertex class where the composition class ends
	 * @return the created composition class
	 */
	public CompositionClass createCompositionClass(QualifiedName name,
			VertexClass from, boolean compositeFrom, VertexClass to);

	/**
	 * creates a composition class between vertex class from with the rolename
	 * fromRoleName and vertex class to with the rolename toRoleName and the
	 * compositionclassname name
	 * 
	 * @param name
	 *            a unique name in the schema
	 * @param from
	 *            the vertex class where the composition class starts
	 * @param fromRoleName
	 *            the unique rolename of the 'from'-end
	 * @param compositeFrom
	 *            set to TRUE, if the composition is on the 'from'-side of the
	 *            composition class, set to FALSE, if the composition is on the
	 *            'to'-side of the composition class
	 * @param to
	 *            the vertex class where the composition class ends
	 * @param toRoleName
	 *            the unique rolename of the 'to'-end
	 * @return the created composition class
	 */
	public CompositionClass createCompositionClass(QualifiedName name,
			VertexClass from, String fromRoleName, boolean compositeFrom,
			VertexClass to, String toRoleName);

	/**
	 * creates a composition class between vertex class from, multiplicity
	 * fromMin and fromMax, and vertex class to, multiplicity toMin and toMax
	 * with the compositionclassname name
	 * 
	 * @param name
	 *            a unique name in the schema
	 * @param from
	 *            the vertex class where the composition class starts
	 * @param fromMin
	 *            the minimum multiplicity of the composition class on the
	 *            'from'-end
	 * @param fromMax
	 *            the maximum multiplicity of the composition class on the
	 *            'from'-end
	 * @param compositeFrom
	 *            set to TRUE, if the composition is on the 'from'-side of the
	 *            composition class, set to FALSE, if the composition is on the
	 *            'to'-side of the composition class
	 * @param to
	 *            the vertex class where the composition class ends
	 * @param toMin
	 *            the minimum multiplicity of the composition class on the
	 *            'to'-end
	 * @param toMax
	 *            the maximum multiplicity of the composition class on the
	 *            'to-end
	 * @return the created composition class
	 */
	public CompositionClass createCompositionClass(QualifiedName name,
			VertexClass from, int fromMin, int fromMax, boolean compositeFrom,
			VertexClass to, int toMin, int toMax);

	/**
	 * creates a composition class between vertex class from, multiplicity
	 * fromMin and fromMax with the rolename fromRoleName, and vertex class to,
	 * multiplicity toMin and toMax with the rolename toRoleName and the
	 * composition classname name
	 * 
	 * @param name
	 *            a unique name in the schema
	 * @param from
	 *            the vertex class where the composition class starts
	 * @param fromMin
	 *            the minimum multiplicity of the composition class on the
	 *            'from'-end
	 * @param fromMax
	 *            the maximum multiplicity of the composition class on the
	 *            'from'-end
	 * @param fromRoleName
	 *            the unique rolename of the 'from'-end
	 * @param compositeFrom
	 *            set to TRUE, if the composition is on the 'from'-side of the
	 *            composition class, set to FALSE, if the composition is on the
	 *            'to'-side of the composition class
	 * @param to
	 *            the vertex class where the composition class ends
	 * @param toMin
	 *            the minimum multiplicity of the composition class on the
	 *            'to'-end
	 * @param toMax
	 *            the maximum multiplicity of the composition class on the
	 *            'to-end
	 * @param toRoleName
	 *            the unique rolename of the 'to'-end
	 * @return the created composition class
	 */
	public CompositionClass createCompositionClass(QualifiedName name,
			VertexClass from, int fromMin, int fromMax, String fromRoleName,
			boolean compositeFrom, VertexClass to, int toMin, int toMax,
			String toRoleName);

	/**
	 * creates a vertex class with the vertexclassname name
	 * 
	 * @param name
	 *            the name of the vertex class to be created
	 * @return the created vertex class
	 */
	public VertexClass createVertexClass(QualifiedName name);

	/**
	 * addSuperClass can not be called for GraphClass and always throws a
	 * SchemaException.
	 * 
	 * @param superClass
	 *            a graph class
	 */
	public void addSuperClass(GraphClass superClass);

	/**
	 * addSubClass can not be called for GraphClass and always throws a
	 * SchemaException.
	 * 
	 * @param subClass
	 *            a graph class
	 */
	public void addSubClass(GraphClass subClass);

	/**
	 * @param aGraphElementClass
	 *            a vertex/edge/aggregation/composition class
	 * @return true, if this graph class aggregates aGraphElementClass
	 */
	public boolean knowsOwn(GraphElementClass aGraphElementClass);

	/**
	 * @param aGraphElementClass
	 *            a vertex/edge/aggregation/composition class
	 * @return true, if this graph class aggregates aGraphElementClass
	 */
	public boolean knowsOwn(QualifiedName aGraphElementClass);

	/**
	 * @param aGraphElementClass
	 *            a vertex/edge/aggregation/composition class name
	 * @return true, if this graph class aggregates aGraphElementClass
	 */
	public boolean knows(GraphElementClass aGraphElementClass);

	/**
	 * @param aGraphElementClass
	 *            a vertex/edge/aggregation/composition class name
	 * @return true, if this graph class aggregates aGraphElementClass
	 */
	public boolean knows(QualifiedName aGraphElementClass);

	/**
	 * @param name
	 *            the name to search for
	 * @return the contained graph element class with the name name
	 */
	public GraphElementClass getGraphElementClass(QualifiedName name);

	/**
	 * @return a list of all edge classes this graphclass knows, excluding
	 *         inherited edge classes
	 */
	public List<EdgeClass> getOwnEdgeClasses();

	/**
	 * @return a list of all EdgeClasses this graphclass knows, including
	 *         inherited EdgeClasses
	 */
	public List<EdgeClass> getEdgeClasses();

	/**
	 * @return a list of all composition classes this graphclass knows,
	 *         excluding inherited composition classes
	 */
	public List<CompositionClass> getOwnCompositionClasses();

	/**
	 * @return a list of all composition classes this graphclass knows,
	 *         including inherited composition classes
	 */
	public List<CompositionClass> getCompositionClasses();

	/**
	 * @return a list of all aggregation classes this graphclass knows,
	 *         excluding inherited aggregation classes
	 */
	public List<AggregationClass> getOwnAggregationClasses();

	/**
	 * @return a list of all aggregation classes this graphclass knows,
	 *         including inherited aggregation classes
	 */
	public List<AggregationClass> getAggregationClasses();

	/**
	 * @return a list of all the edge/vertex/aggregation/composition classes of
	 *         this graph class, excluding inherited classes
	 */
	public List<GraphElementClass> getOwnGraphElementClasses();

	/**
	 * @return a list of all the edge/vertex/aggregation/composition classes of
	 *         this graph class, including inherited classes
	 */
	public List<GraphElementClass> getGraphElementClasses();

	/**
	 * @return a list of all the vertex classes of this graph class, excluding
	 *         inherited vertex classes
	 */
	public List<VertexClass> getOwnVertexClasses();

	/**
	 * @return a list of all the vertex classes of this graph class, including
	 *         inherited vertex classes
	 */
	public List<VertexClass> getVertexClasses();

	/**
	 * 
	 * @return the number of edge classes this graph class knows, excluding
	 *         inherited edge classes
	 */
	public int getOwnEdgeClassCount();

	/**
	 * 
	 * @return the number of vertex classes this graph class knows, excluding
	 *         inherited vertex classes
	 */
	public int getOwnVertexClassCount();

	/**
	 * Returns the VertexClass with the given name. This GraphClass and the
	 * superclasses will be searched for a VertexClass with this name
	 * 
	 * @param name
	 *            the name of the VertexClass to search for
	 * @return the VertexClass with the given name or null, if no such
	 *         VertexClass exists
	 */
	public VertexClass getVertexClass(QualifiedName name);

	/**
	 * Returns the EdgeClass with the given name. This GraphClass and the
	 * superclasses will be searched for a EdgeClass with this name
	 * 
	 * @param name
	 *            the name of the EdgeClass to search for
	 * @return the EdgeClass with the given name or null, if no such EdgeClass
	 *         exists
	 */
	public EdgeClass getEdgeClass(QualifiedName name);

	/**
	 * Returns the CompositionClass with the given name. This GraphClass and the
	 * superclasses will be searched for a CompositionClass with this name
	 * 
	 * @param name
	 *            the name of the CompositionClass to search for
	 * @return the CompositionClass with the given name or null, if no such
	 *         CompositionClass exists
	 */
	public CompositionClass getCompositionClass(QualifiedName name);

	/**
	 * Returns the AggregationClass with the given name. This GraphClass and the
	 * superclasses will be searched for a AggregationClass with this name
	 * 
	 * @param name
	 *            the name of the AggregationClass to search for
	 * @return the AggregationClass with the given name or null, if no such
	 *         AggregationClass exists
	 */
	public AggregationClass getAggregationClass(QualifiedName name);

}