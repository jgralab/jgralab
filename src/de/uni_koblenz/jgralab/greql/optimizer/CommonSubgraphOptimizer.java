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
/**
 *
 */
package de.uni_koblenz.jgralab.greql.optimizer;

import java.util.HashMap;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.OptimizerInfo;
import de.uni_koblenz.jgralab.greql.exception.OptimizerException;
import de.uni_koblenz.jgralab.greql.schema.GreqlAggregation;
import de.uni_koblenz.jgralab.greql.schema.GreqlGraph;
import de.uni_koblenz.jgralab.greql.schema.GreqlVertex;
import de.uni_koblenz.jgralab.greql.schema.PathDescription;
import de.uni_koblenz.jgralab.greql.schema.Variable;
import de.uni_koblenz.jgralab.schema.Attribute;

/**
 * This {@link Optimizer} finds all subgraps in a {@link GreqlGraph} that are
 * equal. Two subgraphs are considered equal, if and only if
 * 
 * <ul>
 * <li>their root vertices have the same type,</li>
 * <li>the same {@link Attribute}s and {@link Attribute} values in the same
 * order,</li>
 * <li>the incoming {@link Edge}s have the same types and the same order and</li>
 * <li>the GreqlVertices (see {@link GreqlVertex}) that are the sources of those
 * {@link Edge}s are equal in the same respect.</li>
 * </ul>
 * 
 * When such equal subgraphs are found they are merged. This merging works in
 * three steps.
 * 
 * <nl>
 * <li>The sourcePositions {@link Attribute}s of the {@link GreqlAggregation}
 * edges that run into the root-GreqlVertices are merged recursively.</li>
 * <li>The source vertices of the {@link Edge}s that start in the root-
 * {@link GreqlVertex} with the higher Id are set to the root-
 * {@link GreqlVertex} with the lower Id.</li>
 * <li>The root-{@link GreqlVertex} with the higher Id is deleted (and thus is
 * the subgaph below it).</li>
 * </nl>
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class CommonSubgraphOptimizer extends OptimizerBase {

	private static Logger logger = JGraLab
			.getLogger(CommonSubgraphOptimizer.class);

	private boolean anOptimizationWasDone = false;

	/**
	 * Maps hash-values to GreqlVertices (see {@link GreqlVertex}).
	 */
	private final HashMap<String, GreqlVertex> subgraphMap;

	/**
	 * Maps GreqlVertices to their hash-value. Used to omit double calculation
	 * of hash-values from vertices that have several parent nodes (yeah, these
	 * are {@link Variable} vertices).
	 */
	private final HashMap<GreqlVertex, String> reverseSubgraphMap;

	public CommonSubgraphOptimizer(OptimizerInfo optimizerInfo) {
		super(optimizerInfo);
		subgraphMap = new HashMap<>();
		reverseSubgraphMap = new HashMap<>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.optimizer.Optimizer#isEquivalent(de.uni_koblenz
	 * .jgralab.greql2.optimizer.Optimizer)
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
	 * @see
	 * de.uni_koblenz.jgralab.greql2.optimizer.Optimizer#optimize(de.uni_koblenz
	 * .jgralab.greql2.evaluator.GreqlEvaluator,
	 * de.uni_koblenz.jgralab.greql2.schema.Greql)
	 */
	@Override
	public boolean optimize(GreqlQuery query) throws OptimizerException {
		anOptimizationWasDone = false;

		computeHashAndProcess(query.getQueryGraph().getFirstGreqlExpression());

		return anOptimizationWasDone;
	}

	/**
	 * Compute the hash value of the given {@link GreqlVertex}. If another
	 * {@link GreqlVertex} with the same hash value was processed before then
	 * merge them (see
	 * {@link CommonSubgraphOptimizer#mergeVertices(GreqlVertex, GreqlVertex)}
	 * ).
	 * 
	 * @param vertex
	 *            the {@link GreqlVertex} for which to compute the hash value
	 *            and to process
	 * @return the hash value of the given vertex
	 */
	private String computeHashAndProcess(GreqlVertex vertex) {
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
			buf.append(computeHashAndProcess((GreqlVertex) e.getThat()));
		}
		buf.append("}");

		String hash = buf.toString();

		GreqlVertex lowerVertex = vertex;

		if (subgraphMap.containsKey(hash)) {
			GreqlVertex higherVertex = subgraphMap.get(hash);
			if (lowerVertex.getId() > higherVertex.getId()) {
				// swap them so that the higher vertex gets merged into the
				// lower one.
				GreqlVertex tmp = lowerVertex;
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
	 * {@link GreqlVertex}.
	 * 
	 * @param vertex
	 *            a {@link GreqlVertex}
	 * @return the attribute part of <code>vertex</code>'s hash value
	 */
	private String computeAttributeHash(GreqlVertex vertex) {
		StringBuilder buf = new StringBuilder();
		buf.append("(");
		for (Attribute attr : vertex.getAttributedElementClass()
				.getAttributeList()) {
			buf.append(attr.getName());
			buf.append("=");

			Object attrValue = vertex.getAttribute(attr.getName());
			if (attrValue != null) {
				buf.append(attrValue);
			}

			buf.append(";");
		}
		buf.append(")");
		return buf.toString();
	}

	/**
	 * Merge the given two vertices (see {@link GreqlVertex}).
	 * 
	 * The second {@link GreqlVertex} will be merged into first
	 * {@link GreqlVertex}. This is done in three steps:
	 * 
	 * <nl>
	 * <li>The sourcePosition {@link Attribute}s of the {@link GreqlAggregation}
	 * s in the subgraph below the second {@link GreqlVertex} are merged into
	 * the corresponding {@link GreqlAggregation}s in the subgraph of the first
	 * {@link GreqlVertex}.</li>
	 * <li>The source of {@link Edge}s that start in the second
	 * {@link GreqlVertex} is set to the first {@link GreqlVertex}.</li>
	 * <li>Then the second {@link GreqlVertex} is deleted.</li>
	 * </nl>
	 * 
	 * Note that vertices of type {@link PathDescription} are not merged.
	 * 
	 * @param lowerVertex
	 *            a {@link GreqlVertex}
	 * @param higherVertex
	 *            another {@link GreqlVertex}
	 */
	private void mergeVertices(GreqlVertex lowerVertex, GreqlVertex higherVertex) {
		if (!(lowerVertex instanceof PathDescription)) {
			anOptimizationWasDone = true;

			logger.finer(optimizerHeaderString() + "Merging " + lowerVertex
					+ " and " + higherVertex + ".");

			// Merge the sourcePositions of the incoming edges
			mergeSourcePositionsBelow(lowerVertex, higherVertex);
			// Now set the alphas of the outgoing edges
			while (higherVertex.getFirstIncidence(EdgeDirection.OUT) != null) {
				higherVertex.getFirstIncidence(EdgeDirection.OUT).setAlpha(
						lowerVertex);
			}
			higherVertex.delete();
		}
	}

	/**
	 * The sourcePosition {@link Attribute}s of the {@link GreqlAggregation}s in
	 * the subgraph below the {@link GreqlVertex} <code>higherVertex</code> are
	 * merged into the corresponding {@link GreqlAggregation}s in the subgraph
	 * of <code>lowerVertex</code>.
	 * 
	 * @param lowerVertex
	 *            a {@link GreqlVertex}
	 * @param higherVertex
	 *            another {@link GreqlVertex}
	 */
	private void mergeSourcePositionsBelow(GreqlVertex lowerVertex,
			GreqlVertex higherVertex) {
		GreqlAggregation gal = lowerVertex
				.getFirstGreqlAggregationIncidence(EdgeDirection.IN);
		GreqlAggregation gah = higherVertex
				.getFirstGreqlAggregationIncidence(EdgeDirection.IN);
		while ((gal != null) && (gah != null)) {
			OptimizerUtility.mergeSourcePositions(gah, gal);
			mergeSourcePositionsBelow(gal.getAlpha(), gah.getAlpha());
			gal = gal.getNextGreqlAggregationIncidence(EdgeDirection.IN);
			gah = gah.getNextGreqlAggregationIncidence(EdgeDirection.IN);
		}
	}
}
