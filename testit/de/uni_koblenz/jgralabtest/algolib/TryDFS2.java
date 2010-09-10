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
package de.uni_koblenz.jgralabtest.algolib;

import de.uni_koblenz.jgralab.algolib.algorithms.search.DepthFirstSearch;
import de.uni_koblenz.jgralab.algolib.algorithms.search.RecursiveDepthFirstSearch;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleSchema;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleVertex;

public class TryDFS2 {
	public static void main(String[] args) {
		SimpleGraph graph = SimpleSchema.instance().createSimpleGraph(4, 6);
		SimpleVertex a = graph.createSimpleVertex();
		SimpleVertex b = graph.createSimpleVertex();
		SimpleVertex c = graph.createSimpleVertex();
		SimpleVertex d = graph.createSimpleVertex();
		
		graph.createSimpleEdge(a, b);
		graph.createSimpleEdge(a, c);
		graph.createSimpleEdge(a, d);
		
		graph.createSimpleEdge(b, d);
		
		graph.createSimpleEdge(c, b);
		
		graph.createSimpleEdge(d, a);
		
		DepthFirstSearch dfs = new RecursiveDepthFirstSearch(graph);
		dfs.addVisitor(new DebugSearchVisitor());
		
		dfs.execute();
	}
}
