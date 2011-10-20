package de.uni_koblenz.jgralab.greql2.funlib.graph;

import org.pcollections.PSet;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.types.PathSystem;
import de.uni_koblenz.jgralab.greql2.types.Slice;

public class Leaves extends Function {

	public Leaves() {
		super("Returns the set of leaf vertices in pathsystem $p$/slice $s$.",
				Category.GRAPH);
	}

	public PSet<Vertex> evaluate(PathSystem p) {
		return p.getLeaves();
	}

	public PSet<Vertex> evaluate(Slice s) {
		return s.getLeaves();
	}
}
