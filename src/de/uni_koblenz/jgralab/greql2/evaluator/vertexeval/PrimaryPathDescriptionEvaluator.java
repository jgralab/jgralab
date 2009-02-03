/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2009 Institute for Software Technology
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
 
package de.uni_koblenz.jgralab.greql2.evaluator.vertexeval;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.evaluator.fa.Transition;
import de.uni_koblenz.jgralab.greql2.schema.Direction;
import de.uni_koblenz.jgralab.greql2.schema.PrimaryPathDescription;

/**
 * abstract baseclass for SimplePathDescription and EdgePathDescription
 * @author ist@uni-koblenz.de
 * Summer 2006, Diploma Thesis
 *
 */
public abstract class PrimaryPathDescriptionEvaluator extends
		PathDescriptionEvaluator {

	protected PrimaryPathDescription vertex;

	/**
	 * returns the vertex this VertexEvaluator evaluates
	 */
	@Override
	public Vertex getVertex() {
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
	protected Transition.AllowedEdgeDirection getEdgeDirection(PrimaryPathDescription vertex) {
		Transition.AllowedEdgeDirection validDirection = Transition.AllowedEdgeDirection.ANY;
		Edge dirEdge = vertex.getFirstIsDirectionOf(EdgeDirection.IN);
		if (dirEdge != null) {
			Direction dirVertex = (Direction) dirEdge.getAlpha();
			if (dirVertex.getDirValue() == "in")
				validDirection = Transition.AllowedEdgeDirection.IN;
			else if (dirVertex.getDirValue() == "out")
				validDirection = Transition.AllowedEdgeDirection.OUT;
		}
		return validDirection;
	}

	/**
	 * Returns the edge role this PathDescription accepts
	 */
	protected String getEdgeRole(EdgeRestrictionEvaluator edgeRestEval) {
		if (edgeRestEval == null)
			return null;
		return edgeRestEval.getEdgeRole();
	}




}
