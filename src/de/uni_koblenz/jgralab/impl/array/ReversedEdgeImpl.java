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
import de.uni_koblenz.jgralab.impl.ReversedEdgeBaseImpl;

/**
 * represents an incidence object, created temporarily by Graph class, delegates
 * nearly all methods to corresponding Graph
 * 
 * @author Steffen Kahle
 */
public abstract class ReversedEdgeImpl extends ReversedEdgeBaseImpl implements Edge {


	public ReversedEdgeImpl(EdgeImpl normalEdge, Graph graph) {
		super(normalEdge, graph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Incidence#getNextIncidence()
	 */
	public final Edge getNextEdge() {
		return myGraph.getNextEdge(-normalEdge.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Incidence#getNextIncidence(boolean)
	 */
	public final Edge getNextEdge(EdgeDirection orientation) {
		return myGraph.getNextEdge(-normalEdge.getId(), orientation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Incidence#getNextIncidenceOfClass(jgralab.EdgeClass)
	 */
	public Edge getNextEdgeOfClass(EdgeClass anEdgeClass) {
		return myGraph.getNextEdgeOfClass(-normalEdge.getId(), anEdgeClass, false);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Incidence#getNextIncidenceOfClass(jgralab.EdgeClass)
	 */
	public Edge getNextEdgeOfClass(Class<? extends Edge> anEdgeClass) {
		return myGraph.getNextEdgeOfClass(-normalEdge.getId(), anEdgeClass, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Incidence#getNextIncidenceOfClass(jgralab.EdgeClass,
	 *      boolean)
	 */
	public Edge getNextEdgeOfClass(EdgeClass anEdgeClass,
			EdgeDirection orientation) {
		return myGraph.getNextEdgeOfClass(-normalEdge.getId(), anEdgeClass,
				orientation, false);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Incidence#getNextIncidenceOfClass(jgralab.EdgeClass,
	 *      boolean)
	 */
	public Edge getNextEdgeOfClass(Class<? extends Edge>  anEdgeClass,
			EdgeDirection orientation) {
		return myGraph.getNextEdgeOfClass(-normalEdge.getId(), anEdgeClass,
				orientation, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Incidence#getNextIncidenceOfExplicitClass(jgralab.EdgeClass)
	 */
	public Edge getNextEdgeOfClass(EdgeClass anEdgeClass,
			boolean explicitType) {
		return myGraph.getNextEdgeOfClass(-normalEdge.getId(), anEdgeClass,
				explicitType);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Incidence#getNextIncidenceOfExplicitClass(jgralab.EdgeClass)
	 */
	public Edge getNextEdgeOfClass(Class<? extends Edge>  anEdgeClass,
			boolean explicitType) {
		return myGraph.getNextEdgeOfClass(-normalEdge.getId(), anEdgeClass,
				explicitType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Incidence#getNextIncidenceOfExplicitClass(jgralab.EdgeClass,
	 *      boolean)
	 */
	public Edge getNextEdgeOfClass(EdgeClass anEdgeClass,
			EdgeDirection orientation, boolean explicitType) {
		return myGraph.getNextEdgeOfClass(-normalEdge.getId(), anEdgeClass,
				orientation, explicitType);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Incidence#getNextIncidenceOfExplicitClass(jgralab.EdgeClass,
	 *      boolean)
	 */
	public Edge getNextEdgeOfClass(Class<? extends Edge>  anEdgeClass,
			EdgeDirection orientation, boolean explicitType) {
		return myGraph.getNextEdgeOfClass(-normalEdge.getId(), anEdgeClass,
				orientation, explicitType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jgralab.Incidence#setNextIncidence(jgralab.Incidence)
	 */
	public void setNextEdge(Edge i)  {
		throw new GraphException(
				"command not supported: setNextIncidence (Incidence i)", null);
	}

	
	public void putEdgeBefore(Edge nextEdge)  {
		myGraph.putEdgeBefore(this, nextEdge);
	}
	
	public void putEdgeAfter(Edge previousEdge)  {
		myGraph.putEdgeAfter(this, previousEdge);
	}

	
	public void insertEdgeAt(Vertex vertex, int pos)  {
		myGraph.insertEdgeAt(vertex, this, pos);
	}
	
	public final Graph getGraph() {
		return myGraph;
	}
	

}
