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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Package;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;
import de.uni_koblenz.jgralab.schema.exception.InheritanceException;

public class EdgeClassImpl extends GraphElementClassImpl implements EdgeClass {

	private VertexClass from, to;
	private int fromMin, fromMax, toMin, toMax;
	private final String fromRolename, toRolename;

	/**
	 * Holds the set of rolenames that are redefined by the fromRolename
	 */
	private final Set<String> redefinedFromRoles;

	/**
	 * Holds the set of rolenames that are redefined by the toRolename
	 */
	private final Set<String> redefinedToRoles;

	private final DirectedEdgeClass inEdgeClass;

	private final DirectedEdgeClass outEdgeClass;

	static EdgeClass createDefaultEdgeClass(Schema schema) {
		assert schema.getDefaultGraphClass() != null : "DefaultGraphClass has not yet been created!";
		assert schema.getDefaultVertexClass() != null : "DefaultVertexClass has not yet been created!";
		assert schema.getDefaultEdgeClass() == null : "DefaultEdgeClass already created!";
		EdgeClass ec = schema.getDefaultGraphClass().createEdgeClass(
				DEFAULTEDGECLASS_NAME, schema.getDefaultVertexClass(),
				schema.getDefaultVertexClass());
		ec.setAbstract(true);
		return ec;
	}

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
	protected EdgeClassImpl(String simpleName, Package pkg,
			GraphClass aGraphClass, VertexClass from, int fromMin, int fromMax,
			String fromRoleName, VertexClass to, int toMin, int toMax,
			String toRoleName) {
		super(simpleName, pkg, aGraphClass);
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
		register();
	}

	@Override
	protected void register() {
		((PackageImpl) parentPackage).addEdgeClass(this);
		((GraphClassImpl) graphClass).addEdgeClass(this);
	}

	@Override
	public String getVariableName() {
		return "ec_" + getQualifiedName().replace('.', '_');
	}

	@Override
	public void addSuperClass(EdgeClass superClass) {
		if (!superClass.getClass().isInstance(this)) {
			throw new InheritanceException(qualifiedName
					+ " cannot derive from " + superClass.getQualifiedName()
					+ ", because that is more special.");
		}
		super.addSuperClass(superClass);
		mergeConnectionCardinalities();
		mergeConnectionVertexClasses();
	}

	@Override
	public VertexClass getFrom() {
		return from;
	}

	@Override
	public int getFromMax() {
		return fromMax;
	}

	@Override
	public int getFromMin() {
		return fromMin;
	}

	@Override
	public String getFromRolename() {
		return fromRolename;
	}

	@Override
	public Set<String> getRedefinedFromRoles() {
		return redefinedFromRoles;
	}

	@Override
	public void redefineFromRole(String redefinedRoleName) {
		redefinedFromRoles.add(redefinedRoleName);
	}

	@Override
	public void redefineFromRole(Set<String> redefinedRoleNames) {
		if (redefinedRoleNames != null) {
			redefinedFromRoles.addAll(redefinedRoleNames);
		}
	}

	@Override
	public VertexClass getTo() {
		return to;
	}

	@Override
	public int getToMax() {
		return toMax;
	}

	@Override
	public int getToMin() {
		return toMin;
	}

	@Override
	public String getToRolename() {
		return toRolename;
	}

	@Override
	public Set<String> getRedefinedToRoles() {
		return redefinedToRoles;
	}

	@Override
	public void redefineToRole(String redefinedRoleName) {
		redefinedToRoles.add(redefinedRoleName);
	}

	@Override
	public void redefineToRole(Set<String> redefinedRoleNames) {
		if (redefinedRoleNames != null) {
			redefinedToRoles.addAll(redefinedRoleNames);
		}
	}

