/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
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

import de.uni_koblenz.jgralab.schema.exception.SchemaException;

/**
 * Represents a <code>GraphClass</code> in the <code>Schema</code>, that holds
 * all <code>GraphElementClasses</code>.
 * 
 * <p>
 * <b>Note:</b> in the following, <code>graphClass</code>, and <code>graphClass'</code>,
 * will represent the states of the given <code>GraphClass</code> before,
 * respectively after, any operation.
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
public interface GraphClass extends AttributedElementClass {

	public final static String DEFAULTGRAPHCLASS_NAME = "Graph";

	/**
	 * creates an edge class between from and to with the edgeclassname name
	 * 
	 * @param qualifiedName
	 *            a unique name in the schema
	 * @param from
	 *            the vertex class where the edge class starts
	 * @param to
	 *            the vertex class where the edge class ends
	 * @return the created edge class
	 */
	public EdgeClass createEdgeClass(String qualifiedName, VertexClass from,
			VertexClass to);

	/**
	 * creates an edge class between vertex class from with the rolename
	 * fromRoleName and vertex class to with the rolename toRoleName and the
	 * edgeclassname name
	 * 
	 * @param qualifiedName
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
	public EdgeClass createEdgeClass(String qualifiedName, VertexClass from,
			String fromRoleName, VertexClass to, String toRoleName);

	/**
	 * creates an edge class between vertex class from, multiplicity fromMin and
	 * fromMax, and vertex class to, multiplicity toMin and toMax with the
	 * edgeclassname name
	 * 
	 * @param qualifiedName
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
	public EdgeClass createEdgeClass(String qualifiedName, VertexClass from,
			int fromMin, int fromMax, VertexClass to, int toMin, int toMax);

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
	public EdgeClass createEdgeClass(String qualifiedName, VertexClass from,
			int fromMin, int fromMax, String fromRoleName, VertexClass to,
			int toMin, int toMax, String toRoleName);

	/**
	 * Creates an <code>AggregationClass</code> between the two
	 * <code>VertexClasses</code> <code>from</code> and <code>to</code> in this
	 * <code>GraphClass</code>. <code>from</code> and <code>to</code> have empty
	 * rolenames and cardinalities ranging from 0 to Integer.MAX_Value.
	 * 
	 * <p>
	 * <b>Pattern:</b>
	 * <code>graphClass.createAggregationClass(name, from, aggregateFrom, to)</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b>
	 * <ul>
	 * <li>The <code>name</code> is unique in the <code>Schema</code>.</li>
	 * <li>The <code>name</code> does not contain
	 * {@link de.uni_koblenz.jgralab.schema.Schema#RESERVED_JAVA_WORDS reserved
	 * Java words}.</li>
	 * <li>The <code>from</code> and <code>to</code> <code>VertexClasses</code>
	 * are known by <code>graphClass</code> prior to calling this method. This
	 * is done via
	 * {@link de.uni_koblenz.jgralab.schema.GraphClass#createVertexClass(String qualifiedName)}
	 * .</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * <b>Postcondition:</b> <code>graphClass'</code> contains the freshly created
	 * <code>AggregationClass</code>.
	 * </p>
	 * 
	 * @param qualifiedName
	 *            a unique <code>name</code> in the <code>Schema</code>
	 * @param from
	 *            the <code>VertexClass</code> where the
	 *            <code>AggregationClass</code> starts
	 * @param aggregateFrom
	 *            set to <code>TRUE</code>, if the aggregation is on the
	 *            'from'-side of the <code>AggregationClass</code>, set to
	 *            <code>FALSE</code>, if the aggregation is on the 'to'-side of
	 *            the <code>AggregationClass</code>
	 * @param to
	 *            the <code>VertexClass</code> where the
	 *            <code>AggregationClass</code> ends
	 * @throws SchemaException
	 *             if:
	 *             <ul>
	 *             <li>there is an <code>AttributedElement</code> with the same
	 *             <code>name</code> in the <code>Schema</code></li>
	 *             <li><code>name</code> contains reserved TG/Java words</li>
	 *             <li>the <code>from</code> and <code>to</code>
	 *             <code>VertexClasses</code> are not known by
	 *             <code>graphClass</code></li>
	 *             </ul>
	 * @return the created <code>AggregationClass</code> or <code>NULL</code> if
	 *         an error occurred
	 */
	public AggregationClass createAggregationClass(String qualifiedName,
			VertexClass from, boolean aggregateFrom, VertexClass to);

	/**
	 * Creates an <code>AggregationClass</code> between the two
	 * <code>VertexClasses</code> <code>from</code>, with the given rolename
	 * <code>fromRoleName</code>, and <code>to</code>, with the given rolename
	 * <code>toRoleName</code>, in this <code>GraphClass</code>.
	 * <code>from</code> and <code>to</code> have and cardinalities ranging from
	 * 0 to Integer.MAX_Value.
	 * 
	 * <p>
	 * <b>Pattern:</b>
	 * <code>graphClass.createAggregationClass(name, from, fromRoleName, aggregateFrom, to, toRoleName)</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b>
	 * <ul>
	 * <li>The <code>name</code> is unique in the <code>Schema</code>.</li>
	 * <li>The <code>name</code> does not contain
	 * {@link de.uni_koblenz.jgralab.schema.Schema#RESERVED_JAVA_WORDS reserved
	 * Java words}.</li>
	 * <li>The <code>from</code> and <code>to</code> <code>VertexClasses</code>
	 * are known by <code>graphClass</code> prior to calling this method. This
	 * is done via
	 * {@link de.uni_koblenz.jgralab.schema.GraphClass#createVertexClass(String qualifiedName)}
	 * .</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * <b>Postcondition:</b> <code>graphClass'</code> contains the freshly created
	 * <code>AggregationClass</code>.
	 * </p>
	 * 
	 * @param qualifiedName
	 *            a unique <code>name</code> in the <code>Schema</code>
	 * @param from
	 *            the <code>VertexClass</code> where the
	 *            <code>AggregationClass</code> starts
	 * @param fromRoleName
	 *            the unique rolename of the 'from'-end
	 * @param aggregateFrom
	 *            set to <code>TRUE</code>, if the aggregation is on the
	 *            'from'-side of the <code>AggregationClass</code>, set to
	 *            <code>FALSE</code>, if the aggregation is on the 'to'-side of
	 *            the <code>AggregationClass</code>
	 * @param to
	 *            the <code>VertexClass</code> where the
	 *            <code>AggregationClass</code> ends
	 * @param toRoleName
	 *            the unique rolename of the 'to'-end
	 * @throws SchemaException
	 *             if:
	 *             <ul>
	 *             <li>there is an <code>AttributedElement</code> with the same
	 *             <code>name</code> in the <code>Schema</code></li>
	 *             <li><code>name</code> contains reserved TG/Java words</li>
	 *             <li>the <code>from</code> and <code>to</code>
	 *             <code>VertexClasses</code> are not known by
	 *             <code>graphClass</code></li>
	 *             </ul>
	 * @return the created <code>AggregationClass</code> or <code>NULL</code> if
	 *         an error occurred
	 */
	public AggregationClass createAggregationClass(String qualifiedName,
			VertexClass from, String fromRoleName, boolean aggregateFrom,
			VertexClass to, String toRoleName);

	/**
	 * Creates an <code>AggregationClass</code> between the two
	 * <code>VertexClasses</code> <code>from</code>, with cardinalities ranging
	 * from <code>fromMin</code> to <code>fromMax</code>, and <code>to</code>,
	 * with cardinalities ranging from <code>toMin</code> to <code>toMax</code>,
	 * in this <code>GraphClass</code>. <code>from</code> and <code>to</code>
	 * have empty rolenames.
	 * 
	 * <p>
	 * <b>Pattern:</b>
	 * <code>graphClass.createAggregationClass(name, from, fromMin, fromMax, aggregateFrom, to, toMin, toMax)</code>
	 * </p>
	 * 
	 * <p>
	 * <b>Preconditions:</b>
	 * <ul>
	 * <li>The <code>name</code> is unique in the <code>Schema</code>.</li>
	 * <li>The <code>name</code> does not contain
	 * {@link de.uni_koblenz.jgralab.schema.Schema#RESERVED_JAVA_WORDS reserved
	 * Java words}.</li>
	 * <li>The <code>from</code> and <code>to</code> <code>VertexClasses</code>
	 * are known by <code>graphClass</code> prior to calling this method. This
	 * is done via
	 * {@link de.uni_koblenz.jgralab.schema.GraphClass#createVertexClass(String qualifiedName)}
	 * .</li>
	 * <li><code>0 <= fromMin <= fromMax <= Integer.maxValue</code></li>
	 * <li><code>0 <= toMin <= toMax <= Integer.maxValue</code></li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * <b>Postcondition:</b> <code>graphClass'</code> contains the freshly created
	 * <code>AggregationClass</code>.
	 * </p>
	 * 
	 * @param qualifiedName
	 *            a unique <code>name in the <code>Schema</code>
	 * @param from
	 *            the <code>VertexClass</code> where the
	 *            </code>AggregationClass/code> starts
	 * @param fromMin
	 *            the minimum multiplicity of the <code>AggregationClass</code>
	 *            on the 'from'-end
	 * @param fromMax
	 *            the maximum multiplicity of the <code>AggregationClass</code>
	 *            on the 'from'-end
	 * @param aggregateFrom
	 *            set to <code>TRUE/code>, if the aggregation is on the 'from'-side of the
	 *            <code>AggregationClass</code>, set to FALSE, if the
	 *            aggregation is on the 'to'-side of the
	 *            <code>AggregationClass</code>
	 * @param to
	 *            the <code>VertexClass</code> where the
	 *            <code>AggregationClass</code> ends
	 * @param toMin
	 *            the minimum multiplicity of the <code>AggregationClass</code>
	 *            on the 'to'-end
	 * @param toMax
	 *            the maximum multiplicity of the <code>AggregationClass</code>
	 *            on the 'to-end
	 * @throws SchemaException
	 *             if:
	 *             <ul>
	 *             <li>there is an <code>AttributedElement</code> with the same
	 *             <code>name</code> in the <code>Schema</code></li>
	 *             <li><code>name</code> contains reserved TG/Java words</li>
	 *             </ul>
	 * @return the created <code>AggregationClass</code> or <code>NULL</code> if
	 *         an error occurred
	 */
	public AggregationClass createAggregationClass(String qualifiedName,
			VertexClass from, int fromMin, int fromMax, boolean aggregateFrom,
			VertexClass to, int toMin, int toMax);

	/**
	 * Creates an <code>AggregationClass</code> between the two
	 * <code>VertexClasses</code> <code>from</code>, with the given rolename
	 * <code>fromRoleName</code> and cardinalities ranging from
	 * <code>fromMin</code> to <code>fromMax</code>, and <code>to</code>, with
	 * the given rolename <code>toRoleName</code> and cardinalities ranging from
	 * <code>toMin</code> to <code>toMax</code>, in this <code>GraphClass</code>
	 * .
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
	 * <li>The <code>name</code> is unique in the <code>Schema</code>.</li>
	 * <li>The <code>name</code> does not contain
	 * {@link de.uni_koblenz.jgralab.schema.Schema#RESERVED_JAVA_WORDS reserved
	 * Java words}.</li>
	 * <li>The <code>from</code> and <code>to</code> <code>VertexClasses</code>
	 * are known by <code>graphClass</code> prior to calling this method. This
	 * is done via
	 * {@link de.uni_koblenz.jgralab.schema.GraphClass#createVertexClass(String qualifiedName)}
	 * .</li>
	 * <li><code>0 <= fromMin <= fromMax <= Integer.maxValue</code></li>
	 * <li><code>0 <= toMin <= toMax <= Integer.maxValue</code></li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * <b>Postcondition:</b> <code>graphClass'</code> contains the freshly created
	 * <code>AggregationClass</code>.
	 * </p>
	 * 
	 * 
	 * @param qualifiedName
	 *            a unique <code>name in the <code>Schema</code>
	 * @param from
	 *            the <code>VertexClass</code> where the
	 *            <code>AggregationClass</code> starts
	 * @param fromMin
	 *            the minimum multiplicity of the <code>AggregationClass</code>
	 *            on the 'from'-end
	 * @param fromMax
	 *            the maximum multiplicity of the <code>AggregationClass</code>
	 *            on the 'from'-end
	 * @param fromRoleName
	 *            the unique rolename of the 'from'-end
	 * @param aggregateFrom
	 *            set to <code>TRUE</code>, if the aggregation is on the
	 *            'from'-side of the <code>AggregationClass</code>, set to
	 *            <code>FALSE</code>, if the aggregation is on the 'to'-side of
	 *            the <code>AggregationClass</code>
	 * @param to
	 *            the <code>VertexClass</code> where the
	 *            <code>AggregationClass</code> ends
	 * @param toMin
	 *            the minimum multiplicity of the <code>AggregationClass</code>
	 *            on the 'to'-end
	 * @param toMax
	 *            the maximum multiplicity of the <code>AggregationClass</code>
	 *            on the 'to-end
	 * @param toRoleName
	 *            the unique rolename of the 'to'-end
	 * @throws SchemaException
	 *             if:
	 *             <ul>
	 *             <li>there is an <code>AttributedElement</code> with the same
	 *             <code>name</code> in the <code>Schema</code></li>
	 *             <li><code>name</code> contains reserved Java words</li>
	 *             <li>the <code>from</code> and <code>to</code>
	 *             <code>VertexClasses</code> are not known by
	 *             <code>graphClass</code></li>
	 *             </ul>
	 * @return the created <code>AggregationClass</code> or <code>NULL</code> if
	 *         an error occurred
	 */
	public AggregationClass createAggregationClass(String qualifiedName,
			VertexClass from, int fromMin, int fromMax, String fromRoleName,
			boolean aggregateFrom, VertexClass to, int toMin, int toMax,
			String toRoleName);

	/**
	 * Creates a <code>CompositionClass</code> between two
	 * <code>VertexClasses</code> in this <code>GraphClass</code>. The default
	 * cardinality on composite side is (1,1) and (0,*) on the other side.
	 * 
	 * @param qualifiedName
	 *            a unique <code>name</code> in the <code>Schema</code>
	 * @param from
	 *            the <code>VertexClass</code> where the
	 *            <code>CompositionClass</code> starts
	 * @param compositeFrom
	 *            set to <code>TRUE</code>, if the composition is on the
	 *            'from'-side of the <code>CompositionClass</code>, set to
	 *            <code>FALSE</code>, if the composition is on the 'to'-side of
	 *            the <code>CompositionClass</code>
	 * @param to
	 *            the <code>VertexClass</code> where the
	 *            <code>CompositionClass</code> ends
	 * @return the created <code>CompositionClass</code> or <code>NULL</code> if
	 *         an error occurred
	 * 
	 */
	public CompositionClass createCompositionClass(String qualifiedName,
			VertexClass from, boolean compositeFrom, VertexClass to);

	/**
	 * creates a composition class between vertex class from with the rolename
	 * fromRoleName and vertex class to with the rolename toRoleName and the
	 * compositionclassname name. The default cardinality on composite side is
	 * (1,1) and (0,*) on the other side.
	 * 
	 * @param qualifiedName
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
	public CompositionClass createCompositionClass(String qualifiedName,
			VertexClass from, String fromRoleName, boolean compositeFrom,
			VertexClass to, String toRoleName);

	/**
	 * creates a composition class between vertex class from, multiplicity
	 * fromMin and fromMax, and vertex class to, multiplicity toMin and toMax
	 * with the compositionclassname name
	 * 
	 * @param qualifiedName
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
	public CompositionClass createCompositionClass(String qualifiedName,
			VertexClass from, int fromMin, int fromMax, boolean compositeFrom,
			VertexClass to, int toMin, int toMax);

	/**
	 * creates a composition class between vertex class from, multiplicity
	 * fromMin and fromMax with the rolename fromRoleName, and vertex class to,
	 * multiplicity toMin and toMax with the rolename toRoleName and the
	 * composition classname name
	 * 
	 * @param qualifiedName
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
	public CompositionClass createCompositionClass(String qualifiedName,
			VertexClass from, int fromMin, int fromMax, String fromRoleName,
			boolean compositeFrom, VertexClass to, int toMin, int toMax,
			String toRoleName);

	/**
	 * creates a vertex class with the vertexclassname name
	 * 
	 * @param qualifiedName
	 *            the qualified name of the vertex class to be created
	 * @return the created vertex class
	 */
	public VertexClass createVertexClass(String qualifiedName);

	/**
	 * addSubClass can not be called for GraphClass and always throws a
	 * SchemaException.
	 * 
	 * @param subClass
	 *            a graph class
	 */
	public void addSubClass(GraphClass subClass);

	/**
	 * addSuperClass can not be called for GraphClass and always throws a
	 * SchemaException.
	 * 
	 * @param superClass
	 *            a graph class
	 */
	public void addSuperClass(GraphClass superClass);

	/**
	 * @param name
	 *            the name to search for
	 * @return the contained graph element class with the name name
	 */
	public GraphElementClass getGraphElementClass(String name);

	/**
	 * @return a list of all EdgeClasses this graphclass knows, including
	 *         inherited EdgeClasses
	 */
	public List<EdgeClass> getEdgeClasses();

	/**
	 * @return a list of all the edge/vertex/aggregation/composition classes of
	 *         this graph class, including inherited classes
	 */
	public List<GraphElementClass> getGraphElementClasses();

	/**
	 * @return a list of all the vertex classes of this graph class, including
	 *         inherited vertex classes
	 */
	public List<VertexClass> getVertexClasses();

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
	 * Returns the number of VertexClasses defined in this GraphClass.
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
	 * Returns the number of EdgeClasses (that is Edge-/Aggregation- and
	 * CompositionClasses) defined in this GraphClass.
	 * 
	 * @return the number of EdgeClasses defined in this GraphClass.
	 */
	public int getEdgeClassCount();

	/**
	 * Returns the CompositionClass with the given name. This GraphClass and the
	 * superclasses will be searched for a CompositionClass with this name
	 * 
	 * @param name
	 *            the name of the CompositionClass to search for
	 * @return the CompositionClass with the given name or null, if no such
	 *         CompositionClass exists
	 */
	public CompositionClass getCompositionClass(String name);

	/**
	 * Returns the AggregationClass with the given name. This GraphClass and the
	 * superclasses will be searched for a AggregationClass with this name
	 * 
	 * @param name
	 *            the name of the AggregationClass to search for
	 * @return the AggregationClass with the given name or null, if no such
	 *         AggregationClass exists
	 */
	public AggregationClass getAggregationClass(String name);

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
	public boolean knowsOwn(String aGraphElementClass);

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
	public boolean knows(String aGraphElementClass);
}