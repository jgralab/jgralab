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
public abstract class ReversedEdgeImpl implements
		Edge {

	/**
	 * true, if this incidence is in the normal edge order, false otherwise
	 */
	protected EdgeImpl normalEdge;

	protected Graph myGraph;


	public ReversedEdgeImpl(EdgeImpl normalEdge, Graph graph) {
		this.normalEdge = normalEdge;
		myGraph = graph;
	}

	@Override
	public int compareTo(AttributedElement a) {
		if (a instanceof Edge) {
			Edge e = (Edge) a;
			return getId() - e.getId();
		}
		return -1;
	}

	@Override
	public void delete() {
		normalEdge.delete();
	}

	@Override
	public Vertex getAlpha() {
		return normalEdge.getAlpha();
	}

	@Override
	public Object getAttribute(String name) throws NoSuchFieldException {
		return normalEdge.getAttribute(name);
	}

	@Override
	public AttributedElementClass getAttributedElementClass() {
		return normalEdge.getAttributedElementClass();
	}

	@Override
	public final Graph getGraph() {
		return myGraph;
	}

	@Override
	public GraphClass getGraphClass() {
		return normalEdge.getGraphClass();
	}

	@Override
	public final int getId() {
		return -normalEdge.getId();
	}

	@Override
	public final Class<? extends AttributedElement> getM1Class() {
		return normalEdge.getM1Class();
	}

	@Override
	public final Edge getNextEdge() {
		return myGraph.getNextEdge(this);
	}

	@Override
	public final Edge getNextEdge(EdgeDirection orientation) {
		return myGraph.getNextEdge(this, orientation);
	}

	@Override
	public Edge getNextEdgeInGraph() {
		return normalEdge.getNextEdgeInGraph();
	}

	@Override
	public Edge getNextEdgeOfClass(Class<? extends Edge> anEdgeClass) {
		return myGraph.getNextEdgeOfClass(this, anEdgeClass);
	}

	@Override
	public Edge getNextEdgeOfClass(Class<? extends Edge> anEdgeClass,
			boolean explicitType) {
		return myGraph.getNextEdgeOfClass(this, anEdgeClass, EdgeDirection.INOUT, explicitType);
	}

	@Override
	public Edge getNextEdgeOfClass(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation) {
		return myGraph.getNextEdgeOfClass(this, anEdgeClass, orientation, false);
	}

	@Override
	public Edge getNextEdgeOfClass(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation, boolean explicitType) {
		return myGraph.getNextEdgeOfClass(this, anEdgeClass, orientation, explicitType);
	}

	@Override
	public Edge getNextEdgeOfClass(EdgeClass anEdgeClass) {
		return myGraph.getNextEdgeOfClass(this, anEdgeClass);
	}

	@Override
	public Edge getNextEdgeOfClass(EdgeClass anEdgeClass, boolean explicitType) {
		return myGraph.getNextEdgeOfClass(this, anEdgeClass, EdgeDirection.INOUT, explicitType);
	}

	@Override
	public Edge getNextEdgeOfClass(EdgeClass anEdgeClass,
			EdgeDirection orientation) {
		return myGraph.getNextEdgeOfClass(this, anEdgeClass);
	}

	@Override
	public Edge getNextEdgeOfClass(EdgeClass anEdgeClass,
			EdgeDirection orientation, boolean explicitType) {
		return myGraph.getNextEdgeOfClass(this, anEdgeClass, orientation, explicitType);
	}

	@Override
	public Edge getNextEdgeOfClassInGraph(Class<? extends Edge> anEdgeClass) {
		return normalEdge.getNextEdgeOfClassInGraph(anEdgeClass);
	}


	@Override
	public Edge getNextEdgeOfClassInGraph(Class<? extends Edge> anEdgeClass,
			boolean explicitType) {
		return myGraph.getNextEdgeOfClassInGraph(normalEdge, anEdgeClass, explicitType);
	}

	@Override
	public Edge getNextEdgeOfClassInGraph(EdgeClass anEdgeClass) {
		return normalEdge.getNextEdgeOfClassInGraph(anEdgeClass);
	}

	@Override
	public Edge getNextEdgeOfClassInGraph(EdgeClass anEdgeClass,
			boolean explicitType) {
		return normalEdge.getNextEdgeOfClassInGraph(anEdgeClass, explicitType);
	}

	@Override
	public final Edge getNormalEdge() {
		return normalEdge;
	}

	@Override
	public Vertex getOmega() {
		return normalEdge.getOmega();
	}

	@Override
	public final Edge getReversedEdge() {
		return normalEdge;
	}

	@Override
	public Schema getSchema() {
		return normalEdge.getSchema();
	}

	@Override
	public Vertex getThat() {
		return normalEdge.getAlpha();
	}
	
	@Override
	public String getThatRole() {
		return normalEdge.getThisRole();
	}
	
	@Override
	public Vertex getThis() {
		return normalEdge.getOmega();
	}
	
	@Override
	public String getThisRole() {
		return normalEdge.getThatRole();
	}

	@Override
	public void graphModified() {
		myGraph.graphModified();
	}

	@Override
	public boolean isAfterInGraph(Edge anEdge) {
		return normalEdge.isAfterInGraph(anEdge);
	}


	@Override
	public boolean isBeforeInGraph(Edge anEdge) {
		return normalEdge.isBeforeInGraph(anEdge);
	}

	@Override
	public final boolean isNormal() {
		return false;
	}

	@Override
	public void putAfterInGraph(Edge anEdge) {
		normalEdge.putAfterInGraph(anEdge);
	}

	@Override
	public void putBeforeInGraph(Edge anEdge) {
		normalEdge.putBeforeInGraph(anEdge);
	}

	@Override
	public void putEdgeAfter(Edge previousEdge) {
		myGraph.putEdgeAfter(this, previousEdge);
	}

	@Override
	public void putEdgeBefore(Edge nextEdge) {
		myGraph.putEdgeBefore(this, nextEdge);
	}

	@Override
	public void setAlpha(Vertex alpha) {
		normalEdge.setAlpha(alpha);
	}

	@Override
	public void setId(int anId) {
		normalEdge.setId(anId);
	}

	@Override
	public void setOmega(Vertex omega) {
		normalEdge.setOmega(omega);
	}

	@Override
	public void setThat(Vertex v) {
		normalEdge.setAlpha(v);
	}

	@Override
	public void setThis(Vertex v) {
		normalEdge.setOmega(v);
	}

	@Override
	public String toString() {
		return "-e" + normalEdge.getId() + ": " + getAttributedElementClass().getQualifiedName();
	}

}
