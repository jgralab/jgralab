package de.uni_koblenz.jgralab.impl.generic;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.impl.InternalGraph;
import de.uni_koblenz.jgralab.schema.EdgeClass;

public class GenericEdgeIterable<E extends Edge> implements Iterable<E> {
	
	class GenericVertexIterator implements Iterator<E> {
		
		private E current;
		private InternalGraph graph;
		private EdgeClass ec;
		private long vertexListVersion;

		@SuppressWarnings("unchecked")
		public GenericVertexIterator(InternalGraph graph, EdgeClass ec) {
			this.graph = graph;
			this.ec = ec;
			vertexListVersion = graph.getVertexListVersion();
			current = (E) (ec == null ? graph.getFirstEdge() : graph.getFirstEdge(ec));
		}
		
		@Override
		public boolean hasNext() {
			return current != null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public E next() {
			if (graph.isVertexListModified(vertexListVersion)) {
				throw new ConcurrentModificationException(
						"The vertex list of the graph has been modified - the iterator is not longer valid");
			}
			if (current == null) {
				throw new NoSuchElementException();
			}
			E result = current;
			current = (E) (ec == null ? current.getNextEdge() : current
					.getNextEdge(ec));
			return result;
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException(
					"It is not allowed to remove vertices during iteration.");
		}
		
	}
	
	private GenericVertexIterator iter;

	public GenericEdgeIterable(Graph g) {
		this(g, null);
	}
	
	public GenericEdgeIterable(Graph g, EdgeClass vc) {
		this.iter = new GenericVertexIterator((InternalGraph) g, vc);
	}

	@Override
	public Iterator<E> iterator() {
		return iter;
	}
}