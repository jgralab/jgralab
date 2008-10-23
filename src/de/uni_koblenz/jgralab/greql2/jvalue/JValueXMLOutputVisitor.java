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

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.BooleanGraphMarker;
import de.uni_koblenz.jgralab.Edge;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.Vertex;
import de.uni_koblenz.jgralab.greql2.exception.JValueVisitorException;
import de.uni_koblenz.jgralab.schema.AttributedElementClass;

public class JValueXMLOutputVisitor extends JValueDefaultVisitor {

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

	private String xmlQuote(String string) {
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

	private void storeBrowsingInfo(JValue n) {
		AttributedElement browsingInfo = n.getBrowsingInfo();
		if (browsingInfo instanceof Vertex) {
			Vertex browsingVertex = (Vertex) browsingInfo;
			store("<browsevertex>" + browsingVertex.getId() + "</browsevertex>");
		} else if (browsingInfo instanceof Edge) {
			Edge browsingEdge = (Edge) browsingInfo;
			store("<browseedge>" + browsingEdge.getId() + "</browseedge>");
		}
	}

	public JValueXMLOutputVisitor(JValue value, String filePath) {
		this(value, filePath, null);
	}

	public JValueXMLOutputVisitor(JValue value, String filePath, Graph dataGraph) {
		this.filePath = filePath;
		this.dataGraph = dataGraph;
		head();
		value.accept(this);
		foot();
	}

	@Override
	public void visitSet(JValueSet set) {
		storeln("<set>");
		super.visitSet(set);
		storeln("</set>");
	}

	@Override
	public void visitBag(JValueBag bag) {
		storeln("<bag>");
		super.visitBag(bag);
		storeln("</bag>");
	}

	@Override
	public void visitList(JValueList list) {
		storeln("<list>");
		super.visitList(list);
		storeln("</list>");
	}

	@Override
	public void visitTuple(JValueTuple tuple) {
		storeln("<tuple>");
		super.visitTuple(tuple);
		storeln("</tuple>");
	}

	@Override
	public void visitRecord(JValueRecord record) {
		storeln("<record>");
		super.visitRecord(record);
		storeln("</record>");
	}

	@Override
	public void visitTable(JValueTable table) {
		storeln("<table>");
		storeln("<header>");
		super.visitTuple(table.getHeader());
		storeln("</header>");
		storeln("<tabledata>");
		table.getData().accept(this);
		storeln("</tabledata>");
		storeln("</table>");
	}

	@Override
	public void visitVertex(JValue v) {
		Vertex vertex = null;
		vertex = v.toVertex();
		storeln("<vertex>");
		store("<value>" + vertex.getId() + "</value>");
		storeBrowsingInfo(v);
		storeln("</vertex>");
	}

	@Override
	public void visitEdge(JValue e) {
		Edge edge = null;
		edge = e.toEdge();
		store("<edge>");
		store("<value>" + edge.getId() + "</value>");
		storeBrowsingInfo(e);
		storeln("</edge>");
	}

	@Override
	public void visitInt(JValue n) {
		Integer b = n.toInteger();
		store("<integer>");
		store("<value>" + b.intValue() + "</value>");
		storeBrowsingInfo(n);
		storeln("</integer>");
	}

	@Override
	public void visitLong(JValue n) {
		Long b = n.toLong();
		store("<long>");
		store("<value>" + b.longValue() + "</value>");
		storeBrowsingInfo(n);
		storeln("</long>");
	}

	@Override
	public void visitDouble(JValue n) {
		Double b = n.toDouble();
		store("<double>");
		store("<value>" + b.doubleValue() + "</value>");
		storeBrowsingInfo(n);
		storeln("</double>");
	}

	@Override
	public void visitChar(JValue c) {
		Character b = c.toCharacter();
		String s = String.valueOf(b);
		store("<char>");
		store("<value>" + xmlQuote(s) + "</value>");
		storeBrowsingInfo(c);
		storeln("</char>");
	}

	@Override
	public void visitString(JValue s) {
		String st = s.toString();
		store("<string>");
		store("<value>" + xmlQuote(st) + "</value>");
		storeBrowsingInfo(s);
		storeln("</string>");
	}

	@Override
	public void visitEnumValue(JValue e) {
		String st = e.toString();
		store("<enumvalue>");
		store("<value>" + xmlQuote(st) + "</value>");
		storeBrowsingInfo(e);
		storeln("</enumvalue>");
	}

	@Override
	public void visitGraph(JValue g) {
		Graph gr = g.toGraph();
		store("<graph>");
		store("<value>" + gr.getId() + "</value>");
		storeBrowsingInfo(g);
		storeln("</graph>");
	}

	/**
	 * To store a subgraph is very tricky, one possibility is to store all
	 * vertices and edges
	 */
	public void visitSubgraph(JValue s) {
		if (dataGraph == null) {
			throw new JValueVisitorException(
					"Cannot write a Subgraph to xml if no Graph is given", s);
		}
		storeln("<subgraph>");
		storeBrowsingInfo(s);
		BooleanGraphMarker subgraph = s.toSubgraphTempAttribute();
		Vertex firstVertex = dataGraph.getFirstVertex();
		Vertex currentVertex = firstVertex;
		do {
			if ((subgraph == null) || (subgraph.isMarked(currentVertex)))
				storeln("<vertex>" + currentVertex.getId() + "</vertex>");
			currentVertex = currentVertex.getNextVertex();
		} while (firstVertex != currentVertex);
		Edge firstEdge = dataGraph.getFirstEdgeInGraph();
		Edge currentEdge = firstEdge;
		do {
			if (subgraph.isMarked(currentEdge))
				storeln("<edge>" + currentEdge.getId() + "</edge>");
		} while (firstEdge != currentEdge);

		storeln("</subgraph>");
	}

	@Override
	public void visitBoolean(JValue b) {
		Boolean bool = b.toBoolean();
		store("<bool>");
		store("<value>" + bool + "</value>");
		storeBrowsingInfo(b);
		storeln("</bool>");
	}

	@Override
	public void visitObject(JValue o) {
		Object t = o.toObject();
		store("<object>");
		store("<value>" + xmlQuote(t.toString()) + "</value>");
		storeBrowsingInfo(o);
		storeln("</object>");
	}

	@Override
	public void visitAttributedElementClass(JValue a) {
		AttributedElementClass c = a.toAttributedElementClass();
		store("<attributedelementclass>");
		store("<value>" + c.getQualifiedName() + "</value>");
		storeBrowsingInfo(a);
		storeln("</attributedelementclass>");
	}

	@Override
	public void head() {
		try {
			outputWriter = new BufferedWriter(new FileWriter(filePath));
		} catch (IOException e) {
			throw new JValueVisitorException("Can't create output file", null,
					e);
		}
		storeln("<xmlvalue>");
	}

	@Override
	public void foot() {
		storeln("</xmlvalue>");
		try {
			outputWriter.close();
		} catch (IOException e) {
			throw new JValueVisitorException("Can't close output file", null, e);
		}
	}

}
