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
 
package de.uni_koblenz.jgralab.impl;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.AttributedElementClass;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeClass;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphClass;
import de.uni_koblenz.jgralab.Schema;
import de.uni_koblenz.jgralab.Vertex;

/**
 * represents an incidence object, created temporarily by Graph class, delegates
 * nearly all methods to corresponding Graph
 * 
 * @author Daniel Bildhauer
 */
public abstract class ReversedEdgeBaseImpl implements Edge {

	/**
	 * true, if this incidence is in the normal edge order, falso otherwise
	 */
	protected EdgeBaseImpl normalEdge;

	protected Graph myGraph;

	/**
	 * creates the edge with id -id.
	 * 
	 *
	 */
	public ReversedEdgeBaseImpl(EdgeBaseImpl normalEdge, Graph graph) {
		this.normalEdge = normalEdge;
		myGraph = graph;
	}

	/* (non-Javadoc)
	 * @see jgralab.Edge#setThis(jgralab.Vertex)
	 */
	public void setThis(Vertex v) {
		normalEdge.setOmega(v);
	}

	/* (non-Javadoc)
	 * @see jgralab.Edge#setThat(jgralab.Vertex)
	 */
	public void setThat(Vertex v) {
		normalEdge.setAlpha(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#getId()
	 */
	public final int getId() {
		return -normalEdge.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#setId(int)
	 */
	public void setId(int anId) {
		normalEdge.setId(anId);
	}

	/**
	 * returns true if this incidence is in normal order, false otherwise
	 */
	public final boolean isNormal() {
		return false;
	}

	/**
	 * returns the incidence which has the same direction like the edge
	 */
	public final Edge getNormalEdge() {
		return normalEdge;
	}

	/**
	 * returns the incidence which has the opposite direction than the edge
	 */
	public final Edge getReversedEdge() {
		return normalEdge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Incidence#getThis()
	 */
	public Vertex getThis() {
		return normalEdge.getOmega();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Incidence#getThat()
	 */
	public Vertex getThat() {
		return normalEdge.getAlpha();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#getNextEdge()
	 */
	public Edge getNextEdgeInGraph() {
		return normalEdge.getNextEdgeInGraph();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#setNextEdge()
	 */
	public void setNextEdgeInGraph(Edge nextEdge) {
		normalEdge.setNextEdgeInGraph(nextEdge);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#setPrevEdge()
	 */
	public void setPrevEdgeInGraph(Edge nextEdge) {
		normalEdge.setPrevEdgeInGraph(nextEdge);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#getNextEdgeOfClass(jgralab.EdgeClass)
	 */
	public Edge getNextEdgeOfClassInGraph(EdgeClass anEdgeClass) {
		return normalEdge.getNextEdgeOfClassInGraph(anEdgeClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#getNextEdgeOfClass(jgralab.EdgeClass)
	 */
	public Edge getNextEdgeOfClassInGraph(Class<? extends Edge> anEdgeClass) {
		return normalEdge.getNextEdgeOfClassInGraph(anEdgeClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#getNextEdgeOfExplicitClass(jgralab.EdgeClass)
	 */
	public Edge getNextEdgeOfClassInGraph(EdgeClass anEdgeClass,
			boolean explicitType) {
		return normalEdge.getNextEdgeOfClassInGraph(anEdgeClass, explicitType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#getNextEdgeOfExplicitClass(jgralab.EdgeClass)
	 */
	public Edge getNextEdgeOfClassInGraph(Class<? extends Edge> anEdgeClass,
			boolean explicitType) {
		return normalEdge.getNextEdgeOfClassInGraph(anEdgeClass, explicitType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#getPrevEdge()
	 */
	public Edge getPrevEdgeInGraph() {
		return normalEdge.getPrevEdgeInGraph();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#getAlpha()
	 */
	public Vertex getAlpha() {
		return normalEdge.getAlpha();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#getOmega()
	 */
	public Vertex getOmega() {
		return normalEdge.getOmega();
	}

	public void setAlpha(Vertex alpha) {
		normalEdge.setAlpha(alpha);
	}

	public void setOmega(Vertex omega) {
		normalEdge.setOmega(omega);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.AttributedElement#getAttributedElementClass()
	 */
	public AttributedElementClass getAttributedElementClass() {
		return normalEdge.getAttributedElementClass();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#isBefore(jgralab.Edge)
	 */
	public boolean isBeforeInGraph(Edge anEdge) {
		return normalEdge.isBeforeInGraph(anEdge);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#isBefore(int)
	 */
	public boolean isBeforeInGraph(int anEdge) {
		return normalEdge.isBeforeInGraph(anEdge);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#isAfter(jgralab.Edge)
	 */
	public boolean isAfterInGraph(Edge anEdge) {
		return normalEdge.isAfterInGraph(anEdge);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#isAfter(int)
	 */
	public boolean isAfterInGraph(int anEdge) {
		return normalEdge.isAfterInGraph(anEdge);
	}

	public void putAfterInGraph(int anEdge) {
		normalEdge.putAfterInGraph(anEdge);
	}

	public void putAfterInGraph(Edge anEdge) {
		normalEdge.putAfterInGraph(anEdge);
	}

	public void putBeforeInGraph(int anEdge) {
		normalEdge.putBeforeInGraph(anEdge);
	}

	public void putBeforeInGraph(Edge anEdge) {
		normalEdge.putBeforeInGraph(anEdge);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Edge#insertAt(int)
	 */
	public void insertAtInGraph(int pos) {
		normalEdge.insertAtInGraph(pos);
	}

	public GraphClass getGraphClass() {
		return normalEdge.getGraphClass();
	}

	public Schema getSchema() {
		return normalEdge.getSchema();
	}

	public Graph getGraph() {
		return normalEdge.getGraph();
	}

	public void delete() {
		normalEdge.delete();
	}

	public Object getAttribute(String name) throws NoSuchFieldException {
		return normalEdge.getAttribute(name);
	}

	public final Class<? extends AttributedElement> getM1Class() {
		return normalEdge.getM1Class();
	}

	public int compareTo(AttributedElement a) {
		if (a instanceof Edge) {
			Edge e = (Edge) a;
			return getId() - e.getId();
		}
		return -1;
	}

	public String getThisRole() {
		return normalEdge.getThatRole();
	}

	public String getThatRole() {
		return normalEdge.getThisRole();
	}
	
	public String toString() {
		return "e" + normalEdge.getId() + ": " + getAttributedElementClass().getName();
	}
	
	public void modified() {
		myGraph.modified();
	}
}
