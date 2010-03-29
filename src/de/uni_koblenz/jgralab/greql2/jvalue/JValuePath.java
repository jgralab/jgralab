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

package de.uni_koblenz.jgralab.greql2.jvalue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.GraphElement;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.exception.JValueInvalidTypeException;

/**
 * Represents a path in the Datagraph. A path is a alternating list of vertices
 * and egdes with |vertex] = |edge] + 1, it can be written as v1 e1 v2 e2 v3 e3
 * ..... vn en vn+1
 * 
 * @author ist@uni-koblenz.de
 * 
 */
public class JValuePath extends JValueImpl {

	/**
	 * returns a JValuePath-Reference to this JValue object
	 */
	@Override
	public JValuePath toPath() throws JValueInvalidTypeException {
		return this;
	}
	
	private Vertex startVertex = null;

	/**
	 * The ordered list of edges which are part of this path
	 */
	private List<Edge> edges;

	/**
	 * stores the hashcode of this path
	 */
	private int hashvalue = 0;

	/**
	 * creates a new JValuePath,
	 * 
	 * @param firstVertex
	 *            the first vertex in the path
	 */
	public JValuePath(Vertex firstVertex) {
		edges = new ArrayList<Edge>();
		startVertex = firstVertex;
		type = JValueType.PATH;
	}

	/**
	 * returns true, if this path is valid, that means, it has a valid root
	 * vertex and ends with a root vertx
	 */
	public boolean isValidPath() {
		return startVertex != null;
	}

	/**
	 * creates a copy of the given path
	 */
	public JValuePath(JValuePath path) {
		edges = new ArrayList<Edge>();
		startVertex = path.startVertex;
		edges.addAll(path.edges);
		type = JValueType.PATH;
	}

	/**
	 * returns a string representation of this path
	 */
	@Override
	public String toString() {
		StringBuffer returnString = new StringBuffer();
		if (startVertex != null) {
			returnString.append(startVertex);
		}
		for (Edge e : edges) {
			if (e.isNormal()) {
				returnString.append(" --" + e + "-> ");
			} else {
				returnString.append(" <-" + e + "-- ");
			}
			returnString.append(e.getThat());
		}
		return returnString.toString();
	}

	/**
	 * returns a hashValue for this path
	 */
	@Override
	public int hashCode() {
		if (hashvalue == 0) {
			int i = 1;
			Iterator<Edge> edgeIter = edges.iterator();
			while (edgeIter.hasNext()) {
				hashvalue += edgeIter.next().hashCode() * i * 1.5;
				i++;
			}
		}
		return hashvalue;
	}

	/**
	 * adds an edge and the that-vertex of this edge to the path. The edge will
	 * be added after the last vertex which is already in the path and the
	 * vertex will be added after this edge, so it will be the last vertex in
	 * the path. The edge to be added must start at the last vertex in the path.
	 * costs are O(1)
	 * 
	 * @param e
	 *            the Edge to add
	 * @throws JValuePathException
	 *             if the edge to be added doesn't start at the last vertex in
	 *             the path
	 */
	public void addEdge(Edge e) throws JValuePathException {
		if (edges.size() == 0) {
			if (startVertex != e.getThis()) {
				throw new JValuePathException("The edge " + e + " cannot be added to the path " + this + " because it doesn't start at the paths current end vertex");
			}
		}
		if (edges.get(edges.size()-1).getThat() != e.getThis()) {
			throw new JValuePathException("The edge " + e + " cannot be added to the path " + this + " because it doesn't start at the paths current end vertex");
		}	
		edges.add(e);
		hashvalue = 0;
	}

	/**
	 * returns true if this path is a trail, that means, it doesn't contain any
	 * duplicated vertex,
	 * 
	 * costs worst-case costs, if the path is a trail, are O(n * n/2)
	 */
	public boolean isTrail() {
		if (!isValidPath()) {
			return false;
		}
		HashSet<Vertex> vertices = new HashSet<Vertex>();
		vertices.add(startVertex);
		for (Edge e : edges) {
			if (vertices.contains(e.getThat())) 
				return false;
			vertices.add(e.getThat());
		}
		return true;
	}

	/**
	 * returns a new reversed path, this path is left unchanged, the reversed
	 * path is starts with the end vertex of this part and vice versa
	 */
	public JValuePath reverse() {
		if (edges.size() == 0) {
			return new JValuePath(startVertex);
		}
		JValuePath reversedPath = new JValuePath(edges.get(edges.size()-1).getThat());
		for (int i=edges.size(); i>0; i++) {
			reversedPath.addEdge(edges.get(i).isNormal() ? edges.get(i).getReversedEdge() : edges.get(i).getNormalEdge() );
		}
		return reversedPath;
	}

	/**
	 * @return the length of this path or 0, if this path is not valid
	 */
	public int pathLength() {
		return edges.size();
	}

