package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.eca.ECAException;
import de.uni_koblenz.jgralab.eca.ECARule;
import de.uni_koblenz.jgralab.schema.EdgeClass;

public class DeleteEdgeEventDescription extends EventDescription<EdgeClass> {

	/**
	 * Creates an DeleteEdgeEventDescription with the given parameters
	 * 
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param type
	 *            the Class of elements, this Event monitors
	 */
	public DeleteEdgeEventDescription(EventTime time, EdgeClass type) {
		super(time, type);
	}

	/**
	 * Creates an DeleteEdgeEventDescription with the given parameters
	 * 
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param contextExpr
	 *            the contextExpression to get the context
	 */
	public DeleteEdgeEventDescription(EventTime time, String contextExpr) {
		super(time, contextExpr);
		if (time.equals(EventTime.AFTER)) {
			throw new ECAException(
					"Event \"after delete Edge\" can not match a context expression"
							+ " because there is no element.");
		}
	}

	// -------------------------------------------------------------------

	/**
	 * Triggers the rules if this EventDescription matches the Event
	 * 
	 * @param element
	 *            the to be deleted Edge
	 */
	public void fire(Edge element) {
		if (super.checkContext(element)) {
			int nested = getActiveECARules().get(0).getECARuleManager()
					.getNestedTriggerCalls();
			Graph graph = getActiveECARules().get(0).getECARuleManager()
					.getGraph();
			for (ECARule<EdgeClass> rule : activeRules) {
				rule.trigger(new DeleteEdgeEvent(nested, graph, element));
			}
		}
	}

	/**
	 * Triggers the rule if this EventDescription matches the Event
	 * 
	 * @param type
	 *            the type of the Edge that is deleted
	 */
	public void fire(EdgeClass type) {
		if (super.checkContext(type)) {
			int nested = getActiveECARules().get(0).getECARuleManager()
					.getNestedTriggerCalls();
			Graph graph = getActiveECARules().get(0).getECARuleManager()
					.getGraph();
			for (ECARule<EdgeClass> rule : activeRules) {
				rule.trigger(new DeleteEdgeEvent(nested, graph, type));
			}
		}
	}

}
