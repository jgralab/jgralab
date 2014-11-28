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

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlQueryImpl;
import de.uni_koblenz.jgralab.greql.evaluator.vertexeval.ThisVertexEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql.schema.Expression;
import de.uni_koblenz.jgralab.greql.schema.ThisVertex;
import de.uni_koblenz.jgralab.greql.serialising.GreqlSerializer;

/**
 * This transition may fire, if the VertexEvaluator it holds as attribute
 * returns true as result
 *
 * @author ist@uni-koblenz.de
 *
 */
public class BoolExpressionTransition extends Transition {

	private final VertexEvaluator<? extends Expression> boolExpressionEvaluator;

	public VertexEvaluator<? extends Expression> getBooleanExpressionEvaluator() {
		return boolExpressionEvaluator;
	}

	private ThisVertexEvaluator thisVertexEvaluator;

	/**
	 * returns a string which describes the edge
	 */
	@Override
	public String edgeString() {
		String desc = "BoolExpressionTransition";
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
		if (!(t instanceof BoolExpressionTransition)) {
			return false;
		}
		BoolExpressionTransition bt = (BoolExpressionTransition) t;
		if (bt.boolExpressionEvaluator == boolExpressionEvaluator) {
			return true;
		}
		return false;
	}

	/**
	 * Copy-constructor, creates a copy of the given transition
	 */
	protected BoolExpressionTransition(BoolExpressionTransition t,
			boolean addToStates) {
		super(t, addToStates);
		boolExpressionEvaluator = t.boolExpressionEvaluator;
		thisVertexEvaluator = t.thisVertexEvaluator;
	}

	/**
	 * returns a copy of this transition
	 */
	@Override
	public Transition copy(boolean addToStates) {
		return new BoolExpressionTransition(this, addToStates);
	}

	/**
	 * Creates a new transition from start state to end state.
	 */
	public BoolExpressionTransition(State start, State end,
			VertexEvaluator<? extends Expression> boolEval, GreqlQueryImpl query) {
		super(start, end);
		boolExpressionEvaluator = boolEval;
		ThisVertex v = (ThisVertex) query.getQueryGraph().getFirstVertex(
				ThisVertex.VC);
		if (v != null) {
			thisVertexEvaluator = (ThisVertexEvaluator) query
					.getVertexEvaluator(v);
		}
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
		if (thisVertexEvaluator != null) {
			thisVertexEvaluator.setValue(v, evaluator);
		}
		Object res = boolExpressionEvaluator.getResult(evaluator);
		if ((res instanceof Boolean) && ((Boolean) res).equals(Boolean.TRUE)) {
			return true;
		}
		return false;
	}

	@Override
	public Vertex getNextVertex(Vertex v, Edge e) {
		return v;
	}

	@Override
	public String prettyPrint() {
		return "IntermediateVertex "
				+ GreqlSerializer.serializeVertex(boolExpressionEvaluator
						.getVertex());
	}

	@Override
	public boolean consumesEdge() {
		return false;
	}
}
