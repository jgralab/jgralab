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
package de.uni_koblenz.jgralab.algolib.algorithms.weak_components;

import java.util.HashSet;
import java.util.Set;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.StructureOrientedAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.search.BreadthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.SearchVisitor;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.SearchVisitorAdapter;
import de.uni_koblenz.jgralab.algolib.algorithms.weak_components.visitors.VertexPartitionVisitor;
import de.uni_koblenz.jgralab.algolib.algorithms.weak_components.visitors.VertexPartitionVisitorList;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.Function;
import de.uni_koblenz.jgralab.algolib.problems.WeakComponentsSolver;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;
import de.uni_koblenz.jgralab.graphmarker.ArrayVertexMarker;

public class WeakComponentsWithBFS extends StructureOrientedAlgorithm implements
		WeakComponentsSolver {

	private BreadthFirstSearch bfs;
	private SearchVisitor weakComponentsVisitor;
	private VertexPartitionVisitorList visitors;

	private Function<Vertex, Vertex> weakComponents;
	private Function<Vertex, Set<Vertex>> inverseResult;
	private int kappa;

	public WeakComponentsWithBFS(Graph graph, BreadthFirstSearch bfs) {
		this(graph, bfs, null);
	}

	public WeakComponentsWithBFS(Graph graph, BreadthFirstSearch bfs,
			BooleanFunction<Edge> navigable) {
		super(graph, navigable);
		this.bfs = bfs;
	}

	@Override
	public void addVisitor(Visitor visitor) {
		checkStateForSettingVisitors();
		if (visitor instanceof VertexPartitionVisitor) {
			visitor.setAlgorithm(this);
			visitors.addVisitor(visitor);
		} else {
			// the algorithm is set implicitly to the bfs
			bfs.addVisitor(visitor);
		}
	}

	@Override
	public void removeVisitor(Visitor visitor) {
		checkStateForSettingVisitors();
		if (visitor instanceof VertexPartitionVisitor) {
			visitors.removeVisitor(visitor);
		} else {
			bfs.removeVisitor(visitor);
		}
	}

	@Override
	public WeakComponentsWithBFS normal() {
		throw new UnsupportedOperationException(
				"This algorithm is only defined for undirected graphs.");
	}

	@Override
	public WeakComponentsWithBFS reversed() {
		throw new UnsupportedOperationException(
				"This algorithm is only defined for undirected graphs.");
	}

	@Override
	public WeakComponentsWithBFS undirected() {
		super.undirected();
		return this;
	}

	@Override
	public boolean isHybrid() {
		return false;
	}

	public WeakComponentsWithBFS withInverseResult() {
		checkStateForSettingParameters();
		inverseResult = new ArrayVertexMarker<Set<Vertex>>(graph);
		return this;
	}

	public WeakComponentsWithBFS withoutInverseResult() {
		checkStateForSettingParameters();
		inverseResult = null;
		return this;
	}

	@Override
	public void reset() {
		super.reset();
		weakComponents = new ArrayVertexMarker<Vertex>(graph);
		inverseResult = inverseResult == null ? null
				: new ArrayVertexMarker<Set<Vertex>>(graph);
		kappa = 0;
	}

	@Override
	public void resetParameters() {
		super.resetParameters();
		visitors = new VertexPartitionVisitorList();
		traversalDirection = EdgeDirection.INOUT;
		weakComponentsVisitor = new SearchVisitorAdapter() {

			private Vertex currentRepresentativeVertex;

			@Override
			public void visitRoot(Vertex v) throws AlgorithmTerminatedException {
				kappa++;
				currentRepresentativeVertex = v;
				if (inverseResult != null) {
					assert inverseResult.get(v) == null;
					inverseResult.set(v, new HashSet<Vertex>());
				}
				visitors.visitRepresentativeVertex(v);
			}

			@Override
			public void visitVertex(Vertex v)
					throws AlgorithmTerminatedException {
				weakComponents.set(v, currentRepresentativeVertex);
				if (inverseResult != null) {
					Set<Vertex> currentSubSet = inverseResult
							.get(currentRepresentativeVertex);
					assert currentSubSet != null;
					currentSubSet.add(v);
				}
			}

		};
	}

	@Override
	public void disableOptionalResults() {
		checkStateForSettingParameters();
		inverseResult = null;
	}

	@Override
	public WeakComponentsWithBFS execute() throws AlgorithmTerminatedException {
		bfs.reset();
		bfs.setGraph(graph);
		bfs.setNavigable(navigable);
		bfs.undirected();
		bfs.addVisitor(weakComponentsVisitor);
		startRunning();
		try {
			bfs.execute();
		} catch (AlgorithmTerminatedException e) {
			bfs.terminate();
		} finally {
			bfs.removeVisitor(weakComponentsVisitor);
			done();
		}
		return this;
	}

	@Override
	protected void done() {
		state = AlgorithmStates.FINISHED;
	}

	@Override
	public Function<Vertex, Vertex> getWeakComponents() {
		checkStateForResult();
		return weakComponents;
	}

	public Function<Vertex, Set<Vertex>> getInverseResult() {
		checkStateForResult();
		return inverseResult;
	}

	@Override
	public int getKappa() {
		checkStateForResult();
		return kappa;
	}

	public Function<Vertex, Vertex> getInternalWeakComponents() {
		return weakComponents;
	}

	public Function<Vertex, Set<Vertex>> getInternalInverseResult() {
		return inverseResult;
	}

	public int getInternalKappa() {
		return kappa;
	}

}
