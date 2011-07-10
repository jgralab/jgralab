package de.uni_koblenz.jgralab.eca;

import java.util.ArrayList;
import java.util.List;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.eca.events.ChangeAttributeEventDescription;
import de.uni_koblenz.jgralab.eca.events.ChangeEdgeEventDescription;
import de.uni_koblenz.jgralab.eca.events.ChangeEdgeEventDescription.EdgeEnd;
import de.uni_koblenz.jgralab.eca.events.CreateEdgeEventDescription;
import de.uni_koblenz.jgralab.eca.events.CreateVertexEventDescription;
import de.uni_koblenz.jgralab.eca.events.DeleteEdgeEventDescription;
import de.uni_koblenz.jgralab.eca.events.DeleteVertexEventDescription;
import de.uni_koblenz.jgralab.eca.events.EventDescription;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;

public class ECARuleManager implements ECARuleManagerInterface {

	/**
	 * Graph that owns this ECARuleManager
	 */
	private Graph graph;
	
	/**
	 * List with all ECARules managed by this ECARuleManager
	 */
	private List<ECARule> rules;

	private GreqlEvaluator greqlEvaluator;

	private int nestedTriggerCalls = 0;
	private int maxNestedTriggerCalls = 30;
	private boolean blocked = false;

	/*
	 * CreateVertexEvents
	 */
	private List<CreateVertexEventDescription> beforeCreateVertexEvents;
	private List<CreateVertexEventDescription> afterCreateVertexEvents;
	
	/*
	 * DeleteVertexEvents
	 */
	private List<DeleteVertexEventDescription> beforeDeleteVertexEvents;
	private List<DeleteVertexEventDescription> afterDeleteVertexEvents;
	
	/*
	 * CreateEdgeEvents
	 */
	private List<CreateEdgeEventDescription> beforeCreateEdgeEvents;
	private List<CreateEdgeEventDescription> afterCreateEdgeEvents;
	
	/*
	 * DeleteEdgeEvents
	 */
	private List<DeleteEdgeEventDescription> beforeDeleteEdgeEvents;
	private List<DeleteEdgeEventDescription> afterDeleteEdgeEvents;

	/*
	 * ChangeEdgeEvents
	 */
	private List<ChangeEdgeEventDescription> beforeChangeAlphaOfEdgeEvents;
	private List<ChangeEdgeEventDescription> afterChangeAlphaOfEdgeEvents;
	private List<ChangeEdgeEventDescription> beforeChangeOmegaOfEdgeEvents;
	private List<ChangeEdgeEventDescription> afterChangeOmegaOfEdgeEvents;

	/*
	 * ChangeAttributeEvents
	 */
	private List<ChangeAttributeEventDescription> beforeChangeAttributeEvents;
	private List<ChangeAttributeEventDescription> afterChangeAttributeEvents;
	
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

		this.greqlEvaluator = new GreqlEvaluator("", this.graph, null);

		this.beforeCreateVertexEvents = new ArrayList<CreateVertexEventDescription>();
		this.afterCreateVertexEvents = new ArrayList<CreateVertexEventDescription>();
		
		this.beforeDeleteVertexEvents = new ArrayList<DeleteVertexEventDescription>();
		this.afterDeleteVertexEvents  = new ArrayList<DeleteVertexEventDescription>();
		
		this.beforeCreateEdgeEvents = new ArrayList<CreateEdgeEventDescription>();
		this.afterCreateEdgeEvents = new ArrayList<CreateEdgeEventDescription>();
		
		this.beforeDeleteEdgeEvents = new ArrayList<DeleteEdgeEventDescription>();
		this.afterDeleteEdgeEvents = new ArrayList<DeleteEdgeEventDescription>();
		
		this.beforeChangeAlphaOfEdgeEvents = new ArrayList<ChangeEdgeEventDescription>();
		this.afterChangeAlphaOfEdgeEvents = new ArrayList<ChangeEdgeEventDescription>();
		this.beforeChangeOmegaOfEdgeEvents = new ArrayList<ChangeEdgeEventDescription>();
		this.afterChangeOmegaOfEdgeEvents = new ArrayList<ChangeEdgeEventDescription>();
		
