package de.uni_koblenz.jgralab.algolib.visitors;

import java.util.HashSet;
import java.util.Set;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

public class SearchVisitorComposition extends SimpleVisitorComposition
		implements SearchVisitor {

	private Set<SearchVisitor> searchVisitors;

	public SearchVisitorComposition() {
		searchVisitors = new HashSet<SearchVisitor>();
	}
	
	public void addSearchVisitor(SearchVisitor newVisitor){
		searchVisitors.add(newVisitor);
	}
	
	public void removeSearchVisitor(SearchVisitor toRemove){
		searchVisitors.remove(toRemove);
	}
	
	public void clearSearchVisitors(){
		searchVisitors.clear();
	}

	@Override
	public void visitFrond(Edge e) {
		for (SearchVisitor currentVisitor : searchVisitors) {
			currentVisitor.visitFrond(e);
		}
	}

	@Override
	public void visitRoot(Vertex v) {
		for (SearchVisitor currentVisitor : searchVisitors) {
			currentVisitor.visitRoot(v);
		}
	}

	@Override
	public void visitTreeEdge(Edge e) {
		for (SearchVisitor currentVisitor : searchVisitors) {
			currentVisitor.visitTreeEdge(e);
		}
	}

	@Override
	public void visitEdge(Edge e) {
		super.visitEdge(e);
		for (SearchVisitor currentVisitor : searchVisitors) {
			currentVisitor.visitEdge(e);
		}
	}

	@Override
	public void visitVertex(Vertex v) {
		super.visitVertex(v);
		for (SearchVisitor currentVisitor : searchVisitors) {
			currentVisitor.visitVertex(v);
		}
	}

	@Override
	public void reset() {
		super.reset();
		for (SearchVisitor currentVisitor : searchVisitors) {
			currentVisitor.reset();
		}
	}

}
