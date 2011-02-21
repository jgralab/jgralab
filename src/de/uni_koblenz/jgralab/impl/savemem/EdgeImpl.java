/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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
package de.uni_koblenz.jgralab.impl.savemem;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.IncidenceImpl;
import de.uni_koblenz.jgralab.impl.VertexBaseImpl;

/**
 * The implementation of an <code>Edge</code> accessing attributes without
 * versioning. This implementation uses singly-linked lists only, in contrast to
 * the original version which uses doubly-linked lists.
 * 
 * @author Jose Monte {monte@uni-koblenz.de} (original implementation)
 * @author Mahdi Derakhshanmanesh {manesh@uni-koblenz.de} (adjusted version)
 */
public abstract class EdgeImpl extends de.uni_koblenz.jgralab.impl.EdgeBaseImpl {
	/**
	 * The reference to the next {@link Edge}.
	 */
	private EdgeImpl nextEdge;

	/**
	 * The 'this' {@link Vertex}.
	 */
	private VertexBaseImpl incidentVertex;

	/**
	 * The next incident {@link Edge}.
	 */
	private IncidenceImpl nextIncidence;

	@Override
	public Edge getNextEdge() {
		assert isValid();
		return this.nextEdge;
	}

	@Override
	public Edge getPrevEdge() {
		assert isValid();
		return findPrevEdgeInGraph();
	}

	private Edge findPrevEdgeInGraph() {
		Edge prevEdge = null;

		for (Edge currEdge = getGraph().getFirstEdge(); currEdge != null; currEdge = currEdge
				.getNextEdge()) {
			if (currEdge == this) {
				return prevEdge;
			}
			prevEdge = currEdge;
		}

		return null;
	}

	/**
	 * The constructor.
	 * 
	 * @param anId
	 *            The id of this {@link Edge}.
	 * @param graph
	 *            A reference to the {@link Graph}, this {@link Edge} shall be
	 *            added to.
	 * @param alpha
	 *            The starting {@link Vertex} node.
	 * @param omega
	 *            The ending {@link Vertex} node.
	 * @throws Exception
	 */
	protected EdgeImpl(int anId, Graph graph, Vertex alpha, Vertex omega) {
		super(anId, graph);
		((GraphImpl) graph).addEdge(this, alpha, omega);
	}

	@Override
	protected VertexBaseImpl getIncidentVertex() {
		return incidentVertex;
	}

	@Override
	protected IncidenceImpl getNextIncidenceInternal() {
		return nextIncidence;
	}

	@Override
	protected IncidenceImpl getPrevIncidenceInternal() {
		Edge prevEdge = null;
		return findPrevIncidence(prevEdge);
	}

	private IncidenceImpl findPrevIncidence(Edge prevEdge) {
		for (Edge currEdge = incidentVertex.getFirstIncidence(); currEdge != null; currEdge = currEdge
				.getNextIncidence()) {
			if (currEdge == this) {
				return (IncidenceImpl) prevEdge;
			}
			prevEdge = currEdge;
		}

		return null;
	}

	@Override
	protected void setNextEdgeInGraph(Edge nextEdge) {
		this.nextEdge = (EdgeImpl) nextEdge;
	}

	@Override
	protected void setPrevEdgeInGraph(Edge prevEdge) {
		throw new UnsupportedOperationException(
				"Unsupported in savemem implementation.");
	}

	@Override
	protected void setIncidentVertex(VertexBaseImpl v) {
		this.incidentVertex = v;
	}

	@Override
	protected void setNextIncidenceInternal(IncidenceImpl nextIncidence) {
		this.nextIncidence = nextIncidence;
	}

	@Override
	protected void setPrevIncidenceInternal(IncidenceImpl prevIncidence) {
		throw new UnsupportedOperationException(
				"Unsupported in savemem implementation.");
	}

	@Override
	protected void setId(int id) {
		assert id >= 0;
		this.id = id;
	}
}
