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

package de.uni_koblenz.jgralab.greql.evaluator.fa;

import java.util.Set;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.vertexeval.ThisEdgeEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql.schema.Expression;
import de.uni_koblenz.jgralab.greql.schema.ThisEdge;
import de.uni_koblenz.jgralab.greql.types.TypeCollection;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.EdgeClass;

/**
 * This transition accepts an AggregationPathDescription. Am
 * AggregationPathDescription is for instance something like v --<>{isExprOf} w.
 * 
 * @author ist@uni-koblenz.de
 */
public class AggregationTransition extends Transition {

	private final VertexEvaluator<? extends Expression> predicateEvaluator;

	public VertexEvaluator<? extends Expression> getPredicateEvaluator() {
		return predicateEvaluator;
	}

	private ThisEdgeEvaluator thisEdgeEvaluator;

	/**
	 * The collection of types that are accepted by this transition
	 */
	protected TypeCollection typeCollection;

	public TypeCollection getTypeCollection() {
		return typeCollection;
	}

	/**
	 * an edge may have valid roles. This set holds the valid roles for this
	 * transition. If the transition is valid for all roles, this set is null
	 */
	protected Set<String> validToEdgeRoles;

	public Set<String> getValidToRoles() {
		return validToEdgeRoles;
	}

	public Set<String> getValidFromRoles() {
		return validFromEdgeRoles;
	}

	protected Set<String> validFromEdgeRoles;

	protected boolean aggregateFrom;

	public boolean isAggregateFrom() {
		return aggregateFrom;
	}

