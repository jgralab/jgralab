package de.uni_koblenz.jgralab.eca;

import java.util.ArrayList;
import java.util.List;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.eca.events.ChangeAttributeEvent;
import de.uni_koblenz.jgralab.eca.events.ChangeEdgeEvent;
import de.uni_koblenz.jgralab.eca.events.CreateEdgeEvent;
import de.uni_koblenz.jgralab.eca.events.CreateVertexEvent;
import de.uni_koblenz.jgralab.eca.events.DeleteEdgeEvent;
import de.uni_koblenz.jgralab.eca.events.DeleteVertexEvent;
import de.uni_koblenz.jgralab.eca.events.Event;

public class ECARuleManager {

	/**
	 * Graph that owns this ECARuleManager
	 */
	private Graph graph;
	
	/**
	 * List with all ECARules managed by this ECARuleManager
	 */
	private List<ECARule> rules;

	private int nestedTriggerCalls = 0;
	private int maxNestedTriggerCalls = 30;
	private boolean blocked = false;

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
	 *            Graph that owns this ECARuleManager
	 */
	public ECARuleManager(Graph graph){
		
		this.graph = graph;
		
		this.rules = new ArrayList<ECARule>();

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
		if (this.increaseAndTestOnMaximumNestedCalls()) {
			return;
		}
		for(CreateVertexEvent ev : beforeCreateVertexEvents){
			ev.fire(elementClass);
		}
		this.nestedTriggerCalls--;

	}

	/**
	 * Fire Events from {@link afterCreateVertexEvents} list
	 * 
	 * @param element
	 *            the new created Vertex
	 */
	public void fireAfterCreateVertexEvents(GraphElement element) {
		if (this.increaseAndTestOnMaximumNestedCalls()) {
			return;
		}
		for(CreateVertexEvent ev : afterCreateVertexEvents){
			ev.fire(element);
		}
		this.nestedTriggerCalls--;

	}

	/**
	 * Fire Events from {@link beforeDeleteVertexEvents} list
	 * 
	 * @param element
	 *            the Vertex to delete
	 */
	public void fireBeforeDeleteVertexEvents(GraphElement element) {
		if (this.increaseAndTestOnMaximumNestedCalls()) {
			return;
		}
		for(DeleteVertexEvent ev : beforeDeleteVertexEvents){
			ev.fire(element);
		}
		this.nestedTriggerCalls--;

	}

	/**
	 * Fire Events from {@link afterDeleteVertexEvents} list
	 * 
	 * @param elementClass
	 *            the Class of the deleted Vertex
	 */
	public void fireAfterDeleteVertexEvents(
			Class<? extends AttributedElement> elementClass) {
		if (this.increaseAndTestOnMaximumNestedCalls()) {
			return;
		}
		for(DeleteVertexEvent ev : afterDeleteVertexEvents){
			ev.fire(elementClass);
		}
		this.nestedTriggerCalls--;

	}

	/**
	 * Fire Events from {@link beforeCreateEdgeEvents} list
	 * 
	 * @param elementClass
	 *            the Class of the new Edge
	 */
	public void fireBeforeCreateEdgeEvents(
			Class<? extends AttributedElement> elementClass) {
		if (this.increaseAndTestOnMaximumNestedCalls()) {
			return;
		}

		for(CreateEdgeEvent ev : beforeCreateEdgeEvents){
			ev.fire(elementClass);
		}
		this.nestedTriggerCalls--;

	}
	
	/**
	 * Fire Events from {@link afterCreateEdgeEvents} list
	 * 
	 * @param element
	 *            the new created Edge
	 */
	public void fireAfterCreateEdgeEvents(GraphElement element) {
		if (this.increaseAndTestOnMaximumNestedCalls()) {
			return;
		}

		for(CreateEdgeEvent ev : afterCreateEdgeEvents){
			ev.fire(element);
		}
		this.nestedTriggerCalls--;

	}
	
	/**
	 * Fire Events from {@link beforeDeleteEdgeEvents} list
	 * 
	 * @param element
	 *            the Edge to delete
	 */
	public void fireBeforeDeleteEdgeEvents(GraphElement element) {
		if (this.increaseAndTestOnMaximumNestedCalls()) {
			return;
		}

		for(DeleteEdgeEvent ev : beforeDeleteEdgeEvents){
			ev.fire(element);
		}
		this.nestedTriggerCalls--;

	}
	
	/**
	 * Fire Events from {@link afterDeleteEdgeEvents} list
	 * 
	 * @param elementClass
	 *            the Class of the deleted Edge
	 */
	public void fireAfterDeleteEdgeEvents(
			Class<? extends AttributedElement> elementClass) {
		if (this.increaseAndTestOnMaximumNestedCalls()) {
			return;
		}

		for(DeleteEdgeEvent ev : afterDeleteEdgeEvents){
			ev.fire(elementClass);
		}
		this.nestedTriggerCalls--;

	}
	
