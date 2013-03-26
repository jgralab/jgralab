/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
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
package de.uni_koblenz.jgralab.graphvalidator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.EdgeDirection;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.GraphIO;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.exception.GraphIOException;
import de.uni_koblenz.jgralab.greql.GreqlQuery;
import de.uni_koblenz.jgralab.greql.exception.GreqlException;
import de.uni_koblenz.jgralab.impl.ConsoleProgressFunction;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.Constraint;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.schema.Schema;

/**
 * A <code>GraphValidator</code> can be used to check whether all
 * {@link Constraint}s specified in the {@link Schema} of a given {@link Graph}
 * are fulfilled.
 * 
 * @author ist@uni-koblenz.de
 */
public class GraphValidator {

	private final Graph graph;

	/**
	 * @param graph
	 *            the {@link Graph} to validate
	 */
	public GraphValidator(Graph graph) {
		this.graph = graph;
	}

	// TODO: Add proper apache common CLI handling!
	public static void main(String[] args) throws GraphIOException, IOException {
		if (args.length != 1) {
			System.err.println("Usage: java GraphValidator <graph.tg>");
			System.exit(1);
		}
		Graph g = GraphIO.loadGraphFromFile(args[0],
				new ConsoleProgressFunction("Loading"));
		GraphValidator v = new GraphValidator(g);
		v.createValidationReport("__validation_report.html");
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

		int toMin = ec.getTo().getMin();
		int toMax = ec.getTo().getMax();
		Map<AttributedElement<?, ?>, Integer> badOutgoing = new HashMap<AttributedElement<?, ?>, Integer>();
		for (Vertex v : graph.vertices(ec.getFrom().getVertexClass())) {
			int degree = v.getDegree(ec, EdgeDirection.OUT);
			if ((degree < toMin) || (degree > toMax)) {
				badOutgoing.put(v, degree);
			}
		}
		if (!badOutgoing.isEmpty()) {
			brokenConstraints.add(new MultiplicityConstraintViolation(ec,
					"Invalid number of outgoing edges, allowed are (" + toMin
							+ "," + (toMax == Integer.MAX_VALUE ? "*" : toMax)
							+ ").", badOutgoing));
		}

		int fromMin = ec.getFrom().getMin();
		int fromMax = ec.getFrom().getMax();
		Map<AttributedElement<?, ?>, Integer> badIncoming = new HashMap<AttributedElement<?, ?>, Integer>();
		for (Vertex v : graph.vertices(ec.getTo().getVertexClass())) {
			int degree = v.getDegree(ec, EdgeDirection.IN);
			if ((degree < fromMin) || (degree > fromMax)) {
				badIncoming.put(v, degree);
			}
		}
		if (!badIncoming.isEmpty()) {
			brokenConstraints.add(new MultiplicityConstraintViolation(ec,
					"Invalid number of incoming edges, allowed are (" + fromMin
							+ ","
							+ (fromMax == Integer.MAX_VALUE ? "*" : fromMax)
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
		for (EdgeClass ec : graph.getGraphClass().getEdgeClasses()) {
			brokenConstraints.addAll(validateMultiplicities(ec));
		}

		// check if all greql constraints are met
		List<AttributedElementClass<?, ?>> aecs = new ArrayList<AttributedElementClass<?, ?>>();
		aecs.add(graph.getSchema().getGraphClass());
		aecs.addAll(graph.getSchema().getGraphClass().getVertexClasses());
		aecs.addAll(graph.getSchema().getGraphClass().getEdgeClasses());
		for (AttributedElementClass<?, ?> aec : aecs) {
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
			AttributedElementClass<?, ?> aec) {
		SortedSet<ConstraintViolation> brokenConstraints = new TreeSet<ConstraintViolation>();
		for (Constraint constraint : aec.getConstraints()) {
			String query = constraint.getPredicate();
			try {
				if (!(Boolean) GreqlQuery.createQuery(query).evaluate(graph)) {
					if (constraint.getOffendingElementsQuery() != null) {
						query = constraint.getOffendingElementsQuery();
						@SuppressWarnings("unchecked")
						Set<AttributedElement<?, ?>> resultSet = (Set<AttributedElement<?, ?>>) GreqlQuery
								.createQuery(query).evaluate(graph);
						brokenConstraints.add(new GReQLConstraintViolation(aec,
								constraint, resultSet));
					} else {
						brokenConstraints.add(new GReQLConstraintViolation(aec,
								constraint, null));
					}
				}
			} catch (GreqlException e) {
				brokenConstraints.add(new BrokenGReQLConstraintViolation(aec,
						constraint, query));
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
	 *         invalidation, sorted by their type
	 * @see GraphValidator#validate()
	 * @throws IOException
	 *             if the given file cannot be written
	 */
	public SortedSet<ConstraintViolation> createValidationReport(String fileName)
			throws IOException {
		SortedSet<ConstraintViolation> brokenConstraints = validate();

		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(new File(fileName)));
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
					+ graph.getSchemaClass().getSimpleName() + " with id "
					+ graph.getId() + ".");
			bw.append("</title>");
			bw.append("</head>");

			// The body
			bw.append("<body>");

			if (brokenConstraints.size() == 0) {
				bw.append("<p><b>The graph is valid!</b></p>");
			} else {
				bw.append("<p><b>The " + graph.getSchemaClass().getSimpleName()
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
					if ((no % 2) == 0) {
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
						for (AttributedElement<?, ?> ae : ci
								.getOffendingElements()) {
							bw.append(ae.toString());
							if (ci instanceof MultiplicityConstraintViolation) {
								bw.append(", degree=")
										.append(Integer
												.toString(((MultiplicityConstraintViolation) ci)
														.getDegree(ae)));
							}
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
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException ex) {
					throw new RuntimeException(
							"An Exception occurred while closing the stream.",
							ex);
				}
			}
		}

		return brokenConstraints;
	}
}
