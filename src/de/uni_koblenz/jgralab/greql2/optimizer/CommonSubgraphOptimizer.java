/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.optimizer;

import java.util.HashMap;

import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.OptimizerException;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Aggregation;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.PathDescription;
import de.uni_koblenz.jgralab.greql2.schema.Variable;

/**
 * This {@link Optimizer} finds all subgraps in a {@link Greql2} syntaxgraph
 * that are equal. Two subgraphs are considered equal, if and only if
 * 
 * <ul>
 * <li>their root vertices have the same type,</li>
 * <li>the same {@link Attribute}s and {@link Attribute} values in the same
 * order,</li>
 * <li>the incoming {@link Edge}s have the same types and the same order and</li>
 * <li>the Greql2Vertices (see {@link Greql2Vertex}) that are the sources of
 * those {@link Edge}s are equal in the same respect.</li>
 * </ul>
 * 
 * When such equal subgraphs are found they are merged. This merging works in
 * three steps.
 * 
 * <nl>
 * <li>The sourcePositions {@link Attribute}s of the {@link Greql2Aggregation}
 * edges that run into the root-Greql2Vertices are merged recursively.</li>
 * <li>The source vertices of the {@link Edge}s that start in the root-{@link Greql2Vertex}
 * with the higher Id are set to the root-{@link Greql2Vertex} with the lower
 * Id.</li>
 * <li>The root-{@link Greql2Vertex} with the higher Id is deleted (and thus
 * is the subgaph below it).</li>
 * </nl>
 * 
 * @author Tassilo Horn (heimdall), 2007, Diploma Thesis
 * 
 */
public class CommonSubgraphOptimizer extends OptimizerBase {

	private boolean anOptimizationWasDone = false;

	/**
	 * Maps hash-values to Greql2Vertices (see {@link Greql2Vertex}).
	 */
	private HashMap<String, Greql2Vertex> subgraphMap;

	/**
	 * Maps Greql2Vertices to their hash-value. Used to omit double calculation
	 * of hash-values from vertices that have several parent nodes (yeah, these
	 * are {@link Variable} vertices).
	 */
	private HashMap<Greql2Vertex, String> reverseSubgraphMap;

