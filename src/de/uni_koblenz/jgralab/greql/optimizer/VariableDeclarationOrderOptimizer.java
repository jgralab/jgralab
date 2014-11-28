/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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
/**
 *
 */
package de.uni_koblenz.jgralab.greql.optimizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.OptimizerInfo;
import de.uni_koblenz.jgralab.greql.exception.OptimizerException;
import de.uni_koblenz.jgralab.greql.schema.Declaration;
import de.uni_koblenz.jgralab.greql.schema.GreqlGraph;
import de.uni_koblenz.jgralab.greql.schema.IsDeclaredVarOf;
import de.uni_koblenz.jgralab.greql.schema.IsSimpleDeclOf;
import de.uni_koblenz.jgralab.greql.schema.SimpleDeclaration;
import de.uni_koblenz.jgralab.greql.schema.Variable;

/**
 * Optimizes the order of {@link Variable} declarations for each
 * {@link Declaration}, so that {@link Variable}s which produce huge costs on
 * value changes are declared before those where the needed re-evaluation of the
 * constraints is cheaper.
 * 
 * If two {@link Variable} result in the same re-evaluation costs on value
 * changes, the one with a higher cardinality is declared after the other one.
 * 
 * If there's a dependency, then it is ensured, that the variables stay in the
 * correct order. Example: In a declaration containing
 * <code>x : list(1..10), y : list(x..20)</code>, the simple declaration of
 * <code>y</code> will never be moved before the simple declaration of
 * <code>x</code>.
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class VariableDeclarationOrderOptimizer extends OptimizerBase {

	private static Logger logger = JGraLab
			.getLogger(VariableDeclarationOrderOptimizer.class);

	public VariableDeclarationOrderOptimizer(OptimizerInfo optimizerInfo) {
		super(optimizerInfo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.optimizer.Optimizer#isEquivalent(de.uni_koblenz
	 * .jgralab.greql2.optimizer.Optimizer)
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
	 * @see
	 * de.uni_koblenz.jgralab.greql2.optimizer.Optimizer#optimize(de.uni_koblenz
	 * .jgralab.greql2.evaluator.GreqlEvaluator,
	 * de.uni_koblenz.jgralab.greql2.schema.Greql)
	 */
	@Override
	public boolean optimize(GreqlQuery query) throws OptimizerException {
		GreqlGraph syntaxgraph = query.getQueryGraph();

		ArrayList<List<VariableDeclarationOrderUnit>> unitsList = new ArrayList<>();
		for (Declaration decl : syntaxgraph.getDeclarationVertices()) {
			List<VariableDeclarationOrderUnit> units = new ArrayList<>();
			Set<Variable> varsOfDecl = collectVariablesDeclaredBy(decl);
			if (varsOfDecl.size() < 2) {
				// With only one Variable, there's
				// nothing to reorder
				continue;
			}

			for (Variable var : varsOfDecl) {
				units.add(new VariableDeclarationOrderUnit(var, decl, query));
			}
			unitsList.add(units);
		}

		boolean varDeclOrderChanged = false;

		Set<SimpleDeclaration> oldSDs = new HashSet<>();
		for (List<VariableDeclarationOrderUnit> units : unitsList) {
			Collections.sort(units);

			Declaration declaringDecl = units.get(0).getDeclaringDeclaration();

			List<Variable> varDeclOrderBefore = collectVariablesInDeclarationOrder(declaringDecl);
			List<Variable> varDeclOrderAfter = collectVariablesInProposedDeclarationOrder(units);

			if (!varDeclOrderAfter.equals(varDeclOrderBefore)) {
				varDeclOrderChanged = true;

				logger.finer(optimizerHeaderString()
						+ "New order of declarations in " + declaringDecl);

				int i = 0;
				for (VariableDeclarationOrderUnit unit : units) {
					oldSDs.add(unit.getSimpleDeclarationOfVariable());
					Variable var = unit.getVariable();
					Variable old = varDeclOrderBefore.get(i);
					logger.finer("  " + old + " (" + old.get_name() + ") --> "
							+ var + " (" + var.get_name() + "), changeCosts = "
							+ unit.getVariableValueChangeCosts()
							+ ", cardinality = "
							+ unit.getTypeExpressionCardinality());

					i++;
					SimpleDeclaration newSD = syntaxgraph
							.createSimpleDeclaration();
					syntaxgraph.createIsDeclaredVarOf(var, newSD);
					syntaxgraph.createIsTypeExprOfDeclaration(
							unit.getTypeExpressionOfVariable(), newSD);
					syntaxgraph.createIsSimpleDeclOf(newSD,
							unit.getDeclaringDeclaration());
				}

			}
		}
		for (SimpleDeclaration sd : oldSDs) {
			sd.delete();
		}

		OptimizerUtility.createMissingSourcePositions(syntaxgraph);

		// Tg2Dot.printGraphAsDot(syntaxgraph, true, "/home/horn/vdoo.dot");
		// System.out.println("Afted VDOO:");
		// System.out.println(((SerializableGreql) syntaxgraph).serialize());

		return varDeclOrderChanged;
	}

	private List<Variable> collectVariablesInDeclarationOrder(Declaration decl) {
		ArrayList<Variable> varList = new ArrayList<>();
		for (IsSimpleDeclOf isSD : decl.getIsSimpleDeclOfIncidences()) {
			for (IsDeclaredVarOf isVar : isSD.getAlpha()
					.getIsDeclaredVarOfIncidences()) {
				varList.add(isVar.getAlpha());
			}
		}
		return varList;
	}

	private List<Variable> collectVariablesInProposedDeclarationOrder(
			List<VariableDeclarationOrderUnit> units) {
		ArrayList<Variable> varList = new ArrayList<>();
		for (VariableDeclarationOrderUnit unit : units) {
			varList.add(unit.getVariable());
		}
		return varList;
	}

	/**
	 * Collect all {@link Variable}s declared by the {@link SimpleDeclaration}s
	 * of <code>decl</code>.
	 * 
	 * @param decl
	 *            a {@link Declaration}
	 * @return a {@link Set} of all {@link Variable}s declared by the
	 *         {@link SimpleDeclaration}s of <code>decl</code>.
	 */
	private Set<Variable> collectVariablesDeclaredBy(Declaration decl) {
		HashSet<Variable> vars = new HashSet<>();
		for (IsSimpleDeclOf inc : decl
				.getIsSimpleDeclOfIncidences(EdgeDirection.IN)) {
			vars.addAll(OptimizerUtility.collectVariablesDeclaredBy(inc
					.getAlpha()));
		}
		return vars;
	}
}
