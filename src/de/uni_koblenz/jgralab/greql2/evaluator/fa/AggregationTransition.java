/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
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

package de.uni_koblenz.jgralab.greql2.evaluator.fa;

import java.util.Set;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.ThisEdgeEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueImpl;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTypeCollection;
import de.uni_koblenz.jgralab.greql2.schema.ThisEdge;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

/**
 * This transition accepts an AggregationPathDescription. Am
 * AggregationPathDescription is for instance something like v --<>{isExprOf} w.
 * 
 * @author ist@uni-koblenz.de
 */
public class AggregationTransition extends Transition {

	private VertexEvaluator predicateEvaluator;

	private ThisEdgeEvaluator thisEdgeEvaluator;

	/**
	 * The collection of types that are accepted by this transition
	 */
	protected JValueTypeCollection typeCollection;

	/**
	 * an edge may have valid roles. This set holds the valid roles for this
	 * transition. If the transition is valid for all roles, this set is null
	 */
	protected Set<String> validEdgeRoles;

	protected boolean aggregateFrom;

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
		if (validEdgeRoles != null) {
			if (et.validEdgeRoles == null) {
				return false;
			}
			if (!validEdgeRoles.equals(et.validEdgeRoles)) {
				return false;
			}
		}
		if (validEdgeRoles == null) {
			if (et.validEdgeRoles != null) {
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
		typeCollection = new JValueTypeCollection(t.typeCollection);
		validEdgeRoles = t.validEdgeRoles;
		predicateEvaluator = t.predicateEvaluator;
		thisEdgeEvaluator = t.thisEdgeEvaluator;
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
	 * @param role
	 *            The accepted edge role, or null if any role is accepted
	 */
	public AggregationTransition(State start, State end, boolean aggregateFrom,
			JValueTypeCollection typeCollection, Set<String> roles,
			VertexEvaluator predicateEvaluator,
			GraphMarker<VertexEvaluator> graphMarker) {
		super(start, end);
		this.aggregateFrom = aggregateFrom;
		this.validEdgeRoles = roles;
		this.typeCollection = typeCollection;
		this.predicateEvaluator = predicateEvaluator;
		Vertex v = graphMarker.getGraph().getFirstVertexOfClass(ThisEdge.class);
		if (v != null) {
			thisEdgeEvaluator = (ThisEdgeEvaluator) graphMarker.getMark(v);
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
	 * @see greql2.evaluator.fa.Transition#accepts(jgralab.Vertex, jgralab.Edge,
	 * greql2.evaluator.SubgraphTempAttribute)
	 */
	@Override
	public boolean accepts(Vertex v, Edge e, AbstractGraphMarker<AttributedElement> subgraph)
			throws EvaluateException {
		if (e == null) {
			return false;
		}

		if (aggregateFrom) {
			if (e.getThatSemantics() == AggregationKind.NONE) {
				return false;
			}
		} else {
			if (e.getThisSemantics() == AggregationKind.NONE) {
				return false;
			}
		}

		// checks if the subgraphattribute is set and if the edge belongs to
		// this subgraph (if the edge belongs to it, also the endvertex must
		// belong to it)
		if ((subgraph != null) && !subgraph.isMarked(e)) {
			return false;
		}

		// checks if a role restriction is set and if e has the right role
		if ((validEdgeRoles != null)
				&& !(validEdgeRoles.contains(e.getThatRole()))) {
			return false;
		}

		// checks if a edgeTypeRestriction is set and if e has the right type
		AttributedElementClass edgeClass = e.getAttributedElementClass();
		if (!typeCollection.acceptsType(edgeClass)) {
			return false;
		}

		// checks if a boolean expression exists and if it evaluates to true
		if (predicateEvaluator != null) {
			thisEdgeEvaluator.setValue(new JValueImpl(e));
			JValue res = predicateEvaluator.getResult(subgraph);
			if (res.isBoolean()) {
				try {
					if (res.toBoolean().equals(Boolean.TRUE)) {
						return true;
					}
				} catch (JValueInvalidTypeException ex) {
					ex.printStackTrace();
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
		StringBuilder b = new StringBuilder();
		String delim = "";
		for (AttributedElementClass c : typeCollection.getAllowedTypes()) {
			b.append(delim);
			b.append(c.getSimpleName());
			delim = ",";
		}	
		String symbol = "--<>";
		if (aggregateFrom)
			symbol = "<>--";

		return symbol + "{" + b + "}";
	}
	
}
