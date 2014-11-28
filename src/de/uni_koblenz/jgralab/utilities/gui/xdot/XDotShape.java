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
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import de.uni_koblenz.jgralab.AttributedElement;

public class XDotShape {
	private final Shape shape;
	private Color lineColor;
	private Color fillColor;
	private Color textColor;
	private Stroke stroke;
	private AttributedElement<?, ?> element;

	public XDotShape(Shape s, Color l, Color f, Color t, Stroke st) {
		shape = s;
		lineColor = l;
		fillColor = f;
		textColor = t;
		stroke = st;
	}

	public AttributedElement<?, ?> getElement() {
		return element;
	}

	public void setElement(AttributedElement<?, ?> element) {
		this.element = element;
	}

	public Shape getShape() {
		return shape;
	}

	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}

	public Color getLineColor() {
		return lineColor;
	}

	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}

	public Color getFillColor() {
		return fillColor;
	}

	public void setTextColor(Color textColor) {
		this.textColor = textColor;
	}

	public Color getTextColor() {
		return textColor;
	}

	public Stroke getStroke() {
		return stroke;
	}

	public void setStroke(Stroke stroke) {
		this.stroke = stroke;
	}

	public void draw(Graphics2D g2) {
		if (stroke != null) {
			g2.setStroke(stroke);
		}
		if (fillColor != null) {
			g2.setColor(fillColor);
			g2.fill(shape);
		}
		if (shape instanceof TextShape && textColor != null) {
			TextShape ts = (TextShape) shape;
			ts.justify(g2);
			// g2.draw(ts);
			AffineTransform a = g2.getTransform();
			g2.scale(1.0, -1.0);
			g2.setFont(ts.getFont());
			g2.setColor(textColor);
			g2.drawString(ts.getText(), (float) ts.x, (float) -ts.y);
			g2.setTransform(a);
		}
		if (lineColor != null) {
			g2.setColor(lineColor);
			g2.draw(shape);
		}

		// g2.setColor(Color.MAGENTA);
		// Rectangle r = shape.getBounds();
		// g2.drawRect(r.x, r.y, r.width, r.height);
	}
}
