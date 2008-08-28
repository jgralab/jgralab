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
	
	

	private boolean storeln(String s) {
		try {
			outputWriter.write(s + "\n");
			return true;
		} catch (IOException ex) {
			return false;
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

	public JValueHTMLOutputVisitor(JValue value, String filePath) throws Exception {
		this(value, filePath, null);
	}
	
	public JValueHTMLOutputVisitor(JValue value, String filePath, Graph dataGraph) throws Exception {
		this.filePath = filePath;
		this.dataGraph = dataGraph;
		this.rootValue = value;
		head();
		value.accept(this);
		foot();
	}
	
	
	public void pre() {
		storeln("<table><tr><td>");
	}
	
	public void post() {
		storeln("</td></tr></table>");
	}
	
	public void inter() {
		storeln("</td></tr><tr><td>");
	}
	
	public void visitTuple(JValueTuple t) throws Exception {
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
	
	public void visitRecord(JValueRecord r) throws Exception {
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

	
	public void visitTable(JValueTable table) throws Exception {
		storeln("<table style=\"align:left;\"><tr><th>");
		boolean first = true;
		for (JValue val : table.getHeader()) {
			if (first)
				first = false;
			else
				storeln("</th><th>");
			val.accept(this);
		}
		storeln("</th></tr>");
		
		for (JValue row : table.getData()) {
			storeln("<tr>");
			for (JValue cell : row.toJValueTuple()) {
				storeln("<th>");
				cell.accept(this);
				storeln("</th>");
			}
			storeln("</tr>");
		}
		
		storeln("</table>");
	}
	
	
	
	public void visitPathSystem(JValuePathSystem p) throws Exception {
		//TODO visitPathSystem
	}
	
	public void visitSlice(JValueSlice s) throws Exception {
		// TODO visitSlice
	}

	public void visitVertex(JValue v) throws Exception {
		Vertex vertex = v.toVertex();
		storeln("<a href=\"v" + vertex.getId() + "\">");
		storeln("v" + vertex.getId() + ": " + vertex.getAttributedElementClass().getUniqueName());
		storeln("</a>");
	}

	public void visitEdge(JValue e) throws Exception {
		Edge edge = e.toEdge();
		storeln("<a href=\"e" + edge.getId() + "\">");
		storeln("e" + edge.getId() + ": " + edge.getAttributedElementClass().getUniqueName());
		storeln("</a>");
	}

	
	private void simplePre(JValue n) {
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
		if ((n.getBrowsingInfo() instanceof Vertex) ||
		    (n.getBrowsingInfo() instanceof Edge)   ||
		    (n.getBrowsingInfo() instanceof Graph)) {
			storeln("</a>");
		}
	}
	
	
	public void visitInt(JValue n) throws Exception {
		simplePre(n);
		Integer b = n.toInteger();
		storeln(b.toString());
		simplePost(n);
	}
	
	public void visitLong(JValue n) throws Exception {
		simplePre(n);
		Long b = n.toLong();
		storeln(b.toString());
		simplePost(n);
	}
	
	public void visitDouble(JValue n) throws Exception {
		simplePre(n);
		Double b = n.toDouble();
		storeln(b.toString());
		simplePost(n);
	}

	public void visitChar(JValue c) throws Exception {
		simplePre(c);
		Character b = c.toCharacter();
		storeln(htmlQuote(b.toString()));
		simplePost(c);
	}

	public void visitString(JValue s) throws Exception {
		simplePre(s);
		String b = s.toString();
		storeln(htmlQuote(b));
		simplePost(s);
	}
	
	public void visitEnumValue(JValue e) throws Exception {
		simplePre(e);
		String b = e.toString();
		storeln(b);
		simplePost(e);
	}

	public void visitGraph(JValue g) throws Exception {
		Graph gr = g.toGraph();
		storeln("<a href=\"e" + gr.getId() + "\">");
		storeln(gr.getId() + ": " + gr.getAttributedElementClass().getUniqueName());
		storeln("</a>");
	}

	/**
	 * To store a subgraph is very tricky, one possibility is to store all vertices and edges
	 */
	public void visitSubgraph(JValue s) throws Exception {
//		if (dataGraph == null)
//			throw new JValueVisitorException("Cannot write a Subgraph to xml if no Graph is given", s);
//		storeln("<subgraph>");
//		storeBrowsingInfo(s);
//		BooleanGraphMarker subgraph = s.toSubgraphTempAttribute();
//		Vertex firstVertex = dataGraph.getFirstVertex();
//		Vertex currentVertex = firstVertex;
//		do {
//			if ((subgraph==null) || (subgraph.isMarked(currentVertex)))
//				storeln("<vertex>" + currentVertex.getId() + "</vertex>");
//			currentVertex = currentVertex.getNextVertex();
//		} while (firstVertex != currentVertex);
//		Edge firstEdge = dataGraph.getFirstEdgeInGraph();
//		Edge currentEdge = firstEdge;
//		do {
//			if (subgraph.isMarked(currentEdge))
//				storeln("<edge>" + currentEdge.getId() + "</edge>");
//		} while (firstEdge != currentEdge);
//		
//		storeln("</subgraph>");
	}

	public void visitDFA(JValue d) throws Exception {
		throw new JValueVisitorException("Cannot write a DFA to xml", d);
	}

	public void visitNFA(JValue n) throws Exception {
		throw new JValueVisitorException("Cannot write a NFA to xml", n);
	}

	public void visitInvalid(JValue i) throws Exception {

	}

	public void visitBoolean(JValue b) throws Exception {
		simplePre(b);
		Boolean v = b.toBoolean();
		storeln(v.toString());
		storeln("</a>");
	}

	public void visitObject(JValue o) throws Exception {
		simplePre(o);
		String b = o.toString();
		storeln(b.toString());
		storeln("</a>");
	}

	public void visitAttributedElementClass(JValue a) throws Exception {
		simplePre(a);
		AttributedElementClass c = a.toAttributedElementClass();
		storeln(c.getQualifiedName());
		storeln("</a>");
	}
		

	public void visitState(JValue s) throws Exception {
		throw new JValueVisitorException("Cannot write a state to html", s);
	}

	public void visitTransition(JValue t) throws Exception {
		throw new JValueVisitorException("Cannot write a transition to html", t);
	}

	public void visitDeclaration(JValue d) throws Exception  {
		throw new JValueVisitorException("Cannot write a Declaration to html", d);
	}

	public void visitDeclarationLayer(JValue d) throws Exception  {
		throw new JValueVisitorException("Cannot write a DeclarationLayer to html", d);
	}
	

	public void head() throws Exception {
		outputWriter = new BufferedWriter(new FileWriter(filePath));
		storeln("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		storeln("<html>");
		storeln("<head>\n");
		storeln("<meta http-equiv=\"content-type\" content=\"text/html; charset=ISO-8859-1\">");
		storeln("<style type=\"text/css\"> \n" +
				"table {\n" +
				"  border: 1px;\n" +
				"  border-color: #555555;\n" +
				"  border-collapse: collapse;" +
				"}\n" +
				"\n" +
				"td {\n" +
				"  text-align:left;\n" +
				"  border-style: groove;" +
				"  border-color: #505050;" +
				"  border-width: 2px;" +
				"}\n" + 
				"\n" +
				"th {\n" +
				"  text-align:left;\n" +
				"  border-style: groove;" +
				"  border-color: #505050;" +
				"  border-width: 2px;" +
				"}\n" + 
				"</style>\n");
		storeln("</head><body>");
		storeln("<table><tr><th>Graph id: </th><th>" + dataGraph.getId() + "</th></tr>");
		storeln("<tr><th>Result size: </th><th>");
		if (rootValue.isCollection()) {
			storeln(Integer.toString(rootValue.toCollection().size()));
		} else {
			storeln("1");
		}	
		storeln("</th></tr></table>\n\n<br/><br/>\n\n");
	}
	

	public void foot() throws Exception {
		storeln("</body></html>");
		outputWriter.close();
	}
	
}
