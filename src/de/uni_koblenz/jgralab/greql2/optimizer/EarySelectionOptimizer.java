/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.optimizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.exception.OptimizerException;
import de.uni_koblenz.jgralab.greql2.schema.Comprehension;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.FunctionId;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.Identifier;
import de.uni_koblenz.jgralab.greql2.schema.IsArgumentOf;
import de.uni_koblenz.jgralab.greql2.schema.IsConstraintOf;
import de.uni_koblenz.jgralab.greql2.schema.IsDeclaredVarOf;
import de.uni_koblenz.jgralab.greql2.schema.QuantifiedExpression;
import de.uni_koblenz.jgralab.greql2.schema.RecordConstruction;
import de.uni_koblenz.jgralab.greql2.schema.RecordElement;
import de.uni_koblenz.jgralab.greql2.schema.RecordId;
import de.uni_koblenz.jgralab.greql2.schema.SetComprehension;
import de.uni_koblenz.jgralab.greql2.schema.SimpleDeclaration;
import de.uni_koblenz.jgralab.greql2.schema.Variable;

/**
 * This {@link Optimizer} implements the transformation "Selection as early as
 * possible".
 * 
 * @author Tassilo Horn (heimdall), 2007, Diploma Thesis
 * 
 */
public class EarySelectionOptimizer extends OptimizerBase {

	private Greql2 syntaxgraph;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.optimizer.Optimizer#isEquivalent(de.uni_koblenz.jgralab.greql2.optimizer.Optimizer)
	 */
	@Override
	public boolean isEquivalent(Optimizer optimizer) {
		if (optimizer instanceof EarySelectionOptimizer) {
			return true;
		}
		return false;
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
		this.syntaxgraph = syntaxgraph;

		int noOfRuns = 1;
		while (runOptimization()) {
			if (printMessages) {
				GreqlEvaluator.println(optimizerHeaderString() + "Iteration "
						+ noOfRuns + " finished.  Restarting...");
			}

			// printGraphAsDot(syntaxgraph, "sg-after-" + noOfRuns +
			// "-iterations");
			noOfRuns++;
		}

		if (noOfRuns > 1 && printMessages) {
			// We want no output if that optimizer didn't do anything.
			GreqlEvaluator.println(optimizerHeaderString() + "finished after "
					+ noOfRuns + " runs.");
		}

		OptimizerUtility.createMissingSourcePositions(syntaxgraph);

		try {
			eval.createVertexEvaluators();
		} catch (EvaluateException e) {
			e.printStackTrace();
		}

		// If there was more than one optimization run, a transformation was
		// done.
		return noOfRuns > 1;
	}

