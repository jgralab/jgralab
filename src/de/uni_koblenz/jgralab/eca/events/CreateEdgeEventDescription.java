package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.eca.ECAException;
import de.uni_koblenz.jgralab.eca.ECARule;
import de.uni_koblenz.jgralab.schema.EdgeClass;

public class CreateEdgeEventDescription extends EventDescription<EdgeClass> {

	/**
	 * Creates an CreateEdgeEventDescription with the given parameters
	 * 
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param type
	 *            the Class of elements, this EventDescription monitors
	 */
	public CreateEdgeEventDescription(EventTime time, EdgeClass type) {
		super(time, type);
	}

	/**
	 * Creates an CreateEdgeEventDescription with the given parameters
	 * 
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param contextExpr
	 *            the contextExpression to get the context
	 */
	public CreateEdgeEventDescription(EventTime time, String contextExpr) {
		super(time, contextExpr);
		if (time.equals(EventTime.BEFORE)) {
			throw new ECAException(
					"Event \"before create Edge\" can not match a context expression"
							+ " because there is no element.");
		}
	}

	// --------------------------------------------------------------------

	/**
	 * Triggers the rules if this EventDescription matches the Event
	 * 
	 * @param element
	 *            the created Edge
	 */
	public void fire(Edge element) {
		if (super.checkContext(element)) {
			int nested = getActiveECARules().get(0).getECARuleManager()
					.getNestedTriggerCalls();
			Graph graph = getActiveECARules().get(0).getECARuleManager()
					.getGraph();
			for (ECARule<EdgeClass> rule : activeRules) {
				rule.trigger(new CreateEdgeEvent(nested, graph, element));
			}
		}
	}

	/**
	 * Triggers the rule if this EventDescription matches the Event
	 * 
	 * @param type
	 *            the type of the Edge that will become created
	 */
	public void fire(EdgeClass type) {
		if (super.checkContext(type)) {
			int nested = getActiveECARules().get(0).getECARuleManager()
					.getNestedTriggerCalls();
			Graph graph = getActiveECARules().get(0).getECARuleManager()
					.getGraph();
			for (ECARule<EdgeClass> rule : activeRules) {
				rule.trigger(new CreateEdgeEvent(nested, graph, getType()));
			}
		}
	}
}
