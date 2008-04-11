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
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;

/**
 * represents an incidence object, created temporarily by Graph class, delegates
 * nearly all methods to corresponding Graph
 * 
 * @author Daniel Bildhauer
 */
public abstract class EdgeImpl extends GraphElementImpl implements Edge {

	/**
	 * the id of this edge
	 */
	protected int id;

	/**
	 * the reversed edge
	 */
	protected ReversedEdgeImpl reversedEdge;

	/**
	 * creates the edge with id -id.
	 * 
	 */
	public EdgeImpl(int anId, Graph graph, AttributedElementClass cls) {
		super(graph, cls);
		id = anId;
		if (graph == null) {
			System.out.println("aGraph in EdgeBaseConstructor is null");
			System.exit(1);
		}
	}

	@Override
	public int compareTo(AttributedElement a) {
		if (a instanceof Edge) {
			Edge e = (Edge) a;
			return id - e.getId();
		}
		return -1;
	}

	@Override
	public void delete() {
		myGraph.deleteEdge(this);
	}

	@Override
	public Vertex getAlpha() {
		return myGraph.getAlpha(this);
	}

	public final int getId() {
		return id;
	}

	@Override
	public Edge getNextEdge() {
		return myGraph.getNextEdge(this, EdgeDirection.INOUT);
	}

	@Override
	public Edge getNextEdge(EdgeDirection orientation) {
		return myGraph.getNextEdge(this);
	}

	@Override
	public Edge getNextEdgeInGraph() {
		return myGraph.getNextEdgeInGraph(this);
	}

	@Override
	public Edge getNextEdgeOfClass(Class<? extends Edge> anEdgeClass) {
		return myGraph.getNextEdgeOfClass(this, anEdgeClass, EdgeDirection.INOUT, false);
	}


	@Override
	public Edge getNextEdgeOfClass(Class<? extends Edge> anEdgeClass,
			boolean explicitType) {
		return myGraph.getNextEdgeOfClass(this, anEdgeClass, EdgeDirection.INOUT,
				explicitType);
	}


	@Override
	public Edge getNextEdgeOfClass(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation) {
		return myGraph.getNextEdgeOfClass(this, anEdgeClass, orientation, false);
	}

	@Override
	public Edge getNextEdgeOfClass(Class<? extends Edge> anEdgeClass,
			EdgeDirection orientation, boolean explicitType) {
		return myGraph.getNextEdgeOfClass(this, anEdgeClass, orientation,
				explicitType);
	}


	@SuppressWarnings("unchecked")
	@Override
	public Edge getNextEdgeOfClass(EdgeClass anEdgeClass) {
		return myGraph.getNextEdgeOfClass(this, (Class<? extends Edge>) anEdgeClass
				.getM1Class(), EdgeDirection.INOUT, false);
	}



	@SuppressWarnings("unchecked")
	@Override
	public Edge getNextEdgeOfClass(EdgeClass anEdgeClass, boolean explicitType) {
		return getNextEdgeOfClass((Class<? extends Edge>) anEdgeClass
				.getM1Class(), EdgeDirection.INOUT, explicitType);
	}

	@Override
	public Edge getNextEdgeOfClass(EdgeClass anEdgeClass,
			EdgeDirection orientation) {
		return myGraph.getNextEdgeOfClass(this, anEdgeClass.getM1Class(), orientation, false);
	}


	@Override
	public Edge getNextEdgeOfClass(EdgeClass anEdgeClass,
			EdgeDirection orientation, boolean explicitType) {
		return myGraph.getNextEdgeOfClass(this, anEdgeClass, orientation,
				explicitType);
	}

	@Override
	public Edge getNextEdgeOfClassInGraph(Class<? extends Edge> anEdgeClass) {
		return getNextEdgeOfClassInGraph(anEdgeClass, false);
	}

	@Override
	public Edge getNextEdgeOfClassInGraph(Class<? extends Edge> anEdgeClass,
			boolean explicitType) {
		return myGraph.getNextEdgeOfClassInGraph(this, anEdgeClass, explicitType);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Edge getNextEdgeOfClassInGraph(EdgeClass anEdgeClass) {
		return getNextEdgeOfClassInGraph((Class<? extends Edge>) anEdgeClass
				.getM1Class(), false);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Edge getNextEdgeOfClassInGraph(EdgeClass anEdgeClass,
			boolean explicitType) {
		return getNextEdgeOfClassInGraph((Class<? extends Edge>) anEdgeClass
				.getM1Class(), explicitType);
	}

	@Override
	public final Edge getNormalEdge() {
		return this;
	}
	
	@Override
	public Vertex getOmega() {
		return myGraph.getOmega(this);
	}

	@Override
	public final Edge getReversedEdge() {
		return reversedEdge;
	}

	@Override
	public Vertex getThat() {
		return myGraph.getOmega(this);
	}

	@Override
	public String getThatRole() {
		return ((EdgeClass) this.getAttributedElementClass()).getToRolename();
	}

	@Override
	public Vertex getThis() {
		return myGraph.getAlpha(this);
	}

	@Override
	public String getThisRole() {
		return ((EdgeClass) this.getAttributedElementClass()).getFromRolename();
	}

	@Override
	public boolean isAfterInGraph(Edge e) {
		return myGraph.isAfterEdgeInGraph(e, this);
	}

	@Override
	public boolean isBeforeInGraph(Edge e) {
		return myGraph.isBeforeEdgeInGraph(e, this);
	}

	@Override
	public final boolean isNormal() {
		return true;
	}

	@Override
	public void putAfterInGraph(Edge e) {
		myGraph.putAfterEdgeInGraph(e, this);
	}

	@Override
	public void putBeforeInGraph(Edge e) {
		myGraph.putBeforeEdgeInGraph(e, this);
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
		myGraph.setAlpha(this, alpha);
	}

	public void setId(int id) {
		if ((this.id == 0) && (id > 0))
			this.id = id;
	}

	@Override
	public void setOmega(Vertex omega) {
		myGraph.setOmega(this, omega);
	}

	@Override
	public void setThat(Vertex v) {
		setOmega(v);
	}

	@Override
	public void setThis(Vertex v) {
		setAlpha(v);
	}

	@Override
	public String toString() {
		return "+e" + id + ": " + getAttributedElementClass().getQualifiedName();
	}

}
