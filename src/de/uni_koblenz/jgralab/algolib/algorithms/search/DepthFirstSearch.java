/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
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
package de.uni_koblenz.jgralab.algolib.algorithms.search;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.DFSVisitorComposition;
import de.uni_koblenz.jgralab.algolib.functions.ArrayPermutation;
import de.uni_koblenz.jgralab.algolib.functions.BooleanFunction;
import de.uni_koblenz.jgralab.algolib.functions.Permutation;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;
import de.uni_koblenz.jgralab.graphmarker.IntegerVertexMarker;

/**
 * This is the abstract superclass for all algorithms implementing the depth
 * first search. The optional function <code>number</code> is mandatory for DFS
 * and cannot be switched off. It introduces a new algorithm result
 * <code>rnumber</code> and a new optional result <code>rorder</code>. Both
 * results describe the order the vertices are left after all incident edges
 * have been completely traversed.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public abstract class DepthFirstSearch extends SearchAlgorithm {

	/**
	 * The visitor composition containing all visitors.
	 */
	protected DFSVisitorComposition visitors;

	/**
	 * This variable is needed for the computation of <code>rnumber</code> and
	 * <code>rorder</code>.
	 */
	protected int rNum;

	/**
	 * The internal representation of the result <code>rnumber</code>.
	 */
	protected IntFunction<Vertex> rnumber;

	/**
	 * The internal representation of the optional result <code>rorder</code>.
	 */
	protected Vertex[] rorder;

	public DepthFirstSearch(Graph graph,
			BooleanFunction<GraphElement> subgraph,
			BooleanFunction<Edge> navigable) {
		super(graph, subgraph, navigable);
	}

	public DepthFirstSearch(Graph graph) {
		this(graph, null, null);
	}

	@Override
	public void reset() {
		super.reset();
		visitors.reset();
		rNum = 1;
		rorder = rorder == null ? null : new Vertex[getVertexCount() + 1];
		rnumber = new IntegerVertexMarker(graph);
		number = new IntegerVertexMarker(graph);
	}

	@Override
	public DepthFirstSearch withLevel() {
		super.withLevel();
		return this;
	}

	@Override
	public DepthFirstSearch withNumber() {
		checkStateForSettingParameters();
		super.withNumber();
		return this;
	}

	@Override
	public DepthFirstSearch withENumber() {
		checkStateForSettingParameters();
		super.withENumber();
		return this;
	}

	@Override
	public DepthFirstSearch withParent() {
		super.withParent();
		return this;
	}

	/**
	 * Activates the computation of the optional result <code>rorder</code>.
	 * 
	 * @return this <code>DepthFirstSearch</code>.
	 * @throws IllegalStateException
	 *             if not in state <code>INITIALIZED</code>.
	 */
	public DepthFirstSearch withRorder() {
		checkStateForSettingParameters();
		rorder = new Vertex[getVertexCount() + 1];
		return this;
	}

	@Override
	public DepthFirstSearch withoutLevel() {
		super.withoutLevel();
		return this;
	}

	@Override
	public DepthFirstSearch withoutNumber() {
		checkStateForSettingParameters();
		throw new UnsupportedOperationException(
				"The result \"number\" is mandatory for DFS and cannot be deactivated.");
	}
	
	public DepthFirstSearch withoutENumber() {
		checkStateForSettingParameters();
		super.withoutENumber();
		return this;
	}

	@Override
	public DepthFirstSearch withoutParent() {
		super.withoutParent();
		return this;
	}

	/**
	 * Deactivates the computation of the optional result <code>rorder</code>.
	 * 
	 * @return this <code>DepthFirstSearch</code>.
	 * @throws IllegalStateException
	 *             if not in state <code>INITIALIZED</code>.
	 */
	public DepthFirstSearch withoutRorder() {
		checkStateForSettingParameters();
		rorder = null;
		return this;
	}

	@Override
	public DepthFirstSearch normal() {
		super.normal();
		return this;
	}

	@Override
	public DepthFirstSearch reversed() {
		super.reversed();
		return this;
	}

	@Override
	public DepthFirstSearch undirected() {
		super.undirected();
		return this;
	}

	@Override
	public void resetParameters() {
		super.resetParameters();
		visitors = new DFSVisitorComposition();
	}

	@Override
	public void disableOptionalResults() {
		checkStateForSettingParameters();
		level = null;
		parent = null;
		rorder = null;
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

	/**
	 * @return the internal representation of the optional result
	 *         <code>rorder</code>.
	 */
	public Vertex[] getInternalRorder() {
		return rorder;
	}

	public int getRNum() {
		return rNum;
	}

	/**
	 * @return the internal representation of the result <code>rnumber</code>.
	 */
	public IntFunction<Vertex> getInternalRnumber() {
		return rnumber;
	}

	/**
	 * @return the result <code>rnumber</code>.
	 */
	public IntFunction<Vertex> getRnumber() {
		checkStateForResult();
		return rnumber;
	}

	/**
	 * @return the result <code>rorder</code>.
	 */
	public Permutation<Vertex> getRorder() {
		checkStateForResult();
		return rorder == null ? null : new ArrayPermutation<Vertex>(rorder);
	}

}
