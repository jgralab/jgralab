package de.uni_koblenz.jgralab.algolib.visitors;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.search.SearchAlgorithm;

public abstract class SearchVisitorAdapter extends SimpleVisitorAdapter implements
		SearchVisitor {

	protected SearchAlgorithm searchAlgorithm;
	
	@Override
	public void visitFrond(Edge e) {
		
	}

	@Override
	public void visitRoot(Vertex v) {
		
	}

	@Override
	public void visitTreeEdge(Edge e) {
		
	}

	public SearchAlgorithm getSearchAlgorithm() {
		return searchAlgorithm;
	}

	public void setSearchAlgorithm(SearchAlgorithm searchAlgorithm) {
		this.searchAlgorithm = searchAlgorithm;
	}
	
}
