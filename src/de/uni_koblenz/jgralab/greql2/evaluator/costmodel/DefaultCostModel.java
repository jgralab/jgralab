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

import java.util.ArrayList;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.AlternativePathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.BackwardVertexSetEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.BagComprehensionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.BagConstructionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.ConditionalExpressionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.DeclarationEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.DefinitionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.EdgePathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.EdgeRestrictionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.EdgeSetExpressionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.EdgeSubgraphExpressionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.ExponentiatedPathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.ForwardVertexSetEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.FunctionApplicationEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.Greql2ExpressionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.IntLiteralEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.IntermediateVertexPathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.IteratedPathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.LetExpressionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.ListConstructionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.ListRangeConstructionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.MapConstructionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.OptionalPathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.PathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.PathExistenceEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.QuantifiedExpressionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.RecordConstructionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.RecordElementEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.RestrictedExpressionEvaluator;
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
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexSetExpressionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexSubgraphExpressionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.WhereExpressionEvaluator;
import de.uni_koblenz.jgralab.greql2.funlib.Greql2Function;
import de.uni_koblenz.jgralab.greql2.schema.AlternativePathDescription;
import de.uni_koblenz.jgralab.greql2.schema.BackwardVertexSet;
import de.uni_koblenz.jgralab.greql2.schema.BagComprehension;
import de.uni_koblenz.jgralab.greql2.schema.BagConstruction;
import de.uni_koblenz.jgralab.greql2.schema.ConditionalExpression;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.Definition;
import de.uni_koblenz.jgralab.greql2.schema.EdgePathDescription;
import de.uni_koblenz.jgralab.greql2.schema.EdgeRestriction;
import de.uni_koblenz.jgralab.greql2.schema.EdgeSetExpression;
import de.uni_koblenz.jgralab.greql2.schema.EdgeSubgraphExpression;
import de.uni_koblenz.jgralab.greql2.schema.ExponentiatedPathDescription;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.ForwardVertexSet;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Expression;
import de.uni_koblenz.jgralab.greql2.schema.IntermediateVertexPathDescription;
import de.uni_koblenz.jgralab.greql2.schema.IsAlternativePathOf;
import de.uni_koblenz.jgralab.greql2.schema.IsArgumentOf;
import de.uni_koblenz.jgralab.greql2.schema.IsBoundVarOf;
import de.uni_koblenz.jgralab.greql2.schema.IsConstraintOf;
import de.uni_koblenz.jgralab.greql2.schema.IsDeclaredVarOf;
import de.uni_koblenz.jgralab.greql2.schema.IsDefinitionOf;
import de.uni_koblenz.jgralab.greql2.schema.IsFalseExprOf;
import de.uni_koblenz.jgralab.greql2.schema.IsKeyValueTupleOf;
import de.uni_koblenz.jgralab.greql2.schema.IsNullExprOf;
import de.uni_koblenz.jgralab.greql2.schema.IsPartOf;
import de.uni_koblenz.jgralab.greql2.schema.IsRecordElementOf;
import de.uni_koblenz.jgralab.greql2.schema.IsRecordExprOf;
import de.uni_koblenz.jgralab.greql2.schema.IsSequenceElementOf;
import de.uni_koblenz.jgralab.greql2.schema.IsSimpleDeclOf;
import de.uni_koblenz.jgralab.greql2.schema.IsSubPathOf;
import de.uni_koblenz.jgralab.greql2.schema.IsTrueExprOf;
import de.uni_koblenz.jgralab.greql2.schema.IsTypeRestrOf;
import de.uni_koblenz.jgralab.greql2.schema.IteratedPathDescription;
import de.uni_koblenz.jgralab.greql2.schema.LetExpression;
import de.uni_koblenz.jgralab.greql2.schema.ListConstruction;
import de.uni_koblenz.jgralab.greql2.schema.ListRangeConstruction;
import de.uni_koblenz.jgralab.greql2.schema.MapConstruction;
import de.uni_koblenz.jgralab.greql2.schema.OptionalPathDescription;
import de.uni_koblenz.jgralab.greql2.schema.PathDescription;
import de.uni_koblenz.jgralab.greql2.schema.PathExistence;
import de.uni_koblenz.jgralab.greql2.schema.QuantifiedExpression;
import de.uni_koblenz.jgralab.greql2.schema.RecordConstruction;
import de.uni_koblenz.jgralab.greql2.schema.RecordElement;
import de.uni_koblenz.jgralab.greql2.schema.RestrictedExpression;
import de.uni_koblenz.jgralab.greql2.schema.SequentialPathDescription;
import de.uni_koblenz.jgralab.greql2.schema.SetComprehension;
import de.uni_koblenz.jgralab.greql2.schema.SetConstruction;
import de.uni_koblenz.jgralab.greql2.schema.SimpleDeclaration;
import de.uni_koblenz.jgralab.greql2.schema.TableComprehension;
import de.uni_koblenz.jgralab.greql2.schema.TransposedPathDescription;
import de.uni_koblenz.jgralab.greql2.schema.TupleConstruction;
import de.uni_koblenz.jgralab.greql2.schema.TypeId;
import de.uni_koblenz.jgralab.greql2.schema.Variable;
import de.uni_koblenz.jgralab.greql2.schema.VertexSetExpression;
import de.uni_koblenz.jgralab.greql2.schema.VertexSubgraphExpression;
import de.uni_koblenz.jgralab.greql2.schema.WhereExpression;

/**
 * This is the default costmodel the evaluator uses if no other costmodel is
 * set.
 *
 * @author ist@uni-koblenz.de
 *
 */
public class DefaultCostModel extends CostModelBase implements CostModel {

	private static Logger logger = Logger.getLogger(DefaultCostModel.class
			.getName());

	/**
	 * Nullary constructor needed for reflective instantiation. Creates a
	 * non-functional CostModel.
	 */
	public DefaultCostModel() {
	}

	/**
	 * Creates a new CostModel for a graph whose {@link GreqlEvaluator} is given
	 * here.
	 *
	 * @param eval
	 *            a {@link GreqlEvaluator}
	 */
	public DefaultCostModel(GreqlEvaluator eval) {
		greqlEvaluator = eval;
	}

	@Override
	public long calculateCardinalityBackwardVertexSet(
			BackwardVertexSetEvaluator e, GraphSize graphSize) {
		// TODO Auto-generated method stub
		return 5;
	}

	@Override
	public long calculateCardinalityBagComprehension(
			BagComprehensionEvaluator e, GraphSize graphSize) {
		BagComprehension bagComp = (BagComprehension) e.getVertex();
		Declaration decl = (Declaration) bagComp.getFirstIsCompDeclOf()
				.getAlpha();
		DeclarationEvaluator declEval = (DeclarationEvaluator) greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(decl);
		return declEval.getEstimatedCardinality(graphSize);
	}

	@Override
	public long calculateCardinalityBagConstruction(BagConstructionEvaluator e,
			GraphSize graphSize) {
		BagConstruction bagCons = (BagConstruction) e.getVertex();
		IsPartOf inc = bagCons.getFirstIsPartOf();
		long parts = 0;
		while (inc != null) {
			parts++;
			inc = inc.getNextIsPartOf();
		}
		return parts;
	}

