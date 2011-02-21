/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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

import java.util.HashSet;
import java.util.Set;

import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.IncidenceClass;
import de.uni_koblenz.jgralab.schema.IncidenceDirection;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;

public class IncidenceClassImpl implements IncidenceClass {

	public IncidenceClassImpl(EdgeClass edgeClass, VertexClass vertexClass,
			String rolename, int minEdgesAtVertex, int maxEdgesAtVertex,
			IncidenceDirection direction, AggregationKind aggregationKind) {
		super();
		this.aggregationKind = aggregationKind;
		this.direction = direction;
		this.edgeClass = edgeClass;
		this.maxEdgesAtVertex = maxEdgesAtVertex;
		this.minEdgesAtVertex = minEdgesAtVertex;
		this.rolename = rolename;
		if (rolename == null) {
			rolename = "";
		}
		this.vertexClass = vertexClass;
		this.subsettedIncidenceClasses = new HashSet<IncidenceClass>();
		this.redefinedIncidenceClasses = new HashSet<IncidenceClass>();
	}

	private AggregationKind aggregationKind;

	private IncidenceDirection direction;

	private EdgeClass edgeClass;

	private VertexClass vertexClass;

	private int maxEdgesAtVertex;

	private int minEdgesAtVertex;

	private String rolename;

	private Set<IncidenceClass> redefinedIncidenceClasses;

	private Set<IncidenceClass> subsettedIncidenceClasses;

	@Override
	public AggregationKind getAggregationKind() {
		return aggregationKind;
	}

	@Override
	public IncidenceClass getOpposite() {
		if (edgeClass.getFrom() == this) {
			return edgeClass.getTo();
		} else {
			return edgeClass.getFrom();
		}
	}

	@Override
	public void setAggregationKind(AggregationKind kind) {
		if ((kind != AggregationKind.NONE)
				&& (getOpposite().getAggregationKind() != AggregationKind.NONE)) {
			throw new SchemaException(
					"At least one end of each EdgeClass must be of AggregationKind NONE at EdgeClass "
							+ edgeClass.getQualifiedName());
		}
		this.aggregationKind = kind;
	}

	@Override
	public IncidenceDirection getDirection() {
		return direction;
	}

	@Override
	public EdgeClass getEdgeClass() {
		return edgeClass;
	}

	@Override
	public int getMax() {
		return maxEdgesAtVertex;
	}

	@Override
	public int getMin() {
		return minEdgesAtVertex;
	}

	@Override
	public Set<IncidenceClass> getRedefinedIncidenceClasses() {
		Set<IncidenceClass> result = new HashSet<IncidenceClass>();
		result.addAll(redefinedIncidenceClasses);
		for (IncidenceClass ic : subsettedIncidenceClasses) {
			result.addAll(ic.getRedefinedIncidenceClasses());
		}
		for (IncidenceClass ic : redefinedIncidenceClasses) {
			result.addAll(ic.getRedefinedIncidenceClasses());
		}
		return result;
	}

	@Override
	public Set<IncidenceClass> getOwnRedefinedIncidenceClasses() {
		return redefinedIncidenceClasses;
	}

	@Override
	public String getRolename() {
		return rolename;
	}

	@Override
	public Set<IncidenceClass> getOwnSubsettedIncidenceClasses() {
		return subsettedIncidenceClasses;
	}

	@Override
	public Set<IncidenceClass> getSubsettedIncidenceClasses() {
		Set<IncidenceClass> result = new HashSet<IncidenceClass>();
		result.addAll(subsettedIncidenceClasses);
		for (IncidenceClass ic : subsettedIncidenceClasses) {
			result.addAll(ic.getSubsettedIncidenceClasses());
		}
		return result;
	}

	@Override
	public VertexClass getVertexClass() {
		return vertexClass;
	}

	public void addRedefinedRole(String rolename) {
		boolean foundRole = false;

		for (IncidenceClass ic : getSubsettedIncidenceClasses()) {
			if (ic.getRolename().equals(rolename)) {
				// found a base incidence class whose rolename matches

				// TODO This check does not cover all illegal cases
				// TODO Daniel's job: give a specification of illegal
				// redefinitions

				// Check if this rolename is redefined by another EdgeClass
				// originating from the same VertexClass
				for (EdgeClass ec : getOpposite().getVertexClass()
						.getOwnConnectedEdgeClasses()) {
					if (ec == edgeClass) {
						// skip the EdgeClass of this IncidenceClass
						continue;
					}
					// determine proper end
					IncidenceClass other = direction == IncidenceDirection.IN ? ec
							.getTo()
							: ec.getFrom();
					if (other.getRedefinedIncidenceClasses().contains(ic)) {
						throw new SchemaException("The role '" + rolename
								+ "' of EdgeClass '"
								+ edgeClass.getQualifiedName()
								+ "' is already redefined in EdgeClass '"
								+ ec.getQualifiedName() + "'");
					}
				}
				redefinedIncidenceClasses.add(ic);
				foundRole = true;
				break;
			}
		}
		if (!foundRole) {
			throw new SchemaException(
					"The role '"
							+ rolename
							+ "' is not defined in any subsetted IncidenceClass, so it cannot be redefined.");
		}
	}

	public void addRedefinedRoles(Set<String> rolenames) {
		if (rolenames == null) {
			return;
		}
		for (String role : rolenames) {
			addRedefinedRole(role);
		}
	}

	public void addSubsettedIncidenceClass(IncidenceClass other) {
		EdgeClassImpl.checkIncidenceClassSpecialization(this, other);
		if (other.getSubsettedIncidenceClasses().contains(this)) {
			throw new SchemaException(
					"Subsetting/Redefinition of IncidenceClasses need to be acyclic");
		}
		subsettedIncidenceClasses.add(other);
	}

	public Set<String> getAllRoles() {
		Set<String> result = new HashSet<String>();
		result.add(getRolename());
		for (IncidenceClass ic : getSubsettedIncidenceClasses()) {
			result.add(ic.getRolename());
		}
		return result;
	}

	public Set<String> getRedefinedRoles() {
		Set<String> result = new HashSet<String>();
		for (IncidenceClass ic : getRedefinedIncidenceClasses()) {
			result.add(ic.getRolename());
		}
		return result;
	}

	// public Set<String> getRedefinedAndOwnRoles() {
	// Set<String> roles = new HashSet<String>(getRedefinedRoles());
	// roles.add(getRolename());
	// return roles;
	// }

}