		this.beforeChangeAttributeEvents = new ArrayList<ChangeAttributeEventDescription>();
		this.afterChangeAttributeEvents = new ArrayList<ChangeAttributeEventDescription>();

	}
	
	// +++++ Fire Events ++++++++++++++++++++++++++++++++++++++++++++++

	/* (non-Javadoc)
	 * @see de.uni_koblenz.jgralab.eca.ECARuleManagerInterface#fireBeforeCreateVertexEvents(java.lang.Class)
	 */
	@Override
	public void fireBeforeCreateVertexEvents(
			Class<? extends AttributedElement> elementClass) {
		if (this.increaseAndTestOnMaximumNestedCalls()) {
			return;
		}
		for(CreateVertexEventDescription ev : beforeCreateVertexEvents){
			ev.fire(elementClass);
		}
		this.nestedTriggerCalls--;

	}

	/* (non-Javadoc)
	 * @see de.uni_koblenz.jgralab.eca.ECARuleManagerInterface#fireAfterCreateVertexEvents(de.uni_koblenz.jgralab.GraphElement)
	 */
	@Override
	public void fireAfterCreateVertexEvents(GraphElement element) {
		if (this.increaseAndTestOnMaximumNestedCalls()) {
			return;
		}
		for(CreateVertexEventDescription ev : afterCreateVertexEvents){
			ev.fire(element);
		}
		this.nestedTriggerCalls--;

	}

	/* (non-Javadoc)
	 * @see de.uni_koblenz.jgralab.eca.ECARuleManagerInterface#fireBeforeDeleteVertexEvents(de.uni_koblenz.jgralab.GraphElement)
	 */
	@Override
	public void fireBeforeDeleteVertexEvents(GraphElement element) {
		if (this.increaseAndTestOnMaximumNestedCalls()) {
			return;
		}
		for(DeleteVertexEventDescription ev : beforeDeleteVertexEvents){
			ev.fire(element);
		}
		this.nestedTriggerCalls--;

	}

	/* (non-Javadoc)
	 * @see de.uni_koblenz.jgralab.eca.ECARuleManagerInterface#fireAfterDeleteVertexEvents(java.lang.Class)
	 */
	@Override
	public void fireAfterDeleteVertexEvents(
			Class<? extends AttributedElement> elementClass) {
		if (this.increaseAndTestOnMaximumNestedCalls()) {
			return;
		}
		for(DeleteVertexEventDescription ev : afterDeleteVertexEvents){
			ev.fire(elementClass);
		}
		this.nestedTriggerCalls--;

	}

	/* (non-Javadoc)
	 * @see de.uni_koblenz.jgralab.eca.ECARuleManagerInterface#fireBeforeCreateEdgeEvents(java.lang.Class)
	 */
	@Override
	public void fireBeforeCreateEdgeEvents(
			Class<? extends AttributedElement> elementClass) {
		if (this.increaseAndTestOnMaximumNestedCalls()) {
			return;
		}

		for(CreateEdgeEventDescription ev : beforeCreateEdgeEvents){
			ev.fire(elementClass);
		}
		this.nestedTriggerCalls--;

	}
	
	/* (non-Javadoc)
	 * @see de.uni_koblenz.jgralab.eca.ECARuleManagerInterface#fireAfterCreateEdgeEvents(de.uni_koblenz.jgralab.GraphElement)
	 */
	@Override
	public void fireAfterCreateEdgeEvents(GraphElement element) {
		if (this.increaseAndTestOnMaximumNestedCalls()) {
			return;
		}

		for(CreateEdgeEventDescription ev : afterCreateEdgeEvents){
			ev.fire(element);
		}
		this.nestedTriggerCalls--;

	}
	
	/* (non-Javadoc)
	 * @see de.uni_koblenz.jgralab.eca.ECARuleManagerInterface#fireBeforeDeleteEdgeEvents(de.uni_koblenz.jgralab.GraphElement)
	 */
	@Override
	public void fireBeforeDeleteEdgeEvents(GraphElement element) {
		if (this.increaseAndTestOnMaximumNestedCalls()) {
			return;
		}

		for(DeleteEdgeEventDescription ev : beforeDeleteEdgeEvents){
			ev.fire(element);
		}
		this.nestedTriggerCalls--;

	}
	
	/* (non-Javadoc)
	 * @see de.uni_koblenz.jgralab.eca.ECARuleManagerInterface#fireAfterDeleteEdgeEvents(java.lang.Class)
	 */
	@Override
	public void fireAfterDeleteEdgeEvents(
			Class<? extends AttributedElement> elementClass) {
		if (this.increaseAndTestOnMaximumNestedCalls()) {
			return;
		}

		for(DeleteEdgeEventDescription ev : afterDeleteEdgeEvents){
			ev.fire(elementClass);
		}
		this.nestedTriggerCalls--;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.eca.ECARuleManagerInterface#
	 * fireBeforeChangeAlphaOfEdgeEvents(de.uni_koblenz.jgralab.GraphElement,
	 * de.uni_koblenz.jgralab.Vertex, de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public void fireBeforeChangeAlphaOfEdgeEvents(GraphElement element,
			Vertex oldVertex, Vertex newVertex) {
		if (this.increaseAndTestOnMaximumNestedCalls()) {
			return;
		}

		for(ChangeEdgeEventDescription ev : beforeChangeAlphaOfEdgeEvents){
			ev.fire(element, oldVertex, newVertex, EdgeEnd.ALPHA);
		}
		this.nestedTriggerCalls--;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.eca.ECARuleManagerInterface#
	 * fireAfterChangeAlphaOfEdgeEvents(de.uni_koblenz.jgralab.GraphElement,
	 * de.uni_koblenz.jgralab.Vertex, de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public void fireAfterChangeAlphaOfEdgeEvents(GraphElement element,
			Vertex oldVertex, Vertex newVertex) {
		if (this.increaseAndTestOnMaximumNestedCalls()) {
			return;
		}

		for(ChangeEdgeEventDescription ev : afterChangeAlphaOfEdgeEvents){
			ev.fire(element, oldVertex, newVertex, EdgeEnd.ALPHA);
		}
		this.nestedTriggerCalls--;

	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.eca.ECARuleManagerInterface#
	 * fireBeforeChangeOmegaOfEdgeEvents(de.uni_koblenz.jgralab.GraphElement,
	 * de.uni_koblenz.jgralab.Vertex, de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public void fireBeforeChangeOmegaOfEdgeEvents(GraphElement element,
			Vertex oldVertex, Vertex newVertex) {
		if (this.increaseAndTestOnMaximumNestedCalls()) {
			return;
		}

		for (ChangeEdgeEventDescription ev : beforeChangeOmegaOfEdgeEvents) {
			ev.fire(element, oldVertex, newVertex, EdgeEnd.OMEGA);
		}
		this.nestedTriggerCalls--;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.eca.ECARuleManagerInterface#
	 * fireAfterChangeOmegaOfEdgeEvents(de.uni_koblenz.jgralab.GraphElement,
	 * de.uni_koblenz.jgralab.Vertex, de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public void fireAfterChangeOmegaOfEdgeEvents(GraphElement element,
			Vertex oldVertex, Vertex newVertex) {
		if (this.increaseAndTestOnMaximumNestedCalls()) {
			return;
		}

		for (ChangeEdgeEventDescription ev : afterChangeOmegaOfEdgeEvents) {
			ev.fire(element, oldVertex, newVertex, EdgeEnd.OMEGA);
		}
		this.nestedTriggerCalls--;

	}

	/* (non-Javadoc)
	 * @see de.uni_koblenz.jgralab.eca.ECARuleManagerInterface#fireBeforeChangeAttributeEvents(de.uni_koblenz.jgralab.AttributedElement, java.lang.String, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void fireBeforeChangeAttributeEvents(AttributedElement element,
			String attributeName, Object oldValue, Object newValue) {
		if (this.increaseAndTestOnMaximumNestedCalls()) {
			return;
		}

		for(ChangeAttributeEventDescription ev : beforeChangeAttributeEvents){
			ev.fire(element, attributeName, oldValue, newValue);
		}
		this.nestedTriggerCalls--;

	}
	
	/* (non-Javadoc)
	 * @see de.uni_koblenz.jgralab.eca.ECARuleManagerInterface#fireAfterChangeAttributeEvents(de.uni_koblenz.jgralab.AttributedElement, java.lang.String, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void fireAfterChangeAttributeEvents(AttributedElement element,
			String attributeName, Object oldValue, Object newValue) {
		if (this.increaseAndTestOnMaximumNestedCalls()) {
			return;
		}

		for(ChangeAttributeEventDescription ev : afterChangeAttributeEvents){
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
	
	/* (non-Javadoc)
	 * @see de.uni_koblenz.jgralab.eca.ECARuleManagerInterface#getGraph()
	 */
	@Override
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

	public GreqlEvaluator getGreqlEvaluator() {
		return greqlEvaluator;
	}

	/* (non-Javadoc)
	 * @see de.uni_koblenz.jgralab.eca.ECARuleManagerInterface#getMaxNestedTriggerCalls()
	 */
	@Override
	public int getMaxNestedTriggerCalls() {
		return maxNestedTriggerCalls;
	}

	/* (non-Javadoc)
	 * @see de.uni_koblenz.jgralab.eca.ECARuleManagerInterface#setMaxNestedTriggerCalls(int)
	 */
	@Override
	public void setMaxNestedTriggerCalls(int maxNestedTriggerCalls) {
		this.maxNestedTriggerCalls = maxNestedTriggerCalls;
	}

	/* (non-Javadoc)
	 * @see de.uni_koblenz.jgralab.eca.ECARuleManagerInterface#getNestedTriggerCalls()
	 */
	@Override
	public int getNestedTriggerCalls() {
		return nestedTriggerCalls;
	}

	// +++++ Add and delete rules ++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Adds an ECARule with the given Event and Action to this ECARuleManager,
	 * throws a Runtime Exception if the ECARule has already an ECARuleManager
	 * 
	 * @param event
	 *            Event part of Rule
	 * @param action
	 *            Action part of Rule
	 */
	public void addECARule(EventDescription event, Action action) {
		ECARule newRule = new ECARule(event, action);
		this.addECARule(newRule);

	}

	/**
	 * Adds an ECARule with the given Event, Condition and Action to this
	 * ECARuleManager, throws a Runtime Exception if the ECARule has already an
	 * ECARuleManager
	 * 
	 * @param event
	 *            Event part of Rule
	 * @param condition
	 *            Condition part of the Rule
	 * @param action
	 *            Action part of Rule
	 */
	public void addECARule(EventDescription event, Condition condition, Action action) {
		ECARule newRule = new ECARule(event, condition, action);
		this.addECARule(newRule);
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
		EventDescription ev = rule.getEventDescription();
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
		if (ev instanceof CreateVertexEventDescription) {
			this.addEventToList((CreateVertexEventDescription) ev);
		}
		if (ev instanceof DeleteVertexEventDescription) {
			this.addEventToList((DeleteVertexEventDescription) ev);
		}
		if (ev instanceof CreateEdgeEventDescription) {
			this.addEventToList((CreateEdgeEventDescription) ev);
		}
		if (ev instanceof DeleteEdgeEventDescription) {
			this.addEventToList((DeleteEdgeEventDescription) ev);
		}
		if (ev instanceof ChangeEdgeEventDescription) {
			this.addEventToList((ChangeEdgeEventDescription) ev);
		}
		if (ev instanceof ChangeAttributeEventDescription) {
			this.addEventToList((ChangeAttributeEventDescription) ev);
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
		EventDescription ev = rule.getEventDescription();
		ev.getActiveECARules().remove(rule);
		if (ev.getActiveECARules().isEmpty()) {
			if (ev instanceof CreateVertexEventDescription) {
				removeEventFromList((CreateVertexEventDescription) ev);
			}
			if (ev instanceof DeleteVertexEventDescription) {
				removeEventFromList((DeleteVertexEventDescription) ev);
			}
			if (ev instanceof CreateEdgeEventDescription) {
				removeEventFromList((CreateEdgeEventDescription) ev);
			}
			if (ev instanceof DeleteEdgeEventDescription) {
				removeEventFromList((DeleteEdgeEventDescription) ev);
			}
			if (ev instanceof ChangeEdgeEventDescription) {
				removeEventFromList((ChangeEdgeEventDescription) ev);
			}
			if (ev instanceof ChangeAttributeEventDescription) {
				removeEventFromList((ChangeAttributeEventDescription) ev);
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
	private void addEventToList(CreateVertexEventDescription e) {
		if (e.getTime().equals(EventDescription.EventTime.BEFORE)) {
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
	private void addEventToList(DeleteVertexEventDescription e) {
		if (e.getTime().equals(EventDescription.EventTime.BEFORE)) {
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
	private void addEventToList(CreateEdgeEventDescription e) {
		if (e.getTime().equals(EventDescription.EventTime.BEFORE)) {
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
	private void addEventToList(DeleteEdgeEventDescription e) {
		if (e.getTime().equals(EventDescription.EventTime.BEFORE)) {
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
	private void addEventToList(ChangeEdgeEventDescription e) {
		if (e.getTime().equals(EventDescription.EventTime.BEFORE)) {
			if (e.getEdgeEnd().equals(EdgeEnd.ALPHA)) {
				if (!this.beforeChangeAlphaOfEdgeEvents.contains(e)) {
					this.beforeChangeAlphaOfEdgeEvents.add(e);
				}
			} else if (e.getEdgeEnd().equals(EdgeEnd.OMEGA)) {
				if (!this.beforeChangeOmegaOfEdgeEvents.contains(e)) {
					this.beforeChangeOmegaOfEdgeEvents.add(e);
				}
			} else /* BOTH */{
				if (!this.beforeChangeAlphaOfEdgeEvents.contains(e)) {
					this.beforeChangeAlphaOfEdgeEvents.add(e);
				}
				if (!this.beforeChangeOmegaOfEdgeEvents.contains(e)) {
					this.beforeChangeOmegaOfEdgeEvents.add(e);
				}
			}
		} else {
			if (e.getEdgeEnd().equals(EdgeEnd.ALPHA)) {
				if (!this.afterChangeAlphaOfEdgeEvents.contains(e)) {
					this.afterChangeAlphaOfEdgeEvents.add(e);
				}
			} else if (e.getEdgeEnd().equals(EdgeEnd.OMEGA)) {
				if (!this.afterChangeOmegaOfEdgeEvents.contains(e)) {
					this.afterChangeOmegaOfEdgeEvents.add(e);
				}
			} else /* BOTH */{
				if (!this.afterChangeAlphaOfEdgeEvents.contains(e)) {
					this.afterChangeAlphaOfEdgeEvents.add(e);
				}
				if (!this.afterChangeOmegaOfEdgeEvents.contains(e)) {
					this.afterChangeOmegaOfEdgeEvents.add(e);
				}
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
	private void addEventToList(ChangeAttributeEventDescription e) {
		if (e.getTime().equals(EventDescription.EventTime.BEFORE)) {
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
	private void removeEventFromList(CreateVertexEventDescription ev) {
		if (ev.getTime().equals(EventDescription.EventTime.BEFORE)) {
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
	private void removeEventFromList(DeleteVertexEventDescription ev) {
		if (ev.getTime().equals(EventDescription.EventTime.BEFORE)) {
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
	private void removeEventFromList(CreateEdgeEventDescription ev) {
		if (ev.getTime().equals(EventDescription.EventTime.BEFORE)) {
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
	private void removeEventFromList(DeleteEdgeEventDescription ev) {
		if (ev.getTime().equals(EventDescription.EventTime.BEFORE)) {
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
	private void removeEventFromList(ChangeEdgeEventDescription ev) {
		if (ev.getTime().equals(EventDescription.EventTime.BEFORE)) {
			this.beforeChangeAlphaOfEdgeEvents.remove(ev);
			this.beforeChangeOmegaOfEdgeEvents.remove(ev);
		} else /* AFTER */{
			this.afterChangeAlphaOfEdgeEvents.remove(ev);
			this.afterChangeOmegaOfEdgeEvents.remove(ev);
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
	private void removeEventFromList(ChangeAttributeEventDescription ev) {
		if (ev.getTime().equals(EventDescription.EventTime.BEFORE)) {
			this.beforeChangeAttributeEvents.remove(ev);
		} else {
			this.afterChangeAttributeEvents.remove(ev);
		}
	}

}