	/**
	 * Fire Events from {@link beforeChangeEdgeEvents} list
	 * 
	 * @param element
	 *            the Edge that will change
	 */
	public void fireBeforeChangeEdgeEvents(GraphElement element,
			Vertex oldVertex, Vertex newVertex) {
		if (this.increaseAndTestOnMaximumNestedCalls()) {
			return;
		}

		for(ChangeEdgeEvent ev : beforeChangeEdgeEvents){
			ev.fire(element, oldVertex, newVertex);
		}
		this.nestedTriggerCalls--;

	}
	
	/**
	 * Fire Events from {@link afterChangeEdgeEvents} list
	 * 
	 * @param element
	 *            the Edge that changed
	 */
	public void fireAfterChangeEdgeEvents(GraphElement element,
			Vertex oldVertex, Vertex newVertex) {
		if (this.increaseAndTestOnMaximumNestedCalls()) {
			return;
		}

		for(ChangeEdgeEvent ev : afterChangeEdgeEvents){
			ev.fire(element, oldVertex, newVertex);
		}
		this.nestedTriggerCalls--;

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
			String attributeName, Object oldValue, Object newValue) {
		if (this.increaseAndTestOnMaximumNestedCalls()) {
			return;
		}

		for(ChangeAttributeEvent ev : beforeChangeAttributeEvents){
			ev.fire(element, attributeName, oldValue, newValue);
		}
		this.nestedTriggerCalls--;

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
			String attributeName, Object oldValue, Object newValue) {
		if (this.increaseAndTestOnMaximumNestedCalls()) {
			return;
		}

		for(ChangeAttributeEvent ev : afterChangeAttributeEvents){
			ev.fire(element, attributeName, oldValue, newValue);
		}
		this.nestedTriggerCalls--;
	}

	private boolean increaseAndTestOnMaximumNestedCalls() {
		if (this.nestedTriggerCalls == 0) {
			this.blocked = false;
		}
		if (this.blocked) {
			return true;
		}
		this.nestedTriggerCalls++;
		if (this.nestedTriggerCalls >= this.maxNestedTriggerCalls) {
			this.blocked = true;
			System.err
					.println("CAUTION: Maximum nested Trigger Calls arrived, Rule evaluation aborted. Stack will become cleaned up.");
			this.nestedTriggerCalls--;
			return true;
		}
		return false;
	}

	// +++++ Getter and Setter ++++++++++++++++++++++++++++++++++++++
	
	/**
	 * @return the Graph that owns this EventManager
	 */
	public Graph getGraph(){
		return this.graph;
	}

	/**
	 * Getter for managed ECARules - WARNING! Don't use this method to add or
	 * delete rules! Use {@link addECARule} and {@link deleteECARule} instead!
	 * 
	 * @return the List of ECARules managed by this ECARuleManager
	 */
	public List<ECARule> getRules() {
		return rules;
	}

	public int getMaxNestedTriggerCalls() {
		return maxNestedTriggerCalls;
	}

	public void setMaxNestedTriggerCalls(int maxNestedTriggerCalls) {
		this.maxNestedTriggerCalls = maxNestedTriggerCalls;
	}

	// +++++ Add and delete rules ++++++++++++++++++++++++++++++++++++++++++++++


	public int getNestedTriggerCalls() {
		return nestedTriggerCalls;
	}

	/**
	 * Adds an ECARule to this ECARuleManager, throws a Runtime Exception if the
	 * ECARule has already an ECARuleManager
	 * 
	 * @param rule
	 *            the ECARule to add
	 */
	public void addECARule(ECARule rule) {
		if(rule.getECARuleManager()!=null){
			throw new RuntimeException(
					"ERROR: Tried to add an ECARule to an ECARulemanager,"
							+ " but the ECARule has already a manager.");
		}
		Event ev = rule.getEvent();
		for (ECARule temprule : ev.getActiveECARules()) {
			if (temprule.getECARuleManager() != this) {
				throw new RuntimeException(
						"ERROR: Tried to add an ECARule to an ECARulemanager,"
								+ " but the Event part monitors already another Graph.");
			}
		}
		this.rules.add(rule);
		rule.setECARuleManager(this);
		ev.getActiveECARules().add(rule);
		if (ev instanceof CreateVertexEvent) {
			this.addEventToList((CreateVertexEvent) ev);
		}
		if (ev instanceof DeleteVertexEvent) {
			this.addEventToList((DeleteVertexEvent) ev);
		}
		if (ev instanceof CreateEdgeEvent) {
			this.addEventToList((CreateEdgeEvent) ev);
		}
		if (ev instanceof DeleteEdgeEvent) {
			this.addEventToList((DeleteEdgeEvent) ev);
		}
		if (ev instanceof ChangeEdgeEvent) {
			this.addEventToList((ChangeEdgeEvent) ev);
		}
		if (ev instanceof ChangeAttributeEvent) {
			this.addEventToList((ChangeAttributeEvent) ev);
		}
	}

