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

	private Greql2 syntaxgraph;
	private GreqlEvaluator greqlEvaluator;

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
		this.syntaxgraph = syntaxgraph;
		this.greqlEvaluator = eval;

		runOptimization();

		OptimizerUtility.createMissingSourcePositions(this.syntaxgraph);

		Optimizer mergeSDOpt = new MergeSimpleDeclarationsOptimizer();
		mergeSDOpt.optimize(this.greqlEvaluator, this.syntaxgraph);

		// FIXME (horn): Return true if something was done!
		return false;
	}

	/**
	 * Iterates through all {@link Declaration}s and sorts the {@link Variable}
	 * declarations.
	 * 
	 * @throws OptimizerException
	 */
	private void runOptimization() throws OptimizerException {
		GraphSize graphSize;
		if (greqlEvaluator.getDatagraph() != null) {
			graphSize = new GraphSize(greqlEvaluator.getDatagraph());
		} else {
			graphSize = OptimizerUtility.getDefaultGraphSize();
		}

		GraphMarker<VertexEvaluator> marker = greqlEvaluator
				.getVertexEvaluatorGraphMarker();

		ArrayList<List<VariableDeclarationOrderUnit>> unitsList = new ArrayList<List<VariableDeclarationOrderUnit>>();
		for (Declaration decl : syntaxgraph.getDeclarationVertices()) {
			List<VariableDeclarationOrderUnit> units = new ArrayList<VariableDeclarationOrderUnit>();
			Set<Variable> varsOfDecl = OptimizerUtility
					.collectVariablesDeclaredBy(decl);
			if (varsOfDecl.size() < 2
					|| decl.getFirstIsConstraintOf(EdgeDirection.IN) == null)
				continue;

			for (Variable var : varsOfDecl) {
				units.add(new VariableDeclarationOrderUnit(var, decl, marker,
						graphSize));
			}
			unitsList.add(units);
		}

		Set<SimpleDeclaration> oldSDs = new HashSet<SimpleDeclaration>();
		for (List<VariableDeclarationOrderUnit> units : unitsList) {
			Collections.sort(units);
			Declaration declaringDecl = units.get(0).getDeclaringDeclaration();
			System.out.println(optimizerHeaderString()
					+ "New order of declarations in " + declaringDecl);
			for (VariableDeclarationOrderUnit unit : units) {
				oldSDs.add(unit.getSimpleDeclarationOfVariable());
				marker.removeMark(unit.getSimpleDeclarationOfVariable());
				Variable var = unit.getVariable();
				System.out.println("  --> [" + var + " (" + var.getName()
						+ "), changeCosts = "
						+ unit.getVariableValueChangeCosts()
						+ ", cardinality = "
						+ unit.getTypeExpressionCardinality() + "]");
				SimpleDeclaration newSD = syntaxgraph.createSimpleDeclaration();
				syntaxgraph.createIsDeclaredVarOf(var, newSD);
				syntaxgraph.createIsTypeExprOfDeclaration(unit
						.getTypeExpressionOfVariable(), newSD);
				syntaxgraph.createIsSimpleDeclOf(newSD, unit
						.getDeclaringDeclaration());
				marker.mark(newSD, new SimpleDeclarationEvaluator(newSD,
						greqlEvaluator));

			}
		}
		for (SimpleDeclaration sd : oldSDs) {
			sd.delete();
		}

	}
}
