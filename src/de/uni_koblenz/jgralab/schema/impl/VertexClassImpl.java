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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.IncidenceClass;
import de.uni_koblenz.jgralab.schema.IncidenceDirection;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;

public class VertexClassImpl extends GraphElementClassImpl<VertexClass, Vertex>
		implements VertexClass {
	/**
	 * the own in IncidenceClasses
	 */
	private Set<IncidenceClass> inIncidenceClasses = new HashSet<IncidenceClass>();

	/**
	 * the in IncidenceClasses - only set if schema is finish
	 */
	private Set<IncidenceClass> allInIncidenceClasses;

	/**
	 * the own out IncidenceClasses
	 */
	private Set<IncidenceClass> outIncidenceClasses = new HashSet<IncidenceClass>();

	/**
	 * the out IncidenceClasses - only set if schema is finish
	 */
	private Set<IncidenceClass> allOutIncidenceClasses;

	/**
	 * the valid from far IncidenceClasses - only set if schema is finished
	 */
	private Set<IncidenceClass> validFromFarIncidenceClasses;

	/**
	 * the valid from EdgeClasses - only set if schema is finished
	 */
	private Set<EdgeClass> validFromEdgeClasses;

	/**
	 * the valid to EdgeClasses - only set if schema is finished
	 */
	private Set<EdgeClass> validToEdgeClasses;

	/**
	 * the valid to far IncidenceClasses - only set if schema is finished
	 */
	private Set<IncidenceClass> validToFarIncidenceClasses;

	/**
	 * A map from far-end role name to the corresponding directed edge class -
	 * only set if schema is finished
	 */
	private Map<String, DirectedSchemaEdgeClass> farRoleNameToEdgeClass;

	/**
	 * builds a new vertex class object
	 */
	protected VertexClassImpl(String simpleName, PackageImpl pkg,
			GraphClassImpl gc) {
		super(simpleName, pkg, gc, gc.vertexClassDag);
		parentPackage.addVertexClass(this);
		graphClass.addVertexClass(this);
	}

	void addInIncidenceClass(IncidenceClass incClass) {
		if (incClass.getVertexClass() != this) {
			throwSchemaException(incClass);
		}
		checkDuplicateRolenames(incClass);
		inIncidenceClasses.add(incClass);
	}

	void addOutIncidenceClass(IncidenceClass incClass) {
		if (incClass.getVertexClass() != this) {
			throwSchemaException(incClass);
		}
		checkDuplicateRolenames(incClass);
		outIncidenceClasses.add(incClass);
	}

	private void checkDuplicateRolenames(IncidenceClass incClass) {
		String rolename = incClass.getOpposite().getRolename();
		if (rolename.isEmpty()) {
			return;
		}
		checkDuplicatedRolenameForACyclicIncidence(incClass);
		checkDuplicatedRolenameForAllIncidences(incClass,
				getAllInIncidenceClasses());
		checkDuplicatedRolenameForAllIncidences(incClass,
				getAllOutIncidenceClasses());
	}

	private void checkDuplicatedRolenameForACyclicIncidence(
			IncidenceClass incClass) {
		String rolename = incClass.getOpposite().getRolename();
		VertexClass oppositeVertexClass = incClass.getOpposite()
				.getVertexClass();
		boolean equalRolenames = incClass.getRolename().equals(rolename);
		boolean identicalClasses = this == oppositeVertexClass;
		if (equalRolenames && identicalClasses) {
			throw new SchemaException(
					"The rolename "
							+ incClass.getRolename()
							+ " may be not used at both ends of the reflexive edge class "
							+ incClass.getEdgeClass().getQualifiedName());
		}
	}

	private void checkDuplicatedRolenameForAllIncidences(
			IncidenceClass incClass, Set<IncidenceClass> incidenceSet) {
		String rolename = incClass.getOpposite().getRolename();
		if (rolename.isEmpty()) {
			return;
		}
		for (IncidenceClass incidence : incidenceSet) {
			if (incidence == incClass) {
				continue;
			}
			if (incidence.getOpposite().getRolename().equals(rolename)) {
				throw new SchemaException("The rolename "
						+ incidence.getOpposite().getRolename()
						+ " is used twice at class " + getQualifiedName()
						+ ". Concerning edge classes are "
						+ incClass.getEdgeClass().getQualifiedName() + " and "
						+ incidence.getEdgeClass().getQualifiedName());
			}
		}
	}

	private void throwSchemaException(IncidenceClass ic) {
		throw new SchemaException(
				"Try to add IncidenceClass ending at '"
						+ ic.getVertexClass().getQualifiedName()
						+ "' to VertexClass '"
						+ getQualifiedName()
						+ "'.IncidenceClasses may be added only to VertexClasses they are connected to.");
	}

	@Override
	public void addSuperClass(VertexClass superClass) {
		assertNotFinished();
		if (superClass == this) {
			return;
		}
		checkDuplicateRolenames(superClass);
		super.addSuperClass(superClass);
	}

	private void checkDuplicateRolenames(VertexClass superClass) {
		checkDuplicatedRolenamesAgainstAllIncidences(superClass
				.getAllInIncidenceClasses());
		checkDuplicatedRolenamesAgainstAllIncidences(superClass
				.getAllOutIncidenceClasses());
	}

	private void checkDuplicatedRolenamesAgainstAllIncidences(
			Set<IncidenceClass> incidences) {
		for (IncidenceClass incidence : incidences) {
			checkDuplicateRolenames(incidence);
		}
	}

	/**
	 * For a vertexclass A are all edgeclasses valid froms, which (1) run from A
	 * to a B or (2) run from a superclass of A to a B.
	 */
	@Override
	public Set<IncidenceClass> getValidFromFarIncidenceClasses() {
		if (isFinished()) {
			return validFromFarIncidenceClasses;
		}

		Set<IncidenceClass> validFromInc = new HashSet<IncidenceClass>();
		for (IncidenceClass ic : getAllOutIncidenceClasses()) {
			IncidenceClass farInc = ic.getEdgeClass().getTo();
			validFromInc.add(farInc);
		}
		for (VertexClass vc : getAllSuperClasses()) {
			for (IncidenceClass ic : vc.getAllOutIncidenceClasses()) {
				IncidenceClass farInc = ic.getEdgeClass().getTo();
				validFromInc.add(farInc);
			}
		}

		return validFromInc;
	}

	@Override
	public Set<IncidenceClass> getValidToFarIncidenceClasses() {
		if (isFinished()) {
			return validToFarIncidenceClasses;
		}
		Set<IncidenceClass> validToInc = new HashSet<IncidenceClass>();
		for (IncidenceClass ic : getAllInIncidenceClasses()) {
			IncidenceClass farInc = ic.getEdgeClass().getFrom();
			validToInc.add(farInc);
		}
		for (VertexClass vc : getAllSuperClasses()) {
			for (IncidenceClass ic : vc.getAllInIncidenceClasses()) {
				IncidenceClass farInc = ic.getEdgeClass().getFrom();
				validToInc.add(farInc);
			}
		}

		return validToInc;
	}

	@Override
	public Set<EdgeClass> getValidFromEdgeClasses() {
		if (isFinished()) {
			return validFromEdgeClasses;
		}
		// System.err.print("+");
		Set<EdgeClass> validFrom = new HashSet<EdgeClass>();
		for (IncidenceClass ic : getValidFromFarIncidenceClasses()) {
			if (!ic.getEdgeClass().isDefaultGraphElementClass()) {
				validFrom.add(ic.getEdgeClass());
			}
		}
		return validFrom;
	}

	@Override
	public Set<EdgeClass> getValidToEdgeClasses() {
		if (isFinished()) {
			return validToEdgeClasses;
		}
		// System.err.print("-");
		Set<EdgeClass> validTo = new HashSet<EdgeClass>();
		for (IncidenceClass ic : getValidToFarIncidenceClasses()) {
			if (!ic.getEdgeClass().isDefaultGraphElementClass()) {
				validTo.add(ic.getEdgeClass());
			}
		}
		return validTo;
	}

	public Set<IncidenceClass> getOwnInIncidenceClasses() {
		return inIncidenceClasses;
	}

	public Set<IncidenceClass> getOwnOutIncidenceClasses() {
		return outIncidenceClasses;
	}

	@Override
	public Set<IncidenceClass> getAllInIncidenceClasses() {
		if (isFinished()) {
			return allInIncidenceClasses;
		}
		Set<IncidenceClass> incidenceClasses = new HashSet<IncidenceClass>();
		incidenceClasses.addAll(inIncidenceClasses);
		for (VertexClass vc : getDirectSuperClasses()) {
			incidenceClasses.addAll(vc.getAllInIncidenceClasses());
		}
		return incidenceClasses;
	}

	@Override
	public Set<IncidenceClass> getAllOutIncidenceClasses() {
		if (isFinished()) {
			return allOutIncidenceClasses;
		}
		Set<IncidenceClass> incidenceClasses = new HashSet<IncidenceClass>();
		incidenceClasses.addAll(outIncidenceClasses);
		for (VertexClass vc : getDirectSuperClasses()) {
			incidenceClasses.addAll(vc.getAllOutIncidenceClasses());
		}
		return incidenceClasses;
	}

	@Override
	public Set<IncidenceClass> getOwnAndInheritedFarIncidenceClasses() {
		Set<IncidenceClass> result = new HashSet<IncidenceClass>();
		for (IncidenceClass ic : getAllInIncidenceClasses()) {
			result.add(ic.getEdgeClass().getFrom());
			for (IncidenceClass sup : ic.getSubsettedIncidenceClasses()) {
				result.add(sup.getEdgeClass().getFrom());
			}
		}
		for (IncidenceClass ic : getAllOutIncidenceClasses()) {
			result.add(ic.getEdgeClass().getTo());
			for (IncidenceClass sup : ic.getSubsettedIncidenceClasses()) {
				result.add(sup.getEdgeClass().getTo());
			}
		}
		return result;
	}

	@Override
	public Set<EdgeClass> getConnectedEdgeClasses() {
		Set<EdgeClass> result = new HashSet<EdgeClass>();
		for (IncidenceClass ic : getAllInIncidenceClasses()) {
			result.add(ic.getEdgeClass());
		}
		for (IncidenceClass ic : getAllOutIncidenceClasses()) {
			result.add(ic.getEdgeClass());
		}
		return result;
	}

	@Override
	public Set<EdgeClass> getOwnConnectedEdgeClasses() {
		Set<EdgeClass> result = new HashSet<EdgeClass>();
		for (IncidenceClass ic : getOwnInIncidenceClasses()) {
			result.add(ic.getEdgeClass());
		}
		for (IncidenceClass ic : getOwnOutIncidenceClasses()) {
			result.add(ic.getEdgeClass());
		}
		return result;
	}

	@Override
	protected void finish() {
		allInIncidenceClasses = new HashSet<IncidenceClass>();
		allInIncidenceClasses.addAll(inIncidenceClasses);

		allOutIncidenceClasses = new HashSet<IncidenceClass>();
		allOutIncidenceClasses.addAll(outIncidenceClasses);

		for (VertexClass vc : getDirectSuperClasses()) {
			allInIncidenceClasses.addAll(vc.getAllInIncidenceClasses());
			allOutIncidenceClasses.addAll(vc.getAllOutIncidenceClasses());
		}

		allInIncidenceClasses = Collections
				.unmodifiableSet(allInIncidenceClasses);
		allOutIncidenceClasses = Collections
				.unmodifiableSet(allOutIncidenceClasses);

		validFromFarIncidenceClasses = Collections
				.unmodifiableSet(getValidFromFarIncidenceClasses());
		validToFarIncidenceClasses = Collections
				.unmodifiableSet(getValidToFarIncidenceClasses());

		validFromEdgeClasses = Collections
				.unmodifiableSet(getValidFromEdgeClasses());
		validToEdgeClasses = Collections
				.unmodifiableSet(getValidToEdgeClasses());

		farRoleNameToEdgeClass = new HashMap<String, DirectedSchemaEdgeClass>();
		for (IncidenceClass ic : getOwnAndInheritedFarIncidenceClasses()) {
			String role = ic.getRolename();
			if (role.length() == 0) {
				continue;
			}
			farRoleNameToEdgeClass.put(role,
					getDirectedEdgeClassForFarEndRole(role));
		}
		farRoleNameToEdgeClass = Collections
				.unmodifiableMap(farRoleNameToEdgeClass);

		inIncidenceClasses = Collections.unmodifiableSet(inIncidenceClasses);
		outIncidenceClasses = Collections.unmodifiableSet(outIncidenceClasses);

		for (IncidenceClass ic : inIncidenceClasses) {
			((IncidenceClassImpl) ic).finish();
		}
		for (IncidenceClass ic : outIncidenceClasses) {
			((IncidenceClassImpl) ic).finish();
		}
		super.finish();
	}

	@Override
	public boolean isValidFromFor(EdgeClass ec) {
		if (ec.equals(this.graphClass.getTemporaryEdgeClass())) {
			return true;
		}
		return getValidFromEdgeClasses().contains(ec);
	}

	@Override
	public boolean isValidToFor(EdgeClass ec) {
		if (ec.equals(this.graphClass.getTemporaryEdgeClass())) {
			return true;
		}
		return getValidToEdgeClasses().contains(ec);
	}

	@Override
	public DirectedSchemaEdgeClass getDirectedEdgeClassForFarEndRole(
			String roleName) {
		if (isFinished()) {
			return farRoleNameToEdgeClass.get(roleName);
		}
		for (IncidenceClass ic : getOwnAndInheritedFarIncidenceClasses()) {
			if (roleName.equals(ic.getRolename())) {
				EdgeClass ec = ic.getEdgeClass();
				return new DirectedSchemaEdgeClass(
						ec,
						(ic.getDirection() == IncidenceDirection.IN ? EdgeDirection.OUT
								: EdgeDirection.IN));
			}
		}
		return null;
	}

	@Override
	protected void reopen() {
		allInIncidenceClasses = null;
		allOutIncidenceClasses = null;
		validFromFarIncidenceClasses = null;
		validToFarIncidenceClasses = null;
		validFromEdgeClasses = null;
		validToEdgeClasses = null;
		farRoleNameToEdgeClass = null;
		for (IncidenceClass ic : inIncidenceClasses) {
			((IncidenceClassImpl) ic).reopen();
		}
		for (IncidenceClass ic : outIncidenceClasses) {
			((IncidenceClassImpl) ic).reopen();
		}

		// Make em modifiable again
		inIncidenceClasses = new HashSet<IncidenceClass>(inIncidenceClasses);
		outIncidenceClasses = new HashSet<IncidenceClass>(outIncidenceClasses);

		super.reopen();
	}

	@Override
	protected final void register() {
		super.register();
		graphClass.vertexClasses.put(qualifiedName, this);
		parentPackage.vertexClasses.put(simpleName, this);
	}

	@Override
	protected final void unregister() {
		super.unregister();
		graphClass.vertexClasses.remove(qualifiedName);
		parentPackage.vertexClasses.remove(simpleName);
	}

	@Override
	public void delete() {
		schema.assertNotFinished();
		if (this == graphClass.getDefaultVertexClass()) {
			throw new SchemaException(
					"The default vertex class cannot be deleted.");
		}
		if (!getConnectedEdgeClasses().isEmpty()) {
			throw new SchemaException("Cannot delete vertex class "
					+ qualifiedName
					+ " because there are still connected edge classes: "
					+ getConnectedEdgeClasses());
		}
		super.delete();
		graphClass.vertexClasses.remove(qualifiedName);
		graphClass.vertexClassDag.delete(this);
		parentPackage.vertexClasses.remove(simpleName);
	}

	/**
	 * Called when an edge class connected to this vertex class is deleted
	 * 
	 * @param ic
	 */
	void unlink(IncidenceClass ic) {
		schema.assertNotFinished();
		outIncidenceClasses.remove(ic);
		inIncidenceClasses.remove(ic);
	}

	@Override
	protected VertexClass getDefaultClass() {
		return graphClass.getDefaultVertexClass();
	}

	@Override
	public boolean isDefaultGraphElementClass() {
		return this == graphClass.getDefaultVertexClass();
	}
}
