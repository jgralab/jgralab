package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;

public class ChangeEdgeEvent extends Event {

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
			Graph graph,
			Edge edge, Vertex oldVertex,
			Vertex newVertex) {
		super(nestedCalls, time, graph, edge.getM1Class());
		this.edge = edge;
		this.oldVertex = oldVertex;
		this.newVertex = newVertex;
	}

	// ------------------------------------------------------------------------

	/**
	 * @return the Edge that causes this Event
	 */
	public Edge getEdge() {
		return edge;
	}

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
	public AttributedElement getElement() {
		return this.edge;
	}

}
