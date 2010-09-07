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
package de.uni_koblenz.jgralab.impl.std;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.impl.EdgeBaseImpl;
import de.uni_koblenz.jgralab.impl.IncidenceImpl;
import de.uni_koblenz.jgralab.impl.VertexBaseImpl;

/**
 * The implementation of an ReversedEdge accessing attributes without
 * versioning.
 * 
 * @author Jose Monte(monte@uni-koblenz.de)
 */
public abstract class ReversedEdgeImpl extends
		de.uni_koblenz.jgralab.impl.ReversedEdgeBaseImpl {
	private VertexBaseImpl incidentVertex;
	private IncidenceImpl nextIncidence;
	private IncidenceImpl prevIncidence;

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
		return prevIncidence;
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
		this.prevIncidence = prevIncidence;
	}

	/**
	 * 
	 * @param normalEdge
	 * @param graph
	 */
	protected ReversedEdgeImpl(EdgeBaseImpl normalEdge, Graph graph) {
		super(normalEdge, graph);
	}
}
