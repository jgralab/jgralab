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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql.GreqlEnvironment;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlEnvironmentAdapter;
import de.uni_koblenz.jgralab.schema.exception.CycleException;
import de.uni_koblenz.jgralab.schema.exception.SchemaException;
import de.uni_koblenz.jgralab.schema.impl.DirectedAcyclicGraph;

/**
 * {@link ParallelGreqlEvaluator} executes
 * {@link ParallelGreqlEvaluatorCallable}s parallely. Particularly,
 * {@link GreqlQuery} also implements {@link ParallelGreqlEvaluatorCallable}.
 * 
 * Adding a query or a callable returns a {@link TaskHandle}. This
 * {@link TaskHandle} can later be used to access the results.
 * 
 * The tasks may depend on each other. For example, a query using a variable
 * stored by an other query is dependent on that storing query. Such variable
 * dependencies are computed automatically. Additional dependencies can be
 * specified by {@link #defineDependency(TaskHandle, TaskHandle)}.
 * {@link ParallelGreqlEvaluator} internally maintains a directed acyclic graph
 * to store dependencies. Tasks submitted to a {@link ThreadPoolExecutor} as
 * soon as all predecessors have completed.
 * 
 * When adding the same {@link ParallelGreqlEvaluatorCallable} task multiple
 * times, the {@link ParallelGreqlEvaluatorCallable} is responsible for
 * maintaining separate internal states for possibly parallel executions. For
 * GReQL queries, {@link ParallelGreqlEvaluator} takes care of separate
 * environments.
 * 
 * A call to one of the {@link #evaluate()} methods executes all tasks using a
 * {@link ThreadPoolExecutor}. The number of active threads is set automatically
 * to {@link Runtime#availableProcessors()}. Independent tasks are executed
 * parallely.
 * 
 * {@link #evaluate()} methods are thread-safe, i.e. many evaluations can be
 * started in parallel. Local data of each evaluation is stored in an
 * {@link EvaluationEnvironment}. The {@link EvaluationEnvironment} is also used
 * to access the results via {@link EvaluationEnvironment#getResult(TaskHandle)}
 * , and to access the {@link GreqlEnvironment} used by all queries.
 * 
 * To somehow control execution order, a priority can be specified when adding a
 * {@link GreqlQuery} or a {@link ParallelGreqlEvaluatorCallable}. the add...
 * methods. Tasks with higher priority are submitted first to the
 * {@link ThreadPoolExecutor}. Tasks with the same priority value are sumbitted
 * in the order of addition.
 * 
 * A good choice for priority values is the estimated execution time of the
 * tasks. The {@link ParallelGreqlEvaluator#evaluate(boolean)} methods provide a
 * flag to optionally set the priorities of all tasks to the actual evaluation
 * time. This usually results in optimal schedules in subsequent evaluations.
 */
public class ParallelGreqlEvaluator {
	// Log levels:
	// OFF: no logging
	// FINE: Log task execution and termination
	// FINER: Additionally, log task begin, final waiting task, and
	// dependency graph
	private static Logger logger = JGraLab
			.getLogger(ParallelGreqlEvaluator.class);

	private DirectedAcyclicGraph<TaskHandle> dependencyGraph;

	/**
	 * {@link EvaluationTask} is responsible to execute a {@link Callable}. In
	 * case of an {@link ExecutionException} thrown by the
	 * {@link Callable#call()} method, the executor of the
	 * {@link EvaluationEnvironment} is shut down and the exception is preserved
	 * in the environment. Additionally, {@link EvaluationTask} records the
	 * execution time and does some logging.
	 */
	class EvaluationTask extends FutureTask<Object> {
		private TaskHandle handle;
		private EvaluationEnvironment environment;
		private long startTime, doneTime;

		private EvaluationTask(EvaluationEnvironment environment,
				TaskHandle handle, Callable<Object> callable) {
			super(callable);
			this.environment = environment;
			this.handle = handle;
		}

