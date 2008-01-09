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
 
package de.uni_koblenz.jgralab.impl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.uni_koblenz.jgralab.AggregationClass;
import de.uni_koblenz.jgralab.AttributedElementClass;
import de.uni_koblenz.jgralab.CompositionClass;
import de.uni_koblenz.jgralab.EdgeClass;
import de.uni_koblenz.jgralab.GraphClass;
import de.uni_koblenz.jgralab.GraphElementClass;
import de.uni_koblenz.jgralab.Schema;
import de.uni_koblenz.jgralab.SchemaException;
import de.uni_koblenz.jgralab.VertexClass;


public class GraphClassImpl extends AttributedElementClassImpl implements GraphClass {
	
	private Schema schema;
	
//	private int maxId = 0;
	
	private Map<String, GraphElementClass> graphElementClasses;
	
	private Map<String, EdgeClass> edgeClasses;
	
	private Map<String, VertexClass> vertexClasses;
	
	private Map<String, AggregationClass> aggregationClasses;
	
	private Map<String, CompositionClass> compositionClasses;
	
	public GraphClassImpl(String name, Schema aSchema) {
		super(name);
		schema = aSchema;
		graphElementClasses = new HashMap<String, GraphElementClass>();
		edgeClasses = new HashMap<String, EdgeClass>();
		vertexClasses = new HashMap<String, VertexClass>();
		aggregationClasses = new HashMap<String, AggregationClass>();
		compositionClasses = new HashMap<String, CompositionClass>();
	}
	
	/* (non-Javadoc)
	 * @see jgralab.GraphClass#createEdgeClass(java.lang.String, jgralab.VertexClass, jgralab.VertexClass)
	 */
	public EdgeClass createEdgeClass(String name, VertexClass from, VertexClass to)  {
		return createEdgeClass(name, from, 0, Integer.MAX_VALUE, "", to, 0, Integer.MAX_VALUE, "");
	}

	/* (non-Javadoc)
	 * @see jgralab.GraphClass#createEdgeClass(java.lang.String, jgralab.VertexClass, java.lang.String, jgralab.VertexClass, java.lang.String)
	 */
	public EdgeClass createEdgeClass(String name, VertexClass from, String fromRoleName, VertexClass to, String toRoleName)  {
		return createEdgeClass(name, from, 0, Integer.MAX_VALUE, fromRoleName, to, 0, Integer.MAX_VALUE, toRoleName);
	}

	/* (non-Javadoc)
	 * @see jgralab.GraphClass#createEdgeClass(java.lang.String, jgralab.VertexClass, int, int, jgralab.VertexClass, int, int)
	 */
	public EdgeClass createEdgeClass(String name, VertexClass from, int fromMin, int fromMax, VertexClass to, int toMin, int toMax)  {
		return createEdgeClass(name, from, fromMin, fromMax, "", to, toMin, toMax, "");
	}
	
	/* (non-Javadoc)
	 * @see jgralab.GraphClass#createEdgeClass(java.lang.String, jgralab.VertexClass, int, int, java.lang.String, jgralab.VertexClass, int, int, java.lang.String)
	 */
	public EdgeClass createEdgeClass(String name, VertexClass from, int fromMin, int fromMax, String fromRoleName, VertexClass to, int toMin, int toMax, String toRoleName)  {
		if (!schema.isFreeSchemaElementName(name))
			throw new SchemaException(
					"there is already an element with the name " + name
							+ " in the schema", null);
		EdgeClassImpl ec = new EdgeClassImpl (name, this, from, fromMin, fromMax, fromRoleName, to, toMin, toMax, toRoleName);
		if (!name.equals("Edge")) {
			EdgeClass s = (EdgeClass) schema.getAttributedElementClass("Edge");
		//	System.out.println("Adding " + s.getName() + " as superclass of " + ec.getName());
			ec.addSuperClass(s);
		}
		from.addEdgeClass(ec);
		to.addEdgeClass(ec);
		
		graphElementClasses.put(name, ec);
		edgeClasses.put(name, ec);
		return ec;
	}

