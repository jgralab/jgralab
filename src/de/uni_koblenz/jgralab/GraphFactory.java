/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2012 Institute for Software Technology
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

package de.uni_koblenz.jgralab;

import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.GraphClass;
import de.uni_koblenz.jgralab.schema.Schema;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * Creates instances of graphs, edges and vertices. By changing factory it is
 * possible to extend Graph, Vertex, and Edge classes used in a graph.
 * 
 * @author ist@uni-koblenz.de
 */
public interface GraphFactory {
	/**
	 * @return the {@link ImplementationType} of this GraphFactory
	 */
	public ImplementationType getImplementationType();

	/**
	 * @return the {@link Schema} this GraphFactory is bound to
	 */
	public Schema getSchema();

	/**
	 * Creates a {@link Graph} for the specified {@link GraphClass}
	 * 
	 * @param gc
	 * 			the {@GraphClass} of the new created {@link Graph}
	 * @param id
	 * 			the {@code String} representing the ID of the new {@link Graph}
	 * @param vMax
	 * 			the maximal number of vertices in the new {@link Graph} 
	 * 			(not forced, number of vertices can grow above)
	 * @param eMax
	 * 			the maximal number of edges in the new {@link Graph} 
	 * 			(not forced, number of edges can grow above)
 	 */
	public <G extends Graph> G createGraph(GraphClass gc, String id, int vMax,
			int eMax);

	/**
	 * Creates a {@link Vertex} for the specified {@link VertexClass}.
	 * 
	 * @param vc
	 * 			the {@link VertexClass} of the new created {@link Vertex}
	 * @param id
	 * 			the {@code int} value representing the ID of the new {@link Vertex}
	 * @param g
	 * 			the {@link Graph} that contains the new {@link Vertex}
	 */
	public <V extends Vertex> V createVertex(VertexClass vc, int id, Graph g);
	
	public <V extends Vertex> V restoreVertex(VertexClass vc, int id, Graph g);

	/**
	 * Creates an {@link Edge} for the specified {@link EdgeClass}.
	 * 
	 * @param ec
	 * 			the {@link EdgeClass} of the new created {@link Edge}
	 * @param id 
	 * 			the {@link int} value representing the ID of the new {@link Edge}
	 * @param g
	 * 			the {@link Graph} that contains the new {@link Edge}
	 * @param alpha
	 * 			the start {@link Vertex} of the new {@link Edge}
	 * @param omega
	 * 			the omega {@link Vertex} of the new {@link Edge}
	 */
	public <E extends Edge> E createEdge(EdgeClass ec, int id, Graph g,
			Vertex alpha, Vertex omega);

	public <E extends Edge> E restoreEdge(EdgeClass ec, int id, Graph g,
			Vertex alpha, Vertex omega);
	
	/**
	 * Sets the implementation class of the specified {@link GraphClass}
	 * 
	 * @param gc
	 * 			the {@link GraphClass} to set the implementation class for
	 * @param graphImplementationClass
	 * 			the implementation class to set
	 */
	public void setGraphImplementationClass(GraphClass gc,
			Class<? extends Graph> graphImplementationClass);

	/**
	 * Sets the implementation class of the specified {@link VertexClass}
	 * 
	 * @param vc
	 * 			the {@link VertexClass} to set the implementation class for
	 * @param vertexImplementationClass
	 * 			the implementation class to set
	 */
	public void setVertexImplementationClass(VertexClass vc,
			Class<? extends Vertex> vertexImplementationClass);

	/**
	 * Sets the implementation class of the specified {@link EdgeClass}
	 * 
	 * @param vc
	 * 			the {@link EdgeClass} to set the implementation class for
	 * @param vertexImplementationClass
	 * 			the implementation class to set
	 */
	public void setEdgeImplementationClass(EdgeClass ec,
			Class<? extends Edge> edgeImplementationClass);
	
	public Class<? extends Vertex> getVertexImplementationClass(VertexClass vc);
	public Class<? extends Edge> getEdgeImplementationClass(EdgeClass ec);
	

}
