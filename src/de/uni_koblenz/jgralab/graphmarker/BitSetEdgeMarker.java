/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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
import de.uni_koblenz.jgralab.Vertex;

public class BitSetEdgeMarker extends BitSetGraphMarker<Edge> {

	public BitSetEdgeMarker(Graph graph) {
		super(graph);
	}

	@Override
	public void edgeDeleted(Edge e) {
		removeMark(e.getNormalEdge());
	}

	@Override
	public void vertexDeleted(Vertex v) {
		// do nothing
	}

	@Override
	public boolean mark(Edge edge) {
		return super.mark(edge.getNormalEdge());
	}

	@Override
	public boolean isMarked(Edge edge) {
		return super.isMarked(edge.getNormalEdge());
	}

	@Override
	public Iterable<Edge> getMarkedElements() {
		return new Iterable<Edge>() {

			@Override
			public Iterator<Edge> iterator() {
				return new ArrayGraphMarkerIterator<Edge>(version) {

					@Override
					public boolean hasNext() {
						return index < marks.size();
					}

					@Override
					protected void moveIndex() {
						int length = marks.size();
						while (index < length && !marks.get(index)) {
							index++;
						}
					}

					@Override
					public Edge next() {
						if (!hasNext()) {
							throw new NoSuchElementException(
									NO_MORE_ELEMENTS_ERROR_MESSAGE);
						}
						if (version != BitSetEdgeMarker.this.version) {
							throw new ConcurrentModificationException(
									MODIFIED_ERROR_MESSAGE);
						}
						Edge next = graph.getEdge(index++);
						moveIndex();
						return next;
					}
				};

			}

		};
	}

}
