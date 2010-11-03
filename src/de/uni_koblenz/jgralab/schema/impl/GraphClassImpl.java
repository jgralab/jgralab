/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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

package de.uni_koblenz.jgralab.schema.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.exception.InheritanceException;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;

public final class GraphClassImpl extends AttributedElementClassImpl implements
		GraphClass {

	private Map<String, EdgeClass> edgeClasses = new HashMap<String, EdgeClass>();

	private Map<String, GraphElementClass> graphElementClasses = new HashMap<String, GraphElementClass>();

	private Map<String, VertexClass> vertexClasses = new HashMap<String, VertexClass>();

	static GraphClass createDefaultGraphClass(SchemaImpl schema) {
		assert schema.getDefaultPackage() != null : "DefaultPackage has not yet been created!";
		assert schema.getDefaultGraphClass() == null : "DefaultGraphClass already created!";
		GraphClass gc = new GraphClassImpl(schema);
		gc.setAbstract(true);
		return gc;
	}

	private GraphClassImpl(SchemaImpl schema) {
		this(DEFAULTGRAPHCLASS_NAME, schema);
	}

	/**
	 * Creates the <b>sole</b> <code>GraphClass</code> in the
	 * <code>Schema</code>, that holds all <code>GraphElementClasses</code>/
	 * <code>EdgeClasses</code>/ <code>VertexClasses</code>/
	 * <code>AggregationClasses</code>/ <code>CompositionClasses</code>.
	 * <p>
	 * <b>Caution:</b> The <code>GraphClass</code> should only be created by
	 * using
	 * {@link de.uni_koblenz.jgralab.schema.Schema#createGraphClass(String qualifiedName)}
	 * in <code>Schema</code>. Unfortunately, due to restrictions in Java, the
	 * visibility of this constructor cannot be changed without causing serious
	 * issues in the program.
	 * </p>
	 * 
	 * @param qn
	 *            a unique name in the <code>Schema</code>
	 * @param aSchema
	 *            the <code>Schema</code> containing this
	 *            <code>GraphClass</code>
	 */
	GraphClassImpl(String gcName, SchemaImpl schema) {
		super(gcName, schema.getDefaultPackage(), schema);
		register();
	}

	void addEdgeClass(EdgeClass ec) {
		if (edgeClasses.containsKey(ec.getQualifiedName())) {
			throw new SchemaException("Duplicate edge class name '"
					+ ec.getQualifiedName() + "'");
		}
		if (graphElementClasses.containsKey(ec.getQualifiedName())) {
			throw new SchemaException("Edge class name '"
					+ ec.getQualifiedName()
					+ "' already used as vertex class name");
		}
		graphElementClasses.put(ec.getQualifiedName(), ec);
		edgeClasses.put(ec.getQualifiedName(), ec);
	}

	void addVertexClass(VertexClass vc) {
		if (vertexClasses.containsKey(vc.getQualifiedName())) {
			throw new SchemaException("Duplicate vertex class name '"
					+ vc.getQualifiedName() + "'");
		}
		if (graphElementClasses.containsKey(vc.getQualifiedName())) {
			throw new SchemaException("Vertex class name '"
					+ vc.getQualifiedName()
					+ "' already used as edge class name");
		}

		graphElementClasses.put(vc.getQualifiedName(), vc);
		vertexClasses.put(vc.getQualifiedName(), vc);
	}

	public void addSuperClass(GraphClass superClass) {
		// only the internal abstract base class "Graph" can be a superclass
		if (!superClass.getQualifiedName().equals(
				getSchema().getDefaultGraphClass().getQualifiedName())) {
			throw new InheritanceException(
					"GraphClass can not be generealized.");
		}
		super.addSuperClass(superClass);
	}

	@Override
	protected final void register() {
		assert parentPackage == getSchema().getDefaultPackage() : "The GraphClass must be in the default package.";
		((PackageImpl) parentPackage).addGraphClass(this);
		if (!getSimpleName().equals(GraphClass.DEFAULTGRAPHCLASS_NAME)) {
			((SchemaImpl) getSchema()).setGraphClass(this);
		}
	}

	@Override
	public String getVariableName() {
		return "gc_" + getQualifiedName().replace('.', '_');
	}

	@Override
	public EdgeClass createEdgeClass(String qualifiedName, VertexClass from,
			int fromMin, int fromMax, String fromRoleName,
			AggregationKind aggrFrom, VertexClass to, int toMin, int toMax,
			String toRoleName, AggregationKind aggrTo) {
		if (!(aggrFrom == AggregationKind.NONE)
				&& !(aggrTo == AggregationKind.NONE)) {
			throw new SchemaException(
					"At least one end of each class must be of AggregationKind NONE at EdgeClass "
							+ qualifiedName);
		}
		String[] qn = SchemaImpl.splitQualifiedName(qualifiedName);
		Package parent = ((SchemaImpl) getSchema())
				.createPackageWithParents(qn[0]);
		EdgeClassImpl ec = new EdgeClassImpl(qn[1], parent, this, from,
				fromMin, fromMax, fromRoleName, aggrFrom, to, toMin, toMax,
				toRoleName, aggrTo);
		if (!ec.getQualifiedName().equals(EdgeClass.DEFAULTEDGECLASS_NAME)) {
			EdgeClass s = getSchema().getDefaultEdgeClass();
			ec.addSuperClass(s);
		}
		return ec;
	}

	@Override
	public VertexClass createVertexClass(String qualifiedName) {
		String[] qn = SchemaImpl.splitQualifiedName(qualifiedName);
		Package parent = ((SchemaImpl) getSchema())
				.createPackageWithParents(qn[0]);
		VertexClassImpl vc = new VertexClassImpl(qn[1], parent, this);
		vc.addSuperClass(getSchema().getDefaultVertexClass());
		return vc;
	}

	@Override
	public boolean knowsOwn(GraphElementClass aGraphElementClass) {
		return (graphElementClasses.containsKey(aGraphElementClass
				.getQualifiedName()));
	}

	@Override
	public boolean knowsOwn(String qn) {
		return (graphElementClasses.containsKey(qn));
	}

	@Override
	public boolean knows(GraphElementClass aGraphElementClass) {
		if (graphElementClasses.containsKey(aGraphElementClass
				.getQualifiedName())) {
			return true;
		}
		for (AttributedElementClass superClass : directSuperClasses) {
			if (((GraphClass) superClass).knows(aGraphElementClass)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean knows(String qn) {
		if (graphElementClasses.containsKey(qn)) {
			return true;
		}
		for (AttributedElementClass superClass : directSuperClasses) {
			if (((GraphClass) superClass).knows(qn)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public GraphElementClass getGraphElementClass(String qn) {
		if (graphElementClasses.containsKey(qn)) {
			return graphElementClasses.get(qn);
		}
		for (AttributedElementClass superClass : directSuperClasses) {
			if (((GraphClass) superClass).knows(qn)) {
				return ((GraphClass) superClass).getGraphElementClass(qn);
			}
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder output = new StringBuilder("GraphClassImpl '"
				+ getQualifiedName() + "'");
		if (isAbstract()) {
			output.append(" (abstract)");
		}
		output.append(": \n");

		output.append("subClasses of '" + getQualifiedName() + "': ");
		Iterator<AttributedElementClass> it = getAllSubClasses().iterator();
		while (it.hasNext()) {
			output.append("'" + ((GraphClassImpl) it.next()).getQualifiedName()
					+ "' ");
		}

		output.append("\nsuperClasses of '" + getQualifiedName() + "': ");
		Iterator<AttributedElementClass> it2 = getAllSuperClasses().iterator();
		while (it2.hasNext()) {
			output.append("'"
					+ ((GraphClassImpl) it2.next()).getQualifiedName() + "' ");
		}
		output.append(attributesToString());

		output.append("\n\nGraphElementClasses of '" + getQualifiedName()
				+ "':\n\n");
		Iterator<GraphElementClass> it3 = graphElementClasses.values()
				.iterator();
		while (it3.hasNext()) {
			output.append(it3.next().toString() + "\n");
		}
		return output.toString();
	}

	@Override
	public List<GraphElementClass> getGraphElementClasses() {
		return new ArrayList<GraphElementClass>(graphElementClasses.values());
	}

	@Override
	public List<EdgeClass> getEdgeClasses() {
		return new ArrayList<EdgeClass>(edgeClasses.values());
	}

	@Override
	public List<VertexClass> getVertexClasses() {
		return new ArrayList<VertexClass>(vertexClasses.values());
	}

	@Override
	public VertexClass getVertexClass(String qn) {
		VertexClass vc = vertexClasses.get(qn);
		if (vc != null) {
			return vc;
		}
		for (AttributedElementClass superclass : directSuperClasses) {
			vc = ((GraphClass) superclass).getVertexClass(qn);
			if (vc != null) {
				return vc;
			}
		}
		return null;
	}

	@Override
	public EdgeClass getEdgeClass(String qn) {
		EdgeClass ec = edgeClasses.get(qn);
		if (ec != null) {
			return ec;
		}
		for (AttributedElementClass superclass : directSuperClasses) {
			ec = ((GraphClass) superclass).getEdgeClass(qn);
			if (ec != null) {
				return ec;
			}
		}
		return null;
	}

	@Override
	public int getEdgeClassCount() {
		return edgeClasses.size();
	}

	@Override
	public int getVertexClassCount() {
		return vertexClasses.size();
	}

}
