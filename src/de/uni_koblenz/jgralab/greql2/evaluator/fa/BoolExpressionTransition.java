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
 
package de.uni_koblenz.jgralab.greql2.evaluator.fa;

import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

/**
 * This transition may fire, if the VertexEvaluator it holds as attribute returns true as result
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> 
 * Summer 2006, Diploma Thesis
 *
 */
public class BoolExpressionTransition extends Transition {

	private VertexEvaluator boolExpressionEvaluator;


	/**
	 * returns a string which describes the edge
	 */
	public String edgeString() {
		String desc = "BoolExpressionTransition";
		return desc;
	}



	/* (non-Javadoc)
	 * @see greql2.evaluator.fa.Transition#equalSymbol(greql2.evaluator.fa.EdgeTransition)
	 */
	public boolean equalSymbol(Transition t) {
		if (!(t instanceof BoolExpressionTransition))
			return false;
		BoolExpressionTransition bt = (BoolExpressionTransition) t;
		if (bt.boolExpressionEvaluator == this.boolExpressionEvaluator)
			return true;
		return false;
	}

	/**
	 * Copy-constructor, creates a copy of the given transition
	 */
	protected BoolExpressionTransition(BoolExpressionTransition t, boolean addToStates) {
		super(t, addToStates);
		boolExpressionEvaluator = t.boolExpressionEvaluator;

	}
	
	
	/**
	 * returns a copy of this transition
	 */
	public Transition copy(boolean addToStates) {
		return new BoolExpressionTransition(this, addToStates);
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
	 * @param boolEval the VertexEvaluator which evaluates the boolean expression this 
	 * transition accepts
	 */
	public BoolExpressionTransition(State start, State end, VertexEvaluator boolEval) {
		super(start, end);
		boolExpressionEvaluator = boolEval;
	}


	/* (non-Javadoc)
	 * @see greql2.evaluator.fa.Transition#isEpsilon()
	 */
	public boolean isEpsilon() {
		return false;
	}


	/* (non-Javadoc)
	 * @see greql2.evaluator.fa.Transition#accepts(jgralab.Vertex, jgralab.Edge, greql2.evaluator.SubgraphTempAttribute)
	 */
	public boolean accepts(Vertex v, Edge e, BooleanGraphMarker subgraph)
			throws EvaluateException {
		JValue res = boolExpressionEvaluator.getResult(subgraph);
		if (res.isBoolean()) {
			try {
				if (res.toBoolean() == Boolean.TRUE)
					return true;
			} catch (JValueInvalidTypeException ex) {
				// may not happen here
			}
		}
		return false;
	}
	
	
	public Vertex getNextVertex(Vertex v, Edge e) {
		return v;
	}
}
