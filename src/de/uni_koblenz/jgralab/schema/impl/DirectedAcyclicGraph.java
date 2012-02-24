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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.pcollections.ArrayPSet;
import org.pcollections.ArrayPVector;
import org.pcollections.PSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.schema.exception.CycleException;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;

public class DirectedAcyclicGraph<T> extends DirectedGraph<T> {

	private PVector<T> topologicalOrder;
	private HashMap<T, PSet<T>> cachedPredecessors;
	private HashMap<T, PSet<T>> cachedSuccessors;
	private boolean transitive;

	/**
	 * Constructs an empty {@link DirectedAcyclicGraph} with non-transitive
	 * edges.
	 */
	public DirectedAcyclicGraph() {
		this(false);
	}

	/**
	 * Constructs an empty {@link DirectedAcyclicGraph} with the specified
	 * transitivity.
	 * 
	 * @param transitive
	 *            if set to <code>true</code>, redundant edges are removed,
	 *            otherwise they are retained. E.g. when edges A-&gt;B, A-&gt;C,
	 *            B-&gt;C are created, the edge A-&gt;C would be considered
	 *            redundant and would be removed, since it it implied by the
	 *            remaining edges A-&gt;B, B-&gt;C.
	 */
	public DirectedAcyclicGraph(boolean transitive) {
		topologicalOrder = ArrayPVector.empty();
		this.transitive = transitive;
	}

	protected boolean computeTopologicalOrder() {
		Queue<Node<T>> q = new LinkedList<Node<T>>();
		// Enter all nodes without a predecessor into q
		for (Node<T> n : nodes) {
			n.mark = n.predecessors.size();
			if (n.mark == 0) {
				q.offer(n);
			}
		}
		// Take a node from q as long as q is not empty
		// Add it to the topolocicalOrder list
		// Add all of his successors to q for which it is true,
		// that all predecessors are already in the list
		topologicalOrder = ArrayPVector.empty();
		while (!q.isEmpty()) {
			Node<T> n = q.poll();
			topologicalOrder = topologicalOrder.plus(n.data);
			for (T c : n.successors) {
				Node<T> o = entries.get(c);
				--o.mark;
				if (o.mark == 0) {
					q.offer(o);
				}
			}
		}
		assert topologicalOrder.size() <= nodes.size();
		return topologicalOrder.size() == nodes.size();
	}

	@Override
	public void createNode(T data) {
		super.createNode(data);
		computeTopologicalOrder();
	}

	@Override
	public void finish() {
		cachedPredecessors = new HashMap<T, PSet<T>>();
		cachedSuccessors = new HashMap<T, PSet<T>>();
		for (Node<T> n : nodes) {
			cachedPredecessors.put(n.data,
					getAllPredecessorsInTopologicalOrder(n.data));
			cachedSuccessors.put(n.data,
					getAllSuccessorsInTopologicalOrder(n.data));
		}
		super.finish();
		if (transitive) {
			for (T data : topologicalOrder) {
				Node<T> n = entries.get(data);
				Set<T> indirectSuccessors = getIndirectSuccessors(data);
				indirectSuccessors.retainAll(n.successors);
				n.successors = n.successors.minusAll(indirectSuccessors);
				for (T p : indirectSuccessors) {
					Node<T> np = entries.get(p);
					np.predecessors = np.predecessors.minus(data);
				}
			}
		}
	}

	private Set<T> getIndirectSuccessors(T data) {
		if (!finished) {
			throw new SchemaException();
		}
		Node<T> n = entries.get(data);
		Set<T> s = new HashSet<T>();
		Queue<T> q = new LinkedList<T>();
		for (T p : n.successors) {
			q.addAll(entries.get(p).successors);
		}
		while (!q.isEmpty()) {
			n = entries.get(q.poll());
			if (!s.contains(n.data)) {
				s.add(n.data);
				for (T x : n.successors) {
					q.offer(x);
				}
			}
		}
		return s;
	}

	public PSet<T> getAllPredecessorsInTopologicalOrder(T data) {
		if (finished) {
			return cachedPredecessors.get(data);
		}
		Node<T> n = entries.get(data);
		Set<T> s = new HashSet<T>();
		Queue<T> q = new LinkedList<T>(n.predecessors);
		while (!q.isEmpty()) {
			n = entries.get(q.poll());
			if (!s.contains(n.data)) {
				s.add(n.data);
				for (T x : n.predecessors) {
					q.offer(x);
				}
			}
		}
		PSet<T> result = ArrayPSet.empty();
		for (T x : topologicalOrder) {
			if (s.contains(x)) {
				result = result.plus(x);
			}
		}
		return result;
	}

	public PSet<T> getAllSuccessorsInTopologicalOrder(T data) {
		if (finished) {
			return cachedSuccessors.get(data);
		}
		Node<T> n = entries.get(data);
		Set<T> s = new HashSet<T>();
		Queue<T> q = new LinkedList<T>(n.successors);
		while (!q.isEmpty()) {
			n = entries.get(q.poll());
			if (!s.contains(n.data)) {
				s.add(n.data);
				for (T x : n.successors) {
					q.offer(x);
				}
			}
		}
		PSet<T> result = ArrayPSet.empty();
		for (T x : topologicalOrder) {
			if (s.contains(x)) {
				result = result.plus(x);
			}
		}
		return result;
	}

	@Override
	public void createEdge(T alpha, T omega) {
		super.createEdge(alpha, omega);
		if (!computeTopologicalOrder()) {
			Node<T> fromNode = entries.get(alpha);
			Node<T> toNode = entries.get(omega);
			fromNode.successors = fromNode.successors.minus(toNode);
			toNode.predecessors = toNode.predecessors.minus(fromNode);
			computeTopologicalOrder();
			throw new CycleException(alpha, omega);
		}
	}

	public PVector<T> getNodesInTopologicalOrder() {
		return topologicalOrder;
	}

	@Override
	public String toString() {
		HashMap<T, Integer> idx = new HashMap<T, Integer>();
		StringBuilder sb = new StringBuilder();
		sb.append("digraph g {\n");
		int i = 0;
		for (T data : topologicalOrder) {
			idx.put(data, i);
			sb.append("\tn").append(i).append(" [ label=\"")
					.append(data.toString()).append("\" ];\n");
			++i;
		}
		for (T data : topologicalOrder) {
			Node<T> n = entries.get(data);
			for (T s : n.successors) {
				sb.append("\tn").append(idx.get(data)).append(" -> n")
						.append(idx.get(s)).append(";\n");
			}
		}
		sb.append("}\n");
		return sb.toString();
	}
}
