package de.uni_koblenz.jgralabtest.algolib;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.DFSAlgorithmVisitor;
import de.uni_koblenz.jgralab.algolib.functions.IntFunction;

public class DebugSearchVisitor extends DFSAlgorithmVisitor {

	public static String generateEdgeString(Edge e) {
		if (e == null) {
			return null;
		}
		StringBuilder out = new StringBuilder();
		out.append("(");
		out.append(e.getThis());
		out.append(")");
		if (!e.isNormal()) {
			out.append("<");
		}
		out.append("--");
		out.append("(");
		out.append(e);
		out.append(")");
		out.append("--");
		if (e.isNormal()) {
			out.append(">");
		}
		out.append("(");
		out.append(e.getThat());
		out.append(")");
		// return "(" + e.getThis() + ")" + "--" + "(" + e + ")" + "-->" + "("
		// + e.getThat() + ")";
		return out.toString();
	}

	protected IntFunction<Vertex> level;

	private void printIndent(Vertex v) {
		int amount = level.isDefined(v) ? level.get(v) : 0;
		for (int i = 0; i < amount; i++) {
			System.out.print("     ");
		}
	}

	@Override
	public void leaveTreeEdge(Edge e) {
		printIndent(e.getThis());
		System.out.println("Leaving tree edge " + generateEdgeString(e));
	}

	@Override
	public void leaveVertex(Vertex v) {
		printIndent(v);
		System.out.println("Leaving vertex " + v);
	}

	@Override
	public void visitBackwardArc(Edge e) {
		printIndent(e.getThis());
		System.out.println("Visiting backward arc " + generateEdgeString(e));
	}

	@Override
	public void visitCrosslink(Edge e) {
		printIndent(e.getThis());
		System.out.println("Visiting crosslink " + generateEdgeString(e));
	}

	@Override
	public void visitForwardArc(Edge e) {
		printIndent(e.getThis());
		System.out.println("Visiting forward arc " + generateEdgeString(e));
	}

	@Override
	public void visitFrond(Edge e) {
		printIndent(e.getThis());
		System.out.println("Visiting frond " + generateEdgeString(e));
	}

	@Override
	public void visitRoot(Vertex v) {
		printIndent(v);
		System.out.println("Visiting root " + v);
	}

	@Override
	public void visitTreeEdge(Edge e) {
		printIndent(e.getThis());
		System.out.println("Visiting tree edge " + generateEdgeString(e));
	}

	@Override
	public void visitEdge(Edge e) {
		printIndent(e.getThis());
		System.out.println("Visiting edge " + generateEdgeString(e));
	}

	@Override
	public void visitVertex(Vertex v) {
		printIndent(v);
		System.out.println("Visiting vertex " + v);
	}

	@Override
	public void reset() {
		level = algorithm.getInternalLevel();
	}

	@Override
	public void setAlgorithm(GraphAlgorithm alg) {
		super.setAlgorithm(alg);
		reset();
	}

}
