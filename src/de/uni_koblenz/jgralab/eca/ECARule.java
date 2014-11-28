/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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

import de.uni_koblenz.jgralab.eca.events.Event;
import de.uni_koblenz.jgralab.eca.events.EventDescription;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class ECARule<AEC extends AttributedElementClass<AEC, ?>> {

	/**
	 * ECARuleManager of this ECARule
	 */
	private ECARuleManager manager;

	/**
	 * Event part of ECARule
	 */
	private EventDescription<AEC> eventDescription;

	/**
	 * Condition part of ECARule, optional
	 */
	private Condition<AEC> condition;

	/**
	 * Action part of ECARule
	 */
	private Action<AEC> action;

	// +++++ Constructor Summary +++++++++++++++++++++++++++

	/**
	 * Creates an ECARule with the given Event and Action
	 * 
	 * @param event
	 *            Event
	 * @param action
	 *            Action
	 */
	public ECARule(EventDescription<AEC> event, Action<AEC> action) {
		this.setEventDescription(event);
		this.setAction(action);
	}

	/**
	 * Creates an ECARule with the given Event, Condition and Action
	 * 
	 * @param event
	 *            Event
	 * @param condition
	 *            Condition
	 * @param action
	 *            Action
	 */
	public ECARule(EventDescription<AEC> event, Condition<AEC> condition,
			Action<AEC> action) {
		this.setEventDescription(event);
		this.setCondition(condition);
		this.setAction(action);
	}

	// ++++++ Methods +++++++++++++++++++++++++++++++++++++

	/**
	 * Triggers the ECARule. If a concerned element exists, it can become used
	 * by the condition, if not, null is excepted as element. If there is no
	 * condition or the condition is evaluated to true, the action is executed.
	 * 
	 * @param event
	 *            an Event containing the concerned element
	 */
	public void trigger(Event<AEC> event) {
		if (this.condition == null || this.condition.evaluate(event)) {
			this.action.doAction(event);
		}
	}

	// +++++ Getter and Setter +++++++++++++++++++++++++++++

	/**
	 * @return the Event
	 */
	public EventDescription<AEC> getEventDescription() {
		return eventDescription;
	}

	/**
	 * Sets the Event part of the ECARule
	 * 
	 * @param event
	 *            the Event
	 */
	private void setEventDescription(EventDescription<AEC> event) {
		this.eventDescription = event;
	}

	/**
	 * @return the Condition
	 */
	public Condition<AEC> getCondition() {
		return condition;
	}

	/**
	 * Sets the Condition part of the ECARule
	 * 
	 * @param condition
	 *            the Condition
	 */
	private void setCondition(Condition<AEC> condition) {
		this.condition = condition;
	}

	/**
	 * @return the Action
	 */
	public Action<AEC> getAction() {
		return action;
	}

	/**
	 * Sets the Action part of the ECARule
	 * 
	 * @param action
	 *            the Action
	 */
	private void setAction(Action<AEC> action) {
		this.action = action;
	}

	/**
	 * Sets the ECARuleManager of this rule. The method is called by the
	 * addECARule Method of the ECARuleManager.
	 * 
	 * @param manager
	 *            the ECARulemanager that should manage this rule
	 */
	public void setECARuleManager(ECARuleManager manager) {
		this.manager = manager;
	}

	/**
	 * @return the ECARuleManager of this rule if there is one
	 */
	public ECARuleManager getECARuleManager() {
		return manager;
	}

}
