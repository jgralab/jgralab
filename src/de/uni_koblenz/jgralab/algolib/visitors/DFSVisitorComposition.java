package de.uni_koblenz.jgralab.algolib.visitors;

import java.util.HashSet;
import java.util.Set;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

public class DFSVisitorComposition extends SearchVisitorComposition implements
		DFSVisitor {
	private Set<DFSVisitor> dfsVisitors;

	public DFSVisitorComposition() {
		dfsVisitors = new HashSet<DFSVisitor>();
	}

	public void addDFSVisitor(DFSVisitor newVisitor) {
		dfsVisitors.add(newVisitor);
	}

	public void removeDFSVisitor(DFSVisitor toRemove) {
		dfsVisitors.remove(dfsVisitors);
	}

	public void clearDFSVisitors() {
		dfsVisitors.clear();
	}

	@Override
	public void leaveTreeEdge(Edge e) {
		for (DFSVisitor currentVisitor : dfsVisitors) {
			currentVisitor.leaveTreeEdge(e);
		}
	}

	@Override
	public void leaveVertex(Vertex v) {
		for (DFSVisitor currentVisitor : dfsVisitors) {
			currentVisitor.leaveVertex(v);
		}
	}

	@Override
	public void visitBackwardArc(Edge e) {
		for (DFSVisitor currentVisitor : dfsVisitors) {
			currentVisitor.visitBackwardArc(e);
		}
	}

	@Override
	public void visitCrosslink(Edge e) {
		for (DFSVisitor currentVisitor : dfsVisitors) {
			currentVisitor.visitForwardArc(e);
		}
	}

	@Override
	public void visitForwardArc(Edge e) {
		for (DFSVisitor currentVisitor : dfsVisitors) {
			currentVisitor.visitForwardArc(e);
		}
	}

	@Override
	public void visitFrond(Edge e) {
		super.visitFrond(e);
		for (DFSVisitor currentVisitor : dfsVisitors) {
			currentVisitor.visitFrond(e);
		}
	}

	@Override
	public void visitRoot(Vertex v) {
		super.visitRoot(v);
		for (DFSVisitor currentVisitor : dfsVisitors) {
			currentVisitor.visitRoot(v);
		}
	}

	@Override
	public void visitTreeEdge(Edge e) {
		super.visitTreeEdge(e);
		for (DFSVisitor currentVisitor : dfsVisitors) {
			currentVisitor.visitTreeEdge(e);
		}
	}

	@Override
	public void visitEdge(Edge e) {
		super.visitEdge(e);
		for (DFSVisitor currentVisitor : dfsVisitors) {
			currentVisitor.visitEdge(e);
		}
	}

	@Override
	public void visitVertex(Vertex v) {
		super.visitVertex(v);
		for (DFSVisitor currentVisitor : dfsVisitors) {
			currentVisitor.visitVertex(v);
		}
	}

	@Override
	public void reset() {
		super.reset();
		for (DFSVisitor currentVisitor : dfsVisitors) {
			currentVisitor.reset();
		}
	}

}
