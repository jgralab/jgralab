package de.uni_koblenz.jgralab.eca;

import java.util.ArrayList;
import java.util.List;

import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.eca.events.*;

public class EventManager {

	public List<CreateVertexEvent> beforeCreateVertexEvents;
	public List<CreateVertexEvent> afterCreateVertexEvents;
	
	public List<DeleteVertexEvent> beforeDeleteVertexEvents;
	public List<DeleteVertexEvent> afterDeleteVertexEvents;
	
	public List<CreateEdgeEvent> beforeCreateEdgeEvents;
	public List<CreateEdgeEvent> afterCreateEdgeEvents;
	
	public List<DeleteEdgeEvent> beforeDeleteEdgeEvents;
	public List<DeleteEdgeEvent> afterDeleteEdgeEvents;
	
	public List<ChangeEdgeEvent> beforeChangeEdgeEvents;
	public List<ChangeEdgeEvent> afterChangeEdgeEvents;

	public List<ChangeAttributeEvent> beforeChangeAttributeEvents;
	public List<ChangeAttributeEvent> afterChangeAttributeEvents;
	
	/**
	 * Constructor - initialzes members
	 */
	public EventManager(){
		this.beforeCreateVertexEvents = new ArrayList<CreateVertexEvent>();
		this.afterCreateVertexEvents = new ArrayList<CreateVertexEvent>();
		
		this.beforeDeleteVertexEvents = new ArrayList<DeleteVertexEvent>();
		this.afterDeleteVertexEvents  = new ArrayList<DeleteVertexEvent>();
		
		this.beforeCreateEdgeEvents = new ArrayList<CreateEdgeEvent>();
		this.afterCreateEdgeEvents = new ArrayList<CreateEdgeEvent>();
		
		this.beforeDeleteEdgeEvents = new ArrayList<DeleteEdgeEvent>();
		this.afterDeleteEdgeEvents = new ArrayList<DeleteEdgeEvent>();
		
		this.beforeChangeEdgeEvents = new ArrayList<ChangeEdgeEvent>();
		this.afterChangeEdgeEvents = new ArrayList<ChangeEdgeEvent>();
		
		this.beforeChangeAttributeEvents = new ArrayList<ChangeAttributeEvent>();
		this.afterChangeAttributeEvents = new ArrayList<ChangeAttributeEvent>();

	}
	
	public void fireBeforeCreateVertexEvents(GraphElement e){
		for(CreateVertexEvent ev : beforeCreateVertexEvents){
			ev.fire(e);
		}
	}
	public void fireAfterCreateVertexEvents(GraphElement e){
		for(CreateVertexEvent ev : afterCreateVertexEvents){
			ev.fire(e);
		}
	}
	public void fireBeforeDeleteVertexEvents(GraphElement e){
		for(DeleteVertexEvent ev : beforeDeleteVertexEvents){
			ev.fire(e);
		}
	}
	public void fireAfterDeleteVertexEvents(GraphElement e){
		for(DeleteVertexEvent ev : afterDeleteVertexEvents){
			ev.fire(e);
		}
	}
	
	public void fireBeforeCreateEdgeEvents(GraphElement e){
		for(CreateEdgeEvent ev : beforeCreateEdgeEvents){
			ev.fire(e);
		}
	}
	
	public void fireAfterCreateEdgeEvents(GraphElement e){
		for(CreateEdgeEvent ev : afterCreateEdgeEvents){
			ev.fire(e);
		}
	}
	
	public void fireBeforeDeleteEdgeEvents(GraphElement e){
		for(DeleteEdgeEvent ev : beforeDeleteEdgeEvents){
			ev.fire(e);
		}
	}
	
	public void fireAfterDeleteEdgeEvents(GraphElement e){
		for(DeleteEdgeEvent ev : afterDeleteEdgeEvents){
			ev.fire(e);
		}
	}
	
	public void fireBeforeChangeEdgeEvents(GraphElement e){
		for(ChangeEdgeEvent ev : beforeChangeEdgeEvents){
			ev.fire(e);
		}
	}
	
	public void fireAfterChangeEdgeEvents(GraphElement e){
		for(ChangeEdgeEvent ev : afterChangeEdgeEvents){
			ev.fire(e);
		}
	}
	
	public void fireBeforeChangeAttributeEvents(GraphElement e){
		for(ChangeAttributeEvent ev : beforeChangeAttributeEvents){
			ev.fire(e);
		}
	}
	
	public void fireAfterChangeAttributeEvents(GraphElement e){
		for(ChangeAttributeEvent ev : afterChangeAttributeEvents){
			ev.fire(e);
		}
	}

	public List<CreateVertexEvent> getBeforeCreateVertexEvents() {
		return beforeCreateVertexEvents;
	}

	public List<CreateVertexEvent> getAfterCreateVertexEvents() {
		return afterCreateVertexEvents;
	}

	public List<DeleteVertexEvent> getBeforeDeleteVertexEvents() {
		return beforeDeleteVertexEvents;
	}

	public List<DeleteVertexEvent> getAfterDeleteVertexEvents() {
		return afterDeleteVertexEvents;
	}

	public List<CreateEdgeEvent> getBeforeCreateEdgeEvents() {
		return beforeCreateEdgeEvents;
	}

	public List<CreateEdgeEvent> getAfterCreateEdgeEvents() {
		return afterCreateEdgeEvents;
	}

	public List<DeleteEdgeEvent> getBeforeDeleteEdgeEvents() {
		return beforeDeleteEdgeEvents;
	}

	public List<DeleteEdgeEvent> getAfterDeleteEdgeEvents() {
		return afterDeleteEdgeEvents;
	}

	public List<ChangeEdgeEvent> getBeforeChangeEdgeEvents() {
		return beforeChangeEdgeEvents;
	}

	public List<ChangeEdgeEvent> getAfterChangeEdgeEvents() {
		return afterChangeEdgeEvents;
	}

	public List<ChangeAttributeEvent> getBeforeChangeAttributeEvents() {
		return beforeChangeAttributeEvents;
	}

	public List<ChangeAttributeEvent> getAfterChangeAttributeEvents() {
		return afterChangeAttributeEvents;
	}
	
}
