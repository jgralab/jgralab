/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2014 Institute for Software Technology
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
package de.uni_koblenz.jgralab.utilities.gui.xdot;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
import de.uni_koblenz.jgralab.utilities.gui.xdot.XDotLexer.Token;
import de.uni_koblenz.jgralab.utilities.gui.xdot.XDotLexer.Type;

public class XDotParser {

	private Color lineColor = Color.black;
	private Color fillColor = Color.black;
	private HashMap<String, Color> colorMap;
	private Font font = null;
	private Graph graph;
	private AttributedElement<?, ?> currentElement;
	private List<XDotShape> shapes;
	private GraphMarker<List<XDotShape>> elementShapes;
	private Rectangle2D bounds;
	private XDotLexer xdl;
	private Token la;
	private int nestingDepth;

	public XDotParser(Graph g, GraphMarker<List<XDotShape>> es) {
		graph = g;
		elementShapes = es;
		colorMap = new HashMap<>();
		colorMap.put("black", Color.BLACK);
		colorMap.put("white", Color.WHITE);
		colorMap.put("red", Color.RED);
		colorMap.put("green", Color.GREEN);
		colorMap.put("blue", Color.BLUE);
		colorMap.put("yellow", Color.YELLOW);
		colorMap.put("cyan", Color.CYAN);
		colorMap.put("magenta", Color.MAGENTA);
		colorMap.put("gray", Color.GRAY);
	}

	final class DrawActionLexer {
		final String s;
		final char[] c;
		int p;

		DrawActionLexer(String s) {
			this.s = s;
			c = s.toCharArray();
			p = 0;
			consumeWhitespace();
		}

		final void consumeWhitespace() {
			while (p < c.length && Character.isWhitespace(c[p])) {
				++p;
			}
		}

		final char nextChar() {
			if (p >= c.length) {
				return 0;
			}
			char result = c[p++];
			consumeWhitespace();
			return result;
		}

		final int nextInt() {
			if (p >= c.length) {
				throw new NoSuchElementException();
			}
			int b = p;
			while (p < c.length && !Character.isWhitespace(c[p])) {
				++p;
			}
			int result = Integer.parseInt(s.substring(b, p));
			consumeWhitespace();
			return result;
		}

		final double nextDouble() {
			if (p >= c.length) {
				throw new NoSuchElementException();
			}
			int b = p;
			while (p < c.length && !Character.isWhitespace(c[p])) {
				++p;
			}
			double result = Double.parseDouble(s.substring(b, p));
			consumeWhitespace();
			return result;
		}

		final String nextString() {
			if (p >= c.length) {
				throw new NoSuchElementException();
			}
			int l = nextInt();
			consumeWhitespace();
			if (p >= c.length) {
				throw new NoSuchElementException();
			}
			if (c[p] != '-') {
				throw new IllegalArgumentException();
			}
			++p;
			String result = s.substring(p, p + l);
			p += l;
			consumeWhitespace();
			return result;
		}
	}

