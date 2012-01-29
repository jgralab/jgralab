package de.uni_koblenz.jgralab.eca;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

public interface ECARuleManagerInterface {

	/**
	 * Fire Events from beforeCreateVertexEvents list
	 * 
	 * @param elementClass
	 *            the Class of the new Vertex
	 */
	public abstract void fireBeforeCreateVertexEvents(VertexClass elementClass);

	/**
	 * Fire Events from afterCreateVertexEvents list
	 * 
	 * @param element
	 *            the new created Vertex
	 */
	public abstract void fireAfterCreateVertexEvents(Vertex element);

	/**
	 * Fire Events from beforeDeleteVertexEvents list
	 * 
	 * @param element
	 *            the Vertex to delete
	 */
	public abstract void fireBeforeDeleteVertexEvents(Vertex element);

	/**
	 * Fire Events from afterDeleteVertexEvents list
	 * 
	 * @param elementClass
	 *            the Class of the deleted Vertex
	 */
	public abstract void fireAfterDeleteVertexEvents(VertexClass elementClass);

	/**
	 * Fire Events from beforeCreateEdgeEvents list
	 * 
	 * @param elementClass
	 *            the Class of the new Edge
	 */
	public abstract void fireBeforeCreateEdgeEvents(
			Class<? extends Edge> elementClass);

	/**
	 * Fire Events from afterCreateEdgeEvents list
	 * 
	 * @param element
	 *            the new created Edge
	 */
	public abstract void fireAfterCreateEdgeEvents(Edge element);

	/**
	 * Fire Events from beforeDeleteEdgeEvents list
	 * 
	 * @param element
	 *            the Edge to delete
	 */
	public abstract void fireBeforeDeleteEdgeEvents(Edge element);

	/**
	 * Fire Events from afterDeleteEdgeEvents list
	 * 
	 * @param elementClass
	 *            the Class of the deleted Edge
	 */
	public abstract void fireAfterDeleteEdgeEvents(EdgeClass elementClass);

	/**
	 * Fire Events from beforeChangeAlphaOfEdgeEvents list
	 * 
	 * @param element
	 *            the Edge that will change
	 */
	public abstract void fireBeforeChangeAlphaOfEdgeEvents(Edge element,
			Vertex oldVertex, Vertex newVertex);

	/**
	 * Fire Events from afterChangeAlphaOfEdgeEvents list
	 * 
	 * @param element
	 *            the Edge that changed
	 */
	public abstract void fireAfterChangeAlphaOfEdgeEvents(Edge element,
			Vertex oldVertex, Vertex newVertex);

	/**
	 * Fire Events from beforeChangeOmegaOfEdgeEvents list
	 * 
	 * @param element
	 *            the Edge that will change
	 */
	public abstract void fireBeforeChangeOmegaOfEdgeEvents(Edge element,
			Vertex oldVertex, Vertex newVertex);

	/**
	 * Fire Events from afterChangeOmegaOfEdgeEvents list
	 * 
	 * @param element
	 *            the Edge that changed
	 */
	public abstract void fireAfterChangeOmegaOfEdgeEvents(Edge element,
			Vertex oldVertex, Vertex newVertex);

	/**
	 * Fire Events from beforeChangeAttributeEvents list
	 * 
	 * @param element
	 *            the element of which the Attribute will change
	 * @param attributeName
	 *            the name of the Attribute that will change
	 */
	public abstract void fireBeforeChangeAttributeEvents(
			AttributedElement<?, ?> element, String attributeName,
			Object oldValue, Object newValue);

	/**
	 * Fire Events from afterChangeAttributeEvents list
	 * 
	 * @param element
	 *            the element of which the Attribute changed
	 * @param attributeName
	 *            the name of the changed Attribute
	 */
	public abstract void fireAfterChangeAttributeEvents(
			AttributedElement<?, ?> element, String attributeName,
			Object oldValue, Object newValue);

	/**
	 * @return the Graph that owns this EventManager
	 */
	public abstract Graph getGraph();

	public abstract int getMaxNestedTriggerCalls();

	public abstract void setMaxNestedTriggerCalls(int maxNestedTriggerCalls);

	public abstract int getNestedTriggerCalls();

	// TODO: This cannot be declared here, else we get a cyclic dependency in
	// the build process
	// public abstract GreqlEvaluator getGreqlEvaluator();

}