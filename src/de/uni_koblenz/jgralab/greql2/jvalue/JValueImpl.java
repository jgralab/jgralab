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

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.AbstractGraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.FiniteAutomaton;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class JValueImpl implements JValue {

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#getBrowsingInfo()
	 */
	public AttributedElement getBrowsingInfo() {
		return browsingInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValue#setBrowsingInfo(de.uni_koblenz
	 * .jgralab.AttributedElement)
	 */
	public void setBrowsingInfo(AttributedElement bInfo) {
		storedHashCode = 0;
		browsingInfo = bInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValue#accept(de.uni_koblenz.jgralab
	 * .greql2.jvalue.JValueVisitor)
	 */
	public void accept(JValueVisitor v) {
		if (this.type == null) {
			v.visitObject(this);
			return;
		}
		switch (this.type) {
		case BOOL:
			v.visitBoolean(this);
			return;
		case INT:
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
		case ATTRELEMCLASS:
			v.visitAttributedElementClass(this);
			return;
		case INTERNAL:
			return;
		case AUTOMATON:
			v.visitDFA(this);
			return;
		case EDGE:
			v.visitEdge(this);
			return;
		case GRAPH:
			v.visitGraph(this);
			return;
		case MARKER:
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public int compareTo(JValue o) {
		if (o.getType() == null) {
			return 1;
		}
		if (type == null) {
			return -1;
		}
		if (type != o.getType()) {
			return (type.compareTo(o.getType()));
		}
		// ok, types are identical
		switch (type) {
		case BOOL:
			// true first!
			boolean t = toBoolean();
			boolean other = o.toBoolean();
			if (!t && other) {
				return 1;
			}
			if (t == other) {
				return 0;
			}
			return -1;
		case DOUBLE:
			return Double.compare(toDouble(), o.toDouble());
		case EDGE:
			return Integer.valueOf(toEdge().getId()).compareTo(
					Integer.valueOf(o.toEdge().getId()));
		case ENUMVALUE:
			return ((Enum) toEnum()).compareTo(o.toEnum());
		case INT:
			return toInteger().compareTo(o.toInteger());
		case LONG:
			return toLong().compareTo(o.toLong());
		case STRING:
			return toString().compareTo(o.toString());
		case VERTEX:
			Integer.valueOf(toVertex().getId()).compareTo(
					Integer.valueOf(o.toVertex().getId()));
		default:
			return Integer.valueOf(hashCode()).compareTo(o.hashCode());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#getType()
	 */
	public JValueType getType() {
		return type;
	}

	/**
	 * constructs a new invalid JValue.
	 */
	public JValueImpl() {
		value = null;
		browsingInfo = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#isValid()
	 */
	public boolean isValid() {
		return type != null;
	}

	/**
	 * returns true if this JValue and the given object are equal. That means,
	 * the given object is a JValue which contains an object of the same type
	 * like this one and these objects are equal
	 */
	@Override
	public boolean equals(Object anObject) {
		if (anObject instanceof JValueImpl) {
			JValueImpl anotherValue = (JValueImpl) anObject;
			if (this.value == null) {
				return anotherValue.value == null;
			} else {
				if ((anotherValue.type == this.type)
						|| (anotherValue.type == JValueType.OBJECT)
						|| (this.type == JValueType.OBJECT)) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#hashCode()
	 */
	@Override
	public int hashCode() {
		if (storedHashCode == 0) {
			storedHashCode = 7;
			storedHashCode = 31 * storedHashCode + JValueImpl.class.hashCode();
			storedHashCode = 31 * storedHashCode
					+ (value == null ? 0 : value.hashCode());
		}
		return storedHashCode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#isVariableDeclaration()
	 */
	public boolean isInternal() {
		return (type == JValueType.INTERNAL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#isPath()
	 */
	public boolean isPath() {
		return (this.type == JValueType.PATH);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#toPath()
	 */
	public JValuePath toPath() throws JValueInvalidTypeException {
		throw new JValueInvalidTypeException(JValueType.PATH, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#isPathSystem()
	 */
	public boolean isPathSystem() {
		return (this.type == JValueType.PATHSYSTEM);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#toPathSystem()
	 */
	public JValuePathSystem toPathSystem() throws JValueInvalidTypeException {
		throw new JValueInvalidTypeException(JValueType.PATHSYSTEM, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#toSlice()
	 */
	public JValueSlice toSlice() throws JValueInvalidTypeException {
		throw new JValueInvalidTypeException(JValueType.SLICE, type);
	}

	/**
	 * constructs a new JValue with encapsulates a GraphElementClass (also
	 * called type)
	 */
	public JValueImpl(AttributedElementClass type) {
		this.type = JValueType.ATTRELEMCLASS;
		this.value = type;
		browsingInfo = null;
	}

	/**
	 * constructs a new JValue with encapsulates a GraphElementClass (also
	 * called type)
	 */
	public JValueImpl(AttributedElementClass type,
			AttributedElement browsingInfo) {
		this(type);
		this.browsingInfo = browsingInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValue#isAttributedElementClass()
	 */
	public boolean isAttributedElementClass() {
		return (type == JValueType.ATTRELEMCLASS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValue#toAttributedElementClass()
	 */
	public AttributedElementClass toAttributedElementClass()
			throws JValueInvalidTypeException {
		if (isAttributedElementClass()) {
			return (AttributedElementClass) value;
		}
		throw new JValueInvalidTypeException(JValueType.ATTRELEMCLASS, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#isJValueTypeCollection()
	 */
	public boolean isJValueTypeCollection() {
		return (type == JValueType.TYPECOLLECTION);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#toJValueTypeCollection()
	 */
	public JValueTypeCollection toJValueTypeCollection()
			throws JValueInvalidTypeException {
		if (isJValueTypeCollection()) {
			return (JValueTypeCollection) this;
		}
		throw new JValueInvalidTypeException(JValueType.TYPECOLLECTION, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#isAttributedElement()
	 */
	public boolean isAttributedElement() {
		return (isEdge() || isVertex() || isGraph());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#toAttributedElement()
	 */
	public AttributedElement toAttributedElement()
			throws JValueInvalidTypeException {
		if (isAttributedElement()) {
			return (AttributedElement) value;
		}
		throw new JValueInvalidTypeException(JValueType.ATTRELEM, type);
	}

	/**
	 * constructs a new JValue with encapsulates a TrivalentBoolean value
	 */
	public JValueImpl(Boolean b) {
		this.type = JValueType.BOOL;
		this.value = b;
		browsingInfo = null;
	}

	/**
	 * constructs a new JValue with encapsulates a TrivalentBoolean value
	 */
	public JValueImpl(Boolean b, AttributedElement browsingInfo) {
		this(b);
		this.browsingInfo = browsingInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#isBoolean()
	 */
	public boolean isBoolean() {
		return (type == JValueType.BOOL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#toBoolean()
	 */
	public Boolean toBoolean() throws JValueInvalidTypeException {
		if (isBoolean()) {
			return (Boolean) value;
		}
		throw new JValueInvalidTypeException(JValueType.BOOL, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#isInteger()
	 */
	public boolean isInteger() {
		return (type == JValueType.INT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#isNumber()
	 */
	public boolean isNumber() {
		return isInteger() || isLong() || isDouble();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#toInteger()
	 */
	public Integer toInteger() throws JValueInvalidTypeException {
		if (isInteger() || (canConvert(JValueType.INT))) {
			return (Integer) value;
		}
		throw new JValueInvalidTypeException(JValueType.INT, type);
	}

	/**
	 * constructs a new JValue with encapsulates a Integer or int value
	 */
	public JValueImpl(Integer i) {
		this.type = JValueType.INT;
		this.value = i;
		browsingInfo = null;
	}

	/**
	 * constructs a new JValue with encapsulates a Integer or int value
	 */
	public JValueImpl(Integer i, AttributedElement browsingInfo) {
		this(i);
		this.browsingInfo = browsingInfo;
	}

	/**
	 * creates a new JValue which encapsulates the given Long
	 */
	public JValueImpl(Long l) {
		this.type = JValueType.LONG;
		this.value = l;
		browsingInfo = null;
	}

	/**
	 * creates a new JValue which encapsulates the given Long
	 */
	public JValueImpl(Long l, AttributedElement browsingInfo) {
		this(l);
		this.browsingInfo = browsingInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#isLong()
	 */
	public boolean isLong() {
		return (type == JValueType.LONG);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#toLong()
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
	public JValueImpl(Double d) {
		this.type = JValueType.DOUBLE;
		this.value = d;
		browsingInfo = null;
	}

	/**
	 * creates a new JValue which encapsulates the given Double
	 */
	public JValueImpl(Double d, AttributedElement browsingInfo) {
		this(d);
		this.browsingInfo = browsingInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#isDouble()
	 */
	public boolean isDouble() {
		return (type == JValueType.DOUBLE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#toDouble()
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
	 * creates a new JValue which encapsulates the given String
	 */
	public JValueImpl(String s) {
		this.type = JValueType.STRING;
		this.value = s;
		browsingInfo = null;
	}

	/**
	 * creates a new JValue which encapsulates the given String
	 */
	public JValueImpl(String s, AttributedElement browsingInfo) {
		this(s);
		this.browsingInfo = browsingInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#isString()
	 */
	public boolean isString() {
		return (type == JValueType.STRING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#toString()
	 */
	@Override
	public String toString() {
		if (!isValid()) {
			assert value == null : "JValue.type is null, but value is '"
					+ value + "'";
			return "null";
		}
		return (value != null) ? value.toString() : "null";
	}

	/**
	 * creates a new JValue which encapsulates the given String
	 */
	public JValueImpl(Enum<?> e) {
		this.type = JValueType.ENUMVALUE;
		this.value = e;
		browsingInfo = null;
	}

	/**
	 * creates a new JValue which encapsulates the given String
	 */
	public JValueImpl(Enum<?> e, AttributedElement browsingInfo) {
		this(e);
		this.browsingInfo = browsingInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#isEnum()
	 */
	public boolean isEnum() {
		return (type == JValueType.ENUMVALUE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#toEnum()
	 */
	public Enum<?> toEnum() {
		if (isEnum()) {
			return (Enum<?>) value;
		}
		throw new JValueInvalidTypeException(JValueType.VERTEX, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#isVertex()
	 */
	public boolean isVertex() {
		return (type == JValueType.VERTEX);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#toVertex()
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
	public JValueImpl(Vertex vertex) {
		this.type = JValueType.VERTEX;
		this.value = vertex;
		browsingInfo = vertex;
	}

	/**
	 * creates a new JValue which encapsulates the given vertex
	 */
	public JValueImpl(Vertex vertex, AttributedElement browsingInfo) {
		this(vertex);
		this.browsingInfo = browsingInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#isEdge()
	 */
	public boolean isEdge() {
		return (type == JValueType.EDGE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#toEdge()
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
	public JValueImpl(Edge edge) {
		this.type = JValueType.EDGE;
		this.value = edge;
		browsingInfo = edge;
	}

	/**
	 * creates a new JValue which encapsulates the given edge
	 */
	public JValueImpl(Edge edge, AttributedElement browsingInfo) {
		this(edge);
		this.browsingInfo = browsingInfo;
	}

	/**
	 * creates a new JValue which encapsulates the given graph
	 */
	public JValueImpl(Graph graph) {
		this.type = JValueType.GRAPH;
		this.value = graph;
		browsingInfo = null;
	}

	/**
	 * creates a new JValue which encapsulates the given graph
	 */
	public JValueImpl(Graph graph, AttributedElement browsingInfo) {
		this(graph);
		this.browsingInfo = browsingInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#isGraph()
	 */
	public boolean isGraph() {
		return (type == JValueType.GRAPH);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#toGraph()
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
	public JValueImpl(FiniteAutomaton nfa) {
		this.type = JValueType.AUTOMATON;
		this.value = nfa;
		browsingInfo = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#isNFA()
	 */
	public boolean isAutomaton() {
		return (type == JValueType.AUTOMATON);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#toNFA()
	 */
	public FiniteAutomaton toAutomaton() throws JValueInvalidTypeException {
		if (isAutomaton()) {
			return (FiniteAutomaton) value;
		}
		throw new JValueInvalidTypeException(JValueType.AUTOMATON, type);
	}

	/**
	 * creates a new JValue which encapsulates the given Object
	 */
	public JValueImpl(Object o) {
		if (o instanceof Edge) {
			this.type = JValueType.EDGE;
		} else if (o instanceof Vertex) {
			this.type = JValueType.VERTEX;
		} else if (o instanceof Graph) {
			this.type = JValueType.GRAPH;
		} else {
			this.type = JValueType.OBJECT;
		}
		this.value = o;
		browsingInfo = null;
	}

	/**
	 * creates a new JValue which encapsulates the given Object
	 */
	public JValueImpl(Object o, AttributedElement browsingInfo) {
		this(o);
		this.browsingInfo = browsingInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#isObject()
	 */
	public boolean isObject() {
		return (type == JValueType.OBJECT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#toObject()
	 */
	public Object toObject() {
		if (isObject() || (canConvert(JValueType.OBJECT))) {
			return value;
		}
		throw new JValueInvalidTypeException(JValueType.OBJECT, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#isCollection()
	 */
	public boolean isCollection() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#isMap()
	 */
	public boolean isMap() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#toCollection()
	 */
	public JValueCollection toCollection() throws JValueInvalidTypeException {
		throw new JValueInvalidTypeException(JValueType.COLLECTION, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#toJValueSet()
	 */
	public JValueSet toJValueSet() throws JValueInvalidTypeException {
		throw new JValueInvalidTypeException(JValueType.COLLECTION, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#toJValueBag()
	 */
	public JValueBag toJValueBag() throws JValueInvalidTypeException {
		throw new JValueInvalidTypeException(JValueType.COLLECTION, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#toJValueTable()
	 */
	public JValueTable toJValueTable() throws JValueInvalidTypeException {
		throw new JValueInvalidTypeException(JValueType.COLLECTION, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#toJValueList()
	 */
	public JValueList toJValueList() throws JValueInvalidTypeException {
		throw new JValueInvalidTypeException(JValueType.COLLECTION, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#toJValueMap()
	 */
	public JValueMap toJValueMap() throws JValueInvalidTypeException {
		throw new JValueInvalidTypeException(JValueType.MAP, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#toJValueTuple()
	 */
	public JValueTuple toJValueTuple() throws JValueInvalidTypeException {
		throw new JValueInvalidTypeException(JValueType.COLLECTION, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#toJValueRecord()
	 */
	public JValueRecord toJValueRecord() throws JValueInvalidTypeException {
		throw new JValueInvalidTypeException(JValueType.RECORD, type);
	}

	/**
	 * constructs a new invalid JValue. Is only called in subclasses
	 */
	public JValueImpl(AbstractGraphMarker<?> t) {
		type = JValueType.MARKER;
		value = t;
		browsingInfo = null;
	}

	/**
	 * constructs a new invalid JValue. Is only called in subclasses
	 */
	public JValueImpl(AbstractGraphMarker<?> t, AttributedElement browsingInfo) {
		this(t);
		this.browsingInfo = browsingInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValue#isSubgraphTempAttribute()
	 */
	public boolean isGraphMarker() {
		return (type == JValueType.MARKER);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValue#toSubgraphTempAttribute()
	 */
	@SuppressWarnings("unchecked")
	public AbstractGraphMarker<AttributedElement> toGraphMarker()
			throws JValueInvalidTypeException {
		if (isGraphMarker()) {
			return (AbstractGraphMarker<AttributedElement>) value;
		}
		throw new JValueInvalidTypeException(JValueType.MARKER, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValue#canConvert(de.uni_koblenz
	 * .jgralab.greql2.jvalue.JValueType)
	 */
	public boolean canConvert(JValueType atype) {
		return conversionCosts(atype) >= 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.jvalue.JValue#conversionCosts(de.uni_koblenz
	 * .jgralab.greql2.jvalue.JValueType)
	 */
	public int conversionCosts(JValueType targetType) {
		if (this.type == targetType) {
			return 0;
		}
		if (type == null) {
			return 50;
		}
		if (targetType == JValueType.STRING) {
			// String representation
			return 100;
		}
		if (targetType == JValueType.OBJECT) {
			return 100;
		}
		switch (this.type) {
		case BOOL:
			return -1;
		case INT:
			switch (targetType) {
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
			switch (targetType) {
			case DOUBLE:
				return 2;
			case NUMBER:
				return 0;
			default:
				return -1;
			}
		case DOUBLE:
			switch (targetType) {
			case NUMBER:
				return 0;
			default:
				return -1;
			}
		case VERTEX:
			switch (targetType) {
			case ATTRELEM:
				return 0;
			default:
				return -1;
			}
		case EDGE:
			switch (targetType) {
			case ATTRELEM:
				return 0;
			default:
				return -1;
			}
		case GRAPH:
			switch (targetType) {
			case ATTRELEM:
				return 0;
			default:
				return -1;
			}
		case PATHSYSTEM:
			switch (targetType) {
			case COLLECTION:
				return 10;
			default:
				return -1;
			}
		case SLICE:
			switch (targetType) {
			case COLLECTION:
				return 10;
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
	public static JValueImpl fromObject(Object o) {
		if (o == null) {
			return new JValueImpl();
		}
		Class<?> objectsClass = o.getClass();
		if (objectsClass == String.class) {
			return new JValueImpl((String) o);
		}
		if (o instanceof Enum) {
			return new JValueImpl((Enum<?>) o);
		}
		if (objectsClass == Integer.class) {
			return new JValueImpl((Integer) o);
		}
		if (objectsClass == Long.class) {
			return new JValueImpl((Long) o);
		}
		if (objectsClass == Double.class) {
			return new JValueImpl((Double) o);
		}
		if (objectsClass == Boolean.class) {
			return new JValueImpl((Boolean) o);
		}
		if (objectsClass == JValueBoolean.class) {
			return new JValueImpl(o);
		}
		if (objectsClass == Character.class) {
			return new JValueImpl(o);
		}
		if (o instanceof Edge) {
			return new JValueImpl((Edge) o);
		}
		if (o instanceof Vertex) {
			return new JValueImpl((Vertex) o);
		}
		if (o instanceof Graph) {
			return new JValueImpl((Graph) o);
		}
		if (o instanceof Set) {
			JValueSet retVal = new JValueSet();
			for (Object member : ((Set<?>) o)) {
				retVal.add(JValueImpl.fromObject(member));
			}
			return retVal;
		}
		if (o instanceof List) {
			JValueList retVal = new JValueList();
			for (Object member : ((List<?>) o)) {
				retVal.add(JValueImpl.fromObject(member));
			}
			return retVal;
		}
		if (o instanceof Map) {
			JValueMap retVal = new JValueMap();
			Map<? extends Object, ? extends Object> m = (Map<?, ?>) o;
			for (Map.Entry<? extends Object, ? extends Object> entry : m
					.entrySet()) {
				retVal.put(JValueImpl.fromObject(entry.getKey()),
						JValueImpl.fromObject(entry.getValue()));
			}
			return retVal;
		}
		return new JValueImpl(o);
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
	public static JValueImpl fromObject(Object o, AttributedElement browsingInfo) {
		JValueImpl j = fromObject(o);
		j.setBrowsingInfo(browsingInfo);
		return j;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.jvalue.JValue#toNumber()
	 */
	public Number toNumber() {
		if (isNumber() || (canConvert(JValueType.NUMBER))) {
			return (Number) value;
		}
		throw new JValueInvalidTypeException(JValueType.NUMBER, type);
	}

	@Override
	public boolean isSlice() {
		return false;
	}
}
