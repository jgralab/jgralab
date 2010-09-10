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
package de.uni_koblenz.jgralab.algolib.visitors;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;

/**
 * This visitor allows visiting vertices and edges without distinguishing
 * between special vertices and edges. During an algorithm run, each method
 * should only be called at most once per vertex/edge. The algorithms have to
 * ensure this.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public interface GraphVisitor extends Visitor {

	/**
	 * Executes arbitrary code in the context of the given vertex <code>v</code>
	 * .
	 * 
	 * @param v
	 *            the vertex that is currently visited
	 */
	public void visitVertex(Vertex v);

	/**
	 * Executes arbitrary code in the context of the given edge <code>e</code>.
	 * 
	 * @param e
	 *            the edge that is currently visited
	 */
	public void visitEdge(Edge e);

}
