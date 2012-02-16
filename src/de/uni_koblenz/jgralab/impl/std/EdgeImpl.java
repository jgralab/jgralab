/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2012 Institute for Software Technology
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
package de.uni_koblenz.jgralab.impl.std;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.InternalEdge;
import de.uni_koblenz.jgralab.impl.InternalVertex;

/**
 * The implementation of an <code>Edge</code> accessing attributes without
 * versioning.
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 */
public abstract class EdgeImpl extends de.uni_koblenz.jgralab.impl.EdgeBaseImpl {
	// global edge sequence
	private InternalEdge nextEdge;
	private InternalEdge prevEdge;

	// the this-vertex
	private InternalVertex incidentVertex;

	// incidence list
	private InternalEdge nextIncidence;
	private InternalEdge prevIncidence;

	@Override
	public InternalEdge getNextEdgeInESeq() {
		assert isValid();
		return nextEdge;
	}

	@Override
	public InternalEdge getPrevEdgeInESeq() {
		assert isValid();
		return prevEdge;
	}

	@Override
	public InternalVertex getIncidentVertex() {
		return incidentVertex;
	}

	@Override
	public InternalEdge getNextIncidenceInISeq() {
		return nextIncidence;
	}

	@Override
	public InternalEdge getPrevIncidenceInISeq() {
		return prevIncidence;
	}

	@Override
	public void setNextEdgeInGraph(Edge nextEdge) {
		this.nextEdge = (InternalEdge) nextEdge;
	}

	@Override
	public void setPrevEdgeInGraph(Edge prevEdge) {
		this.prevEdge = (InternalEdge) prevEdge;
	}

	@Override
	public void setIncidentVertex(Vertex v) {
		incidentVertex = (InternalVertex) v;
	}

	@Override
	public void setNextIncidenceInternal(InternalEdge nextIncidence) {
		this.nextIncidence = nextIncidence;
	}

	@Override
	public void setPrevIncidenceInternal(InternalEdge prevIncidence) {
		this.prevIncidence = prevIncidence;
	}

	protected EdgeImpl(int anId, Graph graph, Vertex alpha, Vertex omega) {
		super(anId, graph, alpha, omega);
	}

	@Override
	public void setId(int id) {
		assert id >= 0;
		this.id = id;
	}
}
