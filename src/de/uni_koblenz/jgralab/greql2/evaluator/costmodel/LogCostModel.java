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
/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.evaluator.costmodel;

import java.util.logging.LogRecord;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.NFA;
import de.uni_koblenz.jgralab.greql2.evaluator.logging.EvaluationLogReader;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.AlternativePathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.BackwardVertexSetEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.BagConstructionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.ComprehensionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.ConditionalExpressionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.DeclarationEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.EdgePathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.EdgeSetExpressionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.EdgeSubgraphExpressionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.ExponentiatedPathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.ForwardVertexSetEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.FunctionApplicationEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.IntLiteralEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.IntermediateVertexPathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.IteratedPathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.ListConstructionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.ListRangeConstructionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.OptionalPathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.PathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.PathExistenceEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.RecordConstructionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.SequentialPathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.SetComprehensionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.SetConstructionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.SimpleDeclarationEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.SimplePathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.TableComprehensionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.TransposedPathDescriptionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.TupleConstructionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.TypeIdEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexSetExpressionEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexSubgraphExpressionEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.CostModelException;
import de.uni_koblenz.jgralab.greql2.funlib.Greql2Function;
import de.uni_koblenz.jgralab.greql2.schema.BackwardVertexSet;
import de.uni_koblenz.jgralab.greql2.schema.ConditionalExpression;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.ForwardVertexSet;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.IsPathDescriptionOf;
import de.uni_koblenz.jgralab.greql2.schema.ListRangeConstruction;
import de.uni_koblenz.jgralab.greql2.schema.PathDescription;
import de.uni_koblenz.jgralab.greql2.schema.PathExistence;