	/**
	 * Do an optimization run.
	 * 
	 * @throws OptimizerException
	 */
	private boolean runOptimization() throws OptimizerException {
		HashMap<SimpleDeclaration, Set<Expression>> movableExpressions = new HashMap<SimpleDeclaration, Set<Expression>>();

		// find all movable constraints in all Declarations
		for (Declaration decl : syntaxgraph.getDeclarationVertices()) {
			IsConstraintOf isConst = decl
					.getFirstIsConstraintOf(EdgeDirection.IN);
			while (isConst != null) {
				Expression exp = (Expression) isConst.getAlpha();
				HashMap<SimpleDeclaration, Set<Expression>> movableExpressionsOfDecl = collectMovableExpressions(exp);

				for (SimpleDeclaration sd : movableExpressionsOfDecl.keySet()) {
					if (movableExpressions.containsKey(sd)) {
						movableExpressions.get(sd).addAll(
								movableExpressionsOfDecl.get(sd));
					} else {
						movableExpressions.put(sd, movableExpressionsOfDecl
								.get(sd));
					}
				}
				isConst = isConst.getNextIsConstraintOf(EdgeDirection.IN);
			}
		}

		// now perform the needed transformations
		boolean aTransformationWasDone = false;
		List<SimpleDeclaration> simpleDeclsWithMovableExpressions = new ArrayList<SimpleDeclaration>(
				movableExpressions.keySet());

		// Sort the list of SDs with associated movable expressions to handle
		// this case: A predicate P is moved, and its nodes are copied. But P
		// contains another declaration (e.g. in a QuantifiedExpression) which
		// can be optimized, too. But due to the copying its simple
		// declarations have another ID now, but movableExpressions
		// contains the old SD. By moving expressions to their SD in a bottom-up
		// order, we omit this problem.
		Collections.sort(simpleDeclsWithMovableExpressions,
				new Comparator<SimpleDeclaration>() {

					@Override
					public int compare(SimpleDeclaration sd1,
							SimpleDeclaration sd2) {
						Declaration decl1 = (Declaration) sd1
								.getFirstIsSimpleDeclOf().getOmega();
						Declaration decl2 = (Declaration) sd2
								.getFirstIsSimpleDeclOf().getOmega();
						if (OptimizerUtility.isAbove(decl1, decl2))
							return 1;
						if (OptimizerUtility.isAbove(decl2, decl1))
							return -1;
						return 0;
					}
				});

		for (SimpleDeclaration sd : simpleDeclsWithMovableExpressions) {
			Declaration parentDecl = (Declaration) sd.getFirstIsSimpleDeclOf()
					.getOmega();
			Set<Variable> varsDeclaredBySd = OptimizerUtility
					.collectVariablesDeclaredBy(sd);
			// Check if there's a predicate needing only part of the variables
			// the SimpleDeclaration declares and one that needs all of them.
			boolean foundPredicateNeedingAllVars = false, foundPredNeedingPartOfVars = false;
			Set<Variable> varsMaybeToSplitOut = new HashSet<Variable>();
			for (Expression pred : movableExpressions.get(sd)) {
				Set<Variable> neededLocalVars = collectNeededLocalVariables(pred);
				if (neededLocalVars.size() < varsDeclaredBySd.size()) {
					foundPredNeedingPartOfVars = true;
					if (varsMaybeToSplitOut.size() < neededLocalVars.size()) {
						// Prefer splits with higher number of variables. That
						// makes the Mn transformation possible more often.
						varsMaybeToSplitOut = neededLocalVars;
					}
				} else {
					foundPredicateNeedingAllVars = true;
				}
			}
			List<SimpleDeclaration> sdsOfParentDecl = OptimizerUtility
					.collectSimpleDeclarationsOf(parentDecl);

			// If there's such a predicate that needs only part of the variables
			// then split it, if there was no predicate that uses all variables
			// found (in that case the Mn rule has to be done first) or if the
			// current SimpleDeclaration is the only one of the parent
			// Declaration.
			if (foundPredNeedingPartOfVars
					&& (!foundPredicateNeedingAllVars || sdsOfParentDecl.size() == 1)) {
				splitSimpleDeclaration(sd, varsMaybeToSplitOut);
				aTransformationWasDone = true;
			} else {
				// Ok, no split could be done, so use the move rules.
				if (varsDeclaredBySd.size() == 1) {
					movePredicatesToOneVarSimpleDeclaration(sd,
							movableExpressions.get(sd), varsDeclaredBySd);
					aTransformationWasDone = true;
				} else {
					// The Mn rule may only be performed if there are more
					// SimpleDeclarations below the current SD's parent
					// Declaration, else it would loop forever.
					if (sdsOfParentDecl.size() > 1) {
						movePredicatesToMultiVarSimpleDeclaration(sd,
								movableExpressions.get(sd), varsDeclaredBySd);
						aTransformationWasDone = true;
					}
				}
			}
		}

		return aTransformationWasDone;
	}

