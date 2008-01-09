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
import de.uni_koblenz.jgralab.VertexClass;
import de.uni_koblenz.jgralab.impl.VertexBaseImpl;

/**
 * represents a vertex in the graph and delegates nearly all methods
 * back to its corresponding graph
 * @author Steffen Kahle
 */
public abstract class VertexImpl extends VertexBaseImpl implements Vertex {

	private static final boolean DEBUG = false;
	
	/**
	 * @param anId the id of the vertex to be
	 * @param theGraph the corresponding graph
	 */
	public VertexImpl(int anId, Graph theGraph, VertexClass theClass) {
		super(anId, theGraph, theClass);
	}


	/* (non-Javadoc)
	 * @see jgralab.Vertex#getDegree()
	 */
	public int getDegree() {
		return myGraph.getDegree(id);
	}
	
	/* (non-Javadoc)
	 * @see jgralab.Vertex#getDegree(boolean)
	 */
	public int getDegree(EdgeDirection orientation) {
		return myGraph.getDegree(id, orientation);
	}
	
	/* (non-Javadoc)
	 * @see jgralab.Vertex#getNextVertex()
	 */
	public Vertex getNextVertex() {
		if (DEBUG)
			System.out.println("Retrieving next vertex of vertex: " + id + " getId: " + getId());
		return myGraph.getNextVertex(id);
	}

	/* (non-Javadoc)
	 * @see jgralab.Vertex#getNextVertexOfClass(jgralab.VertexClass)
	 */
	public Vertex getNextVertexOfClass(VertexClass aVertexClass) {
		if (DEBUG)
			System.out.println("Retrieving next vertex of class of vertex: " + id);
		return myGraph.getNextVertexOfClass(this, aVertexClass);
	}
	
	/* (non-Javadoc)
	 * @see jgralab.Vertex#getNextVertexOfClass(jgralab.VertexClass)
	 */
	public Vertex getNextVertexOfClass(Class<? extends Vertex>  aM1VertexClass) {
		if (DEBUG)
			System.out.println("Retrieving next vertex of class of vertex: " + id);
		return myGraph.getNextVertexOfClass(this, aM1VertexClass);
	}

	/* (non-Javadoc)
	 * @see jgralab.Vertex#getNextVertexOfExplicitClass(jgralab.VertexClass)
	 */
	public Vertex getNextVertexOfClass(VertexClass aVertexClass, boolean explicitType) {
		return myGraph.getNextVertexOfClass(this, aVertexClass, explicitType);
	}
	
	/* (non-Javadoc)
	 * @see jgralab.Vertex#getNextVertexOfExplicitClass(jgralab.VertexClass)
	 */
	public Vertex getNextVertexOfClass(Class<? extends Vertex> aM1VertexClass, boolean explicitType) {
		return myGraph.getNextVertexOfClass(this, aM1VertexClass, explicitType);
	}

	/* (non-Javadoc)
	 * @see jgralab.Vertex#getPrevVertex()
	 */
	public Vertex getPrevVertex() {
		return myGraph.getPrevVertex(id);
	}

	
	/* (non-Javadoc)
	 * @see jgralab.Vertex#isBefore(jgralab.Vertex)
	 */
	public boolean isBefore(Vertex v) {
		return isBefore(v.getId());
	}
	
	/* (non-Javadoc)
	 * @see jgralab.Vertex#isBefore(int)
	 */
	public boolean isBefore(int v) {
		return myGraph.isBeforeVertex(v, id);
	}
	
	/* (non-Javadoc)
	 * @see jgralab.Vertex#putBefore(jgralab.Vertex)
	 */
	public void putBefore(Vertex v) {
		putBefore(v.getId());
	}
	
	/* (non-Javadoc)
	 * @see jgralab.Vertex#putBefore(int)
	 */
	public void putBefore(int v) {
		myGraph.putBeforeVertex(v, id);
	}
	
	/* (non-Javadoc)
	 * @see jgralab.Vertex#isAfter(jgralab.Vertex)
	 */
	public boolean isAfter(Vertex v) {
		return isAfter(v.getId());
	}
	
	/* (non-Javadoc)
	 * @see jgralab.Vertex#isAfter(int)
	 */
	public boolean isAfter(int v) {
		return myGraph.isAfterVertex(v, id);
	}
	
	/* (non-Javadoc)
	 * @see jgralab.Vertex#putAfter(jgralab.Vertex)
	 */
	public void putAfter(Vertex v) {
		putAfter(v.getId());
	}
	
	/* (non-Javadoc)
	 * @see jgralab.Vertex#putAfter(int)
	 */
	public void putAfter(int v) {
		myGraph.putAfterVertex(v, id);
	}

	 /* (non-Javadoc)
	 * @see jgralab.Vertex#insertAt(int)
	 */
	public void insertAt(int pos) {
		myGraph.insertVertexAtPos(id, pos);
	}

	
	/* (non-Javadoc)
	 * @see jgralab.Vertex#getFirstIncidence()
	 */
	public Edge getFirstEdge() {
		return myGraph.getFirstEdge(id);
	}
	
	
	/* (non-Javadoc)
	 * @see jgralab.Vertex#getFirstIncidence(boolean)
	 */
	public Edge getFirstEdge( EdgeDirection orientation) {
		return myGraph.getFirstEdge(id, orientation);
	}
	
