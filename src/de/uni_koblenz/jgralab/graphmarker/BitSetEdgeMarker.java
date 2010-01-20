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
						if(!hasNext()){
							throw new NoSuchElementException(NO_MORE_ELEMENTS_ERROR_MESSAGE);
						}
						if(version != BitSetEdgeMarker.this.version){
							throw new ConcurrentModificationException(MODIFIED_ERROR_MESSAGE);
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
