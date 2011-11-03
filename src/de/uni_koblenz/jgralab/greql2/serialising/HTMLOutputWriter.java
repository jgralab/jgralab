package de.uni_koblenz.jgralab.greql2.serialising;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map.Entry;

import org.pcollections.ArrayPMap;
import org.pcollections.PCollection;
import org.pcollections.PMap;
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

	/**
	 * The writer which stores the elements
	 */
	private PrintWriter out;

	/**
	 * The graph all elements in the value to visit belong to
	 */
	private Graph dataGraph = null;

	private Object rootValue;

	private boolean createElementLinks;

	@Override
	public void write(Object o) throws IOException {
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
	public void writeSlice(Slice s) throws Exception {
		writeDefaultObject(s);
	}

	@Override
	public void writePathSystem(PathSystem p) throws Exception {
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

	public HTMLOutputWriter(Object value, File file) throws IOException {
		this(value, file, null);
	}

	public HTMLOutputWriter(Object value, File file, Graph dataGraph)
			throws IOException {
		this(value, file, dataGraph, true);
	}

	public HTMLOutputWriter(Object value, File file, Graph dataGraph,
			boolean createElementLinks) throws IOException {
		try {
			out = new PrintWriter(new FileWriter(file));
			this.dataGraph = dataGraph;
			this.rootValue = value;
			this.createElementLinks = createElementLinks;
			head();
			this.write(value);
			foot();
		} finally {
			out.close();
		}
	}

	@Override
	public void pre() throws IOException {
		out.println("<table><tr><td>");
	}

	@Override
	public void post() throws IOException {
		out.println("</td></tr></table>");
	}

	@Override
	public void inter() throws IOException {
		out.println("</td></tr><tr><td>");
	}

	@Override
	public void writeTuple(Tuple t) throws IOException {
		out.print("<!-- Tuple --> ");
		out.println("<table><tr><td>");
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
	public void writeRecord(Record r) throws IOException {
		out.print("<!-- Record --> ");
		out.println("<table><tr><td>");
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
	public void writeTable(Table<?> table) throws IOException {
		out.print("<!-- Table --> ");
		out.print("<table style=\"align:left;\"><tr><th>");
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
		for (int i = 0; i < table.size(); i++) {
			Object o = table.get(i);
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
	public void writePath(Path p) throws IOException {
		boolean first = true;
		pre();
		PVector<Edge> edges = p.getEdgeTrace();
		PVector<Vertex> vertices = p.getVertexTrace();
		for (int i = 0; i < vertices.size()-1; i++) {
			if (first) {
				first = false;
				write(vertices.get(i));
			} 
			inter();
			write(edges.get(i));
			inter();
			write(vertices.get(i+1));
		}
		post();
	}

	@Override
	public void writeVertex(Vertex vertex) throws IOException {
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
	public void writeEdge(Edge edge) throws IOException {
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
	public void writeInteger(Integer b) throws IOException {
		out.println(b.toString());
	}

	@Override
	public void writeLong(Long b) throws IOException {
		out.println(b.toString());
	}

	@Override
	public void writeDouble(Double b) throws IOException {
		out.println(b.toString());
	}

	@Override
	public void writeString(String b) throws IOException {
		out.println(htmlQuote(b));
	}

	@Override
	public void writeEnum(Enum<?> e) throws IOException {
		String b = e.toString();
		out.println(b);
	}

	@Override
	public void writeGraph(Graph gr) throws IOException {
		if (createElementLinks) {
			out.println("<a href=\"g" + gr.getId() + "\">" + gr.getId() + ": "
					+ gr.getAttributedElementClass().getUniqueName() + "</a>");
		} else {
			out.println(gr.getId() + ": "
					+ gr.getAttributedElementClass().getUniqueName());
		}
	}

	@Override
	public void writeBoolean(Boolean v) throws IOException {
		if (v != null) {
			out.println(v.toString());
		} else {
			out.println("null");
		}
	}

	@Override
	public void writeDefaultObject(Object o) throws IOException {
		out.println("<pre>");
		out.println(htmlQuote(o.toString()));
		out.println("</pre>");
	}

	@Override
	public void writeAttributedElementClass(AttributedElementClass c)
			throws IOException {
		out.println(c.getQualifiedName());
	}

	@Override
	public void writePVector(PVector<?> b) throws Exception {
		out.print("<!-- PVector --> ");
		super.writePVector(b);
	}

	@Override
	public void writePSet(PSet<?> s) throws Exception {
		out.print("<!-- PSet --> ");
		super.writePSet(s);
	}

	@Override
	public void writePMap(PMap<?, ?> b) throws Exception {
		out.print("<!-- PMap --> ");
		out.print("<table>");
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
	public void head() throws IOException {
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<html>");
		out.println("<head>\n");
		out.println("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">");
		out.println("<style type=\"text/css\">\n"
				+ "table { border: thin gray solid; border-collapse: collapse; border-spacing: 2px }\n"
				+ "td { border: thin gray solid; border-collapse: collapse; border-spacing: 2px }\n"
				+ "th { border: thin gray solid; border-collapse: collapse; border-spacing: 2px }\n"
				+ "</style>\n");
		out.println("</head><body>");

		out.print("<table>");
		if (dataGraph != null) {
			out.print("<tr><td>Graph id: </td><td>" + dataGraph.getId()
					+ "</td></tr>");
		}
		out.print("<tr><td>Result size: </td><td>");
		if (rootValue instanceof PCollection) {
			out.println(Integer.toString(((PCollection<?>) rootValue).size()));
		} else {
			out.println("1");
		}
		out.println("</td></tr></table>");
		out.println("<hr/>");

	}

	@Override
	public void writeUndefined() throws Exception {
		out.print("&not;");
	}

	@Override
	public void foot() throws IOException {
		out.println("</body></html>");
	}
}
