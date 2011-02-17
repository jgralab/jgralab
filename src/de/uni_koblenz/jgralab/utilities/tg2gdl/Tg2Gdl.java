/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2010 Institute for Software Technology
 *                         University of Koblenz-Landau, Germany
 *                         ist@uni-koblenz.de
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
package de.uni_koblenz.jgralab.utilities.tg2gdl;

import java.io.PrintStream;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.schema.Attribute;
import de.uni_koblenz.jgralab.utilities.tg2whatever.Tg2Whatever;

/**
 * @author Tassilo Horn <horn@uni-koblenz.de>
 * 
 */
public class Tg2Gdl extends Tg2Whatever {

	private int indent = 0;

	private void indent(PrintStream out) {
		for (int i = 0; i < indent; i++) {
			out.print("  ");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.utilities.tg2whatever.Tg2Whatever#graphEnd(java
	 * .io.PrintStream)
	 */
	@Override
	protected void graphEnd(PrintStream out) {
		indent--;
		indent(out);
		out.println("}");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.utilities.tg2whatever.Tg2Whatever#graphStart(java
	 * .io.PrintStream)
	 */
	@Override
	protected void graphStart(PrintStream out) {
		indent(out);
		out.print("graph: { layout_algorithm: maxdegree finetuning: yes ");
		indent++;
		printLabel(graph, out);
		out.println();
	}

	private void printLabel(AttributedElement ae, PrintStream out) {
		out.print("label: \"");
		if (ae instanceof Vertex) {
			out.print("v" + ((Vertex) ae).getId() + " : "
					+ ae.getAttributedElementClass().getSimpleName() + "\\n");
		} else if (ae instanceof Edge) {
			out.print("e" + ((Edge) ae).getId() + " : "
					+ ae.getAttributedElementClass().getSimpleName() + "\\n");
		}
		boolean first = true;
		for (Attribute attr : ae.getAttributedElementClass().getAttributeList()) {
			if (first) {
				first = false;
			} else {
				out.print("\\n");
			}
			out.print(attr.getName()
					+ " = "
					+ stringQuote(String.valueOf(ae
							.getAttribute(attr.getName()))));
		}
		out.print("\"");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.utilities.tg2whatever.Tg2Whatever#printEdge(java
	 * .io.PrintStream, de.uni_koblenz.jgralab.Edge)
	 */
	@Override
	protected void printEdge(PrintStream out, Edge e) {
		indent(out);
		out.print("edge: { source: " + vTitle(e.getAlpha()) + " target: "
				+ vTitle(e.getOmega()) + " ");
		printLabel(e, out);
		out.println(" }");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.utilities.tg2whatever.Tg2Whatever#printVertex(
	 * java.io.PrintStream, de.uni_koblenz.jgralab.Vertex)
	 */
	@Override
	protected void printVertex(PrintStream out, Vertex v) {
		indent(out);
		out.print("node: { title: " + vTitle(v) + " ");
		printLabel(v, out);
		out.println(" }");
	}

	private String vTitle(Vertex v) {
		return "\"v" + v.getId() + "\"";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_koblenz.jgralab.utilities.tg2whatever.Tg2Whatever#stringQuote(
	 * java.lang.String)
	 */
	@Override
	protected String stringQuote(String s) {
		// TODO Auto-generated method stub
		return s.replace("\"", "\\\"");
	}

	public static void main(String[] args) {
		Tg2Gdl converter = new Tg2Gdl();
		converter.getOptions(args);
		converter.convert();
	}
}
