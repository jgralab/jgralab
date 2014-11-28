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
package de.uni_koblenz.jgralab.algolib.algorithms.topological_order;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.StructureOrientedAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.topological_order.visitors.TopologicalOrderVisitorList;
import de.uni_koblenz.jgralab.algolib.functions.ArrayPermutation;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;
import de.uni_koblenz.jgralab.algolib.functions.Permutation;
import de.uni_koblenz.jgralab.algolib.problems.AcyclicitySolver;
import de.uni_koblenz.jgralab.algolib.problems.TopologicalOrderSolver;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;
import de.uni_koblenz.jgralab.graphmarker.IntegerVertexMarker;

public class KahnKnuthAlgorithm extends StructureOrientedAlgorithm implements
		AcyclicitySolver, TopologicalOrderSolver {

	private TopologicalOrderVisitorList visitors;
	private int tnum;
	private int firstV;
	private Vertex[] torder;
	private IntFunction<Vertex> tnumber;
	private IntFunction<Vertex> inDegree;
	private boolean acyclic;
	private EdgeDirection degreeDirection;

	public KahnKnuthAlgorithm(Graph graph, BooleanFunction<Edge> navigable) {
		super(graph, navigable);
	}

	public KahnKnuthAlgorithm(Graph graph) {
		this(graph, null);
	}

	public KahnKnuthAlgorithm withTNumber() {
		checkStateForSettingParameters();
		tnumber = new IntegerVertexMarker(graph);
		return this;
	}

	@Override
	public KahnKnuthAlgorithm normal() {
		super.normal();
		degreeDirection = EdgeDirection.IN;
		return this;
	}

	@Override
	public KahnKnuthAlgorithm reversed() {
		super.reversed();
		degreeDirection = EdgeDirection.OUT;
		return this;
	}

	public KahnKnuthAlgorithm withTnumber() {
		checkStateForSettingParameters();
		tnumber = new IntegerVertexMarker(graph);
		return this;
	}

	public KahnKnuthAlgorithm withoutTnumber() {
		checkStateForSettingParameters();
		tnumber = null;
		return this;
	}

	@Override
	public void disableOptionalResults() {
		checkStateForSettingParameters();
		tnumber = null;
	}

	@Override
	public void addVisitor(Visitor visitor) {
		checkStateForSettingVisitors();
		visitor.setAlgorithm(this);
		visitors.addVisitor(visitor);
	}

	@Override
	public void removeVisitor(Visitor visitor) {
		checkStateForSettingVisitors();
		visitors.removeVisitor(visitor);
	}

	@Override
	public void reset() {
		super.reset();
		tnum = 1;
		firstV = 1;
		torder = new Vertex[graph.getVCount() + 1];
		inDegree = new IntegerVertexMarker(graph);
		acyclic = true;
		visitors.reset();
	}

	@Override
	public void resetParameters() {
		super.resetParameters();
		visitors = new TopologicalOrderVisitorList();
		normal();
	}

	@Override
	protected void done() {
		state = AlgorithmStates.FINISHED;
	}

	@Override
	public boolean isDirected() {
		return true;
	}

	@Override
	public boolean isHybrid() {
		return false;
	}

	@Override
	public boolean isAcyclic() {
		checkStateForResult();
		return acyclic;
	}

	@Override
	public Permutation<Vertex> getTopologicalOrder() {
		checkStateForResult();
		return new ArrayPermutation<>(torder);
	}

	public IntFunction<Vertex> getTNumber() {
		checkStateForResult();
		return tnumber;
	}

	public Vertex[] getInternalTopologicalOrder() {
		return torder;
	}

	public IntFunction<Vertex> getInternalTNumber() {
		return tnumber;
	}

	public boolean getInternalAcyclic() {
		return acyclic;
	}

	public int getFirstV() {
		return firstV;
	}

	public int getTNum() {
		return tnum;
	}

	public IntFunction<Vertex> getInDegree() {
		return inDegree;
	}

	@Override
	public KahnKnuthAlgorithm execute() throws AlgorithmTerminatedException {
		startRunning();

		// store actual in degree for all vertices
		for (Vertex currentVertex : graph.vertices()) {
			int degree = currentVertex.getDegree(degreeDirection);
			inDegree.set(currentVertex, degree);
			if (degree == 0) {
				torder[tnum] = currentVertex;
				if (tnumber != null) {
					tnumber.set(currentVertex, tnum);
				}
				tnum++;
			}

		}
		while (firstV < tnum) {
			Vertex currentVertex = torder[firstV++];
			visitors.visitVertexInTopologicalOrder(currentVertex);
			for (Edge currentEdge : currentVertex
					.incidences(traversalDirection)) {
				cancelIfInterrupted();
				if (navigable != null && !navigable.get(currentEdge)) {
					continue;
				}
				Vertex nextVertex = currentEdge.getThat();
				inDegree.set(nextVertex, inDegree.get(nextVertex) - 1);
				if (inDegree.get(nextVertex) == 0) {
					torder[tnum] = nextVertex;
					if (tnumber != null) {
						tnumber.set(nextVertex, tnum);
					}
					tnum++;
				}
			}
		}
		if (tnum < graph.getVCount()) {
			acyclic = false;
		}
		done();
		return this;
	}

}
