package de.uni_koblenz.jgralab.impl.diskv2;


public class EdgeTracker extends Tracker<EdgeImpl> {

	/**
	 * Create a new tracker with a ByteBuffer size of 64 Bytes.
	 */
	public EdgeTracker() {
		super(EDGE_BASE_SIZE);
	}

	/**
	 * Store all variables of a GraphElement in the Tracker
	 * 
	 * @param ge
	 *            The GraphElement whose variables will be stored
	 */
	@Override
	protected void storeVariables(EdgeImpl ge) {
		EdgeImpl ed = ge;
		// TODO Don't put typeID in the buffer, but declare it as a normal
		// member
		int typeId = ed.getAttributedElementClass()
				.getGraphElementClassIdInSchema();
		variables.putInt(0, (typeId + 1));

		putVariable(4, ed.getNextEdgeId());
		putVariable(12, ed.getPrevEdgeId());
		putVariable(20, ed.getNextIncidenceId());
		putVariable(28, ed.getPrevIncidenceId());
		putVariable(36, ed.getIncidentVertexId());
		putVariable(44,
				((ReversedEdgeImpl) ed.getReversedEdge()).getNextIncidenceId());
		putVariable(52,
				((ReversedEdgeImpl) ed.getReversedEdge()).getPrevIncidenceId());
		// System.out.println("** store " +
		// (long)((ReversedEdgeImpl)ed.getReversedEdge()).getIncidentVertexId());
		putVariable(60,
				((ReversedEdgeImpl) ed.getReversedEdge()).getIncidentVertexId());
	}
}