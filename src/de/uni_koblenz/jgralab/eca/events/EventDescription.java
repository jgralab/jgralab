package de.uni_koblenz.jgralab.eca.events;

import java.util.ArrayList;
import java.util.List;

import org.pcollections.PCollection;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.eca.ECARule;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluatorImpl;

public abstract class EventDescription {

	/**
	 * Rules that can possibly become triggered by this EventDescription
	 */
	protected List<ECARule> activeRules;

	/**
	 * EventTime: BEFORE or AFTER
	 */
	private EventTime time;

	public enum EventTime {
		BEFORE, AFTER
	}

	/**
	 * Context, specifies whether this Event monitors a single Class of elements
	 * or all elements, queried by a contextExpression
	 */
	private Context context;

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
	private Class<? extends AttributedElement> type;

	// +++++++ Constructors ++++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Creates an EventDescription with the given parameters
	 * 
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param type
	 *            the Class of elements, this Event monitors
	 */
	public EventDescription(EventTime time,
			Class<? extends AttributedElement> type) {
		this.time = time;
		activeRules = new ArrayList<ECARule>();
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
		activeRules = new ArrayList<ECARule>();
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
	protected boolean checkContext(
			Class<? extends AttributedElement> elementClass) {
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
	protected boolean checkContext(AttributedElement element) {
		if (context.equals(Context.TYPE)) {
			if (element.getSchemaClass().equals(type)) {
				return true;
			} else {
				return false;
			}
		} else {
			GreqlEvaluatorImpl eval = activeRules.get(0).getECARuleManager()
					.getGreqlEvaluator();
			eval.setQuery(contextExpression);
			eval.startEvaluation();
			Object resultingContext = eval.getResult();
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
	public List<ECARule> getActiveECARules() {
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
	public Class<? extends AttributedElement> getType() {
		return type;
	}

	/**
	 * @return EXPRESSION or TYPE
	 */
	public Context getContext() {
		return context;
	}

}
