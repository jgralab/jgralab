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

import java.util.HashMap;
import java.util.Map;

import org.pcollections.ArrayPSet;
import org.pcollections.PSet;

import de.uni_koblenz.jgralab.schema.exception.SchemaException;

public class DirectedGraph<T> {

	protected static class Node<T> {
		final T data;
		PSet<T> successors;
		PSet<T> predecessors;

		int mark;

		Node(T data) {
			assert data != null;
			this.data = data;
			successors = ArrayPSet.empty();
			predecessors = ArrayPSet.empty();
		}
	}

	protected PSet<Node<T>> nodes;
	protected final Map<T, Node<T>> entries;
	protected boolean finished;
	protected PSet<T> nodeValues;

	public DirectedGraph() {
		nodes = ArrayPSet.empty();
		entries = new HashMap<T, Node<T>>();
		nodeValues = ArrayPSet.empty();
	}

	public void finish() {
		finished = true;
	}

	public boolean isFinished() {
		return finished;
	}

	public void createEdge(T alpha, T omega) {
		if (finished) {
			throw new IllegalStateException("Graph is already finished.");
		}
		if (!nodeValues.contains(alpha)) {
			throw new IllegalArgumentException(
					"alpha doesn't belong to this graph.");
		}
		if (!nodeValues.contains(omega)) {
			throw new IllegalArgumentException(
					"omega doesn't belong to this graph.");
		}
		if (alpha.equals(omega)) {
			// don't allow loops
			throw new SchemaException("Loops are not supported.");
		}
		Node<T> fromNode = entries.get(alpha);
		if (fromNode.successors.contains(omega)) {
			// don't create parallel edges
			return;
		}
		assert fromNode != null;
		Node<T> toNode = entries.get(omega);
		assert toNode != null;
		fromNode.successors = fromNode.successors.plus(omega);
		toNode.predecessors = toNode.predecessors.plus(alpha);
	}

	public T createNode(T data) {
		if (finished) {
			throw new IllegalStateException("Graph is already finished.");
		}
		assert !nodeValues.contains(data);
		assert entries.get(data) == null;
		nodeValues = nodeValues.plus(data);
		Node<T> n = new Node<T>(data);
		nodes = nodes.plus(n);
		entries.put(data, n);
		return data;
	}

	public int getNodeCount() {
		return nodeValues.size();
	}

	public PSet<T> getNodes() {
		return nodeValues;
	}

	public boolean isConnected(T alpha, T omega) {
		return entries.get(alpha).successors.contains(omega);
	}

	public PSet<T> getDirectPredecessors(T data) {
		assert nodeValues.contains(data);
		return entries.get(data).predecessors;
	}

	public PSet<T> getDirectSucccessors(T data) {
		assert nodeValues.contains(data);
		return entries.get(data).successors;
	}

	public void delete(T data) {
		if (finished) {
			throw new IllegalStateException("Graph is already finished.");
		}
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