	private void parseDrawActions(String s) {
		DrawActionLexer l = new DrawActionLexer(s);
		for (char action = l.nextChar(); action != 0; action = l.nextChar()) {
			switch (action) {
			case 'c':
			case 'C': {
				// Color
				String color = l.nextString();
				Color c = colorMap.get(color);
				if (c == null) {
					if (color.charAt(0) == '#') {

						int r = Integer.parseInt(color.substring(1, 3), 16);
						int g = Integer.parseInt(color.substring(3, 5), 16);
						int b = Integer.parseInt(color.substring(5, 7), 16);
						if (color.length() == 9) {
							int a = Integer.parseInt(color.substring(7, 9), 16);
							c = new Color(r, g, b, a);
						} else {
							c = new Color(r, g, b);
						}
					} else {
						c = Color.black;
					}
					colorMap.put(color, c);
				}
				if (action == 'c') {
					lineColor = c;
				} else {
					fillColor = c;
				}
			}
				break;

			case 'p':
			case 'P':
			case 'L':
			case 'b':
			case 'B': {
				// Polygon, Polyline, B-Spline
				int n = l.nextInt();
				double[] xp = new double[n];
				double[] yp = new double[n];
				for (int i = 0; i < n; ++i) {
					xp[i] = l.nextDouble();
					yp[i] = l.nextDouble();
				}
				XDotShape xs;
				if (action == 'p' || action == 'P' || action == 'L') {
					Path2D p = new Path2D.Double();
					for (int i = 0; i < n - 1; i++) {
						p.append(new Line2D.Double(xp[i], yp[i], xp[i + 1],
								yp[i + 1]), i != 0);
					}
					if (action != 'L') {
						p.closePath();
					}
					xs = new XDotShape(p, lineColor, action == 'P' ? fillColor
							: null, null, null);
				} else {
					Path2D p = new Path2D.Double();
					for (int i = 0; i < n - 1; i += 3) {
						p.append(new CubicCurve2D.Double(xp[i], yp[i],
								xp[i + 1], yp[i + 1], xp[i + 2], yp[i + 2],
								xp[i + 3], yp[i + 3]), i != 0);
					}
					xs = new XDotShape(p, lineColor, action == 'b' ? fillColor
							: null, null, null);
				}
				shapes.add(xs);
				addShapeToCurrentElement(xs);
			}
				break;

			case 'e':
			case 'E': {
				// Ellipse
				double x = l.nextDouble();
				double y = l.nextDouble();
				double w = l.nextDouble();
				double h = l.nextDouble();
				Ellipse2D e = new Ellipse2D.Double(x - w, y - h, w * 2, h * 2);
				XDotShape xs = new XDotShape(e, lineColor,
						action == 'E' ? fillColor : null, null, null);
				shapes.add(xs);
				addShapeToCurrentElement(xs);
			}
				break;

			case 'F': {
				// Font
				double size = l.nextDouble();
				String name = l.nextString();
				font = new Font(name, Font.PLAIN, (int) size);
			}
				break;

			case 'T': {
				// Text
				double x = l.nextDouble();
				double y = l.nextDouble();
				int j = l.nextInt(); // -1=left, 0=centered, 1=right
				double w = l.nextDouble();
				String text = l.nextString();
				TextShape t = new TextShape(x, y, j, w, text, font);
				XDotShape xs = new XDotShape(t, null, null, lineColor, null);
				shapes.add(xs);
				addShapeToCurrentElement(xs);
			}
				break;

			case 'S': {
				// TODO: Style attribute
				@SuppressWarnings("unused")
				String style = l.nextString();
			}
				break;

			default:
				throw new RuntimeException(xdl.getLine()
						+ ": FIXME Unknown action '" + action + "'");
			}
		}
	}

	public void addShapeToCurrentElement(XDotShape xs) {
		if (currentElement == null) {
			return;
		}
		xs.setElement(currentElement);
		List<XDotShape> l = elementShapes.get(currentElement);
		if (l == null) {
			l = new ArrayList<>();
			elementShapes.mark(currentElement, l);
		}
		l.add(xs);
	}

	private String match() throws IOException {
		if (la.type == Type.EOF) {
			throw new IOException(xdl.getLine() + ": Unexpected EOF");
		}
		String s = la.text;
		la = xdl.nextToken();
		return s;
	}

	private String matchID() throws IOException {
		if (la.type == Type.EOF) {
			throw new IOException("Unexpected EOF");
		}
		if (la.type == Type.SEPARATOR) {
			throw new IOException(xdl.getLine() + ": Expected ID, found "
					+ la.text);
		}
		String s = la.text;
		la = xdl.nextToken();
		return s;
	}

