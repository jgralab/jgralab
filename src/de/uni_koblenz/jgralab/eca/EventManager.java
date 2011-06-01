package de.uni_koblenz.jgralab.eca;

import java.util.ArrayList;
import java.util.List;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.eca.events.ChangeAttributeEvent;
import de.uni_koblenz.jgralab.eca.events.ChangeEdgeEvent;
import de.uni_koblenz.jgralab.eca.events.CreateEdgeEvent;
import de.uni_koblenz.jgralab.eca.events.CreateVertexEvent;
import de.uni_koblenz.jgralab.eca.events.DeleteEdgeEvent;
import de.uni_koblenz.jgralab.eca.events.DeleteVertexEvent;

public class EventManager {

	/**
	 * Graph that owns this EventManager
	 */
	private Graph graph;
	
	/*
	 * CreateVertexEvents
	 */
	private List<CreateVertexEvent> beforeCreateVertexEvents;
	private List<CreateVertexEvent> afterCreateVertexEvents;
	
	/*
	 * DeleteVertexEvents
	 */
	private List<DeleteVertexEvent> beforeDeleteVertexEvents;
	private List<DeleteVertexEvent> afterDeleteVertexEvents;
	
	/*
	 * CreateEdgeEvents
	 */
	private List<CreateEdgeEvent> beforeCreateEdgeEvents;
	private List<CreateEdgeEvent> afterCreateEdgeEvents;
	
	/*
	 * DeleteEdgeEvents
	 */
	private List<DeleteEdgeEvent> beforeDeleteEdgeEvents;
	private List<DeleteEdgeEvent> afterDeleteEdgeEvents;

	/*
	 * ChangeEdgeEvents
	 */
	private List<ChangeEdgeEvent> beforeChangeEdgeEvents;
	private List<ChangeEdgeEvent> afterChangeEdgeEvents;

	/*
	 * ChangeAttributeEvents
	 */
	private List<ChangeAttributeEvent> beforeChangeAttributeEvents;
	private List<ChangeAttributeEvent> afterChangeAttributeEvents;
	
	// +++++ Constructor ++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Constructor - initializes members
	 * 
	 * @param the
	 *            Graph that owns this EventManager
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
	
	// +++++ Fire Events ++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Fire Events from {@link beforeCreateVertexEvents} list
	 * 
	 * @param elementClass
	 *            the Class of the new Vertex
	 */
	public void fireBeforeCreateVertexEvents(
			Class<? extends AttributedElement> elementClass) {
		for(CreateVertexEvent ev : beforeCreateVertexEvents){
			ev.fire(elementClass);
		}
	}

	/**
	 * Fire Events from {@link afterCreateVertexEvents} list
	 * 
	 * @param element
	 *            the new created Vertex
	 */
	public void fireAfterCreateVertexEvents(GraphElement element) {
		for(CreateVertexEvent ev : afterCreateVertexEvents){
			ev.fire(element);
		}
	}

	/**
	 * Fire Events from {@link beforeDeleteVertexEvents} list
	 * 
	 * @param element
	 *            the Vertex to delete
	 */
	public void fireBeforeDeleteVertexEvents(GraphElement element) {
		for(DeleteVertexEvent ev : beforeDeleteVertexEvents){
			ev.fire(element);
		}
	}

	/**
	 * Fire Events from {@link afterDeleteVertexEvents} list
	 * 
	 * @param elementClass
	 *            the Class of the deleted Vertex
	 */
	public void fireAfterDeleteVertexEvents(
			Class<? extends AttributedElement> elementClass) {
		for(DeleteVertexEvent ev : afterDeleteVertexEvents){
			ev.fire(elementClass);
		}
	}

	/**
	 * Fire Events from {@link beforeCreateEdgeEvents} list
	 * 
	 * @param elementClass
	 *            the Class of the new Edge
	 */
	public void fireBeforeCreateEdgeEvents(
			Class<? extends AttributedElement> elementClass) {
		for(CreateEdgeEvent ev : beforeCreateEdgeEvents){
			ev.fire(elementClass);
		}
	}
	
	/**
	 * Fire Events from {@link afterCreateEdgeEvents} list
	 * 
	 * @param element
	 *            the new created Edge
	 */
	public void fireAfterCreateEdgeEvents(GraphElement element) {
		for(CreateEdgeEvent ev : afterCreateEdgeEvents){
			ev.fire(element);
		}
	}
	
	/**
	 * Fire Events from {@link beforeDeleteEdgeEvents} list
	 * 
	 * @param element
	 *            the Edge to delete
	 */
	public void fireBeforeDeleteEdgeEvents(GraphElement element) {
		for(DeleteEdgeEvent ev : beforeDeleteEdgeEvents){
			ev.fire(element);
		}
	}
	
