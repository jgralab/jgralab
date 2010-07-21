package de.uni_koblenz.jgralab.algolib.visitors;

import java.util.LinkedList;
import java.util.List;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

public class DFSVisitorComposition extends SearchVisitorComposition implements
		DFSVisitor {
	private List<DFSVisitor> dfsVisitors;

	public DFSVisitorComposition() {
		createDfsVisitorsLazily();
	}

	private void createDfsVisitorsLazily() {
		if (dfsVisitors == null) {
			dfsVisitors = new LinkedList<DFSVisitor>();
		}
	}

	public void addDFSVisitor(DFSVisitor newVisitor) {
		createDfsVisitorsLazily();
		dfsVisitors.add(newVisitor);
	}

	public void removeDFSVisitor(DFSVisitor toRemove) {
		if (dfsVisitors != null) {
			dfsVisitors.remove(dfsVisitors);
			if (dfsVisitors.size() == 0) {
				clearDFSVisitors();
			}
		}
	}

	public void clearDFSVisitors() {
		dfsVisitors = null;
	}

	@Override
	public void leaveTreeEdge(Edge e) {
		if (dfsVisitors != null) {
			for (DFSVisitor currentVisitor : dfsVisitors) {
				currentVisitor.leaveTreeEdge(e);
			}
		}
	}

	@Override
	public void leaveVertex(Vertex v) {
		if (dfsVisitors != null) {
			for (DFSVisitor currentVisitor : dfsVisitors) {
				currentVisitor.leaveVertex(v);
			}
		}
	}

	@Override
	public void visitBackwardArc(Edge e) {
		if (dfsVisitors != null) {
			for (DFSVisitor currentVisitor : dfsVisitors) {
				currentVisitor.visitBackwardArc(e);
			}
		}
	}

	@Override
	public void visitCrosslink(Edge e) {
		if (dfsVisitors != null) {
			for (DFSVisitor currentVisitor : dfsVisitors) {
				currentVisitor.visitForwardArc(e);
			}
		}
	}

	@Override
	public void visitForwardArc(Edge e) {
		if (dfsVisitors != null) {
			for (DFSVisitor currentVisitor : dfsVisitors) {
				currentVisitor.visitForwardArc(e);
			}
		}
	}

	@Override
	public void visitFrond(Edge e) {
		super.visitFrond(e);
		if (dfsVisitors != null) {
			for (DFSVisitor currentVisitor : dfsVisitors) {
				currentVisitor.visitFrond(e);
			}
		}
	}

	@Override
	public void visitRoot(Vertex v) {
		super.visitRoot(v);
		if (dfsVisitors != null) {
			for (DFSVisitor currentVisitor : dfsVisitors) {
				currentVisitor.visitRoot(v);
			}
		}
	}

	@Override
	public void visitTreeEdge(Edge e) {
		super.visitTreeEdge(e);
		if (dfsVisitors != null) {
			for (DFSVisitor currentVisitor : dfsVisitors) {
				currentVisitor.visitTreeEdge(e);
			}
		}
	}

	@Override
	public void visitEdge(Edge e) {
		super.visitEdge(e);
		if (dfsVisitors != null) {
			for (DFSVisitor currentVisitor : dfsVisitors) {
				currentVisitor.visitEdge(e);
			}
		}
	}

	@Override
	public void visitVertex(Vertex v) {
		super.visitVertex(v);
		if (dfsVisitors != null) {
			for (DFSVisitor currentVisitor : dfsVisitors) {
				currentVisitor.visitVertex(v);
			}
		}
	}

	@Override
	public void reset() {
		super.reset();
		if (dfsVisitors != null) {
			for (DFSVisitor currentVisitor : dfsVisitors) {
				currentVisitor.reset();
			}
		}
	}

}
