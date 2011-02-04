package de.uni_koblenz.jgralabtest.algolib.nonjunit;

import java.util.HashSet;
import java.util.Set;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.AlgorithmTerminatedException;
import de.uni_koblenz.jgralab.algolib.algorithms.search.BreadthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.visitors.SearchVisitorAdapter;
import de.uni_koblenz.jgralab.algolib.algorithms.weak_components.WeakComponentsWithBFS;
import de.uni_koblenz.jgralab.algolib.functions.Function;
import de.uni_koblenz.jgralab.algolib.problems.WeakComponentsSolver;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;

public class TryWeakComponents {

	private static final int KAPPA = 500;
	private static final int VERTICES_PER_COMPONENT = 1000;
	private static final int ADDITIONAL_EDGES_PER_COMPONENT = 500;

	public static void main(String[] args) {
		SimpleGraph graph = RandomGraph.createEmptyGraph();
		for(int i = 0; i < KAPPA; i++){
			RandomGraph.addWeakComponent(0, graph, VERTICES_PER_COMPONENT, ADDITIONAL_EDGES_PER_COMPONENT);
		}
		System.out.println("Expected Kappa: " + KAPPA);
		BreadthFirstSearch bfs = new BreadthFirstSearch(graph);
		final Set<Vertex> representatives = new HashSet<Vertex>();
		bfs.addVisitor(new SearchVisitorAdapter(){

			@Override
			public void visitRoot(Vertex v) throws AlgorithmTerminatedException {
				representatives.add(v);
			}
			
		});
		WeakComponentsSolver solver = new WeakComponentsWithBFS(graph, bfs);
		try {
			solver.execute();
		} catch (AlgorithmTerminatedException e) {
		}
		System.out.println("Computed kappa: " + solver.getKappa());
		
		Function<Vertex, Vertex> weakComponents = solver.getWeakComponents();
		
		System.out.println("Representative vertices: " + representatives.size());
		
		for(Vertex current : graph.vertices()){
			Vertex currentRep = weakComponents.get(current);
			if(!representatives.contains(currentRep)){
				System.err.println("Wrong representative: " + currentRep);
			}
		}
		
		System.out.println("Fini.");
		
	}
}
