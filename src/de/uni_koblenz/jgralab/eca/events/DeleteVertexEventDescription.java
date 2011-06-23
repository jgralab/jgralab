package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.eca.ECARule;

public class DeleteVertexEventDescription extends EventDescription {

	/**
	 * Creates an DeleteVertexEvent with the given parameters
	 * 
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param type
	 *            the Class of elements, this Event monitors
	 */
	public DeleteVertexEventDescription(EventTime time,
			Class<? extends AttributedElement> type) {
		super(time, type);
	}

	/**
	 * Creates an DeleteVertexEvent with the given parameters
	 * 
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param contextExpr
	 *            the contextExpression to get the context
	 */
	public DeleteVertexEventDescription(EventTime time, String contextExpr) {
		super(time, contextExpr);
	}

	public void fire(AttributedElement element) {
		if (super.checkContext(element)) {
			int nested = this.getActiveECARules().get(0).getECARuleManager()
					.getNestedTriggerCalls();
			for (ECARule rule : activeRules) {
				rule.trigger(new DeleteVertexEvent(nested, (Vertex) element));
			}
		}
	}
	
	public void fire(Class<? extends AttributedElement> type) {
		if (super.checkContext(type)) {
			int nested = this.getActiveECARules().get(0).getECARuleManager()
					.getNestedTriggerCalls();
			for (ECARule rule : activeRules) {
				rule.trigger(new DeleteVertexEvent(nested, null));
			}
		}
	}
	
}
