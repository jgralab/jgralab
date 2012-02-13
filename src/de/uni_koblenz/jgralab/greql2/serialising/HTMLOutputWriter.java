/*
 * JGraLab - The Java Graph Laboratory
 * 
 * Copyright (C) 2006-2012 Institute for Software Technology
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
package de.uni_koblenz.jgralab.greql2.serialising;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map.Entry;

import org.pcollections.ArrayPMap;
import org.pcollections.PCollection;
import org.pcollections.PMap;
import org.pcollections.POrderedSet;
import org.pcollections.PSet;
import org.pcollections.PVector;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Record;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.exception.SerialisingException;
import de.uni_koblenz.jgralab.greql2.types.Path;
import de.uni_koblenz.jgralab.greql2.types.PathSystem;
import de.uni_koblenz.jgralab.greql2.types.Slice;
import de.uni_koblenz.jgralab.greql2.types.Table;
import de.uni_koblenz.jgralab.greql2.types.Tuple;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class HTMLOutputWriter extends DefaultWriter {

	private boolean createElementLinks;
	private boolean useCss;
	private PrintWriter out;

	public HTMLOutputWriter() {
		this(null);
	}

	public HTMLOutputWriter(Graph g) {
		super(g);
		useCss = true;
		createElementLinks = false;
	}

	public void writeValue(Object value, File file) throws IOException,
			SerialisingException {
		out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "UTF-8")));
			writeValue(value);
		} catch (SerialisingException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new SerialisingException("Unhandled Exception", rootValue, e);
		} finally {
			if (out != null) {
				out.close();
				out = null;
			}
		}
	}

	public boolean isCreateElementLinks() {
		return createElementLinks;
	}

	public void setCreateElementLinks(boolean createElementLinks) {
		this.createElementLinks = createElementLinks;
	}

	public boolean isUseCss() {
		return useCss;
	}

	public void setUseCss(boolean useCss) {
		this.useCss = useCss;
	}

	@Override
	protected void write(Object o) throws IOException {
		try {
			super.write(o);
		} catch (IOException e) {
			throw e;
		} catch (SerialisingException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Unexpected Exception", e);
		}
	}

	@Override
	protected void writeSlice(Slice s) throws Exception {
		writeDefaultObject(s);
	}

	@Override
	protected void writePathSystem(PathSystem p) throws Exception {
		writeDefaultObject(p);
	}

	private String htmlQuote(String string) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < string.length(); ++i) {
			char c = string.charAt(i);
			if (c == '<') {
				result.append("&lt;");
			} else if (c == '>') {
				result.append("&gt;");
			} else if (c == '"') {
				result.append("&quot;");
			} else if (c == '\'') {
				result.append("&apos;");
			} else if (c == '&') {
				result.append("&amp;");
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}

	@Override
	protected void pre() throws IOException {
		out.println(table(1, 2) + "<tr><td>");
	}

	@Override
	protected void post() throws IOException {
		out.println("</td></tr></table>");
	}

	@Override
	protected void inter() throws IOException {
		out.println("</td></tr><tr><td>");
	}

	@Override
	protected void writeTuple(Tuple t) throws IOException {
		out.println(table(1, 2) + "<tr><td>");
		boolean first = true;
		for (Object val : t) {
			if (first) {
				first = false;
			} else {
				out.println("</td><td>");
			}
			write(val);
		}
		out.println("</td></tr></table>");
	}

	@Override
	protected void writeRecord(Record r) throws IOException {
		out.println(table(1, 2) + "<tr><td>");
		boolean first = true;
		for (String compName : r.getComponentNames()) {
			if (first) {
				first = false;
			} else {
				out.println("</td><td>");
			}
			out.println(compName + ": ");
			write(r.getComponent(compName));
		}
		out.println("</td></tr></table>");
	}

	@Override
	protected void writeTable(Table<?> table) throws IOException {
		out.print(table(1, 2) + "<tr><th>");
		boolean first = true;
		for (Object val : table.getTitles()) {
			if (first) {
				first = false;
			} else {
				out.print("</th><th>");
			}
			write(val);
		}
		out.println("</th></tr>");
		for (Object o : table) {
			out.println("<tr><td>");
			if (o instanceof Tuple) {
				first = true;
				for (Object val : (Tuple) o) {
					if (first) {
						first = false;
					} else {
						out.println("</td><td>");
					}
					write(val);
				}
			} else {
				write(o);
			}
			out.println("</td></tr>");
		}
		out.println("</table>");
	}

	@Override
	protected void writePath(Path p) throws IOException {
		boolean first = true;
		pre();
		PVector<Edge> edges = p.getEdgeTrace();
		PVector<Vertex> vertices = p.getVertexTrace();
		for (int i = 0; i < vertices.size() - 1; i++) {
			if (first) {
				first = false;
				write(vertices.get(i));
			}
			inter();
			write(edges.get(i));
			inter();
			write(vertices.get(i + 1));
		}
		post();
	}

	@Override
	protected void writeVertex(Vertex vertex) throws IOException {
		if (createElementLinks) {
			out.println("<a href=\"v" + vertex.getId() + "\">v"
					+ vertex.getId() + ": "
					+ vertex.getAttributedElementClass().getUniqueName()
					+ "</a>");
		} else {
			out.println("v" + vertex.getId() + ": "
					+ vertex.getAttributedElementClass().getUniqueName());
		}
	}

	@Override
	protected void writeEdge(Edge edge) throws IOException {
		if (createElementLinks) {
			out.println("<a href=\"e" + edge.getId() + "\">e" + edge.getId()
					+ ": " + edge.getAttributedElementClass().getUniqueName()
					+ "</a>");
		} else {
			out.println("e" + edge.getId() + ": "
					+ edge.getAttributedElementClass().getUniqueName());
		}
	}

	@Override
	protected void writeInteger(Integer b) throws IOException {
		out.println(b.toString());
	}

	@Override
	protected void writeLong(Long b) throws IOException {
		out.println(b.toString());
	}

	@Override
	protected void writeDouble(Double b) throws IOException {
		out.println(b.toString());
	}

	@Override
	protected void writeString(String b) throws IOException {
		out.println(htmlQuote(b));
	}

	@Override
	protected void writeEnum(Enum<?> e) throws IOException {
		String b = e.toString();
		out.println(b);
	}

	@Override
	protected void writeGraph(Graph gr) throws IOException {
		if (createElementLinks) {
			out.println("<a href=\"g" + gr.getId() + "\">" + gr.getId() + ": "
					+ gr.getAttributedElementClass().getUniqueName() + "</a>");
		} else {
			out.println(gr.getId() + ": "
					+ gr.getAttributedElementClass().getUniqueName());
		}
	}

	@Override
	protected void writeBoolean(Boolean v) throws IOException {
		if (v != null) {
			out.println(v.toString());
		} else {
			out.println("null");
		}
	}

	@Override
	protected void writeDefaultObject(Object o) throws IOException {
		out.println("<pre>");
		out.println(htmlQuote(o.toString()));
		out.println("</pre>");
	}

	@Override
	protected void writeAttributedElementClass(AttributedElementClass<?, ?> c)
			throws IOException {
		out.println(c.getQualifiedName());
	}

	@Override
	protected void writePVector(PVector<?> b) throws Exception {
		if (b.size() > 0 && b.get(0) instanceof Tuple) {
			writeTableOfTuples(b);
		} else {
			super.writePVector(b);
		}
	}

	private void writeTableOfTuples(PCollection<?> c) throws IOException {
		out.print(table(1, 2));
		boolean first = true;
		for (Object o : c) {
			out.println("<tr><td>");
			if (o instanceof Tuple) {
				first = true;
				for (Object val : (Tuple) o) {
					if (first) {
						first = false;
					} else {
						out.println("</td><td>");
					}
					write(val);
				}
			} else {
				write(o);
			}
			out.println("</td></tr>");
		}
		out.println("</table>");
	}

	@Override
	protected void writePSet(PSet<?> s) throws Exception {
		if (s.size() > 0) {
			if ((s instanceof POrderedSet)
					&& (((POrderedSet<?>) s).get(0) instanceof Tuple)) {
				writeTableOfTuples(s);
				return;
			}
		}
		super.writePSet(s);
	}

	@Override
	protected void writePMap(PMap<?, ?> b) throws Exception {
		out.print(table(1, 2));
		if (b instanceof ArrayPMap) {
			ArrayPMap<?, ?> m = (ArrayPMap<?, ?>) b;
			for (Entry<?, ?> e : m) {
				out.print("<tr><td>");
				write(e.getKey());
				out.print("</td><td>");
				write(e.getValue());
				out.print("</td></tr>");
			}
		} else {
			for (Entry<?, ?> e : b.entrySet()) {
				out.print("<tr><td>");
				write(e.getKey());
				out.print("</td><td>");
				write(e.getValue());
				out.print("</td></tr>");
			}
		}
		out.print("</table>");
	}

	@Override
	protected void head() throws IOException {
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<html>");
		out.println("<head>");
		out.println("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">");
		if (useCss) {
			out.println("<style type=\"text/css\">");
			out.println("table { border: thin gray solid; border-collapse: collapse; border-spacing: 2px }");
			out.println("td { border: thin gray solid; border-collapse: collapse; border-spacing: 2px }");
			out.println("th { border: thin gray solid; border-collapse: collapse; border-spacing: 2px }");
			out.println("</style>\n");
		}
		out.println("</head><body>");
		if (getGraph() != null) {
			out.print("<p>Graph id: " + getGraph().getId() + "</p>");
		}
		out.print("<p>Result size: ");
		if (rootValue instanceof PCollection) {
			out.println(Integer.toString(((PCollection<?>) rootValue).size()));
		} else {
			out.println("1");
		}
		out.println("</p><hr/>");
	}

	private String table(int border, int padding) {
		return table(border, padding, 0);
	}

	private String table(int border, int padding, int spacing) {
		return "<table border=\"" + border + "\" cellpadding=\"" + padding
				+ "\" cellspacing=\"" + spacing + "\">";
	}

	@Override
	protected void writeUndefined() throws Exception {
		out.print("&not;");
	}

	@Override
	protected void foot() throws IOException {
		out.println("</body></html>");
	}
}
