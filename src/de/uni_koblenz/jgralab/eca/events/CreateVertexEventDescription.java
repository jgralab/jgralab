package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.eca.ECARule;

public class CreateVertexEventDescription extends EventDescription {

	/**
	 * Creates an CreateVertexEvent with the given parameters
	 * 
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param type
	 *            the Class of elements, this Event monitors
	 */
	public CreateVertexEventDescription(EventTime time,
			Class<? extends AttributedElement> type) {
		super(time, type);
	}

	/**
	 * Creates an CreateVertexEvent with the given parameters
	 * 
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param contextExpr
	 *            the contextExpression to get the context
	 */
	public CreateVertexEventDescription(EventTime time, String contextExpr) {
		super(time, contextExpr);
	}

	public void fire(AttributedElement element) {
		if (super.checkContext(element)) {
			int nested = this.getActiveECARules().get(0).getECARuleManager()
					.getNestedTriggerCalls();
			for (ECARule rule : activeRules) {
				rule.trigger(new CreateVertexEvent(nested, this.getTime(),
						(Vertex) element));
			}
		}
	}

	public void fire(Class<? extends AttributedElement> type) {
		if (super.checkContext(type)) {
			int nested = this.getActiveECARules().get(0).getECARuleManager()
					.getNestedTriggerCalls();
			for (ECARule rule : activeRules) {
				rule.trigger(new CreateVertexEvent(nested, this.getTime(), null));
			}
		}
	}
	
	
}
