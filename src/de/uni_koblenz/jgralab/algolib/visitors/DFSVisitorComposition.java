package de.uni_koblenz.jgralab.algolib.visitors;

import java.util.LinkedList;
import java.util.List;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

public class DFSVisitorComposition extends DFSVisitorAdapter {
	private List<DFSVisitor> visitors;

	public DFSVisitorComposition(DFSVisitor initialVisitor) {
		visitors = new LinkedList<DFSVisitor>();
		visitors.add(initialVisitor);
	}

	@Override
	public void leaveTreeEdge(Edge e) {
		for (DFSVisitor currentVisitor : visitors) {
			currentVisitor.leaveTreeEdge(e);
		}
	}

	@Override
	public void leaveVertex(Vertex v) {
		for (DFSVisitor currentVisitor : visitors) {
			currentVisitor.leaveVertex(v);
		}
	}

	@Override
	public void visitBackwardArc(Edge e) {
		for (DFSVisitor currentVisitor : visitors) {
			currentVisitor.visitBackwardArc(e);
		}
	}

	@Override
	public void visitCrosslink(Edge e) {
		for (DFSVisitor currentVisitor : visitors) {
			currentVisitor.visitForwardArc(e);
		}
	}

	@Override
	public void visitForwardArc(Edge e) {
		for (DFSVisitor currentVisitor : visitors) {
			currentVisitor.visitForwardArc(e);
		}
	}

	@Override
	public void visitFrond(Edge e) {
		for (DFSVisitor currentVisitor : visitors) {
			currentVisitor.visitFrond(e);
		}
	}

	@Override
	public void visitRoot(Vertex v) {
		for (DFSVisitor currentVisitor : visitors) {
			currentVisitor.visitRoot(v);
		}
	}

	@Override
	public void visitTreeEdge(Edge e) {
		for (DFSVisitor currentVisitor : visitors) {
			currentVisitor.visitTreeEdge(e);
		}
	}

	@Override
	public void visitEdge(Edge e) {
		for (DFSVisitor currentVisitor : visitors) {
			currentVisitor.visitEdge(e);
		}
	}

	@Override
	public void visitVertex(Vertex v) {
		for (DFSVisitor currentVisitor : visitors) {
			currentVisitor.visitVertex(v);
		}
	}

}
