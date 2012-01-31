package de.uni_koblenz.jgralab.eca.events;

import java.util.ArrayList;
import java.util.List;

import org.pcollections.PCollection;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.eca.ECARule;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public abstract class EventDescription<AEC extends AttributedElementClass<AEC, ?>> {

	/**
	 * Rules that can possibly become triggered by this EventDescription
	 */
	protected List<ECARule<AEC>> activeRules;

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
			GreqlEvaluator eval = activeRules.get(0).getECARuleManager()
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
