package de.uni_koblenz.jgralab.impl.generic;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.InternalGraph;
import de.uni_koblenz.jgralab.impl.VertexIterable;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * This is a clone of {@link VertexIterable}, but changed to accommodate
 * type-specific iteration over generic vertices.
 *  
 * @author Bernhard
 *
 * @param <V>
 */
public class GenericVertexIterable<V extends Vertex> implements Iterable<V> {
	
	class GenericVertexIterator implements Iterator<V> {
		
		private V current;
		private InternalGraph graph;
		private VertexClass vc;
		private long vertexListVersion;

		@SuppressWarnings("unchecked")
		public GenericVertexIterator(InternalGraph graph, VertexClass vc) {
			this.graph = graph;
			this.vc = vc;
			vertexListVersion = graph.getVertexListVersion();
			current = (V) (vc == null ? graph.getFirstVertex() : graph.getFirstVertex(vc));
		}
		
		@Override
		public boolean hasNext() {
			return current != null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public V next() {
			if (graph.isVertexListModified(vertexListVersion)) {
				throw new ConcurrentModificationException(
						"The vertex list of the graph has been modified - the iterator is not longer valid");
			}
			if (current == null) {
				throw new NoSuchElementException();
			}
			V result = current;
			current = (V) (vc == null ? current.getNextVertex() : current
					.getNextVertex(vc));
			return result;
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException(
					"It is not allowed to remove vertices during iteration.");
		}
		
	}
	
	private GenericVertexIterator iter;

	public GenericVertexIterable(Graph g) {
		this(g, null);
	}
	
	public GenericVertexIterable(Graph g, VertexClass vc) {
		this.iter = new GenericVertexIterator((InternalGraph) g, vc);
	}

	@Override
	public Iterator<V> iterator() {
		return iter;
	}
}
