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
package de.uni_koblenz.jgralab.impl.std;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.InternalEdge;
import de.uni_koblenz.jgralab.impl.InternalVertex;

/**
 * The implementation of a <code>Vertex</code> accessing attributes without
 * versioning.
 *
 * @author Jose Monte(monte@uni-koblenz.de)
 */
public abstract class VertexImpl extends
		de.uni_koblenz.jgralab.impl.VertexBaseImpl {
	private InternalVertex nextVertex;
	private InternalVertex prevVertex;
	private InternalEdge firstIncidence;
	private InternalEdge lastIncidence;

	/**
	 * holds the version of the vertex structure, for every modification of the
	 * structure (e.g. adding or deleting an incident edge or changing the
	 * incidence sequence) this version number is increased by one. It is set to
	 * 0 when the vertex is created or the graph is loaded.
	 */
	private long incidenceListVersion = 0;

	@Override
	public InternalVertex getNextVertexInVSeq() {
		assert isValid() : this + " is not valid.";
		return nextVertex;
	}

	@Override
	public InternalVertex getPrevVertexInVSeq() {
		assert isValid();
		return prevVertex;
	}

	@Override
	public InternalEdge getFirstIncidenceInISeq() {
		return firstIncidence;
	}

	@Override
	public InternalEdge getLastIncidenceInISeq() {
		return lastIncidence;
	}

	@Override
	public void setNextVertex(Vertex nextVertex) {
		this.nextVertex = (VertexImpl) nextVertex;
	}

	@Override
	public void setPrevVertex(Vertex prevVertex) {
		this.prevVertex = (VertexImpl) prevVertex;
	}

	@Override
	public void setFirstIncidence(InternalEdge firstIncidence) {
		this.firstIncidence = firstIncidence;
	}

	@Override
	public void setLastIncidence(InternalEdge lastIncidence) {
		this.lastIncidence = lastIncidence;
	}

	@Override
	public void setIncidenceListVersion(long incidenceListVersion) {
		this.incidenceListVersion = incidenceListVersion;
	}

	@Override
	public long getIncidenceListVersion() {
		assert isValid();
		return incidenceListVersion;
	}

	/**
	 *
	 * @param id
	 * @param graph
	 */
	protected VertexImpl(int id, Graph graph) {
		super(id, graph);
		((GraphImpl) graph).addVertex(this);
	}

	@Override
	public void setId(int id) {
		assert id >= 0;
		this.id = id;
	}
}
