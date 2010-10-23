/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 * 
 *               ist@uni-koblenz.de
 * 
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package de.uni_koblenz.jgralab.greql2.jvalue;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.FiniteAutomaton;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.greql2.exception.JValueVisitorException;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public interface JValue extends Comparable<JValue> {

	/**
	 * @return the browsing info or null if none exists
	 */
	public AttributedElement getBrowsingInfo();

	/**
	 * sets the browsing info of this jvalue
	 */
	public void setBrowsingInfo(AttributedElement bInfo);

	/**
	 * accepts the given visitor to visit this jvalue
	 */
	public void accept(JValueVisitor v);

	public int compareTo(JValue o);

	/**
	 * returns the type of this JValue
	 */
	public JValueType getType();

	/**
	 * returns true if this JValue is valid, that means, that its type is not
	 * null.
	 */
	public boolean isValid();

	/**
	 * calculates the hash-code of this jvalue. This is needed because the
	 * JValueBag and JValueSet are based on HashSets and for instance JValueSet
	 * should allow only one JValue which contains a "7". All subtypes of
	 * JValue, which don't set the "value" field, must overwrite this method
	 */
	public int hashCode();

	/**
	 * returns true if this JValue is internal
	 */
	public boolean isInternal();

	/**
	 * returns true if this JValue is a Path
	 */
	public boolean isPath();

	/**
	 * returns a JValuePath-Reference to this JValue object if it is a
	 * JValuePath
	 * 
	 * @throws JValueInvalidTypeException
	 *             if this JValue object is not a JValuePath
	 */
	public JValuePath toPath() throws JValueInvalidTypeException;

	/**
	 * returns true if this JValue is a PathSystem
	 */
	public boolean isPathSystem();

	/**
	 * returns a JValuePathSystem-Reference to this JValue object if it is a
	 * JValuePathSystem
	 * 
	 * @throws JValueInvalidTypeException
	 *             if this JValue object is not a JValuePathSystem
	 */
	public JValuePathSystem toPathSystem() throws JValueInvalidTypeException;

	public boolean isSlice();

	public JValueSlice toSlice() throws JValueInvalidTypeException;

	/**
	 * @return true if this JValue encapsulates a {@link AttributedElementClass}
	 *         , false otherwise
	 */
	public boolean isAttributedElementClass();

	/**
	 * @return the encapsulated AttributedElementClass
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a GraphElementClass
	 */
	public AttributedElementClass toAttributedElementClass()
			throws JValueInvalidTypeException;

	/**
	 * @return true if this JValue encapsulates a GraphElementClass, false
	 *         otherwise
	 */
	public boolean isJValueTypeCollection();

	/**
	 * @return the encapsulated AttributedElementClass
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a GraphElementClass
	 */
	public JValueTypeCollection toJValueTypeCollection()
			throws JValueInvalidTypeException;

	/**
	 * @return true if this JValue encapsulates a graphlement value, that is
	 *         either a vertex, an edge, or a graph, false otherwise
	 */
	public boolean isAttributedElement();

	/**
	 * @return the encapsulated AttributedElement
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a AttributedElement
	 */
	public AttributedElement toAttributedElement()
			throws JValueInvalidTypeException;

	/**
	 * @return true if this JValue encapsulates a boolean value, false otherwise
	 */
	public boolean isBoolean();

	/**
	 * @return the encapsulated boolean value
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a boolean value
	 */
	public Boolean toBoolean() throws JValueInvalidTypeException;

	/**
	 * @return true if this JValue encapsulates a integer value, false otherwise
	 */
	public boolean isInteger();

	public boolean isNumber();

	/**
	 * @return the encapsulated integer value
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate an integer value
	 */
	public Integer toInteger() throws JValueInvalidTypeException;

	/**
	 * @return true if this JValue encapsulates a long value, false otherwise
	 */
	public boolean isLong();

	/**
	 * @return the encapsulated long value
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a long value
	 */
	public Long toLong() throws JValueInvalidTypeException;

	/**
	 * @return true if this JValue encapsulates a Double value, false otherwise
	 */
	public boolean isDouble();

	/**
	 * @return the encapsulated Double value
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a T value
	 */
	public Double toDouble() throws JValueInvalidTypeException;

	/**
	 * @return true if this JValue encapsulates a String value, false otherwise
	 */
	public boolean isString();

	/**
	 * Returns the encapsulated Stringvalue or transforms the encapsulted value
	 * to its string representation usinng object.toString
	 * 
	 * @return the encapsulated String value or a String representation of the
	 *         encapsulated vale if it's not a string
	 * 
	 */
	public String toString();

	/**
	 * @return true if this JValue encapsulates a String value, false otherwise
	 */
	public boolean isEnum();

	/**
	 * 
	 * @return the encapsulated Enum
	 * 
	 */
	public Enum<?> toEnum();

	/**
	 * @return true if this JValue encapsulates a Vertex value, false otherwise
	 */
	public boolean isVertex();

	/**
	 * @return the encapsulated Vertex value
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a T value
	 */
	public Vertex toVertex() throws JValueInvalidTypeException;

	/**
	 * @return true if this JValue encapsulates a Edge value, false otherwise
	 */
	public boolean isEdge();

	/**
	 * @return the encapsulated Vertex value
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a T value
	 */
	public Edge toEdge() throws JValueInvalidTypeException;

	/**
	 * @return true if this JValue encapsulates a Graph value, false otherwise
	 */
	public boolean isGraph();

	/**
	 * @return the encapsulated Vertex value
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a T value
	 */
	public Graph toGraph() throws JValueInvalidTypeException;

	/**
	 * return true if this JValue is a NFA, false Otherwise
	 */
	public boolean isAutomaton();

	/**
	 * @return the encapsulated NFA value
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a NFA
	 */
	public FiniteAutomaton toAutomaton() throws JValueInvalidTypeException;

	/**
	 * @return true if this JValue encapsulates a Object value, false otherwise
	 */
	public boolean isObject();

	/**
	 * @return the encapsulated Object value
	 */
	public Object toObject();

	/**
	 * 
	 * @return true if this JValue is a Collection, false otherwise
	 */
	public boolean isCollection();

	/**
	 * 
	 * @return true if this JValue is a Map, false otherwise
	 */
	public boolean isMap();

	/**
	 * returns a JValueCollection-Reference of this JValue
	 * 
	 * @throws JValueInvalidTypeException
	 *             if the JValue cannot be converted to a collection
	 */
	public JValueCollection toCollection() throws JValueInvalidTypeException;

	/**
	 * returns a JValueSet-Reference of this JValue
	 * 
	 * @throws ValueInvalidTypeException
	 *             if the JValue cannot be converted to a set
	 */
	public JValueSet toJValueSet() throws JValueInvalidTypeException;

	/**
	 * returns a JValueBag-Reference of this JValue
	 * 
	 * @throws ValueInvalidTypeException
	 *             if the JValue cannot be converted to a bag
	 */
	public JValueBag toJValueBag() throws JValueInvalidTypeException;

	/**
	 * returns a JValueTable-Reference of this JValue
	 * 
	 * @throws ValueInvalidTypeException
	 *             if the JValue cannot be converted to a table
	 */
	public JValueTable toJValueTable() throws JValueInvalidTypeException;

	/**
	 * returns a JValueList-Reference of this JValue
	 * 
	 * @throws ValueInvalidTypeException
	 *             if the JValue cannot be converted to a list
	 */
	public JValueList toJValueList() throws JValueInvalidTypeException;

	/**
	 * returns a JValueMap-Reference of this JValue
	 * 
	 * @throws ValueInvalidTypeException
	 *             if the JValue cannot be converted to a map
	 */
	public JValueMap toJValueMap() throws JValueInvalidTypeException;

	/**
	 * returns a JValueTuple-Reference of this JValue
	 * 
	 * @throws ValueInvalidTypeException
	 *             if the JValue cannot be converted to a tuple
	 */
	public JValueTuple toJValueTuple() throws JValueInvalidTypeException;

	/**
	 * returns a JValueRecord-Reference of this JValue
	 * 
	 * @throws ValueInvalidTypeException
	 *             if the JValue cannot be converted to a record
	 */
	public JValueRecord toJValueRecord() throws JValueInvalidTypeException;

	/**
	 * @return true if this JValue encapsulates a graph marker, false otherwise
	 */
	public boolean isGraphMarker();

	/**
	 * @return the encapsulated graph marker
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a boolean value
	 */
	public AbstractGraphMarker<AttributedElement> toGraphMarker()
			throws JValueInvalidTypeException;

	/**
	 * @return true, if the encapsulated JValue can be converted to the given
	 *         type, false otherwise
	 */
	public boolean canConvert(JValueType atype);

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
	public int conversionCosts(JValueType targetType);

	/**
	 * returns a Number representing this JValue
	 * 
	 * @throws ValueInvalidTypeException
	 *             if the JValue cannot be converted into a Number
	 */
	public Number toNumber() throws JValueInvalidTypeException;

	/**
	 * Stores this JValue as HTML file.
	 * 
	 * @param filename
	 *            the name of the file
	 * @throws JValueVisitorException
	 *             if something goes wrong, e.g. the file can not be created
	 */
	public void storeAsHTML(String filename) throws JValueVisitorException;

	/**
	 * Stores this JValue as XML file.
	 * 
	 * @param filename
	 *            the name of the file
	 * @throws JValueVisitorException
	 *             if something goes wrong, e.g. the file can not be created
	 */
	public void storeAsXML(String filename) throws JValueVisitorException;

	/**
	 * Stores this JValue as XML file.
	 * 
	 * @param filename
	 *            the name of the file
	 * @param graph
	 *            the graph corresponding to this JValue
	 * @throws JValueVisitorException
	 *             if something goes wrong, e.g. the file can not be created
	 */
	void storeAsXML(String filename, Graph graph) throws JValueVisitorException;
}
