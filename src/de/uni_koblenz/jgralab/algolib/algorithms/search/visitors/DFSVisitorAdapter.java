/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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
package de.uni_koblenz.jgralab.algolib.algorithms.search.visitors;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.search.DepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.SearchAlgorithm;

/**
 * It implements all methods from <code>DFSVisitor</code> as empty stubs. Beyond
 * that, it also handles the storage of the DFS algorithm object implementing
 * visitors are used by. All instances of <code>DFSVisitor</code> should use
 * this class as superclass.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public class DFSVisitorAdapter extends SearchVisitorAdapter implements
		DFSVisitor {

	/**
	 * The DFS this visitor is used by.
	 */
	protected DepthFirstSearch algorithm;

	@Override
	public void leaveTreeEdge(Edge e) throws AlgorithmTerminatedException {

	}

	@Override
	public void leaveVertex(Vertex v) throws AlgorithmTerminatedException {

	}

	@Override
	public void visitBackwardArc(Edge e) throws AlgorithmTerminatedException {

	}

	@Override
	public void visitCrosslink(Edge e) throws AlgorithmTerminatedException {

	}

	@Override
	public void visitForwardArc(Edge e) throws AlgorithmTerminatedException {

	}

	@Override
	public void setAlgorithm(GraphAlgorithm algorithm) {
		if (algorithm instanceof SearchAlgorithm) {
			this.algorithm = (DepthFirstSearch) algorithm;
			reset();
		} else {
			throw new IllegalArgumentException(
					"This visitor is not compatible with "
							+ algorithm.getClass().getSimpleName()
							+ " It only works with instances of "
							+ DepthFirstSearch.class.getSimpleName());
		}
	}

	@Override
	public DepthFirstSearch getAlgorithm() {
		return algorithm;
	}

}
