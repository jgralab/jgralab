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

	public static ECARuleManager getECARuleManagerForGraph(Graph g) {
		return (ECARuleManager) g.getECARuleManager();
	}

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
	public ECARuleManager(Graph graph) {

		this.graph = graph;

		rules = new ArrayList<ECARule>();

		greqlEvaluator = new GreqlEvaluator("", this.graph, null);

		beforeCreateVertexEvents = new ArrayList<CreateVertexEventDescription>();
		afterCreateVertexEvents = new ArrayList<CreateVertexEventDescription>();

		beforeDeleteVertexEvents = new ArrayList<DeleteVertexEventDescription>();
		afterDeleteVertexEvents = new ArrayList<DeleteVertexEventDescription>();

		beforeCreateEdgeEvents = new ArrayList<CreateEdgeEventDescription>();
		afterCreateEdgeEvents = new ArrayList<CreateEdgeEventDescription>();

		beforeDeleteEdgeEvents = new ArrayList<DeleteEdgeEventDescription>();
		afterDeleteEdgeEvents = new ArrayList<DeleteEdgeEventDescription>();

		beforeChangeAlphaOfEdgeEvents = new ArrayList<ChangeEdgeEventDescription>();
		afterChangeAlphaOfEdgeEvents = new ArrayList<ChangeEdgeEventDescription>();
		beforeChangeOmegaOfEdgeEvents = new ArrayList<ChangeEdgeEventDescription>();
		afterChangeOmegaOfEdgeEvents = new ArrayList<ChangeEdgeEventDescription>();

		beforeChangeAttributeEvents = new ArrayList<ChangeAttributeEventDescription>();
		afterChangeAttributeEvents = new ArrayList<ChangeAttributeEventDescription>();

	}

	// +++++ Fire Events ++++++++++++++++++++++++++++++++++++++++++++++

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.eca.ECARuleManagerInterface#
	 * fireBeforeCreateVertexEvents(java.lang.Class)
	 */
	@Override
	public void fireBeforeCreateVertexEvents(
			Class<? extends AttributedElement> elementClass) {
		if (increaseAndTestOnMaximumNestedCalls()) {
			return;
		}
		for (CreateVertexEventDescription ev : beforeCreateVertexEvents) {
			ev.fire(elementClass);
		}
		nestedTriggerCalls--;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.eca.ECARuleManagerInterface#
	 * fireAfterCreateVertexEvents(de.uni_koblenz.jgralab.GraphElement)
	 */
	@Override
	public void fireAfterCreateVertexEvents(GraphElement element) {
		if (increaseAndTestOnMaximumNestedCalls()) {
			return;
		}
		for (CreateVertexEventDescription ev : afterCreateVertexEvents) {
			ev.fire(element);
		}
		nestedTriggerCalls--;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.eca.ECARuleManagerInterface#
	 * fireBeforeDeleteVertexEvents(de.uni_koblenz.jgralab.GraphElement)
	 */
	@Override
	public void fireBeforeDeleteVertexEvents(GraphElement element) {
		if (increaseAndTestOnMaximumNestedCalls()) {
			return;
		}
		for (DeleteVertexEventDescription ev : beforeDeleteVertexEvents) {
			ev.fire(element);
		}
		nestedTriggerCalls--;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.eca.ECARuleManagerInterface#
	 * fireAfterDeleteVertexEvents(java.lang.Class)
	 */
	@Override
	public void fireAfterDeleteVertexEvents(
			Class<? extends AttributedElement> elementClass) {
		if (increaseAndTestOnMaximumNestedCalls()) {
			return;
		}
		for (DeleteVertexEventDescription ev : afterDeleteVertexEvents) {
			ev.fire(elementClass);
		}
		nestedTriggerCalls--;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.eca.ECARuleManagerInterface#fireBeforeCreateEdgeEvents
	 * (java.lang.Class)
	 */
	@Override
	public void fireBeforeCreateEdgeEvents(
			Class<? extends AttributedElement> elementClass) {
		if (increaseAndTestOnMaximumNestedCalls()) {
			return;
		}

		for (CreateEdgeEventDescription ev : beforeCreateEdgeEvents) {
			ev.fire(elementClass);
		}
		nestedTriggerCalls--;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.eca.ECARuleManagerInterface#fireAfterCreateEdgeEvents
	 * (de.uni_koblenz.jgralab.GraphElement)
	 */
	@Override
	public void fireAfterCreateEdgeEvents(GraphElement element) {
		if (increaseAndTestOnMaximumNestedCalls()) {
			return;
		}

		for (CreateEdgeEventDescription ev : afterCreateEdgeEvents) {
			ev.fire(element);
		}
		nestedTriggerCalls--;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.eca.ECARuleManagerInterface#fireBeforeDeleteEdgeEvents
	 * (de.uni_koblenz.jgralab.GraphElement)
	 */
	@Override
	public void fireBeforeDeleteEdgeEvents(GraphElement element) {
		if (increaseAndTestOnMaximumNestedCalls()) {
			return;
		}

		for (DeleteEdgeEventDescription ev : beforeDeleteEdgeEvents) {
			ev.fire(element);
		}
		nestedTriggerCalls--;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.eca.ECARuleManagerInterface#fireAfterDeleteEdgeEvents
	 * (java.lang.Class)
	 */
	@Override
	public void fireAfterDeleteEdgeEvents(
			Class<? extends AttributedElement> elementClass) {
		if (increaseAndTestOnMaximumNestedCalls()) {
			return;
		}

		for (DeleteEdgeEventDescription ev : afterDeleteEdgeEvents) {
			ev.fire(elementClass);
		}
		nestedTriggerCalls--;

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
		if (increaseAndTestOnMaximumNestedCalls()) {
			return;
		}

		for (ChangeEdgeEventDescription ev : beforeChangeAlphaOfEdgeEvents) {
			ev.fire(element, oldVertex, newVertex, EdgeEnd.ALPHA);
		}
		nestedTriggerCalls--;

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
		if (increaseAndTestOnMaximumNestedCalls()) {
			return;
		}

		for (ChangeEdgeEventDescription ev : afterChangeAlphaOfEdgeEvents) {
			ev.fire(element, oldVertex, newVertex, EdgeEnd.ALPHA);
		}
		nestedTriggerCalls--;

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
		if (increaseAndTestOnMaximumNestedCalls()) {
			return;
		}

		for (ChangeEdgeEventDescription ev : beforeChangeOmegaOfEdgeEvents) {
			ev.fire(element, oldVertex, newVertex, EdgeEnd.OMEGA);
		}
		nestedTriggerCalls--;

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
		if (increaseAndTestOnMaximumNestedCalls()) {
			return;
		}

		for (ChangeEdgeEventDescription ev : afterChangeOmegaOfEdgeEvents) {
			ev.fire(element, oldVertex, newVertex, EdgeEnd.OMEGA);
		}
		nestedTriggerCalls--;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.eca.ECARuleManagerInterface#
	 * fireBeforeChangeAttributeEvents(de.uni_koblenz.jgralab.AttributedElement,
	 * java.lang.String, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void fireBeforeChangeAttributeEvents(AttributedElement element,
			String attributeName, Object oldValue, Object newValue) {
		if (increaseAndTestOnMaximumNestedCalls()) {
			return;
		}

		for (ChangeAttributeEventDescription ev : beforeChangeAttributeEvents) {
			ev.fire(element, attributeName, oldValue, newValue);
		}
		nestedTriggerCalls--;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.eca.ECARuleManagerInterface#
	 * fireAfterChangeAttributeEvents(de.uni_koblenz.jgralab.AttributedElement,
	 * java.lang.String, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void fireAfterChangeAttributeEvents(AttributedElement element,
			String attributeName, Object oldValue, Object newValue) {
		if (increaseAndTestOnMaximumNestedCalls()) {
			return;
		}

		for (ChangeAttributeEventDescription ev : afterChangeAttributeEvents) {
			ev.fire(element, attributeName, oldValue, newValue);
		}
		nestedTriggerCalls--;
	}

	private boolean increaseAndTestOnMaximumNestedCalls() {
		if (nestedTriggerCalls == 0) {
			blocked = false;
		}
		if (blocked) {
			return true;
		}
		nestedTriggerCalls++;
		if (nestedTriggerCalls >= maxNestedTriggerCalls) {
			blocked = true;
			System.err
					.println("CAUTION: Maximum nested Trigger Calls arrived, Rule evaluation aborted. Stack will become cleaned up.");
			nestedTriggerCalls--;
			return true;
		}
		return false;
	}

	// +++++ Getter and Setter ++++++++++++++++++++++++++++++++++++++

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.eca.ECARuleManagerInterface#getGraph()
	 */
	@Override
	public Graph getGraph() {
		return graph;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.eca.ECARuleManagerInterface#getMaxNestedTriggerCalls
	 * ()
	 */
	@Override
	public int getMaxNestedTriggerCalls() {
		return maxNestedTriggerCalls;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.eca.ECARuleManagerInterface#setMaxNestedTriggerCalls
	 * (int)
	 */
	@Override
	public void setMaxNestedTriggerCalls(int maxNestedTriggerCalls) {
		this.maxNestedTriggerCalls = maxNestedTriggerCalls;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.eca.ECARuleManagerInterface#getNestedTriggerCalls
	 * ()
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
	public void addECARule(EventDescription event, Condition condition,
			Action action) {
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
		if (rule.getECARuleManager() != null) {
			throw new ECAException(
					"ERROR: Tried to add an ECARule to an ECARulemanager,"
							+ " but the ECARule has already a manager.");
		}
		EventDescription ev = rule.getEventDescription();
		for (ECARule temprule : ev.getActiveECARules()) {
			if (temprule.getECARuleManager() != this) {
				throw new ECAException(
						"ERROR: Tried to add an ECARule to an ECARulemanager,"
								+ " but the Event part monitors already another Graph.");
			}
		}
		rules.add(rule);
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
		rules.remove(rule);
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
			if (!beforeCreateVertexEvents.contains(e)) {
				beforeCreateVertexEvents.add(e);
			}
		} else {
			if (!afterCreateVertexEvents.contains(e)) {
				afterCreateVertexEvents.add(e);
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
			if (!beforeDeleteVertexEvents.contains(e)) {
				beforeDeleteVertexEvents.add(e);
			}
		} else {
			if (!afterDeleteVertexEvents.contains(e)) {
				afterDeleteVertexEvents.add(e);
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
			if (!beforeCreateEdgeEvents.contains(e)) {
				beforeCreateEdgeEvents.add(e);
			}
		} else {
			if (!afterCreateEdgeEvents.contains(e)) {
				afterCreateEdgeEvents.add(e);
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
			if (!beforeDeleteEdgeEvents.contains(e)) {
				beforeDeleteEdgeEvents.add(e);
			}
		} else {
			if (!afterDeleteEdgeEvents.contains(e)) {
				afterDeleteEdgeEvents.add(e);
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
				if (!beforeChangeAlphaOfEdgeEvents.contains(e)) {
					beforeChangeAlphaOfEdgeEvents.add(e);
				}
			} else if (e.getEdgeEnd().equals(EdgeEnd.OMEGA)) {
				if (!beforeChangeOmegaOfEdgeEvents.contains(e)) {
					beforeChangeOmegaOfEdgeEvents.add(e);
				}
			} else /* BOTH */{
				if (!beforeChangeAlphaOfEdgeEvents.contains(e)) {
					beforeChangeAlphaOfEdgeEvents.add(e);
				}
				if (!beforeChangeOmegaOfEdgeEvents.contains(e)) {
					beforeChangeOmegaOfEdgeEvents.add(e);
				}
			}
		} else {
			if (e.getEdgeEnd().equals(EdgeEnd.ALPHA)) {
				if (!afterChangeAlphaOfEdgeEvents.contains(e)) {
					afterChangeAlphaOfEdgeEvents.add(e);
				}
			} else if (e.getEdgeEnd().equals(EdgeEnd.OMEGA)) {
				if (!afterChangeOmegaOfEdgeEvents.contains(e)) {
					afterChangeOmegaOfEdgeEvents.add(e);
				}
			} else /* BOTH */{
				if (!afterChangeAlphaOfEdgeEvents.contains(e)) {
					afterChangeAlphaOfEdgeEvents.add(e);
				}
				if (!afterChangeOmegaOfEdgeEvents.contains(e)) {
					afterChangeOmegaOfEdgeEvents.add(e);
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
			if (!beforeChangeAttributeEvents.contains(e)) {
				beforeChangeAttributeEvents.add(e);
			}
		} else {
			if (!afterChangeAttributeEvents.contains(e)) {
				afterChangeAttributeEvents.add(e);
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
			beforeCreateVertexEvents.remove(ev);
		} else {
			afterCreateVertexEvents.remove(ev);
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
			beforeDeleteVertexEvents.remove(ev);
		} else {
			afterDeleteVertexEvents.remove(ev);
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
			beforeCreateEdgeEvents.remove(ev);
		} else {
			afterCreateEdgeEvents.remove(ev);
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
			beforeDeleteEdgeEvents.remove(ev);
		} else {
			afterDeleteEdgeEvents.remove(ev);
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
			beforeChangeAlphaOfEdgeEvents.remove(ev);
			beforeChangeOmegaOfEdgeEvents.remove(ev);
		} else /* AFTER */{
			afterChangeAlphaOfEdgeEvents.remove(ev);
			afterChangeOmegaOfEdgeEvents.remove(ev);
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
			beforeChangeAttributeEvents.remove(ev);
		} else {
			afterChangeAttributeEvents.remove(ev);
		}
	}

}