	/* (non-Javadoc)
	 * @see jgralab.GraphClass#createAggregationClass(java.lang.String, jgralab.VertexClass, boolean, jgralab.VertexClass)
	 */
	public AggregationClass createAggregationClass(String name, VertexClass from, boolean aggregateFrom, VertexClass to)  {
		return createAggregationClass(name, from, 0, Integer.MAX_VALUE, "", aggregateFrom, to, 0, Integer.MAX_VALUE, "");
	}

	/* (non-Javadoc)
	 * @see jgralab.GraphClass#createAggregationClass(java.lang.String, jgralab.VertexClass, java.lang.String, boolean, jgralab.VertexClass, java.lang.String)
	 */
	public AggregationClass createAggregationClass(String name, VertexClass from, String fromRoleName, boolean aggregateFrom, VertexClass to, String toRoleName)  {
		return createAggregationClass(name, from, 0, Integer.MAX_VALUE, "", aggregateFrom, to, 0, Integer.MAX_VALUE, "");
	}

	/* (non-Javadoc)
	 * @see jgralab.GraphClass#createAggregationClass(java.lang.String, jgralab.VertexClass, int, int, boolean, jgralab.VertexClass, int, int)
	 */
	public AggregationClass createAggregationClass(String name, VertexClass from, int fromMin, int fromMax, boolean aggregateFrom, VertexClass to, int toMin, int toMax)  {
		return createAggregationClass(name, from, fromMin, fromMax, "", aggregateFrom, to, toMin, toMax, "");
	}

	/* (non-Javadoc)
	 * @see jgralab.GraphClass#createAggregationClass(java.lang.String, jgralab.VertexClass, int, int, java.lang.String, boolean, jgralab.VertexClass, int, int, java.lang.String)
	 */
	public AggregationClass createAggregationClass(String name, VertexClass from, int fromMin, int fromMax, String fromRoleName, boolean aggregateFrom, VertexClass to, int toMin, int toMax, String toRoleName)  {
		if (!schema.isFreeSchemaElementName(name))
			throw new SchemaException(
					"there is already an element with the name " + name
							+ " in the schema", null);
		AggregationClassImpl ac = new AggregationClassImpl (name, this, from, fromMin, fromMax, fromRoleName, aggregateFrom, to, toMin, toMax, toRoleName);
		if (!name.equals("Aggregation"))
			ac.addSuperClass(schema.getAttributedElementClass("Aggregation"));
		else
			ac.addSuperClass(schema.getAttributedElementClass("Edge"));
		from.addEdgeClass(ac);
		to.addEdgeClass(ac);
		graphElementClasses.put(name, ac);
		aggregationClasses.put(name, ac);
		return ac;
	}

	/* (non-Javadoc)
	 * @see jgralab.GraphClass#createCompositionClass(java.lang.String, jgralab.VertexClass, boolean, jgralab.VertexClass)
	 */
	public CompositionClass createCompositionClass(String name, VertexClass from, boolean compositeFrom, VertexClass to)  {
		return createCompositionClass(name, from, 0, Integer.MAX_VALUE, "", compositeFrom, to, 0, Integer.MAX_VALUE, "");
	}

	/* (non-Javadoc)
	 * @see jgralab.GraphClass#createCompositionClass(java.lang.String, jgralab.VertexClass, java.lang.String, boolean, jgralab.VertexClass, java.lang.String)
	 */
	public CompositionClass createCompositionClass(String name, VertexClass from, String fromRoleName, boolean compositeFrom, VertexClass to, String toRoleName)  {
		return createCompositionClass(name, from, 0, Integer.MAX_VALUE, fromRoleName, compositeFrom, to, 0, Integer.MAX_VALUE, toRoleName);
	}

