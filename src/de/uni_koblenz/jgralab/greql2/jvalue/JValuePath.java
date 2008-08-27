/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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

import java.util.*;
import de.uni_koblenz.jgralab.*;

/**
 * Represents a path in the Datagraph. A path is a alternating list of vertices
 * and egdes with |vertex] = |edge] + 1, it can be written as v1 e1 v2 e2 v3 e3
 * ..... vn en vn+1
 * 
 * @author Daniel Bildhauer <dbildh@uni-koblenz.de> Summer 2006, Diploma Thesis
 * 
 */
public class JValuePath extends JValue {

	/**
	 * returns a JValuePath-Reference to this JValue object
	 */
	public JValuePath toPath() throws JValueInvalidTypeException {
		return this;
	}

	/**
	 * The ordered list of vertices which are part of this path
	 */
	private ArrayList<Vertex> vertices;

	/**
	 * The ordered list of edges which are part of this path
	 */
	private ArrayList<Edge> edges;

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
		vertices = new ArrayList<Vertex>();
		if (firstVertex != null)
			vertices.add(firstVertex);
		type = JValueType.PATH;
	}
	
	/**
	 * returns true, if this path is valid, that means, it has a valid root vertex and ends with a root vertx
	 */
	public boolean isValidPath() {
		return !vertices.isEmpty();
	}

	/**
	 * creates a copy of the given path
	 */
	public JValuePath(JValuePath path) {
		edges = new ArrayList<Edge>();
		vertices = new ArrayList<Vertex>();
		vertices.addAll(path.vertices);
		edges.addAll(path.edges);
		type = JValueType.PATH;
	}

	/**
	 * returns a string representation of this path
	 */
	public String toString() {
		StringBuffer returnString = new StringBuffer();
		Iterator<Vertex> vertexIter = vertices.iterator();
		Iterator<Edge> edgeIter = edges.iterator();
		boolean printed = true;
		while (printed) {
			printed = false;
			if (vertexIter.hasNext()) {
				returnString.append(vertexIter.next());
				printed = true;
			}
			if (edgeIter.hasNext()) {
				returnString.append(" --" + edgeIter.next() + "-> ");
				printed = true;
			}
		}
		returnString.append("\n");
		return returnString.toString();
	}

	/**
	 * returns a hashValue for this path
	 */
	public int hashCode() {
		if (hashvalue == 0) {
			int i = 1;
			Iterator<Vertex> vertexIter = vertices.iterator();
			while (vertexIter.hasNext()) {
				hashvalue += vertexIter.next().hashCode() * i;
				i++;
			}
			Iterator<Edge> edgeIter = edges.iterator();
			while (edgeIter.hasNext()) {
				hashvalue += edgeIter.next().hashCode() * i * 1.5;
				i++;
			}
		}
		return hashvalue;
	}

	/**
	 * adds an edge and the that-vertex of this edge to the path.
	 * The edge will be added after the last vertex which is already
	 * in the path and the vertex will be added after this edge,
	 * so it will be the last vertex in the path. The edge to be added
	 * must start at the last vertex in the path.
	 * costs are O(1)
	 * @param e the Edge to add 
	 * @throws JValuePathException if the edge to be added doesn't start at the last vertex in the path
	 */
	public void addEdge(Edge e) throws JValuePathException {
		if (vertices.get(vertices.size()-1) != e.getThis())
			throw new JValuePathException("The edge " + e.toString() + " could not be added to the path " + this.toString() + " because the last vertex in the path is not the this-vertex of the edge");
		edges.add(e);		
		vertices.add(e.getThat());
		hashvalue = 0;
	}

	/**
	 * returns true if this path is a trail, that means, it doesn't contain any
	 * duplicated vertex,
	 * 
	 * costs worst-case costs, if the path is a trail, are O(n * n/2)
	 */
	public boolean isTrail() {
		if (!isValidPath())
			return false;
		for (int i = 0; i < vertices.size() - 1; i++)
			for (int j = i + 1; j < vertices.size(); j++)
				if (vertices.get(i) == vertices.get(j))
					return false;
		return true;
	}

	/**
	 * returns a new reversed path, this path is left unchanged, the reversed
	 * path is starts with the end vertex of this part and vice versa
	 */
	public JValuePath reverse() {
		int size = vertices.size();
		JValuePath reversedPath = new JValuePath(vertices.get(size - 1));
		for (int i = size - 2; i >= 0; i--) {
			reversedPath.edges.add(edges.get(i));
			reversedPath.vertices.add(vertices.get(i));
		}
		return reversedPath;
	}

	/**
	 * @return the length of this path or 0, if this path is not valid
	 */
	public int pathLength() {
		return vertices.size();
	}

	/**
	 * @return the vertex at the given position in the path or null if no such
	 *         vertex exists. The counting starts at position 0, this is the
	 *         root vertex
	 */
	public Vertex getVertexAt(int i) {
		return vertices.get(i);
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
		if (vertices.isEmpty())
			return null;
		return vertices.get(0);
	}

	/**
	 * @return the end vertex of this path
	 */
	public Vertex getEndVertex() {
		if (vertices.isEmpty())
			return null;
		return vertices.get(vertices.size() - 1);
	}

	/**
	 * @return the list of nodes
	 */
	public List<Vertex> nodeTrace() {
		return vertices;
	}

	/**
	 * @return the edgetrace, but all edges are encalpsulated in jvalues
	 */
	public JValueList nodeTraceAsJValue() {
		JValueList nodeList = new JValueList();
		Iterator<Vertex> iter = vertices.iterator();
		while (iter.hasNext()) {
			//TODO
			nodeList.add(new JValue(iter.next(), null));
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
	 * @return the edgetrace, but all edges are encalpsulated in jvalues
	 */
	public JValueList edgeTraceAsJValue() {
		JValueList edgeList = new JValueList();
		Iterator<Edge> iter = edges.iterator();
		while (iter.hasNext()) {
			Edge edge = iter.next();
			edgeList.add(new JValue(edge, edge));
		}
		return edgeList;
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
		int position = vertices.indexOf(vertex);
		if (position < 0)
			return -1;
		if (vertices.size() == 1)
			return 0;
		if (isCycle())
			return 2;
		if ((vertex == getEndVertex()) || (vertex == getStartVertex()))
			return 1;
		return 2;
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
	public int degree(Vertex vertex, boolean orientation) {
		int position = vertices.indexOf(vertex);
		if (position < 0)
			return -1;
		if (vertices.size() == 1)
			return 0;
		if (isCycle())
			return 2;
		if ((vertex == getEndVertex()) && (!orientation))
			return 0;
		if ((vertex == getStartVertex()) && (orientation))
			return 0;
		return 1;
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
	public JValueSet edgesConnected(Vertex vertex, boolean orientation) {
		JValueSet returnSet = new JValueSet();
		int index = vertices.indexOf(vertex);
		if (index == -1)
			return returnSet;
		for (int i = index - 1; i <= index; i++) {
			if ((index < edges.size()) && (index > 0)) {
				if (orientation) {
					if (edges.get(index).getOmega() == vertex)
						returnSet.add(new JValue(edges.get(index), edges.get(index)));
				} else {
					if (edges.get(index).getAlpha() == vertex)
						returnSet.add(new JValue(edges.get(index), edges.get(index)));
				}
			}
		}
		return returnSet;
	}
	
	/**
	 * returns the edges which are connected to the given vertex in
	 * this path
	 * 
	 * @param vertex
	 *            the vertex for which the connected edge will be returned
	 * @return the edges connected to the given vertex in the given orientation
	 */
	public JValueSet edgesConnected(Vertex vertex) {
		JValueSet returnSet = new JValueSet();
		int index = vertices.indexOf(vertex);
		if (index == -1)
			return returnSet;
		for (int i = index - 1; i <= index; i++) {
			if ((index < edges.size()) && (index > 0)) {
				if ((edges.get(index).getOmega() == vertex) || (edges.get(index).getAlpha() == vertex))
					returnSet.add(new JValue(edges.get(index), edges.get(index)));
			}
		}
		return returnSet;
	}

	/**
	 * returns the set of vertexTypes in this path
	 */
	public JValueSet vertexTypes() {
		JValueSet resultSet = new JValueSet();
		Iterator<Vertex> iter = vertices.iterator();
		while (iter.hasNext()) {
			//TODO
			resultSet.add(new JValue(iter.next().getAttributedElementClass(), null));
		}
		return resultSet;
	}
	
	/**
	 * returns the set of edgeTypes in this path
	 */
	public JValueSet edgeTypes() {
		JValueSet resultSet = new JValueSet();
		Iterator<Edge> iter = edges.iterator();
		while (iter.hasNext()) {
			Edge edge = iter.next();
			resultSet.add(new JValue(edge.getAttributedElementClass(), edge));
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
		if (elem instanceof Vertex)
			return contains((Vertex) elem);
		else
			return contains((Edge) elem);
	}
	
	/**
	 * returns true, if this path contains the vertex
	 */
	public boolean contains(Vertex vertex) {
		return (vertices.indexOf(vertex) >= 0);
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
		if (path.getEndVertex() != this.getEndVertex())
			return false;
		if (path.getStartVertex() != this.getStartVertex())
			return false;
		if (path.pathLength() != this.pathLength())
			return false;
		Iterator<Vertex> iter1 = vertices.iterator();
		Iterator<Vertex> iter2 = path.vertices.iterator();
		while (iter1.hasNext() && iter2.hasNext()) {
			if (iter1.next() == iter2.next())
				return false;
		}
		return true;
	}

	/**
	 * @return true, if this path is a cycle, that means, if start and end
	 *         vertex are the same
	 */
	public boolean isCycle() {
		if (!isValidPath())
			return false;
		return (getEndVertex() == getStartVertex());
	}

	/**
	 * @return true, if this path is a subpath of the given path
	 */
	public boolean isSubPathOf(JValuePath path) {
		if (this.pathLength() > path.pathLength())
			return false;
		Iterator<Vertex> iter1 = path.vertices.iterator();
		Iterator<Vertex> iter2 = vertices.iterator();
		Vertex vertex1;
		if (iter1.hasNext())
			vertex1 = iter1.next();
		else
			return false;
		Vertex vertex2;
		if (iter2.hasNext())
			vertex2 = iter2.next();
		else
			return false;
		while ((vertex1 != vertex2) && (iter1.hasNext())) {
			vertex1 = iter1.next();
		}
		while ((vertex1 == vertex2) && (iter1.hasNext()) && (iter2.hasNext())) {
			vertex1 = iter1.next();
			vertex2 = iter2.next();
		}
		if (iter2.hasNext())
			return false; // there are more vertices in this path that are not
							// in the given path
		return true;
	}

	/**
	 * @return true, if this path and the given object are equal, that is, iff
	 *         the given object if also a path and it contains the same vertices
	 *         and edges in the same order
	 */
	public boolean equals(Object o) {
		if (o instanceof JValuePath) {
			JValuePath path = (JValuePath) o;
			Iterator<Vertex> iter1 = path.vertices.iterator();
			Iterator<Vertex> iter2 = vertices.iterator();
			Vertex vertex1;
			if (iter1.hasNext())
				vertex1 = iter1.next();
			else
				return false;
			Vertex vertex2;
			if (iter2.hasNext())
				vertex2 = iter2.next();
			else
				return false;
			while ((vertex1 == vertex2) && (iter1.hasNext())
					&& (iter2.hasNext())) {
				vertex1 = iter1.next();
				vertex2 = iter2.next();
			}
			if (iter1.hasNext() || iter2.hasNext())
				return false;
			return true;
		}
		return false;
	}

	/**
	 * concatenates this path and the given one, so that the resulting path
	 * contains [p1] + [p2] -1 vertices. The end vertex of this path and the
	 * start vertex of the given path must be the same
	 */
	public JValuePath pathConcat(JValuePath p2) {
		if (this.getEndVertex() != p2.getStartVertex())
			return null;
		JValuePath newPath = new JValuePath(this);
		newPath.vertices.addAll(p2.vertices);
		newPath.edges.addAll(p2.edges);
		return newPath;
	}
	
	/**
	 * accepts te given visitor to visit this jvalue
	 */
	public void accept(JValueVisitor v)  throws Exception {
		v.visitPath(this);
	}

}
