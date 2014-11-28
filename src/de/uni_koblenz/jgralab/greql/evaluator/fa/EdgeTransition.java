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
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql.schema.Expression;
import de.uni_koblenz.jgralab.greql.schema.GReQLDirection;
import de.uni_koblenz.jgralab.greql.types.TypeCollection;

/**
 * This transition accepts only one edge. Because this edge may be a variable or
 * even the result of an expression containing a variable, a reference to the
 * VertexEvaluator which evaluates this variable/expression is stored in this
 * transition and the result of this evaluator is acceptes.
 * 
 * This transition accepts the greql2 syntax: --{edge}->
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class EdgeTransition extends SimpleTransition {

	/**
	 * In GReQL 2 it is possible to specify an explicit edge. Cause this edge
	 * may be a variable or the result of an expression containing a variable,
	 * the VertexEvalutor which evaluates this edge expression is stored here so
	 * the result can be used as allowed edge
	 */
	private final VertexEvaluator<?> allowedEdgeEvaluator;

	public VertexEvaluator<?> getAllowedEdgeEvaluator() {
		return allowedEdgeEvaluator;
	}

	/**
	 * returns a string which describes the edge
	 */
	@Override
	public String edgeString() {
		String desc = "EdgeTransition";
		// String desc = "EdgeTransition ( Dir:" + validDirection.toString() + "
		// "
		// + edgeTypeRestriction.toString() + " Edge: " +
		// allowedEdgeEvaluator.toString() + " )";
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
		if (!(t instanceof EdgeTransition)) {
			return false;
		}
		EdgeTransition et = (EdgeTransition) t;
		if (!typeCollection.equals(et.typeCollection)) {
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
		if (validFromEdgeRoles == null) {
			if (et.validFromEdgeRoles != null) {
				return false;
			}
		} else {
			if (et.validFromEdgeRoles == null) {
				return false;
			}
			if (!validFromEdgeRoles.equals(et.validFromEdgeRoles)) {
				return false;
			}
		}
		if (allowedEdgeEvaluator != et.allowedEdgeEvaluator) {
			return false;
		}
		if (validDirection != et.validDirection) {
			return false;
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
	protected EdgeTransition(EdgeTransition t, boolean addToStates) {
		super(t, addToStates);
		allowedEdgeEvaluator = t.allowedEdgeEvaluator;
	}

	/**
	 * returns a copy of this transition
	 */
	@Override
	public Transition copy(boolean addToStates) {
		return new EdgeTransition(this, addToStates);
	}

	/**
	 * Creates a new transition from start state to end state. The Transition
	 * accepts all edges that have the right direction, role, startVertexType,
	 * endVertexType, edgeType and even it's possible to define a specific edge.
	 * This constructor creates a transition to accept a EdgePathDescription
	 * 
	 * @param start
	 *            The state where this transition starts
	 * @param end
	 *            The state where this transition ends
	 * @param dir
	 *            The direction of the accepted edges, may be EdeDirection.IN,
	 *            EdgeDirection.OUT or EdgeDirection.ANY
	 * @param typeCollection
	 *            The types which restrict the possible edges
	 * @param roles
	 *            The set of accepted edge role names, or null if any role is
	 *            accepted
	 * @param edgeEval
	 *            If this is set, only the resulting edge of this evaluator will
	 *            be accepted
	 */
	public EdgeTransition(State start, State end, GReQLDirection dir,
			TypeCollection typeCollection, Set<String> roles,
			VertexEvaluator<?> edgeEval,
			VertexEvaluator<? extends Expression> predicateEval, GreqlQueryImpl query) {
		super(start, end, dir, typeCollection, roles, predicateEval, query);
		allowedEdgeEvaluator = edgeEval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see greql2.evaluator.fa.Transition#accepts(jgralab.Vertex, jgralab.Edge)
	 */@Override
	public boolean accepts(Vertex v, Edge e, InternalGreqlEvaluator evaluator) {
		if (!super.accepts(v, e, evaluator)) {
			return false;
		}
		// checks if only one edge is allowed an if e is this allowed edge
		if (allowedEdgeEvaluator != null) {

			Edge allowedEdge = ((Edge) allowedEdgeEvaluator
					.getResult(evaluator)).getNormalEdge();
			if (e.getNormalEdge() != allowedEdge) {
				return false;
			}

		}
		return true;
	}

	public boolean consumedEdge() {
		return true;
	}

}