	/**
	 * Split the given {@link SimpleDeclaration} so that there's one
	 * {@link SimpleDeclaration} that declares the {@link Variable}s in
	 * <code>varsToBeSplit</code> and one for the rest.
	 * 
	 * @param sd
	 *            the {@link SimpleDeclaration} to be split
	 * @param varsToBeSplit
	 *            a {@link Set} of {@link Variable}s that should have their own
	 *            {@link SimpleDeclaration}
	 */
	private void splitSimpleDeclaration(SimpleDeclaration sd,
			Set<Variable> varsToBeSplit) {
		Set<Variable> varsDeclaredBySD = OptimizerUtility
				.collectVariablesDeclaredBy(sd);

		if (printMessages) {
			GreqlEvaluator.println(optimizerHeaderString() + "(S) Splitting out "
					+ varsToBeSplit + " of " + sd + " that declares "
					+ varsDeclaredBySD);
		}

		if (varsDeclaredBySD.size() == varsToBeSplit.size()) {
			// there's nothing to split out anymore
			return;
		}
		Declaration parentDecl = (Declaration) sd.getFirstIsSimpleDeclOf(
				EdgeDirection.OUT).getOmega();
		SimpleDeclaration newSD = syntaxgraph.createSimpleDeclaration();
		syntaxgraph.createIsSimpleDeclOf(newSD, parentDecl);
		syntaxgraph.createIsTypeExprOfDeclaration((Expression) sd
				.getFirstIsTypeExprOfDeclaration(EdgeDirection.IN).getAlpha(),
				newSD);

		for (Variable var : varsToBeSplit) {
			IsDeclaredVarOf inc = sd.getFirstIsDeclaredVarOf(EdgeDirection.IN);
			HashSet<IsDeclaredVarOf> relinkIncs = new HashSet<IsDeclaredVarOf>();
			while (inc != null) {
				if (inc.getAlpha() == var) {
					// This inc is now declared by newSD, so we need to relink
					// the edge.
					relinkIncs.add(inc);
				}
				inc = inc.getNextIsDeclaredVarOf(EdgeDirection.IN);
			}
			for (IsDeclaredVarOf relinkEdge : relinkIncs) {
				relinkEdge.setOmega(newSD);
			}
		}
	}

