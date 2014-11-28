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
package de.uni_koblenz.ist.utilities.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DrawingPanel extends JComponent {
	private static final long serialVersionUID = 1L;

	private transient RenderingHints antialiasOn;
	private BoundedRangeModel zoomLevelModel;
	private Rectangle2D boundingBox;
	private Dimension preferredSize;

	public BoundedRangeModel getZoomLevelModel() {
		return zoomLevelModel;
	}

	private double scale;
	private double pixelPerUnit;

	public static final int ZOOM_MAX = 160;
	public static final int ZOOM_INIT = 80;
	public static final int ZOOM_MIN = 0;
	public static final int CONTINUOUS_DRAG_DELAY = 20;

	private boolean positiveYAxis;

	// true: drawing is in centered
	// false: origin is top left (posititiveYAxis) or bottom left
	// (!positiveYAxis)
	private boolean centerDrawing;

	public DrawingPanel(boolean centerDrawing, boolean positiveYAxis) {
		this.centerDrawing = centerDrawing;
		this.positiveYAxis = positiveYAxis;
		pixelPerUnit = 1.0;

		// antialiasOff = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
		// RenderingHints.VALUE_ANTIALIAS_OFF);

		antialiasOn = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		antialiasOn.put(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);

		zoomLevelModel = new DefaultBoundedRangeModel(ZOOM_INIT, 0, ZOOM_MIN,
				ZOOM_MAX);
		scale = 1.0;
		zoomLevelModel.setValue(ZOOM_INIT);
		zoomLevelModel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				setScale(zoomToScale(zoomLevelModel.getValue()));
			}
		});
		// addMouseMotionListener(new MouseMotionListener() {
		// public void mouseMoved(MouseEvent e) {
		// }
		//
		// public void mouseDragged(MouseEvent e) {
		// // The user is dragging us, so scroll!
		// Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
		// scrollRectToVisible(r);
		// }
		// });
		setBackground(UIManager.getColor("Panel.background"));
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		Dimension size = getSize();
		Insets i = getInsets();
		g2.setColor(getBackground());
		size.width -= i.left + i.right;
		size.height -= i.top + i.bottom;
		g2.translate(i.left, i.top);
		Rectangle b = new Rectangle(0, 0, size.width, size.height);
		Rectangle r = g2.getClipBounds();
		g2.setClip(r != null ? b.intersection(r) : b);

		g2.fillRect(0, 0, size.width, size.height);
		if (centerDrawing) {
			g2.translate(size.width / 2.0, size.height / 2.0);
		} else {
			g2.translate(0, positiveYAxis ? 0 : size.height);
		}
		g2.scale(scale * pixelPerUnit, (positiveYAxis ? scale : -scale)
				* pixelPerUnit);
		if (centerDrawing) {
			g2.translate(-boundingBox.getWidth() / 2,
					-boundingBox.getHeight() / 2);
		}
		g2.setRenderingHints(antialiasOn);
		paintContent(g2);
	}

	public Point screenToModel(Point p) {
		double x = p.x;
		double y = p.y;
		Insets i = getInsets();

		Dimension size = getSize();
		size.width -= i.left + i.right;
		size.height -= i.top + i.bottom;

		x -= i.top;
		y -= i.top;

		if (centerDrawing) {
			x -= size.width / 2.0;
			y -= size.height / 2.0;
		} else {
			y -= positiveYAxis ? 0 : size.height;
		}

		x /= scale * pixelPerUnit;
		y /= (positiveYAxis ? scale : -scale) * pixelPerUnit;

		if (centerDrawing) {
			x -= -boundingBox.getWidth() / 2;
			y -= -boundingBox.getHeight() / 2;
		}

		return new Point((int) x, (int) y);
	}

	protected void paintContent(Graphics2D g2) {
	}

	public double getPixelPerUnit() {
		return pixelPerUnit;
	}

	public void setPixelPerUnit(double pixelPerUnit) {
		this.pixelPerUnit = pixelPerUnit;
	}

	public Rectangle2D getBoundingBox() {
		return boundingBox;
	}

	public void setBoundingBox(Rectangle2D bbx) {
		boundingBox = bbx;
		calculatePreferredSize();
	}

	private void calculatePreferredSize() {
		if (boundingBox == null) {
			throw new IllegalStateException("Bounding box has not been set");
		}
		preferredSize = new Dimension((int) Math.round(boundingBox.getWidth()
				* scale), (int) Math.round(boundingBox.getHeight() * scale));
		if (isVisible()) {
			revalidate();
			repaint();
		}
	}

	@Override
	public Dimension getPreferredSize() {
		if (boundingBox == null) {
			throw new IllegalStateException("Bounding box has not been set");
		}
		assert preferredSize != null;
		return preferredSize;
	}

	@Override
	public void setPreferredSize(Dimension preferredSize) {
		throw new UnsupportedOperationException(
				"Preferred size is computed from boundingbox and scale");
	}

	public void zoomToFit(Dimension size) {
		if (boundingBox == null) {
			return;
		}
		Insets i = getInsets();
		int w = size.width - i.left - i.right;
		int h = size.height - i.top - i.bottom;
		double sc = Math.min(w / boundingBox.getWidth(),
				h / boundingBox.getHeight());
		zoomLevelModel.setValue(scaleToZoom(sc));
	}

	private double zoomToScale(int z) {
		return Math.pow(10.0, z / 40.0) / 100.0;
	}

	private void setScale(double scale) {
		this.scale = scale;
		calculatePreferredSize();
	}

	private int scaleToZoom(double sc) {
		return Math.max(
				ZOOM_MIN,
				Math.min(ZOOM_MAX,
						(int) (Math.floor(Math.log10(sc * 100.0) * 40.0))));
	}

	public void zoomIn() {
		zoomLevelModel.setValue(zoomLevelModel.getValue() + 10);
	}

	public void zoomOut() {
		zoomLevelModel.setValue(zoomLevelModel.getValue() - 10);
	}
}
