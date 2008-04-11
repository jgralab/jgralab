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

package de.uni_koblenz.jgralab.schema.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.DirectedEdgeClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.QualifiedName;
import de.uni_koblenz.jgralab.schema.SchemaException;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class EdgeClassImpl extends GraphElementClassImpl implements EdgeClass {

	private VertexClass from, to;
	private int fromMin, fromMax, toMin, toMax;
	private String fromRolename, toRolename;

	/**
	 * Holds the set of rolenames that are redefined by the fromRolename
	 */
	private Set<String> redefinedFromRoles;

	/**
	 * Holds the set of rolenames that are redefined by the toRolename
	 */
	private Set<String> redefinedToRoles;

	private DirectedEdgeClass inEdgeClass;

	private DirectedEdgeClass outEdgeClass;

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
	public EdgeClassImpl(QualifiedName qn, GraphClass aGraphClass,
			VertexClass from, int fromMin, int fromMax, String fromRoleName,
			VertexClass to, int toMin, int toMax, String toRoleName) {
		super(qn, aGraphClass);
		this.from = from;
		this.to = to;
		this.fromMin = fromMin;
		this.fromMax = fromMax;
		this.toMin = toMin;
		this.toMax = toMax;
		this.fromRolename = fromRoleName;
		this.toRolename = toRoleName;
		redefinedFromRoles = new HashSet<String>();
		redefinedToRoles = new HashSet<String>();
		inEdgeClass = new DirectedEdgeClass(this, EdgeDirection.IN);
		outEdgeClass = new DirectedEdgeClass(this, EdgeDirection.OUT);
	}


	@Override
	public String getVariableName() {
		return "ec_" + getQualifiedName().replace('.', '_');
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.EdgeClass#addSuperClass(jgralab.EdgeClass)
	 */
	public void addSuperClass(EdgeClass superClass) {
		super.addSuperClass(superClass);
		mergeConnectionCardinalities();
		mergeConnectionVertexClasses();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.EdgeClass#getFrom()
	 */
	public VertexClass getFrom() {
		return from;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.EdgeClass#getFromMax()
	 */
	public int getFromMax() {
		return fromMax;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.EdgeClass#getFromMin()
	 */
	public int getFromMin() {
		return fromMin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.EdgeClass#getFromRolename()
	 */
	public String getFromRolename() {
		return fromRolename;
	}

	/*
	 * @see jgralab.EdgeClass#getRedefinesFromRoles()
	 */
	public Set<String> getRedefinedFromRoles() {
		return redefinedFromRoles;
	}

	public void redefineFromRole(String redefinedRoleName) {
		redefinedFromRoles.add(redefinedRoleName);
	}

	public void redefineFromRole(Set<String> redefinedRoleNames) {
		if (redefinedRoleNames != null)
			redefinedFromRoles.addAll(redefinedRoleNames);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.EdgeClass#getTo()
	 */
	public VertexClass getTo() {
		return to;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.EdgeClass#getToMax()
	 */
	public int getToMax() {
		return toMax;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.EdgeClass#getToMin()
	 */
	public int getToMin() {
		return toMin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.EdgeClass#getToRolename()
	 */
	public String getToRolename() {
		return toRolename;
	}

	/*
	 * @see jgralab.EdgeClass#getRedefinesToRoles()
	 */
	public Set<String> getRedefinedToRoles() {
		return redefinedToRoles;
	}

	public void redefineToRole(String redefinedRoleName) {
		redefinedToRoles.add(redefinedRoleName);
	}

	public void redefineToRole(Set<String> redefinedRoleNames) {
		if (redefinedRoleNames != null)
			redefinedToRoles.addAll(redefinedRoleNames);
	}

	public boolean checkConnectionRestrictions() {
		Iterator<? extends AttributedElementClass> iter = directSuperClasses
				.iterator();
		while (iter.hasNext()) {
			EdgeClass ec = (EdgeClass) iter.next();
			if (to != ec.getTo() && !to.isSubClassOf(ec.getTo()))
				return false;
			if (toMin < ec.getToMin() || toMin > ec.getToMax())
				return false;
			if (toMax > ec.getToMax() || toMax < ec.getToMin())
				return false;
			if (from != ec.getFrom() && !from.isSubClassOf(ec.getFrom()))
				return false;
			if (fromMin < ec.getFromMin() || fromMin > ec.getFromMax())
				return false;
			if (fromMax > ec.getFromMax() || fromMax < ec.getFromMin())
				return false;
		}
		return true;
	}

	public boolean mergeConnectionCardinalities() {
		int newMinTo = toMin;
		int newMaxTo = toMax;
		int newMinFrom = fromMin;
		int newMaxFrom = fromMax;

		Iterator<? extends AttributedElementClass> iter = directSuperClasses
				.iterator();
		while (iter.hasNext()) {
			EdgeClass ec = (EdgeClass) iter.next();
			if (newMinTo > ec.getToMax())
				throw new SchemaException(
						"Cardinalities for To-connection of EdgeClass "
								+ this.getName()
								+ " cannot be merged, minimal cardinality "
								+ newMinTo
								+ " is bigger than maximal cardinality "
								+ ec.getToMax() + " of inherited EdgeClass "
								+ ec.getQualifiedName());
			if (newMinTo < ec.getToMin())
				newMinTo = ec.getToMin();
			if (newMinFrom > ec.getFromMax())
				throw new SchemaException(
						"Cardinalities for From-connection of EdgeClass "
								+ this.getName()
								+ " cannot be merged, minimal cardinality "
								+ newMinFrom
								+ " is bigger than maximal cardinality "
								+ ec.getFromMax() + " of inherited EdgeClass "
								+ ec.getQualifiedName());
			if (newMinFrom < ec.getFromMin())
				newMinFrom = ec.getFromMin();

			if (newMaxTo < ec.getToMin())
				throw new SchemaException(
						"Cardinalities for To-connection of EdgeClass "
								+ this.getName()
								+ " cannot be merged, maximal cardinality "
								+ newMaxTo
								+ " is lesser than minimal cardinality "
								+ ec.getToMin() + " of inherited EdgeClass "
								+ ec.getQualifiedName());
			if (newMaxTo > ec.getToMax())
				newMaxTo = ec.getToMax();
			if (newMaxFrom < ec.getFromMin())
				throw new SchemaException(
						"Cardinalities for From-connection of EdgeClass "
								+ this.getName()
								+ " cannot be merged, maximal cardinality "
								+ newMaxFrom
								+ " is lesser than minimal cardinality "
								+ ec.getFromMin() + " of inherited EdgeClass "
								+ ec.getQualifiedName());
			if (newMaxFrom > ec.getFromMax())
				newMaxFrom = ec.getFromMax();
		}
		if ((fromMin != newMinFrom) || (fromMax != newMaxFrom)
				|| (toMin != newMinTo) || (toMax != newMaxTo)) {
			fromMin = newMinFrom;
			fromMax = newMaxFrom;
			toMin = newMinTo;
			toMax = newMaxTo;
			return true;
		} else
			return false;
	}

	public boolean mergeConnectionVertexClasses() {
		Iterator<? extends AttributedElementClass> iter = directSuperClasses
				.iterator();
		VertexClass mostSpecialTo = getTo();
		// System.out.println("To vertex class is: " + mostSpecialTo.getName());
		// System.out.println("Superclasses are");
		// for (jgralab.AttributedElementClass ac :
		// mostSpecialTo.getAllSuperClasses())
		// System.out.println(" " + ac.getName());
		VertexClass mostSpecialFrom = getFrom();
		while (iter.hasNext()) {
			EdgeClass ec = (EdgeClass) iter.next();
			if ((ec.getTo() != mostSpecialTo)
					&& (!mostSpecialTo.isSubClassOf(ec.getTo()))) {
				// System.out.println("MostSpecialTo: " +
				// mostSpecialTo.getName() + " is subclass of: " +
				// ec.getTo().getName() + " : " +
				// mostSpecialTo.isSubClassOf(ec.getTo()) );

				if (ec.getTo().isSubClassOf(mostSpecialTo))
					mostSpecialTo = ec.getTo();
				else
					throw new SchemaException(
							"Cannot merge ToVertexClasses for EdgeClass "
									+ this.getName() + " VertexClass "
									+ mostSpecialTo.getQualifiedName() + " and "
									+ ec.getTo().getQualifiedName()
									+ " cannot be merged");
			}
			if ((ec.getFrom() != mostSpecialFrom)
					&& (!mostSpecialFrom.isSubClassOf(ec.getFrom()))) {
				if (ec.getFrom().isSubClassOf(mostSpecialFrom))
					mostSpecialFrom = ec.getFrom();
				else
					throw new SchemaException(
							"Cannot merge FromVertexClasses for EdgeClass "
									+ this.getName() + " VertexClass "
									+ mostSpecialFrom.getQualifiedName() + " and "
									+ ec.getFrom().getQualifiedName()
									+ " cannot be merged");
			}
		}
		if ((mostSpecialTo != getTo()) || (mostSpecialFrom != getFrom())) {
			to = mostSpecialTo;
			from = mostSpecialFrom;
			return true;
		}
		return false;
	}

	public DirectedEdgeClass getInEdgeClass() {
		return inEdgeClass;
	}

	public DirectedEdgeClass getOutEdgeClass() {
		return outEdgeClass;
	}

	@SuppressWarnings("unchecked")
	public Class<? extends Edge> getM1Class() {
		return (Class<? extends Edge>) super.getM1Class();
	}
	
}
