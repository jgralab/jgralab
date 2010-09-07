/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
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

package de.uni_koblenz.jgralab.utilities.common;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.BooleanGraphMarker;

/**
 * An abstract visitor class visiting all (marked) vertices and edges.
 */
public abstract class GraphVisitor {
	protected Graph graph;
	protected BooleanGraphMarker marker;

	public GraphVisitor(Graph graph) {
		super();
		this.graph = graph;
	}

	public GraphVisitor(BooleanGraphMarker marker) {
		this(marker.getGraph());
		this.marker = marker;
	}

	public void visitAll() throws Exception {
		// visit the graph
		preVisitor();

		// visit all (marked) vertices
		for (Vertex currentVertex : graph.vertices()) {
			if (isMarked(currentVertex)) {
				visitVertex(currentVertex);
			}
		}

		// visit all (marked) edges
		for (Edge currentEdge : graph.edges()) {
			if (isMarked(currentEdge)) {
				visitEdge(currentEdge);
			}
		}

		postVisitor();
	}

	private boolean isMarked(AttributedElement e) {
		if (marker == null) {
			return true;
		}
		return marker.isMarked(e);
	}

	protected abstract void preVisitor() throws Exception;

	protected abstract void visitVertex(Vertex v) throws Exception;

	protected abstract void visitEdge(Edge e) throws Exception;

	protected abstract void postVisitor() throws Exception;
}