/**
 * This {@link CostModel} estimates the costs, cardinality and selectivity for
 * all {@link Greql2Vertex}s of a given query syntaxgraph. It uses an instance
 * of {@link EvaluationLogReader} to calculate estimations based on experience.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class LogCostModel extends DefaultCostModel {

	/**
	 * The {@link LogRecord} which provides the logged values used for
	 * estimation of the costs, cardinality and selectivity of the various
	 * vertices.
	 */
	protected EvaluationLogReader logReader;

	/**
	 * The factor used to weight the logged values.
	 */
	protected float logScalingFactor;

	/**
	 * Creates a new {@link LogCostModel}. The given {@link EvaluationLogReader}
	 * provides the average sizes of input and output and selectivity of the
	 * {@link Greql2Vertex}s.
	 * 
	 * @param logReader
	 *            the {@link EvaluationLogReader} to be used
	 * @param logScalingFactor
	 *            a value between 0 and 1. It determines how much to trust the
	 *            values given by the <code>logReader</code>. 0 means, don't use
	 *            the value at all, 1 means, use the logged values as if they
	 *            were provided by the Oracle of Delphi. This has one exception:
	 *            If the <code>logReader</code> returns 0 for some
	 *            {@link Greql2Vertex}, it most probably means that till now no
	 *            values have been logged. In that case
	 *            <code>logScalingFactor</code> has no effect and this
	 *            {@link LogCostModel} behaves as if it was set to 0.
	 * @param eval
	 *            the {@link GreqlEvaluator} that will be used for accessing the
	 *            {@link VertexEvaluator}s
	 * @throws CostModelException
	 *             if an invalid <code>scalingFactor</code> was given
	 */
	public LogCostModel(EvaluationLogReader logReader, float logScalingFactor,
			GreqlEvaluator eval) throws CostModelException {
		super(eval);
		this.logReader = logReader;

		if ((logScalingFactor < 0) || (logScalingFactor > 1)) {
			throw new CostModelException(
					"scalingFactor has to be between 0.0 and 1.0, but was "
							+ logScalingFactor);
		}
		this.logScalingFactor = logScalingFactor;
	}

	/**
	 * Calculate the mean of the given two values.
	 * 
	 * @param logCard
	 *            the cardinality based on the logger
	 * @param defaultCard
	 *            the cardinality based on {@link DefaultCostModel}
	 * @return <code>defaultCard</code>, if <code>logCard</code> is zero (which
	 *         most probably means no cardinality for this {@link Greql2Vertex}
	 *         has been logged so far). Else return a mean of both values scaled
	 *         by <code>logScalingFactor</code>.
	 */
	private long getMeanCardinality(double logCard, long defaultCard) {
		if (logCard == 0) {
			return defaultCard;
		}
		return Math.round((logCard * logScalingFactor)
				+ (defaultCard * (1 - logScalingFactor)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCardinalityBackwardVertexSet
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.BackwardVertexSetEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public long calculateCardinalityBackwardVertexSet(
			BackwardVertexSetEvaluator e, GraphSize graphSize) {
		return getMeanCardinality(logReader
				.getAvgResultSize(e.getLoggingName()), super
				.calculateCardinalityBackwardVertexSet(e, graphSize));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCardinalityBagComprehension
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.BagComprehensionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public long calculateCardinalityBagComprehension(ComprehensionEvaluator e,
			GraphSize graphSize) {
		return getMeanCardinality(logReader
				.getAvgResultSize(e.getLoggingName()), super
				.calculateCardinalityBagComprehension(e, graphSize));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCardinalityBagConstruction
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.BagConstructionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public long calculateCardinalityBagConstruction(BagConstructionEvaluator e,
			GraphSize graphSize) {
		return getMeanCardinality(logReader
				.getAvgResultSize(e.getLoggingName()), super
				.calculateCardinalityBagConstruction(e, graphSize));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCardinalityConditionalExpression
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.ConditionalExpressionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public long calculateCardinalityConditionalExpression(
			ConditionalExpressionEvaluator e, GraphSize graphSize) {
		return getMeanCardinality(logReader
				.getAvgResultSize(e.getLoggingName()), super
				.calculateCardinalityConditionalExpression(e, graphSize));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCardinalityDeclaration
	 * (de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.DeclarationEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public long calculateCardinalityDeclaration(DeclarationEvaluator e,
			GraphSize graphSize) {
		return super.calculateCardinalityDeclaration(e, graphSize);
		// I think the logging doesn't make sense here...
		//
		// return getMeanCardinality(logReader
		// .getAvgResultSize(e.getLoggingName()), super
		// .calculateCardinalityDeclaration(e, graphSize));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCardinalityEdgeSetExpression
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.EdgeSetExpressionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public long calculateCardinalityEdgeSetExpression(
			EdgeSetExpressionEvaluator e, GraphSize graphSize) {
		return getMeanCardinality(logReader
				.getAvgResultSize(e.getLoggingName()), super
				.calculateCardinalityEdgeSetExpression(e, graphSize));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCardinalityEdgeSubgraphExpression
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.EdgeSubgraphExpressionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public long calculateCardinalityEdgeSubgraphExpression(
			EdgeSubgraphExpressionEvaluator e, GraphSize graphSize) {
		return getMeanCardinality(logReader
				.getAvgResultSize(e.getLoggingName()), super
				.calculateCardinalityEdgeSubgraphExpression(e, graphSize));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCardinalityForwardVertexSet
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.ForwardVertexSetEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public long calculateCardinalityForwardVertexSet(
			ForwardVertexSetEvaluator e, GraphSize graphSize) {
		return getMeanCardinality(logReader
				.getAvgResultSize(e.getLoggingName()), super
				.calculateCardinalityForwardVertexSet(e, graphSize));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCardinalityFunctionApplication
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.FunctionApplicationEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public long calculateCardinalityFunctionApplication(
			FunctionApplicationEvaluator e, GraphSize graphSize) {
		return getMeanCardinality(logReader
				.getAvgResultSize(e.getLoggingName()), super
				.calculateCardinalityFunctionApplication(e, graphSize));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCardinalityListConstruction
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.ListConstructionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public long calculateCardinalityListConstruction(
			ListConstructionEvaluator e, GraphSize graphSize) {
		return getMeanCardinality(logReader
				.getAvgResultSize(e.getLoggingName()), super
				.calculateCardinalityListConstruction(e, graphSize));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCardinalityListRangeConstruction
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.ListRangeConstructionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public long calculateCardinalityListRangeConstruction(
			ListRangeConstructionEvaluator e, GraphSize graphSize) {
		return getMeanCardinality(logReader
				.getAvgResultSize(e.getLoggingName()), super
				.calculateCardinalityListRangeConstruction(e, graphSize));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCardinalityRecordConstruction
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.RecordConstructionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public long calculateCardinalityRecordConstruction(
			RecordConstructionEvaluator e, GraphSize graphSize) {
		return getMeanCardinality(logReader
				.getAvgResultSize(e.getLoggingName()), super
				.calculateCardinalityRecordConstruction(e, graphSize));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCardinalitySetComprehension
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.SetComprehensionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public long calculateCardinalitySetComprehension(
			SetComprehensionEvaluator e, GraphSize graphSize) {
		return getMeanCardinality(logReader
				.getAvgResultSize(e.getLoggingName()), super
				.calculateCardinalitySetComprehension(e, graphSize));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCardinalitySetConstruction
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.SetConstructionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public long calculateCardinalitySetConstruction(SetConstructionEvaluator e,
			GraphSize graphSize) {
		return getMeanCardinality(logReader
				.getAvgResultSize(e.getLoggingName()), super
				.calculateCardinalitySetConstruction(e, graphSize));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCardinalitySimpleDeclaration
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.SimpleDeclarationEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public long calculateCardinalitySimpleDeclaration(
			SimpleDeclarationEvaluator e, GraphSize graphSize) {
		return super.calculateCardinalitySimpleDeclaration(e, graphSize);
		// I think the log results don't make sense here...
		//
		// return getMeanCardinality(logReader
		// .getAvgResultSize(e.getLoggingName()), super
		// .calculateCardinalitySimpleDeclaration(e, graphSize));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCardinalityTableComprehension
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.TableComprehensionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public long calculateCardinalityTableComprehension(
			TableComprehensionEvaluator e, GraphSize graphSize) {
		return getMeanCardinality(logReader
				.getAvgResultSize(e.getLoggingName()), super
				.calculateCardinalityTableComprehension(e, graphSize));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCardinalityTupleConstruction
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.TupleConstructionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public long calculateCardinalityTupleConstruction(
			TupleConstructionEvaluator e, GraphSize graphSize) {
		return getMeanCardinality(logReader
				.getAvgResultSize(e.getLoggingName()), super
				.calculateCardinalityTupleConstruction(e, graphSize));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCardinalityVertexSetExpression
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.VertexSetExpressionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public long calculateCardinalityVertexSetExpression(
			VertexSetExpressionEvaluator e, GraphSize graphSize) {
		return getMeanCardinality(logReader
				.getAvgResultSize(e.getLoggingName()), super
				.calculateCardinalityVertexSetExpression(e, graphSize));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCardinalityVertexSubgraphExpression
	 * (de.uni_koblenz.jgralab.greql2
	 * .evaluator.vertexeval.VertexSubgraphExpressionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public long calculateCardinalityVertexSubgraphExpression(
			VertexSubgraphExpressionEvaluator e, GraphSize graphSize) {
		return getMeanCardinality(logReader
				.getAvgResultSize(e.getLoggingName()), super
				.calculateCardinalityVertexSubgraphExpression(e, graphSize));
	}

	/**
	 * Calculates the costs for any {@link PathDescription}. Such vertices are
	 * not really evaluated. Instead a {@link NFA} is constructed which combines
	 * the {@link NFA}s from the child vertices. The real evaluation is
	 * performed in the {@link PathExistence}, {@link ForwardVertexSet} or
	 * {@link BackwardVertexSet} which are the root vertex of the
	 * {@link PathDescription} subgraph. So the costs for any
	 * {@link PathDescription} are only given by
	 * {@link CostModelBase#defaultNfaConstructionCosts} + the subtree costs.
	 * 
	 * @param e
	 *            a {@link PathDescriptionEvaluator}
	 * @param graphSize
	 *            the {@link GraphSize} of the datagraph
	 * @return the costs for creating a {@link NFA} for this
	 *         {@link PathDescription}
	 */
	private VertexCosts calculateCostsPathDescription(
			PathDescriptionEvaluator e, GraphSize graphSize) {
		PathDescription p = (PathDescription) e.getVertex();

		IsPathDescriptionOf inc = p
				.getFirstIsPathDescriptionOfIncidence(EdgeDirection.IN);

		long subtreeCosts = 0;
		while (inc != null) {
			PathDescriptionEvaluator pathEval = (PathDescriptionEvaluator) greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
			subtreeCosts += pathEval
					.getCurrentSubtreeEvaluationCosts(graphSize);
			inc = inc.getNextIsPathDescriptionOf(EdgeDirection.IN);
		}

		long ownCosts = defaultNfaConstructionCosts;
		long iteratedCosts = ownCosts * e.getVariableCombinations(graphSize);
		return new VertexCosts(ownCosts, iteratedCosts, iteratedCosts
				+ subtreeCosts);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsAlternativePathDescription
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.AlternativePathDescriptionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsAlternativePathDescription(
			AlternativePathDescriptionEvaluator e, GraphSize graphSize) {
		return calculateCostsPathDescription(e, graphSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsBackwardVertexSet
	 * (de.uni_koblenz.jgralab.greql2.evaluator.vertexeval
	 * .BackwardVertexSetEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsBackwardVertexSet(
			BackwardVertexSetEvaluator e, GraphSize graphSize) {
		BackwardVertexSet vertex = (BackwardVertexSet) e.getVertex();
		Expression targetExpression = (Expression) vertex
				.getFirstIsTargetExprOfIncidence().getAlpha();
		VertexEvaluator vertexEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(targetExpression);
		long targetCosts = vertexEval
				.getCurrentSubtreeEvaluationCosts(graphSize);
		PathDescription p = (PathDescription) vertex.getFirstIsPathOfIncidence()
				.getAlpha();
		PathDescriptionEvaluator pathDescEval = (PathDescriptionEvaluator) greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(p);
		long pathDescCosts = pathDescEval
				.getCurrentSubtreeEvaluationCosts(graphSize);

		long noOfStates = Math.round(logReader
				.getAvgResultSize("PathDescription"));
		if (noOfStates == 0) {
			noOfStates = defaultDfaStateNumber;
		}

		// We assume the costs for evaluating a DFA is the square of its number
		// of states.
		long ownCosts = noOfStates * noOfStates;
		long iteratedCosts = ownCosts * e.getVariableCombinations(graphSize);
		long subtreeCosts = iteratedCosts + targetCosts + pathDescCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsConditionalExpression
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.ConditionalExpressionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsConditionalExpression(
			ConditionalExpressionEvaluator e, GraphSize graphSize) {
		ConditionalExpression vertex = (ConditionalExpression) e.getVertex();

		Expression condition = (Expression) vertex.getFirstIsConditionOfIncidence()
				.getAlpha();
		VertexEvaluator conditionEvaluator = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(condition);
		long conditionCosts = conditionEvaluator
				.getCurrentSubtreeEvaluationCosts(graphSize);

		double conditionSelectivity = logReader
				.getAvgSelectivity(conditionEvaluator.getLoggingName());
		if ((conditionSelectivity <= 0) || (conditionSelectivity >= 1)) {
			conditionSelectivity = 1.0 / 3.0;
		}

		Expression expressionToEvaluate;
		expressionToEvaluate = (Expression) vertex.getFirstIsTrueExprOfIncidence()
				.getAlpha();
		VertexEvaluator vertexEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(expressionToEvaluate);
		long trueCosts = vertexEval.getCurrentSubtreeEvaluationCosts(graphSize);

		expressionToEvaluate = (Expression) vertex.getFirstIsFalseExprOfIncidence()
				.getAlpha();
		vertexEval = greqlEvaluator.getVertexEvaluatorGraphMarker().getMark(
				expressionToEvaluate);
		long falseCosts = vertexEval
				.getCurrentSubtreeEvaluationCosts(graphSize);

		// We say the costs are the average of all costs weighted by the
		// probability for each case. The probability of the true-case is given
		// by conditionSelectivity.
		long avgCosts = Math.round((trueCosts * conditionSelectivity)
				+ falseCosts * (1 - conditionSelectivity));

		long ownCosts = 4;
		long iteratedCosts = ownCosts * e.getVariableCombinations(graphSize);
		long subtreeCosts = iteratedCosts + avgCosts + conditionCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsEdgePathDescription
	 * (de.uni_koblenz.jgralab.greql2.evaluator.
	 * vertexeval.EdgePathDescriptionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsEdgePathDescription(
			EdgePathDescriptionEvaluator e, GraphSize graphSize) {
		return calculateCostsPathDescription(e, graphSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsExponentiatedPathDescription
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.ExponentiatedPathDescriptionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsExponentiatedPathDescription(
			ExponentiatedPathDescriptionEvaluator e, GraphSize graphSize) {
		return calculateCostsPathDescription(e, graphSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsForwardVertexSet
	 * (de.uni_koblenz.jgralab.greql2.evaluator.vertexeval
	 * .ForwardVertexSetEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsForwardVertexSet(
			ForwardVertexSetEvaluator e, GraphSize graphSize) {
		ForwardVertexSet vertex = (ForwardVertexSet) e.getVertex();
		Expression startExpression = (Expression) vertex
				.getFirstIsStartExprOfIncidence().getAlpha();
		VertexEvaluator vertexEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(startExpression);
		long startCosts = vertexEval
				.getCurrentSubtreeEvaluationCosts(graphSize);
		PathDescription p = (PathDescription) vertex.getFirstIsPathOfIncidence()
				.getAlpha();
		PathDescriptionEvaluator pathDescEval = (PathDescriptionEvaluator) greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(p);
		long pathDescCosts = pathDescEval
				.getCurrentSubtreeEvaluationCosts(graphSize);

		long noOfStates = Math.round(logReader
				.getAvgResultSize("PathDescription"));
		if (noOfStates == 0) {
			noOfStates = defaultDfaStateNumber;
		}

		// We assume the costs for evaluating a DFA is the square of its number
		// of states.
		long ownCosts = noOfStates * noOfStates;
		long iteratedCosts = ownCosts * e.getVariableCombinations(graphSize);
		long subtreeCosts = iteratedCosts + startCosts + pathDescCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsIntermediateVertexPathDescription
	 * (de.uni_koblenz.jgralab.greql2
	 * .evaluator.vertexeval.IntermediateVertexPathDescriptionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsIntermediateVertexPathDescription(
			IntermediateVertexPathDescriptionEvaluator e, GraphSize graphSize) {
		return calculateCostsPathDescription(e, graphSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsIteratedPathDescription
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.IteratedPathDescriptionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsIteratedPathDescription(
			IteratedPathDescriptionEvaluator e, GraphSize graphSize) {
		return calculateCostsPathDescription(e, graphSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsListRangeConstruction
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.ListRangeConstructionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsListRangeConstruction(
			ListRangeConstructionEvaluator e, GraphSize graphSize) {
		ListRangeConstruction exp = (ListRangeConstruction) e.getVertex();

		VertexEvaluator startExpEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(
						exp.getFirstIsFirstValueOfIncidence().getAlpha());
		VertexEvaluator targetExpEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(
						exp.getFirstIsLastValueOfIncidence().getAlpha());

		long startCosts = startExpEval
				.getCurrentSubtreeEvaluationCosts(graphSize);
		long targetCosts = targetExpEval
				.getCurrentSubtreeEvaluationCosts(graphSize);

		long range = -1;
		if (startExpEval instanceof IntLiteralEvaluator) {
			if (targetExpEval instanceof IntLiteralEvaluator) {
				try {
					range = targetExpEval.getResult(null).toInteger()
							- startExpEval.getResult(null).toInteger() + 1;
				} catch (Exception ex) {
					// if an exception occurs, ignore it!
				}
			}
		}

		if (range <= 0) {
			range = Math.round(logReader.getAvgResultSize(e.getLoggingName()));
		}

		if (range <= 0) {
			range = defaultListRangeSize;
		}

		long ownCosts = addToListCosts * range;
		long iteratedCosts = ownCosts * e.getVariableCombinations(graphSize);
		long subtreeCosts = iteratedCosts + startCosts + targetCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsOptionalPathDescription
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.OptionalPathDescriptionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsOptionalPathDescription(
			OptionalPathDescriptionEvaluator e, GraphSize graphSize) {
		return calculateCostsPathDescription(e, graphSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsPathExistence
	 * (de.uni_koblenz.jgralab.greql2.evaluator.vertexeval
	 * .PathExistenceEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsPathExistence(PathExistenceEvaluator e,
			GraphSize graphSize) {
		PathExistence existence = (PathExistence) e.getVertex();
		Expression startExpression = (Expression) existence
				.getFirstIsStartExprOfIncidence().getAlpha();
		VertexEvaluator vertexEval = greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(startExpression);
		long startCosts = vertexEval
				.getCurrentSubtreeEvaluationCosts(graphSize);

		Expression targetExpression = (Expression) existence
				.getFirstIsTargetExprOfIncidence().getAlpha();
		vertexEval = greqlEvaluator.getVertexEvaluatorGraphMarker().getMark(
				targetExpression);
		long targetCosts = vertexEval
				.getCurrentSubtreeEvaluationCosts(graphSize);

		PathDescription p = (PathDescription) existence.getFirstIsPathOfIncidence()
				.getAlpha();
		PathDescriptionEvaluator pathDescEval = (PathDescriptionEvaluator) greqlEvaluator
				.getVertexEvaluatorGraphMarker().getMark(p);
		long pathDescCosts = pathDescEval
				.getCurrentSubtreeEvaluationCosts(graphSize);

		long noOfStates = Math.round(logReader
				.getAvgResultSize("PathDescription"));
		if (noOfStates == 0) {
			noOfStates = defaultDfaStateNumber;
		}

		// We assume the costs for evaluating a DFA is the square of its number
		// of states + 50 base costs.
		long ownCosts = Math.round(1.0f * noOfStates * noOfStates) + 50;
		long iteratedCosts = ownCosts * e.getVariableCombinations(graphSize);
		long subtreeCosts = startCosts + targetCosts + pathDescCosts
				+ iteratedCosts;
		return new VertexCosts(ownCosts, iteratedCosts, subtreeCosts);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsSequentialPathDescription
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.SequentialPathDescriptionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsSequentialPathDescription(
			SequentialPathDescriptionEvaluator e, GraphSize graphSize) {
		return calculateCostsPathDescription(e, graphSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsSimplePathDescription
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.SimplePathDescriptionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsSimplePathDescription(
			SimplePathDescriptionEvaluator e, GraphSize graphSize) {
		return calculateCostsPathDescription(e, graphSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateCostsTransposedPathDescription
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.TransposedPathDescriptionEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public VertexCosts calculateCostsTransposedPathDescription(
			TransposedPathDescriptionEvaluator e, GraphSize graphSize) {
		return calculateCostsPathDescription(e, graphSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateSelectivityFunctionApplication
	 * (de.uni_koblenz.jgralab.greql2.evaluator
	 * .vertexeval.FunctionApplicationEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public double calculateSelectivityFunctionApplication(
			FunctionApplicationEvaluator e, GraphSize graphSize) {
		double loggedSelectivity = logReader.getAvgSelectivity(e
				.getLoggingName());
		Greql2Function func = e.getGreql2Function();
		double definedSelectiviy = func.getSelectivity();
		if ((loggedSelectivity > 0) && (loggedSelectivity < 1)) {
			return (loggedSelectivity * logScalingFactor)
					+ ((1 - logScalingFactor) * definedSelectiviy);
		}
		return definedSelectiviy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateSelectivityPathExistence
	 * (de.uni_koblenz.jgralab.greql2.evaluator.
	 * vertexeval.PathExistenceEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public double calculateSelectivityPathExistence(PathExistenceEvaluator e,
			GraphSize graphSize) {
		double loggedSelectivity = logReader.getAvgSelectivity(e
				.getLoggingName());
		if ((loggedSelectivity > 0) || (loggedSelectivity <= 1)) {
			return (loggedSelectivity * logScalingFactor)
					+ ((1 - logScalingFactor) * super
							.calculateSelectivityPathExistence(e, graphSize));
		}
		return super.calculateSelectivityPathExistence(e, graphSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.uni_koblenz.jgralab.greql2.evaluator.costmodel.CostModel#
	 * calculateSelectivityTypeId
	 * (de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.TypeIdEvaluator,
	 * de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize)
	 */
	@Override
	public double calculateSelectivityTypeId(TypeIdEvaluator e,
			GraphSize graphSize) {
		double loggedSelectivity = logReader.getAvgSelectivity(e
				.getLoggingName());
		if ((loggedSelectivity > 0) && (loggedSelectivity <= 1)) {
			return (loggedSelectivity * logScalingFactor)
					+ ((1 - logScalingFactor) * super
							.calculateSelectivityTypeId(e, graphSize));
		}
		return super.calculateSelectivityTypeId(e, graphSize);
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
		if (costModel instanceof LogCostModel) {
			return true;
		}
		return false;
	}

}
