package de.uni_koblenz.jgralab.graphmarker;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;

public class LongEdgeMarker extends LongGraphMarker<Edge> {

	public LongEdgeMarker(Graph graph) {
		super(graph, graph.getMaxECount() + 1);
	}

	@Override
	public void edgeDeleted(Edge e) {
		removeMark(e.getNormalEdge());
	}

	@Override
	public void maxEdgeCountIncreased(int newValue) {
		newValue++;
		if (newValue > temporaryAttributes.length) {
			expand(newValue);
		}
	}

	@Override
	public void maxVertexCountIncreased(int newValue) {
		// do nothing
	}

	@Override
	public void vertexDeleted(Vertex v) {
		// do nothing
	}

	@Override
	public long mark(Edge edge, long value) {
		return super.mark(edge.getNormalEdge(), value);
	}
	
	@Override
	public boolean isMarked(Edge edge){
		return super.isMarked(edge.getNormalEdge());
	}
	
	@Override
	public long getMark(Edge edge){
		return super.getMark(edge.getNormalEdge());
	}

	@Override
	public Iterable<Edge> getMarkedElements() {
		return new Iterable<Edge>() {

			@Override
			public Iterator<Edge> iterator() {
				return new ArrayGraphMarkerIterator<Edge>(version) {

					@Override
					public boolean hasNext() {
						return index < temporaryAttributes.length;
					}

					@Override
					protected void moveIndex() {
						int length = temporaryAttributes.length;
						while (index < length
								&& temporaryAttributes[index] == unmarkedValue) {
							index++;
						}
					}

					@Override
					public Edge next() {
						if (!hasNext()) {
							throw new NoSuchElementException(
									NO_MORE_ELEMENTS_ERROR_MESSAGE);
						}
						if (version != LongEdgeMarker.this.version) {
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