	/**
	 * Deletes a ECARule from this ECARuleManager
	 * 
	 * @param rule
	 *            the ECARule to delete
	 */
	public void deleteECARule(ECARule rule) {
		this.rules.remove(rule);
		rule.setECARuleManager(null);
		Event ev = rule.getEvent();
		ev.getActiveECARules().remove(rule);
		if (ev.getActiveECARules().isEmpty()) {
			if (ev instanceof CreateVertexEvent) {
				removeEventFromList((CreateVertexEvent) ev);
			}
			if (ev instanceof DeleteVertexEvent) {
				removeEventFromList((DeleteVertexEvent) ev);
			}
			if (ev instanceof CreateEdgeEvent) {
				removeEventFromList((CreateEdgeEvent) ev);
			}
			if (ev instanceof DeleteEdgeEvent) {
				removeEventFromList((DeleteEdgeEvent) ev);
			}
			if (ev instanceof ChangeEdgeEvent) {
				removeEventFromList((ChangeEdgeEvent) ev);
			}
			if (ev instanceof ChangeAttributeEvent) {
				removeEventFromList((ChangeAttributeEvent) ev);
			}
		}
	}

	// +++++ Add and Delete Events to the Lists

	/**
	 * Adds an CreateVertexEvent to the {@link beforeCreateVertexEvents} or
	 * {@link afterCreateVertexEvents} list depending on its EventTime property,
	 * if it is not already contained
	 * 
	 * @param e
	 *            the CreateVertexEvent to add
	 */
	private void addEventToList(CreateVertexEvent e) {
		if (e.getTime().equals(Event.EventTime.BEFORE)) {
			if (!this.beforeCreateVertexEvents.contains(e)) {
				this.beforeCreateVertexEvents.add(e);
			}
		} else {
			if (!this.afterCreateVertexEvents.contains(e)) {
				this.afterCreateVertexEvents.add(e);
			}
		}
	}

	/**
	 * Adds an DeleteVertexEvent to the {@link beforeDeleteVertexEvents} or
	 * {@link afterDeleteVertexEvents} list depending on its EventTime property,
	 * if it is not already contained
	 * 
	 * @param e
	 *            the DeleteVertexEvent to add
	 */
	private void addEventToList(DeleteVertexEvent e) {
		if (e.getTime().equals(Event.EventTime.BEFORE)) {
			if (!this.beforeDeleteVertexEvents.contains(e)) {
				this.beforeDeleteVertexEvents.add(e);
			}
		} else {
			if (!this.afterDeleteVertexEvents.contains(e)) {
				this.afterDeleteVertexEvents.add(e);
			}
		}
	}

	/**
	 * Adds an CreateEdgeEvent to the {@link beforeCreateEdgeEvents} or
	 * {@link afterCreateEdgeEvents} list depending on its EventTime property,
	 * if it is not already contained
	 * 
	 * @param e
	 *            the CreateEdgeEvent to add
	 */
	private void addEventToList(CreateEdgeEvent e) {
		if (e.getTime().equals(Event.EventTime.BEFORE)) {
			if (!this.beforeCreateEdgeEvents.contains(e)) {
				this.beforeCreateEdgeEvents.add(e);
			}
		} else {
			if (!this.afterCreateEdgeEvents.contains(e)) {
				this.afterCreateEdgeEvents.add(e);
			}
		}
	}

	/**
	 * Adds an DeleteEdgeEvent to the {@link beforeDeleteEdgeEvents} or
	 * {@link afterDeleteEdgeEvents} list depending on its EventTime property,
	 * if it is not already contained
	 * 
	 * @param e
	 *            the DeleteEdgeEvent to add
	 */
	private void addEventToList(DeleteEdgeEvent e) {
		if (e.getTime().equals(Event.EventTime.BEFORE)) {
			if (!this.beforeDeleteEdgeEvents.contains(e)) {
				this.beforeDeleteEdgeEvents.add(e);
			}
		} else {
			if (!this.afterDeleteEdgeEvents.contains(e)) {
				this.afterDeleteEdgeEvents.add(e);
			}
		}
	}

