/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
package de.uni_koblenz.jgralab.graphmarker;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.TraversalContext;
import de.uni_koblenz.jgralab.Vertex;

/**
 * This class serves as a special <code>BitSetGraphmarker</code>, although it
 * does not extend it. It is capable of marking both vertices and edges. This is
 * necessary for defining subgraphs. Internally all calls are delegated to an
 * instance of <code>BitSetVertexGraphMarker</code> and an instance of
 * <code>BitSetEdgeGraphMarker</code>.
 * 
 * When marking an {@link Edge}, by default a {@link SubGraphMarker} also marks
 * the incident {@linkplain Vertex vertices}. This behaviour can be changed via
 * {@link #setAutoMarkIncidentVertices(boolean)}.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class SubGraphMarker extends AbstractBooleanGraphMarker implements
		TraversalContext {

	private final BitSetEdgeMarker edgeGraphMarker;
	private final BitSetVertexMarker vertexGraphMarker;
	private boolean autoMarkIncidentVertices;
	private long version;

	public SubGraphMarker(Graph graph) {
		super(graph);
		edgeGraphMarker = new BitSetEdgeMarker(graph);
		vertexGraphMarker = new BitSetVertexMarker(graph);
		autoMarkIncidentVertices = true;
	}

	@Override
	public void clear() {
		if (isEmpty()) {
			return;
		}
		++version;
		edgeGraphMarker.clear();
		vertexGraphMarker.clear();
	}

	/**
	 * @return true iff this {@link SubGraphMarker} automatically marks incident
	 *         vertices when an edge is marked
	 */
	public boolean isAutoMarkIncidentVertices() {
		return autoMarkIncidentVertices;
	}

	/**
	 * Controls the behaviour of this {@link SubGraphMarker} when marking an
	 * {@linkplain Edge edge}. When set to <code>true</code>, both incident
	 * {@linkplain Vertex vertices} are marked when an {@linkplain Edge edge} is
	 * marked. When set to <code>false</code>, marking an {@linkplain Edge edge}
	 * does <i>not</i> automatically mark the {@linkplain Vertex vertices}.
	 * 
	 * @param autoMarkIncidentVertices
	 *            enable (<code>true</code>)/disable (<code>false</code>)
	 *            autmatic vertex marking
	 */
	public void setAutoMarkIncidentVertices(boolean autoMarkIncidentVertices) {
		this.autoMarkIncidentVertices = autoMarkIncidentVertices;
	}

	public int getECount() {
		return edgeGraphMarker.size();
	}

	public int getVCount() {
		return vertexGraphMarker.size();
	}

	@Override
	public boolean isEmpty() {
		return edgeGraphMarker.isEmpty() && vertexGraphMarker.isEmpty();
	}

	@Override
	public boolean isMarked(GraphElement<?, ?> graphElement) {
		return graphElement instanceof Edge ? edgeGraphMarker
				.isMarked((Edge) graphElement) : vertexGraphMarker
				.isMarked((Vertex) graphElement);
	}

	public boolean isMarked(Vertex v) {
		return vertexGraphMarker.isMarked(v);
	}

	public boolean isMarked(Edge e) {
		return edgeGraphMarker.isMarked(e);
	}

	@Override
	public int size() {
		return edgeGraphMarker.size() + vertexGraphMarker.size();
	}

	@Override
	public boolean removeMark(GraphElement<?, ?> graphElement) {
		return graphElement instanceof Edge ? removeMark((Edge) graphElement)
				: removeMark((Vertex) graphElement);
	}

	/**
	 * Does the same as <code>removeMark</code> but without performing an
	 * <code>instanceof</code> check. It is recommended to use this method
	 * instead.
	 * 
	 * @param e
	 *            the edge to unmark
	 * @return false if the given edge has already been unmarked.
	 */
	public boolean removeMark(Edge e) {
		if (edgeGraphMarker.removeMark(e)) {
			++version;
			return true;
		}
		return false;
	}

	/**
	 * Does the same as <code>unmark</code> but without performing an
	 * <code>instanceof</code> check. It is recommended to use this method
	 * instead. This method also removes the mark of all incident edges.
	 * 
	 * @param v
	 *            the vertex to unmark
	 * @return false if the given vertex has already been unmarked.
	 */
	public boolean removeMark(Vertex v) {
		if (vertexGraphMarker.removeMark(v)) {
			++version;
			for (Edge e : v.incidences()) {
				edgeGraphMarker.removeMark(e);
			}
			return true;
		}
		return false;
	}

	/**
	 * Marks the given <code>graphElement</code>.
	 * 
	 * @param graphElement
	 *            the graph element to mark
	 * @return false if the given <code>graphElement</code> has already been
	 *         marked.
	 */
	@Override
	public boolean mark(GraphElement<?, ?> graphElement) {
		return graphElement instanceof Edge ? mark((Edge) graphElement)
				: mark((Vertex) graphElement);
	}

	/**
	 * Does the same as <code>mark</code> but without performing an
	 * <code>instanceof</code> check. It is recommended to use this method
	 * instead. This method also marks the alpha and omega vertex of the given
	 * edge.
	 * 
	 * @param e
	 *            the edge to mark
	 * @return false if the given edge has already been marked.
	 */
	public boolean mark(Edge e) {
		if (edgeGraphMarker.mark(e)) {
			++version;
			if (autoMarkIncidentVertices) {
				vertexGraphMarker.mark(e.getAlpha());
				vertexGraphMarker.mark(e.getOmega());
			}
			return true;
		}
		return false;
	}

	/**
	 * Does the same as <code>mark</code> but without performing an
	 * <code>instanceof</code> check. It is recommended to use this method
	 * instead.
	 * 
	 * @param v
	 *            the vertex to mark
	 * @return false if the given vertex has already been marked.
	 */
	public boolean mark(Vertex v) {
		if (vertexGraphMarker.mark(v)) {
			version++;
			return true;
		}
		return false;
	}

	@Override
	public void edgeDeleted(Edge e) {
		edgeGraphMarker.edgeDeleted(e);
	}

	@Override
	public void vertexDeleted(Vertex v) {
		vertexGraphMarker.vertexDeleted(v);
	}

	@Override
	public Iterable<GraphElement<?, ?>> getMarkedElements() {
		return new Iterable<GraphElement<?, ?>>() {

			@Override
			public Iterator<GraphElement<?, ?>> iterator() {
				return new ArrayGraphMarkerIterator<GraphElement<?, ?>>(version) {

					Iterator<Vertex> vertexIterator;
					Iterator<Edge> edgeIterator;

					{
						vertexIterator = vertexGraphMarker.getMarkedElements()
								.iterator();
						edgeIterator = edgeGraphMarker.getMarkedElements()
								.iterator();
					}

					@Override
					public boolean hasNext() {
						return vertexIterator.hasNext()
								|| edgeIterator.hasNext();
					}

					@Override
					protected void moveIndex() {
						// not required
					}

					@Override
					public GraphElement<?, ?> next() {
						if (version != SubGraphMarker.this.version) {
							throw new ConcurrentModificationException(
									MODIFIED_ERROR_MESSAGE);
						}
						if (vertexIterator.hasNext()) {
							return vertexIterator.next();
						}
						if (edgeIterator.hasNext()) {
							return edgeIterator.next();
						}
						throw new NoSuchElementException(
								NO_MORE_ELEMENTS_ERROR_MESSAGE);
					}

				};
			}

		};
	}

	public Iterable<Vertex> getMarkedVertices() {
		return vertexGraphMarker.getMarkedElements();
	}

	public Iterable<Edge> getMarkedEdges() {
		return edgeGraphMarker.getMarkedElements();
	}

	@Override
	public boolean containsVertex(Vertex v) {
		return vertexGraphMarker.isMarked(v);
	}

	@Override
	public boolean containsEdge(Edge e) {
		return edgeGraphMarker.isMarked(e);
	}
}
