/**
 * 
 */
package de.uni_koblenz.jgralab.greql2.optimizer.dissolution;

import java.util.HashSet;
import java.util.Set;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphMarker;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.costmodel.GraphSize;
import de.uni_koblenz.jgralab.greql2.evaluator.vertexeval.VertexEvaluator;
import de.uni_koblenz.jgralab.greql2.optimizer.OptimizerUtility;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;
import de.uni_koblenz.jgralab.greql2.schema.IsArgumentOf;

/**
 * TODO: (heimdall) Comment class!
 * 
 * @author Tassilo Horn (heimdall), 2008, Diploma Thesis
 * 
 */
public abstract class SemanticGraph {

	protected static GraphMarker<VertexEvaluator> vertexEvalGraphMarker;
	protected static GraphSize graphSize;

	public SemanticGraph dissolve() {
		// Work an a copy and don't modify the original graph
		GreqlEvaluator.println("Original formula:\n  --> " + this);
		SemanticGraph graphCopy = deepCopy().toNegationNormalForm();
		GreqlEvaluator.println("Transformed to negation normal form:\n  --> "
				+ graphCopy);

		int step = 1;

		Set<Set<Leaf>> allCPaths = graphCopy.getCPaths();
		Set<Set<Leaf>> allDPaths = graphCopy.getDPaths();
		Set<Link> links = getLinks(allCPaths);
		while (links.size() > 0) {
			Link link = links.iterator().next();
			// We don't need to test if link is a resolution chain, cause a
			// single link always is. See Link.isResolutionChain() for
			// details.
			SemanticGraph fullBlock = graphCopy.getSmallestFullBlockContaining(
					link, allCPaths, allDPaths);
			SemanticGraph dissolvent = graphCopy.getDissolvent(link, fullBlock,
					allCPaths);

			assert dissolvent != null : "dissolvent == null, so " + link
					+ " seems to be no dissolution chain!";

			GreqlEvaluator.println("Dissolution step " + step + " using link "
					+ link + ":");

			graphCopy = graphCopy.replaceInGraph(fullBlock, dissolvent);
			graphCopy = graphCopy.simplify();

			GreqlEvaluator.println("  --> " + graphCopy);

			step++;

			allCPaths = graphCopy.getCPaths();
			allDPaths = graphCopy.getDPaths();
			links = getLinks(allCPaths);
		}
		return graphCopy;
	}

	@Override
	public abstract String toString();

	public abstract long getCosts();

	public static SemanticGraph createSemanticGraphFromExpression(
			Expression exp, GraphMarker<VertexEvaluator> graphMarker,
			GraphSize graphSize) {
		vertexEvalGraphMarker = graphMarker;
		SemanticGraph.graphSize = graphSize;
		Atom.originalExpressionsCostSet = new HashSet<Expression>();

		HashSet<Expression> oldExpressions = new HashSet<Expression>();
		SemanticGraph sg = createSemanticGraphFromExpressionInternal(exp,
				oldExpressions);
		for (Expression e : oldExpressions) {
			e.delete();
		}
		return sg;
	}

	public static SemanticGraph createSemanticGraphFromExpressionInternal(
			Expression exp, HashSet<Expression> oldExps) {
		if (exp instanceof FunctionApplication) {
			FunctionApplication funApp = (FunctionApplication) exp;
			if (OptimizerUtility.isAnd(funApp)) {
				IsArgumentOf inc = funApp
						.getFirstIsArgumentOf(EdgeDirection.IN);
				Expression leftArg = (Expression) inc.getAlpha();
				Expression rightArg = (Expression) inc.getNextIsArgumentOf(
						EdgeDirection.IN).getAlpha();
				oldExps.add(funApp);
				return new And(createSemanticGraphFromExpressionInternal(
						leftArg, oldExps),
						createSemanticGraphFromExpressionInternal(rightArg,
								oldExps));
			}
			if (OptimizerUtility.isOr(funApp)) {
				IsArgumentOf inc = funApp
						.getFirstIsArgumentOf(EdgeDirection.IN);
				Expression leftArg = (Expression) inc.getAlpha();
				Expression rightArg = (Expression) inc.getNextIsArgumentOf(
						EdgeDirection.IN).getAlpha();
				oldExps.add(funApp);
				return new Or(createSemanticGraphFromExpressionInternal(
						leftArg, oldExps),
						createSemanticGraphFromExpressionInternal(rightArg,
								oldExps));
			}
			if (OptimizerUtility.isNot(funApp)) {
				IsArgumentOf inc = funApp
						.getFirstIsArgumentOf(EdgeDirection.IN);
				Expression arg = (Expression) inc.getAlpha();
				oldExps.add(exp);
				return new Not(createSemanticGraphFromExpressionInternal(arg,
						oldExps));
			}
		}

		// vertex is a non-logical FunApp or some other term.
		return new Atom("v" + exp.getId(), exp);
	}

	public abstract Expression toExpression(Greql2 syntaxgraph);

	public abstract SemanticGraph toNegationNormalForm();

	public abstract Set<Set<Leaf>> getCPaths();

	public abstract Set<Set<Leaf>> getDPaths();

