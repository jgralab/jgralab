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

import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.GraphMarker;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.ThisEdgeEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.ThisVertexEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.schema.ThisEdge;
import de.uni_koblenz.jgralab.greql2.schema.ThisVertex;

/**
 * This transition may fire, if the VertexEvaluator it holds as attribute returns true as result
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> 
 * Summer 2006, Diploma Thesis
 *
 */
public class BoolExpressionTransition extends Transition {

	private VertexEvaluator boolExpressionEvaluator;

	private ThisVertexEvaluator thisVertexEvaluator;
	
	private ThisEdgeEvaluator thisEdgeEvaluator;
	

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
		thisVertexEvaluator = t.thisVertexEvaluator;
		thisEdgeEvaluator = t.thisEdgeEvaluator;

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
	public BoolExpressionTransition(State start, State end, VertexEvaluator boolEval, GraphMarker<VertexEvaluator> graphMarker) { 
		super(start, end);
		boolExpressionEvaluator = boolEval;
		Vertex v = graphMarker.getGraph().getFirstVertexOfClass(ThisVertex.class);
		if (v != null)
			thisVertexEvaluator = (ThisVertexEvaluator) graphMarker.getMark(v);
		v = graphMarker.getGraph().getFirstVertexOfClass(ThisEdge.class);
		if (v != null)
			thisEdgeEvaluator = (ThisEdgeEvaluator) graphMarker.getMark(v);
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
		System.out.println("checking if boolean expression accepts vertex " + v + " and edge " +e );
		if (thisEdgeEvaluator != null) {
			System.out.println("Setting thisEdge to " + e);
			thisEdgeEvaluator.setValue(new JValue(e));
		}	
		if (thisVertexEvaluator != null) {
			thisVertexEvaluator.setValue(new JValue(v));
		}	
		System.out.println("Try to get result");
		JValue res = boolExpressionEvaluator.getResult(subgraph);
		if (res.isBoolean()) {
			try {
				if (res.toBoolean() == Boolean.TRUE) {
					System.out.println("BooleanExpressionTransition accepting vertex");
					return true;
				}	
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
