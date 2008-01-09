package de.uni_koblenz.jgralab.greql2.evaluator.costmodel;

import de.uni_koblenz.jgralab.GraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;

/**
 * Base class for {@link CostModel}s. Holds constants that are meant to be used
 * in derived casses that implement {@link CostModel}.
 * 
 * @author Tassilo Horn (heimdall), 2007, Diploma Thesis
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
	 * Stores a reference to the GraphMarker that holds the VertexEvaluator
	 * object
	 */
	protected GraphMarker<VertexEvaluator> vertexEvalMarker;
}