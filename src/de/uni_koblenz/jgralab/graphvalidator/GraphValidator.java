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

import java.util.HashSet;
import java.util.Set;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphvalidator.ConstraintInvalidation.ConstraintType;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
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

	public Set<ConstraintInvalidation> validate() {
		Set<ConstraintInvalidation> brokenConstraints = new HashSet<ConstraintInvalidation>();

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
							ConstraintType.MULTIPLICITY, v + " has " + degree
									+ " outgoing " + ec.getQualifiedName()
									+ " edges, but only " + toMin + " to "
									+ toMax + " are allowed."));
				}
			}
			for (Vertex v : graph.vertices(ec.getTo())) {
				int degree = v.getDegree(ec);
				if (degree < fromMin || degree > fromMax) {
					brokenConstraints.add(new ConstraintInvalidation(
							ConstraintType.MULTIPLICITY, v + " has " + degree
									+ " incoming " + ec.getQualifiedName()
									+ " edges, but only " + fromMin + " to "
									+ fromMax + " are allowed."));
				}
			}
		}

		// check if all greql constraints are met
		for (String greql2Pred : graph.getGraphClass().getConstraints()) {
			GreqlEvaluator eval = new GreqlEvaluator(greql2Pred, graph, null);
			eval.startEvaluation();
			JValue result = eval.getEvaluationResult();
			handleGreqlResult(result, greql2Pred, brokenConstraints);
		}
		for (VertexClass vc : graph.getSchema()
				.getVertexClassesInTopologicalOrder()) {
			for (String greql2Pred : vc.getConstraints()) {
				GreqlEvaluator eval = new GreqlEvaluator(greql2Pred, graph,
						null);
				eval.startEvaluation();
				JValue result = eval.getEvaluationResult();
				handleGreqlResult(result, greql2Pred, brokenConstraints);
			}
		}
		for (EdgeClass vc : graph.getSchema()
				.getEdgeClassesInTopologicalOrder()) {
			for (String greql2Pred : vc.getConstraints()) {
				GreqlEvaluator eval = new GreqlEvaluator(greql2Pred, graph,
						null);
				eval.startEvaluation();
				JValue result = eval.getEvaluationResult();
				handleGreqlResult(result, greql2Pred, brokenConstraints);
			}
		}
		return brokenConstraints;
	}

	private void handleGreqlResult(JValue result, String greqlExp,
			Set<ConstraintInvalidation> brokenConstraints) {
		if (result.isBoolean() && !result.toBoolean()) {
			brokenConstraints.add(new ConstraintInvalidation(
					ConstraintType.GREQL, "\"" + greqlExp + "\" returned "
							+ result.toBoolean() + "."));
		} else if (result.isCollection()) {
			JValueCollection c = result.toCollection();
			for (JValue jv : c) {
				brokenConstraints.add(new ConstraintInvalidation(
						ConstraintType.GREQL, jv.toString()));
			}
		} else {
			// TODO: normally we shouldn't get here, so maybe we should handle
			// that situation more appropriate...
			brokenConstraints.add(new ConstraintInvalidation(
					ConstraintType.GREQL, "\"" + greqlExp + "\" returned "
							+ result.toString() + "."));
		}
	}
}
