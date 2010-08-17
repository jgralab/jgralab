package de.uni_koblenz.jgralab.algolib.algorithms.search.visitors;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.search.SearchAlgorithm;
import de.uni_koblenz.jgralab.algolib.visitors.GraphVisitorAdapter;

/**
 * It implements all methods from <code>SearchVisitor</code> as empty stubs. Beyond
 * that, it also handles the storage of the search algorithm object implementing
 * visitors are used by. All instances of <code>SearchVisitor</code> should use
 * this class as superclass.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public class SearchVisitorAdapter extends GraphVisitorAdapter implements
		SearchVisitor {

	@Override
	public void visitFrond(Edge e) {

	}

	@Override
	public void visitRoot(Vertex v) {

	}

	@Override
	public void visitTreeEdge(Edge e) {

	}
	
	protected SearchAlgorithm algorithm;

	@Override
	public void setAlgorithm(GraphAlgorithm algorithm) {
		if (algorithm instanceof SearchAlgorithm) {
			this.algorithm = (SearchAlgorithm) algorithm;
			reset();
		} else {
			throw new IllegalArgumentException(
					"This visitor is not compatible with "
							+ algorithm.getClass().getSimpleName()
							+ " It only works with instances of "
							+ SearchAlgorithm.class.getSimpleName());
		}
	}

	public SearchAlgorithm getAlgorithm() {
		return algorithm;
	}


}
