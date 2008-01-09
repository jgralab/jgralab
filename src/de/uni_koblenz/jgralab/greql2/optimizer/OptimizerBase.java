/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.optimizer;

import java.util.List;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.schema.FunctionId;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Aggregation;
import de.uni_koblenz.jgralab.greql2.schema.SourcePosition;
import de.uni_koblenz.jgralab.greql2.schema.Variable;

/**
 * Base class for all {@link Optimizer}s which defines some useful methods that
 * are needed in derived Classes.
 * 
 * @author Tassilo Horn (heimdall), 2007, Diploma Thesis
 * 
 */
public abstract class OptimizerBase implements Optimizer {

	/**
	 * Merges the contents of the sourcePosition attribute of <code>from</code>
	 * to the contents of the sourcePosition attribute of <code>to</code>. If
	 * a {@link SourcePosition} already exists in <code>to</code> it won't be
	 * added again.
	 * 
	 * @param from
	 *            a {@link Greql2Aggregation}
	 * @param to
	 *            another {@link Greql2Aggregation}
	 */
	protected void mergeSourcePositions(Greql2Aggregation from,
			Greql2Aggregation to) {
		List<SourcePosition> toSourcePositions = to.getSourcePositions();
		for (SourcePosition sp : from.getSourcePositions()) {
			if (!toSourcePositions.contains(sp)) {
				toSourcePositions.add(sp);
			}
		}
	}

	/**
	 * Makes a deep copy of the subgraph given by <code>origVertex</code>.
	 * For each {@link Vertex} in that subgraph a new {@link Vertex} of the same
	 * will be created, likewise for the {@link Edge}s. As an exception to that
	 * rule, {@link FunctionId}s and {@link Variable} vertices won't be copied.
	 * 
	 * The new {@link Edge} will have the same <code>sourcePositions</code> as
	 * the original {@link Edge}s.
	 * 
	 * @param origVertex
	 *            the root {@link Vertex} of the subgraph to be copied
	 * @param graph
	 *            the {@link Graph} where <code>origVertex</code> is part of.
	 * @return the root {@link Vertex} of the copy
	 */
	protected Vertex copySubgraph(Vertex origVertex, Greql2 graph) {
		Vertex newVertex;
		if (origVertex instanceof Variable || origVertex instanceof FunctionId) {
			// FunctionIds and Variables aren't copied. Since those are always
			// leaves, we can stop here.
			return origVertex;
		}
		newVertex = graph.createVertex(origVertex.getClass());
		Edge origEdge = origVertex.getFirstEdge(EdgeDirection.IN);
		Vertex subVertex;
		Edge newEdge;
		while (origEdge != null) {
			subVertex = copySubgraph(origEdge.getAlpha(), graph);
			newEdge = graph.createEdge(origEdge.getClass(), subVertex,
					newVertex);
			mergeSourcePositions((Greql2Aggregation) origEdge,
					(Greql2Aggregation) newEdge);
			origEdge = origEdge.getNextEdge(EdgeDirection.IN);
		}
		return newVertex;
	}
}