	/**
	 * Fire Events from {@link afterDeleteEdgeEvents} list
	 * 
	 * @param elementClass
	 *            the Class of the deleted Edge
	 */
	public void fireAfterDeleteEdgeEvents(
			Class<? extends AttributedElement> elementClass) {
		for(DeleteEdgeEvent ev : afterDeleteEdgeEvents){
			ev.fire(elementClass);
		}
	}
	
	/**
	 * Fire Events from {@link beforeChangeEdgeEvents} list
	 * 
	 * @param element
	 *            the Edge that will change
	 */
	public void fireBeforeChangeEdgeEvents(GraphElement element) {
		for(ChangeEdgeEvent ev : beforeChangeEdgeEvents){
			ev.fire(element);
		}
	}
	
	/**
	 * Fire Events from {@link afterChangeEdgeEvents} list
	 * 
	 * @param element
	 *            the Edge that changed
	 */
	public void fireAfterChangeEdgeEvents(GraphElement element) {
		for(ChangeEdgeEvent ev : afterChangeEdgeEvents){
			ev.fire(element);
		}
	}
	
	/**
	 * Fire Events from {@link beforeChangeAttributeEvents} list
	 * 
	 * @param element
	 *            the element of which the Attribute will change
	 * @param attributeName
	 *            the name of the Attribute that will change
	 */
	public void fireBeforeChangeAttributeEvents(AttributedElement element,
			String attributeName) {
		for(ChangeAttributeEvent ev : beforeChangeAttributeEvents){
			ev.fire(element, attributeName);
		}
	}
	
	/**
	 * Fire Events from {@link afterChangeAttributeEvents} list
	 * 
	 * @param element
	 *            the element of which the Attribute changed
	 * @param attributeName
	 *            the name of the changed Attribute
	 */
	public void fireAfterChangeAttributeEvents(AttributedElement element,
			String attributeName) {
		for(ChangeAttributeEvent ev : afterChangeAttributeEvents){
			ev.fire(element, attributeName);
		}
	}

	// +++++ Getter ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	/**
	 * @return the Graph that owns this EventManager
	 */
	public Graph getGraph(){
		return this.graph;
	}
	
	/**
	 * @return the List of CreateVertexEvents with EventTime BEFORE
	 */
	public List<CreateVertexEvent> getBeforeCreateVertexEvents() {
		return beforeCreateVertexEvents;
	}

	/**
	 * @return the List of CreateVertexEvents with EventTime AFTER
	 */
	public List<CreateVertexEvent> getAfterCreateVertexEvents() {
		return afterCreateVertexEvents;
	}

	/**
	 * @return the List of DeleteVertexEvents with EventTime BEFORE
	 */
	public List<DeleteVertexEvent> getBeforeDeleteVertexEvents() {
		return beforeDeleteVertexEvents;
	}

	/**
	 * @return the List of DeleteVertexEvents with EventTime AFTER
	 */
	public List<DeleteVertexEvent> getAfterDeleteVertexEvents() {
		return afterDeleteVertexEvents;
	}

	/**
	 * @return the List of CreateEdgeEvents with EventTime BEFORE
	 */
	public List<CreateEdgeEvent> getBeforeCreateEdgeEvents() {
		return beforeCreateEdgeEvents;
	}

	/**
	 * @return the List of CreateEdgeEvents with EventTime AFTER
	 */
	public List<CreateEdgeEvent> getAfterCreateEdgeEvents() {
		return afterCreateEdgeEvents;
	}

	/**
	 * @return the List of DeleteEdgeEvents with EventTime BEFORE
	 */
	public List<DeleteEdgeEvent> getBeforeDeleteEdgeEvents() {
		return beforeDeleteEdgeEvents;
	}

	/**
	 * @return the List of DeleteEdgeEvents with EventTime AFTER
	 */
	public List<DeleteEdgeEvent> getAfterDeleteEdgeEvents() {
		return afterDeleteEdgeEvents;
	}

	/**
	 * @return the List of ChangeEdgeEvents with EventTime BEFORE
	 */
	public List<ChangeEdgeEvent> getBeforeChangeEdgeEvents() {
		return beforeChangeEdgeEvents;
	}

	/**
	 * @return the List of ChangeEdgeEvents with EventTime AFTER
	 */
	public List<ChangeEdgeEvent> getAfterChangeEdgeEvents() {
		return afterChangeEdgeEvents;
	}

	/**
	 * @return the List of ChangeAttributeEvents with EventTime BEFORE
	 */
	public List<ChangeAttributeEvent> getBeforeChangeAttributeEvents() {
		return beforeChangeAttributeEvents;
	}

	/**
	 * @return the List of ChangeAttributeEvents with EventTime AFTER
	 */
	public List<ChangeAttributeEvent> getAfterChangeAttributeEvents() {
		return afterChangeAttributeEvents;
	}
	
}