	/**
	 * @param allCPaths
	 *            all C-Paths of the over-all {@link SemanticGraph}
	 * @return <code>true</code>, if this {@link SemanticGraph} is a C-Block,
	 *         <code>false</code> otherwise
	 */
	public boolean isCBlock(Set<Set<Leaf>> allCPaths) {
		Set<Leaf> myNodes = getLeafs();
		Set<Set<Leaf>> myCPaths = getCPaths();
		for (Set<Leaf> cpath : allCPaths) {
			Set<Leaf> intersection = intersection(cpath, myNodes);
			if (!intersection.isEmpty() && !myCPaths.contains(intersection)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param allDPaths
	 *            all D-Paths of the over-all {@link SemanticGraph}
	 * @return <code>true</code>, if this {@link SemanticGraph} is a D-Block,
	 *         <code>false</code> otherwise
	 */
	public boolean isDBlock(Set<Set<Leaf>> allDPaths) {
		Set<Leaf> myNodes = getLeafs();
		Set<Set<Leaf>> myDPaths = getDPaths();
		for (Set<Leaf> dpath : allDPaths) {
			Set<Leaf> intersection = intersection(dpath, myNodes);
			if (!intersection.isEmpty() && !myDPaths.contains(intersection)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param allCPaths
	 *            all C-Paths of the over-all {@link SemanticGraph}
	 * @param allDPaths
	 *            all D-Paths of the over-all {@link SemanticGraph}
	 * @return <code>true</code>, if this {@link SemanticGraph} is a full
	 *         block, <code>false</code> otherwise
	 */
	public boolean isFullBlock(Set<Set<Leaf>> allCPaths,
			Set<Set<Leaf>> allDPaths) {
		return isCBlock(allCPaths) && isDBlock(allDPaths);
	}

	protected static Set<Leaf> intersection(Set<Leaf> set1, Set<Leaf> set2) {
		Set<Leaf> intersection = new HashSet<Leaf>();
		for (Leaf f : set1) {
			if (set2.contains(f)) {
				intersection.add(f);
			}
		}
		return intersection;
	}

	/**
	 * @return a Set of all leafs in the graph. A leafs is either {@link Atom}
	 *         or {@link Not}. This should be used only after the
	 *         {@link SemanticGraph} was transformed to negation normal form.
	 */
	public abstract Set<Leaf> getLeafs();

	/**
	 * @param leafs
	 *            A Set of leafs
	 * @return The original Formula restricted to the given set of leafs
	 */
	public abstract SemanticGraph getSubgraphRelativeTo(Set<Leaf> leafs);

	/**
	 * @param subGraph
	 *            some {@link SemanticGraph}
	 * @return the c-path extension with respect to the given sub-formula
	 */
	public abstract SemanticGraph getCPathExtension(SemanticGraph subGraph);

	/**
	 * @param subGraph
	 *            some {@link SemanticGraph}
	 * @return the c-path complement with respect to the given subgraph
	 */
	public abstract SemanticGraph getCPathComplement(SemanticGraph subGraph);

	public static Set<Link> getLinks(Set<Set<Leaf>> cpaths) {
		Set<Link> links = new HashSet<Link>();
		for (Set<Leaf> cpath : cpaths) {
			for (Leaf leaf : cpath) {
				if (leaf instanceof Not) {
					Not not = (Not) leaf;
					for (Leaf otherLeaf : cpath) {
						if (otherLeaf instanceof Atom) {
							Atom atom = (Atom) otherLeaf;
							if (((Atom) not.formula).originalExpression == atom.originalExpression) {
								links.add(new Link(atom, not));
							}
						}
					}
				}
			}
		}
		return links;
	}

	public SemanticGraph getSmallestFullBlockContaining(Link link,
			Set<Set<Leaf>> allCPaths, Set<Set<Leaf>> allDPaths) {
		// This applies to every graph element except binary operators.
		return new EmptySemanticGraph();
	}

	public SemanticGraph getDissolvent(Link link, SemanticGraph fullBlock,
			Set<Set<Leaf>> allCPaths) {
		Set<Leaf> leafsOfLink = link.getLeafs();
		SemanticGraph chainSubgraph = getSubgraphRelativeTo(leafsOfLink);

		if (chainSubgraph.isCBlock(allCPaths)) {
			return fullBlock.getCPathComplement(chainSubgraph);
		}

		/*
		 * When dissolving wrt one single link, this case always matches, so the
		 * tests are commented out.
		 */

		// if (fullBlock instanceof And) {
		And conjunction = (And) fullBlock;
		SemanticGraph hx = conjunction.leftHandSide
				.getSubgraphRelativeTo(leafsOfLink);
		SemanticGraph hy = conjunction.rightHandSide
				.getSubgraphRelativeTo(leafsOfLink);
		// if (chainSubgraph.isEqualTo(new And(hx, hy))
		// && hx.isCBlock(allCPaths) && hy.isCBlock(allCPaths)) {
		return new Or(
		// We use deep copies in the first and, so that we don't
				// get duplicate Atoms in the left and right part of the
				// Or.
				new And(conjunction.leftHandSide, conjunction.rightHandSide
						.getCPathComplement(hy)), new And(
						conjunction.leftHandSide.getCPathComplement(hx)
								.deepCopy(), conjunction.rightHandSide
								.getCPathExtension(hy).deepCopy()));
		// } else {
		// System.out
		// .println("chainSubgraph != And(hx, hy) or hx or hy are no cblocks.");
		// }
		// } else {
		// GreqlEvaluator.println("fullBlock " + fullBlock + " containing " +
		// link
		// + " is a " + fullBlock.getClass().getSimpleName());
		// }
		//
		// // No case matches, so it's no dissolution chain.
		// return null;
	}

	public SemanticGraph replaceInGraph(SemanticGraph originalSubgraph,
			SemanticGraph newSubgraph) {
		if (originalSubgraph == this) {
			return newSubgraph;
		}
		return this;
	}

	public SemanticGraph simplify() {
		return this;
	}

	public abstract SemanticGraph deepCopy();

	public abstract boolean isEqualTo(SemanticGraph sg);
}