	public CommonSubgraphOptimizer() {
		subgraphMap = new HashMap<String, Greql2Vertex>();
		reverseSubgraphMap = new HashMap<Greql2Vertex, String>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.optimizer.Optimizer#isEquivalent(de.uni_koblenz.jgralab.greql2.optimizer.Optimizer)
	 */
	@Override
	public boolean isEquivalent(Optimizer optimizer) {
		if (optimizer instanceof CommonSubgraphOptimizer) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.optimizer.Optimizer#optimize(de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator,
	 *      de.uni_koblenz.jgralab.greql2.schema.Greql2)
	 */
	@Override
	public boolean optimize(GreqlEvaluator eval, Greql2 syntaxgraph)
			throws OptimizerException {
		anOptimizationWasDone = false;

		computeHashAndProcess(syntaxgraph.getFirstGreql2Expression());

		return anOptimizationWasDone;
	}

	/**
	 * Compute the hash value of the given {@link Greql2Vertex}. If another
	 * {@link Greql2Vertex} with the same hash value was processed before then
	 * merge them (see
	 * {@link CommonSubgraphOptimizer#mergeVertices(Greql2Vertex, Greql2Vertex)}).
	 * 
	 * @param vertex
	 *            the {@link Greql2Vertex} for which to compute the hash value
	 *            and to process
	 * @return the hash value of the given vertex
	 */
	private String computeHashAndProcess(Greql2Vertex vertex) {
		if (reverseSubgraphMap.containsKey(vertex)) {
			return "{V" + vertex.getId() + "}";
		}

		if (vertex instanceof Variable) {
			// Variables are merged by the parser before. If there are more
			// variables with equal names left, they may not be merged because
			// they have different scopes.
			return "{V" + vertex.getId() + "}";
		}

		StringBuilder buf = new StringBuilder();
		buf.append("{V");
		buf.append(":");
		buf.append(vertex.getAttributedElementClass().getQualifiedName());
		buf.append(computeAttributeHash(vertex));

		// Compute the hashes of the children
		for (Edge e : vertex.incidences(EdgeDirection.IN)) {
			buf.append("{E:");
			buf.append(e.getAttributedElementClass().getQualifiedName());
			buf.append("}");
			buf.append(computeHashAndProcess((Greql2Vertex) e.getThat()));
		}
		buf.append("}");

		String hash = buf.toString();

		Greql2Vertex lowerVertex = vertex;

		if (subgraphMap.containsKey(hash)) {
			Greql2Vertex higherVertex = subgraphMap.get(hash);
			if (lowerVertex.getId() > higherVertex.getId()) {
				// swap them so that the higher vertex gets merged into the
				// lower one.
				Greql2Vertex tmp = lowerVertex;
				lowerVertex = higherVertex;
				higherVertex = tmp;
				// higherVertex will die, so remove it from the map.
				reverseSubgraphMap.remove(higherVertex);
			}
			mergeVertices(lowerVertex, higherVertex);
		}

		// ensure the surviving vertex is in the hashmaps
		subgraphMap.put(hash, lowerVertex);
		reverseSubgraphMap.put(lowerVertex, hash);

		return "{V" + lowerVertex.getId() + "}";
	}

	/**
	 * Compute the attribute part of the hash value of the given
	 * {@link Greql2Vertex}.
	 * 
	 * @param vertex
	 *            a {@link Greql2Vertex}
	 * @return the attribute part of <code>vertex</code>'s hash value
	 */
	private String computeAttributeHash(Greql2Vertex vertex) {
		StringBuilder buf = new StringBuilder();
		buf.append("(");
		for (Attribute attr : vertex.getAttributedElementClass()
				.getAttributeList()) {
			buf.append(attr.getName());
			buf.append("=");
			try {
				Object attrValue = vertex.getAttribute(attr.getName());
				if (attrValue != null) {
					buf.append(attrValue);
				}
			} catch (NoSuchFieldException e1) {
				e1.printStackTrace();
			}
			buf.append(";");
		}
		buf.append(")");
		return buf.toString();
	}

	/**
	 * Merge the given two vertices (see {@link Greql2Vertex}).
	 * 
	 * The second {@link Greql2Vertex} will be merged into first
	 * {@link Greql2Vertex}. This is done in three steps:
	 * 
	 * <nl>
	 * <li>The sourcePosition {@link Attribute}s of the
	 * {@link Greql2Aggregation}s in the subgraph below the second
	 * {@link Greql2Vertex} are merged into the corresponding
	 * {@link Greql2Aggregation}s in the subgraph of the first
	 * {@link Greql2Vertex}.</li>
	 * <li>The source of {@link Edge}s that start in the second
	 * {@link Greql2Vertex} is set to the first {@link Greql2Vertex}.</li>
	 * <li>Then the second {@link Greql2Vertex} is deleted.</li>
	 * </nl>
	 * 
	 * Note that vertices of type {@link PathDescription} are not merged.
	 * 
	 * @param lowerVertex
	 *            a {@link Greql2Vertex}
	 * @param higherVertex
	 *            another {@link Greql2Vertex}
	 */
	private void mergeVertices(Greql2Vertex lowerVertex,
			Greql2Vertex higherVertex) {
		if (!(lowerVertex instanceof PathDescription)) {
			anOptimizationWasDone = true;

			if (printMessages) {
				System.out.println(optimizerHeaderString() + "Merging "
						+ lowerVertex + " and " + higherVertex + ".");
			}

			// Merge the sourcePositions of the incoming edges
			mergeSourcePositionsBelow(lowerVertex, higherVertex);
			// Now set the alphas of the outgoing edges
			while (higherVertex.getFirstEdge(EdgeDirection.OUT) != null) {
				higherVertex.getFirstEdge(EdgeDirection.OUT).setAlpha(
						lowerVertex);
			}
			higherVertex.delete();
		}
	}

	/**
	 * The sourcePosition {@link Attribute}s of the {@link Greql2Aggregation}s
	 * in the subgraph below the {@link Greql2Vertex} <code>higherVertex</code>
	 * are merged into the corresponding {@link Greql2Aggregation}s in the
	 * subgraph of <code>lowerVertex</code>.
	 * 
	 * @param lowerVertex
	 *            a {@link Greql2Vertex}
	 * @param higherVertex
	 *            another {@link Greql2Vertex}
	 */
	private void mergeSourcePositionsBelow(Greql2Vertex lowerVertex,
			Greql2Vertex higherVertex) {
		Greql2Aggregation gal = lowerVertex
				.getFirstGreql2Aggregation(EdgeDirection.IN);
		Greql2Aggregation gah = higherVertex
				.getFirstGreql2Aggregation(EdgeDirection.IN);
		while (gal != null && gah != null) {
			OptimizerUtility.mergeSourcePositions(gah, gal);
			mergeSourcePositionsBelow((Greql2Vertex) gal.getAlpha(),
					(Greql2Vertex) gah.getAlpha());
			gal = gal.getNextGreql2Aggregation(EdgeDirection.IN);
			gah = gah.getNextGreql2Aggregation(EdgeDirection.IN);
		}
	}
}
