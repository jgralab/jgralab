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

package de.uni_koblenz.jgralab.impl;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;

/**
 * represents an incidence object, created temporarily by Graph class, delegates
 * nearly all methods to corresponding Graph
 * 
 * @author Steffen Kahle
 */
public abstract class ReversedEdgeImpl implements Edge {

	/**
	 * true, if this incidence is in the normal edge order, false otherwise
	 */
	protected EdgeImpl normalEdge;

	protected Graph myGraph;

	public ReversedEdgeImpl(EdgeImpl normalEdge, Graph graph) {
		this.normalEdge = normalEdge;
		myGraph = graph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(AttributedElement a) {
		if (a instanceof Edge) {
			Edge e = (Edge) a;
			return getId() - e.getId();
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#delete()
	 */
	@Override
	public void delete() {
		normalEdge.delete();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getAlpha()
	 */
	@Override
	public Vertex getAlpha() {
		return normalEdge.getAlpha();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.AttributedElement#getAttribute(java.lang.String)
	 */
	@Override
	public Object getAttribute(String name) throws NoSuchFieldException {
		return normalEdge.getAttribute(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.AttributedElement#getAttributedElementClass()
	 */
	@Override
	public AttributedElementClass getAttributedElementClass() {
		return normalEdge.getAttributedElementClass();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.GraphElement#getGraph()
	 */
	@Override
	public final Graph getGraph() {
		return myGraph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.AttributedElement#getGraphClass()
	 */
	@Override
	public GraphClass getGraphClass() {
		return normalEdge.getGraphClass();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getId()
	 */
	@Override
	public final int getId() {
		return -normalEdge.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.AttributedElement#getM1Class()
	 */
	@Override
	public final Class<? extends AttributedElement> getM1Class() {
		return normalEdge.getM1Class();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdge()
	 */
	@Override
	public final Edge getNextEdge() {
		return myGraph.getNextEdge(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdge(de.uni_koblenz.jgralab.EdgeDirection)
	 */
	@Override
	public final Edge getNextEdge(EdgeDirection orientation) {
		return myGraph.getNextEdge(this, orientation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdgeInGraph()
	 */
	@Override
	public Edge getNextEdgeInGraph() {
		return normalEdge.getNextEdgeInGraph();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdgeOfClass(java.lang.Class)
	 */
	@Override
	public Edge getNextEdgeOfClass(Class<? extends Edge> anEdgeClass) {
		return myGraph.getNextEdgeOfClass(this, anEdgeClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdgeOfClass(java.lang.Class,
	 *      boolean)
	 */
	@Override
	public Edge getNextEdgeOfClass(Class<? extends Edge> anEdgeClass,
			boolean explicitType) {
		return myGraph.getNextEdgeOfClass(this, anEdgeClass,
				EdgeDirection.INOUT, explicitType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdgeOfClass(java.lang.Class,
	 *      de.uni_koblenz.jgralab.EdgeDirection)
	 */
	@Override
	public Edge getNextEdgeOfClass(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation) {
		return myGraph
				.getNextEdgeOfClass(this, anEdgeClass, orientation, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdgeOfClass(java.lang.Class,
	 *      de.uni_koblenz.jgralab.EdgeDirection, boolean)
	 */
	@Override
	public Edge getNextEdgeOfClass(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation, boolean explicitType) {
		return myGraph.getNextEdgeOfClass(this, anEdgeClass, orientation,
				explicitType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdgeOfClass(de.uni_koblenz.jgralab.schema.EdgeClass)
	 */
	@Override
	public Edge getNextEdgeOfClass(EdgeClass anEdgeClass) {
		return myGraph.getNextEdgeOfClass(this, anEdgeClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdgeOfClass(de.uni_koblenz.jgralab.schema.EdgeClass,
	 *      boolean)
	 */
	@Override
	public Edge getNextEdgeOfClass(EdgeClass anEdgeClass, boolean explicitType) {
		return myGraph.getNextEdgeOfClass(this, anEdgeClass,
				EdgeDirection.INOUT, explicitType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdgeOfClass(de.uni_koblenz.jgralab.schema.EdgeClass,
	 *      de.uni_koblenz.jgralab.EdgeDirection)
	 */
	@Override
	public Edge getNextEdgeOfClass(EdgeClass anEdgeClass,
			EdgeDirection orientation) {
		return myGraph.getNextEdgeOfClass(this, anEdgeClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdgeOfClass(de.uni_koblenz.jgralab.schema.EdgeClass,
	 *      de.uni_koblenz.jgralab.EdgeDirection, boolean)
	 */
	@Override
	public Edge getNextEdgeOfClass(EdgeClass anEdgeClass,
			EdgeDirection orientation, boolean explicitType) {
		return myGraph.getNextEdgeOfClass(this, anEdgeClass, orientation,
				explicitType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdgeOfClassInGraph(java.lang.Class)
	 */
	@Override
	public Edge getNextEdgeOfClassInGraph(Class<? extends Edge> anEdgeClass) {
		return normalEdge.getNextEdgeOfClassInGraph(anEdgeClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdgeOfClassInGraph(java.lang.Class,
	 *      boolean)
	 */
	@Override
	public Edge getNextEdgeOfClassInGraph(Class<? extends Edge> anEdgeClass,
			boolean explicitType) {
		return myGraph.getNextEdgeOfClassInGraph(normalEdge, anEdgeClass,
				explicitType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdgeOfClassInGraph(de.uni_koblenz.jgralab.schema.EdgeClass)
	 */
	@Override
	public Edge getNextEdgeOfClassInGraph(EdgeClass anEdgeClass) {
		return normalEdge.getNextEdgeOfClassInGraph(anEdgeClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNextEdgeOfClassInGraph(de.uni_koblenz.jgralab.schema.EdgeClass,
	 *      boolean)
	 */
	@Override
	public Edge getNextEdgeOfClassInGraph(EdgeClass anEdgeClass,
			boolean explicitType) {
		return normalEdge.getNextEdgeOfClassInGraph(anEdgeClass, explicitType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getNormalEdge()
	 */
	@Override
	public final Edge getNormalEdge() {
		return normalEdge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getOmega()
	 */
	@Override
	public Vertex getOmega() {
		return normalEdge.getOmega();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getReversedEdge()
	 */
	@Override
	public final Edge getReversedEdge() {
		return normalEdge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.AttributedElement#getSchema()
	 */
	@Override
	public Schema getSchema() {
		return normalEdge.getSchema();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getThat()
	 */
	@Override
	public Vertex getThat() {
		return normalEdge.getAlpha();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getThatRole()
	 */
	@Override
	public String getThatRole() {
		return normalEdge.getThisRole();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getThis()
	 */
	@Override
	public Vertex getThis() {
		return normalEdge.getOmega();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#getThisRole()
	 */
	@Override
	public String getThisRole() {
		return normalEdge.getThatRole();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.GraphElement#graphModified()
	 */
	@Override
	public void graphModified() {
		myGraph.graphModified();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#isAfterInGraph(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public boolean isAfterInGraph(Edge anEdge) {
		return normalEdge.isAfterInGraph(anEdge);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#isBeforeInGraph(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public boolean isBeforeInGraph(Edge anEdge) {
		return normalEdge.isBeforeInGraph(anEdge);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#isNormal()
	 */
	@Override
	public final boolean isNormal() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#putAfterInGraph(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public void putAfterInGraph(Edge anEdge) {
		normalEdge.putAfterInGraph(anEdge);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#putBeforeInGraph(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public void putBeforeInGraph(Edge anEdge) {
		normalEdge.putBeforeInGraph(anEdge);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#putEdgeAfter(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public void putEdgeAfter(Edge previousEdge) {
		myGraph.putEdgeAfter(this, previousEdge);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#putEdgeBefore(de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	public void putEdgeBefore(Edge nextEdge) {
		myGraph.putEdgeBefore(this, nextEdge);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#setAlpha(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public void setAlpha(Vertex alpha) {
		normalEdge.setAlpha(alpha);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#setId(int)
	 */
	@Override
	public void setId(int anId) {
		normalEdge.setId(anId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#setOmega(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public void setOmega(Vertex omega) {
		normalEdge.setOmega(omega);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#setThat(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public void setThat(Vertex v) {
		normalEdge.setAlpha(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#setThis(de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	public void setThis(Vertex v) {
		normalEdge.setOmega(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "-e" + normalEdge.getId() + ": "
				+ getAttributedElementClass().getQualifiedName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.Edge#isValid()
	 */
	@Override
	public final boolean isValid() {
		return myGraph.containsEdge(this);
	}

}
