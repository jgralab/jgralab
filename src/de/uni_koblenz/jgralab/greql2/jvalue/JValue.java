/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
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
import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.VariableDeclaration;
import de.uni_koblenz.jgralab.greql2.evaluator.VariableDeclarationLayer;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.DFA;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.NFA;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class JValue implements Comparable<JValue> {

	/**
	 * The type of the object this JValue stores
	 */
	protected JValueType type;

	/**
	 * The object this JValue stored
	 */
	protected Object value;

	/**
	 * This attributedElement holds the information, that can be browsed.
	 */
	protected AttributedElement browsingInfo;

	/**
	 * The stored hashCode
	 */
	protected int storedHashCode = 0;

	/**
	 * @return the browsing info or null if none exists
	 */
	public AttributedElement getBrowsingInfo() {
		return browsingInfo;
	}

	/**
	 * sets the browsing info of this jvalue
	 */
	public void setBrowsingInfo(AttributedElement bInfo) {
		storedHashCode = 0;
		browsingInfo = bInfo;
	}

	/**
	 * accepts the given visitor to visit this jvalue
	 */
	public void accept(JValueVisitor v) {
		if (this.type == null) {
			v.visitObject(this);
			return;
		}
		switch (this.type) {
		case BOOLEAN:
			v.visitBoolean(this);
			return;
		case CHARACTER:
			v.visitChar(this);
			return;
		case INTEGER:
			v.visitInt(this);
			return;
		case LONG:
			v.visitLong(this);
			return;
		case DOUBLE:
			v.visitDouble(this);
			return;
		case STRING:
			v.visitString(this);
			return;
		case ENUMVALUE:
			v.visitEnumValue(this);
			return;
		case ATTRIBUTEDELEMENTCLASS:
			v.visitAttributedElementClass(this);
			return;
		case VARIABLEDECLARATION:
			v.visitDeclaration(this);
			return;
		case DECLARATIONLAYER:
			v.visitDeclarationLayer(this);
			return;
		case DFA:
			v.visitDFA(this);
			return;
		case EDGE:
			v.visitEdge(this);
			return;
		case GRAPH:
			v.visitGraph(this);
			return;
		case NFA:
			v.visitNFA(this);
			return;
		case STATE:
			v.visitState(this);
			return;
		case TRANSITION:
			v.visitTransition(this);
			return;
		case SUBGRAPHTEMPATTRIBUTE:
			v.visitSubgraph(this);
			return;
		case VERTEX:
			v.visitVertex(this);
			return;
		default:
			v.visitObject(this);
			return;
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("unchecked")
	public int compareTo(JValue o) {
		if (type != o.type) {
			return (type.compareTo(o.type));
		} else {
			if (value instanceof Comparable) {
				return ((Comparable) value).compareTo(o.value);
			} else {
				return hashCode() - o.hashCode();
			}
		}
	}

	/**
	 * returns the type of this JValue
	 */
	public JValueType getType() {
		return type;
	}

	/**
	 * constructs a new invalid JValue.
	 */
	public JValue() {
		value = null;
		browsingInfo = null;
	}

	/**
	 * returns true if this JValue is valid, that means, that its value is not
	 * null.
	 */
	public boolean isValid() {
		return value != null;
	}

	/**
	 * returns true if this JValue and the given object are equal. That means,
	 * the given object is a JValue which contains an object of the same type
	 * like this one and these objects are equal
	 */
	@Override
	public boolean equals(Object anObject) {
		if (anObject instanceof JValue) {
			JValue anotherValue = (JValue) anObject;
			if (this.value == null) {
				return anotherValue.value == null;
			} else {
				if ((anotherValue.type == this.type)
						|| (anotherValue.type == JValueType.OBJECT)
						|| (this.type == JValueType.OBJECT)) {
					if (this.value == null) {
						return (anotherValue.value == null);
					}
					return value.equals(anotherValue.value);
				} else if (
				// In GReQL enum values can only specified as string for
				// comparison
				((this.type == JValueType.ENUMVALUE) && (anotherValue.type == JValueType.STRING))
						|| ((this.type == JValueType.STRING) && (anotherValue.type == JValueType.ENUMVALUE))) {
					return value.toString().equals(
							anotherValue.value.toString());
				}
			}
		}
		return false;
	}

	/**
	 * calculates the hash-code of this jvalue. This is needed because the
	 * JValueBag and JValueSet are based on HashSets and for instance JValueSet
	 * should allow only one JValue which contains a "7". All subtypes of
	 * JValue, which don't set the "value" field, must overwrite this method
	 */
	@Override
	public int hashCode() {
		if (storedHashCode == 0) {
			storedHashCode = 7;
			storedHashCode = 31 * storedHashCode + JValue.class.hashCode();
			storedHashCode = 31 * storedHashCode
					+ (value == null ? 0 : value.hashCode());
		}
		return storedHashCode;
	}

	/**
	 * returns true if their JValue contains a {@link VariableDeclarationLayer}
	 */
	public boolean isDeclarationLayer() {
		return (type == JValueType.DECLARATIONLAYER);
	}

	/**
	 * returns the encapsulated declarationlayer
	 * 
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a DeclarationLayer value
	 */
	public VariableDeclarationLayer toDeclarationLayer()
			throws JValueInvalidTypeException {
		if (isDeclarationLayer()) {
			return (VariableDeclarationLayer) value;
		}
		throw new JValueInvalidTypeException(JValueType.DECLARATIONLAYER, type);
	}

	/**
	 * creates a new JValue that encapsulates the given VariableDEclarationLayer
	 */
	public JValue(VariableDeclarationLayer d) {
		type = JValueType.DECLARATIONLAYER;
		value = d;
		browsingInfo = null;
	}

	/**
	 * creates a new JValue that encapsulates the given VariableDEclarationLayer
	 */
	public JValue(VariableDeclarationLayer d, AttributedElement browsingInfo) {
		this(d);
		this.browsingInfo = browsingInfo;
	}

	/**
	 * returns true if theis JValue contains a VaraibleDeclaration
	 */
	public boolean isVariableDeclaration() {
		return (type == JValueType.VARIABLEDECLARATION);
	}

	/**
	 * returns the encapsulated variableDeclaration
	 * 
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a DeclarationLayer value
	 */
	public VariableDeclaration toVariableDeclaration()
			throws JValueInvalidTypeException {
		if (isVariableDeclaration()) {
			return (VariableDeclaration) value;
		}
		throw new JValueInvalidTypeException(JValueType.VARIABLEDECLARATION,
				type);
	}

	/**
	 * creates a new JValue that encapsulates the given VariableDeclaration
	 */
	public JValue(VariableDeclaration d) {
		type = JValueType.VARIABLEDECLARATION;
		value = d;
		browsingInfo = null;
	}

	/**
	 * creates a new JValue that encapsulates the given VariableDeclaration
	 */
	public JValue(VariableDeclaration d, AttributedElement browsingInfo) {
		this(d);
		this.browsingInfo = browsingInfo;
	}

	/**
	 * returns true if this JValue is a Path
	 */
	public boolean isPath() {
		return (this.type == JValueType.PATH);
	}

	/**
	 * returns a JValuePath-Reference to this JValue object if it is a
	 * JValuePath
	 * 
	 * @throws JValueInvalidTypeException
	 *             if this JValue object is not a JValuePath
	 */
	public JValuePath toPath() throws JValueInvalidTypeException {
		throw new JValueInvalidTypeException(JValueType.PATH, type);
	}

	/**
	 * returns true if this JValue is a PathSystem
	 */
	public boolean isPathSystem() {
		return (this.type == JValueType.PATHSYSTEM);
	}

	/**
	 * returns a JValuePathSystem-Reference to this JValue object if it is a
	 * JValuePathSystem
	 * 
	 * @throws JValueInvalidTypeException
	 *             if this JValue object is not a JValuePathSystem
	 */
	public JValuePathSystem toPathSystem() throws JValueInvalidTypeException {
		throw new JValueInvalidTypeException(JValueType.PATHSYSTEM, type);
	}

	/**
	 * constructs a new JValue with encapsulates a GraphElementClass (also
	 * called type)
	 */
	public JValue(AttributedElementClass type) {
		this.type = JValueType.ATTRIBUTEDELEMENTCLASS;
		this.value = type;
		browsingInfo = null;
	}

	/**
	 * constructs a new JValue with encapsulates a GraphElementClass (also
	 * called type)
	 */
	public JValue(AttributedElementClass type, AttributedElement browsingInfo) {
		this(type);
		this.browsingInfo = browsingInfo;
	}

	/**
	 * @return true if this JValue encapsulates a {@link AttributedElementClass}
	 *         , false otherwise
	 */
	public boolean isAttributedElementClass() {
		return (type == JValueType.ATTRIBUTEDELEMENTCLASS);
	}

	/**
	 * @return the encapsulated AttributedElementClass
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a GraphElementClass
	 */
	public AttributedElementClass toAttributedElementClass()
			throws JValueInvalidTypeException {
		if (isAttributedElementClass()) {
			return (AttributedElementClass) value;
		}
		throw new JValueInvalidTypeException(JValueType.ATTRIBUTEDELEMENTCLASS,
				type);
	}

	/**
	 * @return true if this JValue encapsulates a GraphElementClass, false
	 *         otherwise
	 */
	public boolean isJValueTypeCollection() {
		return (type == JValueType.TYPECOLLECTION);
	}

	/**
	 * @return the encapsulated AttributedElementClass
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a GraphElementClass
	 */
	public JValueTypeCollection toJValueTypeCollection()
			throws JValueInvalidTypeException {
		if (isJValueTypeCollection()) {
			return (JValueTypeCollection) this;
		}
		throw new JValueInvalidTypeException(JValueType.TYPECOLLECTION, type);
	}

	/**
	 * constructs a new JValue with encapsulates a AttributedElement
	 */
	public JValue(AttributedElement elem) {
		this.type = JValueType.ATTRIBUTEDELEMENT;
		this.value = elem;
		browsingInfo = elem;
	}

	/**
	 * constructs a new JValue with encapsulates a AttributedElement
	 */
	public JValue(AttributedElement elem, AttributedElement browsingInfo) {
		this(elem);
		this.browsingInfo = browsingInfo;
	}

	/**
	 * @return true if this JValue encapsulates a graphlement value, that is
	 *         either a vertex or an edge, false otherwise
	 */
	public boolean isAttributedElement() {
		return (type == JValueType.ATTRIBUTEDELEMENT) || isEdge() || isVertex();
	}

	/**
	 * @return the encapsulated AttributedElement
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a AttributedElement
	 */
	public AttributedElement toAttributedElement()
			throws JValueInvalidTypeException {
		if (isAttributedElement() || (canConvert(JValueType.ATTRIBUTEDELEMENT))) {
			return (AttributedElement) value;
		}
		throw new JValueInvalidTypeException(JValueType.ATTRIBUTEDELEMENT, type);
	}

	/**
	 * constructs a new JValue with encapsulates a TrivalentBoolean value
	 */
	public JValue(Boolean b) {
		this.type = JValueType.BOOLEAN;
		this.value = b;
		browsingInfo = null;
	}

	/**
	 * constructs a new JValue with encapsulates a TrivalentBoolean value
	 */
	public JValue(Boolean b, AttributedElement browsingInfo) {
		this(b);
		this.browsingInfo = browsingInfo;
	}

	/**
	 * @return true if this JValue encapsulates a boolean value, false otherwise
	 */
	public boolean isBoolean() {
		return (type == JValueType.BOOLEAN);
	}

	/**
	 * @return the encapsulated boolean value
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a boolean value
	 */
	public Boolean toBoolean() throws JValueInvalidTypeException {
		if (isBoolean()) {
			return (Boolean) value;
		}
		throw new JValueInvalidTypeException(JValueType.BOOLEAN, type);
	}

	/**
	 * @return true if this JValue encapsulates a integer value, false otherwise
	 */
	public boolean isInteger() {
		return (type == JValueType.INTEGER);
	}

	public boolean isNumber() {
		return isInteger() || isLong() || isDouble();
	}

	/**
	 * @return the encapsulated integer value
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate an integer value
	 */
	public Integer toInteger() throws JValueInvalidTypeException {
		if (isInteger() || (canConvert(JValueType.INTEGER))) {
			return (Integer) value;
		}
		throw new JValueInvalidTypeException(JValueType.INTEGER, type);
	}

	/**
	 * constructs a new JValue with encapsulates a Integer or int value
	 */
	public JValue(Integer i) {
		this.type = JValueType.INTEGER;
		this.value = i;
		browsingInfo = null;
	}

	/**
	 * constructs a new JValue with encapsulates a Integer or int value
	 */
	public JValue(Integer i, AttributedElement browsingInfo) {
		this(i);
		this.browsingInfo = browsingInfo;
	}

	/**
	 * creates a new JValue which encapsulates the given Long
	 */
	public JValue(Long l) {
		this.type = JValueType.LONG;
		this.value = l;
		browsingInfo = null;
	}

	/**
	 * creates a new JValue which encapsulates the given Long
	 */
	public JValue(Long l, AttributedElement browsingInfo) {
		this(l);
		this.browsingInfo = browsingInfo;
	}

	/**
	 * @return true if this JValue encapsulates a long value, false otherwise
	 */
	public boolean isLong() {
		return (type == JValueType.LONG);
	}

	/**
	 * @return the encapsulated long value
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a long value
	 */
	public Long toLong() throws JValueInvalidTypeException {
		if (isLong() || (canConvert(JValueType.LONG))) {
			if (value instanceof Long) {
				return (Long) value;
			}
			if (value instanceof Integer) {
				return (long) (((Integer) value));
			}
		}
		throw new JValueInvalidTypeException(JValueType.LONG, type);
	}

	/**
	 * creates a new JValue which encapsulates the given Double
	 */
	public JValue(Double d) {
		this.type = JValueType.DOUBLE;
		this.value = d;
		browsingInfo = null;
	}

	/**
	 * creates a new JValue which encapsulates the given Double
	 */
	public JValue(Double d, AttributedElement browsingInfo) {
		this(d);
		this.browsingInfo = browsingInfo;
	}

	/**
	 * @return true if this JValue encapsulates a Double value, false otherwise
	 */
	public boolean isDouble() {
		return (type == JValueType.DOUBLE);
	}

	/**
	 * @return the encapsulated Double value
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a T value
	 */
	public Double toDouble() throws JValueInvalidTypeException {
		if (isDouble() || (canConvert(JValueType.DOUBLE))) {
			if (isInteger()) {
				int i = (Integer) value;
				return Double.valueOf(i);
			} else if (isLong()) {
				long l = (Long) value;
				return Double.valueOf(l);
			}
			return (Double) value;
		}
		throw new JValueInvalidTypeException(JValueType.DOUBLE, type);
	}

	/**
	 * creates a new JValue which encapsulates the given Character
	 */
	public JValue(Character c) {
		this.type = JValueType.CHARACTER;
		this.value = c;
		browsingInfo = null;
	}

	/**
	 * creates a new JValue which encapsulates the given Character
	 */
	public JValue(Character c, AttributedElement browsingInfo) {
		this(c);
		this.browsingInfo = browsingInfo;
	}

	/**
	 * @return true if this JValue encapsulates a Char value, false otherwise
	 */
	public boolean isCharacter() {
		return (type == JValueType.CHARACTER);
	}

	/**
	 * @return the encapsulated Character value
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a T value
	 */
	public Character toCharacter() throws JValueInvalidTypeException {
		if (isCharacter()) {
			return (Character) value;
		}
		throw new JValueInvalidTypeException(JValueType.CHARACTER, type);
	}

	/**
	 * creates a new JValue which encapsulates the given String
	 */
	public JValue(String s) {
		this.type = JValueType.STRING;
		this.value = s;
		browsingInfo = null;
	}

	/**
	 * creates a new JValue which encapsulates the given String
	 */
	public JValue(String s, AttributedElement browsingInfo) {
		this(s);
		this.browsingInfo = browsingInfo;
	}

	/**
	 * @return true if this JValue encapsulates a String value, false otherwise
	 */
	public boolean isString() {
		return (type == JValueType.STRING);
	}

	/**
	 * Returns the encapsulated Stringvalue or transforms the encapsulted value
	 * to its string representation usinng object.toString
	 * 
	 * @return the encapsulated String value or a String representation of the
	 *         encapsulated vale if it's not a string
	 * 
	 */
	@Override
	public String toString() {
		if (isString()) {
			return (String) value;
		} else if (isValid()) {
			return value.toString();
		} else {
			return "null";
		}
	}

	/**
	 * creates a new JValue which encapsulates the given String
	 */
	@SuppressWarnings("unchecked")
	public JValue(Enum e) {
		this.type = JValueType.ENUMVALUE;
		this.value = e;
		browsingInfo = null;
	}

	/**
	 * creates a new JValue which encapsulates the given String
	 */
	@SuppressWarnings("unchecked")
	public JValue(Enum e, AttributedElement browsingInfo) {
		this(e);
		this.browsingInfo = browsingInfo;
	}

	/**
	 * @return true if this JValue encapsulates a String value, false otherwise
	 */
	public boolean isEnum() {
		return (type == JValueType.ENUMVALUE);
	}

	/**
	 * Returns the encapsulated Stringvalue or transforms the encapsulted value
	 * to its string representation usinng object.toString
	 * 
	 * @return the encapsulated String value or a String representation of the
	 *         encapsulated vale if it's not a string
	 * 
	 */
	public String toEnum() {
		return value.toString();
	}

	/**
	 * @return true if this JValue encapsulates a Vertex value, false otherwise
	 */
	public boolean isVertex() {
		return (type == JValueType.VERTEX);
	}

	/**
	 * @return the encapsulated Vertex value
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a T value
	 */
	public Vertex toVertex() throws JValueInvalidTypeException {
		if (isVertex()) {
			return (Vertex) value;
		}
		throw new JValueInvalidTypeException(JValueType.VERTEX, type);
	}

	/**
	 * creates a new JValue which encapsulates the given vertex
	 */
	public JValue(Vertex vertex) {
		this.type = JValueType.VERTEX;
		this.value = vertex;
		browsingInfo = vertex;
	}

	/**
	 * creates a new JValue which encapsulates the given vertex
	 */
	public JValue(Vertex vertex, AttributedElement browsingInfo) {
		this(vertex);
		this.browsingInfo = browsingInfo;
	}

	/**
	 * @return true if this JValue encapsulates a Edge value, false otherwise
	 */
	public boolean isEdge() {
		return (type == JValueType.EDGE);
	}

	/**
	 * @return the encapsulated Vertex value
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a T value
	 */
	public Edge toEdge() throws JValueInvalidTypeException {
		if (isEdge()) {
			return (Edge) value;
		}
		throw new JValueInvalidTypeException(JValueType.EDGE, type);
	}

	/**
	 * creates a new JValue which encapsulates the given edge
	 */
	public JValue(Edge edge) {
		this.type = JValueType.EDGE;
		this.value = edge;
		browsingInfo = edge;
	}

	/**
	 * creates a new JValue which encapsulates the given edge
	 */
	public JValue(Edge edge, AttributedElement browsingInfo) {
		this(edge);
		this.browsingInfo = browsingInfo;
	}

	/**
	 * creates a new JValue which encapsulates the given graph
	 */
	public JValue(Graph graph) {
		this.type = JValueType.GRAPH;
		this.value = graph;
		browsingInfo = null;
	}

	/**
	 * creates a new JValue which encapsulates the given graph
	 */
	public JValue(Graph graph, AttributedElement browsingInfo) {
		this(graph);
		this.browsingInfo = browsingInfo;
	}

	/**
	 * @return true if this JValue encapsulates a Graph value, false otherwise
	 */
	public boolean isGraph() {
		return (type == JValueType.GRAPH);
	}

	/**
	 * @return the encapsulated Vertex value
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a T value
	 */
	public Graph toGraph() throws JValueInvalidTypeException {
		if (isGraph()) {
			return (Graph) value;
		}
		throw new JValueInvalidTypeException(JValueType.GRAPH, type);
	}

	/**
	 * creates a new JValue which encapsulates the given NFA
	 */
	public JValue(NFA nfa) {
		this.type = JValueType.NFA;
		this.value = nfa;
		browsingInfo = null;
	}

	/**
	 * creates a new JValue which encapsulates the given NFA
	 */
	public JValue(NFA nfa, AttributedElement browsingInfo) {
		this(nfa);
		this.browsingInfo = browsingInfo;
	}

	/**
	 * return true if this JValue is a NFA, false Otherwise
	 */
	public boolean isNFA() {
		return (type == JValueType.NFA);
	}

	/**
	 * @return the encapsulated NFA value
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a NFA
	 */
	public NFA toNFA() throws JValueInvalidTypeException {
		if (isNFA()) {
			return (NFA) value;
		}
		throw new JValueInvalidTypeException(JValueType.NFA, type);
	}

	/**
	 * return true if this JValue is a DFA, false Otherwise
	 */
	public boolean isDFA() {
		return (type == JValueType.DFA);
	}

	/**
	 * @return the encapsulated DFA
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a DFA
	 */
	public DFA toDFA() throws JValueInvalidTypeException {
		if (isDFA()) {
			return (DFA) value;
		}
		throw new JValueInvalidTypeException(JValueType.DFA, type);
	}

	/**
	 * creates a new JValue which encapsulates the given DFA
	 */
	public JValue(DFA dfa) {
		this.type = JValueType.DFA;
		this.value = dfa;
		browsingInfo = null;
	}

	/**
	 * creates a new JValue which encapsulates the given DFA
	 */
	public JValue(DFA dfa, AttributedElement browsingInfo) {
		this(dfa);
		this.browsingInfo = browsingInfo;
	}

	/**
	 * creates a new JValue which encapsulates the given Object
	 */
	public JValue(Object o) {
		this.type = JValueType.OBJECT;
		this.value = o;
		browsingInfo = null;
	}

	/**
	 * creates a new JValue which encapsulates the given Object
	 */
	public JValue(Object o, AttributedElement browsingInfo) {
		this(o);
		this.browsingInfo = browsingInfo;
	}

	/**
	 * @return true if this JValue encapsulates a Object value, false otherwise
	 */
	public boolean isObject() {
		return (type == JValueType.OBJECT);
	}

	/**
	 * @return the encapsulated Object value
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a T value
	 */
	public Object toObject() throws JValueInvalidTypeException {
		if (isObject() || (canConvert(JValueType.OBJECT))) {
			return value;
		}
		throw new JValueInvalidTypeException(JValueType.OBJECT, type);
	}

	/**
	 * 
	 * @return true if this JValue is a Collection, false otherwise
	 */
	public boolean isCollection() {
		return false;
	}

	/**
	 * 
	 * @return true if this JValue is a Map, false otherwise
	 */
	public boolean isMap() {
		return false;
	}

	/**
	 * returns a JValueCollection-Reference of this JValue
	 * 
	 * @throws JValueInvalidTypeException
	 *             if the JValue cannot be converted to a collection
	 */
	public JValueCollection toCollection() throws JValueInvalidTypeException {
		throw new JValueInvalidTypeException(JValueType.COLLECTION, type);
	}

	/**
	 * returns a JValueSet-Reference of this JValue
	 * 
	 * @throws ValueInvalidTypeException
	 *             if the JValue cannot be converted to a set
	 */
	public JValueSet toJValueSet() throws JValueInvalidTypeException {
		throw new JValueInvalidTypeException(JValueType.COLLECTION, type);
	}

	/**
	 * returns a JValueBag-Reference of this JValue
	 * 
	 * @throws ValueInvalidTypeException
	 *             if the JValue cannot be converted to a bag
	 */
	public JValueBag toJValueBag() throws JValueInvalidTypeException {
		throw new JValueInvalidTypeException(JValueType.COLLECTION, type);
	}

	/**
	 * returns a JValueTable-Reference of this JValue
	 * 
	 * @throws ValueInvalidTypeException
	 *             if the JValue cannot be converted to a table
	 */
	public JValueTable toJValueTable() throws JValueInvalidTypeException {
		throw new JValueInvalidTypeException(JValueType.COLLECTION, type);
	}

	/**
	 * returns a JValueList-Reference of this JValue
	 * 
	 * @throws ValueInvalidTypeException
	 *             if the JValue cannot be converted to a list
	 */
	public JValueList toJValueList() throws JValueInvalidTypeException {
		throw new JValueInvalidTypeException(JValueType.COLLECTION, type);
	}

	/**
	 * returns a JValueMap-Reference of this JValue
	 * 
	 * @throws ValueInvalidTypeException
	 *             if the JValue cannot be converted to a map
	 */
	public JValueMap toJValueMap() throws JValueInvalidTypeException {
		throw new JValueInvalidTypeException(JValueType.MAP, type);
	}

	/**
	 * returns a JValueTuple-Reference of this JValue
	 * 
	 * @throws ValueInvalidTypeException
	 *             if the JValue cannot be converted to a tuple
	 */
	public JValueTuple toJValueTuple() throws JValueInvalidTypeException {
		throw new JValueInvalidTypeException(JValueType.COLLECTION, type);
	}

	/**
	 * returns a JValueRecord-Reference of this JValue
	 * 
	 * @throws ValueInvalidTypeException
	 *             if the JValue cannot be converted to a record
	 */
	public JValueRecord toJValueRecord() throws JValueInvalidTypeException {
		throw new JValueInvalidTypeException(JValueType.RECORD, type);
	}

	/**
	 * constructs a new invalid JValue. Is only called in subclasses
	 */
	public JValue(BooleanGraphMarker t) {
		type = JValueType.SUBGRAPHTEMPATTRIBUTE;
		value = t;
		browsingInfo = null;
	}

	/**
	 * constructs a new invalid JValue. Is only called in subclasses
	 */
	public JValue(BooleanGraphMarker t, AttributedElement browsingInfo) {
		this(t);
		this.browsingInfo = browsingInfo;
	}

	/**
	 * @return true if this JValue encapsulates a boolean value, false otherwise
	 */
	public boolean isSubgraphTempAttribute() {
		return (type == JValueType.SUBGRAPHTEMPATTRIBUTE);
	}

	/**
	 * @return the encapsulated boolean value
	 * @throws JValueInvalidTypeException
	 *             if this JValue does not encapsulate a boolean value
	 */
	public BooleanGraphMarker toSubgraphTempAttribute()
			throws JValueInvalidTypeException {
		if (isSubgraphTempAttribute()) {
			return (BooleanGraphMarker) value;
		}
		throw new JValueInvalidTypeException(JValueType.SUBGRAPHTEMPATTRIBUTE,
				type);
	}

	/**
	 * @return true, if the encapsulated JValue can be converted to the given
	 *         type, false otherwise
	 */
	public boolean canConvert(JValueType atype) {
		return conversionCosts(atype) >= 0;
	}

	/**
	 * @param atype
	 *            a {@link JValueType}
	 * @return -1 it the encapsulated value cannot be converted to
	 *         <code>atype</code>, 0 if the value is-a <code>atype</code> and
	 *         the conversion costs in all other cases.
	 * 
	 *         Conversion to string and object are expensive, so that in GReQL
	 *         functions the most special is used independently of the
	 *         declaration order.
	 */
	public int conversionCosts(JValueType atype) {
		if (this.type == atype) {
			return 0;
		}
		if (type == null) {
			return 50;
		}
		if (atype == JValueType.STRING) {
			// String representation
			return 100;
		}
		if (atype == JValueType.OBJECT) {
			return 100;
		}
		switch (this.type) {
		case BOOLEAN:
			return -1;
		case CHARACTER:
			switch (atype) {
			case STRING:
				return 2;
			default:
				return -1;
			}
		case INTEGER:
			switch (atype) {
			case LONG:
				return 1;
			case NUMBER:
				return 0;
			case DOUBLE:
				return 2;
			default:
				return -1;
			}
		case LONG:
			switch (atype) {
			case DOUBLE:
				return 2;
			case NUMBER:
				return 0;
			default:
				return -1;
			}
		case DOUBLE:
			switch (atype) {
			case NUMBER:
				return 0;
			default:
				return -1;
			}
		case VERTEX:
			switch (atype) {
			case ATTRIBUTEDELEMENT:
				return 0;
			default:
				return -1;
			}
		case EDGE:
			switch (atype) {
			case ATTRIBUTEDELEMENT:
				return 0;
			default:
				return -1;
			}
		case GRAPH:
			switch (atype) {
			case ATTRIBUTEDELEMENT:
				return 0;
			default:
				return -1;
			}
		case OBJECT:
			return 5;
		}
		return -1;
	}

	/**
	 * encapsulates the given object in a jvalue. Doesn't create an
	 * object-jvalue per default but tries to determine the class of the object.
	 * Use it with care, because it's slow....
	 * 
	 * @param o
	 *            the object to encapsulte
	 * @return the encapsulated object
	 */
	@SuppressWarnings("unchecked")
	public static JValue fromObject(Object o) {
		if (o == null) {
			return new JValue();
		}
		Class objectsClass = o.getClass();
		if (objectsClass == String.class) {
			return new JValue((String) o);
		}
		if (o instanceof Enum) {
			return new JValue((Enum) o);
		}
		if (objectsClass == Integer.class) {
			return new JValue((Integer) o);
		}
		if (objectsClass == Long.class) {
			return new JValue((Long) o);
		}
		if (objectsClass == Double.class) {
			return new JValue((Double) o);
		}
		if (objectsClass == Boolean.class) {
			return new JValue((Boolean) o);
		}
		if (objectsClass == JValueBoolean.class) {
			return new JValue(o);
		}
		if (objectsClass == Character.class) {
			return new JValue((Character) o);
		}
		if (o instanceof Edge) {
			return new JValue((Edge) o);
		}
		if (o instanceof Vertex) {
			return new JValue((Vertex) o);
		}
		if (o instanceof Graph) {
			return new JValue((Graph) o);
		}
		return new JValue(o);
	}

	/**
	 * encapsulates the given object in a jvalue. Doesn't create a object-jvalue
	 * per default but tries to determine the class of the object. Use it with
	 * care, because it's slow....
	 * 
	 * @param o
	 *            the object to encapsulte
	 * @param browsingInfo
	 *            the AttributedElement to set as browsing info
	 * @return the encapsulated object
	 */
	public static JValue fromObject(Object o, AttributedElement browsingInfo) {
		JValue j = fromObject(o);
		j.setBrowsingInfo(browsingInfo);
		return j;
	}

	public Number toNumber() {
		if (isNumber() || (canConvert(JValueType.NUMBER))) {
			return (Number) value;
		}
		throw new JValueInvalidTypeException(JValueType.NUMBER, type);
	}

}
