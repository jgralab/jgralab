package de.uni_koblenz.jgralabtest.algolib;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.AlternativeDFSVisitorComposition;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.DFSVisitor;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.DFSVisitorComposition;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.SearchVisitor;
import de.uni_koblenz.jgralab.algolib.visitors.GraphVisitor;
import de.uni_koblenz.jgralab.algolib.visitors.GraphVisitorAdapter;

/**
 * This class compares the speed of the two different visitor composition
 * implementations. The current one is implemented with instanceof checks in
 * every visit step. The alternative one is implemented with instanceof checks
 * while adding new visitors, but not in the visit methods. This is done using
 * redundant storing of visitors in the corresponding composition classes.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public class CompareWithAlternativeVisitorCompositions {
	private static final int GRAPH_VISITOR_COUNT = 0;
	private static final int SEARCH_VISITOR_COUNT = 2;
	private static final int DFS_VISITOR_COUNT = 0;
	private static final int ITERATIONS = 10000000;
	private static final int RUNS = 100;
	private static final int IGNORE = 10; // number of best and worst times to

	// ignore

	private static class GraphVisitorExample extends GraphVisitorAdapter {
		protected int j;

		public GraphVisitorExample(int number) {
			this.j = number;
		}

		@Override
		public void visitEdge(Edge e) {
			// System.out.println(this.getClass().getSimpleName() + " " + j
			// + ": visitEdge.");
		}

		@Override
		public void visitVertex(Vertex v) {
			// System.out.println(this.getClass().getSimpleName() + " " + j
			// + ": visitVertex.");
		}

	}

	private static class SearchVisitorExample extends GraphVisitorExample
			implements SearchVisitor {

		public SearchVisitorExample(int number) {
			super(number);
		}

		@Override
		public void visitFrond(Edge e) {
			// System.out.println(this.getClass().getSimpleName() + " " + j
			// + ": visitFrond.");
		}

		@Override
		public void visitRoot(Vertex v) {
			// System.out.println(this.getClass().getSimpleName() + " " + j
			// + ": visitRoot.");
		}

		@Override
		public void visitTreeEdge(Edge e) {
			// System.out.println(this.getClass().getSimpleName() + " " + j
			// + ": visitTreeEdge");
		}

	}

	private static class DFSVisitorExample extends SearchVisitorExample
			implements DFSVisitor {

		public DFSVisitorExample(int number) {
			super(number);
		}

		@Override
		public void leaveTreeEdge(Edge e) {
			// System.out.println(this.getClass().getSimpleName() + " " + j
			// + ": leaveTreeEdge.");
		}

		@Override
		public void leaveVertex(Vertex v) {
			// System.out.println(this.getClass().getSimpleName() + " " + j
			// + ": leaveVertex.");
		}

		@Override
		public void visitBackwardArc(Edge e) {
			// System.out.println(this.getClass().getSimpleName() + " " + j
			// + ": visitBackwardArc.");
		}

		@Override
		public void visitCrosslink(Edge e) {
			// System.out.println(this.getClass().getSimpleName() + " " + j
			// + ": visitCrosslink.");
		}

		@Override
		public void visitForwardArc(Edge e) {
			// System.out.println(this.getClass().getSimpleName() + " " + j
			// + ": visitForwardArc.");
		}

	}

	public static void main(String[] args) {
		Stopwatch sw = new Stopwatch();

		GraphVisitor[] graphVisitors = new GraphVisitor[GRAPH_VISITOR_COUNT];
		SearchVisitor[] searchVisitors = new SearchVisitor[SEARCH_VISITOR_COUNT];
		DFSVisitor[] dfsVisitors = new DFSVisitor[DFS_VISITOR_COUNT];

		for (int i = 0; i < GRAPH_VISITOR_COUNT; i++) {
			graphVisitors[i] = new GraphVisitorExample(i);
		}

		for (int i = 0; i < SEARCH_VISITOR_COUNT; i++) {
			searchVisitors[i] = new SearchVisitorExample(i);
		}

		for (int i = 0; i < DFS_VISITOR_COUNT; i++) {
			dfsVisitors[i] = new DFSVisitorExample(i);
		}

		DFSVisitorComposition comp = new DFSVisitorComposition();
		AlternativeDFSVisitorComposition acomp = new AlternativeDFSVisitorComposition();

		for (GraphVisitor currentVisitor : graphVisitors) {
			comp.addVisitor(currentVisitor);
			acomp.addVisitor(currentVisitor);
		}
		for (SearchVisitor currentVisitor : searchVisitors) {
			comp.addVisitor(currentVisitor);
			acomp.addVisitor(currentVisitor);
		}
		for (DFSVisitor currentVisitor : dfsVisitors) {
			comp.addVisitor(currentVisitor);
			acomp.addVisitor(currentVisitor);
		}

		long[] average = new long[RUNS];

		System.out.println("Current implementation:");
		for (int k = 0; k < RUNS; k++) {
			sw.reset();
			sw.start();
			for (int i = 0; i < ITERATIONS; i++) {
				comp.visitVertex(null);
				comp.visitEdge(null);
				comp.visitRoot(null);
				comp.visitTreeEdge(null);
				comp.visitFrond(null);
				comp.leaveVertex(null);
				comp.leaveTreeEdge(null);
				comp.visitForwardArc(null);
				comp.visitBackwardArc(null);
				comp.visitCrosslink(null);
			}
			sw.stop();
			System.out.print(".");
			System.out.flush();
			// System.out.println(sw.getDurationString());
			average[k] = sw.getNanoDuration();
		}
		System.out.println();
		printResult(average);
		System.out.println();

		System.out.println("Alternative implementation:");
		for (int k = 0; k < RUNS; k++) {
			sw.reset();
			sw.start();
			for (int i = 0; i < ITERATIONS; i++) {
				acomp.visitVertex(null);
				acomp.visitEdge(null);
				acomp.visitRoot(null);
				acomp.visitTreeEdge(null);
				acomp.visitFrond(null);
				acomp.leaveVertex(null);
				acomp.leaveTreeEdge(null);
				acomp.visitForwardArc(null);
				acomp.visitBackwardArc(null);
				acomp.visitCrosslink(null);
			}
			sw.stop();
			System.out.print(".");
			System.out.flush();
			// System.out.println(sw.getDurationString());
			average[k] = sw.getNanoDuration();
		}
		System.out.println();
		printResult(average);
		System.out.println("Fini.");
	}

	private static void printResult(long[] average) {
		long sum = 0;
		for (int i = 0 + IGNORE; i < average.length - IGNORE; i++) {
			sum += average[i];
		}
		System.out.println("Average: "
				+ (sum / ((RUNS - 2 * IGNORE) * 1000.0 * 1000.0 * 1000.0))
				+ " sec");
	}
}
