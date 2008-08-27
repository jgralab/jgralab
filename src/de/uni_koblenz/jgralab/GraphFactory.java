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

package de.uni_koblenz.jgralab;

/**
 * This interface provides the method signature for a graph factory. A graph
 * factory creates instances of graphs, edges and vertices. By changing the
 * factory it is possible to extend the Graph, Vertex, or Edge classes that are
 * used in a graph.
 * 
 * @author dbildh
 */

public interface GraphFactory {

	/**
	 * creates a Graph-object for the specified class. The returned object may
	 * be an instance of a subclass of the specified graphClass.
	 */
	public Graph createGraph(Class<? extends Graph> graphClass, String id,
			int vMax, int eMax);

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
	public Edge createEdge(Class<? extends Edge> edgeClass, int id, Graph g);

	public void setGraphImplementationClass(Class<? extends Graph> graphM1Class, Class <? extends Graph> implementationClass);
	public void setVertexImplementationClass(Class<? extends Vertex> vertexM1Class, Class <? extends Vertex> implementationClass);
	public void setEdgeImplementationClass(Class<? extends Edge> edgeM1Class, Class <? extends Edge> implementationClass);

}
