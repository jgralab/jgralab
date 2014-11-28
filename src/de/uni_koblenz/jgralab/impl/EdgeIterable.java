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

package de.uni_koblenz.jgralab.impl;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.schema.EdgeClass;

/**
 * This class provides an Iterable to iterate over edges in a graph. One may use
 * this class to use the advanced for-loop of Java 5. Instances of this class
 * should never, and this means <b>never</b> created manually but only using the
 * methods <code>edges(params)</code> of th graph. Every special graphclass
 * contains generated methods similar to <code>edges(params)</code> for every
 * EdgeClass that is part of the GraphClass.
 * 
 * @author ist@uni-koblenz.de
 * 
 * @param <E>
 *            The type of the Edges to iterate over. To mention it again,
 *            <b>don't</b> create instances of this class directly.
 */
public class EdgeIterable<E extends Edge> implements Iterable<E> {

	class EdgeIterator implements Iterator<E> {

		protected E current = null;

		protected InternalGraph graph = null;

		protected EdgeClass schemaEc;

		/**
		 * the version of the edge list of the graph at the beginning of the
		 * iteration. This information is used to check if the edge list has
		 * changed, the failfast-iterator will then throw an exception the next
		 * time "next()" is called
		 */
		protected long edgeListVersion;

		@SuppressWarnings("unchecked")
		EdgeIterator(InternalGraph g, EdgeClass ec) {
			graph = g;
			schemaEc = ec;
			edgeListVersion = g.getEdgeListVersion();
			current = (E) (schemaEc == null ? graph.getFirstEdge() : graph
					.getFirstEdge(schemaEc));
		}

		@Override
		@SuppressWarnings("unchecked")
		public E next() {
			if (graph.isEdgeListModified(edgeListVersion)) {
				throw new ConcurrentModificationException(
						"The edge list of the graph has been modified - the iterator is not longer valid");
			}
			if (current == null) {
				throw new NoSuchElementException();
			}
			E result = current;
			current = (E) (schemaEc == null ? current.getNextEdge() : current
					.getNextEdge(schemaEc));
			return result;
		}

		@Override
		public boolean hasNext() {
			return current != null;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException(
					"It is not allowed to remove edges during iteration.");
		}

	}

	public EdgeIterable(Graph g) {
		this(g, (EdgeClass) null);
	}

	private Graph g;
	private EdgeClass ec;

	public EdgeIterable(Graph g, EdgeClass ec) {
		assert g != null;
		this.g = g;
		this.ec = ec;
	}

	@Override
	public Iterator<E> iterator() {
		return new EdgeIterator((InternalGraph) g, ec);
	}
}
