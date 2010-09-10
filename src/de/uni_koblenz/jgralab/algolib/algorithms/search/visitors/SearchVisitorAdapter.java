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
import de.uni_koblenz.jgralab.algolib.algorithms.GraphAlgorithm;
import de.uni_koblenz.jgralab.algolib.algorithms.search.SearchAlgorithm;
import de.uni_koblenz.jgralab.algolib.visitors.GraphVisitorAdapter;

/**
 * It implements all methods from <code>SearchVisitor</code> as empty stubs. Beyond
 * that, it also handles the storage of the search algorithm object implementing
 * visitors are used by. All instances of <code>SearchVisitor</code> should use
 * this class as superclass.
 * 
 * @author strauss@uni-koblenz.de
 * 
 */
public class SearchVisitorAdapter extends GraphVisitorAdapter implements
		SearchVisitor {

	@Override
	public void visitFrond(Edge e) {

	}

	@Override
	public void visitRoot(Vertex v) {

	}

	@Override
	public void visitTreeEdge(Edge e) {

	}
	
	protected SearchAlgorithm algorithm;

	@Override
	public void setAlgorithm(GraphAlgorithm algorithm) {
		if (algorithm instanceof SearchAlgorithm) {
			this.algorithm = (SearchAlgorithm) algorithm;
			reset();
		} else {
			throw new IllegalArgumentException(
					"This visitor is not compatible with "
							+ algorithm.getClass().getSimpleName()
							+ " It only works with instances of "
							+ SearchAlgorithm.class.getSimpleName());
		}
	}

	public SearchAlgorithm getAlgorithm() {
		return algorithm;
	}


}
