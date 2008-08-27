/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 *
 *               ist@uni-koblenz.de
 *
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package de.uni_koblenz.jgralab.greql2.evaluator.costmodel;

import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.*;

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
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
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

	public VertexCosts calculateCostsBagComprehension(
			BagComprehensionEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsBagConstruction(
			BagConstructionEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsConditionalExpression(
			ConditionalExpressionEvaluator e, GraphSize graphSize);

	public VertexCosts calculateCostsDeclaration(DeclarationEvaluator e,
			GraphSize graphSize);

	public VertexCosts calculateCostsDefinition(DefinitionEvaluator e,
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

	public VertexCosts calculateCostsLetExpression(LetExpressionEvaluator e,
			GraphSize graphSize);

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

	public VertexCosts calculateCostsRestrictedExpression(
			RestrictedExpressionEvaluator e, GraphSize graphSize);

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

	public VertexCosts calculateCostsWhereExpression(
			WhereExpressionEvaluator e, GraphSize graphSize);

	/*
	 * The methods to calculate the cardinality
	 */

	public long calculateCardinalityBackwardVertexSet(
			BackwardVertexSetEvaluator e, GraphSize graphSize);

	public long calculateCardinalityBagComprehension(
			BagComprehensionEvaluator e, GraphSize graphSize);

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

}
