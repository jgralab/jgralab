package de.uni_koblenz.jgralab.algolib.visitors;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

public class DFSVisitorComposition extends SearchVisitorComposition implements
		DFSVisitor {

	@Override
	public void leaveTreeEdge(Edge e) {
		if (visitors != null) {
			for (Visitor currentVisitor : visitors) {
				if (currentVisitor instanceof DFSVisitor) {
					((DFSVisitor) currentVisitor).leaveTreeEdge(e);
				}
			}
		}
	}

	@Override
	public void leaveVertex(Vertex v) {
		if (visitors != null) {
			for (Visitor currentVisitor : visitors) {
				if (currentVisitor instanceof DFSVisitor) {
					((DFSVisitor) currentVisitor).leaveVertex(v);
				}
			}
		}
	}

	@Override
	public void visitBackwardArc(Edge e) {
		if (visitors != null) {
			for (Visitor currentVisitor : visitors) {
				if (currentVisitor instanceof DFSVisitor) {
					((DFSVisitor) currentVisitor).visitBackwardArc(e);
				}
			}
		}
	}

	@Override
	public void visitCrosslink(Edge e) {
		if (visitors != null) {
			for (Visitor currentVisitor : visitors) {
				if (currentVisitor instanceof DFSVisitor) {
					((DFSVisitor) currentVisitor).visitForwardArc(e);
				}
			}
		}
	}

	@Override
	public void visitForwardArc(Edge e) {
		if (visitors != null) {
			for (Visitor currentVisitor : visitors) {
				if (currentVisitor instanceof DFSVisitor) {
					((DFSVisitor) currentVisitor).visitForwardArc(e);
				}
			}
		}
	}
}
