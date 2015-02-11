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

package de.uni_koblenz.jgralab.schema.impl;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.IncidenceClass;
import de.uni_koblenz.jgralab.schema.IncidenceDirection;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;

public class EdgeClassImpl extends GraphElementClassImpl<EdgeClass, Edge>
		implements EdgeClass {

	private final IncidenceClass from;
	private final IncidenceClass to;

	/**
	 * builds a new edge class
	 * 
	 * @param qn
	 *            the unique identifier of the edge class in the schema
	 * @param from
	 *            the vertex class from which the edge class may connect from
	 * @param fromMin
	 *            the minimum multiplicity of the 'from' vertex class,
	 *            represents the minimum allowed number of connections from the
	 *            edge class to the 'from' vertex class
	 * @param fromMax
	 *            the maximum multiplicity of the 'from' vertex class,
	 *            represents the maximum allowed number of connections from the
	 *            edge class to the 'from' vertex class
	 * @param fromRoleName
	 *            a name which identifies the 'from' side of the edge class in a
	 *            unique way
	 * @param to
	 *            the vertex class to which the edge class may connect to
	 * @param toMin
	 *            the minimum multiplicity of the 'to' vertex class, represents
	 *            the minimum allowed number of connections from the edge class
	 *            to the 'to' vertex class
	 * @param toMax
	 *            the minimum multiplicity of the 'to' vertex class, represents
	 *            the maximum allowed number of connections from the edge class
	 *            to the 'to' vertex class
	 * @param toRoleName
	 *            a name which identifies the 'to' side of the edge class in a
	 *            unique way
	 */
	protected EdgeClassImpl(String simpleName, PackageImpl pkg,
			GraphClassImpl gc, VertexClass from, int fromMin, int fromMax,
			String fromRoleName, AggregationKind aggrFrom, VertexClass to,
			int toMin, int toMax, String toRoleName, AggregationKind aggrTo,
			ClassLoader schemaClassLoader) {
		super(simpleName, pkg, gc, gc.edgeClassDag, schemaClassLoader);

		if (pkg.isDefaultPackage() && simpleName.equals(DEFAULTEDGECLASS_NAME)) {
			// the default EC is just created
		} else if (pkg.isDefaultPackage()
				&& simpleName.equals(TEMPORARYEDGECLASS_NAME)) {
			// the temporary EC is just created
		} else {
			if ((from == graphClass.getDefaultVertexClass())
					|| (to == graphClass.getDefaultVertexClass())) {
				throw new SchemaException(
						"EdgeClasses from/to the default vertex class are forbidden!\n "
								+ "Tried to create edge class " + simpleName
								+ ": " + to.getQualifiedName() + " -> "
								+ to.getQualifiedName());
			}
		}

		IncidenceClass fromInc = new IncidenceClassImpl(this, from,
				fromRoleName, fromMin, fromMax, IncidenceDirection.OUT,
				aggrFrom);
		IncidenceClass toInc = new IncidenceClassImpl(this, to, toRoleName,
				toMin, toMax, IncidenceDirection.IN, aggrTo);
		this.from = fromInc;
		this.to = toInc;
		((VertexClassImpl) from).addOutIncidenceClass(fromInc);
		((VertexClassImpl) to).addInIncidenceClass(toInc);
		parentPackage.addEdgeClass(this);
		graphClass.addEdgeClass(this);
	}

	@Override
	public void addSuperClass(EdgeClass superClass) {
		assertNotFinished();
		if (superClass == this) {
			return;
		}
		super.addSuperClass(superClass);
		((IncidenceClassImpl) getFrom()).addSubsettedIncidenceClass(superClass
				.getFrom());
		((IncidenceClassImpl) getTo()).addSubsettedIncidenceClass(superClass
				.getTo());
	}

	@Override
	public void removeSuperClass(EdgeClass superClass) {
		assertNotFinished();
		if (superClass == this) {
			return;
		}
		super.removeSuperClass(superClass);
		((IncidenceClassImpl) getFrom())
				.removeSubsettedIncidenceClass(superClass.getFrom());
		((IncidenceClassImpl) getTo()).removeSubsettedIncidenceClass(superClass
				.getTo());
	}

	@Override
	public final IncidenceClass getFrom() {
		return from;
	}

	@Override
	public final IncidenceClass getTo() {
		return to;
	}

	@Override
	protected void register() {
		super.register();
		graphClass.edgeClasses.put(qualifiedName, this);
		parentPackage.edgeClasses.put(simpleName, this);
		graphClass.edgeClassDag.finishHashCodeChange();
	}

	@Override
	protected void unregister() {
		super.unregister();
		graphClass.edgeClasses.remove(qualifiedName);
		parentPackage.edgeClasses.remove(simpleName);
		graphClass.edgeClassDag.prepareHashCodeChange(this);
	}

	@Override
	public void delete() {
		if (this == graphClass.getDefaultEdgeClass()) {
			throw new SchemaException(
					"The default edge class cannot be deleted.");
		}

		VertexClassImpl fromVC = (VertexClassImpl) from.getVertexClass();
		VertexClassImpl toVC = (VertexClassImpl) to.getVertexClass();
		fromVC.unlink(from);
		toVC.unlink(to);

		super.delete();

		graphClass.edgeClasses.remove(qualifiedName);
		graphClass.edgeClassDag.delete(this);
		parentPackage.edgeClasses.remove(simpleName);
	}

	@Override
	protected EdgeClass getDefaultClass() {
		return graphClass.getDefaultEdgeClass();
	}

	@Override
	public boolean isDefaultGraphElementClass() {
		return this == graphClass.getDefaultEdgeClass();
	}

}