		@Override
		public void run() {
			logger.finer("Run " + this + " " + handle);
			startTime = System.nanoTime();
			super.run();
		}

		long getEvaluationTime() {
			if (!isDone()) {
				throw new IllegalStateException(
						"EvaluationTask is not yet done.");
			}
			return doneTime - startTime;
		}

		@Override
		protected void done() {
			doneTime = System.nanoTime();
			super.done();
			logger.fine("Done " + handle + " (" + getEvaluationTime() + " ns)");
			if (environment.executor != null) {
				try {
					// try to get the result in order to handle a possible
					// exception
					// exception is rapped into an ExecutionException
					get();

					// no exception - schedule next task
					scheduleNext(environment, handle);
				} catch (InterruptedException e) {
					// interrupted by shutdown
					environment.executor.shutdownNow();
				} catch (ExecutionException e) {
					// remember exception and shuwdown executor
					synchronized (environment) {
						if (environment.exception == null) {
							environment.exception = e;
						}
					}
					environment.executor.shutdownNow();
				}
			}
		}
	}

	/**
	 * used to generate task handle numbers
	 */
	private static int taskHandleSequence = 0;

	/**
	 * A {@link TaskHandle} is used to identify a task added to a
	 * {@link ParallelGreqlEvaluator}. The add... methods return a unique
	 * {@link TaskHandle}. After evaluation, a {@link TaskHandle} can be used to
	 * get the result Object of the associated task from an
	 * {@link EvaluationEnvironment}.
	 */
	public class TaskHandle implements Comparable<TaskHandle> {
		private ParallelGreqlEvaluatorCallable callable;
		private long priority;
		private int seq;

		@Override
		public String toString() {
			return "TaskHandle " + seq + " prio " + priority + " (use: "
					+ getUsedVariables() + ", store:" + getStoredVariables()
					+ ")";
		}

		public Set<String> getStoredVariables() {
			return callable.getStoredVariables();
		}

		public Set<String> getUsedVariables() {
			return callable.getUsedVariables();
		}

		@Override
		public int compareTo(TaskHandle other) {
			// order w.r.t. descending priority
			long r = other.priority - priority;
			if (r != 0) {
				return r < 0 ? -1 : 1;
			}
			// same priority, order w.r.t. ascending sequence number
			return seq - other.seq;
		}

		private TaskHandle(ParallelGreqlEvaluatorCallable callable,
				long priority) {
			this.callable = callable;
			this.priority = priority;
			synchronized (TaskHandle.class) {
				seq = taskHandleSequence++;
			}
		}