	@Override
	public long calculateCardinalityConditionalExpression(
			ConditionalExpressionEvaluator e, GraphSize graphSize) {
		ConditionalExpression condExp = (ConditionalExpression) e.getVertex();
		IsTrueExprOf trueInc = condExp.getFirstIsTrueExprOf();
		long trueCard = 0;
		if (trueInc != null) {
			VertexEvaluator trueEval = greqlEvaluator
					.getVertexEvaluatorGraphMarker()
					.getMark(trueInc.getAlpha());
			trueCard = trueEval.getEstimatedCardinality(graphSize);
		}
		IsFalseExprOf falseInc = condExp.getFirstIsFalseExprOf();
		long falseCard = 0;
		if (falseInc != null) {
			VertexEvaluator falseEval = greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(
							falseInc.getAlpha());
			falseCard = falseEval.getEstimatedCardinality(graphSize);
		}
		IsNullExprOf nullInc = condExp.getFirstIsNullExprOf();
		long nullCard = 0;
		if (falseInc != null) {
			VertexEvaluator nullEval = greqlEvaluator
					.getVertexEvaluatorGraphMarker()
					.getMark(nullInc.getAlpha());
			nullCard = nullEval.getEstimatedCardinality(graphSize);
		}
		long maxCard = trueCard;
		if (falseCard > maxCard) {
			maxCard = falseCard;
		}
		if (nullCard > maxCard) {
			maxCard = nullCard;
		}
		return maxCard;
	}

	@Override
	public long calculateCardinalityDeclaration(DeclarationEvaluator e,
			GraphSize graphSize) {
		Declaration decl = (Declaration) e.getVertex();
		IsConstraintOf inc = decl.getFirstIsConstraintOf(EdgeDirection.IN);
		double selectivity = 1.0;
		while (inc != null) {
			VertexEvaluator constEval = greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
			selectivity *= constEval.getEstimatedSelectivity(graphSize);
			inc = inc.getNextIsConstraintOf(EdgeDirection.IN);
		}
		return Math.round(e.getDefinedVariableCombinations(graphSize)
				* selectivity);
	}

	@Override
	public long calculateCardinalityEdgeSetExpression(
			EdgeSetExpressionEvaluator e, GraphSize graphSize) {
		EdgeSetExpression exp = (EdgeSetExpression) e.getVertex();
		IsTypeRestrOf inc = exp.getFirstIsTypeRestrOf();
		double selectivity = 1.0;
		if (inc != null) {
			TypeIdEvaluator typeIdEval = (TypeIdEvaluator) greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
			selectivity = typeIdEval.getEstimatedSelectivity(graphSize);
		}
		return Math.round(graphSize.getEdgeCount() * selectivity);
	}

	@Override
	public long calculateCardinalityEdgeSubgraphExpression(
			EdgeSubgraphExpressionEvaluator e, GraphSize graphSize) {
		EdgeSubgraphExpression exp = (EdgeSubgraphExpression) e.getVertex();
		IsTypeRestrOf inc = exp.getFirstIsTypeRestrOf();
		double selectivity = 1.0;
		if (inc != null) {
			TypeIdEvaluator typeIdEval = (TypeIdEvaluator) greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
			selectivity = typeIdEval.getEstimatedSelectivity(graphSize);
		}
		return Math.round((graphSize.getEdgeCount() + graphSize
				.getVertexCount())
				* selectivity);
	}

	@Override
	public long calculateCardinalityForwardVertexSet(
			ForwardVertexSetEvaluator e, GraphSize graphSize) {
		// TODO Auto-generated method stub
		return 5;
	}

	@Override
	public long calculateCardinalityFunctionApplication(
			FunctionApplicationEvaluator e, GraphSize graphSize) {
		FunctionApplication funApp = (FunctionApplication) e.getVertex();
		IsArgumentOf inc = funApp.getFirstIsArgumentOf(EdgeDirection.IN);
		int elements = 0;
		while (inc != null) {
			VertexEvaluator argEval = greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
			elements += argEval.getEstimatedCardinality(graphSize);
			inc = inc.getNextIsArgumentOf(EdgeDirection.IN);
		}
		Greql2Function func = e.getGreql2Function();
		if (func != null) {
			return func.getEstimatedCardinality(elements);
		} else {
			return 1;
		}
	}

	@Override
	public long calculateCardinalityListConstruction(
			ListConstructionEvaluator e, GraphSize graphSize) {
		ListConstruction listCons = (ListConstruction) e.getVertex();
		IsPartOf inc = listCons.getFirstIsPartOf();
		long parts = 0;
		while (inc != null) {
			parts++;
			inc = inc.getNextIsPartOf();
		}
		return parts;
	}

	@Override
	public long calculateCardinalityListRangeConstruction(
			ListRangeConstructionEvaluator e, GraphSize graphSize) {
		ListRangeConstruction exp = (ListRangeConstruction) e.getVertex();
		VertexEvaluator startExpEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker()
				.getMark(
						exp.getFirstIsFirstValueOf(EdgeDirection.IN).getAlpha());
		VertexEvaluator targetExpEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(
						exp.getFirstIsLastValueOf(EdgeDirection.IN).getAlpha());
		long range = 0;
		if (startExpEval instanceof IntLiteralEvaluator) {
			if (targetExpEval instanceof IntLiteralEvaluator) {
				try {
					range = targetExpEval.getResult(null).toInteger()
							- startExpEval.getResult(null).toInteger() + 1;
				} catch (Exception ex) {
					// if an exception occurs, the default value is used, so no
					// exceptionhandling is needed
				}
			}
		}
		if (range > 0) {
			return range;
		} else {
			return defaultListRangeSize;
		}
	}

	@Override
	public long calculateCardinalityRecordConstruction(
			RecordConstructionEvaluator e, GraphSize graphSize) {
		RecordConstruction recCons = (RecordConstruction) e.getVertex();
		IsRecordElementOf inc = recCons
				.getFirstIsRecordElementOf(EdgeDirection.IN);
		long parts = 0;
		while (inc != null) {
			parts++;
			inc = inc.getNextIsRecordElementOf(EdgeDirection.IN);
		}
		return parts;
	}

	@Override
	public long calculateCardinalitySetComprehension(
			SetComprehensionEvaluator e, GraphSize graphSize) {
		SetComprehension setComp = (SetComprehension) e.getVertex();
		Declaration decl = (Declaration) setComp.getFirstIsCompDeclOf()
				.getAlpha();
		DeclarationEvaluator declEval = (DeclarationEvaluator) greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(decl);
		return declEval.getEstimatedCardinality(graphSize);
	}

	@Override
	public long calculateCardinalitySetConstruction(SetConstructionEvaluator e,
			GraphSize graphSize) {
		SetConstruction setCons = (SetConstruction) e.getVertex();
		IsPartOf inc = setCons.getFirstIsPartOf();
		long parts = 0;
		while (inc != null) {
			parts++;
			inc = inc.getNextIsPartOf();
		}
		return parts;
	}

	@Override
	public long calculateCardinalitySimpleDeclaration(
			SimpleDeclarationEvaluator e, GraphSize graphSize) {
		SimpleDeclaration decl = (SimpleDeclaration) e.getVertex();
		VertexEvaluator typeExprEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(
						decl.getFirstIsTypeExprOf(EdgeDirection.IN).getAlpha());
		long singleCardinality = typeExprEval
				.getEstimatedCardinality(graphSize);
		long wholeCardinality = singleCardinality
				* e.getDefinedVariables().size();
		return wholeCardinality;
	}

