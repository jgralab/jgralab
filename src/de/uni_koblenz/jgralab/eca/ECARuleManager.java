/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
package de.uni_koblenz.jgralab.eca;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphChangeListener;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.eca.events.ChangeAttributeEventDescription;
import de.uni_koblenz.jgralab.eca.events.ChangeEdgeEventDescription;
import de.uni_koblenz.jgralab.eca.events.ChangeEdgeEventDescription.EdgeEnd;
import de.uni_koblenz.jgralab.eca.events.CreateEdgeEventDescription;
import de.uni_koblenz.jgralab.eca.events.CreateVertexEventDescription;
import de.uni_koblenz.jgralab.eca.events.DeleteEdgeEventDescription;
import de.uni_koblenz.jgralab.eca.events.DeleteVertexEventDescription;
import de.uni_koblenz.jgralab.eca.events.EventDescription;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

public class ECARuleManager implements GraphChangeListener {
	/**
	 * Graph that owns this ECARuleManager
	 */
	private Graph graph;

	/**
	 * List with all ECARules managed by this ECARuleManager
	 */
	private List<ECARule<?>> rules;

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
	private List<ChangeAttributeEventDescription<?>> beforeChangeAttributeEvents;
	private List<ChangeAttributeEventDescription<?>> afterChangeAttributeEvents;

	// +++++ Constructor ++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Constructor - initializes members
	 * 
	 * @param graph
	 *            the Graph that owns this ECARuleManager
	 */
	public ECARuleManager(Graph graph) {

		this.graph = graph;

		rules = new ArrayList<ECARule<?>>();
	}

	private void createBeforeCreateVertexEventsLazily() {
		if (beforeCreateVertexEvents == null) {
			beforeCreateVertexEvents = new ArrayList<CreateVertexEventDescription>();
		}
	}

	private void createAfterCreateVertexEventsLazily() {
		if (afterCreateVertexEvents == null) {
			afterCreateVertexEvents = new ArrayList<CreateVertexEventDescription>();
		}
	}

	private void createBeforeDeleteVertexEventsLazily() {
		if (beforeDeleteVertexEvents == null) {
			beforeDeleteVertexEvents = new ArrayList<DeleteVertexEventDescription>();
		}
	}

	private void createAfterDeleteVertexEventsLazily() {
		if (afterDeleteVertexEvents == null) {
			afterDeleteVertexEvents = new ArrayList<DeleteVertexEventDescription>();
		}
	}

	private void createBeforeCreateEdgeEventsLazily() {
		if (beforeCreateEdgeEvents == null) {
			beforeCreateEdgeEvents = new ArrayList<CreateEdgeEventDescription>();
		}
	}

	private void createAfterCreateEdgeEventsLazily() {
		if (afterCreateEdgeEvents == null) {
			afterCreateEdgeEvents = new ArrayList<CreateEdgeEventDescription>();
		}
	}

	private void createBeforeDeleteEdgeEventsLazily() {
		if (beforeDeleteEdgeEvents == null) {
			beforeDeleteEdgeEvents = new ArrayList<DeleteEdgeEventDescription>();
		}
	}

	private void createAfterDeleteEdgeEventsLazily() {
		if (afterDeleteEdgeEvents == null) {
			afterDeleteEdgeEvents = new ArrayList<DeleteEdgeEventDescription>();
		}
	}

	private void createBeforeChangeAlphaOfEdgeEventsLazily() {
		if (beforeChangeAlphaOfEdgeEvents == null) {
			beforeChangeAlphaOfEdgeEvents = new ArrayList<ChangeEdgeEventDescription>();
		}
	}

	private void createAfterChangeAlphaOfEdgeEventsLazily() {
		if (afterChangeAlphaOfEdgeEvents == null) {
			afterChangeAlphaOfEdgeEvents = new ArrayList<ChangeEdgeEventDescription>();
		}
	}

	private void createBeforeChangeOmegaOfEdgeEventsLazily() {
		if (beforeChangeOmegaOfEdgeEvents == null) {
			beforeChangeOmegaOfEdgeEvents = new ArrayList<ChangeEdgeEventDescription>();
		}
	}

	private void createAfterChangeOmegaOfEdgeEventsLazily() {
		if (afterChangeOmegaOfEdgeEvents == null) {
			afterChangeOmegaOfEdgeEvents = new ArrayList<ChangeEdgeEventDescription>();
		}
	}

