/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2012 Institute for Software Technology
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
	public Set<T> getDirectPredecessors(T data) {
		return (Set<T>) Collections
				.unmodifiableSet(entries.get(data).predecessors);
	}

	@SuppressWarnings("unchecked")
	public Set<T> getDirectSucccessors(T data) {
		return (Set<T>) Collections
				.unmodifiableSet(entries.get(data).successors);
	}

}
