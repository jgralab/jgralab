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

package de.uni_koblenz.jgralab.schema.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.uni_koblenz.jgralab.schema.AggregationClass;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.CompositionClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.GraphElementClass;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.exception.InheritanceException;

public final class GraphClassImpl extends AttributedElementClassImpl implements
		GraphClass {

	private Map<String, AggregationClass> aggregationClasses = new HashMap<String, AggregationClass>();

	private Map<String, CompositionClass> compositionClasses = new HashMap<String, CompositionClass>();

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

	void addAggregationClass(AggregationClass ac) {
		assert !aggregationClasses.containsKey(ac.getQualifiedName()) : "There already is an AggregationClass with the qualified name: "
				+ ac.getQualifiedName() + " in the GraphClass!";
		assert !graphElementClasses.containsKey(ac.getQualifiedName()) : "There already is a GraphElementClass with the qualified name: "
				+ ac.getQualifiedName() + " in this GraphClass!";

		graphElementClasses.put(ac.getQualifiedName(), ac);
		aggregationClasses.put(ac.getQualifiedName(), ac);
	}

	void addCompositionClass(CompositionClass cc) {
		assert !compositionClasses.containsKey(cc.getQualifiedName()) : "There already is a CompositionClass with the qualified name: "
				+ cc.getQualifiedName() + " in the GraphClass!";
		assert !graphElementClasses.containsKey(cc.getQualifiedName()) : "There already is a GraphElementClass with the qualified name: "
				+ cc.getQualifiedName() + " in this GraphClass!";

		graphElementClasses.put(cc.getQualifiedName(), cc);
		compositionClasses.put(cc.getQualifiedName(), cc);
	}

	void addEdgeClass(EdgeClass ec) {
		assert !edgeClasses.containsKey(ec.getQualifiedName()) : "There already is an EdgeClass with the qualified name: "
				+ ec.getQualifiedName() + " in the GraphClass!";
		assert !graphElementClasses.containsKey(ec.getQualifiedName()) : "There already is a GraphElementClass with the qualified name: "
				+ ec.getQualifiedName() + " in this GraphClass!";

		graphElementClasses.put(ec.getQualifiedName(), ec);
		edgeClasses.put(ec.getQualifiedName(), ec);
	}

	void addVertexClass(VertexClass vc) {
		assert !vertexClasses.containsKey(vc.getQualifiedName()) : "There already is a VertexClass with the qualified name '"
				+ vc.getQualifiedName()
				+ "' in the GraphClass '"
				+ qualifiedName + "'.";
		assert !graphElementClasses.containsKey(vc.getQualifiedName()) : "There already is a GraphElementClass with the qualified name: "
				+ vc.getQualifiedName() + " in this GraphClass!";

		graphElementClasses.put(vc.getQualifiedName(), vc);
		vertexClasses.put(vc.getQualifiedName(), vc);
	}

	@Override
	public void addSubClass(GraphClass subClass) {
		throw new InheritanceException("GraphClass can not be generealized.");
	}

	@Override
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
		assert parentPackage == getSchema().getDefaultPackage() : "A GraphClass must be in the default package.";
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
			VertexClass to) {
		return createEdgeClass(qualifiedName, from, 0, Integer.MAX_VALUE, "",
				to, 0, Integer.MAX_VALUE, "");
	}

	@Override
	public EdgeClass createEdgeClass(String qualifiedName, VertexClass from,
			String fromRoleName, VertexClass to, String toRoleName) {
		return createEdgeClass(qualifiedName, from, 0, Integer.MAX_VALUE,
				fromRoleName, to, 0, Integer.MAX_VALUE, toRoleName);
	}

	public EdgeClass createEdgeClass(String qualifiedName, VertexClass from,
			int fromMin, int fromMax, VertexClass to, int toMin, int toMax) {
		return createEdgeClass(qualifiedName, from, fromMin, fromMax, "", to,
				toMin, toMax, "");
	}

	@Override
	public EdgeClass createEdgeClass(String qualifiedName, VertexClass from,
			int fromMin, int fromMax, String fromRoleName, VertexClass to,
			int toMin, int toMax, String toRoleName) {
		String[] qn = SchemaImpl.splitQualifiedName(qualifiedName);
		Package parent = ((SchemaImpl) getSchema())
				.createPackageWithParents(qn[0]);
		EdgeClassImpl ec = new EdgeClassImpl(qn[1], parent, this, from,
				fromMin, fromMax, fromRoleName, to, toMin, toMax, toRoleName);
		if (!ec.getQualifiedName().equals(EdgeClass.DEFAULTEDGECLASS_NAME)) {
			EdgeClass s = getSchema().getDefaultEdgeClass();
			ec.addSuperClass(s);
		}
		from.addEdgeClass(ec);
		to.addEdgeClass(ec);
		return ec;
	}

	@Override
	public AggregationClass createAggregationClass(String qualifiedName,
			VertexClass from, boolean aggregateFrom, VertexClass to) {
		return createAggregationClass(qualifiedName, from, 0,
				Integer.MAX_VALUE, "", aggregateFrom, to, 0, Integer.MAX_VALUE,
				"");
	}

	@Override
	public AggregationClass createAggregationClass(String qualifiedName,
			VertexClass from, String fromRoleName, boolean aggregateFrom,
			VertexClass to, String toRoleName) {
		return createAggregationClass(qualifiedName, from, 0,
				Integer.MAX_VALUE, "", aggregateFrom, to, 0, Integer.MAX_VALUE,
				"");
	}

	@Override
	public AggregationClass createAggregationClass(String qualifiedName,
			VertexClass from, int fromMin, int fromMax, boolean aggregateFrom,
			VertexClass to, int toMin, int toMax) {
		return createAggregationClass(qualifiedName, from, fromMin, fromMax,
				"", aggregateFrom, to, toMin, toMax, "");
	}

	@Override
	public AggregationClass createAggregationClass(String qualifiedName,
			VertexClass from, int fromMin, int fromMax, String fromRoleName,
			boolean aggregateFrom, VertexClass to, int toMin, int toMax,
			String toRoleName) {
		String[] qn = SchemaImpl.splitQualifiedName(qualifiedName);
		Package parent = ((SchemaImpl) getSchema())
				.createPackageWithParents(qn[0]);
		AggregationClassImpl ac = new AggregationClassImpl(qn[1], parent, this,
				from, fromMin, fromMax, fromRoleName, aggregateFrom, to, toMin,
				toMax, toRoleName);
		if (!ac.getQualifiedName().equals(
				AggregationClass.DEFAULTAGGREGATIONCLASS_NAME)) {
			ac.addSuperClass(getSchema().getDefaultAggregationClass());
		} else {
			ac.addSuperClass(getSchema().getDefaultEdgeClass());
		}
		from.addEdgeClass(ac);
		to.addEdgeClass(ac);
		return ac;
	}

	@Override
	public CompositionClass createCompositionClass(String qualifiedName,
			VertexClass from, boolean compositeFrom, VertexClass to) {
		if (compositeFrom) {
			return createCompositionClass(qualifiedName, from, 1, 1, "",
					compositeFrom, to, 0, Integer.MAX_VALUE, "");
		} else {
			return createCompositionClass(qualifiedName, from, 0,
					Integer.MAX_VALUE, "", compositeFrom, to, 1, 1, "");
		}
	}

	@Override
	public CompositionClass createCompositionClass(String qualifiedName,
			VertexClass from, String fromRoleName, boolean compositeFrom,
			VertexClass to, String toRoleName) {
		if (compositeFrom) {
			return createCompositionClass(qualifiedName, from, 1, 1,
					fromRoleName, compositeFrom, to, 0, Integer.MAX_VALUE,
					toRoleName);
		} else {
			return createCompositionClass(qualifiedName, from, 0,
					Integer.MAX_VALUE, fromRoleName, compositeFrom, to, 1, 1,
					toRoleName);
		}
	}

	@Override
	public CompositionClass createCompositionClass(String qualifiedName,
			VertexClass from, int fromMin, int fromMax, boolean compositeFrom,
			VertexClass to, int toMin, int toMax) {
		return createCompositionClass(qualifiedName, from, fromMin, fromMax,
				"", compositeFrom, to, toMin, toMax, "");
	}

	@Override
	public CompositionClass createCompositionClass(String qualifiedName,
			VertexClass from, int fromMin, int fromMax, String fromRoleName,
			boolean compositeFrom, VertexClass to, int toMin, int toMax,
			String toRoleName) {
		String[] qn = SchemaImpl.splitQualifiedName(qualifiedName);
		Package parent = ((SchemaImpl) getSchema())
				.createPackageWithParents(qn[0]);
		CompositionClassImpl cc = new CompositionClassImpl(qn[1], parent, this,
				from, fromMin, fromMax, fromRoleName, compositeFrom, to, toMin,
				toMax, toRoleName);

		if (!cc.getQualifiedName().equals(
				CompositionClass.DEFAULTCOMPOSITIONCLASS_NAME)) {
			cc.addSuperClass(getSchema().getDefaultCompositionClass());
		} else {
			cc.addSuperClass(getSchema().getDefaultAggregationClass());
		}
		from.addEdgeClass(cc);
		to.addEdgeClass(cc);
		return cc;
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
		return (graphElementClasses.containsValue(aGraphElementClass));
	}

	@Override
	public boolean knowsOwn(String qn) {
		return (graphElementClasses.containsKey(qn));
	}

	@Override
	public boolean knows(GraphElementClass aGraphElementClass) {
		if (graphElementClasses.containsKey(aGraphElementClass)) {
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
		String output = "GraphClassImpl '" + getQualifiedName() + "'";
		if (isAbstract()) {
			output += " (abstract)";
		}
		output += ": \n";

		output += "subClasses of '" + getQualifiedName() + "': ";
		Iterator<AttributedElementClass> it = getAllSubClasses().iterator();
		while (it.hasNext()) {
			output += "'" + ((GraphClassImpl) it.next()).getQualifiedName()
					+ "' ";
		}

		output += "\nsuperClasses of '" + getQualifiedName() + "': ";
		Iterator<AttributedElementClass> it2 = getAllSuperClasses().iterator();
		while (it2.hasNext()) {
			output += "'" + ((GraphClassImpl) it2.next()).getQualifiedName()
					+ "' ";
		}
		output += attributesToString();

		output += "\n\nGraphElementClasses of '" + getQualifiedName()
				+ "':\n\n";
		Iterator<GraphElementClass> it3 = graphElementClasses.values()
				.iterator();
		while (it3.hasNext()) {
			output += it3.next().toString() + "\n";
		}
		return output;
	}

	@Override
	public List<GraphElementClass> getOwnGraphElementClasses() {
		return new ArrayList<GraphElementClass>(graphElementClasses.values());
	}

	@Override
	public List<GraphElementClass> getGraphElementClasses() {
		List<GraphElementClass> allClasses = new ArrayList<GraphElementClass>();

		for (AttributedElementClass superGraphClass : getAllSuperClasses()) {
			allClasses.addAll(((GraphClass) superGraphClass)
					.getOwnGraphElementClasses());
		}

		allClasses.addAll(graphElementClasses.values());

		return allClasses;
	}

	@Override
	public List<EdgeClass> getOwnEdgeClasses() {
		List<EdgeClass> list = new ArrayList<EdgeClass>(edgeClasses.values());
		for (EdgeClass ac : getOwnAggregationClasses()) {
			list.add(ac);
		}
		return list;
	}

	@Override
	public List<EdgeClass> getEdgeClasses() {
		List<EdgeClass> allClasses = new ArrayList<EdgeClass>();

		for (AttributedElementClass superGraphClass : getAllSuperClasses()) {
			allClasses.addAll(((GraphClass) superGraphClass)
					.getOwnEdgeClasses());
		}

		allClasses.addAll(getOwnEdgeClasses());

		return allClasses;
	}

	@Override
	public List<CompositionClass> getOwnCompositionClasses() {
		return new ArrayList<CompositionClass>(compositionClasses.values());
	}

	@Override
	public List<CompositionClass> getCompositionClasses() {
		List<CompositionClass> allClasses = new ArrayList<CompositionClass>();

		for (AttributedElementClass superGraphClass : getAllSuperClasses()) {
			allClasses.addAll(((GraphClass) superGraphClass)
					.getOwnCompositionClasses());
		}

		allClasses.addAll(getOwnCompositionClasses());

		return allClasses;
	}

	@Override
	public List<AggregationClass> getOwnAggregationClasses() {
		List<AggregationClass> list = new ArrayList<AggregationClass>(
				aggregationClasses.values());
		for (AggregationClass cc : getOwnCompositionClasses()) {
			list.add(cc);
		}
		return list;
	}

	@Override
	public List<AggregationClass> getAggregationClasses() {
		List<AggregationClass> allClasses = new ArrayList<AggregationClass>();

		for (AttributedElementClass superGraphClass : getAllSuperClasses()) {
			allClasses.addAll(((GraphClass) superGraphClass)
					.getOwnAggregationClasses());
		}

		allClasses.addAll(getOwnAggregationClasses());

		return allClasses;
	}

	@Override
	public List<VertexClass> getOwnVertexClasses() {
		return new ArrayList<VertexClass>(vertexClasses.values());
	}

	@Override
	public List<VertexClass> getVertexClasses() {
		List<VertexClass> allClasses = new ArrayList<VertexClass>();

		for (AttributedElementClass superGraphClass : getAllSuperClasses()) {
			allClasses.addAll(((GraphClass) superGraphClass)
					.getOwnVertexClasses());
		}

		allClasses.addAll(vertexClasses.values());

		return allClasses;
	}

	@Override
	public int getOwnEdgeClassCount() {
		return edgeClasses.size();
	}

	@Override
	public int getOwnVertexClassCount() {
		return vertexClasses.size();
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
		ec = aggregationClasses.get(qn);
		if (ec != null) {
			return ec;
		}
		ec = compositionClasses.get(qn);
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
	public CompositionClass getCompositionClass(String qn) {
		CompositionClass cc = compositionClasses.get(qn);
		if (cc != null) {
			return cc;
		}
		for (AttributedElementClass superclass : directSuperClasses) {
			cc = ((GraphClass) superclass).getCompositionClass(qn);
			if (cc != null) {
				return cc;
			}
		}
		return null;
	}

	@Override
	public AggregationClass getAggregationClass(String qn) {
		AggregationClass ac = aggregationClasses.get(qn);
		if (ac != null) {
			return ac;
		}
		ac = compositionClasses.get(qn);
		if (ac != null) {
			return ac;
		}
		for (AttributedElementClass superclass : directSuperClasses) {
			ac = ((GraphClass) superclass).getAggregationClass(qn);
			if (ac != null) {
				return ac;
			}
		}
		return null;
	}

}
