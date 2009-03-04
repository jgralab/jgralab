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

package de.uni_koblenz.jgralab.utilities.tg2dot;

import java.io.PrintStream;

import de.uni_koblenz.jgralab.Attribute;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;
import de.uni_koblenz.jgralab.schema.EdgeClass;
import de.uni_koblenz.jgralab.utilities.tg2whatever.Tg2Whatever;

public class Tg2Dot extends Tg2Whatever {

	private double ranksep = 1.5;
	private boolean ranksepEqually = false;
	private double nodesep = 0.25;
	private String fontname = "Helvetica";
	private int fontsize = 14;
	private boolean abbreviateEdgeAttributeNames = false;

	/**
	 * prints the graph to the output file
	 */
	@Override
	public void graphStart(PrintStream out) {
		out.println("digraph \"" + graph.getId() + "\"");
		out.println("{");

		// Set the ranksep
		if (ranksepEqually) {
			out.println("ranksep=\"" + ranksep + " equally\";");
		} else {
			out.println("ranksep=\"" + ranksep + "\";");
		}

		// Set the nodesep
		out.println("nodesep=\"" + nodesep + "\";");

		out.println("node [shape=\"record\" " + "fontname=\"" + fontname
				+ "\" " + "fontsize=\"" + fontsize + "\" color=\"#999999\"];");
		out.println("edge [fontname=\"" + fontname + "\" fontsize=\""
				+ fontsize + "\" labelfontname=\"" + fontname
				+ "\" labelfontsize=\"" + fontsize + "\" color=\"#999999\"];");
	}

	@Override
	public void graphEnd(PrintStream out) {
		out.println("}");
	}

	@Override
	protected void printVertex(PrintStream out, Vertex v) {
		AttributedElementClass cls = v.getAttributedElementClass();
		out.print("v" + v.getId() + " [label=\"{{v" + v.getId() + "|"
				+ cls.getUniqueName() + "}");
		if (cls.getAttributeCount() > 0) {
			out.print("|");
			printAttributes(out, v);
		}
		out.println("}\"];");
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
				sb.append("\\\"");
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

	@Override
	protected void printEdge(PrintStream out, Edge e) {
		Vertex alpha = (reversedEdges ? e.getOmega() : e.getAlpha());
		Vertex omega = (reversedEdges ? e.getAlpha() : e.getOmega());
		out.print("v" + alpha.getId() + " -> v" + omega.getId() + " [");
		if (reversedEdges) {
			out.print("dir=back ");
		}

		EdgeClass cls = (EdgeClass) e.getAttributedElementClass();

		if (roleNames) {
			String toRole = cls.getToRolename();
			if (toRole != null && toRole.length() > 0) {
				out.print((reversedEdges ? "tail" : "head") + "label=\""
						+ stringQuote(toRole) + "\" ");
			}
			String fromRole = cls.getFromRolename();
			if (fromRole != null && fromRole.length() > 0) {
				out.print((reversedEdges ? "head" : "tail") + "label=\""
						+ stringQuote(fromRole) + "\" ");
			}
		}

		out.print("label=\"e" + e.getId() + ": " + cls.getUniqueName() + "");

		if (edgeAttributes && cls.getAttributeCount() > 0) {
			out.print("\\l");
			printAttributes(out, e);
		}

		out.println("\"];");
	}

	private void printAttributes(PrintStream out, AttributedElement elem) {
		AttributedElementClass cls = elem.getAttributedElementClass();
		for (Attribute attr : cls.getAttributeList()) {
			try {
				if (abbreviateEdgeAttributeNames && elem instanceof Edge) {
					// sourcePosition => sP
					// fooBarBaz => fBB
					out.print(attr.getName().charAt(0)
							+ attr.getName().replaceAll("[a-z]+", ""));
				} else {
					out.print(attr.getName());
				}
				if (domainNames) {
					out.print(": "
							+ stringQuote(attr.getDomain().getQualifiedName()));
				}
				Object attribute = elem.getAttribute(attr.getName());
				String attributeString = (attribute != null) ? attribute
						.toString() : "null";
				if (shortenStrings && attributeString.length() > 17) {
					attributeString = attributeString.substring(0, 18) + "...";
				}
				if (attribute instanceof String) {
					attributeString = '"' + attributeString + '"';
				}
				out.print(" = " + stringQuote(attributeString) + "\\l");
			} catch (NoSuchFieldException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	public double getRanksep() {
		return ranksep;
	}

	/**
	 * Sets the desired rank separation, in inches.
	 * 
	 * This is the minimum vertical distance between the bottom of the nodes in
	 * one rank and the tops of nodes in the next.
	 * 
	 * @param ranksep
	 *            The value as described above.
	 */
	public void setRanksep(double ranksep) {
		this.ranksep = ranksep;
	}

	public double getNodesep() {
		return nodesep;
	}

	/**
	 * Sets the minimum space between two adjacent nodes in the same rank, in
	 * inches.
	 * 
	 * @param nodesep
	 *            Minimum space between two adjacent nodes in the same rank, in
	 *            inches.
	 */
	public void setNodesep(double nodesep) {
		this.nodesep = nodesep;
	}

	public boolean isRanksepEqually() {
		return ranksepEqually;
	}

	/**
	 * Decides if the space between all ranks should be equal.
	 * 
	 * @param ranksepEqually
	 *            If true the space between all ranks is equal.
	 */
	public void setRanksepEqually(boolean ranksepEqually) {
		this.ranksepEqually = ranksepEqually;
	}

	public String getFontname() {
		return fontname;
	}

	/**
	 * @param fontname
	 *            The name of the font to be used for nodes, edges and labels.
	 */
	public void setFontname(String fontname) {
		this.fontname = fontname;
	}

	public int getFontsize() {
		return fontsize;
	}

	/**
	 * @param fontsize
	 *            The size of the font used for nodes, edges and labels.
	 */
	public void setFontsize(int fontsize) {
		this.fontsize = fontsize;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Tg2Dot converter = new Tg2Dot();
		converter.getOptions(args);
		converter.printGraph();
	}

	public boolean isAbbreviateAttributeNames() {
		return abbreviateEdgeAttributeNames;
	}

	public void setAbbreviateAttributeNames(boolean abbreviateAttributeNames) {
		this.abbreviateEdgeAttributeNames = abbreviateAttributeNames;
	}
}
