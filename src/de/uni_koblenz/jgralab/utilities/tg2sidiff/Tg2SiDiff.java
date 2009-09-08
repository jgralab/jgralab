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

package de.uni_koblenz.jgralab.utilities.tg2sidiff;

import java.io.PrintStream;

import de.uni_koblenz.jgralab.Aggregation;
import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.AggregationClass;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.utilities.tg2whatever.Tg2Whatever;

public class Tg2SiDiff extends Tg2Whatever {

	public static final boolean PRINT_EDGES_AS_NODES = false;

	/**
	 * prints the graph to the output file − <Document> <Graph
	 * name="Unnamed Project" root="id261">
	 */
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
							.equals("SoftwareCase")))
				id = Integer.toString(v.getId());
			v = v.getNextVertex();
		}
		if (id == null)
			id = "none";
		return "vertex" + id;
	}

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
	protected void printVertex(PrintStream out, Vertex v) {
		AttributedElementClass cls = v.getAttributedElementClass();
		out.print("<Node type=\"" + cls.getQualifiedName() + "\" id=\"vertex"
				+ v.getId() + "\">\n");
		if (cls.getAttributeCount() > 0) {
			printAttributes(out, v);
		}
		out.print("</Node>\n");
	}

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
				if (ch < ' ' || ch > '\u007F') {
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
	protected void printEdge(PrintStream out, Edge e) {
		EdgeClass cls = (EdgeClass) e.getAttributedElementClass();
		Vertex alpha = (reversedEdges ? e.getOmega() : e.getAlpha());
		Vertex omega = (reversedEdges ? e.getAlpha() : e.getOmega());
		boolean aggregateTo = false;
		boolean aggregateFrom = false;
		if (e instanceof Aggregation) {
			AggregationClass ac = (AggregationClass) cls;
			aggregateFrom = ac.isAggregateFrom() ^ reversedEdges;
			aggregateTo = !aggregateFrom;
		}

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
			try {
				Object val = elem.getAttribute(attr.getName());
				String attributeValue = "null";
				if (val != null)
					attributeValue = stringQuote(val.toString());
				out.print("    <Attribute name=\"" + attr.getName()
						+ "\" value=\"" + attributeValue + "\"/>\n");
			} catch (NoSuchFieldException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Tg2SiDiff converter = new Tg2SiDiff();
		converter.getOptions(args);
		converter.printGraph();
	}

}