	/* (non-Javadoc)
	 * @see jgralab.GraphClass#createCompositionClass(java.lang.String, jgralab.VertexClass, int, int, boolean, jgralab.VertexClass, int, int)
	 */
	public CompositionClass createCompositionClass(String name, VertexClass from, int fromMin, int fromMax, boolean compositeFrom, VertexClass to, int toMin, int toMax)  {
		return createCompositionClass(name, from, fromMin, fromMax, "", compositeFrom, to, toMin, toMax, "");
	}

	/* (non-Javadoc)
	 * @see jgralab.GraphClass#createCompositionClass(java.lang.String, jgralab.VertexClass, int, int, java.lang.String, boolean, jgralab.VertexClass, int, int, java.lang.String)
	 */
	public CompositionClass createCompositionClass(String name, VertexClass from, int fromMin, int fromMax, String fromRoleName, boolean compositeFrom, VertexClass to, int toMin, int toMax, String toRoleName)  {
		if (!schema.isFreeSchemaElementName(name))
			throw new SchemaException(
					"there is already an element with the name " + name
							+ " in the schema", null);
		CompositionClassImpl cc = new CompositionClassImpl (name, this, from, fromMin, fromMax, fromRoleName, compositeFrom, to, toMin, toMax, toRoleName);
		if (!name.equals("Composition"))
			cc.addSuperClass(schema.getAttributedElementClass("Composition"));
		else
			cc.addSuperClass(schema.getAttributedElementClass("Aggregation"));
		from.addEdgeClass(cc);
		to.addEdgeClass(cc);
		graphElementClasses.put(name, cc);
		compositionClasses.put(name, cc);
		return cc;
	}
	
	
	/* (non-Javadoc)
	 * @see jgralab.GraphClass#createVertexClass(java.lang.String)
	 */
	public VertexClass createVertexClass(String name)  {
		if (!schema.isFreeSchemaElementName(name))
			throw new SchemaException(
					"there is already an element with the name " + name
							+ " in the schema", null);
		VertexClassImpl vc = new VertexClassImpl(name, this);
		vc.addSuperClass(schema.getAttributedElementClass("Vertex"));
		graphElementClasses.put(name, vc);
		vertexClasses.put(name, vc);
		return vc;
	}
	
	/* (non-Javadoc)
	 * @see jgralab.GraphClass#addSuperClass(jgralab.GraphClass)
	 */
	public void addSuperClass(GraphClass superClass)  {
		super.addSuperClass(superClass);
	}
	
	/* (non-Javadoc)
	 * @see jgralab.GraphClass#knows(jgralab.GraphElementClass)
	 */
	public boolean knowsOwn(GraphElementClass aGraphElementClass) {
		return (graphElementClasses.containsValue(aGraphElementClass));
	}
	
	/* (non-Javadoc)
	 * @see jgralab.GraphClass#knows(jgralab.GraphElementClass)
	 */
	public boolean knowsOwn(String aGraphElementClass) {
		return (graphElementClasses.containsKey(aGraphElementClass));
	}
	
