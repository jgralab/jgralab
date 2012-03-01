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
import de.uni_koblenz.jgralab.eca.events.EventDescription.EventTime;
import de.uni_koblenz.jgralab.schema.EdgeClass;

public class DeleteEdgeEvent extends Event<EdgeClass> {

	/**
	 * The to be deleted Edge or null if the EventTime is after
	 */
	private Edge edge;
	private Vertex alpha;
	private Vertex omega;

	/**
	 * Creates an DeleteEdgeEvent with the given parameters
	 * 
	 * @param nestedCalls
	 *            depth of nested trigger calls
	 * @param graph
	 *            Graph where the Event happened
	 * @param edge
	 *            the to be deleted Edge or null if the EventTime is after
	 */
	public DeleteEdgeEvent(int nestedCalls, Graph graph, Edge edge) {
		super(nestedCalls, EventTime.BEFORE, graph, edge
				.getAttributedElementClass());
		this.edge = edge;
		this.alpha = edge.getAlpha();
		this.omega = edge.getOmega();
	}

	/**
	 * Creates an DeleteEdgeEvent with the given parameters
	 * 
	 * @param nestedCalls
	 *            depth of nested trigger calls
	 * @param graph
	 *            Graph where the Event happened
	 * @param type
	 *            type of the deleted Edge
	 */
	public DeleteEdgeEvent(int nestedCalls, Graph graph, EdgeClass type,Vertex oldAlpha, Vertex oldOmega) {
		super(nestedCalls, EventTime.AFTER, graph, type);
		edge = null;
		this.alpha = oldAlpha;
		this.omega = oldOmega;
	}

	/**
	 * @return the AttributedElement that causes this Event or null if the
	 *         EventTime is after
	 */
	@Override
	public Edge getElement() {
		return edge;
	}
	
	/**
	 * Returns the old alpha {@link Vertex} of the deleted Edge
	 * 
	 * @return the alpha {@link Vertex} of the deleted Edge 
	 */
	public Vertex getAlpha(){
		return this.alpha;
	}
	
	/**
	 * Returns the old omega {@link Vertex} of the deleted Edge
	 * 
	 * @return the omega {@link Vertex} of the deleted Edge 
	 */
	public Vertex getOmega(){
		return this.omega;
	}
}
