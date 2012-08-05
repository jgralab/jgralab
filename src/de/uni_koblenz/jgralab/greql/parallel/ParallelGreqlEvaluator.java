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
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql.GreqlEnvironment;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.evaluator.GreqlEnvironmentAdapter;
import de.uni_koblenz.jgralab.schema.impl.DirectedAcyclicGraph;

/**
 */
public class ParallelGreqlEvaluator {

	private DirectedAcyclicGraph<TaskHandle> dependencyGraph;
	private static Logger log = JGraLab.getLogger(ParallelGreqlEvaluator.class
			.getPackage().getName());

	static {
		log.setLevel(Level.OFF);
	}

	/**
	 */
	public class EvaluationEnvironment {
		private Graph datagraph;
		private GreqlEnvironment greqlEnvironment;
		private HashMap<TaskHandle, Integer> indegree;
		private HashMap<TaskHandle, EvaluationTask> tasks;
		private ExecutorService executor;
		private Exception exception;

		private EvaluationEnvironment() {
			// no construction from outside
			indegree = new HashMap<TaskHandle, Integer>();
			tasks = new HashMap<TaskHandle, EvaluationTask>();
		}

		public GreqlEnvironment getGreqlEnvironment() {
			return greqlEnvironment;
		}

		public Object getResult(TaskHandle handle) {
			EvaluationTask t = tasks.get(handle);
			if (!t.isDone()) {
				throw new IllegalStateException("Task is not done");
			}
			try {
				return tasks.get(handle).get();
			} catch (InterruptedException e) {
				// should not occur since task is done
				e.printStackTrace();
			} catch (ExecutionException e) {
				// should not occur since task is done
				e.printStackTrace();
			}
			return null;
		}
	}

	/**
	 *
	 */
	private class EvaluationTask extends FutureTask<Object> {
		TaskHandle handle;
		EvaluationEnvironment environment;

		EvaluationTask(EvaluationEnvironment environment, TaskHandle handle,
				Callable<Object> callable) {
			super(callable);
			this.environment = environment;
			this.handle = handle;
		}

		@Override
		public void run() {
			log.finer("Run " + handle);
			super.run();
		}

		@Override
		protected void done() {
			log.fine("Done " + handle);
			super.done();
			try {
				// try to get the result in order to handle a possible exception
				// exception is rapped into an ExecutionException
				get();

				// no exception - schedule next task
				scheduleNext(environment, handle);
			} catch (InterruptedException e) {
				// interrupted by shutdown
				environment.executor.shutdownNow();
			} catch (ExecutionException e) {
				// remember exception an shuwdown executor
				synchronized (environment) {
					if (environment.exception == null) {
						environment.exception = e;
					}
				}
				environment.executor.shutdownNow();
			}
		}
	}

	/**
	 * 
	 */
	private static int sequence = 0;

	public class TaskHandle implements Comparable<TaskHandle> {
		private Callable<Object> callable;
		private GreqlQuery query;
		private int priority;
		private int seq;

		@Override
		public String toString() {
			return "TaskHandle " + seq + " (prio " + priority + ")";
		}

		@Override
		public int compareTo(TaskHandle other) {
			// order w.r.t. descending priority
			int r = other.priority - priority;
			return r == 0 ? other.seq - seq : r;
		}

		private TaskHandle() {
			seq = sequence++;
		}

		private TaskHandle(Callable<Object> callable, int priority) {
			this();
			this.callable = callable;
			this.priority = priority;
		}

		private TaskHandle(GreqlQuery query, int priority) {
			this();
			this.query = query;
			this.priority = priority;
		}

