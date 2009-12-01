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

/**
 * This interface declares the methods that are used to traverse a complex
 * structure of JValues
 * 
 * @author ist@uni-koblenz.de
 */
public interface JValueVisitor {

	/**
	 * Method to visit a JValueSet
	 * 
	 * @param s
	 *            the set to visit
	 */
	public void visitSet(JValueSet s);

	/**
	 * Method to visit a JValueBag
	 * 
	 * @param b
	 *            the bag to visit
	 */
	public void visitBag(JValueBag b);

	/**
	 * Method to visit a JValueMap
	 * 
	 * @param b
	 *            the bag to visit
	 */
	public void visitMap(JValueMap b);

	/**
	 * Method to visit a JValueTable
	 * 
	 * @param t
	 *            table
	 */
	public void visitTable(JValueTable t);

	/**
	 * Method to visit a JValueList
	 * 
	 * @param b
	 *            the list to visit
	 */
	public void visitList(JValueList b);

	/**
	 * Method to visit a JValueTuple
	 * 
	 * @param t
	 *            the tuple to visit
	 */
	public void visitTuple(JValueTuple t);

	/**
	 * Method to visit a JValueRecord
	 * 
	 * @param r
	 *            the record to visit
	 */
	public void visitRecord(JValueRecord r);

	/**
	 * Method to visit a Path
	 * 
	 * @param p
	 *            the path to visit
	 */
	public void visitPath(JValuePath p);

	/**
	 * Method to visit a PathSystem
	 * 
	 * @param p
	 *            the pathSystem to visit
	 */
	public void visitPathSystem(JValuePathSystem p);

	/**
	 * Method to visit a Slice
	 * 
	 * @param s
	 *            the slice to visit
	 */
	public void visitSlice(JValueSlice s);

	/**
	 * Method to visit a Vertex
	 * 
	 * @param v
	 *            the Vertex to visit
	 */
	public void visitVertex(JValue v);

	/**
	 * Method to visit an Edge
	 * 
	 * @param e
	 *            the edge to visit
	 */
	public void visitEdge(JValue e);

	/**
	 * Method to visit a Number
	 * 
	 * @param n
	 *            the number to visit
	 */
	public void visitInt(JValue n);

	/**
	 * Method to visit a Number
	 * 
	 * @param n
	 *            the number to visit
	 */
	public void visitLong(JValue n);

	/**
	 * Method to visit a Number
	 * 
	 * @param n
	 *            the number to visit
	 */
	public void visitDouble(JValue n);

	/**
	 * Method to visit a String
	 * 
	 * @param s
	 *            the string to visit
	 */
	public void visitString(JValue s);

	/**
	 * Method to visit an EnumValue
	 */
	public void visitEnumValue(JValue e);

	/**
	 * Method to visit a Graph
	 * 
	 * @param g
	 *            the graph to visit
	 */
	public void visitGraph(JValue g);

	/**
	 * Method to visit a SubgraphTempAttribute
	 * 
	 * @param s
	 *            the subgraph to visit
	 */
	public void visitSubgraph(JValue s);

	/**
	 * The method to visit a DFA
	 * 
	 * @param d
	 *            the dfa to visit
	 */
	public void visitDFA(JValue d);

	/**
	 * The method to visit a NFA
	 * 
	 * @param n
	 *            the nfa to visit
	 */
	public void visitNFA(JValue n);

	/**
	 * The method to visit a invalid value
	 * 
	 * @param i
	 *            the invalid value to visit
	 */
	public void visitInvalid(JValue i);

	/**
	 * The method to visit a boolean value
	 * 
	 * @param b
	 *            the boolean value to visit
	 */
	public void visitBoolean(JValue b);

	/**
	 * The method to visit a Object value
	 * 
	 * @param o
	 *            the object value to visit
	 */
	public void visitObject(JValue o);

	/**
	 * The method to visit a AttributedElementClass
	 * 
	 * @param a
	 *            the AttributedElementClass to visit
	 */
	public void visitAttributedElementClass(JValue a);

	/**
	 * The method to visit a type collection
	 * 
	 * @param a
	 *            the type collection to visit
	 */
	public void visitTypeCollection(JValue a);

	/**
	 * The method to visit a state
	 * 
	 * @param s
	 *            the state to visit
	 */
	public void visitState(JValue s);

	/**
	 * The Transition to visit
	 * 
	 * @param t
	 *            the transition to visit
	 */
	public void visitTransition(JValue t);

	/**
	 * The method to visit a variable declaration
	 * 
	 * @param d
	 *            the VariableDeclaration value to visit
	 */
	public void visitDeclaration(JValue d);

	/**
	 * The method to visit a variable declaration layer
	 * 
	 * @param d
	 *            the VariableDeclarationLayer to visit
	 */
	public void visitDeclarationLayer(JValue d);

	/**
	 * This method should be called after the last element in a collection was
	 * visited
	 */
	public void post();

	/**
	 * This method should be called before the first element in a collection was
	 * visited
	 */
	public void pre();

	/**
	 * This method should be called between two elements a collection are
	 * visited
	 */
	public void inter();

	/**
	 * This method should be called when the visitor is created
	 */
	public void head();

	/**
	 * This method should be called after the last element was visited
	 */
	public void foot();

	/**
	 * This method should be called by visit... implementations that can not
	 * handle a specific JValue type.
	 * 
	 * @throws a
	 *             JValueVisitorExeption indicating the problem.
	 */
	public void cantVisit(JValue v);

}
