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

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTypeCollection;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class VertexRestrictionTransition extends Transition {

	/**
	 * The list of types that are excluded by the type restriction
	 */
	protected JValueTypeCollection typeCollection;

	/**
	 * returns true if this transition and the given transition t accept the
	 * same edges
	 */
	public boolean equalSymbol(Transition t) {
		if (!(t instanceof VertexRestrictionTransition))
			return false;
		VertexRestrictionTransition vt = (VertexRestrictionTransition) t;
		if (!typeCollection.equals(vt.typeCollection))
			return false;
		return true;
	}

	/**
	 * Creates a new transition from start state to end state. The Transition
	 * may fire if the start vertex has the right type
	 * 
	 * @param start
	 *            The state where this transition starts
	 * @param end
	 *            The state where this transition ends
	 * @param typeCollection
	 *            The typeIDs which restrict the possible start vertices
	 */
	public VertexRestrictionTransition(State start, State end,
			JValueTypeCollection typeCollection) {
		super(start, end);
		this.typeCollection = typeCollection;
	}

	/**
	 * Copy-constructor, creates a copy of the given transition
	 */
	protected VertexRestrictionTransition(VertexRestrictionTransition t,
			boolean addToStates) {
		super(t, addToStates);
		startState = t.startState;
		startState.addOutTransition(this);
		endState = t.endState;
		endState.addInTransition(this);
		typeCollection = new JValueTypeCollection(t.typeCollection);
	}

	/**
	 * returns a copy of this transition
	 */
	public Transition copy(boolean addToStates) {
		return new VertexRestrictionTransition(this, addToStates);
	}

	/**
	 * reverses this transition, that means, the former end state gets the new
	 * start state and vice versa,
	 */
	public void reverse() {
		State s = startState;
		startState = endState;
		endState = s;
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
		String desc = "VertexRestrictinTransition";
		return desc;
	}

	/**
	 * Checks if the transition can fire with the vertex as input, this means,
	 * no edge will be traversed but its only checked if this transition accepts
	 * the given vertex, this is needed to check things like
	 * startVertexRestriction etc
	 * 
	 * @param v
	 *            the current vertex
	 * @param subgraph
	 *            the SubgraphTempAttribute which should be accepted
	 * @return true if the transition can fire with e, false otherwise
	 */
	public boolean accepts(Vertex v, Edge e, BooleanGraphMarker subgraph) {
		// it is not neccessary to check if the vertex belongs to a special
		// subgraph, because if it does not, this method will not be called and
		// there is no edge connected to this vertex wich belongs to the
		// subgraph
		// checks if a startVertexTypeRestriction is set and if v has the right
		// type
		AttributedElementClass vertexClass = v.getAttributedElementClass();
		if (!typeCollection.acceptsType(vertexClass))
			return false;
		return true;
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
		StringBuilder b = new StringBuilder();
		String delim = "";
		for (AttributedElementClass c : typeCollection.getAllowedTypes()) {
			b.append(delim);
			b.append(c.getSimpleName());
			delim = ",";
		}	
		return "&{" + b + "}";
	}

}
