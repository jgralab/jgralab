package de.uni_koblenz.jgralab.eca.events;

import java.util.ArrayList;
import java.util.List;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.eca.ECARule;
import de.uni_koblenz.jgralab.eca.EventManager;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;


public abstract class Event {

	/**
	 * EventManager that manages this Event
	 */
	private EventManager manager;
	
	/**
	 * Rules that can possibly become triggered by this Event
	 */
	protected List<ECARule> rules;
	
	/**
	 * EventTime: BEFORE or AFTER
	 */
	private EventTime time;
	
	public enum EventTime{
		BEFORE,
		AFTER
	}

	/**
	 * Context, specifies whether this Event monitors a single Class of elements
	 * or all elements, queried by a contextExpression
	 */
	private Context context;
	
	private enum Context{
		TYPE,
		EXPRESSION
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
	 * Creates an Event with the given parameters
	 * 
	 * @param manager
	 *            the EventManager that manages this Event
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param type
	 *            the Class of elements, this Event monitors
	 */
	public Event(EventManager manager, EventTime time, Class <? extends AttributedElement> type){
		this.manager = manager;
		this.time = time;
		this.rules = new ArrayList<ECARule>();
		this.type = type;
		this.context = Context.TYPE;
	}

	/**
	 * Creates an Event with the given parameters
	 * 
	 * @param manager
	 *            the EventManager that manages this Event
	 * @param time
	 *            the EventTime, BEFORE or AFTER
	 * @param contExpr
	 *            the contextExpression to get the context
	 */
	public Event(EventManager manager, EventTime time, String contExpr){
		this.manager = manager;
		this.time = time;
		this.rules = new ArrayList<ECARule>();
		this.contextExpression = contExpr;
		this.context = Context.EXPRESSION;
	}
	
	// +++++ Methods ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Triggers each ECARule, if the element is of the right type or in the
	 * context
	 * 
	 * @param element
	 *            the AttributedElement, this Event is fired for
	 */
	public void fire(AttributedElement element){
		if (this.checkContext(element)) {
			for (ECARule rule : rules) {
				rule.trigger(element);
			}
		}
	}

	/**
	 * Triggers each ECARule, if the parameter is the right type
	 * 
	 * @param elementClass
	 *            Class of the element that invokes this Event
	 */
	public void fire(Class<? extends AttributedElement> elementClass){
		if (this.getType().equals(elementClass)) {
			for (ECARule rule : rules) {
				rule.trigger(null);		
			}
		}
	}

	/**
	 * Returns whether the given element is of the right type or in the
	 * evaluated context
	 * 
	 * @param element
	 *            the element to check
	 * @return whether the element really invokes this Event
	 */
	private boolean checkContext(AttributedElement element){
		if(this.context.equals(Context.TYPE)){
			if(element.getM1Class().equals(this.type)){
				return true;
			}
			else{
				return false;
			}
		}else{
			Graph graph = this.getEventManager().getGraph();
			GreqlEvaluator eval = new GreqlEvaluator(this.contextExpression, graph, null);			
			eval.startEvaluation();
			JValue resultingContext = eval.getEvaluationResult();
			if(resultingContext.isCollection()){
				JValueCollection col = resultingContext.toCollection();
				for(JValue val : col){
					if(val.isAttributedElement() && 
							val.toAttributedElement().equals(element)){
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
	 * Adds an ECARule to this Event
	 * 
	 * @param rule
	 *            the new added ECARule
	 */
	public void addRule(ECARule rule){
		this.rules.add(rule);
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
	public Class <? extends AttributedElement> getType() {
		return type;
	}

	/**
	 * @return EXPRESSION or TYPE
	 */
	public Context getContext() {
		return context;
	}
	
	/**
	 * @return the managing EventManager
	 */
	public EventManager getEventManager() {
		return manager;
	}
}
