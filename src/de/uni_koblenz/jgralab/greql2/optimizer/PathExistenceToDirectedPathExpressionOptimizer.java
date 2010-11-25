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
package de.uni_koblenz.jgralab.greql2.optimizer;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.OptimizerException;
import de.uni_koblenz.jgralab.greql2.funlib.Intersection;
import de.uni_koblenz.jgralab.greql2.schema.BoolLiteral;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.EdgePathDescription;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.IsBoundVarOf;
import de.uni_koblenz.jgralab.greql2.schema.IsDeclaredVarOf;
import de.uni_koblenz.jgralab.greql2.schema.IsPathDescriptionOf;
import de.uni_koblenz.jgralab.greql2.schema.PathDescription;
import de.uni_koblenz.jgralab.greql2.schema.PathExistence;
import de.uni_koblenz.jgralab.greql2.schema.PathExpression;
import de.uni_koblenz.jgralab.greql2.schema.SimpleDeclaration;
import de.uni_koblenz.jgralab.greql2.schema.TypeId;
import de.uni_koblenz.jgralab.greql2.schema.Variable;
import de.uni_koblenz.jgralab.greql2.schema.VertexSetExpression;

/**
 * @author Tassilo Horn &lt;horn@uni-koblenz.de&gt;
 * 
 */
