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

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.evaluator.GreqlEvaluator;
import de.uni_koblenz.jgralab.greql2.exception.Greql2Exception;
import de.uni_koblenz.jgralab.greql2.jvalue.JValue;
import de.uni_koblenz.jgralab.greql2.jvalue.JValueSet;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Constraint;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.Schema;

/**
 * A <code>GraphValidator</code> can be used to check if all {@link Constraint}s
 * specified in the {@link Schema} of a given {@link Graph} are fulfilled.
 * 
 * @author Tassilo Horn <horn@uni-koblenz.de>
 */
public class GraphValidator {

	private Graph graph;
	private GreqlEvaluator eval;

	/**
	 * @param graph
	 *            the {@link Graph} to validate
	 */
	public GraphValidator(Graph graph) {
		this.graph = graph;
		this.eval = new GreqlEvaluator((String) null, graph, null);
	}

	/**
	 * Checks if all multiplicities specified for the {@link EdgeClass}
	 * <code>ec</code> are fulfilled.
	 * 
	 * 
	 * @param ec
	 *            an {@link EdgeClass}
	 * @return a set of {@link MultiplicityConstraintViolation} describing which
	 *         and where {@link MultiplicityConstraintViolation} constraints
	 *         where violated
	 */
	public SortedSet<MultiplicityConstraintViolation> validateMultiplicities(
			EdgeClass ec) {
		SortedSet<MultiplicityConstraintViolation> brokenConstraints = new TreeSet<MultiplicityConstraintViolation>();

		int toMin = ec.getToMin();
		int toMax = ec.getToMax();
		Set<AttributedElement> badOutgoing = new HashSet<AttributedElement>();
		for (Vertex v : graph.vertices(ec.getFrom())) {
			int degree = v.getDegree(ec, EdgeDirection.OUT);
			if ((degree < toMin) || (degree > toMax)) {
				badOutgoing.add(v);
			}
		}
		if (!badOutgoing.isEmpty()) {
			brokenConstraints.add(new MultiplicityConstraintViolation(ec,
					"Invalid number of outgoing edges, allowed are (" + toMin
							+ ", "
							+ ((toMax == Integer.MAX_VALUE) ? "*" : toMax)
							+ ").", badOutgoing));
		}

		int fromMin = ec.getFromMin();
		int fromMax = ec.getFromMax();
		Set<AttributedElement> badIncoming = new HashSet<AttributedElement>();
		for (Vertex v : graph.vertices(ec.getTo())) {
			int degree = v.getDegree(ec, EdgeDirection.IN);
			if ((degree < fromMin) || (degree > fromMax)) {
				badIncoming.add(v);
			}
		}
		if (!badIncoming.isEmpty()) {
			brokenConstraints.add(new MultiplicityConstraintViolation(ec,
					"Invalid number of incoming edges, allowed are (" + fromMin
							+ ", "
							+ ((fromMax == Integer.MAX_VALUE) ? "*" : fromMax)
							+ ").", badIncoming));
		}
		return brokenConstraints;
	}

	/**
	 * Validates all constraints of the graph.
	 * 
	 * @see GraphValidator#validateMultiplicities(EdgeClass)
	 * @see GraphValidator#validateConstraints(AttributedElementClass)
	 * @return a set of {@link ConstraintViolation} objects, one for each
	 *         violation, sorted by their type
	 */
	public SortedSet<ConstraintViolation> validate() {
		SortedSet<ConstraintViolation> brokenConstraints = new TreeSet<ConstraintViolation>();

		// Check if all multiplicities are correct
		for (EdgeClass ec : graph.getSchema()
				.getEdgeClassesInTopologicalOrder()) {
			if (ec.isInternal()) {
				continue;
			}
			brokenConstraints.addAll(validateMultiplicities(ec));
		}

		// check if all greql constraints are met
		List<AttributedElementClass> aecs = new ArrayList<AttributedElementClass>();
		aecs.add(graph.getSchema().getGraphClass());
		aecs.addAll(graph.getSchema().getVertexClassesInTopologicalOrder());
		aecs.addAll(graph.getSchema().getEdgeClassesInTopologicalOrder());
		for (AttributedElementClass aec : aecs) {
			if (aec.isInternal()) {
				continue;
			}
			brokenConstraints.addAll(validateConstraints(aec));
		}
		return brokenConstraints;
	}

