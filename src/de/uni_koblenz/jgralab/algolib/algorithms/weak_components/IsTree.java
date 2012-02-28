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

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.StructureOrientedAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.search.BreadthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.SearchVisitor;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.SearchVisitorAdapter;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.problems.IsTreeSolver;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

public class IsTree extends StructureOrientedAlgorithm implements IsTreeSolver {

	private WeakComponentsWithBFS wcbfs;
	private SearchVisitor isTreeVisitor;
	private boolean isTree;

	public IsTree(Graph graph, BooleanFunction<Edge> navigable) {
		super(graph, navigable);
	}

	public IsTree(Graph graph) {
		this(graph, null);
	}

	@Override
	public void resetParameters() {
		super.resetParameters();
		traversalDirection = EdgeDirection.INOUT;
		wcbfs = new WeakComponentsWithBFS(graph, new BreadthFirstSearch(graph));
		isTreeVisitor = new SearchVisitorAdapter() {

			@Override
			public void visitFrond(Edge e) throws AlgorithmTerminatedException {
				terminate();
			}

		};
	}

	@Override
	public void reset() {
		super.reset();
		isTree = false;
	}

	@Override
	public void addVisitor(Visitor visitor) {
		wcbfs.addVisitor(visitor);
	}

	@Override
	public void disableOptionalResults() {
	}

	@Override
	protected void done() {
		state = AlgorithmStates.FINISHED;
	}

	@Override
	public boolean isHybrid() {
		return false;
	}

	@Override
	public void removeVisitor(Visitor visitor) {
		wcbfs.removeVisitor(visitor);
	}

	@Override
	public IsTreeSolver execute() throws AlgorithmTerminatedException {
		wcbfs.reset();
		wcbfs.setGraph(graph);
		wcbfs.setNavigable(navigable);
		wcbfs.addVisitor(isTreeVisitor);
		try {
			startRunning();
			wcbfs.execute();
			isTree = wcbfs.getKappa() <= 1;
		} catch (AlgorithmTerminatedException e) {
			isTree = false;
		}
		wcbfs.removeVisitor(isTreeVisitor);
		done();
		return this;
	}

	@Override
	public boolean isTree() throws IllegalStateException {
		checkStateForResult();
		return isTree;
	}

	public boolean getInternalIsTree() {
		return isTree;
	}

}
