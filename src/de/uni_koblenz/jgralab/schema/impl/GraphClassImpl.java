/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
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

package de.uni_koblenz.jgralab.schema.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;


public class GraphClassImpl extends
		AttributedElementClassImpl<GraphClass, Graph> implements GraphClass {

	private Map<String, GraphElementClass<?, ?>> graphElementClasses = new HashMap<String, GraphElementClass<?, ?>>();

	private Map<String, VertexClass> vertexClasses = new HashMap<String, VertexClass>();

	DirectedAcyclicGraph<VertexClass> vertexClassDag = new DirectedAcyclicGraph<VertexClass>(
			true);
	private Map<String, EdgeClass> edgeClasses = new HashMap<String, EdgeClass>();

	DirectedAcyclicGraph<EdgeClass> edgeClassDag = new DirectedAcyclicGraph<EdgeClass>(
			true);

	private VertexClassImpl defaultVertexClass;

	private EdgeClassImpl defaultEdgeClass;

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
	protected GraphClassImpl(String gcName, SchemaImpl schema) {
		super(gcName, (PackageImpl) schema.getDefaultPackage(), schema);
		parentPackage.addGraphClass(this);
		schema.setGraphClass(this);
		defaultVertexClass = createDefaultVertexClass();
		defaultEdgeClass = createDefaultEdgeClass();
	}

	@Override
	public VertexClass getDefaultVertexClass() {
		return defaultVertexClass;
	}

	private VertexClassImpl createDefaultVertexClass() {
		VertexClassImpl vc = new VertexClassImpl(
				VertexClass.DEFAULTVERTEXCLASS_NAME,
				(PackageImpl) schema.getDefaultPackage(), this);
		vc.setAbstract(true);
		vc.setInternal(true);
		return vc;
	}

	private EdgeClassImpl createDefaultEdgeClass() {
		assert getDefaultVertexClass() != null : "Default VertexClass has not yet been created!";
		assert getDefaultEdgeClass() == null : "Default EdgeClass already created!";
		EdgeClassImpl ec = new EdgeClassImpl(EdgeClass.DEFAULTEDGECLASS_NAME,
				(PackageImpl) schema.getDefaultPackage(), this,
				defaultVertexClass, 0, Integer.MAX_VALUE, "",
				AggregationKind.NONE, defaultVertexClass, 0, Integer.MAX_VALUE,
				"", AggregationKind.NONE);
		ec.setAbstract(true);
		ec.setInternal(true);
		return ec;
	}

	@Override
	public EdgeClass getDefaultEdgeClass() {
		return defaultEdgeClass;
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

	@Override
	public String getVariableName() {
		return "gc_" + getQualifiedName().replace('.', '_');
	}

	@Override
	public EdgeClass createEdgeClass(String qualifiedName, VertexClass from,
			int fromMin, int fromMax, String fromRoleName,
			AggregationKind aggrFrom, VertexClass to, int toMin, int toMax,
			String toRoleName, AggregationKind aggrTo) {
		assertNotFinished();
		if (!(aggrFrom == AggregationKind.NONE)
				&& !(aggrTo == AggregationKind.NONE)) {
			throw new SchemaException(
					"At least one end of each class must be of AggregationKind NONE at EdgeClass "
							+ qualifiedName);
		}
		String[] qn = SchemaImpl.splitQualifiedName(qualifiedName);
		PackageImpl parent = (PackageImpl) schema.createPackageWithParents(qn[0]);
		EdgeClassImpl ec = new EdgeClassImpl(qn[1], parent, this, from,
				fromMin, fromMax, fromRoleName, aggrFrom, to, toMin, toMax,
				toRoleName, aggrTo);
		if (defaultEdgeClass != null) {
			ec.addSuperClass(defaultEdgeClass);
		}
		return ec;
	}

	@Override
	public VertexClass createVertexClass(String qualifiedName) {
		assertNotFinished();

		String[] qn = SchemaImpl.splitQualifiedName(qualifiedName);
		PackageImpl parent = (PackageImpl) ((SchemaImpl) getSchema())
				.createPackageWithParents(qn[0]);
		VertexClassImpl vc = new VertexClassImpl(qn[1], parent, this);
		if (defaultVertexClass != null) {
			vc.addSuperClass(defaultVertexClass);
		}
		return vc;
	}

	@Override
	public boolean knowsOwn(String qn) {
		return (graphElementClasses.containsKey(qn));
	}

	@Override
	public boolean knows(String qn) {
		return graphElementClasses.containsKey(qn);
	}

	@Override
	public GraphElementClass<?, ?> getGraphElementClass(String qn) {
		return graphElementClasses.get(qn);
	}

	public String getDescriptionString() {
		StringBuilder output = new StringBuilder("GraphClassImpl '"
				+ getQualifiedName() + "'");
		if (isAbstract()) {
			output.append(" (abstract)");
		}
		output.append(":\n");
		output.append(attributesToString());
		output.append("\n\nGraphElementClasses of '" + getQualifiedName()
				+ "':\n\n");
		Iterator<GraphElementClass<?, ?>> it3 = graphElementClasses.values()
				.iterator();
		while (it3.hasNext()) {
			output.append(it3.next().toString() + "\n");
		}
		return output.toString();
	}

	@Override
	public List<GraphElementClass<?, ?>> getGraphElementClasses() {
		return new ArrayList<GraphElementClass<?, ?>>(
				graphElementClasses.values());
	}

	@Override
	public List<EdgeClass> getEdgeClasses() {
		return edgeClassDag.getNodesInTopologicalOrder();
	}

	@Override
	public List<VertexClass> getVertexClasses() {
		return vertexClassDag.getNodesInTopologicalOrder();
	}

	@Override
	public VertexClass getVertexClass(String qn) {
		return vertexClasses.get(qn);
	}

	@Override
	public EdgeClass getEdgeClass(String qn) {
		return edgeClasses.get(qn);
	}

	@Override
	public int getEdgeClassCount() {
		return edgeClasses.size();
	}

	@Override
	public int getVertexClassCount() {
		return vertexClasses.size();
	}

	@Override
	protected void finish() {
		assertNotFinished();
		vertexClassDag.finish();
		edgeClassDag.finish();
		for (VertexClass vc : vertexClassDag.getNodesInTopologicalOrder()) {
			((VertexClassImpl) vc).finish();
		}
		for (EdgeClass ec : edgeClassDag.getNodesInTopologicalOrder()) {
			((EdgeClassImpl) ec).finish();
		}
		super.finish();
	}

	@Override
	public boolean hasOwnAttributes() {
		return hasAttributes();
	}

	@Override
	public Attribute getOwnAttribute(String name) {
		return getAttribute(name);
	}

	@Override
	public int getOwnAttributeCount() {
		return getAttributeCount();
	}

	@Override
	public List<Attribute> getOwnAttributeList() {
		return getAttributeList();
	}
}