	private void createBeforeChangeAttributeEventsLazily() {
		if (beforeChangeAttributeEvents == null) {
			beforeChangeAttributeEvents = new ArrayList<ChangeAttributeEventDescription<?>>();
		}
	}

	private void createAfterChangeAttributeEventsLazily() {
		if (afterChangeAttributeEvents == null) {
			afterChangeAttributeEvents = new ArrayList<ChangeAttributeEventDescription<?>>();
		}
	}

	// +++++ Fire Events ++++++++++++++++++++++++++++++++++++++++++++++

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.eca.ECARuleManagerInterface#
	 * fireBeforeCreateVertexEvents(java.lang.Class)
	 */
	@Override
	public void beforeCreateVertex(VertexClass vc) {
		if (beforeCreateVertexEvents == null) {
			return;
		}
		if (increaseAndTestOnMaximumNestedCalls()) {
			return;
		}
		int max = beforeCreateVertexEvents.size();
		for (int i = 0; i < max; i++) {
			beforeCreateVertexEvents.get(i).fire(vc);
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
	public void afterCreateVertex(Vertex element) {
		if (afterCreateVertexEvents == null) {
			return;
		}
		if (increaseAndTestOnMaximumNestedCalls()) {
			return;
		}
		int max = afterCreateVertexEvents.size();
		for (int i = 0; i < max; i++) {
			afterCreateVertexEvents.get(i).fire(element);
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
	public void beforeDeleteVertex(Vertex element) {
		if (beforeDeleteVertexEvents == null) {
			return;
		}
		if (increaseAndTestOnMaximumNestedCalls()) {
			return;
		}
		int max = beforeDeleteVertexEvents.size();
		for (int i = 0; i < max; i++) {
			beforeDeleteVertexEvents.get(i).fire(element);
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
	public void afterDeleteVertex(VertexClass vc) {
		if (afterDeleteVertexEvents == null) {
			return;
		}
		if (increaseAndTestOnMaximumNestedCalls()) {
			return;
		}
		int max = afterDeleteVertexEvents.size();
		for (int i = 0; i < max; i++) {
			afterDeleteVertexEvents.get(i).fire(vc);
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
	public void beforeCreateEdge(EdgeClass elementClass,
			Vertex alpha, Vertex omega) {
		if (beforeCreateEdgeEvents == null) {
			return;
		}
		if (increaseAndTestOnMaximumNestedCalls()) {
			return;
		}
		int max = beforeCreateEdgeEvents.size();
		for (int i = 0; i < max; i++) {
			beforeCreateEdgeEvents.get(i).fire(elementClass);
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
	public void afterCreateEdge(Edge element) {
		if (afterCreateEdgeEvents == null) {
			return;
		}
		if (increaseAndTestOnMaximumNestedCalls()) {
			return;
		}
		int max = afterCreateEdgeEvents.size();
		for (int i = 0; i < max; i++) {
			afterCreateEdgeEvents.get(i).fire(element);
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
	public void beforeDeleteEdge(Edge element) {
		if (beforeDeleteEdgeEvents == null) {
			return;
		}
		if (increaseAndTestOnMaximumNestedCalls()) {
			return;
		}
		int max = beforeDeleteEdgeEvents.size();
		for (int i = 0; i < max; i++) {
			beforeDeleteEdgeEvents.get(i).fire(element);
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
	public void afterDeleteEdge(EdgeClass ec, Vertex oldAlpha,
			Vertex oldOmega) {
		if (afterDeleteEdgeEvents == null) {
			return;
		}
		if (increaseAndTestOnMaximumNestedCalls()) {
			return;
		}
		int max = afterDeleteEdgeEvents.size();
		for (int i = 0; i < max; i++) {
			afterDeleteEdgeEvents.get(i).fire(ec, oldAlpha, oldOmega);
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
	public void beforeChangeAlpha(Edge element,
			Vertex oldVertex, Vertex newVertex) {
		if (beforeChangeAlphaOfEdgeEvents == null) {
			return;
		}
		if (increaseAndTestOnMaximumNestedCalls()) {
			return;
		}
		int max = beforeChangeAlphaOfEdgeEvents.size();
		for (int i = 0; i < max; i++) {
			beforeChangeAlphaOfEdgeEvents.get(i).fire(element, oldVertex,
					newVertex, EdgeEnd.ALPHA);
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
	public void afterChangeAlpha(Edge element,
			Vertex oldVertex, Vertex newVertex) {
		if (afterChangeAlphaOfEdgeEvents == null) {
			return;
		}
		if (increaseAndTestOnMaximumNestedCalls()) {
			return;
		}
		int max = afterChangeAlphaOfEdgeEvents.size();
		for (int i = 0; i < max; i++) {
			afterChangeAlphaOfEdgeEvents.get(i).fire(element, oldVertex,
					newVertex, EdgeEnd.ALPHA);
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
	public void beforeChangeOmega(Edge element,
			Vertex oldVertex, Vertex newVertex) {
		if (beforeChangeOmegaOfEdgeEvents == null) {
			return;
		}
		if (increaseAndTestOnMaximumNestedCalls()) {
			return;
		}
		int max = beforeChangeOmegaOfEdgeEvents.size();
		for (int i = 0; i < max; i++) {
			beforeChangeOmegaOfEdgeEvents.get(i).fire(element, oldVertex,
					newVertex, EdgeEnd.OMEGA);
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
	public void afterChangeOmega(Edge element,
			Vertex oldVertex, Vertex newVertex) {
		if (afterChangeOmegaOfEdgeEvents == null) {
			return;
		}
		if (increaseAndTestOnMaximumNestedCalls()) {
			return;
		}
		int max = afterChangeOmegaOfEdgeEvents.size();
		for (int i = 0; i < max; i++) {
			afterChangeOmegaOfEdgeEvents.get(i).fire(element, oldVertex,
					newVertex, EdgeEnd.OMEGA);
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
	public <AEC extends AttributedElementClass<AEC, ?>> void beforeChangeAttribute(
			AttributedElement<AEC, ?> element, String attributeName,
			Object oldValue, Object newValue) {
		if (beforeChangeAttributeEvents == null) {
			return;
		}
		if (increaseAndTestOnMaximumNestedCalls()) {
			return;
		}
		int max = beforeChangeAttributeEvents.size();
		for (int i = 0; i < max; i++) {
			@SuppressWarnings("unchecked")
			ChangeAttributeEventDescription<AEC> ed = (ChangeAttributeEventDescription<AEC>) beforeChangeAttributeEvents
					.get(i);
			ed.fire(element, attributeName, oldValue, newValue);
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
	public <AEC extends AttributedElementClass<AEC, ?>> void afterChangeAttribute(
			AttributedElement<AEC, ?> element, String attributeName,
			Object oldValue, Object newValue) {
		if (afterChangeAttributeEvents == null) {
			return;
		}
		if (increaseAndTestOnMaximumNestedCalls()) {
			return;
		}
		int max = afterChangeAttributeEvents.size();
		for (int i = 0; i < max; i++) {
			@SuppressWarnings("unchecked")
			ChangeAttributeEventDescription<AEC> ed = (ChangeAttributeEventDescription<AEC>) afterChangeAttributeEvents
					.get(i);
			ed.fire(element, attributeName, oldValue, newValue);
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
	 * Getter for managed ECARules.
	 * 
	 * @return the List of ECARules managed by this ECARuleManager
	 */
	public List<ECARule<?>> getRules() {
		return Collections.unmodifiableList(rules);
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
	public <AEC extends AttributedElementClass<AEC, ?>> void addECARule(
			EventDescription<AEC> event, Action<AEC> action) {
		ECARule<AEC> newRule = new ECARule<AEC>(event, action);
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
	public <AEC extends AttributedElementClass<AEC, ?>> void addECARule(
			EventDescription<AEC> event, Condition<AEC> condition,
			Action<AEC> action) {
		ECARule<AEC> newRule = new ECARule<AEC>(event, condition, action);
		this.addECARule(newRule);
	}

	/**
	 * Adds an ECARule to this ECARuleManager, throws a Runtime Exception if the
	 * ECARule has already an ECARuleManager
	 * 
	 * @param rule
	 *            the ECARule to add
	 */
	public void addECARule(ECARule<?> rule) {
		if (rule.getECARuleManager() != null) {
			throw new ECAException(
					"ERROR: Tried to add an ECARule to an ECARulemanager,"
							+ " but the ECARule has already a manager.");
		}
		EventDescription<?> ev = rule.getEventDescription();
		for (ECARule<?> temprule : ev.getActiveECARules()) {
			if (temprule.getECARuleManager() != this) {
				throw new ECAException(
						"ERROR: Tried to add an ECARule to an ECARulemanager,"
								+ " but the Event part monitors already another Graph.");
			}
		}
		rules.add(rule);
		rule.setECARuleManager(this);
		ev.addActiveRule(rule);
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
			this.addEventToList((ChangeAttributeEventDescription<?>) ev);
		}
	}

	/**
	 * Deletes a ECARule from this ECARuleManager
	 * 
	 * @param rule
	 *            the ECARule to delete
	 */
	public void deleteECARule(ECARule<?> rule) {
		rules.remove(rule);
		rule.setECARuleManager(null);
		EventDescription<?> ev = rule.getEventDescription();
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
				removeEventFromList((ChangeAttributeEventDescription<?>) ev);
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
			createBeforeCreateVertexEventsLazily();
			if (!beforeCreateVertexEvents.contains(e)) {
				beforeCreateVertexEvents.add(e);
			}
		} else {
			createAfterCreateVertexEventsLazily();
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
			createBeforeDeleteVertexEventsLazily();
			if (!beforeDeleteVertexEvents.contains(e)) {
				beforeDeleteVertexEvents.add(e);
			}
		} else {
			createAfterDeleteVertexEventsLazily();
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
			createBeforeCreateEdgeEventsLazily();
			if (!beforeCreateEdgeEvents.contains(e)) {
				beforeCreateEdgeEvents.add(e);
			}
		} else {
			createAfterCreateEdgeEventsLazily();
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
			createBeforeDeleteEdgeEventsLazily();
			if (!beforeDeleteEdgeEvents.contains(e)) {
				beforeDeleteEdgeEvents.add(e);
			}
		} else {
			createAfterDeleteEdgeEventsLazily();
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
				createBeforeChangeAlphaOfEdgeEventsLazily();
				if (!beforeChangeAlphaOfEdgeEvents.contains(e)) {
					beforeChangeAlphaOfEdgeEvents.add(e);
				}
			} else if (e.getEdgeEnd().equals(EdgeEnd.OMEGA)) {
				createBeforeChangeOmegaOfEdgeEventsLazily();
				if (!beforeChangeOmegaOfEdgeEvents.contains(e)) {
					beforeChangeOmegaOfEdgeEvents.add(e);
				}
			} else /* BOTH */{
				createBeforeChangeAlphaOfEdgeEventsLazily();
				createBeforeChangeOmegaOfEdgeEventsLazily();
				if (!beforeChangeAlphaOfEdgeEvents.contains(e)) {
					beforeChangeAlphaOfEdgeEvents.add(e);
				}
				if (!beforeChangeOmegaOfEdgeEvents.contains(e)) {
					beforeChangeOmegaOfEdgeEvents.add(e);
				}
			}
		} else {
			if (e.getEdgeEnd().equals(EdgeEnd.ALPHA)) {
				createAfterChangeAlphaOfEdgeEventsLazily();
				if (!afterChangeAlphaOfEdgeEvents.contains(e)) {
					afterChangeAlphaOfEdgeEvents.add(e);
				}
			} else if (e.getEdgeEnd().equals(EdgeEnd.OMEGA)) {
				createAfterChangeOmegaOfEdgeEventsLazily();
				if (!afterChangeOmegaOfEdgeEvents.contains(e)) {
					afterChangeOmegaOfEdgeEvents.add(e);
				}
			} else /* BOTH */{
				createAfterChangeAlphaOfEdgeEventsLazily();
				createAfterChangeOmegaOfEdgeEventsLazily();
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
	private <AEC extends AttributedElementClass<AEC, ?>> void addEventToList(
			ChangeAttributeEventDescription<AEC> e) {
		if (e.getTime().equals(EventDescription.EventTime.BEFORE)) {
			createBeforeChangeAttributeEventsLazily();
			if (!beforeChangeAttributeEvents.contains(e)) {
				beforeChangeAttributeEvents.add(e);
			}
		} else {
			createAfterChangeAttributeEventsLazily();
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
	private <AEC extends AttributedElementClass<AEC, ?>> void removeEventFromList(
			ChangeAttributeEventDescription<AEC> ev) {
		if (ev.getTime().equals(EventDescription.EventTime.BEFORE)) {
			beforeChangeAttributeEvents.remove(ev);
		} else {
			afterChangeAttributeEvents.remove(ev);
		}
	}

}