	/**
	 * @return true, if the connectable VertexClasses and cardinalities of this
	 *         EdgeClass satisfy the restrictions of its superclasses
	 */
	protected boolean checkConnectionRestrictions() {
		Iterator<? extends AttributedElementClass> iter = directSuperClasses
				.iterator();
		while (iter.hasNext()) {
			EdgeClass ec = (EdgeClass) iter.next();
			if (to != ec.getTo() && !to.isSubClassOf(ec.getTo())) {
				return false;
			}
			if (toMin < ec.getToMin() || toMin > ec.getToMax()) {
				return false;
			}
			if (toMax > ec.getToMax() || toMax < ec.getToMin()) {
				return false;
			}
			if (from != ec.getFrom() && !from.isSubClassOf(ec.getFrom())) {
				return false;
			}
			if (fromMin < ec.getFromMin() || fromMin > ec.getFromMax()) {
				return false;
			}
			if (fromMax > ec.getFromMax() || fromMax < ec.getFromMin()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Tries to merge the cardinalities of the edges endpoints
	 *
	 * @return true if a merge was done successfull, false if no merge was
	 *         needed or if a merge is not possible
	 *
	 */
	protected boolean mergeConnectionCardinalities() {
		int newToMin = toMin;
		int newToMax = toMax;
		int newFromMin = fromMin;
		int newFromMax = fromMax;

		Iterator<? extends AttributedElementClass> iter = directSuperClasses
				.iterator();
		while (iter.hasNext()) {
			EdgeClass ec = (EdgeClass) iter.next();
			if (newToMin > ec.getToMax()) {
				throw new InheritanceException(
						"Cardinalities for To-connection of edge class "
								+ getQualifiedName()
								+ " cannot be merged, minimal cardinality "
								+ newToMin
								+ " is bigger than maximal cardinality "
								+ ec.getToMax() + " of inherited edge class "
								+ ec.getQualifiedName());
			}
			if (newToMin < ec.getToMin()) {
				newToMin = ec.getToMin();
			}
			if (newFromMin > ec.getFromMax()) {
				throw new InheritanceException(
						"Cardinalities for From-connection of edge class "
								+ getQualifiedName()
								+ " cannot be merged, minimal cardinality "
								+ newFromMin
								+ " is bigger than maximal cardinality "
								+ ec.getFromMax() + " of inherited edge class "
								+ ec.getQualifiedName());
			}
			if (newFromMin < ec.getFromMin()) {
				newFromMin = ec.getFromMin();
			}

			if (newToMax < ec.getToMin()) {
				throw new InheritanceException(
						"Cardinalities for To-connection of edge class "
								+ getQualifiedName()
								+ " cannot be merged, maximal cardinality "
								+ newToMax
								+ " is lesser than minimal cardinality "
								+ ec.getToMin() + " of inherited edge class "
								+ ec.getQualifiedName());
			}
			if (newToMax > ec.getToMax()) {
				newToMax = ec.getToMax();
			}
			if (newFromMax < ec.getFromMin()) {
				throw new InheritanceException(
						"Cardinalities for From-connection of edge class "
								+ getQualifiedName()
								+ " cannot be merged, maximal cardinality "
								+ newFromMax
								+ " is lesser than minimal cardinality "
								+ ec.getFromMin() + " of inherited edge class "
								+ ec.getQualifiedName());
			}
			if (newFromMax > ec.getFromMax()) {
				newFromMax = ec.getFromMax();
			}
		}
		if ((fromMin != newFromMin) || (fromMax != newFromMax)
				|| (toMin != newToMin) || (toMax != newToMax)) {
			fromMin = newFromMin;
			fromMax = newFromMax;
			toMin = newToMin;
			toMax = newToMax;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Tries to merge the VertexClasses of the edges endpoints
	 *
	 * @return true if a merge was done successfull, false if no merge was
	 *         needed or if a merge is not possible
	 */
	protected boolean mergeConnectionVertexClasses() {
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

				if (ec.getTo().isSubClassOf(mostSpecialTo)) {
					mostSpecialTo = ec.getTo();
				} else {
					throw new InheritanceException(
							"Cannot merge ToVertexClasses for EdgeClass "
									+ getQualifiedName() + ": VertexClass "
									+ mostSpecialTo.getQualifiedName()
									+ " and " + ec.getTo().getQualifiedName()
									+ " cannot be merged");
				}
			}
			if ((ec.getFrom() != mostSpecialFrom)
					&& (!mostSpecialFrom.isSubClassOf(ec.getFrom()))) {
				if (ec.getFrom().isSubClassOf(mostSpecialFrom)) {
					mostSpecialFrom = ec.getFrom();
				} else {
					throw new InheritanceException(
							"Cannot merge FromVertexClasses for EdgeClass "
									+ getQualifiedName() + " VertexClass "
									+ mostSpecialFrom.getQualifiedName()
									+ " and " + ec.getFrom().getQualifiedName()
									+ " cannot be merged");
				}
			}
		}
		if ((mostSpecialTo != getTo()) || (mostSpecialFrom != getFrom())) {
			to = mostSpecialTo;
			from = mostSpecialFrom;
			return true;
		}
		return false;
	}

	@Override
	public DirectedEdgeClass getInEdgeClass() {
		return inEdgeClass;
	}

	@Override
	public DirectedEdgeClass getOutEdgeClass() {
		return outEdgeClass;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends Edge> getM1Class() {
		return (Class<? extends Edge>) super.getM1Class();
	}

}
