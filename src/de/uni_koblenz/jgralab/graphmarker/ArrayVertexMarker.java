/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.InternalGraph;

/**
 * This class is the generic vertex graph marker. It is used for temporary
 * attributes on vertices which can be of an arbitrary type.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class ArrayVertexMarker<O> extends ArrayGraphMarker<Vertex, O> {

	public ArrayVertexMarker(Graph graph) {
		super(graph, ((InternalGraph) graph).getMaxVCount() + 1);
	}

	@Override
	public void edgeDeleted(Edge e) {
		// do nothing
	}

	@Override
	public void maxEdgeCountIncreased(int newValue) {
		// do nothing
	}

	@Override
	public void maxVertexCountIncreased(int newValue) {
		newValue++;
		if (newValue > temporaryAttributes.length) {
			expand(newValue);
		}
	}

	@Override
	public void vertexDeleted(Vertex v) {
		removeMark(v);
	}

	@Override
	public Iterable<Vertex> getMarkedElements() {
		return new Iterable<Vertex>() {

			@Override
			public Iterator<Vertex> iterator() {
				return new ArrayGraphMarkerIterator<Vertex>(version) {

					@Override
					public boolean hasNext() {
						return index < temporaryAttributes.length;
					}

					@Override
					protected void moveIndex() {
						int length = temporaryAttributes.length;
						while (index < length
								&& temporaryAttributes[index] == null) {
							index++;
						}
					}

					@Override
					public Vertex next() {
						if (!hasNext()) {
							throw new NoSuchElementException(
									NO_MORE_ELEMENTS_ERROR_MESSAGE);
						}
						if (version != ArrayVertexMarker.this.version) {
							throw new ConcurrentModificationException(
									MODIFIED_ERROR_MESSAGE);
						}
						Vertex next = graph.getVertex(index++);
						moveIndex();
						return next;
					}
				};

			}

		};
	}

}
