/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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
import de.uni_koblenz.jgralab.impl.EdgeBaseImpl;
import de.uni_koblenz.jgralab.impl.IncidenceImpl;
import de.uni_koblenz.jgralab.impl.VertexBaseImpl;

/**
 * The implementation of a reversed edge accessing attributes without
 * versioning. This implementation uses singly-linked lists only, in contrast to
 * the original version which uses doubly-linked lists.
 * 
 * @author Jose Monte {monte@uni-koblenz.de} (original implementation)
 * @author Mahdi Derakhshanmanesh {manesh@uni-koblenz.de} (adjusted version)
 */
public abstract class ReversedEdgeImpl extends
		de.uni_koblenz.jgralab.impl.ReversedEdgeBaseImpl {
	/**
	 * The 'this' {@link Vertex}.
	 */
	private VertexBaseImpl incidentVertex;

	/**
	 * The next incident {@link Edge}.
	 */
	private IncidenceImpl nextIncidence;

	/**
	 * The constructor.
	 * 
	 * @param normalEdge
	 *            The {@link Edge} to be reversed.
	 * @param graph
	 *            A reference to the {@link Graph}, this {@link Edge} shall be
	 *            added to.
	 */
	protected ReversedEdgeImpl(EdgeBaseImpl normalEdge, Graph graph) {
		super(normalEdge, graph);
	}

	@Override
	protected VertexBaseImpl getIncidentVertex() {
		return incidentVertex;
	}

	@Override
	protected IncidenceImpl getNextIncidence() {
		return nextIncidence;
	}

	@Override
	protected IncidenceImpl getPrevIncidence() {
		Edge prevEdge = null;

		for (Edge currEdge = incidentVertex.getFirstEdge(); currEdge != null; currEdge = currEdge
				.getNextEdge()) {
			if (currEdge == this) {
				return (IncidenceImpl) prevEdge;
			}
			prevEdge = currEdge;
		}

		return null;
	}

	@Override
	protected void setIncidentVertex(VertexBaseImpl v) {
		this.incidentVertex = v;
	}

	@Override
	protected void setNextIncidence(IncidenceImpl nextIncidence) {
		this.nextIncidence = nextIncidence;
	}

	@Override
	protected void setPrevIncidence(IncidenceImpl prevIncidence) {
		// throw new UnsupportedOperationException(
		// "Unsupported in savemem implementation.");
	}
}
