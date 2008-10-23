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

package de.uni_koblenz.jgralab.greql2.jvalue;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.exception.JValueVisitorException;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class JValueHTMLOutputVisitor extends JValueDefaultVisitor {

	/**
	 * The writer which stores the elements
	 */
	private BufferedWriter outputWriter;

	/**
	 * The path to the file the value should be stored in
	 */
	private String filePath;

	/**
	 * The graph all elements in the jvalue to visit belong to
	 */
	private Graph dataGraph = null;

	private JValue rootValue;

	private boolean createElementLinks;
	private boolean createBrowsingLinks;

	private void storeln(String s) {
		try {
			outputWriter.write(s);
			outputWriter.write("\n");
		} catch (IOException e) {
			throw new JValueVisitorException("Can't write to output file",
					null, e);
		}
	}

	private void store(String s) {
		try {
			outputWriter.write(s);
		} catch (IOException e) {
			throw new JValueVisitorException("Can't write to output file",
					null, e);
		}
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

	public JValueHTMLOutputVisitor(JValue value, String filePath) {
		this(value, filePath, null);
	}

	public JValueHTMLOutputVisitor(JValue value, String filePath,
			Graph dataGraph) {
		this(value, filePath, dataGraph, true, true);
	}

	public JValueHTMLOutputVisitor(JValue value, String filePath,
			Graph dataGraph, boolean createElementLinks,
			boolean createBrowsingLinks) {
		this.filePath = filePath;
		this.dataGraph = dataGraph;
		this.rootValue = value;
		this.createElementLinks = createElementLinks;
		this.createBrowsingLinks = createBrowsingLinks;
		head();
		value.accept(this);
		foot();
	}

	@Override
	public void pre() {
		storeln("<table><tr><td>");
	}

	@Override
	public void post() {
		storeln("</td></tr></table>");
	}

	@Override
	public void inter() {
		storeln("</td></tr><tr><td>");
	}

	@Override
	public void visitTuple(JValueTuple t) {
		storeln("<table><tr><td>");
		boolean first = true;
		for (JValue val : t) {
			if (first)
				first = false;
			else
				storeln("</td><td>");
			val.accept(this);
		}
		storeln("</td></tr></table>");
	}

	@Override
	public void visitRecord(JValueRecord r) {
		storeln("<table><tr><td>");
		boolean first = true;
		for (Map.Entry<String, JValue> entry : r.entrySet()) {
			if (first)
				first = false;
			else
				storeln("</td><td>");
			storeln(entry.getKey() + ": ");
			entry.getValue().accept(this);
		}
		storeln("</td></tr></table>");
	}

	@Override
	public void visitTable(JValueTable table) {
		store("<table style=\"align:left;\"><tr><th>");
		boolean first = true;
		for (JValue val : table.getHeader()) {
			if (first)
				first = false;
			else
				store("</th><th>");
			val.accept(this);
		}
		storeln("</th></tr>");
		for (JValue row : table.getData()) {
			store("<tr>");
			for (JValue cell : row.toJValueTuple()) {
				store("<td>");
				cell.accept(this);
				store("</td>");
			}
			store("</tr>");
		}
		storeln("</table>");
	}

	@Override
	public void visitVertex(JValue v) {
		Vertex vertex = v.toVertex();
		if (createElementLinks) {
			storeln("<a href=\"v" + vertex.getId() + "\">v" + vertex.getId()
					+ ": " + vertex.getAttributedElementClass().getUniqueName()
					+ "</a>");
		} else {
			storeln("v" + vertex.getId() + ": "
					+ vertex.getAttributedElementClass().getUniqueName());
		}
	}

	@Override
	public void visitEdge(JValue e) {
		Edge edge = e.toEdge();
		if (createElementLinks) {
			storeln("<a href=\"e" + edge.getId() + "\">e" + edge.getId() + ": "
					+ edge.getAttributedElementClass().getUniqueName() + "</a>");
		} else {
			storeln("e" + edge.getId() + ": "
					+ edge.getAttributedElementClass().getUniqueName());
		}
	}

	private void simplePre(JValue n) {
		if (!createBrowsingLinks) {
			return;
		}
		if (n.getBrowsingInfo() instanceof Vertex) {
			Vertex v = (Vertex) n.getBrowsingInfo();
			storeln("<a href=\"v" + v.getId() + "\">");
		}
		if (n.getBrowsingInfo() instanceof Edge) {
			Edge e = (Edge) n.getBrowsingInfo();
			storeln("<a href=\"e" + e.getId() + "\">");
		}
		if (n.getBrowsingInfo() instanceof Graph) {
			Graph g = (Graph) n.getBrowsingInfo();
			storeln("<a href=\"g" + g.getId() + "\">");
		}
	}

	private void simplePost(JValue n) {
		if (!createBrowsingLinks) {
			return;
		}
		if ((n.getBrowsingInfo() instanceof Vertex)
				|| (n.getBrowsingInfo() instanceof Edge)
				|| (n.getBrowsingInfo() instanceof Graph)) {
			storeln("</a>");
		}
	}

	@Override
	public void visitInt(JValue n) {
		simplePre(n);
		Integer b = n.toInteger();
		storeln(b.toString());
		simplePost(n);
	}

	@Override
	public void visitLong(JValue n) {
		simplePre(n);
		Long b = n.toLong();
		storeln(b.toString());
		simplePost(n);
	}

	@Override
	public void visitDouble(JValue n) {
		simplePre(n);
		Double b = n.toDouble();
		storeln(b.toString());
		simplePost(n);
	}

	@Override
	public void visitChar(JValue c) {
		simplePre(c);
		Character b = c.toCharacter();
		storeln(htmlQuote(b.toString()));
		simplePost(c);
	}

	@Override
	public void visitString(JValue s) {
		simplePre(s);
		String b = s.toString();
		storeln(htmlQuote(b));
		simplePost(s);
	}

	@Override
	public void visitEnumValue(JValue e) {
		simplePre(e);
		String b = e.toString();
		storeln(b);
		simplePost(e);
	}

	@Override
	public void visitGraph(JValue g) {
		Graph gr = g.toGraph();
		if (createElementLinks) {
			storeln("<a href=\"g" + gr.getId() + "\">" + gr.getId() + ": "
					+ gr.getAttributedElementClass().getUniqueName() + "</a>");
		} else {
			storeln(gr.getId() + ": "
					+ gr.getAttributedElementClass().getUniqueName());
		}
	}

	public void visitInvalid(JValue i) {
		store("[invalid value]");
	}

	@Override
	public void visitBoolean(JValue b) {
		simplePre(b);
		Boolean v = b.toBoolean();
		storeln(v.toString());
		simplePost(b);
	}

	@Override
	public void visitObject(JValue o) {
		simplePre(o);
		String b = o.toString();
		storeln(b.toString());
		simplePost(o);
	}

	@Override
	public void visitAttributedElementClass(JValue a) {
		simplePre(a);
		AttributedElementClass c = a.toAttributedElementClass();
		storeln(c.getQualifiedName());
		simplePost(a);
	}

	@Override
	public void head() {
		try {
			outputWriter = new BufferedWriter(new FileWriter(filePath));
		} catch (IOException e) {
			throw new JValueVisitorException("Can't create output file", null,
					e);
		}
		storeln("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		storeln("<html>");
		storeln("<head>\n");
		storeln("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">");
		storeln("<style type=\"text/css\">\n"
				+ "table { border: thin gray solid; border-collapse: collapse; border-spacing: 2px }\n"
				+ "td { border: thin gray solid; border-collapse: collapse; border-spacing: 2px }\n"
				+ "th { border: thin gray solid; border-collapse: collapse; border-spacing: 2px }\n"
				+ "</style>\n");
		storeln("</head><body><table>");
		if (dataGraph != null) {
			storeln("<tr><td>Graph id: </td><td>" + dataGraph.getId()
					+ "</td></tr>");
		}
		storeln("<tr><td>Result size: </td><td>");
		if (rootValue.isCollection()) {
			storeln(Integer.toString(rootValue.toCollection().size()));
		} else {
			storeln("1");
		}
		storeln("</td></tr></table>\n<br/><br/>\n");
	}

	@Override
	public void foot() {
		storeln("</body></html>");
		try {
			outputWriter.close();
		} catch (IOException e) {
			throw new JValueVisitorException("Can't close file", null, e);
		}
	}

}
