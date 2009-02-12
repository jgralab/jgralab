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
package de.uni_koblenz.jgralab.graphvalidator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.graphvalidator.ConstraintViolation.ConstraintType;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.EvaluateException;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueCollection;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueRecord;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;

/**
 * @author Tassilo Horn <horn@uni-koblenz.de>
 * 
 */
public class GraphValidator {

	private Graph graph;

	public GraphValidator(Graph graph) {
		this.graph = graph;
	}

	/**
	 * Validate the graph
	 * 
	 * @return a set of {@link ConstraintViolation} objects, one for each
	 *         violation, sorted by the {@link ConstraintType}
	 */
	public SortedSet<ConstraintViolation> validate() {
		SortedSet<ConstraintViolation> brokenConstraints = new TreeSet<ConstraintViolation>();

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
					JValueRecord rec = new JValueRecord();
					rec.add("vertex", new JValue(v));
					rec.add("degree", new JValue(degree));
					rec.add("edgeClass", new JValue(ec));
					rec.add("direction", new JValue("outgoing"));
					rec.add("min", new JValue(toMin));
					rec.add("max", new JValue(toMax));
					brokenConstraints.add(new ConstraintViolation(
							ConstraintType.MULTIPLICITY, rec));
				}
			}
			for (Vertex v : graph.vertices(ec.getTo())) {
				int degree = v.getDegree(ec);
				if (degree < fromMin || degree > fromMax) {
					JValueRecord rec = new JValueRecord();
					rec.add("vertex", new JValue(v));
					rec.add("degree", new JValue(degree));
					rec.add("edgeClass", new JValue(ec));
					rec.add("direction", new JValue("incoming"));
					rec.add("min", new JValue(fromMin));
					rec.add("max", new JValue(fromMax));
					brokenConstraints.add(new ConstraintViolation(
							ConstraintType.MULTIPLICITY, rec));
				}
			}
		}

		// check if all greql constraints are met
		List<AttributedElementClass> aecs = new ArrayList<AttributedElementClass>();
		aecs.addAll(graph.getSchema().getGraphClassesInTopologicalOrder());
		aecs.addAll(graph.getSchema().getVertexClassesInTopologicalOrder());
		aecs.addAll(graph.getSchema().getEdgeClassesInTopologicalOrder());
		for (AttributedElementClass aec : aecs) {
			for (String greql2Exp : aec.getConstraints()) {
				GreqlEvaluator eval = new GreqlEvaluator(greql2Exp, graph, null);
				try {
					eval.startEvaluation();
					JValue result = eval.getEvaluationResult();
					brokenConstraints.addAll(handleGreqlResult(result,
							greql2Exp, aec));
				} catch (EvaluateException e) {
					brokenConstraints.add(new ConstraintViolation(
							ConstraintType.INVALID_GREQL_EXPRESSION,
							new JValue(greql2Exp)));
				}
			}
		}
		return brokenConstraints;
	}

	/**
	 * Do just like {@link GraphValidator#validate()}, but generate a HTML
	 * report saved to <code>fileName</code>, too.
	 * 
	 * @param fileName
	 *            the name of the HTML report file
	 * @return a set of {@link ConstraintViolation} objects, one for each
	 *         invalidation
	 * @throws IOException
	 *             if the given file cannot be written
	 */
	public SortedSet<ConstraintViolation> createValidationReport(String fileName)
			throws IOException {
		SortedSet<ConstraintViolation> brokenConstraints = validate();

		BufferedWriter bw = new BufferedWriter(new FileWriter(
				new File(fileName)));
		// The header
		bw.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\"\n"
				+ "\"http://www.w3.org/TR/html4/strict.dtd\">\n" + "<html>");
		bw.append("<head><title>");
		bw.append("Validation Report for the "
				+ graph.getM1Class().getSimpleName() + " with id "
				+ graph.getId() + ".");
		bw.append("</title></head>");

		// The body
		bw.append("<body>");

		if (brokenConstraints.size() == 0) {
			bw.append("<p><b>The graph is perfectly valid!</b></p>");
		} else {
			bw.append("<p><b>The " + graph.getM1Class().getSimpleName()
					+ " invalidates " + brokenConstraints.size()
					+ " constraints.</b></p>");
			// Here goes the table
			bw.append("<table border=\"1\">");
			bw.append("<tr>");
			bw.append("<th>#</th>");
			bw.append("<th>Constraint Type</th>");
			bw.append("<th>Message</th>");
			bw.append("</tr>");
			int row = 1;
			for (ConstraintViolation ci : brokenConstraints) {
				bw.append("<tr>");
				bw.append("<td align=\"right\">" + row + "</td>");
				bw.append("<td>" + ci.getConstraintType() + "</td>");
				bw.append("<td>" + ci.getInvalidationDescription() + "</td>");
				bw.append("</tr>");
				row++;
			}
			bw.append("</table>");
		}

		bw.append("</body></html>");
		bw.flush();
		bw.close();
		return brokenConstraints;
	}

	private Set<ConstraintViolation> handleGreqlResult(JValue result,
			String greqlExp, AttributedElementClass aec) {
		Set<ConstraintViolation> brokenConstraints = new HashSet<ConstraintViolation>();
		if (result.isBoolean()) {
			if (!result.toBoolean()) {
				JValueRecord rec = new JValueRecord();
				rec.add("greqlExpression", new JValue(greqlExp));
				rec.add("result", new JValue(false));
				rec.add("attributedElementClass", new JValue(aec));
				brokenConstraints.add(new ConstraintViolation(
						ConstraintType.GREQL, rec));
			}
		} else if (result.isCollection()) {
			JValueCollection c = result.toCollection();
			for (JValue jv : c) {
				JValueRecord rec = new JValueRecord();
				rec.add("greqlExpression", new JValue(greqlExp));
				rec.add("result", jv);
				rec.add("attributedElementClass", new JValue(aec));
				brokenConstraints.add(new ConstraintViolation(
						ConstraintType.GREQL, rec));
			}
		} else {
			// TODO: normally we shouldn't get here, so maybe we should handle
			// that situation more appropriate...
			JValueRecord rec = new JValueRecord();
			rec.add("greqlExpression", new JValue(greqlExp));
			rec.add("result", result);
			rec.add("attributedElementClass", new JValue(aec));
			brokenConstraints.add(new ConstraintViolation(ConstraintType.GREQL,
					rec));
		}
		return brokenConstraints;
	}
}
