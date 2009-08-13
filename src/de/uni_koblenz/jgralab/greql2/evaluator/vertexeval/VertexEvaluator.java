/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
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

package de.uni_koblenz.jgralab.greql2.evaluator.vertexeval;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphMarker;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.VariableDeclaration;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.VertexCosts;
import de.uni_koblenz.jgralab.greql2.evaluator.logging.EvaluationLogger;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.IncompleteVertexEvaluatorException;
import de.uni_koblenz.jgralab.greql2.exception.QuerySourceException;
import de.uni_koblenz.jgralab.greql2.exception.UnknownVertexException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueTypeCollection;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Aggregation;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.SourcePosition;
import de.uni_koblenz.jgralab.greql2.schema.Variable;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * This is the base class for all VertexEvaluators which evaluate the vertices
 * in the GReQL Syntaxgraph
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public abstract class VertexEvaluator {
	/**
	 * This classes get not evaluated
	 */
	private static ArrayList<String> unevaluatedVertices;

	/**
	 * constructs a the list of vertex classes that shouldn't get evaluated as
	 * soon as the class is loaded
	 */
	static {
		unevaluatedVertices = new ArrayList<String>();
		unevaluatedVertices.add("Quantifier");
		unevaluatedVertices.add("RoleId");
		unevaluatedVertices.add("FunctionId");
		unevaluatedVertices.add("RecordId");
		unevaluatedVertices.add("Direction");
	}

	/**
	 * A reference to the datagraph
	 */
	protected Graph graph = null;

	/**
	 * used only for debugging, the indentation of the debug-messages on stdout
	 * for this vertexeval
	 */
	protected static int currentIndentation = 0;

	/**
	 * The GreqlEvaluator this VertexEvaluator belongs to
	 */
	protected GreqlEvaluator greqlEvaluator = null;

	/**
	 * The evaluation logger that should be used for logging
	 */
	protected EvaluationLogger evaluationLogger;

	/**
	 * The costs for the current evaluation of the whole subtree in the abstract
	 * measurement unit "interpretation steps"
	 */
	protected long currentSubtreeEvaluationCosts = Long.MIN_VALUE;

	/**
	 * The costs for the evaluation of the whole subtree for the first time
	 */
	protected long initialSubtreeEvaluationCosts = Long.MIN_VALUE;

	/**
	 * The costs for <b>one</b> evaluation of this vertex _without_ the costs of
	 * the evaluation of the subtrees
	 */
	protected long ownEvaluationCosts = Long.MIN_VALUE;

	/**
	 * The costs for all evaluations of this vertex for all variable
	 * combinations <b>without</b> the costs for the subtree evaluation
	 */
	protected long iteratedEvaluationCosts = Long.MIN_VALUE;

	/**
	 * The expected cardinality of the evaluation result this evaluator creates
	 */
	protected long estimatedCardinality = Long.MIN_VALUE;

	/**
	 * The expected selectivity of this vertexevaluator
	 */
	protected double estimatedSelectivity = Double.NaN;

	/**
	 * The GraphSize-Object for which the costs have been evaluated the last
	 * time
	 */
	protected GraphSize costsGraphSize = null;

	/**
	 * The evaluation result
	 */
	protected JValue result = null;

	/**
	 * The subgraph which was uses for the last evaluation and should be used
	 * for the next evaluation
	 */
	protected BooleanGraphMarker subgraph = null;

	/**
	 * The set of variables this vertex depends on
	 */
	protected Set<Variable> neededVariables = null;

	/**
	 * The set of variables this vertex defines and that are valid in all
	 * subgraphs
	 */
	protected Set<Variable> definedVariables = null;

	/**
	 * @param eval
	 *            the GreqlEvaluator this VertexEvaluator belongs to
	 */
	protected VertexEvaluator(GreqlEvaluator eval) {
		greqlEvaluator = eval;
		graph = eval.getDatagraph();
		evaluationLogger = eval.getEvaluationLogger();
	}

	/**
	 * @return the datagraph which gets evaluated
	 */
	protected Graph getDatagraph() {
		return greqlEvaluator.getDatagraph();
	}

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	public abstract Vertex getVertex();

	/**
	 * @return the name of the associated {@link Greql2Vertex} used for logging.
	 *         By default this is the type name, i.e. BagComprehension, but
	 *         subclasses may override this method to get a more finegrained
	 *         control. For example {@link FunctionApplicationEvaluator}s use
	 *         the function name of the corresponding function for logging.
	 */
	public String getLoggingName() {
		return this.getVertex().getAttributedElementClass().getSimpleName();
	}

	/**
	 * Gets the result of the evaluation of this vertex on the given subgraph
	 * 
	 * @param subgraphMarker
	 *            the subgraph to evaluate the vertex on or null if it should be
	 *            evaluated on the whole datagraph
	 * @return the evaluation result
	 */
	public JValue getResult(BooleanGraphMarker subgraphMarker)
			throws EvaluateException {
		if ((result != null) && (this.subgraph == subgraphMarker)) {
			// greqlEvaluator.progress(1);
			return result;
		}
		// currentIndentation++;
		// printIndentation();
		// GreqlEvaluator.println("Evaluating : " + this);
		this.subgraph = subgraphMarker;
		try {
			result = evaluate();
			// System.out.println("VertexEvaluator.getResult() " + result
			// + " of vertex " + getVertex());
		} catch (QuerySourceException ex) {
			removeInvalidSourcePosition(ex);
			throw ex;
		}

		// Logging...
		if ((evaluationLogger != null) && (result != null)) {
			if (result.isBoolean()) {
				// Log the selectivity for vertices that return a boolean
				Boolean bool = result.toBoolean();
				evaluationLogger.logSelectivity(getLoggingName(),
						(bool != null) && bool.booleanValue());
			} else if (result.isJValueTypeCollection()) {
				// Log the selectivity for TypeId vertices
				JValueTypeCollection col = result.toJValueTypeCollection();
				AttributedElementClass aec = null;
				if (col.getAllowedTypes().iterator().hasNext()) {
					aec = col.getAllowedTypes().iterator().next();
				} else {
					aec = col.getForbiddenTypes().iterator().next();
				}
				if (aec.isSubClassOf(aec.getSchema().getAttributedElementClass(
						"Vertex"))) {
					// The typeId restricts vertex classes
					for (VertexClass vc : greqlEvaluator.getDatagraph()
							.getSchema().getVertexClassesInTopologicalOrder()) {
						evaluationLogger.logSelectivity(getLoggingName(), col
								.acceptsType(vc));
					}
				} else {
					// The typeId restricts edge classes
					for (EdgeClass ec : greqlEvaluator.getDatagraph()
							.getSchema().getEdgeClassesInTopologicalOrder()) {
						evaluationLogger.logSelectivity(getLoggingName(), col
								.acceptsType(ec));
					}
				}
			}

			// Log the size of the result
			if (result.isCollection()) {
				if (this instanceof SimpleDeclarationEvaluator) {
					int size = 1;
					for (JValue val : result.toJValueList()) {
						VariableDeclaration d = val.toVariableDeclaration();
						size *= d.getDefinitionCardinality();
					}
					evaluationLogger.logResultSize(getLoggingName(), size);
				} else {
					evaluationLogger.logResultSize(getLoggingName(), result
							.toCollection().size());
				}
			} else if (result.isDeclarationLayer()) {
				// Declarations return a VariableDeclarationLayer object as
				// result. The real result size is the number of possible
				// variable combinations. This cannot be logged here, but it
				// is done in DeclarationLayer itself.
			} else if (result.isDFA() || result.isNFA()) {
				// Result sizes for PathDescriptions are logged as the
				// number of states the DFA has. That is done in
				// Forward-/BackwardVertexSet and PathExistance.
			} else {
				evaluationLogger.logResultSize(getLoggingName(), 1);
			}
		}

		// printIndentation();
		// GreqlEvaluator.println("Evaluating : " + this + " finished");
		// currentIndentation--;
		// GreqlEvaluator.println("Result is: " + result);

		greqlEvaluator.progress(ownEvaluationCosts);
		return result;
	}

	/**
	 * @return true, if this expression has already been evaluated, useful
	 *         mostly for debugging
	 */
	public boolean isEvaluated() {
		return (result != null);
	}

	/**
	 * this method does the evaluation. It must be implemented by concrete
	 * evaluators
	 */
	public abstract JValue evaluate() throws EvaluateException;

	/**
	 * clears the evaluation result
	 */
	public final void clear() {
		result = null;
	}

	/**
	 * resets the VertexEvaluators internal state (evaluation result, costs,
	 * etc) to the initial one, that means, sets all variables of this
	 * vertexevaluator to values, that it is in the same state like it was
	 * directly after creation
	 */
	public void resetToInitialState() {
		result = null;
		currentSubtreeEvaluationCosts = Long.MIN_VALUE;
		initialSubtreeEvaluationCosts = Long.MIN_VALUE;
		ownEvaluationCosts = Long.MIN_VALUE;
		iteratedEvaluationCosts = Long.MIN_VALUE;
		estimatedCardinality = Long.MIN_VALUE;
		estimatedSelectivity = Double.NaN;
		costsGraphSize = null;
		subgraph = null;
	}

	public void resetSubtreeToInitialState() {
		resetToInitialState();
		GraphMarker<VertexEvaluator> marker = greqlEvaluator
				.getVertexEvaluatorGraphMarker();
		for (Edge e : getVertex().incidences(EdgeDirection.IN)) {
			Vertex vertex = e.getThat();
			VertexEvaluator eval = marker.getMark(vertex);
			if (eval != null) {
				eval.resetSubtreeToInitialState();
			}
		}
	}

	/**
	 * This method must be overwritten by every subclass. It should call the
	 * right method of the GreqlEvaluators costmodel.
	 * 
	 * @return a 3-Tupel (ownCosts, iteratedCosts, subtreeCosts) of costs the
	 *         evaluation of the subtree with this vertex as root causes
	 */
	protected abstract VertexCosts calculateSubtreeEvaluationCosts(
			GraphSize graphSize);

	/**
	 * Calculates the costs the current evaluation of the subtree causes. These
	 * cost differ from the initialEvaluationCosts, because only for the first
	 * evaluation, the result really gets evaluated, for all other evaluations,
	 * the evaluated result only gets copied, these costs are 1
	 * 
	 * @return the costs of this evaluation of the subtree the vertex this
	 *         evaluator evaluates is root of
	 */
	public long getCurrentSubtreeEvaluationCosts(GraphSize graphSize) {
		if (currentSubtreeEvaluationCosts == Long.MIN_VALUE) {
			return getInitialSubtreeEvaluationCosts(graphSize);
		} else {
			return 1;
		}
	}

	/**
	 * Calculates the costs the first evaluation of the subtree causes. These
	 * cost differ from the second "evaluation", because for the second one, the
	 * already evaluated result only gets copied, these costs are 1
	 * 
	 * @return the costs of the first evaluation of the subgraph the vertex this
	 *         evaluator evaluates is root of
	 */
	public long getInitialSubtreeEvaluationCosts(GraphSize graphSize) {
		if ((costsGraphSize == graphSize)
				&& (initialSubtreeEvaluationCosts > 0)) {
			return initialSubtreeEvaluationCosts;
		} else {
			costsGraphSize = graphSize;
			VertexCosts costs = calculateSubtreeEvaluationCosts(graphSize);
			this.ownEvaluationCosts = costs.ownEvaluationCosts;
			this.iteratedEvaluationCosts = costs.iteratedEvaluationCosts;
			this.currentSubtreeEvaluationCosts = costs.subtreeEvaluationCosts;
			this.initialSubtreeEvaluationCosts = costs.subtreeEvaluationCosts;
			return this.initialSubtreeEvaluationCosts;
		}
	}

	/**
	 * Get the costs for evaluating the associated vertex one time. No subtree
	 * or iteration costs are taken into account.
	 * 
	 * @param graphSize
	 *            a {@link GraphSize} object indicating the size of the data-
	 *            {@link Graph}
	 * @return the costs for evaluating the associated vertex one time excluding
	 *         subtree and iteration costs
	 */
	public long getOwnEvaluationCosts(GraphSize graphSize) {
		if (ownEvaluationCosts == Long.MIN_VALUE) {
			// call for side-effects
			getInitialSubtreeEvaluationCosts(graphSize);
		}
		return ownEvaluationCosts;
	}

	/**
	 * calculate the set of needed and defined variables
	 */
	public void calculateNeededAndDefinedVariables() {
		neededVariables = new HashSet<Variable>();
		definedVariables = new HashSet<Variable>();
		Edge inc = getVertex().getFirstEdge(EdgeDirection.IN);
		while (inc != null) {
			VertexEvaluator veval = greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(inc.getAlpha());
			if (veval != null) {
				neededVariables.addAll(veval.getNeededVariables());
				definedVariables.addAll(veval.getDefinedVariables());
			}
			inc = inc.getNextEdge(EdgeDirection.IN);
		}
		HashSet<Variable> bothVariables = new HashSet<Variable>();
		bothVariables.addAll(neededVariables);
		neededVariables.removeAll(definedVariables);
		definedVariables.removeAll(bothVariables);
	}

	/**
	 * Calculates the set of variables this vertex depends on
	 * 
	 * @return the set of variables this vertex depends on
	 */
	public Set<Variable> getNeededVariables() {
		if (neededVariables == null) {
			calculateNeededAndDefinedVariables();
		}
		return neededVariables;
	}

	/**
	 * Calculates the set of variables this vertex (or even a vertex in a
	 * subgraph) defines and that is valid in the whole subtree with this vertex
	 * as head.
	 * 
	 * @return the set of variables this vertex defines and that are valid
	 */
	public Set<Variable> getDefinedVariables() {
		if (definedVariables == null) {
			calculateNeededAndDefinedVariables();
		}
		return definedVariables;
	}

	/**
	 * Returns the number of combinations of the variables this vertex depends
	 * on
	 */
	public long getVariableCombinations(GraphSize graphSize) {
		int combinations = 1;
		Iterator<Variable> iter = getNeededVariables().iterator();
		while (iter.hasNext()) {
			VariableEvaluator veval = (VariableEvaluator) greqlEvaluator
					.getVertexEvaluatorGraphMarker().getMark(iter.next());
			// combinations *= veval.getEstimatedCardinality(graphSize);
			combinations *= veval.getVariableCombinations(graphSize);
		}
		return combinations;
	}

	/**
	 * returns the estimated size of the result.
	 */
	public long getEstimatedCardinality(GraphSize graphSize) {
		if (estimatedCardinality == Long.MIN_VALUE) {
			estimatedCardinality = calculateEstimatedCardinality(graphSize);
		}
		return estimatedCardinality;
	}

	/**
	 * calculates the estimated cardinality of the evaluationeresult this
	 * vertexevaluator creates. By default, this size is 1, if a VertexEvaluator
	 * has bigger resultsizes, it should override this method
	 */
	public long calculateEstimatedCardinality(GraphSize graphSize) {
		return 1;
	}

	/**
	 * returns the estimated selectivity of the vertex evaluation.
	 */
	public double getEstimatedSelectivity(GraphSize graphSize) {
		if (Double.isNaN(estimatedSelectivity)) {
			estimatedSelectivity = calculateEstimatedSelectivity(graphSize);
		}
		return estimatedSelectivity;
	}

	/**
	 * calculates the estimated selectivity for this vertex.By default, this is
	 * 1, if a VertexEvaluator has an other selectivity, it should override this
	 * method
	 */
	public double calculateEstimatedSelectivity(GraphSize graphSize) {
		return 1;
	}

	/**
	 * creates a list of possible source positions for the current vertex
	 */
	public List<SourcePosition> createPossibleSourcePositions() {
		Greql2Aggregation inc = (Greql2Aggregation) getVertex().getFirstEdge(
				EdgeDirection.OUT);
		List<SourcePosition> possibleSourcePositions = new ArrayList<SourcePosition>();
		while (inc != null) {
			List<SourcePosition> sourcePositions = inc.getSourcePositions();
			possibleSourcePositions.addAll(sourcePositions);
			inc = inc.getNextGreql2Aggregation(EdgeDirection.OUT);
		}
		return possibleSourcePositions;
	}

	/**
	 * creates the sourcepositions for the given edge
	 */
	protected List<SourcePosition> createSourcePositions(Greql2Aggregation edge) {
		List<SourcePosition> possibleSourcePositions = new ArrayList<SourcePosition>();
		List<SourcePosition> sourcePositions = edge.getSourcePositions();
		possibleSourcePositions.addAll(sourcePositions);
		return possibleSourcePositions;
	}

	/**
	 * eliminates all sourcepoistions at the given exception, that are not
	 * possible if the vertex, that throwed the exception, was accessed via this
	 * vertex
	 */
	private void removeInvalidSourcePosition(QuerySourceException ex) {
		Greql2Aggregation inc = (Greql2Aggregation) getVertex().getFirstEdge(
				EdgeDirection.OUT);
		List<SourcePosition> possibleSourcePositions = new ArrayList<SourcePosition>();
		while (inc != null) {
			List<SourcePosition> sourcePositions = inc.getSourcePositions();
			possibleSourcePositions.addAll(sourcePositions);
			inc = inc.getNextGreql2Aggregation(EdgeDirection.OUT);
		}
		if (possibleSourcePositions.size() == 0) {
			return; // maybe the vertex is the root vertex, than it has no
		}
		Iterator<SourcePosition> iter = ex.getSourcePositions().iterator();
		while (iter.hasNext()) {
			boolean accepted = false;
			SourcePosition currentPosition = iter.next();
			Iterator<SourcePosition> availableIter = possibleSourcePositions
					.iterator();
			while (availableIter.hasNext()) {
				SourcePosition availablePosition = availableIter.next();
				if ((availablePosition.offset <= currentPosition.offset)
						&& (availablePosition.offset + availablePosition.length >= currentPosition.offset
								+ currentPosition.length)) {
					accepted = true;
					break;
				}
			}
			if (!accepted) {
				// GreqlEvaluator.println("SourcePosition: (" +
				// currentPosition.offset + ", " + currentPosition.length + ")
				// is not accepted");
				iter.remove();
			}
		}

	}

	/**
	 * creates a vertex evaluator for the given vertex
	 */
	@SuppressWarnings("unchecked")
	public static VertexEvaluator createVertexEvaluator(Vertex vertex,
			GreqlEvaluator eval) throws EvaluateException {
		Class vertexClass = vertex.getClass();
		String fullClassName = vertexClass.getName();
		// remove the "Impl" ...
		fullClassName = fullClassName.substring(0, fullClassName.length() - 4);
		fullClassName = fullClassName.replaceFirst(".impl.", ".");
		// remove the packages
		String className = fullClassName.substring(fullClassName
				.lastIndexOf(".") + 1);

		if (unevaluatedVertices.contains(className)) {
			return null;
		}
		String evalName = className + "Evaluator";
		evalName = evalName.substring(className.lastIndexOf(".") + 1);
		evalName = VertexEvaluator.class.getPackage().getName() + "."
				+ evalName;
		try {
			Class argsClass[] = new Class[] { Class.forName(fullClassName),
					GreqlEvaluator.class };
			Class evalClass = Class.forName(evalName);
			Constructor constructor = evalClass.getConstructor(argsClass);
			VertexEvaluator vertexEval = (VertexEvaluator) constructor
					.newInstance(vertex, eval);
			return vertexEval;
		} catch (ClassNotFoundException ex) {
			throw new UnknownVertexException(className, ex);
		} catch (NoSuchMethodException ex) {
			throw new IncompleteVertexEvaluatorException(className, ex);
		} catch (IllegalAccessException ex) {
			throw new IncompleteVertexEvaluatorException(className, ex);
		} catch (InstantiationException ex) {
			throw new IncompleteVertexEvaluatorException(className, ex);
		} catch (InvocationTargetException ex) {
			throw new IncompleteVertexEvaluatorException(className, ex);
		}
	}

}