	private String match(String s) throws IOException {
		if (la.type == Type.EOF) {
			throw new IOException(xdl.getLine() + ": Expected " + s
					+ " found EOF");
		}
		if (!s.equals(la.text)) {
			throw new IOException(xdl.getLine() + ": Expected " + s + " found "
					+ la.text);
		}
		la = xdl.nextToken();
		return s;
	}

	private String matchOpt(String s) throws IOException {
		if (s.equals(la.text)) {
			return match();
		}
		return null;
	}

	private void parseDot() throws IOException {
		matchOpt("strict");
		if (la.text.equals("graph") || la.text.equals("digraph")) {
			match();
		} else {
			throw new IOException(xdl.getLine()
					+ ": Expected 'graph' or 'digraph', found " + la.text);
		}
		if (!la.text.equals("{")) {
			matchID();
		}
		match("{");
		while (!la.text.equals("}")) {
			parseStatement();
		}
		match("}");
	}

	private void parseSubgraph() throws IOException {
		matchOpt("subgraph");
		if (!la.text.equals("{")) {
			String id = matchID();
			if (id.startsWith("cluster_")) {
				setCurrentElement(id.substring(8));
			}
		}
		++nestingDepth;
		match("{");
		while (!la.text.equals("}")) {
			parseStatement();
		}
		match("}");
		--nestingDepth;
	}

	private void parseStatement() throws IOException {
		if (la.text.equals("{") || la.text.equals("subgraph")) {
			// stmt ::= subgraph
			parseSubgraph();
		} else {
			String id = matchID();
			Map<String, String> attrs = new TreeMap<>();
			if (la.text.equals("=")) {
				// here, we have an assignment
				// stmt ::= ID = ID
				match(); // =
				matchID(); // ID
			} else {
				// here, we have a node or an edge
				while (la.text.equals("--") || la.text.equals("->")) {
					// egdeRHS ::= edgeop (node_id|subgraph) [ edgeRHS ]
					match(); // edgeop
					if (la.text.equals("{") || la.text.equals("subgraph")) {
						// target is subgraph
						parseSubgraph();
					} else {
						// target is node
						matchID();
					}
				}
				// attr_list
				while (la.text.equals("[")) {
					match();
					while (!la.text.equals("]")) {
						String name = matchID();
						match("=");
						String value = matchID();
						attrs.put(name, value);
						matchOpt(",");
					}
					match("]");
				}
				if (nestingDepth == 0 && id.equals("graph")) {
					String bb = attrs.get("bb");
					if (bb != null) {
						String[] s = bb.split(",");
						double x0 = Double.parseDouble(s[0]);
						double y0 = Double.parseDouble(s[1]);
						double x1 = Double.parseDouble(s[2]);
						double y1 = Double.parseDouble(s[3]);
						bounds = new Rectangle2D.Double(x0, y0, x1, y1);
					}
				}
				setCurrentElement(attrs.get("id"));
				for (String name : attrs.keySet()) {
					if (name.matches("_(l|h|t|hl|tl)?draw_")) {
						parseDrawActions(attrs.get(name));
					}
				}
			}
		}
		matchOpt(";");
	}

	public List<XDotShape> parseXDotFile(InputStream is) throws IOException {

		// System.out.println("----- XDOT FILE -----");
		shapes = new ArrayList<>();
		elementShapes.clear();
		bounds = null;
		try {
			xdl = new XDotLexer(is);
			la = xdl.nextToken();
			parseDot();
		} finally {
			is.close();
		}
		// System.out.println("----- END -----");
		return shapes;
	}

	private void setCurrentElement(String id) throws IOException {
		if (id == null) {
			return;
		}
		if (id.charAt(0) == 'v') {
			currentElement = graph.getVertex(Integer.parseInt(id.substring(1)));
		} else if (id.charAt(0) == 'e') {
			currentElement = graph.getEdge(Integer.parseInt(id.substring(1)));
		} else {
			throw new IOException(xdl.getLine() + ": Unexpected element id "
					+ id);
		}
	}

	public Rectangle2D getBounds() {
		return bounds;
	}
}
