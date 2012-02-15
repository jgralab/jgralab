/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2012 Institute for Software Technology
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

package de.uni_koblenz.jgralab.greql2.evaluator.costmodel;

import java.util.ArrayList;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.AggregationPathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.AlternativePathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.BackwardVertexSetEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.ComprehensionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.ConditionalExpressionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.DeclarationEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.EdgePathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.EdgeRestrictionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.EdgeSetExpressionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.ExponentiatedPathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.ForwardVertexSetEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.FunctionApplicationEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.Greql2ExpressionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.IntLiteralEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.IntermediateVertexPathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.IteratedPathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.ListConstructionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.ListRangeConstructionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.MapComprehensionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.MapConstructionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.OptionalPathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.PathDescriptionEvaluator;
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
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexSetExpressionEvaluator;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.schema.AlternativePathDescription;
import de.uni_koblenz.jgralab.greql2.schema.BackwardVertexSet;
import de.uni_koblenz.jgralab.greql2.schema.ConditionalExpression;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.EdgePathDescription;
import de.uni_koblenz.jgralab.greql2.schema.EdgeRestriction;
import de.uni_koblenz.jgralab.greql2.schema.EdgeSetExpression;
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
import de.uni_koblenz.jgralab.greql2.schema.IsFalseExprOf;
import de.uni_koblenz.jgralab.greql2.schema.IsKeyExprOfConstruction;
import de.uni_koblenz.jgralab.greql2.schema.IsPartOf;
import de.uni_koblenz.jgralab.greql2.schema.IsRecordElementOf;
import de.uni_koblenz.jgralab.greql2.schema.IsRecordExprOf;
import de.uni_koblenz.jgralab.greql2.schema.IsSequenceElementOf;
import de.uni_koblenz.jgralab.greql2.schema.IsSimpleDeclOf;
import de.uni_koblenz.jgralab.greql2.schema.IsSubPathOf;
import de.uni_koblenz.jgralab.greql2.schema.IsTrueExprOf;
import de.uni_koblenz.jgralab.greql2.schema.IsTypeRestrOfExpression;
import de.uni_koblenz.jgralab.greql2.schema.IsValueExprOfConstruction;
import de.uni_koblenz.jgralab.greql2.schema.IteratedPathDescription;
import de.uni_koblenz.jgralab.greql2.schema.ListComprehension;
import de.uni_koblenz.jgralab.greql2.schema.ListConstruction;
import de.uni_koblenz.jgralab.greql2.schema.ListRangeConstruction;
import de.uni_koblenz.jgralab.greql2.schema.MapComprehension;
import de.uni_koblenz.jgralab.greql2.schema.MapConstruction;
import de.uni_koblenz.jgralab.greql2.schema.OptionalPathDescription;
import de.uni_koblenz.jgralab.greql2.schema.PathDescription;
import de.uni_koblenz.jgralab.greql2.schema.PathExistence;
import de.uni_koblenz.jgralab.greql2.schema.QuantifiedExpression;
import de.uni_koblenz.jgralab.greql2.schema.RecordConstruction;
import de.uni_koblenz.jgralab.greql2.schema.RecordElement;
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

	@Override
	public long calculateCardinalityBackwardVertexSet(
			BackwardVertexSetEvaluator e) {
		// TODO Auto-generated method stub
		return 5;
	}

	@Override
	public long calculateCardinalityListComprehension(ComprehensionEvaluator e) {
		ListComprehension listComp = (ListComprehension) e.getVertex();
		Declaration decl = listComp.getFirstIsCompDeclOfIncidence().getAlpha();
		DeclarationEvaluator declEval = (DeclarationEvaluator) e
				.getVertexEvalMarker().getMark(decl);
		return declEval.getEstimatedCardinality();
	}

	@Override
	public long calculateCardinalityConditionalExpression(
			ConditionalExpressionEvaluator e) {
		ConditionalExpression condExp = (ConditionalExpression) e.getVertex();
		IsTrueExprOf trueInc = condExp.getFirstIsTrueExprOfIncidence();
		long trueCard = 0;
		if (trueInc != null) {
			VertexEvaluator trueEval = e.getVertexEvalMarker().getMark(
					trueInc.getAlpha());
			trueCard = trueEval.getEstimatedCardinality();
		}
		IsFalseExprOf falseInc = condExp.getFirstIsFalseExprOfIncidence();
		long falseCard = 0;
		if (falseInc != null) {
			VertexEvaluator falseEval = e.getVertexEvalMarker().getMark(
					falseInc.getAlpha());
			falseCard = falseEval.getEstimatedCardinality();
		}
		long maxCard = trueCard;
		if (falseCard > maxCard) {
			maxCard = falseCard;
		}
		return maxCard;
	}

	@Override
	public long calculateCardinalityDeclaration(DeclarationEvaluator e) {
		Declaration decl = (Declaration) e.getVertex();
		IsConstraintOf inc = decl
				.getFirstIsConstraintOfIncidence(EdgeDirection.IN);
		double selectivity = 1.0;
		while (inc != null) {
			VertexEvaluator constEval = e.getVertexEvalMarker().getMark(
					inc.getAlpha());
			selectivity *= constEval.getEstimatedSelectivity();
			inc = inc.getNextIsConstraintOfIncidence(EdgeDirection.IN);
		}
		return Math.round(e.getDefinedVariableCombinations() * selectivity);
	}

	@Override
	public long calculateCardinalityEdgeSetExpression(
			EdgeSetExpressionEvaluator e) {
		EdgeSetExpression exp = (EdgeSetExpression) e.getVertex();
		IsTypeRestrOfExpression inc = exp
				.getFirstIsTypeRestrOfExpressionIncidence();
		double selectivity = 1.0;
		if (inc != null) {
			TypeIdEvaluator typeIdEval = (TypeIdEvaluator) e
					.getVertexEvalMarker().getMark(inc.getAlpha());
			selectivity = typeIdEval.getEstimatedSelectivity();
		}
		return Math.round(graphSize.getEdgeCount() * selectivity);
	}

	@Override
	public long calculateCardinalityForwardVertexSet(ForwardVertexSetEvaluator e) {
		// TODO Auto-generated method stub
		return 5;
	}

	@Override
	public long calculateCardinalityFunctionApplication(
			FunctionApplicationEvaluator e) {
		FunctionApplication funApp = (FunctionApplication) e.getVertex();
		IsArgumentOf inc = funApp
				.getFirstIsArgumentOfIncidence(EdgeDirection.IN);
		int elements = 0;
		while (inc != null) {
			VertexEvaluator argEval = e.getVertexEvalMarker().getMark(
					inc.getAlpha());
			elements += argEval.getEstimatedCardinality();
			inc = inc.getNextIsArgumentOfIncidence(EdgeDirection.IN);
		}

		Function func = e.getFunction();
		if (func != null) {
			return func.getEstimatedCardinality(elements);
		} else {
			return 1;
		}
	}

	@Override
	public long calculateCardinalityListConstruction(ListConstructionEvaluator e) {
		ListConstruction listCons = (ListConstruction) e.getVertex();
		IsPartOf inc = listCons.getFirstIsPartOfIncidence();
		long parts = 0;
		while (inc != null) {
			parts++;
			inc = inc.getNextIsPartOfIncidence();
		}
		return parts;
	}

	@Override
	public long calculateCardinalityListRangeConstruction(
			ListRangeConstructionEvaluator e) {
		ListRangeConstruction exp = (ListRangeConstruction) e.getVertex();
		VertexEvaluator startExpEval = e.getVertexEvalMarker().getMark(
				exp.getFirstIsFirstValueOfIncidence(EdgeDirection.IN)
						.getAlpha());
		VertexEvaluator targetExpEval = e.getVertexEvalMarker()
				.getMark(
						exp.getFirstIsLastValueOfIncidence(EdgeDirection.IN)
								.getAlpha());
		long range = 0;
		if (startExpEval instanceof IntLiteralEvaluator) {
			if (targetExpEval instanceof IntLiteralEvaluator) {
				try {
					range = (((Number) targetExpEval.getResult(null))
							.longValue() - ((Number) startExpEval
							.getResult(null)).longValue()) + 1;
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
			RecordConstructionEvaluator e) {
		RecordConstruction recCons = (RecordConstruction) e.getVertex();
		IsRecordElementOf inc = recCons
				.getFirstIsRecordElementOfIncidence(EdgeDirection.IN);
		long parts = 0;
		while (inc != null) {
			parts++;
			inc = inc.getNextIsRecordElementOfIncidence(EdgeDirection.IN);
		}
		return parts;
	}

	@Override
	public long calculateCardinalitySetComprehension(SetComprehensionEvaluator e) {
		SetComprehension setComp = e.getVertex();
		Declaration decl = setComp.getFirstIsCompDeclOfIncidence().getAlpha();
		DeclarationEvaluator declEval = (DeclarationEvaluator) e
				.getVertexEvalMarker().getMark(decl);
		return declEval.getEstimatedCardinality();
	}

	@Override
	public long calculateCardinalitySetConstruction(SetConstructionEvaluator e) {
		SetConstruction setCons = (SetConstruction) e.getVertex();
		IsPartOf inc = setCons.getFirstIsPartOfIncidence();
		long parts = 0;
		while (inc != null) {
			parts++;
			inc = inc.getNextIsPartOfIncidence();
		}
		return parts;
	}

	@Override
	public long calculateCardinalitySimpleDeclaration(
			SimpleDeclarationEvaluator e) {
		SimpleDeclaration decl = (SimpleDeclaration) e.getVertex();
		VertexEvaluator typeExprEval = e.getVertexEvalMarker()
				.getMark(
						decl.getFirstIsTypeExprOfIncidence(EdgeDirection.IN)
								.getAlpha());
		long singleCardinality = typeExprEval.getEstimatedCardinality();
		long wholeCardinality = singleCardinality
				* e.getDefinedVariables().size();
		return wholeCardinality;
	}

	@Override
	public long calculateCardinalityTableComprehension(
			TableComprehensionEvaluator e) {
		TableComprehension tableComp = (TableComprehension) e.getVertex();
		Declaration decl = tableComp.getFirstIsCompDeclOfIncidence().getAlpha();
		DeclarationEvaluator declEval = (DeclarationEvaluator) e
				.getVertexEvalMarker().getMark(decl);
		return declEval.getEstimatedCardinality();
	}

	@Override
	public long calculateCardinalityTupleConstruction(
			TupleConstructionEvaluator e) {
		TupleConstruction tupleCons = (TupleConstruction) e.getVertex();
		IsPartOf inc = tupleCons.getFirstIsPartOfIncidence(EdgeDirection.IN);
		long parts = 0;
		while (inc != null) {
			parts++;
			inc = inc.getNextIsPartOfIncidence(EdgeDirection.IN);
		}
		return parts;
	}

	@Override
	public long calculateCardinalityVertexSetExpression(
			VertexSetExpressionEvaluator e) {
		VertexSetExpression exp = (VertexSetExpression) e.getVertex();
		IsTypeRestrOfExpression inc = exp
				.getFirstIsTypeRestrOfExpressionIncidence();
		double selectivity = 1.0;
		if (inc != null) {
			TypeIdEvaluator typeIdEval = (TypeIdEvaluator) e
					.getVertexEvalMarker().getMark(inc.getAlpha());
			selectivity = typeIdEval.getEstimatedSelectivity();
		}
		return Math.round(graphSize.getVertexCount() * selectivity);
	}

	@Override
	public VertexCosts calculateCostsAlternativePathDescription(
			AlternativePathDescriptionEvaluator e) {
		AlternativePathDescription p = (AlternativePathDescription) e
				.getVertex();
		long aggregatedCosts = 0;
		IsAlternativePathOf inc = p
				.getFirstIsAlternativePathOfIncidence(EdgeDirection.IN);
		long alternatives = 0;
		while (inc != null) {
			PathDescriptionEvaluator pathEval = (PathDescriptionEvaluator) e
					.getVertexEvalMarker().getMark(inc.getAlpha());
			aggregatedCosts += pathEval.getCurrentSubtreeEvaluationCosts();
			inc = inc.getNextIsAlternativePathOfIncidence(EdgeDirection.IN);
			alternatives++;
		}
		aggregatedCosts += 10 * alternatives;
		return new VertexCosts(10 * alternatives, 10 * alternatives,
				aggregatedCosts);
	}

	@Override
	public VertexCosts calculateCostsBackwardVertexSet(
			BackwardVertexSetEvaluator e) {
		BackwardVertexSet bwvertex = (BackwardVertexSet) e.getVertex();
		Expression targetExpression = bwvertex
				.getFirstIsTargetExprOfIncidence().getAlpha();
		VertexEvaluator vertexEval = e.getVertexEvalMarker().getMark(
				targetExpression);
		long targetCosts = vertexEval.getCurrentSubtreeEvaluationCosts();
		PathDescription p = (PathDescription) bwvertex
				.getFirstIsPathOfIncidence().getAlpha();
		PathDescriptionEvaluator pathDescEval = (PathDescriptionEvaluator) e
				.getVertexEvalMarker().getMark(p);
		long pathDescCosts = pathDescEval.getCurrentSubtreeEvaluationCosts();
		long searchCosts = Math.round(pathDescCosts * searchFactor
				* Math.sqrt(graphSize.getEdgeCount()));
		long ownCosts = searchCosts;
		long iteratedCosts = ownCosts * e.getVariableCombinations();
		long subtreeCosts = targetCosts + pathDescCosts + iteratedCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsListComprehension
	 * (de.uni_koblenz.jgralab.greql2.evaluator.vertexeval
	 * .ListComprehensionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsListComprehension(ComprehensionEvaluator e) {
		ListComprehension listComp = (ListComprehension) e.getVertex();
		Declaration decl = listComp.getFirstIsCompDeclOfIncidence().getAlpha();
		DeclarationEvaluator declEval = (DeclarationEvaluator) e
				.getVertexEvalMarker().getMark(decl);
		long declCosts = declEval.getCurrentSubtreeEvaluationCosts();

		Vertex resultDef = listComp.getFirstIsCompResultDefOfIncidence()
				.getAlpha();
		VertexEvaluator resultDefEval = e.getVertexEvalMarker().getMark(
				resultDef);
		long resultCosts = resultDefEval.getCurrentSubtreeEvaluationCosts();

		long ownCosts = declEval.getEstimatedCardinality() * addToListCosts;
		long iteratedCosts = ownCosts * e.getVariableCombinations();
		long subtreeCosts = iteratedCosts + resultCosts + declCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public VertexCosts calculateCostsConditionalExpression(
			ConditionalExpressionEvaluator e) {
		ConditionalExpression vertex = (ConditionalExpression) e.getVertex();
		Expression condition = vertex.getFirstIsConditionOfIncidence()
				.getAlpha();
		VertexEvaluator conditionEvaluator = e.getVertexEvalMarker().getMark(
				condition);
		long conditionCosts = conditionEvaluator
				.getCurrentSubtreeEvaluationCosts();
		Expression expressionToEvaluate;
		expressionToEvaluate = vertex.getFirstIsTrueExprOfIncidence()
				.getAlpha();
		VertexEvaluator vertexEval = e.getVertexEvalMarker().getMark(
				expressionToEvaluate);
		long trueCosts = vertexEval.getCurrentSubtreeEvaluationCosts();
		expressionToEvaluate = vertex.getFirstIsFalseExprOfIncidence()
				.getAlpha();
		vertexEval = e.getVertexEvalMarker().getMark(expressionToEvaluate);
		long falseCosts = vertexEval.getCurrentSubtreeEvaluationCosts();
		long maxCosts = trueCosts;
		if (falseCosts > trueCosts) {
			maxCosts = falseCosts;
		}
		long ownCosts = 4;
		long iteratedCosts = ownCosts * e.getVariableCombinations();
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
	public VertexCosts calculateCostsDeclaration(DeclarationEvaluator e) {
		Declaration decl = (Declaration) e.getVertex();

		IsSimpleDeclOf inc = decl.getFirstIsSimpleDeclOfIncidence();
		long simpleDeclCosts = 0;
		while (inc != null) {
			SimpleDeclaration simpleDecl = inc.getAlpha();
			SimpleDeclarationEvaluator simpleEval = (SimpleDeclarationEvaluator) e
					.getVertexEvalMarker().getMark(simpleDecl);
			simpleDeclCosts += simpleEval.getCurrentSubtreeEvaluationCosts();
			inc = inc.getNextIsSimpleDeclOfIncidence();
		}

		IsConstraintOf consInc = decl.getFirstIsConstraintOfIncidence();
		int constraintsCosts = 0;
		while (consInc != null) {
			VertexEvaluator constraint = e.getVertexEvalMarker().getMark(
					consInc.getAlpha());
			constraintsCosts += constraint.getCurrentSubtreeEvaluationCosts();
			consInc = consInc.getNextIsConstraintOfIncidence();
		}

		long iterationCosts = e.getDefinedVariableCombinations()
				* declarationCostsFactor;
		long ownCosts = iterationCosts + 2;
		long iteratedCosts = ownCosts * e.getVariableCombinations();
		long subtreeCosts = iteratedCosts + constraintsCosts + simpleDeclCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public VertexCosts calculateCostsEdgePathDescription(
			EdgePathDescriptionEvaluator e) {
		EdgePathDescription edgePathDesc = (EdgePathDescription) e.getVertex();
		VertexEvaluator edgeEval = e.getVertexEvalMarker().getMark(
				edgePathDesc.getFirstIsEdgeExprOfIncidence().getAlpha());
		long edgeCosts = edgeEval.getCurrentSubtreeEvaluationCosts();
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
	public VertexCosts calculateCostsEdgeRestriction(EdgeRestrictionEvaluator e) {
		EdgeRestriction er = (EdgeRestriction) e.getVertex();

		long subtreeCosts = 0;
		if (er.getFirstIsTypeIdOfIncidence(EdgeDirection.IN) != null) {
			TypeIdEvaluator tEval = (TypeIdEvaluator) e.getVertexEvalMarker()
					.getMark(
							er.getFirstIsTypeIdOfIncidence(EdgeDirection.IN)
									.getAlpha());
			subtreeCosts += tEval.getCurrentSubtreeEvaluationCosts();
		}
		if (er.getFirstIsRoleIdOfIncidence(EdgeDirection.IN) != null) {
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
			EdgeSetExpressionEvaluator e) {
		EdgeSetExpression ese = (EdgeSetExpression) e.getVertex();

		long typeRestrCosts = 0;
		IsTypeRestrOfExpression inc = ese
				.getFirstIsTypeRestrOfExpressionIncidence();
		while (inc != null) {
			TypeIdEvaluator tideval = (TypeIdEvaluator) e.getVertexEvalMarker()
					.getMark(inc.getAlpha());
			typeRestrCosts += tideval.getCurrentSubtreeEvaluationCosts();
			inc = inc.getNextIsTypeRestrOfExpressionIncidence();
		}

		long ownCosts = graphSize.getEdgeCount() * edgeSetExpressionCostsFactor;
		return new VertexCosts(ownCosts, ownCosts, typeRestrCosts + ownCosts);
	}

	@Override
	public VertexCosts calculateCostsExponentiatedPathDescription(
			ExponentiatedPathDescriptionEvaluator e) {
		ExponentiatedPathDescription p = (ExponentiatedPathDescription) e
				.getVertex();
		long exponent = defaultExponent;
		VertexEvaluator expEval = e.getVertexEvalMarker().getMark(
				p.getFirstIsExponentOfIncidence(EdgeDirection.IN).getAlpha());
		if (expEval instanceof IntLiteralEvaluator) {
			try {
				exponent = ((Number) expEval.getResult(null)).longValue();
			} catch (Exception ex) {
			}
		}
		long exponentCosts = expEval.getCurrentSubtreeEvaluationCosts();
		VertexEvaluator pathEval = e.getVertexEvalMarker().getMark(
				p.getFirstIsExponentiatedPathOfIncidence(EdgeDirection.IN)
						.getAlpha());
		long pathCosts = pathEval.getCurrentSubtreeEvaluationCosts();
		long ownCosts = ((pathCosts * exponent) * 1) / 3;
		long subtreeCosts = pathCosts + ownCosts + exponentCosts;
		return new VertexCosts(ownCosts, ownCosts, subtreeCosts);
	}

	@Override
	public VertexCosts calculateCostsForwardVertexSet(
			ForwardVertexSetEvaluator e) {
		ForwardVertexSet bwvertex = (ForwardVertexSet) e.getVertex();
		Expression targetExpression = bwvertex.getFirstIsStartExprOfIncidence()
				.getAlpha();
		VertexEvaluator vertexEval = e.getVertexEvalMarker().getMark(
				targetExpression);
		long targetCosts = vertexEval.getCurrentSubtreeEvaluationCosts();
		PathDescription p = (PathDescription) bwvertex
				.getFirstIsPathOfIncidence().getAlpha();
		PathDescriptionEvaluator pathDescEval = (PathDescriptionEvaluator) e
				.getVertexEvalMarker().getMark(p);
		long pathDescCosts = pathDescEval.getCurrentSubtreeEvaluationCosts();
		long searchCosts = Math.round(pathDescCosts * searchFactor
				* Math.sqrt(graphSize.getEdgeCount()));
		long ownCosts = searchCosts;
		long iteratedCosts = ownCosts * e.getVariableCombinations();
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
			FunctionApplicationEvaluator e) {
		FunctionApplication funApp = (FunctionApplication) e.getVertex();

		IsArgumentOf inc = funApp
				.getFirstIsArgumentOfIncidence(EdgeDirection.IN);
		long argCosts = 0;
		ArrayList<Long> elements = new ArrayList<Long>();
		while (inc != null) {
			VertexEvaluator argEval = e.getVertexEvalMarker().getMark(
					inc.getAlpha());
			argCosts += argEval.getCurrentSubtreeEvaluationCosts();
			elements.add(argEval.getEstimatedCardinality());
			inc = inc.getNextIsArgumentOfIncidence(EdgeDirection.IN);
		}

		Function func = e.getFunction();
		long ownCosts = func.getEstimatedCosts(elements);
		long iteratedCosts = ownCosts * e.getVariableCombinations();
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
			Greql2ExpressionEvaluator e) {
		Greql2Expression greqlExp = (Greql2Expression) e.getVertex();
		VertexEvaluator queryExpEval = e.getVertexEvalMarker().getMark(
				greqlExp.getFirstIsQueryExprOfIncidence().getAlpha());
		long queryCosts = queryExpEval.getCurrentSubtreeEvaluationCosts();
		logger.info("QueryCosts: " + queryCosts);
		IsBoundVarOf boundVarInc = greqlExp.getFirstIsBoundVarOfIncidence();
		int boundVars = 0;
		while (boundVarInc != null) {
			boundVars++;
			boundVarInc = boundVarInc.getNextIsBoundVarOfIncidence();
		}
		long ownCosts = boundVars * greql2ExpressionCostsFactor;
		long iteratedCosts = ownCosts;
		long subtreeCosts = ownCosts + queryCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public VertexCosts calculateCostsIntermediateVertexPathDescription(
			IntermediateVertexPathDescriptionEvaluator e) {
		IntermediateVertexPathDescription pathDesc = (IntermediateVertexPathDescription) e
				.getVertex();
		IsSubPathOf inc = pathDesc.getFirstIsSubPathOfIncidence();
		PathDescriptionEvaluator firstPathEval = (PathDescriptionEvaluator) e
				.getVertexEvalMarker().getMark(inc.getAlpha());
		inc = inc.getNextIsSubPathOfIncidence();
		PathDescriptionEvaluator secondPathEval = (PathDescriptionEvaluator) e
				.getVertexEvalMarker().getMark(inc.getAlpha());
		long firstCosts = firstPathEval.getCurrentSubtreeEvaluationCosts();
		long secondCosts = secondPathEval.getCurrentSubtreeEvaluationCosts();
		VertexEvaluator vertexEval = e.getVertexEvalMarker().getMark(
				pathDesc.getFirstIsIntermediateVertexOfIncidence().getAlpha());
		long intermVertexCosts = vertexEval.getCurrentSubtreeEvaluationCosts();
		long ownCosts = 10;
		long iteratedCosts = 10;
		long subtreeCosts = iteratedCosts + intermVertexCosts + firstCosts
				+ secondCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public VertexCosts calculateCostsIteratedPathDescription(
			IteratedPathDescriptionEvaluator e) {
		IteratedPathDescription iterPath = (IteratedPathDescription) e
				.getVertex();
		VertexEvaluator pathEval = e.getVertexEvalMarker().getMark(
				iterPath.getFirstIsIteratedPathOfIncidence(EdgeDirection.IN)
						.getAlpha());
		long ownCosts = 5;
		long iteratedCosts = 5;
		long subtreeCosts = ownCosts
				+ pathEval.getCurrentSubtreeEvaluationCosts();
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
			ListConstructionEvaluator e) {
		ListConstruction listCons = (ListConstruction) e.getVertex();
		IsPartOf inc = listCons.getFirstIsPartOfIncidence();
		long parts = 0;
		long partCosts = 0;
		while (inc != null) {
			VertexEvaluator veval = e.getVertexEvalMarker().getMark(
					inc.getAlpha());
			partCosts += veval.getCurrentSubtreeEvaluationCosts();
			parts++;
			inc = inc.getNextIsPartOfIncidence();
		}

		long ownCosts = (parts * addToListCosts) + 2;
		long iteratedCosts = ownCosts * e.getVariableCombinations();
		long subtreeCosts = iteratedCosts + partCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public VertexCosts calculateCostsListRangeConstruction(
			ListRangeConstructionEvaluator e) {
		ListRangeConstruction exp = (ListRangeConstruction) e.getVertex();
		VertexEvaluator startExpEval = e.getVertexEvalMarker().getMark(
				exp.getFirstIsFirstValueOfIncidence().getAlpha());
		VertexEvaluator targetExpEval = e.getVertexEvalMarker().getMark(
				exp.getFirstIsLastValueOfIncidence().getAlpha());
		long startCosts = startExpEval.getCurrentSubtreeEvaluationCosts();
		long targetCosts = targetExpEval.getCurrentSubtreeEvaluationCosts();
		long range = 0;
		if (startExpEval instanceof IntLiteralEvaluator) {
			if (targetExpEval instanceof IntLiteralEvaluator) {
				try {
					range = (((Number) targetExpEval.getResult(null))
							.longValue() - ((Number) startExpEval
							.getResult(null)).longValue()) + 1;
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
		long iteratedCosts = ownCosts * e.getVariableCombinations();
		long subtreeCosts = iteratedCosts + startCosts + targetCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public VertexCosts calculateCostsOptionalPathDescription(
			OptionalPathDescriptionEvaluator e) {
		OptionalPathDescription iterPath = (OptionalPathDescription) e
				.getVertex();
		VertexEvaluator pathEval = e.getVertexEvalMarker().getMark(
				iterPath.getFirstIsOptionalPathOfIncidence(EdgeDirection.IN)
						.getAlpha());
		long ownCosts = 5;
		long iteratedCosts = 5;
		long subtreeCosts = ownCosts
				+ pathEval.getCurrentSubtreeEvaluationCosts();
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public VertexCosts calculateCostsPathExistence(PathExistenceEvaluator e) {
		PathExistence existence = (PathExistence) e.getVertex();
		Expression startExpression = existence.getFirstIsStartExprOfIncidence()
				.getAlpha();
		VertexEvaluator vertexEval = e.getVertexEvalMarker().getMark(
				startExpression);
		long startCosts = vertexEval.getCurrentSubtreeEvaluationCosts();
		Expression targetExpression = existence
				.getFirstIsTargetExprOfIncidence().getAlpha();
		vertexEval = e.getVertexEvalMarker().getMark(targetExpression);
		long targetCosts = vertexEval.getCurrentSubtreeEvaluationCosts();
		PathDescription p = (PathDescription) existence
				.getFirstIsPathOfIncidence().getAlpha();
		PathDescriptionEvaluator pathDescEval = (PathDescriptionEvaluator) e
				.getVertexEvalMarker().getMark(p);
		long pathDescCosts = pathDescEval.getCurrentSubtreeEvaluationCosts();
		long searchCosts = Math.round(((pathDescCosts * searchFactor) / 2.0)
				* Math.sqrt(graphSize.getEdgeCount()));
		long ownCosts = searchCosts;
		long iteratedCosts = ownCosts * e.getVariableCombinations();
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
			QuantifiedExpressionEvaluator e) {
		QuantifiedExpression quantifiedExpr = (QuantifiedExpression) e
				.getVertex();

		VertexEvaluator declEval = e.getVertexEvalMarker()
				.getMark(
						quantifiedExpr.getFirstIsQuantifiedDeclOfIncidence()
								.getAlpha());
		long declCosts = declEval.getCurrentSubtreeEvaluationCosts();

		VertexEvaluator boundExprEval = e.getVertexEvalMarker().getMark(
				quantifiedExpr.getFirstIsBoundExprOfQuantifierIncidence()
						.getAlpha());
		long boundExprCosts = boundExprEval.getCurrentSubtreeEvaluationCosts();

		long ownCosts = 20;
		long iteratedCosts = ownCosts * e.getVariableCombinations();
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
			RecordConstructionEvaluator e) {
		RecordConstruction recCons = (RecordConstruction) e.getVertex();
		IsPartOf inc = recCons.getFirstIsPartOfIncidence(EdgeDirection.IN);
		long recElems = 0;
		long recElemCosts = 0;
		while (inc != null) {
			RecordElement recElem = (RecordElement) inc.getAlpha();
			VertexEvaluator veval = e.getVertexEvalMarker().getMark(recElem);
			recElemCosts += veval.getCurrentSubtreeEvaluationCosts();
			recElems++;
			inc = inc.getNextIsPartOfIncidence(EdgeDirection.IN);
		}

		long ownCosts = (recElems * addToRecordCosts) + 2;
		long iteratedCosts = ownCosts * e.getVariableCombinations();
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
	public VertexCosts calculateCostsRecordElement(RecordElementEvaluator e) {
		RecordElement recElem = (RecordElement) e.getVertex();

		IsRecordExprOf inc = recElem.getFirstIsRecordExprOfIncidence();
		VertexEvaluator veval = e.getVertexEvalMarker().getMark(inc.getAlpha());
		long recordExprCosts = veval.getCurrentSubtreeEvaluationCosts();

		long ownCosts = 3;
		long iteratedCosts = ownCosts * e.getVariableCombinations();
		long subtreeCosts = recordExprCosts + iteratedCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public VertexCosts calculateCostsSequentialPathDescription(
			SequentialPathDescriptionEvaluator e) {
		SequentialPathDescription p = (SequentialPathDescription) e.getVertex();
		long aggregatedCosts = 0;
		IsSequenceElementOf inc = p
				.getFirstIsSequenceElementOfIncidence(EdgeDirection.IN);
		long alternatives = 0;
		while (inc != null) {
			PathDescriptionEvaluator pathEval = (PathDescriptionEvaluator) e
					.getVertexEvalMarker().getMark(inc.getAlpha());
			aggregatedCosts += pathEval.getCurrentSubtreeEvaluationCosts();
			inc = inc.getNextIsSequenceElementOfIncidence(EdgeDirection.IN);
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
			SetComprehensionEvaluator e) {
		SetComprehension setComp = e.getVertex();
		Declaration decl = setComp.getFirstIsCompDeclOfIncidence().getAlpha();
		DeclarationEvaluator declEval = (DeclarationEvaluator) e
				.getVertexEvalMarker().getMark(decl);
		long declCosts = declEval.getCurrentSubtreeEvaluationCosts();

		Vertex resultDef = setComp.getFirstIsCompResultDefOfIncidence()
				.getAlpha();
		VertexEvaluator resultDefEval = e.getVertexEvalMarker().getMark(
				resultDef);
		long resultCosts = resultDefEval.getCurrentSubtreeEvaluationCosts();

		long ownCosts = resultDefEval.getEstimatedCardinality() * addToSetCosts;
		long iteratedCosts = ownCosts * e.getVariableCombinations();
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
	public VertexCosts calculateCostsSetConstruction(SetConstructionEvaluator e) {
		SetConstruction setCons = (SetConstruction) e.getVertex();
		IsPartOf inc = setCons.getFirstIsPartOfIncidence(EdgeDirection.IN);
		long parts = 0;
		long partCosts = 0;
		while (inc != null) {
			VertexEvaluator veval = e.getVertexEvalMarker().getMark(
					inc.getAlpha());
			partCosts += veval.getCurrentSubtreeEvaluationCosts();
			parts++;
			inc = inc.getNextIsPartOfIncidence(EdgeDirection.IN);
		}

		long ownCosts = (parts * addToSetCosts) + 2;
		long iteratedCosts = ownCosts * e.getVariableCombinations();
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
			SimpleDeclarationEvaluator e) {
		SimpleDeclaration simpleDecl = (SimpleDeclaration) e.getVertex();

		// Calculate the costs for the type definition
		VertexEvaluator typeExprEval = e.getVertexEvalMarker().getMark(
				simpleDecl.getFirstIsTypeExprOfIncidence().getAlpha());

		long typeCosts = typeExprEval.getCurrentSubtreeEvaluationCosts();

		// Calculate the costs for the declared variables
		long declaredVarCosts = 0;
		IsDeclaredVarOf inc = simpleDecl
				.getFirstIsDeclaredVarOfIncidence(EdgeDirection.IN);
		while (inc != null) {
			VariableEvaluator varEval = (VariableEvaluator) e
					.getVertexEvalMarker().getMark(inc.getAlpha());
			declaredVarCosts += varEval.getCurrentSubtreeEvaluationCosts();
			inc = inc.getNextIsDeclaredVarOfIncidence(EdgeDirection.IN);
		}

		long ownCosts = 2;
		long iteratedCosts = ownCosts * e.getVariableCombinations();
		long subtreeCosts = iteratedCosts + declaredVarCosts + typeCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public VertexCosts calculateCostsSimplePathDescription(
			SimplePathDescriptionEvaluator e) {
		return new VertexCosts(transitionCosts, transitionCosts,
				transitionCosts);
	}

	@Override
	public VertexCosts calculateCostsAggregationPathDescription(
			AggregationPathDescriptionEvaluator e) {
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
			TableComprehensionEvaluator e) {
		// TODO (heimdall): What is a TableComprehension? Syntax? Where do the
		// costs differ from a ListComprehension?
		TableComprehension tableComp = (TableComprehension) e.getVertex();

		Declaration decl = tableComp.getFirstIsCompDeclOfIncidence().getAlpha();
		DeclarationEvaluator declEval = (DeclarationEvaluator) e
				.getVertexEvalMarker().getMark(decl);
		long declCosts = declEval.getCurrentSubtreeEvaluationCosts();

		Vertex resultDef = tableComp.getFirstIsCompResultDefOfIncidence()
				.getAlpha();
		VertexEvaluator resultDefEval = e.getVertexEvalMarker().getMark(
				resultDef);
		long resultCosts = resultDefEval.getCurrentSubtreeEvaluationCosts();

		long ownCosts = resultDefEval.getEstimatedCardinality()
				* addToListCosts;
		long iteratedCosts = ownCosts * e.getVariableCombinations();
		long subtreeCosts = iteratedCosts + resultCosts + declCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public VertexCosts calculateCostsTransposedPathDescription(
			TransposedPathDescriptionEvaluator e) {
		TransposedPathDescription transPath = (TransposedPathDescription) e
				.getVertex();
		PathDescriptionEvaluator pathEval = (PathDescriptionEvaluator) e
				.getVertexEvalMarker().getMark(
						transPath.getFirstIsTransposedPathOfIncidence()
								.getAlpha());
		long pathCosts = pathEval.getCurrentSubtreeEvaluationCosts();
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
			TupleConstructionEvaluator e) {
		TupleConstruction tupCons = (TupleConstruction) e.getVertex();
		IsPartOf inc = tupCons.getFirstIsPartOfIncidence(EdgeDirection.IN);
		long parts = 0;
		long partCosts = 0;
		while (inc != null) {
			VertexEvaluator veval = e.getVertexEvalMarker().getMark(
					inc.getAlpha());
			partCosts += veval.getCurrentSubtreeEvaluationCosts();
			parts++;
			inc = inc.getNextIsPartOfIncidence(EdgeDirection.IN);
		}

		long ownCosts = (parts * addToTupleCosts) + 2;
		long iteratedCosts = ownCosts * e.getVariableCombinations();
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
	public VertexCosts calculateCostsTypeId(TypeIdEvaluator e) {
		long costs = graphSize.getKnownEdgeTypes()
				+ graphSize.getKnownVertexTypes();
		return new VertexCosts(costs, costs, costs);
	}

	@Override
	public VertexCosts calculateCostsVariable(VariableEvaluator e) {
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
			VertexSetExpressionEvaluator e) {
		VertexSetExpression vse = (VertexSetExpression) e.getVertex();

		long typeRestrCosts = 0;
		IsTypeRestrOfExpression inc = vse
				.getFirstIsTypeRestrOfExpressionIncidence();
		while (inc != null) {
			TypeIdEvaluator tideval = (TypeIdEvaluator) e.getVertexEvalMarker()
					.getMark(inc.getAlpha());
			typeRestrCosts += tideval.getCurrentSubtreeEvaluationCosts();
			inc = inc.getNextIsTypeRestrOfExpressionIncidence();
		}

		long ownCosts = graphSize.getVertexCount()
				* vertexSetExpressionCostsFactor;
		return new VertexCosts(ownCosts, ownCosts, typeRestrCosts + ownCosts);
	}

	@Override
	public double calculateSelectivityFunctionApplication(
			FunctionApplicationEvaluator e) {
		Function func = e.getFunction();
		if (func != null) {
			return func.getSelectivity();
		} else {
			return 1;
		}
	}

	@Override
	public double calculateSelectivityPathExistence(PathExistenceEvaluator e) {
		return 0.1;
	}

	@Override
	public double calculateSelectivityTypeId(TypeIdEvaluator e) {
		int typesInSchema = (int) Math
				.round((graphSize.getKnownEdgeTypes() + graphSize
						.getKnownVertexTypes()) / 2.0);
		double selectivity = 1.0;
		TypeId id = (TypeId) e.getVertex();
		if (id.is_type()) {
			selectivity = 1.0 / typesInSchema;
		} else {
			double avgSubclasses = (graphSize.getAverageEdgeSubclasses() + graphSize
					.getAverageVertexSubclasses()) / 2.0;
			selectivity = avgSubclasses / typesInSchema;
		}
		if (id.is_excluded()) {
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
	public long calculateVariableAssignments(VariableEvaluator e) {
		Variable v = (Variable) e.getVertex();
		IsDeclaredVarOf inc = v.getFirstIsDeclaredVarOfIncidence();
		if (inc != null) {
			SimpleDeclaration decl = inc.getOmega();
			VertexEvaluator typeExpEval = e.getVertexEvalMarker().getMark(
					decl.getFirstIsTypeExprOfIncidence().getAlpha());
			return typeExpEval.getEstimatedCardinality();
		} else {
			// if there exists no "isDeclaredVarOf"-Edge the variable is not
			// declared but defined, so there exists only 1 possible assignment
			return 1;
		}
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

	@Override
	public VertexCosts calculateCostsMapConstruction(MapConstructionEvaluator e) {
		MapConstruction mapCons = (MapConstruction) e.getVertex();
		IsKeyExprOfConstruction keyInc = mapCons
				.getFirstIsKeyExprOfConstructionIncidence(EdgeDirection.IN);
		IsValueExprOfConstruction valInc = mapCons
				.getFirstIsValueExprOfConstructionIncidence(EdgeDirection.IN);
		long parts = 0;
		long partCosts = 0;
		while (keyInc != null) {
			VertexEvaluator keyEval = e.getVertexEvalMarker().getMark(
					keyInc.getAlpha());
			partCosts += keyEval.getCurrentSubtreeEvaluationCosts();
			VertexEvaluator valueEval = e.getVertexEvalMarker().getMark(
					valInc.getAlpha());
			partCosts += keyEval.getCurrentSubtreeEvaluationCosts()
					+ valueEval.getCurrentSubtreeEvaluationCosts();
			parts++;
			keyInc = keyInc
					.getNextIsKeyExprOfConstructionIncidence(EdgeDirection.IN);
			valInc = valInc
					.getNextIsValueExprOfConstructionIncidence(EdgeDirection.IN);
		}

		long ownCosts = (parts * addToSetCosts) + 2;
		long iteratedCosts = ownCosts * e.getVariableCombinations();
		long subtreeCosts = iteratedCosts + partCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	@Override
	public long calculateCardinalityMapConstruction(MapConstructionEvaluator e) {
		long mappings = 0;
		MapConstruction mapCons = (MapConstruction) e.getVertex();
		IsKeyExprOfConstruction inc = mapCons
				.getFirstIsKeyExprOfConstructionIncidence(EdgeDirection.IN);
		while (inc != null) {
			mappings++;
			inc = inc.getNextIsKeyExprOfConstructionIncidence(EdgeDirection.IN);
		}
		return mappings;
	}

	@Override
	public VertexCosts calculateCostsMapComprehension(
			MapComprehensionEvaluator e) {
		MapComprehension mapComp = (MapComprehension) e.getVertex();
		Declaration decl = mapComp.getFirstIsCompDeclOfIncidence().getAlpha();
		DeclarationEvaluator declEval = (DeclarationEvaluator) e
				.getVertexEvalMarker().getMark(decl);
		long declCosts = declEval.getCurrentSubtreeEvaluationCosts();

		Vertex key = mapComp.getFirstIsKeyExprOfComprehensionIncidence(
				EdgeDirection.IN).getAlpha();
		VertexEvaluator keyEval = e.getVertexEvalMarker().getMark(key);
		Vertex value = mapComp.getFirstIsValueExprOfComprehensionIncidence(
				EdgeDirection.IN).getAlpha();
		VertexEvaluator valEval = e.getVertexEvalMarker().getMark(value);

		long resultCosts = keyEval.getCurrentSubtreeEvaluationCosts()
				+ valEval.getCurrentSubtreeEvaluationCosts();

		long ownCosts = keyEval.getEstimatedCardinality()
				+ (valEval.getEstimatedCardinality() * addToSetCosts);
		long iteratedCosts = ownCosts * e.getVariableCombinations();
		long subtreeCosts = iteratedCosts + resultCosts + declCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);

	}

	@Override
	public long calculateCardinalityMapComprehension(MapComprehensionEvaluator e) {
		MapComprehension setComp = (MapComprehension) e.getVertex();
		Declaration decl = setComp.getFirstIsCompDeclOfIncidence().getAlpha();
		DeclarationEvaluator declEval = (DeclarationEvaluator) e
				.getVertexEvalMarker().getMark(decl);
		return declEval.getEstimatedCardinality();
	}

}
