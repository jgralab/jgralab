package de.uni_koblenz.jgralab.eca.events;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.eca.ECAException;
import de.uni_koblenz.jgralab.eca.ECARule;

public class DeleteVertexEventDescription extends EventDescription {

	/**
	 * Creates an DeleteVertexEventDescription with the given parameters
	 * 
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param type
	 *            the Class of elements, this EventDescription monitors
	 */
	public DeleteVertexEventDescription(EventTime time,
			Class<? extends AttributedElement> type) {
		super(time, type);
	}

	/**
	 * Creates an DeleteVertexEventDescription with the given parameters
	 * 
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param contextExpr
	 *            the contextExpression to get the context
	 */
	public DeleteVertexEventDescription(EventTime time, String contextExpr) {
		super(time, contextExpr);
		if (time.equals(EventTime.AFTER)) {
			throw new ECAException(
					"Event \"after delete Vertex\" can not match a context expression"
							+ " because there is no element.");
		}
	}

	// -------------------------------------------------------------------

	/**
	 * Triggers the rules if this EventDescription matches the Event
	 * 
	 * @param element
	 *            the to be deleted Vertex
	 */
	public void fire(AttributedElement element) {
		if (super.checkContext(element)) {
			int nested = this.getActiveECARules().get(0).getECARuleManager()
					.getNestedTriggerCalls();
			Graph graph = this.getActiveECARules().get(0).getECARuleManager()
					.getGraph();
			for (ECARule rule : activeRules) {
				rule.trigger(new DeleteVertexEvent(nested, graph,
						(Vertex) element));
			}
		}
	}
	
	/**
	 * Triggers the rule if this EventDescription matches the Event
	 * 
	 * @param type
	 *            the type of the Vertex that is deleted
	 */
	public void fire(Class<? extends AttributedElement> type) {
		if (super.checkContext(type)) {
			int nested = this.getActiveECARules().get(0).getECARuleManager()
					.getNestedTriggerCalls();
			Graph graph = this.getActiveECARules().get(0).getECARuleManager()
					.getGraph();
			for (ECARule rule : activeRules) {
				rule.trigger(new DeleteVertexEvent(nested, graph, this
						.getType()));
			}
		}
	}
	
}
