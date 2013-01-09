package de.uni_koblenz.jgralab.impl.diskv2;

import de.uni_koblenz.jgralab.impl.GraphElementImpl;

public class EdgeTracker extends Tracker{

	/**
	 * Create a new tracker with a ByteBuffer size of 64 Bytes.
	 */
	public EdgeTracker(){
		super(EDGE_BASE_SIZE);
	}
	
	
	/**
	 * Store all variables of a GraphElement in the Tracker
	 * 
	 * @param ge
	 * 		The GraphElement whose variables will be stored
	 */
	protected void storeVariables(GraphElementImpl<?,?> ge){
		EdgeImpl ed = (EdgeImpl) ge;
		//TODO Don't put typeID in the buffer, but declare it as a normal member
		int typeId = ed.getAttributedElementClass().getGraphElementClassIdInSchema();
		variables.putInt(0, (typeId + 1));
				
		putVariable(4, (long)ed.getNextEdgeId());
		putVariable(12, (long)ed.getPrevEdgeId());
		putVariable(20, (long)ed.getNextIncidenceId());
		putVariable(28, (long)ed.getPrevIncidenceId());
		putVariable(36, (long)ed.getIncidentVertexId());
		putVariable(44, (long)((ReversedEdgeImpl)ed.getReversedEdge()).getNextIncidenceId());
		putVariable(52, (long)((ReversedEdgeImpl)ed.getReversedEdge()).getPrevIncidenceId());
		//System.out.println("** store " + (long)((ReversedEdgeImpl)ed.getReversedEdge()).getIncidentVertexId());
		putVariable(60, (long)((ReversedEdgeImpl)ed.getReversedEdge()).getIncidentVertexId());
	}
}
