package de.uni_koblenz.jgralab.impl;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.VertexFilter;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * This Iterator iterates over all vertices in a graph
 * 
 * @author ist@uni-koblenz.de
 * 
 */
class VertexIterator<V extends Vertex> implements Iterator<V> {

	/**
	 * the vertex that hasNext() retrieved and that a call of next() will return
	 */
	protected V current = null;

	/**
	 * the graph this iterator works on
	 */
	protected InternalGraph graph = null;

	protected VertexClass schemaVc;

	/**
	 * the version of the vertex list of the graph at the beginning of the
	 * iteration. This information is used to check if the vertex list has
	 * changed, the failfast-iterator will then throw an exception the next time
	 * "next()" is called
	 */
	protected long vertexListVersion;

	private VertexFilter<V> filter;

	/**
	 * Creates a new Vertex iterator for the given <code>Graph</code>, that
	 * iterates over vertices of a given <code>VertexClass</code>
	 * 
	 * @param g
	 *            The <code>Graph</code>.
	 * @param vc
	 *            They <code>VertexClass</code> determining which type of vertex
	 *            should be iterated over.
	 */
	@SuppressWarnings("unchecked")
	public VertexIterator(InternalGraph g, VertexClass vc,
			VertexFilter<V> filter) {
		graph = g;
		schemaVc = vc;
		this.filter = filter;
		vertexListVersion = g.getVertexListVersion();
		current = (V) (vc == null ? graph.getFirstVertex() : graph
				.getFirstVertex(vc));
		if (current != null && filter != null && !filter.accepts(current)) {
			getNext();
		}
	}

	/**
	 * @return the next vertex in the graph which mathes the conditions of this
	 *         iterator
	 */
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
		current = getNext();
		return result;
	}

	/*
	 * Advances <code>current</code> to the next valid vertex
	 */
	@SuppressWarnings("unchecked")
	private V getNext() {
		do {
			current = (V) (schemaVc == null ? current.getNextVertex() : current
					.getNextVertex(schemaVc));
		} while (current != null && filter != null && !filter.accepts(current));
		return current;
	}

	/**
	 * @return true iff there is at least one next vertex to retrieve
	 */
	@Override
	public boolean hasNext() {
		return current != null;
	}

	/**
	 * Using the VertexIterator, it is <b>not</b> possible to remove vertices
	 * from a graph neither the iterator will recognize such a removal.
	 * 
	 * @throw UnsupportedOperationException every time the method is called
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException(
				"It is not allowed to remove vertices during iteration.");
	}
}