		private EvaluationTask createFutureTask(final EvaluationEnvironment env) {
			return new EvaluationTask(env, this, new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					return callable.call(env);
				}
			});
		}
	}

	/**
	 * Constructs an empty {@link ParallelGreqlEvaluator} with no tasks.
	 */
	public ParallelGreqlEvaluator() {
		dependencyGraph = new DirectedAcyclicGraph<TaskHandle>();
	}

	/**
	 * Evaluates all tasks parallely in topological order, according to their
	 * priority, without a data graph and with an initially empty
	 * {@link GreqlEnvironment}.
	 * 
	 * @return an {@link EvaluationEnvironment} containing the results
	 */
	public EvaluationEnvironment evaluate() {
		return evaluate(null, new GreqlEnvironmentAdapter(), false);
	}

	/**
	 * Evaluates all tasks parallely in topological order, according to their
	 * priority, without a data graph and with an initially empty
	 * {@link GreqlEnvironment}.
	 * 
	 * @param adjustPriorityValues
	 *            when set to <code>true</code>, the priority values of all
	 *            {@link TaskHandle}s are set to the evaluation time of the
	 *            associated tasks. This usually results in optimal schedules in
	 *            subsequent evaluations.
	 * @return an {@link EvaluationEnvironment} containing the results
	 */
	public EvaluationEnvironment evaluate(boolean adjustPriorityValues) {
		return evaluate(null, new GreqlEnvironmentAdapter(),
				adjustPriorityValues);
	}

	/**
	 * Evaluates all tasks parallely in topological order, according to their
	 * priority, on the specified <code>datagraph</code> and with an initially
	 * empty {@link GreqlEnvironment}.
	 * 
	 * @param datagraph
	 *            a graph
	 * @return an {@link EvaluationEnvironment} containing the results
	 */
	public EvaluationEnvironment evaluate(Graph datagraph) {
		return evaluate(datagraph, new GreqlEnvironmentAdapter(), false);
	}

	/**
	 * Evaluates all tasks parallely in topological order, according to their
	 * priority, on the specified <code>datagraph</code> and with an initially
	 * empty {@link GreqlEnvironment}.
	 * 
	 * @param datagraph
	 *            a graph
	 * @param adjustPriorityValues
	 *            when set to <code>true</code>, the priority values of all
	 *            {@link TaskHandle}s are set to the evaluation time of the
	 *            associated tasks. This usually results in optimal schedules in
	 *            subsequent evaluations.
	 * @return an {@link EvaluationEnvironment} containing the results
	 */
	public EvaluationEnvironment evaluate(Graph datagraph,
			boolean adjustPriorityValues) {
		return evaluate(datagraph, new GreqlEnvironmentAdapter(),
				adjustPriorityValues);
	}

	/**
	 * Evaluates all tasks parallely in topological order, according to their
	 * priority, on the specified <code>datagraph</code> using and probably
	 * modifiying the provided <code>greqlEnvironment</code>.
	 * 
	 * @param datagraph
	 *            a {@link Graph}
	 * @param greqlEnvironment
	 *            a {@link GreqlEnvironment}, can be used to define external
	 *            variables. STORE queries will put their results into this
	 *            {@link GreqlEnvironment}.
	 * @return an {@link EvaluationEnvironment} containing the results
	 */
	public EvaluationEnvironment evaluate(Graph datagraph,
			GreqlEnvironment greqlEnvironment) {
		return evaluate(datagraph, greqlEnvironment, false);
	}

	/**
	 * Evaluates all tasks parallely in topological order, according to their
	 * priority, on the specified <code>datagraph</code> using and probably
	 * modifiying the provided <code>greqlEnvironment</code>.
	 * 
	 * @param datagraph
	 *            a {@link Graph}
	 * @param greqlEnvironment
	 *            a {@link GreqlEnvironment}, can be used to define external
	 *            variables. STORE queries will put their results into this
	 *            {@link GreqlEnvironment}.
	 * @param adjustPriorityValues
	 *            when set to <code>true</code>, the priority values of all
	 *            {@link TaskHandle}s are set to the evaluation time of the
	 *            associated tasks. This usually results in optimal schedules in
	 *            subsequent evaluations.
	 * @return an {@link EvaluationEnvironment} containing the results
	 */
	public EvaluationEnvironment evaluate(Graph datagraph,
			GreqlEnvironment greqlEnvironment, boolean adjustPriorityValues) {
		final EvaluationEnvironment evaluationEnvironment = new EvaluationEnvironment();
		evaluationEnvironment.startTime = System.nanoTime();
		evaluationEnvironment.datagraph = datagraph;
		evaluationEnvironment.greqlEnvironment = greqlEnvironment;

		Set<TaskHandle> initialTasks = createEvaluationTasks(evaluationEnvironment);

		// at least 2 threads, at most available processors + 1 (for the
		// termination task)
		int threads = Math.max(2,
				Runtime.getRuntime().availableProcessors() + 1);
		logger.fine("Create executor with " + threads + " threads");
		evaluationEnvironment.executor = Executors.newFixedThreadPool(threads);

		// create a task that waits until all other tasks are terminated
		FutureTask<Object> waitForTerminationTask = new FutureTask<Object>(
				new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						logger.finer("Run waiting for final tasks");
						try {
							for (TaskHandle handle : dependencyGraph.getNodes()) {
								evaluationEnvironment.tasks.get(handle).get();
							}
						} catch (InterruptedException e) {
							// do nothing, probably interrupted by exception
						}
						return null;
					}
				}) {
			@Override
			protected void done() {
				super.done();
				logger.finer("Done waiting for final tasks");
			}
		};
		logger.finer("Execute waiting for final tasks");
		evaluationEnvironment.executor.execute(waitForTerminationTask);

		// execute initial tasks
		for (TaskHandle handle : initialTasks) {
			logger.fine("Execute initial " + handle);
			evaluationEnvironment.executor.execute(evaluationEnvironment.tasks
					.get(handle));
		}

		// wait for termination
		try {
			waitForTerminationTask.get();
		} catch (InterruptedException e) {
			// do nothing, since exception is handled below
			e.printStackTrace();
		} catch (ExecutionException e) {
			// do nothing, since exception is handled below
			e.printStackTrace();
		}

		evaluationEnvironment.executor.shutdown();

		// propagate a possible exception thrown by a task execution
		synchronized (evaluationEnvironment) {
			if (evaluationEnvironment.exception != null) {
				// unwrap ExecutionExceptions
				Throwable inner = evaluationEnvironment.exception;
				while (inner != null && inner instanceof ExecutionException) {
					inner = inner.getCause();
				}
				// throw the causing exception
				if (inner instanceof RuntimeException) {
					throw (RuntimeException) inner;
				} else {
					throw new RuntimeException(inner);
				}
			}
		}

		if (adjustPriorityValues) {
			adjustPriorities(evaluationEnvironment);
		}
		evaluationEnvironment.doneTime = System.nanoTime();
		return evaluationEnvironment;
	}

	/**
	 * Sets priorities of all tasks to their evaluation time stored in the
	 * <code>evaluationEnvironment</code>.
	 * 
	 * @param evaluationEnvironment
	 *            an {@link EvaluationEnvironment}
	 */
	private void adjustPriorities(
			final EvaluationEnvironment evaluationEnvironment) {
		// set priority values of all TaskHandles to the actual execution
		// time of the task
		synchronized (dependencyGraph) {
			logger.fine("Adjust priority values");
			for (TaskHandle handle : dependencyGraph.getNodes()) {
				long p = handle.priority;
				handle.priority = evaluationEnvironment
						.getEvaluationTime(handle);
				logger.finer(handle.toString() + " - old prio " + p);
			}
		}
	}

	/**
	 * @return the SortedSet of tasks that can start immediately in order of
	 *         descending priority
	 */
	private SortedSet<TaskHandle> createEvaluationTasks(
			EvaluationEnvironment evaluationEnvironment) {
		// - create EvaluationTasks for all TaskHandles
		// - initalize inDegree map with number of predecessors
		// - determine initial tasks (tasks without predecessors)
		synchronized (dependencyGraph) {
			calculateVariableDependencies();
			SortedSet<TaskHandle> initialTasks = new TreeSet<TaskHandle>();
			for (TaskHandle handle : dependencyGraph.getNodes()) {
				EvaluationTask t = handle
						.createFutureTask(evaluationEnvironment);
				evaluationEnvironment.tasks.put(handle, t);
				int i = dependencyGraph.getDirectPredecessors(handle).size();
				evaluationEnvironment.inDegree.put(handle, i);
				if (i == 0) {
					initialTasks.add(handle);
				}
			}
			return initialTasks;
		}
	}

	/**
	 * Evaluates all tasks sequentially in topological order, according to their
	 * priority, on the specified <code>datagraph</code> using and probably
	 * modifiying the provided <code>greqlEnvironment</code>.
	 * 
	 * @param datagraph
	 *            a {@link Graph}
	 * @param greqlEnvironment
	 *            a {@link GreqlEnvironment}, can be used to define external
	 *            variables. STORE queries will put their results into this
	 *            {@link GreqlEnvironment}.
	 * @param adjustPriorityValues
	 *            when set to <code>true</code>, the priority values of all
	 *            {@link TaskHandle}s are set to the evaluation time of the
	 *            associated tasks. This usually results in optimal schedules in
	 *            subsequent evaluations.
	 * @return an {@link EvaluationEnvironment} containing the results
	 */
	public EvaluationEnvironment evaluateSequentially(Graph datagraph,
			GreqlEnvironment greqlEnvironment, boolean adjustPriorityValues) {
		EvaluationEnvironment env = new EvaluationEnvironment();
		env.startTime = System.nanoTime();
		env.datagraph = datagraph;
		env.greqlEnvironment = greqlEnvironment;
		// create tasks and determine initial tasks
		SortedSet<TaskHandle> tasks = createEvaluationTasks(env);
		Queue<TaskHandle> q = new LinkedList<TaskHandle>(tasks);
		while (!q.isEmpty()) {
			TaskHandle t = q.poll();
			// run the task
			env.tasks.get(t).run();
			// determine tasks that can be started after t has completed, i.e.
			// tasks that have no more unfinished predecessors
			synchronized (dependencyGraph) {
				tasks.clear();
				for (TaskHandle succ : dependencyGraph.getDirectSuccessors(t)) {
					int i = env.inDegree.get(succ) - 1;
					env.inDegree.put(succ, i);
					if (i == 0) {
						tasks.add(succ);
					}
				}
				for (TaskHandle th : tasks) {
					q.offer(th);
				}
			}
		}
		if (adjustPriorityValues) {
			adjustPriorities(env);
		}
		env.doneTime = System.nanoTime();
		return env;
	}

	/**
	 * Adds the <code>callable</code> to this {@link ParallelGreqlEvaluator}
	 * with priority 0.
	 * 
	 * @param callable
	 *            a {@link ParallelGreqlEvaluatorCallable} to be called by this
	 *            {@link ParallelGreqlEvaluator}
	 * @return a {@link TaskHandle} identifying the task associated with the
	 *         <code>callable</code>
	 */
	public TaskHandle addTask(ParallelGreqlEvaluatorCallable callable) {
		return addTask(callable, 0);
	}

	/**
	 * Adds the <code>callable</code> to this {@link ParallelGreqlEvaluator}
	 * with priority <code>priority</code>.
	 * 
	 * @param callable
	 *            a {@link ParallelGreqlEvaluatorCallable} to be called by this
	 *            {@link ParallelGreqlEvaluator}
	 * @param priority
	 *            a priority value, higher values mean higher priority
	 * @return a {@link TaskHandle} identifying the task associated with the
	 *         <code>callable</code>
	 */
	public TaskHandle addTask(ParallelGreqlEvaluatorCallable callable,
			long priority) {
		return dependencyGraph.createNode(new TaskHandle(callable, priority));
	}

	/**
	 * Constructs a {@link GreqlQuery} from the <code>queryText</code> and adds
	 * it to this {@link ParallelGreqlEvaluator} with priority 0.
	 * 
	 * @param queryText
	 *            the GReQL text for the {@link GreqlQuery}
	 * @return a {@link TaskHandle} identifying the task associated with the
	 *         <code>greqlQuery</code>
	 */
	public TaskHandle addGreqlQuery(String queryText) {
		return addGreqlQuery(queryText, 0);
	}

	/**
	 * Constructs a {@link GreqlQuery} from the <code>queryText</code> and adds
	 * it to this {@link ParallelGreqlEvaluator} with priority
	 * <code>priority</code>.
	 * 
	 * @param queryText
	 *            the GReQL text for the {@link GreqlQuery}
	 * @param priority
	 *            a priority value, higher values mean higher priority
	 * @return a {@link TaskHandle} identifying the task associated with the
	 *         <code>greqlQuery</code>
	 */
	public TaskHandle addGreqlQuery(String queryText, long priority) {
		return addTask(GreqlQuery.createQuery(queryText), priority);
	}

	/**
	 * Defines a dependency to specify that the task <code>predecessor</code>
	 * must be completed before the task <code>successor</code> can start.
	 * 
	 * @param successor
	 *            the TaskHandle of the successor task
	 * @param predecessor
	 *            the TaskHandle of the predecessor task
	 */
	public void defineDependency(TaskHandle successor, TaskHandle predecessor) {
		try {
			dependencyGraph.createEdge(predecessor, successor);
		} catch (CycleException e) {
			throw new RuntimeException(
					"Task dependencies are cyclic. Offending dependency: "
							+ successor + " ---dependsOn--> " + predecessor);
		} catch (SchemaException e) {
			throw new RuntimeException("Task " + predecessor
					+ " depends on itself");
		}
	}

	/**
	 * Adds dependencies between GReQL queries. A query using a variable
	 * <em>v</em> (<code>using v: ...</code>) is dependent on all queries
	 * defining this variable (<code>... store as v</code>).
	 */
	private void calculateVariableDependencies() {
		synchronized (dependencyGraph) {
			if (dependencyGraph.isFinished()) {
				return;
			}
			// add dependencies based on used/stored variables of GReQL queries
			HashMap<String, HashSet<TaskHandle>> definingTasks = new HashMap<String, HashSet<TaskHandle>>();

			// determine TaskHandles that define (store) a variable
			for (TaskHandle handle : dependencyGraph.getNodes()) {
				Set<String> sv = handle.getStoredVariables();
				if (sv == null) {
					continue;
				}
				for (String var : sv) {
					HashSet<TaskHandle> vs = definingTasks.get(var);
					if (vs == null) {
						vs = new HashSet<TaskHandle>();
						definingTasks.put(var, vs);
					}
					vs.add(handle);
				}
			}

			// create dependencies for the usages of variables
			for (TaskHandle usingTask : dependencyGraph.getNodes()) {
				Set<String> uv = usingTask.getUsedVariables();
				if (uv == null) {
					continue;
				}
				for (String var : uv) {
					HashSet<TaskHandle> defines = definingTasks.get(var);
					if (defines != null) {
						for (TaskHandle def : defines) {
							defineDependency(usingTask, def);
						}
					}
				}
			}
			dependencyGraph.finish();
			logger.finer(dependencyGraph.toString());
		}
	}

	/**
	 * Submits all executable successors of the <code>finishedTask</code> to the
	 * <code>environment</code>'s executor. A successor is executable if
	 * <code>finishedTask</code> was the successor's last completed predecessor.
	 * 
	 * @param environment
	 *            the {@link EvaluationEnvironment}
	 * @param finishedTask
	 *            the {@link TaskHandle} of the completed task
	 */
	private void scheduleNext(EvaluationEnvironment environment,
			TaskHandle finishedTask) {
		// determine tasks that can be started after finishedTask has completed,
		// i.e. tasks that have no more unfinished predecessors
		synchronized (dependencyGraph) {
			Set<TaskHandle> nextTasks = new TreeSet<TaskHandle>();
			for (TaskHandle succ : dependencyGraph
					.getDirectSuccessors(finishedTask)) {
				int i = environment.inDegree.get(succ) - 1;
				environment.inDegree.put(succ, i);
				if (i == 0) {
					nextTasks.add(succ);
				}
			}
			// submit successor tasks to the executor
			for (TaskHandle succ : nextTasks) {
				logger.fine("Execute " + succ);
				environment.executor.execute(environment.tasks.get(succ));
			}
		}
	}
}
