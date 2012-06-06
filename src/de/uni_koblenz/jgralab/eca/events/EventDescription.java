/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
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
package de.uni_koblenz.jgralab.eca.events;

import java.util.ArrayList;
import java.util.List;

import org.pcollections.PCollection;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.eca.ECARule;
import de.uni_koblenz.jgralab.greql2.evaluator.QueryImpl;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public abstract class EventDescription<AEC extends AttributedElementClass<AEC, ?>> {

	/**
	 * Rules that can possibly become triggered by this EventDescription
	 */
	protected List<ECARule<AEC>> activeRules;

	/**
	 * EventTime: BEFORE or AFTER
	 */
	private final EventTime time;

	public enum EventTime {
		BEFORE, AFTER
	}

	/**
	 * Context, specifies whether this Event monitors a single Class of elements
	 * or all elements, queried by a contextExpression
	 */
	private final Context context;

	public enum Context {
		TYPE, EXPRESSION
	}

	/**
	 * GReQuL Query that evaluates to a context of elements if the
	 * {@link context} is set to EXPRESSION, null otherwise
	 */
	private String contextExpression;

	/**
	 * Class of the elements, this Event monitors if the {@link context} is set
	 * to TYPE, null otherwise
	 */
	private AEC type;

	// +++++++ Constructors ++++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Creates an EventDescription with the given parameters
	 * 
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param type
	 *            the Class of elements, this Event monitors
	 */
	public EventDescription(EventTime time, AEC type) {
		this.time = time;
		activeRules = new ArrayList<ECARule<AEC>>();
		this.type = type;
		context = Context.TYPE;
	}

	/**
	 * Creates an EventDescription with the given parameters
	 * 
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param contExpr
	 *            the contextExpression to get the context
	 */
	public EventDescription(EventTime time, String contExpr) {
		this.time = time;
		activeRules = new ArrayList<ECARule<AEC>>();
		contextExpression = contExpr;
		context = Context.EXPRESSION;
	}

	// +++++ Methods ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Returns whether the before create or after delete Event is of the type of
	 * this EventDescription
	 * 
	 * @param elementClass
	 *            Type of the element that will become created or was deleted
	 * @return whether the Event matches this EventDescription
	 */
	protected boolean checkContext(AEC elementClass) {
		if (getType().equals(elementClass)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns whether the given element is of the right type or in the
	 * evaluated context
	 * 
	 * @param element
	 *            the element to check
	 * @return whether the Event matches this EventDescription
	 */
	protected boolean checkContext(AttributedElement<AEC, ?> element) {
		if (context.equals(Context.TYPE)) {
			if (element.getAttributedElementClass().equals(type)) {
				return true;
			} else {
				return false;
			}
		} else {
			Graph graph = activeRules.get(0).getECARuleManager().getGraph();
			Object resultingContext = new QueryImpl(contextExpression)
					.evaluate(graph);
			if (resultingContext instanceof PCollection) {
				PCollection<?> col = (PCollection<?>) resultingContext;
				for (Object val : col) {
					if (val instanceof AttributedElement && val.equals(element)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	// +++++ Getter and Setter ++++++++++++++++++++++++++++++++++++++++++

	/**
	 * @return BEFORE or AFTER
	 */
	public EventTime getTime() {
		return time;
	}

	/**
	 * @return list with all currently active ECARules of this Event
	 */
	public List<ECARule<AEC>> getActiveECARules() {
		return activeRules;
	}

	/**
	 * @return the contextExpression
	 */
	public String getContextExpression() {
		return contextExpression;
	}

	/**
	 * @return the type of the monitored elements
	 */
	public AEC getType() {
		return type;
	}

	/**
	 * @return EXPRESSION or TYPE
	 */
	public Context getContext() {
		return context;
	}

	@SuppressWarnings("unchecked")
	public void addActiveRule(ECARule<?> rule) {
		activeRules.add((ECARule<AEC>) rule);
	}

}
