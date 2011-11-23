package de.uni_koblenz.jgralab.greql2.funlib.graph;

import org.pcollections.PSet;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.types.PathSystem;
import de.uni_koblenz.jgralab.greql2.types.Slice;

public class Vertices extends Function {

	public Vertices() {
		super("Returns the set of vertices in the given path system or slice.",
				Category.PATHS_AND_PATHSYSTEMS_AND_SLICES);
	}

	public PSet<Vertex> evaluate(PathSystem p) {
		return p.getVertices();
	}

	public PSet<Vertex> evaluate(Slice s) {
		return s.getVertices();
	}
}
