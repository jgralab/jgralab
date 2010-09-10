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
package de.uni_koblenz.jgralab.algolib.algorithms.search.visitors;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.visitors.GraphVisitorComposition;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;

public class SearchVisitorComposition extends GraphVisitorComposition
		implements SearchVisitor {

	@Override
	public void visitFrond(Edge e) {
		if (visitors != null) {
			for (Visitor currentVisitor : visitors) {
				if (currentVisitor instanceof SearchVisitor) {
					((SearchVisitor) currentVisitor).visitFrond(e);
				}
			}
		}
	}

	@Override
	public void visitRoot(Vertex v) {
		if (visitors != null) {
			for (Visitor currentVisitor : visitors) {
				if (currentVisitor instanceof SearchVisitor) {
					((SearchVisitor) currentVisitor).visitRoot(v);
				}
			}
		}
	}

	@Override
	public void visitTreeEdge(Edge e) {
		if (visitors != null) {
			for (Visitor currentVisitor : visitors) {
				if (currentVisitor instanceof SearchVisitor) {
					((SearchVisitor) currentVisitor).visitTreeEdge(e);
				}
			}
		}
	}
}