	/**
	 * Adds an ChangeEdgeEvent to the {@link beforeChangeEdgeEvents} or
	 * {@link afterChangeEdgeEvents} list depending on its EventTime property,
	 * if it is not already contained
	 * 
	 * @param e
	 *            the ChangeEdgeEvent to add
	 */
	private void addEventToList(ChangeEdgeEvent e) {
		if (e.getTime().equals(Event.EventTime.BEFORE)) {
			if (!this.beforeChangeEdgeEvents.contains(e)) {
				this.beforeChangeEdgeEvents.add(e);
			}
		} else {
			if (!this.afterChangeEdgeEvents.contains(e)) {
				this.afterChangeEdgeEvents.add(e);
			}
		}
	}

	/**
	 * Adds an ChangeAttributeEvent to the {@link beforeChangeAttributeEvents}
	 * or {@link afterChangeAttributeEvents} list depending on its EventTime
	 * property, if it is not already contained
	 * 
	 * @param e
	 *            the ChangeAttributeEvent to add
	 */
	private void addEventToList(ChangeAttributeEvent e) {
		if (e.getTime().equals(Event.EventTime.BEFORE)) {
			if (!this.beforeChangeAttributeEvents.contains(e)) {
				this.beforeChangeAttributeEvents.add(e);
			}
		} else {
			if (!this.afterChangeAttributeEvents.contains(e)) {
				this.afterChangeAttributeEvents.add(e);
			}
		}
	}

	/**
	 * Removes an CreateVertexEvent from the {@link beforeCreateVertexEvents} or
	 * {@link afterCreateVertexEvents} list depending on its EventTime property
	 * 
	 * @param e
	 *            the CreateVertexEvent to delete
	 */
	private void removeEventFromList(CreateVertexEvent ev) {
		if (ev.getTime().equals(Event.EventTime.BEFORE)) {
			this.beforeCreateVertexEvents.remove(ev);
		} else {
			this.afterCreateVertexEvents.remove(ev);
		}
	}

	/**
	 * Removes an DeleteVertexEvent from the {@link beforeDeleteVertexEvents} or
	 * {@link afterDeleteVertexEvents} list depending on its EventTime property
	 * 
	 * @param e
	 *            the DeleteVertexEvent to delete
	 */
	private void removeEventFromList(DeleteVertexEvent ev) {
		if (ev.getTime().equals(Event.EventTime.BEFORE)) {
			this.beforeDeleteVertexEvents.remove(ev);
		} else {
			this.afterDeleteVertexEvents.remove(ev);
		}
	}

	/**
	 * Removes an CreateEdgeEvent from the {@link beforeCreateEdgeEvents} or
	 * {@link afterCreateEdgeEvents} list depending on its EventTime property
	 * 
	 * @param e
	 *            the CreateEdgeEvent to delete
	 */
	private void removeEventFromList(CreateEdgeEvent ev) {
		if (ev.getTime().equals(Event.EventTime.BEFORE)) {
			this.beforeCreateEdgeEvents.remove(ev);
		} else {
			this.afterCreateEdgeEvents.remove(ev);
		}
	}

	/**
	 * Removes an DeleteEdgeEvent from the {@link beforeDeleteEdgeEvents} or
	 * {@link afterDeleteEdgeEvents} list depending on its EventTime property
	 * 
	 * @param e
	 *            the DeleteEdgeEvent to delete
	 */
	private void removeEventFromList(DeleteEdgeEvent ev) {
		if (ev.getTime().equals(Event.EventTime.BEFORE)) {
			this.beforeDeleteEdgeEvents.remove(ev);
		} else {
			this.afterDeleteEdgeEvents.remove(ev);
		}
	}

	/**
	 * Removes an ChangeEdgeEvent from the {@link beforeChangeEdgeEvents} or
	 * {@link afterChangeEdgeEvents} list depending on its EventTime property
	 * 
	 * @param e
	 *            the ChangeEdgeEvent to delete
	 */
	private void removeEventFromList(ChangeEdgeEvent ev) {
		if (ev.getTime().equals(Event.EventTime.BEFORE)) {
			this.beforeChangeEdgeEvents.remove(ev);
		} else {
			this.afterChangeEdgeEvents.remove(ev);
		}
	}

	/**
	 * Removes an ChangeAttributeEvent from the
	 * {@link beforeChangeAttributeEvents} or {@link afterChangeAttributeEvents}
	 * list depending on its EventTime property
	 * 
	 * @param e
	 *            the ChangeAttributeEvent to delete
	 */
	private void removeEventFromList(ChangeAttributeEvent ev) {
		if (ev.getTime().equals(Event.EventTime.BEFORE)) {
			this.beforeChangeAttributeEvents.remove(ev);
		} else {
			this.afterChangeAttributeEvents.remove(ev);
		}
	}

}
