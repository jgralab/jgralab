/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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

package de.uni_koblenz.jgralab;


/**
 * Creates instances of graphs, edges and vertices. By changing factory it is
 * possible to extend Graph, Vertex, and Edge classes used in a graph.
 * 
 * @author ist@uni-koblenz.de
 */
public interface GraphFactory {

	/**
	 * creates a Graph-object for the specified class. The returned object may
	 * be an instance of a subclass of the specified graphClass.
	 */
	public Graph createGraph(String id,
			int vMax, int eMax);

	/**
	 * creates a Graph-object for the specified class. The returned object may
	 * be an instance of a subclass of the specified graphClass.
	 */
	public Graph createGraph(String id);

	/**
	 * creates a Vertex-object for the specified class. The returned object may
	 * be an instance of a subclass of the specified vertexClass.
	 */
	public Vertex createVertex(Class<? extends Vertex> vertexClass, int id,
			Graph g);

	/**
	 * creates a Edge-object for the specified class. The returned object may be
	 * an instance of a subclass of the specified edgeClass.
	 */
	public Edge createEdge(Class<? extends Edge> edgeClass, int id, Graph g,
			Vertex alpha, Vertex omega);

	public void setGraphImplementationClass(
			Class<? extends Graph> graphSchemaClass,
			Class<? extends Graph> graphImplementationClass);

	public void setVertexImplementationClass(
			Class<? extends Vertex> vertexSchemaClass,
			Class<? extends Vertex> vertexImplementationClass);

	public void setEdgeImplementationClass(
			Class<? extends Edge> edgeSchemaClass,
			Class<? extends Edge> edgeImplementationClass);

	

}
