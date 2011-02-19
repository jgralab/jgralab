/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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
package de.uni_koblenz.jgralabtest.algolib.algorithms;

public abstract class GraphAlgorithmTest {

	/**
	 * Tests the algorithm with the whole graph.
	 */
	public abstract void testAlgorithm();

	/**
	 * Tests if the vertex count is computed correctly.
	 */
	public abstract void testGetVertexCount();

	/**
	 * Tests if the edge count is computed correctly.
	 */
	public abstract void testGetEdgeCount();

	/**
	 * Tests the states and the state transitions.
	 */
	public abstract void testStates();

	/**
	 * Tests if early termination works correctly.
	 */
	public abstract void testEarlyTermination();

	/**
	 * Tests if thread interruption works correctly.
	 */
	public abstract void testCancel();

	/*
	 * Tests if addVisitor works correctly and that visitors are not added multiply.
	 */
	// public abstract void testAddVisitor();

	/**
	 * Tests addVisitor for throwing an exception if the algorithm is not
	 * compatible with the visitor (it acctually tests setAlgorithm from
	 * visitor).
	 */
	public abstract void testAddVisitorForException();

	/**
	 * Tests if remove visitor actually removes the visitors from the list.
	 */
	public abstract void testRemoveVisitor();
	
	/**
	 * Tests if all parameters have been resetted to their default values.
	 */
	public abstract void testResetParameters();
	
	/**
	 * Tests if all runtime variables have been initialized correctly.
	 */
	public abstract void testReset();

}