	/**
	 * returns a string which describes the edge
	 */
	@Override
	public String edgeString() {
		// String desc = "SimpleTransition";
		String desc = "AggregationTransition (aggregateFrom:" + aggregateFrom;
		if (typeCollection != null) {
			desc = desc + "\n " + typeCollection.toString() + "\n ";
		}
		desc += ")";
		return desc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * greql2.evaluator.fa.Transition#equalSymbol(greql2.evaluator.fa.EdgeTransition
	 * )
	 */
	@Override
	public boolean equalSymbol(Transition t) {
		if (!(t instanceof AggregationTransition)) {
			return false;
		}
		AggregationTransition et = (AggregationTransition) t;
		if (!typeCollection.equals(et.typeCollection)) {
			return false;
		}
		if (aggregateFrom != et.aggregateFrom) {
			return false;
		}
		if (validToEdgeRoles != null) {
			if (et.validToEdgeRoles == null) {
				return false;
			}
			if (!validToEdgeRoles.equals(et.validToEdgeRoles)) {
				return false;
			}
		} else {
			if (et.validToEdgeRoles != null) {
				return false;
			}
		}
		if (validFromEdgeRoles != null) {
			if (et.validFromEdgeRoles == null) {
				return false;
			}
			if (!validFromEdgeRoles.equals(et.validFromEdgeRoles)) {
				return false;
			}
		} else {
			if (et.validFromEdgeRoles != null) {
				return false;
			}
		}

		if (predicateEvaluator != null) {
			if (et.predicateEvaluator == null) {
				return false;
			}
			if (!predicateEvaluator.equals(et.predicateEvaluator)) {
				return false;
			}
		} else {
			if (et.predicateEvaluator != null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Copy-constructor, creates a copy of the given transition
	 */
	protected AggregationTransition(AggregationTransition t, boolean addToStates) {
		super(t, addToStates);
		aggregateFrom = t.aggregateFrom;
		typeCollection = t.typeCollection;
		validToEdgeRoles = t.validToEdgeRoles;
		predicateEvaluator = t.predicateEvaluator;
		thisEdgeEvaluator = t.thisEdgeEvaluator;
		validToEdgeRoles = t.validToEdgeRoles;
		validFromEdgeRoles = t.validFromEdgeRoles;
	}

	/**
	 * returns a copy of this transition
	 */
	@Override
	public Transition copy(boolean addToStates) {
		return new AggregationTransition(this, addToStates);
	}

	/**
	 * Creates a new transition from start state to end state. The Transition
	 * accepts all aggregations that have the right aggregation direction, role,
	 * startVertexType, endVertexType, edgeType and even it's possible to define
	 * a specific edge. This constructor creates a transition to accept a
	 * simplePathDescription
	 * 
	 * @param start
	 *            The state where this transition starts
	 * @param end
	 *            The state where this transition ends
	 * @param aggregateFrom
	 *            The direction of the aggregation, true for an aggregation with
	 *            the aggregation end at the near vertex, false for an
	 *            aggregation with the aggregation end at the far vertex
	 * @param typeCollection
	 *            The types which restrict the possible edges
	 * @param roles
	 *            The set of accepted edge role names, or null if any role is
	 *            accepted
	 */
	public AggregationTransition(State start, State end, boolean aggregateFrom,
			TypeCollection typeCollection, Set<String> roles,
			VertexEvaluator<? extends Expression> predicateEvaluator,
			GreqlQueryImpl query) {
		super(start, end);
		this.aggregateFrom = aggregateFrom;
		validToEdgeRoles = roles;
		validFromEdgeRoles = null;
		this.typeCollection = typeCollection;
		this.predicateEvaluator = predicateEvaluator;
		ThisEdge v = (ThisEdge) query.getQueryGraph().getFirstVertex(
				ThisEdge.VC);
		if (v != null) {
			thisEdgeEvaluator = (ThisEdgeEvaluator) query.getVertexEvaluator(v);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see greql2.evaluator.fa.Transition#reverse()
	 */
	@Override
	public void reverse() {
		super.reverse();
		aggregateFrom = !aggregateFrom;
		Set<String> tempSet = validFromEdgeRoles;
		validFromEdgeRoles = validToEdgeRoles;
		validToEdgeRoles = tempSet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see greql2.evaluator.fa.Transition#isEpsilon()
	 */
	@Override
	public boolean isEpsilon() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see greql2.evaluator.fa.Transition#accepts(jgralab.Vertex, jgralab.Edge)
	 */
	@Override
	public boolean accepts(Vertex v, Edge e, InternalGreqlEvaluator evaluator) {
		if (e == null) {
			return false;
		}

		if (aggregateFrom) {
			if (e.getThatAggregationKind() == AggregationKind.NONE) {
				return false;
			}
		} else {
			if (e.getThisAggregationKind() == AggregationKind.NONE) {
				return false;
			}
		}

		Set<String> validEdgeRoles = validToEdgeRoles;
		boolean checkToEdgeRoles = true;
		if (validEdgeRoles == null) {
			validEdgeRoles = validFromEdgeRoles;
			checkToEdgeRoles = false;
		}

		boolean rolesOnly = validEdgeRoles != null && typeCollection.isEmpty();
		boolean acceptedByRole = false;

		// checks if a role restriction is set and if e has the right role
		if (validEdgeRoles != null) {
			EdgeClass ec = e.getAttributedElementClass();
			Set<String> roles = null;
			if (e.isNormal() == checkToEdgeRoles) {
				roles = ec.getTo().getAllRoles();
			} else {
				roles = ec.getFrom().getAllRoles();
			}
			for (String role : roles) {
				if (validEdgeRoles.contains(role)) {
					acceptedByRole = true;
					break;
				}
			}
		}
		if (rolesOnly) {
			if (!acceptedByRole) {
				return false;
			}
		} else {
			if (!acceptedByRole) {
				EdgeClass edgeClass = e.getAttributedElementClass();
				if (!typeCollection.acceptsType(edgeClass)) {
					return false;
				}
			}
		}

		// checks if a boolean expression exists and if it evaluates to true
		if (predicateEvaluator != null) {
			thisEdgeEvaluator.setValue(e, evaluator);
			Object res = predicateEvaluator.getResult(evaluator);
			if (res instanceof Boolean) {
				if (((Boolean) res).equals(Boolean.TRUE)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	/**
	 * returns the vertex of the datagraph which can be visited after this
	 * transition has fired. This is the vertex at the end of the edge
	 */
	@Override
	public Vertex getNextVertex(Vertex v, Edge e) {
		return e.getThat();
	}

	@Override
	public String prettyPrint() {
		String symbol = "--<>";
		if (aggregateFrom) {
			symbol = "<>--";
		}

		return symbol + typeCollection.toString();
	}

	@Override
	public boolean consumesEdge() {
		return true;
	}
}
