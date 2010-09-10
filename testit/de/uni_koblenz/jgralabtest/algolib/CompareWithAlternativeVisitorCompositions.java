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

	private static final int VISITOR_COUNT_PER_TYPE = 10;
	private static final int ITERATIONS = 1000000;

	public static void main(String[] args) {
		Stopwatch sw = new Stopwatch();

		GraphVisitor[] graphVisitors = new GraphVisitor[VISITOR_COUNT_PER_TYPE];
		SearchVisitor[] searchVisitors = new SearchVisitor[VISITOR_COUNT_PER_TYPE];
		DFSVisitor[] dfsVisitors = new DFSVisitor[VISITOR_COUNT_PER_TYPE];

		for (int i = 0; i < VISITOR_COUNT_PER_TYPE; i++) {
			graphVisitors[i] = new GraphVisitorExample(i);

			searchVisitors[i] = new SearchVisitorExample(i);

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

		System.out.println("Current implementation:");
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
		System.out.println(sw.getDurationString());

		System.out.println("Alternative implementation:");
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
		System.out.println(sw.getDurationString());

	}
}
