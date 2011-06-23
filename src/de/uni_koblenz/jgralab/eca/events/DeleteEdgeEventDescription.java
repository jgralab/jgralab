package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.eca.ECARule;

public class DeleteEdgeEventDescription extends EventDescription {

	/**
	 * Creates an DeleteEdgeEvent with the given parameters
	 * 
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param type
	 *            the Class of elements, this Event monitors
	 */
	public DeleteEdgeEventDescription(EventTime time,
			Class<? extends AttributedElement> type) {
		super(time, type);
	}

	/**
	 * Creates an DeleteEdgeEvent with the given parameters
	 * 
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param contextExpr
	 *            the contextExpression to get the context
	 */
	public DeleteEdgeEventDescription(EventTime time, String contextExpr) {
		super(time, contextExpr);
	}

	public void fire(AttributedElement element) {
		if (super.checkContext(element)) {
			int nested = this.getActiveECARules().get(0).getECARuleManager()
					.getNestedTriggerCalls();
			Graph graph = this.getActiveECARules().get(0).getECARuleManager()
					.getGraph();
			for (ECARule rule : activeRules) {
				rule.trigger(new DeleteEdgeEvent(nested, this.getTime(), graph,
						(Edge) element));
			}
		}
	}

	public void fire(Class<? extends AttributedElement> type) {
		if (super.checkContext(type)) {
			int nested = this.getActiveECARules().get(0).getECARuleManager()
					.getNestedTriggerCalls();
			Graph graph = this.getActiveECARules().get(0).getECARuleManager()
					.getGraph();
			for (ECARule rule : activeRules) {
				rule.trigger(new DeleteEdgeEvent(nested, this.getTime(), graph,
						null));
			}
		}
	}

}