	private void movePredicatesToMultiVarSimpleDeclaration(
			SimpleDeclaration origSD, Set<Expression> predicates,
			Set<Variable> varsDeclaredByOrigSD) throws OptimizerException {

		if (printMessages) {
			GreqlEvaluator.print(optimizerHeaderString()
					+ "(Mn) Performing early selection transformation for "
					+ origSD + " declaring ");

			int varsSize = varsDeclaredByOrigSD.size();
			int i = 1;
			for (Variable var : varsDeclaredByOrigSD) {
				GreqlEvaluator.print(var + " (" + var.getName() + ")");
				if (i < varsSize)
					GreqlEvaluator.print(", ");
				i++;
			}
			GreqlEvaluator.println(" with predicates " + predicates + ".");
		}

		// First we search the edges that are connected to each variable in
		// the result definition or bound expression of the parent
		// comprehension or quantified expression that have to be relinked
		// to the record access funApp later.
		HashMap<Variable, Set<Edge>> varEdgeMap = new HashMap<Variable, Set<Edge>>();
		Declaration parentDeclOfOrigSD = (Declaration) origSD
				.getFirstIsSimpleDeclOf().getOmega();
		Expression parentComprOrQuantExpr = (Expression) parentDeclOfOrigSD
				.getFirstEdge(EdgeDirection.OUT).getOmega();
		Edge targetEdge = null;
		if (parentComprOrQuantExpr instanceof Comprehension) {
			targetEdge = ((Comprehension) parentComprOrQuantExpr)
					.getFirstIsCompResultDefOf(EdgeDirection.IN);
		} else {
			targetEdge = ((QuantifiedExpression) parentComprOrQuantExpr)
					.getFirstIsBoundExprOf(EdgeDirection.IN);
		}
		for (Variable var : varsDeclaredByOrigSD) {
			varEdgeMap.put(var, collectEdgesComingFrom(var, targetEdge));
		}

		// this will be the result definition of the inner Comprehension
		RecordConstruction newOuterRecord = syntaxgraph
				.createRecordConstruction();
		StringBuilder newOuterVarName = new StringBuilder();
		for (Variable var : varsDeclaredByOrigSD) {
			// The new outer Record-variable is named as concatenation of
			// all variable names.
			newOuterVarName.append(var.getName());
			RecordElement recElem = syntaxgraph.createRecordElement();
			syntaxgraph.createIsRecordElementOf(recElem, newOuterRecord);
			RecordId recId = syntaxgraph.createRecordId();
			recId.setName(var.getName());
			syntaxgraph.createIsRecordIdOf(recId, recElem);
			syntaxgraph.createIsRecordExprOf(var, recElem);
		}

		// create the new outer record variable
		Variable newOuterRecordVar = syntaxgraph.createVariable();
		newOuterRecordVar.setName(newOuterVarName.toString());

		// We create a new SimpleDeclaration that will be used as new outer
		// SimpleDeclaration
		SimpleDeclaration newOuterSD = syntaxgraph.createSimpleDeclaration();
		syntaxgraph.createIsSimpleDeclOf(newOuterSD, parentDeclOfOrigSD);

		// The new outer SimpleDeclaration declares the record variable
		syntaxgraph.createIsDeclaredVarOf(newOuterRecordVar, newOuterSD);

		// Create a new inner SetComprehension as type expression of the new
		// outer SimpleDeclaration
		SetComprehension newInnerCompr = syntaxgraph.createSetComprehension();
		syntaxgraph.createIsTypeExprOfDeclaration(newInnerCompr, newOuterSD);
		Declaration newInnerDecl = syntaxgraph.createDeclaration();
		syntaxgraph.createIsCompDeclOf(newInnerDecl, newInnerCompr);
		syntaxgraph.createIsCompResultDefOf(newOuterRecord, newInnerCompr);
		origSD.getFirstIsSimpleDeclOf(EdgeDirection.OUT).setOmega(newInnerDecl);

		Expression newCombinedConstraint = createConjunction(
				new ArrayList<Expression>(predicates), new HashSet<Variable>());
		syntaxgraph.createIsConstraintOf(newCombinedConstraint, newInnerDecl);

		// printGraphAsDot(syntaxgraph, "before-deleting.");

		for (Expression pred : predicates) {
			removeExpressionFromOriginalConstraint(pred, parentDeclOfOrigSD);
		}

		// Collect the relinkable edges that are in the constraints of the
		// parent outer declaration.
		for (Variable var : varsDeclaredByOrigSD) {
			IsConstraintOf inc = parentDeclOfOrigSD
					.getFirstIsConstraintOf(EdgeDirection.IN);
			while (inc != null) {
				varEdgeMap.get(var).addAll(collectEdgesComingFrom(var, inc));
				inc = inc.getNextIsConstraintOf(EdgeDirection.IN);
			}
		}

		// at last set the edges that connected to the original variables at
		// the outer scope to a record access function
		for (Variable var : varEdgeMap.keySet()) {
			FunctionApplication funApp = syntaxgraph
					.createFunctionApplication();
			FunctionId funId = OptimizerUtility.findOrCreateFunctionId(
					"getValue", syntaxgraph);
			syntaxgraph.createIsFunctionIdOf(funId, funApp);
			Identifier identifier = syntaxgraph.createIdentifier();
			identifier.setName(var.getName());
			syntaxgraph.createIsArgumentOf(newOuterRecordVar, funApp);
			syntaxgraph.createIsArgumentOf(identifier, funApp);
			// now reset all old outgoing edges of the variable to the new
			// funApp
			for (Edge edge : varEdgeMap.get(var)) {
				edge.setAlpha(funApp);
			}
		}
	}

	private void movePredicatesToOneVarSimpleDeclaration(
			SimpleDeclaration origSD, Set<Expression> predicates,
			Set<Variable> varsDeclaredByOrigSD) throws OptimizerException {
		Variable var = varsDeclaredByOrigSD.iterator().next();
		if (printMessages) {
			GreqlEvaluator.println(optimizerHeaderString()
					+ "(M1) Performing early selection transformation for "
					+ origSD + " declaring variable " + var + " ("
					+ var.getName() + ") with predicates " + predicates);
		}

		// Create the new vertices
		Expression newCombinedConstraint = createConjunction(
				new ArrayList<Expression>(predicates), varsDeclaredByOrigSD);
		SetComprehension newSetComp = syntaxgraph.createSetComprehension();
		Declaration newDecl = syntaxgraph.createDeclaration();
		SimpleDeclaration newInnerSD = syntaxgraph.createSimpleDeclaration();
		Set<Variable> undeclaredVars = collectUndeclaredVariablesBelow(newCombinedConstraint);
		if (undeclaredVars.size() != 1) {
			throw new OptimizerException("undeclaredVars = " + undeclaredVars
					+ " has size different form 1.");
		}
		Variable newInnerVar = undeclaredVars.iterator().next();

		// Connect the edges
		origSD.getFirstIsTypeExprOf(EdgeDirection.IN).setOmega(newInnerSD);
		syntaxgraph.createIsTypeExprOfDeclaration(newSetComp, origSD);
		syntaxgraph.createIsCompDeclOf(newDecl, newSetComp);
		syntaxgraph.createIsSimpleDeclOf(newInnerSD, newDecl);
		syntaxgraph.createIsDeclaredVarOf(newInnerVar, newInnerSD);
		syntaxgraph.createIsConstraintOf(newCombinedConstraint, newDecl);
		syntaxgraph.createIsCompResultDefOf(newInnerVar, newSetComp);

		for (Expression exp : predicates) {
			removeExpressionFromOriginalConstraint(exp, (Declaration) origSD
					.getFirstIsSimpleDeclOf().getOmega());
		}
	}

