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
import de.uni_koblenz.jgralab.greql.types.TypeCollection;
import de.uni_koblenz.jgralab.schema.VertexClass;

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
	private final TypeCollection typeCollection;

	public TypeCollection getAcceptedVertexTypes() {
		return typeCollection;
	}

	/**
	 * returns true if this transition and the given transition t accept the
	 * same edges
	 */
	@Override
	public boolean equalSymbol(Transition t) {
		if (!(t instanceof VertexTypeRestrictionTransition)) {
			return false;
		}
		VertexTypeRestrictionTransition vt = (VertexTypeRestrictionTransition) t;
		if (!typeCollection.equals(vt.typeCollection)) {
			return false;
		}
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
			TypeCollection typeCollection) {
		super(start, end);
		assert typeCollection.isBound();
		this.typeCollection = typeCollection;
	}

	/**
	 * Copy-constructor, creates a copy of the given transition
	 */
	protected VertexTypeRestrictionTransition(
			VertexTypeRestrictionTransition t, boolean addToStates) {
		super(t, addToStates);
		typeCollection = t.typeCollection;
	}

	/**
	 * returns a copy of this transition
	 */
	@Override
	public Transition copy(boolean addToStates) {
		return new VertexTypeRestrictionTransition(this, addToStates);
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
	 * @return true if the transition can fire with e, false otherwise
	 */
	@Override
	public boolean accepts(Vertex v, Edge e, InternalGreqlEvaluator evaluator) {
		VertexClass vertexClass = v.getAttributedElementClass();
		if (!typeCollection.acceptsType(vertexClass)) {
			return false;
		}
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
		return "&" + typeCollection.toString();
	}

	@Override
	public boolean consumesEdge() {
		return false;
	}
}
