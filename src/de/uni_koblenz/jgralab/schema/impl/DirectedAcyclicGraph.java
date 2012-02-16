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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class DirectedAcyclicGraph<T> extends DirectedGraph<T> {

	private List<T> topologicalOrder;

	public DirectedAcyclicGraph() {
		topologicalOrder = Collections.unmodifiableList(new ArrayList<T>(0));
	}

	protected boolean computeTopologicalOrder() {
		topologicalOrder = new ArrayList<T>(nodes.size());
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
		while (!q.isEmpty()) {
			Node<T> n = q.poll();
			topologicalOrder.add(n.data);
			for (Node<T> c : n.successors) {
				--c.mark;
				if (c.mark == 0) {
					q.offer(c);
				}
			}
		}
		assert topologicalOrder.size() <= nodes.size();
		topologicalOrder = Collections.unmodifiableList(topologicalOrder);
		return topologicalOrder.size() == nodes.size();
	}

	@Override
	public void createNode(T data) {
		super.createNode(data);
		computeTopologicalOrder();
	}

	public List<T> getAllPredecessorsInTopologicalOrder(T data) {
		Node<T> n = entries.get(data);
		Set<T> s = new HashSet<T>();
		Queue<Node<T>> q = new LinkedList<Node<T>>(n.predecessors);
		while (!q.isEmpty()) {
			n = q.poll();
			if (!s.contains(n.data)) {
				s.add(n.data);
				for (Node<T> x : n.predecessors) {
					q.offer(x);
				}
			}
		}
		List<T> result = new ArrayList<T>();
		for (T x : topologicalOrder) {
			if (s.contains(x)) {
				result.add(x);
			}
		}
		return Collections.unmodifiableList(result);
	}

	public List<T> getAllSucccessorsInTopologicalOrder(T data) {
		Node<T> n = entries.get(data);
		Set<T> s = new HashSet<T>();
		Queue<Node<T>> q = new LinkedList<Node<T>>(n.successors);
		while (!q.isEmpty()) {
			n = q.poll();
			if (!s.contains(n.data)) {
				s.add(n.data);
				for (Node<T> x : n.successors) {
					q.offer(x);
				}
			}
		}
		List<T> result = new ArrayList<T>();
		for (T x : topologicalOrder) {
			if (s.contains(x)) {
				result.add(x);
			}
		}
		return Collections.unmodifiableList(result);
	}

	@Override
	public void createEdge(T alpha, T omega) {
		super.createEdge(alpha, omega);
		if (!computeTopologicalOrder()) {
			Node<T> fromNode = entries.get(alpha);
			Node<T> toNode = entries.get(omega);
			fromNode.successors.remove(toNode);
			toNode.predecessors.remove(fromNode);
			computeTopologicalOrder();
			throw new CycleException(alpha, omega);
		}
	}

	public List<T> getNodesInTopologicalOrder() {
		return topologicalOrder;
	}
}