	/* (non-Javadoc)
	 * @see jgralab.GraphClass#knows(java.lang.String)
	 */
	public boolean knows(GraphElementClass aGraphElementClass) {
		if (graphElementClasses.containsKey(aGraphElementClass))
			return true;
		for (AttributedElementClass superClass: directSuperClasses) {
			if (((GraphClass)superClass).knows(aGraphElementClass))
				return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see jgralab.GraphClass#knows(java.lang.String)
	 */
	public boolean knows(String aGraphElementClass) {
		if (graphElementClasses.containsKey(aGraphElementClass))
			return true;
		for (AttributedElementClass superClass: directSuperClasses) {
			if (((GraphClass)superClass).knows(aGraphElementClass))
				return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see jgralab.GraphClass#getGraphElementClass(java.lang.String)
	 */
	public GraphElementClass getGraphElementClass(String name) {
		if (graphElementClasses.containsKey(name))
			return graphElementClasses.get(name);
		for (AttributedElementClass superClass: directSuperClasses) {
			if (((GraphClass)superClass).knows(name))
				return ((GraphClass)superClass).getGraphElementClass(name);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String output = "GraphClassImpl '" + super.getName() + "'";
		if (isAbstract())
			output += " (abstract)";
		output += ": \n";
		
		output += "subClasses of '" + super.getName() + "': ";
		Iterator<AttributedElementClass> it = subClasses.iterator();
		while (it.hasNext()) {
			output+= "'"+((GraphClassImpl)it.next()).getName() + "' ";
		}
		
		output += "\nsuperClasses of '" + super.getName() + "': ";
		Iterator<AttributedElementClass> it2 = getAllSuperClasses().iterator();
		while (it2.hasNext()) {
			output+= "'"+((GraphClassImpl)it2.next()).getName() + "' ";
		}
		output += attributesToString();
		
		output += "\n\nGraphElementClasses of '" + super.getName() + "':\n\n";
		Iterator<GraphElementClass> it3 = graphElementClasses.values().iterator();
		while (it3.hasNext()) {
			output+= it3.next().toString() +"\n";
		}
		return output;
	}

	/* (non-Javadoc)
	 * @see jgralab.GraphClass#getGraphElementClasses()
	 */
	public List<GraphElementClass> getOwnGraphElementClasses() {
		return new ArrayList<GraphElementClass>(graphElementClasses.values());
	}
	
	/* (non-Javadoc)
	 * @see jgralab.GraphClass#getGraphElementClasses()
	 */
	public List<GraphElementClass> getGraphElementClasses() {
		List<GraphElementClass> allClasses = new ArrayList<GraphElementClass>();
		
		for (AttributedElementClass superGraphClass : getAllSuperClasses()) {
			allClasses.addAll(((GraphClass)superGraphClass).getOwnGraphElementClasses());
		}
		
		allClasses.addAll(graphElementClasses.values());
		
		return allClasses;
	}
	
	/* (non-Javadoc)
	 * @see jgralab.GraphClass#getGraphElementClasses()
	 */
	public List<EdgeClass> getOwnEdgeClasses() {
		return new ArrayList<EdgeClass>(edgeClasses.values());
	}
	
	/* (non-Javadoc)
	 * @see jgralab.GraphClass#getGraphElementClasses()
	 */
	public List<EdgeClass> getEdgeClasses() {
		List<EdgeClass> allClasses = new ArrayList<EdgeClass>();
		
		for (AttributedElementClass superGraphClass : getAllSuperClasses()) {
			allClasses.addAll(((GraphClass)superGraphClass).getOwnEdgeClasses());
		}
		
		allClasses.addAll(edgeClasses.values());
		
		return allClasses;
	}
	
	/* (non-Javadoc)
	 * @see jgralab.GraphClass#getGraphElementClasses()
	 */
	public List<CompositionClass> getOwnCompositionClasses() {
		return new ArrayList<CompositionClass>(compositionClasses.values());
	}
	
	/* (non-Javadoc)
	 * @see jgralab.GraphClass#getGraphElementClasses()
	 */
	public List<CompositionClass> getCompositionClasses() {
		List<CompositionClass> allClasses = new ArrayList<CompositionClass>();
		
		for (AttributedElementClass superGraphClass : getAllSuperClasses()) {
			allClasses.addAll(((GraphClass)superGraphClass).getOwnCompositionClasses());
		}
		
		allClasses.addAll(compositionClasses.values());
		
		return allClasses;
	}
	
	/* (non-Javadoc)
	 * @see jgralab.GraphClass#getGraphElementClasses()
	 */
	public List<AggregationClass> getOwnAggregationClasses() {
		return new ArrayList<AggregationClass>(aggregationClasses.values());
	}
	
	/* (non-Javadoc)
	 * @see jgralab.GraphClass#getGraphElementClasses()
	 */
	public List<AggregationClass> getAggregationClasses() {
		List<AggregationClass> allClasses = new ArrayList<AggregationClass>();
		
		for (AttributedElementClass superGraphClass : getAllSuperClasses()) {
			allClasses.addAll(((GraphClass)superGraphClass).getOwnAggregationClasses());
		}
		
		allClasses.addAll(aggregationClasses.values());
		
		return allClasses;
	}
	
	/* (non-Javadoc)
	 * @see jgralab.GraphClass#getGraphElementClasses()
	 */
	public List<VertexClass> getOwnVertexClasses() {
		return new ArrayList<VertexClass>(vertexClasses.values());
	}
	
	/* (non-Javadoc)
	 * @see jgralab.GraphClass#getGraphElementClasses()
	 */
	public List<VertexClass> getVertexClasses() {
		List<VertexClass> allClasses = new ArrayList<VertexClass>();
		
		for (AttributedElementClass superGraphClass : getAllSuperClasses()) {
			allClasses.addAll(((GraphClass)superGraphClass).getOwnVertexClasses());
		}
		
		allClasses.addAll(vertexClasses.values());
		
		return allClasses;
	}
	
	/* (non-Javadoc)
	 * @see jgralab.GraphClass#createGraph(jgralab.GraphClass, int, int)
	 */
//	public Graph createGraph(GraphClass aGraphClass, int vMax, int eMax)  {
//		return new GraphImpl(schema.generateId(), aGraphClass, schema, vMax, eMax);
//	}

	/* (non-Javadoc)
	 * @see jgralab.GraphClass#createGraph(java.lang.String, jgralab.GraphClass, int, int)
	 */
//	public Graph createGraph(String name, GraphClass aGraphClass, int vMax, int eMax)  {
//		return new GraphImpl(name, aGraphClass, schema, vMax, eMax);
//	}

	/* (non-Javadoc)
	 * @see jgralab.GraphClass#getSchema()
	 */
	public Schema getSchema() {
		return schema;
	}
	
	/* (non-Javadoc)
	 * @see jgralab.GraphClass#getEdgeClassCount()
	 */
	public int getOwnEdgeClassCount() {
		return edgeClasses.size();
	}
	
	
	/* (non-Javadoc)
	 * @see jgralab.GraphClass#getVertexClassCount()
	 */
	public int getOwnVertexClassCount() {
		return vertexClasses.size();
	}
	
	/* (non-Javadoc)
	 * @see jgralab.GraphClass#getVertexClass(String name)
	 */
	public VertexClass getVertexClass(String name) {
		VertexClass vc = vertexClasses.get(name);
		if (vc != null)
			return vc;
		for (AttributedElementClass superclass : directSuperClasses) {
			vc = ((GraphClass)superclass).getVertexClass(name);
			if (vc != null)
				return vc;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see jgralab.GraphClass#getEdgeClass(String name)
	 */
	public EdgeClass getEdgeClass(String name) {
		EdgeClass ec = edgeClasses.get(name);
		if (ec != null)
			return ec;
		ec = aggregationClasses.get(name);
		if (ec != null)
			return ec;
		ec = compositionClasses.get(name);
		if (ec != null)
			return ec;
		for (AttributedElementClass superclass : directSuperClasses) {
			ec = ((GraphClass)superclass).getEdgeClass(name);
			if (ec != null)
				return ec;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see jgralab.GraphClass#getCompositionClass(String name)
	 */
	public CompositionClass getCompositionClass(String name) {
		CompositionClass cc = compositionClasses.get(name);
		if (cc != null)
			return cc;
		for (AttributedElementClass superclass : directSuperClasses) {
			cc = ((GraphClass)superclass).getCompositionClass(name);
			if (cc != null)
				return cc;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see jgralab.GraphClass#getAggregationClass(String name)
	 */
	public AggregationClass getAggregationClass(String name) {
		AggregationClass ac = aggregationClasses.get(name);
		if (ac != null)
			return ac;
		ac = compositionClasses.get(name);
		if (ac != null)
			return ac;
		for (AttributedElementClass superclass : directSuperClasses) {
			ac = ((GraphClass)superclass).getAggregationClass(name);
			if (ac != null)
				return ac;
		}
		return null;
	}
	
}
