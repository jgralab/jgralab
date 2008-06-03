package de.uni_koblenz.jgralab.greql2.optimizer.dissolution;

import java.util.HashSet;
import java.util.Set;

import de.uni_koblenz.jgralab.greql2.optimizer.OptimizerUtility;
import de.uni_koblenz.jgralab.greql2.schema.Expression;
import de.uni_koblenz.jgralab.greql2.schema.FunctionApplication;
import de.uni_koblenz.jgralab.greql2.schema.FunctionId;
import de.uni_koblenz.jgralab.greql2.schema.Greql2;

public class And extends BinaryOperator {

	@Override
	public boolean isEqualTo(SemanticGraph sg) {
		if (sg instanceof And) {
			And and = (And) sg;
			return leftHandSide.isEqualTo(and.leftHandSide)
					&& rightHandSide.isEqualTo(and.rightHandSide);
		}
		return false;
	}

	public And(SemanticGraph lhs, SemanticGraph rhs) {
		super(lhs, rhs);
	}

	@Override
	public String toString() {
		return "(" + leftHandSide.toString() + " & " + rightHandSide.toString()
				+ ")";
	}

	@Override
	public Expression toExpression(Greql2 syntaxgraph) {
		FunctionApplication and = syntaxgraph.createFunctionApplication();
		FunctionId andId = OptimizerUtility.findOrCreateFunctionId("and",
				syntaxgraph);
		syntaxgraph.createIsFunctionIdOf(andId, and);
		syntaxgraph.createIsArgumentOf(leftHandSide.toExpression(syntaxgraph),
				and);
		syntaxgraph.createIsArgumentOf(rightHandSide.toExpression(syntaxgraph),
				and);
		return and;
	}

	@Override
	public Set<Set<Leaf>> getCPaths() {
		Set<Set<Leaf>> leftCPaths = leftHandSide.getCPaths();
		Set<Set<Leaf>> rightCPaths = rightHandSide.getCPaths();
		Set<Set<Leaf>> myCPaths = new HashSet<Set<Leaf>>(leftCPaths.size()
				* rightCPaths.size(), 1);
		Set<Leaf> mergedCPath;
		for (Set<Leaf> leftCPath : leftCPaths) {
			for (Set<Leaf> rightCPath : rightCPaths) {
				mergedCPath = new HashSet<Leaf>(leftCPath.size()
						+ rightCPath.size(), 1);
				mergedCPath.addAll(leftCPath);
				mergedCPath.addAll(rightCPath);
				myCPaths.add(mergedCPath);
			}
		}
		return myCPaths;
	}

	@Override
	public Set<Set<Leaf>> getDPaths() {
		Set<Set<Leaf>> leftDPaths = leftHandSide.getDPaths();
		Set<Set<Leaf>> rightDPaths = rightHandSide.getDPaths();
		Set<Set<Leaf>> myDPaths = new HashSet<Set<Leaf>>(leftDPaths.size()
				+ rightDPaths.size(), 1);
		myDPaths.addAll(leftDPaths);
		myDPaths.addAll(rightDPaths);
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
		return new And(leftSubformula, rightSubformula);
	}

	@Override
	public SemanticGraph getCPathExtension(SemanticGraph subGraph) {
		if (subGraph instanceof EmptySemanticGraph) {
			return new EmptySemanticGraph();
		}

		if (subGraph.isEqualTo(this)) {
			return this;
		}

		Set<Leaf> subGraphNodes = subGraph.getLeafs();
		Set<Leaf> lhsLeafs = leftHandSide.getLeafs();
		Set<Leaf> rhsLeafs = rightHandSide.getLeafs();

		SemanticGraph lhsGraph;
		if (!intersection(subGraphNodes, lhsLeafs).isEmpty())
			lhsGraph = leftHandSide.getCPathExtension(subGraph
					.getSubgraphRelativeTo(lhsLeafs));
		else
			lhsGraph = leftHandSide;

		SemanticGraph rhsGraph;
		if (!intersection(subGraphNodes, rhsLeafs).isEmpty())
			rhsGraph = rightHandSide.getCPathExtension(subGraph
					.getSubgraphRelativeTo(rhsLeafs));
		else
			rhsGraph = rightHandSide;

		return new And(lhsGraph, rhsGraph);
	}

	@Override
	public SemanticGraph getCPathComplement(SemanticGraph subGraph) {
		if (subGraph instanceof EmptySemanticGraph) {
			return this;
		}

		if (subGraph.isEqualTo(this)) {
			return new EmptySemanticGraph();
		}

		Set<Leaf> subGraphNodes = subGraph.getLeafs();
		Set<Leaf> lhsLeafs = leftHandSide.getLeafs();
		Set<Leaf> rhsLeafs = rightHandSide.getLeafs();

		SemanticGraph lhsGraph;
		if (!intersection(subGraphNodes, lhsLeafs).isEmpty())
			lhsGraph = leftHandSide.getCPathComplement(subGraph
					.getSubgraphRelativeTo(lhsLeafs));
		else
			lhsGraph = leftHandSide;

		SemanticGraph rhsGraph;
		if (!intersection(subGraphNodes, rhsLeafs).isEmpty())
			rhsGraph = rightHandSide.getCPathComplement(subGraph
					.getSubgraphRelativeTo(rhsLeafs));
		else
			rhsGraph = rightHandSide;

		return new And(lhsGraph, rhsGraph);
	}

	@Override
	public SemanticGraph simplify() {
		leftHandSide = leftHandSide.simplify();
		if (leftHandSide instanceof EmptySemanticGraph)
			return leftHandSide;

		rightHandSide = rightHandSide.simplify();
		if (rightHandSide instanceof EmptySemanticGraph) {
			return rightHandSide;
		}

		return this;
	}

	@Override
	public SemanticGraph deepCopy() {
		return new And(leftHandSide.deepCopy(), rightHandSide.deepCopy());
	}
}