	/**
	 * @return the vertex at the given position in the path or null if no such
	 *         vertex exists. The counting starts at position 0, this is the
	 *         root vertex
	 */
	public Vertex getVertexAt(int i) {
		if (i == 0) {
			return startVertex;
		}
		return edges.get(i).getThat();
	}

	/**
	 * @return the edge at the given position in the path or null if no such
	 *         vertex exists. The counting starts at position 0, this is the
	 *         edge which starts at the root vertex
	 */
	public Edge getEdgeAt(int i) {
		return edges.get(i);
	}

	/**
	 * @return the start vertex of this path
	 */
	public Vertex getStartVertex() {
		return startVertex; 
	}

	/**
	 * @return the end vertex of this path
	 */
	public Vertex getEndVertex() {
		if (edges.size() == 0)
			return startVertex;
		return edges.get(edges.size() - 1).getThat();
	}

	/**
	 * @return the list of nodes
	 */
	public List<Vertex> nodeTrace() {
		List<Vertex> vertices = new ArrayList<Vertex>();
		vertices.add(startVertex);
		for (Edge e : edges) {
			vertices.add(e.getThat());
		}
		return vertices;
	}

	/**
	 * @return the edgetrace, but all edges are encalpsulated in jvalues
	 */
	public JValueList nodeTraceAsJValue() {
		JValueList nodeList = new JValueList();
		nodeList.add(new JValueImpl(startVertex));
		for (Edge e : edges) {
			nodeList.add(new JValueImpl(e.getThat(), e.getThat()));
		}
		return nodeList;
	}

	/**
	 * @return the list of edges
	 */
	public List<Edge> edgeTrace() {
		return edges;
	}

	/**
	 * @return the edgetrace, but all edges are encapsulated in jvalues
	 */
	public JValueList edgeTraceAsJValue() {
		JValueList edgeList = new JValueList();
		for (Edge edge : edges) {
			edgeList.add(new JValueImpl(edge, edge));
		}
		return edgeList;
	}

	/**
	 * @return the trace (v e v e v), but all vertices/edges are encapsulated in
	 *         jvalues
	 */
	public JValueList traceAsJValue() {
		JValueList list = new JValueList();
		list.add(new JValueImpl(startVertex));
		for (Edge edge : edges) {
			list.add(new JValueImpl(edge, edge));
			list.add(new JValueImpl(edge.getThat(), edge.getThat()));
		}
		return list;
	}

	/**
	 * calculates the degree of the given vertex in this path
	 * 
	 * @param vertex
	 *            the vertex to calculate the degree for
	 * @return -1 (vertex is not part of this path) 0 (vertex is the only vertex
	 *         in this path) 1 (vertex is the first or last vertex) 2 (vertex
	 *         has a successor and a predecessor)
	 */
	public int degree(Vertex vertex) {
		int degree = 0;
		for (Edge e : edges) {
			if (e.getThat() == vertex)
				degree++;
			if (e.getThis() == vertex)
				degree++;
		}
		return degree;
	}

	/**
	 * calculates the in or out degree of the given vertex in this path
	 * 
	 * @param vertex
	 *            the vertex to calculate the degree for
	 * @param orientation
	 *            if set to true, the number of incomming edges will be counted,
	 *            otherwise, the number of outgoing edges will be counted
	 * @return -1 (vertex is not part of this path) 0 (vertex is the only vertex
	 *         in this path) 1 (vertex is the first or last vertex) 2 (vertex
	 *         has a successor and a predecessor)
	 */
	public int degree(Vertex vertex, EdgeDirection dir) {
			int degree = 0;
			switch (dir) {
			case IN:
				for (Edge e : edges) {
					if (e.getOmega() == vertex)
						degree++;
				}
				break;
			case OUT:
				for (Edge e : edges) {
					if (e.getAlpha() == vertex)
						degree++;
				}
				break;
			case INOUT:
				for (Edge e : edges) {
					if (e.getOmega() == vertex)
						degree++;
					if (e.getAlpha() == vertex)
						degree++;
				}
				break;
			default:
				throw new JValuePathException("Undefined EdgeDirection " + dir);
			}
			return degree;
		}

	/**
	 * returns the edges which are incomming or outgoing to the given vertex in
	 * this path
	 * 
	 * @param vertex
	 *            the vertex for which the connected edge will be returned
	 * @param orientation
	 *            if set to true, the incomming edge will be returned,
	 *            otherwise, the outgoing one will be returned
	 * @return the edges connected to the given vertex in the given orientation
	 */
	public JValueSet edgesConnected(Vertex vertex, EdgeDirection dir) {
		JValueSet returnSet = new JValueSet();
		switch (dir) {
		case IN:
			for (Edge e : edges) {
				if (e.getOmega() == vertex)
					returnSet.add(new JValueImpl(e));
			}
			break;
		case OUT:
			for (Edge e : edges) {
				if (e.getAlpha() == vertex)
					returnSet.add(new JValueImpl(e));
			}
			break;
		case INOUT:
			for (Edge e : edges) {
				if (e.getOmega() == vertex)
					returnSet.add(new JValueImpl(e));
				if (e.getAlpha() == vertex)
					returnSet.add(new JValueImpl(e));
			}
			break;
		default:
			throw new JValuePathException("Undefined EdgeDirection " + dir);
		}
		return returnSet;
	}
	