		private EvaluationTask createFutureTask(final EvaluationEnvironment env) {
			if (callable != null) {
				return new EvaluationTask(env, this, callable);
			} else {
				return new EvaluationTask(env, this, new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						return query.evaluate(env.datagraph,
								env.greqlEnvironment);
					}
				});
			}
		}
	}

	/**
	 * 
	 */
	public ParallelGreqlEvaluator() {
		dependencyGraph = new DirectedAcyclicGraph<TaskHandle>();
	}

	/**
	 * @return
	 */
	public EvaluationEnvironment evaluate() {
		return evaluate(null, new GreqlEnvironmentAdapter());
	}

	/**
	 * @param datagraph
	 * @return
	 */
	public EvaluationEnvironment evaluate(Graph datagraph) {
		return evaluate(datagraph, new GreqlEnvironmentAdapter());
	}

	/**
	 * @param datagraph
	 * @param environment
	 * @return
	 */
	public EvaluationEnvironment evaluate(Graph datagraph,
			GreqlEnvironment environment) {
		if (!dependencyGraph.isFinished()) {
			calculateVariableDependencies();
			dependencyGraph.finish();
			log.finer(dependencyGraph.toString());
		}

		final EvaluationEnvironment env = new EvaluationEnvironment();
		env.datagraph = datagraph;
		env.greqlEnvironment = environment;

		// at least 2 threads, at most available processors + 1 (for the
		// resultcollector)
		int threads = Math.max(2,
				Runtime.getRuntime().availableProcessors() + 1);
		env.executor = Executors.newFixedThreadPool(threads);

		Set<TaskHandle> initialTasks = new TreeSet<TaskHandle>();
		for (TaskHandle handle : dependencyGraph.getNodes()) {
			EvaluationTask t = handle.createFutureTask(env);
			env.tasks.put(handle, t);
			int i = dependencyGraph.getDirectPredecessors(handle).size();
			env.indegree.put(handle, i);
			if (i == 0) {
				initialTasks.add(handle);
			}
		}
		FutureTask<Object> waitForTerminationTask = new FutureTask<Object>(
				new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						try {
							for (TaskHandle handle : dependencyGraph.getNodes()) {
								env.tasks.get(handle).get();
							}
						} catch (InterruptedException e) {
							// do nothing, probably interrupted by exception
						}
						return null;
					}
				});
		env.executor.execute(waitForTerminationTask);
		for (TaskHandle handle : initialTasks) {
			log.fine("Execute initial " + handle);
			env.executor.execute(env.tasks.get(handle));
		}
		try {
			waitForTerminationTask.get();
		} catch (InterruptedException e) {
			// do nothing, since exception is handled below
			e.printStackTrace();
		} catch (ExecutionException e) {
			// do nothing, since exception is handled below
			e.printStackTrace();
		}
		synchronized (env) {
			if (env.exception != null) {
				Throwable inner = env.exception;
				while (inner != null && inner instanceof ExecutionException) {
					inner = inner.getCause();
				}
				if (inner instanceof RuntimeException) {
					throw (RuntimeException) inner;
				} else {
					throw new RuntimeException(inner);
				}
			}
		}
		env.executor.shutdown();
		return env;
	}

	/**
	 * @param callable
	 * @return
	 */
	public TaskHandle addCallable(Callable<Object> callable) {
		return addCallable(callable, 0);
	}

	/**
	 * @param callable
	 * @param priority
	 * @return
	 */
	public TaskHandle addCallable(Callable<Object> callable, int priority) {
		return dependencyGraph.createNode(new TaskHandle(callable, priority));
	}

	/**
	 * @param queryText
	 * @return
	 */
	public TaskHandle addGreqlQuery(String queryText) {
		return addGreqlQuery(queryText, 0);
	}

	/**
	 * @param queryText
	 * @param priority
	 * @return
	 */
	public TaskHandle addGreqlQuery(String queryText, int priority) {
		return addGreqlQuery(GreqlQuery.createQuery(queryText), priority);
	}

	/**
	 * @param query
	 * @return
	 */
	public TaskHandle addGreqlQuery(GreqlQuery query) {
		return addGreqlQuery(query, 0);
	}

	/**
	 * @param query
	 * @param priority
	 * @return
	 */
	public TaskHandle addGreqlQuery(GreqlQuery query, int priority) {
		return dependencyGraph.createNode(new TaskHandle(query, priority));
	}

	/**
	 * @param successor
	 * @param predecessor
	 */
	public void createDependency(TaskHandle use, TaskHandle def) {
		dependencyGraph.createEdge(def, use);
	}

	private void calculateVariableDependencies() {
		// add dependencies based on used/stored variables of GReQL queries
		HashMap<String, HashSet<TaskHandle>> defs = new HashMap<String, HashSet<TaskHandle>>();
		for (TaskHandle handle : dependencyGraph.getNodes()) {
			if (handle.query == null) {
				continue;
			}
			Set<String> sv = handle.query.getStoredVariables();
			if (sv == null) {
				continue;
			}
			for (String var : sv) {
				HashSet<TaskHandle> vs = defs.get(var);
				if (vs == null) {
					vs = new HashSet<TaskHandle>();
					defs.put(var, vs);
				}
				vs.add(handle);
			}
		}
		for (TaskHandle use : dependencyGraph.getNodes()) {
			if (use.query == null) {
				continue;
			}
			Set<String> uv = use.query.getUsedVariables();
			if (uv == null) {
				continue;
			}
			for (String var : uv) {
				HashSet<TaskHandle> defines = defs.get(var);
				if (defines != null) {
					for (TaskHandle def : defines) {
						createDependency(use, def);
					}
				}
			}
		}
	}

	/**
	 * @param env
	 * @param finishedTask
	 */
	private void scheduleNext(EvaluationEnvironment env, TaskHandle finishedTask) {
		Set<TaskHandle> nextTasks = new TreeSet<TaskHandle>();
		synchronized (env) {
			for (TaskHandle succ : dependencyGraph
					.getDirectSuccessors(finishedTask)) {
				int i = env.indegree.get(succ) - 1;
				env.indegree.put(succ, i);
				if (i == 0) {
					nextTasks.add(succ);
				}
			}
		}
		for (TaskHandle succ : nextTasks) {
			log.fine("Execute " + succ);
			env.executor.execute(env.tasks.get(succ));
		}
	}
}
