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
package de.uni_koblenz.jgralab.algolib.algorithms.reachability.visitors;

import java.util.Collection;
import java.util.LinkedHashSet;

import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.visitors.Visitor;
import de.uni_koblenz.jgralab.algolib.visitors.VisitorComposition;

public class TransitiveVisitorComposition extends VisitorComposition implements
		TransitiveVisitor {

	private Collection<TransitiveVisitor> visitors;

	@Override
	protected void createVisitorsLazily() {
		super.createVisitorsLazily();
		if (visitors == null) {
			visitors = new LinkedHashSet<TransitiveVisitor>();
		}
	}

	@Override
	public void addVisitor(Visitor visitor) {
		if (visitor instanceof TransitiveVisitor) {
			super.addVisitor(visitor);
			visitors.add((TransitiveVisitor) visitor);
		} else {
			throw new IllegalArgumentException(
					"The given visitor is incompatiple with this visitor composition.");
		}
	}

	@Override
	public void removeVisitor(Visitor visitor) {
		super.removeVisitor(visitor);
		if (visitors != null) {
			if (visitor instanceof TransitiveVisitor) {
				visitors.remove(visitor);
				if (visitors.size() == 0) {
					visitors = null;
				}
			}
		}
	}

	@Override
	public void clearVisitors() {
		super.clearVisitors();
		visitors = null;
	}

	@Override
	public void visitVertexTriple(Vertex u, Vertex v, Vertex w) {
		if (visitors != null) {
			for (TransitiveVisitor currentVisitor : visitors) {
				currentVisitor.visitVertexTriple(u, v, w);
			}
		}
	}

}
