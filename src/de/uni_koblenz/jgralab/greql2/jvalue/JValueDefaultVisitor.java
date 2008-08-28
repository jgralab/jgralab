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
import java.util.Iterator;

/**
 * This class implements a default visitor. It visits all elements in the structure given recursivly,
 * but it does nothing with them. To implement a own visitor, which for example prints alls vertices
 * to a list, extend this class an overwrite the method visitVertex(Vertex v)
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> 
 * Summer 2006, Diploma Thesis
 *
 */

public class JValueDefaultVisitor implements JValueVisitor {

	public void visitSet(JValueSet s) throws Exception {
		Iterator<JValue> iter = s.iterator();
		boolean first = true;
		pre();
		while (iter.hasNext()) {
			if (first)
				first = false;
			else
				inter();
			iter.next().accept(this);
		}
		post();
	}

	public void visitBag(JValueBag b) throws Exception {
		Iterator<JValue> iter = b.iterator();
		boolean first = true;
		pre();
		while (iter.hasNext()) {
			if (first)
				first = false;
			else
				inter();
			iter.next().accept(this);
		}
		post();
	}
	
	public void visitTable(JValueTable t) throws Exception {
		t.getHeader().accept(this);
		t.getData().accept(this);
	}

	public void visitList(JValueList b) throws Exception {
		Iterator<JValue> iter = b.iterator();
		boolean first = true;
		pre();
		while (iter.hasNext()) {
			if (first)
				first = false;
			else
				inter();
			iter.next().accept(this);
		}
		post();
	}

	public void visitTuple(JValueTuple t) throws Exception {
		Iterator<JValue> iter = t.iterator();
		boolean first = true;
		pre();
		while (iter.hasNext()) {
			if (first)
				first = false;
			else
				inter();
			iter.next().accept(this);
		}
		post();
	}

	public void visitRecord(JValueRecord r) throws Exception {
		Iterator<JValue> iter = r.iterator();
		boolean first = true;
		pre();
		while (iter.hasNext()) {
			if (first)
				first = false;
			else
				inter();
			iter.next().accept(this);
		}
		post();
	}

	public void visitPath(JValuePath p) throws Exception {
		Iterator<JValue> eiter = p.edgeTraceAsJValue().iterator();
		boolean first = true;
		pre();
		while (eiter.hasNext()) {
			if (first)
				first = false;
			else
				inter();
			eiter.next().accept(this);
		}
		post();
		Iterator<JValue> viter = p.nodeTraceAsJValue().iterator();
		first = true;
		pre();
		while (viter.hasNext()) {
			if (first)
				first = false;
			else
				inter();
			viter.next().accept(this);
		}
		post();
	}

	public void visitPathSystem(JValuePathSystem p) throws Exception {}
	
	public void visitSlice(JValueSlice s) throws Exception {}

	public void visitVertex(JValue v) throws Exception {}

	
	public void visitEdge(JValue e) throws Exception {}

	/**
	 * Method to visit a Number
	 * @param n the number to visit
	 */
	public void visitInt(JValue n) throws Exception {}
	
	/**
	 * Method to visit a Number
	 * @param n the number to visit
	 */
	public void visitLong(JValue n) throws Exception {}
	
	/**
	 * Method to visit a Number
	 * @param n the number to visit
	 */
	public void visitDouble(JValue n) throws Exception {}

	public void visitChar(JValue c) throws Exception {}

	public void visitString(JValue s) throws Exception {}
	
	public void visitEnumValue(JValue e) throws Exception {}

	public void visitGraph(JValue g) throws Exception {}

	public void visitSubgraph(JValue s) throws Exception {}

	public void visitDFA(JValue d) throws Exception {}

	public void visitNFA(JValue n) throws Exception {}

	public void visitInvalid(JValue i) throws Exception {}

	public void visitBoolean(JValue b) throws Exception {}

	public void visitTrivalentBoolean(JValue b) throws Exception {}

	public void visitObject(JValue o) throws Exception {}

	public void visitAttributedElementClass(JValue a) throws Exception {}
	
	public void visitTypeCollection(JValue a) throws Exception {}

	public void visitState(JValue s) throws Exception {}

	public void visitTransition(JValue t) throws Exception {}

	public void visitDeclaration(JValue d) throws Exception {}

	public void visitDeclarationLayer(JValue d) throws Exception {}
	
	/**
	 * This method should be called after the last element in a collection
	 * was visited
	 */
	public void post() throws Exception {}
	
	/**
	 * This method should be called before the last element in a collection
	 * was visited
	 */
	public void pre() throws Exception {}
	
	/**
	 * This method should be called between two elements a collection
	 * are visited
	 */
	public void inter() throws Exception {}
	
	/**
	 * This method should be called when the visitor is created
	 */
	public void head() throws Exception {}
	
	/**
	 * This method should be called after the last element was visited
	 */
	public void foot() throws Exception {}

}
