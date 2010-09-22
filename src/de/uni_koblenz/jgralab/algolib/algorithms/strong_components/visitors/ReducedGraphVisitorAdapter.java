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
package de.uni_koblenz.jgralab.algolib.algorithms.strong_components.visitors;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.problems.directed.StrongComponentsSolver;

public class ReducedGraphVisitorAdapter implements ReducedGraphVisitor {

	protected StrongComponentsSolver algorithm;

	@Override
	public void reset() {

	}

	@Override
	public void setAlgorithm(GraphAlgorithm alg) {
		if (alg instanceof StrongComponentsSolver) {
			this.algorithm = (StrongComponentsSolver) alg;
			reset();
		} else {
			throw new IllegalArgumentException(
					"This visitor is not compatible with "
							+ alg.getClass().getSimpleName()
							+ " It only works with instances of "
							+ StrongComponentsSolver.class.getSimpleName());
		}
	}

	@Override
	public void visitReducedEdge(Edge e) {
		
	}

	@Override
	public void visitRepresentativeVertex(Vertex v) {
		
	}

}