public class PathExistenceToDirectedPathExpressionOptimizer extends
		OptimizerBase {

	private static Logger logger = JGraLab
			.getLogger(PathExistenceToDirectedPathExpressionOptimizer.class
					.getPackage().getName());

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.optimizer.Optimizer#isEquivalent(de.uni_koblenz
	 * .jgralab.greql2.optimizer.Optimizer)
	 */
	@Override
	public boolean isEquivalent(Optimizer optimizer) {
		if (optimizer instanceof PathExistenceToDirectedPathExpressionOptimizer) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.optimizer.Optimizer#optimize(de.uni_koblenz
	 * .jgralab.greql2.evaluator.GreqlEvaluator,
	 * de.uni_koblenz.jgralab.greql2.schema.Greql2)
	 */
	@Override
	public boolean optimize(GreqlEvaluator eval, Greql2 syntaxgraph)
			throws OptimizerException {
		if (syntaxgraph.getFirstVertex(PathExistence.class) == null) {
			return false;
		}

		List<PathExistence> pes = new LinkedList<PathExistence>();
		for (PathExistence pe : syntaxgraph.getPathExistenceVertices()) {
			pes.add(pe);
		}

		Iterator<PathExistence> it = pes.iterator();
		while (it.hasNext()) {
			PathExistence pe = it.next();
			boolean optimized = tryOptimizePathExistence(pe);
			if (!optimized) {
				it.remove();
			}
		}

		for (PathExistence pe : pes) {
			BoolLiteral lit = syntaxgraph.createBoolLiteral();
			lit.set_boolValue(true);
			while (pe.getFirstIncidence(EdgeDirection.OUT) != null) {
				Edge e = pe.getFirstIncidence(EdgeDirection.OUT);
				e.setAlpha(lit);
				assert e.getAlpha() == lit;
			}
			pe.delete();
		}

		// System.out.println("PETDPEO: "
		// + ((SerializableGreql2) syntaxgraph).serialize());

		recreateVertexEvaluators(eval);
		OptimizerUtility.createMissingSourcePositions(syntaxgraph);
		return !pes.isEmpty();
	}

	private boolean tryOptimizePathExistence(PathExistence pe) {
		Expression startExp = pe.get_startExpr();
		Expression targetExp = pe.get_targetExpr();

		// TODO: For now, we want that both exps are variables. Maybe that's not
		// needed...
		if (!(startExp instanceof Variable) || !(targetExp instanceof Variable)) {
			logger.finer(optimizerHeaderString()
					+ "PathExistence hasn't form var1 --> var2, skipping...");
			return false;
		}

		// Don't optimize PathDescriptions containing EdgePathDescriptions,
		// cause we don't handle these dependencies right now...
		Expression pathDesc = pe.get_path();
		if (!(pathDesc instanceof PathDescription)) {
			logger.finer(optimizerHeaderString()
					+ "PathExistence contains an Expression as PathDescription, skipping...");
			return false;
		}
		if (!isOptimizablePathDescription((PathDescription) pathDesc)) {
			logger.finer(optimizerHeaderString()
					+ "PathExistence contains an EdgePathDescription, skipping...");
			return false;
		}

		Variable start = (Variable) startExp;
		Variable target = (Variable) targetExp;

		if (start == target) {
			logger.finer(optimizerHeaderString()
					+ "PathExistence specifies a loop, skipping...");
			return false;
		}

		boolean startIsDeclaredFirst = true;
		if (isDeclaredBefore(target, start)) {
			startIsDeclaredFirst = false;
		}

		// sd is the simple decl where we want to change the type expr of
		SimpleDeclaration sd = null;
		// the variable that is declared in there
		Variable sdsVar = null;
		// the variable that will be used in the simple decl:
		// "sdsVar: anchorVar -->"
		Variable anchorVar = null;

		IsBoundVarOf ibvoStart = start.getFirstIsBoundVarOfIncidence();
		IsBoundVarOf ibvoTarget = target.getFirstIsBoundVarOfIncidence();
		if ((ibvoStart != null) && (ibvoTarget != null)) {
			// In case of "using , b: a <-- b" this is as efficient as it is...
			return false;
		} else if ((ibvoStart != null) && (ibvoTarget == null)) {
			// start is a bound var, target is declared
			anchorVar = start;
			sdsVar = target;
			SimpleDeclaration targetSD = (SimpleDeclaration) target
					.getFirstIsDeclaredVarOfIncidence().getOmega();
			if (targetSD.getDegree(IsDeclaredVarOf.class) > 1) {
				sd = splitSimpleDecl(targetSD, target);
			} else {
				sd = targetSD;
			}
		} else if ((ibvoStart == null) && (ibvoTarget != null)) {
			// target is a bound var, start is declared
			anchorVar = target;
			sdsVar = start;
			SimpleDeclaration startSD = (SimpleDeclaration) start
					.getFirstIsDeclaredVarOfIncidence().getOmega();
			if (startSD.getDegree(IsDeclaredVarOf.class) > 1) {
				sd = splitSimpleDecl(startSD, target);
			} else {
				sd = startSD;
			}
		} else {
			// both are declared vars
			SimpleDeclaration startSD = (SimpleDeclaration) start
					.getFirstIsDeclaredVarOfIncidence().getOmega();
			SimpleDeclaration targetSD = (SimpleDeclaration) target
					.getFirstIsDeclaredVarOfIncidence().getOmega();

			if (targetSD == startSD) {
				if (startIsDeclaredFirst) {
					sd = splitSimpleDecl(startSD, target);
					anchorVar = start;
					sdsVar = target;
				} else {
					sd = splitSimpleDecl(startSD, start);
					anchorVar = target;
					sdsVar = start;
				}
			} else if (startIsDeclaredFirst) {
				if (targetSD.getDegree(IsDeclaredVarOf.class) > 1) {
					sd = splitSimpleDecl(targetSD, target);
				} else {
					sd = targetSD;
				}
				sdsVar = target;
				anchorVar = start;
			} else {
				if (startSD.getDegree(IsDeclaredVarOf.class) > 1) {
					sd = splitSimpleDecl(startSD, start);
				} else {
					sd = startSD;
				}
				anchorVar = target;
				sdsVar = start;
			}
		}

		// We must ensure that the start/target vertex of the new
		// forward/backward vertex set is declared before the simple
		// declaration's variable.
		if (isDeclaredBefore(sdsVar, anchorVar)) {
			return false;
		}

		// The path expression must be in a constraint and it must be in a
		// top-level conjunction.
		if (!isConstraintAndTopLevelConjunction(pe, (Declaration) sd
				.getFirstIsSimpleDeclOfIncidence().getOmega())) {
			logger.finer(optimizerHeaderString()
					+ pe
					+ " cannot be optimized, cause it's not in an constraint conjunction...");
			return false;
		}

		PathDescription path = (PathDescription) pe.get_path();
		Set<Variable> varsUsedInPath = OptimizerUtility
				.collectInternallyDeclaredVariablesBelow(path);
		if (varsUsedInPath.contains(sdsVar)) {
			logger.finer(optimizerHeaderString()
					+ "PathExistence path contains declared var, so skipping...");
			return false;
		}
		for (Variable usedVar : varsUsedInPath) {
			if (isDeclaredBefore(sdsVar, usedVar)) {
				logger.finer(optimizerHeaderString()
						+ "PathExistence path contains a previously declared var, so skipping...");
				return false;
			}
		}

		Expression typeExp = sd.get_typeExpr();
		if (typeExp instanceof VertexSetExpression) {
			VertexSetExpression vse = (VertexSetExpression) typeExp;
			optimizeVertexSetExpression(pe, sd, anchorVar, vse);
			return true;
		}

		// System.out.println("after splitting PETDPEO: "
		// + ((SerializableGreql2) start.getGraph()).serialize());

		// Ok, so the type expression is something more complex, so create a set
		// difference between the old type expression and a new forward/backward
		// vertex set.

		sd.getFirstIsTypeExprOfDeclarationIncidence(EdgeDirection.IN).delete();
		Greql2 g = (Greql2) typeExp.getGraph();
		FunctionApplication diff = g.createFunctionApplication();
		g.createIsFunctionIdOf(OptimizerUtility.findOrCreateFunctionId(
				Intersection.class.getSimpleName().toLowerCase(), g), diff);
		diff.add_argument(typeExp);
		sd.add_typeExpr(diff);

		// now create the new forward/backward vertex set as other arg of the
		// differenc funApp
		boolean forward = true;
		if (pe.get_targetExpr() == anchorVar) {
			forward = false;
		}
		PathExpression directedVS = createForwardOrBackwardVertexSet(forward,
				anchorVar, path, null);
		diff.add_argument(directedVS);

		// that's it!
		return true;
	}

	private boolean isOptimizablePathDescription(PathDescription pd) {
		if (pd instanceof EdgePathDescription) {
			return false;
		}
		for (IsPathDescriptionOf ipdo : pd
				.getIsPathDescriptionOfIncidences(EdgeDirection.IN)) {
			boolean isOptimizable = isOptimizablePathDescription((PathDescription) ipdo
					.getThat());
			if (!isOptimizable) {
				return false;
			}
		}
		return true;
	}

	private boolean isConstraintAndTopLevelConjunction(Vertex current,
			Declaration top) {
		if (current == top) {
			return true;
		}
		if ((current instanceof FunctionApplication)
				&& OptimizerUtility.isOr((FunctionApplication) current)) {
			return false;
		}
		for (Edge e : current.incidences(EdgeDirection.OUT)) {
			Vertex newCurrent = e.getOmega();
			if (isConstraintAndTopLevelConjunction(newCurrent, top)) {
				return true;
			}
		}
		return false;
	}

	private PathExpression createForwardOrBackwardVertexSet(boolean forward,
			Variable anchor, PathDescription path,
			Iterable<? extends TypeId> restrictions) {
		Greql2 g = (Greql2) path.getGraph();

		PathExpression newPE = null;
		if (forward) {
			newPE = g.createForwardVertexSet();
			newPE.add_path(path);
			if (restrictions != null) {
				for (TypeId tid : restrictions) {
					path.add_goalRestr(tid);
				}
			}
			newPE.add_startExpr(anchor);
		} else {
			newPE = g.createBackwardVertexSet();
			newPE.add_path(path);
			if (restrictions != null) {
				for (TypeId tid : restrictions) {
					path.add_startRestr(tid);
				}
			}
			newPE.add_targetExpr(anchor);
		}
		return newPE;
	}

	private boolean optimizeVertexSetExpression(PathExistence pe,
			SimpleDeclaration sd, Variable otherVar, VertexSetExpression vse) {
		boolean forward = true;
		if (pe.get_targetExpr() == otherVar) {
			forward = false;
		}

		Greql2 g = (Greql2) pe.getGraph();

		PathExpression newPE = createForwardOrBackwardVertexSet(forward,
				otherVar, (PathDescription) pe.get_path(), vse.get_typeRestr());
		if (vse.getDegree(EdgeDirection.OUT) < 2) {
			vse.delete();
		} else {
			sd.getFirstIsTypeExprOfDeclarationIncidence().delete();
		}
		g.createIsTypeExprOfDeclaration(newPE, sd);
		logger.finer(optimizerHeaderString() + "Created " + newPE
				+ " as optimization...");
		return true;
	}

	private SimpleDeclaration splitSimpleDecl(SimpleDeclaration sd, Variable var) {
		Set<Variable> splitSet = new HashSet<Variable>(1);
		splitSet.add(var);
		return splitSimpleDeclaration(sd, splitSet);
	}

}
