package de.uni_koblenz.jgralab.eca;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;

public interface ECARuleManagerInterface {

	/**
	 * Fire Events from {@link beforeCreateVertexEvents} list
	 * 
	 * @param elementClass
	 *            the Class of the new Vertex
	 */
	public abstract void fireBeforeCreateVertexEvents(
			Class<? extends AttributedElement> elementClass);

	/**
	 * Fire Events from {@link afterCreateVertexEvents} list
	 * 
	 * @param element
	 *            the new created Vertex
	 */
	public abstract void fireAfterCreateVertexEvents(GraphElement element);

	/**
	 * Fire Events from {@link beforeDeleteVertexEvents} list
	 * 
	 * @param element
	 *            the Vertex to delete
	 */
	public abstract void fireBeforeDeleteVertexEvents(GraphElement element);

	/**
	 * Fire Events from {@link afterDeleteVertexEvents} list
	 * 
	 * @param elementClass
	 *            the Class of the deleted Vertex
	 */
	public abstract void fireAfterDeleteVertexEvents(
			Class<? extends AttributedElement> elementClass);

	/**
	 * Fire Events from {@link beforeCreateEdgeEvents} list
	 * 
	 * @param elementClass
	 *            the Class of the new Edge
	 */
	public abstract void fireBeforeCreateEdgeEvents(
			Class<? extends AttributedElement> elementClass);

	/**
	 * Fire Events from {@link afterCreateEdgeEvents} list
	 * 
	 * @param element
	 *            the new created Edge
	 */
	public abstract void fireAfterCreateEdgeEvents(GraphElement element);

	/**
	 * Fire Events from {@link beforeDeleteEdgeEvents} list
	 * 
	 * @param element
	 *            the Edge to delete
	 */
	public abstract void fireBeforeDeleteEdgeEvents(GraphElement element);

	/**
	 * Fire Events from {@link afterDeleteEdgeEvents} list
	 * 
	 * @param elementClass
	 *            the Class of the deleted Edge
	 */
	public abstract void fireAfterDeleteEdgeEvents(
			Class<? extends AttributedElement> elementClass);

	/**
	 * Fire Events from {@link beforeChangeEdgeEvents} list
	 * 
	 * @param element
	 *            the Edge that will change
	 */
	public abstract void fireBeforeChangeEdgeEvents(GraphElement element,
			Vertex oldVertex, Vertex newVertex);

	/**
	 * Fire Events from {@link afterChangeEdgeEvents} list
	 * 
	 * @param element
	 *            the Edge that changed
	 */
	public abstract void fireAfterChangeEdgeEvents(GraphElement element,
			Vertex oldVertex, Vertex newVertex);

	/**
	 * Fire Events from {@link beforeChangeAttributeEvents} list
	 * 
	 * @param element
	 *            the element of which the Attribute will change
	 * @param attributeName
	 *            the name of the Attribute that will change
	 */
	public abstract void fireBeforeChangeAttributeEvents(
			AttributedElement element, String attributeName, Object oldValue,
			Object newValue);

	/**
	 * Fire Events from {@link afterChangeAttributeEvents} list
	 * 
	 * @param element
	 *            the element of which the Attribute changed
	 * @param attributeName
	 *            the name of the changed Attribute
	 */
	public abstract void fireAfterChangeAttributeEvents(
			AttributedElement element, String attributeName, Object oldValue,
			Object newValue);

	/**
	 * @return the Graph that owns this EventManager
	 */
	public abstract Graph getGraph();

	public abstract int getMaxNestedTriggerCalls();

	public abstract void setMaxNestedTriggerCalls(int maxNestedTriggerCalls);

	public abstract int getNestedTriggerCalls();

}