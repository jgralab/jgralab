package de.uni_koblenz.jgralab.graphmarker;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;

public class BitSetVertexMarker extends BitSetGraphMarker<Vertex> {

	public BitSetVertexMarker(Graph graph) {
		super(graph);
	}

	@Override
	public void vertexDeleted(Vertex v) {
		removeMark(v);
	}

	@Override
	public void edgeDeleted(Edge e) {
		// do nothing
	}

	@Override
	public Iterable<Vertex> getMarkedElements() {
		return new Iterable<Vertex>() {

			@Override
			public Iterator<Vertex> iterator() {
				return new ArrayGraphMarkerIterator<Vertex>(version) {

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
					public Vertex next() {
						if (!hasNext()) {
							throw new NoSuchElementException(
									NO_MORE_ELEMENTS_ERROR_MESSAGE);
						}
						if (version != BitSetVertexMarker.this.version) {
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
