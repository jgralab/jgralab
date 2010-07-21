package de.uni_koblenz.jgralab.algolib.visitors;

import java.util.LinkedList;
import java.util.List;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

public class SearchVisitorComposition extends SimpleVisitorComposition
		implements SearchVisitor {

	private List<SearchVisitor> searchVisitors;

	public SearchVisitorComposition() {

	}

	private void createSearchVisitorsLazily() {
		if (searchVisitors == null) {
			searchVisitors = new LinkedList<SearchVisitor>();
		}
	}

	public void addSearchVisitor(SearchVisitor newVisitor) {
		createSearchVisitorsLazily();
		searchVisitors.add(newVisitor);
	}

	public void removeSearchVisitor(SearchVisitor toRemove) {
		if (searchVisitors != null) {
			searchVisitors.remove(toRemove);
			if (searchVisitors.size() == 0) {
				clearSearchVisitors();
			}
		}
	}

	public void clearSearchVisitors() {
		searchVisitors = null;
	}

	@Override
	public void visitFrond(Edge e) {
		if (searchVisitors != null) {
			for (SearchVisitor currentVisitor : searchVisitors) {
				currentVisitor.visitFrond(e);
			}
		}
	}

	@Override
	public void visitRoot(Vertex v) {
		if (searchVisitors != null) {
			for (SearchVisitor currentVisitor : searchVisitors) {
				currentVisitor.visitRoot(v);
			}
		}
	}

	@Override
	public void visitTreeEdge(Edge e) {
		if (searchVisitors != null) {
			for (SearchVisitor currentVisitor : searchVisitors) {
				currentVisitor.visitTreeEdge(e);
			}
		}
	}

	@Override
	public void visitEdge(Edge e) {
		super.visitEdge(e);
		if (searchVisitors != null) {
			for (SearchVisitor currentVisitor : searchVisitors) {
				currentVisitor.visitEdge(e);
			}
		}
	}

	@Override
	public void visitVertex(Vertex v) {
		super.visitVertex(v);
		if (searchVisitors != null) {
			for (SearchVisitor currentVisitor : searchVisitors) {
				currentVisitor.visitVertex(v);
			}
		}
	}

	@Override
	public void reset() {
		super.reset();
		if (searchVisitors != null) {
			for (SearchVisitor currentVisitor : searchVisitors) {
				currentVisitor.reset();
			}
		}
	}

}
