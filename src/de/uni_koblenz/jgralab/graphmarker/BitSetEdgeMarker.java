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
