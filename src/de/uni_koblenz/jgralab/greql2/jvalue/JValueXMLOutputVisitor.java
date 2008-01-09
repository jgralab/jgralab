/*
 * JGraLab - The Java graph laboratory
 * (c) 2006-2007 Institute for Software Technology
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

import java.io.*;
import de.uni_koblenz.jgralab.*;
import de.uni_koblenz.jgralab.greql2.exception.*;

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
	
	
//	private boolean store(String s) {
//		try {
//			outputWriter.write(s);
//			return true;
//		} catch (IOException ex) {
//			return false;
//		}
//	}
	
	private boolean storeln(String s) {
		try {
			outputWriter.write(s + "\n");
			return true;
		} catch (IOException ex) {
			return false;
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
			storeln("<browsevertex>" + browsingVertex.getId() + "</browsevertex>");
		} else if (browsingInfo instanceof Edge) {
			Edge browsingEdge = (Edge) browsingInfo;
			storeln("<browseedge>" + browsingEdge.getId() + "</browseedge>");
		}
	}
	

	public JValueXMLOutputVisitor(JValue value, String filePath) throws Exception {
		this(value, filePath, null);
	}
	
	public JValueXMLOutputVisitor(JValue value, String filePath, Graph dataGraph) throws Exception {
		this.filePath = filePath;
		this.dataGraph = dataGraph;
		head();
		value.accept(this);
		foot();
	}
	
	
	public void visitSet(JValueSet set) throws Exception {
		storeln("<set>");
		super.visitSet(set);
		storeln("</set>");
	}
	
	public void visitBag(JValueBag bag) throws Exception {
		storeln("<bag>");
		super.visitBag(bag);
		storeln("</bag>");
	}
	
	public void visitList(JValueList list) throws Exception {
		storeln("<list>");
		super.visitList(list);
		storeln("</list>");
	}
	
	public void visitTuple(JValueTuple tuple) throws Exception {
		storeln("<tuple>");
		super.visitTuple(tuple);
		storeln("</tuple>");
	}
	
	public void visitRecord(JValueRecord record) throws Exception {
		storeln("<record>");
		super.visitRecord(record);
		storeln("</record>");
	}
	
	public void visitTable(JValueTable table) throws Exception {
		storeln("<table>");
		storeln("<header>");
		super.visitTuple(table.getHeader());
		storeln("</header>");
		storeln("<tabledata>");
		table.getData().accept(this);
		storeln("</tabledata>");
		storeln("</table>");
	}
	
	
	
	public void visitPathSystem(JValuePathSystem p) throws Exception {
		//TODO visitPathSystem
	}

	public void visitVertex(JValue v) throws Exception {
		Vertex vertex = null;
		try {
			vertex = v.toVertex();
			storeln("<vertex>");
			storeln("<value>" + vertex.getId() + "</value>");
			storeBrowsingInfo(v);
			storeln("</vertex>");
		} catch (JValueInvalidTypeException ex) {
			return;
			//this may not happed here
		}
	}

	public void visitEdge(JValue e) throws Exception {
		Edge edge = null;
		try {
			edge = e.toEdge();
			storeln("<edge>");
			storeln("<value>" + edge.getId() + "</value>");
			storeBrowsingInfo(e);
			storeln("</edge>");
		} catch (JValueInvalidTypeException ex) {
			return;
			//this may not happed here
		}
	}

	
	public void visitInt(JValue n) throws Exception {
		try {
			Integer b = n.toInteger();
			storeln("<integer>");
			storeln("<value>" + b.intValue() + "</value>");
			storeBrowsingInfo(n);
			storeln("</integer>");
		} catch (JValueInvalidTypeException ex) {
			return;
			//this may not happed here
		}
	}
	
	public void visitLong(JValue n) throws Exception {
		try {
			Long b = n.toLong();
			storeln("<long>");
			storeln("<value>" + b.longValue() + "</value>");
			storeBrowsingInfo(n);
			storeln("</long>");
		} catch (JValueInvalidTypeException ex) {
			return;
			//this may not happed here
		}
	}
	
	public void visitDouble(JValue n) throws Exception {
		try {
			Double b = n.toDouble();
			storeln("<double>");
			storeln("<value>" + b.doubleValue() + "</value>");
			storeBrowsingInfo(n);
			storeln("</double>");
		} catch (JValueInvalidTypeException ex) {
			return;
			//this may not happed here
		}
	}

	public void visitChar(JValue c) throws Exception {
		try {
			Character b = c.toCharacter();
			String s = String.valueOf(b);
			storeln("<char>");
			storeln("<value>" + xmlQuote(s) + "</value>");
			storeBrowsingInfo(c);
			storeln("</char>");
		} catch (JValueInvalidTypeException ex) {
			return;
			//this may not happed here
		}
	}

	public void visitString(JValue s) throws Exception {
			String st = s.toString();
			storeln("<string>");
			storeln("<value>" + xmlQuote(st) + "</value>");
			storeBrowsingInfo(s);
			storeln("</string>");
	}
	
	public void visitEnumValue(JValue e) throws Exception {
		String st = e.toString();
		storeln("<enumvalue>");
		storeln("<value>" + xmlQuote(st) + "</value>");
		storeBrowsingInfo(e);
		storeln("</enumvalue>");
}

	public void visitGraph(JValue g) throws Exception {
		try {
			Graph gr = g.toGraph();
			storeln("<graph>");
			storeln("<value>" + gr.getId() + "</value>");
			storeBrowsingInfo(g);
			storeln("</graph>");
		} catch (JValueInvalidTypeException ex) {
			return;
			//this may not happed here
		}
	}

	/**
	 * To store a subgraph is very tricky, one possibility is to store all vertices and edges
	 */
	public void visitSubgraph(JValue s) throws Exception {
		if (dataGraph == null)
			throw new JValueVisitorException("Cannot write a Subgraph to xml if no Graph is given", s);
		storeln("<subgraph>");
		storeBrowsingInfo(s);
		BooleanGraphMarker subgraph = s.toSubgraphTempAttribute();
		Vertex firstVertex = dataGraph.getFirstVertex();
		Vertex currentVertex = firstVertex;
		do {
			if ((subgraph==null) || (subgraph.isMarked(currentVertex)))
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

	public void visitDFA(JValue d) throws Exception {
		throw new JValueVisitorException("Cannot write a DFA to xml", d);
	}

	public void visitNFA(JValue n) throws Exception {
		throw new JValueVisitorException("Cannot write a NFA to xml", n);
	}

	public void visitInvalid(JValue i) throws Exception {

	}

	public void visitBoolean(JValue b) throws Exception {
		try {
			Boolean bool = b.toBoolean();
			storeln("<bool>");
			storeln("<value>" + bool + "</value>");
			storeBrowsingInfo(b);
			storeln("</bool>");
		} catch (JValueInvalidTypeException ex) {
			return;
			//this may not happed here
		}
	}

	public void visitObject(JValue o) throws Exception {
		try {
			Object t = o.toObject();
			storeln("<object>");
			storeln("<value>" + xmlQuote(t.toString()) + "</value>");
			storeBrowsingInfo(o);
			storeln("</object>");
		} catch (JValueInvalidTypeException ex) {
			return;
			//this may not happed here
		}
	}

	public void visitAttributedElementClass(JValue a) throws Exception {
		try {
			AttributedElementClass c = a.toAttributedElementClass();
			storeln("<attributedelementclass>");
			storeln("<value>" + c.getName() + "</value>");
			storeBrowsingInfo(a);
			storeln("</attributedelementclass>");
		} catch (JValueInvalidTypeException ex) {
			return;
			//this may not happed here
		}
	}

	public void visitState(JValue s) throws Exception {
		throw new JValueVisitorException("Cannot write a state to xml", s);

	}

	public void visitTransition(JValue t) throws Exception {
		throw new JValueVisitorException("Cannot write a transition to xml", t);
	}

	public void visitDeclaration(JValue d) throws Exception  {
		throw new JValueVisitorException("Cannot write a Declaration to xml", d);
	}

	public void visitDeclarationLayer(JValue d) throws Exception  {
		throw new JValueVisitorException("Cannot write a DeclarationLayer to xml", d);
	}
	

	public void head() throws Exception {
		outputWriter = new BufferedWriter(new FileWriter(filePath));
		storeln("<xmlvalue>");
	}
	

	public void foot() throws Exception {
		storeln("</xmlvalue>");
		outputWriter.close();
	}
	
}
