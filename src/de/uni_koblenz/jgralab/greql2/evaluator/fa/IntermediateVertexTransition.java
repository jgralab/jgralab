/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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

import java.util.Iterator;

import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;

public class IntermediateVertexTransition extends Transition {

	/**
	 * this transition may only fire, if the end-vertex of the edge e is part of the result of 
	 * this VertexEvaluator
	 */
	public VertexEvaluator intermediateVertexEvaluator;


	/**
	 * returns true if this transition and the given transition t accept the
	 * same edges
	 */
	public boolean equalSymbol(Transition t) {
		if (!(t instanceof IntermediateVertexTransition))
			return false;
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
			VertexEvaluator intermediateVertices) {
		super(start, end);
		this.intermediateVertexEvaluator = intermediateVertices;
	}

	

	/**
	 * Copy-constructor, creates a copy of the given transition
	 */
	protected IntermediateVertexTransition(IntermediateVertexTransition t, boolean addToStates) {
		super(t, addToStates);
		intermediateVertexEvaluator = t.intermediateVertexEvaluator;
	}
	
	/**
	 * returns a copy of this transition
	 */
	public Transition copy(boolean addToStates) {
		return new IntermediateVertexTransition(this, addToStates);
	}

	/**
	 * Checks if the transition is an epsilon-transition
	 * 
	 * @return true if this transition is an epsilon-transition, false otherwise
	 */
	public boolean isEpsilon() {
		return false;
	}
	
	/**
	 * returns a string which describes the edge
	 */
	public String edgeString() {
		String desc = "IndermediateVertexTransition";
		return desc;
	}


	/* (non-Javadoc)
	 * @see greql2.evaluator.fa.Transition#accepts(jgralab.Vertex, jgralab.Edge, greql2.evaluator.SubgraphTempAttribute)
	 */
	public boolean accepts(Vertex v, Edge e, BooleanGraphMarker subgraph)
			throws EvaluateException {
		// checks if a intermediateVertexExpression exists and if the end-vertex
		// of e is part of the result of this expression

		if (intermediateVertexEvaluator != null) {
			JValue tempRes = intermediateVertexEvaluator.getResult(subgraph);
			try {
			if (tempRes.isCollection()) {
				JValueCollection intermediateVertices = tempRes.toCollection();
				Iterator<JValue> iter = intermediateVertices.iterator();
				while (iter.hasNext()) {
					if (iter.next().toVertex().equals(v))
						return true;
				}
			} else {
				Vertex intermediateVertex = tempRes.toVertex();
				if (v == intermediateVertex)
					return true;
			}
			} catch (JValueInvalidTypeException exception) {
				throw new EvaluateException("Error in Transition.accept : "
						+ exception.toString());
			}
		}
		return false;
	}
	
	/**
	 * returns the vertex of the datagraph which can be visited after this transition has fired.
	 * This is the vertex itself
	 */
	@Override
	public  Vertex getNextVertex(Vertex v, Edge e) {
		return v;
	}

}
