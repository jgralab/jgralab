/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2011 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
 * 
 * For bug reports, documentation and further information, visit
 * 
 *                         http://jgralab.uni-koblenz.de
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

package de.uni_koblenz.jgralab.greql2.evaluator.vertexeval;

import java.util.Set;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.Transition;
import de.uni_koblenz.jgralab.greql2.schema.Direction;
import de.uni_koblenz.jgralab.greql2.schema.Greql2Vertex;
import de.uni_koblenz.jgralab.greql2.schema.PrimaryPathDescription;

/**
 * abstract baseclass for SimplePathDescription and EdgePathDescription
 * 
 * @author ist@uni-koblenz.de Summer 2006, Diploma Thesis
 * 
 */
public abstract class PrimaryPathDescriptionEvaluator extends
		PathDescriptionEvaluator {

	protected PrimaryPathDescription vertex;

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	@Override
	public Greql2Vertex getVertex() {
		return vertex;
	}

	public PrimaryPathDescriptionEvaluator(PrimaryPathDescription vertex,
			GreqlEvaluator eval) {
		super(eval);
		this.vertex = vertex;
	}

	/**
	 * Returns the edge direction this pathDescription accepts
	 */
	protected Transition.AllowedEdgeDirection getEdgeDirection(
			PrimaryPathDescription vertex) {
		Transition.AllowedEdgeDirection validDirection = Transition.AllowedEdgeDirection.ANY;
		Edge dirEdge = vertex.getFirstIsDirectionOfIncidence(EdgeDirection.IN);
		if (dirEdge != null) {
			Direction dirVertex = (Direction) dirEdge.getAlpha();
			if (dirVertex.get_dirValue() == "in") {
				validDirection = Transition.AllowedEdgeDirection.IN;
			} else if (dirVertex.get_dirValue() == "out") {
				validDirection = Transition.AllowedEdgeDirection.OUT;
			}
		}
		return validDirection;
	}

	/**
	 * Returns the set of edge role this PathDescription accepts
	 */
	protected Set<String> getEdgeRoles(EdgeRestrictionEvaluator edgeRestEval) {
		if (edgeRestEval == null) {
			return null;
		}
		return edgeRestEval.getEdgeRoles();
	}

}
