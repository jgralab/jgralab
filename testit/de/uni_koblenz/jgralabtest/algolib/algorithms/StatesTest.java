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
package de.uni_koblenz.jgralabtest.algolib.algorithms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmStates;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;

public class StatesTest {

	private SimpleGraph graph;
	private TestAlgorithm algorithm;

	private static class TestAlgorithm extends GraphAlgorithm {

		public TestAlgorithm(Graph graph) {
			super(graph);
		}

		@Override
		public void removeVisitor(Visitor visitor) {
		}

		@Override
		public boolean isHybrid() {
			return false;
		}

		@Override
		public boolean isDirected() {
			return true;
		}

		@Override
		protected void done() {
			state = AlgorithmStates.FINISHED;
		}

		@Override
		public void disableOptionalResults() {
		}

		@Override
		public void addVisitor(Visitor visitor) {
		}

		public void setAlgorithmState(AlgorithmStates newState) {
			state = newState;
		}
	}

	@Before
	public void setUp() {
		graph = TestGraphs.getSimpleAcyclicGraph();
		algorithm = new TestAlgorithm(graph);
	}

	@Test
	public void testInitialized() {
		algorithm.setAlgorithmState(AlgorithmStates.INITIALIZED);
		try {
			algorithm.reset();
		} catch (IllegalStateException e) {
			fail("It should be possible to reset an algorithm while in state INITIALIZED.");
		}
		try {
			algorithm.checkStateForSettingParameters();
		} catch (IllegalStateException e) {
			fail("It should be possible to set parameters while in state INITIALIZED.");
		}
		try {
			algorithm.checkStateForResult();
			fail("It should not be possible to retrieve the result while in state INITIALIZED.");
		} catch (IllegalStateException e) {
		}
		try {
			algorithm.checkStateForSettingVisitors();
		} catch (IllegalStateException e) {
			fail("It should be possible to set visitors while in state INITIALIZED.");
		}
		try {
			algorithm.terminate();
		} catch (AlgorithmTerminatedException e) {
			fail("It should not be possible to terminate an algorithm while in state INITIALIZED.");
		} catch (IllegalStateException e) {
		}
	}

	@Test
	public void testRunning() {
		algorithm.setAlgorithmState(AlgorithmStates.RUNNING);
		try {
			algorithm.reset();
			fail("It should not be possible to reset an algorithm while in state RUNNING.");
		} catch (IllegalStateException e) {
		}
		try {
			algorithm.checkStateForSettingParameters();
			fail("It should not be possible to set parameters while in state RUNNING.");
		} catch (IllegalStateException e) {
		}
		try {
			algorithm.checkStateForResult();
			fail("It should not be possible to retrieve the result while in state RUNNING.");
		} catch (IllegalStateException e) {
		}
		try {
			algorithm.checkStateForSettingVisitors();
			fail("It should not be possible to set visitors while in state RUNNING.");
		} catch (IllegalStateException e) {
		}
		try {
			algorithm.terminate();
		} catch (AlgorithmTerminatedException e) {
		} catch (IllegalStateException e) {
			fail("It should be possible to terminate an algorithm while in state RUNNING.");
		}
	}

	@Test
	public void testFinished() {
		algorithm.setAlgorithmState(AlgorithmStates.FINISHED);
		try {
			algorithm.reset();
		} catch (IllegalStateException e) {
			fail("It should be possible to reset an algorithm while in state FINISHED.");
		}
		assertEquals(AlgorithmStates.INITIALIZED, algorithm.getState());
		algorithm.setAlgorithmState(AlgorithmStates.FINISHED);
		try {
			algorithm.checkStateForSettingParameters();
			fail("It should not be possible to set parameters while in state FINISHED.");
		} catch (IllegalStateException e) {
		}
		try {
			algorithm.checkStateForResult();
		} catch (IllegalStateException e) {
			fail("It should be possible to retrieve the result while in state FINISHED.");
		}
		try {
			algorithm.checkStateForSettingVisitors();
		} catch (IllegalStateException e) {
			fail("It should be possible to set visitors while in state FINISHED.");
		}
		try {
			algorithm.terminate();
		} catch (AlgorithmTerminatedException e) {
			fail("It should not be possible to terminate an algorithm while in state FINISHED.");
		} catch (IllegalStateException e) {
		}
	}

	@Test
	public void testStopped() {
		algorithm.setAlgorithmState(AlgorithmStates.STOPPED);
		try {
			algorithm.reset();
		} catch (IllegalStateException e) {
			fail("It should be possible to reset an algorithm while in state STOPPED.");
		}
		assertEquals(AlgorithmStates.INITIALIZED, algorithm.getState());
		algorithm.setAlgorithmState(AlgorithmStates.STOPPED);
		try {
			algorithm.checkStateForSettingParameters();
			fail("It should not be possible to set parameters while in state STOPPED.");
		} catch (IllegalStateException e) {
		}
		try {
			algorithm.checkStateForResult();
		} catch (IllegalStateException e) {
			fail("It should be possible to retrieve the result while in state STOPPED.");
		}
		try {
			algorithm.checkStateForSettingVisitors();
		} catch (IllegalStateException e) {
			fail("It should be possible to set visitors while in state STOPPED.");
		}
		try {
			algorithm.terminate();
		} catch (AlgorithmTerminatedException e) {
			fail("It should not be possible to terminate an algorithm while in state STOPPED.");
		} catch (IllegalStateException e) {
		}
	}

	@Test
	public void testCanceled() {
		algorithm.setAlgorithmState(AlgorithmStates.CANCELED);
		try {
			algorithm.reset();
		} catch (IllegalStateException e) {
			fail("It should be possible to reset an algorithm while in state CANCELED.");
		}
		assertEquals(AlgorithmStates.INITIALIZED, algorithm.getState());
		algorithm.setAlgorithmState(AlgorithmStates.CANCELED);
		try {
			algorithm.checkStateForSettingParameters();
			fail("It should not be possible to set parameters while in state CANCELED.");
		} catch (IllegalStateException e) {
		}
		try {
			algorithm.checkStateForResult();
			fail("It should not be possible to retrieve the result while in state CANCELED.");
		} catch (IllegalStateException e) {
		}
		try {
			algorithm.checkStateForSettingVisitors();
			fail("It should not be possible to set visitors while in state CANCELED.");
		} catch (IllegalStateException e) {
		}
		try {
			algorithm.terminate();
		} catch (AlgorithmTerminatedException e) {
			fail("It should not be possible to terminate an algorithm while in state CANCELED.");
		} catch (IllegalStateException e) {
		}
	}
}
