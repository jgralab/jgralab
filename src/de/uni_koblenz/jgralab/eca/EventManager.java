package de.uni_koblenz.jgralab.eca;

import java.util.ArrayList;
import java.util.List;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.eca.events.*;

public class EventManager {

	private Graph graph;
	
	private List<CreateVertexEvent> beforeCreateVertexEvents;
	private List<CreateVertexEvent> afterCreateVertexEvents;
	
	private List<DeleteVertexEvent> beforeDeleteVertexEvents;
	private List<DeleteVertexEvent> afterDeleteVertexEvents;
	
	private List<CreateEdgeEvent> beforeCreateEdgeEvents;
	private List<CreateEdgeEvent> afterCreateEdgeEvents;
	
	private List<DeleteEdgeEvent> beforeDeleteEdgeEvents;
	private List<DeleteEdgeEvent> afterDeleteEdgeEvents;
	
	private List<ChangeEdgeEvent> beforeChangeEdgeEvents;
	private List<ChangeEdgeEvent> afterChangeEdgeEvents;

	private List<ChangeAttributeEvent> beforeChangeAttributeEvents;
	private List<ChangeAttributeEvent> afterChangeAttributeEvents;
	
	/**
	 * Constructor - initialzes members
	 */
	public EventManager(Graph graph){
		
		this.graph = graph;
		
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
	
	public void fireBeforeCreateVertexEvents(Class<? extends Vertex> vertexClass){
		for(CreateVertexEvent ev : beforeCreateVertexEvents){
			ev.fire(vertexClass);
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
	public void fireAfterDeleteVertexEvents(Class <? extends AttributedElement> atClass){
		for(DeleteVertexEvent ev : afterDeleteVertexEvents){
			ev.fire(atClass);
		}
	}
	
	public void fireBeforeCreateEdgeEvents(Class<? extends Edge> edgeClass){
		for(CreateEdgeEvent ev : beforeCreateEdgeEvents){
			ev.fire(edgeClass);
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
	
	public void fireAfterDeleteEdgeEvents(Class <? extends AttributedElement> atClass){
		for(DeleteEdgeEvent ev : afterDeleteEdgeEvents){
			ev.fire(atClass);
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
	
	public void fireBeforeChangeAttributeEvents(AttributedElement e, String attributeName){
		for(ChangeAttributeEvent ev : beforeChangeAttributeEvents){
			ev.fire(e, attributeName);
		}
	}
	
	public void fireAfterChangeAttributeEvents(AttributedElement e, String attributeName){
		for(ChangeAttributeEvent ev : afterChangeAttributeEvents){
			ev.fire(e,attributeName);
		}
	}

	//Getter und Setter
	
	public Graph getGraph(){
		return this.graph;
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
