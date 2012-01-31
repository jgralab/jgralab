package de.uni_koblenz.jgralabtest.non_junit_tests;

import java.util.HashMap;
import java.util.Map;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.GraphIOException;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;
import de.uni_koblenz.jgralab.schema.NamedElement;

public class GraphStatistics {
	static Map<NamedElement, Integer> counters = new HashMap<NamedElement, Integer>();

	public static void main(String[] args) {
		try {
			Graph g = GraphIO.loadGraphFromFile(args[0],
					new ConsoleProgressFunction());

			for (Vertex v : g.vertices()) {
				count(v.getAttributedElementClass());
				count(v.getAttributedElementClass().getPackage());
			}

			for (Edge e : g.edges()) {
				count(e.getAttributedElementClass());
				count(e.getAttributedElementClass().getPackage());
			}
			for (NamedElement e : counters.keySet()) {
				System.out.println(e.getClass().getSimpleName() + ";"
						+ e.getQualifiedName() + ";" + counters.get(e));
			}
		} catch (GraphIOException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void count(NamedElement e) {
		if (counters.containsKey(e)) {
			counters.put(e, counters.get(e) + 1);
		} else {
			counters.put(e, 1);
		}
	}
}
