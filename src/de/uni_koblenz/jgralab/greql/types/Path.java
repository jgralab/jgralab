/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 *
 * For bug reports, documentation and further information, visit
 *
 *                         https://github.com/jgralab/jgralab
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining
 * it with Eclipse (or a modified version of that program or an Eclipse
 * plugin), containing parts covered by the terms of the Eclipse Public
 * License (EPL), the licensors of this Program grant you additional
 * permission to convey the resulting work.  Corresponding Source for a
 * non-source form of such a combination shall include the source code for
 * the parts of JGraLab used as well as that of the covered work.
 */
package de.uni_koblenz.jgralab.greql.types;

import java.util.HashSet;

import org.pcollections.PVector;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.JGraLab;
import de.uni_koblenz.jgralab.Vertex;

public class Path {
	private final PVector<Vertex> vertices;
	private final PVector<Edge> edges;

	private Path(PVector<Vertex> vs, PVector<Edge> es) {
		vertices = vs;
		edges = es;
	}

	public static Path start(Vertex v) {
		if (v == null || !v.isValid()) {
			throw new IllegalArgumentException(
					"The vertex must be != null and valid");
		}
		PVector<Vertex> vs = JGraLab.vector();
		PVector<Edge> es = JGraLab.vector();
		return new Path(vs.plus(v), es);
	}

	public Path reverse() {
		PVector<Vertex> vs = JGraLab.vector();
		PVector<Edge> es = JGraLab.vector();
		for (int i = vertices.size() - 1; i >= 0; --i) {
			vs = vs.plus(vertices.get(i));
		}
		for (int i = edges.size() - 1; i >= 0; --i) {
			es = es.plus(edges.get(i).getReversedEdge());
		}
		return new Path(vs, es);
	}

	public Path append(Edge e) {
		if (e.getThis() != getEndVertex()) {
			throw new IllegalArgumentException("Can't append " + e
					+ " to this Path (e.getThis() (" + e.getThis() + ") !="
					+ getEndVertex() + ")");
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
		HashSet<Vertex> h = new HashSet<>();
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

	public boolean contains(GraphElement<?, ?> el) {
		return (el instanceof Vertex) ? containsVertex((Vertex) el)
				: containsEdge((Edge) el);
	}

	public boolean containsVertex(Vertex v) {
		return vertices.contains(v);
	}

	public boolean containsEdge(Edge e) {
		return edges.contains(e);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Path: ");
		if (vertices.isEmpty()) {
			sb.append("empty");
		} else {
			sb.append(vertices.get(0));
			for (Edge e : edges) {
				sb.append(" ").append(e).append(" ").append(e.getThat());
			}
		}
		return sb.toString();
	}
}
