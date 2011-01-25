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
package de.uni_koblenz.jgralabtest.algolib.nonjunit;

import java.util.Random;

import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleGraph;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleSchema;
import de.uni_koblenz.jgralabtest.schemas.algolib.simple.SimpleVertex;

public class RandomGraph {

	public static SimpleGraph createTotallyRandomGraph(long seed,
			int vertexCount, int edgeCount) {
		System.out.println("Creating random graph with " + vertexCount
				+ " vertices and " + edgeCount + " edges.");
		SimpleGraph out = createEmptyGraph();
		Random rng = new Random(seed);
		for (int i = 0; i < vertexCount; i++) {
			out.createSimpleVertex();
		}
		for (int i = 0; i < edgeCount; i++) {
			int alpha = rng.nextInt(vertexCount) + 1;
			int omega = rng.nextInt(vertexCount) + 1;
			out.createSimpleEdge((SimpleVertex) out.getVertex(alpha),
					(SimpleVertex) out.getVertex(omega));
		}
		return out;
	}

	public static SimpleGraph createEmptyGraph() {
		System.out.println("Creating empty graph.");
		SimpleGraph out = SimpleSchema.instance().createSimpleGraph();
		return out;
	}

	public static void addWeakComponent(long seed, SimpleGraph g,
			int vertexCount, int additionalEdgeCount) {
		System.out.println("Adding weak component with " + vertexCount
				+ " vertices and " + (vertexCount + additionalEdgeCount)
				+ " edges.");
		SimpleVertex[] vertices = new SimpleVertex[vertexCount];
		int filled = 0;
		// create "root"
		vertices[filled++] = g.createSimpleVertex();
		// create spanning tree
		Random rng = new Random(seed);
		for (int i = 1; i < vertexCount; i++) {
			SimpleVertex alpha = vertices[rng.nextInt(filled)];
			vertices[filled] = g.createSimpleVertex();
			SimpleVertex omega = vertices[filled++];
			// create tree edge
			g.createSimpleEdge(alpha, omega);
		}
		// create additional edges
		for (int i = 0; i < additionalEdgeCount; i++) {
			SimpleVertex alpha = vertices[rng.nextInt(vertices.length)];
			SimpleVertex omega = vertices[rng.nextInt(vertices.length)];
			g.createSimpleEdge(alpha, omega);
		}
	}
}
