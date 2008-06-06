/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.optimizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.SimpleDeclarationEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.OptimizerException;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.IsDeclaredVarOf;
import de.uni_koblenz.jgralab.greql2.schema.IsSimpleDeclOf;
import de.uni_koblenz.jgralab.greql2.schema.SimpleDeclaration;
import de.uni_koblenz.jgralab.greql2.schema.Variable;

/**
 * Optimizes the order of {@link Variable} declarations for each
 * {@link Declaration}, so that {@link Variable}s which produce huge costs on
 * value changes are declared before those where the needed re-evaluation of the
 * constraints is cheaper.
 * 
 * If two {@link Variable} result in the same re-evaluation costs on value
 * changes, the one with a higher cardinality is declared after the other one.
 * 
 * @author Tassilo Horn (heimdall), 2008, Diploma Thesis
 * 
 */
public class VariableDeclarationOrderOptimizer extends OptimizerBase {

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.greql2.optimizer.Optimizer#isEquivalent(de.uni_koblenz.jgralab.greql2.optimizer.Optimizer)
	 */
	@Override
	public boolean isEquivalent(Optimizer optimizer) {
		if (optimizer instanceof VariableDeclarationOrderOptimizer) {
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
		GraphSize graphSize;
		if (eval.getDatagraph() != null) {
			graphSize = new GraphSize(eval.getDatagraph());
		} else {
			graphSize = OptimizerUtility.getDefaultGraphSize();
		}

		GraphMarker<VertexEvaluator> marker = eval
				.getVertexEvaluatorGraphMarker();

		ArrayList<List<VariableDeclarationOrderUnit>> unitsList = new ArrayList<List<VariableDeclarationOrderUnit>>();
		for (Declaration decl : syntaxgraph.getDeclarationVertices()) {
			List<VariableDeclarationOrderUnit> units = new ArrayList<VariableDeclarationOrderUnit>();
			Set<Variable> varsOfDecl = OptimizerUtility
					.collectVariablesDeclaredBy(decl);
			if (varsOfDecl.size() < 2
					|| decl.getFirstIsConstraintOf(EdgeDirection.IN) == null
					|| OptimizerUtility.collectVariablesBelow(
							decl.getFirstIsConstraintOf(EdgeDirection.IN)
									.getAlpha()).size() == 0)
				continue;

			for (Variable var : varsOfDecl) {
				units.add(new VariableDeclarationOrderUnit(var, decl, marker,
						graphSize));
			}
			unitsList.add(units);
		}

		boolean varDeclOrderChanged = false;

		for (List<VariableDeclarationOrderUnit> units : unitsList) {
			Collections.sort(units);

			Declaration declaringDecl = units.get(0).getDeclaringDeclaration();

			List<Variable> varDeclOrderBefore = collectVariablesInDeclarationOrder(declaringDecl);
			List<Variable> varDeclOrderAfter = collectVariablesInProposedDeclarationOrder(units);

			if (!varDeclOrderAfter.equals(varDeclOrderBefore)) {
				varDeclOrderChanged = true;
				System.out.println(optimizerHeaderString()
						+ "New order of declarations in " + declaringDecl);
				Set<SimpleDeclaration> oldSDs = new HashSet<SimpleDeclaration>();
				int i = 0;
				for (VariableDeclarationOrderUnit unit : units) {
					oldSDs.add(unit.getSimpleDeclarationOfVariable());
					marker.removeMark(unit.getSimpleDeclarationOfVariable());
					Variable var = unit.getVariable();
					System.out.println("  " + varDeclOrderBefore.get(i)
							+ "  -->  v" + var.getId() + " (" + var.getName()
							+ "), changeCosts = "
							+ unit.getVariableValueChangeCosts()
							+ ", cardinality = "
							+ unit.getTypeExpressionCardinality());
					i++;
					SimpleDeclaration newSD = syntaxgraph
							.createSimpleDeclaration();
					syntaxgraph.createIsDeclaredVarOf(var, newSD);
					syntaxgraph.createIsTypeExprOfDeclaration(unit
							.getTypeExpressionOfVariable(), newSD);
					syntaxgraph.createIsSimpleDeclOf(newSD, unit
							.getDeclaringDeclaration());
					marker.mark(newSD, new SimpleDeclarationEvaluator(newSD,
							eval));
				}
				for (SimpleDeclaration sd : oldSDs) {
					sd.delete();
				}
			}
		}

		OptimizerUtility.createMissingSourcePositions(syntaxgraph);

		return varDeclOrderChanged;
	}

	private List<Variable> collectVariablesInDeclarationOrder(Declaration decl) {
		ArrayList<Variable> varList = new ArrayList<Variable>();
		for (IsSimpleDeclOf isSD : decl.getIsSimpleDeclOfIncidences()) {
			for (IsDeclaredVarOf isVar : ((SimpleDeclaration) isSD.getAlpha())
					.getIsDeclaredVarOfIncidences()) {
				varList.add((Variable) isVar.getAlpha());
			}
		}
		return varList;
	}

	private List<Variable> collectVariablesInProposedDeclarationOrder(
			List<VariableDeclarationOrderUnit> units) {
		ArrayList<Variable> varList = new ArrayList<Variable>();
		for (VariableDeclarationOrderUnit unit : units) {
			varList.add(unit.getVariable());
		}
		return varList;
	}
}
