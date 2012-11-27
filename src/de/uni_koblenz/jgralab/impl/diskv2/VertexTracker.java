package de.uni_koblenz.jgralab.impl.diskv2;

import de.uni_koblenz.jgralab.impl.GraphElementImpl;


public class VertexTracker extends Tracker{
	
	
	/**
	 * Create a new tracker with a ByteBuffer size of 64 Bytes.
	 */
	public VertexTracker(){
		super(VERTEX_BASE_SIZE);
	}
	
	
	/**
	 * Store all variables of a GraphElement in the Tracker
	 * 
	 * @param ge
	 * 		The GraphElement whose variables will be stored
	 */
	protected void storeVariables(GraphElementImpl<?,?> ge){
		VertexImpl ve = (VertexImpl) ge;
		//TODO Don't put typeID in the buffer, but declare it as a normal member
		int typeId = ve.getAttributedElementClass().getGraphElementClassIdInSchema();
		variables.putInt(0, (typeId + 1));
				
		putVariable(4, ve.getNextVertexId());
		putVariable(12, ve.getPrevVertexId());
		putVariable(20, ve.getFirstIncidenceId());
		putVariable(28, ve.getLastIncidenceId());
		putVariable(36, ve.getIncidenceListVersion());	
	}
	
}