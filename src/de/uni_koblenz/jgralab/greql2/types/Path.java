package de.uni_koblenz.jgralab.greql2.types;

import java.util.HashSet;

import org.pcollections.ArrayPVector;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;

public class Path {
	private PVector<Vertex> vertices;
	private PVector<Edge> edges;

	private Path(PVector<Vertex> vs, PVector<Edge> es) {
		vertices = vs;
		edges = es;
	}

	public static Path start(Vertex v) {
		if (v == null || !v.isValid()) {
			throw new IllegalArgumentException(
					"The vertex must be != null and valid");
		}
		ArrayPVector<Vertex> vs = ArrayPVector.empty();
		ArrayPVector<Edge> es = ArrayPVector.empty();
		return new Path(vs.plus(v), es);
	}

	public Path reverse() {
		PVector<Vertex> vs = ArrayPVector.empty();
		PVector<Edge> es = ArrayPVector.empty();
		for (int i = vs.size() - 1; i >= 0; --i) {
			vs = vs.plus(vertices.get(i));
		}
		for (int i = es.size() - 1; i >= 0; --i) {
			es = es.plus(edges.get(i).getReversedEdge());
		}
		return new Path(vs, es);
	}

	public Path append(Edge e) {
		if (e.getThis() != getEndVertex()) {
			throw new IllegalArgumentException("Can't append " + e
					+ " to this Path (e.getThis() !=" + getEndVertex() + ")");
		}
		return new Path(vertices.plus(e.getThat()), edges.plus(e));
	}

	public Vertex getStartVertex() {
		return vertices.get(0);
	}

	public Vertex getEndVertex() {
		return vertices.get(vertices.size() - 1);
	}

	public int getLength() {
		return edges.size();
	}

	public Vertex getVertexAt(int i) {
		return vertices.get(i);
	}

	public Edge getEdgeAt(int i) {
		return edges.get(i);
	}

	public boolean isTrail() {
		HashSet<Vertex> h = new HashSet<Vertex>();
		h.add(getStartVertex());
		for (Edge e : edges) {
			if (h.contains(e.getThat())) {
				return false;
			}
			h.add(e.getThat());
		}
		return true;
	}

	public PVector<Edge> getEdgeTrace() {
		return edges;
	}

	public PVector<Vertex> getVertexTrace() {
		return vertices;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Path)) {
			return false;
		}
		Path p = (Path) o;
		return vertices.equals(p.vertices) && edges.equals(p.edges);
	}

	@Override
	public int hashCode() {
		return vertices.hashCode() + edges.hashCode();
	}

	public int degree(Vertex vertex, EdgeDirection dir) {
		int degree = 0;
		switch (dir) {
		case IN:
			for (Edge e : edges) {
				if (e.getOmega() == vertex) {
					degree++;
				}
			}
			return degree;
		case OUT:
			for (Edge e : edges) {
				if (e.getAlpha() == vertex) {
					degree++;
				}
			}
			return degree;
		case INOUT:
			for (Edge e : edges) {
				if (e.getOmega() == vertex) {
					degree++;
				} else if (e.getAlpha() == vertex) {
					degree++;
				}
			}
			return degree;
		default:
			throw new RuntimeException("FIXME: Unhandled EdgeDirection " + dir);
		}
	}

	public boolean contains(GraphElement el) {
		return (el instanceof Vertex) ? containsVertex((Vertex) el)
				: containsEdge((Edge) el);
	}

	public boolean containsVertex(Vertex v) {
		return vertices.contains(v);
	}

	public boolean containsEdge(Edge e) {
		return edges.contains(e);
	}
}