	/* (non-Javadoc)
	 * @see jgralab.Vertex#getFirstIncidenceOfClass(jgralab.EdgeClass)
	 */
	public Edge getFirstEdgeOfClass(EdgeClass anEdgeClass) {
		return myGraph.getFirstEdgeOfClass(id, anEdgeClass, false);
	}
	

	/* (non-Javadoc)
	 * @see jgralab.Vertex#getFirstIncidenceOfClass(jgralab.EdgeClass)
	 */
	public Edge getFirstEdgeOfClass(Class<? extends Edge> anEdgeClass) {
		return myGraph.getFirstEdgeOfClass(id, anEdgeClass, false);
	}

	/* (non-Javadoc)
	 * @see jgralab.Vertex#getFirstIncidenceOfClass(jgralab.EdgeClass, boolean)
	 */
	public Edge getFirstEdgeOfClass(EdgeClass anEdgeClass,  EdgeDirection orientation) {
		return myGraph.getFirstEdgeOfClass(id, anEdgeClass, orientation, false);
	}
	
	/* (non-Javadoc)
	 * @see jgralab.Vertex#getFirstIncidenceOfClass(jgralab.EdgeClass, boolean)
	 */
	public Edge getFirstEdgeOfClass(Class<? extends Edge>  anEdgeClass,  EdgeDirection orientation) {
		return myGraph.getFirstEdgeOfClass(id, anEdgeClass, orientation, false);
	}

	/* (non-Javadoc)
	 * @see jgralab.Vertex#getFirstIncidenceOfExplicitClass(jgralab.EdgeClass)
	 */
	public Edge getFirstEdgeOfClass(EdgeClass anEdgeClass, boolean explicitType) {
		return myGraph.getFirstEdgeOfClass(id, anEdgeClass, explicitType);
	}

	/* (non-Javadoc)
	 * @see jgralab.Vertex#getFirstIncidenceOfExplicitClass(jgralab.EdgeClass)
	 */
	public Edge getFirstEdgeOfClass(Class<? extends Edge>  anEdgeClass, boolean explicitType) {
		return myGraph.getFirstEdgeOfClass(id, anEdgeClass, explicitType);
	}
	
	/* (non-Javadoc)
	 * @see jgralab.Vertex#getFirstIncidenceOfExplicitClass(jgralab.EdgeClass, boolean)
	 */
	public Edge getFirstEdgeOfClass(EdgeClass anEdgeClass,  EdgeDirection orientation, boolean explicitType) {
		return myGraph.getFirstEdgeOfClass(id, anEdgeClass, orientation, explicitType);
	}
	
	/* (non-Javadoc)
	 * @see jgralab.Vertex#getFirstIncidenceOfExplicitClass(jgralab.EdgeClass, boolean)
	 */
	public Edge getFirstEdgeOfClass(Class<? extends Edge>  anEdgeClass,  EdgeDirection orientation, boolean explicitType) {
		return myGraph.getFirstEdgeOfClass(id, anEdgeClass, orientation, explicitType);
	}

	/* (non-Javadoc)
	 * @see jgralab.Vertex#deleteIncidenceTo(int)
	 */
	public void deleteIncidenceTo(int eNo)  {
		myGraph.deleteEdgeTo(id, eNo);
	}

	/* (non-Javadoc)
	 * @see jgralab.Vertex#setNextVertex(jgralab.Vertex)
	 */
	public void setNextVertex(Vertex v)   {
		throw new GraphException("command not supported: setNextVertex(Vertex v)", null);
	}

	/* (non-Javadoc)
	 * @see jgralab.Vertex#setPrevVertex(jgralab.Vertex)
	 */
	public void setPrevVertex(Vertex v)   {
		throw new GraphException("command not supported: setPrevVertex(Vertex v)", null);
	}

	/* (non-Javadoc)
	 * @see jgralab.Vertex#setFirstIncidence(jgralab.Incidence)
	 */
	public void setFirstEdge(Edge i)  {
		throw new GraphException("command not supported: setFirstIncidence(Incidence i)", null);
	}
	
	/* (non-Javadoc)
	 * @see jgralab.Vertex#delete()
	 */
	public void delete()  {
		myGraph.deleteVertex(this);
	}
	

	public void putEdgeBefore(Edge edge, Edge nextEdge)  {
		if ((edge.getThis() != this) || (nextEdge.getThis() != this))
			return;
		myGraph.putEdgeBefore(edge, nextEdge);
	}
	

	public void putEdgeAfter(Edge edge, Edge previousEdge)  {
		if ((edge.getThis() != this) || (previousEdge.getThis() != this))
			return;
		myGraph.putEdgeAfter(edge, previousEdge);
	}
	

	public void insertEdgeAt(Edge edge, int pos)  {
		if (edge.getThis() != this) 
			return;
		myGraph.insertEdgeAt(this, edge, pos);
	}
	

	

}
