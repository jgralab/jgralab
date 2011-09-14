package de.uni_koblenz.jgralab.greql2.funlib.graph;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphmarker.SubGraphMarker;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.types.Path;
import de.uni_koblenz.jgralab.greql2.types.TypeCollection;

public abstract class DegreeFunction extends Function {

	private EdgeDirection direction;

	public DegreeFunction(String description, EdgeDirection direction) {
		super(description, 10, 1, 1, Category.GRAPH);
		this.direction = direction;
	}

	public Integer evaluate(Vertex v) {
		return v.getDegree(direction);
	}

	public Integer evaluate(Vertex v, TypeCollection c) {
		return evaluate(null, v, c);
	}

	public Integer evaluate(Vertex v, Path p) {
		return p.degree(v, direction);
	}

	public Integer evaluate(SubGraphMarker subgraph, Vertex v, TypeCollection c) {
		if (subgraph != null && !subgraph.isMarked(v)) {
			return null;
		}
		int degree = 0;
		for (Edge e = v.getFirstIncidence(); e != null; e = e
				.getNextIncidence()) {
			if (subgraph != null && !subgraph.isMarked(e)) {
				continue;
			}
			if (c != null && !c.acceptsType(e.getAttributedElementClass())) {
				continue;
			}
			switch (direction) {
			case INOUT:
				++degree;
				break;
			case OUT:
				if (e.isNormal()) {
					++degree;
				}
				break;
			case IN:
				if (!e.isNormal()) {
					++degree;
				}
				break;
			}
		}

		return degree;
	}

	public Integer evaluate(SubGraphMarker subgraph, Vertex v) {
		return evaluate(subgraph, v, null);
	}
}
