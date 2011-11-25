package de.uni_koblenz.jgralab.greql2.funlib.graph;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.types.TypeCollection;

public class Next extends Function {
	public Next() {
		super(
				"Returns the next edge or vertex for a given element, optionally restricted by a type collection."
						+ " For edges, the optional boolean parameter decides if successor is taken from the global edge sequence (true),"
						+ " or from the incidence sequence (false).", 2, 1,
				1.0, Category.GRAPH);
	}

	public Edge evaluate(Edge e) {
		return e.getNextIncidence();
	}

	public Edge evaluate(Edge e, Boolean global) {
		return global ? e.getNextEdge() : e.getNextIncidence();
	}

	public Edge evaluate(Edge e, Boolean global, TypeCollection tc) {
		if (global) {
			for (Edge n = e.getNextEdge(); n != null; n = n.getNextEdge()) {
				if (tc.acceptsType(n.getAttributedElementClass())) {
					return n;
				}
			}
			return null;
		} else {
			return evaluate(e, tc);
		}
	}

	public Edge evaluate(Edge e, TypeCollection tc) {
		for (Edge n = e.getNextIncidence(); n != null; n = n.getNextIncidence()) {
			if (tc.acceptsType(n.getAttributedElementClass())) {
				return n;
			}
		}
		return null;
	}

	public Vertex evaluate(Vertex v) {
		return v.getNextVertex();
	}

	public Vertex evaluate(Vertex v, TypeCollection tc) {
		for (Vertex n = v.getNextVertex(); n != null; n = n.getNextVertex()) {
			if (tc.acceptsType(n.getAttributedElementClass())) {
				return n;
			}
		}
		return null;
	}
}
