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

package de.uni_koblenz.jgralab.utilities.tg2sidiff;

import java.io.PrintStream;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.AggregationKind;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.utilities.tg2whatever.Tg2Whatever;

public class Tg2SiDiff extends Tg2Whatever {

	public static final boolean PRINT_EDGES_AS_NODES = false;

	/**
	 * prints the graph to the output file − <Document> <Graph
	 * name="Unnamed Project" root="id261">
	 */
	@Override
	protected void graphStart(PrintStream out) {
		out.println("<Document type=\"RSL\">");
		out.println("<Graph name=\"" + graph.getId() + "\" root=\""
				+ getRootVertexId() + "\">");
	}

	private String getRootVertexId() {
		Vertex v = graph.getFirstVertex();
		String id = null;
		while ((v != null) && (id == null)) {
			if (((marker == null) || (marker.isMarked(v)))
					&& (v.getAttributedElementClass().getQualifiedName()
							.equals("SoftwareCase"))) {
				id = Integer.toString(v.getId());
			}
			v = v.getNextVertex();
		}
		if (id == null) {
			id = "none";
		}
		return "vertex" + id;
	}

	@Override
	public void graphEnd(PrintStream out) {
		out.println("</Graph>");
		out.print("</Document>");
	}

	/*
	 * <Node type="model" id="vertex261"> <Attribute name="name"
	 * value="Unnamed Project"/> </Node> (non-Javadoc)
	 * 
	 * @see
	 * jgralab.utilities.tg2whatever.Tg2Whatever#printVertex(java.io.PrintStream
	 * , jgralab.Vertex)
	 */
	@Override
	protected void printVertex(PrintStream out, Vertex v) {
		AttributedElementClass cls = v.getAttributedElementClass();
		out.print("<Node type=\"" + cls.getQualifiedName() + "\" id=\"vertex"
				+ v.getId() + "\">\n");
		if (cls.getAttributeCount() > 0) {
			printAttributes(out, v);
		}
		out.print("</Node>\n");
	}

	@Override
	protected String stringQuote(String s) {
		StringBuffer sb = new StringBuffer();
		for (char ch : s.toCharArray()) {
			switch (ch) {
			case '\\':
				sb.append("\\\\");
				break;
			case '<':
				sb.append("\\<");
				break;
			case '>':
				sb.append("\\>");
				break;
			case '{':
				sb.append("\\{");
				break;
			case '}':
				sb.append("\\}");
				break;
			case '"':
				sb.append("&quot;");
				break;
			case '|':
				sb.append("\\|");
				break;
			case '\n':
				sb.append("\\\\n");
				break;
			case '\r':
				sb.append("\\\\r");
				break;
			case '\t':
				sb.append("\\\\t");
				break;
			default:
				if ((ch < ' ') || (ch > '\u007F')) {
					sb.append("\\\\u");
					String code = ("000" + Integer.toHexString(ch));
					sb.append(code.substring(code.length() - 4, code.length()));
				} else {
					sb.append(ch);
				}
				break;
			}
		}
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jgralab.utilities.tg2whatever.Tg2Whatever#printEdge(java.io.PrintStream,
	 * jgralab.Edge) <Node type="Contains" id="edge2"> <Attribute name="name"
	 * value="...."/> </Node> <Edge type="fromContains" src="vertex2"
	 * tar="edge2" nesting="true"/>
	 */
	@Override
	protected void printEdge(PrintStream out, Edge e) {
		EdgeClass cls = (EdgeClass) e.getAttributedElementClass();
		Vertex alpha = (reversedEdges ? e.getOmega() : e.getAlpha());
		Vertex omega = (reversedEdges ? e.getAlpha() : e.getOmega());
		boolean aggregateTo = (e.getAlphaSemantics() != AggregationKind.NONE);
		boolean aggregateFrom = (e.getOmegaSemantics() != AggregationKind.NONE);

		if (PRINT_EDGES_AS_NODES) {
			out.print("<Node type=\"" + cls.getQualifiedName() + "\" id=\"edge"
					+ e.getId() + "\">\n");
			if (cls.getAttributeCount() > 0) {
				printAttributes(out, e);
			}
			out.print("</Node>\n");

			out.print("<Edge type=\"" + cls.getQualifiedName()
					+ "From\" src=\"vertex" + alpha.getId() + "\" tar=\"edge"
					+ e.getId() + "\" nesting=\"" + aggregateFrom + "\"/>\n");
			out.print("<Edge type=\"" + cls.getQualifiedName()
					+ "To\" src=\"edge" + e.getId() + "\" tar=\"vertex"
					+ omega.getId() + "\" nesting=\"" + aggregateTo + "\"/>\n");
		} else {
			out.print("<Edge type=\"" + cls.getQualifiedName()
					+ "\" src=\"vertex" + alpha.getId() + "\" tar=\"vertex"
					+ omega.getId() + "\" nesting=\"" + aggregateFrom
					+ "\"/>\n");
		}
	}

	/*
	 * <Attribute name="isAbstract" value="false"/> <Attribute name="name"
	 * value="Dimension"/> <Attribute name="visibility" value="public"/>
	 */
	private void printAttributes(PrintStream out, AttributedElement elem) {
		AttributedElementClass cls = elem.getAttributedElementClass();
		for (Attribute attr : cls.getAttributeList()) {
			Object val = elem.getAttribute(attr.getName());
			String attributeValue = "null";
			if (val != null) {
				attributeValue = stringQuote(val.toString());
			}
			out.print("    <Attribute name=\"" + attr.getName() + "\" value=\""
					+ attributeValue + "\"/>\n");
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Tg2SiDiff converter = new Tg2SiDiff();
		converter.getOptions(args);
		converter.convert();
	}

}
