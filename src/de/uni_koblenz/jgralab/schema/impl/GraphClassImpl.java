/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pcollections.PVector;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;

public final class GraphClassImpl extends
		AttributedElementClassImpl<GraphClass, Graph> implements GraphClass {

	Map<String, VertexClass> vertexClasses = new HashMap<String, VertexClass>();

	DirectedAcyclicGraph<VertexClass> vertexClassDag = new DirectedAcyclicGraph<VertexClass>(
			true);
	Map<String, EdgeClass> edgeClasses = new HashMap<String, EdgeClass>();

	DirectedAcyclicGraph<EdgeClass> edgeClassDag = new DirectedAcyclicGraph<EdgeClass>(
			true);

	PVector<VertexClass> userDefinedVertexClasses;
	PVector<EdgeClass> userDefinedEdgeClasses;
	PVector<GraphElementClass<?, ?>> userDefinedGraphElementClasses;

	private VertexClassImpl defaultVertexClass;

	private EdgeClassImpl defaultEdgeClass;

	private TemporaryVertexClassImpl tempVertexClass;

	private TemporaryEdgeClassImpl tempEdgeClass;

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
	GraphClassImpl(String gcName, SchemaImpl schema,
			ClassLoader schemaClassLoader) {
		super(gcName, (PackageImpl) schema.getDefaultPackage(), schema,
				schemaClassLoader);
		schema.setGraphClass(this);
	}

	@Override
	public final VertexClass getDefaultVertexClass() {
		return defaultVertexClass;
	}

	final void initializeDefaultVertexClass() {
		VertexClassImpl vc = new VertexClassImpl(
				VertexClass.DEFAULTVERTEXCLASS_NAME,
				(PackageImpl) schema.getDefaultPackage(), this,
				schemaClassLoader);
		vc.setAbstract(true);
		defaultVertexClass = vc;
	}

	final void initializeDefaultEdgeClass() {
		assert getDefaultVertexClass() != null : "Default VertexClass has not yet been created!";
		assert getDefaultEdgeClass() == null : "Default EdgeClass already created!";
		EdgeClassImpl ec = new EdgeClassImpl(EdgeClass.DEFAULTEDGECLASS_NAME,
				(PackageImpl) schema.getDefaultPackage(), this,
				defaultVertexClass, 0, Integer.MAX_VALUE, "",
				AggregationKind.NONE, defaultVertexClass, 0, Integer.MAX_VALUE,
				"", AggregationKind.NONE, schemaClassLoader);
		ec.setAbstract(true);
		defaultEdgeClass = ec;
	}

	@Override
	public final VertexClass getTemporaryVertexClass() {
		return tempVertexClass;
	}

	final void initializeTemporaryVertexClass() {
		assert getTemporaryVertexClass() == null : "TemporaryVertexClass already created!";
		tempVertexClass = new TemporaryVertexClassImpl(this, schemaClassLoader);
	}

	final void initializeTemporaryEdgeClass() {
		assert getDefaultVertexClass() != null : "Default VertexClass has not yet been created!";
		assert getTemporaryEdgeClass() == null : "TemporaryEdgeClass already created!";
		this.tempEdgeClass = new TemporaryEdgeClassImpl(this, schemaClassLoader);
	}

	@Override
	public final EdgeClass getTemporaryEdgeClass() {
		return tempEdgeClass;
	}

	@Override
	public final EdgeClass getDefaultEdgeClass() {
		return defaultEdgeClass;
	}

	void addEdgeClass(EdgeClass ec) {
		if (edgeClasses.containsKey(ec.getQualifiedName())) {
			throw new SchemaException("Duplicate edge class name '"
					+ ec.getQualifiedName() + "'");
		}
		// Don't track the default EC
		if (!ec.getQualifiedName().equals(EdgeClass.DEFAULTEDGECLASS_NAME)
				&& !ec.getQualifiedName().equals(
						EdgeClass.TEMPORARYEDGECLASS_NAME)) {
			edgeClasses.put(ec.getQualifiedName(), ec);
		}
	}

	void addVertexClass(VertexClass vc) {
		if (vertexClasses.containsKey(vc.getQualifiedName())) {
			throw new SchemaException("Duplicate vertex class name '"
					+ vc.getQualifiedName() + "'");
		}
		// Don't track the default VC
		if (!vc.getQualifiedName().equals(VertexClass.DEFAULTVERTEXCLASS_NAME)
				&& !vc.getQualifiedName().equals(
						VertexClass.TEMPORARYVERTEXCLASS_NAME)) {
			vertexClasses.put(vc.getQualifiedName(), vc);
		}

	}

	@Override
	public final EdgeClass createEdgeClass(String qualifiedName,
			VertexClass from, int fromMin, int fromMax, String fromRoleName,
			AggregationKind aggrFrom, VertexClass to, int toMin, int toMax,
			String toRoleName, AggregationKind aggrTo) {
		assertNotFinished();
		if (from.isDefaultGraphElementClass()
				|| to.isDefaultGraphElementClass()) {
			throw new SchemaException(
					"EdgeClasses starting or ending at the default "
							+ "VertexClass Vertex are forbidden.");
		}
		if (!(aggrFrom == AggregationKind.NONE)
				&& !(aggrTo == AggregationKind.NONE)) {
			throw new SchemaException(
					"At least one end of each class must be of AggregationKind NONE at EdgeClass "
							+ qualifiedName);
		}
		String[] qn = SchemaImpl.splitQualifiedName(qualifiedName);
		PackageImpl parent = schema.createPackageWithParents(qn[0]);
		EdgeClassImpl ec = new EdgeClassImpl(qn[1], parent, this, from,
				fromMin, fromMax, fromRoleName, aggrFrom, to, toMin, toMax,
				toRoleName, aggrTo, schemaClassLoader);
		if (defaultEdgeClass != null) {
			ec.addSuperClass(defaultEdgeClass);
		}
		return ec;
	}

	@Override
	public final VertexClass createVertexClass(String qualifiedName) {
		assertNotFinished();

		String[] qn = SchemaImpl.splitQualifiedName(qualifiedName);
		PackageImpl parent = ((SchemaImpl) getSchema())
				.createPackageWithParents(qn[0]);
		VertexClassImpl vc = new VertexClassImpl(qn[1], parent, this,
				schemaClassLoader);
		if (defaultVertexClass != null) {
			vc.addSuperClass(defaultVertexClass);
		}
		return vc;
	}

	@Override
	public final GraphElementClass<?, ?> getGraphElementClass(String qn) {
		GraphElementClass<?, ?> gec = vertexClasses.get(qn);
		if (gec != null) {
			return gec;
		}
		return edgeClasses.get(qn);
	}

	@Override
	public final List<GraphElementClass<?, ?>> getGraphElementClasses() {
		if (finished) {
			return userDefinedGraphElementClasses;
		}
		PVector<GraphElementClass<?, ?>> vec = JGraLab.vector();
		return vec.plusAll(getVertexClasses()).plusAll(getEdgeClasses());
	}

	@Override
	public final List<EdgeClass> getEdgeClasses() {
		if (finished) {
			return userDefinedEdgeClasses;
		}

		PVector<EdgeClass> vec = edgeClassDag.getNodesInTopologicalOrder();
		assert vec.get(0) == defaultEdgeClass;
		assert vec.get(1) == tempEdgeClass;
		return vec.subList(2, vec.size());
	}

	@Override
	public final List<VertexClass> getVertexClasses() {
		if (finished) {
			return userDefinedVertexClasses;
		}
		PVector<VertexClass> vec = vertexClassDag.getNodesInTopologicalOrder();
		assert vec.get(0) == defaultVertexClass;
		assert vec.get(1) == tempVertexClass;
		return vec.subList(2, vec.size());
	}

	@Override
	public final VertexClass getVertexClass(String qn) {
		return vertexClasses.get(qn);
	}

	@Override
	public final EdgeClass getEdgeClass(String qn) {
		return edgeClasses.get(qn);
	}

	@Override
	public final int getEdgeClassCount() {
		return edgeClasses.size();
	}

	@Override
	public final int getVertexClassCount() {
		return vertexClasses.size();
	}

	@Override
	protected final void finish() {
		assertNotFinished();
		vertexClassDag.finish();
		edgeClassDag.finish();
		for (VertexClass vc : vertexClassDag.getNodesInTopologicalOrder()) {
			((VertexClassImpl) vc).finish();
		}
		for (EdgeClass ec : edgeClassDag.getNodesInTopologicalOrder()) {
			((EdgeClassImpl) ec).finish();
		}

		userDefinedVertexClasses = vertexClassDag.getNodesInTopologicalOrder();
		assert userDefinedVertexClasses.get(0) == defaultVertexClass;
		assert userDefinedVertexClasses.get(1) == tempVertexClass;
		userDefinedVertexClasses = userDefinedVertexClasses.subList(2,
				userDefinedVertexClasses.size());

		userDefinedEdgeClasses = edgeClassDag.getNodesInTopologicalOrder();
		assert userDefinedEdgeClasses.get(0) == defaultEdgeClass;
		assert userDefinedEdgeClasses.get(1) == tempEdgeClass;
		userDefinedEdgeClasses = userDefinedEdgeClasses.subList(2,
				userDefinedEdgeClasses.size());

		userDefinedGraphElementClasses = JGraLab.vector();
		userDefinedGraphElementClasses = userDefinedGraphElementClasses
				.plusAll(userDefinedVertexClasses).plusAll(
						userDefinedEdgeClasses);
		super.finish();
	}

	@Override
	public final boolean hasOwnAttributes() {
		return hasAttributes();
	}

	@Override
	public final Attribute getOwnAttribute(String name) {
		return getAttribute(name);
	}

	@Override
	public final int getOwnAttributeCount() {
		return getAttributeCount();
	}

	@Override
	public final List<Attribute> getOwnAttributeList() {
		return getAttributeList();
	}

	@Override
	public void setQualifiedName(String newQName) {
		if (qualifiedName.equals(newQName)) {
			return;
		}
		if (schema.knows(newQName)) {
			throw new SchemaException(newQName
					+ " is already known to the schema.");
		}
		if (newQName.contains(".")) {
			throw new SchemaException(
					"The GraphClass must be in the default package. "
							+ "You tried to move it to '" + newQName + "'.");
		}

		unregister();

		qualifiedName = newQName;
		simpleName = newQName;

		register();
	}

	@Override
	protected final void reopen() {
		for (VertexClass vc : vertexClassDag.getNodesInTopologicalOrder()) {
			((VertexClassImpl) vc).reopen();
		}
		for (EdgeClass ec : edgeClassDag.getNodesInTopologicalOrder()) {
			((EdgeClassImpl) ec).reopen();
		}
		vertexClassDag.reopen();
		edgeClassDag.reopen();
		super.reopen();
	}

	@Override
	protected void deleteAttribute(AttributeImpl attr) {
		allAttributes = allAttributes.minus(attr);
	}

}
