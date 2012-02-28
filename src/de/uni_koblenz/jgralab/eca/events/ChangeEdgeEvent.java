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
package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.eca.events.ChangeEdgeEventDescription.EdgeEnd;
import de.uni_koblenz.jgralab.schema.EdgeClass;

public class ChangeEdgeEvent extends Event<EdgeClass> {

	/**
	 * Edge that causes the Event
	 */
	private Edge edge;

	/**
	 * Old Vertex of Edge
	 */
	private Vertex oldVertex;

	/**
	 * New Vertex of Edge
	 */
	private Vertex newVertex;

	/**
	 * If alpha or omega end is affected
	 */
	private EdgeEnd edgeEnd;

	/**
	 * Creates a new ChangeEdgeEvent with the given parameters
	 * 
	 * @param nestedCalls
	 *            depth of nested trigger calls
	 * @param time
	 *            before or after
	 * @param graph
	 *            Graph where the Event happened
	 * @param edge
	 *            Edge that causes the Event
	 * @param oldVertex
	 *            old Vertex of Edge
	 * @param newVertex
	 *            new Vertex of Edge
	 */
	public ChangeEdgeEvent(int nestedCalls, EventDescription.EventTime time,
			Graph graph, Edge edge, Vertex oldVertex, Vertex newVertex,
			EdgeEnd end) {
		super(nestedCalls, time, graph, edge.getAttributedElementClass());
		this.edge = edge;
		this.oldVertex = oldVertex;
		this.newVertex = newVertex;
		edgeEnd = end;
	}

	// ------------------------------------------------------------------------

	/**
	 * @return the old Vertex of this Edge
	 */
	public Vertex getOldVertex() {
		return oldVertex;
	}

	/**
	 * @return the new Vertex of this Edge
	 */
	public Vertex getNewVertex() {
		return newVertex;
	}

	/**
	 * @return the AttributedElement that causes this Event
	 */
	@Override
	public Edge getElement() {
		return edge;
	}

	/**
	 * @return whether the alpha or omega end is affected
	 */
	public EdgeEnd getEdgeEnd() {
		return edgeEnd;
	}

}
