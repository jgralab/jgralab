package de.uni_koblenz.jgralab.greql2.evaluator.costmodel;

import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.EdgeSetExpression;
import de.uni_koblenz.jgralab.greql2.schema.EdgeSubgraphExpression;
import de.uni_koblenz.jgralab.greql2.schema.VertexSetExpression;
import de.uni_koblenz.jgralab.greql2.schema.VertexSubgraphExpression;

/**
 * Base class for {@link CostModel}s. Holds constants that are meant to be used
 * in derived casses that implement {@link CostModel}.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public abstract class CostModelBase {

	/**
	 * describes, how much interpretation steps it takes to add a element to a
	 * bag
	 */
	protected static final int addToBagCosts = 10;

	/**
	 * describes, how much interpretation steps it takes to add a element to a
	 * set
	 */
	protected static final int addToSetCosts = 10;

	/**
	 * describes, how much interpretation steps it takes to add a element to a
	 * list
	 */
	protected static final int addToListCosts = 5;

	/**
	 * describes, how much interpretation steps it takes to add a element to a
	 * tuple
	 */
	protected static final int addToTupleCosts = 5;

	/**
	 * describes, how much interpretation steps it takes to add a element to a
	 * record
	 */
	protected static final int addToRecordCosts = 10;

	/**
	 * describes, how much interpretation steps it takes to do a regular
	 * pathsearch in relation to the size of the search automaton and the size
	 * of the datagraph.
	 */
	protected static final int searchFactor = 20;

	/**
	 * the default exponent that is used if the exponent of an exponentiated
	 * path description is not an integer literal but some complex expression
	 */
	protected static final int defaultExponent = 3;

	/**
	 * the default value that is estimated if the size of a listrange cannot be
	 * estimated
	 */
	protected static final int defaultListRangeSize = 50;

	/**
	 * the costs needed for constructing a new NFA out of several given NFAs.
	 */
	protected static final int defaultNfaConstructionCosts = 20;

	/**
	 * The default number of states a DFA has.
	 */
	protected static final int defaultDfaStateNumber = 15;

	/**
	 * the costs to create one transition
	 */
	protected static final int transitionCosts = 10;

	/**
	 * the costs that a typeId evaluation causes for each defined type
	 */
	protected static final int typeIdCosts = 5;

	/**
	 * A factor that will be multiplied with the number of variable combinations
	 * to estimate the own costs of a {@link Declaration}.
	 */
	protected static final int declarationCostsFactor = 5;

	/**
	 * A factor that will be multiplied with the number of edges of the
	 * datagraph to estimate the own costs of a {@link EdgeSetExpression}.
	 */
	protected static final int edgeSetExpressionCostsFactor = 3;

	/**
	 * A factor that will be multiplied with the number of vertices of the
	 * datagraph to estimate the own costs of a {@link VertexSetExpression}.
	 */
	protected static final int vertexSetExpressionCostsFactor = 3;

	/**
	 * A factor that will be multiplied with the number of edges of the
	 * datagraph to estimate the own costs of a {@link EdgeSubgraphExpression}.
	 */
	protected static final int edgeSubgraphExpressionCostsFactor = 3;

	/**
	 * A factor that will be multiplied with the number of vertices of the
	 * datagraph to estimate the own costs of a {@link VertexSubgraphExpression}
	 * .
	 */
	protected static final int vertexSubgraphExpressionCostsFactor = 3;

	protected static final int greql2ExpressionCostsFactor = 3;

	protected static final int definitionExpressionCostsFactor = 2;

	/**
	 * Stores a reference to the {@link GreqlEvaluator} that will evaluate the
	 * query
	 */
	protected GreqlEvaluator greqlEvaluator;
}