	@Override
	public long calculateCardinalityTableComprehension(
			TableComprehensionEvaluator e, GraphSize graphSize) {
		TableComprehension tableComp = (TableComprehension) e.getVertex();
		Declaration decl = (Declaration) tableComp.getFirstIsCompDeclOf()
				.getAlpha();
		DeclarationEvaluator declEval = (DeclarationEvaluator) greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(decl);
		return declEval.getEstimatedCardinality(graphSize);
	}

	@Override
	public long calculateCardinalityTupleConstruction(
			TupleConstructionEvaluator e, GraphSize graphSize) {
		TupleConstruction tupleCons = (TupleConstruction) e.getVertex();
		IsPartOf inc = tupleCons.getFirstIsPartOf(EdgeDirection.IN);
		long parts = 0;
		while (inc != null) {
			parts++;
			inc = inc.getNextIsPartOf(EdgeDirection.IN);
		}
		return parts;
	}

	@Override
	public long calculateCardinalityVertexSetExpression(
			VertexSetExpressionEvaluator e, GraphSize graphSize) {
		VertexSetExpression exp = (VertexSetExpression) e.getVertex();
		IsTypeRestrOf inc = exp.getFirstIsTypeRestrOf();
		double selectivity = 1.0;
		if (inc != null) {
			TypeIdEvaluator typeIdEval = (TypeIdEvaluator) greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
			selectivity = typeIdEval.getEstimatedSelectivity(graphSize);
		}
		return Math.round(graphSize.getVertexCount() * selectivity);
	}

	@Override
	public long calculateCardinalityVertexSubgraphExpression(
			VertexSubgraphExpressionEvaluator e, GraphSize graphSize) {
		VertexSubgraphExpression exp = (VertexSubgraphExpression) e.getVertex();
		IsTypeRestrOf inc = exp.getFirstIsTypeRestrOf();
		double selectivity = 1.0;
		if (inc != null) {
			TypeIdEvaluator typeIdEval = (TypeIdEvaluator) greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
			selectivity = typeIdEval.getEstimatedSelectivity(graphSize);
		}
		return Math.round((graphSize.getEdgeCount() + graphSize
				.getVertexCount())
				* selectivity);
	}

	@Override
	public VertexCosts calculateCostsAlternativePathDescription(
			AlternativePathDescriptionEvaluator e, GraphSize graphSize) {
		AlternativePathDescription p = (AlternativePathDescription) e
				.getVertex();
		long aggregatedCosts = 0;
		IsAlternativePathOf inc = p.getFirstIsAlternativePathOf();
		long alternatives = 0;
		while (inc != null) {
			PathDescriptionEvaluator pathEval = (PathDescriptionEvaluator) greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
			aggregatedCosts += pathEval
					.getCurrentSubtreeEvaluationCosts(graphSize);
			inc = inc.getNextIsAlternativePathOf();
			alternatives++;
		}
		aggregatedCosts += 10 * alternatives;
		return new VertexCosts(10 * alternatives, 10 * alternatives,
				aggregatedCosts);
	}

