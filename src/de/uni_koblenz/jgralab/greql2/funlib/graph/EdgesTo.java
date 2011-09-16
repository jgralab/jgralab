package de.uni_koblenz.jgralab.greql2.funlib.graph;

import org.pcollections.ArrayPVector;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.types.TypeCollection;

public class EdgesTo extends Function {

	public EdgesTo() {
		super("Returns the list of incoming edges of vertex $v$", 2, 5, 1.0,
				Category.GRAPH);
	}

	public PVector<Edge> evaluate(Vertex v) {
		return evaluate(v, null);
	}

	public PVector<Edge> evaluate(Vertex v, TypeCollection tc) {
		PVector<Edge> result = ArrayPVector.empty();
		for (Edge e : v.incidences(EdgeDirection.IN)) {
			if (tc == null || tc.acceptsType(e.getAttributedElementClass())) {
				result = result.plus(e);
			}
		}
		return result;
	}
}
