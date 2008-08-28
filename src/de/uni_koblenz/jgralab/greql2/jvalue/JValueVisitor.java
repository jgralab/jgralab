/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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
 * This interface declares the methods that are used to traverse a complex structure of JValues
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> 
 * Summer 2006, Diploma Thesis
 */ 
public interface JValueVisitor {
	
	
	/**
	 * Method to visit a JValueSet
	 * @param s the set to visit
	 */
	public void visitSet(JValueSet s) throws Exception;
	
	/**
	 * Method to visit a JValueBag
	 * @param b the bag to visit
	 */
	public void visitBag(JValueBag b) throws Exception;

	/**
	 * Method to visit a JValueTable
	 * @param t table
	 */
	public void visitTable(JValueTable t) throws Exception;
	
	
	/**
	 * Method to visit a JValueList
	 * @param b the list to visit
	 */
	public void visitList(JValueList b) throws Exception;

	
	/**
	 * Method to visit a JValueTuple
	 * @param t the tuple to visit
	 */
	public void visitTuple(JValueTuple t) throws Exception;
	
	
	/**
	 * Method to visit a JValueRecord
	 * @param r the record to visit
	 */
	public void visitRecord(JValueRecord r) throws Exception;
	
	
	/**
	 * Method to visit a Path
	 * @param p the path to visit
	 */
	public void visitPath(JValuePath p) throws Exception;
	
	
	/**
	 * Method to visit a PathSystem
	 * @param p the pathSystem to visit
	 */
	public void visitPathSystem(JValuePathSystem p) throws Exception;
	
	/**
	 * Method to visit a Slice
	 * @param s the slice to visit
	 */
	public void visitSlice(JValueSlice s) throws Exception;
		
	/**
	 * Method to visit a Vertex
	 * @param v the Vertex to visit
	 */
	public void visitVertex(JValue v) throws Exception;
	
	
	/**
	 * Method to visit an Edge
	 * @param e the edge to visit
	 */
	public void visitEdge(JValue e) throws Exception;
	
	/**
	 * Method to visit a Number
	 * @param n the number to visit
	 */
	public void visitInt(JValue n) throws Exception;
	
	/**
	 * Method to visit a Number
	 * @param n the number to visit
	 */
	public void visitLong(JValue n) throws Exception;
	
	/**
	 * Method to visit a Number
	 * @param n the number to visit
	 */
	public void visitDouble(JValue n) throws Exception;
	
	/**
	 * Method to visit a Character
	 * @param c the Character to visit
	 */
	public void visitChar(JValue c) throws Exception;
	
	
	/**
	 * Method to visit a String
	 * @param s the string to visit
	 */
	public void visitString(JValue s) throws Exception;
	
	
	/**
	 * Method to visit an EnumValue
	 */
	public void visitEnumValue(JValue e) throws Exception;
	
	/**
	 * Method to visit a Graph
	 * @param g the graph to visit
	 */
	public void visitGraph(JValue g) throws Exception;
	
	
	/**
	 * Method to visit a SubgraphTempAttribute
	 * @param s the subgraph to visit
	 */
	public void visitSubgraph(JValue s) throws Exception;
	
	
	/**
	 * The method to visit a DFA
	 * @param d the dfa to visit
	 */
	public void visitDFA(JValue d) throws Exception;
	
	
	/**
	 * The method to visit a NFA
	 * @param n the nfa to visit
	 */
	public void visitNFA(JValue n) throws Exception;
	
	/**
	 * The method to visit a invalid value
	 * @param i the invalid value to visit
	 */
	public void visitInvalid(JValue i) throws Exception;
	
	/**
	 * The method to visit a boolean value
	 * @param b the boolean value to visit
	 */
	public void visitBoolean(JValue b) throws Exception;
	
	
	/**
	 * The method to visit a TrivalentBoolean value
	 * @param b the TrivalentBoolean value to visit
	 */
	public void visitTrivalentBoolean(JValue b) throws Exception;
	
	/**
	 * The method to visit a Object value
	 * @param o the object value to visit
	 */
	public void visitObject(JValue o) throws Exception;
	
	/**
	 * The method to visit a AttributedElementClass
	 * @param a the AttributedElementClass to visit
	 */
	public void visitAttributedElementClass(JValue a) throws Exception;
	
	/**
	 * The method to visit a type collection
	 * @param a the type collection to visit
	 */
	public void visitTypeCollection(JValue a) throws Exception;
	
	
	/**
	 * The method to visit a state
	 * @param s the state to visit
	 */
	public void visitState(JValue s) throws Exception; 
	
	/**
	 * The Transition to visit
	 * @param t the transition to visit
	 */
	public void visitTransition(JValue t) throws Exception;
	
	/**
	 * The method to visit a variable declaration
	 * @param d the VariableDeclaration value to visit
	 */
	public void visitDeclaration(JValue d) throws Exception;
	
	/**
	 * The method to visit a variable declaration layer
	 * @param d the VariableDeclarationLayer to visit
	 */
	public void visitDeclarationLayer(JValue d) throws Exception;
	
	/**
	 * This method should be called after the last element in a collection
	 * was visited
	 */
	public void post() throws Exception;
	
	/**
	 * This method should be called before the last element in a collection
	 * was visited
	 */
	public void pre() throws Exception;
	
	/**
	 * This method should be called between two elements a collection
	 * are visited
	 */
	public void inter() throws Exception;
	
	/**
	 * This method should be called when the visitor is created
	 */
	public void head() throws Exception;
	
	/**
	 * This method should be called after the last element was visited
	 */
	public void foot() throws Exception;
	
	
	
}
