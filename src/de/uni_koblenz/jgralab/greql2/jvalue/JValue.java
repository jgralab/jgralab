package de.uni_koblenz.jgralab.greql2.jvalue;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.FiniteAutomaton;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public interface JValue extends Comparable<JValue> {

	/**
	 * @return the browsing info or null if none exists
	 */
	public abstract AttributedElement getBrowsingInfo();

	/**
	 * sets the browsing info of this jvalue
	 */
	public abstract void setBrowsingInfo(AttributedElement bInfo);

	/**
	 * accepts the given visitor to visit this jvalue
	 */
	public abstract void accept(JValueVisitor v);

	public abstract int compareTo(JValue o);

	/**
	 * returns the type of this JValue
	 */
	public abstract JValueType getType();

	/**
	 * returns true if this JValue is valid, that means, that its type is not
	 * null.
	 */
	public abstract boolean isValid();

	/**
	 * calculates the hash-code of this jvalue. This is needed because the
	 * JValueBag and JValueSet are based on HashSets and for instance JValueSet
	 * should allow only one JValue which contains a "7". All subtypes of
	 * JValue, which don't set the "value" field, must overwrite this method
	 */
	public abstract int hashCode();

	/**
	 * returns true if this JValue is internal
	 */
	public abstract boolean isInternal();

	/**
	 * returns true if this JValue is a Path
	 */
	public abstract boolean isPath();

	/**
	 * returns a JValuePath-Reference to this JValue object if it is a
	 * JValuePath
	 * 
	 * @throws JValueInvalidTypeException
	 *             if this JValue object is not a JValuePath
	 */
	public abstract JValuePath toPath() throws JValueInvalidTypeException;

	/**
	 * returns true if this JValue is a PathSystem
	 */
	public abstract boolean isPathSystem();

	/**
	 * returns a JValuePathSystem-Reference to this JValue object if it is a
	 * JValuePathSystem
	 * 
	 * @throws JValueInvalidTypeException
	 *             if this JValue object is not a JValuePathSystem
	 */
	public abstract JValuePathSystem toPathSystem()
			throws JValueInvalidTypeException;

	public abstract boolean isSlice();

	public abstract JValueSlice toSlice() throws JValueInvalidTypeException;

	/**
	 * @return true if this JValue encapsulates a {@link AttributedElementClass}
	 *         , false otherwise
	 */
	public abstract boolean isAttributedElementClass();

	/**
	 * @return the encapsulated AttributedElementClass
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a GraphElementClass
	 */
	public abstract AttributedElementClass toAttributedElementClass()
			throws JValueInvalidTypeException;

	/**
	 * @return true if this JValue encapsulates a GraphElementClass, false
	 *         otherwise
	 */
	public abstract boolean isJValueTypeCollection();

	/**
	 * @return the encapsulated AttributedElementClass
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a GraphElementClass
	 */
	public abstract JValueTypeCollection toJValueTypeCollection()
			throws JValueInvalidTypeException;

	/**
	 * @return true if this JValue encapsulates a graphlement value, that is
	 *         either a vertex, an edge, or a graph, false otherwise
	 */
	public abstract boolean isAttributedElement();

	/**
	 * @return the encapsulated AttributedElement
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a AttributedElement
	 */
	public abstract AttributedElement toAttributedElement()
			throws JValueInvalidTypeException;

	/**
	 * @return true if this JValue encapsulates a boolean value, false otherwise
	 */
	public abstract boolean isBoolean();

	/**
	 * @return the encapsulated boolean value
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a boolean value
	 */
	public abstract Boolean toBoolean() throws JValueInvalidTypeException;

	/**
	 * @return true if this JValue encapsulates a integer value, false otherwise
	 */
	public abstract boolean isInteger();

	public abstract boolean isNumber();

	/**
	 * @return the encapsulated integer value
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate an integer value
	 */
	public abstract Integer toInteger() throws JValueInvalidTypeException;

	/**
	 * @return true if this JValue encapsulates a long value, false otherwise
	 */
	public abstract boolean isLong();

	/**
	 * @return the encapsulated long value
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a long value
	 */
	public abstract Long toLong() throws JValueInvalidTypeException;

	/**
	 * @return true if this JValue encapsulates a Double value, false otherwise
	 */
	public abstract boolean isDouble();

	/**
	 * @return the encapsulated Double value
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a T value
	 */
	public abstract Double toDouble() throws JValueInvalidTypeException;

	/**
	 * @return true if this JValue encapsulates a String value, false otherwise
	 */
	public abstract boolean isString();

	/**
	 * Returns the encapsulated Stringvalue or transforms the encapsulted value
	 * to its string representation usinng object.toString
	 * 
	 * @return the encapsulated String value or a String representation of the
	 *         encapsulated vale if it's not a string
	 * 
	 */
	public abstract String toString();

	/**
	 * @return true if this JValue encapsulates a String value, false otherwise
	 */
	public abstract boolean isEnum();

	/**
	 * 
	 * @return the encapsulated Enum
	 * 
	 */
	@SuppressWarnings("unchecked")
	public abstract Enum toEnum();

	/**
	 * @return true if this JValue encapsulates a Vertex value, false otherwise
	 */
	public abstract boolean isVertex();

	/**
	 * @return the encapsulated Vertex value
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a T value
	 */
	public abstract Vertex toVertex() throws JValueInvalidTypeException;

	/**
	 * @return true if this JValue encapsulates a Edge value, false otherwise
	 */
	public abstract boolean isEdge();

	/**
	 * @return the encapsulated Vertex value
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a T value
	 */
	public abstract Edge toEdge() throws JValueInvalidTypeException;

	/**
	 * @return true if this JValue encapsulates a Graph value, false otherwise
	 */
	public abstract boolean isGraph();

	/**
	 * @return the encapsulated Vertex value
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a T value
	 */
	public abstract Graph toGraph() throws JValueInvalidTypeException;

	/**
	 * return true if this JValue is a NFA, false Otherwise
	 */
	public abstract boolean isAutomaton();

	/**
	 * @return the encapsulated NFA value
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a NFA
	 */
	public abstract FiniteAutomaton toAutomaton()
			throws JValueInvalidTypeException;

	/**
	 * @return true if this JValue encapsulates a Object value, false otherwise
	 */
	public abstract boolean isObject();

	/**
	 * @return the encapsulated Object value
	 */
	public abstract Object toObject();

	/**
	 * 
	 * @return true if this JValue is a Collection, false otherwise
	 */
	public abstract boolean isCollection();

	/**
	 * 
	 * @return true if this JValue is a Map, false otherwise
	 */
	public abstract boolean isMap();

	/**
	 * returns a JValueCollection-Reference of this JValue
	 * 
	 * @throws JValueInvalidTypeException
	 *             if the JValue cannot be converted to a collection
	 */
	public abstract JValueCollection toCollection()
			throws JValueInvalidTypeException;

	/**
	 * returns a JValueSet-Reference of this JValue
	 * 
	 * @throws ValueInvalidTypeException
	 *             if the JValue cannot be converted to a set
	 */
	public abstract JValueSet toJValueSet() throws JValueInvalidTypeException;

	/**
	 * returns a JValueBag-Reference of this JValue
	 * 
	 * @throws ValueInvalidTypeException
	 *             if the JValue cannot be converted to a bag
	 */
	public abstract JValueBag toJValueBag() throws JValueInvalidTypeException;

	/**
	 * returns a JValueTable-Reference of this JValue
	 * 
	 * @throws ValueInvalidTypeException
	 *             if the JValue cannot be converted to a table
	 */
	public abstract JValueTable toJValueTable()
			throws JValueInvalidTypeException;

	/**
	 * returns a JValueList-Reference of this JValue
	 * 
	 * @throws ValueInvalidTypeException
	 *             if the JValue cannot be converted to a list
	 */
	public abstract JValueList toJValueList() throws JValueInvalidTypeException;

	/**
	 * returns a JValueMap-Reference of this JValue
	 * 
	 * @throws ValueInvalidTypeException
	 *             if the JValue cannot be converted to a map
	 */
	public abstract JValueMap toJValueMap() throws JValueInvalidTypeException;

	/**
	 * returns a JValueTuple-Reference of this JValue
	 * 
	 * @throws ValueInvalidTypeException
	 *             if the JValue cannot be converted to a tuple
	 */
	public abstract JValueTuple toJValueTuple()
			throws JValueInvalidTypeException;

	/**
	 * returns a JValueRecord-Reference of this JValue
	 * 
	 * @throws ValueInvalidTypeException
	 *             if the JValue cannot be converted to a record
	 */
	public abstract JValueRecord toJValueRecord()
			throws JValueInvalidTypeException;

	/**
	 * @return true if this JValue encapsulates a boolean value, false otherwise
	 */
	public abstract boolean isSubgraphTempAttribute();

	/**
	 * @return the encapsulated boolean value
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a boolean value
	 */
	public abstract BooleanGraphMarker toSubgraphTempAttribute()
			throws JValueInvalidTypeException;

	/**
	 * @return true, if the encapsulated JValue can be converted to the given
	 *         type, false otherwise
	 */
	public abstract boolean canConvert(JValueType atype);

	/**
	 * @param targetType
	 *            a {@link JValueType}
	 * @return -1 it the encapsulated value cannot be converted to
	 *         <code>targetType</code>, 0 if the value is-a
	 *         <code>targetType</code> and the conversion costs in all other
	 *         cases.
	 * 
	 *         Conversion to string and object are expensive, so that in GReQL
	 *         functions the most special is used independently of the
	 *         declaration order.
	 */
	public abstract int conversionCosts(JValueType targetType);

	public abstract Number toNumber();

}