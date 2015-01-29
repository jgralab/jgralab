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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.IncidenceClass;
import de.uni_koblenz.jgralab.schema.IncidenceDirection;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;

public class IncidenceClassImpl implements IncidenceClass {

	protected IncidenceClassImpl(EdgeClass edgeClass, VertexClass vertexClass,
			String rolename, int minEdgesAtVertex, int maxEdgesAtVertex,
			IncidenceDirection direction, AggregationKind aggregationKind) {
		super();
		if (aggregationKind == null) {
			this.aggregationKind = AggregationKind.NONE;
		} else {
			this.aggregationKind = aggregationKind;
		}
		this.edgeClass = edgeClass;
		this.maxEdgesAtVertex = maxEdgesAtVertex;
		this.minEdgesAtVertex = minEdgesAtVertex;
		if (rolename == null) {
			this.rolename = "";
		} else {
			this.rolename = rolename;
		}
		this.direction = direction;
		this.vertexClass = vertexClass;
		this.subsettedIncidenceClasses = new HashSet<>();
		this.incidenceClassIdInSchema = ((SchemaImpl) edgeClass.getSchema())
				.getNextIncidenceClassId();
	}

	private AggregationKind aggregationKind;

	private final EdgeClass edgeClass;

	private final VertexClass vertexClass;

	private int maxEdgesAtVertex;

	private int minEdgesAtVertex;

	private String rolename;

	private final IncidenceDirection direction;

	private final Set<IncidenceClass> subsettedIncidenceClasses;

	private Set<IncidenceClass> allSubsettedIncidenceClasses;

	private final int incidenceClassIdInSchema;

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

