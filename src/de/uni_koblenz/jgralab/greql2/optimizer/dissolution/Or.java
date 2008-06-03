package de.uni_koblenz.jgralab.greql2.optimizer.dissolution;

import java.util.HashSet;
import java.util.Set;

import de.uni_koblenz.jgralab.greql2.optimizer.OptimizerUtility;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.FunctionId;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;

public class Or extends BinaryOperator {

	public Or(SemanticGraph lhs, SemanticGraph rhs) {
		super(lhs, rhs);
	}

	@Override
	public String toString() {
		return "(" + leftHandSide.toString() + " | " + rightHandSide.toString()
				+ ")";
	}

	@Override
	public boolean isEqualTo(SemanticGraph sg) {
		if (sg instanceof Or) {
			Or or = (Or) sg;
			return leftHandSide.isEqualTo(or.leftHandSide)
					&& rightHandSide.isEqualTo(or.rightHandSide);
		}
		return false;
	}

	@Override
	public Expression toExpression(Greql2 syntaxgraph) {
		FunctionApplication or = syntaxgraph.createFunctionApplication();
		FunctionId orId = OptimizerUtility.findOrCreateFunctionId("or",
				syntaxgraph);
		syntaxgraph.createIsFunctionIdOf(orId, or);
		syntaxgraph.createIsArgumentOf(leftHandSide.toExpression(syntaxgraph),
				or);
		syntaxgraph.createIsArgumentOf(rightHandSide.toExpression(syntaxgraph),
				or);
		return or;
	}

	@Override
	public Set<Set<Leaf>> getCPaths() {
		Set<Set<Leaf>> leftCPaths = leftHandSide.getCPaths();
		Set<Set<Leaf>> rightCPaths = rightHandSide.getCPaths();
		Set<Set<Leaf>> myCPaths = new HashSet<Set<Leaf>>(leftCPaths.size()
				+ rightCPaths.size(), 1);
		myCPaths.addAll(leftCPaths);
		myCPaths.addAll(rightCPaths);
		return myCPaths;
	}

	@Override
	public Set<Set<Leaf>> getDPaths() {
		Set<Set<Leaf>> leftDPaths = leftHandSide.getDPaths();
		Set<Set<Leaf>> rightDPaths = rightHandSide.getDPaths();
		Set<Set<Leaf>> myDPaths = new HashSet<Set<Leaf>>(leftDPaths.size()
				* rightDPaths.size(), 1);
		Set<Leaf> mergedDPath;
		for (Set<Leaf> leftDPath : leftDPaths) {
			for (Set<Leaf> rightDPath : rightDPaths) {
				mergedDPath = new HashSet<Leaf>(leftDPath.size()
						+ rightDPath.size(), 1);
				mergedDPath.addAll(leftDPath);
				mergedDPath.addAll(rightDPath);
				myDPaths.add(mergedDPath);
			}
		}
		return myDPaths;
	}

	@Override
	public SemanticGraph getSubgraphRelativeTo(Set<Leaf> leafs) {
		Set<Leaf> leafsInThisFormula = getLeafs();
		if (leafs.containsAll(leafsInThisFormula)) {
			return this;
		}

		if (intersection(leafsInThisFormula, leafs).isEmpty()) {
			return new EmptySemanticGraph();
		}

		if (intersection(leftHandSide.getLeafs(), leafs).isEmpty()) {
			return rightHandSide.getSubgraphRelativeTo(leafs);
		}

		if (intersection(rightHandSide.getLeafs(), leafs).isEmpty()) {
			return leftHandSide.getSubgraphRelativeTo(leafs);
		}

		SemanticGraph leftSubformula = leftHandSide
				.getSubgraphRelativeTo(leafs);
		SemanticGraph rightSubformula = rightHandSide
				.getSubgraphRelativeTo(leafs);
		return new Or(leftSubformula, rightSubformula);
	}

	@Override
	public SemanticGraph getCPathExtension(SemanticGraph subGraph) {
		if (subGraph instanceof EmptySemanticGraph) {
			return new EmptySemanticGraph();
		}

		if (subGraph.isEqualTo(this)) {
			return this;
		}

		SemanticGraph leftCPE = leftHandSide.getCPathExtension(subGraph
				.getSubgraphRelativeTo(leftHandSide.getLeafs()));
		SemanticGraph rightCPE = rightHandSide.getCPathExtension(subGraph
				.getSubgraphRelativeTo(rightHandSide.getLeafs()));

		return new Or(leftCPE, rightCPE);
	}

	@Override
	public SemanticGraph getCPathComplement(SemanticGraph subGraph) {
		if (subGraph instanceof EmptySemanticGraph) {
			return this;
		}

		if (subGraph.isEqualTo(this)) {
			return new EmptySemanticGraph();
		}

		SemanticGraph leftCPE = leftHandSide.getCPathComplement(subGraph
				.getSubgraphRelativeTo(leftHandSide.getLeafs()));
		SemanticGraph rightCPE = rightHandSide.getCPathComplement(subGraph
				.getSubgraphRelativeTo(rightHandSide.getLeafs()));

		return new Or(leftCPE, rightCPE);
	}

	@Override
	public SemanticGraph simplify() {
		leftHandSide = leftHandSide.simplify();
		rightHandSide = rightHandSide.simplify();

		if (leftHandSide instanceof EmptySemanticGraph) {
			return rightHandSide;
		}

		if (rightHandSide instanceof EmptySemanticGraph) {
			return leftHandSide;
		}

		return this;
	}

	@Override
	public SemanticGraph deepCopy() {
		return new Or(leftHandSide.deepCopy(), rightHandSide.deepCopy());
	}

}