	@Override
	public VertexCosts calculateCostsBackwardVertexSet(
			BackwardVertexSetEvaluator e, GraphSize graphSize) {
		BackwardVertexSet bwvertex = (BackwardVertexSet) e.getVertex();
		Expression targetExpression = (Expression) bwvertex
				.getFirstIsTargetExprOf().getAlpha();
		VertexEvaluator vertexEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(targetExpression);
		long targetCosts = vertexEval
				.getCurrentSubtreeEvaluationCosts(graphSize);
		PathDescription p = (PathDescription) bwvertex.getFirstIsPathOf()
				.getAlpha();
		PathDescriptionEvaluator pathDescEval = (PathDescriptionEvaluator) greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(p);
		long pathDescCosts = pathDescEval
				.getCurrentSubtreeEvaluationCosts(graphSize);
		long searchCosts = Math.round(pathDescCosts * searchFactor
				* Math.sqrt(graphSize.getEdgeCount()));
		long ownCosts = searchCosts;
		long iteratedCosts = ownCosts * e.getVariableCombinations(graphSize);
		long subtreeCosts = targetCosts + pathDescCosts + iteratedCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsBagComprehension
	 * (de.uni_koblenz.jgralab.greql2.evaluator.vertexeval
	 * .BagComprehensionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsBagComprehension(
			BagComprehensionEvaluator e, GraphSize graphSize) {
		BagComprehension bagComp = (BagComprehension) e.getVertex();
		Declaration decl = (Declaration) bagComp.getFirstIsCompDeclOf()
				.getAlpha();
		DeclarationEvaluator declEval = (DeclarationEvaluator) greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(decl);
		long declCosts = declEval.getCurrentSubtreeEvaluationCosts(graphSize);

		Vertex resultDef = bagComp.getFirstIsCompResultDefOf().getAlpha();
		VertexEvaluator resultDefEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(resultDef);
		long resultCosts = resultDefEval
				.getCurrentSubtreeEvaluationCosts(graphSize);

		long ownCosts = declEval.getEstimatedCardinality(graphSize)
				* addToBagCosts;
		long iteratedCosts = ownCosts * e.getVariableCombinations(graphSize);
		long subtreeCosts = iteratedCosts + resultCosts + declCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsBagConstruction
	 * (de.uni_koblenz.jgralab.greql2.evaluator.vertexeval
	 * .BagConstructionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsBagConstruction(
			BagConstructionEvaluator e, GraphSize graphSize) {
		BagConstruction bagCons = (BagConstruction) e.getVertex();
		IsPartOf inc = bagCons.getFirstIsPartOf(EdgeDirection.IN);

		long parts = 0;
		long partCosts = 0;
		while (inc != null) {
			parts++;
			VertexEvaluator partEval = greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
			partCosts += partEval.getCurrentSubtreeEvaluationCosts(graphSize);
			inc = inc.getNextIsPartOf(EdgeDirection.IN);
		}

		long ownCosts = parts * addToBagCosts + 2;
		long iteratedCosts = ownCosts * e.getVariableCombinations(graphSize);
		long subtreeCosts = iteratedCosts + partCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public VertexCosts calculateCostsConditionalExpression(
			ConditionalExpressionEvaluator e, GraphSize graphSize) {
		ConditionalExpression vertex = (ConditionalExpression) e.getVertex();
		Expression condition = (Expression) vertex.getFirstIsConditionOf()
				.getAlpha();
		VertexEvaluator conditionEvaluator = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(condition);
		long conditionCosts = conditionEvaluator
				.getCurrentSubtreeEvaluationCosts(graphSize);
		Expression expressionToEvaluate;
		expressionToEvaluate = (Expression) vertex.getFirstIsTrueExprOf()
				.getAlpha();
		VertexEvaluator vertexEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(expressionToEvaluate);
		long trueCosts = vertexEval.getCurrentSubtreeEvaluationCosts(graphSize);
		expressionToEvaluate = (Expression) vertex.getFirstIsFalseExprOf()
				.getAlpha();
		vertexEval = greqlEvaluator.getVertexEvaluatorGraphMarker().getMark(
				expressionToEvaluate);
		long falseCosts = vertexEval
				.getCurrentSubtreeEvaluationCosts(graphSize);
		expressionToEvaluate = (Expression) vertex.getFirstIsNullExprOf()
				.getAlpha();
		vertexEval = greqlEvaluator.getVertexEvaluatorGraphMarker().getMark(
				expressionToEvaluate);
		long nullCosts = vertexEval.getCurrentSubtreeEvaluationCosts(graphSize);
		long maxCosts = trueCosts;
		if (falseCosts > trueCosts) {
			maxCosts = falseCosts;
		}
		if (nullCosts > maxCosts) {
			maxCosts = nullCosts;
		}
		long ownCosts = 4;
		long iteratedCosts = ownCosts * e.getVariableCombinations(graphSize);
		long subtreeCosts = iteratedCosts + maxCosts + conditionCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsDeclaration
	 * (de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.DeclarationEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsDeclaration(DeclarationEvaluator e,
			GraphSize graphSize) {
		Declaration decl = (Declaration) e.getVertex();

		IsSimpleDeclOf inc = decl.getFirstIsSimpleDeclOf();
		long simpleDeclCosts = 0;
		while (inc != null) {
			SimpleDeclaration simpleDecl = (SimpleDeclaration) inc.getAlpha();
			SimpleDeclarationEvaluator simpleEval = (SimpleDeclarationEvaluator) greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(simpleDecl);
			simpleDeclCosts += simpleEval
					.getCurrentSubtreeEvaluationCosts(graphSize);
			inc = inc.getNextIsSimpleDeclOf();
		}

		IsConstraintOf consInc = decl.getFirstIsConstraintOf();
		int constraintsCosts = 0;
		while (consInc != null) {
			VertexEvaluator constraint = greqlEvaluator
					.getVertexEvaluatorGraphMarker()
					.getMark(consInc.getAlpha());
			constraintsCosts += constraint
					.getCurrentSubtreeEvaluationCosts(graphSize);
			consInc = consInc.getNextIsConstraintOf();
		}

		long iterationCosts = e.getDefinedVariableCombinations(graphSize)
				* declarationCostsFactor;
		long ownCosts = iterationCosts + 2;
		long iteratedCosts = ownCosts * e.getVariableCombinations(graphSize);
		long subtreeCosts = iteratedCosts + constraintsCosts + simpleDeclCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsDefinition
	 * (de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.DefinitionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsDefinition(DefinitionEvaluator e,
			GraphSize graphSize) {
		Definition def = (Definition) e.getVertex();
		VertexEvaluator expEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(
						def.getFirstIsExprOf().getAlpha());
		// + 1 for the Variable
		long subtreeCosts = expEval.getCurrentSubtreeEvaluationCosts(graphSize) + 1;
		long ownCosts = 2;
		return new VertexCosts(ownCosts, ownCosts, ownCosts + subtreeCosts);
	}

	@Override
	public VertexCosts calculateCostsEdgePathDescription(
			EdgePathDescriptionEvaluator e, GraphSize graphSize) {
		EdgePathDescription edgePathDesc = (EdgePathDescription) e.getVertex();
		VertexEvaluator edgeEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(
						edgePathDesc.getFirstIsEdgeExprOf().getAlpha());
		long edgeCosts = edgeEval.getCurrentSubtreeEvaluationCosts(graphSize);
		return new VertexCosts(transitionCosts, transitionCosts,
				transitionCosts + edgeCosts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsEdgeRestriction
	 * (de.uni_koblenz.jgralab.greql2.evaluator.vertexeval
	 * .EdgeRestrictionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsEdgeRestriction(
			EdgeRestrictionEvaluator e, GraphSize graphSize) {
		EdgeRestriction er = (EdgeRestriction) e.getVertex();

		long subtreeCosts = 0;
		if (er.getFirstIsTypeIdOf(EdgeDirection.IN) != null) {
			TypeIdEvaluator tEval = (TypeIdEvaluator) greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(
							er.getFirstIsTypeIdOf(EdgeDirection.IN).getAlpha());
			subtreeCosts += tEval.getCurrentSubtreeEvaluationCosts(graphSize);
		}
		if (er.getFirstIsRoleIdOf(EdgeDirection.IN) != null) {
			subtreeCosts += 1;
		}
		return new VertexCosts(transitionCosts, transitionCosts, subtreeCosts
				+ transitionCosts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsEdgeSetExpression
	 * (de.uni_koblenz.jgralab.greql2.evaluator.vertexeval
	 * .EdgeSetExpressionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsEdgeSetExpression(
			EdgeSetExpressionEvaluator e, GraphSize graphSize) {
		EdgeSetExpression ese = (EdgeSetExpression) e.getVertex();

		long typeRestrCosts = 0;
		IsTypeRestrOf inc = ese.getFirstIsTypeRestrOf();
		while (inc != null) {
			TypeIdEvaluator tideval = (TypeIdEvaluator) greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
			typeRestrCosts += tideval
					.getCurrentSubtreeEvaluationCosts(graphSize);
			inc = inc.getNextIsTypeRestrOf();
		}

		long ownCosts = graphSize.getEdgeCount() * edgeSetExpressionCostsFactor;
		return new VertexCosts(ownCosts, ownCosts, typeRestrCosts + ownCosts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsEdgeSubgraphExpression
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.EdgeSubgraphExpressionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsEdgeSubgraphExpression(
			EdgeSubgraphExpressionEvaluator e, GraphSize graphSize) {
		EdgeSubgraphExpression ese = (EdgeSubgraphExpression) e.getVertex();

		long typeRestrCosts = 0;
		IsTypeRestrOf inc = ese.getFirstIsTypeRestrOf();
		while (inc != null) {
			TypeIdEvaluator tideval = (TypeIdEvaluator) greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
			typeRestrCosts += tideval
					.getCurrentSubtreeEvaluationCosts(graphSize);
			inc = inc.getNextIsTypeRestrOf();
		}

		long ownCosts = graphSize.getEdgeCount()
				* edgeSubgraphExpressionCostsFactor;
		return new VertexCosts(ownCosts, ownCosts, typeRestrCosts + ownCosts);
	}

	@Override
	public VertexCosts calculateCostsExponentiatedPathDescription(
			ExponentiatedPathDescriptionEvaluator e, GraphSize graphSize) {
		ExponentiatedPathDescription p = (ExponentiatedPathDescription) e
				.getVertex();
		long exponent = defaultExponent;
		VertexEvaluator expEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(
						p.getFirstIsExponentOf().getAlpha());
		if (expEval instanceof IntLiteralEvaluator) {
			try {
				exponent = expEval.getResult(null).toLong();
			} catch (Exception ex) {
			}
		}
		long exponentCosts = expEval
				.getCurrentSubtreeEvaluationCosts(graphSize);
		VertexEvaluator pathEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(
						p.getFirstIsExponentiatedPathOf().getAlpha());
		long pathCosts = pathEval.getCurrentSubtreeEvaluationCosts(graphSize);
		long ownCosts = (pathCosts * exponent) * 1 / 3;
		long subtreeCosts = pathCosts + ownCosts + exponentCosts;
		return new VertexCosts(ownCosts, ownCosts, subtreeCosts);
	}

	@Override
	public VertexCosts calculateCostsForwardVertexSet(
			ForwardVertexSetEvaluator e, GraphSize graphSize) {
		ForwardVertexSet bwvertex = (ForwardVertexSet) e.getVertex();
		Expression targetExpression = (Expression) bwvertex
				.getFirstIsStartExprOf().getAlpha();
		VertexEvaluator vertexEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(targetExpression);
		long targetCosts = vertexEval
				.getCurrentSubtreeEvaluationCosts(graphSize);
		PathDescription p = (PathDescription) bwvertex.getFirstIsPathOf()
				.getAlpha();
		PathDescriptionEvaluator pathDescEval = (PathDescriptionEvaluator) greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(p);
		long pathDescCosts = pathDescEval
				.getCurrentSubtreeEvaluationCosts(graphSize);
		long searchCosts = Math.round(pathDescCosts * searchFactor
				* Math.sqrt(graphSize.getEdgeCount()));
		long ownCosts = searchCosts;
		long iteratedCosts = ownCosts * e.getVariableCombinations(graphSize);
		long subtreeCosts = targetCosts + pathDescCosts + iteratedCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsFunctionApplication
	 * (de.uni_koblenz.jgralab.greql2.evaluator.
	 * vertexeval.FunctionApplicationEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsFunctionApplication(
			FunctionApplicationEvaluator e, GraphSize graphSize) {
		FunctionApplication funApp = (FunctionApplication) e.getVertex();

		IsArgumentOf inc = funApp.getFirstIsArgumentOf(EdgeDirection.IN);
		long argCosts = 0;
		ArrayList<Long> elements = new ArrayList<Long>();
		while (inc != null) {
			VertexEvaluator argEval = greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
			argCosts += argEval.getCurrentSubtreeEvaluationCosts(graphSize);
			elements.add(argEval.getEstimatedCardinality(graphSize));
			inc = inc.getNextIsArgumentOf(EdgeDirection.IN);
		}

		Greql2Function func = e.getGreql2Function();
		long ownCosts = func.getEstimatedCosts(elements);
		long iteratedCosts = ownCosts * e.getVariableCombinations(graphSize);
		long subtreeCosts = iteratedCosts + argCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsGreql2Expression
	 * (de.uni_koblenz.jgralab.greql2.evaluator.vertexeval
	 * .Greql2ExpressionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsGreql2Expression(
			Greql2ExpressionEvaluator e, GraphSize graphSize) {
		Greql2Expression greqlExp = (Greql2Expression) e.getVertex();
		VertexEvaluator queryExpEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(
						greqlExp.getFirstIsQueryExprOf().getAlpha());
		long queryCosts = queryExpEval
				.getCurrentSubtreeEvaluationCosts(graphSize);
		logger.info("QueryCosts: " + queryCosts);
		IsBoundVarOf boundVarInc = greqlExp.getFirstIsBoundVarOf();
		int boundVars = 0;
		while (boundVarInc != null) {
			boundVars++;
			boundVarInc = boundVarInc.getNextIsBoundVarOf();
		}
		long ownCosts = boundVars * greql2ExpressionCostsFactor;
		long iteratedCosts = ownCosts;
		long subtreeCosts = ownCosts + queryCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public VertexCosts calculateCostsIntermediateVertexPathDescription(
			IntermediateVertexPathDescriptionEvaluator e, GraphSize graphSize) {
		IntermediateVertexPathDescription pathDesc = (IntermediateVertexPathDescription) e
				.getVertex();
		IsSubPathOf inc = pathDesc.getFirstIsSubPathOf();
		PathDescriptionEvaluator firstPathEval = (PathDescriptionEvaluator) greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
		inc = inc.getNextIsSubPathOf();
		PathDescriptionEvaluator secondPathEval = (PathDescriptionEvaluator) greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
		long firstCosts = firstPathEval
				.getCurrentSubtreeEvaluationCosts(graphSize);
		long secondCosts = secondPathEval
				.getCurrentSubtreeEvaluationCosts(graphSize);
		VertexEvaluator vertexEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(
						pathDesc.getFirstIsIntermediateVertexOf().getAlpha());
		long intermVertexCosts = vertexEval
				.getCurrentSubtreeEvaluationCosts(graphSize);
		long ownCosts = 10;
		long iteratedCosts = 10;
		long subtreeCosts = iteratedCosts + intermVertexCosts + firstCosts
				+ secondCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public VertexCosts calculateCostsIteratedPathDescription(
			IteratedPathDescriptionEvaluator e, GraphSize graphSize) {
		IteratedPathDescription iterPath = (IteratedPathDescription) e
				.getVertex();
		VertexEvaluator pathEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(
						iterPath.getFirstIsIteratedPathOf().getAlpha());
		long ownCosts = 5;
		long iteratedCosts = 5;
		long subtreeCosts = ownCosts
				+ pathEval.getCurrentSubtreeEvaluationCosts(graphSize);
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsLetExpression
	 * (de.uni_koblenz.jgralab.greql2.evaluator.vertexeval
	 * .LetExpressionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsLetExpression(LetExpressionEvaluator e,
			GraphSize graphSize) {
		LetExpression letExp = (LetExpression) e.getVertex();

		IsDefinitionOf inc = letExp.getFirstIsDefinitionOf();
		long definitionCosts = 0;
		long definitions = 0;
		while (inc != null) {
			definitions++;
			DefinitionEvaluator definEval = (DefinitionEvaluator) greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
			definitionCosts += definEval
					.getCurrentSubtreeEvaluationCosts(graphSize);
			inc = inc.getNextIsDefinitionOf();
		}

		VertexEvaluator boundExpressionEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(
						letExp.getFirstIsBoundExprOfDefinition().getAlpha());
		long expressionCosts = boundExpressionEval
				.getCurrentSubtreeEvaluationCosts(graphSize);

		long ownCosts = definitions * definitionExpressionCostsFactor + 2;
		long iteratedCosts = ownCosts * e.getVariableCombinations(graphSize);
		long subtreeCosts = iteratedCosts + definitionCosts + expressionCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsListConstruction
	 * (de.uni_koblenz.jgralab.greql2.evaluator.vertexeval
	 * .ListConstructionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsListConstruction(
			ListConstructionEvaluator e, GraphSize graphSize) {
		ListConstruction listCons = (ListConstruction) e.getVertex();
		IsPartOf inc = listCons.getFirstIsPartOf();
		long parts = 0;
		long partCosts = 0;
		while (inc != null) {
			VertexEvaluator veval = greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
			partCosts += veval.getCurrentSubtreeEvaluationCosts(graphSize);
			parts++;
			inc = inc.getNextIsPartOf();
		}

		long ownCosts = parts * addToListCosts + 2;
		long iteratedCosts = ownCosts * e.getVariableCombinations(graphSize);
		long subtreeCosts = iteratedCosts + partCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public VertexCosts calculateCostsListRangeConstruction(
			ListRangeConstructionEvaluator e, GraphSize graphSize) {
		ListRangeConstruction exp = (ListRangeConstruction) e.getVertex();
		VertexEvaluator startExpEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(
						exp.getFirstIsFirstValueOf().getAlpha());
		VertexEvaluator targetExpEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(
						exp.getFirstIsLastValueOf().getAlpha());
		long startCosts = startExpEval
				.getCurrentSubtreeEvaluationCosts(graphSize);
		long targetCosts = targetExpEval
				.getCurrentSubtreeEvaluationCosts(graphSize);
		long range = 0;
		if (startExpEval instanceof IntLiteralEvaluator) {
			if (targetExpEval instanceof IntLiteralEvaluator) {
				try {
					range = targetExpEval.getResult(null).toInteger()
							- startExpEval.getResult(null).toInteger() + 1;
				} catch (Exception ex) {
					// if an exception occurs, the default value is used, so no
					// exceptionhandling is needed
				}
			}
		}
		if (range <= 0) {
			range = defaultListRangeSize;
		}
		long ownCosts = addToListCosts * range;
		long iteratedCosts = ownCosts * e.getVariableCombinations(graphSize);
		long subtreeCosts = iteratedCosts + startCosts + targetCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public VertexCosts calculateCostsOptionalPathDescription(
			OptionalPathDescriptionEvaluator e, GraphSize graphSize) {
		OptionalPathDescription iterPath = (OptionalPathDescription) e
				.getVertex();
		VertexEvaluator pathEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(
						iterPath.getFirstIsOptionalPathOf().getAlpha());
		long ownCosts = 5;
		long iteratedCosts = 5;
		long subtreeCosts = ownCosts
				+ pathEval.getCurrentSubtreeEvaluationCosts(graphSize);
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public VertexCosts calculateCostsPathExistence(PathExistenceEvaluator e,
			GraphSize graphSize) {
		PathExistence existence = (PathExistence) e.getVertex();
		Expression startExpression = (Expression) existence
				.getFirstIsStartExprOf().getAlpha();
		VertexEvaluator vertexEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(startExpression);
		long startCosts = vertexEval
				.getCurrentSubtreeEvaluationCosts(graphSize);
		Expression targetExpression = (Expression) existence
				.getFirstIsTargetExprOf().getAlpha();
		vertexEval = greqlEvaluator.getVertexEvaluatorGraphMarker().getMark(
				targetExpression);
		long targetCosts = vertexEval
				.getCurrentSubtreeEvaluationCosts(graphSize);
		PathDescription p = (PathDescription) existence.getFirstIsPathOf()
				.getAlpha();
		PathDescriptionEvaluator pathDescEval = (PathDescriptionEvaluator) greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(p);
		long pathDescCosts = pathDescEval
				.getCurrentSubtreeEvaluationCosts(graphSize);
		long searchCosts = Math.round(pathDescCosts * searchFactor / 2
				* Math.sqrt(graphSize.getEdgeCount()));
		long ownCosts = searchCosts;
		long iteratedCosts = ownCosts * e.getVariableCombinations(graphSize);
		long subtreeCosts = targetCosts + pathDescCosts + iteratedCosts
				+ startCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsQuantifiedExpression
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.QuantifiedExpressionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsQuantifiedExpression(
			QuantifiedExpressionEvaluator e, GraphSize graphSize) {
		QuantifiedExpression quantifiedExpr = (QuantifiedExpression) e
				.getVertex();

		VertexEvaluator declEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(
						quantifiedExpr.getFirstIsQuantifiedDeclOf().getAlpha());
		long declCosts = declEval.getCurrentSubtreeEvaluationCosts(graphSize);

		VertexEvaluator boundExprEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(
						quantifiedExpr.getFirstIsBoundExprOfQuantifier()
								.getAlpha());
		long boundExprCosts = boundExprEval
				.getCurrentSubtreeEvaluationCosts(graphSize);

		long ownCosts = 20;
		long iteratedCosts = ownCosts * e.getVariableCombinations(graphSize);
		long subtreeCosts = iteratedCosts + declCosts + boundExprCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsRecordConstruction
	 * (de.uni_koblenz.jgralab.greql2.evaluator.vertexeval
	 * .RecordConstructionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsRecordConstruction(
			RecordConstructionEvaluator e, GraphSize graphSize) {
		RecordConstruction recCons = (RecordConstruction) e.getVertex();
		IsPartOf inc = recCons.getFirstIsPartOf(EdgeDirection.IN);
		long recElems = 0;
		long recElemCosts = 0;
		while (inc != null) {
			RecordElement recElem = (RecordElement) inc.getAlpha();
			VertexEvaluator veval = greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(recElem);
			recElemCosts += veval.getCurrentSubtreeEvaluationCosts(graphSize);
			recElems++;
			inc = inc.getNextIsPartOf(EdgeDirection.IN);
		}

		long ownCosts = recElems * addToRecordCosts + 2;
		long iteratedCosts = ownCosts * e.getVariableCombinations(graphSize);
		long subtreeCosts = iteratedCosts + recElemCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsRecordElement
	 * (de.uni_koblenz.jgralab.greql2.evaluator.vertexeval
	 * .RecordElementEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsRecordElement(RecordElementEvaluator e,
			GraphSize graphSize) {
		RecordElement recElem = (RecordElement) e.getVertex();

		IsRecordExprOf inc = recElem.getFirstIsRecordExprOf();
		VertexEvaluator veval = greqlEvaluator.getVertexEvaluatorGraphMarker()
				.getMark(inc.getAlpha());
		long recordExprCosts = veval
				.getCurrentSubtreeEvaluationCosts(graphSize);

		long ownCosts = 3;
		long iteratedCosts = ownCosts * e.getVariableCombinations(graphSize);
		long subtreeCosts = recordExprCosts + iteratedCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsRestrictedExpression
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.RestrictedExpressionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsRestrictedExpression(
			RestrictedExpressionEvaluator e, GraphSize graphSize) {
		RestrictedExpression resExp = (RestrictedExpression) e.getVertex();

		VertexEvaluator restrictedExprEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(
						resExp.getFirstIsRestrictedExprOf().getAlpha());
		long restrictedExprCosts = restrictedExprEval
				.getCurrentSubtreeEvaluationCosts(graphSize);

		VertexEvaluator restrictionEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(
						resExp.getFirstIsRestrictionOf().getAlpha());
		long restrictionCosts = restrictionEval
				.getCurrentSubtreeEvaluationCosts(graphSize);

		long ownCosts = 5;
		long iteratedCosts = ownCosts * e.getVariableCombinations(graphSize);
		long subtreeCosts = iteratedCosts + restrictedExprCosts
				+ restrictionCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public VertexCosts calculateCostsSequentialPathDescription(
			SequentialPathDescriptionEvaluator e, GraphSize graphSize) {
		SequentialPathDescription p = (SequentialPathDescription) e.getVertex();
		long aggregatedCosts = 0;
		IsSequenceElementOf inc = p.getFirstIsSequenceElementOf();
		long alternatives = 0;
		while (inc != null) {
			PathDescriptionEvaluator pathEval = (PathDescriptionEvaluator) greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
			aggregatedCosts += pathEval
					.getCurrentSubtreeEvaluationCosts(graphSize);
			inc = inc.getNextIsSequenceElementOf();
			alternatives++;
		}
		aggregatedCosts += 10 * alternatives;
		return new VertexCosts(10 * alternatives, 10 * alternatives,
				aggregatedCosts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsSetComprehension
	 * (de.uni_koblenz.jgralab.greql2.evaluator.vertexeval
	 * .SetComprehensionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsSetComprehension(
			SetComprehensionEvaluator e, GraphSize graphSize) {
		SetComprehension setComp = (SetComprehension) e.getVertex();
		Declaration decl = (Declaration) setComp.getFirstIsCompDeclOf()
				.getAlpha();
		DeclarationEvaluator declEval = (DeclarationEvaluator) greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(decl);
		long declCosts = declEval.getCurrentSubtreeEvaluationCosts(graphSize);

		Vertex resultDef = setComp.getFirstIsCompResultDefOf().getAlpha();
		VertexEvaluator resultDefEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(resultDef);
		long resultCosts = resultDefEval
				.getCurrentSubtreeEvaluationCosts(graphSize);

		long ownCosts = resultDefEval.getEstimatedCardinality(graphSize)
				* addToSetCosts;
		long iteratedCosts = ownCosts * e.getVariableCombinations(graphSize);
		long subtreeCosts = iteratedCosts + resultCosts + declCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsSetConstruction
	 * (de.uni_koblenz.jgralab.greql2.evaluator.vertexeval
	 * .SetConstructionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsSetConstruction(
			SetConstructionEvaluator e, GraphSize graphSize) {
		SetConstruction setCons = (SetConstruction) e.getVertex();
		IsPartOf inc = setCons.getFirstIsPartOf();
		long parts = 0;
		long partCosts = 0;
		while (inc != null) {
			VertexEvaluator veval = greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
			partCosts += veval.getCurrentSubtreeEvaluationCosts(graphSize);
			parts++;
			inc = inc.getNextIsPartOf();
		}

		long ownCosts = parts * addToSetCosts + 2;
		long iteratedCosts = ownCosts * e.getVariableCombinations(graphSize);
		long subtreeCosts = iteratedCosts + partCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsSimpleDeclaration
	 * (de.uni_koblenz.jgralab.greql2.evaluator.vertexeval
	 * .SimpleDeclarationEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsSimpleDeclaration(
			SimpleDeclarationEvaluator e, GraphSize graphSize) {
		SimpleDeclaration simpleDecl = (SimpleDeclaration) e.getVertex();

		// Calculate the costs for the type definition
		VertexEvaluator typeExprEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(
						simpleDecl.getFirstIsTypeExprOf().getAlpha());

		long typeCosts = typeExprEval
				.getCurrentSubtreeEvaluationCosts(graphSize);

		// Calculate the costs for the declared variables
		long declaredVarCosts = 0;
		IsDeclaredVarOf inc = simpleDecl
				.getFirstIsDeclaredVarOf(EdgeDirection.IN);
		while (inc != null) {
			VariableEvaluator varEval = (VariableEvaluator) greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
			declaredVarCosts += varEval
					.getCurrentSubtreeEvaluationCosts(graphSize);
			inc = inc.getNextIsDeclaredVarOf(EdgeDirection.IN);
		}

		long ownCosts = 2;
		long iteratedCosts = ownCosts * e.getVariableCombinations(graphSize);
		long subtreeCosts = iteratedCosts + declaredVarCosts + typeCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public VertexCosts calculateCostsSimplePathDescription(
			SimplePathDescriptionEvaluator e, GraphSize graphSize) {
		return new VertexCosts(transitionCosts, transitionCosts,
				transitionCosts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsTableComprehension
	 * (de.uni_koblenz.jgralab.greql2.evaluator.vertexeval
	 * .TableComprehensionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsTableComprehension(
			TableComprehensionEvaluator e, GraphSize graphSize) {
		// TODO (heimdall): What is a TableComprehension? Syntax? Where do the
		// costs differ from a BagComprehension?
		TableComprehension tableComp = (TableComprehension) e.getVertex();

		Declaration decl = (Declaration) tableComp.getFirstIsCompDeclOf()
				.getAlpha();
		DeclarationEvaluator declEval = (DeclarationEvaluator) greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(decl);
		long declCosts = declEval.getCurrentSubtreeEvaluationCosts(graphSize);

		Vertex resultDef = tableComp.getFirstIsCompResultDefOf().getAlpha();
		VertexEvaluator resultDefEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(resultDef);
		long resultCosts = resultDefEval
				.getCurrentSubtreeEvaluationCosts(graphSize);

		long ownCosts = resultDefEval.getEstimatedCardinality(graphSize)
				* addToBagCosts;
		long iteratedCosts = ownCosts * e.getVariableCombinations(graphSize);
		long subtreeCosts = iteratedCosts + resultCosts + declCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public VertexCosts calculateCostsTransposedPathDescription(
			TransposedPathDescriptionEvaluator e, GraphSize graphSize) {
		TransposedPathDescription transPath = (TransposedPathDescription) e
				.getVertex();
		PathDescriptionEvaluator pathEval = (PathDescriptionEvaluator) greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(
						transPath.getFirstIsTransposedPathOf().getAlpha());
		long pathCosts = pathEval.getCurrentSubtreeEvaluationCosts(graphSize);
		long transpositionCosts = pathCosts / 20;
		long subtreeCosts = transpositionCosts + pathCosts;
		return new VertexCosts(transpositionCosts, transpositionCosts,
				subtreeCosts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsTupleConstruction
	 * (de.uni_koblenz.jgralab.greql2.evaluator.vertexeval
	 * .TupleConstructionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsTupleConstruction(
			TupleConstructionEvaluator e, GraphSize graphSize) {
		TupleConstruction tupCons = (TupleConstruction) e.getVertex();
		IsPartOf inc = tupCons.getFirstIsPartOf(EdgeDirection.IN);
		long parts = 0;
		long partCosts = 0;
		while (inc != null) {
			VertexEvaluator veval = greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
			partCosts += veval.getCurrentSubtreeEvaluationCosts(graphSize);
			parts++;
			inc = inc.getNextIsPartOf(EdgeDirection.IN);
		}

		long ownCosts = parts * addToTupleCosts + 2;
		long iteratedCosts = ownCosts * e.getVariableCombinations(graphSize);
		long subtreeCosts = iteratedCosts + partCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsTypeId
	 * (de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.TypeIdEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsTypeId(TypeIdEvaluator e,
			GraphSize graphSize) {
		long costs = graphSize.getKnownEdgeTypes()
				+ graphSize.getKnownVertexTypes();
		return new VertexCosts(costs, costs, costs);
	}

	@Override
	public VertexCosts calculateCostsVariable(VariableEvaluator e,
			GraphSize graphSize) {
		return new VertexCosts(1, 1, 1);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsVertexSetExpression
	 * (de.uni_koblenz.jgralab.greql2.evaluator.
	 * vertexeval.VertexSetExpressionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsVertexSetExpression(
			VertexSetExpressionEvaluator e, GraphSize graphSize) {
		VertexSetExpression vse = (VertexSetExpression) e.getVertex();

		long typeRestrCosts = 0;
		IsTypeRestrOf inc = vse.getFirstIsTypeRestrOf();
		while (inc != null) {
			TypeIdEvaluator tideval = (TypeIdEvaluator) greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
			typeRestrCosts += tideval
					.getCurrentSubtreeEvaluationCosts(graphSize);
			inc = inc.getNextIsTypeRestrOf();
		}

		long ownCosts = graphSize.getVertexCount()
				* vertexSetExpressionCostsFactor;
		return new VertexCosts(ownCosts, ownCosts, typeRestrCosts + ownCosts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsVertexSubgraphExpression
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.VertexSubgraphExpressionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsVertexSubgraphExpression(
			VertexSubgraphExpressionEvaluator e, GraphSize graphSize) {
		VertexSubgraphExpression vse = (VertexSubgraphExpression) e.getVertex();

		long typeRestrCosts = 0;
		IsTypeRestrOf inc = vse.getFirstIsTypeRestrOf();
		while (inc != null) {
			TypeIdEvaluator tideval = (TypeIdEvaluator) greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
			typeRestrCosts += tideval
					.getCurrentSubtreeEvaluationCosts(graphSize);
			inc = inc.getNextIsTypeRestrOf();
		}

		long ownCosts = graphSize.getVertexCount()
				* vertexSubgraphExpressionCostsFactor;
		return new VertexCosts(ownCosts, ownCosts, typeRestrCosts + ownCosts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsWhereExpression
	 * (de.uni_koblenz.jgralab.greql2.evaluator.vertexeval
	 * .WhereExpressionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsWhereExpression(
			WhereExpressionEvaluator e, GraphSize graphSize) {
		WhereExpression whereExp = (WhereExpression) e.getVertex();

		IsDefinitionOf inc = whereExp.getFirstIsDefinitionOf();
		long definitionCosts = 0;
		long definitions = 0;
		while (inc != null) {
			definitions++;
			DefinitionEvaluator definEval = (DefinitionEvaluator) greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
			definitionCosts += definEval
					.getCurrentSubtreeEvaluationCosts(graphSize);
			inc = inc.getNextIsDefinitionOf();
		}

		VertexEvaluator boundExpressionEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(
						whereExp.getFirstIsBoundExprOfDefinition().getAlpha());
		long expressionCosts = boundExpressionEval
				.getCurrentSubtreeEvaluationCosts(graphSize);

		long ownCosts = definitions * definitionExpressionCostsFactor + 2;
		long iteratedCosts = ownCosts * e.getVariableCombinations(graphSize);
		long subtreeCosts = iteratedCosts + definitionCosts + expressionCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateEdgeSubgraphSize
	 * (de.uni_koblenz.jgralab.greql2.evaluator.vertexeval
	 * .EdgeSubgraphExpressionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public GraphSize calculateEdgeSubgraphSize(
			EdgeSubgraphExpressionEvaluator e, GraphSize graphSize) {
		EdgeSubgraphExpression ese = (EdgeSubgraphExpression) e.getVertex();
		IsTypeRestrOf inc = ese.getFirstIsTypeRestrOf(EdgeDirection.IN);
		double selectivity = 1.0;
		while (inc != null) {
			TypeId tid = (TypeId) inc.getAlpha();
			TypeIdEvaluator tidEval = (TypeIdEvaluator) greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(tid);
			selectivity *= tidEval.getEstimatedSelectivity(graphSize);
		}
		return new GraphSize(Math.round(graphSize.getVertexCount()
				* selectivity), Math.round(graphSize.getEdgeCount()
				* selectivity), graphSize.getKnownVertexTypes(), graphSize
				.getKnownVertexTypes());
	}

	@Override
	public double calculateSelectivityFunctionApplication(
			FunctionApplicationEvaluator e, GraphSize graphSize) {
		Greql2Function func = e.getGreql2Function();
		if (func != null) {
			return func.getSelectivity();
		} else {
			return 1;
		}
	}

	@Override
	public double calculateSelectivityPathExistence(PathExistenceEvaluator e,
			GraphSize graphSize) {
		return 0.1;
	}

	@Override
	public double calculateSelectivityTypeId(TypeIdEvaluator e,
			GraphSize graphSize) {
		int typesInSchema = (int) Math
				.round((graphSize.getKnownEdgeTypes() + graphSize
						.getKnownVertexTypes()) / 2.0);
		double selectivity = 1.0;
		TypeId id = (TypeId) e.getVertex();
		if (id.isType()) {
			selectivity = 1.0 / typesInSchema;
		} else {
			double avgSubclasses = (graphSize.getAverageEdgeSubclasses() + graphSize
					.getAverageVertexSubclasses()) / 2.0;
			selectivity = avgSubclasses / typesInSchema;
		}
		if (id.isExcluded()) {
			selectivity = 1 - selectivity;
		}
		return selectivity;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateVariableAssignments
	 * (de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VariableEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public long calculateVariableAssignments(VariableEvaluator e,
			GraphSize graphSize) {
		Variable v = (Variable) e.getVertex();
		IsDeclaredVarOf inc = v.getFirstIsDeclaredVarOf();
		if (inc != null) {
			SimpleDeclaration decl = (SimpleDeclaration) inc.getOmega();
			VertexEvaluator typeExpEval = greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(
							decl.getFirstIsTypeExprOf().getAlpha());
			return typeExpEval.getEstimatedCardinality(graphSize);
		} else {
			// if there exists no "isDeclaredVarOf"-Edge, the variable is not
			// declared but defined, so there exists only 1 possible assignment
			return 1;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateVertexSubgraphSize
	 * (de.uni_koblenz.jgralab.greql2.evaluator.vertexeval
	 * .VertexSubgraphExpressionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public GraphSize calculateVertexSubgraphSize(
			VertexSubgraphExpressionEvaluator e, GraphSize graphSize) {
		VertexSubgraphExpression vse = (VertexSubgraphExpression) e.getVertex();
		IsTypeRestrOf inc = vse.getFirstIsTypeRestrOf(EdgeDirection.IN);
		double selectivity = 1.0;
		while (inc != null) {
			TypeId tid = (TypeId) inc.getAlpha();
			TypeIdEvaluator tidEval = (TypeIdEvaluator) greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(tid);
			selectivity *= tidEval.getEstimatedSelectivity(graphSize);
		}
		return new GraphSize((int) Math.round(graphSize.getVertexCount()
				* selectivity), (int) Math.round(graphSize.getEdgeCount()
				* selectivity), graphSize.getKnownVertexTypes(), graphSize
				.getKnownVertexTypes());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#isEquivalent
	 * (de.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel)
	 */
	@Override
	public boolean isEquivalent(CostModel costModel) {
		if (costModel instanceof DefaultCostModel) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#setGreqlEvaluator
	 * (de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator)
	 */
	@Override
	public void setGreqlEvaluator(GreqlEvaluator eval) {
		greqlEvaluator = eval;
	}

	@Override
	public VertexCosts calculateCostsMapConstruction(
			MapConstructionEvaluator e, GraphSize graphSize) {
		MapConstruction mapCons = (MapConstruction) e.getVertex();
		IsKeyValueTupleOf inc = mapCons.getFirstIsKeyValueTupleOf();
		long parts = 0;
		long partCosts = 0;
		while (inc != null) {
			VertexEvaluator veval = greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
			partCosts += veval.getCurrentSubtreeEvaluationCosts(graphSize);
			parts++;
			inc = inc.getNextIsKeyValueTupleOf();
		}

		long ownCosts = parts * addToSetCosts + 2;
		long iteratedCosts = ownCosts * e.getVariableCombinations(graphSize);
		long subtreeCosts = iteratedCosts + partCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public long calculateCardinalityMapConstruction(MapConstructionEvaluator e,
			GraphSize graphSize) {
		long keyValuePairs = 0;
		MapConstruction mapCons = (MapConstruction) e.getVertex();
		for (@SuppressWarnings("unused")
		IsKeyValueTupleOf ikvt : mapCons.getIsKeyValueTupleOfIncidences()) {
			keyValuePairs++;
		}
		return keyValuePairs;
	}

}