	private void checkAllIncidenceClassSpecializations() {
		// check above
		for (IncidenceClass sup : getOwnSubsettedIncidenceClasses()) {
			checkIncidenceClassSpecialization(sup);
		}
		// check below
		for (EdgeClass subEC : getEdgeClass().getAllSubClasses()) {
			IncidenceClassImpl subsettingIC = (IncidenceClassImpl) (direction == IncidenceDirection.OUT ? subEC
					.getFrom() : subEC.getTo());
			for (IncidenceClass sup : subsettingIC
					.getOwnSubsettedIncidenceClasses()) {
				subsettingIC.checkIncidenceClassSpecialization(sup);
			}
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
		checkAllIncidenceClassSpecializations();
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
	public String getRolename() {
		return rolename;
	}

	@Override
	public Set<IncidenceClass> getOwnSubsettedIncidenceClasses() {
		return subsettedIncidenceClasses;
	}

	@Override
	public Set<IncidenceClass> getSubsettedIncidenceClasses() {
		if (((VertexClassImpl) vertexClass).isFinished()) {
			return this.allSubsettedIncidenceClasses;
		}
		Set<IncidenceClass> result = new HashSet<>();
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

	public void addSubsettedIncidenceClass(IncidenceClass superIC) {
		if (((VertexClassImpl) vertexClass).isFinished()) {
			throw new SchemaException("No changes to finished schema!");
		}
		checkIncidenceClassSpecialization(superIC);
		if (superIC.getSubsettedIncidenceClasses().contains(this)) {
			throw new SchemaException(
					"Subsetting of IncidenceClasses need to be acyclic");
		}
		subsettedIncidenceClasses.add(superIC);
	}

	void removeSubsettedIncidenceClass(IncidenceClass other) {
		if (((VertexClassImpl) vertexClass).isFinished()) {
			throw new SchemaException("No changes to finished schema!");
		}
		subsettedIncidenceClasses.remove(other);
	}

	@Override
	public Set<String> getAllRoles() {
		Set<String> result = new HashSet<>();
		result.add(getRolename());
		for (IncidenceClass ic : getSubsettedIncidenceClasses()) {
			result.add(ic.getRolename());
		}
		return result;
	}

	void finish() {
		this.allSubsettedIncidenceClasses = new HashSet<>();
		this.allSubsettedIncidenceClasses.addAll(subsettedIncidenceClasses);
		for (IncidenceClass ic : subsettedIncidenceClasses) {
			this.allSubsettedIncidenceClasses.addAll(ic
					.getSubsettedIncidenceClasses());
		}

		this.allSubsettedIncidenceClasses = Collections
				.unmodifiableSet(this.allSubsettedIncidenceClasses);
	}

	@Override
	public int getIncidenceClassIdInSchema() {
		return incidenceClassIdInSchema;
	}

	void reopen() {
		allSubsettedIncidenceClasses = null;
	}

	@Override
	public void setMax(int max) {
		maxEdgesAtVertex = max;
		checkAllIncidenceClassSpecializations();
	}

	@Override
	public void setMin(int min) {
		minEdgesAtVertex = min;
		checkAllIncidenceClassSpecializations();
	}

	@Override
	public void setRolename(String name) {
		rolename = name;
		checkAllIncidenceClassSpecializations();
	}

	/**
	 * checks if the incidence classes own and inherited are compatible, i.e. if
	 * the upper multiplicity of own is lower or equal than the one of inherited
	 * and so on
	 * 
	 * @param special
	 * @param general
	 * @throws SchemaException
	 *             upon illegal combinations
	 */
	void checkIncidenceClassSpecialization(IncidenceClass general) {
		// direction
		if (!(direction == general.getDirection())) {
			String dir = getDirection() == IncidenceDirection.OUT ? "Alpha"
					: "Omega";
			throw new SchemaException(
					"An IncidenceClass may specialize only IncidenceClasses whose direction is the same. "
							+ "Offending EdgeClasses are "
							+ getEdgeClass().getQualifiedName()
							+ " which wants to specialize "
							+ general.getEdgeClass().getQualifiedName()
							+ " at end "
							+ dir
							+ ". Connected VertexClass of special IncidenceClass ist "
							+ getVertexClass().getQualifiedName()
							+ " and of general VertexClass is "
							+ general.getVertexClass().getQualifiedName() + ".");
		}
		// Vertex same
		if (!(general.getVertexClass().equals(getVertexClass()) || general
				.getVertexClass().isSuperClassOf(getVertexClass()))) {
			String dir = getDirection() == IncidenceDirection.OUT ? "Alpha"
					: "Omega";
			throw new SchemaException(
					"An IncidenceClass may specialize only IncidenceClasses whose connected vertex class "
							+ "is identical or a superclass of the own one. Offending EdgeClasses are "
							+ getEdgeClass().getQualifiedName()
							+ " which wants to specialize "
							+ general.getEdgeClass().getQualifiedName()
							+ " at end "
							+ dir
							+ ". Connected VertexClass of special IncidenceClass ist "
							+ getVertexClass().getQualifiedName()
							+ " and of general VertexClass is "
							+ general.getVertexClass().getQualifiedName() + ".");
		}
		// Multiplicities
		if (getMax() > general.getMax()) {
			String dir = getDirection() == IncidenceDirection.OUT ? "Alpha"
					: "Omega";
			throw new SchemaException(
					"The multiplicity of an edge class may not be larger than "
							+ "the multiplicities of its superclass. Offending EdgeClasses are "
							+ getEdgeClass().getQualifiedName() + " and "
							+ general.getEdgeClass().getQualifiedName()
							+ " at end " + dir);
		}

		// Aggregation kind must equal the super-IC's kind
		if (!general.getEdgeClass().isDefaultGraphElementClass()
				&& getAggregationKind() != general.getAggregationKind()) {
			String dir = getDirection() == IncidenceDirection.OUT ? "Alpha"
					: "Omega";
			throw new SchemaException(
					"The aggregation kind of an incidence class must equal the one of its subsetted class. "
							+ "Offending EdgeClasses are "
							+ getEdgeClass().getQualifiedName()
							+ " and "
							+ general.getEdgeClass().getQualifiedName()
							+ " at end " + dir);
		}

		// name clashes
		if (general.getRolename().equals(getRolename())
				&& !general.getRolename().isEmpty() && !getRolename().isEmpty()) {
			String dir = getDirection() == IncidenceDirection.OUT ? "Alpha"
					: "Omega";
			throw new SchemaException(
					"An IncidenceClass may only subset an IncidenceClass with a different name. Offending"
							+ "EdgeClasses are "
							+ getEdgeClass().getQualifiedName()
							+ " and "
							+ general.getEdgeClass().getQualifiedName()
							+ " at end " + dir);
		}
		for (IncidenceClass ic : general.getSubsettedIncidenceClasses()) {
			if (ic.getRolename().equals(getRolename())
					&& !general.getRolename().isEmpty()
					&& !ic.getRolename().isEmpty()) {
				String dir = ic.getDirection() == IncidenceDirection.OUT ? "Alpha"
						: "Omega";
				throw new SchemaException(
						"An IncidenceClass may only subset an IncidenceClass with a different name. Offending"
								+ "EdgeClasses are "
								+ getEdgeClass().getQualifiedName()
								+ " and "
								+ ic.getEdgeClass().getQualifiedName()
								+ " at end " + dir);
			}
		}
	}
}
