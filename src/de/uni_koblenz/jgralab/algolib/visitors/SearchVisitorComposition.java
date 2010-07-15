package de.uni_koblenz.jgralab.algolib.visitors;

import java.util.LinkedList;
import java.util.List;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

public class SearchVisitorComposition extends SearchVisitorAdapter {

	private List<SearchVisitor> visitors;
	
	public SearchVisitorComposition(SearchVisitor initialVisitor){
		visitors = new LinkedList<SearchVisitor>();
		visitors.add(initialVisitor);
	}

	@Override
	public void visitFrond(Edge e) {
		for (SearchVisitor currentVisitor : visitors) {
			currentVisitor.visitFrond(e);
		}
	}

	@Override
	public void visitRoot(Vertex v) {
		for (SearchVisitor currentVisitor : visitors) {
			currentVisitor.visitRoot(v);
		}
	}

	@Override
	public void visitTreeEdge(Edge e) {
		for (SearchVisitor currentVisitor : visitors) {
			currentVisitor.visitTreeEdge(e);
		}
	}

	@Override
	public void visitEdge(Edge e) {
		for (SearchVisitor currentVisitor : visitors) {
			currentVisitor.visitEdge(e);
		}
	}

	@Override
	public void visitVertex(Vertex v) {
		for (SearchVisitor currentVisitor : visitors) {
			currentVisitor.visitVertex(v);
		}
	}
	
	

}
