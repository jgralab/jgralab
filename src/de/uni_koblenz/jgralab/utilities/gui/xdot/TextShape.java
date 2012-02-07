package de.uni_koblenz.jgralab.utilities.gui.xdot;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public class TextShape extends Rectangle2D.Double {
	private static final long serialVersionUID = -582551581120693871L;

	private Font font;
	private String text;
	private int justify;
	private boolean justified;

	TextShape(double x, double y, int j, double w, String t, Font f) {
		// super(j == -1 ? x : j == 0 ? x - w / 2 : x - w, y, w, f.getSize2D());
		super(x, y, w, f.getSize2D());
		justify = j;
		font = f;
		text = t;
	}

	public Font getFont() {
		return font;
	}

	public String getText() {
		return text;
	}

	public void justify(Graphics2D g2) {
		if (justified) {
			return;
		}
		if (justify >= 0) {
			Rectangle2D bounds = font.getStringBounds(text,
					g2.getFontRenderContext());
			if (justify == 0) {
				x -= bounds.getWidth() / 2;
			} else {
				x -= bounds.getWidth();
			}
			width = bounds.getWidth();
		}
		justified = true;
	}
}
