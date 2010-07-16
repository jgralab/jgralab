package de.uni_koblenz.jgralab.algolib.visitors;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;

/**
 * This visitor allows visiting vertices and edges without distinguishing
 * between special vertices and edges. During an algorithm run, each method
 * should only be called at most once per vertex/edge. The algorithms have to
 * ensure this.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface SimpleVisitor {

	/**
	 * Executes arbitrary code in the context of the given vertex <code>v</code>
	 * .
	 * 
	 * @param v
	 *            the vertex that is currently visited
	 */
	public void visitVertex(Vertex v);

	/**
	 * Executes arbitrary code in the context of the given edge <code>e</code>.
	 * 
	 * @param e
	 *            the edge that is currently visited
	 */
	public void visitEdge(Edge e);

	/**
	 * If this visitor is used for computing results, this function resets this
	 * visitor. This includes reinitializing all variables needed for the
	 * computation of the result. If this visitor does not contain variables
	 * that need to be reinitialized, this method may do nothing.
	 */
	public void reset();

	/**
	 * Sets the graph algorithm this visitor is used by. This method is to be
	 * used for setting a field in the actual visitor object for allowing access
	 * to the intermediate results of the algorithm. This is required by many
	 * visitors for performing their own computations based on the computations
	 * made by the algorithm. The field of implementing visitor classes should
	 * use an explicit algorithm type and this method should perform a type
	 * check. This avoids unnecessary casts when accessing the visitor objects.
	 * 
	 * @param alg
	 *            the algorithm object that uses this visitor
	 * @throws IllegalArgumentException
	 *             if the given algorithm is incompatible with this visitor
	 */
	public void setAlgorithm(GraphAlgorithm alg);

}