	/**
	 * returns the edges which are connected to the given vertex in this path
	 * 
	 * @param vertex
	 *            the vertex for which the connected edge will be returned
	 * @return the edges connected to the given vertex in the given orientation
	 */
	public JValueSet edgesConnected(Vertex vertex) {
		JValueSet returnSet = new JValueSet();
		for (Edge e : edges) {
			if (e.getOmega() == vertex)
				returnSet.add(new JValueImpl(e));
			if (e.getAlpha() == vertex)
				returnSet.add(new JValueImpl(e));
		}
		return returnSet;
	}

	/**
	 * returns the set of vertexTypes in this path
	 */
	public JValueSet vertexTypes() {
		JValueSet resultSet = new JValueSet();
		resultSet.add(new JValueImpl(startVertex.getAttributedElementClass()));
		for (Edge e : edges) {
			resultSet.add(new JValueImpl(e.getThat().getAttributedElementClass()));
		}
		return resultSet;
	}

	/**
	 * returns the set of edgeTypes in this path
	 */
	public JValueSet edgeTypes() {
		JValueSet resultSet = new JValueSet();
		for (Edge edge : edges) {
			resultSet.add(new JValueImpl(edge.getAttributedElementClass(), edge));
		}
		return resultSet;
	}

	/**
	 * return the set of types in this path
	 */
	public JValueSet types() {
		JValueSet types = edgeTypes();
		types.addAll(vertexTypes());
		return types;
	}

	/**
	 * returns true, if this path contains the given graphelement
	 */
	public boolean contains(GraphElement elem) {
		if (elem instanceof Vertex) {
			return contains((Vertex) elem);
		} else {
			return contains((Edge) elem);
		}
	}

	/**
	 * returns true, if this path contains the vertex
	 */
	public boolean contains(Vertex vertex) {
		if (startVertex == vertex)
			return true;
		for (Edge e : edges) {
			if (e.getThat() == vertex)
				return true;
		}
		return false;
	}

	/**
	 * returns true, if this path contains the edge
	 */
	public boolean contains(Edge edge) {
		return (edges.indexOf(edge) >= 0);
	}

	/**
	 * @return true, if this path and the given path are parallel, that means
	 *         they have the same start and end vertex, but no more same
	 *         vertices
	 */
	public boolean isParallel(JValuePath path) {
		if (path.getEndVertex() != this.getEndVertex()) {
			return false;
		}
		if (path.getStartVertex() != this.getStartVertex()) {
			return false;
		}
		if (path.pathLength() != this.pathLength()) {
			return false;
		}
		for (int i=0; i<edges.size()-1; i++) {
			if (edges.get(i).getThat() == path.edges.get(i).getThat())
				return false;
		}
		return true;
	}

	/**
	 * @return true, if this path is a cycle, that means, if start and end
	 *         vertex are the same
	 */
	public boolean isCycle() {
		if (!isValidPath()) {
			return false;
		}
		return (getEndVertex() == getStartVertex());
	}

	/**
	 * @return true, if this path is a subpath of the given path
	 */
	public boolean isSubPathOf(JValuePath path) {
		if (this.pathLength() > path.pathLength()) {
			return false;
		}
		int i = 0;
		for (i=0; i<path.edges.size(); i++) {
			if (edges.get(0) == path.edges.get(i))
				break;
		}
		if (i == edges.size())
			return false;
		for (int j = 0; j<edges.size(); j++) {
			if (edges.get(j) != path.edges.get(i+j))
				return false;
		}
		
		return true;
	}

	/**
	 * @return true, if this path and the given object are equal, that is, iff
	 *         the given object if also a path and it contains the same vertices
	 *         and edges in the same order
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof JValuePath))
			return false;
		JValuePath path = (JValuePath) o;
		if (this.pathLength() > path.pathLength()) {
			return false;
		}
		for (int i=0; i<edges.size(); i++) {
			if (edges.get(i) != path.edges.get(i))
				return false;
		}
		return true;
	}

	/**
	 * concatenates this path and the given one, so that the resulting path
	 * contains [p1] + [p2] -1 vertices. The end vertex of this path and the
	 * start vertex of the given path must be the same
	 */
	public JValuePath pathConcat(JValuePath p2) {
		if (this.getEndVertex() != p2.getStartVertex()) {
			throw new JValuePathException("Cannot append a path to another with an end vertex other than the start vertex of the path to append");
		}
		JValuePath newPath = new JValuePath(this);
		newPath.edges.addAll(p2.edges);
		return newPath;
	}

	/**
	 * accepts te given visitor to visit this jvalue
	 */
	@Override
	public void accept(JValueVisitor v) {
		v.visitPath(this);
	}

}
