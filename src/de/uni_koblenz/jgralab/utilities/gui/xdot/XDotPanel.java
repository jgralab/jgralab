/*
 * JGraLab - The Java Graph Laboratory
 *
 * Copyright (C) 2006-2013 Institute for Software Technology
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Rectangle2D;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.uni_koblenz.ist.utilities.gui.DrawingPanel;
import de.uni_koblenz.jgralab.AttributedElement;
import de.uni_koblenz.jgralab.Graph;
import de.uni_koblenz.jgralab.graphmarker.GraphMarker;

public class XDotPanel extends DrawingPanel {
	private static final long serialVersionUID = 1L;

	private Graph graph;
	private List<XDotShape> shapes;
	private GraphMarker<List<XDotShape>> elementShapes;
	private GraphMarker<Rectangle> elementBoxes;
	private GraphMarker<Rectangle> elementTexts;
	private Stroke standardStroke;
	private Stroke dashedStroke;
	private Stroke thickStroke;
	private Stroke thickDashedStroke;
	private boolean trackMouseMotion;
	private AttributedElement<?, ?> hoverElement;

	public XDotPanel(Graph g, InputStream xdotInputStream) throws IOException {
		super(true, false);
		setBackground(Color.WHITE);
		setGraph(g, xdotInputStream);
		standardStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
		thickStroke = new BasicStroke(2.5f, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
		float[] dash = new float[] { 5.0f, 5.0f };
		dashedStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND, 1.0f, dash, 0);
		thickDashedStroke = new BasicStroke(2.5f, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND, 1.0f, dash, 0);

		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				if (trackMouseMotion) {
					AttributedElement<?, ?> el = getElementAt(screenToModel(e
							.getPoint()));
					if (el != hoverElement) {
						hoverElement = el;
						fireMouseOver(new ElementSelectionEvent(el, e));
					}
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (trackMouseMotion) {
					AttributedElement<?, ?> el = getElementAt(screenToModel(e
							.getPoint()));
					if (el != hoverElement) {
						hoverElement = el;
						fireMouseOver(new ElementSelectionEvent(el, e));
					}
				}
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				AttributedElement<?, ?> el = getElementAt(screenToModel(e
						.getPoint()));
				fireMousePressed(new ElementSelectionEvent(el, e));
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				AttributedElement<?, ?> el = getElementAt(screenToModel(e
						.getPoint()));
				fireMouseReleased(new ElementSelectionEvent(el, e));
			}
		});

		listenerList = new ArrayList<ElementSelectionListener>();
	}

	public void setGraph(Graph g, InputStream xdotInputStream)
			throws IOException {
		graph = g;
		elementShapes = new GraphMarker<List<XDotShape>>(g);
		XDotParser p = new XDotParser(graph, elementShapes);
		shapes = p.parseXDotFile(new BufferedInputStream(xdotInputStream));
		Rectangle2D bounds = p.getBounds();
		if (bounds != null) {
			setBoundingBox(bounds);
		}
		elementBoxes = null;
	}

	private List<ElementSelectionListener> listenerList;

	private void fireMousePressed(ElementSelectionEvent e) {
		for (ElementSelectionListener l : listenerList) {
			l.mousePressed(e);
		}
	}

	private void fireMouseReleased(ElementSelectionEvent e) {
		for (ElementSelectionListener l : listenerList) {
			l.mouseReleased(e);
		}
	}

	private void fireMouseOver(ElementSelectionEvent e) {
		for (ElementSelectionListener l : listenerList) {
			l.mouseOver(e);
		}
	}

	public void addElementSelectionListener(ElementSelectionListener l) {
		if (!listenerList.contains(l)) {
			listenerList.add(l);
			trackMouseMotion = true;
		}
	}

	public void removeElementSelectionListener(ElementSelectionListener l) {
		listenerList.remove(l);
		if (listenerList.isEmpty()) {
			trackMouseMotion = false;
		}
	}

	private void computeBoundingBoxes() {
		elementBoxes = new GraphMarker<Rectangle>(graph);
		elementTexts = new GraphMarker<Rectangle>(graph);
		for (AttributedElement<?, ?> el : elementShapes.getMarkedElements()) {
			List<XDotShape> l = elementShapes.get(el);
			Rectangle rb = null;
			Rectangle rt = null;
			for (XDotShape s : l) {
				if (s.getShape() instanceof TextShape) {
					if (rt == null) {
						rt = new Rectangle(s.getShape().getBounds());
					} else {
						rt = rt.union(s.getShape().getBounds());
					}
				} else {
					if (rb == null) {
						rb = new Rectangle(s.getShape().getBounds());
					} else {
						rb = rb.union(s.getShape().getBounds());
					}
				}
			}
			elementBoxes.mark(el, rb);
			elementTexts.mark(el, rt);
		}
	}

	@Override
	protected void paintContent(Graphics2D g2) {
		for (XDotShape s : shapes) {
			g2.setStroke(standardStroke);
			s.draw(g2);
		}
		if (elementBoxes == null) {
			computeBoundingBoxes();
		}
	}

	protected AttributedElement<?, ?> getElementAt(Point p) {
		int fm = Integer.MAX_VALUE;

		AttributedElement<?, ?> result = null;
		for (XDotShape xs : shapes) {
			Rectangle r = xs.getShape().getBounds();
			if (r.width * r.height < fm && r.contains(p)) {
				fm = r.width * r.height;
				result = xs.getElement();
			}
		}
		if (elementBoxes != null) {
			for (AttributedElement<?, ?> el : elementBoxes.getMarkedElements()) {
				Rectangle r = elementTexts.get(el);
				if (r != null && r.width * r.height < fm && r.contains(p)) {
					fm = r.width * r.height;
					result = el;
				}
				r = elementBoxes.get(el);
				if (r != null && r.width * r.height < fm && r.contains(p)) {
					fm = r.width * r.height;
					result = el;
				}
			}
		}
		return result;
	}

	public void setElementColors(AttributedElement<?, ?> el, Color lineColor,
			Color fillColor, Color textColor) {
		assert lineColor != null;
		assert fillColor != null;
		assert textColor != null;
		List<XDotShape> l = elementShapes.get(el);
		if (l == null) {
			return;
		}
		for (XDotShape xs : l) {
			if (xs.getLineColor() != null) {
				xs.setLineColor(lineColor);
			}
			if (xs.getFillColor() != null) {
				xs.setFillColor(fillColor);
			}
			if (xs.getTextColor() != null) {
				xs.setTextColor(textColor);
			}
		}
	}

	public void setElementStroke(AttributedElement<?, ?> el, Stroke s) {
		assert s != null;
		List<XDotShape> l = elementShapes.get(el);
		if (l == null) {
			return;
		}
		for (XDotShape xs : l) {
			xs.setStroke(s);
		}
	}

	public Stroke getStandardStroke() {
		return standardStroke;
	}

	public Stroke getDashedStroke() {
		return dashedStroke;
	}

	public Stroke getThickStroke() {
		return thickStroke;
	}

	public Stroke getThickDashedStroke() {
		return thickDashedStroke;
	}

}
