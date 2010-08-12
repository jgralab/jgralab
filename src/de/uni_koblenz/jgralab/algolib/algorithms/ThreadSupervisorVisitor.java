package de.uni_koblenz.jgralab.algolib.algorithms;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.visitors.SimpleVisitorAdapter;

/**
 * This visitor checks, if the current thread has been interrupted and cancels
 * the algorithm if this has happened. If the algorithm should check when
 * visiting vertices and edges, just create an instance of this class. If the
 * algorithm should only check when visiting vertices xor edges, use the
 * provided factory methods for creating the appropriate visitor.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public class ThreadSupervisorVisitor extends SimpleVisitorAdapter {

	/**
	 * Creates a <code>ThreadSupervisorVisitor</code> that only checks the state
	 * of the Thread when visiting vertices.
	 * 
	 * @return a new instance of <code>ThreadSupervisorVisitor</code>
	 */
	public static ThreadSupervisorVisitor createThreadSupervisorVisitorForVerticesOnly() {
		return new ThreadSupervisorVisitor() {

			@Override
			public void visitEdge(Edge e) {
				// do nothing
			}

		};
	}

	/**
	 * Creates a <code>ThreadSupervisorVisitor</code> that only checks the state
	 * of the Thread when visiting edges.
	 * 
	 * @return a new instance of <code>ThreadSupervisorVisitor</code>
	 */
	public static ThreadSupervisorVisitor createThreadSupervisorVisitorForEdgesOnly() {
		return new ThreadSupervisorVisitor() {

			@Override
			public void visitVertex(Vertex v) {
				// do nothing
			}

		};
	}

	/**
	 * Creates a <code>ThreadSupervisorVisitor</code> that checks the state of
	 * the Thread when visiting vertices and edges.
	 * 
	 */
	public ThreadSupervisorVisitor() {

	}
	
	/**
	 * The graph algorithm this visitor is used by.
	 */
	protected GraphAlgorithm alg;

	@Override
	public void setAlgorithm(GraphAlgorithm alg) {
		this.alg = alg;
	}

	@Override
	public void visitVertex(Vertex v) {
		alg.cancelIfInterrupted();
	}

	@Override
	public void visitEdge(Edge e) {
		alg.cancelIfInterrupted();
	}

}
