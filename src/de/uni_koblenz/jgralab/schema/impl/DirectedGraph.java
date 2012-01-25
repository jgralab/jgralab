/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
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

package de.uni_koblenz.jgralab.schema.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DirectedGraph<T> {

	protected static class Node<T> {
		final T data;
		final Set<Node<T>> successors;
		final Set<Node<T>> predecessors;
		int mark;

		Node(T data) {
			assert data != null;
			this.data = data;
			successors = new HashSet<Node<T>>();
			predecessors = new HashSet<Node<T>>();
		}
	}

	protected final Set<Node<T>> nodes;
	protected final Map<T, Node<T>> entries;

	public DirectedGraph() {
		nodes = new HashSet<Node<T>>();
		entries = new HashMap<T, Node<T>>();
	}

	public void createEdge(T alpha, T omega) {
		if (alpha == omega || alpha.equals(omega)) {
			throw new IllegalArgumentException("Loops are not supported.");
		}
		Node<T> fromNode = entries.get(alpha);
		assert fromNode != null;
		Node<T> toNode = entries.get(omega);
		assert toNode != null;
		fromNode.successors.add(toNode);
		toNode.predecessors.add(fromNode);
	}

	public void createNode(T data) {
		assert entries.get(data) == null;
		Node<T> n = new Node<T>(data);
		nodes.add(n);
		entries.put(data, n);
	}

	public int getNodeCount() {
		return nodes.size();
	}

	public Set<T> getNodes() {
		return Collections.unmodifiableSet(entries.keySet());
	}

	@SuppressWarnings("unchecked")
	public Set<T> getDirectPredecessorsInTopologicalOrder(T data) {
		return (Set<T>) Collections
				.unmodifiableSet(entries.get(data).predecessors);
	}

	@SuppressWarnings("unchecked")
	public Set<T> getDirectSucccessorsInTopologicalOrder(T data) {
		return (Set<T>) Collections
				.unmodifiableSet(entries.get(data).successors);
	}

}
