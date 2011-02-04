package de.uni_koblenz.jgralabtest.algolib.nonjunit;

import java.util.Arrays;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.DFSVisitorList;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.DFSVisitorList;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.DFSVisitor;
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

	private static final int RUNS = 150;
	/**
	 * number of best and worst times to ignore
	 */
	private static final int IGNORE = 25;

	private static final int GRAPH_VISITOR_COUNT = 0;
	private static final int SEARCH_VISITOR_COUNT = 0;
	private static final int DFS_VISITOR_COUNT = 0;
	private static final int VERTEX_COUNT = 200000;
	private static final int FROND_COUNT = 100005;
	private static final int KAPPA = 5;
	private static final int TREE_EDGE_COUNT = VERTEX_COUNT - KAPPA;
	private static final int EDGE_COUNT = TREE_EDGE_COUNT + FROND_COUNT;

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

	public static void main(String[] args) throws AlgorithmTerminatedException {
		System.out
				.println("Simulating complete DFS with the following attributes:");
		System.out.println();
		System.out.println("Runs     : " + RUNS);
		System.out.println("Ignoring : " + IGNORE * 2);
		System.out.println();
		System.out.println("GraphVisitors  : " + GRAPH_VISITOR_COUNT);
		System.out.println("SearchVisitors : " + SEARCH_VISITOR_COUNT);
		System.out.println("DFS Visitors   : " + DFS_VISITOR_COUNT);
		System.out.println();
		System.out.println("Vertices : " + VERTEX_COUNT);
		System.out.println("Edges    : " + EDGE_COUNT);
		System.out.println("\u03f0        : " + KAPPA);
		System.out.println();
		System.out.println("Each dot represents a run.");
		System.out.println();
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

		DFSVisitorList comp = new DFSVisitorList();
		// TODO change alternative implementation class name
		DFSVisitorList acomp = new DFSVisitorList();

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

		System.out.println("Alternative implementation:");
		for (int k = 0; k < RUNS; k++) {
			oneRun(sw, acomp);
			average[k] = sw.getNanoDuration();
		}
		System.out.println();
		printResult(average);
		System.out.println();
		
		System.out.println("Current implementation:");
		for (int k = 0; k < RUNS; k++) {
			oneRun(sw, comp);
			average[k] = sw.getNanoDuration();
		}
		System.out.println();
		printResult(average);
		System.out.println();
		
		System.out.println("Fini.");
	}

	private static void oneRun(Stopwatch sw, DFSVisitor comp) throws AlgorithmTerminatedException {
		sw.reset();
		sw.start();
		for (int i = 0; i < KAPPA; i++) {
			comp.visitRoot(null);
		}
		for (int i = 0; i < VERTEX_COUNT; i++) {
			comp.visitVertex(null);
			comp.leaveVertex(null);
		}
		for (int i = 0; i < EDGE_COUNT; i++) {
			comp.visitEdge(null);
		}
		for (int i = 0; i < TREE_EDGE_COUNT; i++) {
			comp.visitTreeEdge(null);
			comp.leaveTreeEdge(null);
		}
		for (int i = 0, frondType = 0; i < FROND_COUNT; i++, frondType = (frondType + 1) % 3) {
			comp.visitFrond(null);
			switch (frondType) {
			case 0:
				comp.visitForwardArc(null);
				break;
			case 1:
				comp.visitBackwardArc(null);
				break;
			case 2:
				comp.visitCrosslink(null);
				break;
			}
		}
		sw.stop();
		System.out.print(".");
		System.out.flush();
	}

	private static void printResult(long[] average) {
		Arrays.sort(average);
		long sum = 0;
		for (int i = 0 + IGNORE; i < average.length - IGNORE; i++) {
			sum += average[i];
		}
		System.out.println("Average: "
				+ (sum / ((RUNS - 2 * IGNORE) * 1000.0 * 1000.0 * 1000.0))
				+ " sec");
	}
}