	private void removeExpressionFromOriginalConstraint(Expression exp,
			Declaration origDecl) throws OptimizerException {
		if (exp.getFirstIsConstraintOf(EdgeDirection.OUT) != null) {
			// This was the only constraint expression of the parent
			// Declaration, so we can simply delete it, unless it's used in
			// other places. In that case, only the edge may be
			// deleted. deleteOrphanedVertices() DTRT.
			exp.getFirstIsConstraintOf(EdgeDirection.OUT).delete();
			OptimizerUtility.deleteOrphanedVerticesBelow(exp,
					new HashSet<Vertex>());
			return;
		}

		ArrayList<Edge> upEdges = new ArrayList<Edge>();
		Edge e = exp.getFirstEdge(EdgeDirection.OUT);
		while (e != null) {
			if (e.getOmega() instanceof FunctionApplication
					&& OptimizerUtility
							.existsForwardPathExcludingOtherTargetClassVertices(
									e, origDecl)) {
				FunctionApplication father = (FunctionApplication) e.getOmega();
				if (OptimizerUtility.isAnd(father)) {
					upEdges.add(e);
				}
			}
			e = e.getNextEdge(EdgeDirection.OUT);
		}
		for (Edge upEdge : upEdges) {
			FunctionApplication funApp = (FunctionApplication) upEdge
					.getOmega();
			Expression otherArg = null;
			IsArgumentOf inc = funApp.getFirstIsArgumentOf(EdgeDirection.IN);
			while (inc != null) {
				if (inc.getNormalEdge() != upEdge.getNormalEdge()) {
					otherArg = (Expression) inc.getAlpha();
				}
				inc = inc.getNextIsArgumentOf(EdgeDirection.IN);
			}
			ArrayList<Edge> funAppEdges = new ArrayList<Edge>();
			Edge funAppEdge = funApp.getFirstEdge(EdgeDirection.OUT);
			while (funAppEdge != null) {
				funAppEdges.add(funAppEdge);
				funAppEdge = funAppEdge.getNextEdge(EdgeDirection.OUT);
			}
			for (Edge fae : funAppEdges) {
				fae.setAlpha(otherArg);
			}
			OptimizerUtility.deleteOrphanedVerticesBelow(funApp,
					new HashSet<Vertex>());
		}
	}

	/**
	 * Collects the {@link Edge}s that start at <code>startVertex</code> and
	 * have a forward directed path to <code>targetEdge</code>.
	 * 
	 * @param startVertex
	 * @param targetEdge
	 * @return a {@link List} of {@link Edge}s going out of
	 *         <code>startVertex</code> that have a forward directed path to
	 *         <code>targetEdge</code>
	 */
	private Set<Edge> collectEdgesComingFrom(Vertex startVertex, Edge targetEdge) {
		// GreqlEvaluator.println("collectEdgesComingFrom(" + startVertex + ", "
		// + targetEdge + ")");
		HashSet<Edge> edges = new HashSet<Edge>();
		if (targetEdge.getAlpha() == startVertex) {
			edges.add(targetEdge);
			return edges;
		}
		Edge inc = targetEdge.getAlpha().getFirstEdge(EdgeDirection.IN);
		while (inc != null) {
			edges.addAll(collectEdgesComingFrom(startVertex, inc));
			inc = inc.getNextEdge(EdgeDirection.IN);
		}
		return edges;
	}

