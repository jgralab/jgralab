package de.uni_koblenz.jgralab.greql2.optimizer.dissolution;

import java.util.Set;

public abstract class BinaryOperator extends SemanticGraph {

	protected SemanticGraph leftHandSide;
	protected SemanticGraph rightHandSide;

	public BinaryOperator(SemanticGraph leftHS, SemanticGraph rightHS) {
		leftHandSide = leftHS;
		rightHandSide = rightHS;
	}

	@Override
	public SemanticGraph toNegationNormalForm() {
		leftHandSide = leftHandSide.toNegationNormalForm();
		rightHandSide = rightHandSide.toNegationNormalForm();
		return this;
	}

	@Override
	public Set<Leaf> getLeafs() {
		Set<Leaf> leafs = leftHandSide.getLeafs();
		leafs.addAll(rightHandSide.getLeafs());
		return leafs;
	}

	@Override
	public SemanticGraph getSmallestFullBlockContaining(Link link,
			Set<Set<Leaf>> allCPaths, Set<Set<Leaf>> allDPaths) {
		SemanticGraph leftFB = leftHandSide.getSmallestFullBlockContaining(
				link, allCPaths, allDPaths);
		if (!(leftFB instanceof EmptySemanticGraph)) {
			return leftFB;
		}

		SemanticGraph rightFB = rightHandSide.getSmallestFullBlockContaining(
				link, allCPaths, allDPaths);
		if (!(rightFB instanceof EmptySemanticGraph)) {
			return rightFB;
		}

		if (link.isContainedIn(getLeafs()) && isFullBlock(allCPaths, allDPaths))
			return this;

		return new EmptySemanticGraph();
	}

	@Override
	public SemanticGraph replaceInGraph(SemanticGraph originalSubgraph,
			SemanticGraph newSubgraph) {
		if (originalSubgraph == this) {
			return newSubgraph;
		}

		leftHandSide = leftHandSide.replaceInGraph(originalSubgraph,
				newSubgraph);
		rightHandSide = rightHandSide.replaceInGraph(originalSubgraph,
				newSubgraph);

		return this;
	}

	@Override
	public long getCosts() {
		// And, Or, Xor cost 2 according to funlib
		return leftHandSide.getCosts() + rightHandSide.getCosts() + 2;
	}
}
