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

import org.pcollections.PCollection;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql.evaluator.InternalGreqlEvaluator;
import de.uni_koblenz.jgralab.greql.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql.serialising.GreqlSerializer;

public class IntermediateVertexTransition extends Transition {

	/**
	 * this transition may only fire, if the end-vertex of the edge e is part of
	 * the result of this VertexEvaluator
	 */
	public VertexEvaluator<?> intermediateVertexEvaluator;

	public VertexEvaluator<?> getIntermediateVertexEvaluator() {
		return intermediateVertexEvaluator;
	}

	/**
	 * returns true if this transition and the given transition t accept the
	 * same edges
	 */
	@Override
	public boolean equalSymbol(Transition t) {
		if (!(t instanceof IntermediateVertexTransition)) {
			return false;
		}
		IntermediateVertexTransition vt = (IntermediateVertexTransition) t;
		if (intermediateVertexEvaluator != vt.intermediateVertexEvaluator) {
			return false;
		}
		return true;
	}

	/**
	 * creates a new transition, which accepts a intermediate vertex
	 * 
	 * @param start
	 *            state where this transition should start
	 * @param end
	 *            state where this transition should
	 * @param intermediateVertices
	 *            the collection of intermediate vertices
	 */
	public IntermediateVertexTransition(State start, State end,
			VertexEvaluator<?> intermediateVertices) {
		super(start, end);
		intermediateVertexEvaluator = intermediateVertices;
	}

	/**
	 * Copy-constructor, creates a copy of the given transition
	 */
	protected IntermediateVertexTransition(IntermediateVertexTransition t,
			boolean addToStates) {
		super(t, addToStates);
		intermediateVertexEvaluator = t.intermediateVertexEvaluator;
	}

	/**
	 * returns a copy of this transition
	 */
	@Override
	public Transition copy(boolean addToStates) {
		return new IntermediateVertexTransition(this, addToStates);
	}

	/**
	 * Checks if the transition is an epsilon-transition
	 * 
	 * @return true if this transition is an epsilon-transition, false otherwise
	 */
	@Override
	public boolean isEpsilon() {
		return false;
	}

	/**
	 * returns a string which describes the edge
	 */
	@Override
	public String edgeString() {
		String desc = "IndermediateVertexTransition";
		return desc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see greql2.evaluator.fa.Transition#accepts(jgralab.Vertex, jgralab.Edge)
	 */
	@Override
	public boolean accepts(Vertex v, Edge e, InternalGreqlEvaluator evaluator) {
		// checks if a intermediateVertexExpression exists and if the end-vertex
		// of e is part of the result of this expression

		if (intermediateVertexEvaluator != null) {
			Object tempRes = intermediateVertexEvaluator.getResult(evaluator);
			if (tempRes instanceof PCollection) {
				@SuppressWarnings("unchecked")
				PCollection<Vertex> intermediateVertices = (PCollection<Vertex>) tempRes;
				return intermediateVertices.contains(v);
			} else {
				Vertex intermediateVertex = (Vertex) tempRes;
				return v == intermediateVertex;
			}
		}
		return false;
	}

	/**
	 * returns the vertex of the datagraph which can be visited after this
	 * transition has fired. This is the vertex itself
	 */
	@Override
	public Vertex getNextVertex(Vertex v, Edge e) {
		return v;
	}

	@Override
	public String prettyPrint() {
		return "IntermediateVertex "
				+ GreqlSerializer.serializeVertex(intermediateVertexEvaluator
						.getVertex());
	}

	@Override
	public boolean consumesEdge() {
		return false;
	}
}