	private Expression createConjunction(List<Expression> predicates,
			Set<Variable> varsToBeCopied, HashMap<Variable, Variable> copiedVars) {
		// GreqlEvaluator.println("createConjunction()");
		if (predicates.size() == 1) {
			return (Expression) OptimizerUtility.copySubgraph(
					predicates.get(0), syntaxgraph, varsToBeCopied, copiedVars);
		}
		FunctionApplication funApp = syntaxgraph.createFunctionApplication();
		FunctionId funId = OptimizerUtility.findOrCreateFunctionId("and",
				syntaxgraph);
		syntaxgraph.createIsFunctionIdOf(funId, funApp);
		syntaxgraph.createIsArgumentOf((Expression) OptimizerUtility
				.copySubgraph(predicates.get(0), syntaxgraph, varsToBeCopied,
						copiedVars), funApp);
		syntaxgraph.createIsArgumentOf(createConjunction(predicates.subList(1,
				predicates.size()), varsToBeCopied, copiedVars), funApp);
		return funApp;
	}

	/**
	 * Given a {@link List} of {@link Expression}s build a new conjunction
	 * (AND-{@link FunctionApplication}) that combines all the predicate
	 * {@link Expression} in the list. This will be done by copying the vertices
	 * with exceptions for {@link FunctionId}s (never copied) and
	 * {@link Variable}s (only those in <code>varsToBeCopied</code> will be
	 * copied ONCE).
	 * 
	 * @param predicates
	 *            a {@link List} of {@link Expression}s
	 * @param varsToBeCopied
	 *            a {@link Set} of {@link Variable}s that shall be copied ONCE.
	 *            (After a variable was copied, the one and only copy will be
	 *            reused instead of the original {@link Variable}.)
	 * @return a AND-{@link FunctionApplication} that combines all
	 *         {@link Expression}s in <code>predicates</code>, or a copy of
	 *         the {@link Expression} in <code>predicates</code> if that
	 *         contains only one {@link Expression}
	 */
	private Expression createConjunction(List<Expression> predicates,
			Set<Variable> varsToBeCopied) {
		return createConjunction(predicates, varsToBeCopied,
				new HashMap<Variable, Variable>());
	}

	/**
	 * Find all {@link Expression}s below <code>exp</code> that can be moved
	 * and return them.
	 * 
	 * An {@link Expression} is considered movable if it needs only
	 * {@link Variable}s that are locally declared in one
	 * {@link SimpleDeclaration} and this {@link SimpleDeclaration} is not the
	 * only one in the parent {@link Declaration}.
	 * 
	 * @param exp
	 *            the {@link Expression} below which to look for movable
	 *            {@link Expression}s
	 */
	private HashMap<SimpleDeclaration, Set<Expression>> collectMovableExpressions(
			Expression exp) {
		HashMap<SimpleDeclaration, Set<Expression>> movableExpressions = new HashMap<SimpleDeclaration, Set<Expression>>();

		if (exp instanceof FunctionApplication
				&& OptimizerUtility.isAnd((FunctionApplication) exp)) {
			// For AND expressions we dive deeper into the arguments.
			FunctionApplication funApp = (FunctionApplication) exp;
			IsArgumentOf isArg = funApp.getFirstIsArgumentOf(EdgeDirection.IN);
			while (isArg != null) {
				HashMap<SimpleDeclaration, Set<Expression>> subtreeMovableExpressions = collectMovableExpressions((Expression) isArg
						.getAlpha());
				for (SimpleDeclaration subSD : subtreeMovableExpressions
						.keySet()) {
					if (movableExpressions.containsKey(subSD)) {
						movableExpressions.get(subSD).addAll(
								subtreeMovableExpressions.get(subSD));
					} else {
						movableExpressions.put(subSD, subtreeMovableExpressions
								.get(subSD));
					}
				}
				isArg = isArg.getNextIsArgumentOf(EdgeDirection.IN);
			}
			return movableExpressions;
		}

		SimpleDeclaration sd = findSimpleDeclarationThatDeclaresAllNeededLocalVariables(exp);
		if (sd != null) {
			// Only collect those SimpleDeclarations whose parent Declaration
			// has more than one SimpleDeclaration or which declare more than
			// one variable.
			Declaration parent = (Declaration) sd.getFirstIsSimpleDeclOf(
					EdgeDirection.OUT).getOmega();
			if (OptimizerUtility.collectSimpleDeclarationsOf(parent).size() > 1
					|| OptimizerUtility.collectVariablesDeclaredBy(sd).size() > 1) {
				if (movableExpressions.containsKey(sd)) {
					movableExpressions.get(sd).add(exp);
				} else {
					HashSet<Expression> predicates = new HashSet<Expression>();
					predicates.add(exp);
					movableExpressions.put(sd, predicates);
				}
			}
		}
		return movableExpressions;
	}

