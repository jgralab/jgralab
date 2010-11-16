/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2010 Institute for Software Technology
 *               University of Koblenz-Landau, Germany
 * 
 *               ist@uni-koblenz.de
 * 
 * Please report bugs to http://serres.uni-koblenz.de/bugzilla
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package de.uni_koblenz.jgralabtest.algolib;

import java.util.Arrays;

import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.algolib.algorithms.search.BreadthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.SearchAlgorithm;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleSchema;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleVertex;

public class TryBFS {

	private static SimpleGraph getSmallGraph() {
		SimpleGraph graph = SimpleSchema.instance().createSimpleGraph();
		SimpleVertex v1 = graph.createSimpleVertex();
		SimpleVertex v2 = graph.createSimpleVertex();
		SimpleVertex v3 = graph.createSimpleVertex();
		SimpleVertex v4 = graph.createSimpleVertex();
		SimpleVertex v5 = graph.createSimpleVertex();
		graph.createSimpleVertex();
		graph.createSimpleEdge(v1, v2);
		graph.createSimpleEdge(v1, v4);
		graph.createSimpleEdge(v2, v1);
		graph.createSimpleEdge(v1, v3);
		// graph.createSimpleEdge(v1, v5);
		graph.createSimpleEdge(v3, v2);
		graph.createSimpleEdge(v2, v4);
		graph.createSimpleEdge(v3, v4);
		graph.createSimpleEdge(v4, v5);
		graph.createSimpleEdge(v5, v3);
		return graph;
	}

	private static Stopwatch sw;
	private static SimpleVertex v1;

	public static void main(String[] args) {
		sw = new Stopwatch();

		SimpleGraph graph = getSmallGraph();
		// SimpleGraph graph = RandomGraph.createEmptyGraph();
		// RandomGraph.addWeakComponent(0l, graph, 300000, 100000);

		v1 = graph.getFirstSimpleVertex();

		// bfs.addSearchVisitor(levelVisitor);
		// bfs.addSearchVisitor(numberVisitor);
		// bfs.addSearchVisitor(parentVisitor);
		// bfs.addSearchVisitor(new PauseVisitor());
		// bfs.setSearchDirection(EdgeDirection.IN);

		int amount = 1;

		System.out.println();

		System.out
				.println("Starting search without visitors for optional results:");

		System.out.println("No optional functions:");
		BreadthFirstSearch bfs2 = new BreadthFirstSearch(graph);
		bfs2.setTraversalDirection(EdgeDirection.IN);
		bfs2.addVisitor(new DebugSearchVisitor());

		// bfs2.reset();
		makeRunAndPrint(bfs2, amount);
		//
		// System.out.println("Compute number:");
		// bfs2 = bfs2.withNumber();
		// bfs2.reset();
		// makeRunAndPrint(bfs2, amount);
		//
		// System.out.println("Compute number, level and parent:");
		// bfs2 = bfs2.withLevel().withParent();
		// makeRunAndPrint(bfs2, amount);

		System.out.println("Fini");
	}

	private static void makeRunAndPrint(SearchAlgorithm bfs, int amount) {
		long average;
		average = makeRun(bfs, amount);
		System.out.print("Average time: ");
		printTime(average);
	}

	private static long makeRun(SearchAlgorithm bfs, int amount) {

		int skip = Math.round(amount * 0.1f);
		int skipUntil = amount - skip;

		long[] results = new long[amount];

		for (int i = 0; i < amount; i++) {
			bfs.reset();
			sw.reset();
			sw.start();
			bfs.execute(v1);
			sw.stop();
			results[i] = sw.getDuration();
		}

		Arrays.sort(results);

		double sum = 0;

		for (int i = skip; i < skipUntil; i++) {
			// printTime(results[i]);
			sum += results[i];
		}

		bfs.reset();
		return Math.round(sum / (skipUntil - skip));
	}

	private static void printTime(long millis) {
		System.out.println(millis / 1000.0);
	}
}
