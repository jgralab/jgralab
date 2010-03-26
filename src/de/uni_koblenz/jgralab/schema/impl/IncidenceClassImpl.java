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
