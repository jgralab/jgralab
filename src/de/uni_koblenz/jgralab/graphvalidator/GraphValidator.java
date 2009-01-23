/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2008 Institute for Software Technology
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
package de.uni_koblenz.jgralab.graphvalidator;

import java.util.ArrayList;
import java.util.List;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphvalidator.ConstraintInvalidation.ConstraintType;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.VertexClass;

/**
 * @author Tassilo Horn <horn@uni-koblenz.de>
 *
 */
public class GraphValidator {

	private Graph graph;

	public GraphValidator(Graph graph) {
		this.graph = graph;
	}

	public Iterable<ConstraintInvalidation> validate() {
		List<ConstraintInvalidation> brokenConstraints = new ArrayList<ConstraintInvalidation>();

		// Check if all multiplicities are correct
		for (EdgeClass ec : graph.getSchema()
				.getEdgeClassesInTopologicalOrder()) {
			if (ec.isInternal()) {
				continue;
			}

			int fromMin = ec.getFromMin();
			int fromMax = ec.getFromMax();
			int toMin = ec.getToMin();
			int toMax = ec.getToMax();

			for (Vertex v : graph.vertices(ec.getFrom())) {
				int degree = v.getDegree(ec);
				if (degree < toMin || degree > toMax) {
					brokenConstraints.add(new ConstraintInvalidation(
							ConstraintType.MULTIPLICITY, v, degree
									+ " outgoing " + ec.getQualifiedName()
									+ " edges, but only " + toMin + " to "
									+ toMax + " are allowed."));
				}
			}
			for (Vertex v : graph.vertices(ec.getTo())) {
				int degree = v.getDegree(ec);
				if (degree < fromMin || degree > fromMax) {
					brokenConstraints.add(new ConstraintInvalidation(
							ConstraintType.MULTIPLICITY, v, degree
									+ " incoming " + ec.getQualifiedName()
									+ " edges, but only " + fromMin + " to "
									+ fromMax + " are allowed."));
				}
			}
		}

		// check if all constraints are met
		for (String greql2Pred : graph.getGraphClass().getConstraints()) {
			GreqlEvaluator eval = new GreqlEvaluator(greql2Pred, graph, null);
			eval.startEvaluation();
			JValue result = eval.getEvaluationResult();
			if (!result.toBoolean()) {
				brokenConstraints.add(new ConstraintInvalidation(
						ConstraintType.GRAPH_CLASS, graph, "\"" + greql2Pred
								+ "\" returned " + result.toBoolean() + "."));
			}
		}
		for (VertexClass vc : graph.getSchema()
				.getVertexClassesInTopologicalOrder()) {
			for (String greql2Pred : vc.getConstraints()) {
				GreqlEvaluator eval = new GreqlEvaluator(greql2Pred, graph,
						null);
				eval.startEvaluation();
				JValue result = eval.getEvaluationResult();
				if (!result.toBoolean()) {
					brokenConstraints.add(new ConstraintInvalidation(
							ConstraintType.VERTEX_CLASS, graph, "\""
									+ greql2Pred + "\" returned "
									+ result.toBoolean() + "."));
				}
			}
		}
		for (EdgeClass vc : graph.getSchema()
				.getEdgeClassesInTopologicalOrder()) {
			for (String greql2Pred : vc.getConstraints()) {
				GreqlEvaluator eval = new GreqlEvaluator(greql2Pred, graph,
						null);
				eval.startEvaluation();
				JValue result = eval.getEvaluationResult();
				if (!result.toBoolean()) {
					brokenConstraints
							.add(new ConstraintInvalidation(
									ConstraintType.EDGE_CLASS, graph, "\""
											+ greql2Pred + "\" returned "
											+ result.toBoolean() + "."));
				}
			}
		}
		return brokenConstraints;
	}

}