	/**
	 * Find the {@link SimpleDeclaration} that declares all local
	 * {@link Variable}s the {@link Expression} <code>exp</code> needs. If
	 * <code>exp</code> doesn't need any variables or such an
	 * {@link SimpleDeclaration} doesn't exist, return <code>null</code>.
	 * 
	 * @param exp
	 *            an {@link Expression}
	 * @return the {@link SimpleDeclaration} that declares all local
	 *         {@link Variable}s the {@link Expression} <code>exp</code>
	 *         needs or <code>null</code>, if such a
	 *         {@link SimpleDeclaration} doesn't exist.
	 */
	private SimpleDeclaration findSimpleDeclarationThatDeclaresAllNeededLocalVariables(
			Expression exp) {
		Set<Variable> neededVars = collectNeededLocalVariables(exp);

		SimpleDeclaration sd = null, oldSd = null;
		for (Variable var : neededVars) {
			sd = (SimpleDeclaration) var.getFirstIsDeclaredVarOf().getOmega();
			if (oldSd != null && sd != oldSd) {
				// the last variable was declared in another
				// SimpleDeclaration
				return null;
			}
			oldSd = sd;
		}
		return sd;
	}

	/**
	 * @param exp
	 *            the {@link Expression} you want to calculate the needed local
	 *            {@link Variable}s for. A {@link Variable} is local if it's
	 *            declared by one of the {@link SimpleDeclaration}s of the
	 *            nearest {@link Declaration} above <code>exp</code>.
	 * @return a {@link Set} of {@link Variable}s the {@link Expression}
	 *         <code>exp</code> needs and that are declared by the nearest
	 *         {@link Declaration} above <code>exp</code>.
	 */
	private Set<Variable> collectNeededLocalVariables(Expression exp) {
		Set<Variable> neededVars = OptimizerUtility.collectVariablesBelow(exp);
		Set<Variable> neededLocalVars = new HashSet<Variable>();
		Declaration localDecl = findNearestDeclarationAbove(exp);
		for (SimpleDeclaration sd : OptimizerUtility
				.collectSimpleDeclarationsOf(localDecl)) {
			for (Variable var : neededVars) {
				IsDeclaredVarOf inc = sd.getFirstIsDeclaredVarOf();
				while (inc != null) {
					if (inc.getAlpha() == var) {
						neededLocalVars.add(var);
					}
					inc = inc.getNextIsDeclaredVarOf();
				}
			}
		}
		return neededLocalVars;
	}

	/**
	 * Find the nearest {@link Declaration} above <code>vertex</code>.
	 * 
	 * @param vertex
	 *            a {@link Vertex}
	 * @return nearest {@link Declaration} above <code>vertex</code>
	 */
	private Declaration findNearestDeclarationAbove(Vertex vertex) {
		if (vertex instanceof Declaration) {
			return (Declaration) vertex;
		}
		Declaration result = null;
		Edge inc = vertex.getFirstEdge(EdgeDirection.OUT);
		while (inc != null) {
			result = findNearestDeclarationAbove(inc.getOmega());
			if (result != null) {
				return result;
			}
			inc = inc.getNextEdge(EdgeDirection.OUT);
		}
		return null;
	}
}
