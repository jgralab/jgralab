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

package de.uni_koblenz.jgralab.schema.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.pcollections.ArrayPSet;
import org.pcollections.PSet;

import de.uni_koblenz.jgralab.schema.exception.SchemaException;

public class DirectedGraph<T> {

	protected final static class Node<T> {
		final T data;
		PSet<T> successors;
		PSet<T> predecessors;

		int mark;

		protected void rehash() {
			PSet<T> tmpSuccessors = successors;
			successors = ArrayPSet.empty();
			successors = successors.plusAll(tmpSuccessors);

			PSet<T> tmpPredecessors = predecessors;
			predecessors = ArrayPSet.empty();
			predecessors = predecessors.plusAll(tmpPredecessors);
		}

		Node(T data) {
			assert data != null;
			this.data = data;
			successors = ArrayPSet.empty();
			predecessors = ArrayPSet.empty();
		}
	}

	protected PSet<Node<T>> nodes;
	protected Map<T, Node<T>> entries;
	protected boolean finished;
	protected PSet<T> nodeValues;

	public DirectedGraph() {
		nodes = ArrayPSet.empty();
		entries = new HashMap<>();
		nodeValues = ArrayPSet.empty();
	}

	private boolean rehashNeeded = false;

	/**
	 * To be called whenever a T-object contained in this DirectedGraph changes
	 * in such a way that its hashCode() changes. Since T-objects are stored in
	 * hash-sets and are used as keys in hash-maps, a rehash is needed and will
	 * be performed when the directed graph is finished again.
	 */
	public void setRehashNeeded() {
		rehashNeeded = true;
	}

	protected void rehashIfNeeded() {
		if (!rehashNeeded) {
			return;
		}
		// The qualifiedNames of the elements might have changed, so rebuild
		// hash maps and sets.
		Map<T, Node<T>> s = new HashMap<>();
		for (Entry<T, Node<T>> e : entries.entrySet()) {
			s.put(e.getKey(), e.getValue());
		}
		entries = s;
		for (Node<T> n : nodes) {
			n.rehash();
		}

		PSet<T> tmpNodeValues = nodeValues;
		nodeValues = ArrayPSet.empty();
		nodeValues = nodeValues.plusAll(tmpNodeValues);

		rehashNeeded = false;
	}

	public void finish() {
		rehashIfNeeded();
		finished = true;
	}

	public boolean isFinished() {
		return finished;
	}

	private void checkEdgeCreateDelete(T alpha, T omega) {
		if (finished) {
			throw new IllegalStateException("Graph is already finished.");
		}
		if (!nodeValues.contains(alpha)) {
			throw new IllegalArgumentException("alpha " + alpha
					+ " doesn't belong to this graph.");
		}
		if (!nodeValues.contains(omega)) {
			throw new IllegalArgumentException("omega " + omega
					+ " doesn't belong to this graph.");
		}
	}

	public void createEdge(T alpha, T omega) {
		checkEdgeCreateDelete(alpha, omega);
		if (alpha.equals(omega)) {
			// don't allow loops
			throw new SchemaException("Loops are not supported.");
		}
		rehashIfNeeded();
		Node<T> fromNode = entries.get(alpha);
		assert fromNode != null;
		if (fromNode.successors.contains(omega)) {
			// don't create parallel edges
			return;
		}
		Node<T> toNode = entries.get(omega);
		assert toNode != null;
		fromNode.successors = fromNode.successors.plus(omega);
		toNode.predecessors = toNode.predecessors.plus(alpha);
	}

	public void deleteEdge(T alpha, T omega) {
		checkEdgeCreateDelete(alpha, omega);
		if (alpha.equals(omega)) {
			return;
		}
		rehashIfNeeded();
		Node<T> fromNode = entries.get(alpha);
		assert fromNode != null;
		Node<T> toNode = entries.get(omega);
		assert toNode != null;
		fromNode.successors = fromNode.successors.minus(omega);
		toNode.predecessors = toNode.predecessors.minus(alpha);
	}

	public T createNode(T data) {
		if (finished) {
			throw new IllegalStateException("Graph is already finished.");
		}
		rehashIfNeeded();
		assert !nodeValues.contains(data);
		assert entries.get(data) == null;
		nodeValues = nodeValues.plus(data);
		Node<T> n = new Node<>(data);
		nodes = nodes.plus(n);
		entries.put(data, n);
		return data;
	}

	public int getNodeCount() {
		rehashIfNeeded();
		return nodeValues.size();
	}

	public PSet<T> getNodes() {
		rehashIfNeeded();
		return nodeValues;
	}

	public boolean isConnected(T alpha, T omega) {
		rehashIfNeeded();
		return entries.get(alpha).successors.contains(omega);
	}

	public PSet<T> getDirectPredecessors(T data) {
		rehashIfNeeded();
		assert nodeValues.contains(data);
		return entries.get(data).predecessors;
	}

	public PSet<T> getDirectSuccessors(T data) {
		rehashIfNeeded();
		assert nodeValues.contains(data);
		return entries.get(data).successors;
	}

	public void delete(T data) {
		if (finished) {
			throw new IllegalStateException("Graph is already finished.");
		}
		rehashIfNeeded();
		Node<T> node = entries.get(data);
		entries.remove(data);
		nodes = nodes.minus(node);
		nodeValues = nodeValues.minus(node);
		for (T pred : node.predecessors) {
			Node<T> predNode = entries.get(pred);
			predNode.successors = predNode.successors.minus(data);
		}
		for (T succ : node.successors) {
			Node<T> succNode = entries.get(succ);
			succNode.predecessors = succNode.predecessors.minus(data);
		}
	}
}
