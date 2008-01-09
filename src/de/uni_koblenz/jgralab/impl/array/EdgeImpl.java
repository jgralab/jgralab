/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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
 
package de.uni_koblenz.jgralab.impl.array;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeClass;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.EdgeBaseImpl;

/**
 * represents an incidence object, created temporarily by Graph class, delegates
 * nearly all methods to corresponding Graph
 * 
 * @author Steffen Kahle
 */
public abstract class EdgeImpl extends EdgeBaseImpl implements Edge {

	public EdgeImpl(int anId, Graph aGraph, EdgeClass theClass) {
		super(anId, aGraph, theClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Incidence#getNextIncidence()
	 */
	public Edge getNextEdge() {
		return myGraph.getNextEdge(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Incidence#getNextIncidence(boolean)
	 */
	public Edge getNextEdge(EdgeDirection orientation) {
		return myGraph.getNextEdge(id, orientation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Incidence#getNextIncidenceOfClass(jgralab.EdgeClass)
	 */
	public Edge getNextEdgeOfClass(EdgeClass anEdgeClass) {
		return myGraph.getNextEdgeOfClass(id, anEdgeClass, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Incidence#getNextIncidenceOfClass(jgralab.EdgeClass)
	 */
	public Edge getNextEdgeOfClass(Class<? extends Edge> anEdgeClass) {
		return myGraph.getNextEdgeOfClass(id, anEdgeClass, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Incidence#getNextIncidenceOfClass(jgralab.EdgeClass,
	 *      boolean)
	 */
	public Edge getNextEdgeOfClass(EdgeClass anEdgeClass,
			EdgeDirection orientation) {
		return myGraph.getNextEdgeOfClass(id, anEdgeClass, orientation, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Incidence#getNextIncidenceOfClass(jgralab.EdgeClass,
	 *      boolean)
	 */
	public Edge getNextEdgeOfClass(Class<? extends Edge> anEdgeClass, EdgeDirection orientation) {
		return myGraph.getNextEdgeOfClass(id, anEdgeClass, orientation, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Incidence#getNextIncidenceOfExplicitClass(jgralab.EdgeClass)
	 */
	public Edge getNextEdgeOfClass(EdgeClass anEdgeClass, boolean explicitType) {
		return myGraph.getNextEdgeOfClass(id, anEdgeClass, explicitType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Incidence#getNextIncidenceOfExplicitClass(jgralab.EdgeClass)
	 */
	public Edge getNextEdgeOfClass(Class<? extends Edge> anEdgeClass, boolean explicitType) {
		return myGraph.getNextEdgeOfClass(id, anEdgeClass, explicitType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Incidence#getNextIncidenceOfExplicitClass(jgralab.EdgeClass,
	 *      boolean)
	 */
	public Edge getNextEdgeOfClass(EdgeClass anEdgeClass,
			EdgeDirection orientation, boolean explicitType) {
		return myGraph.getNextEdgeOfClass(id, anEdgeClass, orientation,
				explicitType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Incidence#getNextIncidenceOfExplicitClass(jgralab.EdgeClass,
	 *      boolean)
	 */
	public Edge getNextEdgeOfClass(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation, boolean explicitType) {
		return myGraph.getNextEdgeOfClass(id, anEdgeClass, orientation,
				explicitType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Incidence#setNextIncidence(jgralab.Incidence)
	 */
	public void setNextEdge(Edge i) {
		throw new GraphException(
				"command not supported: setNextIncidence (Incidence i)", null);
	}

	public void putEdgeBefore(Edge nextEdge) {
		myGraph.putEdgeBefore(this, nextEdge);
	}

	public void putEdgeAfter(Edge previousEdge) {
		myGraph.putEdgeAfter(this, previousEdge);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#isBefore(jgralab.Edge)
	 */
	public boolean isBeforeInGraph(Edge e) {
		return isBeforeInGraph(e.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#isBefore(int)
	 */
	public boolean isBeforeInGraph(int e) {
		return myGraph.isBeforeEdgeInGraph(e, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#putBefore(jgralab.Edge)
	 */
	public void putBeforeInGraph(Edge e) {
		putBeforeInGraph(e.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#putBefore(int)
	 */
	public void putBeforeInGraph(int e) {
		myGraph.putBeforeEdgeInGraph(e, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#isAfter(jgralab.Edge)
	 */
	public boolean isAfterInGraph(Edge e) {
		return isAfterInGraph(e.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#isAfter(int)
	 */
	public boolean isAfterInGraph(int e) {
		return myGraph.isAfterEdgeInGraph(e, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#putAfter(jgralab.Edge)
	 */
	public void putAfterInGraph(Edge e) {
		putAfterInGraph(e.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#putAfter(int)
	 */
	public void putAfterInGraph(int e) {
		myGraph.putAfterEdgeInGraph(e, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#insertAt(int)
	 */
	public void insertAtInGraph(int pos) {
		myGraph.insertEdgeInGraphAtPos(id, pos);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#setAlpha(jgralab.Vertex)
	 */
	public void setAlpha(Vertex alpha) {
		myGraph.setAlpha(id, alpha.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#setOmega(jgralab.Vertex)
	 */
	public void setOmega(Vertex omega) {
		myGraph.setOmega(id, omega.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#getAlpha()
	 */
	public Vertex getAlpha() {
		return myGraph.getAlpha(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#getOmega()
	 */
	public Vertex getOmega() {
		return myGraph.getOmega(id);
	}

	public Vertex getThis() {
		return myGraph.getAlpha(id);
	}

	public Vertex getThat() {
		return myGraph.getOmega(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#delete()
	 */
	public void delete() {
		myGraph.deleteEdge(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#getNextEdge()
	 */
	public Edge getNextEdgeInGraph() {
		return myGraph.getNextEdgeInGraph(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#getNextEdgeOfClass(java.lang.Class)
	 */
	public Edge getNextEdgeOfClassInGraph(EdgeClass anEdgeClass) {
		return myGraph.getNextEdgeOfClassInGraph(id, anEdgeClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#getNextEdgeOfClass(java.lang.Class)
	 */
	public Edge getNextEdgeOfClassInGraph(Class<? extends Edge> anEdgeClass) {
		return myGraph.getNextEdgeOfClassInGraph(id, anEdgeClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#getNextEdgeOfExplicitClass(java.lang.Class)
	 */
	public Edge getNextEdgeOfClassInGraph(EdgeClass anEdgeClass,
			boolean explicitType) {
		return myGraph.getNextEdgeOfClassInGraph(id, anEdgeClass, explicitType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#getNextEdgeOfExplicitClass(java.lang.Class)
	 */
	public Edge getNextEdgeOfClassInGraph(Class<? extends Edge> anEdgeClass,
			boolean explicitType) {
		return myGraph.getNextEdgeOfClassInGraph(id, anEdgeClass, explicitType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#getPrevEdge()
	 */
	public Edge getPrevEdgeInGraph() {
		return myGraph.getPrevEdgeInGraph(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#setNextEdge(jgralab.Edge)
	 */
	public void setNextEdgeInGraph(Edge e) {
		throw new GraphException(
				"command not supported: setNextEdgeInGraph(Edge e)", null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#setPrevEdge(jgralab.Edge)
	 */
	public void setPrevEdgeInGraph(Edge e) {
		throw new GraphException(
				"command not supported: setPrevEdgeInGraph(Edge e)", null);
	}

	public void insertEdgeAt(Vertex vertex, int pos) {
		myGraph.insertEdgeAt(vertex, this, pos);
	}

}
