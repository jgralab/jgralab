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
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTypeCollection;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

/**
 * This transition accepts a vertex type restriction. It is used to accept
 * start- and goalrestrictions.
 * 
 * @author ist@uni-koblenz.de Summer 2006, Diploma Thesis
 * 
 */
public class VertexTypeRestrictionTransition extends Transition {

	/**
	 * The type collection that toggles which types are accepted and which are
	 * not
	 */
	private JValueTypeCollection typeCollection;

	/**
	 * returns true if this transition and the given transition t accept the
	 * same edges
	 */
	public boolean equalSymbol(Transition t) {
		if (!(t instanceof VertexTypeRestrictionTransition))
			return false;
		VertexTypeRestrictionTransition vt = (VertexTypeRestrictionTransition) t;
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
	 *            The typeIds which restricts the possible start vertices
	 */
	public VertexTypeRestrictionTransition(State start, State end,
			JValueTypeCollection typeCollection) {
		super(start, end);
		this.typeCollection = typeCollection;
	}

	/**
	 * Copy-constructor, creates a copy of the given transition
	 */
	protected VertexTypeRestrictionTransition(
			VertexTypeRestrictionTransition t, boolean addToStates) {
		super(t, addToStates);
		typeCollection = new JValueTypeCollection(t.typeCollection);
	}

	/**
	 * returns a copy of this transition
	 */
	public Transition copy(boolean addToStates) {
		return new VertexTypeRestrictionTransition(this, addToStates);
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
	@SuppressWarnings("unchecked")
	public boolean accepts(Vertex v, Edge e, AbstractGraphMarker subgraph)
			throws EvaluateException {
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
