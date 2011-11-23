package de.uni_koblenz.jgralab.greql2.funlib.graph;

import org.pcollections.PSet;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.types.PathSystem;
import de.uni_koblenz.jgralab.greql2.types.Slice;

public class Edges extends Function {

	public Edges() {
		super("Returns the set of edges in the given path system or slice.",
				Category.GRAPH);
	}

	public PSet<Edge> evaluate(PathSystem p) {
		return p.getEdges();
	}

	public PSet<Edge> evaluate(Slice s) {
		return s.getEdges();
	}
}
