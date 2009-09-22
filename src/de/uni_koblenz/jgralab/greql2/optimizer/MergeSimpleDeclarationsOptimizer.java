package de.uni_koblenz.jgralab.greql2.optimizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.schema.Declaration;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.IsDeclaredVarOf;
import de.uni_koblenz.jgralab.greql2.schema.IsSimpleDeclOf;
import de.uni_koblenz.jgralab.greql2.schema.IsTargetExprOf;
import de.uni_koblenz.jgralab.greql2.schema.IsTypeExprOfDeclaration;
import de.uni_koblenz.jgralab.greql2.schema.SimpleDeclaration;

/**
 * This {@link MergeSimpleDeclarationsOptimizer} finds and merges all
 * {@link SimpleDeclaration}s in the given syntaxgraph that are below the same
 * {@link Declaration} and share the same {@link Expression} on their
 * {@link IsTargetExprOf} edge. The {@link SimpleDeclaration} with the lowest ID
 * survives, all others are deleted and their {@link IsDeclaredVarOf} edges are
 * relocated to the surviving {@link SimpleDeclaration}.
 * 
 * @author ist@uni-koblenz.de
 */
public class MergeSimpleDeclarationsOptimizer extends OptimizerBase {

	private static Logger logger = JGraLab
			.getChildLogger(MergeSimpleDeclarationsOptimizer.class.getPackage()
					.getName());

	private boolean anOptimizationWasDone = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.greql2.optimizer.Optimizer#isEquivalent(de.uni_koblenz
	 * .jgralab.greql2.optimizer.Optimizer)
	 */
	@Override
	public boolean isEquivalent(Optimizer optimizer) {
		if (optimizer instanceof MergeSimpleDeclarationsOptimizer) {
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
	public boolean optimize(GreqlEvaluator eval, Greql2 syntaxgraph) {
		anOptimizationWasDone = false;

		findAndMergeSimpleDeclarations(syntaxgraph);

		return anOptimizationWasDone;
	}

	/**
	 * Finds and merges all {@link SimpleDeclaration}s in the given syntaxgraph
	 * that are below the same {@link Declaration} and share the same
	 * {@link Expression} on their {@link IsTargetExprOf} edge. The
	 * {@link SimpleDeclaration} with the lowest ID survives, all others are
	 * deleted and their {@link IsDeclaredVarOf} edges are relocated to the
	 * surviving {@link SimpleDeclaration}.
	 * 
	 * @param syntaxgraph
	 *            a {@link Greql2} graph
	 */
	private void findAndMergeSimpleDeclarations(Greql2 syntaxgraph) {
		HashMap<String, ArrayList<SimpleDeclaration>> mergableSDMap = new HashMap<String, ArrayList<SimpleDeclaration>>();
		Declaration decl = syntaxgraph.getFirstDeclaration();
		while (decl != null) {
			IsSimpleDeclOf isSimpleDeclOf = decl
					.getFirstIsSimpleDeclOf(EdgeDirection.IN);
			while (isSimpleDeclOf != null) {
				SimpleDeclaration sDecl = (SimpleDeclaration) isSimpleDeclOf
						.getAlpha();
				String key = decl.getId()
						+ "-"
						+ sDecl.getFirstIsTypeExprOf(EdgeDirection.IN)
								.getAlpha().getId();
				if (mergableSDMap.containsKey(key)) {
					// We've found another SimpleDeclaration with the same type
					// expression under the current Declaration, thus we add it
					// to the list of SimpleDeclarations for this
					// typeExprId-declId-Pair.
					mergableSDMap.get(key).add(sDecl);
				} else {
					ArrayList<SimpleDeclaration> simpleDecls = new ArrayList<SimpleDeclaration>();
					simpleDecls.add(sDecl);
					mergableSDMap.put(key, simpleDecls);
				}
				isSimpleDeclOf = isSimpleDeclOf.getNextIsSimpleDeclOf();
			}
			decl = decl.getNextDeclaration();
		}
		mergeSimpleDeclarations(mergableSDMap);
	}

	/**
	 * Merges the {@link SimpleDeclaration} given as the values of
	 * <code>mergableSDMap</code> if the order of variable declarations isn't
	 * changed by the merge.
	 * 
	 * @param mergableSDMap
	 */
	private void mergeSimpleDeclarations(
			HashMap<String, ArrayList<SimpleDeclaration>> mergableSDMap) {
		for (Entry<String, ArrayList<SimpleDeclaration>> e : mergableSDMap
				.entrySet()) {
			SimpleDeclaration survivor = e.getValue().get(0);
			Declaration decl = (Declaration) survivor.getFirstIsSimpleDeclOf()
					.getOmega();
			IsSimpleDeclOf isSDOfSurvivor = survivor
					.getFirstIsSimpleDeclOf(EdgeDirection.OUT);
			IsTypeExprOfDeclaration isTEODSurvivor = survivor
					.getFirstIsTypeExprOfDeclaration(EdgeDirection.IN);

			for (SimpleDeclaration s : e.getValue()) {

				IsSimpleDeclOf isSDOfS = s
						.getFirstIsSimpleDeclOf(EdgeDirection.OUT);

				if (isNextInIncidenceList(decl, isSDOfSurvivor, isSDOfS)) {
					logger.finer(optimizerHeaderString()
							+ "Merging all variables of " + s + " into "
							+ survivor + ".");

					while (s.getFirstIsDeclaredVarOf() != null) {
						s.getFirstIsDeclaredVarOf().setOmega(survivor);
					}

					// merge the sourcePositions
					OptimizerUtility.mergeSourcePositions(isSDOfS,
							isSDOfSurvivor);
					IsTypeExprOfDeclaration isTEODS = s
							.getFirstIsTypeExprOfDeclaration(EdgeDirection.IN);
					OptimizerUtility.mergeSourcePositions(isTEODS,
							isTEODSurvivor);

					s.delete();
					anOptimizationWasDone = true;
				} else {
					// survivor and s couldn't be merged, which means that the
					// edge from s doesn't follow directly after the edge from
					// survivor in decl's incidence list. But maybe the next
					// IsSDEdge follows the edge from s.
					survivor = s;
				}
			}
		}
	}

	/**
	 * @param decl
	 * @param isSDOfSurvivor
	 * @param isSDOfS
	 * @return <code>true</code> if <code>isSDOfS</code> follows directly
	 *         <code>isSDOfSurvivor</code> in the incidence list of
	 *         <code>decl</code>, <code>false</code> otherwise
	 */
	private boolean isNextInIncidenceList(Declaration decl,
			IsSimpleDeclOf isSDOfSurvivor, IsSimpleDeclOf isSDOfS) {
		IsSimpleDeclOf edge = decl.getFirstIsSimpleDeclOf();
		while (edge != null) {
			if (edge.getNormalEdge() != isSDOfSurvivor) {
				edge = edge.getNextIsSimpleDeclOf();
				continue;
			}
			IsSimpleDeclOf nextEdge = edge.getNextIsSimpleDeclOf();
			if ((nextEdge != null) && (nextEdge.getNormalEdge() == isSDOfS)) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
}
