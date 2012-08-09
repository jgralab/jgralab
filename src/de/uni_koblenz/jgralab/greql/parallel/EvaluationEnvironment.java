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
package de.uni_koblenz.jgralab.greql.parallel;

import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.greql.GreqlEnvironment;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.parallel.ParallelGreqlEvaluator.EvaluationTask;
import de.uni_koblenz.jgralab.greql.parallel.ParallelGreqlEvaluator.TaskHandle;

/**
 * {@link EvaluationEnvironment} contains data local to a single evaluation.
 * Additionally, the {@link GreqlEnvironment} used to evaluate the queries and
 * the result objects of each task are available.
 */
public class EvaluationEnvironment {
	Graph datagraph;
	GreqlEnvironment greqlEnvironment;
	ExecutorService executor;
	Exception exception;
	HashMap<TaskHandle, Integer> inDegree;
	HashMap<TaskHandle, EvaluationTask> tasks;
	long startTime;
	long doneTime;

	EvaluationEnvironment() {
		// package scoped constructor, no construction from outside
		inDegree = new HashMap<TaskHandle, Integer>();
		tasks = new HashMap<TaskHandle, EvaluationTask>();
	}

	/**
	 * @return the {@link GreqlEnvironment} used to evaluate all queries
	 */
	public GreqlEnvironment getGreqlEnvironment() {
		return greqlEnvironment;
	}

	/**
	 * @return the {@link Graph} used to evaluate all queries
	 */
	public Graph getDatagraph() {
		return datagraph;
	}

	/**
	 * Returns the result Object of the {@link GreqlQuery} or the
	 * {@link Callable} associated with the {@link TaskHandle}
	 * <code>handle</code>.
	 * 
	 * @param handle
	 *            a {@link TaskHandle} that was returned by one of the
	 *            {@link ParallelGreqlEvaluator}'s add... methods.
	 * @return the result Object of the {@link GreqlQuery} or the
	 *         {@link Callable}
	 * @throws IllegalStateException
	 *             when the task for the {@link TaskHandle} is not done
	 * @throws IllegalArgumentException
	 *             when the {@link TaskHandle} does not belong to this
	 *             {@link EvaluationEnvironment}
	 */
	public Object getResult(TaskHandle handle) {
		EvaluationTask t = tasks.get(handle);
		if (!t.isDone()) {
			throw new IllegalStateException(handle + " is not yet done.");
		}
		if (!tasks.containsKey(handle)) {
			throw new IllegalArgumentException(handle
					+ " does not belong to this EvaluationEnvironment.");
		}
		try {
			return tasks.get(handle).get();
		} catch (InterruptedException e) {
			// should not occur since task is done
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			// should not occur since task is done
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return the total evaluation time in ns
	 */
	public long getEvaluationTime() {
		return doneTime - startTime;
	}

	/**
	 * @param handle
	 *            a TaskHandle returned by one of {@link ParallelGreqlEvaluator}
	 *            's add... methods
	 * @return the evaluation time in ms for the task associated with
	 *         {@link TaskHandle} <code>handle</code>
	 */
	public long getEvaluationTime(TaskHandle handle) {
		return tasks.get(handle).getEvaluationTime();
	}
}