	/**
	 * Checks if all {@link Constraint}s attached to the
	 * {@link AttributedElementClass} <code>aec</code> are fulfilled.
	 * 
	 * @param aec
	 *            an {@link AttributedElementClass}
	 * @return a set of {@link ConstraintViolation} objects
	 */
	public SortedSet<ConstraintViolation> validateConstraints(
			AttributedElementClass aec) {
		SortedSet<ConstraintViolation> brokenConstraints = new TreeSet<ConstraintViolation>();
		for (Constraint constraint : aec.getConstraints()) {
			String query = constraint.getPredicate();
			eval.setQuery(query);
			try {
				eval.startEvaluation();
				if (!eval.getEvaluationResult().toBoolean()) {
					if (constraint.getOffendingElementsQuery() != null) {
						query = constraint.getOffendingElementsQuery();
						eval.setQuery(query);
						eval.startEvaluation();
						JValueSet resultSet = eval.getEvaluationResult()
								.toJValueSet();
						brokenConstraints.add(new GReQLConstraintViolation(aec,
								constraint, jvalueSet2Set(resultSet)));
					} else {
						brokenConstraints.add(new GReQLConstraintViolation(aec,
								constraint, null));
					}
				}
			} catch (Greql2Exception e) {
				brokenConstraints.add(new BrokenGReQLConstraintViolation(aec,
						constraint, query));
			}
		}
		return brokenConstraints;
	}

	private Set<AttributedElement> jvalueSet2Set(JValueSet resultSet) {
		Set<AttributedElement> set = new HashSet<AttributedElement>(resultSet
				.size());
		for (JValue jv : resultSet) {
			set.add(jv.toAttributedElement());
		}
		return set;
	}

	/**
	 * Do just like {@link GraphValidator#validate()}, but generate a HTML
	 * report saved to <code>fileName</code>, too.
	 * 
	 * @param fileName
	 *            the name of the HTML report file
	 * @return a set of {@link ConstraintViolation} objects, one for each
	 *         invalidation, sorted by their type
	 * @see GraphValidator#validate()
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
		bw.append("<head>");

		bw.append("<style type=\"text/css\">");
		bw.append("th {");
		bw.append("	font: bold 11px sans-serif;");
		bw.append("	color: MidnightBlue;");
		bw.append("	border-right: 1px solid #C1DAD7;");
		bw.append("	border-bottom: 1px solid #C1DAD7;");
		bw.append("	border-top: 1px solid #C1DAD7;");
		bw.append("	letter-spacing: 2px;");
		bw.append("	text-align: left;");
		bw.append(" padding: 6px 6px 6px 12px;");
		bw.append("	background: #CAE8EA;");
		bw.append("}");
		bw.append("td {");
		bw.append(" border-right: 1px solid #C1DAD7;");
		bw.append("	border-bottom: 1px solid #C1DAD7;");
		bw.append("	background: #fff;");
		bw.append("	padding: 6px 6px 6px 12px;");
		bw.append("	color: DimGrey;");
		bw.append("}");
		bw.append("td.other {");
		bw.append(" border-right: 1px solid #C1DAD7;");
		bw.append("	border-bottom: 1px solid #C1DAD7;");
		bw.append("	background: AliceBlue;");
		bw.append("	padding: 6px 6px 6px 12px;");
		bw.append("	color: DimGrey;");
		bw.append("}");
		bw.append("</style>");

		bw.append("<title>");
		bw.append("Validation Report for the "
				+ graph.getM1Class().getSimpleName() + " with id "
				+ graph.getId() + ".");
		bw.append("</title>");
		bw.append("</head>");

		// The body
		bw.append("<body>");

		if (brokenConstraints.size() == 0) {
			bw.append("<p><b>The graph is valid!</b></p>");
		} else {
			bw.append("<p><b>The " + graph.getM1Class().getSimpleName()
					+ " violates " + brokenConstraints.size()
					+ " constraints.</b></p>");
			// Here goes the table
			bw.append("<table border=\"1\">");
			bw.append("<tr>");
			bw.append("<th>#</th>");
			bw.append("<th>ConstraintType</th>");
			bw.append("<th>AttributedElementClass</th>");
			bw.append("<th>Message</th>");
			bw.append("<th>Broken Elements</th>");
			bw.append("</tr>");
			int no = 1;
			String cssClass = "";
			for (ConstraintViolation ci : brokenConstraints) {
				if (no % 2 == 0) {
					cssClass = "other";
				} else {
					cssClass = "";
				}
				bw.append("<tr>");
				bw.append("<td class=\"" + cssClass + "\">");
				bw.append(Integer.valueOf(no++).toString());
				bw.append("</td>");
				bw.append("<td class=\"" + cssClass + "\">");
				bw.append(ci.getClass().getSimpleName());
				bw.append("</td>");
				bw.append("<td class=\"" + cssClass + "\">");
				bw.append(ci.getAttributedElementClass().getQualifiedName());
				bw.append("</td>");
				bw.append("<td class=\"" + cssClass + "\">");
				bw.append(ci.getMessage());
				bw.append("</td>");
				bw.append("<td class=\"" + cssClass + "\">");
				if (ci.getOffendingElements() != null) {
					for (AttributedElement ae : ci.getOffendingElements()) {
						bw.append(ae.toString());
						bw.append("<br/>");
					}
				}
				bw.append("</td>");
				bw.append("</tr>");
			}
			bw.append("</table>");
		}

		bw.append("</body></html>");
		bw.flush();
		bw.close();
		return brokenConstraints;
	}
}
