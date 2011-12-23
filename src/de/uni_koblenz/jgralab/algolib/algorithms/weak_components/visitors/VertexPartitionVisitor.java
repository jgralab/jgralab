package de.uni_koblenz.jgralab.algolib.algorithms.weak_components.visitors;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

public interface VertexPartitionVisitor extends Visitor {

	/**
	 * Visits the representative vertex of a partition's subset.
	 * 
	 * @param v
	 *            the representative vertex to visit
	 */
	public void visitRepresentativeVertex(Vertex v);

}
