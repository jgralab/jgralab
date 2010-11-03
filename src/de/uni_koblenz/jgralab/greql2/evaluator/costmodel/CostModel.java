/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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

package de.uni_koblenz.jgralab.greql2.evaluator.costmodel;

import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.AggregationPathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.AlternativePathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.BackwardVertexSetEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.BagConstructionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.ComprehensionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.ConditionalExpressionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.DeclarationEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.EdgePathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.EdgeRestrictionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.EdgeSetExpressionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.EdgeSubgraphExpressionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.ExponentiatedPathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.ForwardVertexSetEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.FunctionApplicationEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.Greql2ExpressionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.IntermediateVertexPathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.IteratedPathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.ListConstructionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.ListRangeConstructionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.MapComprehensionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.MapConstructionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.OptionalPathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.PathExistenceEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.QuantifiedExpressionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.RecordConstructionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.RecordElementEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.SequentialPathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.SetComprehensionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.SetConstructionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.SimpleDeclarationEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.SimplePathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.TableComprehensionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.TransposedPathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.TupleConstructionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.TypeIdEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VariableEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexSetExpressionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexSubgraphExpressionEvaluator;

/**
 * This interface is implemented by all costmodels.
 * 
 * The returntype of the several methods is a VertexCosts, its a 3-Tuple
 * containing
 * 
 * <ul>
 * <li>the costs of evaluating this vertex itself once (ownEvaluationCosts),</li>
 * <li>the costs all evaluations of this vertex that are needed when evaluating
 * the current query and</li> *
 * <li>the costs of evaluating the whole subtree below this vertex plus
 * ownEvaluationCosts (subtreeEvaluationCosts).</li>
 * </ul>
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public interface CostModel {

	/**
	 * @param costModel
	 *            another CostModel
	 * @return <code>true</code>, if this costmodel and the given one are
	 *         logical equivalent, that means, if the costfunction is identical
	 */
	public boolean isEquivalent(CostModel costModel);

	/**
	 * Set the given {@link GreqlEvaluator} for this CostModel.
	 * 
	 * @param eval
	 *            the {@link GreqlEvaluator}
	 */
	public void setGreqlEvaluator(GreqlEvaluator eval);

	public VertexCosts calculateCostsAlternativePathDescription(
			AlternativePathDescriptionEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsBackwardVertexSet(
			BackwardVertexSetEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsBagComprehension(ComprehensionEvaluator e,
			GraphSize graphSize);

	public VertexCosts calculateCostsBagConstruction(
			BagConstructionEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsConditionalExpression(
			ConditionalExpressionEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsDeclaration(DeclarationEvaluator e,
			GraphSize graphSize);

	public VertexCosts calculateCostsEdgePathDescription(
			EdgePathDescriptionEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsEdgeRestriction(
			EdgeRestrictionEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsEdgeSetExpression(
			EdgeSetExpressionEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsEdgeSubgraphExpression(
			EdgeSubgraphExpressionEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsExponentiatedPathDescription(
			ExponentiatedPathDescriptionEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsForwardVertexSet(
			ForwardVertexSetEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsFunctionApplication(
			FunctionApplicationEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsGreql2Expression(
			Greql2ExpressionEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsIntermediateVertexPathDescription(
			IntermediateVertexPathDescriptionEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsIteratedPathDescription(
			IteratedPathDescriptionEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsListConstruction(
			ListConstructionEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsListRangeConstruction(
			ListRangeConstructionEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsOptionalPathDescription(
			OptionalPathDescriptionEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsPathExistence(PathExistenceEvaluator e,
			GraphSize graphSize);

	public VertexCosts calculateCostsQuantifiedExpression(
			QuantifiedExpressionEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsRecordConstruction(
			RecordConstructionEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsRecordElement(RecordElementEvaluator e,
			GraphSize graphSize);

	public VertexCosts calculateCostsSequentialPathDescription(
			SequentialPathDescriptionEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsSetComprehension(
			SetComprehensionEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsSetConstruction(
			SetConstructionEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsSimpleDeclaration(
			SimpleDeclarationEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsSimplePathDescription(
			SimplePathDescriptionEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsAggregationPathDescription(
			AggregationPathDescriptionEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsTableComprehension(
			TableComprehensionEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsTupleConstruction(
			TupleConstructionEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsTypeId(TypeIdEvaluator e,
			GraphSize graphSize);

	/**
	 * Calculates the evaluation costs of a TransposedPathDescription.
	 * 
	 * @param e
	 * @param graphSize
	 * @return a tuple (subtreeCosts, vertexCosts) that describes the evaluation
	 *         costs of the given evaluator
	 */
	public VertexCosts calculateCostsTransposedPathDescription(
			TransposedPathDescriptionEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsVariable(VariableEvaluator e,
			GraphSize graphSize);

	public VertexCosts calculateCostsVertexSetExpression(
			VertexSetExpressionEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsVertexSubgraphExpression(
			VertexSubgraphExpressionEvaluator e, GraphSize graphSize);

	/*
	 * The methods to calculate the cardinality
	 */

	public long calculateCardinalityBackwardVertexSet(
			BackwardVertexSetEvaluator e, GraphSize graphSize);

	public long calculateCardinalityBagComprehension(ComprehensionEvaluator e,
			GraphSize graphSize);

	public long calculateCardinalityBagConstruction(BagConstructionEvaluator e,
			GraphSize graphSize);

	public long calculateCardinalityConditionalExpression(
			ConditionalExpressionEvaluator e, GraphSize graphSize);

	public long calculateCardinalityDeclaration(DeclarationEvaluator e,
			GraphSize graphSize);

	public long calculateCardinalityEdgeSetExpression(
			EdgeSetExpressionEvaluator e, GraphSize graphSize);

	public long calculateCardinalityEdgeSubgraphExpression(
			EdgeSubgraphExpressionEvaluator e, GraphSize graphSize);

	public long calculateCardinalityForwardVertexSet(
			ForwardVertexSetEvaluator e, GraphSize graphSize);

	public long calculateCardinalityFunctionApplication(
			FunctionApplicationEvaluator e, GraphSize graphSize);

	public long calculateCardinalityListConstruction(
			ListConstructionEvaluator e, GraphSize graphSize);

	public long calculateCardinalityListRangeConstruction(
			ListRangeConstructionEvaluator e, GraphSize graphSize);

	public long calculateCardinalityRecordConstruction(
			RecordConstructionEvaluator e, GraphSize graphSize);

	public long calculateCardinalitySetComprehension(
			SetComprehensionEvaluator e, GraphSize graphSize);

	public long calculateCardinalitySetConstruction(SetConstructionEvaluator e,
			GraphSize graphSize);

	public long calculateCardinalitySimpleDeclaration(
			SimpleDeclarationEvaluator e, GraphSize graphSize);

	public long calculateCardinalityTableComprehension(
			TableComprehensionEvaluator e, GraphSize graphSize);

	public long calculateCardinalityTupleConstruction(
			TupleConstructionEvaluator e, GraphSize graphSize);

	public long calculateCardinalityVertexSetExpression(
			VertexSetExpressionEvaluator e, GraphSize graphSize);

	public long calculateCardinalityVertexSubgraphExpression(
			VertexSubgraphExpressionEvaluator e, GraphSize graphSize);

	/*
	 * The methods to calculate the selectivity
	 */

	public double calculateSelectivityFunctionApplication(
			FunctionApplicationEvaluator e, GraphSize graphSize);

	public double calculateSelectivityPathExistence(PathExistenceEvaluator e,
			GraphSize graphSize);

	public double calculateSelectivityTypeId(TypeIdEvaluator e,
			GraphSize graphSize);

	/*
	 * The methods to calculate the size of the expected subgraph
	 */

	public GraphSize calculateVertexSubgraphSize(
			VertexSubgraphExpressionEvaluator e, GraphSize graphSize);

	public GraphSize calculateEdgeSubgraphSize(
			EdgeSubgraphExpressionEvaluator e, GraphSize graphSize);

	public long calculateVariableAssignments(VariableEvaluator e,
			GraphSize graphSize);

	public VertexCosts calculateCostsMapConstruction(
			MapConstructionEvaluator mapConstructionEvaluator,
			GraphSize graphSize);

	public long calculateCardinalityMapConstruction(
			MapConstructionEvaluator mapConstructionEvaluator,
			GraphSize graphSize);

	public VertexCosts calculateCostsMapComprehension(
			MapComprehensionEvaluator mapComprehensionEvaluator,
			GraphSize graphSize);

	public long calculateCardinalityMapComprehension(
			MapComprehensionEvaluator mapComprehensionEvaluator,
			GraphSize graphSize);

}
