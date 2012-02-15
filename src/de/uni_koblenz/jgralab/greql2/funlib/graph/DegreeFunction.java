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
package de.uni_koblenz.jgralab.greql2.funlib.graph;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.funlib.Function;
import de.uni_koblenz.jgralab.greql2.types.Path;
import de.uni_koblenz.jgralab.greql2.types.TypeCollection;

public abstract class DegreeFunction extends Function {

	private EdgeDirection direction;

	public DegreeFunction(String description, EdgeDirection direction) {
		super(description, 10, 1, 1, Category.GRAPH);
		this.direction = direction;
	}

	public Integer evaluate(Vertex v) {
		return v.getDegree(direction);
	}

	public Integer evaluate(Vertex v, TypeCollection c) {
		int degree = 0;
		for (Edge e = v.getFirstIncidence(); e != null; e = e
				.getNextIncidence()) {
			if (c.acceptsType(e.getAttributedElementClass())) {
				switch (direction) {
				case INOUT:
					++degree;
					break;
				case OUT:
					if (e.isNormal()) {
						++degree;
					}
					break;
				case IN:
					if (!e.isNormal()) {
						++degree;
					}
					break;
				}
			}
		}
		return degree;
	}

	public Integer evaluate(Vertex v, Path p) {
		return p.degree(v, direction);
	}

}
